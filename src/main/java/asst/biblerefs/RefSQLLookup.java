/* @name RefSQLLookup.java

Look up a scripture reference and return the verse or verses from a
SQL database.

java asst.biblerefs.RefSQLLookup "John 3:16" "Genesis 3:16" "Jb. 1:1" "Mi. 1:1" "Rev. 1:1" "Song 1:2" "Jude 3-5"

    Copyright (c) 2003 by Advanced Systems and Software Technologies.
    All Rights Reserved

*/

package asst.biblerefs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import asst.dbase.DBProp;
import asst.dbase.DataBase;
import asst.dbase.SQLUtilities;
import asst.dbcommon.PUTs;
import loadsql.LoadKJVTextVerses;

/**
 * Define a way to look up scripture references based on storing
 * scripture in a SQL database.  This class implements a lookup
 * interface.  It is designed to be instantiated by class name and
 * then used.</p>

 * <P>This class requires that the database connection be established
 * by calling its setup() method..

 * @author money
 * @version %I%, %G%
 * @since 2003 01
 *
 */

public class RefSQLLookup extends RefLookup {

  /** Obligatory constructor.*/
  public RefSQLLookup() {
  }

  /** Set up the database.  This is the SQL-specific initialization */
  public void setup() {
    DBProp.PropertyConnection(LoadKJVTextVerses.DATABASE_NAME);
    try {
		PUTs.anyStatement("SET CHARACTER SET 'utf8'", DataBase.connDB);
	} catch (Exception e) {
		throw new RuntimeException(e.getMessage());
	}
  }

  /** Shut down the database.  This is the SQL-specific shutdown */
  public void shutDown() {
    DataBase.closeDB();
  }

  /** Convert a book number to its formal name
      @param bookNumber book number 1-66*/
  public String findFormalBookName(int bookNumber, String lang) {
    Vector<String> vec;
    /**/

    vec = SQLUtilities.VectorizeQuery("select " + lang + "Book from FormalBookNames where ID=" + bookNumber);
    try {
      return (String)vec.elementAt(0);
    } catch (Exception e) {
    }
    return null;
  }

  /** Look up a book number.
      @param bookName a full book name or abbreviation.*/
  public int findBookNumber(String bookName) {
    Integer bno;
    Vector<String> vec;
    int val;
    /**/

    if ( (bno = (Integer) bookNameToNumber.get(bookName)) != null) {
      return bno.intValue();
    }

    vec = SQLUtilities.VectorizeQuery("select ID from BookNames where Book='" +
				      bookName + "'  or Abbreviation='" +
				      bookName + "'  or Book='" +
				      bookName + ".' or Abbreviation='" +
				      bookName + ".'");
    if (vec.size() <= 0) {
      return 0;
    }
    val = Integer.parseInt((String)vec.elementAt(0));
    bookNameToNumber.put(bookName, new Integer(val));
    return val;
  }

  /* select * from BookChapters where VerseID = select MAX(VerseID) from BookChapters where VerseID <= 500;*/
  public static String refFromVerseID(int verseID) {
    Vector<String> vec = SQLUtilities.VectorizeQuery("select MAX(VerseID) from BookChapters where VerseID<=" + verseID);
    if (vec.size() <= 0) { return ""; }
    int fVID = Integer.parseInt(vec.elementAt(0)); // First verse in chapter
    vec = SQLUtilities.VectorizeQuery("select f.Book,bc.Chapter from BookChapters bc left join FormalBookNames f on (f.ID = bc.BookID) where bc.VerseID=" + vec.elementAt(0));
    if (vec.size() <= 0) { return ""; }
    String[] s = vec.elementAt(0).split("\\|");
    return s[0] + " " + s[1] + ":" + (verseID - fVID + 1);
  }

/* select c.ID, fb.Book, c.ToChapter, c.ToVerse, c.ToVerseID, v.Content,fb2.book,c.FromChapter,c.FromVerse from VerseCref c left join FormalBookNames fb on (c.ToBookID = fb.ID) left join Verses v on (v.ID=c.ToVerseID) left join FormalBookNames fb2 on (c.FromBookID = fb2.ID) where FromVerseID=1; */

