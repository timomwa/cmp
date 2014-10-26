package com.inmobia.mms.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class SoapMMSDN {

	private static final String RT = "\r";
	private static final String EMTSR = "";
	private static final String CR = "\n";
	private String TransactionID;
	private String MM7Version;
	private String MMSRelayServerID;
	private String MessageID;
	private String Recipient;
	private String Sender;
	private String Date;
	private String MMStatus;
	private String StatusText;
	private SAXBuilder saxBuilder;

	public String getTransactionID() {
		return TransactionID;
	}

	public void setTransactionID(String transactionID) {
		TransactionID = transactionID;
	}

	public String getMM7Version() {
		return MM7Version;
	}

	public String getMMSRelayServerID() {
		return MMSRelayServerID;
	}

	public String getMessageID() {
		return MessageID;
	}

	public String getRecipient() {
		return Recipient;
	}

	public String getSender() {
		return Sender;
	}

	public String getDate() {
		return Date;
	}

	public String getMMStatus() {
		return MMStatus;
	}

	public String getStatusText() {
		return StatusText;
	}

	public void setMM7Version(String mM7Version) {
		MM7Version = mM7Version;
	}

	public void setMMSRelayServerID(String mMSRelayServerID) {
		MMSRelayServerID = mMSRelayServerID;
	}

	public void setMessageID(String messageID) {
		MessageID = messageID;
	}

	public void setRecipient(String recipient) {
		Recipient = recipient;
	}

	public void setSender(String sender) {
		Sender = sender;
	}

	public void setDate(String date) {
		Date = date;
	}

	public void setMMStatus(String mMStatus) {
		MMStatus = mMStatus;
	}

	public void setStatusText(String statusText) {
		StatusText = statusText;
	}

	/*public static String XMLDN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ " <env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ " <env:Header>"
			+ " <TransactionID"
			+ " xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-0\""
			+ " env:mustUnderstand=\"1\">23316115</TransactionID>"
			+ " </env:Header>"
			+ " <env:Body>"
			+ " <DeliveryReportReq"
			+ " xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-0\">"
			+ " <MM7Version>5.3.0</MM7Version>" + " <MMSRelayServerID />"
			+ " <MessageID>23316115@mmsc1</MessageID>" + " <Recipient>"
			+ " <Number>60193685271</Number>" + " </Recipient>" + " <Sender>"
			+ " <ShortCode>23355/TYPE=PLMN</ShortCode>" + " </Sender>"
			+ " <Date>2012-02-29T19:46:03Z</Date>"
			+ " <MMStatus>Retrieved</MMStatus>"
			+ " <StatusText>Message Retrieved</StatusText>"
			+ " </DeliveryReportReq>" + " </env:Body>" + " </env:Envelope>";*/

	public static void main(String[] args) {

		try {
			System.out.println(new SoapMMSDN("").toString());
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	

	@Override
	public String toString() {
		return "JdomDNParser [TransactionID=" + TransactionID + ", MM7Version="
				+ MM7Version + ", MMSRelayServerID=" + MMSRelayServerID
				+ ", MessageID=" + MessageID + ", Recipient=" + Recipient
				+ ", Sender=" + Sender + ", Date=" + Date + ", MMStatus="
				+ MMStatus + ", StatusText=" + StatusText + ", saxBuilder="
				+ saxBuilder + "]";
	}

	public SoapMMSDN(String txt) throws JDOMException, IOException {

		Document doc = createDocument(txt);

		Element rootElement = doc.getRootElement();

		List<Element> elems = rootElement.getChildren();

		for (Element el : elems) {

			if (el.getName().equalsIgnoreCase("header")) {
				
				if (el.getName().trim().equalsIgnoreCase("header")) {
					setTransactionID(el.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
				}

			} else if (el.getName().trim().equalsIgnoreCase("body")) {

				List<Element> bchdr = el.getChildren();

				for (Element bdch : bchdr) {

					List<Element> z = bdch.getChildren();

					for (Element el2 : z) {
						if (el2.getName().trim().equalsIgnoreCase("StatusText")) {
							setStatusText(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("MMStatus")) {
							setMMStatus(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("Date")) {
							setDate(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("Sender")) {
							setSender(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("Recipient")) {
							setRecipient(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("MessageID")) {
							setMessageID(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("MMSRelayServerID")) {
							setMMSRelayServerID(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}
						if (el2.getName().trim().equalsIgnoreCase("MM7Version")) {
							setMM7Version(el2.getValue().trim().replaceAll(RT,EMTSR).replaceAll(CR,EMTSR));
						}

					}
				}
			}
		}

	}

	/**
	 * 
	 * @param xmlStringD
	 *            - xml string representation
	 * @return org.jdom.Document - the xml document
	 * @throws IOException 
	 * @throws JDOMException 
	 * 
	 */
	public Document createDocument(String xmlStringD) throws JDOMException, IOException {

		if(saxBuilder==null)
			saxBuilder = new SAXBuilder(false);

		return saxBuilder.build(new StringReader(xmlStringD));

		

	}

}
