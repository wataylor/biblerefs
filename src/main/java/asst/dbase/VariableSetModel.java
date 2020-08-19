/* @name VariableSetModel.java

    Copyright (c) 2001 by Advanced Systems and Software Technologies.  All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: VariableSetModel.java,v $
    Revision 1.17  2006/04/06 14:19:25  asst
    separate table head and body methods

    Revision 1.16  2006/01/19 16:40:13  asst
    more flexible cell control

    Revision 1.15  2005/03/15 04:10:52  asst
    sortable models

    Revision 1.14  2004/09/18 22:50:13  asst
    documentation

    Revision 1.13  2003/10/06 20:51:55  asst
    add background color to cell spec

    Revision 1.12  2003/08/07 20:18:34  asst
    generalize setValueAt to add rows

    Revision 1.11  2003/02/04 04:29:26  asst
    delete row, set column count

    Revision 1.10  2002/12/31 18:51:39  asst
    makeExcelBody accepts a variable line limit

    Revision 1.9  2002/11/24 04:09:16  asst
    do notnull column names during finalize

    Revision 1.8  2002/11/13 16:38:27  asst
    runtime exception on bad cell reference

    Revision 1.7  2002/10/16 01:25:28  asst
    added limit variable and methods

    Revision 1.6  2002/10/15 05:34:17  asst
    Added paging variables and methods

    Revision 1.5  2002/10/06 01:36:46  asst
    first upload

    Revision 1.4  2002/08/18 03:38:08  asst
    documentation

    Revision 1.3  2002/06/04 04:13:33  zonediet
    Variable limit on table size

    Revision 1.2  2002/05/18 20:21:23  zonediet
    added excel output method

    Revision 1.1.1.1  2002/04/09 03:20:50  zonediet
    first import

*/

