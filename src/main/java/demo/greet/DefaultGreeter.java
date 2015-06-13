package demo.greet;

public class DefaultGreeter implements Greeter {

	@Override
	public String greet(String name) {
		return "Huhu " + name;
	}

}
