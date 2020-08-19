/* @name SelectorManager.java

Manage selection objects

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SelectorManager.java,v $
    Revision 1.15  2006/01/19 16:40:54  asst
    read-only attribute for fields and forms

    Revision 1.14  2003/07/08 19:04:05  asst
    documentation

    Revision 1.13  2003/05/19 16:05:49  asst
    typos

    Revision 1.12  2003/02/08 14:56:29  asst
    automatic SQL wildcards in where string

    Revision 1.11  2002/10/15 05:32:17  asst
    documentation, getAddParam()

    Revision 1.10  2002/10/06 01:36:46  asst
    first upload

    Revision 1.9  2002/09/20 21:42:27  asst
    documentation

    Revision 1.8  2002/09/12 04:52:57  asst
    added RadioSet, CheckboxSet, RadioSetMember

    Revision 1.7  2002/09/08 02:57:01  asst
    Separated date and password classes

    Revision 1.6  2002/09/04 21:33:35  asst
    Added text-only methods, error string and flag

    Revision 1.5  2002/08/13 00:31:26  zonediet
    documentation

    Revision 1.4  2002/07/13 17:56:05  zonediet
    documentation

    Revision 1.3  2002/04/30 15:36:04  zonediet
    Selector managers do any year, begin and end times

    Revision 1.2  2002/04/11 03:22:44  zonediet
    Added date type selector field

    Revision 1.1.1.1  2002/04/09 03:20:36  zonediet
    first import

*/

package asst.dbase;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for objects which read their state from an input form and
 * remember it for later use in selecting information from a database
 * or vice versa.

 * <P>An array of selector managers drives a .html form which is
 * wrapped around the array using .jsp.  Each Selector generates .html
 * to display itself, extracts its current value from the form
 * parameters returned by a Submit button, and gives its value to
 * utilities which communicate between forms and databases.  One array
 * of these objects generally corresponds to one row of a database
 * table.</p>

 * <P>A selector may have any number of potential values, but only one
 * value is stored in the database or displayed in the form at any
 * given time.  The current value is referred to as the object's
 * <code>choice</code>.  This may or may not reflect what's in the
 * database depending on whether the form is creating a new object or
 * whether the user has changed an existing object's value in the
 * form.</p>

 * <P>The current choice, that is, the value which the object displays in
 * the .html form or writes to the database, can be set
 * either by a string passed in from the caller or by scanning for the
 * form parameter which corresponds to the object in the .html form.
 * The caller may or may not retrieve initial values from the database
 * depending on whether the form is editing an existing object or
 * creating a new one.</p>

 * <P>When the form is initialized by reading from a database, the
 * choice is set based on the value retrieved from the database.  When
 * the form is updated by a .html submit, the form value may be
 * changed by reading from the form.  The intent is that the form
 * values will be used to update the database after sundry error
 * checking.</p>

 * <P>A .jsp program uses selector managers in the following way:</p>

 <ul> <li>Define an array long enough to hold all the selector
 managers, that is, one for every user-editable input variable in the
 form.</li>

 <li>If the array is <code>null</code>, this is the first entry to the
 .jsp, the program should create one selector manager for each
 user-editable field and initialize the form from the database if the
 form is being created to edit an existing entry.</li>

 <li>If the array is not <code>null</code>, determine the current
 choice by calling getChoice for each selector manager.</li>

 <li>Construct a .html <code>form</code> around the selector managers,
 using getHTML() or getHTMLOnly() as needed to insert current values
 into the form.</li>

 <li>When editing is complete, write the form to the database.</li>

 </ul>

 Sample code to do these editing functions is found in {@link
 asst.dbase.AnyFieldArray}

 * @author money
 * @version %I%, %G%
 * @since

 * @see asst.dbase.TwixtTableAndForm
 * @see asst.dbase.ChangeLog
 */

public interface  SelectorManager {