package asst.dbase;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Implements a JTable model which stores and returns a variable
 * number of rows.  The table need not be perfectly rectangualar; it
 * records the maximum number of columns in any row and supplies blank
 * values for undefined cells.</p>

 * <P>The length of the longest row or the length of the column label
 * array determines how many columns there are in the table.  The
 * number of rows is determined by the number of rows in the row
 * vector.  The rows need not all be the same length; short rows are
 * padded with spaces to match the widest row.</p>

 * <P>The {@link #makeTableBody(int limit) makeTableBody} method
 * generates the table based on the contents of the column label array
 * and the array of cell values.  Alignment and color of individual
 * cells may be controlled by adding .html modifiers at the beginning
 * of each cell. This method also is able to display only part of a
 * table for situations where it is desirable to page forward and
 * backward in a table.</p>

 * <P>The {@link #canPageForward() canPageForward}, {@link
 * #canPageBackward() canPageBackward}, and {@link #canPageBothWays()
 * canPageBothWays} methods are intended to support the generation of
 * .html pages which include buttons to page hte model forward,
 * backward, or both depending on the model size and current display
 * area.  The {@link #setStartingLine(int start) setStartingLine},
 * {@link #getStartingLine() getStartingLine}, {@link #setPageSize(int
 * size) setPageSize}, and {@link #getPageSize() getPageSize} methods
 * help to control the operation of the {@link #pageForward()
 * pageForward} and {@link #pageBackward() pageBackward} methods. </p>

 * <P>The <code>makeTableBody</code> method generates a row of column
 * labels.  In addition, the {@link #setLabel(String label) setLabel}
 * and {@link #getLabel() getLabel} methods permit the user to
 * associate an arbitrary string with the table.  This label string is
 * ignored by <code>makeTableBody</code>; it is intended for any
 * convenient use in a .html page.</p>

 * <P></p>This class implements the <CODE>Comparable</code> interface;
 * the {@link #compareTo(Object o) compareTo} method orders models
 * based on their label strings which default to the empty string.
 * This makes it possible to sort an array of models by their labels.</p>

 * <P>The {@link #sortRows(java.lang.Class cl ) sortRows} method makes
 * it possible to sort the rows of a model based on user-specified
 * criteria.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see JTable */

public class VariableSetModel extends AbstractTableModel
  implements Comparable {

  /** Complex classes need empty strings. */
  public static final String ES = "";

/** Construct an empty table to be filled and passed to a JTable. */
  public VariableSetModel() {
    this.clearTheTable();	// Make sure it's empty
  }

/** Empty the table, this should be called before any new data are added.  */
  public void clearTheTable() {
    this.colNames = new String[0]; // Empty array
    this.rows = new Vector();	// Empty vector
    this.columnCount = 0;
  }

  /** Method to null the pointers to all internal structures
      as early as possible, this makes things easier for the garbage
      collector. */
  public void finalize() {
    int i;			// Array walker
    int j;			// Row walker
    String row[];		// One row
    /**/

    this.colNames = null;
    this.rows     = null;
    row           = null;
    // super.destroy();
  }

/** Override one column name by passing in one string, adds a column
    if the column number is out of range, returns the number of column
    names, updates the number of columns for the entire object if
    adding the label increases the overall number of columns.
    @param column zero-based column number in the table.
    @param colNameIn new name for the column
    @return number of columns in the table, adding a new column as
    needed.
*/
  public int setOneColumnHeading(int column, String colNameIn) {
    try {
      this.colNames[column] = colNameIn;
    } catch (ArrayIndexOutOfBoundsException e) {
      String[] newNames = new String[column + 1];
      for (int i=0; i<colNames.length; i++) {
	newNames[i] = colNames[i];
      }
      newNames[column] = colNameIn;
      this.colNames = newNames;
      if (this.columnCount < (column + 1)) { this.columnCount = column+1; }
    }
    return this.colNames.length;
  }

/** Set column names by passing in an array of strings; returns the
    number of columns labeled.  If this is the first call to the model,
    the column count is guaranteed to be the same as the column label
    count.  The column count is adjusted upward as necessary.
    @param colNamesIn array of column names
    @return number of columns in the table, may be greater than the
    number of column names if one of the rows is longer. */
  public int setColumnHeadings(String[] colNamesIn) {
    this.colNames = colNamesIn;
    if (this.columnCount < this.colNames.length) {
      this.columnCount = this.colNames.length;
    }
    return this.columnCount;
  }

/** Set column names by passing in a vector of strings and return the
    number of column labels.  It is convenient that all objects can be
    turned to strings.  This method is provided for the convenience of
    callers who don't know in advance how many columns the table will
    have; they can add names as they go along and submit the vector
    late in the process.  The column count is adjusted upward as
    necessary.
    @param colNamesIn vector of column names
    @return number of columns in the table, may be greater than the
    number of column names if one of the rows is longer. */
  public int setColumnHeadings(Vector colNamesIn) {
    String[] nameStrings = new String[colNamesIn.size()];
    int i;
    for (i=0; i<colNamesIn.size(); i++) {
      nameStrings[i] = (String)colNamesIn.elementAt(i);
    }
    return this.setColumnHeadings(nameStrings);
  }

/** Add a string array containing one row of table values to the
    table.  The longest of the longest row or the column label array
    determine how many columns there are in the table.  The number of
    rows is determined by the number of rows in the row vector.  The
    rows need not all be the same length; short rows are padded with
    spaces to match the widest row.  Copies the array so that the
    array can be changed without affecting the table.  The column
    count is adjusted upward as necessary.
    @param oneRow array of strings giving values to the cells in a row.*/
  public void addOneRow(String[] oneRow) {
    String[] theRow;
    int i;
    /**/

    theRow = new String[oneRow.length];
    for (i=0; i<oneRow.length; i++) {
      theRow[i] = oneRow[i];
    }

    this.rows.addElement(theRow);
    if (this.columnCount < oneRow.length) {
      this.columnCount = oneRow.length;
    }
  }

  /** Delete one row of the model.  Does not affect the column count.
   @param row the number of the row to delete*/
  public void deleteOneRow(int row) {
    try {
      this.rows.remove(row);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new RuntimeException("deleteOneRow excp wanted " +
				 row + " max available " +
	this.rows.size() + " " + e.toString());
    }
  }

  /** Delete all the rows in the table by clearing the vector.  Does
   * not affect the column count.*/
  public void deleteAllRows() {
    this.rows = new Vector(this.rows.size());
  }

  /** Get one row of the model.
   @param row the number of the row to return
  @return a String array representing the cells of the row*/
  public String[] getOneRow(int row) {
    try {
      return((String[])this.rows.elementAt(row));
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new RuntimeException("getOneRow excp wanted " +
				 row + " max available " +
	this.rows.size() + " " + e.toString());
    }
  }

/** Add a vector containing values for one row to the table, exiting
    if the vector is null.  The column count is adjusted upward as
    necessary.
    @param oneRow vector of strings giving values to the cells in a row. */
  public void addOneRow(Vector oneRow) {
    if (oneRow == null) return;	// Null vectors are OK
    String[] valueStrings = new String[oneRow.size()];
    int i;
    for (i=0; i<oneRow.size(); i++) {
      valueStrings[i] = (String)oneRow.elementAt(i);
    }
    this.addOneRow(valueStrings);
  }

/** Tell the table to repaint itself.  This should be called after all
    the rows have been added.  */
  public void repaintTable() {
    fireTableChanged(null);
  }

/** Return the number of columns in the table. */
  public int getColumnCount() {
    return columnCount;
  }

  /** Set the value of the column count.  This is generally used for
   * decrementing the coolumn count so as not to display columns at
   * the right edge of the table.  This lets a table be created with
   * extra filter columns which can be used to trigger row deletion.
   * It is the caller's responsibilty not to set this value too large
   * for the actual data.
   @param count new value for the column count */
  public void setColumnCount(int count) {
    this.columnCount = count;
  }

/** Return the number of rows in the table. */
  public int getRowCount() {
    return rows.size();
  }

/** Return a cell value, returns a space if the reference is outside
    the array.  This permits any cell of the array to be empty and any
    row to be shorter than others.
    @param row zero-based row number.
    @param column zero-based column number.
    @return value at the selected cell or a space if the cell is empty */
  public Object getValueAt(int row, int column) {
    String element;
    try {
      element = ((String[])rows.elementAt(row))[column];
      if (element == null) return " ";
      return element;
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    return " ";
  }

  /** Set a cell value in preparation for a new display action.  Find
      the approprate string array, then set the appropriate element
      thereof.  If the row array is too short, it is lengthened,
      permitting users to add columns at will.  If the row vector is
      short, it is lengthened so that users can add rows at will.
    @param row zero-based row nmber.
    @param column zero-based column number.
    @param value specifies what to put in the cell.*/
  public void setValueAt(int row, int column, String value) {
    String[] oneRow = null;
    /**/

    try {
      oneRow = (String[])rows.elementAt(row);
    } catch (ArrayIndexOutOfBoundsException e) {
      while (rows.size() <= row) { // Add rows until there are enough
	oneRow = new String[column + 1];
	rows.add(oneRow);
      }
      if (this.columnCount < (column + 1)) { this.columnCount = column+1; }
    }

    try {
      oneRow[column] = value;
    } catch (ArrayIndexOutOfBoundsException e) {
      String[] newRow = new String[column + 1];
      for (int i=0; i<oneRow.length; i++) {
	newRow[i] = oneRow[i];
      }
      newRow[column] = value;
      rows.setElementAt(newRow, row);
      if (this.columnCount < (column + 1)) { this.columnCount = column+1; }
    }
  }

  /** Return a column name, supplying a space if there isn't one.
   @param column zero-based column number.
   @return column name or space if the name is empty.*/
  public String getColumnName(int column) {
    try {
    return ((colNames[column] == null) ? " " : colNames[column]);
    } catch (ArrayIndexOutOfBoundsException e) {
      return " ";
    }
  }

/** Convert a cell value string to the appropriate table cell and
    return the number of cells to skip.  Looks for the .html verb
    <code>colspan</code>, <code>align</code>, or <code>bgcolor</code>
    at the beginning of the the cell content string and acts
    accordingly.
    @param value cell content
    @param sb buffer which accumulates .html to generate the cell
    value as a table.
    @param ct .html string such as <code>TH</code>,
    <code>strong</code> or <code>TD</code> which tells how to generate
    the .html table cell of the appropriate width.
    @return number of cells to skip to generate the next .html table
    cell.*/
  private int appendValue(String value, StringBuffer sb, String ct) {
    int index;
    int columns = 0;
    /**/

    if (" ".equals(value)) { value = "&nbsp;"; }
    if (value.startsWith("colspan")) {
      index = value.indexOf("\"", 9); // Find the close quote
      columns=Integer.parseInt(value.substring(9, index));
      sb.append("<" + ct + " align=\"center\" colspan=\"" + columns + "\">" +
		value.substring(index+1) + "</" + ct + ">");
      columns--;	// Skip some cells
    } else if (value.startsWith("align=\"")) {
      index = value.indexOf("\"", 7); // Find the close quote
      sb.append("<" + ct + " " + value.substring(0, index+1) + "> " +
		value.substring(index+1) + "</" + ct + ">");
    } else if (value.startsWith("bgcolor=\"")) {
      index = value.indexOf("\"", 9); // Find the close quote
      sb.append("<" + ct + " " + value.substring(0, index+1) + "> " +
		value.substring(index+1) + "</" + ct + ">");
    } else if (value.startsWith("|")) {
      index = value.indexOf("|", 2); // Find the close pipe
      sb.append("<" + ct + " " + value.substring(1, index) + "> " +
		value.substring(index+1) + "</" + ct + ">");
    } else {
      sb.append("<" + ct + ">" + value + "</" + ct + ">");
    }
    return columns;
  }

  /** Generate a table which is limited to 500 lines. */
  public StringBuffer makeTableBody() {
    return this.makeTableBody(500, ES, ES, ES);
  }

/** Generate a header row for a table.  Augments the string buffer as
 * a side effect but returns it in case it was passed in as null.
 @param sb String Buffer to be augmented or null
 @param headerMod string to modify the TR command for the header row.
 @return old string buffer or new one.*/
  public StringBuffer makeTableHead(StringBuffer sb, String headerMod) {
    int j;
    String value;
    /**/

    if (sb == null) { sb = new StringBuffer(100); }

    sb.append("<TR " + headerMod + " >");
    for (j=0; j<this.getColumnCount(); j++) {
      value = this.getColumnName(j);
      j += this.appendValue(value, sb, "TH");
    }
    sb.append("</tr>\n");
    return sb;
  }

/** Generate the content part of a table body
    @param limit maximum number of rows to generate.
    @param er put into even-rowed &lt;TR&gt; operators starting with
    row 0.
    @param er put into odd-rowed &lt;TR&gt; operators starting with
    row 0
    @param sb String Buffer to be augmented or null
    @return StringBuffer containing the .html table definition.*/

  public StringBuffer makeTableDecap(int limit, String er, String or,
				     StringBuffer sb) {
    String value;
    int i;			// Select rows
    int j;			// Count columns
    int k;			// Count rows actually dislayed
    int index;			// Where the colspan count ends
    int columns;		// Number of columns for a colspan cell
    /**/

    if (sb == null) { sb = new StringBuffer(500); }

    k = 0;
    for (i=this.startingLine; ((i<this.getRowCount()) && (k < limit)); i++) {
      sb.append("<TR");
      if ((k & 1) != 0) {
	sb.append(" " + or);
      } else {
	sb.append(" " + er);
      }
      sb.append(">");
      k++;			// Count rows displayed
      for (j=0; j<this.getColumnCount(); j++) {
	value = (String)this.getValueAt(i, j);
	j += this.appendValue(value, sb, "TD");
      }
      sb.append("</tr>\n");
    }
    return sb;
  }

/** Generate a striped table content string in .html, one table row for each
    row of the model plus one row for the column headers.  The number
    of rows in the table is limited to the lesser of the limit passed
    in or the current <CODE>pageSize</code>.  The starting row is
    determined by the current value of <CODE>startingLine</code> which
    defaults to zero.</p>

    <P>If a cell
    value begins with <B>colspan="&lt;value&gt;"</b>, the column span
    command is extracted from the cell and inserted into the &lt;TD
    &gt; string and the value is centered.  The quotes are
    <B>MANDATORY</b>; the parser depends on them to isolate the number
    of columns needed for the cell.</p>

    <P>The column count is incremented by one less than the number of
    columns spanned so that some number of cells <B>after</b> the
    <B>colspan</b> cell are skipped.  For example, a cell value
    beginning <B>colspan="3"</b> generates a column three columns wide
    and the following two cells in the row are skipped.</p>

    <P>If the column value starts with <B>align="&lt;value&gt;"</b> or
    with <B>bgcolor="&lt;value&gt;"</b> the <B>align</b> or
    <B>bgcolor</b> is moved to the &lt;TD &gt; string.  The quotes are
    <B>MANDATORY</b>.  See <CODE>appendValue</code> for details.</p>

    <P>Alternately, if the column value starts with <B>|</b>, the
    characters between two pipe characters are extracted from the cell
    and inserted into the &lt;TD &gt; string.  This permits a cell to
    have both a color and alignment, for example.  See
    <CODE>appendValue</code> for details.</p>

    @param limit maximum number of rows to generate.
    @param headerMod put into the header row &lt;TH&gt; code to modify
    the appearance of the header row.
    @param er put into even-rowed &lt;TR&gt; operators starting with
    row 0.
    @param er put into odd-rowed &lt;TR&gt; operators starting with
    row 0
    @return StringBuffer containing the .html table definition.*/

  public StringBuffer makeTableBody(int limit, String headerMod,
				    String er, String or) {
    StringBuffer sb = new StringBuffer();
    /**/

    if (limit > this.pageSize) { limit = pageSize; }

    sb = makeTableHead(sb, headerMod);
    sb = makeTableDecap(limit, er, or, sb);

    return sb;
  }

  /** Generate a non-striped table with a limited number of rows.
   @param limit number of rows to generate  */
  public StringBuffer makeTableBody(int limit) {
    return makeTableBody(limit, ES, ES, ES);
  }

/** Generate a table content string in text-tab-delimited Excel
    format, one table row for each row of the model plus one row for
    the column headers.  Text-tab-delimited format does NOT support
    .html modifiers, it simply spins out the text in a tab-separated
    way, one tab-separated cell per table cell.  This mehotd generates
    only 400 lines, to generate a table with an arbitrary number of
    lines, pass an integer limit.
    @return StringBuffer containing the .html table definition.  */

  public StringBuffer makeExcelBody() {
    return this.makeExcelBody(400);
  }

  /** Render the head row of the table as a tab-separated line.
      @param sb String Buffer to be augmented or null, creates one if
      null.
   */
  public StringBuffer makeExcelHead(StringBuffer sb) {
    String value;
    int j;

    if (sb == null) { sb = new StringBuffer(100); }

    for (j=0; j<this.getColumnCount(); j++) {
      value = this.getColumnName(j);
      sb.append(value + "\t");
    }
    sb.append("\n");
    return sb;
  }

  /** Render the body of the table as a series of tab-separated lines.
      @limit maximum number of lines of table body to generate.
      @param sb String Buffer to be augmented or null, creates one if
      null.
*/
  public StringBuffer makeExcelDecap(int limit, StringBuffer sb) {
    String value;
    int i;
    int j;

    if (sb == null) { sb = new StringBuffer(600); }

    for (i=0; ((i<this.getRowCount()) && (i < limit)); i++) {
      for (j=0; j<this.getColumnCount(); j++) {
	value = (String)this.getValueAt(i, j);
	sb.append(value + "\t");
      }
      sb.append("\n");
    }
    return sb;
  }

/** Generate a table content string in text-tab-delimited Excel
    format, one table row for each row of the model plus one row for
    the column headers.  Text-tab-delimited format does NOT support
    .html modifiers, it simply spins out the text in a tab-separated
    way, one tab-separated cell per table cell.
    @param limit is the maximum number of rows to output
    @return StringBuffer containing the .html table definition.  */

  public StringBuffer makeExcelBody(int limit) {
    StringBuffer sb;
    String value;
    int i;
    int j;
    int index;			// Where the colspan count ends
    int columns;		// Number of columns for a colspan cell
    /**/

    sb = makeExcelHead(null);
    sb = makeExcelDecap(limit, sb);
    return sb;
  }

  /** Create a table which is rotated 90 degrees; works only if there
      is exactly one row.
      @param title string which labels the table
      @return StringBuffer containing the .html table definition.
  */
  public StringBuffer makeTableSideways(String title) {
    StringBuffer sb;
    String value;
    int i;
    int j;
    /**/

    if (this.getRowCount() > 1) { return this.makeTableBody(); }

    sb = new StringBuffer();

    if ((title != null) && (title.length() > 0)) {
      sb.append("<TR><TH colspan=\"2\" align=\"center\">" + title +
		"</th></tr>");
    }

    for (j=0; j<this.getColumnCount(); j++) {
      sb.append("<TR>");
      value = this.getColumnName(j);
      this.appendValue(value, sb, "TD");
      value = (String)this.getValueAt(0, j);
      this.appendValue(value, sb, "TD");
      sb.append("</tr>\n");
    }

    return sb;
  }

  /** Set the first line of the display.  It is sometimes desirable to
   * display only a part of a table and permit users to page back and
   * forth.  In this case, the model needs to know where to start the
   * display and how much to move the display start when paging
   * forward or backward.
   @param start zero-based first line of the display.*/
  public void setStartingLine (int start) {
    this.startingLine = start;
  }

  /** Get the first line of the display.  It is sometimes desirable to
   * display only a part of a table and permit users to page back and
   * forth.  In this case, the model needs to know where to start the
   * display and how much to move the display start when paging
   * forward or backward.
   @return the first line of the display.  */
  public int getStartingLine () {
    return this.startingLine;
  }

  /** Set the number of lines in a page.  It is sometimes desirable to
   * display only a part of a table and permit users to page back and
   * forth.  In this case, the model needs to know where to start the
   * display and how much to move the display start when paging
   * forward or backward.
   @param size the number of lines in a page; */
  public void setPageSize (int size) {
    this.pageSize = size;
  }

  /** Get the number of lines in a page.  It is sometimes desirable to
   * display only a part of a table and permit users to page back and
   * forth.  In this case, the model needs to know where to start the
   * display and how much to move the display start when paging
   * forward or backward.
   @return the number of lines in a page; */
  public int getPageSize () {
    return this.pageSize;
  }

  /** Return the 1-based line number of the first visible line.*/
  public int getFirstVisibleLine() {
    return this.startingLine+1;
  }

   /** Return the 1-based line number of the last visible line given
    * the first line and the current page size.*/
  public int getLastVisibleLine() {
    int last = this.startingLine + this.pageSize;
    /**/

    if (last > this.getRowCount()) { last = this.getRowCount(); }
    return last;
  }

 /** Move the display one page forward.  This has no effect if the
   * model is empty because the starting row is not permitted to grow
   * past the last row of the model.  No matter how many times the
   * model is paged forward, there is a minimum of one row visible.
   @return the starting line of the display after paging forward.*/
  public int pageForward() {
    this.startingLine += this.pageSize;
    if (this.startingLine >= this.getRowCount()) {
      this.startingLine = this.getRowCount() -1;
    }
    return this.startingLine;
  }

  /** Move the display one page backward.  This has no effect if the
   * model is empty because the starting row is not permitted to move
   * before the first row of the model.
   @return the starting line of the display after paging backward.*/
  public int pageBackward() {
    this.startingLine -= this.pageSize;
    if (this.startingLine < 0) {
      this.startingLine = 0;
    }
    return this.startingLine;
  }

  /** Determine whether the model can page backward.  A model is able
   * to page backward if <CODE>startingLine</code> selects any line
   * other than the 0th line of the model. */
  public boolean canPageBackward () {
    return (this.startingLine > 0);
  }

  /** Determine whether the model can page forward.  A model can page
   * forward if <CODE>startingLine</code> selects any line which does
   * not fall within <CODE>pageSize</code> lines of the end of the
   * model as defined by its row count.*/
  public boolean canPageForward () {
    return ((this.startingLine + this.pageSize) < this.getRowCount());
  }

  /** Determine whether a model can page both ways.  This is needed
   * when putting up either no paging buttons, one button, or
   * both.  */
  public boolean canPageBothWays() {
    return (this.canPageBackward() && this.canPageForward());
  }

  /** Set the label string.  It is occasionally helpful to label a
   model beyond its column headings.  It is up to the user to position
   the label artfully.  An array of models can be sorted by label.*/
  public void setLabel(String label) {
    this.label = label;
  }

  /** Get the label string.  It is occasionally helpful to label a
   model beyond its column headings.  It is up to the user to position
   the label artfully.  An array of models can be sorted by label.*/
  public String getLabel() {
    return this.label;
  }

  /** Compare two models for sorting based on their labels*/
  public int compareTo(Object o) {
    VariableSetModel him;
    /**/

    him = (VariableSetModel)o;
    return this.label.compareTo(him.label);
  }

  /** Set the model size limit.  It is occasionally helpful to record
   * a limit to the size of a particular model.  It is up to the
   * caller to enforce the limit when constructing the model.
   @param limit size limit for the model.*/
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /** Get the model size limit.  It is occasionally helpful to record
   * a limit to the size of a particular model.  It is up to the
   * caller to enforce the limit when constructing the model.*/
  public int getLimit() {
    return this.limit;
  }

  /**
   * Interface to define how rows can be sorted */
  public interface RowSorter {

  /**
   * Used to retrieve one row of the model from the
   * <CODE>RowSorter</code> object after sorting the array of
   * <CODE>RowSorter</code> objects.*/
    public String[] getStrings();
  /**
   * Used to store one row of the model in the <CODE>RowSorter</code>
   * when creating the array of <CODE>RowSorter</code> objects.*/
    public void setStrings(String[] strings);
  }

  /**
   * Sort the rows by a caller-specified criterion
   @param cl user-supplied class which implements a default
   constructor, the <CODE>Comparable</code> interface, and the
   <CODE>VariableSetModel.RowSorter</code> interface.  This method
   instantiates an array of these objects and passes each instance one
   row of the table expressed as a <CODE>String[]</code> using the
   <CODE>setStrings(String[] strings)</code> method of the
   <CODE>RowSorter</class> class which is passed in.  The {@link
   #compareTo(Object o) compareTo} method required by the
   <CODE>Comparable</code> interface is used to compare rows for
   sorting, after which the rows are put back in the model using the
   <CODE>getStrings()</code> method of this class.  */
  public void sortRows(Class cl) {
    Object[] ray;
    int i;
    int Len;
    /**/

    try {
      Len = this.getRowCount();
      ray = new RowSorter[Len];
      for (i=0; i<Len; i++) {
	ray[i] = cl.newInstance(); // Create a new object
	((RowSorter)ray[i]).setStrings((String[])this.rows.elementAt(i));
      }
      java.util.Arrays.sort(ray); // sql has an Arrays class also
      this.deleteAllRows();	// Keeps the column headings
      for (i=0; i<Len; i++) {
	this.addOneRow(((RowSorter)ray[i]).getStrings());
      }

    } catch (Exception e) {
      throw new RuntimeException("sortRows excp " + e.toString());
    }
  }

/** Array of strings for column names.  */
  public String[] colNames = new String[0]; // Empty name array

/** Vector of arrays of strings for the rows.  */
  Vector rows = new Vector();	// Empty row vector

/** Column count stores the length of the longest string array passed
    into the model, this can be either the column label array or the
    longest row. */
  int columnCount = 0;		// Number of columns in the result set

  /** Starting line of the display.  It is sometimes desirable to
   * display only a part of a table and permit users to page back and
   * forth.  This number is set to zero when the model is created.  */
  int startingLine = 0;

  /** Store the number of lines in one page.  It is sometimes
   * desirable to display only a part of a table and permit users to
   * page back and forth.  This number is set to a VERY LARGE number
   * when the model is created so that display is not limited by the
   * default page size.*/
  int pageSize = 9999999;

  /** It is occasionally helpful to label a model beyond its column
   * headings.  It is up to the user to position the label artfully.
   * Arrays of models can be sorted by label.*/
  String label = ES;

  /** It is occasionally helpful to record a limit in the size of a
   * particular model.  It is up to the caller to enforce the limit
   * when constructing the model.*/
  int limit;
}
