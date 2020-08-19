<!-- sugForm.jsp

   Copyright (c) 2008 by Advanced Systems and Software Technologies.
   All Rights Reserved

--><%--
   Under revision by: $Locker:  $
   Change Log:
   $Log: sugForm.jsp,v $
   Revision 1.1  2008/10/18 18:06:55  peren
   allow users to add suggestions

--%>
Suggest a link from <input type="text" id="fromV" name="fromV" value="">
to <input type="text" id="toV" name="toV" value="">
<button type="reset" id="sugBtn"  name="sugBtn" class="btnBlue cursorHand"
title="Submits your suggestion."
onClick="javascript:suggestL(this)">Relate Verses</button>
