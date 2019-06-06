import java.util.Arrays;

public class SplitPort extends PortFactory {
	
	String[] portsList = new String[] {};
	String ports;
	public SplitPort(String[] portsList, String ports) {
		// TODO Auto-generated constructor stub
		this.portsList = portsList;
		this.ports = ports;
	}

	@Override
	public void makeLog() {
		// TODO Auto-generated method stub
		Scanner.myLogger.fine("There is a ',' in ports, split ports to scan each port");
		Scanner.myLogger.fine("Ports to scan:"+Arrays.toString(portsList));
	}

	@Override
	protected String[] makePortList() {
		portsList = ports.split(",");
		return portsList;
	}
}
