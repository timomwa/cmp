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
<title>CGW Latency Monitor</title>

<link rel="stylesheet" type="text/css" href="css/ext-all.css" />

<!-- Chart.js -->
<script type="text/javascript" src="js/Chart.js"></script>
<!-- LIBS -->
<script type="text/javascript" src="js/adapter/ext/ext-base.js"></script>
<!-- ENDLIBS -->

<script type="text/javascript" src="js/ext-all-debug.js"></script>

<script type="text/javascript" src="js/billingmonitor.js?v=3.1"></script>
</head>
<body >
<h1>CGW Link Latency trend</h1>
<div  style="width:80%; margin-left:5px">
<canvas id="billingmonitor"  height="400" width="600"></canvas> 
</div>
</body>
</html>
