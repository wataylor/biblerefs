/* @name AnyFieldArray.java

    Copyright (c) 2002 by Advanced Systems and Software Technologies
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: AnyFieldArray.java,v $
    Revision 1.5  2007/11/28 05:05:29  asst
    makeTableBody

    Revision 1.4  2007/05/15 01:55:58  asst
    get and set ID col, deleteRecord

    Revision 1.3  2007/01/17 04:01:50  asst
    documentation

    Revision 1.2  2003/12/07 02:30:29  asst
    documentation

*/

package asst.dbase;

import javax.servlet.http.HttpServletRequest;

import java.lang.InstantiationException;
import java.lang.IllegalAccessException;

import java.sql.*;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import asst.dbase.TwixtTableAndForm;
import asst.dbase.DataBase;

/**
 * Base class for arrays of <code>SelectorManager</code> objects.  An
 * array of selector managers implements an object which can read,
 * write, display, and edit one database row at a time.  An array of
 * these objects can represent a two-dimensional editable field
 * similar to a spreadshet.</p>

 * <P>This class defines methods to read, write, and create table
 * rows as well as an abstract method to specify default values after
 * a field row is cleared.  Setting the defaults depends on the
 * specifics of the class which extends this class.</p>

 * <P>SelectorManagers are generally stored in arrays when used in
 * servlets or .jsp pages which process database tables one row at a
 * time.  Implementing a class which stores an entire database row in
 * an internal array makes it possible to process more than one row at
 * a time by creating an array of such one-row objects.  This makes it
 * simple to create two-dimensional object editors.  {@link
 * asst.dbase.ClassManip} uses introspection to facilitate
 * initializing such arrays.</p>

 * <P><B>Note:</b> Extending classes should <B>NOT</b> define a field
 * whose name is the same as the index column for the table.  If there
 * is such a field, its value overwrites the index which produces
 * inconsistent results whenthe row is written to the database.  Thus,
 * if the index column is named ID, there should <B>not</b> be a field
 * named "ID" in the extending class.

 * @author Web Work
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.TwixtTableAndForm
 * @see asst.dbase.ClassManip
 * @see asst.dbase.SelectorManager
 */

public abstract class AnyFieldArray {

  /** Obligatory constructor */
  public AnyFieldArray() {
  }

  /**  Constructor which sets the table name or names and the index
   column name.  The usual practice is for an extending class to be
   associated with a specific database table so the no-argument
   constuctor in the extending class calls this constructor to specify
   the table name, index column name, snd supplemnt table list.</p>

   <P><B>Note:</b> The constructor does <B>NOT</b> set the default
   field values because the field array is not initialized until the
   extending constructor finishes.  Thus, setting the defaults is up
   to the extending class constructor.</p>

   <P>The extending constructor must allocate the
   <CODE>fieldMgrs</code> array and initialize it with
   <CODE>SelectorManager</code> objects whose names match the names of
   the database columns so that they can be read automatically.  These
   objects are fields of the class and must be declared
   <CODE>public</code> so that the methods to set them are accessible
   from outside the class.
   @param tableName name of the table to be manipulated, may be null
   if the fields are being used for another purpose.
   @param idCol name of the numerical column within the table and all
   supplemental tables which stores the unique row index; defaults to
   "ID."
   @param suppTables list of names of additional tables whichhave the
   same column key as the main table and which are to be initialized
   with a new row whenver the main table gets a new row.  This should
   be set to null if there are no tables which join with the main
   table ID column. */
  public AnyFieldArray(String tableName, String idCol, String[] suppTables) {
    this.tableName  = tableName;
    this.idCol      = idCol;
    this.suppTables = suppTables;
  }

  /** Read object values from servlet request, set values, and clear
   all of the error flags.
   @param request servlet request which contains .html field values.
   Any values whose names match the names of selector managers in this
   object are read into the <code>choice</code> fields */
  public void ReadFromRequest(HttpServletRequest request) {
    int i;
    SelectorManager fm;
    /**/

    for (i=0; i<fieldMgrs.length; i++) {
      if ( (fm = fieldMgrs[i]) != null) {
	fm.setChoice(request);
	fm.setErrorFlag(false);
      }
    }
  }