  /** Express the input object in .html, that is, generate a .html
      string which labels the object based on its name and displays
      the current form field value as its .html value.  The string is
      suitable for display in a .html table.  The field width is not
      specified so width is left to the browser.
  @return HTML code needed to label the field and display and edit the
  field value */
  public StringBuffer getHTML();

  /** Express the input object in .html, that is, generate a .html
      string which labels the object based on its name and displays
      the current form field value as its .html value.  The string is
      suitable for display in a .html table.  The field width is
      specified.
  @param width specifies the field display width instead of using the
  default width.
  @return HTML code needed to label the field and display and edit the
  field value */
  public StringBuffer getHTML(int width);

  /** Express the input object in a large labeled text .html area by
      generating a string that displays the current form field value
      stored in the object in .html of the specified dimensions.
  @param width specifies the field display width instead of using the
  default width.
  @param height text box height instead of using the
  default height
  @return HTML code needed to label the field and display and edit the
  field value */
  public StringBuffer getHTML(int width, int height);

  /** Express the input object in a .html field, that is, generate a
      string that displays the current form field value stored in the
      object in .html without labeling it.  This uses the default
      field dimensions.
  @return HTML code needed to display and edit the field value without
  labeling the field.  */
  public StringBuffer getHTMLOnly();

  /** Express the input object in a text .html field, that is,
      generate a string that displays the current form field value
      stored in the object in .html without labeling it.
  @param width specifies the field display width instead of using the
  default width.
  @return HTML code needed to display and edit the field value without
  labeling the field.*/
  public StringBuffer getHTMLOnly(int width)
    throws java.sql.SQLException;

  /** Express the input object in a large text .html area by
      generating a string that displays the current form field value
      stored in the object in .html of the specified dimensions
      without labeling it.
  @param width specifies the field display width instead of using the
  default width.
  @param height text box height instead of using the
  default height
  @return HTML code needed to display and edit the field value without
  labeling the field.*/
  public StringBuffer getHTMLOnly(int width, int height)
    throws java.sql.SQLException;

  /** Set the choice from a string.  This is used to set initial
      values or to initialize the form from the database when the form
      is generated for the first time.  Setting the choice to
      <code>null</code> indicate that the choice is not known.</p>

      <P>The usual practice is to set the choice to null when the form
      is cleared, then to set it to the value read from the database
      when the field is read.  This sets both <CODE>choice</code> and
      <CODE>priorChoice</code> first to null and then to the value
      from the database.</p>

      <P>When the choice is updated from the form,
      <CODE>setDirtyChoice</code> is used to set the dirty bit if the
      value from the form differs from the value of
      <CODE>choice</code> at the time.  Thus, the value of
      <CODE>choice</code> and <CODE>priorChoice</code> differ as
      required by the <CODE>ChangeLog</code>.
  @param choice is set as the value of the choice field even if it is
  null.
  @see asst.dbase.ChangeLog */
  public void setChoice(String choice);

  /** Set the choice from a string and set the dirty bit if the value
      changes.  If both the current value and the new value are either
      null or the empty string, no change is made.  This is used when
      a value for the string is computed from form values rather than
      being set into the field from teh database or by setting
      defaults.
  @param choice new, and possibly different, value for the field.*/
  public void setDirtyChoice(String choice);

  /** Ask the form to record the current form value which has been
      entered for its field.  If the form returns a null value, the
      field was not mentioned in the form so the form has no effect on
      this selector object.  If the field returns a different value,
      however, the dirty bit is set to indicate that the field must be
      written to the database.  There is one exception - if the field
      has a null value and the form returns an empty string, this is
      not regarded as a change.
  @param request from the form which may have a parameter whose name
  matches the name of this object, in which case the parameter value
  replaces the current choice.*/
  public void setChoice(HttpServletRequest request);

