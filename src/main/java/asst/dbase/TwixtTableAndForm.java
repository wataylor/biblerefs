/* @name TwixtTableAndForm.java

Utilities which translate between database tables and forms

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: TwixtTableAndForm.java,v $
    Revision 1.27  2007/05/15 01:58:51  asst
    handle SQL dates better

    Revision 1.26  2007/01/17 04:04:43  asst
    fixed comma bug on short column names

    Revision 1.25  2006/01/19 16:40:54  asst
    read-only attribute for fields and forms

    Revision 1.24  2005/08/27 14:48:31  asst
    better handling of bad dates in DB

    Revision 1.23  2005/03/22 19:48:44  asst
    date selector returns 0 when empty

    Revision 1.22  2003/11/07 19:02:01  asst
    include seconds in date-only selectors

    Revision 1.21  2003/07/08 19:00:32  asst
    documentation

    Revision 1.20  2003/02/08 14:56:29  asst
    automatic SQL wildcards in where string

    Revision 1.19  2003/02/04 04:28:56  asst
    handle datettime widgets

    Revision 1.18  2003/01/27 04:26:00  asst
    Clean up exceptions in TwixtTableAndForm

    Revision 1.17  2003/01/24 18:44:27  asst
    Gripe about zero values in needed form fields

    Revision 1.16  2002/12/17 03:52:51  asst
    ResultSetToForm

    Revision 1.15  2002/11/24 04:08:32  asst
    documentation, set error flag when griping

    Revision 1.14  2002/10/15 05:34:05  asst
    formToHumanWhereString()

    Revision 1.13  2002/10/06 01:36:46  asst
    first upload

    Revision 1.12  2002/09/22 03:59:35  asst
    Set choice to raw string on malformed dates

    Revision 1.11  2002/09/20 21:43:29  asst
    handle null SQL values in date columns

    Revision 1.10  2002/09/13 03:27:23  asst
    change internal date choice format to JDBC string

    Revision 1.9  2002/09/12 04:51:34  asst
    ignore fields with null tables or null columns

    Revision 1.8  2002/09/05 02:15:25  asst
    added FormToInsertString

    Revision 1.7  2002/09/03 23:34:37  asst
    initial upload

    Revision 1.6  2002/08/18 03:38:08  asst
    documentation

    Revision 1.5  2002/08/13 00:31:26  zonediet
    documentation

    Revision 1.4  2002/07/13 17:56:05  zonediet
    documentation

    Revision 1.3  2002/06/17 17:13:12  zonediet
    documentation

    Revision 1.2  2002/04/30 15:36:04  zonediet
    Selector managers do any year, begin and end times

    Revision 1.1.1.1  2002/04/09 03:20:42  zonediet
    first import
<br>

*/

package asst.dbase;

import java.lang.RuntimeException;
import java.lang.NullPointerException;
import java.util.Enumeration;
import java.sql.*;

import asst.dbase.SelectorManager;
import asst.dbase.SelectorDatePreload;
import asst.dbase.SelectorDatetimePreload;
import asst.dbase.DataBase;
import asst.dbase.Quotable;

/**
 * Methods to convert between an entry form consisting of an array of
 * selector managers and a database table, and vice versa.  The
 * selector managers are intended to be displayed in an entry form;
 * initial values are inserted into the form using
 * <code>TableToForm</code>.  This permits the values to be
 * manipulated.  When editing is complete, <code>FormToTable</code> is
 * called to update the associated database row.

 * @author Web Work
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorManager
 * @see asst.dbase.TwixtObjectAndForm
 */

public class TwixtTableAndForm {

  /** Complex classes need empty strings.  */
  public static final String ES = "";

  /** No-argument constructor */
  public TwixtTableAndForm() {
  }

  /** Copy some number of fields of one form to another.  This is used
      for copying related, contiguous form fields from an input form
      to an output form.  It is also useful for copying columns
      between tables.
  @param destForm the field array which defines the destination form.
  @param destIndex the offset into the destination field array which
  defines the first field to be copied into.
  @param sourceForm the field array which defines the source form.
  @param sourceIndex the offset into the source field array which
  defines the first field to be copied from.
  @param count determines the number of form objects whose values are
  to be copied.
  */
  static public void FormToForm(SelectorManager[] destForm, int destIndex,
				SelectorManager[] sourceForm, int sourceIndex,
				int count) {
    for (; count > 0; count--) {
      /* setChoice clears the dirty flag; it is used when setting
         values from the database, clearing the flag so that the value
         will not be written back to the database.  In this case, the
         choice is being copied from another table so the dirty flag
         must be set.  */
      destForm[destIndex].setChoice(sourceForm[sourceIndex++].getChoice());
      destForm[destIndex++].setDirtyFlag(true);
    }
  }

