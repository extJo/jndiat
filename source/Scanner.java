

import java.lang.Integer;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

public class Scanner extends T3Connection{

	public static Logger myLogger = Logger.getLogger("JNDIAT");
	ScannerData data = new ScannerData();

	/*Constructor*/
	public Scanner(){
		super("weblogic.jndi.WLInitialContextFactory", false);
		myLogger.fine("Scanner object created");
		this.data.openedPorts = new ArrayList<Integer>();
	}
	
	/* To scan ports of a server
	 * Compute openedPorts */
	public void scan (String ip, String ports, String username, String password){
		data.scan(this, ip, ports, username, password);
	}
	
	/*return opened ports from this.openedPorts*/
	public List<Integer> getOpenedPorts(){
		return this.data.openedPorts;
	}
	
	/*Print opened ports from this.openedPorts*/
	public void printOpenedPorts(){
		if (this.data.openedPorts.isEmpty()) {
			this.printBadNews("No opened port has been found to connect with T3 protocol");
		}
		else {
			this.printGoodNews("You can use the T3 protocol to connect to these ports: "+this.data.openedPorts.toString());
		}
	}
}