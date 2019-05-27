/*
Database Table Printer
Copyright (C) 2014  Hami Galip Torun

Email: hamitorun@e-fabrika.net
Project Home: https://github.com/htorun/dbtableprinter

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
This is my first Java program that does something more or less
useful. It is part of my effort to learn Java, how to use
an IDE (IntelliJ IDEA 13.1.15 in this case), how to apply an
open source license and how to use Git and GitHub (https://github.com)
for version control and publishing an open source software.

Hami
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Just a utility to print rows from a given DB table or a
 * <code>ResultSet</code> to standard out, formatted to look
 * like a table with rows and columns with borders.
 *
 * <p>Stack Overflow website
 * (<a target="_blank" href="http://stackoverflow.com">stackoverflow.com</a>)
 * was the primary source of inspiration and help to put this
 * code together. Especially the questions and answers of
 * the following people were very useful:</p>
 *
 * <p>Question:
 * <a target="_blank" href="http://tinyurl.com/q7lbqeh">How to display or
 * print the contents of a database table as is</a><br>
 *     People: sky scraper</p>
 *
 * <p>Question:
 * <a target="_blank" href="http://tinyurl.com/pbwgess">System.out.println()
 * from database into a table</a><br>
 *     People: Simon Cottrill, Tony Toews, Costis Aivali, Riggy, corsiKa</p>
 *
 * <p>Question:
 * <a target="_blank" href="http://tinyurl.com/7x9qtyg">Simple way to repeat
 * a string in java</a><br>
 *     People: Everybody who contributed but especially user102008</p>
 *
 */


public class DBTablePrinter {

	 /**
     * Default maximum number of rows to query and print.
     */
    private static final int DEFAULT_MAX_ROWS = 10;

    /**
     * Default maximum width for text columns
     * (like a <code>VARCHAR</code>) column.
     */
    private static final int DEFAULT_MAX_TEXT_COL_WIDTH = 150;

    /**
     * Column type category for <code>CHAR</code>, <code>VARCHAR</code>
     * and similar text columns.
     */
    public static final int CATEGORY_STRING = 1;

    /**
     * Column type category for <code>TINYINT</code>, <code>SMALLINT</code>,
     * <code>INT</code> and <code>BIGINT</code> columns.
     */
    public static final int CATEGORY_INTEGER = 2;

    /**
     * Column type category for <code>REAL</code>, <code>DOUBLE</code>,
     * and <code>DECIMAL</code> columns.
     */
    public static final int CATEGORY_DOUBLE = 3;

    /**
     * Column type category for date and time related columns like
     * <code>DATE</code>, <code>TIME</code>, <code>TIMESTAMP</code> etc.
     */
    public static final int CATEGORY_DATETIME = 4;

    /**
     * Column type category for <code>BOOLEAN</code> columns.
     */
    public static final int CATEGORY_BOOLEAN = 5;

    /**
     * Column type category for types for which the type name
     * will be printed instead of the content, like <code>BLOB</code>,
     * <code>BINARY</code>, <code>ARRAY</code> etc.
     */
    public static final int CATEGORY_OTHER = 0;

    /**
     * Represents a database table column.
     */
    private static class Column {

        /**
         * Column label.
         */
        private String label;

        /**
         * Generic SQL type of the column as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>.
         */
        private int type;

        /**
         * Generic SQL type name of the column as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>.
         */
        private String typeName;

        /**
         * Width of the column that will be adjusted according to column label
         * and values to be printed.
         */
        private int width = 0;

        /**
         * Column values from each row of a <code>ResultSet</code>.
         */
        private List<String> values = new ArrayList<>();

        /**
         * Flag for text justification using <code>String.format</code>.
         * Empty string (<code>""</code>) to justify right,
         * dash (<code>-</code>) to justify left.
         *
         * @see #justifyLeft()
         */
        private String justifyFlag = "";

        /**
         * Column type category. The columns will be categorised according
         * to their column types and specific needs to print them correctly.
         */
        private int typeCategory = 0;

        /**
         * Constructs a new <code>Column</code> with a column label,
         * generic SQL type and type name (as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>)
         *
         * @param label Column label or name
         * @param type Generic SQL type
         * @param typeName Generic SQL type name
         */
        public Column (String label, int type, String typeName) {
            this.label = label;
            this.type = type;
            this.typeName = typeName;
        }

        /**
         * Returns the column label
         *
         * @return Column label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the generic SQL type of the column
         *
         * @return Generic SQL type
         */
        public int getType() {
            return type;
        }

