/* @name Quotable.java

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: Quotable.java,v $
    Revision 1.16  2007/01/17 04:02:37  asst
    do not quote a quoted character

    Revision 1.15  2005/03/22 19:38:10  asst
    convert inbound data columns

    Revision 1.14  2003/07/08 19:05:09  asst
    documentation, mediumblob

    Revision 1.13  2003/05/19 16:05:08  asst
    DeQuote method

    Revision 1.12  2003/02/05 06:00:40  asst-dlh
    Added Microsoft SQL Server fix

    Revision 1.11  2003/02/04 04:28:27  asst
    blob columns are quoted

    Revision 1.10  2002/09/20 21:40:30  asst
    ConvertToJavaScript

    Revision 1.9  2002/08/18 03:38:08  asst
    documentation

    Revision 1.8  2002/08/01 16:42:02  zonediet
    not add space when gluing commas

    Revision 1.7  2002/07/13 17:56:05  zonediet
    documentation

    Revision 1.6  2002/07/07 01:06:01  zonediet
    added StrongStringEqual

    Revision 1.5  2002/07/05 02:54:50  zonediet
    day-only yyyy string converter

    Revision 1.4  2002/05/22 02:20:59  zonediet
    documentation

    Revision 1.3  2002/05/16 17:04:21  zonediet
    changed package structure

    Revision 1.2  2002/05/08 14:46:16  zonediet
    added find line ending string

    Revision 1.1.1.1  2002/04/09 03:20:25  zonediet
    first import

*/

package asst.dbase;

import java.lang.StringIndexOutOfBoundsException;

import java.sql.ResultSetMetaData;

import asst.dbase.BinaryQuote;

/**
 * Library of SQL data manipulation functions, it started with a
 * function to determine whether a SQL column needs quotes around its
 * values.  This was defined as a separate function because there is no
 * real standard describing character-type database column
 * characteristics.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SQLUtilities
 * @see asst.test.TestQuote
 */

public class Quotable {

  /** Many classes need an empty string.  Defining only one minimizes
   * garbage generation.*/
  public static final String ES = "";

  /** Obligatory constructor.  */
  public Quotable() {
  }

  /** Determine whether the SQL column type description indicates that
      the column requires quotes for constants input to it via
      <CODE>INSERT</code> or <CODE>UPDATE</code>.
  @param SQLColumnType string extracted from result set meta data.
  @return true if the column holds a data field. */

  public static boolean IsColumnTypeDate(String SQLColumnType) {
    //Microsoft, as usual, does not follow the standard.
    //They appear to always return lowercase--yea microsoft!
    SQLColumnType = SQLColumnType.toUpperCase();
    return (SQLColumnType.startsWith("DATE") ||
	    SQLColumnType.startsWith("TIME"));
  }

  public static boolean IsColumnTypeQuotable(String SQLColumnType) {
    //Microsoft, as usual, does not follow the standard.
    //They appear to always return lowercase--yea microsoft!
    SQLColumnType = SQLColumnType.toUpperCase();
    return (SQLColumnType.startsWith("CHAR") ||
	    SQLColumnType.startsWith("VARCHAR") ||
	    SQLColumnType.startsWith("DATE") ||
	    SQLColumnType.startsWith("TIME") ||
	    SQLColumnType.startsWith("BLOB") ||
	    SQLColumnType.startsWith("MEDIUMBLOB") ||
	    SQLColumnType.startsWith("TEXT"));
  }

  /** Determine whether the SQL column requires quotes for constants
      input to it.  If the column name is not found in the table,
      returns -1 so that the caller knows not to select on that
      particular column.
      @param SQLColumn name of the column
      @param meta result set meta data which should describe the
      column
      @return 1 -> column not in table, 0 -> in table but not
  quotable, 1 -> in table and quotable.  */
  public static int IsColumnQuotable(String SQLColumn,
				     ResultSetMetaData meta)
    throws java.sql.SQLException {

    int i;			// Array index
    int cols;			// Number of columns in meta data
    /**/

    cols = meta.getColumnCount();
    for (i=1; i<=cols; i++) {
      if (SQLColumn.equals(meta.getColumnName(i))) {
	return (Quotable.IsColumnTypeQuotable(meta.getColumnTypeName(i)) ? 1 : 0);
      }
    }
    return -1;		// Did not find it, don't select for it
  }

