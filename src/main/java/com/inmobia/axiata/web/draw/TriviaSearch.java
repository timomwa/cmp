package com.inmobia.axiata.web.draw;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.inmobia.util.POI;



/**
 * Servlet Class
 *
 * @web.servlet              name="TriviaSearch"
 *                           display-name="Name for TriviaSearch"
 *                           description="Description for TriviaSearch"
 * @web.servlet-mapping      url-pattern="/triviasearch"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class TriviaSearch extends HttpServlet {
	final String DB = "axiata_trivia";
	static final long serialVersionUID = 0;
	Logger log = Logger.getLogger(TriviaSearch.class);
	public TriviaSearch() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException,
	IOException {
		Connection con = null;
		try {
			final String FROM = req.getParameter("from");
			final String TO = req.getParameter("to");
			final String MSISDN = req.getParameter("msisdn");
			Context initContext = new InitialContext();
			//DataSource ds = (DataSource)initContext.lookup("java:/VasDS");
			DataSource ds = (DataSource)initContext.lookup("java:/SMSDS");
			con = ds.getConnection();
			
			if (req.getParameter("username") != null && req.getParameter("password") != null) {
				Cookie myCookie = new Cookie("mamas_username",req.getParameter("username"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);
				
				myCookie = new Cookie("mamas_password",req.getParameter("password"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);
			}
			
			if (!isUserLoggedIn(req,con)) {
				printLoggin(resp);
				return;
			}
			
			if (req.getParameter("getreport") != null) {
				HSSFWorkbook wb = new HSSFWorkbook();
				POI poi = new POI();
				HSSFSheet s = poi.createSheet("Report","",wb);
				PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "+DB+".trivia_master_log WHERE DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND country_id = ? ORDER BY timestamp ASC");
				pstmt.setString(1,FROM);
				pstmt.setString(2,TO);
				pstmt.setInt(3,getCountryID(req,con));
				ResultSet rs = pstmt.executeQuery();
				poi.writeCell(s,null, 0,0,"MSISDN");
				poi.writeCell(s,null,0,1,"Name");
				poi.writeCell(s,null,0,2,"Points");
				poi.writeCell(s,null,0,3,"Correct");
				
				while (rs.next()) {
					poi.writeCell(s,null,rs.getRow(),0,rs.getString("msisdn"));
					poi.writeCell(s,null,rs.getRow(),1,rs.getString("name"));
					poi.writeCell(s,null,rs.getRow(),2,rs.getBoolean("correct")?50000:10000);
					if (rs.getInt("question_id")>0)
						poi.writeCell(s,null,rs.getRow(),3,rs.getBoolean("correct"));
				}
				rs.close();
				pstmt.close();
				String name = FROM+"-"+TO+".xls";
				if (FROM.equalsIgnoreCase(TO))
					name = FROM+".xls";
					
				
				resp.setContentType("text/plain");
				resp.setHeader("Content-Disposition","attachment; filename=\""+name+"\";");
				ServletOutputStream sos = resp.getOutputStream();
				wb.write(sos);
				
				return;
			}
			PrintWriter out = resp.getWriter();
			
			
			
			out.println("<html>");
			out.print("<head><style>"+
					"body"+
					"{"+
					"font-family:Arial, Helvetica, sans-serif; padding:0; margin:0; background-color:#6689AB;"+
					"}"+
					".content"+
					"{"+
					"width:600px; background-color:#FFFFCC; padding:10px;"+
					"-moz-border-radius: 5px;"+
					"-webkit-border-radius: 5px;"+
					"-moz-box-shadow: 0 1px 2px #444;"+
					"-webkit-box-shadow: 0 1px 2px #444;"+
					"}"
					+"h2{"
					+"	padding-top:0;"
					+"	margin-top:0;"
					+"	border-bottom:1px dashed #999;"
					+"}" +
					".odd{background-color:#CCFFCC;}" +
					".even{background-color:#FFFFCC;}" +
					".firstRow{background-color:#FF9933; font-weight: bold; font-size:16px;}" +
					"</style></head>");
			out.println("<body>");
			out.println("<div align=\"center\">");
			out.println("<img src=\"images/airtel.png\"><br>");
			out.println("<div class=\"content\"><h1>Trivia Search</h1><br>");
			out.println("<form action=\"triviasearch\" method=\"post\">");
			PreparedStatement pstmt = null;
			if (req.getParameter("submitsearch") == null || MSISDN == null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				String to = TO != null ? TO : sdf.format(cal.getTime());
				cal.add(Calendar.DATE,-1);
				String from = FROM != null ? FROM : sdf.format(cal.getTime());
				
				out.println("<table>");
				out.println("<tr><td>MSISDN</td><td><input type=\"text\" name=\"msisdn\" value=\"\"></td><td><input type=\"submit\" name=\"submitsearch\" value=\"Search\"></td></tr>");
				out.println("<tr><td>From</td><td><input type=\"text\" name=\"from\" value=\""+from+"\"></td><td>&nbsp;</td></tr>");
				out.println("<tr><td>To</td><td><input type=\"text\" name=\"to\" value=\""+to+"\"></td><td><input type=\"submit\" name=\"getreport\" value=\"Get Report\"></td></tr>");
				out.println("</table>");
			}
			else {
				pstmt = con.prepareStatement("SELECT points, timestamp, correct,msisdn, question_id FROM "+DB+".trivia_master_log WHERE msisdn = ? AND country_id = ? ORDER BY timestamp ASC");
				pstmt.setString(1,MSISDN);
				pstmt.setInt(2,getCountryID(req,con));
				ResultSet rs = pstmt.executeQuery();
				
				StringBuffer htmlsb = new StringBuffer();
				int points = 0;
				int i = 0;
				while (rs.next()) {
					i++;
					htmlsb.append("<tr class="+((i%2)>0 ? "'even'" : "'odd'")+"><td>"+rs.getString("msisdn")+"</td><td>"+rs.getString("timestamp")+"</td><td>"+(rs.getInt("question_id") > 0 ? rs.getBoolean("correct") : "&nbsp;")+"</td></tr>");
					points += rs.getInt("points");
				}
				out.println("<br><a href=\"triviasearch\">Back</a><br><br>");
			if (points == 0)
				out.println("No answers found for this user.");
			else {
				out.println("Total points: " + points);
				out.println("<table border=\"1\">");
				out.println("<tr><td>MSISDN</td><td>Timestamp</td><td>Correct Answer</td></tr>");
				out.println(htmlsb.toString());
				out.println("</table>");
			}
			}
		out.println("</div></div>");
		String reqUrl = req.getRequestURL().toString();
		 log.info("reqUrl::::::::::::::::::: "+reqUrl);
		 reqUrl = reqUrl.split("maketriviadraw")[0];
		out.println("</form>"
		+"<br><p align=\"center\"><a href=\""+reqUrl+"logout?url=triviasearch%3Flogout%31\" style=\"color:#FFF\">Logout</a></p>");
		out.println("</body>");
		out.println("</html>");
		
	}
	catch (Exception e) {
		log.error(e,e);
	}
	finally { try { con.close(); } catch (Exception e) { } }
}

protected void doPost(HttpServletRequest req, HttpServletResponse resp)
throws ServletException,
IOException {
	doGet(req,resp);
}
private void printLoggin(HttpServletResponse resp) throws Exception{
	PrintWriter out = resp.getWriter();
	out.println("<html>");
	out.print("<head><style>"+
			"body"+
			"{"+
			"font-family:Arial, Helvetica, sans-serif; padding:0; margin:0; background-color:#ed1c24;"+
			"}"+
			".content"+
			"{"+
			"width:600px; background-color:#FFF; padding:10px;"+
			"-moz-border-radius: 5px;"+
			"-webkit-border-radius: 5px;"+
			"-moz-box-shadow: 0 1px 2px #444;"+
			"-webkit-box-shadow: 0 1px 2px #444;"+
			"}"
			+"h2{"
			+"	padding-top:0;"
			+"	margin-top:0;"
			+"	border-bottom:1px dashed #999;"
			+"}" +
			".odd{background-color:#FFCC66;}" +
			".even{background-color:#FF9966;}" +
			"</style></head>");
	out.println("<body>");
	out.println("<div align=\"center\" width=\"100%\">");
	out.println("<img src=\"images/airtel.png\"><br>");
	out.println("<div class=\"content\"><h1>Trivia Search</h1><br>");
	out.println("<form action=\"triviasearch\" method=\"post\">");
	out.println("<table border=\"0\">");
	out.println("<tr><td>Username</td><td><input type=\"text\" name=\"username\"></td></tr>");
	out.println("<tr><td>Password</td><td><input type=\"password\" name=\"password\"></td></tr>");
	out.println("</table>");
	out.println("<input type=\"submit\">");
	out.println("</form>");
	out.println("</div></div>");
	out.println("</body>");
	out.println("</html>");
}
private int getCountryID (HttpServletRequest req, Connection con) throws Exception{
	String username = "";
	String password = "";
	Cookie[] cookies = req.getCookies();
	if (cookies != null) {
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equalsIgnoreCase("mamas_username")) {
				username = cookies[i].getValue();
			}
			else if (cookies[i].getName().equalsIgnoreCase("mamas_password")) {
				password = cookies[i].getValue();
			}
		}
	}
	
	username = req.getParameter("username") != null ? req.getParameter("username") : username;
	password = req.getParameter("password") != null ? req.getParameter("password") : password;
	
	int country_id = 0;
	PreparedStatement pstmt = con.prepareStatement("SELECT country_id FROM "+DB+".user WHERE username = ? AND password = ?");
	pstmt.setString(1,username);
	pstmt.setString(2,password);
	ResultSet rs = pstmt.executeQuery();
	if (rs.next()) {
		country_id = rs.getInt("country_id");
	}
	rs.close();
	pstmt.close();
	return country_id;
}
private boolean isUserLoggedIn(HttpServletRequest req, Connection con) throws Exception{
	String username = "";
	String password = "";
	Cookie[] cookies = req.getCookies();
	if (cookies != null) {
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equalsIgnoreCase("mamas_username")) {
				username = cookies[i].getValue();
			}
			else if (cookies[i].getName().equalsIgnoreCase("mamas_password")) {
				password = cookies[i].getValue();
			}
		}
	}
	
	username = req.getParameter("username") != null ? req.getParameter("username") : username;
	password = req.getParameter("password") != null ? req.getParameter("password") : password;
	
	boolean isUserLoggedIn = false;
	PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "+DB+".user WHERE username = ? AND password = ?");
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

}