  /** Change the mode of each field in the form to determine whether
      fields are readable or not, stopping at the null entry which
      defines the limit of the form.
  @param form array of selector objects some of which should be affected
  @param ro true if the form is to be set to read only*/
  public static void ChmodFormFields(SelectorManager[] form, boolean ro) {
    int j;
    /**/

    for (j=0; (j<form.length && (form[j] != null)); j++) {
      form[j].setReadOnly(ro);
    }
  }

  /** Clear all fields and error flags in the form, stopping at any
      null entry which defines the limit of the form.
  @param form array of selector objects some of which should be cleared  */
  public static void ClearFormFields(SelectorManager[] form) {
    int j;
    /**/

    for (j=0; (j<form.length && (form[j] != null)); j++) {
      form[j].setChoice((String)null);
      form[j].setErrorFlag(false);
    }
  }

  /** Clear all fields and error flags in the form, skipping the null
      entry which defines the limit of the form but clearing all fields.
  @param form array of selector objects all of which should be cleared  */
  public static void ClearAllFormFields(SelectorManager[] form) {
    int j;
    /**/

    for (j=0; j<form.length; j++) {
      if (form[j] != null) {
	form[j].setChoice((String)null);
	form[j].setErrorFlag(false);
      }
    }
  }

  /** SelectorManagers are usually grouped into arrays to define a
      form which corresponds to one row of a database table.  Such
      arrays of fields are occasionally grouped into arrays to
      represent many database table rows simultaneously.  In this
      case, each row needs a different prefix for field names so that
      the same field from different rows can be distinguished.  This
      method specifies the prefix which differentiates the rows; it is
      up to the caller to specify a different prefix for all selector
      arrays in a two-dimensional form.
  @param form array of selector objects which need the same prefix
  @param pre the prefix to be applied to each field in the form */
  public static void PrefixAllFormFields(SelectorManager[] form, String pre) {
    int j;
    /**/

    for (j=0; j<form.length; j++) {
      if (form[j] != null) form[j].setFieldNamePrefix(pre);
    }
  }

  /** Create a select string.  Turns all fields in a form regardless
      of dirty flags into a field list to be used as part of a
      comma-separated SQL select string by prepending "select."  The
      resulting column list may reference multiple tables; it is up to
      the caller to ensure that all tables are mentioned in the "from"
      field of the query.</p>
      <P>This routine <B>does not</b> affect the dirty flags.
  @param form array of selector objects from which to extract a
  select statement
  @return either a select string or the empty string.  */
  static public String FormToSelectString(SelectorManager[] form) {
    StringBuffer query;		// The constructed query
    boolean oneDirty = false;
    String sub;
    int j;
    /**/

    query = new StringBuffer(100);

    for (j=0; (j<form.length && (form[j] != null)); j++) {
      if ( (sub = form[j].getColumn()) != null) {
	sub = form[j].getTable() + "." + sub;
	  if (query.length() > 4) {
	    query.append(", ");
	  }
	query.append(sub);
	oneDirty = true;
      }
    }
    if (oneDirty) return query.toString();
    return TwixtTableAndForm.ES;
  }

  /** Create a table list.  Turns all fields in a form regardless of
      dirty flags into a comma-separated table list to be used as part
      of an SQL "select ... from ... " string by prepending "from."
      </p>

  <P>This routine <B>does not</b> affect the dirty flags.
  @param form array of selector objects from which to extract a
  select statement
  @return either a from string or the empty string. */
  static public String FormToFromString(SelectorManager[] form) {
    String query;		// The constructed query
    int j;
    boolean oneDirty = false;
    String table;
    /**/

    query = new String();

    for (j=0; (j<form.length && (form[j] != null)); j++) {
      if ( (table = form[j].getTable()) != null) {
	if (query.indexOf(table) < 0) { // Table name not found in list
	  if (query.length() > 4) {
	    query = query + ", ";
	  }
	  query = query + table;
	  oneDirty = true;
	}
      }
    }
    if (oneDirty) return query;
    return TwixtTableAndForm.ES;
  }

