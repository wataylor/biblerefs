/* @name StringSetUtils.java

Manipulate strings

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: StringSetUtils.java,v $
    Revision 1.4  2007/01/17 04:02:59  asst
    documentation

    Revision 1.3  2003/07/08 19:02:00  asst
    makeSetsLikeMask

    Revision 1.2  2002/09/13 03:26:06  asst
    test a set string member

    Revision 1.1  2002/09/12 04:50:17  asst
    upload
<br>

*/

package asst.dbase;

/**
 * It is useful to store sets as SQL strings so that each character of
 * the string represents one potential set member.  This class
 * implements methods for manipulating such set strings.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class StringSetUtils {

  /** Obligatory constructor.*/
  public StringSetUtils() {
  }

  /** Set a specified member of a string set to a specified value,
   extending the string with 'n' characters as needed to make space.
   @param setValue the current value of the entire set string
  @param member tells which member of the set to change
  @param value string whose first character gives the new value of the
  set member
  @return set string with the specified member set to the specified value.*/
  public static String SetASetStringMember(String setValue, int member,
				    String value) {
    return StringSetUtils.SetASetStringMember(setValue, member,
					      value.charAt(0));
  }

  /** Set a specified member of a string set to a specified value,
   extending the string with 'n' characters as needed to make space.
   @param setValue the current value of the entire set string
  @param member tells which member of the string set to change
  @param value character which gives the new value of the set member
  @return set string with the specified member set to the specified value.*/
  public static String SetASetStringMember(String setValue, int member,
					   char value) {
    char[] statChars;		// Status string as a character array
    /**/

    while (setValue.length() <= member) { setValue = setValue + "n"; }
    statChars = setValue.toCharArray();
    try {
      if (statChars[member] == value) { return setValue; } // already good
      statChars[member] = value;
    } catch (Exception e) {}
    return new String(statChars);
  }

  /** Test a particular set string member to see if it is set,
   returning <code>false</code> if the string is not long enough or if
   the value of the specified set member is other than 'Y'.
   @param setValue the current value of the set string
  @param member tells which member to test
  @return true if the selected character is 'Y'
 */
  public static boolean TestASetStringMember(String setValue, int member) {
    try {
      return (setValue.charAt(member) == 'Y');
    } catch (Exception e) {}
    return false;
  }

  /** Return the index of the first Y in a string or zero if the
      string is null or if there is no Y in it. */
  public static int FirstYInSet(String setS) {
    int i;
    try {
      for (i=0; i<setS.length(); i++) {
	if (setS.charAt(i) == 'Y') { return i; }
      }
    } catch (Exception e) {}
    return 0;
  }

  /** Return the index of the last Y in a string, returning zero if the
      string is null or if there is no Y in it.
  @param setS String representation of the set.
  @return index of the last Y in the set or 0.*/
  public static int LastYInSet(String setS) {
    int i;
    try {
      for (i=setS.length()-1; i>0; i--) { // >0 is actually correct....
	if (setS.charAt(i) == 'Y') { return i; }
      }
    } catch (Exception e) {}
    return 0;
  }

  /** Return a string to be used in a SQL <code> where / like </code>
   * clause to test a specified set member for a specified value.  It
   * is up to the caller to be sure that the member actually exists in
   * the database column which stores the sets which will be compared
   * to the mask.
   @param member selects which member is to be tested.
  @param value string whose first character gives the desired value of
  the set member */
  public static String makeSetsLikeMask(int member, String value) {
    return StringSetUtils.makeSetsLikeMask(member, value.charAt(0));
  }

  /** Return a string to be used in a SQL <code> where / like
   * </code> clause to test a specified set member for a specified
   * value.  It is up to the caller to be sure that the member
   * actually exists in the database column which stores the sets
   * which will be compared to the mask.
   @param member selects which member is to be tested.
  @param value character which gives the desired value of the set member */
  public static String makeSetsLikeMask(int member, char value) {
    int i;
    StringBuffer sb = new StringBuffer(member);
    /**/

    for (i=0; i<member; i++) {
      sb.append('_');
    }
    sb.append(value);
    sb.append("%");
    return sb.toString();
  }

}

