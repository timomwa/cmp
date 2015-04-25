package com.pixelandtag.api.bulksms;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.APIAuthenticationException;
import com.pixelandtag.cmp.ejb.BulkSMSI;
import com.pixelandtag.cmp.ejb.ParameterException;
import com.pixelandtag.cmp.ejb.PersistenceException;

@Stateless
public class SendBulkAPI implements Sendbulk{

	
	private Logger logger = Logger.getLogger(getClass());
	
	@EJB
	private BulkSMSI bulksms_api;
	
	@Override
	public Response pushList(@Context HttpHeaders headers, InputStream incomingData, @Context HttpServletRequest req)
			throws QueueException {
	
		Response response = null;
		JSONObject jsob = new JSONObject();
		
		try{
			
			if(headers.getRequestHeader("username")==null || headers.getRequestHeader("username").size()<=0)
				throw new APIAuthenticationException("Username not provided. Kindly provide this.");
			if(headers.getRequestHeader("password")==null || headers.getRequestHeader("password").size()<=0)
				throw new APIAuthenticationException("Password not provided. Kindly provide this.");
			if(headers.getRequestHeader("apiKey")==null || headers.getRequestHeader("apiKey").size()<=0)
				throw new APIAuthenticationException("API key not provided.");
			String ipAddress = req.getRemoteAddr();
			
			String password = headers.getRequestHeader("password").get(0).trim();
			String username = headers.getRequestHeader("username").get(0).trim();
			String apiKey = headers.getRequestHeader("apiKey").get(0).trim();
			
			
			String jsonString = readString(incomingData);
			
			bulksms_api.enqueue(ipAddress, apiKey,username,password,jsonString);
			
			jsob.put("success", true);
			jsob.put("message", "Successfully queued bulk message");
			response = Response.status(Response.Status.CREATED)
					.entity(jsob.toString()).build();
			return response;
		}catch(ParameterException parame){
			//logger.error(parame.getMessage(),parame);
			try {
				jsob.put("success", false);
				jsob.put("message", parame.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(jsob.toString()).build();
		}catch(PersistenceException pse){
			//logger.error(pse.getMessage(),pse);
			try {
				jsob.put("success", false);
				jsob.put("message", pse.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(jsob.toString()).build();
		}catch(JSONException jse){
			//logger.error(jse.getMessage(),jse);
			try {
				jsob.put("success", false);
				jsob.put("message", "Malformed json string. Kindly check your json.");
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.NOT_ACCEPTABLE)
				.entity(jsob.toString()).build();
		}catch(APIAuthenticationException authe){
			//logger.error(authe.getMessage(),authe);
			try {
				jsob.put("success", false);
				jsob.put("message", authe.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.UNAUTHORIZED)
				.entity(jsob.toString()).build();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			try {
				jsob.put("success", false);
				jsob.put("message", "Problem occurred");
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(jsob.toString()).build();
		}
		
		return response;
	}

	/**
	 * Converts input stream to
	 * String
	 * @param incomingData
	 * @return java.lang.String
	 */
	private String readString(InputStream incomingData) {
		InputStreamReader isr = null;
		StringBuffer sb = new StringBuffer();
		String data = "";
		try {
			isr = new InputStreamReader(incomingData);
			BufferedReader in = new BufferedReader(isr);
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			data = sb.toString();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}finally{
			try{
				isr.close();
			}catch(Exception exp){
				logger.warn(exp.getMessage());
			}
		}
		
		return data;
	}
	
	
	
	

}
