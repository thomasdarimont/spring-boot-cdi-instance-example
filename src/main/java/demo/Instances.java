package demo;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.Order;

public class Instances {

	static abstract class AbstractInstance<T> implements Instance<T> {

		private final Class<?> type;

		public AbstractInstance(Class<?> type) {
			this.type = type;
		}

		protected abstract List<Bean> getBeans();

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

	static class BeanInstance extends AbstractInstance<Object> {

		private final List<Bean> beans;

		public BeanInstance(Class<?> type, List<Bean> beans) {
			super(type);
			this.beans = beans;
		}

		@Override
		protected List<Bean> getBeans() {
			return beans;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U extends Object> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
			return (Instance<U>) new BeanInstance(subtype, filterBeans(subtype, qualifiers));
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
