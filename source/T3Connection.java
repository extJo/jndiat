//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.Arrays;
import java.util.Hashtable;
import javax.naming.*;
import java.util.logging.Logger;
import javax.naming.CommunicationException;

//Connection with T3 protocol
public class T3Connection extends MyPrinter {
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private String ip;
	private int port;
	private String user;
	private String password;
	private String uri;
	private String initial_context_factory;
	private Context context;
	private boolean connectionErrorAsSevereError;
	private String lastConnectionErrorDescription;
	private T3s t3s;
	
	//*************   Constructor *************
	public T3Connection(String initial_context_factory, boolean connectionErrorAsSevereError){
		super();
		myLogger.fine("T3Connection object created");
		this.initial_context_factory = initial_context_factory;
		this.context = null;
		this.connectionErrorAsSevereError = connectionErrorAsSevereError;
		this.t3s = null;
	}
	
	public T3Connection(String ip, int port, String username, String password, String initial_context_factory, boolean connectionErrorAsSevereError){
		super();
		myLogger.fine("T3Connection object created");
		this.initial_context_factory = initial_context_factory;
		this.context = null;
		this.connectionErrorAsSevereError = connectionErrorAsSevereError;
		this.ip = ip;
		this.port = port;
		this.user = username;
		this.password = password;
		this.t3s = null;
	}
	
	//*************   Connection *************
	//Return True if connected. Otherwise return False
	public boolean connection (String ip, int port, String username, String password){
		boolean resultOfConnection = false;
		myLogger.fine("Try to establish a connection to "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
		this.ip = ip;
		this.port = port;
		this.user = username;
		this.password = password;
		this.uri = "t3://"+ this.ip +":"+ this.port +"";
		Hashtable<String, String> environmentOfConnection = new Hashtable<String, String>();
		environmentOfConnection.put(Context.INITIAL_CONTEXT_FACTORY, this.initial_context_factory);
		environmentOfConnection.put(Context.PROVIDER_URL,this.uri);
		environmentOfConnection.put(Context.SECURITY_PRINCIPAL,this.user);
		environmentOfConnection.put(Context.SECURITY_CREDENTIALS,this.password);
		try {
			this.context = new InitialContext(environmentOfConnection);
			myLogger.info("The connection is established trough the T3 protocol (no encryption)");
			myLogger.fine("You can use "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
			resultOfConnection = true;
		}catch (CommunicationException communicationException) {
			resultOfConnection = false;
			boolean isConnectionReset = communicationException.toString().contains(ERROR_CONNECTION_RESET);
			if (isConnectionReset){
				myLogger.fine("Trying to connect with t3s (t3 over SSL) because there is a reset with t3");
				this.t3s = new T3s (this.ip, this.port);
				if (t3s.makeT3sConfig() == false){
					myLogger.severe("Impossible to make the T3s configuration");
				}else {
					myLogger.fine("T3s configuration made");
				}
				this.uri = "t3s://"+ this.ip +":"+ this.port +"";
				environmentOfConnection.put(Context.PROVIDER_URL,this.uri);
				try {
					this.context = new InitialContext(environmentOfConnection);
					myLogger.info("The connection is established trough the T3s protocol (SSL/TLS encryption)");
					myLogger.fine("You can use "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
					resultOfConnection = true;
				}catch (AuthenticationException authenticationException) {
					if (this.connectionErrorAsSevereError == true){
						myLogger.severe("'"+this.user+"' can't be authenticated on "+ip+":"+port);
						}
					else {
						myLogger.fine("Can't be authenticated on "+ip+":"+port+"with credentials '"+username+"'/'"+password+"': invalid credentials");
						}
					this.lastConnectionErrorDescription = authenticationException.toString();
				}catch (Exception exception) {
					this.genericConnectionErrorPrinter(exception);
				}
			}
			else {
				this.genericConnectionErrorPrinter(communicationException);
			}
		}catch (AuthenticationException authenticationException) {
			resultOfConnection = false;
			if (this.connectionErrorAsSevereError == true)
			{
				myLogger.severe("'"+this.user+"' can't be authenticated on "+ip+":"+port);
			}else {
				myLogger.fine("Can't be authenticated on "+ip+":"+port+"with credentials '"+username+"'/'"+password+"': invalid credentials");
				}
			this.lastConnectionErrorDescription = authenticationException.toString();
		}catch (Exception exception) {
			this.genericConnectionErrorPrinter(exception);
			resultOfConnection = false;
		}
		return resultOfConnection;
	}
	
	public boolean connection (){
		return this.connection(ip, port, user, password);
	}
	
	//*************   Deconnection *************
	//Return True if deconnected. Otherwise return False
	public boolean disConnection (){
		boolean resultOfConnection = true;
		try {
			this.context.close();
		}catch (Exception exception) {
			resultOfConnection = false;
		}
		return resultOfConnection;
	}
	
	public boolean isConnected (){
		boolean resultOfConnection;;
		if (this.context==null)
		{
			resultOfConnection = false;
		}else 
		{
			resultOfConnection = true;
		}
		return resultOfConnection;
	}
	
	//Contains the last connection error descrition 
	public String getLastConnectionErrorDescription(){
		return this.lastConnectionErrorDescription;
	}
	
	/* Print a Generic error connection*/
	public void genericConnectionErrorPrinter (Exception exception){
		boolean isServerError = this.connectionErrorAsSevereError == true;
		boolean isStreamClosed = exception.toString().contains(this.ERROR_STREAM_CLOSED);
		
		if (isServerError){
			myLogger.severe("Error during connection with '"+this.user+"' to "+this.ip+":"+this.port+":"+exception.toString());
			if (isStreamClosed) {
				myLogger.severe("You should retry to establish a connection: The server is probably busy");
			}
		}else {
			myLogger.fine("Error during connection with '"+this.user+"' to "+this.ip+":"+this.port+":"+exception.toString());
			if (isStreamClosed) {
				myLogger.fine("You should retry to establish a connection: The server is probably busy");
			}
		}
		this.lastConnectionErrorDescription = exception.toString();
	}
	
	//*************   ACCESSEURS *************
	public String getUri (){
		return this.uri;
	}
	public String getIp (){
		return this.ip;
	}
	public int getPort (){
		return this.port;
	}
	public String getUser (){
		return this.user;
	}
	public String getPassword (){
		return this.password;
	}
	public Context getContext(){
		return this.context;
	}
	
	//*************   MUTATEURS *************
	
	
}