  /** Turns non-empty choice fields in a form into an and-separated
      string which can be used as part of an SQL "where" string by
      prepending "where" <B>and clear the dirty flags</b>.  The
      resulting where string may reference multiple tables; it is up
      to the caller to ensure that all tables are mentioned in the
      "from" field of the query.  It is also up to the caller to
      specify which fields are to be selected and which tables to
      select from to produce a valid SQL query, see
      {@link #FormToFromString(SelectorManager[] form) FormToFromString}.</p>

      <P>The resulting "where" string is sensitive to column type in
      that a character field is surrounded by SQL quotes.  It is also
      sensitive to column content in that if a character field
      contains the SQL wild card character '%', <CODE>LIKE</code>
      comparison operator is used instead of the <CODE>=</code>
      operator.</p>

  <P> This routine <B>clears</b> the dirty flags.
  @param form array of selector objects from which to extract a
  where string.
  @return either a where string or the empty string if no fields are
  non-empty. */
  static public String FormToWhereString(SelectorManager[] form) {
    StringBuffer query;		// The constructed query
    boolean oneDirty = false;
    String choice;
    String sub;
    SelectorManager frm;
    int j;
    /**/

    query = new StringBuffer(100);

    for (j=0; (j<form.length && ( (frm = form[j]) != null)); j++) {
	if (frm.getKnowsChoice() && ( (choice = frm.getChoice()) != null) &&
	    !TwixtTableAndForm.ES.equals(choice)) {
	  if ( (sub = frm.getColumn()) != null) {
	    sub = frm.getTable() + "." + sub;
	    if (query.length() > 4) {
	      query.append(" and ");
	    }
	    query.append(sub);
	    if (frm.getNumericFlag()) {
	      query.append(" = " + Quotable.ConstructSQLNumericValue(choice));
	    } else {
	      if (choice.indexOf("%") >= 0) {
		query.append(" like ");
	      } else {
		if (frm.getFreeWildcard()) {
		  query.append(" like ");
		  choice = "%" + choice + "%"; // wrap in wild cards
		} else {
		  query.append(" = ");
		}
	      }
	      query.append(Quotable.ConstructSQLCharacterValue(choice));
	    }
	  }
	  frm.setDirtyFlag(false);
	  oneDirty = true;
	}
      }
    if (oneDirty) return query.toString();
    return TwixtTableAndForm.ES;
  }

  /** Turns non-empty choice fields in a form into a human-readable
      string which describes the choices that would be made by an
      equivalent SQL where string.</p>

  <P> This routine <B>ignores</b> the dirty flags.
  @param form array of selector objects from which to extract a
  where string.
  @return either a where string or the empty string if no fields are
  non-empty. */
  static public String FormToHumanWhereString(SelectorManager[] form) {
    StringBuffer query;		// The constructed query
    boolean oneDirty = false;
    String choice;
    String sub;
    SelectorManager frm;
    int j;
    /**/

    query = new StringBuffer(100);

    for (j=0; (j<form.length && ( (frm = form[j]) != null)); j++) {

      if (frm.getKnowsChoice() && ( (choice = frm.getChoice()) != null) &&
	  !TwixtTableAndForm.ES.equals(choice)) {
	if (frm.getColumn() != null) {
	  sub = frm.getPrettyName();
	  if (query.length() > 4) {
	    query.append(", ");
	  }
	  query.append(sub + "=" + choice);
	}
	oneDirty = true;
      }
    }
    if (oneDirty) return query.toString();
    return TwixtTableAndForm.ES;
  }

