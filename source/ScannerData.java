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
		int nb;
		boolean connected = false;
		String[] portsList = new String[] {};
		if (ports.contains(",")==true){
			Scanner.myLogger.fine("There is a ',' in ports, split ports to scan each port");
			portsList = ports.split(",");
			Scanner.myLogger.fine("Ports to scan:"+Arrays.toString(portsList));
		}
		else if (ports.contains("-")==true){
			Scanner.myLogger.fine("There is a '-' in ports");
			String[] limits = new String[] {};
			limits = ports.split("-");
			int port = 0;
			List<String> tempPorts = new ArrayList<String>();
			for( port = Integer.parseInt(limits[0]); port <= Integer.parseInt(limits[1]); port++){
				tempPorts.add(Integer.toString(port));
			}
			portsList = tempPorts.toArray(new String[tempPorts.size()]);
		}
		else {
			//ports contains a port only
			portsList = new String[] {ports};
		}
		for (nb=0; nb<portsList.length; nb++) {
			Scanner.myLogger.fine("Scanning the port "+portsList[nb]);
			int portToTest = Integer.valueOf(portsList[nb]);
			connected = scanner.connection(ip, portToTest, username, password);
			if (connected == true){
				Scanner.myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection establish :)");
				openedPorts.add(portToTest);
				scanner.deconnection();
			}
			else {
				Scanner.myLogger.fine("Target "+ip+":"+portToTest+" : T3 connection impossible");
			}
		}
	}
}