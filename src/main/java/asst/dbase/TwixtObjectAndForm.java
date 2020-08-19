/* @name TwixtObjectAndForm.java

Utilities which translate between object arrays and forms

    Copyright (c) 2001 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: TwixtObjectAndForm.java,v $
    Revision 1.4  2002/08/18 03:38:08  asst
    documentation

    Revision 1.3  2002/07/20 02:09:45  zonediet
    find table-friendly constructor

    Revision 1.2  2002/07/13 03:07:52  zonediet
    documentation

    Revision 1.1  2002/04/23 20:54:35  zonediet
    Object and Form

*/

package asst.dbase;

import java.util.Vector;

import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.RuntimeException;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import asst.dbase.SelectorManager;

/**
 * Methods to convert between a two-dimensional entry form consisting
 * of an array of arrays of selector managers and an array of objects,
 * and vice versa.  The selector managers are intended to be displayed
 * in an entry form; initial values are inserted into the form from
 * the database using <code>ObjectToForm</code>.  This permits the
 * values to be manipulated.  When editing is complete,
 * <code>FormToTable</code> is called to update the associated
 * database row.</p>

 * <P>Selector managers generally specify a table name and a column
 * name; column names are specified individually for each field by the
 * constructor whereas table names must be known during instantiation
 * so that each selector object can be initialized properly.
 * Accordingly, the table name can be passed not only to the
 * <code>ClassManip</code> method which reads object instances from
 * the database but to the <code>TwixtObjectAndForm</code> method
 * which converts an array of objects to an array of field objects.</p>

 * <P>If the class has a constructor which takes one string parameter,
 * the table name is passed to this constructor, otherwise the default
 * constructor is used to create the objects.

 * @author Web Work
 * @version %I%, %G%
 * @since

 * @see asst.dbase.SelectorManager
 * @see asst.dbase.TwixtTableAndForm
 * @see asst.dbase.AnyFieldArray
 * @see asst.dbase.ClassManip */

public class TwixtObjectAndForm {

  /** No-argument constructor */
  public TwixtObjectAndForm() {
  }

  /** Find a field whose name matches the name passed in.  This would
      work faster as a hash map, but that's more trouble than it's
      worth for now.*/
  private static Field FindMatchingField(Field[] fieldArray, String name) {
    int i;
    /**/

    for (i=0; i<fieldArray.length; i++) {
      if (name.equals(fieldArray[i].getName())) { return fieldArray[i]; }
    }
    return null;
  }

  /**
   * Convert an array of objects into an array of form objects.  The
   * method works by matching field names between the form array
   * object and the database object, just as the database object is
   * loaded from the database table by matching field names with
   * column names.  It is important that field names NOT match
   * accidently.  For this reason, SQL column names and thus field
   * names in the database objects and in the field array objects are
   * defined with initial capital letters, thus differentiating them
   * from normal Java field names which start with an initial lower
   * case letter.

   * @param objects an array of objects whose public field names
   * match the names of the SelectorManager fields in the class whose
   * name is passed in.

   * @param className the name of the class which has an array of
   * SelectorManager fields whose names match the public field names
   * in the object array; it is usually an extension of
   * <code>AnyFieldArray</code>.  If this class has a constructor
   * which takes one string parameter and the table name is not null
   * or empty, the table name is passed to the constructor.

   * @param tableName name of the table to be passed to the field
   * array object so that the field array is able to associate the row
   * with a table.

   * @return a Vector of instances of className whose field values
   * have been initialized to match the field values of the
   * corresponding objects from the objects array.*/
  public static Vector ObjectToForm(Object[] objects, String className,
				    String tableName) {
    Class formClass;
    Field[] formFields;
    Field aFormField;
    SelectorManager aManager;
    Object newObject;		// Create new instances of className

    Class objectClass;
    Field[] objectFields;
    String objectFieldName;
    Object objectField;

    Constructor tableConstructor; // Constructor to set the table name
    Class[]  paramFind  = new Class[1]; // Find the table setting method
    Object[] paramInput = new Object[1]; // Pass table name to setting method

    Vector vec = new Vector();
    String mess;

    int i;			// Walk the object array
    int j;			// Walt the object field array
    /**/

    if ((className == null) || (objects == null)) {
      return null;
    }

    try {
      formClass = Class.forName(className); // Load the field class
      formFields = formClass.getFields();
    } catch (Exception e) {
      mess = "forName excpt on " + className + " " + e.toString();
      System.out.println(mess);
      return null;
    }

    tableConstructor = null;	// Cannot set the table name
    if ((tableName != null) && (tableName.length() > 0)) {
      try {
	paramFind[0] = tableName.getClass(); // It is a string
	// getDeclaredConstructor does not search super classes.
	if ( (tableConstructor = formClass.getConstructor(paramFind)) !=
	     null) {
	  paramInput[0] = (Object)tableName; // Pass an object array
	}
      } catch (NoSuchMethodException e) { // Cannot initialize the table name
      }
    }

    try {
      objectClass = objects[0].getClass(); // Load the object class
      objectFields = objectClass.getFields();
    } catch (Exception e) {
      mess = "classOf excpt " + e.toString();
      System.out.println(mess);
      return null;
    }

    for (i=0; i<objects.length; i++) {
      try {
	if (tableConstructor == null) {
	  newObject = formClass.newInstance(); // Create a new object
	} else {
	  // Pass the table name to the new object.
	  newObject = tableConstructor.newInstance(paramInput);
	}
      } catch (InstantiationException e) {
	mess = "Instantiation excpt " + className + " " +e.toString();
	System.out.println(mess);
	continue;
      } catch (IllegalAccessException e) {
	mess = "Class access excpt " + className + " " + e.toString();
	System.out.println(mess);
	continue;
      } catch (InvocationTargetException e) {
	mess = "Invocation target excpt " + className + " " + e.toString();
	System.out.println(mess);
	continue;
      }
      vec.add(newObject);
      for (j=0; j<objectFields.length; j++) {
	objectFieldName = objectFields[j].getName();
	if ( (aFormField = TwixtObjectAndForm.FindMatchingField(formFields, objectFieldName)) != null) {
	  // two fields match, must set choice in new object.
	  //if (i<=2) { System.out.println(j+" Matched " + objectFieldName); }
	  aManager = null;
	  try {
	  aManager = (SelectorManager) aFormField.get(newObject);
	  } catch (IllegalAccessException e) {
	    System.out.println("Getting new object field " + e.toString());
	  }
	  objectField = null;
	  try {
	  objectField = objectFields[j].get(objects[i]);
	  } catch (IllegalAccessException e) {
	    System.out.println("Getting object field " + e.toString());
	  }
	  try {
	    if (objectField instanceof String) {
	      // Strings can be used without change
	      aManager.setChoice((String)objectField);
	    } else {
	      // Anything other than a string can be converted to a string
	      aManager.setChoice(String.valueOf(objectFields[j].getInt(objects[i])));
	    }
	    aManager.setDirtyFlag(false); // Say that it has not been modified
	  } catch (IllegalAccessException e) {
	    System.out.println("Getting object field " + e.toString());
	  }
	}
      }
    }
    return vec;
  }
}
