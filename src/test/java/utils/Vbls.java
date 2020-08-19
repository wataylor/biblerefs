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
import asst.dbase.DataBase;
import asst.dbcommon.PUTs;


public class Vbls {

	public static NumberFormat twoDigInt = DecimalFormat.getIntegerInstance();
	static {
		twoDigInt.setMinimumIntegerDigits(2);
	}

	public boolean verbose;
    public boolean doIt;
    /** How many files to process, 66 for final run*/
    public int fileLimit;
    /** How many verses to process in each file*/
    public int verseLimit;
	public int bookNo; // 1-66
    public int chapterNo;
    public int verseNo;
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
			System.out.println("Commandl ine must specify path= and lang=");
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
		BufferedReader in = null;
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

			query = "select distinct Code from BookNames where ID=" + bookNo;
			if (verbose) { System.out.println(sb.length() + " " + query); }
			results = stmt.executeQuery(query);
			if (results.next()) {
				bookCode = results.getString(1);
			} else {
				throw new RuntimeException("Cannot find book code for " + bookNo);
			}
			results.close();

		} catch (Exception e) {
			System.err.println(fileName + " problem, " + e.toString());
			return null;		// Skip the rest of the file
		}
		return sb;
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

}
