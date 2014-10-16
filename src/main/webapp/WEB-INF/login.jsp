<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>

<s:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
	title="Content Management Platform: Content360">
	<!-- (1) -->

	<s:layout-component name="navipanel_area">
		<script type="text/javascript" src="js/examples.js"></script>
		<div>Navipanel_area</div>
	</s:layout-component>

<security:allowed>
allowed?
</security:allowed>

	<s:layout-component name="content_area">
		<div>Content area</div>
	</s:layout-component>

</s:layout-render>
