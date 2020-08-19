/* @name LoadLanguageVerses.java

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
 * Reads all the files in a folder xx.htm assuming a specific language

* Command line parameters to process Spanish
* <pre><code>
lang=Spanish
path=F:\BabarBackup\zetektransfer\bible\bibleWSpanish\sp
"limit=66"  "verseLimit=999999"
-verbose +doIt
</code></pre>
* Note a tab after the verse number and that overflow lines are tabbed in.
<pre>
Génesis 1 

1.	EN el principio crió Dios los cielos y la tierra. 

2.	Y la tierra estaba desordenada y vacía, y las tinieblas estaban sobre la haz del 
	abismo, y el Espíritu de Dios se movía sobre la haz de las aguas. 
</pre>
 * @author money
 * @since 2020 07

 */

public class LoadLanguageVerses {

	/** Database name for SQL version, selects a properties file which
	 * tells how to connect. */
	public static final String DATABASE_NAME = "Perennity";

	static NumberFormat twoDigInt = DecimalFormat.getIntegerInstance();
	static {
		twoDigInt.setMinimumIntegerDigits(2);
	}
	/** Obligatory constructor.*/
	public LoadLanguageVerses() {
	}

	static Statement stmt;
	static ResultSet results;
	static boolean verbose;
	static boolean doIt;
	static String bookCode = "_";
	static Number chapterNo = -1;
	static int verseNo = -1;
	static String query = null;
	static StringBuilder verse = new StringBuilder();		// Verse reference in a line from a book
	static String lang;;
	static String bookName;

	/**
	 * @param args see default args
	 */
	public static void main (String args[]) {
		IntervalPrinter ip = new IntervalPrinter();
		IntervalPrinter bip = new IntervalPrinter();
		String path;
		String fileName = "";
		BufferedReader in;
		String line = null;
		int bookNo;
		int ix;
		int fileLine;
		int verseLimit; // Max number of verses to examine in a book
		char ch;
		/**/

		CommandArgs cargs = new CommandArgs(new String[] {
				"limit=3",
				"+verbose", "-doIt", "verseLimit=9999"
		});
		cargs.parseArgs(args);
		path = (String)cargs.get("path");
		lang = (String)cargs.get("lang");
		verbose = cargs.getBoolean("verbose");
		doIt = cargs.getBoolean("doIt");
		verseLimit = cargs.getInt("verseLimit");

		if (PUTs.isStringMTP(path) || PUTs.isStringMTP(lang)) {
			System.err.println("Usage: java LoadLanguageVerses expects to find path= to files 01.htm through 66.htm and lang=");
			System.exit(-1); }

		//DBProp.PropertyConnection(DATABASE_NAME);
		DataBase.openDB((String)cargs.get("driver"), (String)cargs.get("url"), (String)cargs.get("user"), (String)cargs.get("pass"));
		try {
			//       System.out.println(DataBase.identifyDB());
			//       tables = DataBase.listDBTables();
			//       for (i=0; i<tables.length; i++) {
			// 	System.out.println(tables[i]);
			//       }
			//       tables = null;
			stmt  = DataBase.connDB.createStatement();
		} catch (SQLException e) {
			System.err.println("Statement create exception " + e.toString());
			System.exit(-1);
		}

		int limit = cargs.getInt("limit"); // maximum book number 66 for all
		for (bookNo = 1; bookNo <= limit; bookNo++) {
			bookName = null;
			fileLine = 0;
			try {
				fileName = path + File.separator + twoDigInt.format(bookNo) + ".htm";
				try {
					File file = new File(fileName);
					in = new BufferedReader(
							new InputStreamReader(
									new FileInputStream(file), "Cp1252"));
				} catch (Exception e) {
					System.err.println(fileName + " problem, " + e.toString());
					continue;		// Skip the rest of the file
				}
				do {
					line = in.readLine();
					fileLine++;
				} while (!line.startsWith("<pre>"));
				line = in.readLine();
				verse.setLength(0);
				do {
					fileLine++;
					if (PUTs.isStringMTP(line)) {
						recordAccumulatedVerse();
						continue;
					}
					ch = line.charAt(0);
					if (ch == '<') { continue; }
					if (ch == '\t') {
						verse.append(" " + line.substring(1));
						continue;
					}
					line = line.trim();
					if (Character.isAlphabetic(ch)
							|| (Character.isDigit(ch)
									&& (Character.isSpaceChar(line.charAt(1))))) {
						/* The previous verse might not have been written if
						 * there is no blank line before this verse.  */
						recordAccumulatedVerse();
						ix = line.lastIndexOf(" ");
						String aName = line.substring(0, ix);
						chapterNo = twoDigInt.parse(line.substring(ix + 1));
						if (PUTs.isStringMTP(bookName)) {
							bookName = aName;
							query = "select distinct Code from BookNames where ID=" + bookNo;
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
						} else {
							if (!aName.equals(bookName)) {
								System.out.println("ERR " + bookNo + " " + bookName);
							}
						}
					} else {
						ix = line.indexOf(".");
						/* Starting a new verse write out any pending verse*/
						recordAccumulatedVerse();
						verseNo = Integer.valueOf(line.substring(0, ix));
						verse.setLength(0);
						verse.append(line.substring(ix+2));
					}
				} while ((fileLine < verseLimit) && (line = in.readLine()) != null);
				recordAccumulatedVerse();
				System.out.println(bookName + "\t" + chapterNo + " " + verseNo + " " + bip.howLongSince(true, true));
			} catch (Exception e) {
				System.out.println(e.toString() + " " + fileName + " line " + fileLine + " " + line);
				e.printStackTrace();
				continue;		// Skip the rest of the file
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

	/** Write out any verse which has accumulated in the verse string*/
	private static void recordAccumulatedVerse() throws Exception {
		if (verse.length() > 0) {
			if (verbose) { System.out.println(bookName + " " + chapterNo + " " + verseNo + " " + verse.toString()); }
			query = "update Verses set " + lang + "Content = '" + BinaryQuote.binaryQuote(verse.toString()) + "' where Code='" + bookCode + "' and Chapter=" + chapterNo + " and Verse=" + verseNo;
			if (verbose) { System.out.println(query); }
			if (doIt) { PUTs.anyStatement(query, stmt); }
		}
		verse.setLength(0);
	}
}