  /** Append the cross references as .html text */
  public boolean addCrefHTML(StringBuilder sb, int verseID) {
	return addCrefHTML(sb, verseID, "");
}

/** Append the cross references as .html text 
 * @param lang TODO*/
  public boolean addCrefHTML(StringBuilder sb, int verseID, String lang) {
    ResultSet results = null;	// Result of the retrieval
    Statement getRow = null;	// Retrieve the mapping row
    String query = "select c.ID, c.ToVerseID, fb.Book, c.ToChapter, c.ToVerse, v." + lang + "Content, fb2.book, c.FromChapter, c.FromVerse from VerseCref c left join FormalBookNames fb on (c.ToBookID = fb.ID) left join Verses v on (v.ID=c.ToVerseID) left join FormalBookNames fb2 on (c.FromBookID = fb2.ID) where FromVerseID=" + verseID;
    String click;
    String post;

    try {
      getRow  = DataBase.connDB.createStatement();
      //System.out.println(query);
      results = getRow.executeQuery(query);
      if (results.next()) {
	post = "<img src=\"images/plus.gif\" cursor=\"hand\" onClick='plusMe(this,\"" +
	  results.getString(7) + " " + results.getString(8) + ":" +
	  results.getString(9) + "\")'>\n";
	sb.append(" " + post + " ");
	do {
	  query = "<img src=\"images/minus.gif\" cursor=\"hand\" onClick='minusMe(this," +
	    results.getString(1) + ")'>";
	  click = "onClick='hitSpan(this," + results.getString(1) + "," +
	    results.getString(2) + ")'";
	  sb.append("<span class=\"cursorHand refSpan\" title=\"" +
		    results.getString(6) + "\" ><span " + click +">" +
		    results.getString(3) + " " + results.getString(4) + ":" +
		    results.getString(5) + "</span>" + query + " </span>\n");
	} while (results.next());
	sb.append(post);
	return true;
      }
      post = "<img src=\"images/plus.gif\" cursor=\"hand\" onClick='plusMe(this,\"" +
      refFromVerseID(verseID) + "\")'>\n";
      sb.append(" " + post + " ");
    } catch (Exception e) {
      System.out.println("ERR " + query + " " + e.toString());
    } finally {
      try {
        if (results != null) { results.close(); }
        if (getRow  != null) { getRow.close(); }
	} catch (SQLException e) { }
    }
    return false;
  }

