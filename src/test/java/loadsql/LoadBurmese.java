package loadsql;

import asst.biblerefs.CommandArgs;
import asst.dbase.BinaryQuote;
import asst.dbcommon.PUTs;
import utils.Vbls;

/** read a Burmese bible, may apply to other languages
 * @author wat
 * @since 2020 09
 */
public class LoadBurmese {

	/** Default command line arguments used for most
	 * similar programs.
	 */
	public static String[] DEFAULT_ARGS =  {
			"limit=3",
			"+verbose", "-doIt", "verseLimit=3"
	};

	/** All the command line arguments being passed along. */
	public static Vbls vbls;

	/** Read a Bible as one file
	 * @param args
	 */
	public static void main(String[] args) {
		String aLine = null;
		StringBuilder sb = new StringBuilder();
		String query;
		int ix;
		CommandArgs cargs = new CommandArgs(DEFAULT_ARGS);
		vbls = new Vbls(cargs, args);
		vbls.openDB();
		vbls.openBibleFile();

		try {
			while (vbls.bookNo < vbls.fileLimit) {
				vbls.fileLine++;
				aLine = vbls.in.readLine();
				if (aLine == null) { break; }
				if (PUTs.isStringMTP(aLine)) { continue; }
				/* Uses numbers 1, 2, or 3 for Peter letters */
				if (Character.isDigit(aLine.charAt(0)) && !Character.isWhitespace(aLine.charAt(1))) {
					/* Lines are <chapter>:<verse><space>verse*/
					ix = vbls.sctChVerse(aLine);
					query = "update Verses set " + vbls.lang + "Content = '"
					+ BinaryQuote.binaryQuote(aLine.substring(ix).trim())
					+ "' where Code='" + vbls.bookCode
					+ "' and Chapter=" + vbls.chapterNo
					+ " and Verse=" + vbls.verseNo;
					vbls.doUpdate(query);
				} else {
					/* The name of a book */
					if (vbls.bookNo > 0) {
						vbls.bookEnd();
					}
					vbls.bookNo++;
					vbls.bookName = aLine.trim();
					vbls.setBookCode(vbls.bookNo, sb);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString() + " " + vbls.fileName + " line "
					+ vbls.fileLine + " " + aLine);
			e.printStackTrace();
			System.exit(-1);
		} finally {
			vbls.closeDB();
		}
	}

}
