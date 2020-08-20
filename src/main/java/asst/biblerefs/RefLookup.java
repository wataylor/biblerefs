/* @name RefLookup.java

Abstract class to define facility to look up a scripture reference and
return the verse or verses. This class defines core routine for
parsing scripture references.

    Copyright (c) 2003 by Advanced Systems and Software Technologies.
    All Rights Reserved
*/

package asst.biblerefs;

import java.util.HashMap;

/**
 * Define a way to look up scripture references.  Subclasses are
 * designed to be instantiated by class name and then used.

 * @author money
 * @version %I%, %G%
 * @since 2003 01
 */

public abstract class RefLookup {

  /** Obligatory constructor.*/
  public RefLookup() {
  }

  /** Abstract object which implements the facility to read scripture
   * verses by reference.*/
  public static RefLookup REF_LOOKUP;

  /** Map book names to numbers, the default permission makes it
   * available to subclasses. */
  HashMap<String, Integer> bookNameToNumber = new HashMap<String, Integer>(66);

  /** Verse number for the most recent verse lookup*/
  int recentVerseNumber;

  /** Set up the database or file system */
  public abstract void setup();

  /** Shut down the database or file system */
  public abstract void shutDown();

  /** Look up a book number.  This requires I/O.
      @param bookName a full book name or abbreviation.*/
  public abstract int findBookNumber(String bookName);

  /** Convert a book number to its formal name.  This requires I/O. 
 * @param lang empty string for English, otherwise language name*/
  public abstract String findFormalBookName(int bookNumber, String lang);

  /** Return the text of a verse given book number, chapter number, *
      and verse number.  This requires I/O.
 * @param wantRefs If true, want to include verse cross references*/
  public abstract String findVerseText(int bookNumber, int chapterNumber,
		  int verseNumber, int verseTo, int verseAnd, boolean wantRefs);

/** Return the text of a verse given book number, chapter number, *
      and verse number.  This requires I/O.
 * @param wantRefs If true, want to include verse cross references
 * @param lang Empty string for English, otherwise non-English language*/
  public abstract String findVerseText(int bookNumber, int chapterNumber,
		  int verseNumber, int verseTo, int verseAnd,
		  boolean wantRefs, String lang);

/** Return the text of a verse given book number, chapter number, *
      and verse number.  This requires I/O.
 * @param wantRefs If true, want to include verse cross references
 * @param langs array of language names.  Empty string for English,
 * otherwise non-English language*/
  public abstract String findVerseText(int bookNumber, int chapterNumber,
		  int verseNumber, int verseTo, int verseAnd,
		  boolean wantRefs, String[] langs);

  /** Return the verse number from the most recent verse lookup.  This
   * is helpful in sorting scripture references into Biblical order.
   * this is NOT thread safe; there is no guarantee that the number
   * matches the immediately prior lookup when more than one thread is
   * looking up verses.*/
  public int getRecentVerseNumber() {
    return recentVerseNumber;
  }

