package com.pixelandtag.cmp.ejb.api.billing;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.pixelandtag.billing.Biller;
import com.pixelandtag.billing.BillerFactory;
import com.pixelandtag.billing.BillerProfileConfig;
import com.pixelandtag.billing.BillingConfigSet;
import com.pixelandtag.billing.OpcoBillingProfile;
import com.pixelandtag.billing.entities.BillerProfileTemplate;
import com.pixelandtag.cmp.dao.core.SuccessfullyBillingRequestsDAOI;
import com.pixelandtag.cmp.entities.BillingType;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.TemplateType;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.Operation;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.smssenders.SenderResp;

@Stateless
@Remote
public class BillingGatewayEJBImpl implements BillingGatewayEJBI {
	
	
	@EJB
	private BillerConfigsI billerConfigEJB;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private SuccessfullyBillingRequestsDAOI successfullbillingDAO;
	
	@Override
	public void createSuccesBillRec(Billable billable){
    	try{
    		
    		SuccessfullyBillingRequests successfulBill = new SuccessfullyBillingRequests();
    		successfulBill.setCp_tx_id(billable.getCp_tx_id());
    		successfulBill.setKeyword(billable.getKeyword());
    		successfulBill.setMsisdn(billable.getMsisdn());
    		successfulBill.setOperation(billable.getOperation());
    		successfulBill.setPrice(billable.getPrice());
    		successfulBill.setPricePointKeyword(billable.getPricePointKeyword());
    		successfulBill.setResp_status_code(billable.getResp_status_code());
    		successfulBill.setShortcode(billable.getShortcode());
    		successfulBill.setSuccess(billable.getSuccess());
    		successfulBill.setTimeStamp( (null!=billable.getTimeStamp() ? billable.getTimeStamp() : new Date()) );
    		successfulBill.setTransactionId(billable.getTransactionId());
    		successfulBill.setTransferin(billable.getTransferIn());
    		successfulBill.setOpco(billable.getOpco());
    		successfulBill = successfullbillingDAO.save(successfulBill);
    		
    	}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
    }
	
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
	
	
	

	public SuccessfullyBillingRequests createSuccessBillingRec(OutgoingSMS outgoingsms, BillingType billingType) {
		
		SuccessfullyBillingRequests success_billing = null;
		
		if(outgoingsms.getPrice()==null || outgoingsms.getPrice().compareTo(BigDecimal.ZERO)<=0 )
			return null;
		
		try{
			
			success_billing = new SuccessfullyBillingRequests();
			success_billing.setCp_tx_id(outgoingsms.getCmp_tx_id());
			success_billing.setKeyword(outgoingsms.getSms()!=null ? outgoingsms.getSms().split("\\s")[0].toUpperCase() : "recharge");
			success_billing.setMsisdn(outgoingsms.getMsisdn());
			success_billing.setOpco(outgoingsms.getOpcosenderprofile().getOpco());
			success_billing.setOperation(outgoingsms.getPrice().compareTo(BigDecimal.ZERO)>0 ? Operation.debit.toString() : Operation.credit.toString());
			success_billing.setPrice(outgoingsms.getPrice());
			success_billing.setResp_status_code("OK");
			success_billing.setShortcode(outgoingsms.getShortcode());
			success_billing.setSuccess(Boolean.TRUE);
			success_billing.setTimeStamp(new Date());
			success_billing.setTransactionId(outgoingsms.getCmp_tx_id());
			success_billing.setTransferin(Boolean.FALSE);
			success_billing.setBillingType(billingType);
			success_billing = successfullbillingDAO.save(success_billing);
		
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return success_billing;
		
	}
	
	
	public SuccessfullyBillingRequests createSuccessBillingRec(IncomingSMS incomingSms, BillingType billingType){

		SuccessfullyBillingRequests success_billing = null;
		
		if(incomingSms.getPrice()==null || incomingSms.getPrice().compareTo(BigDecimal.ZERO)<=0 )
			return null;
		
		try{
			
			success_billing = new SuccessfullyBillingRequests();
			success_billing.setCp_tx_id(incomingSms.getCmp_tx_id());
			success_billing.setKeyword(incomingSms.getSms()!=null ? incomingSms.getSms().split("\\s")[0].toUpperCase() : "recharge");
			success_billing.setMsisdn(incomingSms.getMsisdn());
			success_billing.setOpco(incomingSms.getOpco());
			success_billing.setOperation(incomingSms.getPrice().compareTo(BigDecimal.ZERO)>0 ? Operation.debit.toString() : Operation.credit.toString());
			success_billing.setPrice(incomingSms.getPrice());
			success_billing.setResp_status_code("OK");
			success_billing.setShortcode(incomingSms.getShortcode());
			success_billing.setSuccess(Boolean.TRUE);
			success_billing.setTimeStamp(new Date());
			success_billing.setTransactionId(incomingSms.getCmp_tx_id());
			success_billing.setTransferin(Boolean.FALSE);
			success_billing.setBillingType(billingType);
			success_billing = successfullbillingDAO.save(success_billing);
		
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return success_billing;
	}

}
