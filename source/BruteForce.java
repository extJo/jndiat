//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import javax.naming.*;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BruteForce extends T3Connection{

	private static Logger myLogger = Logger.getLogger("JNDIAT");	
	private List<String[]> accountsFound;
	private String credentialFilename;
	private String ip;
	private Integer port;
	private String separator;
			
	//*************   Constructor *************
	public BruteForce(String ip, Integer port, String initial_context_factory, boolean printErrorConnection, String credentialFilename, String separator){
		super(initial_context_factory, false);
		myLogger.fine("Brutforce object created");
		this.ip = ip;
		this.port = port;
		this.accountsFound = new ArrayList<String[]>();
		this.credentialFilename = credentialFilename;
		this.separator = separator;
	}
	
	/*Search valid credentials
	 * Return True if no error
	 * Otherwise return true */
	public boolean searchValidCredentials(){
		boolean resultOfsearchValidCredential = true;
		String line = "";
		String[] credentials = {};
		boolean connectionStatus = false;
		BufferedReader reader = null;
		try {
			if (this.credentialFilename==""){
				myLogger.finer("We use the credentials.txt file stored in the Jar file");
				URL urlToDictionary = this.getClass().getResource("/" + "credentials.txt");
				reader = new BufferedReader(new InputStreamReader(urlToDictionary.openStream(), "UTF-8"));
				credentialFilename = "jar://credentials.txt";
			}
			else {
				myLogger.finer("We use your own file stored");
				reader = new BufferedReader(new FileReader(this.credentialFilename));
			}
		}
		catch (Exception exception){
			myLogger.severe("Exception occurred trying to read '"+this.credentialFilename+"': '"+exception+"'");
			resultOfsearchValidCredential = false;
		}
		myLogger.fine("Searching valid credentials thanks to "+this.credentialFilename+" file...");			
		try {
			while ((line = reader.readLine()) != null){
				credentials = line.replaceAll("\n","").replaceAll("\t","").replaceAll("\r","").split(this.separator);
				if (credentials.length == 0) {credentials = new String[]{"",""};};
				myLogger.finer("Using the username '"+credentials[0]+"' and the password '"+credentials[1]+"'");
				connectionStatus = connection (this.ip, this.port, credentials[0], credentials[1]);
				if (connectionStatus == true){
					myLogger.fine("We can use the login '"+credentials[0]+"' with the password '"+credentials[1]+"' to establish a T3 connection");
					this.accountsFound.add(credentials);
				}
				else {
					myLogger.finer("We can't use the login '"+credentials[0]+"' with the password '"+credentials[1]+"' to establish a T3 connection");
				}
			}
			reader.close();
		}
		catch (Exception exception){
			myLogger.warning("Exception occurred trying to use credentials stored the line '"+line+"' ('"+this.credentialFilename+"' file): '"+exception+"'");
		}
		return resultOfsearchValidCredential;	
	}
	
	public List<String[]> getValidAccounts (){
		return this.accountsFound;
	}
	
	public void printValidCredentials(){
		String lineForPrint="";
		int position = 0;
		boolean isNotValidCredential = this.accountsFound.toArray().length == 0;
		if (isNotValidCredential){
			this.printBadNews ("No credentials found to connect");
		}
		else {
			for (position = 0;position<this.accountsFound.toArray().length; position = position+1){
				lineForPrint = lineForPrint + ", login='"+this.accountsFound.get(position)[0]+ "'/password='"+this.accountsFound.get(position)[1]+"'";
			}
			this.printGoodNews ("Some credentials found: "+lineForPrint);
		}
	}
}
