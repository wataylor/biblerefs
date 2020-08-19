/* @name DBProp.java

Ensure that the database is connected

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: DBProp.java,v $
    Revision 1.8  2003/01/24 18:43:10  asst
    Commented out need for servlet request

    Revision 1.7  2002/10/15 05:27:40  asst
    set database debug flag on property option

    Revision 1.6  2002/09/20 21:39:52  asst
    better error messages when not find bundle

    Revision 1.5  2002/09/08 02:57:58  asst
    documentation

    Revision 1.4  2002/09/03 23:33:38  asst
    added dbaseProduct property for MSSQL

    Revision 1.3  2002/08/17 16:21:48  asst
    documentation

    Revision 1.2  2002/07/17 15:51:36  zonediet
    documentation

    Revision 1.1.1.1  2002/04/09 03:20:10  zonediet
    first import

*/

package asst.dbase;

import java.lang.RuntimeException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

import asst.dbase.DataBase;

/**
 * Make a data base connection as needed using database connection
 * parameters as specified in a property file.

 * This is a sample properties  file which would be selected by

 <code>DataBase.PropertyConnection("News");</code>

<PRE><CODE>
News=Newsletter Database
News.dbaseURL=jdbc:mysql://&lt;host&gt;/News?user=&lt;uid&gt;&password=&lt;pw&gt;
News.dbaseDriver=org.gjt.mm.mysql.Driver
News.dbaseUser=NONE
News.dbasePassword=NONE
News.dbaseOwner=NONE
News.dbaseSequence=Sequence
News.dbaseRattler=select_ID_from_Recipients_where_ID_=1
News.dbaseProduct=MySQL
News.debug=true
</code></pre>

The database URL format and the name of the JDBC driver vary depending
on the database product.  If the user ID and password are both
specified as "NONE," the software assumes that the user name and
password are included in the URL and opens the database using the
method

<PRE><CODE>
DriverManager.getConnection(url);
</code></pre>

If either the user ID or password are not equal to "NONE," the
database is opened by

<PRE><CODE>
DriverManager.getConnection(url, userID, password);
</code></pre>

Which version to use depends on the database product.</p>

<P>The dbaseProduct parameter specifies which database product is
being used and customizes the various database functions to the
differences between products.  The supported products are:

<UL>
<LI>MySQL</li>
<LI>MSSQLServer</li>
<LI>Oracle</li>
</ul>

The <CODE>dbaseProduct</code> property value must match one of these
strings <B>exactly</b>.</p>

<P>The <CODE>debug</code> property value when set to <CODE>true</code>
invokes additional debug output from the database routines.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.DataBase */

public class DBProp {

  /** Stores the name of the database properties file from which the
   * current connection was established.  */
  public static String whichDB = "";

  /** Obligatory null constructor. */
  public DBProp() {
  }

  /**
   * Test the database connection and establish one if a connection
   * does not exist.  Establishing the conection requires reading a
   * property file <code>&lt;Data base name&gt;.properties</code>;
   * this file msut be on the class path of the JVM.  The data base
   * name is used to select the proper database property file.  The
   * properties file specifies database connection parameters as
   * described above.</p>

   * <P>The property file gives the database URL, driver,
   * password, user, and owner.  The owner is stored in
   * <code>DataBase.dbaseOwner</code> for later use; the data base
   * user is stored in <code>DataBase.dbaseUser</code>.  The database
   * owner and user are taken from the parameter file, they have
   * NOTHING to do with the human being who activated the
   * connection.</p>

   * <P>The connection is tested to ensure that the global variable
   * DataBase.connDB is a connecton to the database specified by the
   * database name and database property file.

   @param whichDB names a properties file which specifies parameters
   which tell how to attach to the database.

  @exception RuntimeException if the database connection cannot be
  established or if the property file cannot be opened. */

