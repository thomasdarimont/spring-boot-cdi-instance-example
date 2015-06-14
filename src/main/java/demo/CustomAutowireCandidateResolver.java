package demo;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

public class CustomAutowireCandidateResolver extends ContextAnnotationAutowireCandidateResolver {

	static final boolean IS_CDI_INSTANCE_CLASS_PRESENT = ClassUtils.isPresent("javax.enterprise.inject.Instance", null);

	@Override
	public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {

		if (IS_CDI_INSTANCE_CLASS_PRESENT && !Instance.class.equals(descriptor.getDependencyType())) {
			return super.getLazyResolutionProxyIfNecessary(descriptor, beanName);
		}

		// TODO refactor getLazyResolutionProxyIfNecessary to allow to customize
		// lazy dependency resolution for Instance<T>
		return getInstanceAdapterFor(descriptor);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getInstanceAdapterFor(DependencyDescriptor descriptor) {

		ListableBeanFactory listableBeanFactory = (ListableBeanFactory) getBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) listableBeanFactory;

		// Instance<TargetType>
		Class targetType = descriptor.getResolvableType().getGeneric(0).getRawClass();
		Map<String, Object> beansOfType = listableBeanFactory.getBeansOfType(targetType);

		List<Bean> beansInstances = beansOfType.entrySet().stream() //
				.map(e -> new Bean(e.getValue(), registry.getBeanDefinition(e.getKey()).isPrimary()))//
				.collect(Collectors.toList());

		Annotation[] qualifiers = retainQualifierAnnotations(descriptor.getAnnotations());

		Beans beans = new Beans(targetType, beansInstances);
		return qualifiers.length == 0 ? beans : beans.select(qualifiers);
	}

	private Annotation[] retainQualifierAnnotations(Annotation[] annotations) {
		return Arrays.stream(annotations) //
				.filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)) //
				.toArray(Annotation[]::new);
	}

	static class Beans<T> implements Instance<T> {

		private final List<Bean> beans;
		private final Class<?> type;

		public Beans(Class<?> type, List<Bean> beans) {
			this.type = type;
			this.beans = beans;
		}

		protected List<Bean> getBeans() {
			return beans;
		}

		@Override
		public T get() {
			return (T) findDefaultInstance();
		}

		protected Object findDefaultInstance() {

			List<Bean> beans = getBeans();

			if (beans.size() == 1) {
				return beans.get(0).getInstance();
			}

			Object highestPrioBean = returnPrimaryOrHighestPriorityBean(beans);

			if (highestPrioBean != null) {
				return highestPrioBean;
			}

			// TODO figure out a sane default to use here - maybe throw an
			// exception?
			return beans.get(0).getInstance();
		}

		private Object returnPrimaryOrHighestPriorityBean(List<Bean> beans) {

			long highestPriority = Integer.MIN_VALUE;
			Object highestPrioBean = null;

			for (Bean bean : beans) {

				if (bean.isPrimary()) {
					return bean.getInstance();
				}

				// TODO figure out to retrieve order from BeanDefinition /
				// BeanDeclaration

				Object instance = bean.getInstance();
				Order order = instance.getClass().getAnnotation(Order.class);
				if (order != null) {
					if (order.value() > highestPriority) {
						highestPriority = order.value();
						highestPrioBean = instance;
					}
				}

				Priority priority = instance.getClass().getAnnotation(Priority.class);
				if (priority != null) {
					if (priority.value() > highestPriority) {
						highestPriority = priority.value();
						highestPrioBean = instance;
					}
				}
			}

			return highestPrioBean;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Instance<T> select(Annotation... qualifiers) {
			return select((Class<T>) type, qualifiers);
		}

		@Override
		public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
			return new Beans<U>(subtype, filterBeans(subtype, qualifiers));
		}

		protected List<Bean> filterBeans(Class<?> subtype, Annotation... qualifiers) {

			List<Annotation> requiredQualifiers = Arrays.asList(qualifiers);

			return getBeans().stream() //
					.filter(bean -> subtype.isInstance(bean.getInstance())) //
					.filter(bean -> bean.getAnnotations().containsAll(requiredQualifiers)) //
					.collect(Collectors.toList());
		}

		@Override
		public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {

			// TODO implement (Class<U> subtype, Annotation... qualifiers) via
			// select(TypeLiteral<U> subtype, Annotation... qualifiers)
			return select(subtype.getRawType(), qualifiers);
		}

		@Override
		public Iterator<T> iterator() {
			return getBeans().stream().map(bean -> (T) bean.getInstance()).iterator();
		}

		@Override
		public boolean isUnsatisfied() {
			return getBeans().isEmpty();
		}

		@Override
		public boolean isAmbiguous() {
			return getBeans().size() > 1;
		}

		@Override
		public void destroy(Object bean) {

			if (bean instanceof DisposableBean) {
				try {
					((DisposableBean) bean).destroy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	static class Bean {

		private final boolean primary;
		private final Object instance;
		private final List<Annotation> annotations;

		public Bean(Object instance, boolean primary) {
			this.primary = primary;
			this.instance = instance;
			this.annotations = Arrays.asList(instance.getClass().getAnnotations());
		}

		public Object getInstance() {
			return instance;
		}

		public boolean isPrimary() {
			return primary;
		}

		public List<Annotation> getAnnotations() {
			return annotations;
		}
	}
}
