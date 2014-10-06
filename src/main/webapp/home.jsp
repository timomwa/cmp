<%@ page
	import="com.mysql.jdbc.Driver,javax.naming.Context,javax.sql.DataSource,java.util.GregorianCalendar,java.sql.Connection,javax.naming.InitialContext,org.json.JSONObject,com.inmobia.giant.web.UserSessionObject,java.sql.Statement,java.sql.ResultSet,java.sql.SQLException,java.io.BufferedReader,java.io.PrintWriter,org.apache.log4j.Logger,java.sql.PreparedStatement"%>

<%
	 int SUPER_USER = 0;//CAN READ,EDIT,DELETE ANYTHING EDITABLE
	 int ADMIN_USER = 1;//CONTENT IS READ ONLY ACCESS, CAN EDIT THEIR ACCOUNT AND THOSE FOR OTHER USERS.
	 int NORMAL_USER = 2;//CONTENT IS READ ONLY ACCESS, CAN EDIT THEIR ACCOUNT ONLY (change password only as well as first and last name)
	 int READ_ONLY = 3;//CONTENT IS READ ONLY ACCESS, CAN'T EDIT THEIR ACCOUNT

	request.setAttribute("SUPER_USER",SUPER_USER);
	request.setAttribute("ADMIN_USER",ADMIN_USER);
	request.setAttribute("NORMAL_USER",NORMAL_USER);
	request.setAttribute("READ_ONLY",READ_ONLY);
	Logger logger = Logger.getLogger(getClass().getClass());
	UserSessionObject user = (UserSessionObject) session
			.getAttribute("userObject");

	if (user != null) {

		String username = user.getFirst_name() + ", "
				+ user.getLast_name();
		int id = user.getId();
		int role = user.getRole();
		request.setAttribute("username", username);
		request.setAttribute("id", id);
		request.setAttribute("role", role);
		request.setAttribute("active", user.isActive());
%>
<html>
<head>
<!-- meta http-equiv="Content-Type" content="text/html; charset=utf-8" /-->
<title>Home</title>
<script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="js/jquery.json-2.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.7.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/daterangepicker.jQuery.js"></script>
<script type="text/javascript" src="js/giant.js"></script>
<link href="css/styles.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="css/ui.daterangepicker.css" type="text/css" />
<link rel="stylesheet" href="css/redmond/jquery-ui-1.7.1.custom.css" type="text/css" title="ui-theme" />

</head>

<body>
<div id="main_div">
<div id="home_container">
<div id="header">
<div class="home_text">Welcome
${username}&nbsp;&nbsp;|&nbsp;&nbsp;<a href="#" onclick="GIANT.logout()">Logout</a></div>

</div>
 <%
 	if (role <= NORMAL_USER) {
 %> 
<div id="menu"><a href="#" name="voucher"
	onclick="GIANT.navigate(this)">M-Vouchers</a><a href="#" name="account" onclick="GIANT.navigate(this)">My
Account</a> <a href="#" name="help" onclick="GIANT.navigate(this)">Help</a> </div> <%
 	}
 %> 
<div id="status_span"></div>

<div id="content">











<%
	} else {
		out.println("You're not authorized to access this page. <a href='login.jsp'> Login </a>");
		
		%>		
		<script type="text/javascript">	
			$(location).attr('href','login.jsp');
		</script>
		<%
		
	}
%>
</div>
<script type="text/javascript">	
			//$(function(){
				
				 // $('#phone_voucher_num').daterangepicker({arrows: true, dateFormat: 'yy-mm-dd'}); 
				// alert(GIANT.CURRENT_PAGE);
				//$('#content').load(name);
			// });
			//$(document).ready(function(){
			//	alert($.cookie('current_page'));
			//});
			var current_page = $.cookie('current_page');
				//alert('current_page = '+current_page);
			if(current_page!=null){
				//GIANT.navigate(current_page);
				$(location).attr('href',current_page);
				//$('html').parent.load(current_page);
			}
			
</script>

<div id="footer">
<div class="footer_content"><span class="inmobia_footer_logo"></span></div>
</div>
</div>
</div>
</body>
</html>