  /** Sets are used for such data as permissions which are not always
   * visible to all users.  There is a problem with .html checkboxes
   * in that a checkbox which is checked returns <CODE>On</code> as
   * its value whereas a checkbox which is not checked returns
   * <CODE>null</code>, which is the same return as a checkbox which
   * is not in the form at all.  Thus, the <CODE>request</code> alone
   * does not provide enough information to determine the difference
   * between a checkbox which is not set and a checkbox which does not
   * appear the form at all.</p>

   * <P>This method sets a flag which indicates whether the set has
   * been put into the form or not.  If the checkbox appears in the
   * form, the flag must be set <CODE>true</code> <B>before</b> the
   * object is asked to read its value from the <CODE>request</code>
   * which is passed from the form.</p>

   * <P>This flag is ignored for radio sets because it is possible to
   * tell whether a set of radio buttons appears in the form and leave
   * the set value unchanged if the buttons are not in the form.  This
   * method is provided for both radio sets and for checkbox sets so
   * that a form which uses a

   {@link asst.dbase.SelectorCheckboxSet}

   * can set this flag and then be changed to a {@link
   * asst.dbase.SelectorRadioSet} without other code changes and
   * similarly for {@link asst.dbase.SelectorCheckboxSetMember} and
   * {@link asst.dbase.SelectorRadioSetMember}.</p>

   * <P>Supose,for example that a form either displays a field or does
   * not depending on user permissions. In such a case, the .jsp code
   * could be:

<PRE><CODE>&lt;%
boolean maySeeField;
...
maySeeField = PermissionCheck( ,,, );
...
fieldObject.setIsInForm(maySeeField);
...
fieldObject.setChoice(request);
...
if (maySeeField) { %&gt;
<TR><TD align=right>&lt;%=fieldObject.getPrettyName%&gt;</td>
<td>&lt;%=fieldObject.getHTML()%&gt;</td></tr>
</code></pre>

The boolean <CODE>maySeeField</code> determines whether the field
appears in the generated .html and also tells the object whether to
expect a value to be found in the <code>request</code>,

   * @see asst.dbase.SelectorRadioSet
   * @see asst.dbase.SelectorRadioSetMember
   * @see asst.dbase.SelectorCheckboxSet
   * @see asst.dbase.SelectorCheckboxSetMember
   @param is specifies whether the set or set member has been
   displayed in the form or not.*/
  public void setIsInForm(boolean is);

  /** Return the flag which tells whether the set is in the form or
   * not, the purpose of this flag is explained in {@link
   * #setIsInForm(boolean is) setIsInForm}*/
  public boolean getIsInForm();

  /** When a field value is turned into a search string, the SQL
   * operator is = if the choice string does not contail a wild card
   * and <code>like</clde> if it does contain an SQL wild card.  If
   * this flag is true, the choice string is wrapped in wild cards and
   * <code>like</code> is used even if the field does not contain any
   * wild cards.  */
  public void setFreeWildcard(boolean is);

  /** When a field value is turned into a search string, the SQL
   * operator is = if the choice string does not contail a wild card
   * and <code>like</clde> if it does contain an SQL wild card.  If
   * this flag is true, the choice string is wrapped in wild cards and
   * <code>like</code> is used even if the field does not contain any
   * wild cards.  */
  public boolean getFreeWildcard();

  /** Indicate whether a choice has been made or not, that is, the
      current <CODE>choice</code> is non-null and non-empty.  If the
      choice is null, there has been no choice.  An empty string is
      not a known choice even though the user may have chosen the
      empty string.  The empty string is written to the database,
      however, if the <CODE>dirtyFlag</code> is set.*/
  public boolean getKnowsChoice();

  /** Get the current choice, always returning "" rather than null so
      that the choice may be compared with other strings without
      causing a null pointer exception.
  @return current field value, returns the empty string if the choice
  is null*/
  public String getChoice();

  /** Get the previous choice, always returning "" rather than null so
      that the choice may be compared with other strings without
      causing a null pointer exception.
  @return prior field value, returns the empty string if the prior
  value is null*/
  public String getPriorChoice();

  /** Get the current value as a text field which can be displayed in
      plain uneditable .html text as opposed to a .html editable
      field.  This differs from getHTML() in that the result may be
      put into a .html page as opposed to a form.
  @return current choice, returns the empty string if the choice is null*/
  public String getChoiceAsText();

