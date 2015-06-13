package demo.greet;

import org.springframework.core.annotation.Order;

@Order(1000)
public class PriorityGreeter implements Greeter {
	
	@Override
	public String greet(String name) {
		return "Hi " + name;
	}
}
