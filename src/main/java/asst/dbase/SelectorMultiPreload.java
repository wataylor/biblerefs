/* @name SelectorMultiPreload.java

Selector which supports multiple selections at the same time

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SelectorMultiPreload.java,v $
    Revision 1.6  2006/01/19 16:40:54  asst
    read-only attribute for fields and forms

    Revision 1.5  2003/07/08 19:03:44  asst
    documentation

    Revision 1.4  2002/10/22 02:53:28  asst
    getChoiceAsText

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

import javax.servlet.http.HttpServletRequest;

/**
 * Extend the selector to generate a multiple-value selection box.
 * This changes the format of the value stored in that the value or
 * values returned are wrapped in pipes and stored as a pipe-separated
 * string.  The values may be either strings or numbers depending on
 * whether the selection vector strings contain pipes or not, but in
 * any case, they are stored as a pipe-separated and pipe-bracketed
 * string for convenience in setting them as selected.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorRadioPreload
 */

public class SelectorMultiPreload extends SelectorDBPreload {

  /** Obligatory constructor.*/
  public SelectorMultiPreload() {
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
  public SelectorMultiPreload(String name, String table, String column) {
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
  public SelectorMultiPreload(String name, String table, String column,
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
  public SelectorMultiPreload(String name, String table, String column,
			   String filterColumn, String filterColumnValue) {
    super(name, table, column, filterColumn, filterColumnValue);
  }

  /** Ask the object to record the current form value which has been
      entered for its field.  If the form returns a null value, the
      field was not mentioned in the form so the form has no effect on
      this selector object.  If the field returns a different value,
      however, the dirty bit is set to indicate that the field must be
      written to the database.  There is one exception - if the field
      has a null value and the form returns an empty string, this is
      not regarded as a change.</p>

      <P>If the parameter returns multiple values, the values are
      strung together with "|" characters between them.  This feature
      is used to support multiple selections.
  @param request from the form which may have a parameter whose name
  matches the name of this object, in which case the parameter value
  replaces the current choice.*/
  public void setChoice(HttpServletRequest request) {
    StringBuffer aParam;
    String[] values;
    /**/

    values = request.getParameterValues(this.fieldNamePrefix + this.name);

    if (values == null) return; // No values

    aParam = new StringBuffer();
    if (values.length < 2) {
      aParam.append("|" + values[0] + "|");	// Bracket in pipes
    } else {
      aParam.append("|");
      for (int i=0; i<values.length; i++) {
	aParam.append(values[i] + "|");
      }
    }
    // If no values are set, the resulting string is "||" which is
    // equivalent to the null choice.  So if the choice is not null, it
    // means that the form cleared some former choices.  Set the value
    // to null in that case.
    if (aParam.length() <= 2) {	// No choices are selected
      if (this.getChoice().length() > 0) {
	this.choice = null; // Clear it
	this.setDirtyFlag(true); // Note that the value changed.
      }
    } else {
      this.setDirtyChoice(aParam.toString());
    }
  }

  /** Express the choice as a comma-separated string of values
      @return comma-separated string.
  */
  public String getChoiceAsText() {
    return this.makeMultiString(this.entries, this.choice).toString();
  }

  /** Compute the choice as a comma-separated string
   @param items selection vector
   @param selectedItem pipe-separated string of selections
   @return comma-separated selection list */
  public StringBuffer makeMultiString(Vector items, String selectedItem) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;		// Walkable vector
    String name;		// Name of the selectable item
    String value;		// Value with the name
    String indexOf;		// String to search for
    int i;			// Count the entries to format the .htm;
    int ix;			// Index of the pipe
    /**/

    i = 0;
    if (selectedItem == null) { selectedItem = SelectorDBPreload.ES; }

    if (ES.equals(selectedItem)) {
      return sb;
    }
    if (items != null) {
      for (enumV = items.elements(); enumV.hasMoreElements(); ) {
	name = (String)enumV.nextElement();
	if ( (ix = name.indexOf("|")) > 0) {
	  value  = name.substring(ix+1);
	  name   = name.substring(0, ix);
	} else {
	  value  = name;	// No separator
	}
	if (selectedItem.indexOf("|" + name + "|") >= 0) {
	  if (sb.length() > 0) { sb.append(", "); }
	  sb.append(value);
	}
      }
    }
    return sb;
  }

  /** Express the selection list as a named .html SELECTOR object
   * which permits multiple choices.  A multiple selector should NOT
   * be set up as a numeric SQL column because the selection value or
   * values are stored as a pipe-separated string which causes an SQL
   * error when stored into a numeric SQL column..
  @return string buffer containing the multiple selector.*/
  public StringBuffer getHTML() {
    String what = this.fieldNamePrefix + this.getName();
    /**/

    if (readOnly) { return new StringBuffer(getChoiceAsText()); }

    if ((this.entries == null) || (this.entries.size() <= 0)) {
      try {
	this.entries = this.getSelectionVector();
      } catch (SQLException e) {
      }
    }
    return new StringBuffer(what + ": " +
			    this.makeMultilector(what, this.entries,
						 this.getChoice(), 4));
  }

  /** Generate a selection list with no label.  This is used when the
      .html page labels the selector.
  @return .html for an unlabeled selection list.*/
  public StringBuffer getHTMLOnly() {
    String what = this.fieldNamePrefix + this.getName();
    /**/

    if (readOnly) { return new StringBuffer(getChoiceAsText()); }

    return this.makeMultilector(what, this.entries, this.getChoice(), 4);
  }

  /** Express the field in a .html text area without labeling it,
      disregard the width, but use the height to set the height of the
      selector box..
  @param width number of columns in the selection area, this is ignored.
  @param height number of rows in the box.
  @return .html needed to generate the selection list*/
  public StringBuffer getHTMLOnly(int width, int height) {
    String what = this.fieldNamePrefix + this.getName();
    if (readOnly) { return new StringBuffer(getChoiceAsText()); }
    return this.makeMultilector(what,
				this.entries, this.getChoice(), height);
  }

/** Generate a selection list based on an input vector.  The
    parameters permit a choice of caption as well as specifying which
    of the possibilities to select when the selection list is dislayed.
    @param what specifies the name of the selection field in the .html
    form.
    @param items is a vector of selection possibilities
    @param selectedItem is the current selection which should be
    highlighted when the .html is generated.
    @param height number of rows of height of the selection box
    @return .html to generate the selection box.
*/
  public StringBuffer makeMultilector(String what, Vector items,
				      String selectedItem, int height) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;		// Walkable vector
    String name;		// Name of the selectable item
    String value;		// Value with the name
    String indexOf;		// String to search for
    int i;			// Count the entries to format the .htm;
    int ix;			// Index of the pipe
    /**/

    i = 0;
    sb.append("<SELECT NAME=\"" + what + "\" size=\"" + height +
	      "\" multiple id=\"" + what + "\" " + this.addParam + ">\n");

    if (selectedItem == null) { selectedItem = SelectorDBPreload.ES; }

    if (ES.equals(selectedItem)) {
      sb.append("<OPTION selected VALUE=\"\">Make Selections</option>\n");
    } else {
      sb.append("<OPTION VALUE=\"\">Make Selections</option>\n");
    }

    if (items != null) {
      for (enumV = items.elements(); enumV.hasMoreElements(); ) {
	name = (String)enumV.nextElement();
	if ( (ix = name.indexOf("|")) > 0) {
	  value  = name.substring(ix+1);
	  name   = name.substring(0, ix);
	} else {
	  value  = name;	// No separator
	}
	sb.append("<OPTION ");
	if (selectedItem.indexOf("|" + name + "|") >= 0) {
	  sb.append("selected ");
	}
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
}
