/* @name ConvUtils.java

    Copyright (c) 2009 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker$
    Change Log:
    $Log: $
*/

package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Material Gain
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class ConvUtils {

  public static final long serialVersionUID = 1;

  /** Obligatory constructor.*/
  public ConvUtils() { /* */ }

  public static final String BOOK_HEADER =
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"" +
    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
    "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"_LN_\">\n" +
    "<head>\n" +
    "<title>_TITLE_</title>\n" +
    "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n" +
    "<link rel=\"stylesheet\" href=\"bible.css\" type=\"text/css\" />\n" +
    "<script language=\"javascript\" type=\"text/javascript\" src=\"bible.js\"></script>\n" +
    "</head>\n" +
    "<body>\n" +
    "<div id=\"header\"><img src=\"head.jpg\" alt=\"\" /></div>\n" +
    "<div id=\"page\">\n";

  public static final String BOOK_TRAILER =
    "</div>" +
    "</body>" +
    "</html>";

  public static final String TOC_HEADER =
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"" +
    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
    "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"_LN_\">" +
    "<head>" +
    "<title>_TITLE_</title>" +
    "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
    "</head>" +
    "<body>" +
    "<img src=\"toc.jpg\" width=\"190\" alt=\"\" /><br />" +
    "<b>_HIGHLIGHT_</b><br />" +
    "<input type=\"text\" id=\"txtSearch\" name=\"txtSearch\" size=\"20\" />" +
    "<button onclick='top.right_frame.srch(document.getElementById(\"txtSearch\").value);'>_BUTTON_</button>" +
    "<br />" +
    "<br />";

  public static final String TOC_TRAILER =
    "</body>" +
    "</html>";

  /**
   * Replace all occurrences of s1 in the string with s2
   * @param sb string to be modified
   * @param s1 string to be replaced
   * @param s2 replacement string.  If null, changed to the empty string
   * @return modified string
   */
  public static StringBuilder replaceAll(StringBuilder sb,
					 String s1, String s2) {
    int ix  = 0;
    int len = s1.length();
    if (s2 == null) { s2 = ""; }

    while ( (ix = sb.indexOf(s1, ix)) >-1) {
      sb = sb.replace(ix, ix+len, s2);
    }
    return sb;
  }

  /**
   * Read a file and return its entire contents in a mutable string
   * @param fileName name of the file
   * @param sb StringBuilder to use if convenient, otherwise the
   * method creates a new string builder
   * @return contents of the file or an error message if the file
   * cannot be read
   */
  public static StringBuilder slurpFile(String fileName, StringBuilder sb)
    throws FileNotFoundException, IOException {
    File file = new File(fileName);
    String aLine;
    BufferedReader br;
    if (sb == null) {
      sb = new StringBuilder((int)file.length());
    } else {
      sb.setLength(0);
    }

    br = new BufferedReader(new FileReader(file));
    while ( (aLine = br.readLine() ) != null) {
      sb.append(aLine + "\n");
    }
    return sb;
  }
}