  /** Return the string with quotation marks removed, that is, if there
   * is a quote at the beginning, remove bracketing quotes and replace
   * successive quote marks with one quote mark.  This is used to
   * clean up quotation by Excel.
   @param aString string which may have quotation marks in it.
  @return string without quotation marks.*/
  public static String DeQuote(String aString) {
    StringBuffer sb;
    int i;
    int j;
    int len;
    /**/

    if ((aString == null) || !aString.startsWith("\"")) {
      return aString;		// Not quoted, return it
    }

    len = aString.length()-1;	// Ignore trailing quote mark
    sb = new StringBuffer(len);

    try {
      for (i=1; i<len; i++) {
	if (aString.charAt(i) == '"') {
	  i++;
	}
	if (i >= len) break;	// watch for pairs at end
	sb.append(aString.charAt(i));
      }
    } catch (Exception e) {}

    return sb.toString();
  }

  /** Look for characters which need to be quoted in the SQL string,
      return a new string with these characters quoted.  This method
      is tested by the program TestQuote.java; the test program should
      be augmented whenever this routine is upgraded to support more
      special characters.  Quotes line ending
      characters even though most JDBC drivers are able to pass them
      directly to the database.
  @param aString input string
  @return string with single quotes quoted */
  public static String QuoteForSQLString(String aString) {
    return BinaryQuote.binaryQuote(aString);
//     String out = ES;
//     int i;
//     int j = 0;
//     /**/

//     if (aString == null) { return ES; }

//     if ( (i = aString.indexOf("'")) < 0) return aString;

//     do {
//       out = out + aString.substring(j, i);
//       if (out.endsWith("\\")) {
// 	out += "'";		// Already quoted, need only one
//       } else {
// 	out += "''";		// Double quotes it
//       }
//       j = i + 1;
//     } while ( (j <= aString.length()) && (i = aString.indexOf("'", j)) >= 0);
//     if (j < aString.length()) {
//       out = out + aString.substring(j);
//     }
//     return out;
  }

  /** Take a string and prepare it for use in a SQL insert or update
      command by a) quoting whatever special characters it may contain
      and b) wrapping it in SQL quotes so that it can be added to a
      SQL command as a proper value.
  @param aString input string
  @return quoted string enclosed in SQL single quotes */
  public static String ConstructSQLCharacterValue(String aString) {
    return "'" + Quotable.QuoteForSQLString(aString) + "'";
  }

  /** Examine a string and make sure that it is a valid numeric SQL
      constant.  All that this does is to turn nulls and empty strings
      to "0", there is no checking for valid integers.  See
      <code>SQLUtilities.integerFromString</code> for a more thorough
      test.
  @param aString input string
  @return trimmed string or "0" if string is empty or null.*/
  public static String ConstructSQLNumericValue(String aString) {
    if ((aString == null) || ES.equals(aString)) return "0";
    return aString.trim();
  }

  /** Examine a string in mm/dd/yyyy <trailer> AM | PM and turn it
      into yyyy mm dd <trailer> while dropping the AM or PM.  This
      turns the date into something which the SQL time tag datatype
      recognizes.
   @param mmddyyyy input string
   @return yyyymmdd or the empty string in case of an
   error, <B>never</b> returns null.  */
  public static String MDYToSQL(String mmddyyyy) {
    String year;
    String month;
    String day;
    String newDate;
    String rest;
    int idx;
    /**/

    try {
	if ( (idx = mmddyyyy.indexOf(" AM")) < 0) {
	    idx = mmddyyyy.indexOf(" PM");
	}

	if (idx > 11) {
	    rest =  mmddyyyy.substring(11, idx);
	} else {
	    try {
		rest =  mmddyyyy.substring(11);
	    } catch (StringIndexOutOfBoundsException e) { rest = ES; }
	}

	month = mmddyyyy.substring(0,  2);
	day   = mmddyyyy.substring(3,  5);
	year  = mmddyyyy.substring(6, 10);

	newDate = year + "-" + month + "-" + day + " " + rest;

	return newDate.trim();
    } catch (Exception e) { return ES; }
  }

  /** Examine a string yyyymmddhhmmss and make it human-readable as
      hh:mm:ss mm/dd/yyyy.
   @param yyyy input string
   @return hh:mm:ss mm/dd/yyyy or the empty string in case of an
   error, <B>never</b> returns null.*/
  public static String YYYYEtcToHum(String yyyy) {
    if ((yyyy == null) || (yyyy.equals(ES))) return ES;

    try {
    return yyyy.substring(8,10) + ":" + yyyy.substring(10,12) + ":" +
      yyyy.substring(12) + " " +
      yyyy.substring(4,6) + "/" + yyyy.substring(6,8) + "/" +
      yyyy.substring(0,4);
    } catch (Exception e) {
      return ES;
    }
  }

