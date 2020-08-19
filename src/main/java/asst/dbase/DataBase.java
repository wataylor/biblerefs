/* DataBase.java

    Copyright (c) 2001 by Advanced Systems and Software Technologies,
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: DataBase.java,v $
    Revision 1.18  2006/06/26 10:40:47  asst
    log message on connect fail

    Revision 1.17  2005/08/09 18:58:38  asst
    took url user and pass out of conn err

    Revision 1.16  2004/02/14 18:51:54  asst
    finally close

    Revision 1.15  2003/08/07 17:12:06  asst
    identifyDB, listDBTables

    Revision 1.14  2003/07/11 00:08:59  asst
    method to return database name

    Revision 1.13  2003/01/03 04:24:58  asst
    fixed rattler not to throw fatal exception

    Revision 1.12  2002/12/31 18:50:13  asst
    rattle db exception

    Revision 1.11  2002/11/13 16:37:52  asst
    IDFromTableColumn

    Revision 1.10  2002/10/15 05:28:10  asst
    better error messages

    Revision 1.9  2002/09/24 02:32:04  asst
    StringFromTableColumn

    Revision 1.8  2002/09/08 02:58:30  asst
    improved connection failure error messages

    Revision 1.7  2002/09/05 02:13:06  asst
    add query to exception reporting

    Revision 1.6  2002/09/03 23:33:38  asst
    added dbaseProduct property for MSSQL

    Revision 1.5  2002/08/28 03:37:13  asst
    documentation

    Revision 1.4  2002/08/17 16:21:48  asst
    documentation

    Revision 1.3  2002/07/17 15:51:36  zonediet
    documentation

    Revision 1.2  2002/06/17 17:14:25  zonediet
    private database connection method

    Revision 1.1.1.1  2002/04/09 03:20:09  zonediet
    first import

*/

package asst.dbase;

import java.sql.*;

import java.util.Vector;

/**
 * Class which holds variables such as the data base connection.

 * @author money
 * @since 2000 01
 */

public class DataBase {

  /** Complex classes need empty strings.  */
  public static final String ES = "";

  /**
   * The connection variable which is opened by the openDB class
   * method call */

  public static Connection connDB;
  /** The name of the database owner; some database vendors require
   * that the owner name be prepended to table names.*/
  public static String dbaseOwner;
  /** The user name which is associated with the successful
   * connection.  It may or may not have to be the same as the owner
   * name depending on datbase vendor policy.*/
  public static String dbaseUser;
         static String dbasePasswd; // Successful password
  /** URL of a successful connection. */
         static String dbaseURL;
  /** Name of the default database sequence which is used to obtain
   * new row numbers when rows are created.  Since most applicatinos
   * have multiple tables into which new rows are created, most
   * callers pass in a sequence name when creating new rows.*/
  public static String dbaseSequence;
  /** Database connections go stale after some vendor-dependent time
   * interval of inactivity.  The rattler SQL command is used to
   * exercise the connection when a new property connection is
   * established.  If this succeeds, the connection is assumed to be
   * still alive, if not, the connection is assumed to have been
   * closed and a new connection is established.*/
  public static String dbaseRattler;
  /** Various databases have different commands for getting the next
   * ID from an autoincrement table.  This string specifies how to get
   * the next ID from the current database.*/
  public static String dbaseGetNextValue = "select LAST_INSERT_ID()";
  /** Flag to set the database software utilities into debugging mode.*/
  public static boolean dbaseDebug = false;

  /**
   * Method to connect to the database given a url, a driver, a user
   * name, and a password.

   * @param driver the path to the driver for the JDBC database.  This
   * smacks of hard-coding; it would be better to discover the driver
   * associated with the URL but this awaits further understanding of
   * introspection.

   * @param url the URL of the database.  This usually includes the
   * protocol, the data base name, the host name, the port, and the
   * name of the data base.  URL format seems to be database vendor
   * dependent.

   * @param user is the username which is authorized to log on to the
   * database.  If both the user name and password are "NONE" the user
   * name and password are assumed to be included in the URL so only
   * the URL is passed to <code>getConnection()</code>.  This is
   * highly vendor-dependent.

   * @param pass the password associated with the user name.  If both
   * the user name and password are "NONE" the user name and password
   * are assumed to be included in the URL so only the URL is passed
   * to <code>getConnection()</code>.  This is highly
   * vendor-dependent.

  @exception RuntimeException if the database driver cannot be loaded
  or if the database connection cannot be opened. */

