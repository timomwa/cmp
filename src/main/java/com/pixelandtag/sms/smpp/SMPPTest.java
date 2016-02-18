package com.pixelandtag.sms.smpp;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.smssenders.Sender;

public class SMPPTest {
	
	public static void main(String[] args) throws InterruptedException {
		BasicConfigurator.configure();
		
		Transceiver tranceiver = null;
		
		try{
			
			Map<String,ProfileConfigs> opcoconfigs = new HashMap<String,ProfileConfigs>();
			opcoconfigs.put(Sender.SMPP_IP, ProfileConfigs.createBasic(Sender.SMPP_IP, "104.131.90.133") );
			opcoconfigs.put(Sender.SMPP_PORT, ProfileConfigs.createBasic(Sender.SMPP_PORT, "5019") );
			opcoconfigs.put(Sender.SMPP_TYPE, ProfileConfigs.createBasic(Sender.SMPP_TYPE, "smpp") );
			opcoconfigs.put(Sender.SMPP_USERNAME, ProfileConfigs.createBasic(Sender.SMPP_USERNAME, "KYSDP") );
			opcoconfigs.put(Sender.SMPP_PASSWORD, ProfileConfigs.createBasic(Sender.SMPP_PASSWORD, "KYSDP") );
			opcoconfigs.put(Sender.SMPP_TON, ProfileConfigs.createBasic(Sender.SMPP_TON, "0") );
			opcoconfigs.put(Sender.SMPP_NPI, ProfileConfigs.createBasic(Sender.SMPP_NPI, "0") );
			opcoconfigs.put(Sender.SMPP_DESTON, ProfileConfigs.createBasic(Sender.SMPP_DESTON, "0") );
			opcoconfigs.put(Sender.SMPP_DESNPI, ProfileConfigs.createBasic(Sender.SMPP_DESNPI, "0") );
			opcoconfigs.put(Sender.SMPP_SHORTCODE, ProfileConfigs.createBasic(Sender.SMPP_SHORTCODE, "32329") );
			opcoconfigs.put(Sender.SMPP_VERSION, ProfileConfigs.createBasic(Sender.SMPP_VERSION, "52") );
			SenderConfiguration configs = new SenderConfiguration();
			configs.setOpcoconfigs(opcoconfigs);
			
			tranceiver = new Transceiver(configs);
			
			int c = 0;
			while(true){
				c++;
				/*OutgoingSMS outgoingsms = new OutgoingSMS();
				outgoingsms.setMsisdn("254202407004");
				outgoingsms.setId(Long.valueOf(6666660+c));
				
				outgoingsms.setShortcode("32329");
				outgoingsms.setSms(">>> SMS # "+c);
				boolean success =  tranceiver.send(outgoingsms);*/
				Thread.sleep(5000);
				
			}
			
		}catch(Exception exp){
			exp.printStackTrace();
		}finally{
			try{
				if(tranceiver!=null)
					tranceiver.disconnect();
			}catch(Exception exp){
				exp.printStackTrace();
			}
		}
	}

}
