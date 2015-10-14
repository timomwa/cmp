package com.pixelandtag.mo.sms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import antlr.NameSpace;

public class TestReadXMl {

	public static void main(String[] args) throws Exception {
		
		System.out.println("text/xml; charset=utf-8".contains("text/xml"));
		
		String xmlstr = readFile("/home/systech/eclipseWorkspace/cmp/mo_orange.xml");
		Document doc = new SAXBuilder().build(new StringReader(xmlstr));
		Element rootelement = doc.getRootElement();
		Namespace namespace  = rootelement.getNamespace();
		Element body =  rootelement.getChild("Body",namespace);
		Namespace bodyNamespace = Namespace.getNamespace("sms7", "http://www.csapi.org/schema/parlayx/sms/notification/v3_1/local");
		Element notifySmsReception = body.getChild("notifySmsReception",bodyNamespace);
		Element message  = notifySmsReception.getChild("message",bodyNamespace);
		
		String sms = message.getChildText("message");
		String msisdn = message.getChildText("senderAddress");
		String shortcode = message.getChildText("smsServiceActivationNumber");
		String timeStamp = message.getChildText("dateTime");
		
		
		System.out.println("sms = "+sms);
		System.out.println("msisdn = "+msisdn);
		System.out.println("shortcode = "+shortcode);
		System.out.println("timeStamp = "+timeStamp);
	}
	
	
	public static String readFile(String filename)
	{
	    String content = null;
	    File file = new File(filename); //for ex foo.txt
	    FileReader reader = null;
	    try {
	        reader = new FileReader(file);
	        char[] chars = new char[(int) file.length()];
	        reader.read(chars);
	        content = new String(chars);
	        reader.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if(reader !=null){try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
	    }
	    return content;
	}
}
