package com.inmobia.axiata.reports;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import com.inmobia.axiata.reports.bean.RevenueBean;
import com.inmobia.axiata.reports.bean.SubscriptionBean;

/**
 * 
 * RevenueReport
 * 
 * @author <a href="mailto:enter email address">Paul</a>
 * @version enter version, 26 Jun 2013
 * @since enter jdk version
 */
public class DailyReport {

	private Properties properties;

	private String DBNAME = "`celcom`";

	private Logger logger = Logger.getLogger(getClass());
	
	private HSSFWorkbook workbook = null;
	
	private HSSFSheet sheet;
	
	private String strdate;
	
	private String filename;
	
	private String[] header = {"Service",
			"Revenue",
			"Average Revenue"};
	
	
	private String[] subheader = {"Day",
			"New Subscribers",
			"Unsubscribers", "Total Subscriber"};
	
	

	public DailyReport() {

		logger.debug("Revenue Report initialized");

		try {
			properties = PropertyLoader.getPropertyFile("trivia.properties");
			PropertyConfigurator.configure(PropertyLoader
					.getPropertyFile("log4.properties"));
		} catch (Exception e) {
			logger.error(e);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		strdate = sdf.format(new Date());

		filename = "reports/" + properties.getProperty("filePrefix")
				+ "-" + strdate + ".xls";// filePrefix
		logger.debug("filename to create: " + filename);
	}
	
	
	public static void main(String[] args) {
		DailyReport revenueReport = new DailyReport();
		BasicConfigurator.configure();
		revenueReport.createReport();
		revenueReport.sendReport();
	}
	
	/**
	 * 
	 * <p>
	 * Write Doc
	 * </p>
	 * @throws IOException
	 */
	private void writeDocument() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(filename);
		workbook.write(fileOutputStream);
		fileOutputStream.close();
		
	}