   /** Examine a string yyyymmdd and make it human-readable as
       mm/dd/yy.
   @param yyyy input string
   @return mm/dd/yy or the empty string in case of an error,
   <B>never</b> returns null.*/
  public static String YYYYMMDDToHum(String yyyy) {
    if ((yyyy == null) || (yyyy.equals(ES))) return ES;

    try {
    return yyyy.substring(4,6) + "/" + yyyy.substring(6,8) + "/" +
      yyyy.substring(2,4);
    } catch (Exception e) {
      return ES;
    }
  }

  /** Do the work of combining two strings, separating them by a space
    unless the 2nd begins with a comma.
    @param sb StringBuffer to be extended by the string.
    @param s1 String to be appended to the buffer.  A space is
  appended before s1 unless s1 begins with a comma.*/
  private static void glue1String(StringBuffer sb, String s1) {
    if ((s1 == null) || ES.equals(s1)) return;
    if ((sb.length() >= 1) && !s1.startsWith(",")) { sb.append(" "); }
    sb.append(s1.trim());
  }

  /** Combine 5 different strings into one space-sparated string if
      they are not null or empty.  The input strings are separated by
      a space unless a string begins with a comma in which case the
      space is not inserted.
  @param s1 first of 5 strings or null
  @param s2 second of 5 strings or null
  @param s3 third of 5 strings or null
  @param s4 fourth of 5 strings or null
  @param s5 fifth of 5 strings or null
  @return string combining all the non-null input strings*/
  public static String glueStrings(String s1, String s2, String s3, String s4,
				   String s5) {
    StringBuffer sb = new StringBuffer(40);
    /**/

    glue1String(sb, s1);
    glue1String(sb, s2);
    glue1String(sb, s3);
    glue1String(sb, s4);
    glue1String(sb, s5);

    return sb.toString();
  }

  /** Compare meaningful strings and state whether they were equal if
   case is ignored or whether it could not be determined.
   @param s1 one input string or null.
   @param s2 one input string or null.
   @return 0 if either string was null or empty so that they could not
   be compared, 1 if both strings were non-empty and they were equal,
   -1 if both strings were non-empty and they were not equal.
  */

  public static int StrongStringEqual(String s1, String s2) {
    if ((s1 == null) || (s1.length() < 1) ||
	(s2 == null) || (s2.length() < 1)) return 0;
    if (s1.equalsIgnoreCase(s2)) return 1;
    return -1;
  }

  /** Determine whether a string begins with a character format, in
      which case, it needs a prepended paragraph.
  @param aString input string to be tested
  @return true -> begins with formatting, false otherwise*/
  public static boolean NeedsPara(String aString) {
    String first2;
    /**/
    if (aString.length() < 2) return true;
    if (!aString.startsWith("<")) return true;
    first2 = aString.substring(1,2); // Get the 2nd character
    if (first2.equalsIgnoreCase("b") ||
	first2.equalsIgnoreCase("i")) return true;
    return false;
  }

  /** Determine whether a string ENDS with a character format, in
      which case, it needs a prepended paragraph.
  @param aString input string to be tested
  @return true -> ends with formatting, false otherwise*/
  public static boolean NeedsEndPara(String aString) {
    String last2;
    /**/
    if (aString.length() < 2) return true;
    if (!aString.endsWith(">")) return true;
    last2 = String.valueOf(aString.charAt(aString.length()-2));
    if (last2.equalsIgnoreCase("b") ||
	last2.equalsIgnoreCase("i")) return true;
    return false;
  }

  /** Take a string and return the kind of line endings found therein.
   This is because a line ending can consist of \n, \r, or any
   combination thereof, depending on original operating system.  If
   you need to search strings to find the line endings for any reason,
   you have to first examine the string with this method to know what
   to look for.
   @param aString string with unknown line endings
   @return the string which ends lines in the input string.*/

