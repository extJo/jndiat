
public class PrintTitle implements TitlePrintStatistics{

	@Override
	public void printPlain(String message, int position) {
		// TODO Auto-generated method stub
		System.out.println("\n["+position+"] "+ message);
	}

	@Override
	public void printColor(String color, String bold, int position, String message, String color2, String bold2) {
		// TODO Auto-generated method stub
		System.out.println("\n" + color + bold + "["+ position +"] " + message + color2 + bold2);

	}

}
