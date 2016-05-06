package com.pixelandtag.utilities;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.pixelandtag.dating.entities.PersonDatingProfile;

public class Test {

	public static void main(String[] args) throws IOException {
		System.out.println(  getProbabilityStr(BigDecimal.valueOf(0.666632)) );
	}
	
	
	private static String getProbabilityStr(BigDecimal reply_probability) {
		if(reply_probability==null || reply_probability.compareTo(BigDecimal.ZERO)<=0)
			return "0%";
		else
			return reply_probability.multiply(BigDecimal.valueOf(100L)).setScale(2, BigDecimal.ROUND_UP).toString()+"%";
	}

}