  static public void openDB(String driver, String url, String user,
			    String pass) throws RuntimeException {
    boolean bof;
    /**/

    try {
      // Load the JDBC driver
      Class.forName(driver);
      // DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    } catch (Exception e) {
      String errMsg = "Database driver load exception " + driver +
	"\n" + e.toString();
      DataBase.connDB = null;	// Make sure it can't be used
      DataBase.dbaseUser = null;
      RuntimeException f = new RuntimeException(errMsg);
      throw f;
    }

    DataBase.dbaseUser   = user;
    DataBase.dbasePasswd = pass;
    DataBase.dbaseURL    = url;

    bof = false;
    try {
      // Connect to the database
      if (user.equals("NONE") && pass.equals("NONE")) {
	// Assume password and user are buried in the URL
	DataBase.connDB = DriverManager.getConnection(url);
      } else {
	bof = true;
	DataBase.connDB = DriverManager.getConnection(url, user, pass);
      }
    } catch (Exception e) {
      String errMsg = "Database connection exception " + e.toString() +
	"\nURL " + "\nUser " + "\nPassword " +
	"\nDriver " + driver;
      if (bof) {
	errMsg += "\nPassed separate URL, User ID, and Password";
      } else {
	errMsg += "\nUser ID and Password are part of the URL";
      }
      DataBase.connDB = null;	// Make sure it can't be used
      System.out.println("URL " + url + "\nUser " + user +
			 "\nPassword " + pass + "\nDriver " + driver);
      RuntimeException f = new RuntimeException(errMsg);
      throw f;
    }
  }

  /** Return a string which identifies the database to which
   * connection has been made or null.*/
  public static String identifyDB() throws SQLException {
    DatabaseMetaData dbMeta;
    StringBuffer answer;
    /**/

    if (DataBase.connDB == null) { return null; }
    dbMeta = DataBase.connDB.getMetaData();
    answer = new StringBuffer(300);

    answer.append("# Product " + dbMeta.getDatabaseProductName() + "\n");

    // Some JDBC vendors do not support all of the meta data methods.
    try {
      answer.append("# Version " + dbMeta.getDatabaseMajorVersion() +
		    "." + dbMeta.getDatabaseMinorVersion() + "\n");
    } catch (Error e) {
      answer.append("# getDatabaseMajorVersion or getDatabaseMinorVersion failed\n");
    } catch (Exception e) {
      answer.append("# getDatabaseMajorVersion or getDatabaseMinorVersion failed\n");
    }
    answer.append("# Driver " + dbMeta.getDriverMajorVersion() +
		  "." + dbMeta.getDriverMinorVersion() +
		  " " + dbMeta.getDriverVersion() +
		  " " + dbMeta.getDriverName() + "\n");
    try {
      answer.append("# JDBC " + dbMeta.getJDBCMajorVersion() +
		    "." + dbMeta.getJDBCMinorVersion() + "\n");
    } catch (Error e) {
      answer.append("# getJDBCMajorVersion or getJDBCMinorVersion failed\n");
    } catch (Exception e) {
      answer.append("# getJDBCMajorVersion or getJDBCMinorVersion failed\n");
    }
    answer.append("# Separator " + dbMeta.getCatalogSeparator() + "\n");
    return answer.toString();
  }

  /** Return a list of all the tables in the database*/
  public static String[] listDBTables() throws SQLException {
    DatabaseMetaData dbMeta;
    ResultSet results;
    Vector vec;
    String[] tableTypes;	// Ask for all the types in the system
    String[] tableNames;	// Answers to return
    /**/

    if (DataBase.connDB == null) { return null; }
    dbMeta = DataBase.connDB.getMetaData();

    results = dbMeta.getTableTypes();
    vec = new Vector();
    while (results.next()) {
      vec.add(results.getString(1));
    }
    results.close();
    tableTypes = new String[vec.size()];
    tableTypes = (String[])vec.toArray(tableTypes);

    results = dbMeta.getTables(DataBase.parseDBName(), "%", "%", tableTypes);
    vec = new Vector();
    while (results.next()) {
//    	System.out.println("Table " + results.getString(1) + " " + // Catalog
//    			   results.getString(2) + " " +	// Schema
//    			   results.getString(3) + " " +	// Table
//    			   results.getString(4) + " " +	// Type
//    			   results.getString(5));
      vec.add(results.getString(3));
    }
    results.close();
    if (vec.size() <= 0) { return null; }

    tableNames = new String[vec.size()];
    tableNames = (String[])vec.toArray(tableNames);
    java.util.Arrays.sort(tableNames); // SQL has an arrays type
    return tableNames;
  }

