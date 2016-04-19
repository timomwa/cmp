package com.pixelandtag.billing;

import java.lang.reflect.Constructor;
import java.util.Map;

public class BillerFactory {

	@SuppressWarnings("unchecked")
	public static Biller getInstance(BillingConfigSet billerconfigs) throws Exception {
		Map<String,BillerProfileConfig> configurations = billerconfigs.getOpcoconfigs();
		BillerProfileConfig billerimpl = configurations.get("billerimpl");
		if(billerimpl==null)
			throw new BillingGatewayException("\"billerimpl\" FQN that should implement com.pixelandtag.billing.Biller isn't found");
		Class<GenericBiller> theClass;
		Biller classinstance = null;
		theClass = (Class<GenericBiller>) Class.forName(billerimpl.getValue());
		Constructor<?> cons = theClass.getConstructor(BillingConfigSet.class);
		classinstance = (Biller) cons.newInstance(billerconfigs);		
		return classinstance;
	}
	
}