  /** Return the text of a verse given book number, chapter number,
   * and verse number.*/
  public String findVerseText(int bookNumber, int chapterNumber,
			      int verseNumber, int verseTo, int verseAnd,
			      boolean wantRefs) {
					return findVerseText(bookNumber, chapterNumber, verseNumber, verseTo, verseAnd, wantRefs,
							"");
				}
  /** Return the text of a verse given book number, chapter number,
   * and verse number.*/
  public String findVerseText(int bookNumber, int chapterNumber,
			      int verseNumber, int verseTo, int verseAnd,
			      boolean wantRefs, String lang) {
		return findVerseText(bookNumber, chapterNumber, verseNumber, verseTo, verseAnd, wantRefs,
				new String[] { lang });
  }

/** Return the text of a verse given book number, chapter number,
   * and verse number.*/
  public String findVerseText(int bookNumber, int chapterNumber,
			      int verseNumber, int verseTo, int verseAnd,
			      boolean wantRefs, String[] langs) {
    String query;
    StringBuilder sb = new StringBuilder();
    Vector<String> vec;
    int chap1Verse;
    int i;
    boolean had2 = false; // references split
    boolean multi = langs.length > 1;
    boolean wantSup;
    int id;
    /**/
    query = "select VerseID from BookChapters where BookID=" + bookNumber + " and Chapter=" + chapterNumber;

    vec = SQLUtilities.VectorizeQuery(query);
    if (vec.size() <= 0) {
      return null;
    }

    chap1Verse = Integer.parseInt((String)vec.elementAt(0));
    chap1Verse += verseNumber -1;
    id = chap1Verse;

    String sel = selectVerseCols(langs);
    if ((verseTo == 0) && (verseAnd == 0)) {
      query = sel + "where ID=" + chap1Verse;
    } else {
      if (verseTo != 0) {
	query = sel + "where ID>=" + chap1Verse +
	  " and ID<=" + (chap1Verse + verseTo - verseNumber) + " order by ID";
      } else {
    	had2 = true;
	query = sel + "where ID=" + chap1Verse +
	  " OR ID=" + (chap1Verse + verseAnd - verseNumber) + " order by ID";
      }
    }
    // System.out.println(query);

    vec = SQLUtilities.VectorizeQuery(query);
    wantSup = (vec.size() > 1) && !multi;

    if (multi) {
    	sb.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n");
    }

    if (wantRefs || wantSup) { sb.append(" <sup>" + verseNumber + "</sup>"); }
    sb.append(createVerseTableRow((String)vec.elementAt(0), multi, verseNumber));
    if (wantRefs) { addCrefHTML(sb, id, langs[0]); }

    if (had2) {
      if (wantRefs || wantSup) { sb.append(" <sup>" + verseAnd + "</sup>"); }
      sb.append(createVerseTableRow((String)vec.elementAt(1), multi, verseAnd));
      if (wantRefs) { addCrefHTML(sb, chap1Verse + verseAnd - verseNumber, langs[0]); }
    } else {
      for (i=1; i<vec.size(); i++) {
        if (wantRefs || wantSup) { sb.append(" <sup>" + (verseNumber + i) + "</sup>"); }
	sb.append(createVerseTableRow((String)vec.elementAt(i), multi, verseNumber + i));
	if (wantRefs) { addCrefHTML(sb, id + i, langs[0]); }
      }
    }
    recentVerseNumber = chap1Verse;
    if (multi) {
    	sb.append("</table>\n");
    }
    return sb.toString();
  }

  /** Split a vector element on | and turn all such strings into a table row
 * @param element string with multiple elements or not
 * @param multi true if the element has more than one string in it
 * @param verseNumber verse number in the chapter
 * @return table row or single element
 */
  public static String createVerseTableRow(String element, boolean multi, int verseNumber) { {
	  if (multi) {
		  StringBuilder sb = new StringBuilder();
		  String[]vs = element.split("\\|");
		  for (String v : vs) {
			  sb.append("<td style=\"vertical-align:top\"><sup>" + verseNumber + "</sup>" + v + "</td>");
		  }
		  return ("<tr>" + sb.toString() + "</tr>\n");
	  }
	  else return element;
  }

  }
  /** Create a select statement with the appropriate language-specific
   * columns for languages named in the array.
   * @param langs array of languages, the empty string means English
   * @return select statement ready for where clause.
   */
  public static String selectVerseCols(String[] langs) {
	  StringBuilder sb = new StringBuilder();
	  for (String lang : langs) {
		  if (sb.length() > 0) { sb.append(", "); }
		  // CONVERT(<col> USING utf8)
		  sb.append(lang + "Content");
	  }
	  return "select " + sb.toString() + " from Verses ";
  }

  /** Main program to test the SQL version of the verse search
   * routine.  */
  public static void main(String[] args) {
    RefSQLLookup rsl = new RefSQLLookup();
    int i;
    String name;
    /**/

    rsl.setup();
    for(i=0; i<args.length; i++) {
      name = rsl.findCanonicalReference(args[i], "");
      System.out.println(args[i] + " " + name);
      System.out.println(rsl.findAVerse(name, true, "") + "  " + name);
    }
  }
}
