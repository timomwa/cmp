package com.inmobia.celcom.api;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.inmobia.celcom.api.GenericServiceProcessor;


public class MOProcessorFactory {

	private static final Logger logger = Logger
			.getLogger(MOProcessorFactory.class);
	


	

	@SuppressWarnings("unchecked")
	public static <T> T getProcessorClass(String fqcn, Class<T> klass) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<T> theClass;
		T classinstance = null;
		
			theClass = (Class<T>) Class.forName(fqcn);
			classinstance = (T) theClass.newInstance();			
		
		return classinstance;
	}
	
	
	
	public static void main(String[] args) {
		
		
		BasicConfigurator.configure();
		
		
		GenericServiceProcessor c  = null;
		
		try {
			c = MOProcessorFactory.getProcessorClass("com.inmobia.celcom.serviceprocessors.AxiataTriviaProcessor", GenericServiceProcessor.class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(c.toString());
		
		
	}
}

