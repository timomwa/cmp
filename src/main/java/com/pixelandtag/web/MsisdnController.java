package com.pixelandtag.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author tim
 * /msisdnController
 *
 */
public class MsisdnController extends HttpServlet {
	
	private Logger logger = Logger.getLogger(MsisdnController.class);
	private DataSource ds;
	private Context initContext;
	private JSONObject requestJSON = null;
	private JSONObject responseJSON = null;
	private PrintWriter writer;

	private final String SERVER_TIMEZONE = "-05:00";
	private final String CLIENT_TIMEZONE = "+03:00";
	
	private String DB = "pixeland_content360";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4332451L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		
		Connection conn = null;
		PrintWriter out = null;
		String line = null;
		
		try {
			
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try{
				conn = ds.getConnection();
				stmt = conn.prepareStatement("SELECT now()");
				rs = stmt.executeQuery();
				
				while(rs.next()){
					rs.getString(1);
					logger.debug("ds  = ok!");
				}
				
			}catch(Exception e){
					log(e);
					conn = getConnection();
			}finally{
				
				try{
					if(stmt!=null)
						stmt.close();
				}catch(Exception e){}
				
				try{
					if(rs!=null)
						rs.close();
				}catch(Exception e){}
				
			}
			
			@SuppressWarnings("unchecked")
			Enumeration<String> param_enums = req.getParameterNames();
			String elem = "";
			int i = 0;
			while(param_enums.hasMoreElements()){
				elem  =  param_enums.nextElement();
				logger.info(i+". Parameter names :: "+elem );
				i++;
			}
			
			
			
			StringBuffer jb = new StringBuffer();
			BufferedReader reader = req.getReader();
			   
			  while ((line = reader.readLine()) != null)
		    	  jb.append(line);
			
			  String jsonstr = jb.toString();
			  
			  if(jsonstr==null || jsonstr.isEmpty())
				  jsonstr = elem;
			logger.info("Incomming json >> " + jb.toString());
			requestJSON = new JSONObject(jsonstr);
			String command = requestJSON.getString("command");
			logger.info("command>> "+command);
			
			if(command.equals("getMsisdnProfile")){
				getMsisdn(req,resp,conn);
			}else if(command.equals("getLogsFor")){
				getLogsFor(req,resp,conn);
			}
			
			
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			
			try {
				if(out!=null)
					out.close();
			} catch (Exception e) {
				log(e);
			}
			
			
			try {
				if(conn!=null)
				conn.close();
			} catch (SQLException e) {
				log(e);
			}
		}
		
	}

	
	
	private void getLogsFor(HttpServletRequest req, HttpServletResponse resp,
			Connection conn) {
		PreparedStatement ps = null;
		
		ResultSet rs = null;
		
		try {
			
			
			prepare(resp);
			
			 responseJSON.put("success", "false");
			 
			 String query = "";
			 String date = "";
			 
			 String msisdn = requestJSON.getString("msisdn");
			 
			 date = requestJSON.getString("date").trim();
			 
			 if(msisdn!=null && !msisdn.isEmpty()){
				     ps = conn.prepareStatement("select status as 'MT_STATUS',convert_tz(mo_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as 'timeStamp',msisdn as 'SUB_Mobtel',cmp_tx_id as 'CMP_Txid', mo_sms as 'MO_Received',mt_sms as 'MT_Sent', status as 'CMPResponse', convert_tz(mt_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as dlrArrive,`source` as 'source', shortcode from "+DB+".message_log where msisdn=? and date(convert_tz(mo_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"'))=? order by mo_timestamp desc");
				 ps.setString(1, msisdn);
				 ps.setString(2, date);
			 }else{
				 try{
					 
					 int limit = Integer.valueOf(date);
					 ps = conn.prepareStatement("select status as 'MT_STATUS',convert_tz(mo_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as 'timeStamp',msisdn as 'SUB_Mobtel',cmp_tx_id as 'CMP_Txid', mo_sms as 'MO_Received', mt_sms as 'MT_Sent', status as 'CMPResponse', convert_tz(mt_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as dlrArrive,`source` as 'source', shortcode from "+DB+".message_log order by mo_timestamp desc limit "+limit);
					 
				 }catch(NumberFormatException ex){
					 
					 try{
						 ps.close();
					 }catch(Exception exp){}
					 
					 ps = conn.prepareStatement("select status as 'MT_STATUS',convert_tz(mo_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as 'timeStamp',msisdn as 'SUB_Mobtel', cmp_tx_id as 'CMP_Txid', mo_sms as 'MO_Received', mt_sms as 'MT_Sent', status as 'CMPResponse', convert_tz(mt_timestamp,'"+SERVER_TIMEZONE+"','"+CLIENT_TIMEZONE+"') as dlrArrive,`source` as 'source', shortcode from "+DB+".message_log where date(mo_timestamp)=? order by mo_timestamp desc limit 50");
					 ps.setString(1, date);
				 }
			 }
			 rs = ps.executeQuery();
			 
			 
			 String timeStamp="",SUB_Mobtel="",CMP_Txid="",MO_Received="",MT_Sent="",MT_STATUS="", dlrArrive="", source="", shortcode="";
			 
			 int i = 0;
			 
			 while(rs.next()){
				 timeStamp = StringEscapeUtils.escapeHtml(rs.getString("timeStamp"));
				 SUB_Mobtel = StringEscapeUtils.escapeHtml(rs.getString("SUB_Mobtel"));
				 CMP_Txid = StringEscapeUtils.escapeHtml(rs.getString("CMP_Txid"));
				 MO_Received = StringEscapeUtils.escapeHtml(rs.getString("MO_Received"));
				 MT_Sent = StringEscapeUtils.escapeHtml(rs.getString("MT_Sent"));
				 MT_STATUS = StringEscapeUtils.escapeHtml(rs.getString("MT_STATUS"));
				 dlrArrive = StringEscapeUtils.escapeHtml(rs.getString("dlrArrive"));
				 source = StringEscapeUtils.escapeHtml(rs.getString("source"));
				 shortcode  = StringEscapeUtils.escapeHtml(rs.getString("shortcode"));
				 //System.out.println("count:::::::"+count);
				 responseJSON.append("timeStamp", timeStamp);
				 responseJSON.append("SUB_Mobtel", SUB_Mobtel);
				 responseJSON.append("CMP_Txid", CMP_Txid);
				 responseJSON.append("MO_Received", MO_Received);
				 responseJSON.append("MT_Sent", MT_Sent);
				 responseJSON.append("MT_STATUS", MT_STATUS);
				 responseJSON.append("dlrArrive", dlrArrive);
				 responseJSON.append("source", source);
				 responseJSON.append("shortcode", shortcode);
				 i++;
			 }
			 
			 if(i>0){
				 responseJSON.put("success", "true");
			 }else{
				 responseJSON.put("message", "No records found for "+msisdn+" on "+date);
				 responseJSON.put("success", "false");
			 }
			
		 } catch (JSONException e) {
			
			logger.error(e.getMessage(),e);
		
		}catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
		
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}finally{
			
			
				try {
					
					if(rs !=null)
						rs.close();
					if(ps !=null)
						ps.close();
					/*if(conn !=null)
						conn.close();*/
					
				} catch (SQLException e) {
					
					logger.error(e.getMessage(),e);
				}
				
				logger.info("change resp text result :::::::"+responseJSON.toString());
				writer.write(responseJSON.toString());
		}
		
	}

	/**
	 * Prepare for json
	 * @param response
	 */
	private void prepare(HttpServletResponse response) {
		
		responseJSON  = new JSONObject();
		
		try {
		
			writer = response.getWriter();
		
		} catch (IOException e) {
			
			logger.error(e.getMessage(),e);
		
		}
		
		response.setContentType("application/json");
	}
	
	
	
	
	private void getMsisdn(HttpServletRequest req, HttpServletResponse resp,
			Connection conn) {

		PreparedStatement ps = null;
		
		ResultSet rs = null;
		
		
		
		try {
			
			
			prepare(resp);
			
			 responseJSON.put("success", "false");
			 
			 String query = "";
			 String date = "";
			 
			 String msisdn = requestJSON.getString("msisdn");
			 
			 date = requestJSON.getString("date").trim();
			 
			 ps = conn.prepareStatement("select count(*) as 'count', statusCode, price from "+DB+".SMSStatLog where date(timeStamp)=? and msisdn=? group by statusCode, price");
				
			 ps.setString(1, date);
			 
			 ps.setString(2, msisdn);
			 
			 rs = ps.executeQuery();
			 
			 
			 String count="",statusCode="", price="";
			 
			 int i = 0;
			 
			 while(rs.next()){
				 count = rs.getString("count");
				 statusCode = rs.getString("statusCode");
				 price = rs.getString("price");
				 responseJSON.append("count", count);
				 responseJSON.append("statusCode", statusCode);
				 responseJSON.append("price", price);
				 i++;
			 }
			 
			 if(i>0){
				 responseJSON.put("success", "true");
			 }else{
				 responseJSON.put("message", "No records found for "+msisdn+" on "+date);
				 responseJSON.put("success", "false");
			 }
				
			 
			
		
		 } catch (JSONException e) {
			
			logger.error(e.getMessage(),e);
		
		}catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
		
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}finally{
			
			
				try {
					
					if(rs !=null)
						rs.close();
					if(ps !=null)
						ps.close();
					/*if(conn !=null)
						conn.close();*/
					
				} catch (SQLException e) {
					
					logger.error(e.getMessage(),e);
				}
				
				logger.info("change resp text result :::::::"+responseJSON.toString());
				writer.write(responseJSON.toString());
		}
		
	}

	public void destroy() {
		
	}
	
	
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	
	
	private Connection getConnection() {
		
		Connection conn = null;
		
		try {
			
			try{
				if(initContext!=null)
					initContext.close();
			}catch(Exception e){}
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			conn = ds.getConnection();
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
		
		}
		
		return conn;
		
	}

	public void init() throws ServletException {
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			
		} catch (NamingException e) {
			
			log(e);
		
		}
		
	}

}
