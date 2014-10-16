<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>

<%
 //String site = new String("ShowLoginPage.action");
 //response.setStatus(response.SC_MOVED_TEMPORARILY);
// response.setHeader("Location", site); 
%>
<s:useActionBean  var="loginLogout" beanclass="com.pixelandtag.cmp.action.ShowLoginPageAction"/>

<security:NotAllowed  bean="loginLogout" event="showLoginPage">
Secured content goes in here
</security:NotAllowed>


<s:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
	title="Content Management Platform: Content360">
	<!-- (1) -->

	<s:layout-component name="navipanel_area">
		<script type="text/javascript" src="js/examples.js"></script>
		<div>Navipanel_area</div>
	</s:layout-component>



	<s:layout-component name="content_area">
		<div>Content area<br/>
		<% String userAgent=request.getHeader("user-agent"); %>
<%= userAgent %>
</div>
	</s:layout-component>

</s:layout-render>
