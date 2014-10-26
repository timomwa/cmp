package com.pixelandtag.web.draw;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.cookie.SM;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * Servlet Class
 * 
 * @web.servlet name="TriviaStats" display-name="Name for TriviaStats"
 *              description="Description for TriviaStats"
 * @web.servlet-mapping url-pattern="/triviastats"
 * @web.servlet-init-param name="A parameter" value="A value"
 */
public class TriviaStats extends HttpServlet {
	final String DB = "axiata_trivia";
	com.pixelandtag.util.POI poi = new com.pixelandtag.util.POI();
	int message_input_size = 300;
	static final long serialVersionUID = 0;
	private static final String SMS_TRAFFIC = "1";
	private static final String REVENUE = "2";
	Logger log = Logger.getLogger(TriviaStats.class);

	public TriviaStats() {
		super();
	}

	private HSSFSheet setupCountrySheet(HSSFSheet s, String type) {
		s.createFreezePane(1, 1);
		int c = 0;
		if(type.equals(REVENUE)){
			poi.writeCell(s, null, 0, ++c, "New Sub");
			poi.writeCell(s, null, 0, ++c, "Unsubscribed");
			poi.writeCell(s, null, 0, ++c, "Revenue per day (RM)");
		}
		poi.writeCell(s, null, 0, ++c, "Number of days");
		
		if(type.equals(REVENUE)){
			poi.writeCell(s, null, 0, ++c, "Average revenue per day");
			poi.writeCell(s, null, 0, ++c, "Trend %");
			poi.writeCell(s, null, 0, ++c, "Accumulated revenue Trivia");
		}
		
		if(type.equals(REVENUE)){
			poi.writeCell(s, null, 0, ++c, "Successfully billed Transactions");
		}else{
			poi.writeCell(s, null, 0, ++c, "Number of SMS sent per day");
		}
		
		//poi.writeCell(s, null, 0, ++c, "Price per SMS RM");

		if(type.equals(SMS_TRAFFIC)){
		c++;
		}
		
		if(type.equals(SMS_TRAFFIC)){
			poi.writeCell(s, null, 0, ++c, "Active players");
			poi.writeCell(s, null, 0, ++c, "New players");
			poi.writeCell(s, null, 0, ++c, "Stopped players");
		}
		if(type.equals(SMS_TRAFFIC)){
			poi.writeCell(s, null, 0, ++c, "Largest number of answers");
		}else{
			poi.writeCell(s, null, 0, ++c, "Active Subscribers");
		}
		c = 0;
		s.setColumnWidth(c, (short) 7000);
		s.setColumnWidth(c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 12000);
		//s.setColumnWidth(++c, (short) 12000);
		if(type.equals(SMS_TRAFFIC)){
			s.setColumnWidth(++c, (short) 7000);
			s.setColumnWidth(++c, (short) 7000);
			s.setColumnWidth(++c, (short) 7000);
		}
		//s.setColumnWidth(++c, (short) 7000);
		return s;
	}

	public HSSFSheet writeCountryBottom(HSSFSheet s, HSSFWorkbook wb, int r, String type) {

		int c = 0;
		short bgcolor = HSSFColor.WHITE.index;
		poi.writeCell(s, null, ++r, c++, "Total");
		c++;
		c++;
		
		//Revenue per day
		poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(D2:D" + r + ")");
		
		
		//skip number of days
		c++;
		
		//Average revenue per day
		if(type.equals(REVENUE)){
			//poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				//"SUM(E2:E" + r + ")");
		}else{
			poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
					"SUM(C2:C" + r + ")");
		}
		
		if(type.equals(REVENUE)){
		//skip trend
		c++;
		// skip accumulated revenue trivia
		c++;
		
		/*poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"ROUND(B" + (r + 1) + "/D" + (r + 1) + "*100,0)-100");*/
		//c++;
		//Number of content pushed per day
		//poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				//"SUM(I2:I" + r + ")");
		
		}
		
		if(type.equals(SMS_TRAFFIC)){
		c++;
		c++;
		c++;
		poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(K2:K" + r + ")");
		poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(L2:L" + r + ")");
		poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(M2:M" + r + ")");

		}
		r += 5;