  /**
   * Write the contents of the form into one row of as many database
   * tables as are listed in the selector array; each selector may
   * define a separate database table and column.  The method
   * generates as many SQL update statements as needed to write all of
   * the modified form values back into the database.  The form may
   * end before the array ends if one of the array entries is null,
   * this permits a form to have elements such as a second password
   * field which are displayed but which are not included in any
   * database table.
  @param form the field array which defines the form.
  @param column name of the database table column which serves as an
  index into the destination table.  The row selected from this column
  by the value parameter is updated based on the current values of all
  fields whose dirty flags are set.
  @param value the value which selects the update target row based on
  the column parameter.
  @return false
  @exception RunTimeException on SQL error
  */
  static public boolean FormToTable(SelectorManager[] form,
				    String column, int value)
    throws RuntimeException {
    int i;			// Walk the selector manager array
    int j;			// Inner loop walking the selector managers
    boolean oneDirty;		// Must update the table
    String table;
    String choice;		// Current choice in the form row
    ResultSet results;		// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    StringBuffer query = null;	// The constructed query
    String ask = null;	   // The final query submitted to a statement
    /**/

    try {
      for (i=0; (i<form.length && (form[i] != null)); i++) {
	table = null;
	oneDirty = false;
	query = new StringBuffer(100);

	if (form[i].getDirtyFlag() &&
	    ((table == null) || (table.equals(form[i].getTable())))) {
	  if (table == null) {
	    oneDirty = false;
	    table = form[i].getTable();
	  }
	  for (j=i;  (j<form.length && (form[j] != null)); j++) {
	    if (form[j].getDirtyFlag() && table.equals(form[j].getTable())) {
	      if (query.length() > 0) {
		query.append(", ");
	      }
	      query.append(form[j].getColumn() + "=");
	      choice = form[j].getChoice();
	      if (form[j].getNumericFlag()) {
		query.append(Quotable.ConstructSQLNumericValue(choice));
	      } else {
		query.append(Quotable.ConstructSQLCharacterValue(choice));
	      }
	      form[j].setDirtyFlag(false);
	      oneDirty = true;
	    }
	  }
	  ask = "update " + table + " set " + query.toString() +
	    " where " + column + " = " + value;
	  if (oneDirty) {
	    oneDirty = false;
	    getRow = DataBase.connDB.createStatement();
	    getRow.execute(ask);
	    getRow.close();
	    //System.out.println(ask);
	  }
	}
      }
    } catch (SQLException e) {
      throw new RuntimeException("FormToTable " + ask + " " + e.toString());
    }
    return false;
  }

  /** Create a string buffer and turn a form into an SQL update string
      in the buffer.  This ignores the table names in the form, it
      assumes that the caller knows which table to manipulate.  This
      routine does <B>not</b> change any of the dirty flags.
  @param form array of field objects.
  @param all if true, puts all of the fields in the
  update string, otherwise, only the dirty fields.
  @return String Buffer containing the SQL update command.*/

  static public StringBuffer FormToUpdateString(SelectorManager[] form,
						boolean all) {
    return TwixtTableAndForm.FormToUpdateString(form, new StringBuffer(), all);
  }

  /** Turn a form into an SQL update string.  This ignores all of the
      table names in the form, it assumes that the caller knows which
      table to manipulate.  This routine does <B>not</b> change any of
      the dirty flags.
  @param form array of field objects.
  @param query StringBuffer to hold the SQL commands.  The caller may
  start the StringBuffer with "update &lt;table name&gt; set " and
  append a <CODE>where</code> clause to the buffer returned by this
  method.
  @param all if true, puts all of the fields in the
  update string, otherwise only the dirty fields are updated.
  @return String Buffer containing the SQL update command.*/
  static public StringBuffer FormToUpdateString(SelectorManager[] form,
					StringBuffer query, boolean all) {
    int j;
    String choice;
    boolean did;		// Remember to put a comma between them
    /**/

    did = false;
    for (j=0; (j<form.length && (form[j] != null) &&
	       (form[j].getColumn() != null)); j++) {
      if (all || form[j].getDirtyFlag()) {
	if (did) {
	  query.append(", ");
	} else {
	  did = true;
	}
	query.append(form[j].getColumn() + "=");
	choice = form[j].getChoice();
	if (form[j].getNumericFlag()) {
	  query.append(Quotable.ConstructSQLNumericValue(choice));
	} else {
	  query.append(Quotable.ConstructSQLCharacterValue(choice));
	}
      }
    }
    return query;
  }

  /** Turn a form into an SQL insert string.  It ignores the table
      names in the form, it assumes that the caller knows which table
      to manipulate.  All fields are put into the string regardless of
      dirty flags on the assumption that an <CODE>insert</code>
      statement is used to create a new table row, in which case, all
      fields should be defined.  This routine does <B>not</b> change
      any of the dirty flags.
  @param form array of field objects.
  @return String Buffer containing the SQL update command.*/