        /**
         * Returns the generic SQL type name of the column
         *
         * @return Generic SQL type name
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * Returns the width of the column
         *
         * @return Column width
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of the column to <code>width</code>
         *
         * @param width Width of the column
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * Adds a <code>String</code> representation (<code>value</code>)
         * of a value to this column object's {@link #values} list.
         * These values will come from each row of a
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
         * ResultSet
         * </a> of a database query.
         *
         * @param value The column value to add to {@link #values}
         */
        public void addValue(String value) {
            values.add(value);
        }

        /**
         * Returns the column value at row index <code>i</code>.
         * Note that the index starts at 0 so that <code>getValue(0)</code>
         * will get the value for this column from the first row
         * of a <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
         * ResultSet</a>.
         *
         * @param i The index of the column value to get
         * @return The String representation of the value
         */
        public String getValue(int i) {
            return values.get(i);
        }

        /**
         * Returns the value of the {@link #justifyFlag}. The column
         * values will be printed using <code>String.format</code> and
         * this flag will be used to right or left justify the text.
         *
         * @return The {@link #justifyFlag} of this column
         * @see #justifyLeft()
         */
        public String getJustifyFlag() {
            return justifyFlag;
        }

        /**
         * Sets {@link #justifyFlag} to <code>"-"</code> so that
         * the column value will be left justified when printed with
         * <code>String.format</code>. Typically numbers will be right
         * justified and text will be left justified.
         */
        public void justifyLeft() {
            this.justifyFlag = "-";
        }

        /**
         * Returns the generic SQL type category of the column
         *
         * @return The {@link #typeCategory} of the column
         */
        public int getTypeCategory() {
            return typeCategory;
        }

        /**
         * Sets the {@link #typeCategory} of the column
         *
         * @param typeCategory The type category
         */
        public void setTypeCategory(int typeCategory) {
            this.typeCategory = typeCategory;
        }
    }

    /**
     * Overloaded method that prints rows from table <code>tableName</code>
     * to standard out using the given database connection
     * <code>conn</code>. Total number of rows will be limited to
     * {@link #DEFAULT_MAX_ROWS} and
     * {@link #DEFAULT_MAX_TEXT_COL_WIDTH} will be used to limit
     * the width of text columns (like a <code>VARCHAR</code> column).
     *
     * @param connection Database connection object (java.sql.Connection)
     * @param tableName Name of the database table
     */
    public static void printTable(Connection connection, String tableName){
        printTable(connection, tableName, DEFAULT_MAX_ROWS, DEFAULT_MAX_TEXT_COL_WIDTH);
    }

    /**
     * Overloaded method that prints rows from table <code>tableName</code>
     * to standard out using the given database connection
     * <code>conn</code>. Total number of rows will be limited to
     * <code>maxRows</code> and
     * {@link #DEFAULT_MAX_TEXT_COL_WIDTH} will be used to limit
     * the width of text columns (like a <code>VARCHAR</code> column).
     *
     * @param connection Database connection object (java.sql.Connection)
     * @param tableName Name of the database table
     * @param maxRows Number of max. rows to query and print
     */
    public static void printTable(Connection connection, String tableName, int maxRows) {
        printTable(connection, tableName, maxRows, DEFAULT_MAX_TEXT_COL_WIDTH);
    }

    public static boolean checkConnection(Connection connection, String tableName) {
 
        boolean isConnectionNull = connection == null;
        boolean isTableNameNull = tableName == null;
        boolean isEmptyTable = tableName.length() == 0;
    	boolean isPossible = true;
			if (isConnectionNull) {
	            System.err.println("DBTablePrinter Error: No connection to database (Connection is null)!");
	            isPossible = false;
	        }
			if (isTableNameNull) {
	            System.err.println("DBTablePrinter Error: No table name (tableName is null)!");
	            isPossible = false;
	        }
			if (isEmptyTable) {
	            System.err.println("DBTablePrinter Error: Empty table name!");
	            isPossible = false;
	        }
		return isPossible;
    }
    
