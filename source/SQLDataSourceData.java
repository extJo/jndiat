import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SQLDataSourceData implements SQLDataStretegy {
	public Connection connection;
	public String dataSource;

	public SQLDataSourceData() {
	}

	/* (non-Javadoc)
	 * @see SQLDataStretegy#resultSetToXML(java.sql.ResultSet)
	 */
	@Override
	public String resultSetToXML (ResultSet resultset) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SQLException{
		SQLDataSource.myLogger.finest("Parsing SQL results for XML output started");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
	
		Element results = document.createElement("Results");
		document.appendChild(results);
	
		ResultSetMetaData resultsetmetadata = resultset.getMetaData();
		int colCount = resultsetmetadata.getColumnCount();
	
		while (resultset.next()){
			Element row = document.createElement("Row");
			results.appendChild(row);
	
			SQLDataSource.myLogger.finest("Paring a new line");
			for (int i = 1; i <= colCount; i++){
				String columnName = resultsetmetadata.getColumnName(i);
				Object value = resultset.getObject(i);
				SQLDataSource.myLogger.finest("value = '"+value.toString()+"'");
				//Element node = document.createElement(columnName);
				//node.appendChild(document.createTextNode(value.toString()));
				//row.appendChild(node);
				SQLDataSource.myLogger.finest("Node appened in xml");
				
				//
				Element column = document.createElement("Column");
				row.appendChild(column);
				Element nameNode = document.createElement("Name");
				nameNode.appendChild(document.createTextNode(columnName));
				column.appendChild(nameNode);
				Element valueNode = document.createElement("Value");
				valueNode.appendChild(document.createTextNode(value.toString()));
				column.appendChild(valueNode);
				//
			}
		}
		StringWriter stringWriter = new StringWriter();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
		SQLDataSource.myLogger.finest("Parsing SQL results for XML output stopped");
		return stringWriter.toString();
	}
}