package com.pixelandtag.smssenders;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayException;
import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;

public class SMSSenderFactory {
	
	@SuppressWarnings("unchecked")
	public static Sender getSenderInstance(SenderConfiguration senderConfigs) throws Exception {
		Map<String,OpcoConfigs> configurations = senderConfigs.getOpcoconfigs();
		OpcoConfigs senderimplclass = configurations.get("senderimpl");
		if(senderimplclass==null)
			throw new SMSGatewayException("\"senderimpl\" FQN that should implement com.pixelandtag.smssenders.Sender isn't not found");
		Class<GenericSender> theClass;
		Sender classinstance = null;
		theClass = (Class<GenericSender>) Class.forName(senderimplclass.getValue());
		Constructor<?> cons = theClass.getConstructor(SenderConfiguration.class);
		classinstance = (Sender) cons.newInstance(senderConfigs);		
		return classinstance;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Map<String,OpcoConfigs> configurations = new HashMap<String,OpcoConfigs>();
		OpcoConfigs opcoconfig = new OpcoConfigs();
		opcoconfig.setName("senderimpl");
		opcoconfig.setValue("com.pixelandtag.smssenders.PlainHttpSender");
		configurations.put("senderimpl", opcoconfig);
		configurations.put("http_protocol", opcoconfig);
		SenderConfiguration sc = new SenderConfiguration();
		sc.setOpcoconfigs(configurations);
		Sender sender = getSenderInstance(sc);
		System.out.println(sender.sendSMS(null));
	}
	

}
