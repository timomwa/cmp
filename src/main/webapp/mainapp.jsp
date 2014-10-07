<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>

<ss:secure  roles="ADMIN, POWER_USER">
Secured content goes in here
</ss:secure>

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
