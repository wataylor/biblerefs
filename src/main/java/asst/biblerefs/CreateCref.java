/* @name CreateCref.java

    Copyright (c) 2008 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: CreateCref.java,v $
    Revision 1.1  2017/04/22 01:50:25  peren
    safety

*/

package asst.biblerefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import asst.dbase.DataBase;
import asst.dbase.SQLUtilities;

/**
 * Load the cross-reference table and create the cref table.

 * @author Material Gain
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class CreateCref {

  public static final long serialVersionUID = 1;

  static Statement stmt;

  /** Obligatory constructor.*/
  public CreateCref() { /* */ }

  /** Main program to create the SQL verse cross-reference table.  */
  public static void main(String[] args) {
    RefSQLLookup rsl = new RefSQLLookup();
    CommandArgs ca = new CommandArgs(args);
    String[] s;
    String aLine = "";
    String oLine = ""; // original line
    String query = "delete from VerseCref";
    Vector<String> vec;
    int i = 0;
    int lineno = 0;
    int fromNO;
    int toNO;
    int[] numbers;
    /**/

    if (args.length <= 0) {
      System.out.println("Please specify the cross-reference file or +writeRefs to get a printout of the cross references");
      System.exit(-1);
    }

    try {
      rsl.setup(); 		// Establishes database connection
      stmt = DataBase.connDB.createStatement();

      if (ca.getBoolean("writeRefs")) {
	query = "select fb.Book as 'ToBook', c.FromChapter, c.FromVerse, tb.Book as 'ToBook', c.ToChapter, c.ToVerse from VerseCref c left join FormalBookNames fb on (c.FromBookID = fb.ID) left join FormalBookNames tb on (c.ToBookID = tb.ID) order by FromVerseID";

	ResultSet results = null;	// Result of the retrieval
	Statement getRow  = null;	// Retrieve the mapping row
	try {

	  getRow  = DataBase.connDB.createStatement();
	  //System.out.println(query);
	  results = getRow.executeQuery(query);
	  while (results.next()) {
	    System.out.println(results.getString(1) + " " +
			       results.getString(2) + ":" +
			       results.getString(3) + " -> " +
			       results.getString(4) + " " +
			       results.getString(5) + ":" +
			       results.getString(6));
	  }
	} catch (SQLException e) {
	  System.out.println("ERR " + query + " " + e.toString());
	} finally {
	  try {
	    if (results != null) { results.close(); }
	    if (getRow  != null) { getRow.close(); }
	  } catch (SQLException e) { }
	}
	System.exit(0);
      }

      stmt.execute(query);

      File file = new File(args[0]);
      BufferedReader br = new BufferedReader(new FileReader(file));

      while ( (aLine = br.readLine()) != null) {
	lineno++;
	oLine = new String(aLine); // Make a copy
	aLine = aLine.replace(" -- ", ",").replace(" ", "");
	// System.out.println(aLine);
	s = aLine.split(",");
	if (s.length != 6) {
	  System.out.println(lineno + " odd line " + aLine);
	  continue;
	}
	numbers = new int[6];
	for (i=0; i<s.length; i++) {
	  numbers[i] = Integer.parseInt(s[i]);
	}
	// Book numbers jump from 39 to 101, so 101 should be 40
	if (numbers[0] > 39) { numbers[0] = numbers[0] - 61; }
	if (numbers[3] > 39) { numbers[3] = numbers[3] - 61; }

	query = "select VerseID from BookChapters where BookID=" +
	  numbers[0] + " and Chapter=" + numbers[1];
	vec = SQLUtilities.VectorizeQuery(query);
	if (vec.size() > 0) {
	  fromNO = Integer.parseInt(vec.elementAt(0));
	  fromNO += numbers[2]-1;
	} else {
	  System.out.println(lineno + "\t" + oLine + "\tfrom verse not found.");
	  fromNO = 0;
	}

	query = "select VerseID from BookChapters where BookID=" +
	  numbers[3] + " and Chapter=" + numbers[4];
	vec = SQLUtilities.VectorizeQuery(query);
	if (vec.size() > 0) {
	  toNO = Integer.parseInt(vec.elementAt(0));
	  toNO += numbers[5]-1;
	} else {
	  System.out.println(lineno + "\t" + oLine + "\tto verse not found.");
	  toNO = 0;
	  fromNO = 0;
	}

	if ((fromNO > 0) && (toNO > 0)) {
	  query = "insert into VerseCref (FromBookID,FromChapter,FromVerse,FromVerseID,ToBookID,ToChapter,ToVerse,ToVerseID) values (" + numbers[0] + "," + numbers[1] + "," +
	    numbers[2] + "," + fromNO + "," + numbers[3] + "," + numbers[4] + "," +
	    numbers[5] + "," + toNO +  ")";
	  SQLUtilities.AnyStatement(query);
	}
	// if (lineno > 9) { System.exit(0); }
      }

    } catch (Exception e) {
      System.out.println(lineno + " " + oLine + " ERR " + query + " " + e.toString());
      e.printStackTrace();
    }
  }
}
