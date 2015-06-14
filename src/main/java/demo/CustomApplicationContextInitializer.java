package demo;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Custom {@link ApplicationContextInitializer} which registers a
 * {@link CustomAutowireCandidateResolver} to customize auto-wiring.
 * 
 * Note: To use this custom {@code ApplicationContextInitializer} you have to
 * configure it via <code>META-INF/spring.factories</code> with the following
 * content:
 * <p>
 * <code>
 * org.springframework.context.ApplicationContextInitializer=demo.CustomApplicationContextInitializer
 * </code>
 * 
 * You can also enable it for unit tests via the {@code initializers} property
 * of the {@link org.springframework.boot.test.SpringApplicationConfiguration}
 * annotation.
 *
 */
public class CustomApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		// Configure custom CustomAutowireCandidateResolver to handle CDI
		// Instance<T> dependency requests
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
		beanFactory.setAutowireCandidateResolver(new CustomAutowireCandidateResolver());
	}
}
