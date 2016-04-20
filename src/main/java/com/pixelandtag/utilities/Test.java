package com.pixelandtag.utilities;

import java.io.IOException;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Test {

	public static void main(String[] args) throws IOException {
		String xml = "<logo/><transactionid>77</transactionid>";
		System.out.println("1. "+getValue(xml, "transactionid"));
		System.out.println(!Boolean.TRUE+"2. "+getValue(xml, "logo"));
	}
	
	
	private static String getValue(String xml,String tagname) {
		String startTag = "<"+tagname+">";
		String endTag = "</"+tagname+">";
		int start = xml.indexOf(startTag)+startTag.length();
		int end  = xml.indexOf(endTag);
		if(start<0 || end<0)
			return "";
		return xml.substring(start, end);
	}

}
