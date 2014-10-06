package com.inmobia.mms.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import com.inmobia.celcom.api.GenericMessage;

public class MM7DeliveryReport extends GenericMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5421L;


	public MM7DeliveryReport(SOAPMessage response) throws SOAPException, IOException, Exception{
		
		SOAPPart soapPart = response.getSOAPPart();
		SOAPHeader header = response.getSOAPHeader();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody body = envelope.getBody();
		
		if(body.hasFault()){
			SOAPFault fault = body.getFault();
			Detail fd = fault.getDetail();
			
			System.out.println("fault.getTextContent(): "+fault.getTextContent());
			System.out.println(fd.getNodeName() + " : " + fd.getNodeValue());
			
		}else{
			
			String transactionID = header.getTextContent();
			this.setCMP_Txid(header.getTextContent());
			//System.out.println("header.getTextContent(): "+header.getTextContent());
			
			
			
			//bodyElement  = body.
			Iterator<SOAPBodyElement> it = body.getChildElements();
			
			
			
			
			
			while(it.hasNext()){
				SOAPBodyElement be = it.next();
				Name name = be.getElementName();
				
				Iterator<SOAPBodyElement> sbe = be.getChildElements();//.getChildElements();
				
				while(sbe.hasNext()){
					SOAPBodyElement soapBe = sbe.next();
					//System.out.println(soapBe.getNodeValue()+" : "+soapBe.getValue());
					Name disiaName = soapBe.getElementName();
					//System.out.println(disiaName.getLocalName());
					
					
						
						Iterator<SOAPElement> elem = be.getChildElements(disiaName);
						while(elem.hasNext()){
							
							SOAPElement inner = elem.next();
							
							if(!STATUS_NODE_NAME.equalsIgnoreCase(disiaName.getLocalName())){
								
								if(SOAP_MSG_ID_NODE_NAME.equalsIgnoreCase(disiaName.getLocalName())){
									this.setSoapMessageID(inner.getValue());
								}
								///System.out.println(inner.getValue());
							
							}else{
							
								Iterator<SOAPElement> statusElemsIt = inner.getChildElements();
								
								while(statusElemsIt.hasNext()){
									
									SOAPElement statusElm = statusElemsIt.next();
									
									if(STATUS_CODE_NODE_NAME.equalsIgnoreCase(statusElm.getLocalName())){
										this.setStatusCode(statusElm.getTextContent());
										if(statusElm.getTextContent().equals(SUCCESS_STATUS_CODE))
											this.setSuccess(true);
									}else if(STATUS_TEXT_NODE_NAME.equalsIgnoreCase(statusElm.getLocalName())){
										this.setStatusText(statusElm.getTextContent());
									}
									//System.out.println("statusElem: Name: "+statusElm.getLocalName());
									//System.out.println("statusElem: Value: "+statusElm.getTextContent());
									
									
								}
							}
							
						}
						
					
				}
				
			}
			
			
		}
		
	}
	
	private boolean success;
	private String statusCode;
	private String statusText;
	private String SoapMessageID;//from soap.. we might not need at all
	
	
	public String getSoapMessageID() {
		return SoapMessageID;
	}
	public void setSoapMessageID(String soapMessageID) {
		SoapMessageID = soapMessageID;
	}
	public boolean isSuccess() {
		return success;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public String getStatusText() {
		return statusText;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	@Override
	public String toString() {
		return "MM7DeliveryReport [success=" + success + ", statusCode="
				+ statusCode + ", statusText=" + statusText
				+ ", SoapMessageID=" + SoapMessageID + "]";
	}
	
	
	
	
	
	
	
	
	

}
