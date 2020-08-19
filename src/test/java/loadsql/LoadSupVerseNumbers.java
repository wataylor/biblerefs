package loadsql;

import java.text.ParseException;

import asst.biblerefs.CommandArgs;
import asst.common.IntervalPrinter;
import asst.dbase.BinaryQuote;
import utils.Vbls;

/**
 * @author money
 * @since 2020 07

 * Arguments to load German
<pre><code>
lang=German
path=F:\BabarBackup\bibletools\GermanProductShipped\GermanProduct\de
</code></pre>
 *
 */
public class LoadSupVerseNumbers {

	/** Default command line arguments used for most
	 * similar programs.
	 */
	public static String[] DEFAULT_ARGS =  {
			"limit=3",
			"+verbose", "-doIt", "verseLimit=3"
	};

	/** All the command line arguments being passed along. */
	public static Vbls vbls;

	/**
	 * @param args see default string and class comments
	 */
	public static void main(String[] args) {
		IntervalPrinter ip = new IntervalPrinter();
		IntervalPrinter bip = new IntervalPrinter();
		int ixH4;
		int ixP;
		int ixProcessed = 0;
		CommandArgs cargs = new CommandArgs(DEFAULT_ARGS);
		vbls = new Vbls(cargs, args);

		try {
			vbls.openDB();
			for (vbls.bookNo = 1; vbls.bookNo <= vbls.fileLimit; vbls.bookNo++) {
				
				/* Reads the file into the oneBook string and sets the
				 * bookCode to index the verse in the database. */
				if (( (vbls.oneBook = vbls.openBookFile(vbls.bookNo)) == null)
						|| (vbls.oneBook.length() <= 0)) {
					continue; // skip the book file
				}
				ixProcessed = 0;  // scan from beginning of the chapter
				/* <h4> is book name <space> chapter number.  Book name may
				 * contain spaces.  <p> is one verse with verse number
				 * as a superscript.*/
				do {
					ixH4 = vbls.oneBook.indexOf("<h4>", ixProcessed);
					ixP = vbls.oneBook.indexOf("<p>", ixProcessed);
					if ((ixH4 >= 0) && (ixH4 < ixP)) {
						ixProcessed = doH4(ixH4);
					} else if (ixP >= 0) {
						ixProcessed = doP(ixP);
					}
				} while (((ixH4 >= 0) || (ixP >= 0)
						&& (vbls.verseNo <= vbls.verseLimit)));
				System.out.println(vbls.bookName + "\t" + vbls.chapterNo + " "
						+ vbls.verseNo + " " + bip.howLongSince(true, true));
				vbls.verseNo = 0;
				vbls.chapterNo = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			vbls.closeDB();
		}
		System.out.println(ip.howLongSince());
	}

	private static int doP(int ixP) {
		int ix = vbls.oneBook.indexOf("<p />", ixP);
		int ixsupE = vbls.oneBook.indexOf("</sup>", ixP);
		int ixOfSup =  vbls.oneBook.indexOf(">", ixP+8);
		String verseText = vbls.oneBook.substring(ixsupE+6, ix);
		try {
			vbls.verseNo = Vbls.twoDigInt.parse(vbls.oneBook.substring(ixOfSup+1, ixsupE)).intValue();
		} catch (ParseException e) {
			throw new RuntimeException(ixOfSup + " "
					+ verseText + " " + e.getMessage());
		}
		vbls.doUpdate("update Verses set " + vbls.lang + "Content = '" + BinaryQuote.binaryQuote(verseText) + "' where Code='" + vbls.bookCode + "' and Chapter=" + vbls.chapterNo + " and Verse=" + vbls.verseNo);
		return ix;
	}

	private static int doH4(int ixH4) {
		int ix = vbls.oneBook.indexOf("</h4>", ixH4);
		String nameChapNo = vbls.oneBook.substring(ixH4+4, ix);
		int ix2 = nameChapNo.lastIndexOf(" ");
		try {
			vbls.chapterNo = Vbls.twoDigInt.parse(nameChapNo.substring(ix2+1)).intValue();
		} catch (ParseException e) {
			throw new RuntimeException(nameChapNo + " " + e.getMessage());
		}
		if (vbls.chapterNo == 1) {
			vbls.bookName = nameChapNo.substring(0, ix2);
			vbls.doUpdate("update BookNames set " + vbls.lang + "Book = '" + vbls.bookName + "' where ID=" + vbls.bookNo);;
			vbls.doUpdate("update FormalBookNames set " + vbls.lang + "Book = '" + vbls.bookName + "' where ID=" + vbls.bookNo);;
		}
		return ix;
	}

}
