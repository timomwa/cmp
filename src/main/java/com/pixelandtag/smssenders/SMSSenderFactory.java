package com.pixelandtag.smssenders;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoConfigs;

public class SMSSenderFactory {
	
	@SuppressWarnings("unchecked")
	public static Sender getSenderInstance(Map<String,OpcoConfigs> configurations) throws Exception {
		OpcoConfigs senderimplclass = configurations.get("senderimpl");
		if(senderimplclass==null)
			throw new SMSGatewayException("\"senderimpl\" FQN that should implement com.pixelandtag.smssenders.Sender isn't not found");
		Class<Sender> theClass;
		Sender classinstance = null;
		theClass = (Class<Sender>) Class.forName(senderimplclass.getValue());
		Constructor<?> cons = theClass.getConstructor(Map.class);
		classinstance = (Sender) cons.newInstance(configurations);		
		return classinstance;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String template = "name: ${param_name}";
		
		System.out.println(template.replaceAll("\\$\\{param_name\\}", "Timothy Mwangi"));
		
		/*Map<String,OpcoConfigs> configurations = new HashMap<String,OpcoConfigs>();
		OpcoConfigs opcoconfig = new OpcoConfigs();
		opcoconfig.setName("senderimpl");
		opcoconfig.setValue("com.pixelandtag.smssenders.PlainHttpSender");
		configurations.put("senderimpl", opcoconfig);
		configurations.put("protocol", opcoconfig);
		Sender sender = getSenderInstance(configurations);
		System.out.println(sender.sendSMS(null));*/
	}
	

}