  public static String findLineEnding(String aString) {
    String lineReturn;
    int nr;
    int rn;

    if (aString == null) { return ES; }
    aString = aString.trim();	// Get rid of special characters at ends

    // Find out what the line end character is in this string
    nr = aString.indexOf("\n\r");
    rn = aString.indexOf("\r\n");

    if ((nr != -1) && (rn  != -1)) { // Both occur, have to find first one
      if (nr < rn) {
	lineReturn = "\n\r";
	// System.out.println("Line ending is \\n\\r");
      } else {
	lineReturn = "\r\n";
	// System.out.println("Line ending is \\r\\n");
      }
    } else if (nr != -1) {
      lineReturn = "\n\r";
      // System.out.println("Line ending is \\n\\r");
    } else if (rn != -1) {
      lineReturn = "\r\n";
      // System.out.println("Line ending is \\r\\n");
    } else if (aString.indexOf("\n") != -1) {
      lineReturn = "\n";
      // System.out.println("Line ending is \\n");
    } else if (aString.indexOf("\r") != -1) {
      lineReturn = "\r";
      // System.out.println("Line ending is \\r");
    } else {
      //The string IS only one line
      // System.out.println("String is only one line");
      lineReturn = ES;
    }
    //Send back the character(s) that are the line ending.
    return lineReturn;
  }

  /** Convert a .html string to a Java Script string without affecting
      its .html meaning.
      @param inString input string to be converted
      @return input string as converted to be suitable for use as Java
      Script constant */
  public static String ConvertToJavaScript(String inString) {
    char inChar;
    int i;			// Walk the string
    StringBuffer sb;
    /**/

    if (inString == null) return ES;

    sb = new StringBuffer(inString.length());

    for (i=0; i<inString.length(); i++) {
      inChar = inString.charAt(i);
      if (Character.isLetterOrDigit(inChar)) {
	sb.append(inChar);
      } else {
	if (Character.isWhitespace(inChar)) {
	  sb.append(" ");
	} else {
	  if (inChar == '"') {
	    sb.append("&quot;");
	  } else {
	    if (inChar == '\'') {
	      sb.append("&acute;");
	    } else {
	      sb.append(inChar);
	    }
	  }
	}
      }
    }
    return sb.toString();
  }

  /** Take a string and convert it to HTML formatting by noting that
      input paragraphs are separated by blank lines but output
      paragraphs are separated by HTML paragraphs symbols.  This is
      tricky; the string may already have some
      HTML in it which should be preserved.  This prepares a string
      for use a .html, it does NOT make it suitable for use with
      JavaScript, see
      {@link #ConvertToJavaScript(String inString) ConvertToJavaScript}
  @param aString input string to be modified.
  @return HTML-formatted string.*/
  public static String QuoteToHTML(String aString) {
    StringBuffer out = new StringBuffer();
    int i;
    int j = 0;
    int nowAt = 0;		// Cache string indexes
    int wasAt = 0;
    int nextReturn;
    char ch;
    String lineReturn;
    int nr;
    int rn;
    /**/

    if ((aString == null)||(aString.equals(ES))) { return ES; }
    aString = aString.trim();	// Get rid of special characters at ends

    // Find out what the line end character is in this string
    nr = aString.indexOf("\n\r");
    rn = aString.indexOf("\r\n");

    if ((nr != -1) && (rn  != -1)) { // Both occur, have to find first one
      if (nr < rn) {
	lineReturn = "\n\r";
	// System.out.println("Line ending is \\n\\r");
      } else {
	lineReturn = "\r\n";
	// System.out.println("Line ending is \\r\\n");
      }
    } else if (nr != -1) {
      lineReturn = "\n\r";
      // System.out.println("Line ending is \\n\\r");
    } else if (rn != -1) {
      lineReturn = "\r\n";
      // System.out.println("Line ending is \\r\\n");
    } else if (aString.indexOf("\n") != -1) {
      lineReturn = "\n";
      // System.out.println("Line ending is \\n");
    } else if (aString.indexOf("\r") != -1) {
      lineReturn = "\r";
      // System.out.println("Line ending is \\r");
    } else {
      //The string IS only one line
      // System.out.println("String is only one line");
      if (Quotable.NeedsPara(aString)) {
	out.append("<p>" + aString + "</p>");
	return out.toString();
      } else {
	return aString;
      }
    }

    // We know what a line-end looks like, and that the string is more
    // than one line.  If the first line starts with HTML, do nothing,
    // else give it an opening <p>.
    // System.out.println("First char = " + aString.charAt(0));
    if (Quotable.NeedsPara(aString)) {
      out.append("<p>");
    }

    do {
      // Work through the string, one paragraph at a time.  A paragraph
      // is defined as two successive line ends without any data between
      // them.
      nextReturn = aString.indexOf(lineReturn + lineReturn, wasAt);
      // System.out.println("NextReturn = " + nextReturn);
      if (nextReturn == -1) { nextReturn = aString.length(); }

      // Work through the lines in a paragraph
      do {
	nowAt = aString.indexOf(lineReturn, wasAt);
	if (nowAt == -1) { nowAt = aString.length(); }
	out.append(aString.substring(wasAt, nowAt));
	wasAt = nowAt + lineReturn.length();
      } while (wasAt < nextReturn);
      if (wasAt >= aString.length()) {wasAt = aString.length();}
      if (Quotable.NeedsEndPara(aString.substring(0,nowAt))) {
	out.append("</p>");
      }
      out.append("\n");

      for (nowAt = nextReturn; nowAt < aString.length(); nowAt++) {
	if ((aString.charAt(nowAt) != '\n') &&
	    (aString.charAt(nowAt) != '\r')) {
	  if (Quotable.NeedsPara(aString.substring(nowAt))) {
	    out.append("<p>");
	  }
	  break;
	}
      }
      wasAt = nowAt;
    } while (wasAt < aString.length());

    return out.toString();
  }

