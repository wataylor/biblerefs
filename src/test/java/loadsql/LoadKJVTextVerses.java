/* @name LoadVerses.java

    Copyright (c) 2003 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: LoadVerses.java,v $
    Revision 1.3  2008/09/26 00:52:57  peren
    backward tweak

    Revision 1.2  2008/09/26 00:18:48  peren
    Switch to perennity database

    Revision 1.1  2008/09/13 23:29:21  peren
    for .war file, add bible cref Java

*/

package loadsql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import asst.dbase.DBProp;
import asst.dbase.DataBase;
import asst.dbase.LineInputStream;
import asst.dbase.Quotable;

/**
 * Reads all the files passed in as `ls -1 <path>/*.txt`

 * GEN 1:1  In the beginning God created the heaven and the earth.

 * @author money
 * @version %I%, %G%
 * @since 2003 01

 */

public class LoadKJVTextVerses {

  /** Database name for SQL version, selects a properties file which
   * tells how to connect. */
  public static final String DATABASE_NAME = "Perennity";

  /** Obligatory constructor.*/
  public LoadKJVTextVerses() {
  }

  static int bookNo = 0;
  static String oldChapNo;
  static String chapNo;
  static int verseNo= 1;	// Next one to go in
  static int lineNo = 0;
  static Statement stmt;

  public static void main (String args[]) {
    LineInputStream lineStream;
    // String[] tables;
    String bookAbbrev;
    String fileName;
    // String fileNameRoot;
    String line;
    String query = null;
    String ref;
    String vers;		// Verse reference in a line from a book
    int argno;
    int ix;
    int ix2;
    int ix3;
    /**/

    if (args.length < 1) {
      System.err.println("Usage: java LoadVerses <space-separated list of input files>");
      System.exit(-1);
    }

    DBProp.PropertyConnection(DATABASE_NAME);
    try {
//       System.out.println(DataBase.identifyDB());
//       tables = DataBase.listDBTables();
//       for (i=0; i<tables.length; i++) {
// 	System.out.println(tables[i]);
//       }
//       tables = null;
      stmt  = DataBase.connDB.createStatement();
      stmt.execute("delete from BookChapters");
      //stmt.execute("delete from BookNames");
      stmt.execute("delete from Verses");
    } catch (SQLException e) {
      System.err.println("Statement create exception " + e.toString());
      System.exit(-1);
    }

    for (argno = 0; argno < args.length; argno++) {
      fileName = args[argno];
      File file = new File(fileName);
      try {
	lineStream = new LineInputStream(file);
	lineNo = 0;
	bookNo++;

      } catch (FileNotFoundException e) {
	System.err.println(fileName + " is not found, " + e.toString());
	continue;		// Skip the rest of the file
      }
      try {
	lineNo++;
	line = lineStream.readLine();
	ix = line.indexOf(" ");
	bookAbbrev = line.substring(0, ix);
	query = "insert into BookNames (ID,Code) values(" + bookNo +
	  ",'" + bookAbbrev + "')";
	//stmt.execute(query);
	System.out.println("Skip " + query + " on verse " + verseNo);

	oldChapNo = ""; // Current chapter in the current book
	do {
	  ix = line.indexOf(" ");
	  bookAbbrev = line.substring(0, ix);
	  ix++;
	  ix2 = line.indexOf(" ", ix);
	  ref = line.substring(ix, ix2);
	  ix3 = ref.indexOf(":");
	  chapNo = ref.substring(0, ix3);
	  vers = ref.substring(ix3+1, ref.length());
	  //System.out.println(ix + " " + ix2 + " " + ix3 + " " + ref + " " +
	  //		     chapNo + " " + vers);
	  if (!chapNo.equals(oldChapNo)) {
	    query="insert into BookChapters (BookID,Chapter,VerseID) values ("+
	      bookNo + "," + chapNo + "," + verseNo + ")";
	    stmt.execute(query);
	    oldChapNo = chapNo;
	  }
	  for (ix3 = ix2; ix3<line.length(); ix3++) {
	    if (!Character.isSpaceChar(line.charAt(ix3))) {
	      break;
	    }
	  }
	  line = line.substring(ix3, line.length());
	  query = "insert into Verses (ID,Code,Chapter,Verse,Content) values (" + verseNo + ",'" + bookAbbrev + "'," + chapNo + "," + vers + "," +
	    Quotable.ConstructSQLCharacterValue(line) + ")";
	  stmt.execute(query);
	  //System.out.println(query);
	  verseNo++;		// Count verses in the table
	  lineNo++;		// Count lines in the file
	} while ( (line = lineStream.readLine()) != null);
      } catch (IOException e) {
	System.out.println(e.toString() + " " + fileName + " " + lineNo);
	continue;		// Skip the rest of the file
      } catch (SQLException e) {
	System.err.println("insert exception " + query + " " + e.toString());
	continue;
      }
    }
  }
}
