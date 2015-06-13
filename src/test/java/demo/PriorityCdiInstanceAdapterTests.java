package demo;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import demo.PriorityCdiInstanceAdapterTests.Fuck;
import demo.greet.CountryStyleGreeter;
import demo.greet.DefaultGreeter;
import demo.greet.FormalGreeter;
import demo.greet.Greeter;
import demo.greet.HighestPriorityGreeter;
import demo.greet.PriorityGreeter;
import demo.greet.SimpleGreeter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CdiInstanceExampleApp.class, Fuck.class}, initializers = CustomApplicationContextInitializer.class)
public class PriorityCdiInstanceAdapterTests {

	public static class Fuck {

		@Bean
		public Greeter simpleGreeter() {
			return new SimpleGreeter();
		}

		@Bean
		public Greeter formalGreater() {
			return new FormalGreeter();
		}

		@Bean
		public Greeter prioGreeter() {
			return new PriorityGreeter();
		}

		@Bean
		public Greeter highestPrioGreeter() {
			return new HighestPriorityGreeter();
		}

		// Removed "Default" marker so highest prio bean should win
		// @Primary
		// @Default
		@Bean
		public Greeter defaultGreeter() {
			return new DefaultGreeter();
		}

		@Bean
		public Greeter countryStyleGreeter() {
			return new CountryStyleGreeter();
		}
	}
	
	@Autowired
	private Instance<Greeter> greeters;

	@Autowired
	private ListableBeanFactory beanFactory;

	@Test
	public void returnedDefaultInstanceHasHighestPriority() throws Exception {

		System.out.println(greeters.get());
		assertThat(greeters.get()).isInstanceOf(HighestPriorityGreeter.class);
	}

	@Test
	public void listInstances() throws Exception {

		System.out.println("List instances:");
		greeters.iterator().forEachRemaining(System.out::println);

		assertThat(greeters.iterator()).hasSize(beanFactory.getBeansOfType(Greeter.class).size());
	}
}
