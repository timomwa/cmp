package com.pixelandtag.web.draw;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * Servlet Class
 *
 * @web.servlet              name="MakeDraw"
 *                           display-name="Name for MakeDraw"
 *                           description="Description for MakeDraw"
 * @web.servlet-mapping      url-pattern="/makedraw"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class MakeDraw extends HttpServlet {
	final String DB = "pixeland_content360";
	static final long serialVersionUID = 3425345L;
	Logger log = Logger.getLogger(MakeDraw.class);
	public MakeDraw() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		Connection con = null;
		try {
			Context initContext = new InitialContext();
			//DataSource ds = (DataSource)initContext.lookup("java:/VasDS");
			DataSource ds = (DataSource)initContext.lookup("java:/cmpDS");
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
			
			out.print("<head><style>"+
						"body"+
						"{"+
						"	font-family:Arial, Helvetica, sans-serif; padding:0; margin:0; background-color:#ed1c24;"+
						"}"+
						".content"+
						"{"+
						"	width:600px; background-color:#FFF; padding:10px;"+
						"	-moz-border-radius: 5px;"+
						"	-webkit-border-radius: 5px;"+
						"	-moz-box-shadow: 0 1px 2px #444;"+
						" 	-webkit-box-shadow: 0 1px 2px #444;"+
						"}" +
						"</head>");
			out.println("<body>");
			out.println("<div align=\"center\">");
			out.println("<img src=\""+((getCountryID(req, con)==7) ? "images/axiata_logo.png" : "images/axiata_logo.png")+"\" width=\"400\"><br>");
			out.println("<h1>Winner Search</h1><br>");
			out.println("<form action=\"makedraw\" method=\"post\">");
			PreparedStatement pstmt = null;
			if (req.getParameter("from") == null || req.getParameter("back") != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE,-1);
				out.println("<table>");
				out.println("<tr><td>From</td><td><input type=\"text\" name=\"from\" value=\""+sdf.format(cal.getTime())+"\"></td></tr>");
				out.println("<tr><td>Winners</td><td><input type=\"text\" name=\"numbers\" value=\"5\"></td></tr>");
				out.println("</table>");
				out.println("<input type=\"submit\"><br>");
			}
			else {
				out.println("<input type=\"hidden\" name=\"from\" value=\""+req.getParameter("from")+"\">");
				out.println("<input type=\"hidden\" name=\"numbers\" value=\""+req.getParameter("numbers")+"\">");
				
				
				
				if (req.getParameter("random") == null) { 
					out.println("<input type=\"submit\" name=\"random\" value=\"Make draw.\"><br>");
					
					
					if(getCountryID(req,con)==7){
						pstmt = con.prepareStatement("" +
								"SELECT msisdn FROM (" +
								"(SELECT msisdn FROM "+DB+".trivia_master_log WHERE timestamp < CURDATE() AND timestamp >= ? AND country_id in (?,18)) " +
								"UNION ALL " +
								"(SELECT msisdn FROM "+DB+".vote_master_log WHERE accepted = 1 AND timestamp < CURDATE() AND timestamp >= ? AND country_id in (?,?))" +
								") AS t ");
					}else{
						pstmt = con.prepareStatement("" +
								"SELECT msisdn FROM (" +
								"(SELECT msisdn FROM "+DB+".trivia_master_log WHERE timestamp < CURDATE() AND timestamp >= ? AND country_id = ?) " +
								"UNION ALL " +
								"(SELECT msisdn FROM "+DB+".vote_master_log WHERE accepted = 1 AND timestamp < CURDATE() AND timestamp >= ? AND country_id = ?)" +
								") AS t ");
					}
				}
				else 
					if(getCountryID(req,con)==7){
						pstmt = con.prepareStatement("" +
								"SELECT msisdn FROM (" +
								"(SELECT msisdn FROM "+DB+".trivia_master_log WHERE timestamp < CURDATE() AND timestamp >= ? AND country_id in (?,18)) " +
								"UNION " +
								"(SELECT msisdn FROM "+DB+".vote_master_log WHERE accepted = 1 AND timestamp < CURDATE() AND timestamp >= ? AND country_id in (?,?))" +
								") AS t ORDER BY RAND()");
					}else{
						pstmt = con.prepareStatement("" +
								"SELECT msisdn FROM (" +
								"(SELECT msisdn FROM "+DB+".trivia_master_log WHERE timestamp < CURDATE() AND timestamp >= ? AND country_id = ?) " +
								"UNION " +
								"(SELECT msisdn FROM "+DB+".vote_master_log WHERE accepted = 1 AND timestamp < CURDATE() AND timestamp >= ? AND country_id = ?)" +
								") AS t ORDER BY RAND()");
					}
				
				out.println("<input type=\"submit\" name=\"back\" value=\"Back\"><br>");
				out.println("<br>");
				
				pstmt.setString(1,req.getParameter("from"));
				pstmt.setInt(2,getCountryID(req,con));
				pstmt.setString(3,req.getParameter("from"));
				pstmt.setInt(4,getCountryID(req,con));
				ResultSet rs = pstmt.executeQuery();
				out.println("<table border=\"0\">");
				out.println("<tr><td>#</td><td>MSISDN</td></tr>");

				HashMap hm = new HashMap();
				int maxwinners = Integer.parseInt(req.getParameter("numbers"));
				int i = 0;
				while (rs.next()) {
					if (req.getParameter("random") == null || !hm.containsKey(rs.getString("msisdn"))) {
						i++;
						hm.put(rs.getString("msisdn"),"");
						out.println("<tr><td style=\"padding-right:10px;\">"+i+"</td><td>"+rs.getString("msisdn")+"</td></tr>");
						
					}
					if (req.getParameter("random") != null && i==maxwinners)
						break;
				}
				hm.clear();
				out.println("</table>");
			}
			out.println("</div>");
			out.println("</form>");
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
		out.println("<body>");
		out.println("<div align=\"center\" width=\"100%\">");
		out.println("<img src=\"images/airtel.jpg\"><br>");
		out.println("<h1>Winner Search</h1><br>");
		out.println("<form action=\"makedraw\" method=\"post\">");
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
