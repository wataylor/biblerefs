/* @name MonthDayNames.java

    Copyright (c) 2002 by Advanced Systems & Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: MonthDayNames.java,v $
    Revision 1.2  2003/12/07 02:30:29  asst
    documentation


*/

package asst.time;

/**
 * This class defines the names of the days of the week and the names
 * of the months.  For the moment. the class forces a specified
 * locale.  In the future, the day and month name arrays will be
 * defined as non-final and will be loaded by a static initializer
 * based on the locale.

 * @author Web Work
 * @version %I%, %G%
 * @since
 *
 * @see <classname> */

public class MonthDayNames {

  public MonthDayNames() {
  }

  /** List of names of days of the week.  Ideally, this should be part
      of the locale, but for now, constants suffice.  Sunday is day #
      1.*/
  static public final String[] WEEK_DAY_NAMES = {
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
    "Saturday"};

  /** List of the names of months.  January is month # 1. */
  static public final String[] MONTH_NAMES = {
    "January", "February", "March", "April", "May", "June", "July", "August",
    "September", "October", "November", "December"
  };

  /** List of years which are of interest.*/
  static public final String[] YEAR_NAMES = {
    "1999",
    "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008"};
}
