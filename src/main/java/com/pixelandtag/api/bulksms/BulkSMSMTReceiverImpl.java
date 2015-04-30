package com.pixelandtag.api.bulksms;

import java.io.InputStream;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.bulksms.APIAuthenticationException;
import com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI;
import com.pixelandtag.cmp.ejb.bulksms.ParameterException;
import com.pixelandtag.cmp.ejb.bulksms.PersistenceException;
import com.pixelandtag.cmp.ejb.bulksms.PlanBalanceException;
import com.pixelandtag.cmp.ejb.bulksms.QueueFullException;


@Stateless
public class BulkSMSMTReceiverImpl extends BaseRestImpl implements BulkSMSMTReceiverI {
	
	@EJB
	private BulkSmsMTI mtreceiver_bean;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Override
	public Response post(HttpHeaders headers, InputStream incomingData,
			HttpServletRequest req) throws QueueException {
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
			
			String cptxid = mtreceiver_bean.enqueue(ipAddress, apiKey,username,password,jsonString);
			
			
			
			JSONObject tx_id_json = new JSONObject();
			tx_id_json.put("cptxid", cptxid);
			jsob.put("success", true);
			jsob.put("message", tx_id_json);
			response = Response.status(Response.Status.CREATED)
					.entity(jsob.toString()).build();
			
			return response;
			
		}catch(QueueFullException qfe){
			try {
				jsob.put("success", false);
				jsob.put("message", qfe.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.SERVICE_UNAVAILABLE)
				.entity(jsob.toString()).build();
			
		}catch(PlanBalanceException pbe){
			try {
				jsob.put("success", false);
				jsob.put("message", pbe.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.NOT_ACCEPTABLE)
				.entity(jsob.toString()).build();
		}catch(ParameterException parame){
			//logger.error(parame.getMessage(),parame);
			try {
				jsob.put("success", false);
				jsob.put("message", parame.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage(),e1);
			}
			response =  Response.status(Response.Status.NOT_ACCEPTABLE)
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

}
