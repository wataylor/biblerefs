package asst.biblerefs;

import java.sql.ResultSet;
import java.sql.Statement;

import asst.dbase.DataBase;

/** Try to read from a UTF database.
 * @author Material gain
 * @since 2020 08
 */
public class RattleForUTF8 {
	/** read one row of Thai and see if it works as UTF8
	 * @param where
	 * @throws Exception
	 */
	public static void rattle(String where) throws Exception {
		String val;
		Statement stmt;
		ResultSet results;

		String query = "select SpanishContent, ThaiContent from Verses where ID=1";
		stmt = DataBase.connDB.createStatement();
		results = stmt.executeQuery(query);
		if (results.next()) {
			val = results.getString(1) + " " + results.getString(2);
		} else {
			val = "none";
		}
		results.close();
		stmt.close();
		System.out.println(where + " " + val);
	}
}
