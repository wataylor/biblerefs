/* @name DebugUtils.java

Parameter and session debugging utilities

    Copyright (c) 2001 by AdvancedSystems and Software Technologies.  All Rights Reserved<br>

    Under revision by: $Locker:  $<br>
    Change Log:<br>
    $Log: DebugUtils.java,v $
    Revision 1.4  2006/01/19 16:39:22  asst
    enum is a keyword

    Revision 1.3  2005/03/15 04:10:36  asst
    make tables printable

    Revision 1.2  2002/10/06 01:36:46  asst
    first upload

    Revision 1.1.1.1  2002/04/09 03:21:02  zonediet
    first import

*/

package asst.debug;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author money
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class DebugUtils {

  public DebugUtils() {
  }

  static final String table = "<table border=\"1\" cellpadding=\"2\">";
  static final String tr = "<tr><td>";
  static final String trC = "</td></tr>";
  static final String tableC = trC + "</table>";
  static final String th = "<th>Name</th><th>Value</th>";

/**
   Generate a table of various information about the current session.

   It is invoked thus:

   <pre>
   makeSessionTable(request)
   </pre>
*/

  public static StringBuffer makeSessionTable(HttpServletRequest request) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;
    String name;
    String value;
    HttpSession session;
    Object obj;

    sb.append(table);
    sb.append("<tr><th colspan=2>HTTPSession getAttributeNames</th></tr>\n");

    try {
      session = request.getSession();
      enumV = session.getAttributeNames();
      if (enumV != null) {
	sb.append("<tr><td colspan=2>" + enumV.toString() + "</td></tr>\n");
	while (enumV.hasMoreElements()) {
	  name = (String)enumV.nextElement();
	  try {
	    obj = session.getAttribute(name);
	    if (obj instanceof String) { value = (String)obj; }
	    else                       { value = obj.toString(); }
	  } catch (Exception e) {
	    value = e.toString();
	  }
	  if (value == null) { value = "null"; }
	  sb.append("<tr><td>" + name + "</td>" + "<td>" + value + "</td></tr>\n");
	}
      }
    } catch (Exception e) {
      sb.append("<tr><td colspan=2>" + e.toString() + "</td></tr>\n");
    }

    sb.append(tableC);

    try {
      session = request.getSession();
      sb.append("Id " + session.getId() + " New " + session.isNew() +
		" Timeout " + session.getMaxInactiveInterval() +
		"<br>Created " +
		java.text.DateFormat.getInstance().format(new java.util.Date(session.getCreationTime())) +
		" Displayed " +
		java.text.DateFormat.getInstance().format(new java.util.Date()) +
		" Accessed " +
		java.text.DateFormat.getInstance().format(new java.util.Date(session.getLastAccessedTime())) +
		"<br>\n");
    } catch (Exception e) {
      sb.append("session exception" + e.toString() + "<br>\n");
    }
    return sb;
  }

