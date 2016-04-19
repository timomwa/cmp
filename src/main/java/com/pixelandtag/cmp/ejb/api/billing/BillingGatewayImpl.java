package com.pixelandtag.cmp.ejb.api.billing;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

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
public class BillingGatewayImpl implements BillingGatewayI {
	
	@EJB
	private BillerConfigsI billerConfigEJB;
	
	private Logger logger = Logger.getLogger(getClass());
	
	public boolean bill(Billable billable) throws BillingGatewayException{
		
		if(billable==null)
			throw new BillingGatewayException("Billable object passed is null!");
		
		OperatorCountry opco = billable.getOpco();
		
		if(opco==null)
			throw new BillingGatewayException("No opco linked with this billable!");
		
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
			Biller biller = BillerFactory.getInstance(billerconfigs);
			biller.validateMandatory();//Validates mandatory configs.
			SenderResp resp = biller.charge(billable);
			return resp.getSuccess();
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
			throw new BillingGatewayException("Problem occurred instantiating sender. Error: "+exp.getMessage()+" Do you have entries in biller_profile_configs having profile_id_fk =  "+billerprofile.getProfile().getId());
		}
	}

}
