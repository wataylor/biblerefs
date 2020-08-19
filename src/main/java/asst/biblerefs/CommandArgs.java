/* @name CommandArgs.java

    Copyright (c) 2008 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: CommandArgs.java,v $
    Revision 1.1  2017/04/22 01:50:25  peren
    safety

*/

package asst.biblerefs;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility to handle command-line arguments of the form key=value,
 * +key, -key, or plain strings.  Strings are stored in a linked list
 * in the order in which they are found on the command line.  Values
 * can be retrieved by referencing keys.  The value of a + or - key
 * will be a boolean object of TRUE or FALSE.  It is up to the caller
 * to know what type a given arg should be and to parse it
 * accordingly.</p>

 * <P>The calling program specifies defaults by setting a default
 * string before parsing the command line args.

 * @author Bill Taylor
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class CommandArgs extends Hashtable<String, Object> {

  public static final long serialVersionUID = 1;

  List<String> strings=new LinkedList<String>();

  /** Obligatory constructor.*/
  public CommandArgs() { /* */ }

  public CommandArgs(String[] args) {
    parseArgs(args);
  }

  /** Add whatever arguments are specified by the string array to the
   * hash table.
   * @param args array of argument strings.  Quotes are removed and
   * null or zero-length entries are skipped.  */
  public void parseArgs(String[] args) {
    String param;
    for(String arg : args) {
      if (arg == null) { continue; }
      // If a string begins with a quote, assume it has two and remove both
      if (arg.startsWith("\"")) { arg = arg.substring(1, arg.length()-1); }

      // Skip empty strings
      if (arg.length() <= 0) { continue; }

      param=arg.substring(1);
      if(arg.indexOf('=')>=0) {
        put(arg.substring(0, arg.indexOf('=')),
	    arg.substring(arg.indexOf('=')+1));
      } else if(arg.charAt(0)=='-') {
        put(param, Boolean.FALSE);
      } else if(arg.charAt(0)=='+') {
        put(param, Boolean.TRUE);
      } else {
        strings.add(arg);
      }
    }
  }

  /** Parameters which are not of the form name=value are stored in a
   * list in the order they are encountered on the command line.  This
   * method returns a list of all such command line parameters.*/
  public List<String> getStrings() {
    return strings;
  }

  /** Return the value of a parameter as a boolean; defaults to
   * <code>false</code> unless the parameter has been entered as
   * +&lt;param name&gt; in which case, it returns true
   * @param param name of the parameter to be returned.*/
  public boolean getBoolean(String param) {
    Boolean b;
    if ( (b = (Boolean)get(param)) == null) { return false; }
    return b.booleanValue();
  }

  /**
   * Convert a string to an integer, returning 0 on failure
   * @param va string which represents an integer
   * @return the value of the integer or 0 if the string is null or
   * does not represent a properly-formatted integer.
   */
  public static int integerFromString(String va) {
    try {
      return Integer.decode(va);
    } catch (Exception e) { /* */ }
    return 0;
  }

  /**
   * Get the value of a parameter as an integer.  Returns 0 if the
   * parameter is not found or if it is not a properly-formatted
   * integer
   * @param param name of the parameter to be converted to an integer
   * @return value of the integer or 0 if the parameter is not found
   */
  public int getInt(String param) {
    return integerFromString((String)get(param));
  }

  /**
   * Convert a string to a double, returning 0 if the string is null
   * or is not a properly-formatted double
   * @param va string to be converted
   * @return value of the string as a double or 0 if anything goes
   * wrong.
   */
  public static double doubleFromString(String va) {
    try {
      return Double.valueOf(va);
    } catch (Exception e) { /* */ }
    return 0;
  }

  /**
   * Return the value of a parameter as a double.  Returns 0 if the
   * parameter is not found or is not a properly-formatted double
   * @param param name of the desired parameter
   * @return value of the parameter as a double or 0 if it is not a
   * proper double
   */
  public double getDouble(String param) {
    return doubleFromString((String)get(param));
  }

  /**
   * Convert a string to a long, returning 0 if the string is null or
   * is not a properly-formatted long
   * @param va string to be converted
   * @return value of the string as a long or 0 if anything goes wrong.
   */
  public static long longFromString(String va) {
    try {
      return Long.decode(va);
    } catch (Exception e) { /* */ }
    return 0;
  }

  /**
   * Return the value of a parameter as a long, returns 0 if the
   * parameter is not found or it is not a properly-formatted long
   * @param param name of the parameter to be converted to long
   * @return value of the parameter as a long, returns 0 if the
   * parameter is not found
   */
  public long getLong(String param) {
    return longFromString((String)get(param));
  }

  /**
   * Put a new file type on a file name.
   * @param fileName original file name
   * @param ext new extension without a .  Passing a zero-length
   * string as the extension results in returning the original file
   * name without the "." or the original extension.  This is useful
   * for getting the file name without the extension.
   * @return file name with the file type replaced by the new
   * extension.
   */
  public static String newFileType(String fileName, String ext) {
    int ix;

    if (!fileName.endsWith("." + ext)) {
      if ( (ix = fileName.lastIndexOf(".")) >= 0) {
	fileName = fileName.substring(0, ix);
      }
    }
    if (ext.length() <= 0) { return fileName; }
    return fileName + "." + ext;
  }

  /**
   * Return the file name part of a file path
   * @param pathName the name of the path
   * @return the file name portion of the path
   */
  public static String fileNameOnly(String pathName) {
    File file = new File(pathName);
    return file.getName();
  }

  /**
   * Prepare a string for comparison with a string which is in standard format
   * @param in input string
   * @return trimmed, and with all redundant spaces removed.
   */
  public static String canonize(String in) {
    in = in.trim().replace("  ", " ");
    while (in.indexOf("  ") >= 0) { in = in.replace("  ", " "); }
    return in;
  }
}
