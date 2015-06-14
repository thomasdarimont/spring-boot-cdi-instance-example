package demo;

import static demo.QualifierUtils.qualifier;
import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import demo.SimpleCdiInstanceAdapterTests.Config;
import demo.greet.CountryStyle;
import demo.greet.CountryStyleGreeter;
import demo.greet.DefaultGreeter;
import demo.greet.Formal;
import demo.greet.FormalGreeter;
import demo.greet.Greeter;
import demo.greet.HighestPriorityGreeter;
import demo.greet.Official;
import demo.greet.PriorityGreeter;
import demo.greet.SimpleGreeter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { CdiInstanceExampleApp.class, Config.class }, initializers = CustomApplicationContextInitializer.class)
public class SimpleCdiInstanceAdapterTests {

	public static class Config {

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

		@Primary
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
	public void initializeGreeter() throws Exception {
		assertThat(greeters).isNotNull();
	}

	@Test
	public void returnedDefaultInstanceIsPrimaryOrDefault() throws Exception {
		assertThat(greeters.get()).isInstanceOf(DefaultGreeter.class);
	}

	@Test
	public void returnFormalGreeter() throws Exception {

		Instance<Greeter> formalGreeters = greeters.select(qualifier(Formal.class));
		
		assertThat(formalGreeters.isUnsatisfied()).isFalse();
		assertThat(formalGreeters.isAmbiguous()).isFalse();
		assertThat(formalGreeters.get()).isInstanceOf(FormalGreeter.class);
	}

	@Test
	public void returnCountryStyleGreeter() throws Exception {
		assertThat(greeters.select(qualifier(CountryStyle.class)).get()).isInstanceOf(CountryStyleGreeter.class);
	}
	
	@Test
	public void listInstances() throws Exception {

		System.out.println("List instances:");
		greeters.iterator().forEachRemaining(System.out::println);

		assertThat(greeters.iterator()).hasSize(beanFactory.getBeansOfType(Greeter.class).size());
	}
	
	@Test
	public void ambiguousMatch() throws Exception {

		Instance<Greeter> officalGreeters = greeters.select(qualifier(Official.class));
		
		assertThat(officalGreeters.isUnsatisfied()).isFalse();
		assertThat(officalGreeters.isAmbiguous()).isTrue();
		assertThat(officalGreeters.get()).isInstanceOf(Greeter.class); //first match
	}
	
	@Test
	public void matchMultipleQualifiers() throws Exception {

		Instance<Greeter> officalFormalGreeters = greeters.select(qualifier(Official.class), qualifier(Formal.class));
		
		assertThat(officalFormalGreeters.isUnsatisfied()).isFalse();
		assertThat(officalFormalGreeters.isAmbiguous()).isFalse();
		assertThat(officalFormalGreeters.get()).isInstanceOf(FormalGreeter.class); //first match
	}
}
