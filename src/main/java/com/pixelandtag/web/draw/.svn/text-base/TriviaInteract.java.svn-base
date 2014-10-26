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



/**
 * Servlet Class
 *
 * @web.servlet              name="TriviaInteract"
 *                           display-name="Name for TriviaInteract"
 *                           description="Description for TriviaInteract"
 * @web.servlet-mapping      url-pattern="/triviainteract"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class TriviaInteract extends HttpServlet {
	final String DB = "axiata_trivia";
	static final long serialVersionUID = 0;
	Logger log = Logger.getLogger(TriviaInteract.class);
	public TriviaInteract() {
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
			DataSource ds = (DataSource)initContext.lookup("java:/AXIATA_TRIVIA_ONLY");
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
			
			
			PrintWriter out = resp.getWriter();
			
			
			
			out.println("<html>");
			out.println("<body style=\"background-color:#6689AB;\">");
			out.println("<div align=\"center\">");
			out.println("<img src=\"images/axiata_logo.png\"><br>");
			out.println("<h1>Trivia Interact</h1><br>");
			out.println("<form action=\"triviainteract\" method=\"post\">");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			String to = TO != null ? TO : sdf.format(cal.getTime());
			cal.add(Calendar.DATE,-1);
			String from = FROM != null ? FROM : sdf.format(cal.getTime());
			
			if (req.getParameter("submitaward") == null && req.getParameter("submitsearch") == null) {
				out.println("<table>");
				out.println("<tr><td colspan=\"2\">Find users who went inactive</td></tr>");
				out.println("<tr><td>Minimum hours</td><td><input type=\"text\" name=\"minhour\" value=\"\"></td></tr>");
				out.println("<tr><td>Maximum hours</td><td><input type=\"text\" name=\"maxhour\" value=\"\"></td></tr>");
				out.println("<tr><td colspan=\"2\">Find users who send x amount of messages</td></tr>");
				out.println("<tr><td>Minimum messages</td><td><input type=\"text\" name=\"minmsg\" value=\"\"></td></tr>");
				out.println("<tr><td>Maximum messages</td><td><input type=\"text\" name=\"maxmsg\" value=\"\"></td></tr>");
				out.println("<tr><td>From</td><td><input type=\"text\" name=\"from\" value=\""+from+"\"></td></tr>");
				out.println("<tr><td>To</td><td><input type=\"text\" name=\"to\" value=\""+to+"\"></td></tr>");
				out.println("</table><br>");
				out.println("<input type=\"submit\" name=\"submitsearch\">");
			}
			else if (req.getParameter("submitaward") != null) {
				PreparedStatement pstmt = con.prepareStatement("INSERT INTO "+DB+".interact_queue (`from`, `to`, minmsg, maxmsg, message, points, country_id,minhour, maxhour) VALUES(?,?,?,?,?,?,?,?,?)");
				pstmt.setString(1,from);
				pstmt.setString(2, to);
				pstmt.setString(3, req.getParameter("minmsg")!=null?req.getParameter("minmsg"):"0");
				pstmt.setString(4, req.getParameter("maxmsg")!=null?req.getParameter("maxmsg"):"0");
				pstmt.setString(5, req.getParameter("message"));
				pstmt.setString(6, req.getParameter("points"));
				pstmt.setInt(7, getCountryID(req, con));
				pstmt.setString(8, req.getParameter("minhour")!=null?req.getParameter("minhour"):"0");
				pstmt.setString(9, req.getParameter("maxhour")!=null?req.getParameter("maxhour"):"0");
				pstmt.executeUpdate();
				pstmt.close();
				out.println("Update queued");
				out.println("<br><a href=\"triviainteract\">Back</a><br><br>");
			}
			else if (req.getParameter("submitsearch") != null && req.getParameter("minhour") != null && req.getParameter("maxhour") != null && req.getParameter("minhour").length() > 0 && req.getParameter("maxhour").length() > 0) {
				PreparedStatement pstmt = con.prepareStatement("" +
						"SELECT msisdn,COUNT(*) c " +
						"FROM mamas.trivia_master_log " +
						"WHERE miami_timestamp >= SUBDATE(NOW(),INTERVAL +"+Integer.parseInt(req.getParameter("maxhour"))+" HOUR) " +
						"AND miami_timestamp < SUBDATE(NOW(), INTERVAL +"+Integer.parseInt(req.getParameter("minhour"))+" HOUR) " +
						"AND country_id = ? " +
						"AND msisdn NOT IN (" +
						"SELECT msisdn " +
						"FROM mamas.trivia_master_log " +
						"WHERE miami_timestamp > SUBDATE(NOW(), INTERVAL +"+Integer.parseInt(req.getParameter("minhour"))+" HOUR) " +
						"AND country_id = ?) " +
						"GROUP BY msisdn;");
				
				log.debug("SELECT msisdn,COUNT(*) c " +
				"FROM mamas.trivia_master_log " +
				"WHERE miami_timestamp >= SUBDATE(NOW(),INTERVAL +"+Integer.parseInt(req.getParameter("maxhour"))+" HOUR) " +
				"AND miami_timestamp < SUBDATE(NOW(), INTERVAL +"+Integer.parseInt(req.getParameter("minhour"))+" HOUR) " +
				"AND country_id = ? " +
				"AND msisdn NOT IN (" +
				"SELECT msisdn " +
				"FROM mamas.trivia_master_log " +
				"WHERE miami_timestamp > SUBDATE(NOW(), INTERVAL +"+Integer.parseInt(req.getParameter("minhour"))+" HOUR) " +
				"AND country_id = ?) " +
				"GROUP BY msisdn;");
				
				pstmt.setInt(1, getCountryID(req, con));
				pstmt.setInt(2, getCountryID(req, con));
				ResultSet rs = pstmt.executeQuery();
				
				rs.last();
				
				out.println("Customers: " + rs.getRow() + "<br>");
				
				
				out.println("<table border=\"0\">");
				out.println("<tr><td>MSISDN (optional)</td><td><input type=\"text\" name=\"msisdn\"></td></tr>");
				out.println("<tr><td>Send message</td><td><input type=\"text\" name=\"message\"></td></tr>");
				out.println("<tr><td>Award points</td><td><input type=\"text\" name=\"points\" value=\"0\"></td></tr>");
				out.println("</table><br>");
				out.println("<input type=\"hidden\" name=\"from\" value=\""+req.getParameter("from")+"\">");
				out.println("<input type=\"hidden\" name=\"to\" value=\""+req.getParameter("to")+"\">");
				out.println("<input type=\"hidden\" name=\"minhour\" value=\""+req.getParameter("minhour")+"\">");
				out.println("<input type=\"hidden\" name=\"maxhour\" value=\""+req.getParameter("maxhour")+"\">");
				
				out.println("<input type=\"submit\" name=\"submitaward\"><br><br>");

				rs.beforeFirst();

				out.println("<table border=\"0\">");
				out.println("<tr><td>MSISDN</td><td>Messages</td></tr>");
				while(rs.next()) {
					out.println("<tr><td>"+rs.getString("msisdn")+"</td><td>"+rs.getInt("c")+"</td></tr>");
				}
				out.println("</table>");
				rs.close();
				pstmt.close();

				
				out.println("<br><a href=\"triviainteract\">Back</a><br><br>");
			}
			else {
				PreparedStatement pstmt = con.prepareStatement("SELECT msisdn, COUNT(*) c FROM "+DB+".trivia_master_log WHERE DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND country_id = ? GROUP BY msisdn HAVING c >= ? AND c <= ? ORDER BY points DESC");
				pstmt.setString(1,from);
				pstmt.setString(2, to);
				pstmt.setInt(3, getCountryID(req, con));
				pstmt.setString(4, req.getParameter("minmsg"));
				pstmt.setString(5, req.getParameter("maxmsg"));
				ResultSet rs = pstmt.executeQuery();
				
				rs.last();
				
				out.println("Customers: " + rs.getRow() + "<br>");
				
				
				out.println("<table border=\"0\">");
				out.println("<tr><td>Send message</td><td><input type=\"text\" name=\"message\"></td></tr>");
				out.println("<tr><td>Award points</td><td><input type=\"text\" name=\"points\" value=\"0\"></td></tr>");
				out.println("</table><br>");
				out.println("<input type=\"hidden\" name=\"from\" value=\""+req.getParameter("from")+"\">");
				out.println("<input type=\"hidden\" name=\"to\" value=\""+req.getParameter("to")+"\">");
				out.println("<input type=\"hidden\" name=\"minmsg\" value=\""+req.getParameter("minmsg")+"\">");
				out.println("<input type=\"hidden\" name=\"maxmsg\" value=\""+req.getParameter("maxmsg")+"\">");
				out.println("<input type=\"submit\" name=\"submitaward\"><br><br>");

				rs.beforeFirst();

				out.println("<table border=\"0\">");
				out.println("<tr><td>MSISDN</td><td>Messages</td></tr>");
				while(rs.next()) {
					out.println("<tr><td>"+rs.getString("msisdn")+"</td><td>"+rs.getInt("c")+"</td></tr>");
				}
				out.println("</table>");
				rs.close();
				pstmt.close();

				
				out.println("<br><a href=\"triviainteract\">Back</a><br><br>");
			}
		out.println("</div>");
		out.println("</form>");
		out.println("<br><p align=\"center\"><a href=\"logout?url=triviainteract%3Flogout%31\">Logout</a></p>");
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
	out.println("<body style=\"background-color:#6689AB;\">");
	out.println("<div align=\"center\" width=\"100%\">");
	out.println("<img src=\"images/axiata_logo.png\"><br>");
	out.println("<h1>Trivia Interact Module</h1><br>");
	out.println("<form action=\"triviainteract\" method=\"post\">");
	out.println("<table border=\"0\">");
	out.println("<tr><td>Username</td><td><input type=\"text\" name=\"username\"></td></tr>");
	out.println("<tr><td>Password</td><td><input type=\"password\" name=\"password\"></td></tr>");
	out.println("</table>");
	out.println("<input type=\"submit\">");
	out.println("</form>");
	out.println("</div>");
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
