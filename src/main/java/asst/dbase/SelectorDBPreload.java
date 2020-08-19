/* @name SelectorDBPreload.java

Manage selection objects which preload values from a data base column.

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SelectorDBPreload.java,v $
    Revision 1.13  2007/11/28 05:06:54  asst
    Make label for no selection into a variable

    Revision 1.12  2007/06/15 18:16:54  asst
    add dashes to Make A Selection

    Revision 1.11  2006/01/19 16:40:54  asst
    read-only attribute for fields and forms

    Revision 1.10  2003/07/08 19:04:24  asst
    documentation

    Revision 1.9  2003/02/08 14:53:49  asst
    OK to have an empty selection vector

    Revision 1.8  2002/10/06 01:36:46  asst
    first upload

    Revision 1.7  2002/09/12 04:52:57  asst
    added RadioSet, CheckboxSet, RadioSetMember

    Revision 1.6  2002/09/03 23:34:16  asst
    support multi selections and radio selections

    Revision 1.5  2002/08/28 03:37:13  asst
    documentation

    Revision 1.4  2002/08/23 03:23:31  asst
    Added where and order clause

    Revision 1.3  2002/08/18 03:38:08  asst
    documentation

    Revision 1.2  2002/04/30 15:36:04  zonediet
    Selector managers do any year, begin and end times

    Revision 1.1.1.1  2002/04/09 03:20:29  zonediet
    first import

*/

package asst.dbase;

import java.util.Enumeration;
import java.util.Vector;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import asst.dbase.SelectorManager;

/**
 * Implementation of the SelectorManager interface which displays a
 * list of potential selections in a .html drop-down selection box.
 * This class has several ways to accept its list of input selections:

 * <ol> <li>Select distinct values from a column of a database table.
 Database query is done automatically the first time the .html is
 generated unless the selection vector has been previously initialized by some
 other means.</li>

 <li>Select distinct values from a column of a database table where
 the value of the filter column for the row matches the filter value.
 Database query is done automatically the first time the .html is
 generated unless the selection vector has been initialized by some
 other means.</li>

 <li>Pass in a vector of selection strings; the fact that the vector
 is not null keeps the list from being built from the database.</li>

 <li>Pass in an array of strings which are converted to a vector.  The
 fact that the selection vector is not null keeps the list from being
 read from the database.</li> </ol>

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorManager
 * @see asst.dbase.SelectorMultiPreload
 * @see asst.dbase.SelectorRadioPreload
 */

public class SelectorDBPreload extends SelectorFieldPreload {

  /** Define a global empty string.*/
  public static final String ES = "";

  /** No-arg constructor which sets up the default value. */
  public SelectorDBPreload() {
  }

  /** Constructor that sets the selector name, database table, and
      database column.  Because there is no filterColumn, all distinct
      values found in table.column or in table.ID and table.column are
      included in the selection list.
  @param name from which the form field name is to be derived.when
  .html is generated
  @param table tells which SQL table is associated wtih the row of
  which this field stores and manipulates one column value.
  @param column names the database table column whose value is
  manipulated by this object.
  */
  public SelectorDBPreload(String name, String table, String column) {
    this.setName(name);
    this.setTable(table);
    this.setColumn(column);
    this.filterColumn = null;
    this.filterColumnValue = null;
  }

  /** Constructor that sets the selector name, database table, and
      database column and preselects the selections from an array.
      @param name from which the form field name is to be derived.when
      .html is generated
      @param table tells which SQL table is associated wtih the row of
      which this field stores and manipulates one column value.
      @param column names the database table column whose value is
      manipulated by this object.
      @param selections is an array of strings which lists the
      selections to be displayed when .html is generated.
  . */
  public SelectorDBPreload(String name, String table, String column,
			   String[] selections) {
    this.setName(name);
    this.setTable(table);
    this.setColumn(column);
    this.filterColumn = null;
    this.filterColumnValue = null;
    this.setSelections(selections);
  }

