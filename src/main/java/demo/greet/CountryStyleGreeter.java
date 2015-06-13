package demo.greet;

@CountryStyle
public class CountryStyleGreeter implements Greeter {

	@Override
	public String greet(String name) {
		return "Howdy " + name;
	}
}
