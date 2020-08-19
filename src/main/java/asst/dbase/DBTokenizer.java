/* DBTokenizer.java

    Copyright (c) 1999 by AS-ST.  All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: DBTokenizer.java,v $
    Revision 1.2  2003/01/03 04:24:21  asst
    documentation

    Revision 1.1.1.1  2002/04/09 03:20:16  zonediet
    first import


*/

package asst.dbase;

/**
 * The fixed tokenizer class breaks a string into tokens.
 * <code>DBTokenizer</code> methods do not distinguish among
 * identifiers, numbers, and quoted strings, nor do they recognize and
 * skip comments.</p>

 * <p>The set of delimiters (the characters that separate tokens) must
 * be specified at creation.  A <code>null</code> is returned for each
 * pair of successive delimeters.</p>

 * <P>The following is one example of the use of the tokenizer. The code:
 * <blockquote><pre>
 *     DBTokenizer st = new DBTokenizer("this is a test");
 *     while (st.hasMoreTokens()) {
 *         println(st.nextToken());
 *     }
 * </pre></blockquote>
 * <p>
 * prints the following output:
 * <blockquote><pre>
 *     this
 *     is
 *     a
 *     test
 * </pre></blockquote>
 *
 * @author  money
 * @version 1.0, 09/28/99
 * @since  */

public class DBTokenizer implements java.util.Enumeration
{
  private int currentPosition;
  private int maxPosition;
  private String str;
  private String delimiters;
  private boolean retTokens;

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens.  The delimiter characters are not
   * returned, they only serve as separators between tokens.
   *
   * @param   str            a string to be parsed.
   * @param   delim          the delimiters.
   * @param   returnTokens   flag indicating whether to return the delimiters
   *                         as tokens.
   *  */
  public DBTokenizer(String str, String delim, boolean returnTokens) {
    currentPosition = 0;
    this.str = str;
    maxPosition = str.length();
    delimiters = delim;
    retTokens = returnTokens;
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens.
   *
   * @param   str     a string to be parsed.
   * @param   delim   the delimiters.
   *
   */
  public DBTokenizer(String str, String delim) {
    this(str, delim, false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * tokenizer uses the default delimiter set, which is
   * <code>"&#92;t&#92;n&#92;r"</code>: the space character, the tab
   * character, the newline character, and the carriage-return character.
   *
   * @param   str   a string to be parsed.
   *
   */
  public DBTokenizer(String str) {
    this(str, " \t\n\r", false);
  }

  /**
   * Tests if there are more tokens available from this tokenizer's string.
   *
   * @return  <code>true</code> if there are more tokens available from this
   *          tokenizer's string; <code>false</code> otherwise.
   *
   */
  public boolean hasMoreTokens() {
    return (currentPosition < maxPosition);
  }

  /**
   * Returns the next token from this string tokenizer.
   *
   * @return     the next token from this string tokenizer.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   *
   */
  public String nextToken() {

    if (currentPosition >= maxPosition) {
      return "";
    }

    int start = currentPosition;
    while ((currentPosition < maxPosition) &&
	   (delimiters.indexOf(str.charAt(currentPosition)) < 0)) {
      currentPosition++;
    }
    if ((start == currentPosition) && // Tokens always count
	(delimiters.indexOf(str.charAt(currentPosition)) >= 0)) {
      currentPosition++;
      return "";	// Point beyond the delimeter
    }
    return str.substring(start, currentPosition++);
  }

  /**
   * Returns the next token in this string tokenizer's string. The new
   * delimiter set remains the default after this call.
   *
   * @param      delim   the new delimiters.
   * @return     the next token, after switching to the new delimiter set.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   *
   */
  public String nextToken(String delim) {
    delimiters = delim;
    return nextToken();
  }

  /**
   * Returns the same value as the <code>hasMoreTokens</code>
   * method. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return  <code>true</code> if there are more tokens;
   *          <code>false</code> otherwise.
   * @see     java.util.Enumeration
   * @see     #hasMoreTokens()
   *
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Returns the same value as the <code>nextToken</code> method,
   * except that its declared return value is <code>Object</code> rather than
   * <code>String</code>. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return     the next token in the string.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @see        java.util.Enumeration
   * @see        #nextToken()
   *
   */
  public Object nextElement() {
    return nextToken();
  }

  /**
   * Calculates the number of times that this tokenizer's
   * <code>nextToken</code> method can be called before it generates an
   * exception.  This is one more than the number of delimiters; there need
   * be no delimiter after the last token.
   *
   * @return  the number of tokens remaining in the string using the current
   *          delimiter set.
   *
   */
  public int countTokens() {
    int count = 1;
    int currpos = currentPosition;

    while (currpos < maxPosition) { // Count remaining delimiters
      if (delimiters.indexOf(str.charAt(currpos)) >= 0) {
	count++;
      }
      currpos++;
    }
    return count;
  }
}
