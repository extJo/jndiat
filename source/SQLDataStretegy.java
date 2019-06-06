import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public interface SQLDataStretegy {

	String resultSetToXML(ResultSet resultset)
			throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SQLException;

}