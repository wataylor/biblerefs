<%@ page import="java.io.ByteArrayOutputStream,
java.io.PrintStream" isErrorPage="true" %>
<html>
<head><title>Run Error</title>

<%-- runError.jsp

    Copyright (c) 2003\6 by Advanced Systems & Software Technologies.
    All Rights Reserved

   Under revision by: $Locker:  $
   Change Log:
   $Log: runError.jsp,v $
   Revision 1.1  2008/09/26 00:21:28  peren
   first bible ref upload

--%>
</head>
<body>
<h1 align="center">Execution Error</h1>
<p><font color="red"><%= exception.toString() %></font>
<hr />

<pre>
<%
    ByteArrayOutputStream ostr = new ByteArrayOutputStream();
    exception.printStackTrace(new PrintStream(ostr));
    out.print(ostr);
%>
</pre>

<hr />
<p>Please copy this display and email it to the owner of this page.</p>
<p><a href="mailto:krtaylor@as-st.com">
krtaylor@as-st.com</a></p>
</body></html>
