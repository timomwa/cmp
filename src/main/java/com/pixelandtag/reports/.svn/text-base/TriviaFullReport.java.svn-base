package com.inmobia.axiata.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormat;

import com.inmobia.axiata.entities.Country;
import com.inmobia.axiata.web.draw.TriviaStats2;
import com.inmobia.celcom.autodraw.Alarm;
import com.inmobia.util.CurrencyExchange;
import com.inmobia.util.POI;

public class TriviaFullReport {
	static Properties properties = new Properties();
	static Properties log4j = new Properties();
	String DB = "axiata_trivia";
	POI poi = new POI();
	static Logger logger = Logger.getLogger(TriviaFullReport.class);
	final int US_CURRENCY_ID = 6;
	final boolean DEBUG = true;
	//CurrencyExchange myEx = new CurrencyExchange();
	//private Map<String, Country> countries = new LinkedHashMap<String, Country>();
	private static TriviaFullReport trivia;
	private Emailer emailer = null;
	private String server_tz = "+08:00";
	private String client_tz = "+08:00";
	private Alarm alarm = new Alarm();

	public TriviaFullReport() {
		logger.debug("Succesfully exiting constructor");
	}

	public void initialize(String[] args) {
		logger.debug("in initialize()");

		try {
			properties = PropertyLoader
					.getPropertyFile("trivia.properties");
			log4j  = PropertyLoader
			.getPropertyFile("log4.properties");
			
			PropertyConfigurator.configure(log4j);
		
		} catch (Exception e) {
			logger.error(e, e);
		}
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strdate = null;
		if (args.length == 0)
			strdate = sdf.format(new Date());
		else
			strdate = args[0];

		String filename = "reports/" + properties.getProperty("filePrefix")
				+ "-" + strdate + ".xls";// filePrefix
		logger.debug("filename to create: " + filename);

		emailer = new Emailer();
		
		
		//if((new File(filename).exists()) && properties.getProperty("redoReport").equals("false")){
			//emailer.sendEmail("noreply@inmobia.com",properties.getProperty("email"),properties.getProperty("emailTitle"), "Hi,\n\nPlease find the earlier generated report attached",filename);
		//	return;
		//}
		
		try {
			// trivia = new TriviaFullReport();
			logger.debug("if trivia.generatereport()");
			//if (!trivia.generateReport(strdate, filename)) {
			
			if(!TriviaStats2.createFile(args, filename)){	
				System.out
						.println("TriviaStats2.createFile : "+false+" we didnt make it");
				return;
			}
			if (properties.getProperty("email") != null) {
				
				alarm.send(properties.getProperty("email"), properties.getProperty("emailTitle"), "Hi,\n\nPlease find the earlier generated report attached",filename);
				
				//emailer.sendEmail("noreply@inmobia.com",properties.getProperty("email"),properties.getProperty("emailTitle"), "Hi,\n\nPlease find the report attached",filename);
				
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		logger.info("in main()");
		trivia = new TriviaFullReport();
		trivia.initialize(args);

	}

	private HSSFSheet setupOverviewSheet(HSSFSheet s) {
		s.createFreezePane(1, 1);
		
		String [] headings = {"Revenue per day (USD)","Average revenue per day"};
		for(int c = 0; c<headings.length; c++){
			poi.writeCell(s, null, 0,c+1 , headings[c]);
			s.setColumnWidth(c+1, (short) 7000);
		}
		return s;
	}

	private HSSFSheet setupCountrySheet(HSSFSheet s) {
		s.createFreezePane(1, 1);
		int c = 0;
		poi.writeCell(s, null, 0, ++c, "Revenue per day (USD)");
		poi.writeCell(s, null, 0, ++c, "Number of days");
		poi.writeCell(s, null, 0, ++c, "Average revenue per day");
		poi.writeCell(s, null, 0, ++c, "Trend %");
		poi.writeCell(s, null, 0, ++c, "Accumulated revenue Trivia");
		poi.writeCell(s, null, 0, ++c, "Number of SMS sent per day");
		poi.writeCell(s, null, 0, ++c, "Price per SMS USD");

		c++;
		poi.writeCell(s, null, 0, ++c, "Active players");
		poi.writeCell(s, null, 0, ++c, "Total Subscribed");
		poi.writeCell(s, null, 0, ++c, "New players");
		poi.writeCell(s, null, 0, ++c, "Stopped players");
		poi.writeCell(s, null, 0, ++c, "Largest number of answers");
		poi.writeCell(s, null, 0, ++c, "Average SMS/day");
		poi.writeCell(s, null, 0, ++c, "Avg sms per active player");

		c = 0;
		s.setColumnWidth(c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 700);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		s.setColumnWidth(++c, (short) 7000);
		return s;
	}

	public HSSFSheet writeCountryBottom(HSSFSheet s, HSSFWorkbook wb, int r) {

		int c = 0;
		short bgcolor = HSSFColor.WHITE.index;
		poi.writeCell(s, null, ++r, c++, "Total");
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(B2:B" + r + ")");
		c++;
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(D2:D" + r + ")");
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"ROUND(B" + (r + 1) + "/D" + (r + 1) + "*100,0)-100");
		c++;
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(G2:G" + r + ")");
		c++;
		c++;
		c++;
		c++;
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(L2:L" + r + ")");
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(M2:M" + r + ")");
		poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), r, c++,
				"SUM(N2:N" + r + ")");