  int[] referenceToInts(String reference) {
    String bookName   = null;
    // String verseText  = null;
    boolean noverse;
    char ch;			// Remember the separator, comma or dash
    int bookNumber    = 0;
    int chapterNumber = 0;
    int verseNumber   = 0;
    int verseTo       = 0;
    int verseAnd      = 0;
    int addendum      = 0;
    int ix;
    int i;
    int[] ans;
    /**/

    try {
      ix = reference.indexOf(":");
      if (ix < 0) {
    	  ix = reference.lastIndexOf(".");
      }
      if ( ix < 0) {
	ix = reference.length();
	noverse = true;
      } else {
	noverse = false;
      }

      i = ix-1;			// Before the colon if there is one
      while (Character.isDigit(reference.charAt(i))) { i--; }
      chapterNumber = Integer.parseInt(reference.substring(i+1, ix));

      if (noverse && (!Character.isSpaceChar(reference.charAt(i)))) {
	ch = reference.charAt(i);
	if (ch == '-') { verseTo  = chapterNumber; }
	if (ch == ',') { verseAnd = chapterNumber; }
	while (!Character.isDigit(reference.charAt(i))) { i--; }
	ix = i+1;
	while (Character.isDigit(reference.charAt(i))) { i--; }
	chapterNumber = Integer.parseInt(reference.substring(i+1, ix));
      }

      while (Character.isSpaceChar(reference.charAt(i))) { i--; }
      bookName = reference.substring(0, i+1);
      bookNumber = findBookNumber(bookName);

      if (noverse) {
	verseNumber   = chapterNumber;
	chapterNumber = 1;
      } else {
	i = ix + 1;		// After the colon

	while ((i < reference.length()) &&
	       Character.isDigit(reference.charAt(i))) { i++; }
	verseNumber = Integer.parseInt(reference.substring(ix+1, i));

	if (i < reference.length()) {
	  ch = reference.charAt(i);
	  i++;
	  while ((i < reference.length()) &&
		 !Character.isDigit(reference.charAt(i))) { i++; }
	  if (i < reference.length()) {
	    addendum = Integer.parseInt(reference.substring(i));
	  }
	  if (ch == '-') { verseTo  = addendum; }
	  if (ch == ',') { verseAnd = addendum; }
	}
      }
    } catch (Exception e) {	// Most likely number format exception
      //System.out.println(e.toString());
      //e.printStackTrace();
      return null;		// Something went wrong, no verse
    }
    ans = new int[6];		// gives free zeros
    ans[0] = bookNumber;
    ans[1] = chapterNumber;
    ans[2] = verseNumber;
    ans[3] = verseTo;
    ans[4] = verseAnd;
    if (noverse) { ans[5] = 1; }
    return ans;
  }

  /** Convert a verse reference to canonical form.  Input references
   * are of the form <book or abbreviation> <space> <chapter> <colon>
   * <verse or verses>, verse numbers may be separated by a comma or
   * dash.
 * @param lang empty string for English, otherwise language name*/
  public String findCanonicalReference(String reference, String lang) {
    int[] ans;
    String bookName;
    /**/

    if ( (ans = referenceToInts(reference)) == null) return null;
    bookName = findFormalBookName(ans[0], lang) + " ";
    if (ans[5] == 1) {
      bookName+= ans[2];
    } else {
      bookName += ans[1] + ":" + ans[2];
    }
    if (ans[3] != 0) {
      bookName += "-" + ans[3];
    } else if (ans[4] != 0) {
      if (ans[4] == (ans[2] + 1)) {
	bookName += "-" + ans[4];
      } else {
	bookName += ", " + ans[4];
      }
    }
    return bookName;
  }

  /** Look up a scripture reference of the form <book or abbreviation>
   * <space> <chapter> <colon> <verse or verses>, verse numbers may be
   * separated by a comma or dash.
   @param reference verse reference
 * @param wantRefs true -> want to include cross references in verse lookup
   @return verse text or null if it is not found*/
  public String findAVerse(String reference, boolean wantRefs) {
	return findAVerse(reference, wantRefs, "");
}

/** Look up a scripture reference of the form <book or abbreviation>
   * <space> <chapter> <colon> <verse or verses>, verse numbers may be
   * separated by a comma or dash.
   @param reference verse reference
 * @param wantRefs true -> want to include cross references in verse lookup
 * @param lang empty string for English, otherwise language name
 * @return verse text or null if it is not found*/
  public String findAVerse(String reference, boolean wantRefs, String lang) {
    int[] ans;
    /**/

    if ( (ans = referenceToInts(reference)) == null) return null;
    return findVerseText(ans[0], ans[1], ans[2], ans[3], ans[4], wantRefs, lang);
  }
 
  /** Look up a scripture reference of the form <book or abbreviation>
   * <space> <chapter> <colon> <verse or verses>, verse numbers may be
   * separated by a comma or dash.
   @param reference verse reference
 * @param wantRefs true -> want to include cross references in verse lookup
 * @param array of language names, empty string is English
 * @return verse text or null if it is not found*/
  public String findAVerse(String reference, boolean wantRefs, String[] langs) {
    int[] ans;
    /**/

    if ( (ans = referenceToInts(reference)) == null) return null;
    return findVerseText(ans[0], ans[1], ans[2], ans[3], ans[4], wantRefs, langs);
  }

}
