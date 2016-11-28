package com.pixelandtag.api.bulksms;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

//@ApplicationPath("/rest")
public class Configuration{// extends Application {

	public Configuration() {
	}

	//@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(BulkSMSMTReceiverI.class);
		classes.add(BulkSMSMTReceiverImpl.class);
		return classes;
	}

}
