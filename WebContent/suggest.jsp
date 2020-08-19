<!-- suggest.jsp

   Copyright (c) 2008 by Advanced Systems and Software Technologies.
   All Rights Reserved

-->
<%@page import="asst.biblerefs.RefSQLLookup,loadsql.LoadKJVTextVerses,asst.dbase.SQLUtilities,asst.dbase.DBProp" errorPage="runError.jsp"%>
<%!RefSQLLookup rsl;
String fromV;
String toV;
String fromRef;
String toRef;
String fromPassage;
String toPassage;
StringBuilder sb;
boolean doI;
boolean both;%><%
	if (rsl == null) { rsl = new RefSQLLookup(); rsl.setup(); }
if (sb  == null) { sb  = new StringBuilder(); } else { sb.setLength(0); }
DBProp.PropertyConnection(LoadKJVTextVerses.DATABASE_NAME);

doI  = (( (fromV = request.getParameter("do")) != null) && "y".equals(fromV));
both = (( (fromV = request.getParameter("bf")) != null) && "true".equals(fromV));

fromV = request.getParameter("fromV");
toV   = request.getParameter("toV");

if (doI) {
  fromRef = request.getRemoteAddr();
  SQLUtilities.AnyStatement("insert into CrefSug (FromIP,FromVerse,ToVerse,Bothways) values ('" + fromRef + "','" + fromV + "','" + toV + "','" +
	    (both ? "y" : "n") + "')");
  sb.append("Thank you for your suggestion! <button type=\"reset\" id=\"clsBtn\" name=\"clsBtn\" class=\"btnBlue cursorHand\" title=\"Clears the suggestion area.\" onClick=\"javascript:clearL(this)\">Clear Suggestion</button>");
} else {

  fromRef = rsl.findCanonicalReference(fromV, "");

  if (fromRef != null) {
    fromPassage = rsl.findAVerse(fromRef, false);
  }
  if (fromPassage == null) {
    sb.append("Could not find reference " + fromV);
  }

  toRef = rsl.findCanonicalReference(toV, "");

  if (toRef != null) {
    toPassage = rsl.findAVerse(toRef, false);
  }
  if (toPassage == null) {
    sb.append("Could not find reference " + toV);
  }

  if ((fromPassage != null) && (toPassage != null)) {

    sb.append("<P>Please confirm your suggestion that there should be a link from</p><p>" + fromPassage + " <b>" + fromRef + "</b></p><p>to</p><P>" + toPassage + " <b>" + toRef + "</b></p><P>Check this box<input type=\"checkbox\" id=\"two\" name\"two\"> if there should also be a link from <b>" + toRef + "</b> to <b>" + fromRef + "</b></p><button type=\"reset\" id=\"confBtn\" name=\"confBtn\" class=\"btnBlue cursorHand\" title=\"Submits your suggestion.\" onClick=\"javascript:confirmL(this, '" + fromRef + "','" + toRef + "')\">Confirm Suggestion</button><button type=\"reset\" id=\"clsBtn\" name=\"clsBtn\" class=\"btnBlue cursorHand\" title=\"Clears the suggestion area.\" onClick=\"javascript:clearL(this)\">Cancel Suggestion</button>");
  }
}
fromV = null; fromPassage = null; fromRef = null;
toV   = null; toPassage   = null; toRef   = null;
%>
<%= sb.toString() %>