  /** Routine which reads objects from a SQL table and returns an
   * array thereof.  This requires introspection to create the classes
   * and fill in the field values.
   @param table specifies which table to read from the database.
   @param column specifies the row index column, it must be numeric.
   @param where is a <CODE>where</code> and/or <CODE>order by</code>
   clause to specify what is read.
   @param className specifies which class to instantiate.  The class
  must extend <CODE>AnyFieldArray</code>
  @return vector of objects of the specified class.*/
  public static Vector<AnyFieldArray>
  ReadObjectsFromTable(String table, String column, String where,
		       String className) {
    String query = "select " + column + " from " + table;
    ResultSet results = null;	// Results of the retrieval
    Statement stmt = null;	// Retrieve the IDs
    AnyFieldArray afa;
    Vector<AnyFieldArray> vec = new Vector<AnyFieldArray>();
    Class aClass;		// The chosen class object
     /**/

    if (where != null) {
      query += " " + where;
    }

    try {
      aClass = Class.forName(className); // Load the class
    } catch (Exception e) {
      throw new RuntimeException("forName excpt on " + className + " " +
				 e.toString());
    }

   // Outer try to cover creating the statements.
    try {
      stmt = DataBase.connDB.createStatement();
      try {
	results = stmt.executeQuery(query);
	while (results.next()) {
	  afa = (AnyFieldArray)aClass.newInstance(); // Create a new object
	  afa.setTableName(table);
	  afa.setPrefixes(results.getString(1)+ "_");
	  afa.setRecordID(results.getInt(1));
	  afa.readRecord();
	  vec.addElement(afa);
	}
      } catch (SQLException e) {
	throw new RuntimeException("ReadObjectsFromTable excp " + query + " " + e.toString());
      } catch (InstantiationException e) {
	throw new RuntimeException("ReadObjectsFromTable excp " + query + " " + e.toString());
      } catch (IllegalAccessException e) {
	throw new RuntimeException("ReadObjectsFromTable excp " + query + " " + e.toString());
      }
    } catch (SQLException e) {
      throw new RuntimeException("ReadObjectsFromTable excp " + e.toString());
    } finally {
      try {
	if (results  != null) { results.close();  }
	if (stmt     != null) { stmt.close();     }
      } catch (SQLException e) {
      }
    }
    return vec;
  }

  /** This method is abstract because setting appropriate default
      values when a record is cleared depends on the specifics of the
      class.  The extending class must implement its own version which
      may do nothing.*/
  public abstract void specifyDefaults();

  /** Read the form and determine any updates to the field parameters.
   * This sets dirty bits in preparation for logging changes and/or
   * writing changed values out to the database.*/
  public void readFormValues(HttpServletRequest request) {
    int i;
    /**/

    for (i=0; i<fieldMgrs.length; i++) {
      if (fieldMgrs[i] != null) { fieldMgrs[i].setChoice(request); }
    }
  }

  /** Delete the indicated record. */
  public void deleteRecord() {
    deleteRecord(recordID);
    setRecordID(0);		// There is no longer a record here.
  }

  public void deleteRecord(int which) {
    if (which == 0) { return; }

    String query = "delete from " + tableName + " where " +
      idCol + "=" + which;
    Statement stmt = null;	// Retrieve the IDs

    try {
      stmt = DataBase.connDB.createStatement();
      stmt.execute(query);
    } catch (SQLException e) {
      throw new RuntimeException("delete excpt on " + query + " " +
				 e.toString());
    }  finally {
      try {
	if (stmt     != null) { stmt.close();     }
      } catch (SQLException e) {
      }
      //System.out.println(query);
    }
  }

  /** Clear the fields in the field manager array and set the recordID
      to zero indicating that there are no data and that the array of
      field objects is not yet associated with any table row.
  @param wantDefaults if true, calls the default setting method for
  the class.*/
  public void clearRecord(boolean wantDefaults) {
    TwixtTableAndForm.ClearAllFormFields(fieldMgrs);
    recordID = 0;
    if (wantDefaults) { this.specifyDefaults(); }
  }

  /** Paramaterless clear method which clears the fields and then sets
      default values.*/
  public void clearRecord() {
    this.clearRecord(true);
  }

  /** A new record ID must be obtained from the database in order to
      write the data into a new record.*/
  public int obtainNewRecordID() {
    this.recordID =
      DataBase.nextIDForTable(this.tableName, this.suppTables, this.tableName,
			      this.idCol);
    return this.recordID;
  }

  /** Allow an external routine to set the record ID so that data may
      be read or written.
  @param recordID expressed as an integer.  This integer is stored directly
  in the object.*/
  public void setRecordID(int recordID) {
    this.recordID = recordID;
  }

  /** Allow an external routine to set the record ID so that data may
      be read or written.
  @param recordID expressed as a string for the benefit of
  <CODE>ClassManip</code> which reads record IDs from the database.*/
  public void setRecordID(String recordID) {
    this.recordID = SQLUtilities.integerFromString(recordID);
  }

  /** Allow an external routine to get the record ID for joining with
      other tables.*/
  public int getRecordID() {
    return this.recordID;
  }

  /** Write all data in the form to a new record ID.
   @return 0 if there was an SQL Exception, record ID otherwise.*/
  public int createWriteNewRecord() {
    this.obtainNewRecordID();
    return this.writeEntireRecord(this.recordID);
  }

  /** Write dirty data to the current record ID.
   @return 0 if there was an SQL Exception, record ID otherwise.*/
  public int writeRecord() {
    return this.writeRecord(this.recordID);
  }

