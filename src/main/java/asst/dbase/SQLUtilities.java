/* @name SQLUtilities.java

Miscellaneous SQL utilities.

    Copyright (c) 2001 by Advanced Systems and Software Technologies.  All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SQLUtilities.java,v $
    Revision 1.10  2007/09/20 23:24:13  asst
    use generic types

    Revision 1.9  2002/11/24 04:05:45  asst
    empty vector if query returns no results

    Revision 1.8  2002/11/02 04:47:27  asst
    put spaces in vectorized query

    Revision 1.7  2002/10/15 05:29:13  asst
    improved error handling

    Revision 1.6  2002/10/06 01:36:46  asst
    first upload

    Revision 1.5  2002/09/22 03:56:22  asst
    added AnyStatement method

    Revision 1.4  2002/09/05 02:13:58  asst
    added AnyStatement method

    Revision 1.3  2002/08/18 03:38:08  asst
    documentation

    Revision 1.2  2002/07/13 17:56:05  zonediet
    documentation

    Revision 1.1.1.1  2002/04/09 03:20:36  zonediet
    first import
<br>

*/

package asst.dbase;

import java.sql.*;

import java.text.NumberFormat;

import java.util.Vector;

/**
 * Miscellaneous SQL data manipulation utilities.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class SQLUtilities {

  /** Complex classes need empty strings */
  public static final String ES = "";

  /** Number formatter which formts floating point numbers with two
   * digits after the decimal point. */
  public static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
  static {
    NUMBER_FORMAT.setMaximumFractionDigits(2);
    NUMBER_FORMAT.setMinimumFractionDigits(2);
    NUMBER_FORMAT.setGroupingUsed(false); // No commas in numbers
  }

  /** Currency formatter. */
  public static final NumberFormat PRETTY_PRICE_FORMAT =
  NumberFormat.getCurrencyInstance();

  /** Obligatory no-argument constructor. */
  public SQLUtilities() {
  }

  /** Turn the distinct values from an arbitrary SQL table and column
      into a vector.  If the filter and value are both non-null, a row
      must have a value in the filter column which matches value in
      order to be included in the vector.
  @param table specifies the database table from which values are drawn.
  @param column specifies the table column from which values are drawn
  @param filter names a column whose values must match the specified
  value or null in which case all distinct values are accepted.
  @param value required to be found in the filter column.
  @return vector of distinct column values which match the filter condition.*/
  public static Vector<String> VectorizeColumn(String table,  String column,
					       String filter, String value)
    throws java.sql.SQLException {
    String query;		// The query to select the values
    /**/

    if ((table  == null) || ES.equals(table) ||
	(column == null) || ES.equals(column)) return null;

    query   = "select distinct " + column + " from " + table;
    if ((filter != null) && (value != null)) {
      String quotationMark;	// String in which to wrap comparand
      quotationMark = ES;
      for (int i=0; i<value.length(); i++) {
	if (!Character.isDigit(value.charAt(i))) {
	  quotationMark = "'";
	  break;
	}
      }
      query = query + " where " + filter + " = " +
	quotationMark + value + quotationMark;
    }
    query   = query + " order by " + column;

    return SQLUtilities.VectorizeQuery(query);
  }

  /** Execute any update or insert statement against the database.
      @param query insert or update query
   @exception RuntimeException on SQL error. */
  public static void AnyStatement(String query) throws RuntimeException {
    Statement stmt = null;
    /**/

    try {			// stmt.close may throw exception
      try {
	stmt = DataBase.connDB.createStatement();
	stmt.execute(query);
      } finally {
	if (stmt != null) { stmt.close(); }
      }
    } catch (SQLException e) {
      throw new RuntimeException("anyStatement excp " + query + " " +
				 e.toString());
    }
  }

  /** Turn the string or strings in a result set of a query into a vector
   which is intended to be used to generate a selection list.
   @param query which has been properly formatted for the current database
   @return vector with one element per row of results.  If more than
   one column was selected, the column values from a row are
   concatenated and a | is put between the first column and the rest
   of the columns in each row; the code to generate the selection list
   splits the string so that the string before the | is the selection
   value and the rest of the string is displayed.  The vector is empty
   if the search has no results.
  @exception RuntimeException on SQL error.*/
  public static Vector<String> VectorizeQuery(String query) {
    Vector<String> list;		// Results returned to caller
    Statement getRow = null;	// Retrieve all the rows
    StringBuffer value;		// Result of the query
    ResultSet results = null;	// List of all values in the query
    ResultSetMetaData meta = null;
    int columnCount;
    int i;
    /**/

    list = new Vector<String>();

    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);
      meta = results.getMetaData();
      columnCount = meta.getColumnCount();
      while (results.next()) {
	value = new StringBuffer();
	for (i=1; i<=columnCount; i++) {
	  if (i == 2) { value.append("|"); }
	  if (i > 2)  { value.append("|"); }
	  value.append(results.getString(i));
	}
	if (value.length() > 0) {
	  list.addElement(value.toString());
	}
      }
    } catch (SQLException e) {
      throw new RuntimeException("VectorizeQuery excp " + query + " " +
				 e.toString());
    } finally {
      try {
	if (results != null) { results.close(); }
	getRow.close();
      } catch (SQLException e) {
      }
    }
    return list;
  }

  /** Turn the meta data of a result set into information on both the
      err and out System print streams.  The purpose is to aid in
      copying database tables; the output sent to the err stream
      characterizes the table and the string sent to the out stream
      contains all of the database column names.  If this line is put
      at the beginning of a file which contains table data, the
      resulting file can be read by <CODE>FileIntoDB</code> without a
      map file.
  @param results of a SQL query
  @return column count from the result set*/
  public static int DumpMetaColumnInformation(ResultSet results)
    throws SQLException {
    ResultSetMetaData meta = null;
    int columnCount;
    int i;
    StringBuffer sb;
    /**/

    meta = results.getMetaData();
    columnCount = meta.getColumnCount();

    System.err.println("TypeName, DisplaySize, Precision, Column Label, Column Name");

    sb = new StringBuffer();
    for (i = 1; i<=columnCount; i++) {
      if (i != 1) { sb.append("\t"); }
      sb.append(meta.getColumnTypeName(i));
    }
    System.err.println(sb);

    sb = new StringBuffer();
    for (i = 1; i<=columnCount; i++) {
      if (i != 1) { sb.append("\t"); }
      sb.append(meta.getColumnDisplaySize(i));
    }
    System.err.println(sb);

    sb = new StringBuffer();
    for (i = 1; i<=columnCount; i++) {
      if (i != 1) { sb.append("\t"); }
      sb.append(meta.getPrecision(i));
    }
    System.err.println(sb);

    sb = new StringBuffer();
    for (i = 1; i<=columnCount; i++) {
      if (i != 1) { sb.append("\t"); }
      sb.append(meta.getColumnLabel(i));
    }
    System.err.println(sb);

    sb = new StringBuffer();
    for (i = 1; i<=columnCount; i++) {
      if (i != 1) { sb.append("\t"); }
      sb.append(meta.getColumnName(i));
    }
    System.err.println(sb);
    System.out.println(sb);

    return columnCount;
  }

  /** Safely convert a string to an integer, returning 0 in case of
      difficulty.
  @param val input string
  @return integer representation of the string or 0*/
  public static int integerFromString(String val) {
    try {
      return Integer.parseInt(val);
    } catch (Exception e) {
    }
    return 0;
  }

  /** Safely convert a string to a floating point number, returning 0
      in case of difficulty.
  @param val input string
  @return floating point representation of the string or 0*/
  public static float floatFromString(String val) {
    try {
      return Float.parseFloat(val);
    } catch (Exception e) {
    }
    return (float)0.0;
  }

  /** Safely convert a result set string to a non-null string,
      returning a non-null empty string if the result set does not
      have enough strings or if the selected string is null.  The
      effect is to be able to scan a result set without generating a
      SQLException or having to process a null string.
  @param result result set from a prior SQL query.
  @param w selects a string from the result set
  @return the string or the empty string if the column is null or if w
  is out of range for the result set*/
  public static String stringFromResult(ResultSet result, int w) {
    String str;
    /**/

    try {
      str = result.getString(w);
    } catch (Exception e) { str = null; }
    if (str == null) { str = ES; }
    return str;
  }

  /** Safely convert a result-set element to a floating point value,
      returning 0 if the result set does not have enough strings or if
      the selected string is null.  The effect is to be able to scan a
      result set without generating a SQLException or having to
      process a null string.
  @param result set from a prior SQL query.
  @param w selects a string from the result set
  @return the floating point representation of the column or 0 if the
  column is null or if w is out of range for the result set*/
  public static float floatFromResult(ResultSet result, int w) {
    try {
      return SQLUtilities.floatFromString(result.getString(w));
    } catch (Exception e) { }
    return (float)0.0;
  }

  /** Safely convert a result-set element into a floating point value
      and format it as currency.  Formats 0 if the result set does not
      have enough strings or if the selected string is null.  The
      effect is to be able to scan a result set without generating a
      SQLException or having to process a null string.
  @param result result set from a prior SQL query.
  @param w selects a string from the result set
  @return the currency representation of the column or 0 if the column
  is null or if w is out of range for the result set*/
  public static String priceFromResult(ResultSet result, int w) {
    return PRETTY_PRICE_FORMAT.format(SQLUtilities.floatFromResult(result, w));
  }
}
