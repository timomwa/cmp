package com.ibm.TWSS.services.v1_0.ChargingService.Binding2;

import java.rmi.RemoteException;

import ChargingProcess.com.ibm.sdp.services.charging.abstraction.Charging.ChargingProxy;
import ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequest;
import ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestEventType;
import ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingRequestOperation;
import ChargingProcess.com.ibm.sdp.services.charging.vo.ChargingResponse;
import CocLib.com.ibm.sdp.vo.ServiceException;

public class TestWS {
	
	
	public static void main(String[] args)  {
		
		org.apache.commons.discovery.tools.DiscoverSingleton test;
		org.slf4j.spi.LocationAwareLogger rest;
		javax.wsdl.OperationType t;
		
		
		String endpoint = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";
		ChargingProxy cp  = new ChargingProxy(endpoint);
		ChargingRequest chargingRequest = new ChargingRequest();
		chargingRequest.setUserId("254734252504");
		chargingRequest.setContentId("32329_JOBS");
		chargingRequest.setItemName("32329_JOBS");
		chargingRequest.setContentMediaType("SMS");
		chargingRequest.setActualPrice("5");
		chargingRequest.setContentDescription("32329_JOBS");
		chargingRequest.setBasePrice("5");
		chargingRequest.setDiscountApplied("0");
		chargingRequest.setCpId("content360");
		chargingRequest.setSubscriptionTypeCode("32329_JOBS");
		chargingRequest.setSubscriptionName("32329_JOBS");
		chargingRequest.setDeliveryChannel("SMS");
		chargingRequest.setSubscriptionExternalId("32329_JOBS");
		chargingRequest.setCurrency("Kshs");
		chargingRequest.setCopyrightId("content360");
		chargingRequest.setSMSkeyword("JOBS");
		chargingRequest.setSrcCode("32329_JOBS");
		chargingRequest.setContentUrl("http://104.131.29.202:80/cmp");
		chargingRequest.setCircleId("circleID");
		chargingRequest.setContentSize("101011");
		chargingRequest.setCopyrightDescription("copywriteDes");
		chargingRequest.setCpTransactionId("100001");
		chargingRequest.setCustomerClass("customerClass");

		chargingRequest.setCustomerSegment("customerSegment");
		chargingRequest.setEventType(ChargingRequestEventType.value9);
		
		chargingRequest.setItemName("contentName");
		
		chargingRequest.setLineOfBusiness("lineOfBusiness");
		chargingRequest.setLocalTimeStamp("2014-12-01 22:27:00");
		chargingRequest.setNetShare("75");
		chargingRequest.setOperation(ChargingRequestOperation.debit);
		
		chargingRequest.setParentId("com.pixelandtag");
		chargingRequest.setParentType("package");
		chargingRequest.setPaymentMethod("prepaid");
		chargingRequest.setRevenuePercent("1");
		chargingRequest.setServiceId("100");
		chargingRequest.setSubscriptiondays("1");
		chargingRequest.setTransactionId("102354");
		
		
		try {
			ChargingResponse resp  = cp.charge(chargingRequest);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