  static public StringBuffer FormToInsertString(SelectorManager[] form) {
    int j;
    String choice;
    boolean did;		// Remember to put a comma between them
    StringBuffer sb = new StringBuffer("(");
    /**/

    did = false;
    for (j=0; (j<form.length && (form[j] != null) &&
	       (form[j].getColumn() != null)); j++) {
      if (did) {
	sb.append(",");
      } else {
	did = true;
      }
      sb.append(form[j].getColumn());
    }

    sb.append(")VALUES(");
    did = false;
    for (j=0; (j<form.length && (form[j] != null)); j++) {
      if (did) {
	sb.append(",");
      } else {
	did = true;
      }
      choice = form[j].getChoice();
      if (form[j].getNumericFlag()) {
	sb.append(Quotable.ConstructSQLNumericValue(choice));
      } else {
	sb.append(Quotable.ConstructSQLCharacterValue(choice));
      }
    }
    sb.append(")");
    return sb;
  }

  /** This entry point is the same as FormToTable except that all form
      values are written to the table.  This entry is used when
      inserting into new table rows.
  @param form the field array which defines the form.
  @param column is the name of the database table column which serves
  as an index into the destination table.  The row selected from this
  column by the value parameter is updated based on the values of all
  fields whose dirty flags are set.
  @param value the value which selects the update target row based on
  the column parameter.
  @return false  */
  static public boolean AllFormToTable(SelectorManager[] form,
				    String column, int value) {
    for (int i=0; (i<form.length && (form[i] != null)); i++) {
      form[i].setDirtyFlag(true);
    }
    return TwixtTableAndForm.FormToTable(form, column, value);
  }

  /** Use the current result set row to fill as many values of the
      form as possible.  This method is used when the caller must use
      a nonstandard query to read the data or when multiple form
      objects must be created from one query.  It is up to the caller
      to close the result set.
  @param form the field array which defines the form.
  @param results a result set which is known to contain valid data
  which might match the field column names.
  @param table name of the table from which the row came*/
  public static void ResultSetToForm(SelectorManager[] form,
				     ResultSet results, String table) {
    int j;
    SelectorManager field;
    String colValue;
    String colName;
    /**/

    try {
      for (j=0; (j<form.length && ((field = form[j]) != null)); j++) {
	if ((field.getTable() != null) &&
	    table.equals(field.getTable())) {
	  if ( (colName = field.getColumn()) != null) {
	    if (field instanceof SelectorDatePreload) {
	      try {
		if (field instanceof SelectorDatetimePreload) {
		  colValue = SelectorFieldPreload.
		    SQL_DATE_STRING.format(SelectorFieldPreload.
					   SQL_DATE_STRING.
					   parse(results.getString(colName)));
		} else {
		  // Get date sets h, m, and s to 0.
		  colValue = SelectorFieldPreload.
		    SQL_DATE_STRING.format(results.getDate(colName));
		}
		/* If the field is SQL NULL, it causes a null
		 * pointer exception.  If it contains a
		 * badly-formatted date, there is an SQL
		 * exception.  */
	      } catch (NullPointerException e) {
		colValue = null;
	      } catch (SQLException e) {
		colValue = null;
	      } catch (Exception e) {
		colValue = null;
	      }
	    } else {
	      colValue = results.getString(colName);
	    }
	    if ((colValue != null) &&
		!TwixtTableAndForm.ES.equals(colValue)) {
	      field.setChoice(colValue);
	    }
	    //System.out.println("Read " + table + " " +
	    //	     field.getColumn() + " " + colValue);
	  }
	}
      }
    } catch (SQLException e) {
      throw new RuntimeException("ResultSetToForm excp " + table +
				 " " + e.toString());
    }
  }

