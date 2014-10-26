/* Copyright (c) Inmobia Mobile Technology, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Inmobia 
 * Mobile Technology. ("Confidential Information").  You shall not disclose such 
 * Confidential Information and shall use it only in accordance with the terms 
 * of the license agreement you entered into with Inmobia Mobile Technology.
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.inmobia.axiata.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;


/**
 * ReportGenerator, Generates excel reports
 * 
 * @author <a href="mailto:montell@inmbia.com">Montell Tome</a>
 * @version 1.0, August 29, 2011
 * @since jdk 1.6
 */
public class ReportGenerator {
	private static final Logger LOGGER = Logger
			.getLogger(ReportGenerator.class);
	/**
	 * workbook, excel workbook
	 */
	private static HSSFWorkbook workbook;
	/**
	 * sheet, excel sheet
	 */
	private static HSSFSheet sheet;
	
	/**
	 * sheet, excel sheet2
	 */
	private static HSSFSheet sheet2;
	/**
	 * row, excel row
	 */
	private static HSSFRow row;
	
	/**
	 * row, excel row
	 */
	private static HSSFRow row2;
	/**
	 * cell, excel cell
	 */
	private static HSSFCell cell;
	/**
	 * cell, excel cell
	 */
	private static HSSFCell cell2;
	/**
	 * style, excel style
	 */
	private static HSSFCellStyle style;
	/**
	 * font, excel font
	 */
	private static Font font;

	/**
	 * Generates and returns a .xls report given the statusId,categoryId,date
	 * and msisdn
	 * 
	 * @param <code>int</code>statusId, status id
	 * @param <code>int</code>categoryId, category id
	 * @param <code>String</code>date
	 * @param <code>String</code>msisdn, subscriber's mobile number
	 * @param <code>Connection</code>connection to the database
	 * @return<code>File<String></code>
	 */
	public static File generateReport(String dateSelection, Connection connection) {
		
		DAO dao = new DAO(connection);
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet("Giant_Claimed");
		sheet2 = workbook.createSheet("Giant_Unclaimed");
		List<ReportDTO> reportRecs = dao.getReport(dateSelection);
		LOGGER.info("Found " + reportRecs.size() + " question(s)");
		createHeaderRow();
		Iterator<ReportDTO> iterator = reportRecs.iterator();
		int rowNumber = 1;
		ReportDTO question = null;
		while (iterator.hasNext()) {
			question = iterator.next();
			if(createRow(question, rowNumber))
			rowNumber++;
		}
		row = sheet.createRow(rowNumber);
		cell = row.createCell(1);
		cell.setCellStyle(createHeaderStyle());
		cell.setCellValue("Totals");
		
		cell = row.createCell(2);
		cell.setCellStyle(createTotalsStyle());
		cell.setCellFormula("sum(C2:C"+(rowNumber)+")");
		
		//cell = row.createCell(3);
		//cell.setCellStyle(createTotalsStyle());
		//cell.setCellFormula("sum(D2:D"+(rowNumber)+")");
		
		
		iterator = reportRecs.iterator();
		rowNumber = 1;
		question = null;
		while (iterator.hasNext()) {
			question = iterator.next();
			if(createRow2(question, rowNumber))
			rowNumber++;
		}
		
		
		
		row2 = sheet2.createRow(rowNumber);
		cell2 = row2.createCell(1);
		cell2.setCellStyle(createHeaderStyle());
		cell2.setCellValue("Totals");
		
		cell2 = row2.createCell(2);
		cell2.setCellStyle(createTotalsStyle());
		cell2.setCellFormula("sum(C2:C"+(rowNumber)+")");
		
	//	cell2 = row2.createCell(3);
		//cell2.setCellStyle(createTotalsStyle());
		//cell2.setCellFormula("sum(D2:D"+(rowNumber)+")");
		
		File report = ReportGenerator.writeDocument();
		LOGGER.info("Done creating report");
		LOGGER.info("Created report " + report.getAbsolutePath());
		return report;
	}

	/**
	 * Generates and returns a .xls report given the
	 * statusId,categoryId,classId,subCategoryId,date and msisdn
	 * 
	 * @param <code>int</code>statusId, status id
	 * @param <code>int</code>categoryId, category id
	 * @param <code>String</code>date
	 * @param <code>String</code>msisdn, subscriber's mobile number
	 * @param <code>Connection</code>connection to the database
	 * @return<code>File<String></code>
	 */
	public static File generateReport(int statusId, int categoryId,
			int classId, int subCategoryId, String dates, String msisdn,
			Connection connection) {
		LOGGER.info("Creating report...");
		DAO dao = new DAO(connection);
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet("Nacc Report");
		List<ReportDTO> questions = dao.getReport(dates);
		LOGGER.info("Found " + questions.size() + " question(s)");
		createHeaderRow();
		Iterator<ReportDTO> iterator = questions.iterator();
		int rowNumber = 1;
		ReportDTO record = null;
		while (iterator.hasNext()) {
			record = iterator.next();
			createRow(record, rowNumber);
			createRow2(record, rowNumber);
			rowNumber++;
		}
		
		File report = ReportGenerator.writeDocument();
		LOGGER.info("Done creating report");
		LOGGER.info("Created report " + report.getAbsolutePath());
		return report;
	}

