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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author tim
 * URI /controller
 *
 */
public class ResEditorControler extends HttpServlet {
	
	private Logger logger = Logger.getLogger(ResEditorControler.class);
	private DataSource ds;
	private Context initContext;
	private JSONObject requestJSON = null;
	private JSONObject responseJSON = null;
	private PrintWriter writer;
	
	private String DB = "celcom_static_content";

	/**
	 * 
	 */
	private static final long serialVersionUID = 43321L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StringBuffer jb = new StringBuffer();
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
			
			BufferedReader reader = req.getReader();
			   
			  while ((line = reader.readLine()) != null)
		    	  jb.append(line);
			
			logger.info("Incomming json >> " + jb.toString());
			requestJSON = new JSONObject(jb.toString());
			String command = requestJSON.getString("command");
			logger.info("command>> "+command);
			
			if(command.equals("changeRespTxt")){
				changeResponseText(req,resp,conn);
			}else if(command.equals("getRespTexts")){
				getResponseTexts(req,resp,conn);
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
	
	private void getResponseTexts(HttpServletRequest req,
			HttpServletResponse resp, Connection conn) {
		PreparedStatement ps = null;
		
		ResultSet rs = null;
		
		
		
		try {
			
			
			prepare(resp);
			
			 responseJSON.put("success", "false");
			 
			 String query = "";
			 String txt = "";
			 
			 ps = conn.prepareStatement("SELECT * from `celcom_static_content`.`message` where language_id = ? AND `key`= ?");
			 
			 int language_id = Integer.valueOf(requestJSON.getString("language_id"));
			 String key = requestJSON.getString("key");
			 
			 ps.setInt(1, language_id);
			 ps.setString(2, key);
			 
			 rs = ps.executeQuery();
			 
			 String message,description = "";
			 int id;
			 
			 while(rs.next()){
				 id = rs.getInt("id");
				 description = rs.getString("description");
				 message = rs.getString("message");
				 key = rs.getString("key");
				 language_id = rs.getInt("language_id");
				 String lang = StringEscapeUtils.escapeHtml((language_id==1 ? "ENG" : "BM"));
				 int l = message.length();
				
				 responseJSON.append("id", id);
				 responseJSON.append("description", StringEscapeUtils.escapeHtml(description));
				 responseJSON.append("message", StringEscapeUtils.escapeHtml(message));
				 responseJSON.append("key", StringEscapeUtils.escapeHtml(key));
				 responseJSON.append("lang", lang);
				 responseJSON.append("size", l);
			 }
			 
			
			 
			
				
			 responseJSON.put("success", "true");
			
		
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

	private Connection getConnection() {
		
		Connection conn = null;
		
		try {
			
			try{
				if(initContext!=null)
					initContext.close();
			}catch(Exception e){}
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/RESP_EDITOR");
			
			conn = ds.getConnection();
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
		
		}
		
		return conn;
		
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
	
	private void changeResponseText(HttpServletRequest req,
			HttpServletResponse resp, Connection conn) {
		
		PreparedStatement ps = null;
		
		ResultSet rs = null;
		
		
		
		try {
			
			
			prepare(resp);
			
			 responseJSON.put("success", "false");
			 
			 String query = "";
			 String txt = "";
			 
			 ps = conn.prepareStatement("SELECT * from `celcom_static_content`.`message` where id = ?");
			 
			 String id = requestJSON.getString("id");
			 
			 ps.setString(1, id);
			 
			 rs = ps.executeQuery();
			 
			 String status = "";
			 
			 boolean found = false;
			 
			 while(rs.next()){
				 
				 status = rs.getString(1);
				 found = true;
			 }
			 
			 if(!found){
				return;
			 }
			 
			 
			 
			 txt = URLDecoder.decode(requestJSON.getString("text"),"UTF8").trim();
			 
			 ps = conn.prepareStatement("UPDATE `celcom_static_content`.`message` set `message` = ? where id = ?");
				
			 ps.setString(1, txt);
			 
			 ps.setString(2, id);
			 
			
				 
			 ps.execute();
				
			 responseJSON.put("success", "true");
			
		
		 } catch (JSONException e) {
			
			logger.error(e.getMessage(),e);
		
		}catch (SQLException e) {
			
			logger.error(e.getMessage(),e);
		
		} catch (UnsupportedEncodingException e) {
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

	

	private boolean isUserLoggedIn(HttpServletRequest req, Connection con) throws Exception{
		String username = "";
		String password = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equalsIgnoreCase("axiata_username")) {
					username = cookies[i].getValue();
				}
				else if (cookies[i].getName().equalsIgnoreCase("axiata_password")) {
					password = cookies[i].getValue();
				}
			}
		}
		
		username = req.getParameter("username") != null ? req.getParameter("username") : username;
		password = req.getParameter("password") != null ? req.getParameter("password") : password;
		
		boolean isUserLoggedIn = false;
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM voucher_system.users WHERE username = ? AND password = ?");
		pstmt.setString(1,username);
		pstmt.setString(2,password);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			isUserLoggedIn = true;
		}
		rs.close();
		pstmt.close();
		return isUserLoggedIn;
	}

	

	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	@Override
	public void destroy() {
		
		logger.info("RESPONSE_TEXT_EDITOR: in Destroy");
		try {
			
			if(initContext!=null)
				initContext.close();
		
		} catch (NamingException e) {
			
			logger.error(e.getMessage(),e);
		
		}
	
	}

	@Override
	public void init() throws ServletException {
		
		
		try {
			
			initContext = new InitialContext();
			
			ds = (DataSource)initContext.lookup("java:/RESP_EDITOR");
			
			
		} catch (NamingException e) {
			
			log(e);
		
		}
		

	}
	
	
	
	public static void main(String[] ar) throws UnsupportedEncodingException{
		String str = "Anda%20mempunyai%20%3CTOTAL_POINTS%3E%20jumlah%20mata%20%3CNAME%3E%21%20Mari%20teruskan%21%20";
		String s  = URLDecoder.decode(str,"UTF8");
		System.out.println(s);
	}
	
	
	

}
