package com.pixelandtag.cmp.ejb.api.sms;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.ejb.sequences.TimeStampSequenceEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.subscription.dto.MediumType;

@Stateless
@Remote
public class MTCreatorEJBImpl implements MTCreatorEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private TimezoneConverterI timeconverterEJB;
	
	@EJB
	private OpcoSMSServiceEJBI opcoSMSServiceEJB;
	
	@EJB
	private OpcoSenderProfileEJBI opcosenderprofileEJB;
	
	@EJB
	private OperatorCountryRulesEJBI opcorulesEJB;
	
	@EJB
	private TimeStampSequenceEJBI timeStampEJB;
	
	@EJB
	private QueueProcessorEJBI queueProcessorEJB;
	
	@Override
	public OutgoingSMS sendMT(String message, Long serviceid, String msisdn,
			OperatorCountry opco, int priority) throws Exception{
		
		try{
			
			OpcoSMSService opcosmsservice = opcoSMSServiceEJB.getOpcoSMSService(serviceid, opco);
			OpcoSenderReceiverProfile opcosenderprofile = opcosenderprofileEJB.getActiveProfileForOpco(opco.getId());
			Date ealiestSendTime = opcorulesEJB.findEarliestSendtime(opcosenderprofile);
			
			OutgoingSMS outgoingsms = new OutgoingSMS();
			outgoingsms.setSms(message);
			outgoingsms.setPrice(BigDecimal.ZERO);//set price to subscription price
			outgoingsms.setPriority(priority);
			outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
			outgoingsms.setCharged(Boolean.FALSE);
			outgoingsms.setEvent_type(EventType.SUBSCRIPTION_PURCHASE.getName());
			outgoingsms.setServiceid(serviceid);
			outgoingsms.setMoprocessor(opcosmsservice.getMoprocessor());
			outgoingsms.setMediumType(MediumType.sms);
			outgoingsms.setPrice_point_keyword(opcosmsservice.getSmsservice().getPrice_point_keyword());
			outgoingsms.setTtl(10L); 
			outgoingsms.setShortcode(opcosmsservice.getMoprocessor().getShortcode());
			outgoingsms.setIn_outgoing_queue(Boolean.FALSE);
			outgoingsms.setIsSubscription(Boolean.TRUE);
			long cmp_tx_id = -1;
			Thread.sleep(1);
			cmp_tx_id = System.nanoTime();
			outgoingsms.setCmp_tx_id(String.valueOf( cmp_tx_id  ));
			outgoingsms.setMsisdn(msisdn);
			outgoingsms.setOpcosenderprofile(opcosenderprofile);
			outgoingsms.setTimestamp(ealiestSendTime);
		
			return queueProcessorEJB.saveOrUpdate(outgoingsms);
		
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
			throw exp;
		}
		
	}

}