	/**
	 * 
	 * <p>
	 * Create Report
	 * </p>
	 */
	public void createReport(){
		sheet = createSheet("Daily revenue numbers for " + strdate);
		HSSFRow row = null;
		HSSFCell cell = null;
		//sheet.createFreezePane(1, 1);
		List<RevenueBean> revenueBeans = getRevenueData();
		RevenueBean revenueBean = null;
		double total = 0.00;
		double totalAvg = 0.00;
		int k = 1;
		for (int i = 0; i < header.length; i++) {
			row = sheet.createRow(i + 1);
			cell = row.createCell(0);
			cell.setCellValue(header[i]);
			for(int j=0 ; j < revenueBeans.size(); j++){
				revenueBean = revenueBeans.get(j);
				cell = row.createCell(j + 1);
				if (i == 0)
					cell.setCellValue(revenueBean.getService());
				else if (i == 1){
					total += revenueBean.getToday();
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(revenueBean.getToday());
				}
				else if (i == 3){
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					totalAvg += revenueBean.getAverage();
					cell.setCellValue(revenueBean.getAverage());
				}
				k++;
			}
			/*cell = row.createCell(k + 1);
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(total);
			cell = row.createCell(k + 1);
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(totalAvg);*/
		}
		for (int i = 0; i < header.length; i++) {
			sheet.autoSizeColumn(i);
		}
		sheet.createFreezePane(1, 1);
		
		k = 1;
		List<SubscriptionBean> subscriptionBeans = getSubscriptionData();
		sheet = createSheet("Subscription report for " + strdate);
		SubscriptionBean subscriptionBean = null;
		for (int i = 0; i < subheader.length; i++) {
			row = sheet.createRow(i + 1);
			cell = row.createCell(0);
			cell.setCellValue(subheader[i]);
			for(int j=0; j<subscriptionBeans.size(); j++){
				subscriptionBean = subscriptionBeans.get(j);
				cell = row.createCell(j + 1);
				if (i == 0)
					cell.setCellValue(subscriptionBean.getDate());
				else if (i == 1){
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(subscriptionBean.getSubscribers());
				}
				else if (i == 2){
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(subscriptionBean.getUnsubscribers());
				}
				else if (i == 3){
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(subscriptionBean.getTotalSubscribers());
				}	
			}
			
		}
		for (int i = 0; i < subheader.length; i++) {
			sheet.autoSizeColumn(i);
		}
		sheet.createFreezePane(1, 1);
		
		try {
			writeDocument();
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	public void sendReport(){
		MailSender sender = null;
		try {
			sender = new MailSender("no-reply@inmobia.com", properties.getProperty("reportmail"), strdate +" report", "Please find report attached", filename);
			sender.sendEmail();
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		
	}
	
	/**
	 * 
	 * <p>
	 * Provide a brief description of what the method does
	 * </p>
	 * @param sheetName
	 * @return
	 */
	private HSSFSheet createSheet(String sheetName) {

		if (workbook == null)
			workbook = new HSSFWorkbook();
		return workbook.createSheet(sheetName);
	}
	/**
	 * 
	 * <p>
	 * Get all past dates from DB
	 * </p>
	 * 
	 * @return
	 */
	private List<String> getDates() {

		List<String> dates = new ArrayList<String>();
		String sql = "select Date(`subscription_timeStamp`) as subdate from "
				+ DBNAME
				+ ".`subscription` where Date(`subscription_timeStamp`) <= DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY) group by Date(`subscription_timeStamp`)";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			String date = null;
			while (resultSet.next()) {
				date = resultSet.getString("subdate");
				logger.info("Date :: " + date);
				dates.add(date);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}

		}
		return dates;
	}

	/**
	 * 
	 * <p>
	 * Subscription Data
	 * </p>
	 * 
	 * @return
	 */
	private List<SubscriptionBean> getSubscriptionData() {

		List<SubscriptionBean> subscriptionBeans = new ArrayList<SubscriptionBean>();
		String sql = "SELECT (SELECT count(s.`msisdn`) FROM "
				+ DBNAME
				+ ".`subscription` s WHERE s.`subscription_status` = 'confirmed' AND Date(s.`subscription_timeStamp`) = ?) AS subscribers, (SELECT count(s.`msisdn`) FROM "
				+ DBNAME
				+ ".`subscription` s WHERE s.`subscription_status` = 'unsubscribed' AND Date(s.`subscription_timeStamp`) = ?) AS unsubscribers, (SELECT count(s.`msisdn`) FROM "
				+ DBNAME
				+ ".`subscription` s WHERE s.`subscription_status` = 'confirmed' AND Date(s.`subscription_timeStamp`) = ?) AS totalSubs";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try {
			List<String> dates = getDates();
			logger.info("Report for dates " + dates.size());
			connection = getConnection();
			String date = null;
			SubscriptionBean subscriptionBean = null;
			for (int i = 0; i < dates.size(); i++) {
				date = dates.get(i);
				statement = connection.prepareStatement(sql);
				statement.setString(1, date);
				statement.setString(2, date);
				statement.setString(3, date);
				resultSet = statement.executeQuery();
				if (resultSet.next()) {
					subscriptionBean = new SubscriptionBean();
					subscriptionBean.setDate(date);
					subscriptionBean.setSubscribers(resultSet
							.getLong("subscribers"));
					subscriptionBean.setUnsubscribers(resultSet
							.getLong("unsubscribers"));
					subscriptionBean.setTotalSubscribers(resultSet
							.getLong("totalSubs"));
					subscriptionBeans.add(subscriptionBean);
				}
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}

		}
		return subscriptionBeans;
	}

	/**
	 * 
	 * <p>
	 * Get Revenue data
	 * </p>
	 * 
	 * @return
	 */
	private List<RevenueBean> getRevenueData() {

		List<RevenueBean> revenues = new ArrayList<RevenueBean>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		String sql = "SELECT SUM(s.`price`) as revenue , (SELECT SUM(sp.`price`)/30  FROM "
				+ DBNAME
				+ ".`SMSStatLog` sp WHERE sp.`timeStamp` BETWEEN DATE_ADD(CURRENT_DATE, INTERVAL -30 DAY) AND curdate() AND sp.`SMSServiceID` = s.`SMSServiceID`) as average, ss.`service_name` from "
				+ DBNAME
				+ ".`SMSStatLog` s Left JOIN "
				+ DBNAME
				+ ".`sms_service` ss ON(s.`SMSServiceID` = ss.`id`) WHERE Date(s.`timeStamp`) = curdate() and `service_name` IS NOT NULL and `service_name` != 'Unknown Keyword' group by ss.id;";
		try {
			connection = getConnection();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			RevenueBean revenueBean = null; 
			String average = null;
			double avg = 0.00;
			double today = 0.00;
			while (resultSet.next()) {
				revenueBean = new RevenueBean();
				average = resultSet.getString("average");
				if(average == null || average.equalsIgnoreCase("NULL")){
					average = "0.00";
				}
				avg = Double.valueOf(average);
				today = resultSet.getDouble("revenue");
				revenueBean.setAverage(avg);
				revenueBean.setService(resultSet.getString("service_name"));
				revenueBean.setToday(today);
				revenues.add(revenueBean);
			}
		} catch (Exception e) {

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}

		}
		return revenues;
	}
	/**
	 * 
	 * <p>
	 * Get Connection
	 * </p>
	 * 
	 * @return
	 */
	private Connection getConnection() {

		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://db/celcom?user=root&password=");
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;

	}
}