//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Logger;
import java.io.*;
//To get a remote certificate
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;

public class T3s {
	
	private static Logger myLogger = Logger.getLogger("JNDIAT");
	private String ip;
	private int port;
	private String TEMP_CERT_FILE;
	private String TEMP_KEYSTORE_FILE;
	
	/*Constructor*/
	public T3s(String ip, int port){
		myLogger.fine("T3s object created");
		this.ip=ip;
		this.port=port;
		this.TEMP_CERT_FILE = this.ip+"-"+this.port+".cert";
		this.TEMP_KEYSTORE_FILE = this.ip+"-"+this.port+".jks";
	}
	
	/*Make the T3 configuration to establish a T3 connection after.
	 * Get the remote certificate. Create a keystore file. 
	 * Load weblogic parameters to use the keystore.
	 * Returns true if no error. Otherwise returns false*/
	public boolean makeT3sConfig(){
		boolean resultOfConfiguration = true;
		
		myLogger.fine("Making the T3s configuration (t3 over SSL)");
		this.initWeblogicPropertiesForKeyStore();
		boolean isKeyStoreFileExist = this.isKeyStoreFileExist()== true;
		boolean isSaveTheRemoteCertificate = this.saveTheRemoteCertificate()==true;

		if (!isKeyStoreFileExist){
			if (isSaveTheRemoteCertificate){
				boolean isValidKeyExist = this.createAValidKeyStore() == false;
				if (!isValidKeyExist){
					resultOfConfiguration = false;
				}
				else {
					myLogger.fine("I'm trying to connect trough the T3s protocol");
				}
			}
			else {
				myLogger.severe("We can't get the remote certificate. There is a certificate on this port?");
				resultOfConfiguration = false;
			}
		}
		else {
			myLogger.info("The file "+TEMP_KEYSTORE_FILE+" exists: The tool will use it to establish the T3s connection.");
		}
		return resultOfConfiguration;
	}
	
	
	/* Create a valid keystore TEMP_KEYSTORE_FILE which contains the TEMP_CERT_FILE certificat
	 * Returns Truue if no error. Otherwise return False */
	public boolean createAValidKeyStore(){
		KeyStore theKeyStore = null;
		boolean resultOfcreatAValidKey = true;
		try {
			theKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			theKeyStore.load(null , "".toCharArray());
		}
		catch (Exception e){
			myLogger.fine("Impossible to generate an empty KeyStore:"+e);
			resultOfcreatAValidKey = false;
		}
		// Store away the keystore.
		try {
			InputStream inputStreamToCertFile = new FileInputStream(TEMP_CERT_FILE);
			BufferedInputStream inputBufferStreamToCertFile = new BufferedInputStream(inputStreamToCertFile);
			FileOutputStream fileOutputStreamToKeyStore = new FileOutputStream(TEMP_KEYSTORE_FILE);
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			boolean isInputBufferStreamAvailable = inputBufferStreamToCertFile.available() > 0;
			
			while (isInputBufferStreamAvailable) {
				myLogger.fine("Loading in the keystore a certificat stored in "+TEMP_CERT_FILE);
				Certificate certificate = certificateFactory.generateCertificate(inputBufferStreamToCertFile);
				theKeyStore.setCertificateEntry("fiddler"+inputBufferStreamToCertFile.available(), certificate);
			}
			theKeyStore.store(fileOutputStreamToKeyStore,"".toCharArray());
			myLogger.fine("The KeyStore "+TEMP_KEYSTORE_FILE+" has been created locally.");
			fileOutputStreamToKeyStore.close();
			inputStreamToCertFile.close();
		}catch (Exception e){
			myLogger.fine("Impossible to create the new KeyStore:"+e);
			resultOfcreatAValidKey = false;
		}
		deleteFile(this.TEMP_CERT_FILE);
		return resultOfcreatAValidKey;
	}
	
	/*Store the certificate of the remote server in TEMP_CERT_FILE
	 * Return true if no error. Otherwise return False */
	public boolean saveTheRemoteCertificate(){
		boolean resultOfRemoteCertificate = true;
		BufferedWriter bufferedWriter = null;
		// create custom trust manager to ignore trust paths
		TrustManager trustManger = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		};
		SSLSocket sslSocket = null;
		try {
			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(null, new TrustManager[] { trustManger }, null);
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			sslSocket =(SSLSocket)sslSocketFactory.createSocket(this.ip, this.port);
			sslSocket.startHandshake();
			SSLSession sslSession = sslSocket.getSession();
			java.security.cert.Certificate[] serverCertificate = sslSession.getPeerCertificates();
			bufferedWriter = new BufferedWriter(new FileWriter(this.TEMP_CERT_FILE));
			for (int i = 0; i < serverCertificate.length; i++) {
				bufferedWriter.write("-----BEGIN CERTIFICATE-----\n");
				bufferedWriter.write(new sun.misc.BASE64Encoder().encode(serverCertificate[i].getEncoded()));
				bufferedWriter.write("\n-----END CERTIFICATE-----\n");
			}
			bufferedWriter.close();
			sslSocket.close();
		} catch (Exception e) {
			myLogger.severe("Impossible to write the cer file "+this.TEMP_CERT_FILE+": "+e);
			resultOfRemoteCertificate = false;
		}
		myLogger.fine("The file "+this.TEMP_CERT_FILE+" has been created to stored the remote certificate");
		return resultOfRemoteCertificate;
	}
	
	/*Inialize weblogic variable to use a keystore to bypass certificat errors with T3s*/
	public void initWeblogicPropertiesForKeyStore(){
		myLogger.fine("The KeyStore "+TEMP_KEYSTORE_FILE+" will be used to establish the T3s connection in order don't have a certificate error");
		System.setProperty("weblogic.security.SSL.ignoreHostnameVerification","true");
		System.setProperty("weblogic.security.TrustKeyStore","CustomTrust");
		System.setProperty("weblogic.security.CustomTrustKeyStoreFileName", this.TEMP_KEYSTORE_FILE);
		System.setProperty("weblogic.security.CustomTrustKeyStorePassPhrase",""); 
		System.setProperty("weblogic.security.CustomTrustKeyStoreType","JKS");
	}
	
	/* Returns true if the current keystore file exists. Otherwise return false*/
	public boolean isKeyStoreFileExist(){
		boolean resultOfKeyStoreExist = new File(TEMP_KEYSTORE_FILE).exists();
		return resultOfKeyStoreExist;
	}
	
	/* Delete the file fileName
	 * Returns true if the file has been deleted. Otherwise return false*/
	public boolean deleteFile(String fileName){
		boolean resultOfdeleteFile = true;
		try{
			File file = new File(fileName);
			if(file.delete()){
				myLogger.fine("The file "+fileName+" has been removed");
			}
			else {
				myLogger.fine("The file "+fileName+" has NOT been removed");
				resultOfdeleteFile = false;
			}
		}catch(Exception e){
			myLogger.fine("The file "+fileName+" has NOT been removed: "+e);
			resultOfdeleteFile = false;
		}
		return resultOfdeleteFile;
	}
}