  /** In some cases, it is desirable to be able to display a selector
   * as either the .html to produce an editable field or as a fixed
   * uneditable string depending on its internal state.  Selectors
   * have a boolean <CODE>isInForm</code> which is normally used to
   * indicate whether a checkbox is displayed in the form in which
   * case a value of <CODE>null</code> means that the checkbox is not
   * checked, or whether it is not displayed in which case the value
   * is always <CODE>null</code>.  This method uses the
   * <CODE>isInForm</code> boolean to determine whether the display a
   * selectable field or a fixed field.  If <CODE>isInForm</code> is
   * set, meaning that the selector is in the field, variable code is
   * generated, otherwise, a fixed, uneditable field is generated.*/
  public String getEditableOrFixed();

  /** Set one character of the choice string.  This is used when
   * manipulating set fields directly rather than by using one of the
   * SetMember classes such as {@link
   * asst.dbase.SelectorRadioSetMember}
   @param which tells the index of the character to set, the choice
   string is extended by 'n' characters if it is not long enough
   @param ch is the character to store at the location specified by
   <code>which</code>, usually 'Y' or 'n' */
  public void setCharacterOfChoice(int which, char ch);

   /** Test one character of the choice string.  This is used when
   * manipulating set fields directly rather than by using one of the
   * SetMember classes such as {@link
   * asst.dbase.SelectorRadioSetMember}
   @param which tells the index of the character to test, the choice
   string is extended by 'n' characters if it is not long enough.
   @return true if the selected character == 'Y' */
  public boolean testCharacterOfChoice(int which);

 /** Get the value of the flag which tells whether any changes to
      this field should be automatically logged.
  @see asst.dbase.ChangeLog */
  public boolean getIsLogged();

  /** Set the value of the flag which tells whether changes to the
   * field should be logged.
   @see asst.dbase.ChangeLog */
  public void setIsLogged(boolean log);

  /** Tell whether the field value has been changed by the form. */
  public boolean getDirtyFlag();

  /** Set the flag which tells whether the field has been changed by
      the form. */
  public void setDirtyFlag(boolean dirty);

  /** Tell whether the field value is an error. */
  public boolean getErrorFlag();

  /** Set the flag which tells whether the field value has been
   * checked and found to be in error.  This flag is cleared when the
   * field is read from the database. */
  public void setErrorFlag(boolean error);

  /** Set the error string.  This can be done at instantiation and
   * ignored because the routine which returns the error string
   * returns the empty string if there is no error.
   @param errorMessage */
   public void setErrorString(String errorMessage);

  /** Get the error string.  This returns the empty string if the
   * error flag is not set.*/
  public String getErrorString();

  /**
   * A field may be set to be read-only in which case, requesting the
   * .html returns uneditable text instead.*/
  public void setReadOnly(boolean readonly);

  /**
   * A field may be set to be read-only in which case, requesting the
   * .html returns uneditable text instead.*/
  public boolean getReadOnly();

  /** SelectorManagers are usually grouped into arrays to define a
      form which corresponds to one row of a database table.  Such
      arrays of fields are occasionally grouped into arrays to
      represent many database table rows simultaneously.  In this
      case, each row needs a different prefix for field names so that
      the same field from different rows can be distinguished.  This
      method specifies a prefix which differentiates the rows; the
      prefix defaults to the empty string.
  @param prefix string used to differentiate the field name from other
  instances in the same .html form*/
  public void setFieldNamePrefix(String prefix);

  /** SelectorManagers are usually grouped into arrays to define a
      form which corresponds to one row of a database table.  Such
      arrays of fields are occasionally grouped into arrays to
      represent many database table rows simultaneously.  In this
      case, each row needs a different prefix for field names so that
      the same field from different rows can be distinguished in the form.
      @return the current field name prefix.*/
  public String getFieldNamePrefix();