  /** Write dirty data to the specified record ID.
   @param recordID identifies the database table row to be updated.
   @return 0 if there was an SQL Exception, record ID otherwise.  */
  public int writeRecord(int recordID) {
    int arg = recordID;
    if (arg == 0) { return createWriteNewRecord(); }
    TwixtTableAndForm.FormToTable(this.fieldMgrs, this.idCol, recordID);
    return arg;
  }

  /** Write all data to the current record ID regardless of dirty
      bits.  This is used to ensure that all data are set to the
      programmatic defaults rather than relying on database defsults
   @return false if there was an SQL Exception, true otherwise.  */
  public int writeEntireRecord() {
    return this.writeEntireRecord(this.recordID);
  }

  /** Write all data to the specified record ID regardless of dirty
      bits.  This is used to ensure that all data are set to the
      programmatic defaults rather than relying on database defsults.
   @param recordID identifies the database table row to be updated.
   @return false if there was an SQL Exception, true otherwise.  */
  public int writeEntireRecord(int recordID) {
    int arg = recordID;
    TwixtTableAndForm.AllFormToTable(this.fieldMgrs, this.idCol, recordID);
    return arg;
  }

  /** Read data from the current record ID.
   @return false if there was an SQL Exception, true otherwise.  */
  public int readRecord() {
    return this.readRecord(this.recordID);
  }

  /** Read data from the specified record ID.  If the record ID is
   zero, that is, a new record is to be created, clears the field
   managers instead of reading.
   @param recordID identifies the database table row to be updated.
   @return record ID which was read or zero if a record was not read.  */
  public int readRecord(int recordID) {
    this.recordID = recordID;
    if (recordID <= 0) {
      this.clearRecord();
      this.recordID = 0;
    } else {
      if (!TwixtTableAndForm.TableToForm(this.fieldMgrs,this.idCol,recordID)) {
	this.recordID = 0;
      }
    }
    return this.recordID;
  }

  /** Return the selector manager array.  This supports callers who
      need to read values from a request object and allows different
      field array classes to define different names for their arrays.
  @return array of selector manager objects which implement the form.  */
  public SelectorManager[] getSelectorManagerArray() {
    return this.fieldMgrs;
  }

  /** Set prefix values for all fields in the object.  The prefix is
   * prepentded to the object name to generate a unique .html field
   * name.  The purpose is to be able to create an array of field
   * arrays to edit or display multiple objects at the same time.
   @param pre prefix to be prepended to each field name.  It is up to
   the caller to ensure that prefixes are unique within one .html
   page.*/
  public void setPrefixes(String pre) {
    int i;
    SelectorManager sel;
    /**/

    this.fieldNamePrefix = pre;	// Remember for posterity

    for(i=0; i<this.fieldMgrs.length; i++) {
      if ( (sel = this.fieldMgrs[i]) != null) {
	sel.setFieldNamePrefix(pre);
      }
    }
  }

  /** Return the current table name.
   @return name of the current table.  */
  public String getTableName() {
    return tableName;
  }

  /** Set the current table name.
   @param tableName identifies the table associated with the object. */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getIdCol() { return idCol; }
  public void setIdCol(String idCol) { this.idCol = idCol; }

  /** Get the number of fields which were used when the field array was
   * instantiated.*/
  public int getFieldsUsed() {
    return this.fieldsUsed;
  }

  /** Generate a table containing the field names and values in the
   * order in which they appear in the array.  The table ends at the first
   * null in the array as a form would end.
   * @param sb buffer into which to put the data, a new one is created
   * if it is null.
   * @return StringBuffer containing the table definition. */

  public StringBuffer makeTableBody(StringBuffer sb) {
    if (sb == null) { sb = new StringBuffer(); }

    for (SelectorManager sm : fieldMgrs) {
      if (sm == null) { break; }
      sb.append("<tr><td>" + sm.getPrettyName() + "</td><td>" +
		sm.getHTMLOnly() + "<td></tr>\n");
    }
    return sb;
  }

  /** A row object ends up being written to or read from the database.
      This integer, if nonzero, stores the record ID which selects the
      appropriate row in the database.*/
  public int recordID;

  /** Array of selector managers to define the fields of the table
      row. Classes which extend this class are responsible for
      allocating an array of the proper size and for populating it
      with field managers whose options are set to match the
      characteristics of the associated columns. */
  protected SelectorManager [] fieldMgrs;

  /** Name of the ID column in the database.  Defaults to ID.  */
  protected String idCol = "ID";

  /** Relate any supplemental tables to the main table, defaults to
      null to indicate that there are no supplemental tables.  */
  protected String[] suppTables = null;

  /** Table name, defaults to null. */
  protected String tableName = null;

  /** Field name prefix which was set for all fields defined in the
   * field mgrs array.*/
  protected String fieldNamePrefix = "";

  /** Number of fields used in the array.*/
  protected int fieldsUsed = 0;

}