  /** Constructor that sets the selector name, database table, database
      column and a filter column and value.  All distinct values found
      in table, column rows for which filterColumn, filterValue matches
      are included in the selection list.
      @param name from which the form field name is to be derived.when
      .html is generated
      @param table tells which SQL table is associated wtih the row of
      which this field stores and manipulates one column value.
      @param column names the database table column whose value is
      manipulated by this object.
      @param filterColumn names the database table column whose values
      must match the filter value for the row to participate in teh
      selection list.
      @param filterColumnValue gives the value which must be found in
      the <code>filterColumn</code> for its value to be included in
      the selection list.
  */
  public SelectorDBPreload(String name, String table, String column,
			   String filterColumn, String filterColumnValue) {
    this.setName(name);
    this.setTable(table);
    this.setColumn(column);
    this.filterColumn = filterColumn;
    this.filterColumnValue = filterColumnValue;
  }

  /** Express the selection list as a named .html SELECTOR object
      whose initial values are drawn from the current selection
      vector.  The generated .html includes the selector name with
      underscores converted to spaces, a colon, and the selection
      list.  The method <B>getHTMLOnly()</b> produces an unnamed
      selection list in case the label and selector are in separate
      table cells.  The selector is always given the object name so
      that the current value can be examined by Java Script.</p>

      <P>It is assumed that
      the database table contents do not change during the life of the
      selector object so the column is searched only once and its
      value are stored in a Vector.  The caller may force the vector
      to be reloaded using the method getHTMLAgain(). The database is
      not referenced if the selection list has already been
      specified.</p>

 <P>If the column is flagged as numeric, the object assumes that the
 table has a numeric column named ID in addition to the column
 specified by setColumn().  In this case, the selections are created
 by <B>select distinct ID, getColumnName()</b>.  The ID is used as the
 selection value and the column as the selection text.</p>

 <P>If the column is non-numeric, the vector is constructed by
 selecting distinct values from the column; these are used both as the
 value and as the selection text in the generated .html. */
  public StringBuffer getHTML() {
    StringBuffer sb = new StringBuffer(this.getPrettyName() + " " );

    if (readOnly) {
      sb.append(getChoiceAsText());
    } else {

      if ((this.entries == null) || (this.entries.size() <= 0)) {
	try {
	  this.entries = this.getSelectionVector();
	} catch (SQLException e) {
	}
      }
      sb.append(this.makeSelector(this.entries, this.getChoice()));
    }
    return sb;
  }

/** Generate a selection list based on an input vector.  The
    parameters permit a choice of caption as well as specifying which
    of the possibilities to select when the selection list is dislayed.
    @param items is a vector of selection possibilities
    @param selectedItem is the current selection which should be
    highlighted when the .html is generated.
    @return .html to generate the selection box.
*/
  public StringBuffer makeSelector(Vector items,
				   String selectedItem) {
    StringBuffer sb = new StringBuffer();
    String name;		// Name of the selectable item
    String value;		// Value with the name
    int i;			// Count the entries to format the .html
    int j;			// Count the entries in the vector
    int ix;			// Index of the pipe
    /**/

    i = 0;
    sb.append("<SELECT NAME=\"" + this.fieldNamePrefix + this.getName() +
	      "\" " + this.addParam + ">\n");

    if (selectedItem == null) { selectedItem = ES; }

    if (ES.equals(selectedItem)) {
      sb.append("<OPTION selected VALUE=\"\">" + makeASelection +
		"</option>\n");
    } else {
      sb.append("<OPTION VALUE=\"\">--" + makeASelection + "--</option>\n");
    }

    if (items != null) {
      for (j=0; j<items.size(); j++) {
	name = (String)items.elementAt(j);
	if ( (ix = name.indexOf("|")) > 0) {
	  value  = name.substring(ix+1);
	  name   = name.substring(0, ix);
	} else {
	  value  = name;	// No separator
	}
	sb.append("<OPTION ");
	if (selectedItem.equals(name)) { sb.append("selected "); }
	sb.append("VALUE=\"" + name + "\">" + value + "</option>\n");
	if (++i >= 4) {
	  i = 0;
	  sb.append("\n");
	}
      }
      if (i > 0) { sb.append("\n"); }
    }
    sb.append("</select>\n");
    return sb;
  }

