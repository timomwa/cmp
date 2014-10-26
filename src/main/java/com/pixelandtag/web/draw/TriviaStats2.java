package com.pixelandtag.web.draw;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletOutputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormat;

public class TriviaStats2 {
	final static String DB = "axiata_trivia";
	static com.pixelandtag.util.POI poi = new com.pixelandtag.util.POI();
	int staticmessage_input_size = 300;
	static final long serialVersionUID = 0;
	private static final String SMS_TRAFFIC = "1";
	private static final String REVENUE = "2";
	static Logger log = Logger.getLogger(TriviaStats.class);
	private static String country_id = "1";
	private static String YESTERDAY_QUESTIONS_DISTRIBUTION = "select count(*) answered, msisdn from axiata_trivia.trivia_log where timeStamp between TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)) AND DATE_SUB(TIMESTAMP(CURRENT_DATE), INTERVAL 1 SECOND) and answer in ('A','B') group by msisdn order by answered desc";
	
	
	public static void main(String[] args){
		
		//createFile(args);
		
		
	}
	
	
	public static boolean createFile(String[] args, String filename){
		
		try {
			
			HSSFWorkbook book = createWorkbook();
			
			FileOutputStream fileOut = new FileOutputStream(filename);
			book.write(fileOut);
			fileOut.close();
			
			return true;
		
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return false;
		
		}
	}
	
	
	public static HSSFWorkbook createWorkbook() throws Exception{
		
		final String TYPE = REVENUE;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HSSFWorkbook wb = new HSSFWorkbook();
		Connection con  =  getConnection();
		
		
		HSSFSheet distroS = null;
		
		
		int col = 0;
		int row = 0;
		
		
		
		
		
		
		
		String sql = getSql(TYPE);
		
		
		
		
		System.out.println(sql);
		
		
		
		
		
		
		
		pstmt = con.prepareStatement(sql);
		rs = pstmt.executeQuery();
		
		int days = getDays(rs);
		HSSFSheet s = null;
		while (rs.next()) {
			System.out.println("here");
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
				//accumulated active subs
				poi.writeCellFormula(s, getCellStyle(wb, "#,##0", bgcolor),
						row, col++, "K" + (row + 2) + "+J" + (row + 1));
				
				
				//No balance
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("no_balance"));
				
				//psa_barred
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("psa_barred"));
				
			}
			

			if (rs.isLast())
				writeCountryBottom(s, wb, row, TYPE);
		}
		
		
		
		col = 0;
		row = 0;
		pstmt = con.prepareStatement(YESTERDAY_QUESTIONS_DISTRIBUTION);
		rs = pstmt.executeQuery();
		while(rs.next()){
			
			if(rs.isFirst()){
				distroS = wb.createSheet("Questions_Distribution_per_sub");
				distroS = setupDistroSheet(distroS);
			}
			
			String answered = rs.getString("answered");
			String msisdn = rs.getString("msisdn");
			
			
			short bgcolor = HSSFColor.WHITE.index;

			col = 0;
			poi.writeCell(distroS, getCellStyle(wb, null, bgcolor), ++row,
					col++, answered);
			poi.writeCell(distroS, getCellStyle(wb, null, bgcolor), row,
					col++, msisdn);
			
		}
		rs.close();
		pstmt.close();

		con.close();
		
		return wb;
	}
	
	
	
	
	
	
	
	
	private static HSSFSheet setupDistroSheet(HSSFSheet s) {
		s.createFreezePane(0, 1);
		int c = 0;
		
			poi.writeCell(s, null, 0, c, "Questions answered");
			poi.writeCell(s, null, 0, ++c, "Msisdn");
		
		c = 0;
		s.setColumnWidth(c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		//s.setColumnWidth(++c, (short) 7000);
		return s;
	}


	private static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://db/celcom?user=root&password=");
	}



	private static String getSql(String type){
		String sql = "";
		
		if(type.equals(SMS_TRAFFIC)){
			
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
		
		}else if(type.equals(REVENUE)){
			
			String launchDate = "2012-03-24";
			sql ="SELECT SUM(psa_barred) as 'psa_barred', SUM(no_balance) as 'no_balance', SUM(active_players) AS 'active_players', SUM(stopped) AS 'stopped', SUM(new_subs) AS 'new_subs', amount as 'price', country_id, day, c.name AS name, CURDATE() AS current_day, SUM(total_content) total_content_sent, SUM(revenue) revenue, SUM(total_players) total_players, SUM(highest_content_count_recorded) highest_content_count_for_single_sub "
				+ "FROM ("
				+ "   ( "
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players', 0 AS stopped, 0 as new_subs, amount, country_id, DATE(timestamp) day, 0 AS total_content, 0 AS revenue, COUNT(DISTINCT MSISDN) AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM "+DB+".trivia_master_log l  "
				+ "      LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "      WHERE country_id = "
				+ country_id
				+ " AND answer IN ('SMS', 'MMS') AND DATE(timestamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "      GROUP BY country_id,DATE(timestamp)) "
				+ "   UNION ALL "
				+ "   ( "
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players', 0 AS stopped, 0 as new_subs,0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, MAX(c) AS highest_content_count_recorded "
				+ "      FROM ( "
				+ "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c "
				+ "         FROM "+DB+".trivia_master_log l "
				+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "         WHERE country_id = "
				+ country_id
				+ " AND  answer in ('A','B') AND DATE(timestamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY country_id,DATE(timestamp),msisdn"
				+ "      ) AS t2 "
				+ "      GROUP BY country_id,day "
				+ "   ) "
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players',0 AS stopped, 0 as new_subs, 0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT country_id,DATE(timestamp) AS day,answer "
				+ "         FROM "+DB+".trivia_master_log l  "
				+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "         WHERE country_id = "
				+ country_id
				+ "         AND DATE(timestamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE GROUP BY country_id,DATE(timestamp), msisdn "
				+ "	      ORDER BY country_id,timestamp DESC "
				+ "      ) AS t1 "
				+ "      WHERE answer != 'STOP' "
				+ "      GROUP BY country_id,day "
				+ "   ) "
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players',0 AS stopped, 0 as new_subs, 0 as amount, 1, day, successfulbilled AS total_content, true_revenue AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT sum(price) as 'true_revenue', date(timeStamp) as 'day', count(*) as 'successfulbilled' "
				+ "         FROM celcom.SMSStatLog l  "
				+ "         WHERE price>=1 and SMSServiceID=2 AND charged=1 AND statusCode='Success' AND CMP_SKeyword='IOD0100' AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t3 "
				+ "      GROUP BY day"
				+ "   ) "
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players', 0 AS stopped, new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT count(*) as 'new_subs', date(timeStamp) as 'day'"
				+ "         FROM "+DB+".trivia_log tl  "
				+ "         WHERE answer='START' AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t4 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', 0 as 'active_players', stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT count(*) as 'stopped', date(timeStamp) as 'day'"
				+ "         FROM "+DB+".trivia_log tl  "
				+ "         WHERE answer='STOP' AND DATE(timeStamp)>='"+launchDate+"'  AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t5 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT  0 as 'psa_barred', 0 as 'no_balance', active_players as 'active_players', 0 AS stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT count(distinct msisdn) as 'active_players',date(timeStamp) as 'day'"
				+ "         FROM "+DB+".trivia_log tl  "
				+ "         WHERE answer not in ('STOP') AND price>0 AND DATE(timeStamp)>='"+launchDate+"'  AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t5 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT 0 as 'psa_barred', insufficient as 'no_balance', 0 as 'active_players',0 AS stopped, 0 as new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT sum(price) as 'insufficient', date(timeStamp) as 'day', count(*) as 'no_balance' "
				+ "         FROM celcom.SMSStatLog l  "
				+ "         WHERE price>=1 and SMSServiceID=2 AND statusCode='PSAInsufficientBalance'  AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t3 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT barred as 'psa_barred', 0 as 'no_balance', 0 as 'active_players',0 AS stopped, 0 as new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT sum(price) as 'lost_barred', date(timeStamp) as 'day', count(*) as 'barred' "
				+ "         FROM celcom.SMSStatLog l  "
				+ "         WHERE price>=1 and SMSServiceID=2 AND statusCode='PSANumberBarred'  AND DATE(timeStamp)>='"+launchDate+"' AND DATE(timestamp)<CURRENT_DATE"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t3 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				+ ") "
				+ "AS q "
				+ "LEFT JOIN "+DB+".country c ON (c.id = q.country_id) "
				+ "GROUP BY country_id, day "
				+ "ORDER BY name ASC, day DESC";
			
		}
		
		return sql;
	}
	
	
	
	
	private static HSSFSheet setupCountrySheet(HSSFSheet s, String type) {
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
			poi.writeCell(s, null, 0, ++c, "Cumulative active subs");
			poi.writeCell(s, null, 0, ++c, "Insufficient Balance");
			poi.writeCell(s, null, 0, ++c, "PSA Number Barred");
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
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 12000);
		s.setColumnWidth(++c, (short) 12000);
		if(type.equals(SMS_TRAFFIC)){
			s.setColumnWidth(++c, (short) 7000);
			s.setColumnWidth(++c, (short) 7000);
			s.setColumnWidth(++c, (short) 7000);
		}
		//s.setColumnWidth(++c, (short) 7000);
		return s;
	}
	
	
	public static HSSFSheet writeCountryBottom(HSSFSheet s, HSSFWorkbook wb, int r, String type) {

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
		
		poi.writeCell(s, null, ++r, 1,
		"Active Subscribers - Subscribers who've been billed at least once on the day.");
		
		poi.writeCell(s, null, ++r, 1,
		"Cummulative active subscriber - Cumulative number for all active subs and have been billeds");
		poi.writeCell(s, null, ++r, 1,
		"Insufficient Balance - Transactions that did not succeed due to insufficient balance");
		poi.writeCell(s, null, ++r, 1,
		"PSA Number Barred - Transactions that did not succeed due to sub being barred in celcom billing system");
		

		return s;

	}
	
	
	
	public static HSSFCellStyle getCellStyle(HSSFWorkbook wb, String formatstr,
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

	private static double getAccumulatedRevenue(ResultSet rs, String type) throws Exception {
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
	
	
	
	public static int getDays(ResultSet rs) throws Exception {
		rs.last();
		int days = rs.getRow();
		rs.beforeFirst();
		return days;
	}
	
	
	
	



}
