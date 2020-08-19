/* @name makeHtmlTables.java

Class whose static methods generate string buffers for making .html tables

    Copyright (c) 2001 by Advanced Systems and Software Technologies,  All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: makeHtmlTables.java,v $
    Revision 1.2  2006/01/19 16:40:29  asst
    enum is a keyword

    Revision 1.1.1.1  2002/04/09 03:20:22  zonediet
    first import

*/

package asst.dbase;

import java.sql.*;

import java.util.Enumeration;
import java.util.Vector;

import asst.dbase.VariableSetModel;
import asst.dbase.DataBase;

/**
 * Generate a string representing the body of a .html table based on a
 * row set model.  Includes various constants which aid in setting up
 * tables.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname> */

public class makeHtmlTables
{

/** Obligatory null constructor.  */
  public makeHtmlTables() {
  }

  static public final String table = "<TABLE border=\"1\" cellpadding=\"2\">";
  static public final String tableC= "</table>";
  static public final String tr    = "<TR>";
  static public final String trC   = "</tr>\n";
  static public final String td    = "<TD>";
  static public final String tdC   = "</td>";
  static public final String th    = "<TH>";
  static public final String thC   = "</th>";

/** Generate a table content string in .html, one table row for each
    row of the model.  The input model can also be used to refresh a
    JTable.  */
  static public StringBuffer makeTableBody(VariableSetModel model) {
    StringBuffer sb = new StringBuffer();
    String value;
    int i;
    int j;

    if (model == null) { sb.append("<tr>Null model</tr>"); return sb; }

    sb.append(tr);
    for (j=0; j<model.getColumnCount(); j++) {
      sb.append(th + model.getColumnName(j) + thC);
    }
    sb.append(trC);

    for (i=0; ((i<model.getRowCount()) && (i < 400)); i++) {
      sb.append(tr);
      for (j=0; j<model.getColumnCount(); j++) {
	value = (String)model.getValueAt(i, j);
	// Preserve table cell borders, if the cell holds only spaces,
	// many browsers fail to show the cell borders.
	if (" ".equals(value)) { value = "&nbsp;"; }
	sb.append(td + value + tdC);
      }
      sb.append(trC);
    }
    return sb;
  }

/** Generate a selection list based on an input vector.  The
    parameters permit a choice of caption as well as specifying which
    of the possibilities to select when the selection list is dislayed.  */
  static public StringBuffer makeSelector(String what, Vector items,
					  String selectedItem) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;		// Walkable vector
    String name;		// Name of the selectable item
    String value;		// Value with the name
    int i;			// Count the entries to format the .htm;
    int ix;			// Index of the pipe
    /**/

    i = 0;
    sb.append(what + ": <SELECT NAME=\"" + what + "\">\n");

    if (selectedItem == null) { selectedItem = ""; }

    if ("".equals(selectedItem)) {
      sb.append("<OPTION selected VALUE=\"\">Make A Selection</option>\n");
    } else {
      sb.append("<OPTION VALUE=\"\">Make A Selection</option>\n");
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

/** Generate a selection list based on two table columns, one for the
    selections and one for their value.  The parameters name the
    selector as well as specifying which of the possibilities to
    show as selected when the selection list is dislayed.  */
  static public StringBuffer
  makeSelectorFromColumns(String what, String table, String valueCol,
			  String displayCol, String selectedValue)
    throws SQLException {
    StringBuffer sb = new StringBuffer();
    ResultSet results = null;	// Results of the retrieval
    Statement getRow;		// Retrieve the sequence number
    String query;		// Query to retrieve the values
    String selection;		// Human-readable selection
    String value;		// Value in the database
    /**/

    sb.append("<SELECT NAME=\"" + what + "\">\n");

    if (selectedValue == null) { selectedValue = ""; }

    if ("".equals(selectedValue)) {
      sb.append("<OPTION selected VALUE=\"\">Make A Selection</option>\n");
    } else {
      sb.append("<OPTION VALUE=\"\">Make A Selection</option>\n");
    }

    query = "select distinct " + valueCol + ", " + displayCol +
      " from " + table;

    getRow  = DataBase.connDB.createStatement();
    try {			// Finally closes the statement
      results = getRow.executeQuery(query);
      while (results.next()) {
	value     = results.getString(1);
	selection = results.getString(2);
	sb.append("<OPTION ");
	if (selectedValue.equals(value)) { sb.append("selected "); }
	sb.append("VALUE=\"" + value + "\">" + selection + "</option>\n");
      }
    } finally {
      if (results != null) { results.close(); }
      getRow.close();
    }

    sb.append("</select>\n");
    return sb;
  }
}