/**
   Generate information about request headers.  It is invoked thus:

   <pre>
   makeRequestHeaderTable(request)
   </pre>
*/

  public static StringBuffer makeRequestHeaderTable(HttpServletRequest request) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;
    String name;
    String value;

    sb.append(table);
    sb.append("<tr><th colspan=2>HTTPServletRequest getHeaderNames</th></tr>\n");
    enumV = request.getHeaderNames();
    while (enumV.hasMoreElements()) {
      name = (String) enumV.nextElement();
      value = request.getHeader(name);
      if (value == null) { value = "null"; }
      sb.append("<tr><td>" + name + "</td><td>" + value + "</td></tr>\n");
    }

    sb.append("<tr><th>Method</th><th>Result</th></tr>\n");
    sb.append("<tr><td>getAuthType()</td><td>" +
	      request.getAuthType() + "</td></tr>\n");
    sb.append("<tr><td>getContentLength()</td><td>" +
	      request.getContentLength() + "</td></tr>\n");
    sb.append("<tr><td>getContextPath()</td><td>" +
	      request.getContextPath() + "</td></tr>\n");
    sb.append("<tr><td>getContentType()</td><td>" +
	      request.getContentType() + "</td></tr>\n");
    sb.append("<tr><td>getCharacterEncoding()</td><td>" +
	      request.getCharacterEncoding() + "</td></tr>\n");
    sb.append("<tr><td>getMethod()</td><td>"+request.getMethod()+"</td></tr>\n");
    sb.append("<tr><td>getPathInfo()</td><td>" +
	      request.getPathInfo() + "</td></tr>\n");
    sb.append("<tr><td>getPathTranslated()</td><td>" +
	      request.getPathTranslated() + "</td></tr>\n");
    sb.append("<tr><td>getQueryString()</td><td>" +
	      request.getQueryString() + "</td></tr>\n");
    sb.append("<tr><td>getRemoteAddr()</td><td>" +
	      request.getRemoteAddr() + "</td></tr>\n");
    sb.append("<tr><td>getRemoteHost()</td><td>" +
	      request.getRemoteHost() + "</td></tr>\n");
    sb.append("<tr><td>getRemoteUser()</td><td>" +
	      request.getRemoteUser() + "</td></tr>\n");
    sb.append("<tr><td>getRequestURI()</td><td>" +
	      request.getRequestURI() + "</td></tr>\n");
    sb.append("<tr><td>getServerName()</td><td>" +
	      request.getServerName() + "</td></tr>\n");
    sb.append("<tr><td>getServerPort()</td><td>" +
	      request.getServerPort() + "</td></tr>\n");
    sb.append("<tr><td>getProtocol()</td><td>" +
	      request.getProtocol() + "</td></tr>\n");

    sb.append(tableC);
    return sb;
  }

/**
   Generate information about all parameters of the form as well as any
   cookies which may be available.  This file is intended to be included
   in .jsp pages to help debug entry form field processing.  It must be
   included after the table constants.  It is invoked thus:

   <pre>
   makeFormParamTable(request)
   </pre> */

  public static StringBuffer makeFormParamTable(HttpServletRequest request) {
    StringBuffer sb = new StringBuffer();
    Enumeration enumV;
    String name;
    String value;
    Object obj;
    String [] selections;
    /**/

    sb.append(table);
    sb.append("<tr><th>request.getContentType()</th><td>" +
	      request.getContentType() + "</td></tr>\n");
    sb.append("<tr><th>request.getContentLength()</th><td>" +
	      request.getContentLength() + "</td></tr>\n");

    sb.append("<tr><th colspan=2>HTTPServletRequest getParameterNames</th></tr>\n");
    enumV = request.getParameterNames();
    while (enumV.hasMoreElements()) {
      name = (String) enumV.nextElement();
      selections = request.getParameterValues(name); // May be multi-valued
      if (selections == null) {
	value = null;
      } else {
	if (selections.length < 2) {
	  value = selections[0];
	} else {
	  value = "";
	  for (int axi = 0; axi < selections.length; axi++) {
	    value=value+((("").equals(value))?"":",")+selections[axi];
	  }
	}
      }
      if (value == null) { value = "null"; }
      if ("".equals(value)) { value = "empty string"; }
      sb.append("<tr><td>" + name + "</td>" + "<td>" + value + "</td></tr>\n");
    }

    sb.append("<tr><th colspan=2>HTTPServletRequest getAttributeNames</th></tr>\n");
    enumV = request.getAttributeNames();
    while (enumV.hasMoreElements()) {
      name = (String) enumV.nextElement();
      obj = request.getAttribute(name);
      if (obj == null) {
	value = "null";
      } else {
	value = obj.toString();
      }
      sb.append("<tr><td>" + name + "</td>" + "<td>" + value + "</td></tr>\n");
    }

    sb.append(tableC);

    Cookie[] aCookies = request.getCookies();
    if ((aCookies != null) && (aCookies.length > 0)) {
      sb.append(table);
      sb.append("<tr><th>Cookie</th><th>Value</th></tr>\n");
      for (int i = 0; i < aCookies.length; i++) {
	sb.append("<tr><td>" + aCookies[i].getName()  + "</td>" +
		  "<td>" + aCookies[i].getValue() + "</td></tr>\n");
      }
      sb.append(tableC);
    } else {
      sb.append("No cookies for this session<BR>");
    }
    return sb;
  }
}
