/* LineInputStream.java

    Copyright (c) 1999 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: LineInputStream.java,v $
    Revision 1.4  2003/07/08 19:05:27  asst
    documentation

    Revision 1.3  2002/08/17 16:21:48  asst
    documentation

    Revision 1.2  2002/05/16 17:04:21  zonediet
    changed package structure

    Revision 1.1.1.1  2002/04/09 03:20:20  zonediet
    first import

*/

package asst.dbase;

/**
 * Extension to File Input Stream which reads one line at a time.

 * @author
 * @version %I%, %G%
 * @since
 *
 */

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class LineInputStream extends FileInputStream {

  /** Create a line input strem to read the file.
   @param newFile object to read the file. */
  public LineInputStream(java.io.File newFile) throws FileNotFoundException {
    super(newFile);
  }

  /** Accumulating characters in a StringBuffer is faster than using a
      String for 2 reasons: 1) The capacity can be set in advance,
      which saves time when new characters are appended, and 2)
      appending is faster because a new String object is created each
      time a character is appended to a string.  */

  private String getRawLine() throws IOException {
    StringBuffer sb;
    int c;

    sb = new StringBuffer(200);

    while ((c=this.read()) > -1) {
      if ((c == '\n') || (c == '\r')) {
	if (sb.length() <= 0) { return ""; }
	return sb.toString();
      }
      sb.append((char)c);
    }
    if (sb.length() > 0) return sb.toString();
    return null;
  }

  /** A raw line can end with either /r or with /n; either may or may
  not be followed by one of the other, in which case, the pair
  constitutes a line end.  Thus, a line end can be any one of /r, /n,
  /r/n, or /n/r.  Two successive /r or /n characters imply an empty
  line, of course.  This routine discards empty lines, so this is
  OK.*/

  public String readLine() throws IOException {
    String accum;		// Accumulate the line

    while (true) {
      accum = getRawLine();
      if (accum == null) return null;

      if (!((accum.length() <= 0) || (accum.startsWith("#")) ||
            (accum.startsWith("\"#")))) {
	return accum;
      }
    }
  }

  /** A page ends with a control-L character.  */
  public String readPage() throws IOException {
    StringBuffer sb;
    int c;

    sb = new StringBuffer(200);

    while ((c=this.read()) > -1) {
      if (c == '\f') return sb.toString();
      if (c != '\r') {
	sb.append((char)c);
      }
    }
    if (sb.length() > 0) return sb.toString();
    return null;
  }
}