  /** The general format for a selection box is that the value which
      would be returned by getChoice() matches a value in the
      selection vector.  This routine matches the current choice with
      the vector and returns the match.
  @return current choice as pure text so that it cannot be edited in
  the form.*/
  public String getChoiceAsText() {
    Enumeration enumV;		// Walk the selection vector
    String selectedItem;	// Current choice
    String name;		// Selection name
    String value;		// Value stored in the database
    Vector items;		// Vector of possibilities
    int ix;
    /**/

    selectedItem = this.getChoice();
    if (ES.equals(selectedItem)) { return ES; } // Empty choice is empty

    // Create the selection vector as needed
    if ((this.entries == null) || (this.entries.size() <= 0)) {
      try {
	this.entries = this.getSelectionVector();
      } catch (SQLException e) {
      }
    }

    // examine the vector for a match with the current choice
    if ( (items = this.entries) == null) { return ES; }
    for (enumV = items.elements(); enumV.hasMoreElements(); ) {
      name = (String)enumV.nextElement();
      if ( (ix = name.indexOf("|")) > 0) {
	value  = name.substring(ix+1);
	name   = name.substring(0, ix);
      } else {
	value  = name;	// No separator
      }
      if (selectedItem.equals(name)) { return value; }
    }
    return ES;
  }

  /** Turn the distinct values from the specified SQL table and column
      into a vector.  If the filter and value are both non-null, a row
      must have a value in the filter column which matches value in
      order to be included in the vector.</p><P>This is useful only
      when selecting entries from a fixed table column, it is not
      useful for editing the table and column specified in the object
      because the table name and column name are used to fill the
      selection list unless the intent is to select every distinct
      entry in the column.
  @return vector of distinct column values for a selector.*/
  public Vector<String> getSelectionVector()
    throws java.sql.SQLException {
    StringBuffer query;		// The query to select the values
    /**/

    if ((this.table  == null) || ES.equals(this.table) ||
	(this.column == null) || ES.equals(this.column)) return null;

    if (this.numeric || this.wantID) {
      query   = new StringBuffer("select distinct ID, " + column +
				 " from " + table);
    } else {
      query   = new StringBuffer("select distinct " + column +
				 " from " + table);
    }
    if (ES.equals(this.whereOrder)) {
      if ((this.filterColumn != null) && (this.filterColumnValue != null)) {
	String quotationMark;	// String in which to wrap comparand
	quotationMark = ES;
	for (int i=0; i<this.filterColumnValue.length(); i++) {
	  if (!Character.isDigit(this.filterColumnValue.charAt(i))) {
	    quotationMark = "'";
	    break;
	  }
	}
	query.append(" where " + this.filterColumn + " = " +
		     quotationMark + this.filterColumnValue + quotationMark);
      }
      query.append(" order by " + this.column);
    } else {
      query.append(" " + whereOrder);
    }
    return SQLUtilities.VectorizeQuery(query.toString());
  }

  /** Generate a selection list with no label.  This is used when the
      .html page labels the selector.  Creates a selection vector by
      reading the database table and column if the selection vector is
      null.
  @return .html for an unlabeled selection list.*/
  public StringBuffer getHTMLOnly() {

    if (readOnly) return new StringBuffer(getChoiceAsText());

    if (this.entries == null) {
      try {
	this.entries = this.getSelectionVector();
      } catch (SQLException e) {
      }
    }
    return this.makeSelector(this.entries, this.getChoice());
  }

