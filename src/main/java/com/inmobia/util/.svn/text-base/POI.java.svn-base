package com.inmobia.util;

import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class POI {

	public POI() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void writeCellFormula(HSSFSheet s, HSSFCellStyle style, int r, int c, String value) {
		getCell(s,style,r,c).setCellFormula(value);
	}
	public void writeCell(HSSFSheet s, HSSFCellStyle style, int r, int c, String value) {
		getCell(s,style,r,c).setCellValue(new HSSFRichTextString(value));
	}
	public void writeCell(HSSFSheet s, HSSFCellStyle style, int r, int c, boolean value) {
		getCell(s,style,r,c).setCellValue(value);
	}
	public void writeCell(HSSFSheet s, HSSFCellStyle style, int r, int c, double value) {
		getCell(s,style,r,c).setCellValue(value);
	}
	public void writeFormula(HSSFSheet s, HSSFCellStyle style, int r, int c, String value) {
		getCell(s,style,r,c).setCellFormula(value);
	}
	public HSSFCell getCell(HSSFSheet s, HSSFCellStyle style, int r, int c) {
		HSSFRow row = null;
		if (s.getRow(r) != null)
			row = s.getRow(r);
		else 
			row = s.createRow(r);
		
		HSSFCell cell = row.createCell(c);
		
		if (style != null) 
			cell.setCellStyle(style);
		
		return cell;
	}
	
	public HSSFSheet createSheet (String name, String extension, HSSFWorkbook wb) {
		HSSFSheet s = null;
    	name = name.replace("/","").replace("\\","").replace("*","").replace("?","").replace("[","").replace("]","");
    	if (name.length() + extension.length() > 30) {
    		name = name.substring(0,name.length()-(name.length() + extension.length() - 30));
    	}

    	boolean unfinished = true;
    	int count = 0;
    	while (unfinished) {
    		unfinished = false;
    		try {
    			s = wb.createSheet(name + extension);
    		}
    		catch (Exception e) {
    			if (count == 9) {
    				return null;
    			}
    			count++;
    			name = name + count;
    			unfinished = true;
    		}
    	}
    	
    	return s;
	}
}
