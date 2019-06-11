import java.util.List;

public class PortFactory {
	PortProduct portProduct = null;
	
	public PortProduct create(String[] portsList, List<String> tempPorts, String ports) {
		if(ports.contains(",")==true) {
			portProduct = new SplitPort(portsList, ports);
		}else if(ports.contains("-")==true) {
			portProduct = new HyphenPort(portsList, tempPorts, ports);
		}else {
			portProduct = new NormalPort(portsList, ports);

		}
		
		return portProduct;
	}
	
}
