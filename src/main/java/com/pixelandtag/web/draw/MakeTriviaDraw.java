package com.pixelandtag.web.draw;


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
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.util.POI;



/**
 * Servlet Class
 *
 * @web.servlet              name="maketriviadraw"
 *                           display-name="Name for maketriviadraw"
 *                           description="Description for maketriviadraw"
 * @web.servlet-mapping      url-pattern="/maketriviadraw"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class MakeTriviaDraw extends HttpServlet {
	
	
	
	static final long serialVersionUID  = 1135L;
	final String DB = "cmp";
	int message_input_size = 300;
	Logger log = Logger.getLogger(MakeTriviaDraw.class);
	public MakeTriviaDraw() {
		super();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		Connection con = null;
		try {
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
			
			if (req.getParameter("download_session")!=null) {
				POI poi = new POI();
				
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet s = poi.createSheet("Winner", "", wb);
				s.setColumnWidth(0,(short)5000);
				s.setColumnWidth(1,(short)5000);
				
				PreparedStatement pstmt = con.prepareStatement("SELECT `name`, `from`, `to`,`timestamp`,`msisdn` FROM "+DB+".winners_drawn d, "+DB+".winners_sessions s WHERE s.id = d.session_id AND s.id= ?");
				pstmt.setString(1, req.getParameter("session_id"));
				ResultSet rs = pstmt.executeQuery();

				
				int r = 0;
				
				while (rs.next()) {
					if (rs.isFirst()) {
						poi.writeCell(s, null, r, 0, "Name");
						poi.writeCell(s, null, r, 1, rs.getString("name"));
						poi.writeCell(s, null, ++r, 0, "From");
						poi.writeCell(s, null, r, 1, rs.getString("from"));
						poi.writeCell(s, null, ++r, 0, "To");
						poi.writeCell(s, null, r, 1, rs.getString("to"));
						poi.writeCell(s, null, ++r, 0, "Timestamp");
						poi.writeCell(s, null, r++, 1, rs.getString("timestamp"));
						poi.writeCell(s, null, ++r, 0, "Msisdn");
					}
					
					poi.writeCell(s, null, ++r, 0, rs.getString("msisdn"));
				}
				
				
				rs.close();
				pstmt.close();				
				
				resp.setContentType("application/xls");
				resp.setHeader("Content-Disposition","attachment; filename=\"draw.xls\";");
				ServletOutputStream sos = resp.getOutputStream();
				wb.write(sos);
				
				try { con.close(); } catch (Exception e) { }
				return;
				
			}
			
			PrintWriter out = resp.getWriter();
			boolean datesValidate = true;
			if (req.getParameter("from") != null && req.getParameter("to") != null) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					if (sdf.parse(req.getParameter("from")).getTime() > sdf.parse(req.getParameter("to")).getTime()) {
						datesValidate = false;
					}
					if (sdf.parse(req.getParameter("to")).getTime() > sdf.parse(sdf.format(Calendar.getInstance().getTime())).getTime()) {
						datesValidate = false;
					}
				}
				catch (Exception e) {
					datesValidate = false;
					log.debug(e,e);
				}
			}
			
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
			
			
			out.println("<div align=\"center\"><img src=\""+((getCountryID(req, con)==7) ? "images/axiata_logo.png" : "images/axiata_logo.png")+"\" alt=\"\" width=\"510\" height=\"353\"><br><br>"
					+"<div class=\"content\">"
					+"<h2>"+UtilCelcom.getCountryName(con, getCountryID(req, con))+"</h2>"
					+"<form action=\"maketriviadraw\" method=\"post\">");
					
			
			PreparedStatement pstmt = null;
			if (req.getParameter("browse_session")!= null) {
				out.println("<input type=\"hidden\" name=\"session_id\" value=\""+req.getParameter("session_id")+"\">");
				
				pstmt = con.prepareStatement("SELECT * FROM "+DB+".country WHERE id = ?");
				pstmt.setInt(1, getCountryID(req, con));
				ResultSet rs = pstmt.executeQuery();
				boolean do_not_print_send_message = rs.next() ? rs.getBoolean("do_not_print_send_message") : false;
				rs.close();
				pstmt.close();
				
				if (!do_not_print_send_message)
					out.println("MessageEmail <input type=\"text\" style=\"width: "+message_input_size+"px;\"  name=\"message\" values=\"\"> <input type=\"submit\" name=\"send_message\" value=\"Send MessageEmail\"><br/> ");
				
				out.println("<table border=\"0\" cellpadding=\"10\">");
				out.println("<tr><td>MSISDN</td></tr>");
				
				pstmt = con.prepareStatement("SELECT * FROM "+DB+".winners_drawn WHERE session_id = ?");
				pstmt.setString(1, req.getParameter("session_id"));
				rs = pstmt.executeQuery();

				int n = 0;
				while (rs.next()) {
					n++;
					out.println("<tr class="+((n%2)>0 ? "'even'" : "'odd'")+"><td>"+rs.getString("msisdn")+"</td></tr>");
				}
				
				out.println("<input type=\"submit\" name=\"download_session\" value=\"Download\"><br>");
				out.println("<input type=\"submit\" name=\"back\" value=\"Back\"><br>");
				
				rs.close();
				pstmt.close();				
			}
			else if (req.getParameter("send_message") != null) {
				pstmt = con.prepareStatement("SELECT msisdn FROM "+DB+".winners_drawn WHERE session_id = ?");
				pstmt.setString(1, req.getParameter("session_id"));
				ResultSet rs = pstmt.executeQuery();
				PreparedStatement pstmt1 = con.prepareStatement("INSERT INTO "+DB+".interact_queue (msisdn, message, country_id) VALUES(?,?,?)");
				while (rs.next()) {
					pstmt1.setString(1, rs.getString("msisdn"));
					pstmt1.setString(2, req.getParameter("message"));
					pstmt1.setInt(3, getCountryID(req, con));
					pstmt1.addBatch();
				}
				pstmt1.executeBatch();
				rs.close();
				pstmt.close();
				pstmt1.close();
                pstmt = con.prepareStatement("update "+DB+".winners_sessions set sms_sent=1 where id=?");
                pstmt.setString(1, req.getParameter("session_id"));
                pstmt.executeUpdate();
                pstmt.close();
				out.println("Messages have been queued.<br>");
				out.println("<input type=\"submit\" name=\"back\" value=\"Back\"><br>");
			}
			else if (!datesValidate || req.getParameter("from") == null || req.getParameter("back") != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE,-1);
				
				if (!datesValidate)
					out.println("Please check the dates entered as there is a problem.");
				
				
				out.println("<table>"
				+"<tbody><tr><td>Name of Bucket</td><td><input name=\"name\" value=\"Bucket\" type=\"text\"></td></tr>"
				+"<tr><td>From</td><td><input name=\"from\" value=\""+sdf.format(cal.getTime())+"\" type=\"text\"></td></tr>"
				+"<tr><td>To</td><td><input name=\"to\" value=\""+sdf.format(cal.getTime())+"\" type=\"text\"></td></tr>"
				+"<tr><td>Winners</td><td><input name=\"numbers\" value=\"5\" type=\"text\"></td></tr>"
				+"</tbody></table>"
				+"<input type=\"submit\"><br>");
				
				/*
				out.println("<table>");
				out.println("<tr><td>Name of draw</td><td><input type=\"text\" name=\"name\" value=\"No name\"></td></tr>");				
				out.println("<tr><td>From</td><td><input type=\"text\" name=\"from\" value=\""+sdf.format(cal.getTime())+"\"></td></tr>");
				out.println("<tr><td>To</td><td><input type=\"text\" name=\"to\" value=\""+sdf.format(cal.getTime())+"\"></td></tr>");
				out.println("<tr><td>Winners</td><td><input type=\"text\" name=\"numbers\" value=\"5\"></td></tr>");
				out.println("</table>");
				out.println("<input type=\"submit\"><br>");*/
				
				pstmt = con.prepareStatement("SELECT * FROM "+DB+".winners_sessions WHERE user_id = ?");
				pstmt.setInt(1, getUserID(req, con));
				ResultSet rs = pstmt.executeQuery();
				out.println("<table  border=\"0\" cellpadding=\"10\">");
				int x = 0;
				while (rs.next()) {
					x++;
                    boolean show_link = (rs.getInt("sms_sent")==0);
					if (rs.isFirst()) 
						out.println("<tr class=\"firstRow\"><td>Name</td><td>From</td><td>To</td><td>Timestamp</td></tr>");
                    String link="<td><a href=\"maketriviadraw?browse_session=1&session_id="+rs.getInt("id")+"\">"+rs.getString("name")+"</a></td>" ;
                    if (!show_link)
                        link="<td>"+rs.getString("name")+"</td>" ;
					out.println("" +
							"<tr class="+((x%2)>0 ? "'odd'":"'even'")+">" +
							link +
							"<td>"+rs.getString("from")+"</td>" +
							"<td>"+rs.getString("to")+"</td>" +
							"<td>"+rs.getString("timestamp")+"</td>" +
							"</tr>");
				}
				rs.close();
				pstmt.close();
					
				out.println("</table>");
			}
			else {
				out.println("<input type=\"hidden\" name=\"from\" value=\""+req.getParameter("from")+"\">");
				out.println("<input type=\"hidden\" name=\"to\" value=\""+req.getParameter("to")+"\">");
				out.println("<input type=\"hidden\" name=\"name\" value=\""+req.getParameter("name")+"\">");
				out.println("<input type=\"hidden\" name=\"numbers\" value=\""+req.getParameter("numbers")+"\">");
				out.println("<input type=\"hidden\" name=\"country_id____phew\" value=\""+getCountryID(req,con)+"\">");
				
				out.println("<input type=\"submit\" name=\"back\" value=\"Back\"><br>");

				if (req.getParameter("random") == null) { 
					out.println("<input type=\"submit\" name=\"random\" value=\"Make draw.\"><br>");
					
					if(getCountryID(req,con)==7){
						pstmt = con.prepareStatement("SELECT msisdn, SUM(points) points FROM "+DB+".trivia_master_log WHERE DATE(timestamp) <= ? AND timestamp >= ? AND country_id in (?,?) AND points > 0 AND msisdn NOT IN (" +
								"   SELECT msisdn " +
								"   FROM "+DB+".blacklist_winners " +
								") GROUP BY msisdn ORDER BY points DESC");
						
						pstmt.setString(1,req.getParameter("to"));
						pstmt.setString(2,req.getParameter("from"));
						pstmt.setInt(3,getCountryID(req,con));
						pstmt.setInt(4,18);
					}else{
						pstmt = con.prepareStatement("SELECT msisdn, SUM(points) points FROM "+DB+".trivia_master_log WHERE DATE(timestamp) <= ? AND timestamp >= ? AND country_id = ? AND points > 0 AND msisdn NOT IN (" +
								"   SELECT msisdn " +
								"   FROM "+DB+".blacklist_winners " +
								") GROUP BY msisdn ORDER BY points DESC");
						
						pstmt.setString(1,req.getParameter("to"));
						pstmt.setString(2,req.getParameter("from"));
						pstmt.setInt(3,getCountryID(req,con));
					}
					ResultSet rs = pstmt.executeQuery();
					out.println("<table border=\"0\" cellpadding=\"10\"> ");
					out.println("<tr class=\"firstRow\"><td>Points</td><td>MSISDN</td></tr>");
					int players = 0;
					int total_points = 0;
					int n = 0;
					
					while (rs.next()) {
						n++;
						total_points += rs.getInt("points");
						out.println("<tr class="+((n%2)>0 ? "'even'" : "'odd'")+"><td>"+rs.getString("points")+"</td><td>"+rs.getString("msisdn")+"</td></tr>");
						if (rs.isLast())
							players = rs.getRow();
					}
					rs.close();
					pstmt.close();
					out.println("<input type=\"hidden\" name=\"total_players\" value=\""+players+"\">");
					out.println("<input type=\"hidden\" name=\"total_points\" value=\""+total_points+"\">");
					out.println("<tr class=\"firstRow1\"><td colspan='2'>subs chosen: "+players+"</td></tr>");
					out.println("</table>");
					
				}
				else {
					
					if(getCountryID(req,con)==7){
						pstmt = con.prepareStatement("" +
								"SELECT msisdn, SUM(points) points " +
								"FROM "+DB+".trivia_master_log " +
								"WHERE DATE(timestamp) <= ? " +
								"AND timestamp >= ? " +
								"AND country_id in (?,?) " +
								"AND points > 0 " + 
								"AND msisdn NOT IN (" +
								"   SELECT msisdn " +
								"   FROM "+DB+".blacklist_winners " +
								") " +
								"GROUP BY msisdn " +
								"ORDER BY points DESC");
						pstmt.setString(1,req.getParameter("to"));
						pstmt.setString(2,req.getParameter("from"));
						pstmt.setInt(3,getCountryID(req,con));
						pstmt.setInt(4,18);
					}else{
						pstmt = con.prepareStatement("" +
								"SELECT msisdn, SUM(points) points " +
								"FROM "+DB+".trivia_master_log " +
								"WHERE DATE(timestamp) <= ? " +
								"AND timestamp >= ? " +
								"AND country_id = ? " +
								"AND points > 0 " + 
								"AND msisdn NOT IN (" +
								"   SELECT msisdn " +
								"   FROM "+DB+".blacklist_winners " +
								") " +
								"GROUP BY msisdn " +
								"ORDER BY points DESC");
						pstmt.setString(1,req.getParameter("to"));
						pstmt.setString(2,req.getParameter("from"));
						pstmt.setInt(3,getCountryID(req,con));
					}
					ResultSet rs = pstmt.executeQuery();
					
					Vector winners = new Vector();
					if (Integer.parseInt(req.getParameter("numbers")) >= Integer.parseInt(req.getParameter("total_players"))) {
						while (rs.next())
							winners.add(rs.getString("msisdn"));
					}
					else {
						while (winners.size() < Integer.parseInt(req.getParameter("total_players")) && winners.size() < Integer.parseInt(req.getParameter("numbers"))) 
							winners = addAWinner(rs, Integer.parseInt(req.getParameter("total_points")), winners);
					}
					rs.close();
					pstmt.close();
					pstmt = con.prepareStatement("SELECT * FROM "+DB+".country WHERE id = ?");
					pstmt.setInt(1, getCountryID(req, con));
					rs = pstmt.executeQuery();
					boolean do_not_print_send_message = rs.next() ? rs.getBoolean("do_not_print_send_message") : false;
					rs.close();
					pstmt.close();
					
					if (!do_not_print_send_message)
						out.println("MessageEmail <input type=\"text\" style=\"width: "+message_input_size+"px;\" name=\"message\" values=\"\"> <input type=\"submit\" name=\"send_message\" value=\"Send MessageEmail\"><br/> ");
					
					pstmt = con.prepareStatement("INSERT INTO "+DB+".winners_sessions (user_id, `from`,`to`, name, timestamp) VALUES(?,?,?,?,NOW())");
					pstmt.setInt(1,getUserID(req, con));
					pstmt.setString(2, req.getParameter("from"));
					pstmt.setString(3, req.getParameter("to"));
					pstmt.setString(4, req.getParameter("name"));
					pstmt.executeUpdate();
					rs = pstmt.getGeneratedKeys();
					rs.next();
					int id = rs.getInt(1);
					rs.close();
					pstmt.close();
					out.println("<input type=\"hidden\" name=\"session_id\" value=\""+id+"\">");
					
					out.println("<table border=\"0\">");
					out.println("<tr><td>MSISDN</td></tr>");
					
					pstmt = con.prepareStatement("INSERT INTO "+DB+".winners_drawn (session_id, msisdn) VALUES (?,?)");
					for (int i = 0; i < winners.size();i++) {
						pstmt.setInt(1,id);
						pstmt.setString(2, (String)winners.get(i));
						pstmt.addBatch();
						out.println("<tr><td>"+winners.get(i)+"</td></tr>");
					}
					pstmt.executeBatch();
					pstmt.close();
					out.println("</table>");
				}
				
				
				out.println("<br>");
				
			}
			 String reqUrl = req.getRequestURL().toString();
			 log.info("reqUrl::::::::::::::::::: "+reqUrl);
			 reqUrl = reqUrl.split("maketriviadraw")[0];
			out.println("<table>"
			+"</table>"
			+"</form>"
			+"</div>"
			+"</div>"
			//+"<br><p align=\"center\"><a href=\"http://m.inmobia.com/mamas/logout?url=maketriviadraw%3Flogout%31\" style=\"color:#FFF\">Logout</a></p>"
			+"<br><p align=\"center\"><a href=\""+reqUrl+"logout?url=maketriviadraw%3Flogout%31\" style=\"color:#FFF\">Logout</a></p>"
			+"</body></html>");
			/*out.println("</div>");
			out.println("</form>");
			out.println("<br><p align=\"center\"><a href=\"logout?url=maketriviadraw%3Flogout%31\">Logout</a></p>");
			out.println("</body>");
			out.println("</html>");*/
			
		}
		catch (Exception e) {
			log.error(e,e);
		}
		finally { try { con.close(); } catch (Exception e) { } }
	}

	private Vector addAWinner(ResultSet rs,int total_points, Vector winners) throws Exception{
		
		//Find a random point that should win
		Random rand = new Random( System.currentTimeMillis() );
		int winner = rand.nextInt(total_points)+1;
		
		rs.beforeFirst();
		int points = 0;
		
		rs.beforeFirst();
		while(rs.next()) {
			//Does this person have the winning point?
			if (winner > points && winner <= (points+rs.getInt("points"))) {
				//If he is already a winner try again.
				for (int i = 0; i < winners.size();i++) 
					if (winners.get(i).equals(rs.getString("msisdn"))) 
						return winners;
				//He is a new winner add him to the pool and stop searching
				winners.add(rs.getString("msisdn"));
				break;
			}
			points += rs.getInt("points");
		}
		return winners;
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		doGet(req,resp);
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
		+"<div width=\"100%\" align=\"center\"><img src=\"images/axiata_logo.png\" alt=\"\" width=\"510\" height=\"353\">"
		+"<div class=\"content\">"
		+"<form action=\"maketriviadraw\" method=\"post\">"
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
	private int getUserID (HttpServletRequest req, Connection con) throws Exception{
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
		
		int id = 0;
		PreparedStatement pstmt = con.prepareStatement("SELECT id FROM "+DB+".user WHERE username = ? AND password = ?");
		pstmt.setString(1,username);
		pstmt.setString(2,password);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			id = rs.getInt("id");
		}
		rs.close();
		pstmt.close();
		return id;
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
	
	public static void main(String[] ar){
		String DB = "axiata_trivia";
		
		String s = "" +
		"SELECT msisdn, SUM(points) points " +
		"FROM "+DB+".trivia_master_log " +
		"WHERE DATE(timestamp) <= '2012-02-20' " +
		"AND timestamp >= '2012-03-20' " +
		"AND country_id = 1 " +
		"AND points > 0 " + 
		"AND msisdn NOT IN (" +
		"   SELECT msisdn " +
		"   FROM "+DB+".blacklist_winners " +
		") " +
		"GROUP BY msisdn " +
		"ORDER BY points DESC";
		
		System.out.println(s);
	}
	

}
