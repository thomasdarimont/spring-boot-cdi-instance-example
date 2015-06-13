package demo.greet;

import org.springframework.core.annotation.Order;

@Order(10000)
public class HighestPriorityGreeter implements Greeter {
	
	@Override
	public String greet(String name) {
		return "Your highness " + name;
	}
}