    /**
     * Overloaded method that prints rows from table <code>tableName</code>
     * to standard out using the given database connection
     * <code>conn</code>. Total number of rows will be limited to
     * <code>maxRows</code> and
     * <code>maxStringColWidth</code> will be used to limit
     * the width of text columns (like a <code>VARCHAR</code> column).
     *
     * @param connection Database connection object (java.sql.Connection)
     * @param tableName Name of the database table
     * @param maxRows Number of max. rows to query and print
     * @param maxStringColWidth Max. width of text columns
     */
    public static void printTable(Connection connection, String tableName, int maxRows, int maxStringColWidth) {
    	Statement statment = null;
        ResultSet resultSet = null;
        boolean isPossible = true;
        boolean isInvalidMaxRow = maxRows < 1;
        isPossible = checkConnection(connection, tableName );
    	
        if(isPossible) {
	        try {
				if (isInvalidMaxRow) {
		            System.err.println("DBTablePrinter Info: Invalid max. rows number. Using default!");
		            maxRows = DEFAULT_MAX_ROWS;
		        }
	        
	            boolean isConnectionClosed = connection.isClosed();
	
				if (isConnectionClosed) {
	                System.err.println("DBTablePrinter Error: Connection is closed!");
				}
				
	            String sqlSelectAll = "SELECT * FROM " + tableName + " LIMIT " + maxRows;
	            statment = connection.createStatement();
	            resultSet = statment.executeQuery(sqlSelectAll);
	
	            printResultSet(resultSet, maxStringColWidth);
	
	        } catch (SQLException sqlException) {
	            System.err.println("SQL exception in DBTablePrinter. Message:");
	            System.err.println(sqlException.getMessage());
	        }  finally {
	            try {
	                if (statment != null) {
	                    statment.close();
	                }
	                if (resultSet != null) {
	                    resultSet.close();
	                }
	            } catch (SQLException ignore) {
	                // ignore
	            }
	        }
        }else {
        	return;
        }
    }

    /**
     * Overloaded method to print rows of a <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
     * ResultSet</a> to standard out using {@link #DEFAULT_MAX_TEXT_COL_WIDTH}
     * to limit the width of text columns.
     *
     * @param resultSet The <code>ResultSet</code> to print
     */
    public static void printResultSet(ResultSet resultSet) {
        printResultSet(resultSet, DEFAULT_MAX_TEXT_COL_WIDTH);
    }

    public static void isValidResultSet(ResultSet resultSet) {
    	try {
            boolean isEmptyResultSet = resultSet == null;
            boolean isClosedResultSet = resultSet.isClosed();

			if (isEmptyResultSet) {
                System.err.println("DBTablePrinter Error: Result set is null!");
            }
			if (isClosedResultSet) {
                System.err.println("DBTablePrinter Error: Result Set is closed!");
            }
        } catch (SQLException exception) {
            System.err.println("SQL exception in DBTablePrinter. Message:");
            System.err.println(exception.getMessage());
        }
    }
    
    public static int isValidColumnWidth(int maxStringColWidth) {
        boolean isInvalidColumnWidth = maxStringColWidth < 1;

    	if (isInvalidColumnWidth) {
            System.err.println("DBTablePrinter Info: Invalid max. varchar column width. Using default!");
            maxStringColWidth = DEFAULT_MAX_TEXT_COL_WIDTH;
        }
		return maxStringColWidth;
    }
    
    public static List<Column> getColumn(List<Column> columns,ResultSetMetaData resultSetMetaData, int columnCount)
    {
    	try{
    	 for (int i = 1; i <= columnCount; i++) {
             Column column = new Column(resultSetMetaData.getColumnLabel(i),
                     resultSetMetaData.getColumnType(i), resultSetMetaData.getColumnTypeName(i));
             column.setWidth(column.getLabel().length());
             column.setTypeCategory(whichCategory(column.getType()));
             columns.add(column);
         } 
    	}catch (SQLException exception) {
             System.err.println("SQL exception in DBTablePrinter. Message:");
             System.err.println(exception.getMessage());
         }
		return columns;
    }
    
    public static List<String> addTableName(List<String> tableNames, ResultSetMetaData resultSetMetaData, int columnCount)
    {
    	try{
    	 for (int i = 1; i <= columnCount; i++) {
             if (!tableNames.contains(resultSetMetaData.getTableName(i))) {
                 tableNames.add(resultSetMetaData.getTableName(i));
             }
         }
    	} catch (SQLException exception) {
            System.err.println("SQL exception in DBTablePrinter. Message:");
            System.err.println(exception.getMessage());
        }
		return tableNames;
    }
    
