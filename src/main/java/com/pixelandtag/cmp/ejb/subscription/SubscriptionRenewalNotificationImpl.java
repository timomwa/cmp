package com.pixelandtag.cmp.ejb.subscription;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OperatorCountryRulesEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.sequences.TimeStampSequenceEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.subscription.dto.MediumType;

@Remote
@Stateless
public class SubscriptionRenewalNotificationImpl implements
		SubscriptionRenewalNotificationI {
	
	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;

	@EJB
	private TimezoneConverterI timezoneEJB;
	
	@EJB
	private MessageEJBI messageEJB;
	
	@EJB
	private OpcoSenderProfileEJBI opcosenderprofEJB;
	
	@EJB
	private QueueProcessorEJBI queueprocbean;
	
	@EJB
	private OperatorCountryRulesEJBI opcoRulesEJBI;
	
	@EJB
	private TimeStampSequenceEJBI timeStampEJB;
	
	@Override
	public boolean sendSubscriptionRenewalMessage(OperatorCountry operatorCountry,
			SMSService service, String msisdn, Subscription sub){
		
	
		
		boolean success = false;
		
		try{
			
			Long languageid =0L;//Todo personalize language
			
			DatingMessages message_key = sub.getRenewal_count().compareTo(1L)==0 ? DatingMessages.FIRST_SUBSCRIPTION_WELCOME  : DatingMessages.SUBSCRIPTION_RENEWED;
			
			Message messag = messageEJB.getMessage(message_key.toString(), languageid, operatorCountry.getId()); 
			
			if(messag==null)
				throw new Exception("No message for key "+message_key.toString());
			
			String msg = messag.getMessage();
			msg = msg.replaceAll(BaseEntityI.EXPIRY_DATE_TAG, timezoneEJB.convertToPrettyFormat( sub.getExpiryDate() ));
			msg = msg.replaceAll(BaseEntityI.SERVICE_NAME_TAG, service.getService_name());
			msg = msg.replaceAll(BaseEntityI.PRICE_TAG, service.getPrice().toString());
			msg = msg.replaceAll(BaseEntityI.FREQUENCY, service.getSubscription_length_time_unit().toString().toLowerCase());
			OpcoSenderReceiverProfile opcosenderprofile = opcosenderprofEJB.getActiveProfileForOpco(operatorCountry.getId());
			
			OutgoingSMS outgoingsms = new OutgoingSMS();
			outgoingsms.setSms(msg);
			outgoingsms.setPrice(BigDecimal.ZERO);//set price to subscription price
			outgoingsms.setPriority(0);
			outgoingsms.setOpcosenderprofile(opcosenderprofile);
			outgoingsms.setBilling_status(BillingStatus.NO_BILLING_REQUIRED);
			outgoingsms.setCharged(Boolean.FALSE);
			outgoingsms.setEvent_type(EventType.SUBSCRIPTION_PURCHASE.getName());
			outgoingsms.setServiceid(service.getId());
			outgoingsms.setMoprocessor(service.getMoprocessor());
			outgoingsms.setMediumType(MediumType.sms);
			outgoingsms.setPrice_point_keyword(service.getPrice_point_keyword());
			outgoingsms.setTtl(10L); 
			outgoingsms.setShortcode(service.getMoprocessor().getShortcode());
			outgoingsms.setIn_outgoing_queue(Boolean.FALSE);
			outgoingsms.setIsSubscription(Boolean.TRUE);
			outgoingsms.setCmp_tx_id(String.valueOf(timeStampEJB.getNextTimeStampNano()));
			outgoingsms.setMsisdn(msisdn);
			Date earliestsendtimeslot = opcoRulesEJBI.findEarliestSendtime(opcosenderprofile); 
			outgoingsms.setTimestamp(earliestsendtimeslot);
			
			outgoingsms = queueprocbean.saveOrUpdate(outgoingsms);
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return success;
	}

}
