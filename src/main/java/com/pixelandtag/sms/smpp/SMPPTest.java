package com.pixelandtag.sms.smpp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.BasicConfigurator;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.sms.smpp.workers.PDUWorker;
import com.pixelandtag.sms.smpp.workers.Transceiver;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.util.Pair;

public class SMPPTest {
	
	public static BlockingQueue<Pair> queue = new LinkedBlockingQueue<Pair>(10000);
	
	public static void main(String[] args) throws InterruptedException {
		BasicConfigurator.configure();
		
		Transceiver tranceiver = null;
		
		PDUWorker pduWorker = null;
		
		try{
			
			Map<String,ProfileConfigs> opcoconfigs = new HashMap<String,ProfileConfigs>();
			opcoconfigs.put(Sender.SMPP_IP, ProfileConfigs.createBasic(Sender.SMPP_IP, "104.131.90.133") );
			opcoconfigs.put(Sender.SMPP_PORT, ProfileConfigs.createBasic(Sender.SMPP_PORT, "5019") );
			opcoconfigs.put(Sender.SMPP_TYPE, ProfileConfigs.createBasic(Sender.SMPP_TYPE, "trx") );
			opcoconfigs.put(Sender.SMPP_USERNAME, ProfileConfigs.createBasic(Sender.SMPP_USERNAME, "KYSDP") );
			opcoconfigs.put(Sender.SMPP_PASSWORD, ProfileConfigs.createBasic(Sender.SMPP_PASSWORD, "KYSDP") );
			opcoconfigs.put(Sender.SMPP_TON, ProfileConfigs.createBasic(Sender.SMPP_TON, "0") );
			opcoconfigs.put(Sender.SMPP_NPI, ProfileConfigs.createBasic(Sender.SMPP_NPI, "0") );
			opcoconfigs.put(Sender.SMPP_DESTON, ProfileConfigs.createBasic(Sender.SMPP_DESTON, "0") );
			opcoconfigs.put(Sender.SMPP_DESNPI, ProfileConfigs.createBasic(Sender.SMPP_DESNPI, "0") );
			opcoconfigs.put(Sender.SMPP_SHORTCODE, ProfileConfigs.createBasic(Sender.SMPP_SHORTCODE, "32329") );
			opcoconfigs.put(Sender.SMPP_VERSION, ProfileConfigs.createBasic(Sender.SMPP_VERSION, "52") );
			opcoconfigs.put(Sender.SMPP_ID, ProfileConfigs.createBasic(Sender.SMPP_ID, "1") );
			opcoconfigs.put(Sender.ALT_SMPP_ID, ProfileConfigs.createBasic(Sender.ALT_SMPP_ID, "1") );
			
			SenderConfiguration configs = new SenderConfiguration();
			configs.setOpcoconfigs(opcoconfigs);
			
			
			OutgoingSMS outgoingsms = new OutgoingSMS();
			outgoingsms.setMsisdn("254202407004");
			outgoingsms.setMsisdn("254773442134");
			outgoingsms.setId(Long.valueOf(6666660+23));
			tranceiver = new Transceiver(configs,queue);
			
			outgoingsms.setShortcode("32329");
			outgoingsms.setSms(">>> SMS # "+23);
			boolean success =  tranceiver.send(outgoingsms);

			/*pduWorker = new PDUWorker(queue);
			pduWorker.start();*/
			int c = 0;
			while(true){
				c++;
				
				Thread.sleep(5000);
				
			}
			
		}catch(Exception exp){
			exp.printStackTrace();
		}finally{
			try{
				if(tranceiver!=null)
					tranceiver.disconnect();
				if(pduWorker != null)
					pduWorker.stop();
			}catch(Exception exp){
				exp.printStackTrace();
			}
		}
	}

}