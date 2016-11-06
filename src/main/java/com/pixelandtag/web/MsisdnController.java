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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	private final String SERVER_TIMEZONE = "+03:00";
	private final String CLIENT_TIMEZONE = "+03:00";
	private final SimpleDateFormat formatDayOfMonth  = new SimpleDateFormat("d");
	
	//private String DB = "pixeland_content360";

	/**
	 * 
	 */
	private static final long serialVersionUID = 43332451L;
	private static final String KE_COUNTRY_CODE = "254";
	private static final String ZERO = "0";
	private NumberFormat nf = NumberFormat.getInstance();//Be careful only available in Java 8
	
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
			 
			 String msisdn = convertToMsisdnFormat( requestJSON.getString("msisdn") );
			 
			 date = requestJSON.getString("date").trim();
			 
			ps = conn.prepareStatement("SELECT counts FROM gen_counter");
			rs = ps.executeQuery();
			
			String total_counts_so_far = "no count";
			
			if(rs.next())
				 total_counts_so_far = rs.getString("counts");
			try{
				if(ps!=null)
					ps.close();
			}catch(Exception e){}
			
			try{
				if(rs!=null)
					rs.close();
			}catch(Exception e){}
				
			 if(msisdn!=null && !msisdn.isEmpty()){
				 ps = conn.prepareStatement("select id,billRefNumber,businessShortcode, first_name, last_name,  middle_name, msisdn, orgAccountBalance, raw_xml_id, sourceip, timeStamp, transAmount, transId, transType, status from mpesa_in WHERE (msisdn = ? OR transId=?) AND date(timeStamp)=? order by timeStamp desc");
				 ps.setString(1, msisdn);
				 ps.setString(2, msisdn);
				 ps.setString(3, date);
			 }else{
				 try{
					 
					 int limit = Integer.valueOf(date);
					 ps = conn.prepareStatement("select id,billRefNumber,businessShortcode, first_name, last_name,  middle_name, msisdn, orgAccountBalance, raw_xml_id, sourceip, timeStamp, transAmount, transId, transType, status from mpesa_in order by timeStamp desc limit "+limit);
					 
				 }catch(NumberFormatException ex){
					 
					 try{
						 ps.close();
					 }catch(Exception exp){}
					 
					 ps = conn.prepareStatement("select id,billRefNumber,businessShortcode, first_name, last_name,  middle_name, msisdn, orgAccountBalance, raw_xml_id, sourceip, timeStamp, transAmount, transId, transType, status from mpesa_in where date(timeStamp)=? order by timeStamp desc limit 50");
					 ps.setString(1, date);
				 }
			 }
			 rs = ps.executeQuery();
			 
			 
			 String id,billRefNumber="",businessShortcode="", first_name="", last_name="", middle_name="", orgAccountBalance="", 
			 raw_xml_id="", sourceip="", timeStamp="", transAmount="", transId="", transType="", status="";
			 
			 int i = 0;
			 
			 while(rs.next()){
				 
				 id = StringEscapeUtils.escapeHtml(rs.getString("id"));
				 billRefNumber = StringEscapeUtils.escapeHtml(rs.getString("billRefNumber"));
				 businessShortcode = StringEscapeUtils.escapeHtml(rs.getString("businessShortcode"));
				 first_name = StringEscapeUtils.escapeHtml(rs.getString("first_name"));
				 last_name = StringEscapeUtils.escapeHtml(rs.getString("last_name"));
				 middle_name = StringEscapeUtils.escapeHtml(rs.getString("middle_name"));
				 orgAccountBalance = "Kes. "+ nf.format(  Double.valueOf( StringEscapeUtils.escapeHtml(rs.getString("orgAccountBalance")) ) );
				 raw_xml_id = StringEscapeUtils.escapeHtml(rs.getString("raw_xml_id"));
				 sourceip  = StringEscapeUtils.escapeHtml(rs.getString("sourceip"));
				 timeStamp  = StringEscapeUtils.escapeHtml(rs.getString("timeStamp"));
				 timeStamp = convertToPrettyFormat(timeStamp);
				 transAmount  = "Kes. "+  nf.format( Double.valueOf( StringEscapeUtils.escapeHtml(rs.getString("transAmount")) ) );
				 transId  = StringEscapeUtils.escapeHtml(rs.getString("transId")).toUpperCase();
				 transType  = StringEscapeUtils.escapeHtml(rs.getString("transType"));
				 status  = StringEscapeUtils.escapeHtml(rs.getString("status"));
				 msisdn  = StringEscapeUtils.escapeHtml(rs.getString("msisdn"));
				 
				responseJSON.append("id", id );
				responseJSON.append("billRefNumber", billRefNumber);
				responseJSON.append("businessShortcode", businessShortcode);
				responseJSON.append("first_name", first_name);
				responseJSON.append("last_name", last_name);
				responseJSON.append("middle_name", middle_name);
				responseJSON.append("orgAccountBalance", orgAccountBalance);
				responseJSON.append("raw_xml_id", raw_xml_id);
				responseJSON.append("sourceip", sourceip);
				responseJSON.append("timeStamp", timeStamp);
				responseJSON.append("transAmount", transAmount);
				responseJSON.append("transId", transId);
				responseJSON.append("transType", transType);
				responseJSON.append("status", status);	
				responseJSON.append("msisdn", msisdn);
					
				
				 
				i++;
			 }
			 
			 responseJSON.append("totalMpesaCounts", total_counts_so_far);
			 
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

	private String convertToMsisdnFormat(String msisdn) {
		if(msisdn==null || msisdn.isEmpty() || msisdn.trim().length()<1)
			return msisdn;
		if(msisdn.trim().startsWith(KE_COUNTRY_CODE))
			return msisdn;
		if(msisdn.trim().startsWith(ZERO)){
			msisdn = msisdn.substring(1);
			msisdn = KE_COUNTRY_CODE.concat(msisdn);
		}else if((msisdn.trim().length()<10) && !msisdn.trim().startsWith(ZERO)){
			msisdn = KE_COUNTRY_CODE.concat(msisdn);
		}
		return msisdn;
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
			 
			 ps = conn.prepareStatement("select count(*) as 'count', statusCode, price from SMSStatLog where date(timeStamp)=? and msisdn=? group by statusCode, price");
				
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
	
	private String convertToPrettyFormat(String date_str) throws ParseException{
		Date date = stringToDate(date_str);
		return convertToPrettyFormat(date);
	}
	
	
	public String convertToPrettyFormat(Date date){
		int day = Integer.parseInt(formatDayOfMonth.format(date));
		String suff  = getDayNumberSuffix(day);
		DateFormat prettier_df = new SimpleDateFormat("d'"+suff+"' E MMM YYYY h:mm a ");
	    return prettier_df.format(date);
	}
	
	public Date stringToDate(String dateStr, String dateformat) throws ParseException{
		DateFormat format = new SimpleDateFormat(dateformat);
		return format.parse(dateStr);
	}
	
	public Date stringToDate(String dateStr) throws ParseException{
		return stringToDate(dateStr,"yyyy-MM-dd HH:m:ss");
	}
	
	public static String getDayNumberSuffix(int day) {
	    if (day >= 11 && day <= 13) {
	        return "th";
	    }
	    switch (day % 10) {
	    case 1:
	        return "st";
	    case 2:
	        return "nd";
	    case 3:
	        return "rd";
	    default:
	        return "th";
	    }
	}

}
