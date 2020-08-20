<%@page import="asst.biblerefs.RefSQLLookup,
loadsql.LoadKJVTextVerses,asst.dbase.SQLUtilities,
java.util.List, java.util.ArrayList,
asst.biblerefs.RattleForUTF8,
asst.dbase.DBProp" errorPage="runError.jsp"%><%
RefSQLLookup rsl = new RefSQLLookup();
String lang;
String name;
String ref;
String passage = null;
StringBuilder sb = new StringBuilder();
List<String> languages = new ArrayList<String>();
boolean wantRef;
response.setCharacterEncoding("UTF8");
rsl.setup();
//DBProp.PropertyConnection(LoadVerses.DATABASE_NAME);
System.out.println("connDB null " + (asst.dbase.DataBase.connDB == null));
// asst.dbase.DataBase.openDB("com.mysql.jdbc.Driver", "jdbc:mysql://mysql.perennitypublishing.com/Perennity", "perennity", "2indianbk");
// RattleForUTF8.rattle("after");
wantRef = (( (name = request.getParameter("cr")) != null) && "true".equalsIgnoreCase(name));

if (( (name = request.getParameter("dis")) != null) && (name.length() > 0)) {
  SQLUtilities.AnyStatement("update VerseCref set Disagree=Disagree+1 where ID=" + name);
  sb.append("<P>Your feedback has been recorded, thank you very much.  I Corinthians 3:9 says that we labor together with God when we do His work.  We appreciate your help in verifying the cross-reference list.</p>");
} else {
	languages.clear();
	lang    = request.getParameter("lang");
	languages.add(lang);
	if (( (name = request.getParameter("span")) != null) && "true".equalsIgnoreCase(name)) {
		languages.add("Spanish");
	}
	if (( (name = request.getParameter("thai")) != null) && "true".equalsIgnoreCase(name)) {
		languages.add("Thai");
	}
	if (( (name = request.getParameter("germ")) != null) && "true".equalsIgnoreCase(name)) {
		languages.add("German");
	}
	if (( (name = request.getParameter("engl")) != null) && "true".equalsIgnoreCase(name)) {
		languages.add(""); // Empty string is English
	}

  name    = request.getParameter("passage");
  ref     = rsl.findCanonicalReference(name, "");
  System.out.println(ref + " " + name + " lng " + lang);
	if (ref != null) {
		if (languages.size() > 1) {
			 String[] langRay = new String[languages.size()];
	passage = rsl.findAVerse(ref, wantRef, languages.toArray(langRay));
		} else {
	passage = rsl.findAVerse(ref, wantRef, lang);
		}
	}
	if (passage == null) {
		sb.append(name + " was not understood.  Please try again with a form like Re. 5:2-3");
	} else {
		if (!"".equals(lang)) { // Get book name inother langage
	ref = rsl.findCanonicalReference(name, lang);
		}
		sb.append("<p>" + passage + "&nbsp; <b>" + ref + "</b>&nbsp; </p>");
	}
}
name = null;
passage = null;
ref = null;
// System.out.println(sb.toString());
%>

<%= sb.toString() %>