  /** Method to parse the database name out of the URL.  The database
   * name may or may not be the same as the name of the properties
   * file.  For the moment, this works only with MySQL URLs.  MS SQL
   * server uses semicolons and keyword parameters, for example. */
  public static String parseDBName() {
    int ix;
    int jx;
    /**/

    if (DataBase.dbaseURL == null) { return null; }
    if ( (ix = DataBase.dbaseURL.indexOf("//")) < 0) { return null; }
    ix += 2;			// Point beyond the // at the host name
    if ( (jx = DataBase.dbaseURL.indexOf("/", ix)) < 0) { return null; }
    ix = jx + 1;		// Point beyond / at database name
    if ( (jx = DataBase.dbaseURL.indexOf("?", ix)) < 0) {
      return DataBase.dbaseURL.substring(ix); // No ?, dbname runs to end
    }
    return DataBase.dbaseURL.substring(ix, jx); // dbname ends before ?
  }

  /** Return a private connection to the database or throw a run-time
      exception if this is not possible.  This is intended for systems
      which execute long queries.  It is up to the caller to close the
      connection when it is no longer needed.
  @return database connection.
  @exception RuntimeException if a new connection cannot be obtained
  from the driver manager. */
  public static Connection getPrivateConnection() throws RuntimeException {
    try {
      // Connect to the database
      if (DataBase.dbaseUser.equals("NONE") &&
	  DataBase.dbasePasswd.equals("NONE")) {
	// Assume password and user are buried in the URL
	return DriverManager.getConnection(DataBase.dbaseURL);
      } else {
	return DriverManager.getConnection(DataBase.dbaseURL,
					   DataBase.dbaseUser,
					   DataBase.dbasePasswd);
      }
    } catch (Exception e) {
      String errMsg = "Database connection exception " + e.toString() +
	" URL " + DataBase.dbaseURL + " User " + DataBase.dbaseUser +
	" Password " + DataBase.dbasePasswd;
      DataBase.connDB = null;	// Make sure it can't be used
      RuntimeException f = new RuntimeException(errMsg);
      throw f;
    }
  }

  /** If a test statement has been defined, exercise the database
      connection to make sure that the connection has not timed out or
      stopped working for any reason.  If it has, close the connection
      so that the caller can reopen it.. */
  static public void rattleDB() {
    if ((DataBase.dbaseRattler != null) && !ES.equals(DataBase.dbaseRattler)) {
      Statement getRow = null;	// Retrieve the sequence number
      /**/
      try {
	getRow = DataBase.connDB.createStatement();
	getRow.execute(DataBase.dbaseRattler);
	getRow.close();
      } catch (SQLException e) {
	System.out.println("rattleDB excp " + e.toString());
	DataBase.closeDB();	// Did not work, close it
      } finally {
	try {
	  if (getRow != null) { getRow.close(); }
	} catch (Exception e) { System.out.println("rattleDB excp2 " +
						   e.toString()); }
      }
    }
  }