  /** Select one row corresponding to the specified column and integer
      value from as many tables as needed to load data from the row or
      rows into the Selector Manager array.  The database tables and
      column names are specified in each SelectorManager, if more than
      one table name is mentioned in the array, all of the tables must
      have the same key column.  The form may end before the array
      ends if one of the array entries is null, this permits a form to
      have elements which are displayed but which are not included in
      any database table.</p>

  <P>Ignores database column values which are either null or equal to
  the empty string.  This means that the form row does not know the
  choice.</p>

  <P>If a select fails, that is, the table does not have a row
  matching the ID, the affected columns are set empty and flagged as
  if the form knows the choice.
  @param form the field array which defines the form.
  @param column is the name of the database table column which serves
  as an index into the source table.  The row selected from this
  column by the value parameter is read into the field values.
  @param value the value which selects the input row based on
  the column parameter.
  @return false if the row was not found, true otherwise
  @exception RuntimeException on SQL error*/
  static public boolean TableToForm(SelectorManager[] form,
				    String column, int value)
    throws RuntimeException {
    int i;			// Walk the selector manager array
    int j;			// Inner loop walking the selector managers
    String table;
    ResultSet results = null;	// Results of the retrieval
    Statement stmt = null;	// Retrieve the row
    String query = null;	// The constructed query
    String colValue;		// Value from the database
    boolean found;		// Show that something was found
    String colName;
    SelectorManager field;	// One field from the row
    /**/

    for (i=0; (i<form.length && (form[i] != null)); i++) {
      form[i].setChoice((String)null);	// Flag all values as not being known
      form[i].setErrorFlag(false);
    }

    found = false;

    try {
      stmt  = DataBase.connDB.createStatement();
      /* Issue as many table selects as needed to set as many choices
	 non-null as possible */
      for (i=0; (i<form.length && ((field = form[i]) != null)); i++) {
	if (!field.getKnowsChoice() && // Not already processed
	    ( (table = field.getTable()) != null)) {
	  query="select * from " + table + " where " + column + " = " + value;
	  //System.out.println(query);
	  results = stmt.executeQuery(query);
	  if (results.next()) {
	    found = true;	// Query succeeded
	    for (j=i; (j<form.length && ((field = form[j]) != null)); j++) {
	      if ((field.getTable() != null) &&
		  table.equals(field.getTable())) {
		if ( (colName = field.getColumn()) != null) {
		  if (field instanceof SelectorDatePreload) {
		    try {
		      if (field instanceof SelectorDatetimePreload) {
			colValue = SelectorFieldPreload.
			  SQL_DATE_STRING.format(SelectorFieldPreload.
						 SQL_DATE_STRING.parse(results.getString(colName)));
		      } else {
			// Get date sets h, m, and s to 0.
			colValue = SelectorFieldPreload.
			  SQL_DATE_STRING.format(results.getDate(colName));
		      }
		      /* If the field is SQL NULL, it causes a null
		       * pointer exception.  If it contains a
		       * badly-formatted date, there is an SQL
		       * exception.  */
		    } catch (NullPointerException e) {
		      colValue = null;
		    } catch (SQLException e) {
		      colValue = null;
		    } catch (Exception e) {
		      colValue = null;
		    }
		  } else {
		    colValue = results.getString(colName);
		  }
		  if ((colValue != null) &&
		      !TwixtTableAndForm.ES.equals(colValue)) {
		    field.setChoice(colValue);
		  }
		  //System.out.println("Read " + table + " " +
		  //	     field.getColumn() + " " + colValue);
		}
	      }
	    }
	  } else {
	    for (j=i; (j<form.length && (form[j] != null)); j++) {
	      if (table.equals(form[j].getTable())) {
		form[j].setChoice(TwixtTableAndForm.ES); // Will not try again
	      }
	    }
	  }
	  results.close();
	}
      }
    } catch (SQLException e) {
      throw new RuntimeException("TableToForm " + query + " " + e.toString());
    } finally {
      try {
	if (results != null) { results.close(); }
	if (stmt    != null) { stmt.close(); }
      } catch (SQLException e) {}
    }
    return found;		// May have succeeded
  }

  /** Read whatever form content can be found in in the specified
      table.  The assumes that at least some of the column names in
      the form objects will match the column names in the table.  This
      entry is used when the column names in the form happen to match
      column names in a table other than the table for which the form
      was intended.
  @param form the field array which defines the form.
  @param table is the name of the database table from which data are
  to be extracted.
  @param column is the name of the database table column which serves
  as an index into the source table.  The row selected from this
  column by the value parameter is read into the field values.
  @param value the value which selects the input row based on
  the column parameter.
  @return false if the row was not found, true otherwise*/

