/* @name ConnData.java

   Copyright (c) 2002-2013 Advanced Systems and Software Technologies, LLC
   (All Rights Reserved)

-------- Licensed Software Proprietary Information Notice -------------

This software is a working embodiment of certain trade secrets of
AS&ST.  The software is licensed only for the day-to-day business use
of the licensee.  Use of this software for reverse engineering,
decompilation, use as a guide for the design of a competitive product,
or any other use not for day-to-day business use is strictly
prohibited.

All screens and their formats, color combinations, layouts, and
organization are proprietary to and copyrighted by AS&ST.

All rights are reserved.

Authorized AS&ST customer use of this software is subject to the terms
and conditions of the software license executed between Customer and
AS&ST.

------------------------------------------------------------------------

*/

package asst.dbcommon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** Store database connection information for passing around.  It is
 * the caller's responsibility to make sure that the final cleanup
 * method is called.
 *
 * @author Material Gain
 * @since 2017 01
 */

public class ConnData {

  static final long serialVersionUID = 1;

  /** Database connection */
  public Connection conn = null;
  /** Statement associated with the connection. */
  public Statement  stmt = null;
  /** Result set associated with the statement. */
  public ResultSet  res  = null;
  /** SQL utilities object associated with the connection. */
  public PUTs       puts;
  /** Name of the database pool from which the connection was borrowed. */
  public String     poolName;
  long bailout;

  /** Obligatory constructor.*/
  public ConnData() { /* */ }

  /**
   * Open a database connection
   * @param driver class name of the driver
   * @param url where the database is located
   * @param user user name
   * @param pass password
   * @throws Exception when things go wrong
   */
  public void createDBAccess(String driver, String url, String user,
      String pass) throws Exception {
    // Load the JDBC driver
    Class.forName(driver);
    // DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    conn = DriverManager.getConnection(url, user, pass);
    puts = new PUTs(); // Separate instances for timeouts
    stmt = puts.makeStatement(conn);
  }

  /** Close all open SQL objects.*/
  public void cleanup() {
    try {
      res.close();
    } catch (Exception e) { /* */ }
    try {
      stmt.close();
    } catch (Exception e) { /* */ }
    try {
      conn.close();
    } catch (Exception e) { /* */ }
    conn = null;
    stmt = null;
    res  = null;
  }
}
