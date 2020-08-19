/* @name ChangeLog.java

Log database changes

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: ChangeLog.java,v $
    Revision 1.6  2002/12/31 18:52:25  asst
    make it possible to return the log ID

    Revision 1.5  2002/08/13 00:31:26  zonediet
    documentation

    Revision 1.4  2002/07/17 18:48:07  zonediet
    typos

    Revision 1.3  2002/07/17 17:44:20  zonediet
    added comments to log

    Revision 1.2  2002/07/13 17:56:04  zonediet
    documentation

    Revision 1.1  2002/06/17 17:20:32  zonediet
    initial load
<br>

*/

package asst.dbase;

import java.sql.*;

import asst.dbase.DBProp;
import asst.dbase.DataBase;
import asst.dbase.Quotable;
import asst.dbase.SelectorManager;

/**
 * Create change log entries as database table fields are changed.
 * All values are expressed as strings regardless of the actual column
 * datatypes.  The log facility requires a table of the form:

<PRE>
DROP TABLE IF EXISTS ChangeLog;
CREATE TABLE ChangeLog (
  Modified timestamp(14) NOT NULL,
  TableName varchar(250) default '',
  ColumnName varchar(250) default '',
  RecordID varchar(250) default '',
  OldValue varchar(250) default '',
  NewValue varchar(250) default '',
  LoginID varchar(250) default '',
  PerpID  int default '0',
  Comment varchar(250) default '',
  ID int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT
);
</pre>

The ID column is incremented autmatically for each change event but
most IDs are not passed back to the caller.  Passing back an ID is
expensive because the method opens a custom database connection for
the sole purpose of writing the event and getting back the ID.

 * @author Web Work
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorManager
 */

public class ChangeLog {

  /** Obligatory Constructor. */
  public ChangeLog() {
  }

  /** Record a change event.  It is up to the caller to update the
      database table being changed, this method only creates a change log
      entry.  Creating change entries without updating the actual table
      may be useful in debugging situations.

      @param table name of the database table whose column value has changed.
      @param column name of the column in the database table whose
      value has changed.
      @param record identifies the record whose column value was
      changed.  It is up to the caller to supply a meaningful record ID.
      @param oldValue former value of the column or null.  It is up to
      the caller whether to supply the old value or not.  When a null
      value is supplied, the old value in the ChangeLog table is set to
      the SQL constant null.
      @param newValue new value of the column as a string.
      @param ID - identifies some person who is associated with the
      change.  This may be a string representing a login name or a string
      representing an integer which indexes another table.
  @param perpID a number which identifies whomever made the change.
  @param comment any additional string such as an IP address which
  enhances the value of the data.*/
  public static void LogAChange(String table, String column, String record,
				String oldValue,
				String newValue, String ID, int perpID,
				String comment) {
    Statement stmt = null;
    StringBuffer query = new StringBuffer();
    /**/

    if (oldValue == null) {
      oldValue = "'null'";
    } else {
      oldValue = Quotable.ConstructSQLCharacterValue(oldValue);
    }

     if (comment == null) {
      comment = "''";
    } else {
      comment = Quotable.ConstructSQLCharacterValue(comment);
    }
   // Outer try to cover creating the statements.
    try {
      stmt = DataBase.connDB.createStatement();
      query.append("insert into ChangeLog VALUES(null, '" + table + "', '" +
		   column + "', '" +
		   Quotable.QuoteForSQLString(record) + "', " +
		   oldValue + ", '" +
		   Quotable.QuoteForSQLString(newValue) + "', '" +
		   Quotable.QuoteForSQLString(ID) + "', " +
	           perpID + ", " + comment + ")");
      try {
	stmt.execute(query.toString());
      } catch (SQLException e) {
	System.out.println("LogAChange excp " + query.toString() +
			   " " + e.toString());
      }

    } catch (SQLException e) {
      System.out.println("LogAChange outer excp " + e.toString());
    } finally {
      try {
	if (stmt != null) { stmt.close(); }
      } catch (SQLException e) {
      }
    }
  }

  /** Record a change event.  It is up to the caller to update the
      database table being changed, this method only creates a change log
      entry.  Creating change entries without updating the actual table
      may be useful in debugging situations.

      @param table name of the database table whose column value has changed.
      @param column name of the column in the database table whose
      value has changed.
      @param record identifies the record whose column value was
      changed.  It is up to the caller to supply a meaningful record ID.
      @param oldValue former value of the column or null.  It is up to
      the caller whether to supply the old value or not.  When a null
      value is supplied, the old value in the ChangeLog table is set to
      the SQL constant null.
      @param newValue new value of the column as a string.
      @param ID identifies some person who is associated with the
      change.  This may be a string representing a login name or a string
      representing an integer which indexes another table.*/

