<%
final Throwable trhowable = (Throwable) request.getAttribute ("exception");
final String message = (String) request.getAttribute ("msg");
%>
{"success": "false", "message": "<%= message %>"}