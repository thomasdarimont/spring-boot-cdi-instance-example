package demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
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

		// TODO refactor getLazyResolutionProxyIfNecessary to allow to customize lazy dependency resolution for Instance<T>
		return getInstanceAdapterFor(descriptor);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getInstanceAdapterFor(DependencyDescriptor descriptor) {
	
		Class type = descriptor.getResolvableType().getGeneric(0).getRawClass();
		ListableBeanFactory listableBeanFactory = (ListableBeanFactory) getBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry)listableBeanFactory;
		
		Map<String, Object> beansOfType = listableBeanFactory.getBeansOfType(type);
		List<Instances.Bean> instances = new ArrayList<>();

		for(Map.Entry<String, Object> entry: beansOfType.entrySet()) {
			
			BeanDefinition definition = registry.getBeanDefinition(entry.getKey());
			Instances.Bean instance = new Instances.Bean(entry.getKey(), definition, entry.getValue());
			
			instances.add(instance);
		}
		
		return new Instances.InstanceAdapter(type, instances);
	}
}
