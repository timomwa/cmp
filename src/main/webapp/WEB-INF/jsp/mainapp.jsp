
<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>

<%
	//String site = new String("ShowLoginPage.action");
	//response.setStatus(response.SC_MOVED_TEMPORARILY);
	// response.setHeader("Location", site);
%>




<s:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
	title="Content Management Platform: Content360">
	<!-- (1) -->

	<s:layout-component name="navipanel_area">
		<script type="text/javascript" src="js/examples.js"></script>
		<div>Navipanel_area</div>
com.pixelandtag.action.PageAction loginLogout = (com.pixelandtag.action.PageAction) pageContext.getAttribute("loginLogout");
		<c:forEach var="section" items="${loginLogout.sections}">
			
				<security:allowed bean="loginLogout">
					<span class="grayedOut">${section}</span><br/>
				</security:allowed>
				<security:notAllowed bean="loginLogout">
						<span class="grayedOut">Not allowed</span><br/>
				</security:notAllowed>
		</c:forEach>

	</s:layout-component>



	<s:layout-component name="content_area">
		<div>
			Content area<br />
			<%
				String userAgent = request.getHeader("user-agent");
			%>
			<%=userAgent%>
		</div>
	</s:layout-component>

</s:layout-render>