  public static void LogAChange(String table, String column, String record,
				String oldValue,
				String newValue, String ID) {
    ChangeLog.LogAChange(table, column, record, oldValue, newValue, ID, 0,
			 (String)null);
  }

  /**
   * Scan a form array and record change events for all dirty fields
   * whose logging flags are set.  This routine does
   * <B>not</b> affect the dirty flags.
   * @param form array of selector manager objects, some of which may
     be dirty and some of which may be flagged to be logged when dirty.
     @param record identifies the record whose column value was
     changed.  It is up to the caller to supply a meaningful record
     ID.  The record ID is stored as a string so that it can handle
     numeric, alphanumeric, or multi-column keys.
     @param ID identifies some person who is associated with the
     change.  This may be a string representing a login name or a
     string representing an integer which indexes another table.
     @param perp a number which identifies whomever made the change;
     it is passed as an integer and stored as an integer.
     @param comment additional information to enhance the value of the entry*/
  public static void LogDirtyChanges(SelectorManager[] form, String record,
				     String ID, int perp, String comment) {
    int i;
    /**/

    for (i=0; (i<form.length && (form[i] != null)); i++) {
      if (form[i].getDirtyFlag() && form[i].getIsLogged()) {
	ChangeLog.LogAChange(form[i].getTable(), form[i].getColumn(), record,
			     form[i].getPriorChoice(), form[i].getChoice(),
			     ID, perp, comment);
      }
    }
  }

  /** Record a numbered change event and return its ID number.
   * Passing back an ID is expensive because the method opens a custom
   * database connection for the sole purpose of writing the event and
   * getting back the ID.</p>

<P>It is up to the caller to update the database table being changed,
this method only creates a change log entry.  Creating change entries
without updating the actual table may be useful in debugging
situations.

      @param table name of the database table whose column value has changed.
      @param column name of the column in the database table whose
      value has changed.
      @param record identifies the record whose column value was
      changed.  It is up to the caller to supply a meaningful record ID.
      @param oldValue former value of the column or null.  It is up to
      the caller whether to supply the old value or not.  When a null
      value is supplied, the old value in the ChangeLog table is set to
      the SQL constant null.
      @param newValue new value of the column as a string.
      @param ID - identifies some person who is associated with the
      change.  This may be a string representing a login name or a string
      representing an integer which indexes another table.
  @param perpID a number which identifies whomever made the change.
  @param comment any additional string such as an IP address which
  enhances the value of the data.*/
  public static int LogNumberedChange(String table, String column,
					String record,
					String oldValue,
					String newValue, String ID, int perpID,
					String comment) {
    Connection   conn = null;
    Statement    stmt = null;
    ResultSet results = null;
    String query = null;
    int i = 0;
    /**/

    if (oldValue == null) {
      oldValue = "'null'";
    } else {
      oldValue = Quotable.ConstructSQLCharacterValue(oldValue);
    }

    if (comment == null) {
      comment = "''";
    } else {
      comment = Quotable.ConstructSQLCharacterValue(comment);
    }
    // Outer try to cover creating the statements.
    try {
      conn = DataBase.getPrivateConnection();
      stmt = conn.createStatement();
      query ="insert into ChangeLog VALUES(null, '" + table + "', '" +
	column + "', '" +
	Quotable.QuoteForSQLString(record) + "', " +
	oldValue + ", '" +
	Quotable.QuoteForSQLString(newValue) + "', '" +
	Quotable.QuoteForSQLString(ID) + "', " +
	perpID + ", " + comment + ", NULL)";

      try {
	stmt.execute(query.toString());

	query = DataBase.dbaseGetNextValue;
	results = stmt.executeQuery(query);
	if (results.next()) {	// There were some rows in the table
	  i = results.getInt(1);
	} else {		// The table was empty
	  i = 1;
	}
      } catch (SQLException e) {
	throw new RuntimeException("LogNumberedChange excp " +
				   query + " " + e.toString());
      }

    } catch (SQLException e) {
      System.out.println("LogNumberedChange outer excp " + e.toString());
    } finally {
      try {
	if (results != null) { results.close(); }
	if (stmt != null)    { stmt.close(); }
	if (conn != null)    { conn.close(); }
      } catch (SQLException e) {
      }
    }
    return i;			// change log item number
  }
}