  /** Get the next sequence number and set an integer column to its
      value for the specified table.  Uses the table name as a
      sequence if <code>sequence.equals(table)</code>, this supports
      MySQL and MSSSQLServer which use autoincrementing columns to
      create new row IDs.  The column
      name defaults to "ID" unless otherwise specified.  The SQL
      function used to get the next ID number after the increment
      depends on the database vendor which is specified in the
      properties file.</p>

      <P>If the sequence name and table name are different, uses the
      sequence to get the next number and create a new row with that
      ID for the table ro tables, this supports Oracle which uses
      Sequence objects to create unique row numbers.</p>

      <P>This method creates new default rows in all tables, it should
      be called ONLY when the caller is CERTAIN that a new row should
      be created in one or more tables.  The new rows are created
      using default values for all columns other than the ID
      column.</p>

  <P>This method is synchronized to ensure that each caller gets the
  ID that corresponds to the call and that all new rows in related
  tables get the same key.  All related tables must have the same
  index column name which defaults to ID.

  @see asst.dbase.DBProp

  @param table the name of the table which is to receive a new row.
  @param otherTables an array of names of tables which are to
  receive a row with the same index as the new row in the
  <CODE>table</code>.
  @param seq the database sequence which defines the next available
  row number.  If this is the same as the table name, the table is
  assumed to be defined with an autoincrement column, otherwise, the
  sequence is asked for a new number.
  @param col the name of the column which the new row number indexes.
  This column must be an autoincrement column if the sequence name is
  the same as the table name.  The column name defaults to "ID" if
  this parameter is <CODE>null</code>.
  @return new row number for the specified table or tables.  This row
  has been inserted into the table or tables so that actual data may
  be inserted into it with the SQL <code>UPDATE</code> command.  the
  initial row has default values for all columns other than the ID
  column.
  @exception RuntimeException if there is an error inserting the new
  row or if there is an error retrieving the new row number or if
  there is an error inserting empty rows into the other tables.*/
  public static synchronized int
  nextIDForTable(String table, String[] otherTables, String seq, String col)
    throws RuntimeException {
    ResultSet results;		// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    String query = null;	// Exceptions report the value of query
    int i;			// The resulting int
    /**/

    if (seq == null) { seq = DataBase.dbaseSequence; }
    if (col == null) { col = "ID"; }

    i = 0;			// Returned if fails
    try {
      if (seq.equals(table)) {
	getRow = DataBase.connDB.createStatement();
	// MySQL auto increments ONLY when a 0 or NULL value is put
	// into an autoincrement column.  In effect, MySQL uses the ID
	// column of the table as a sequence.  The insert command
	// inserts default values into all columns other than the ID
	// column
	try {
	  query = "insert into " + table + " (" + col + ") values (NULL)";
	  getRow.execute(query);
	} catch (SQLException e) {
	  throw new RuntimeException("Insert new row excp table " + table +
				     " " + query + " " + e.toString());
	}
	// the LAST_INSERT_ID is good ONLY immediately after an insert
	// into an autoincrement column.  This retrieves the ID which
	// was given to the most recent insert and returns it to the
	// caller so that the caller knows the ID of the most recent
	// addition.
	query = dbaseGetNextValue;
	try {
	  results = getRow.executeQuery(query);
	  if (results.next()) {	// There were some rows in the table
	    i = results.getInt(1);
	  } else {		// The table was empty
	    i = 1;
	  }
	} catch (SQLException e) {
	  throw new RuntimeException("Get new row number table " + table +
				     " " + query + " " + e.toString());
	}
	// System.out.println(query + " " + i);
      } else {
	// Unlike MySQL, oracle supports named sequences.  The goal is
	// to get a new ID number from the sequence, then construct a
	// new row with that key for all related tables
	getRow  = DataBase.connDB.createStatement();
	query = "select NEXTVAL from " + DataBase.dbaseSequence;
	try {
	  results = getRow.executeQuery(query);
	  if (results.next()) {
	    i = results.getInt(1);
	    // Oracle tables do not know about sequences, update the
	    // main table
	    query = "insert into " + table + " (ID) values (" + i + ")";
	    getRow.execute(query);;
	  }
	} catch (SQLException e) {
	  throw new RuntimeException("Sequence increment " + table +
				     " " + query + " " + e.toString());
	}
      }
      // Now create key rows for all other tables.  The final result
      // is to return the new key to the caller, having inserted rows
      // with the new key in all associated tables.
      if (otherTables != null) {
	for (int j = 0; j < otherTables.length; j++) {
	  query = "insert into " + otherTables[j] + " (" + col +
	    " ) values (" + i + ")";
	  getRow.execute(query);
	}
      }
      results.close();
      getRow.close();
    } catch (SQLException e) {
      throw new RuntimeException("nextIDForTable " + query + " " +
				 e.toString());
    }
    return i;
  }