  static public boolean AnyTableToForm(SelectorManager[] form, String table,
				    String column, int value)
    throws SQLException {
    int i;			// Walk the selector manager array
    int j;			// Inner loop walking the selector managers
    ResultSet results;		// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    String query;		// The constructed query
    String colValue;		// Value from the database
    boolean found;		// Show that something was found
    /**/

    for (i=0; (i<form.length && (form[i] != null)); i++) {
      form[i].setChoice((String)null);	// Flag all values as not being known
    }

    getRow  = DataBase.connDB.createStatement();
    results = null;
    found   = false;

    try {
      results = getRow.executeQuery("select * from " + table +
				    " where " + column + " = " + value);
      if (results.next()) {
	found = true;	// Query succeeded
	for (j=0; (j<form.length && (form[j] != null)); j++) {
	  try {
	    colValue = results.getString(form[j].getColumn());
	  } catch (Exception e) { colValue = null; }
	  if ((colValue != null) && !TwixtTableAndForm.ES.equals(colValue)) {
	    form[j].setChoice(colValue);
	  }
	}
      }
    } finally {
      if (results != null) { results.close(); }
      getRow.close();
    }
    return found;		// May have succeeded
  }

  /** Look up a specific column name from a form array and return the
      form row object.  Searches the entire array including selector
      managers which fall after a null pointer.  It is important that
      column names be different for post-null fields such as second
      password field which are not intended to be written to the
      database.
  @param form the field array which defines the form.
  @param columnName is the name of the database table column whose field
  is desired.  The form is searched for a field object whose specified
  database column <CODE>equals()</code> this column name regardless of
  the table specified for the field object.
  @return the desired selector manager or null. */
  public static SelectorManager findFormRow(SelectorManager[] form,
					    String columnName) {
    SelectorManager sm;
    int i;
    /**/

    for (i=0; i<form.length; i++) {
      if (( (sm = form[i]) != null) && (sm.getColumn() != null) &&
	  columnName.equals(sm.getColumn())) { return sm; }
    }
    System.out.println("Not find column " + columnName + " in form");
    return null;		// Did not find the column
  }

  /** Look up a specific form row by database column name and return
      its current value.
  @param form the field array which defines the form.
  @param columnName the name of the database table column whose field
  value is desired.  The form is searched for a field object whose specified
  database column matches this column name regardless of the table
  specified for the field object.
  @return the current value of the specified column field or null. */
  public static String findFormRowValue(SelectorManager[] form,
					String columnName) {
    SelectorManager sm;
    int i;
    /**/

    sm = findFormRow(form, columnName);
    if (sm != null) { return sm.getChoice(); }
    return null;
  }

  /** Gripe about a needed Selectormanager not being filled in and set
      the error flag.  The column value whose presence is required is
      extracted from the SelectorManager by searching the array.
  @param form the Selectormanager array which defines the form.
  @param columnName is the name of the database table column whose field
  value must be specified.  The form is searched for a field object
  whose specified database column matches this column name regardless
  of the table specified for the field object.
  @return an error message if the column cannot be found in the form
  or if it has no value or its value is the empty string or if it is
  numeric and its value is zero. */
  public static String gripeNeededField(SelectorManager[] form,
					String columnName) {
    String aValue;
    SelectorManager sm;
    float value;
    /**/

    sm = TwixtTableAndForm.findFormRow(form, columnName);
    if (sm == null) {
      return "<LI>Missing form column " + columnName + "</li>";
    }

    if (sm.getKnowsChoice()) {
      aValue = sm.getChoice();
    } else {
      aValue = null;
    }
    if ((aValue == null) || (TwixtTableAndForm.ES.equals(aValue))) {
      sm.setErrorFlag(true);
      return "<LI>You must supply a value for " + sm.getPrettyName() +
	".</li>";
    }
    // The value choice string is non-null and non-empty, but if the
    // field is numeric, it must also be nonzero.
    if (sm.getNumericFlag()) {
      try {
	value = Float.parseFloat(aValue);
      } catch (Exception e) { value = 0; }
      if (value == 0) {
	sm.setErrorFlag(true);
	return "<LI>You must supply a value for " + sm.getPrettyName() +
	  ".</li>";
      }
    }
    return TwixtTableAndForm.ES;
  }

