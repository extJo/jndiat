import java.util.List;

public class HyphenPort extends PortFactory {

	String[] limits;
	String[] portsList;
	List<String> tempPorts;
	String ports;
	
	public HyphenPort(String[] portsList, List<String> tempPorts, String ports) {
		this.ports = ports;
		this.limits = ports.split("-");
		this.tempPorts = tempPorts;
		this.portsList = portsList;
		makeLog();
	}
	@Override
	public void makeLog() {
		Scanner.myLogger.fine("There is a '-' in ports");
	}
	@Override
	protected String[] makePortList() {
		for( int port = Integer.parseInt(limits[0]); port <= Integer.parseInt(limits[1]); port++){
			tempPorts.add(Integer.toString(port));
		}
		portsList = tempPorts.toArray(new String[tempPorts.size()]);
		
		return portsList;
	}

}
