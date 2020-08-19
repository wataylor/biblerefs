/* @name SelectorRadioPreload.java

Selector which expresses selections as radio buttons

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SelectorRadioPreload.java,v $
    Revision 1.4  2006/01/19 16:40:54  asst
    read-only attribute for fields and forms

    Revision 1.3  2002/09/12 04:52:57  asst
    added RadioSet, CheckboxSet, RadioSetMember

    Revision 1.2  2002/09/04 21:33:35  asst
    Added text-only methods, error string and flag

    Revision 1.1  2002/09/03 23:34:16  asst
    support multi selections and radio selections
<br>
*/

package asst.dbase;

import java.util.Enumeration;
import java.util.Vector;

import java.sql.SQLException;

import asst.dbase.SQLUtilities;

/**
 * Extension of the selector object which displays selections as radio
 * buttons instead of in a drop-down list.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorManager
 * @see asst.dbase.SelectorMultiPreload
 */

public class SelectorRadioPreload extends SelectorDBPreload {

  /** Specify that the radio buttons are to be formatted in a single
   * line, relying on the browser to format the line. */
  public static final int AS_LINE = 0;

  /** Specify that the radio buttons are to be formatted as one line
   * per radio button. */
  public static final int ONE_BUTTON_PER_LINE = 1;

  /** Specify that the radio buttons are to be formatted as a 2-column
   * table with one row per radio button. */
  public static final int AS_TABLE = 2;

  /** Obligatory constructor.*/
  public SelectorRadioPreload() {
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
  public SelectorRadioPreload(String name, String table, String column) {
    super(name, table, column);
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
  public SelectorRadioPreload(String name, String table, String column,
			   String[] selections) {
    super(name, table, column, selections);
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
  public SelectorRadioPreload(String name, String table, String column,
			   String filterColumn, String filterColumnValue) {
    super(name, table, column, filterColumn, filterColumnValue);
  }

  /** Express the selection list as a continuous unformatted set of
   * .html radio buttons.  For other display options, see
   * {@link #getHTMLFormatted(int how) getHTMLFormatted}
  @return string buffer containing the radio selector.*/
  public StringBuffer getHTML() {

    if ((this.entries == null) || (this.entries.size() <= 0)) {
      try {
	this.entries = this.getSelectionVector();
      } catch (SQLException e) {
      }
    }
    return this.makeRadiolector(this.fieldNamePrefix + this.getName(),
				this.entries, this.getChoice(), 0);
  }

  /** Express the selection list as a continuous unformatted set of
   * .html radio buttons.  For other display options, see {@link
   * #getHTMLFormatted(int how) getHTMLFormatted}.  The name of the
   * radio button set is included in the .html syntax so it is not
   * displayed as part of the generated .html.
  @return string buffer containing the radio selector.*/
  public StringBuffer getHTMLOnly() {

    return this.getHTMLFormatted(0);
  }

  /** Express the selection list as a set of .html radio buttons with
   * various formatting options.
    @param how controls formatting, see constants for list of options.
  @return string buffer containing the radio selector.*/
  public StringBuffer getHTMLFormatted(int how) {

    if ((this.entries == null) || (this.entries.size() <= 0)) {
      try {
	this.entries = this.getSelectionVector();
      } catch (SQLException e) {
      }
    }
    return this.makeRadiolector(this.fieldNamePrefix + this.getName(),
				this.entries, this.getChoice(), how);
  }

/** Generate a collection of radio buttons based on an input vector.
    @param what specifies the name of the selection field in the .html
    form.
    @param items is a vector of selection possibilities
    @param selectedItem is the current selection which should be
    highlighted when the .html is generated.
    @param how controls formatting, see constants for list of options.
    @return .html to generate the radio buttons
*/
  public StringBuffer makeRadiolector(String what, Vector items,
				      String selectedItem, int how) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;		// Walkable vector
    String name;		// Name of the selectable item
    String value;		// Value with the name
    String indexOf;		// String to search for
    int ix;			// Index of the pipe
    /**/

    if (selectedItem == null) { selectedItem = ES; }

    if (items != null) {
      if (how == AS_TABLE) {
	sb.append("<TABLE>\n");
      }
      for (enumV = items.elements(); enumV.hasMoreElements(); ) {
	name = (String)enumV.nextElement();
	if ( (ix = name.indexOf("|")) > 0) {
	  value  = name.substring(ix+1);
	  name   = name.substring(0, ix);
	} else {
	  value  = name;	// No separator
	}
	if (how == AS_TABLE) { sb.append("<TR><TD>"); }
	sb.append("<input type=\"radio\" name=\"" + what + "\" value=\"" +
		  name + "\"");
	if (selectedItem.equals(name)) {
	  sb.append(" checked");
	}
	sb.append(">");
	if (how == AS_TABLE) { sb.append("</td><TD>"); }
	sb.append(value);
	if (how == ONE_BUTTON_PER_LINE) { sb.append("<BR>"); }
	if (how == AS_TABLE) { sb.append("</td></tr>"); }
	sb.append("\n");
      }
      if (how == AS_TABLE) {
	sb.append("</table>\n");
      }
    }
    return sb;
  }

  /** Get the choice description in an uneditable text string. */
  public String getChoiceAsText() {
    String cho = this.getChoice();
    int which;
    /**/

    if (cho.equals(SelectorFieldPreload.ES)) { return cho; }
    if (this.numeric) {	// The number selects something from the vector
      which = SQLUtilities.integerFromString(cho);
      try {
	cho = (String)this.entries.elementAt(which);
      } catch (Exception e) {}
      if ( (which = cho.indexOf("|")) > 0) {
	cho = cho.substring(which+1);
      }
    }
    return cho;
  }
}
