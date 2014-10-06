<%@page contentType="text/html;charset=ISO-8859-1" language="java"%>
<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<s:layout-definition>
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd" >
	<html>
<head>
<title>${title}</title>
<link rel="stylesheet" type="text/css"
	href="${contextPath}/css/style.css">

<link rel="stylesheet" type="text/css"
	href="${contextPath}/css/ext-all.css">
<script type="text/javascript" src="${contextPath}/js/ext-base-debug.js"></script>
<%-- <script type="text/javascript" src="${contextPath}/js/ext-base.js"></script> --%>
<script type="text/javascript" src="${contextPath}/js/ext-all-debug.js"></script>
<%-- <script type="text/javascript" src="${contextPath}/js/ext-all.js"></script> --%>
<script type="text/javascript" src="${contextPath}/js/slider.js"></script>
<link rel="stylesheet" type="text/css"
	href="${contextPath}/css/examples.css">
<link rel="stylesheet" type="text/css"
	href="${contextPath}/css/slider.css">
</head>
<body>
	<div id="header" style="width: 500px">
		<span class="title">${title}</span>
	</div>
	
		<div style="width: 500px; right-padding: 10px;  float:left !important; margin: 50px;">
			<s:layout-component name="navipanel_area" />
		</div>
		<div style="width: 300px; float:left !important;  margin: 50px; ">
			<s:layout-component name="content_area" />
		</div>
		
	
</body>
	</html>
</s:layout-definition>