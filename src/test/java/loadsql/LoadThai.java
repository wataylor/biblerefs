/* @name LoadSpanishVerses.java

    Copyright (c) 2003 by Advanced Systems and Software Technologies.
    All Rights Reserved

 */

package loadsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import asst.biblerefs.CommandArgs;
import asst.common.IntervalPrinter;
import asst.dbase.BinaryQuote;
import asst.dbase.DataBase;
import asst.dbcommon.PUTs;

/**
 * Reads one file which is assumed to be in UTF8.  Each line is
 * of the form

 * <book name><space><chapter>.<verse><tab> the verse
 * 
 * Command line parameters
 * <pre><code>
 * lang=Thai
path=F:\BabarBackup\zetektransfer\bible\bibleWSpanish\sp
"limit=67"
-verbose +doIt
</code></pre>

 * @author money
 * @since 2020 07

 */

public class LoadThai {

	/** Database name for SQL version, selects a properties file which
	 * tells how to connect. */
	public static final String DATABASE_NAME = "Perennity";

	static NumberFormat twoDigInt = DecimalFormat.getIntegerInstance();
	static {
		twoDigInt.setMinimumIntegerDigits(2);
	}
	/** Obligatory constructor.*/
	public LoadThai() {
	}

	static Statement stmt;
	static ResultSet results;
	static boolean verbose;
	static boolean doIt;
	static String bookCode = "_";
	static Number chapterNo = -1;
	static Number verseNo = -1;
	static String query = null;
	static String lang;
	static String bookName;

	/**
	 * @param args see default args
	 */
	public static void main (String args[]) {
		IntervalPrinter ip = new IntervalPrinter();
		IntervalPrinter bip = new IntervalPrinter();
		String path;
		String fileName = "";
		BufferedReader in = null;
		String line = null;
		int bookNo = 0;
		int ix;
		int fileLine;
		// int verseLimit; // Max number of verses to examine in a book
		/**/

		CommandArgs cargs = new CommandArgs(new String[] {
				"limit=2",
				"+verbose", "-doIt", "verseLimit=20"
		});
		cargs.parseArgs(args);
		path = (String)cargs.get("path");
		lang = (String)cargs.get("lang");
		verbose = cargs.getBoolean("verbose");
		doIt = cargs.getBoolean("doIt");
		// verseLimit = cargs.getInt("verseLimit");

		if (PUTs.isStringMTP(path) || PUTs.isStringMTP(lang)) {
			System.err.println("Usage: java LoadThai expects to find path= to a file and lang=");
			System.exit(-1); }

		//DBProp.PropertyConnection(DATABASE_NAME);
		if (true) {
			DataBase.openDB((String)cargs.get("driver"), (String)cargs.get("url"), (String)cargs.get("user"), (String)cargs.get("pass"));
			try {
				stmt  = DataBase.connDB.createStatement();
			} catch (SQLException e) {
				System.err.println("Statement create exception " + e.toString());
				System.exit(-1);
			}
		}
		int limit = cargs.getInt("limit");
		{
			bookName = null;
			fileLine = 0;
			try {
				fileName = path;
				try {
					File file = new File(fileName);
					in = new BufferedReader(
							new InputStreamReader(
									new FileInputStream(file), "UTF8"));
				} catch (Exception e) {
					System.err.println(fileName + " problem, " + e.toString());
					System.exit(11);
				}
				while ( (line = in.readLine()) != null) {
					ix = line.indexOf(" 1.1\t");
					if (ix > 0) { // Start of a new book, need book code
						bookNo++;
						bookName = line.substring(0, ix);
						newBook(bookNo);
						System.out.println(bookName + "\t" +bookCode + " "
								+ bip.howLongSince(true, true));
					}
					ix = line.indexOf(" ");
					chapterNo = twoDigInt.parse(line.substring(ix+1));
					ix = line.indexOf(".");
					verseNo = twoDigInt.parse(line.substring(ix+1));
					ix = line.indexOf("\t");
					query = "update Verses set " + lang + "Content = '" + BinaryQuote.binaryQuote(line.substring(ix+1).trim()) + "' where Code='" + bookCode + "' and Chapter=" + chapterNo + " and Verse=" + verseNo;
					if (verbose) { System.out.println(query); }
					if (doIt) { PUTs.anyStatement(query, stmt); }
					if (limit < bookNo) {
						break;
					}
				}
			} catch (Exception e) {
				System.out.println(e.toString() + " " + fileName + " line " + fileLine + " " + line);
				e.printStackTrace();
				System.exit(-1);
			}
		}
		try {
			stmt.close();
			DataBase.connDB.close();
		} catch(Exception e) {
			System.out.println("ERR closing " + e.getMessage());
		}
		System.out.println(ip.howLongSince());
	}

	/** Sets the value of bookCode as a side effect.  The
	 * verses table needs the book code, chapter number, and verse number.
	 * @param bookNo integer 1-66
	 * @throws Exception
	 */
	public static void newBook(int bookNo) throws Exception {
		String query = "select distinct Code from BookNames where ID=" + bookNo;
		if (verbose) { System.out.println(query); }
		results = stmt.executeQuery(query);
		if (results.next()) {
			bookCode = results.getString(1);
		} else {
			throw new RuntimeException("Cannot find book code for " + bookNo);
		}
		results.close();
		query = "update BookNames set " + lang + "Book = '" + bookName + "' where ID=" + bookNo;
		if (verbose) { System.out.println(query); }
		if (doIt) { PUTs.anyStatement(query, stmt); }
		query = "update FormalBookNames set " + lang + "Book = '" + bookName + "' where ID=" + bookNo;
		if (verbose) { System.out.println(query); }
		if (doIt) { PUTs.anyStatement(query, stmt); }
	}
}
