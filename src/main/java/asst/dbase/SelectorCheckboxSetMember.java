/* @name SelectorCheckboxSetMember.java

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SelectorCheckboxSetMember.java,v $
    Revision 1.3  2005/06/23 20:30:18  asst
    can get value of check box choice

    Revision 1.2  2002/10/22 02:52:47  asst
    Simplified state testing

    Revision 1.1  2002/10/06 01:36:46  asst
    first upload

*/

package asst.dbase;

import javax.servlet.http.HttpServletRequest;

import asst.dbase.SelectorFieldPreload;
import asst.dbase.StringSetUtils;

/**
 * Implement one member of a "Set" which is stored as a string.  Each
 * character of the string represents a set member; a value of 'Y'
 * indicates that the set contains the member associated with that
 * character of the string and any other value indicates that it does
 * not.  Any set member whose subscript extends beyond the length of
 * the string is considered to be absent from the set.  The set member
 * is associated with a checkbox whose checked state is associated
 * with 'Y' in its set character and whose unchecked state is
 * associated with 'n'.</p>

 * <P>As defined, the set may include any combination of members.  It
 * is up to the caller to restrict the set to one member if that is
 * required.  In this case, however, it might be better to use

  {@link asst.dbase.SelectorRadioSet} which allows only one member in
  the set.</p>

 * <P>A human-readable string which labels the meanings of the set
 * member may be specified if the field name is not descriptive
 * enough.</p>

 * <P>For example, a <CODE>fieldMgr</code> array which defines a
 * <CODE>SelectorCheckboxSetMember</code> which edits a set
 * might be declared thus:

<CODE><PRE>
SelectorCheckboxSetMember checkboxMember4;
SelectorCheckboxSetMember checkboxMember5;
SelectorCheckboxSetMember checkboxMember6;
SelectorFieldPreload holdsMemberSet;

String[] radioSetSel = {"", "", "", "", "May Enter|or May Not|"};

fieldMgrs = new SelectorManager[8];
i = 0;
...
fieldMgrs[i++] =
(holdsMemberSet = new SelectorFieldPreload("Holds_Set", jtbl, "MemberSet"));
...
i++;  // Create blank fieldManager array member
fieldMgrs[i++] =
(checkboxMember4 = new SelectorCheckboxSetMember("Likes_Butter", 4, holdsMemberSet));
checkboxMember4.setIsInForm(true);
fieldMgrs[i++] =
(checkboxMember5 = new SelectorCheckboxSetMember("checkboxMember5", 5, holdsMemberSet));
checkboxMember5.setIsInForm(true);
checkboxMember5.setSelections(radioSetSel);

fieldMgrs[i++] =
(checkboxMember6 = new SelectorCheckboxSetMember("Set Member 6", 6, holdsMemberSet));
checkboxMember6.setIsInForm(true);
checkboxMember6.setBeforeYesString("BY");
checkboxMember6.setAfterYesString("AY");
checkboxMember6.setBeforeNoString("BN");
checkboxMember6.setAfterNoString("AN");
...
</pre></code>

<B>Note:</b> The extra '|' character at the end of the 5th string of
<CODE>radioSetSel</code> sets the <CODE>beforeNoString</code> to the
empty string which overrides the default value of "No."</p>

<P><B>Note:</b> <CODE>holdsMemberSet</code> must be instantiated before
any of the CheckboxSetMembers so that they can be passed a pointer to the
field whose choice string holds their set.  Since these set member
objects are never written to or from the database, they should be
defined AFTER the blank member of the fieldMgrs array; the extra i++
above creates a blank in the array.  <CODE>holdsMemberSet</code> is
how the set is written and read so it <B>must</b> appear <B>before</b>
the null member of the fieldMgrs array.</p>

<P>Given these definitions, the .jsp code

<CODE><PRE>
&lt;TR&gt;&lt;TD align=center&gt;&lt;%=checkboxMember4.getHTML()%&gt;&lt;/td&gt;&lt;/tr&gt;
&lt;TR&gt;&lt;TD align=center&gt;&lt;%=checkboxMember5.getHTML()%&gt;&lt;/td&gt;&lt;/tr&gt;
&lt;TR&gt;&lt;TD align=center&gt;&lt;%=checkboxMember6.getHTML()%&gt;&lt;/td&gt;&lt;/tr&gt;
&lt;TR&gt;&lt;TD align=center&gt;&lt;%=holdsMemberSet.getPrettyName() +
   " " + holdsMemberSet.getChoiceAsText()%&gt;&lt;/td&gt;&lt;/tr&gt;
</pre></code>

produces the following .html given that the selected row has a value
of '????YnY' in column "MemberSet":</p>

<TABLE>
<TR><TD align=center>Yes<input type="checkbox" name="Likes_Butter4" checked>No</td></tr>
<TR><TD align=center>May Enter<input type="checkbox" name="checkboxMember55">or May Not</td></tr>
<TR><TD align=center>BY<input type="checkbox" name="Set_Member_66" checked>AYBNAN</td></tr>
</table>

<B>Note:</b> The first four columns of the MemberSet choice string are
ignored because <CODE>checkboxMember4</code> specifies that it refers
to the character in string position 4.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see asst.dbase.SelectorRadioPreload
 * @see asst.dbase.SelectorCheckboxSet */

