//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com
import java.util.logging.Logger;
import javax.sql.DataSource;
import java.sql.*;
import javax.naming.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.net.*;
//For SQL requester
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
//For the listener
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

//Connection to a datasource
public class SQLDataSource extends T3Connection {
	public static final int SUCCESS = 0;
	public static final int SOCKETERROR = 1;
	public static final int BUFFERERROR = 2;
	public static final int REQUESTERROR = 3;
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private Connection connection;
	private String dataSource;
	
	public SQLDataSource(String ip, Integer port, String username, String password){
		super(ip, port, username, password, "weblogic.jndi.WLInitialContextFactory", true);
		myLogger.fine("SQLDataSource object created");
		this.connection = null;
		this.dataSource = "";
	}
	
	//Returns True if no error. Otherwise returns False
	private boolean initDataSourceConnection(String dataSource){
		boolean resultOfDataSourceConnetion = true;
		
		String databaseType = "";
		myLogger.info("SQL connection through the datasource '"+dataSource+"'");
		try {
			this.connection = ((DataSource)this.getContext().lookup(dataSource)).getConnection();
			
			databaseType = this.connection.toString().toUpperCase();
			if (databaseType.contains("MYSQL")){
				this.print("Connected to a MYSQL database");
			}else if(databaseType.contains("ORACLE")){
				this.print("Connected to an ORACLE database");
			}else if(databaseType.contains("SYBASE")){
				this.print("Connected to a SYSBASE database");
			}else{
				this.print("Connected to a UNKNOWN database: "+databaseType);
			}
		} catch (Exception exception) {
			myLogger.severe("Impossible to get a DataSource connection: "+exception);
			resultOfDataSourceConnetion = false;
		}
		
		return resultOfDataSourceConnetion;
	}
	
	//Returns true if all is OK. Otherwise return False
	public boolean SQLshell (String dataSource){
		boolean resultOfConnection = false;
		boolean resultOfsqlShell;
		this.dataSource = dataSource;
		this.connection();
		if (this.isConnected() == true){
			try {
				resultOfConnection = this.initDataSourceConnection(dataSource);
				if (resultOfConnection==true){
					generateSQLShell();
				}
				else {
					resultOfsqlShell = false;
				}
			} catch (Exception exception) {
				myLogger.severe("Impossible to get a DataSource connection: "+exception);
				resultOfsqlShell = false;
			}
			resultOfsqlShell = true;
		}
		else {
			myLogger.severe("Impossible to get a SQL shell because we can't establish a connection: "+this.getLastConnectionErrorDescription());
			resultOfsqlShell = false;
		}
		return resultOfsqlShell;
	}

