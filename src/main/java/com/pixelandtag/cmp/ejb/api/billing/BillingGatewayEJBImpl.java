package com.pixelandtag.cmp.ejb.api.billing;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.billing.Biller;
import com.pixelandtag.billing.BillerFactory;
import com.pixelandtag.billing.BillerProfile;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.BillingConfigSet;
import com.pixelandtag.billing.OpcoBillingProfile;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.ejb.api.sms.SMSGatewayException;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.smssenders.SenderFactory;
import com.pixelandtag.smssenders.SenderResp;

@Stateless
@Remote
public class BillingGatewayEJBImpl implements BillingGatewayEJBI {
	
	//private static Map<Long, Biller> biller_cache = new HashMap<Long, Biller>();
	
	@EJB
	private BillerConfigsI billerConfigEJB;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Override
	public SenderResp bill(Billable billable) throws BillingGatewayException{
		
		
		if(billable==null)
			throw new BillingGatewayException("Billable object passed is null!");
		
		OperatorCountry opco = billable.getOpco();
	
		if(opco==null)
			throw new BillingGatewayException("No opco linked with this billable!");
	
		Biller biller = null;
		
		if(biller==null){
			
			OpcoBillingProfile billerprofile = billerConfigEJB.getActiveBillerProfile(opco);
			
			if(billerprofile==null)
				throw new BillingGatewayException("No opco billing profile for opco with id "+opco.getId()
						+". Please insert a record in the table opco_biller_profile");
			
			Map<String,BillerProfileConfig> opcoconfigs = billerConfigEJB.getAllConfigs(billerprofile);
			Map<String,BillerProfileTemplate> opcotemplates = billerConfigEJB.getAllTemplates(billerprofile,TemplateType.PAYLOAD);
			
			BillingConfigSet billerconfigs = new BillingConfigSet();
			billerconfigs.setOpcoconfigs(opcoconfigs);
			billerconfigs.setOpcotemplates(opcotemplates);
			try {
				biller = BillerFactory.getInstance(billerconfigs);
				biller.validateMandatory();//Validates mandatory configs.
				//biller_cache.put(opco.getId(), biller);
			}catch (Exception exp) {
				logger.error(exp.getMessage(),exp);
				throw new BillingGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage());
			}
			
		}
		
		try {
			SenderResp resp = biller.charge(billable);
			return resp;
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
			throw new BillingGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage());
		}
	}

}
