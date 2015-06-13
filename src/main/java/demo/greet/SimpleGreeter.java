package demo.greet;

public class SimpleGreeter implements Greeter{

	@Override
	public String greet(String name) {
		return "Hello " + name;
	}

}
