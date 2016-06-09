package com.pixelandtag.utilities;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.inmobia.util.StopWatch;
import com.pixelandtag.dating.entities.PersonDatingProfile;

public class Test {

	public static void main(String[] args) throws IOException {
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		System.out.println(UUID.randomUUID().toString());
		stopwatch.stop();
		System.out.println("Time: "+stopwatch.elapsedMillis());
	}
	
	
	
	
	public static String replaceAllIllegalCharacters(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.trim();
		
		return text;
		
	}

}