  /** Take a string and convert it to a HTML formatted list by noting
      that input paragraphs are separated by blank lines but output
      paragraphs are separated by HTML paragraphs symbols.  This is
      trickier than it sounds because the string may already have some
      HTML in it which should be preserved.
      @param aString input string to be converted
      @param ordered generate an ordered list, otherwise an unordered
      list
      @return string expressed as HTML*/
  public static String QuoteToHTMLList(String aString, boolean ordered) {
    StringBuffer out = new StringBuffer();
    int i;
    int j = 0;
    int nowAt = 0;		// Cache string indexes
    int wasAt = 0;
    int nextReturn;
    char ch;
    String lineReturn;
    int nr;
    int rn;
    String listStart;
    String listEnd;
    /**/

    if (ordered) {
      listStart = "<ol>";
      listEnd   = "</ol>";
    } else {
      listStart = "<ul>";
      listEnd   = "</ul>";
    }

    if ((aString == null) || aString.equals(ES)) { return ES; }
    aString = aString.trim();	// Get rid of special characters at ends

    // Find out what the line end character is in this string
    nr = aString.indexOf("\n\r");
    rn = aString.indexOf("\r\n");

    if ((nr != -1) && (rn  != -1)) { // Both occur, have to find first one
      if (nr < rn) {
	lineReturn = "\n\r";
	// System.out.println("Line ending is \\n\\r");
      } else {
	lineReturn = "\r\n";
	// System.out.println("Line ending is \\r\\n");
      }
    } else if (nr != -1) {
      lineReturn = "\n\r";
      // System.out.println("Line ending is \\n\\r");
    } else if (rn != -1) {
      lineReturn = "\r\n";
      // System.out.println("Line ending is \\r\\n");
    } else if (aString.indexOf("\n") != -1) {
      lineReturn = "\n";
      // System.out.println("Line ending is \\n");
    } else if (aString.indexOf("\r") != -1) {
      lineReturn = "\r";
      // System.out.println("Line ending is \\r");
    } else {
      //The string IS only one line
	out.append(listStart+"\n<li>" + aString + "</li>\n"+listEnd);
	return out.toString();
    }

// Wrap <li>...</li> around each paragraph

      out.append(listStart+"\n<li>");

    do {
      // Work through the string, one paragraph at a time.  A paragraph
      // is defined as two successive line ends without any data between
      // them.
      nextReturn = aString.indexOf(lineReturn + lineReturn, wasAt);
      // System.out.println("NextReturn = " + nextReturn);
      if (nextReturn == -1) { nextReturn = aString.length(); }

      // Work through the lines in a paragraph
      do {
	nowAt = aString.indexOf(lineReturn, wasAt);
	if (nowAt == -1) { nowAt = aString.length(); }
	out.append(aString.substring(wasAt, nowAt));
	wasAt = nowAt + lineReturn.length();
      } while (wasAt < nextReturn);
      if (wasAt >= aString.length()) {wasAt = aString.length();}

	out.append("</li>");

      out.append("\n");

      for (nowAt = nextReturn; nowAt < aString.length(); nowAt++) {
	if ((aString.charAt(nowAt) != '\n') &&
	    (aString.charAt(nowAt) != '\r')) {

	    out.append("<li>");

	  break;
	}
      }
      wasAt = nowAt;
    } while (wasAt < aString.length());

    out.append("\n"+listEnd);

    return out.toString();
  }
}
