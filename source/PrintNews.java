
public class PrintNews implements NewsPrintStatistics{

	@Override
	public void printPlain(String message) {
		// TODO Auto-generated method stub
		System.out.println(message);
	}

	@Override
	public void printColor(String color1, String message, String color2) {
		// TODO Auto-generated method stub
		System.out.println(color1 + message + color2);
	}

}
