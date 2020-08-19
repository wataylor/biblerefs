/* @name ReferenceQuestion.java

Data for a scripture reference question

    Copyright (c) 2003 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker:  $
    Change Log:
    $Log: ReferenceQuestion.java,v $
    Revision 1.1  2008/09/13 23:29:21  peren
    for .war file, add bible cref Java


*/

package asst.biblerefs;

/**
 * Store data for a scripture reference question.  The verse text and
 * its scripture references are stored in <CODE>text</code> and
 * <CODE>answer</code>, the wrong answers are listed in
 * <CODE>errata</code>.  The number of wrong answers displayed is
 * determined by the current difficulty level.

 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class ReferenceQuestion {

  /** Obligatory constructor.*/
  public ReferenceQuestion() {
  }

  /** Detailed constructor.
   @param text string which is displayed as a question
   @param errata list of wrong answers, some of which are displayed depnding on difficulty level
   @param answer which is mixed into the number of wrong answers which
  are displayed.*/
  public ReferenceQuestion(String text, String[] errata, String answer) {
    this.text   = text;
    this.errata = errata;
    this.answer = answer;
  }

  /** Stores one of the question type constants defined in QuesDB.
   * The type determines how wrong answers are handled, how the
   * correct answer is displayed, and how questions are scored.*/
  public short type;

  /** Stores the database row ID for the question. */
  public short id;

  /** Used for Extraordinary Christians of the Past and Wiles of the
   * Devil, null or empty for other question types.  If non-null and
   * non-empty, the title is displayed in a label above the text of
   * the question.*/
  public String title;

  /** Text of the question.  Many verse identification qustions
   * display partial verses so this cannot be done by references.
   * This becomes rather long for ECP questions.*/
  public String text;

   /** Footer string used for Virtue / Vice questions, empty for other
    * question types.*/
  public String footer;

 /** Array of wrong answers, not all of them are displayed depending
   * on the character's difficulty level.*/
  public String[] errata;

  /** The correct answer.*/
  public String answer;
}
