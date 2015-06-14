package demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
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

		List<Instances.Bean> beans = beansOfType.entrySet().stream() //
				.map(e -> new Instances.Bean(e.getValue(), registry.getBeanDefinition(e.getKey()).isPrimary()))//
				.collect(Collectors.toList());

		return new Instances.BeanInstance(targetType, beans);
	}
}
