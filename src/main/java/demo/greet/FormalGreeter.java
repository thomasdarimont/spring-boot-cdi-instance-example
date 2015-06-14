package demo.greet;

@Formal
@Official
public class FormalGreeter implements Greeter {

	@Override
	public String greet(String name) {
		return "Good day " + name;
	}

}