		poi.writeCell(s, null, ++r, 1,
				"New Sub - This is the number of new subscribers on the given day.");
		poi.writeCell(s, null, ++r, 1,
				"Unsubscribed - The number of subscribers who sent STOP or STOP ALL");
		poi.writeCell(s, null, ++r, 1,
			"Revenue per day (RM) - Revenue generated by trivia in Malaysian Ringgit");
		poi.writeCell(s, null, ++r, 1,
				"Number of Days - Number of days the trivia has been running - the nth Day for each column");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Average revenue per day - The weighted average revenue per day.");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Trend - is the percentage of Revenue per day(RM) compared to Average revenue per day");
		poi.writeCell(s, null, ++r, 1,
				"Accumulated revenue Trivia - This is the accumulated revenue on that day since launch");
		
		poi.writeCell(s, null, ++r, 1,
				"Successfully billed Transactions - The transactions that were successfully billed");
		
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Active Subscribers - Number of subscribers who've been playing and have been billed at least once");

		
		

		return s;

	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Connection con = null;
		try {
			Context initContext = new InitialContext();
			// DataSource ds = (DataSource)initContext.lookup("java:/VasDS");
			DataSource ds = (DataSource) initContext.lookup("java:/RESP_EDITOR");
			con = ds.getConnection();

			if (req.getParameter("username") != null
					&& req.getParameter("password") != null) {
				Cookie myCookie = new Cookie("mamas_username",
						req.getParameter("username"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);

				myCookie = new Cookie("mamas_password",
						req.getParameter("password"));
				myCookie.setMaxAge(-1);
				myCookie.setPath("/");
				resp.addCookie(myCookie);
			}

			if (!isUserLoggedIn(req, con)) {
				printLoggin(resp);
				return;
			}
			int country_id = getCountryID(req, con);
			log.debug("Pre sql statement");
			
			
			String sql = "";
			
			final String TYPE = (req.getParameter("report_type") == null )? REVENUE : req.getParameter("report_type").trim();
			if(TYPE.equals(SMS_TRAFFIC)){
				
					sql = "SELECT SUM(active_players) AS active_players,c.price price,country_id, day, c.name AS name, CURDATE() AS current_day, SUM(total) total, SUM(price_sum) price_sum, SUM(total_players) total_players, SUM(signin) signin, SUM(signout) signout, SUM(top_answers) top_players "
							+ "FROM ("
							+ "   ( "
							+ "      SELECT 0 AS active_players,country_id, DATE(timestamp) day, COUNT(*) total, SUM(price) price_sum, COUNT(DISTINCT MSISDN) AS total_players, SUM(IF(answer='START',1,0)) AS signin, SUM(IF(answer='STOP',1,0)) AS signout, 0 AS top_answers "
							+ "      FROM "+DB+".trivia_master_log l  "
							+ "      LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
							+ "      WHERE country_id = "
							+ country_id
							+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND answer IN ('USSD INPUT','a','b','','START','STOP','BILLED_TEASER','SUBSCRIPTION START','SUBSCRIPTION STOP') "
							+ "      GROUP BY country_id,DATE(timestamp)) "
							+ "   UNION ALL "
							+ "   ( "
							+ "      SELECT 0 AS active_players, country_id, day, 0 AS total, 0 AS price_sum, 0 AS total_players, 0 AS signin, 0 AS signout, MAX(c) AS top_answers "
							+ "      FROM ( "
							+ "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c "
							+ "         FROM "+DB+".trivia_master_log l "
							+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
							+ "         WHERE country_id = "
							+ country_id
							+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND answer IN ('a','b') "
							+ "         GROUP BY country_id,DATE(timestamp),msisdn"
							+ "      ) AS t2 "
							+ "      GROUP BY country_id,day "
							+ "   ) "
							+ "   UNION ALL "
							+ "   ("
							+ "      SELECT COUNT(*) AS active_players , country_id, day, 0 AS total, 0 AS price_sum, 0 AS total_players, 0 AS signin, 0 AS signout, 0 AS top_answers "
							+ "      FROM (  "
							+ "         SELECT country_id,DATE(timestamp) AS day,answer "
							+ "         FROM "+DB+".trivia_master_log l  "
							+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
							+ "         WHERE country_id = "
							+ country_id
							+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ?  "
							+ "         GROUP BY country_id,DATE(timestamp), msisdn "
							+ "	      ORDER BY country_id,timestamp DESC "
							+ "      ) AS t1 "
							+ "      WHERE answer != 'STOP' "
							+ "      GROUP BY country_id,day "
							+ "   ) "
							+ ") "
							+ "AS q "
							+ "LEFT JOIN "+DB+".country c ON (c.id = q.country_id) "
							+ "GROUP BY country_id, day "
							+ "ORDER BY name ASC, day DESC";
			
			}else if(TYPE.equals(REVENUE)){
				
				sql = "SELECT SUM(active_players) AS 'active_players', SUM(stopped) AS 'stopped', SUM(new_subs) AS 'new_subs', amount as 'price', country_id, day, c.name AS name, CURDATE() AS current_day, SUM(total_content) total_content_sent, SUM(revenue) revenue, SUM(total_players) total_players, SUM(highest_content_count_recorded) highest_content_count_for_single_sub "
					+ "FROM ("
					+ "   ( "
					+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, amount, country_id, DATE(timestamp) day, 0 AS total_content, 0 AS revenue, COUNT(DISTINCT MSISDN) AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM "+DB+".trivia_master_log l  "
					+ "      LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
					+ "      WHERE country_id = "
					+ country_id
					+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND answer IN ('SMS', 'MMS') "
					+ "      GROUP BY country_id,DATE(timestamp)) "
					+ "   UNION ALL "
					+ "   ( "
					+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs,0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, MAX(c) AS highest_content_count_recorded "
					+ "      FROM ( "
					+ "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c "
					+ "         FROM "+DB+".trivia_master_log l "
					+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
					+ "         WHERE country_id = "
					+ country_id
					+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ? AND answer IN ('SMS', 'MMS') "
					+ "         GROUP BY country_id,DATE(timestamp),msisdn"
					+ "      ) AS t2 "
					+ "      GROUP BY country_id,day "
					+ "   ) "
					+ "   UNION ALL "
					+ "   ("
					+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, 0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM (  "
					+ "         SELECT country_id,DATE(timestamp) AS day,answer "
					+ "         FROM "+DB+".trivia_master_log l  "
					+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
					+ "         WHERE country_id = "
					+ country_id
					+ " AND DATE(timestamp) >= ? AND DATE(timestamp) <= ?  "
					+ "         GROUP BY country_id,DATE(timestamp), msisdn "
					+ "	      ORDER BY country_id,timestamp DESC "
					+ "      ) AS t1 "
					+ "      WHERE answer != 'STOP' "
					+ "      GROUP BY country_id,day "
					+ "   ) "
					+ "   UNION ALL "
					+ "   ("
					+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, 0 as amount, 1, day, successfulbilled AS total_content, true_revenue AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM (  "
					+ "         SELECT sum(price) as 'true_revenue', date(timeStamp) as 'day', count(*) as 'successfulbilled' "
					+ "         FROM celcom.SMSStatLog l  "
					+ "         WHERE DATE(timestamp) >= ? AND DATE(timestamp) <= ?  AND price>=1 and SMSServiceID=2 AND charged=1 AND CMP_SKeyword='IOD0100' AND statusCode='Success' AND CMP_SKeyword='IOD0100'"
					+ "         GROUP BY date(timeStamp)"
					+ "	      ORDER BY day DESC "
					+ "      ) AS t3 "
					+ "      GROUP BY day"
					+ "   ) "
					
					
					+ "   UNION ALL "
					+ "   ("
					+ "      SELECT  0 as 'active_players', 0 AS stopped, new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM (  "
					+ "         SELECT count(*) as 'new_subs', date(timeStamp) as 'day'"
					+ "         FROM "+DB+".trivia_log tl  "
					+ "         WHERE DATE(timeStamp) >= ? AND DATE(timeStamp) <= ?  AND answer='START'"
					+ "         GROUP BY date(timeStamp)"
					+ "	      ORDER BY day DESC "
					+ "      ) AS t4 "
					+ "      GROUP BY day"
					+ "   ) "
					
					
					+ "   UNION ALL "
					+ "   ("
					+ "      SELECT 0 as 'active_players', stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM (  "
					+ "         SELECT count(*) as 'stopped', date(timeStamp) as 'day'"
					+ "         FROM "+DB+".trivia_log tl  "
					+ "         WHERE DATE(timeStamp) >= ? AND DATE(timeStamp) <= ?  AND answer='STOP'"
					+ "         GROUP BY date(timeStamp)"
					+ "	      ORDER BY day DESC "
					+ "      ) AS t5 "
					+ "      GROUP BY day"
					+ "   ) "
					
					
					
					+ "   UNION ALL "
					+ "   ("
					+ "      SELECT active_players as 'active_players', 0 AS stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
					+ "      FROM (  "
					+ "         SELECT count(distinct msisdn) as 'active_players',date(timeStamp) as 'day'"
					+ "         FROM "+DB+".trivia_log tl  "
					+ "         WHERE answer not in ('STOP') AND price>0 AND DATE(timeStamp) >= ? AND DATE(timeStamp) <= ?  "
					+ "         GROUP BY date(timeStamp)"
					+ "	      ORDER BY day DESC "
					+ "      ) AS t5 "
					+ "      GROUP BY day"
					+ "   ) "
					
					
					
					+ ") "
					+ "AS q "
					+ "LEFT JOIN "+DB+".country c ON (c.id = q.country_id) "
					+ "GROUP BY country_id, day "
					+ "ORDER BY name ASC, day DESC";
				
				
				//log.info(sql);
				
			}

			if (req.getParameter("download_session") != null) {

				HSSFWorkbook wb = new HSSFWorkbook();
				PreparedStatement pstmt = con.prepareStatement(sql);
				int i = 0;
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				
				if(TYPE.equals(REVENUE)){
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
				}
				

				ResultSet rs = pstmt.executeQuery();
				int col = 0;
				int row = 0;
				int days = getDays(rs);

				HSSFSheet s = null;
				while (rs.next()) {
					if (rs.isFirst()) {
						s = wb.createSheet("Revenue & SMS Stats");
						s = setupCountrySheet(s,TYPE);
					}
					short bgcolor = HSSFColor.WHITE.index;

					col = 0;
					poi.writeCell(s, getCellStyle(wb, null, bgcolor), ++row,
							col++, rs.getString("day"));
					
					if(TYPE.equals(REVENUE)){
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, rs.getInt("new_subs"));//New subscribers
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, rs.getInt("stopped"));//Stopped subs
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor),
							row, col++, rs.getInt("revenue"));//Revenue per day
					}else{
						poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, "ROUND(C" + (row + 1) + "*G"
										+ (row + 1) + ",0)");
					}
					//Number of days
					poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, days--);
					
					if(TYPE.equals(REVENUE)){
						//Average revenue per day
						poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, "ROUND(H" + (row + 1) + "/E"
										+ (row + 1) + ",0)");
						//Trend
						poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, "ROUND(D" + (row + 1) + "/F"
										+ (row + 1) + "*100,0)-100");
						//accumulated trivia revenue
						poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor),
								row, col++, "H" + (row + 2) + "+D" + (row + 1));
					}
					
					if(TYPE.equals(SMS_TRAFFIC))//number of sms per day
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, rs.getInt("total"));
					else
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
								col++, rs.getInt("total_content_sent"));
					
					
					//poi.writeCell(s, getCellStyle(wb, null, bgcolor), row,
						//	col++, rs.getDouble("price"));
				//	poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						//	col++, "");
					
					if(TYPE.equals(SMS_TRAFFIC)){
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, rs.getInt("active_players"));
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, rs.getInt("signin"));
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, rs.getInt("signout"));
					}
					
					if(TYPE.equals(SMS_TRAFFIC)){
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
							col++, rs.getInt("top_players"));
					}else{
						poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
								col++, rs.getInt("active_players"));
					}
					

					if (rs.isLast())
						writeCountryBottom(s, wb, row, TYPE);
				}

				resp.setContentType("application/xls");
				resp.setHeader("Content-Disposition",
						"attachment; filename=\"Axiata_Trivia_report.xls\";");
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
					"width:1024px; background-color:#FFFFCC; padding:10px;"+
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
			out.println("<img src=\"images/axiata_logo.png\"><br>");
			out.println(" <div class=\"content\"><h1>Trivia Stats Monitor - Axiata Cup");
					//+ getCountryName(con, getCountryID(req, con)) + "</h1><br>");
			out.println("<form action=\"triviastats\" method=\"post\">");
			PreparedStatement pstmt = null;
			if (req.getParameter("report") != null) {
				out.println("<input type=\"hidden\" name=\"from\" value=\""
						+ req.getParameter("from") + "\">");
				out.println("<input type=\"hidden\" name=\"to\" value=\""
						+ req.getParameter("to") + "\">");
				out.println("<input type=\"hidden\" name=\"report_type\" value=\""
						+ req.getParameter("report_type") + "\">");
				
				
				
				pstmt = con.prepareStatement(sql);
				int i = 0;
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				pstmt.setString(++i, req.getParameter("from"));
				pstmt.setString(++i, req.getParameter("to"));
				
				if(TYPE.equals(REVENUE)){
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
					pstmt.setString(++i, req.getParameter("from"));
					pstmt.setString(++i, req.getParameter("to"));
				}
				ResultSet rs = pstmt.executeQuery();
				int days = getDays(rs);
				int n = 0;
				while (rs.next()) {
					n++;
					if (rs.isFirst()) {
						out.println("<table border=\"1\">");
						out.println("<tr class="
								+ ((n % 2) > 0 ? "'even'" : "'odd'") + ">");
						out.println("<td>Day</td>");
						
						if(TYPE.equals(REVENUE)){
							out.println("<td>New Subs</td>");
							out.println("<td>Unsubscribed</td>");
							out.println("<td>Revenue per day (RM)</td>");
						}
						out.println("<td>Number of days</td>");
						if(TYPE.equals(REVENUE)){
							out.println("<td>Average revenue per day</td>");
							out.println("<td>Trend %</td>");
							out.println("<td>Accumulated revenue Trivia</td>");
						}
						if(TYPE.equals(REVENUE)){
							out.println("<td>Successfully billed Transactions</td>");
							out.println("<td>Active Subs</td>");
						}else{
							out.println("<td>Number of SMS sent per day</td>");
						}
						//if(TYPE.equals(REVENUE))
							//out.println("<td>Price per SMS RM</td>");
						if(TYPE.equals(SMS_TRAFFIC)){
						out.println("<td>Active players</td>");
						out.println("<td>New players</td>");
						out.println("<td>Stopped players</td>");
						}
						if(TYPE.equals(SMS_TRAFFIC))
							out.println("<td>Largest number of answers</td>");
						/*else
							out.println("<td>Highest countent count to 1 subscriber</td>");*/
						out.println("</tr>");
					}
					double accumulatedRevenue = getAccumulatedRevenue(rs,TYPE);
					double averageRevenue = accumulatedRevenue / days;
					double revenue = 0;
					
					if(TYPE.equals(SMS_TRAFFIC)){
						revenue =(rs.getDouble("price") * rs.getDouble("total"));
					}else{
						revenue = rs.getDouble("revenue");
					}
					out.println("<tr class="
							+ ((n % 2) > 0 ? "'even'" : "'odd'") + ">");
					out.println("<td>" + rs.getString("day") + "</td>");
					
					if(TYPE.equals(REVENUE)){

						out.println("<td>" + rs.getString("new_subs") + "</td>");
						out.println("<td>" + rs.getString("stopped") + "</td>");
						out.println("<td>" + Math.round(revenue) + "</td>");
					}
					out.println("<td>" + days-- + "</td>");
					if(TYPE.equals(REVENUE)){
						out.println("<td>" + Math.round(averageRevenue) + "</td>");
						out.println("<td>"
							+ Math.round((revenue / averageRevenue * 100) - 100)
							+ "</td>");
						out.println("<td>" + Math.round(accumulatedRevenue)
							+ "</td>");
					}
						
					if(TYPE.equals(SMS_TRAFFIC))
						out.println("<td>" + rs.getInt("total") + "</td>");
					else
						out.println("<td>" + rs.getInt("total_content_sent") + "</td>");
					//if(TYPE.equals(REVENUE))
						//out.println("<td>" + rs.getDouble("price") + "</td>");
					
					if(TYPE.equals(SMS_TRAFFIC)){
						out.println("<td>" + rs.getInt("active_players") + "</td>");
						
						out.println("<td>" + rs.getInt("signin") + "</td>");
						
						out.println("<td>" + rs.getInt("signout") + "</td>");
					}
					
					if(TYPE.equals(SMS_TRAFFIC)){
						out.println("<td>" + rs.getInt("top_players") + "</td>");
					}else{
						out.println("<td>" + rs.getInt("active_players") + "</td>");
					}
					
					out.println("</tr>");

					if (rs.isLast()) {
						out.println("</table>");
						out.println("<input type=\"submit\" name=\"download_session\" value=\"Download\"><br>");
						out.println("<input type=\"submit\" name=\"back\" value=\"Back\"><br>");

					}
				}

			}else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				out.println("<table>");
				out.println("<tr><td>From</td><td><input type=\"text\" name=\"from\" value=\""
						+ sdf.format(cal.getTime()) + "\"></td></tr>");
				out.println("<tr><td>To</td><td><input type=\"text\" name=\"to\" value=\""
						+ sdf.format(cal.getTime()) + "\"></td></tr>");
				out.println("<tr><td>Report Type</td><td><select id=\"rType\" name=\"report_type\">" +
						"<option value=\"2\">Revenue</option>" +
						//"<option value=\"1\">SMS Traffic</option>" +
						"</select></td></tr>");
				out.println("</table>");
				out.println("<input type=\"submit\" name=\"report\"><br>");
			}

			out.println("<br>");

			out.println("</div></div>");
			out.println("</form>");
			String reqUrl = req.getRequestURL().toString();
			 reqUrl = reqUrl.split("maketriviadraw")[0].split("triviastats")[0];
			out.println("<br><p align=\"center\"><a href=\""+reqUrl+"logout?url=triviastats%3Flogout%31\">Logout</a></p>");
			out.println("</body>");
			out.println("</html>");

		} catch (Exception e) {
			log.error(e, e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {
			}
		}
	}

	public HSSFCellStyle getCellStyle(HSSFWorkbook wb, String formatstr,
			short bgcolor) {
		HSSFCellStyle style = wb.createCellStyle();

		if (formatstr != null) {
			DataFormat format = wb.createDataFormat();
			style.setDataFormat(format.getFormat(formatstr));
		}
		if (bgcolor != HSSFColor.WHITE.index) {
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			style.setFillBackgroundColor(bgcolor);
			style.setFillForegroundColor(bgcolor);
		}
		return style;
	}

	private double getAccumulatedRevenue(ResultSet rs, String type) throws Exception {
		int row = rs.getRow();
		double revenue = 0;
		
		if(type.equals(SMS_TRAFFIC)){
			revenue = (rs.getDouble("price") * rs.getDouble("total"));
		}else{
			revenue = (rs.getDouble("price") * rs.getDouble("total_content_sent"));
		}
		while (rs.next()){
			if(type.equals(SMS_TRAFFIC)){
				revenue += (rs.getDouble("price") * rs.getDouble("total"));
			}else{
				revenue += (rs.getDouble("price") * rs.getDouble("total_content_sent"));
			}
		}
		rs.absolute(row);
		return revenue;

	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	public int getDays(ResultSet rs) throws Exception {
		rs.last();
		int days = rs.getRow();
		rs.beforeFirst();
		return days;
	}

	private void printLoggin(HttpServletResponse resp) throws Exception {
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.print("<head><style>"+
				"body"+
				"{"+
				"font-family:Arial, Helvetica, sans-serif; padding:0; margin:0; background-color:#6689AB;"+
				"}"+
				".content"+
				"{"+
				"width:1024px; background-color:#FFFFCC; padding:10px;"+
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
		out.println("<div align=\"center\" width=\"100%\">");

		out.println("<img src=\"images/axiata_logo.png\"><br>");
		out.println("<div class=\"content\"><h1>Trivia Stats Monitor</h1><br>");
		out.println("<form action=\"triviastats\" method=\"post\">");
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

	private int getCountryID(HttpServletRequest req, Connection con)
			throws Exception {
		String username = "";
		String password = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equalsIgnoreCase("mamas_username")) {
					username = cookies[i].getValue();
				} else if (cookies[i].getName().equalsIgnoreCase(
						"mamas_password")) {
					password = cookies[i].getValue();
				}
			}
		}

		username = req.getParameter("username") != null ? req
				.getParameter("username") : username;
		password = req.getParameter("password") != null ? req
				.getParameter("password") : password;

		int country_id = 0;
		PreparedStatement pstmt = con
				.prepareStatement("SELECT country_id FROM " + DB
						+ ".user WHERE username = ? AND password = ?");
		pstmt.setString(1, username);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			country_id = rs.getInt("country_id");
		}
		rs.close();
		pstmt.close();
		return country_id;
	}

	private int getUserID(HttpServletRequest req, Connection con)
			throws Exception {
		String username = "";
		String password = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equalsIgnoreCase("mamas_username")) {
					username = cookies[i].getValue();
				} else if (cookies[i].getName().equalsIgnoreCase(
						"mamas_password")) {
					password = cookies[i].getValue();
				}
			}
		}

		username = req.getParameter("username") != null ? req
				.getParameter("username") : username;
		password = req.getParameter("password") != null ? req
				.getParameter("password") : password;

		int id = 0;
		PreparedStatement pstmt = con.prepareStatement("SELECT id FROM " + DB
				+ ".user WHERE username = ? AND password = ?");
		pstmt.setString(1, username);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			id = rs.getInt("id");
		}
		rs.close();
		pstmt.close();
		return id;
	}

	private boolean isUserLoggedIn(HttpServletRequest req, Connection con)
			throws Exception {
		String username = "";
		String password = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equalsIgnoreCase("mamas_username")) {
					username = cookies[i].getValue();
				} else if (cookies[i].getName().equalsIgnoreCase(
						"mamas_password")) {
					password = cookies[i].getValue();
				}
			}
		}

		username = req.getParameter("username") != null ? req
				.getParameter("username") : username;
		password = req.getParameter("password") != null ? req
				.getParameter("password") : password;

		boolean isUserLoggedIn = false;
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM " + DB
				+ ".user WHERE username = ? AND password = ?");
		pstmt.setString(1, username);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			isUserLoggedIn = true;
		}
		rs.close();
		pstmt.close();
		return isUserLoggedIn;
	}

	private String getCountryName(Connection con, int id) throws Exception {
		PreparedStatement pstmt = con.prepareStatement("SELECT name FROM " + DB
				+ ".country WHERE id = ?");
		pstmt.setInt(1, id);
		ResultSet rs = pstmt.executeQuery();
		String country = rs.next() ? rs.getString("name") : "";
		rs.close();
		pstmt.close();
		return country;

	}
	
	
	public static void main(String ar[]){
		
		String DB = "axiata_trivia";
		
		int country_id = 1;
		
		String from = "'2012-03-24'";
		String to = "'2012-04-20'";
		String sql = "SELECT SUM(active_players) AS 'active_players', SUM(stopped) AS 'stopped', SUM(new_subs) AS 'new_subs', amount as 'price', country_id, day, c.name AS name, CURDATE() AS current_day, SUM(total_content) total_content_sent, SUM(revenue) revenue, SUM(total_players) total_players, SUM(highest_content_count_recorded) highest_content_count_for_single_sub "
			+ "FROM ("
			+ "   ( "
			+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, amount, country_id, DATE(timestamp) day, 0 AS total_content, 0 AS revenue, COUNT(DISTINCT MSISDN) AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM "+DB+".trivia_master_log l  "
			+ "      LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
			+ "      WHERE country_id = "
			+ country_id
			+ " AND DATE(timestamp) >= "+from+" AND DATE(timestamp) <= "+to+" AND answer IN ('SMS', 'MMS') "
			+ "      GROUP BY country_id,DATE(timestamp)) "
			+ "   UNION ALL "
			+ "   ( "
			+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs,0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, MAX(c) AS highest_content_count_recorded "
			+ "      FROM ( "
			+ "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c "
			+ "         FROM "+DB+".trivia_master_log l "
			+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
			+ "         WHERE country_id = "
			+ country_id
			+ " AND DATE(timestamp) >= "+from+" AND DATE(timestamp) <= "+to+" AND answer IN ('SMS', 'MMS') "
			+ "         GROUP BY country_id,DATE(timestamp),msisdn"
			+ "      ) AS t2 "
			+ "      GROUP BY country_id,day "
			+ "   ) "
			+ "   UNION ALL "
			+ "   ("
			+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, 0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM (  "
			+ "         SELECT country_id,DATE(timestamp) AS day,answer "
			+ "         FROM "+DB+".trivia_master_log l  "
			+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
			+ "         WHERE country_id = "
			+ country_id
			+ " AND DATE(timestamp) >= "+from+" AND DATE(timestamp) <= "+to+"  "
			+ "         GROUP BY country_id,DATE(timestamp), msisdn "
			+ "	      ORDER BY country_id,timestamp DESC "
			+ "      ) AS t1 "
			+ "      WHERE answer != 'STOP' "
			+ "      GROUP BY country_id,day "
			+ "   ) "
			+ "   UNION ALL "
			+ "   ("
			+ "      SELECT  0 as 'active_players', 0 AS stopped, 0 as new_subs, 0 as amount, 1, day, successfulbilled AS total_content, true_revenue AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM (  "
			+ "         SELECT sum(price) as 'true_revenue', date(timeStamp) as 'day', count(*) as 'successfulbilled' "
			+ "         FROM celcom.SMSStatLog l  "
			+ "         WHERE DATE(timestamp) >= "+from+" AND DATE(timestamp) <= "+to+"  AND price>=1 and SMSServiceID=2 AND charged=1 AND CMP_SKeyword='IOD0100' AND statusCode='Success' AND CMP_SKeyword='IOD0100'"
			+ "         GROUP BY date(timeStamp)"
			+ "	      ORDER BY day DESC "
			+ "      ) AS t3 "
			+ "      GROUP BY day"
			+ "   ) "
			
			
			+ "   UNION ALL "
			+ "   ("
			+ "      SELECT  0 as 'active_players', 0 AS stopped, new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM (  "
			+ "         SELECT count(*) as 'new_subs', date(timeStamp) as 'day'"
			+ "         FROM "+DB+".trivia_log tl  "
			+ "         WHERE DATE(timeStamp) >= "+from+" AND DATE(timeStamp) <= "+to+"  AND answer='START'"
			+ "         GROUP BY date(timeStamp)"
			+ "	      ORDER BY day DESC "
			+ "      ) AS t4 "
			+ "      GROUP BY day"
			+ "   ) "
			
			
			+ "   UNION ALL "
			+ "   ("
			+ "      SELECT 0 as 'active_players', stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM (  "
			+ "         SELECT count(*) as 'stopped', date(timeStamp) as 'day'"
			+ "         FROM "+DB+".trivia_log tl  "
			+ "         WHERE DATE(timeStamp) >= "+from+" AND DATE(timeStamp) <= "+to+"  AND answer='STOP'"
			+ "         GROUP BY date(timeStamp)"
			+ "	      ORDER BY day DESC "
			+ "      ) AS t5 "
			+ "      GROUP BY day"
			+ "   ) "
			
			
			
			+ "   UNION ALL "
			+ "   ("
			+ "      SELECT active_players as 'active_players', 0 AS stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
			+ "      FROM (  "
			+ "         SELECT count(distinct msisdn) as 'active_players',date(timeStamp) as 'day'"
			+ "         FROM "+DB+".trivia_log tl  "
			+ "         WHERE answer not in ('STOP') AND price>0 AND DATE(timeStamp) >= "+from+" AND DATE(timeStamp) <= "+to+"  "
			+ "         GROUP BY date(timeStamp)"
			+ "	      ORDER BY day DESC "
			+ "      ) AS t5 "
			+ "      GROUP BY day"
			+ "   ) "
			
			
			
			+ ") "
			+ "AS q "
			+ "LEFT JOIN "+DB+".country c ON (c.id = q.country_id) "
			+ "GROUP BY country_id, day "
			+ "ORDER BY name ASC, day DESC";
		
		
		System.out.println(sql);
		
		
	}

}
