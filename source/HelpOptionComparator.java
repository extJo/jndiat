
//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.Comparator;
import org.apache.commons.cli.Option;

public class HelpOptionComparator implements Comparator {
	/**
	 * <p>
	 * Compares its two arguments for order. Returns a negative integer, zero, or a
	 * positive integer as the first argument is less than, equal to, or greater
	 * than the second.
	 * </p>
	 *
	 * @param object1
	 *            The first Option to be compared.
	 * @param object2
	 *            The second Option to be compared.
	 *
	 * @return a negative integer, zero, or a positive integer as the first argument
	 *         is less than, equal to, or greater than the second.	
	 */
	public int compare(Object object1, Object object2) {
		Option option1 = (Option) object1;
		Option option2 = (Option) object2;
		return option1.getOpt().compareToIgnoreCase(option2.getOpt());
	}
}
