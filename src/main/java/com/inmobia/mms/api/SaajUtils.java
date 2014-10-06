package com.inmobia.mms.api;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SaajUtils {

	/**
	 * extract the MIME header information from within the HTTP Request
	 * 
	 * @param req
	 *            the http request
	 * @return MimeHeaders as defined within the SAAJ API
	 */
	public static MimeHeaders getHeaders(HttpServletRequest req) {

		Enumeration enumer = req.getHeaderNames();

		MimeHeaders headers = new MimeHeaders();

		while (enumer.hasMoreElements()) {
			String headerName = (String) enumer.nextElement();
			String headerValue = req.getHeader(headerName);
			
			
			System.out.println(headerName +"++++++>>>>>>" + headerValue);
			
			StringTokenizer values = new StringTokenizer(headerValue, ",");
			while (values.hasMoreTokens()) {
				
				headers.addHeader(headerName, values.nextToken().trim());
			}
		}
		return headers;
	}

	/**
	 * stuff the MIME headers into the HTTP response
	 * 
	 * @param headers
	 *            the SAAJ MIME headers
	 * @param res
	 *            the Http servlet response
	 */
	public static void putHeaders(MimeHeaders headers, HttpServletResponse res) {
		for (Iterator it = headers.getAllHeaders(); it.hasNext();) {
			MimeHeader header = (MimeHeader) it.next();
			String[] values = headers.getHeader(header.getName());
			if (values.length == 1) {
				res.setHeader(header.getName(), header.getValue());
			} else {
				StringBuffer concat = new StringBuffer();
				for (int i = 0; i < values.length; i++) {
					if (i != 0) {
						concat.append(',');
					}
					concat.append(values[i]);
				}
				res.setHeader(header.getName(), concat.toString());
			}
		}
	}

	public static void attachBytes(SOAPMessage soapMessage, byte[] theBytes,
			String contentType) throws SOAPException {
		AttachmentPart attachment = soapMessage.createAttachmentPart();
		attachment.setContent(new ByteArrayInputStream(theBytes), contentType);
		soapMessage.addAttachmentPart(attachment);
	}

	public static void attachUrlContents(SOAPMessage soapMessage,
			String urlLocation, String contentType) throws SOAPException,
			MalformedURLException {
		URL url = new URL(urlLocation);
		AttachmentPart attachment = soapMessage
				.createAttachmentPart(new DataHandler(url));
		attachment.setContentType(contentType);
		soapMessage.addAttachmentPart(attachment);
	}

	public static String getAttachmentReport(SOAPMessage soapMessage)
			throws SOAPException {
		int numOfAttachments = soapMessage.countAttachments();
		Iterator attachments = soapMessage.getAttachments();

		StringBuffer buf = new StringBuffer("Number of attachments: ");
		buf.append(numOfAttachments);

		while (attachments.hasNext()) {
			buf.append("\n--------------------------------------------\n");
			AttachmentPart attachment = (AttachmentPart) attachments.next();
			buf.append("\nContent Location: " + attachment.getContentLocation());
			buf.append("\nContent Id: " + attachment.getContentId());
			buf.append("\nContent Size: " + attachment.getSize());
			buf.append("\nContent Type: " + attachment.getContentType());
		}

		return buf.toString();
	}
}