	/**
	 * Creates and populates the header row
	 * 
	 */
	public static void createHeaderRow() {
		LOGGER.info("Creating header row...");
		row = sheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellStyle(createHeaderStyle());
		cell.setCellValue("DATE WON");
		sheet.setColumnWidth(0, 4000);

		cell = row.createCell(1);
		cell.setCellStyle(createHeaderStyle());
		cell.setCellValue("Store_Name");
		sheet.setColumnWidth(1, 15000);

		cell = row.createCell(2);
		cell.setCellStyle(createHeaderStyle());
		cell.setCellValue("Claimed prizes (RM)");
		sheet.setColumnWidth(2, 6000);

	//	cell = row.createCell(3);
		//cell.setCellStyle(createHeaderStyle());
		//cell.setCellValue("Unclaimed prizes (RM)");
		//sheet.setColumnWidth(3, 6000);

		
		LOGGER.info("Done creating header row");
		
		
		
		
		
		LOGGER.info("Creating header row...");
		row2 = sheet2.createRow(0);
		cell2 = row2.createCell(0);
		cell2.setCellStyle(createHeaderStyle());
		cell2.setCellValue("DATE WON");
		sheet2.setColumnWidth(0, 4000);

		cell2 = row2.createCell(1);
		cell2.setCellStyle(createHeaderStyle());
		cell2.setCellValue("Store_Name");
		sheet2.setColumnWidth(1, 15000);

		//cell2 = row2.createCell(2);
		//cell2.setCellStyle(createHeaderStyle());
		//cell2.setCellValue("Claimed prizes (RM)");
		//sheet2.setColumnWidth(2, 6000);

		cell2 = row2.createCell(2);
		cell2.setCellStyle(createHeaderStyle());
		cell2.setCellValue("Unclaimed prizes (RM)");
		sheet2.setColumnWidth(2, 6000);

		
		LOGGER.info("Done creating header row");

	}

	
	
	/**
	 * Creates and populates a row given the question and the row number
	 * 
	 * @param<code>Question</code>question, question object to fetch data from
	 * @param <code>int</code>rowNumber, row number
	 */
	public static boolean createRow2(ReportDTO record, int rowNumber) {
		boolean created = false;
		if(record.getStore_name().trim().equalsIgnoreCase("Unclaimed")){
			row2 = sheet2.createRow(rowNumber);
			cell2 = row2.createCell(0);
			cell2.setCellValue(record.getTimeAwarded());
			cell2 = row2.createCell(1);
			cell2.setCellValue(record.getStore_name());
			//cell2 = row2.createCell(2);
			//cell2.setCellValue(record.getPrize_value_claimed());
			cell2 = row2.createCell(2);
			cell2.setCellValue(record.getPrize_value_unclaimed());
			created = true;
		}
		return created;
	}

	
	/**
	 * Creates and populates a row given the question and the row number
	 * 
	 * @param<code>Question</code>question, question object to fetch data from
	 * @param <code>int</code>rowNumber, row number
	 */
	public static boolean  createRow(ReportDTO record, int rowNumber) {
		boolean created = false;
		if(!record.getStore_name().trim().equalsIgnoreCase("Unclaimed")){
			row = sheet.createRow(rowNumber);
			cell = row.createCell(0);
			cell.setCellValue(record.getTimeAwarded());
			cell = row.createCell(1);
			cell.setCellValue(record.getStore_name());
			cell = row.createCell(2);
			cell.setCellValue(record.getPrize_value_claimed());
			created = true;
		}
		
		return created;
		//cell = row.createCell(3);
		//cell.setCellValue(record.getPrize_value_unclaimed());
	}

	/**
	 * Creates and returns the header cell style
	 * 
	 * @return<code>HSSFCellStyle</code>
	 */
	public static HSSFCellStyle createHeaderStyle() {

		font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.BLACK.index);

		style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.ALIGN_LEFT);
		setStyleBorder(style);
		style.setFont(font);
		style.setWrapText(true);
		return style;

	}
	
	
	/**
	 * Creates and returns the header cell style
	 * 
	 * @return<code>HSSFCellStyle</code>
	 */
	public static HSSFCellStyle createTotalsStyle() {

		font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.BLACK.index);

		style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		style.setVerticalAlignment(HSSFCellStyle.ALIGN_RIGHT);
		//style.set
		setStyleBorder(style);
		style.setFont(font);
		style.setWrapText(true);
		return style;

	}

	/**
	 * Sets the cell border
	 * 
	 * @param <code>HSSFCellStyle</code>style to set
	 */
	private static void setStyleBorder(HSSFCellStyle style) {
		style.setBorderBottom((short) 1);
		style.setBorderBottom((short) 1);
		style.setBorderLeft((short) 1);
		style.setBorderRight((short) 1);
		style.setBorderTop((short) 1);
	}

	/**
	 * Writes and returns an excel document
	 * 
	 * @return<code>File</code>
	 */
	public static File writeDocument() {

		File destDir = Utils.createDir();
		String filePath = destDir.getAbsolutePath() + Utils.PATH_SEPARATOR
				+ Utils.generateFileName();

		FileOutputStream fileOutputStream = null;
		File file = null;
		try {
			fileOutputStream = new FileOutputStream(filePath);
			workbook.write(fileOutputStream);
			fileOutputStream.close();
			file = new File(filePath);
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return file;
	}
}
