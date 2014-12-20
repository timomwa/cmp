package com.pixelandtag.serviceprocessors.sms;

import java.util.LinkedList;
import java.util.Queue;

import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;

/**
 * When a service is configured, then it needs a processor.
 * 
 * @author Timothy Mwangi
 *
 */
public class ServiceProcessorLoader implements Runnable {

	public static Queue<ServiceProcessorDTO> services;
	private CelcomHTTPAPI celcomAPI;
	
	
	
	public ServiceProcessorLoader(String name, String constr) throws Exception{
		
		celcomAPI = new CelcomImpl(constr,"THRD_"+name);
		
		services = new LinkedList<ServiceProcessorDTO>();
		
	}
	
	
	
	
	
	
	public void run() {
		
		services = celcomAPI.getServiceProcessors();//.getServiceProcessors();
		
		
	}

}