package demo.greet;

@Official
public class SimpleGreeter implements Greeter{

	@Override
	public String greet(String name) {
		return "Hello " + name;
	}

}
