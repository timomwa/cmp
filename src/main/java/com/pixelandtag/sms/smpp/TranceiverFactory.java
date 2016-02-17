package com.pixelandtag.sms.smpp;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SchemaViolationException;
import javax.naming.spi.DirStateFactory;

public class TranceiverFactory implements DirStateFactory {

	public TranceiverFactory(){
		
	}
	@Override
	public Object getStateToBind(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result getStateToBind(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment, Attributes inAttrs)
			throws NamingException {
	    // Only interested in Person objects
		if (obj instanceof Transceiver) {

		    Attributes outAttrs;
		    if (inAttrs == null) {
			outAttrs = new BasicAttributes(true);
		    } else {
			outAttrs = (Attributes)inAttrs.clone();
		    }

		    // Set up object class
		    if (outAttrs.get("objectclass") == null) {
			BasicAttribute oc = new BasicAttribute("objectclass", "tranceiver");
			oc.add("top");
			outAttrs.put(oc);
		    }

		    Transceiver per = (Transceiver)obj;
		    outAttrs.put("cn", "tranceiver");

		   
		    return new DirStateFactory.Result(null, outAttrs);
		}
		return null;
	}

}
