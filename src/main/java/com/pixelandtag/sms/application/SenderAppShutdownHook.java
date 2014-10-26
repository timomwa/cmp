package com.pixelandtag.sms.application;

import com.pixelandtag.sms.producerthreads.MTProducer;

public class SenderAppShutdownHook {
	
	public static void stopApp(){
		System.out.println(MTProducer.class.getName()+": >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Shutting down...");
		MTProducer.stopApp();
		
	}
	
	

}
