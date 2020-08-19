/* @name SessionDestroyNotifyListener.java

Handle cases where sessions are invalidated for any reason.

    Copyright (c) 2002 by Advanced Systems and Software Technologies.
    All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: SessionDestroyNotifyListener.java,v $
    Revision 1.2  2002/08/13 00:31:26  zonediet
    documentation

    Revision 1.1  2002/08/11 14:10:35  zonediet
    initial upload
<br>

*/

package asst.servlet;

/**
 * This interface is to be used with an instance of the SessionExpiry
 * class.  An object which implements this interface is passed to the
 * SessionExpiry constructor.  Each SessionExpiry object is attached
 * to the running session such that the SessionExpiry object is
 * notified when the session expires.  At that point, the
 * SessionExpiry object calls the the SessionHasExpired method in the
 * object which was passed to the constructor.

 * @author money
 * @version %I%, %G%
 * @since

 * @see asst.servlet.SessionExpiry
 */

public interface SessionDestroyNotifyListener {

  /** Called when the session expires and the object
   * has not been removed from the SessionExpiry object.  It is up
   * to this method to do whatever is required to handle the session
   * expiring. */
  public void theSessionHasExpired();

  /** Called when the session is being canceled in a
   * manner such that its actions should not be taken such as when
   * someone logs out normally.  It is up to this method to change the
   * internal object state such that the cleanup action does not
   * occur.  The meaning of this action depends on the application.*/
  public void cancelTimeout();
}