  /** Verify that a table contains an integer in the integer column
      named ID.  This method calls the more general method and passes
      a hard-coded column name.
      @param table name of the table to be interrogated.
      @param id the value to be sought in a column mamed "ID"
      @return true if the value is found, false otherwise.
  */
  public static boolean isIDInTable(String table, int id) {
    return DataBase.isIntInTableColumn(table, "ID", id);
  }

  /** Verify that a table contains an integer in the integer column
      whose name is passed in.
      @param table name of the table to be interrogated.
      @param column the name of the column to be examined.
      @param id the value to be sought in the specified column.
      @return true if the value is found, false otherwise. */
  public static boolean isIntInTableColumn(String table,String column,int id) {
    ResultSet results;		// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    boolean answer;
    String query;
    /**/

    answer = false;		// Speculate that it will not be in the table

    query = "select " + column + " from " + table +
      " where " + column + " = " + id;
    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);;
      if (results.next()) {
	answer = true;
      }
      results.close();
      getRow.close();
    } catch (Exception e) {
      throw new RuntimeException("isIntInTableColumn excp " + query + " " +
				 e.toString());
    }
    return answer;
  }

  /** Retrieve the value from a string column whose name is passed in
      and whose row is selected by a column named "ID".
      @param table name of the table to be interrogated.
      @param column the name of the column to be examined.
      @param id the row selector which is found in a column named ID.
      @return value found in the column or the empty string.
      @exception RuntimeException if there is an SQL error.*/
  public static String StringFromTableColumn(String table, String column,
					      int id) {
    ResultSet results = null;	// Results of the retrieval
    Statement getRow = null;	// Retrieve the sequence number
    String query;
    String answer = null;
    /**/

    query = "select " + column + " from " + table + " where ID=" + id;
    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);
      if (results.next()) {
	answer = results.getString(1);
      }
    } catch (Exception e) {
      throw new RuntimeException("StringFromTableColumn " + query + " " +
				 e.toString());
    } finally {
      try {
	if (results != null) { results.close(); }
	if (getRow  != null) { getRow.close(); }
      } catch (Exception e) {}
    }
    if (answer == null) { answer = ES; }
    return answer;
  }

  /** Retrieve the row number from a column whose name is passed in
      and whose row is selected by a column value.
      @param table name of the table to be interrogated.
      @param column the name of the column to be examined.
      @param value the column value which selects the row ID
      @return row where the value is found in the column or zero
      @exception RuntimeException if there is an SQL error.*/
  public static int IDFromTableColumn(String table, String column,
				      String value) {
    ResultSet results = null;		// Results of the retrieval
    Statement getRow = null;		// Retrieve the sequence number
    String query;
    /**/

    query = "select ID from " +table+ " where " +column+ "='" + value + "'";
    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);
      if (results.next()) {
	return results.getInt(1);
      }
    } catch (Exception e) {
      throw new RuntimeException("IDFromTableColumn " + query + " " +
				 e.toString());
    } finally {
      try {
	if (results != null) { results.close(); }
	if (getRow  != null) { getRow.close(); }
      } catch (Exception e) {}
    }

    return 0;
  }

  /** Verify that a table contains an specific character string in the
      named character column.
      @param table name of the table to be interrogated.
      @param column the name of the column to be examined.
      @param value the string value to be sought in the specified column.
      @return true if the value is found, false otherwise. */
  public static boolean isStringInTable(String table, String column,
					String value) {
    ResultSet results;		// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    String query;
    boolean answer;
    /**/

    answer = false;		// Speculate that it will not be in the table

    query = "select " + column + " from " + table +
      " where " + column + " = '" + value + "'";
    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);
      if (results.next()) {
	answer = true;
      }
      results.close();
      getRow.close();
    } catch (Exception e) {
      throw new RuntimeException("isStringInTable excpt " + query + " " +
				 e.toString());
    }
    return answer;
  }

  /** Close the database connection, throws a runtime exception if
   * called when the database debug flag is set.  This helps find
   * spurious calls which close the database. */
  static public void closeDB() {
    if (DataBase.connDB == null) return; // Might already be closed
    if (DataBase.dbaseDebug) {
      throw new RuntimeException("DataBase.closeDB called");
    }
    try {
      DataBase.connDB.close();
    } catch (SQLException e) {
      throw new RuntimeException("closeDB excp " + e.toString());
    }
    DataBase.connDB     = null;	// Make sure it's not closed again
  }
}