  /** Express the field in a .html text area without labeling it, but
      disregard the width.
  @param width number of columns in the selection area.
  @return .html needed to generate the selection list*/
  public StringBuffer getHTMLOnly(int width) {
    if (readOnly) return new StringBuffer(getChoiceAsText());
    return this.getHTMLOnly();
  }

  /** Express the field in a .html text area without labeling it, but
      disregard the width and height.
  @param width number of columns in the selection area.
  @param height number of rows in the box.
  @return .html needed to generate the selection list*/
  public StringBuffer getHTMLOnly(int width, int height) {
    if (readOnly) return new StringBuffer(getChoiceAsText());
    return this.getHTMLOnly();
  }

  /** Method which clears the entry vector and reloads the list of
      selections.  This should be used whenever the database has
      changed such that there should be a new entry in the selection
      list.
  @return .html needed to generate the selection list. */
  public StringBuffer getHTMLAgain() throws java.sql.SQLException {
    this.entries = null;
    return this.getHTML();
  }

  /** Set the list of selections from a string array. */
  public void setSelections(String [] selectionArray) {
    this.entries = new Vector<String>();
    for (int i=0; i<selectionArray.length; i++) {
      this.entries.addElement(selectionArray[i]);
    }
  }

  /** Set the list of selections from a vector of strings. */
  public void setSelections(Vector<String> vector) {
    this.entries = vector;
  }

  /** Determine whether the selector returns the selection string or
      the ID.  The string is returned by default unless the box is
      numeric in which case the selection number is returned.*/
  public void setWantID(boolean want) {
    this.wantID = want;
  }

  /** Indicate whether the selector returns the selection string or
      the ID.  The string is returned by default unless the box is
      numeric in which case the selection number is returned. */
  public boolean getWantID() {
    return this.wantID;
  }

  /** The selector list is often built from the database, the database
   * query may require modifiers such as a <code>where</code> or
   * <code>order by</code> clause; set the string which is appended to
   * the end of the query.  If this string is non-empty, the filter
   * column is ignored.
   @param whereOrder the string to be appended to the selection query;
   this string need not begin with a space but it must be a valid SQL
   clause or set of clauses.
   */
  public void setWhereOrder(String whereOrder) {
    this.whereOrder = whereOrder;
  }

  /** The selector list is often built from the database, the database
   * query may require modifiers such as a <code>where</code> or
   * <code>order by</code> clause; get the string which is appended to
   * the end of the query.  If this string is non-empty, the filter
   * column is ignored.
   */
  public String getWhereOrder() {
    return this.whereOrder;
  }

  /** The selector list has a null entry; this sets the name of that
   * item.*/
  public void setMakeASelection(String mas) {
    makeASelection = mas;
  }

  /** If the filterColumn is non-null, it indicates a column whose
      value must be equal to the filterColumnValue for a value found
      in table, column to be included in the selection list.  */
  protected String filterColumn;

  /** If the filterColumnValue is non-null, it indicates a value
      which must be found in filterColumn for a value found in
      table, column to be included in the selection list.  */
  protected String filterColumnValue;

  /** The list of all distinct values from the table and column.  The
      table is presumed to be static and so the data are not retrieved
      again once they have been requested once.  */
  protected Vector<String> entries;

  /** The selector may return either the column values or the value ID
      associated with the column text.  This flag indicates that the
      ID should be returned instead of the srign.*/
  protected boolean wantID = false;

  /** The selector list is often built from the database, the database
   * query may require modifiers such as a <code>where</code> or
   * <code>order by</code> clause; this string is appended to the end
   * of the query. If this string is non-empty, the filter column is
   * ignored. */
  protected String whereOrder = ES;

  /** The default empty item in a selection list is "Make a Selection"
   * but it may be overridden*/
  protected String makeASelection = "Make A Selection";
}