  /** Set the flag which tells that the field is not stored as a
      character in its database column.  This flag is also used to
      check the field for numeric data on submit and to avoid wrapping
      the data in SQL single quotes during SQL updates.  This method
      duplicates {@link #setIsNumeric(boolean unique) setIsNumeric}.
  @param numeric tell whether the field is numeric or not*/
  public void setNumericFlag(boolean numeric);

  /** Set the flag which tells that the field is not stored as a
      character in its database column.  This flag is also used to
      check the field for numeric data on submit and to avoid wrapping
      the data in SQL single quotes during SQL updates.  This method
      duplicates {@link #setNumericFlag(boolean unique)
      setNumericFlag} for user convenience.
  @param numeric tell whether the field is numeric or not */
  public void setIsNumeric(boolean numeric);

  /** Set the flag which indicates that the field must be unique in
      its database table.  If the choice is either null or the empty
      string, the uniqueness check is not performed, it is assumed
      that opther error checking routines will issue error messages if
      the field is required to be non-empty.  This method duplicates
      {@link #setIsUnique(boolean unique) setIsUnique}.
  @param unique tell whether the field is unique or not*/
  public void setUniqueFlag(boolean unique);

  /** Set the flag which indicates that the field must be unique in
      its database table.  If the choice is either null or the empty
      string, the uniqueness check is not performed, it is assumed
      that opther error checking routines will issue error messages if
      the field is required to be non-empty.  This method duplicates
      {@link #setUniqueFlag(boolean unique) setUniqueFlag} for user
      convenience.
  @param unique tell whether the field is unique or not*/
  public void setIsUnique(boolean unique);

  /** Get the flag which indicates that the field must be unique in
      its database table.  */
  public boolean getUniqueFlag();

  /** Determine whether the field is valid or not.  This depends on
   * the field type.  In general this method returns <CODE>true</code>
   * if the choice string is not <CODE>null</code>, but other classes
   * such as the Password class do further validation.*/
  public boolean getIsValid();

  /** Get the flag which tells that the field is not stored as a
      character in its database column.  This flag can also used to
      check the field for numeric data on submit. */
  public boolean getNumericFlag();

  /** Set the field name; this is always put into the .html.  Spaces
      are translated to underscores so that the field name may be
      referenced by Java Script.
   @param name object name to be used to identify the HTML field.*/
  public void setName(String name);

  /** Get the .html object name; this method is used when generating
      the .html; either this or the pretty version of the name may be
      put into the .html. */
  public String getName();

  /** Get the human-readable field name; this may be put into the
      .html.  In the short term, this simply replaces underscores with
      spaces. */
  public String getPrettyName();

  /** Set the Selector table.  This deterimines which database table is
      associated with the object.  Depending on the object which
      implements this interface, the table may or may not be used to
      develop an initial selection list.  The table may be left NULL
      if the Selector or field is not associated with a table.
  @param table name of the SQL table in which this field / column is
  stored.*/
  public void setTable(String table);

  /** Get the name of the database table whci is associated with this
      object.   */
  public String getTable();

  /** Set the Selector column.  This determines the database column
      which is associated wtih the object.  Distinct values of the
      column may be used to generate an initial selection list or the
      column may be used to validate user input.  The column may be
      left NULL if the Selector or Field is not associated with a
      column.
  @param column name of the SQL colun in which this field is stored.
  The SQL data type used is indicated by the state of the numeric
  flag.*/
  public void setColumn(String column);

  /** Get the name of the database table column which holds data
      associated with this field or selector. */
  public String getColumn();

  /** Set a string to be put into the generated .html after the field
      value and before any closing bracket.  This is used to specify
      Java Script functions which apply to the object or .html
      modifiers such as table widths.  It is up to the caller to
      ensure that this parameter is compatible with the rest of the
      generated .html.  It can be used for JavaScript addenda such as
      <CODE>onClick()</code> or <CODE>onChange()</code>.
  @param param extra HTML parameters to be added to the normal field
  definition.*/
  public void setAddParam(String param);

  /** Get the string which is added to the generated .html.*/
  public String getAddParam();
}