public class SelectorCheckboxSetMember extends SelectorRadioSetMember {

  /** Obligatory constructor.*/
  public SelectorCheckboxSetMember() {
  }

  /** Constructor that sets the selector name, set member index, and
      the field which stores the set.
      @param name from which the form field name is derived.when .html
      is generated.  The name is also used as a human-readable label
      unless another label is supplied.  Spaces in the name are turned
      to underscores for compatibilty with Java Script.
      @param setField identifies a selector manager object which
      stores the set of which this object is a member.
      @param memberIndex selects the 'Y' or 'n' character in the set
      which stores this member.
  */
  public SelectorCheckboxSetMember(String name, int memberIndex,
				SelectorManager setField) {
    this.setName(name);
    this.setField = setField;
    this.memberIndex = memberIndex;
  }

  /** In the SelectorRadioSetMember class, this method generates the
   * "Yes" half of a pair of radio buttons but a checkbox cannot be
   * split so this method returns the checkbox.
   @return string that defines the checkbox. */
  public String getYes() {
    return this.getYesNo();
  }

  /** In the SelectorRadioSetMember class, this method generates the
   * "No" half of a pair of radio buttons but a checkbox cannot be
   * split so this method returns an empty string.
   @return string that defines the radio button*/
  public String getNo() {
    return ES;
  }

  /** Express the set member as one stand-alone .html checkbox without
   * any table constructs or human-readable label.  The name tells the
   * form how to return the checkbox state.  Uses
   * <CODE>addParam</code> to modify the checkbox.  This method is
   * supplied for compatibility with

   {@link asst.dbase.SelectorCheckboxSet#getYesNo(int i)
   SelectorCheckboxSet.getYesNo}

   * so that a given field can be changed from one to the other by
   * changing its type declaration without changing any code.  Uses
   * <CODE>addParam</code> to modify each of the pair of radio
   * buttons.
   @param i selects which pair of radio buttons to generate, this
   parameter is ignored because the object knows its set member
   number.
   @return string which expresses the two radio buttons in .html*/

  public String getYesNo(int i) {
    return this.getYesNo();
  }

   /** Express the set member as one named .html checkbox without any
   * table constructs or human-readable label.  The name tells the
   * form how to return the checkbox state.  Uses
   * <CODE>addParam</code> to modify the checkbox. */

  public String getYesNo() {
    String name = this.fieldNamePrefix + this.name + this.memberIndex;
    char ch;
    boolean in;
    StringBuffer sb = new StringBuffer();
    /**/

    in = this.setField.testCharacterOfChoice(this.memberIndex);

    sb.append("<input type=\"checkbox\" name=\"" + name + "\"");
    if (in) sb.append(" checked");
    sb.append(this.addParam + ">");

    return sb.toString();
  }

  /** Ask the form to record the current form values for this set
   * member.  If a set member is put into the same form more than
   * once, it is not clear which value wins.

<P><B>Unfortunately</b>, it is not possible to distinguish between a
checkbox which is in the form and not checked and a checkbox which is
not in the form at all, both cases return
<CODE>null</code>.  For this reason, it is necessary to record whether
the form is displayed in cases where the set may or may not be
displayed in an editor depending on user priviliges.  Thus, this
method does nothing unless <CODE>isInForm</code> is set.

   @param request from the form which may have parameters whose names
   matches the names of the radio button pairs, in which case the new
   set member value replaces the current choice.*/
  public void setChoice(HttpServletRequest request) {
    String prefix = this.fieldNamePrefix + this.name;
    char ch;			// One character from the string
    /**/

    if (!isInForm) return;

    if (request.getParameter(prefix + this.memberIndex) != null) {
      ch = 'Y';
    } else {
      ch = 'n';
    }
    this.setField.setCharacterOfChoice(this.memberIndex, ch);
  }

  /**
   * Set the choice as recorded in the set.  Specifying anything other
   * than the string Y sets the choice to n.  The normal way to set
   * the choice is by manipulating the value of the underlying string
   * field using the string set utilities. */
  public void setChoice(String choice) {
    if ((choice != null) && "Y".equals(choice)) {
      this.setField.setCharacterOfChoice(this.memberIndex, 'Y');
    } else {
      this.setField.setCharacterOfChoice(this.memberIndex, 'n');
    }
  }

  /**
   * Return the choice as recorded in the set as either Y or n
   * depending on the value in the string.  If the value is not set,
   * return n.  The normal way to get the choice is by interrogating
   * the value in the underlying string field using the string set
   * utilities. */
  public String getChoice() {
    if (this.setField.testCharacterOfChoice(this.memberIndex)) {
      return "Y";
    }
    return "n";
  }
}
