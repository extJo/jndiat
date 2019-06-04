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
	private boolean cntionErrorAsSevereError;
	private String lastConnectionErrorDescription;
	private T3s t3s;
	
	//*************   Constructor *************
	public T3Connection(String initial_context_factory, boolean cntionErrorAsSevereError){
		super();
		myLogger.fine("T3Connection object created");
		this.initial_context_factory = initial_context_factory;
		this.context = null;
		this.cntionErrorAsSevereError = cntionErrorAsSevereError;
		this.t3s = null;
	}
	
	public T3Connection(String ip, int port, String username, String password, String initial_context_factory, boolean cntionErrorAsSevereError){
		super();
		myLogger.fine("T3Connection object created");
		this.initial_context_factory = initial_context_factory;
		this.context = null;
		this.cntionErrorAsSevereError = cntionErrorAsSevereError;
		this.ip = ip;
		this.port = port;
		this.user = username;
		this.password = password;
		this.t3s = null;
	}
	
	//*************   Connection *************
	//Return True if connected. Otherwise return False
	public boolean connection (String ip, int port, String username, String password){
		myLogger.fine("Try to establish a connection to "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
		this.ip = ip;
		this.port = port;
		this.user = username;
		this.password = password;
		this.uri = "t3://"+ this.ip +":"+ this.port +"";
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, this.initial_context_factory);
		env.put(Context.PROVIDER_URL,this.uri);
		env.put(Context.SECURITY_PRINCIPAL,this.user);
		env.put(Context.SECURITY_CREDENTIALS,this.password);
		boolean hasSevereError = this.cntionErrorAsSevereError == true;
		try {
			this.context = new InitialContext(env);
			myLogger.info("The connection is established trough the T3 protocol (no encryption)");
			myLogger.fine("You can use "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
			return true;
		}catch (CommunicationException error) {
			boolean isErrorConnectWithReset = error.toString().contains(ERROR_CONNECTION_RESET);
			if (isErrorConnectWithReset){
				myLogger.fine("Trying to connect with t3s (t3 over SSL) because there is a reset with t3");
				this.t3s = new T3s (this.ip, this.port);
				boolean impossibleToMakeT3s = t3s.makeT3sConfig() == false;
				if (impossibleToMakeT3s){
					myLogger.severe("Impossible to make the T3s configuration");
					return false;
				}
				else {
					myLogger.fine("T3s configuration made");
				}
				this.uri = "t3s://"+ this.ip +":"+ this.port +"";
				env.put(Context.PROVIDER_URL,this.uri);
				try {
					this.context = new InitialContext(env);
					myLogger.info("The connection is established trough the T3s protocol (SSL/TLS encryption)");
					myLogger.fine("You can use "+ip+":"+port+" with credentials '"+username+"'/'"+password+"'");
					return true;
				}catch (AuthenticationException e) {
					if (hasSevereError){myLogger.severe("'"+this.user+"' can't be authenticated on "+ip+":"+port);}
					else {myLogger.fine("Can't be authenticated on "+ip+":"+port+"with credentials '"+username+"'/'"+password+"': invalid credentials");}
					this.lastConnectionErrorDescription = e.toString();
				}catch (Exception e) {
					this.genericConnectionErrorPrinter(e);
				}
			}
			else {
				this.genericConnectionErrorPrinter(error);
			}
		}catch (AuthenticationException e) {
			if (hasSevereError){myLogger.severe("'"+this.user+"' can't be authenticated on "+ip+":"+port);}
			else {myLogger.fine("Can't be authenticated on "+ip+":"+port+"with credentials '"+username+"'/'"+password+"': invalid credentials");}
			this.lastConnectionErrorDescription = e.toString();
		}catch (Exception e) {
			this.genericConnectionErrorPrinter(e);
		}
		return false;
	}
	
	public boolean connection (){
		return this.connection(ip, port, user, password);
	}
	
	//*************   Deconnection *************
	//Return True if deconnected. Otherwise return False
	public boolean deconnection (){
		try {
			this.context.close();
			return true;
		}catch (Exception e) {
			return false;
		}
	}
	
	public boolean isConnected (){
		boolean isNotConnected = this.context==null;
		if (isNotConnected){return false;}
		else {return true;}
	}
	
	//Contains the last connection error descrition 
	public String getLastConnectionErrorDescription(){
		return this.lastConnectionErrorDescription;
	}
	
	/* Print a Generic error connection*/
	public void genericConnectionErrorPrinter (Exception error){
		boolean isSevereError = this.cntionErrorAsSevereError == true;
		boolean errorStreamIsClosed = error.toString().contains(this.ERROR_STREAM_CLOSED);
		if (isSevereError){
			myLogger.severe("Error during connection with '"+this.user+"' to "+this.ip+":"+this.port+":"+error.toString());
			if (errorStreamIsClosed) {
				myLogger.severe("You should retry to establish a connection: The server is probably busy");
			}
		}
		else {
			myLogger.fine("Error during connection with '"+this.user+"' to "+this.ip+":"+this.port+":"+error.toString());
			if (errorStreamIsClosed) {
				myLogger.fine("You should retry to establish a connection: The server is probably busy");
			}
		}
		this.lastConnectionErrorDescription = error.toString();
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
	public Context getCtx(){
		return this.context;
	}
	
	//*************   MUTATEURS *************
	
	
}
