/* 
 * Created on Nov 21, 2007
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package com.ibm.TWSS.services.v1_0.ChargingService.Binding2;

public class SampleClientSideHandler extends javax.xml.rpc.handler.GenericHandler {
	private javax.xml.rpc.handler.HandlerInfo hi = null;

	public void init(javax.xml.rpc.handler.HandlerInfo arg0) {
		hi = arg0;
		super.init(arg0);
	}

	public boolean handleRequest(javax.xml.rpc.handler.MessageContext arg0) {
		java.util.Map handlerConfig = hi.getHandlerConfig();
		String userId = (String) handlerConfig.get("username");
		String password = (String) handlerConfig.get("password");
		// set the user id and pwd as Basic Auth in the HTTP header
		/*ChargingProcess.com.ibm.ws.webservices.engine.MessageContext ibmMessageContext = (com.ibm.ws.webservices.engine.MessageContext) arg0;
		ibmMessageContext.setUsername(userId);
		ibmMessageContext.setPassword(password);*/
		
		System.out.println("************************************* HANDLING REQUEST *********************************** ");
		return super.handleRequest(arg0);
	}

	public javax.xml.namespace.QName[] getHeaders() {
		return hi.getHeaders();
	}
}