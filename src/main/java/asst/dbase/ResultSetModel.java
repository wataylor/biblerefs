/* @name ResultSetModel.java

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: ResultSetModel.java,v $
    Revision 1.4  2002/12/17 03:49:44  asst
    not crash on null columnlabel

    Revision 1.3  2002/10/15 05:28:39  asst
    documentation

    Revision 1.2  2002/08/18 03:54:52  asst
    documentation

    Revision 1.1.1.1  2002/04/09 03:20:25  zonediet
    first import

*/

package asst.dbase;

import java.lang.RuntimeException;

import java.sql.*;
import java.util.Vector;

import asst.dbase.VariableSetModel;

/**
 * Implements a JTable model which generates its contents based on a
 * JDBC result set.  It is up to the caller to close the result set.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname> */

public class ResultSetModel extends VariableSetModel {

/** Obligatory null constructor */
  public ResultSetModel() {
  }

  /** Convert a string into a string with a space before every case change.
      @param inString input string
      @return string with spaces inserted before case changes*/
  public String breakOnCaseChange(String inString) {
    StringBuffer sb = new StringBuffer(inString.length()+5);
    char[] chs = inString.toCharArray();
    int i;
    int j = chs.length-1;
    boolean iLo;
    boolean nUp;
    /**/

    if ((inString == null) || (inString.length() <= 0)) { return ""; }

    for (i=0; i<j; i++) {
      sb.append(chs[i]);
      iLo = Character.isLowerCase(chs[i]);
      nUp = Character.isUpperCase(chs[i+1]);
      if (iLo && nUp) { sb.append(' '); }
    }
    sb.append(chs[i]);
    return sb.toString();
  }

  /** Set column labels based on the meta data in a result set.  This
      converts the SQL table column names from the result set meta
      data into an array and sets model column labels from that.  A
      space is inserted into each table column label whenever case
      changes in the column name so that <CODE>OrderNo</code> becomes
      <CODE>Order No</code>.  It is up to the caller to close the
      result set.
  @param results from an SQL query.
  @return number of columns defined in the result set.
  @exception RuntimeException on SQL error. */
  public int setColumnHeadings(ResultSet results) {
    int i;
    int cols;			// Number of columns
    ResultSetMetaData meta;	// Meta data with column names
    /**/

    cols = 0;			// Guarantee a value
    try {
      meta = results.getMetaData();
      cols = meta.getColumnCount();
      colNames = new String[cols]; // New array of the right length

      for (i=0; i<cols; i++) {	// Record column names
	colNames[i] = this.breakOnCaseChange(meta.getColumnLabel(i+1));
      }

      this.setColumnHeadings(colNames);
    } catch (SQLException e) {
      throw new RuntimeException("setColumnHeadings excpt " + e.toString());
    }
    return cols;
  }

/** Generate model rows and column labels from a result set.  It is up
    to the caller to close the result set.
    @param results from an SQL query.
    @exception RuntimeException on SQL error.*/
  public void setResultSet(ResultSet results) {
    int i;
    int cols;			// Number of columns
    String[] oneRow;		// Values from one row
    /**/

    cols = this.setColumnHeadings(results); // Label the columns

    try {
      while (results.next()) {
	oneRow = new String[cols];
	for (i=0; i<cols; i++) {
	  oneRow[i] = results.getString(i+1);
	  // System.out.print("r " + i + " vs " + oneRow[i]);
	}
	// System.out.println();
	this.addOneRow(oneRow);
      }
      this.repaintTable();
    } catch (SQLException e) {
      throw new RuntimeException("setResultSet excpt " + e.toString());
    }
  }

  /** Set the model rows and columns from any SQL query.  The
   ResultSet is generated in the method and is closed after use.
   @param query to be passed to the database.  It is up to the caller
   to supply a proper query for the database.
   @exception RuntimeException on SQL error.*/

  public void setResultSetFromQuery(String query) {
    Statement getRow;		// Retrieve all the rows
    ResultSet results;		// List of all values in the query
    /**/

    try {
      getRow  = DataBase.connDB.createStatement();
      results = getRow.executeQuery(query);
      this.setResultSet(results);
      results.close();
      getRow.close();
    } catch (SQLException e) {
      throw new RuntimeException("setResultSetFromQuery excpt " + query + " " +
				 e.toString());
    }
  }
}