		r += 5;

		poi.writeCell(s, null, ++r, 1,
				"Revenue per day (USD) is the incom of the day");
		poi.writeCell(s, null, ++r, 1,
				"Number of days is the number of days played");
		poi.writeCell(s, null, ++r, 1,
				"Average revenue per day is the average incom up until this day");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Trend % is the percentage of Revenue per day(USD) compared to Average revenue per day");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Accumulated revenue Trivia is the total revenue of the entire period up until this day");
		poi.writeCell(s, null, ++r, 1,
				"Number of SMS sent per day is the number sent SMS this day");
		poi.writeCell(s, null, ++r, 1,
				"Price per SMS USD is the price per SMS in USD");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Active players is the number of players that played this day and didn't opt out as their last action");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Total subscribers is the number of players that played during the period and didn't opt out as their last action");
		poi.writeCell(s, null, ++r, 1,
				"New players is the number of players that started this day");
		poi.writeCell(s, null, ++r, 1,
				"Stopped players is the number of players that stopped this day");
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Largest number of answers is the number of answers the top player had this day.");
		
		poi.writeCell(
				s,
				null,
				++r,
				1,
				"Active Subscribers - Number of subscribers who've been playing and have been billed at least once");

		return s;

	}

	public boolean generateReport(String strdate, String filename) {

		

		int endRow = 0;
		int startRow = 1;

		Connection con = null;

		String curDate = "";

		if (strdate == null)
			curDate = "CURDATE()";
		else
			curDate = "CURDATE()";// "'"+strdate+"'";

		try {
			HSSFWorkbook wb = new HSSFWorkbook();

			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(properties
					.getProperty("connstring"));
			int country_id = 1;
			
			String sql = "SELECT SUM(stopped) AS 'stopped', SUM(new_subs) AS 'new_subs', amount as 'price', country_id, day, c.name AS name, CURDATE() AS current_day, SUM(total_content) total_content_sent, SUM(revenue) revenue, SUM(total_players) total_players, SUM(highest_content_count_recorded) highest_content_count_for_single_sub "
				+ "FROM ("
				+ "   ( "
				+ "      SELECT 0 AS stopped, 0 as new_subs, amount, country_id, DATE(timestamp) day, 0 AS total_content, 0 AS revenue, COUNT(DISTINCT MSISDN) AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM "+DB+".trivia_master_log l  "
				+ "      LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "      WHERE country_id = "
				+ country_id
				+ " AND answer IN ('SMS', 'MMS') "
				+ "      GROUP BY country_id,DATE(timestamp)) "
				+ "   UNION ALL "
				+ "   ( "
				+ "      SELECT 0 AS stopped, 0 as new_subs,0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, MAX(c) AS highest_content_count_recorded "
				+ "      FROM ( "
				+ "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c "
				+ "         FROM "+DB+".trivia_master_log l "
				+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "         WHERE country_id = "
				+ country_id
				+ " AND answer IN ('SMS', 'MMS') "
				+ "         GROUP BY country_id,DATE(timestamp),msisdn"
				+ "      ) AS t2 "
				+ "      GROUP BY country_id,day "
				+ "   ) "
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT 0 AS stopped, 0 as new_subs, 0 as amount, country_id, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT country_id,DATE(timestamp) AS day,answer "
				+ "         FROM "+DB+".trivia_master_log l  "
				+ "         LEFT JOIN "+DB+".country c ON (c.id = l.country_id) "
				+ "         WHERE country_id = "
				+ country_id
				+ "         GROUP BY country_id,DATE(timestamp), msisdn "
				+ "	      ORDER BY country_id,timestamp DESC "
				+ "      ) AS t1 "
				+ "      WHERE answer != 'STOP' "
				+ "      GROUP BY country_id,day "
				+ "   ) "
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT 0 AS stopped, 0 as new_subs, 0 as amount, 1, day, successfulbilled AS total_content, true_revenue AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT sum(price) as 'true_revenue', date(timeStamp) as 'day', count(*) as 'successfulbilled' "
				+ "         FROM celcom.SMSStatLog l  "
				+ "         WHERE price>=1 and SMSServiceID=2 AND statusCode='Success'"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t3 "
				+ "      GROUP BY day"
				+ "   ) "
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT 0 AS stopped, new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT count(*) as 'new_subs', date(timeStamp) as 'day'"
				+ "         FROM "+DB+".trivia_log tl  "
				+ "         WHERE answer='START'"
				+ "         GROUP BY date(timeStamp)"
				+ "	      ORDER BY day DESC "
				+ "      ) AS t4 "
				+ "      GROUP BY day"
				+ "   ) "
				
				
				+ "   UNION ALL "
				+ "   ("
				+ "      SELECT stopped, 0 AS new_subs, 0 as amount, 1, day, 0 AS total_content, 0 AS revenue, 0 AS total_players, 0 AS highest_content_count_recorded "
				+ "      FROM (  "
				+ "         SELECT count(*) as 'stopped', date(timeStamp) as 'day'"
				+ "         FROM "+DB+".trivia_log tl  "
				+ "         WHERE answer='STOP'"
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
			/*
			 * String sql =
			 * "SELECT c.telco AS telco, c.operator AS operator, SUM(active_players) AS active_players,c.price price,country_id, day, c.name AS name, "
			 * + curDate +
			 * " AS current_day, SUM(total) total, SUM(price_sum) price_sum, SUM(total_players) total_players, SUM(signin) signin, SUM(signout) signout, SUM(top_answers) top_players, c.dailyRevTarget, c.launchDate, answer "
			 * + "FROM (" + "   ( " +
			 * "      SELECT 0 AS active_players,country_id, DATE(timestamp) day, COUNT(*) total, SUM(price) price_sum, COUNT(DISTINCT MSISDN) AS total_players, SUM(IF(answer='START',1,0)) AS signin, SUM(IF(answer='STOP',1,0)) AS signout, 0 AS top_answers, answer "
			 * + "      FROM trivia_master_log l  " +
			 * "      LEFT JOIN country c ON (c.id = l.country_id) " +
			 * "      WHERE timestamp < " + curDate +
			 * " AND c.islive  = 1 and l.timestamp >= c.launchDate  AND answer IN ('USSD INPUT','a','b','','START','STOP') "
			 * + "      GROUP BY country_id,DATE(timestamp)) " + "   UNION ALL "
			 * + "   ( " +
			 * "      SELECT 0 AS active_players, country_id, day, 0 AS total, 0 AS price_sum, 0 AS total_players, 0 AS signin, 0 AS signout, MAX(c) AS top_answers, answer "
			 * + "      FROM ( " +
			 * "         SELECT country_id,DATE(timestamp) as day, COUNT(*) c, answer "
			 * + "         FROM trivia_master_log l " +
			 * "         LEFT JOIN country c ON (c.id = l.country_id) " +
			 * "         WHERE timestamp < " + curDate +
			 * " AND c.islive  = 1 and l.timestamp >= c.launchDate AND answer IN ('a','b') "
			 * + "         GROUP BY country_id,DATE(timestamp),msisdn" +
			 * "      ) AS t2 " + "      GROUP BY country_id,day " + "   ) " +
			 * "   UNION ALL " + "   (" +
			 * "      SELECT COUNT(*) AS active_players , country_id, day, 0 AS total, 0 AS price_sum, 0 AS total_players, 0 AS signin, 0 AS signout, 0 AS top_answers, answer "
			 * + "      FROM (  " +
			 * "         SELECT country_id,DATE(timestamp) AS day,answer " +
			 * "         FROM trivia_master_log l  " +
			 * "         LEFT JOIN country c ON (c.id = l.country_id) " +
			 * "         WHERE timestamp < " + curDate +
			 * " AND c.islive  = 1 and l.timestamp >= c.launchDate "+
			 * "         GROUP BY country_id,DATE(timestamp), msisdn " +
			 * "	      ORDER BY country_id,timestamp DESC " + "      ) AS t1 " +
			 * "      WHERE answer != 'STOP' " +
			 * "      GROUP BY country_id,day " + "   ) " + ") " + "AS q " +
			 * "LEFT JOIN country c ON (c.id = q.country_id) " +
			 * "GROUP BY country_id, day " + "ORDER BY name ASC, day DESC;";
			 */

			logger.debug("Just before executing th huge query");

			PreparedStatement pstmt = con.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			System.out.println("executed query!");
			int col = 0;
			int row = 0;
			int overview_row = 0;
			int days = 0;
			String name = "";
			String answer = "";

			//HSSFSheet target = poi.createSheet("Target", "", wb);

			HSSFSheet overview = poi.createSheet("Overview", "", wb);

			//target = setUpTargetSheet(target);
			logger.info("target sheet set up fine");
			HSSFSheet s = null;

			overview = setupOverviewSheet(overview);
			logger.info("overview sheet set up fine");

			String revSQL = "SELECT day, SUM(count) count, price, exchange_rates_id, divide, IF(divide = 1,(SUM(count)*price)/100,SUM(count)*price) AS `value` "
					+ "FROM ( "
					+ "   SELECT DATE(i.timestamp) day, i.count count,i.price price, p.exchange_rates_id exchange_rates_id, p.divide divide  "
					+ "   FROM icp.incstat i  "
					+ "   LEFT JOIN icp.sms_operator_prices p ON (p.operator = i.operator AND p.exchange_rates_id > 0) "
					+ "   WHERE i.timestamp >= ? AND i.charged = 1 AND i.operator = ? AND i.telco = ? AND i.cmd = ? "
					+ "   GROUP BY i.id " + ") AS t " + "GROUP BY day ";
			ResultSet revRs = null;
			PreparedStatement revPstmt = con.prepareStatement(revSQL);

			Map<String, Double> targetMap = new HashMap<String, Double>();

			while (rs.next()) {
				// answer = rs.getString("answer");

				short bgcolor = HSSFColor.WHITE.index;

				if (!rs.getString("name").equalsIgnoreCase(name)) {
					if (name.length() > 0)
						writeCountryBottom(s, wb, row);

					name = rs.getString("name");

					s = poi.createSheet(
							rs.getString("name").replaceAll(" ", "_"), "", wb);

					try {
						targetMap.put(rs.getString("name"),
								Double.valueOf(rs.getString("dailyRevTarget")));
					} catch (Exception e) {
						System.out.println("ERROR: " + e.getMessage());
					}

					col = 0;
					// Country name
					poi.writeCell(overview, null, ++overview_row, col++,
							rs.getString("name"));
					// Revenue
					poi.writeCell(
							overview,
							getCellStyle(
									wb,
									"#,##0",
									rs.getInt("total") > getAverage(rs) ? bgcolor
											: HSSFColor.RED.index),
							overview_row, col++, rs.getInt("price_sum"));
					// Average revenue
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++, rs.getString("name").replaceAll(" ", "_")
									+ "!D2");
					// Trend
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++, "ROUND(B" + (overview_row + 1) + "/C"
									+ (overview_row + 1) + "*100,0)-100");
					// Accumulated Revenue
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++, rs.getString("name").replaceAll(" ", "_")
									+ "!F2");
					// Total SMS
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++, rs.getString("name").replaceAll(" ", "_")
									+ "!G2");

					col++;

					// Active players
					poi.writeCell(overview, getCellStyle(wb, "#,##0", bgcolor),
							overview_row, col++, rs.getInt("active_players"));
					// Total subscribers
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++, rs.getString("name").replaceAll(" ", "_")
									+ "!K2");
					// New players
					poi.writeCell(
							overview,
							getCellStyle(wb, "#,##0", rs.getInt("signin") >= rs
									.getInt("signout") ? bgcolor
									: HSSFColor.RED.index), overview_row,
							col++, rs.getInt("signin"));
					// Stopped players
					poi.writeCell(overview, getCellStyle(wb, "#,##0", bgcolor),
							overview_row, col++, rs.getInt("signout"));
					// Top messages
					poi.writeCell(overview, getCellStyle(wb, "#,##0", bgcolor),
							overview_row, col++, rs.getInt("top_players"));
					// Average SMS per day
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++,
							"(" + rs.getString("name").replaceAll(" ", "_")
									+ "!O2)");
					// Average SMS per active player
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							col++,
							"(" + rs.getString("name").replaceAll(" ", "_")
									+ "!P2)");

					days = getDays(rs);
					s = setupCountrySheet(s);
					row = 0;
					int i = 0;
					/*
					 * revPstmt.setString(++i, getFirstDay(rs));
					 * revPstmt.setString(++i, rs.getString("operator"));
					 * revPstmt.setString(++i, rs.getString("telco"));
					 * revPstmt.setString(++i, "default"); revRs =
					 * revPstmt.executeQuery();
					 */

				}

				if (rs.getInt("total") < getAverage(rs))
					bgcolor = HSSFColor.RED.index;

				col = 0;

				/*
				 * if(answer.equals("USSD INPUT")){ poi.writeCell(s,
				 * getCellStyle(wb,null, bgcolor), (++row+10), col++,
				 * rs.getString("day")); poi.writeFormula(s,
				 * getCellStyle(wb,"#,##0", bgcolor), (row+10), col++,
				 * "ROUND(G"+(row+1)+"*H"+(row+1)+",0)"); poi.writeCell(s,
				 * getCellStyle(wb,"#,##0", bgcolor), (row+10), col++, days--);
				 * poi.writeFormula(s,getCellStyle(wb,"#,##0",
				 * bgcolor),(row+10),
				 * col++,"ROUND(F"+(row+1)+"/C"+(row+1)+",0)");
				 * poi.writeFormula(s,getCellStyle(wb,"#,##0",
				 * bgcolor),(row+10),
				 * col++,"ROUND(B"+(row+1)+"/D"+(row+1)+"*100,0)-100");
				 * poi.writeFormula(s,getCellStyle(wb,"#,##0",
				 * bgcolor),(row+10),col++,"F"+(row+2)+"+B"+(row+1));
				 * poi.writeCell(s, getCellStyle(wb,"#,##0", bgcolor), (row+10),
				 * col++, rs.getInt("total")); //poi.writeCell(s,
				 * getCellStyle(wb,null, bgcolor), (row+10), col++,
				 * rs.getDouble("price")); poi.writeCell(s,
				 * getCellStyle(wb,null, bgcolor), (row+10), col++, 0.21d);
				 * poi.writeCell(s, getCellStyle(wb,"#,##0", bgcolor), (row+10),
				 * col++, ""); poi.writeCell(s, getCellStyle(wb,"#,##0",
				 * bgcolor), (row+10), col++, rs.getInt("active_players"));
				 * poi.writeCell(s, getCellStyle(wb,"#,##0", bgcolor), (row+10),
				 * col++, getTotalSubscribed(con, rs.getString("day"),
				 * rs.getInt("country_id"))); poi.writeCell(s,
				 * getCellStyle(wb,"#,##0", bgcolor), (row+10), col++,
				 * rs.getInt("signin")); poi.writeCell(s,
				 * getCellStyle(wb,"#,##0", bgcolor), (row+10), col++,
				 * rs.getInt("signout")); poi.writeCell(s,
				 * getCellStyle(wb,"#,##0", bgcolor), (row+10), col++,
				 * rs.getInt("top_players"));
				 * poi.writeFormula(s,getCellStyle(wb,"#,##0",
				 * bgcolor),(row+10),
				 * col++,"ROUND(G"+(row+1)+"/C"+(row+1)+",0)"); //average smsms
				 * per active player poi.writeFormula(s,getCellStyle(wb,"#,##0",
				 * bgcolor),row,col++,"ROUND(G"+(row+1)+"/J"+(row+1)+",0)");
				 * }else{
				 */
				poi.writeCell(s, getCellStyle(wb, null, bgcolor), ++row, col++,
						rs.getString("day"));
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "ROUND(G" + (row + 1) + "*H" + (row + 1) + ",0)");
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, days--);
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "ROUND(F" + (row + 1) + "/C" + (row + 1) + ",0)");
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "ROUND(B" + (row + 1) + "/D" + (row + 1)
								+ "*100,0)-100");
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "F" + (row + 2) + "+B" + (row + 1));
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("total"));
				poi.writeCell(s, getCellStyle(wb, null, bgcolor), row, col++,
						rs.getDouble("price"));
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "");
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("active_players"));
				poi.writeCell(
						s,
						getCellStyle(wb, "#,##0", bgcolor),
						row,
						col++,
						getTotalSubscribed(con, rs.getString("day"),
								rs.getInt("country_id")));
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("signin"));
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("signout"));
				poi.writeCell(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, rs.getInt("top_players"));
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "ROUND(G" + (row + 1) + "/C" + (row + 1) + ",0)");
				// average smsms per active player
				poi.writeFormula(s, getCellStyle(wb, "#,##0", bgcolor), row,
						col++, "ROUND(G" + (row + 1) + "/J" + (row + 1) + ",0)");
				// }

				// Write the price value we have in our database.
				/*
				 * revRs = getRowOfTheDay(revRs, rs.getString("day")); double
				 * us_price = revRs == null || revRs.isAfterLast() ? 0 :
				 * myEx.calculate
				 * (revRs.getInt("exchange_rates_id"),US_CURRENCY_ID
				 * ,revRs.getDouble("value")); poi.writeCell(s,
				 * getCellStyle(wb,"#,##0", bgcolor), row, col++, us_price);
				 */

				if (rs.isLast()) {

					logger.info("ROW K" + overview_row);
					writeCountryBottom(s, wb, row);
					logger.info("do we get here????");

					int c = 0;
					poi.writeCell(overview, null, ++overview_row, c++, "Total");

					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(B2:B" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(C2:C" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "ROUND(B" + (overview_row + 1) + "/C"
									+ (overview_row + 1) + "*100,0)-100");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(E2:E" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(F2:F" + overview_row + ")");
					c++;
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(H2:H" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(I2:I" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(J2:J" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(K2:K" + overview_row + ")");
					poi.writeFormula(overview,
							getCellStyle(wb, "#,##0", bgcolor), overview_row,
							c++, "SUM(L2:L" + overview_row + ")");

					endRow = overview_row;

					overview_row += 5;

					poi.writeCell(overview, null, ++overview_row, 1,
							"Revenue per day (USD) is the incom of yesterday for the Trivia");
					poi.writeCell(overview, null, ++overview_row, 1,
							"Average revenue per day is the average incom of the entire period");
					poi.writeCell(
							overview,
							null,
							++overview_row,
							1,
							"Trend % is the percentage of Revenue per day(USD) compared to Average revenue per day");
					poi.writeCell(overview, null, ++overview_row, 1,
							"Accumulated revenue Trivia is the total revenue of the entire period");
					poi.writeCell(overview, null, ++overview_row, 1,
							"Total SMS is the total amount of received SMS for the entire period");
					poi.writeCell(
							overview,
							null,
							++overview_row,
							1,
							"Active players is the number of players that played yesterday and didn't opt out as their last action");
					poi.writeCell(
							overview,
							null,
							++overview_row,
							1,
							"Total subscribers is the number of players that played during the period and didn't opt out as their last action");
					poi.writeCell(overview, null, ++overview_row, 1,
							"New players is the number of players that started yesterday");
					poi.writeCell(overview, null, ++overview_row, 1,
							"Stopped players is the number of players that stopped yesterday");
					poi.writeCell(
							overview,
							null,
							++overview_row,
							1,
							"Largest number of answers is the number of answers the top player had yesterday.");
					logger.info("Finally made th file");
				}

			}

			// Get a map with country - formatting on the target sheet
			Map<String, HSSFCellStyle> revenueMap = new LinkedHashMap<String, HSSFCellStyle>();

			for (int n = startRow; n < endRow; n++) {
				int columnWithFigure = 1;

				// if(name.equals("name"))
				/*Iterator<Map.Entry<String, Country>> it = countries.entrySet()
						.iterator();
				Map.Entry<String, Country> entry;
				Country country;
				String countryName;
				while (it.hasNext()) {

					entry = it.next();
					country = entry.getValue();
					countryName = country.getName();

					try {
						HSSFRow overViewRow = overview.getRow(n);
						HSSFCell cellWithFigure = overViewRow.getCell(1);
						if (countryName.equalsIgnoreCase(overViewRow.getCell(0)
								.getStringCellValue())) {

							double amount = cellWithFigure
									.getNumericCellValue();
							// System.out.println(countryName+"("+overViewRow.getCell(0).getStringCellValue()+")="+amount);
							String direction = "";

							HSSFCellStyle cellStyle = null;

							double percentChange = 0.0;
							double targetSet = targetMap.get(countryName);
							if (amount > targetSet) {
								direction = "UP";
								cellStyle = getCellStyle(wb, null,
										HSSFColor.GREEN.index);

								percentChange = ((amount - targetSet) / 100);
							}
							if (amount < targetSet) {
								direction = "DOWN";
								cellStyle = getCellStyle(wb, null,
										HSSFColor.RED.index);
								percentChange = ((amount - targetSet) / 100);
							}
							if (amount == targetMap.get(countryName)) {
								direction = "FLAT";
								cellStyle = getCellStyle(wb, null,
										HSSFColor.YELLOW.index);
								percentChange = ((amount - targetSet) / 100);
							}

							revenueMap.put(countryName, cellStyle);

							int countryColumn = country.getColumnPosition();

							// Formating for revenue per day...
							int rowToFormat = 17;

							HSSFRow myRow = target.getRow(rowToFormat);

							HSSFCell revenuePerDayCell = target.getRow(
									rowToFormat).getCell(countryColumn);

							revenuePerDayCell.setCellStyle(cellStyle);

							
							 * Drawing drawing =
							 * target.createDrawingPatriarch();
							 * 
							 * 
							 * CreationHelper factory = wb.getCreationHelper();
							 * // When the comment box is visible, have it show
							 * in a 1x3 space ClientAnchor anchor =
							 * factory.createClientAnchor();
							 * anchor.setCol1(revenuePerDayCell
							 * .getColumnIndex());
							 * anchor.setCol2(revenuePerDayCell
							 * .getColumnIndex()+1); anchor.setRow1(1);
							 * anchor.setRow2(3);
							 * 
							 * // Create the comment and set the text+author
							 * Comment comment =
							 * drawing.createCellComment(anchor); RichTextString
							 * str =
							 * factory.createRichTextString("Target "+(percentChange
							 * >0 ?" exeeded by ":
							 * "off by ")+percentChange+"%");
							 * comment.setString(str);
							 * comment.setAuthor("ReportingModule");
							 * 
							 * // Assign the comment to the cell
							 * revenuePerDayCell.setCellComment(comment);
							 

						}

					} catch (Exception e) {
						System.out.println("ERROR: " + e.getMessage());
					}

				}*/

			}

			System.out.println(revenueMap);
			rs.close();
			pstmt.close();

			FileOutputStream fileOut = new FileOutputStream(filename);
			wb.write(fileOut);
			fileOut.close();

			System.out.println("done creating doc!");

		} catch (Exception e) {
			logger.error(e, e);
			return false;
		}
		return true;
	}

	private HSSFSheet setUpTargetSheet(HSSFSheet target) {

		// countries.put("Total Per Month", new
		// Country("Zambia",3492562,167710,7328,0.4,5d,5d,4d,10.7,1.91,18d,7d,5d,
		// 24336.81, 730104.34,1047769));

		/*int target_col = 2;
		int target_row = 0;

		Set<String> strSet = countries.keySet();

		Iterator<String> itst = strSet.iterator();

		String val = "";

		// overview_row=1;
		target_col = 1;
		poi.writeCell(target, null, ++target_row, target_col, "Customer base");
		poi.writeCell(target, null, target_row++, target_col,
				"Total Subscribed");
		poi.writeCell(target, null, target_row++, target_col,
				"Avg Active player");

		target_row++;

		poi.writeCell(target, null, target_row++, target_col, "SMS Rate");
		poi.writeCell(target, null, target_row++, target_col,
				"Avg Number of SMS in Mama");

		target_row++;

		poi.writeCell(target, null, target_row++, target_col,
				"% people participating in Mama");
		poi.writeCell(target, null, target_row++, target_col,
				"% active participants per day in Mama");

		target_row++;

		poi.writeCell(target, null, target_row++, target_col, "ARPU");
		poi.writeCell(target, null, target_row++, target_col,
				"Customer spent per day");
		poi.writeCell(target, null, target_row++, target_col,
				"% Spent for Customer");

		target_row++;

		poi.writeCell(target, null, target_row++, target_col,
				"% people participating");
		poi.writeCell(target, null, target_row++, target_col,
				"% active participants per day");

		target_row++;
		poi.writeCell(target, null, target_row++, target_col, "Revenue per day");

		target_row++;
		poi.writeCell(target, null, target_row++, target_col,
				"Revenue per month");

		target_row++;
		target_row++;
		poi.writeCell(target, null, target_row++, target_col, "Inmobia");

		target_row = 0;

		target_col = 2;

		Country country;

		// Fill the data
		while (itst.hasNext()) {

			target_col++;

			val = itst.next();

			country = countries.get(val);

			poi.writeCell(target, null, target_row, target_col, val);

			// Customer base
			poi.writeCell(target, null, (target_row + 1), (target_col),
					country.getCustomerBase());
			// Total Subscribed
			poi.writeCell(target, null, (target_row + 2), (target_col),
					country.getTitalSubscribed());

			// SMS Rate
			poi.writeCell(target, null, (target_row + 4), (target_col),
					country.getSmsRate());
			// Avg Number of SMS in Mama
			poi.writeCell(target, null, (target_row + 5), (target_col),
					country.getAvgNumSMSinMama());

			// %people participating in mamas
			poi.writeCell(target, null, (target_row + 7), (target_col),
					country.getPercentPeopleParticipatingInMamas());
			// % active participants per day in Mama
			poi.writeCell(target, null, (target_row + 8), (target_col),
					country.getActiveParticipantPerDayInMama());

			// ARPU
			poi.writeCell(target, null, (target_row + 10), (target_col),
					country.getARPU());
			// Customer spent per day
			poi.writeCell(target, null, (target_row + 11), (target_col),
					country.getCustomerSpendPerDat());
			// % Spent for Customer
			poi.writeCell(target, null, (target_row + 12), (target_col),
					country.getPercentSpendPerCustomer());

			// % people participating
			poi.writeCell(target, null, (target_row + 14), (target_col),
					country.getPercentPeopleParticipating());
			// % active participants per day
			poi.writeCell(target, null, (target_row + 15), (target_col),
					country.getPercentActiveParticipantPerDay());

			// Revenue per day
			poi.writeCell(target, null, (target_row + 17), (target_col),
					country.getRevenuePerDay());

			// Revenue per month
			poi.writeCell(target, null, (target_row + 19), (target_col),
					country.getRevenuePerMonth());

			// Inmobia
			poi.writeCell(target, null, (target_row + 22), (target_col),
					country.getInmobia());

		}
*/
		return null;
	}

	public int getDays(ResultSet rs) throws Exception {
		String name = rs.getString("name");
		int row = rs.getRow();
		int days = 1;
		while (rs.next()) {
			if (rs.getString("name").equalsIgnoreCase(name))
				days++;
			else
				break;
		}
		rs.absolute(row);
		return days;
	}

	public String getFirstDay(ResultSet rs) throws Exception {
		String name = rs.getString("name");
		int row = rs.getRow();
		String day = rs.getString("day");
		while (rs.next()) {
			if (rs.getString("name").equalsIgnoreCase(name))
				day = rs.getString("day");
			else
				break;
		}
		rs.absolute(row);
		return day;
	}

	public double getAverage(ResultSet rs) throws Exception {
		String name = rs.getString("name");
		int row = rs.getRow();
		double days = 1;
		double total = 0;
		while (rs.next()) {
			if (rs.getString("name").equalsIgnoreCase(name)) {
				days++;
				total += rs.getDouble("total");
			} else
				break;
		}
		rs.absolute(row);
		return total / days;
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

	public int getTotalSubscribed(Connection con, String day, int country_id)
			throws Exception {
		PreparedStatement pstmt = con.prepareStatement("SELECT COUNT(*) c "
				+ "FROM (  "
				+ "   SELECT country_id,DATE(timestamp) AS day,answer "
				+ "   FROM trivia_master_log l  "
				+ "   WHERE DATE(timestamp) <= ? " + "   AND country_id = ? "
				+ "   GROUP BY msisdn "
				+ "   ORDER BY country_id,timestamp DESC " + ") AS t1 "
				+ "WHERE answer != 'STOP' ");

		pstmt.setString(1, day);
		pstmt.setInt(2, country_id);
		ResultSet rs = pstmt.executeQuery();
		int totalSubscribed = rs.next() ? rs.getInt("c") : 0;
		rs.close();
		pstmt.close();
		return totalSubscribed;
	}

	private ResultSet getRowOfTheDay(ResultSet rs, String day) throws Exception {
		if (rs == null)
			return null;

		rs.beforeFirst();

		if (!rs.next())
			return null;

		rs.beforeFirst();

		while (rs.next()) {
			if (rs.getString("day").equalsIgnoreCase(day))
				break;
			if (rs.isLast())
				rs.afterLast();
		}
		return rs;
	}
}
