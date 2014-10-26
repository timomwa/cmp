package com.pixelandtag.mms.soap;

import java.io.*;  
import java.util.Iterator;  
import javax.xml.soap.*;  
public class AttachmentTest {  
    public static void main(String[] args) {  
        FileReader fr = null;  
        BufferedReader br = null;  
        String line = "";  
        try {  
            // Instantiation of MessageFactory and SOAPFactory  
            MessageFactory messageFactory = MessageFactory.newInstance();  
            SOAPMessage message = messageFactory.createMessage();  
            SOAPFactory soapFactory = SOAPFactory.newInstance();  
            SOAPBody body  = message.getSOAPBody();  
            SOAPBodyElement SourceCaptureMsgEl = body.addBodyElement  
                                        (soapFactory.createName("CarTypes"));  
            SourceCaptureMsgEl.addNamespaceDeclaration("xsi",  
                    "http://www.w3.org/2001/XMLSchema-instance");  
            AddMessageBody(SourceCaptureMsgEl);  
            message.saveChanges();  
            System.out.println("*********New Body") ;  
            message.writeTo(System.out);  
            System.out.println("\n*********End New Body") ;  
              
            // Display the MimeHeaders  
            displayMimeHeaders("Before attachments ", message);  
            // Create attachment part for text  
            AttachmentPart attachment1 = message.createAttachmentPart();  
            fr = new FileReader(new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg"));  
            br = new BufferedReader(fr);  
            String stringContent = "";  
            line = br.readLine();  
            while (line != null) {  
                stringContent = stringContent.concat(line);  
                stringContent = stringContent.concat("\n");  
                line = br.readLine();  
            }  
            attachment1.setContent(stringContent, "text/plain");  
            attachment1.setContentId("attached_text");  
            message.addAttachmentPart(attachment1);  
            message.saveChanges();  
            // Display the Mime Headers  
            displayMimeHeaders("After string attachments - MIMES ", message);  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }  
      
    // Add some elements for the SOAP Body  
    public static void AddMessageBody(SOAPElement el) {  
        try {  
            SOAPElement HeaderEl = el.addChildElement("Audi");  
            SOAPElement MessagesEl = HeaderEl.addChildElement("type");  
            MessagesEl.addTextNode("TT");  
            SOAPElement VersionEl = HeaderEl.addChildElement("version");  
            VersionEl.addTextNode("V6");  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
    }  
      
    // Display Mime Headers  
    public static void displayMimeHeaders(String type, SOAPMessage msg)  
    {  
        System.out.println();  
        System.out.println( "**********" + type + "***********");  
        MimeHeaders headers = msg.getMimeHeaders();  
        Iterator it =  headers.getAllHeaders();  
        while (it.hasNext())  
        {  
            MimeHeader header = (MimeHeader)it.next();  
            String[] values = headers.getHeader(header.getName());  
        if (values.length == 1)  
        {  
            System.out.println(header.getName() + "  ---> " + header.getValue());  
        }  
        else  
        {  
            StringBuffer concat = new StringBuffer();  
            int count = 0;  
            while (count < values.length)  
            {  
                if (count != 0)  
                {  
                    concat.append(',');  
                }  
                    concat.append(values[count++]);  
            }  
            System.out.println(header.getName() + "  ---> " + concat.toString());  
        }  
        }  
    }  
  
}  