  public static synchronized void PropertyConnection(String whichDB)
    throws RuntimeException {

    if (!whichDB.equals(DBProp.whichDB) && (DataBase.connDB != null)) {
      DataBase.closeDB();	// Nulls the connection if it fails.
      DBProp.whichDB = whichDB;	// Remember which database opened
    }

    if (DataBase.connDB != null) { // Database is open, test connection
      DataBase.rattleDB();	// Nulls the connection if it fails.
    }

    if (DataBase.connDB == null) { // Not connected, establish a connection
      ResourceBundle bundle = null;
      String Driver = "";
      String DBUrl = "";
      String Username = "";
      String Password = "";
      String product;
      RuntimeException re;
      Enumeration en;

      try {
	bundle = ResourceBundle.getBundle(whichDB);
//  	{
//
//  	  String name;
//  	  /**/

//  	  e = bundle.getKeys();
//  	  System.out.println(bundle.toString() + " " + e.toString());
//  	  while (e.hasMoreElements()) {
//  	    name = (String)e.nextElement();
//  	    System.out.println(name + " " + bundle.getString(name));
//  	  }
//  	}

	Driver   = bundle.getString(whichDB + ".dbaseDriver");
	DBUrl    = bundle.getString(whichDB + ".dbaseURL");
	Username = bundle.getString(whichDB + ".dbaseUser");
	Password = bundle.getString(whichDB + ".dbasePassword");
	DataBase.dbaseOwner    = bundle.getString(whichDB + ".dbaseOwner");
	DataBase.dbaseSequence = bundle.getString(whichDB + ".dbaseSequence");
	DataBase.dbaseRattler  = bundle.getString(whichDB + ".dbaseRattler");
	if (DataBase.dbaseRattler != null) {
	  DataBase.dbaseRattler = DataBase.dbaseRattler.replace('_', ' ');
	}

	try {
	  String dbg = bundle.getString(whichDB + ".debug");
	  if ("true".equals(dbg)) { DataBase.dbaseDebug = true; }
	} catch (Exception e) {}

	try {
	  product = bundle.getString(whichDB + ".dbaseProduct");
	  if (product.equals("MySQL")) { // Do nothing, MySQL is the default
	  } else if (product.equals("MSSQLServer")) {
	    DataBase.dbaseGetNextValue = "SELECT @@identity";
	  } else if (product.equals("Oracle")) { // Oracle uses sequences
	  } else {
	    String msg;

	    msg = "Unrecognized database product " + product +
	      "use MySQL, MSSQLServer, or Oracle";
	    RuntimeException e = new RuntimeException(msg);
	    throw e;
	  }
	} catch (Exception e) {}
      } catch (MissingResourceException mre) {
	String msg;

	msg = "Error with resource file " +
	  whichDB + ".properties " + mre.toString();
	if (bundle != null) {
	  msg += " D " + Driver + " Ur " + DBUrl +
	    " Un " + Username + " P " + Password +
	    " O " + DataBase.dbaseOwner +
	    " S " + DataBase.dbaseSequence +
	    " R " + DataBase.dbaseRattler +
	    " Keys ";
	  en = bundle.getKeys();
	  while (en.hasMoreElements()) {
	    msg = msg + " " + (String)en.nextElement();
	  }
	} else {
	  msg = msg + ".  Null resource bundle, file was not found";
	}
	RuntimeException e = new RuntimeException(msg);
	throw e;
      }

      DataBase.openDB(Driver, DBUrl, Username, Password);

      if (DataBase.connDB == null) {
	String errMsg = "Database not connected: Driver " + Driver +
	  " URL " + DBUrl +
	  " User " + Username + " Password " + Password +
	  " Owner " + DataBase.dbaseOwner;
	RuntimeException e = new RuntimeException(errMsg);
	throw e;
      }
    }
  }

//   /** Entry point for .jsp applications whose database name is passed
//       via a form parameter.  This permits the application to access
//       different databases without changing the .jsp source code.
//   @param request HTTP request which specifies the database properties
//   file name as the value of the <code>whichDB</code> request
//   parameter.
//   @exception RuntimeException if the database connection cannot be
//   established or if the property file cannot be opened. */
//   public static void PropertyConnection (HttpServletRequest request)
//     throws RuntimeException {
//     String whichDB;
//     /**/

//     if  ( (whichDB = request.getParameter("whichDB")) == null) {
//       whichDB = "";
//     }
//     DBProp.PropertyConnection(whichDB);
//   }
}
