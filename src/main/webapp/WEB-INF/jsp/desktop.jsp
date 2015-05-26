<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<html>
<head>
<link rel="apple-touch-icon" sizes="57x57" href="images/favicon/apple-icon-57x57.png">
<link rel="apple-touch-icon" sizes="60x60" href="images/favicon/apple-icon-60x60.png">
<link rel="apple-touch-icon" sizes="72x72" href="images/favicon/apple-icon-72x72.png">
<link rel="apple-touch-icon" sizes="76x76" href="images/favicon/apple-icon-76x76.png">
<link rel="apple-touch-icon" sizes="114x114" href="images/favicon/apple-icon-114x114.png">
<link rel="apple-touch-icon" sizes="120x120" href="images/favicon/apple-icon-120x120.png">
<link rel="apple-touch-icon" sizes="144x144" href="images/favicon/apple-icon-144x144.png">
<link rel="apple-touch-icon" sizes="152x152" href="images/favicon/apple-icon-152x152.png">
<link rel="apple-touch-icon" sizes="180x180" href="images/favicon/apple-icon-180x180.png">
<link rel="icon" type="image/png" sizes="192x192"  href="images/favicon/android-icon-192x192.png">
<link rel="icon" type="image/png" sizes="32x32" href="images/favicon/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="96x96" href="images/favicon/favicon-96x96.png">
<link rel="icon" type="image/png" sizes="16x16" href="images/favicon/favicon-16x16.png">

<link rel="manifest" href="/manifest.json">
<meta name="msapplication-TileColor" content="#ffffff">
<meta name="msapplication-TileImage" content="/ms-icon-144x144.png">
<meta name="theme-color" content="#ffffff">
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>CMP</title>

<link rel="stylesheet" type="text/css" href="css/ext-all.css" />
<link rel="stylesheet" type="text/css" href="css/desktop.css" />

<!-- GC -->
<!-- LIBS -->
<script type="text/javascript" src="js/adapter/ext/ext-base.js"></script>
<!-- ENDLIBS -->

<script type="text/javascript" src="js/ext-all-debug.js"></script>

<!-- DESKTOP -->
<script type="text/javascript" src="js/permissions.js?v=3.1"></script>
<script type="text/javascript" src="js/StartMenu.js?v=3.1"></script>
<script type="text/javascript" src="js/TaskBar.js?v=3.1"></script>
<script type="text/javascript" src="js/Desktop.js?v=3.1"></script>
<script type="text/javascript" src="js/App.js?v=3.1"></script>
<script type="text/javascript" src="js/Module.js?v=3.1"></script>
<script type="text/javascript" src="js/sample.js?v=3.1"></script>
<script type="text/javascript" src="js/content_manager.js?v=3.1"></script>
<script type="text/javascript" src="js/subscription_management.js?v=3.1"></script>
<script type="text/javascript" src="js/statistics.js?v=2.1"></script>
<script type="text/javascript" src="js/customer_care.js?v=3.1"></script>



<script type="text/javascript" src="js/livebilling_graph.js?v=3.1"></script>


</head>
<body scroll="no">

	<div id="x-desktop">
		<a href="http://pixelandtag.com" target="_blank"
			style="margin: 5px; float: right;"><img
			src="images/pixelandtag.png" /></a>

		<dl id="x-shortcuts">
			<!-- dt id="grid-win-shortcut">
            <a href="#"><img src="images/s.gif" />
            <div>Grid Window</div></a>
        </dt -->
			
			<shiro:hasAnyRoles name="superuser, usermanagement">
				<dt id="acc-win-shortcut">
					<a href="#"><img src="images/s.gif" />
						<div>User Management</div></a>
				</dt>
			</shiro:hasAnyRoles>
			
			
			<shiro:hasAnyRoles name="superuser, contentmanagement">
				<dt id="content-win-shortcut">
					<a href="#"><img src="images/s.gif" />
						<div>Content Management</div></a>
				</dt>
			</shiro:hasAnyRoles>
			
			
			<shiro:hasAnyRoles name="superuser, subscriptionmanagement">
				<dt id="subscription-win-shortcut">
					<a href="#"><img src="images/s.gif" />
						<div>Subscription Management</div></a>
				</dt>
			</shiro:hasAnyRoles>
			
			<shiro:hasAnyRoles name="superuser, statistics">
				<dt id="statistics-win-shortcut">
					<a href="#"><img src="images/s.gif" />
						<div>Statistics</div></a>
				</dt>
			</shiro:hasAnyRoles>
			
			
			<shiro:hasAnyRoles name="superuser, customercare">
				<dt id="customer_care-win-shortcut">
					<a href="#"><img src="images/s.gif" />
						<div>Customer Care</div></a>
				</dt>
			</shiro:hasAnyRoles>
			
			
		</dl>
	</div>

	<div id="ux-taskbar">
		<div id="ux-taskbar-start"></div>
		<div id="ux-taskbuttons-panel"></div>
		<div class="x-clear"></div>
	</div>

</body>
</html>
