import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScannerData implements DataScanStretegy {
	public List<Integer> openedPorts;

	public ScannerData() {
	}

	/* (non-Javadoc)
	 * @see DataScanStretegy#scan(Scanner, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void scan (Scanner scanner, String ip, String ports, String username, String password){
		Scanner.myLogger.info("Scanning ports '"+ports+"' of "+ip+" with password '"+username+"' and password '"+password+"'");
		int numberOfPorts;
		boolean connected = false;
		String[] portsList = new String[] {};
		List<String> tempPorts = new ArrayList<String>();
		PortFactory portFactory = null;
		
		if (ports.contains(",")==true){
			portFactory = new SplitPort(portsList, ports);
			portsList = portFactory.makePortList();
			portFactory.makeLog();
		}
		else if (ports.contains("-")==true){
			portFactory = new HyphenPort(portsList, tempPorts, ports);
			portsList = portFactory.makePortList();
			portFactory.makeLog();			
		}
		else {
			//ports contains a port only
			portsList = new String[] {ports};
		}
		for (numberOfPorts=0; numberOfPorts<portsList.length; numberOfPorts++) {
			Scanner.myLogger.fine("Scanning the port "+portsList[numberOfPorts]);
			int portToTest = Integer.valueOf(portsList[numberOfPorts]);
			connected = scanner.connection(ip, portToTest, username, password);
			if (connected == true){
				Scanner.myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection establish :)");
				openedPorts.add(portToTest);
				scanner.disConnection();
			}
			else {
				Scanner.myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection impossible");
			}
		}
	}

}