    public static String setPrintValueType(int category, Column columnOfI, ResultSet resultSet, int index, int maxStringColWidth)
    {
    	String value="";
    	try {
	    	if (category == CATEGORY_OTHER) {
	
	            // Use generic SQL type name instead of the actual value
	            // for column types BLOB, BINARY etc.
	            value = "(" + columnOfI.getTypeName() + ")";
	
	        } else {
					value = resultSet.getString(index+1) == null ? "NULL" : resultSet.getString(index+1);
	        }
	        switch (category) {
	            case CATEGORY_DOUBLE:
	
	                // For real numbers, format the string value to have 3 digits
	                // after the point. THIS IS TOTALLY ARBITRARY and can be
	                // improved to be CONFIGURABLE.
	                if (!value.equals("NULL")) {
	                    Double dValue = resultSet.getDouble(index+1);
	                    value = String.format("%.3f", dValue);
	                }
	                break;
	
	            case CATEGORY_STRING:
	
	                // Left justify the text columns
	                columnOfI.justifyLeft();
	
	                // and apply the width limit
	                if (value.length() > maxStringColWidth) {
	                    value = value.substring(0, maxStringColWidth - 3) + "...";
	                }
	                break;
	        }
    	} catch (SQLException sqlException) {
			// TODO Auto-generated catch block
			sqlException.printStackTrace();
		}
    	
		return value;
    }
    
    /**
     * Overloaded method to print rows of a <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
     * ResultSet</a> to standard out using <code>maxStringColWidth</code>
     * to limit the width of text columns.
     *
     * @param resultSet The <code>ResultSet</code> to print
     * @param maxStringColWidth Max. width of text columns
     */
    public static void printResultSet(ResultSet resultSet, int maxStringColWidth) {
    	
        try {
        	isValidResultSet(resultSet);
        	maxStringColWidth = isValidColumnWidth(maxStringColWidth);
			
            // Get the meta data object of this ResultSet.
            ResultSetMetaData rsmd;
            rsmd = resultSet.getMetaData();

            // Total number of columns in this ResultSet
            int columnCount = rsmd.getColumnCount();

            // List of Column objects to store each columns of the ResultSet
            // and the String representation of their values.
            List<Column> columns = new ArrayList<>(columnCount);

            // List of table names. Can be more than one if it is a joined
            // table query
            List<String> tableNames = new ArrayList<>(columnCount);
            
            // Get the columns and their meta data.
            // NOTE: columnIndex for rsmd.getXXX methods STARTS AT 1 NOT 0
            columns = getColumn(columns, rsmd, columnCount);
            tableNames = addTableName(tableNames, rsmd, columnCount);

            // Go through each row, get values of each column and adjust
            // column widths.
            int rowCount = 0;
                        
            while (resultSet.next()){
            	
                // NOTE: columnIndex for rs.getXXX methods STARTS AT 1 NOT 0
                for (int index = 0; index < columnCount; index++) {
                    Column columnOfI = columns.get(index);
           
                    int category = columnOfI.getTypeCategory();
                    String value = setPrintValueType(category, columnOfI, resultSet, index, maxStringColWidth);

                    // Adjust the column width
                    columnOfI.setWidth(value.length() > columnOfI.getWidth() ? value.length() : columnOfI.getWidth());
                    columnOfI.addValue(value);
                } // END of for loop columnCount
                rowCount++;

            } // END of while (rs.next)
			
            /*
            At this point we have gone through meta data, get the
            columns and created all Column objects, iterated over the
            ResultSet rows, populated the column values and adjusted
            the column widths.

            We cannot start printing just yet because we have to prepare
            a row separator String.
             */

            // For the fun of it, I will use StringBuilder
            StringBuilder stringToPrint = new StringBuilder();
            StringBuilder rowSeparator = new StringBuilder();

            /*
            Prepare column labels to print as well as the row separator.
            It should look something like this:
            +--------+------------+------------+-----------+  (row separator)
            | EMP_NO | BIRTH_DATE | FIRST_NAME | LAST_NAME |  (labels row)
            +--------+------------+------------+-----------+  (row separator)
             */

            // Iterate over columns
            for (Column column : columns) {
                int width = column.getWidth();

              // Center the column label
                String tempStringToPrint;
                String name = column.getLabel();
                int remainWidthLength = width - name.length();

                if ((remainWidthLength%2) == 1) {
                    // diff is not divisible by 2, add 1 to width (and diff)
                    // so that we can have equal padding to the left and right
                    // of the column label.
                    width++;
                    remainWidthLength++;
                    column.setWidth(width);
                }

                int paddingSize = remainWidthLength/2; // InteliJ says casting to int is redundant.

                // Cool String repeater code thanks to user102008 at stackoverflow.com
                // (http://tinyurl.com/7x9qtyg) "Simple way to repeat a string in java"
                String padding = new String(new char[paddingSize]).replace("\0", " ");

                tempStringToPrint = "| " + padding + name + padding + " ";
              // END centering the column label

                stringToPrint.append(tempStringToPrint);

                rowSeparator.append("+");
                rowSeparator.append(new String(new char[width + 2]).replace("\0", "-"));
            }

            String lineSeparator = System.getProperty("line.separator");

            // Is this really necessary ??
            lineSeparator = lineSeparator == null ? "\n" : lineSeparator;

            rowSeparator.append("+").append(lineSeparator);

            stringToPrint.append("|").append(lineSeparator);
            stringToPrint.insert(0, rowSeparator);
            stringToPrint.append(rowSeparator);

            String tableNameTemplate = "";
            for (String name : tableNames) {
                tableNameTemplate = tableNameTemplate + name +",";
            }

            String info = "Printing " + rowCount;
            info += rowCount > 1 ? " rows from " : " row from ";
            info += tableNames.size() > 1 ? "tables " : "table ";
            info += tableNameTemplate.toString();

            System.out.println(info);

            // Print out the formatted column labels
            System.out.print(stringToPrint.toString());

            String format;

            // Print out the rows
            for (int i = 0; i < rowCount; i++) {
                for (Column c : columns) {

                    // This should form a format string like: "%-60s"
                    format = String.format("| %%%s%ds ", c.getJustifyFlag(), c.getWidth());
                    System.out.print(
                            String.format(format, c.getValue(i))
                    );
                }

                System.out.println("|");
                System.out.print(rowSeparator);
            }

            System.out.println();

            /*
                Hopefully this should have printed something like this:
                +--------+------------+------------+-----------+--------+-------------+
                | EMP_NO | BIRTH_DATE | FIRST_NAME | LAST_NAME | GENDER |  HIRE_DATE  |
                +--------+------------+------------+-----------+--------+-------------+
                |  10001 | 1953-09-02 | Georgi     | Facello   | M      |  1986-06-26 |
                +--------+------------+------------+-----------+--------+-------------+
                |  10002 | 1964-06-02 | Bezalel    | Simmel    | F      |  1985-11-21 |
                +--------+------------+------------+-----------+--------+-------------+
             */

        } catch (SQLException sqlException) {
            System.err.println("SQL exception in DBTablePrinter. Message:");
            System.err.println(sqlException.getMessage());
        }
    }

