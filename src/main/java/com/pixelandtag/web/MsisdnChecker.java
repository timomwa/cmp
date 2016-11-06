package com.pixelandtag.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import org.apache.log4j.Logger;

/**
 * 
 * @author tim
 * URI /msisdncheck
 *
 */
public class MsisdnChecker extends HttpServlet {
	
	private Logger logger = Logger.getLogger(MsisdnChecker.class);
	private DataSource ds;
	private Context initContext;
	
	private final byte[] OK_200 =  "200 OK".getBytes();
	//private String DB = "cmp";

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
		
		
		
		Connection conn = null;
		PrintWriter out = null;
		
		
		
		
		try {
			
			
			if(req.getParameter("testarabic")!=null){
				
				ServletOutputStream sout = null;
				OutputStreamWriter outwriter = null;
				BufferedWriter bufferedWriter = null;
				
				try{
					
					sout = resp.getOutputStream();
					
					resp.setCharacterEncoding("UTF-8;charset=UTF-8");
					resp.setContentType("text/xml"); 
					
					
					outwriter = new OutputStreamWriter(sout, "UTF8");
					
					bufferedWriter = new BufferedWriter(outwriter);
					
					bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF8\"?><content> three: Madinah atau Madinah Al Munawwarah: مدينة رسول الله atau المدينه, adalah kota utama di Arab Saudi. Merupakan kota yang ramai diziarahi atau dikunjungi oleh kaum Muslimin. Di saat ini, penduduknya sekitar 600.000 jiwa.</content>");
					
					return;
					
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				
				}finally{
					

					try{
						bufferedWriter.close();
					}catch(Exception e){}
					
					try{
						outwriter.close();
					}catch(Exception e){}
					
					try{
						if(sout!=null)
							sout.close();
					}catch(Exception e){
						
					}
					
				}
			
		}
				
