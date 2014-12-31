package com.pixelandtag.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.reports.DAO;
import com.pixelandtag.reports.ReportDTO;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.MessageType;

/**
 * Servlet implementation class Giant
 */
public class Giant extends HttpServlet {

	private Logger logger = Logger.getLogger(Giant.class);

	private String DB = "pixeland_content360";
	private int LOG_IN = 0;
	private int LOG_OUT = 1;
	private int SEARCH = 2;
	private int REDEEM_VOUCHER = 3;
	private int VIEW_STATS = 4;

	private DataSource ds;
	private Context initContext;
	private JSONObject requestJSON = null;
	private JSONObject responseJSON = null;

	

	private static final long serialVersionUID = 1L;

	private void log(Exception e) {

		logger.error(e.getMessage(), e);

	}

	public void destroy() {

		logger.info("RESPONSE_TEXT_EDITOR: in Destroy");
		try {

			if (initContext != null)
				initContext.close();

		} catch (NamingException e) {

			logger.error(e.getMessage(), e);

		}

	}

	@Override
	public void init() throws ServletException {

		try {

			initContext = new InitialContext();

			ds = (DataSource) initContext.lookup("java:/cmpDS");

		} catch (NamingException e) {

			log(e);

		}

	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Giant() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private Connection getConnection() {

		Connection conn = null;

		try {

			try {
				if (initContext != null)
					initContext.close();
			} catch (Exception e) {
			}

			initContext = new InitialContext();

			ds = (DataSource) initContext.lookup("java:/GIANT");

			conn = ds.getConnection();

		} catch (Exception e) {

			log(e);

		} finally {

		}

		return conn;

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		StringBuffer jb = new StringBuffer();
		Connection conn = null;
		PrintWriter out = null;
		String line = null;

		try {
			out = response.getWriter();
			
			
			
			
			String command = "";
			
			BufferedReader reader = request.getReader();
			   
			  while ((line = reader.readLine()) != null)
		    	  jb.append(line);
			
			logger.info("Incomming json >> " + jb.toString());
			logger.info("Incomming json length >> " + jb.length());
			
			if(jb.length()>0){
				
				requestJSON = new JSONObject(jb.toString());
				command = requestJSON.getString("command");
				logger.info("command>> "+command);
				
			}
			
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
			
			HttpSession sess = request.getSession();
			
			Object ob = sess.getAttribute("userObject");
			
			UserSessionObject user = null;
			
			if(ob!=null)
				user = (UserSessionObject) ob;
			
			logger.info("SESSION: "+user);

			if(command.equals("login")){
				
				String username = requestJSON.getString("username");
				
				String password = requestJSON.getString("password");
				
				if (username != null && password != null) {
					Cookie myCookie = new Cookie("giant_username",username);
					myCookie.setMaxAge(-1);
					myCookie.setPath("/");
					response.addCookie(myCookie);
	
					myCookie = new Cookie("giant_password",password);
					myCookie.setMaxAge(-1);
					myCookie.setPath("/");
					response.addCookie(myCookie);
				}
			
				request.setAttribute("username", username);
				request.setAttribute("password", password);
				
				user = isUserLoggedIn(request, conn);
				
				logger.info("UserObj in Session: "+user);
				
				String message = (user==null) ? "Sorry, problem logging in. Please confirm your login credentials." : "Logging in.";
				
				JSONObject jsonobject = null;
				
				if(user!=null){
					jsonobject  =  new JSONObject("{\"role_id\" : \""+user.getRole()+"\"}");
					//jsonobject.append("role", user.getRole());
				}
				
				ajaxResponse(out, LOG_IN,message,(user!=null),jsonobject,conn);
				
				
			}else if(command.equals("logout")){
				
				sess.setAttribute("userObject",null);
				
				ajaxResponse(out,LOG_OUT ,"You have been logged out",true,null,conn);
			
			}else if(command.equals("search")){
				
				JSONObject data = search(requestJSON,conn);
				
				String message = "Search results: ";
				try{
					JSONArray array = data.getJSONArray("prize_id");
					int lengt = array.length();
					message = "<span class=\"redeemable\"> Your search returned "+lengt+" voucher"+(lengt>1 ? "s" : "")+"</span>";
				}catch(JSONException jsone){
					message= "<span class=\"non_redeemable\"> Invalid voucher or phone number. Please check and enter again</span>";
				}
				
				ajaxResponse(out,SEARCH,message,true,data,conn);
				
			}else if (command.equals("redeem_voucher")){
				
				try{
					
					if(user!=null)
						requestJSON.put("store_id_fk", user.getStore_id_fk());
					else
						logger.warn("\n\nWE TRIED TO GET THE USER OBJECT, but the session seems not to have it! Perhaps session expired\n\n");
				
				}catch(Exception e){
					
					logger.error(e.getMessage(),e);
				
				}
				
				JSONObject data = redeemVoucher(requestJSON,conn);
					
				ajaxResponse(out,REDEEM_VOUCHER,"Voucher redeemed ",true,data,conn);
				
			}else if(command.equals("viewStats")){
				
				JSONObject data = getStats(requestJSON,conn);
				
				String message = "Search results: ";
				try{
					JSONArray array = data.getJSONArray("claimed_count");
					int lengt = array.length();
					message = "<span class=\"redeemable\"> Your search returned "+lengt+" records"+(lengt>1 ? "s" : "")+"</span>";
				}catch(JSONException jsone){
					message= "<span class=\"non_redeemable\"> No records retrieved for the dates you selected</span>";
				}
				
				
				
				logger.debug(message);
				
				ajaxResponse(out,VIEW_STATS,message,true,data,conn);
				
			}else if(user!=null){
				
				ServletContext contx = getServletContext();
				
				RequestDispatcher disp =  contx.getRequestDispatcher("/home.jsp");
				
				disp.forward(request, response);
				
			}else{
				
				printLoggin(out);
			
			}

			
			
			

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}

	private JSONObject getStats(JSONObject requestJSON2, Connection conn) {
		String dateSelection;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		
		JSONObject data = new JSONObject();
		
		try {
			
			dateSelection = requestJSON.getString("date_range");
			
			DAO dao = new DAO(conn);
			List<ReportDTO> reportRecs = dao.getReport(dateSelection);
			Iterator<ReportDTO> report = reportRecs.iterator();
			
			ReportDTO record = null;
			while(report.hasNext()){
				record = report.next();
				data.append("claimed_count", record.getClaimed());
				data.append("count", record.getCount());
				data.append("prize_value_claimed", record.getPrize_value_claimed());
				data.append("prize_value_unclaimed", record.getPrize_value_unclaimed());
				data.append("store_id_fk", record.getStore_id_fk());
				data.append("time_Awarded", record.getTimeAwarded());
				data.append("store_name", record.getStore_name());
				
				
			}
		
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			try {
				rs.close();
			} catch (Exception e) {
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		return data;
	}

	private JSONObject redeemVoucher(JSONObject requestJSON2, Connection conn) {
		PreparedStatement pstmt = null;
		
		
		
		if(requestJSON2!=null)
			System.out.println(requestJSON2.toString());
		
		
		
		JSONObject data = new JSONObject();
		
		try {
			
			String voucher_id = requestJSON.getString("voucher_id");
			String voucher_num = requestJSON.getString("voucher_num");
			String msisdn = requestJSON.getString("msisdn");
			String store_id_fk = "-1";
			try{
				store_id_fk = requestJSON.getString("store_id_fk");
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
			String sql = "UPDATE `"+DB+"`.`voucher` SET USED = 1, store_id_fk = ?, timeStamp_used = now() "
				+" WHERE id=? AND voucherNumber = ? AND msisdn = ? ";
			
			
			pstmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
			
			
			
			
			
			pstmt.setString(1, store_id_fk);
			pstmt.setString(2, voucher_id);
			pstmt.setString(3, voucher_num);
			pstmt.setString(4, msisdn);
			
			pstmt.executeUpdate();
			
			
		
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		return data;
	}

	private JSONObject search(JSONObject requestJSON, Connection conn) {
		
		String dateSelection;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		
		JSONObject data = new JSONObject();
		
		try {
			
			dateSelection = requestJSON.getString("date_range");
		
		
			String phone_number = requestJSON.getString("phone_number");
			String voucher_number = requestJSON.getString("voucher_number");
			
			String dateFrom = "";
			String dateTo = "";
			
			String sql = "SELECT v.id as 'voucher_id', pz.name as 'prize_name',pz.description as 'prize_description',"
				+" pz.id as 'prize_id', p.name as 'promo_name', p.description as 'promo_description',"
				+" p.id as 'promo_id', v.voucherNumber, v.msisdn, v.winning, v.used, v.timeStamp_awarded, v.timeStamp_used"
				+" FROM `"+DB+"`.`voucher` v"
				+" LEFT JOIN `"+DB+"`.`promotion` p ON p.id = v.promotion_id_fk"
				+" LEFT JOIN `"+DB+"`.`prize` pz ON pz.promotion_id_fk = v.promotion_id_fk"
				+" WHERE v.winning=1 AND (v.voucherNumber = ?) OR (v.msisdn = ?)";
			
			boolean dateRange = false;
			
			if(!dateSelection.equalsIgnoreCase(""))
			if(!dateSelection.equals("Choose a Date")){
				
				dateRange = true;
				
				int length = dateSelection.length();
				logger.info("LENGTH OF DATE: "+length);
				
				if(length>10){
					dateFrom = dateSelection.split("[\\s(-)\\s]")[0] + " 00:00:00";
					dateTo = dateSelection.split("[\\s(-)\\s]")[2] +" 23:59:59";
				}else{
					dateFrom = dateSelection + " 00:00:00";
					dateTo = dateSelection + " 23:59:59";
				}
				
				sql += " AND v.timeStamp_awarded between ? and ?";
				
				
			}
			
			logger.info("dateFrom::::::: "+dateFrom);
			logger.info("dateTo::::::: "+dateTo);
			logger.info("sql::::::: "+sql);
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, voucher_number);
			pstmt.setString(2, phone_number);
			if(dateRange){
				pstmt.setString(3, dateFrom);
				pstmt.setString(4, dateTo);
			}
			
			rs = pstmt.executeQuery();
			
			
			while(rs.next()){
				
				String msisdn = rs.getString("msisdn");
				data.append("prize_name", rs.getString("prize_name"));
				data.append("prize_description", rs.getString("prize_description"));
				data.append("prize_id", rs.getString("prize_id"));
				data.append("promo_name", rs.getString("promo_name"));
				data.append("promo_description", rs.getString("promo_description"));
				data.append("promo_id", rs.getString("promo_id"));
				data.append("voucherNumber", rs.getString("voucherNumber"));
				data.append("msisdn", msisdn);
				data.append("winning", rs.getBoolean("winning"));
				data.append("used", rs.getString("used"));
				data.append("timeStamp_awarded", rs.getString("timeStamp_awarded"));
				data.append("voucher_id", rs.getString("voucher_id"));
				
				
				int language_id = UtilCelcom.getSubscriberLanguage(msisdn, conn);
				
				UtilCelcom.insertIntoHTTPToSend(conn, "22222", "IOD", "IOD0000", msisdn, UtilCelcom.getMessage(MessageType.MSISDN_CHECK_NOTIFICATION, conn, language_id));
				
				
				String used = "-";
				
					try{
						used = rs.getString("timeStamp_used");
					}catch(Exception e){}
				
				data.append("timeStamp_used", used);
				
			}
		
		} catch (Exception e) {
			
			log(e);
			
		}finally{
			try {
				rs.close();
			} catch (Exception e) {
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		return data;
	}

	/**
	 * Ajax Response
	 * @param out
	 * @param success
	 * @param conn
	 */
	private void ajaxResponse(PrintWriter out, int action, String message, boolean success, JSONObject data, Connection conn) {
		
		responseJSON  = new JSONObject();
		
		
		try {
			 
			responseJSON.append("success", success);
			responseJSON.append("message", message);
			responseJSON.append("action", action);
			
			if(data!=null){
				responseJSON.append("data", data);
				logger.info(action+"result :::::::"+data.toString());
			}
			
		 } catch (Exception e) {
			
			 log(e);
			
		 }finally{
			 logger.info(action+"result :::::::"+responseJSON.toString());
			 out.write(responseJSON.toString());
		 
		 }
		
	}

	private void printHomePage(PrintWriter out) {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+" <html xmlns=\"http://www.w3.org/1999/xhtml\">"
				+" <head>"
				+" <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
				+" <title>Home</title>"
				+" <link href=\"css/styles.css\" rel=\"stylesheet\" type=\"text/css\" />"
				+" <script type=\"text/javascript\" src=\"js/jquery-1.7.2.min.js\"></script> " 
				+" <script type=\"text/javascript\" src=\"js/jquery.cookie.js\"></script>"
				+" <script type=\"text/javascript\" src=\"js/giant.js\"></script> </head>"
				+" </head>"

				+" <body>"
				+" <div id=\"home_container\">"
				+" <div id=\"header\">"
				+" <div class=\"home_text\">Welcome ${USERNAME}&nbsp;&nbsp;|&nbsp;&nbsp;<a href=\"#\">Logout</a></div>"
				+" </div>"
				+" <div id=\"menu\">"
				+" <a href=\"#\">Home</a>"
				+" <a href=\"#\">Vouchers</a>"
				+" <a href=\"#\">Users</a>"
				+" </div>"
				+" <div id=\"search\">"
				+" <form>"
				+" <table width=\"100%\" border=\"0\">"
				+" <tr>"
				+" <th width=\"7%\" align=\"left\" scope=\"col\">From</th>"
				+" <th width=\"23%\" align=\"left\" scope=\"col\"><input type=\"text\" class=\"dates\" name=\"from\"/></th>"
				+" <th width=\"4%\" align=\"left\" scope=\"col\">To : </th>"
				+" <th width=\"26%\" align=\"left\" scope=\"col\"><input type=\"text\" class=\"dates\" name=\"to\"/></th>"
				+" <th width=\"40%\" align=\"left\" scope=\"col\"><input type=\"submit\" value=\"SEARCH\" class=\"search_btn\" /></th>"
				+" </tr>"
				+" </table>"

				+" </form>"
				+" </div>"
				+" <div id=\"content\">"
				+" <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam felis tortor, blandit vel feugiat nec, porta a eros. Sed lorem diam, volutpat sed elementum vel, venenatis at quam. Duis metus arcu, convallis in mollis at, elementum non tortor. Morbi at dui lorem. Sed porta urna laoreet massa semper non accumsan orci cursus. Morbi at augue purus, luctus elementum dolor. Suspendisse potenti. Aenean sem arcu, convallis in facilisis sed, faucibus non sapien. Integer at tortor tempor quam luctus sodales a et ipsum. Fusce lobortis interdum diam vel dignissim. Morbi at semper velit. Ut congue hendrerit risus, sed congue ipsum euismod mollis. Nulla interdum fermentum nisl, id gravida metus dictum vitae. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nunc lobortis erat ac est ornare eu volutpat eros congue. Aenean ultrices tristique lacus, vel laoreet purus vulputate at. Sed eget nisl eget sapien suscipit congue sit amet in urna. Donec feugiat mattis nisi egestas congue. Phasellus molestie vulputate ligula quis auctor. Pellentesque ultrices, nisi eu gravida pharetra, lacus velit vehicula dolor, vel condimentum elit orci eu enim.</p>"
				+" </div>"
				+" <div id=\"footer\">"
				+" <div class=\"footer_content\">"
				+" <a href=\"#\">"
				+" <div class=\"inmobia_footer_logo\"></div>"
				+" </a>"
				+" </div>"
				+" </div>"
				+" </div>"
				+" </body>"
				+" </html>");
		
	}

	private void printLoggin(PrintWriter out) throws Exception {
		
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ " <html xmlns=\"http://www.w3.org/1999/xhtml\">"
				+ " <head>"
				+ " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
				+ " <title>Login</title>"
				+ " <link href=\"css/styles.css\" rel=\"stylesheet\" type=\"text/css\" />"
				+ "	<script type=\"text/javascript\" src=\"js/jquery-1.7.2.min.js\"></script> "
				+"  <script type=\"text/javascript\" src=\"js/jquery.cookie.js\"></script> "
				+ "	<script type=\"text/javascript\" src=\"js/giant.js\"></script> </head> "
				+ " <body><div id=\"main_div\">"
				+ " <div id=\"login_container\">"
				+ " <div class=\"login_logo\"></div>"
				+ " <div class=\"login_form\">"
				+ " <table width=\"100%\" border=\"0\">"
				+ " <tr>"
				+ " <th width=\"46%\" align=\"left\" valign=\"top\" scope=\"row\">Username : </th>"
				+ " <td width=\"54%\" valign=\"top\">"
				+ " <input type=\"text\" name=\"uname\" id=\"login1\" class=\"login_input\" />"
				+ " </td>"
				+ " </tr>"
				+ " <tr>"
				+ " <th align=\"left\" valign=\"top\" scope=\"row\">Password : </th>"
				+ " <td valign=\"top\">"
				+ " <input type=\"password\" name=\"pwd\" id=\"login2\" class=\"login_input\" />"
				+ " </td>"
				+ " </tr>"
				+ " <tr>"
				+ " <td colspan=\"2\" valign=\"top\" align=\"center\"> <span id=\"status_span\"></span>"
				+ " </td>"
				+ " </tr>"
				
				+ " <tr>"
				+ " <th align=\"left\" valign=\"top\" scope=\"row\">&nbsp;</th>"
				+ " <td valign=\"top\">"
				+ " <input type=\"submit\" onclick=\"GIANT.login()\"name=\"test\" value=\"Login\" class=\"login_btn\"/>"
				+ " </td>"
				+ " </tr>"
				
				+ " </table>"
				+ " </div>"
				+ " </div>" + "</div> </body>" + " </html>");

	}

	private UserSessionObject isUserLoggedIn(HttpServletRequest request, Connection con)
			throws Exception {
		
		UserSessionObject userObject = null;
		
		String username = (String) request.getAttribute("username");
		String password = (String) request.getAttribute("password");
		
		HttpSession session = request.getSession();
		
		userObject = (UserSessionObject) session.getAttribute("userObject");
		
		if(userObject!=null && username.equals(userObject.getUsername()) && password.equals(userObject.getPassword())){
			
			return userObject;
		
		}else{
			
			userObject = null;
			session.setAttribute("userObject",null);//
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			
			try{
				
				pstmt = con.prepareStatement("SELECT * FROM " + DB + ".users WHERE username = ? AND password = ?");
				
				pstmt.setString(1, username);
				pstmt.setString(2, password);
				
				rs = pstmt.executeQuery();
			
				if (rs.next()) {
					
					userObject = new UserSessionObject();
					userObject.setId(rs.getInt("id"));
					userObject.setRole(rs.getInt("role"));
					userObject.setUsername(rs.getString("username"));
					userObject.setPassword(rs.getString("password"));
					userObject.setRegistered(rs.getString("registrationDate"));
					userObject.setActive(rs.getBoolean("active"));
					userObject.setFirst_name(rs.getString("fName"));
					userObject.setLast_name(rs.getString("lName"));
					userObject.setStore_id_fk(rs.getInt("store_id_fk"));
					
					session.setAttribute("userObject", userObject);
				}
				
			}catch(Exception e){
				
				log(e);
			
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){}
				
				try{
					pstmt.close();
				}catch(Exception e){}
			}
		
		
		}
		
		return userObject;
	}

}
