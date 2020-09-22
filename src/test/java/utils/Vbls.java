package utils;

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
import asst.biblerefs.RattleForUTF8;
import asst.common.IntervalPrinter;
import asst.dbase.DataBase;
import asst.dbcommon.PUTs;

public class Vbls {

	public static NumberFormat twoDigInt = DecimalFormat.getIntegerInstance();
	static {
		twoDigInt.setMinimumIntegerDigits(2);
	}

	public IntervalPrinter ip = new IntervalPrinter();
	public IntervalPrinter bip = new IntervalPrinter();
	public BufferedReader in = null;

	public boolean verbose;
    public boolean doIt;
    /** How many files to process, 66 for final run*/
    public int fileLimit;
    /** How many verses to process in each file*/
    public int verseLimit;
	public int bookNo = 0; // 1-66
    public int chapterNo = 0;
    public int verseNo = 0;
    /** Directory containing 66 .htm files.*/
    public String path;
    /** language prefix for database columns*/
    public String lang;
    /** General-purpose query string. */
    public String query;
    /** The current file name. */
    public String fileName;
    /** Book name in the current language*/
    public String bookName;
    /** Book code for the database, GEN, EXO, etc. */
    public String bookCode;
	public StringBuilder oneBook;

    CommandArgs cargs;
	public Statement stmt;
	public ResultSet results;
	public int fileLine;

	/** Create object which contains all the command line aerguments
	 * and information derived from them.
	 * @param cargs Command line parser
	 * @param args command line arguments
	 */
	public Vbls(CommandArgs cargs, String[] args ) {
    	this.cargs = cargs;
		this.cargs.parseArgs(args);
		path = (String)cargs.get("path");
		lang = (String)cargs.get("lang");
		verbose = cargs.getBoolean("verbose");
		doIt = cargs.getBoolean("doIt");
		fileLimit = cargs.getInt("limit");
		verseLimit = cargs.getInt("verseLimit");

		if (PUTs.isStringMTP(path) || PUTs.isStringMTP(lang)) {
			System.out.println("Command line must specify path= and lang=");
			System.exit(-1);
		}
	}

	/** Open the database from command line arguments.
	 * This is not done in the constructor because if it fails,
	 * the JVM complains that the class is not found.
	 */
	public void openDB() {
		try {
			DataBase.openDB((String)cargs.get("driver"), (String)cargs.get("url"),
					(String)cargs.get("user"), (String)cargs.get("pass"));
			stmt  = DataBase.connDB.createStatement();
			RattleForUTF8.rattle("after");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/** Close everything. */
	public void closeDB() {
		try {
			if (results.isClosed()) {
				System.out.println("Result was closed");
			} else {
				results.close();
			}
		} catch (Exception e) {
			System.out.println("Result " + e.getMessage());
		}
		try {
			if (stmt.isClosed()) {
				System.out.println("Stmt was closed");
			} else {
				stmt.close();
			}
		} catch (Exception e) {
			System.out.println("Stmt " + e.getMessage());
		}
		try {
			DataBase.connDB.close();
		} catch (Exception e) {
			System.out.println("Conn " + e.getMessage());
		}
	}

	/** Open a file for reading a book
	 * @param bookNo 1 through 66
	 * @return string builder with entire file in it
	 */
	public StringBuilder openBookFile(int bookNo) {
		bookName = null;
		StringBuilder sb = new StringBuilder();
		String aLine;
		fileName = path + File.separator + twoDigInt.format(bookNo) + ".htm";
		if (verbose) { System.out.println(fileName); }

		try {
			File file = new File(fileName);
			in = new BufferedReader(
					new InputStreamReader( // Spanish Cp1252
							new FileInputStream(file), "UTF-8"));
			while ( (aLine = in.readLine() ) != null) {
				sb.append(aLine + "\n");
			}
			in.close();
			setBookCode(bookNo, sb);
		} catch (Exception e) {
			System.err.println(fileName + " problem, " + e.toString());
			return null;		// Skip the rest of the file
		}
		return sb;
	}

	/** Open the file whose name is passed in on the command
	 * line.  This file should contain the entire Bible	 */
	public void openBibleFile() {
		fileName = path;
		try {
			File file = new File(fileName);
			in = new BufferedReader(
					new InputStreamReader( // Spanish Cp1252
							new FileInputStream(file), "UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException(fileName + " problem, " + e.toString());
		}
	}

	/** Set the 3-character book name based on the book number
	 * @param bookNo 1-66
	 * @param sb accumulated issues
	 * @throws SQLException
	 */
	public void setBookCode(int bookNo, StringBuilder sb) throws SQLException {
		query = "select distinct Code from BookNames where ID=" + bookNo;
		if (verbose) { System.out.println(sb.length() + " " + query); }
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
		verseNo = 0;
		chapterNo = 0;
	}

	/** Print progress message at the end of a book.  */
	public void bookEnd() {
		System.out.println(bookCode + " " + bookName + "\t" + chapterNo + " "
				+ verseNo + " " + bip.howLongSince(true, true));
	}

	public void doUpdate(String query) {
		if (verbose) {
			System.out.println(query);
		}
		if (doIt) {
			try {
				PUTs.anyStatement(query, stmt);
			} catch (SQLException e) {
				System.out.println(query + " " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/** Parse out chapter number and verse number from the string
	 * and return an index to the first character of the text.
	 * @param aLine line of the file
	 * @return index to get all of the text in the line.
	 */
	public int sctChVerse(String aLine) {
		int ix = aLine.indexOf(":");
		if (ix < 0) {
			throw new RuntimeException("File " + fileName);
		}
		chapterNo = Integer.valueOf(aLine.substring(0, ix));
		ix++; // skip colon
		int iy = aLine.indexOf(" ", ix);
		verseNo = Integer.valueOf(aLine.substring(ix, iy));
		return ++iy;
	}

}