			out = resp.getWriter();
			
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try{
				conn = ds.getConnection();
				stmt = conn.prepareStatement("SELECT 'test'");
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
			
			
			
			if (req.getParameter("username") != null && req.getParameter("password") != null) {
				Cookie myCookie = new Cookie("axiata_username",req.getParameter("username"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);
				
				myCookie = new Cookie("axiata_password",req.getParameter("password"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);
			}
			
			if (!isUserLoggedIn(req,conn)) {
				printLoggin(resp);
				return;
			}
			
			
			if(req.getParameter("command")==null){
				
				out.println("<html>");
				out.println("<head>");
				out.println("	<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"> ");
				out.println("	<LINK REL=StyleSheet HREF=\"css/ourcss.css\" TYPE=\"text/css\"/>");
				out.println("	<script type=\"text/javascript\" src=\"js/utils.js\"></script>");
				out.println("	<script type=\"text/javascript\" src=\"js/jquery-1.7.2.min.js\"></script>");
				out.println("	<script type=\"text/javascript\" src=\"js/jquery.json-2.2.min.js\"></script>");
				out.println("	<script type=\"text/javascript\" src=\"js/ourjs.js?v=2\"></script>");
				out.println("</head>");
				
				out.println("<BODY>");
				out.println("<div class='bodyContent' id='bodyC' >");
				 String reqUrl = req.getRequestURL().toString();
				 logger.info("reqUrl::::::::::::::::::: "+reqUrl);
				 reqUrl = reqUrl.split("msisdncheck")[0];
				 
				out.println("<br/><br/><caption> <H2> MSISDN TRANSACTION STAT CHECK </H2> </caption>");
				out.println("<br/><a href=\""+reqUrl+"logout?url=msisdncheck%3Flogout%31\" style=\"color:#0FFF\">Logout</a><br/><br/><br/>");
		
				out.println("<TABLE>");
				out.println("<TR><TD>MSISDN</TD><TD><input name='msisdn' id='msisdn' type='text'/></TD></TR>");
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				//cal.add(Calendar.DATE, -1);
				out.println("<TR><TD>DATE</TD><TD><input name='msisdn' id='date' type='text' value='"+sdf.format(cal.getTime())+"' /></TD></TR>");
				out.println("<TR><TD colspan='2'><img class='pntz' src='images/sms_stat.png' alt='Transaction stats' title='Transaction stats' onclick='TRIVIA.getTxProfile()' />  <a href='javascript:void(0)' onclick='TRIVIA.getTxProfile()'>TX Status</a> | " +
						"<img class='pntz' src='images/msg_log.png' alt='SMS LOG' title='SMS LOG' onclick='TRIVIA.getLogsFor()'/> <a href='javascript:void(0)' onclick='TRIVIA.getLogsFor()'>SMS LOG</a></TD></TR>");
				
				
				//<input type=\"submit\">
				out.println("</TABLE></TR><br/><br/><br/>");
				
				out.println("<div id='stat_div' class='stat_div'></div>");
				
				
			
			}
			
			out.println("</div>");
			out.println("</BODY>");
			out.println("</html>");
			
			
		} catch (SQLException e) {
			
			log(e);
		
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
	
	
	
	private String getSelect(Connection conn, String kEY) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String html = "<SELECT id='"+kEY+"' name='"+kEY+"'> ";
		
		if(kEY.equals("KEY")){
			
			try {
				ps = conn.prepareStatement("select `key`, `description`  FROM `.`message` group by `key`");
				rs = ps.executeQuery();
				
				String key,description;
				while(rs.next()){
					key = StringEscapeUtils.escapeHtml(rs.getString(1));
					description = StringEscapeUtils.escapeHtml(rs.getString(2));
					
					html += "<option value='"+key+"' >"+key+"</option>";
					
					
				}
				
				
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			html += "<option value='"+1+"'>English (ENG)</option>";
			html += "<option value='"+2+"'>Bahasa Malay (BM)</option>";
		}
		
		html += "</SELECT>";
			
		return html;
	}

	private void printLoggin(HttpServletResponse resp) throws Exception{
		PrintWriter out = resp.getWriter();
		
		out.println("<html><head>"
		+"<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\">"
		+"<style>"
		+"body"
		+"{"
		+"font-family:Arial, Helvetica, sans-serif; padding:0; margin:0; background-color:#6689AB;"
		+"}"
		+".content"
		+"{"
		+"width:600px; background-color:#FFF; padding:10px;"
		+"-moz-border-radius: 5px;"
		+"-webkit-border-radius: 5px;"
		+"-moz-box-shadow: 0 1px 2px #444;"
		+"-webkit-box-shadow: 0 1px 2px #444;"
		+"}"
		+"</style>"
		+"</head>"
		+"<body>"
		+"<div width=\"100%\" align=\"center\"><img src=\"images/crossgate_logo.png\" alt=\"\" width=\"510\" height=\"353\">"
		+"<div class=\"content\">"
		+"<form action=\"msisdncheck\" method=\"post\">"
		+"<br>"
		+"<table border=\"0\">"
		+"<tbody><tr><td>Username</td><td><input name=\"username\" type=\"text\"></td></tr>"
		+"<tr><td>Password</td><td><input name=\"password\" type=\"password\"></td></tr>"
		+"</tbody></table>"
		+"<input type=\"submit\">"
		+"</form>"
		+"</div>"
		+"</div>"
		+"</body></html>");
		
		out.close();
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
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM user WHERE u_name = ? AND u_pwd = ?");
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

	private String tabilizeAllResponseTexts(Connection conn, String keyM, int languageID) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM `message` WHERE `key`= ? and `language_id` = ?";
		String table = "<TABLE id=\"mytable\" cellspacing=\"0\"> ";
		table += "<TR id='header' > <th scope=\"col\" class=\"nobg\"> MESSAGE </th> <th scope=\"col\"> SIZE </th> <th scope=\"col\"> LANG </th> <th scope=\"col\"> DESCRIPTION </th> <th scope=\"col\"> KEY </th  </TR>";
		
		try {
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, keyM);
			pstmt.setInt(2, languageID);
			
			rs = pstmt.executeQuery();
			
			String id,language_id,description,key,message;
			
			while(rs.next()){
				
				id = StringEscapeUtils.escapeHtml(rs.getString("id"));
				language_id = StringEscapeUtils.escapeHtml(rs.getString("language_id"));
				description = StringEscapeUtils.escapeHtml(rs.getString("description"));
				key = StringEscapeUtils.escapeHtml(rs.getString("key"));
				message = StringEscapeUtils.escapeHtml(rs.getString("message"));
				String lang = StringEscapeUtils.escapeHtml((language_id.equals("1") ? "ENG" : "BM"));
				int l = message.length();
				
				String spanC = l > 80 ? "red" : "green";
				
				table += "<TR id='resp_rec_"+id+"'> <TD id='msg_msg_"+id+"' width='250'> <textarea class=\"tastyled\"" +
						"onfocus=\"setbg('#e5fff3');\" onblur=\"setbg('white')\"  id='msg_txt_"+id+"' rows=\"4\" cols= \"80\" > "+message+" </textarea> " +
								"<img class='pntz lefters' src='images/save.png' alt='Save' onclick=\"saveText('"+id+"')\"/> <span class='clearer'>&nbsp;</span></TD> " +
						"<TD id='resp_lang_"+id+"' class='tiny_font'> <span class='"+spanC+"'/> "+l+" </TD> <TD id='resp_lang_"+id+"' class='tiny_font'> "+lang+" </TD> <TD width='50' id='resp_desc_"+id+"' class='medium_font'> "+description+" </TD> <TD id='resp_key_"+id+"' class='tiny_font'> "+key+" </TD>  </TR>";
				
			}
			
			table += "</TABLE>";
			
		} catch (SQLException e) {
			log(e);
		}finally{
			
			try {
				if(rs!=null)
					rs.close();
			} catch (Exception e) {
				log(e);
			}
			
			
			try {
				if(pstmt!=null)
					pstmt.close();
			} catch (SQLException e) {
				log(e);
			}
		}
		
		return table;
	}

	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	
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
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			
		} catch (NamingException e) {
			
			log(e);
		
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
			
			ds = (DataSource)initContext.lookup("java:/cmpDS");
			
			conn = ds.getConnection();
			
		} catch (Exception e) {
			
			log(e);
		
		}finally{
		
		}
		
		return conn;
		
	}
	
	
	

}
