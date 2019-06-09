
public class NormalPort extends PortProduct{

	String[] portsList;
	String ports;

	public NormalPort(String[] portsList, String ports) {
		this.portsList = portsList;
		this.ports = ports;
	}
	@Override
	public void makeLog() {
		// TODO Auto-generated method stub
		Scanner.myLogger.fine("ports contains a port only");
	}

	@Override
	protected String[] makePortList() {
		// TODO Auto-generated method stub
		portsList = new String[] {ports};

		return portsList;
	}

}