  /** Check a form for non-unique values of all fields whose columns
      are flagged as requiring unique values within their database
      table.  Sets the error flag for fields which are non-unique.
      Creates a string buffer which holds the error messages, the
      buffer is empty if there are no errors.</p>

      <P>The field value is checked if the field is flagged as unique,
      the field is dirty, and the value is other than the empty
      string.  Thus, an empty field passes this test.  Use {@link
      #gripeNeededField gripeNeededField} to ensure that mandatory
      fields are filled in.
  @param form the field array which defines the form.
  @param columnName is the name of the database table column which serves
  as an index into the destination table.  The row selected from this
  column by the value parameter must have unique values for all fields
  whose UniqueFlags are set; only dirty fields are checked.
  @param value the value which selects the reference row based on the
  column parameter.
  @return a string buffer of messages, it may be empty.  */

  public static StringBuffer gripeNonUniqueValues(SelectorManager[] form,
						  String columnName, int value)
    throws RuntimeException, SQLException {
    StringBuffer sb = new StringBuffer();
    return gripeNonUniqueValues(form, columnName, value, sb, null);
  }

  /** Check a form for non-unique values of all fields whose columns
      are flagged as requiring unique values within their database
      table.  The field value is checked if the field is flagged as
      unique, the field is dirty, and the value is other than the
      empty string.  Thus, an empty field passes this test.  Use
      {@link #gripeNeededField gripeNeededField} to ensure that
      mandatory fields are filled in.</p>

      <P>Sets the error flag for fields which are non-unique.  Error
      messages are appended to the string buffer which is passed in.
      This may cause the buffer to be reallocated, so the resulting
      buffer should be preserved instead of relying on the data having
      been appended to the original buffer.
  @param form the field array which defines the form.
  @param columnName is the name of the database table column which serves
  as an index into the destination table.  The row selected from this
  column by the value parameter must have unique values for all fields
  whose UniqueFlags are set; only dirty fields are checked.
  @param value the value which selects the reference row based on the
  column parameter.
  @param sb StringBuffer to which to append error messages, this may
  cause the buffer to be reallocated.
  @param where specifies where clauses to restrict the search for
  ooverlapping values, it is added to the row search..
  @return a string buffer of messages, it may be empty.
  @exception RuntimeException if there is an SQL syntax error*/

  public static StringBuffer
  gripeNonUniqueValues(SelectorManager[] form, String columnName, int value,
		       StringBuffer sb, String where)
    throws RuntimeException, SQLException {
    ResultSet results = null;	// Results of the retrieval
    Statement getRow  = null;	// Retrieve the sequence number
    SelectorManager sm;
    String query = null;	// Initialize it for the compiler
    String choice;
    int i;
    /**/

    if (where != null) {
      where += " and ";
    } else {
      where = TwixtTableAndForm.ES;
    }

    try {
      for (i=0; (i<form.length && ( (sm = form[i]) != null)); i++) {
	if (sm.getUniqueFlag() && sm.getDirtyFlag()) {
	  if (sm.getKnowsChoice()) {
	    choice = sm.getChoice();
	  } else {
	    choice = null;
	  }
	  if ((choice == null) || TwixtTableAndForm.ES.equals(choice)) {
	    continue;
	  } else {
	    query = "select " + columnName + " from " +
	      sm.getTable() + " where " + where + sm.getColumn() + " = ";
	    if (sm.getNumericFlag()) {
	      query += Quotable.ConstructSQLNumericValue(choice);
	    } else {
	      query += Quotable.ConstructSQLCharacterValue(choice);
	    }
	    //System.out.println(query);
	    if (getRow == null) {
	      getRow = DataBase.connDB.createStatement();
	    }
	    results = getRow.executeQuery(query);
	    if (results.next()) {
	      if (value != results.getInt(1)) {
		sb.append("<LI>" + sm.getPrettyName() +
			  " must be unique in the " + sm.getTable() +
			  " table but the value " + choice +
			  " is already in use by entry # " +
			  results.getString(1) + ".</li>");
		sm.setErrorFlag(true);
	      }
	    }
	    results.close();
	  }
	}
      }
    } catch (SQLException e) {
      throw new RuntimeException("gripeNonUniqueValues " + query + " " +
				 e.toString());
    } finally {
      if (results != null) { results.close(); }
      if (getRow  != null) { getRow.close(); }
    }
    return sb;
  }
}
