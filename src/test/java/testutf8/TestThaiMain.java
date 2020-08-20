package testutf8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import asst.biblerefs.CommandArgs;
import asst.biblerefs.RattleForUTF8;
import asst.dbase.DataBase;

/** experimenting with reading UTF-8 from the database
 * Do not need to add ?useUnicode=true&characterEncoding=UTF-8
 * to the database URL.
 * @author Money
 * @since 2020 07
 */
public class TestThaiMain {

	static Statement stmt;
	static ResultSet results;

	public static void main(String[] args) {
		String query;
		String lang;
		String path;
		String line;
		int limit;
		int lineNo;
		boolean verbose;
		boolean doIt;
		BufferedReader in = null;
		CommandArgs cargs = new CommandArgs(new String[] {
				"limit=2",
				"+verbose", "-doIt", "verseLimit=20"
		});
		cargs.parseArgs(args);
		path = (String)cargs.get("path");
		lang = (String)cargs.get("lang");
		verbose = cargs.getBoolean("verbose");
		doIt = cargs.getBoolean("doIt");
		limit = cargs.getInt("limit");

		try {
			DataBase.openDB((String)cargs.get("driver"), (String)cargs.get("url"), (String)cargs.get("user"), (String)cargs.get("pass"));
			RattleForUTF8.rattle("after");
			try {
				stmt  = DataBase.connDB.createStatement();
			} catch (SQLException e) {
				System.err.println("Statement create exception " + e.toString());
				System.exit(-1);
			}
			try {
				File file = new File(path);
				in = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(file), "UTF8"));
			} catch (Exception e) {
				System.err.println("Filename " + path + " problem, " + e.toString());
				System.exit(-1);
			}
			for (lineNo=1; lineNo<limit; lineNo++ ) {
				line = in.readLine();
				if (line == null) { break; }
				System.out.print(lineNo + "\t");
				int ix = line.indexOf("\t");
				for (int j = ix+1; j < ix+6; j++) {
					Character ch = line.charAt(j);
					System.out.print(String.format("%02X", (int) ch) + " ");
				}
				System.out.println(" " + line);
				query = "select ThaiContent from Verses where ID=" + lineNo;
				results = stmt.executeQuery(query);
				if (results.next()) {
					line = results.getString(1);
					System.out.print(lineNo + "\t");
					for (int j = 0; j < 5; j++) {
						Character ch = line.charAt(j);
						System.out.print(String.format("%02X", (int) ch) + " ");
					}
					System.out.println(" " + line);
				}
				results.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				DataBase.connDB.close();
			} catch(Exception e) {
				System.out.println("ERR closing " + e.getMessage());
			}
		}
	}

}