    private static boolean checkIntType(int type)
    {
    	boolean isIntType = true;
    	if (type == Types.BIGINT || type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER) {
    		isIntType=true;
    	}else {
    		isIntType=false;
    	}
    	return isIntType;
    }
    
    private static boolean checkDoubleType(int type)
    {
    	boolean isDoubleType = true;
    	if (type ==  Types.REAL || type == Types.DOUBLE|| type == Types.DECIMAL ){
    		isDoubleType=true;
    	}else {
    		isDoubleType=false;
    	}
    	return isDoubleType;
    }
    
    private static boolean checkBooleanType(int type)
    {
    	boolean isBooleanType = true;
    	if (type == Types.DATE || type == Types.TIME|| type == Types.TIMESTAMP|| type == Types.BOOLEAN) {
    		isBooleanType=true;
    	}else {
    		isBooleanType=false;
    	}
    	return isBooleanType;
    }
    
    private static boolean checkStringType(int type)
    {
    	boolean isStringType = true;
    	if (type == Types.VARCHAR|| type == Types.NVARCHAR|| type == Types.LONGVARCHAR|| type == Types.LONGNVARCHAR|| type == Types.CHAR|| type == Types.NCHAR ) {
    		isStringType=true;
    	}else {
    		isStringType=false;
    	}
    	return isStringType;
    }
    
    /**
     * Takes a generic SQL type and returns the category this type
     * belongs to. Types are categorized according to print formatting
     * needs:
     * <p>
     * Integers should not be truncated so column widths should
     * be adjusted without a column width limit. Text columns should be
     * left justified and can be truncated to a max. column width etc...</p>
     *
     * See also: <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
     * java.sql.Types</a>
     *
     * @param type Generic SQL type
     * @return The category this type belongs to
     */
    private static int whichCategory(int type) {
        boolean isIntType =  checkIntType(type);
        boolean isDoubleType = checkDoubleType(type);
        boolean isBooleanType = checkBooleanType(type);
        boolean isStringType = checkStringType(type);
        int resultType;
	        if(isIntType) {
	        	resultType = CATEGORY_INTEGER;
	        }else if(isDoubleType) {
		    	resultType = CATEGORY_DOUBLE;
	        }
	        else if(isBooleanType) {
		    	resultType = CATEGORY_BOOLEAN;
	        }
	        else if(isStringType) {
		    	resultType = CATEGORY_STRING;
	        }
	        else {
	        	resultType = CATEGORY_OTHER;
	        }
	        
        return resultType;
        }
}