	private void generateSQLShell(){
		try {
			//source: http://jeszysblog.wordpress.com/2012/04/14/readline-style-command-line-editing-with-jline/
			ConsoleReader consoleReader = new ConsoleReader();
			consoleReader.setPrompt(this.dataSource+"> ");
			String sql = null;
			boolean readSQLUntilNull = (sql = consoleReader.readLine()) != null;
			
			while (readSQLUntilNull) {
				try{
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(sql);
					DBTablePrinter.printResultSet(resultSet);
				} catch (SQLException sqlException) {
					myLogger.severe("Error with the SQL request '"+sql+"':"+sqlException);
				}
			}
		} catch(IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				TerminalFactory.get().restore();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	public String letHimSelectDatasource(){
		int pos = 0;
		Integer dataSourceNumber = -1;
		String datasourceToUse = "";
		myLogger.fine("Let the user choose the datasource from jndi list");
		JndiListing jndiListing = new JndiListing(this.getIp(),this.getPort(),this.getUser(),this.getPassword());
		jndiListing.searchJndi();
		
		ArrayList datasources = jndiListing.getDatasources();
		boolean isValidDataSource = datasources.toArray().length>0;
		
		if (isValidDataSource){
			boolean isValidDataSourceNumber = dataSourceNumber < 0 || dataSourceNumber >= datasources.toArray().length;
			
			while (isValidDataSourceNumber){
				System.out.println("Choose the number of the datasource to use:");
				for (pos = 0 ; pos < datasources.toArray().length ; pos = pos+1){
					System.out.println(pos+". "+datasources.get(pos));
				}
				try {
					BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(System.in));
					dataSourceNumber = Integer.parseInt(inputStream.readLine());
				} 
				catch (NumberFormatException numberFormatException) {
					myLogger.severe("Not a good value");
				}
				catch (Exception exception){
					myLogger.severe("Unexpected IO ERROR");
				}
			}
			datasourceToUse = ""+datasources.get(dataSourceNumber);
			System.out.println("You have chosen the datasource '"+datasources.get(dataSourceNumber)+"'");
		}
		else {
			myLogger.severe("Not one datasource in jndi list, cancelation...");
		}
		return datasourceToUse;
	}
	
	public String resultSetToXML (ResultSet resultSet) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SQLException{
		myLogger.finest("Parsing SQL results for XML output started");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element resultOfelement = document.createElement("Results");
		document.appendChild(resultOfelement);

		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int columnCount = resultSetMetaData.getColumnCount();

		while (resultSet.next()){
			Element elementOfRow = document.createElement("Row");
			resultOfelement.appendChild(elementOfRow);

			myLogger.finest("Paring a new line");
			for (int i = 1; i <= columnCount; i++){
				String columnName = resultSetMetaData.getColumnName(i);
				Object value = resultSet.getObject(i);
				myLogger.finest("value = '"+value.toString()+"'");
				myLogger.finest("Node appened in xml");
				
				Element elementOfColumn = document.createElement("Column");
				elementOfRow.appendChild(elementOfColumn);
				Element nameNode = document.createElement("Name");
				nameNode.appendChild(document.createTextNode(columnName));
				elementOfColumn.appendChild(nameNode);
				Element valueNode = document.createElement("Value");
				valueNode.appendChild(document.createTextNode(value.toString()));
				elementOfColumn.appendChild(valueNode);
				
			}
		}
		StringWriter stringWriter = new StringWriter();
		TransformerFactory transFormerFactory = TransformerFactory.newInstance();
		Transformer transformer = transFormerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
		myLogger.finest("Parsing SQL results for XML output stopped");
		return stringWriter.toString();
	}
	
	private int checkConnection(int port, String dataSource) {
		int result=0;
		
		myLogger.fine("Listening on the port "+port+" for SQL requests");
		ServerSocket serverSocket = null;
		Socket socket = null;
		BufferedReader bufferReader = null;
		PrintWriter printWriter = null;
		this.connection();
		if (this.isConnected() == true){
			this.initDataSourceConnection(dataSource);
			result = SUCCESS;
			try{
				serverSocket = new ServerSocket(port);
				socket = serverSocket.accept();
				myLogger.finer("A new connection has been established");
			} catch (IOException ioException) {
				myLogger.severe("Could not listen on port " + port);
				result = SOCKETERROR;
			}
			try{
				bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			} catch (IOException ioException) {
				myLogger.severe("Impossible to get a read or write buffer");
				result = BUFFERERROR;
			}
		}
		return result;
	}
	
	public Integer listenFromRequest(int port, String dataSource){
		int result = SUCCESS;
		result = checkConnection(port, dataSource);
		
		BufferedReader bufferReader = null;
		PrintWriter printWriter = null;
			
			while (true) {
				boolean executionError = false;
				String request = "";
				String response = "";
				ResultSet resultSet = null;
				Statement statement = null;
				try{
					myLogger.finer("Waiting a SQL request...");
					request = bufferReader.readLine();
					if (request == null) break;
				} catch (IOException ioException) {
					myLogger.severe("Impossible to get data from client");
					result = REQUESTERROR;
				}
				myLogger.finer("Received from client: '"+request+"'");
				if (request.equals("END")) break;
				try{
					statement = connection.createStatement();
					resultSet = statement.executeQuery(request);
					executionError = false;
				} catch(SQLException sqlException) {
					response = "<error>"+sqlException+"</error>";
					executionError = true;
					myLogger.severe("SQL Error with the request '"+request+"':"+sqlException);
				} catch (Exception exception) {
					executionError = true;
					response = "<error>server side error during XML paring: '"+exception+"'</error>";
					myLogger.severe("Error with the SQL request '"+request+"':"+exception);
				}
				if (executionError==false){
					try{
						response = resultSetToXML(resultSet);
					} catch (Exception exception) {
						myLogger.severe("Error with the response '"+response+"':"+exception);
					}
				}
				myLogger.finer("Sending response to client: '"+response+"'");
				printWriter.println(response);
			}
		
		return result;
	}
}













