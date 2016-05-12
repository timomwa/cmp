package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Date;
import java.util.TimeZone;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.ChatBundleDAOI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.ChatBundle;
import com.pixelandtag.dating.entities.Person;

@Stateless
@Remote
public class ChatCounterEJBImpl implements ChatCounterEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private ChatBundleDAOI chatbundleDAO;
	
	@EJB
	private OpcoSMSServiceEJBI opcoSMSServiceDAO;
	
	@EJB
	private TimezoneConverterI timeZoneConverterEJB;
	
	@EJB
	private OperatorCountryRulesEJBI opcorulesEJB;
	
	
	@Override
	public void updateBundles(Person person, Long value){
		try{
			ChatBundle chatbundle = chatbundleDAO.findBy("msisdn", person.getMsisdn());
			if(chatbundle==null)
				chatbundle = createChatBundle( timeZoneConverterEJB.convertFromOneTimeZoneToAnother(new Date(), TimeZone.getDefault().getID(), person.getOpco().getCountry().getTimeZone()) , person.getMsisdn(), 5L);
			Long availablesms = chatbundle.getSms();
			if(availablesms.compareTo(0L)>0){
				chatbundle.setSms((availablesms + value));
				chatbundleDAO.save(chatbundle);
			}
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
	}
	
	
	@Override
	public boolean isoffBundle(Person person){
		
		boolean isoffbundle = false;
		try{
			ChatBundle chatbundle = chatbundleDAO.findBy("msisdn", person.getMsisdn());
		
		if(chatbundle!=null){
			isoffbundle = chatbundle.getSms().compareTo(0L)<=0;
			if(!isoffbundle){
				Date expiryInServer = timeZoneConverterEJB.convertFromOneTimeZoneToAnother(chatbundle.getExpiryDate(), person.getOpco().getCountry().getTimeZone(), TimeZone.getDefault().getID());
				isoffbundle = timeZoneConverterEJB.isDateInThePast( expiryInServer );//bundle has expired
			}
		}else{
			logger.info("**********No previous data. we're creating new bundle!");
			createChatBundle(person.getMsisdn(), person.getOpco());
		}
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		return isoffbundle;
	}


	@Override
	public void createChatBundle(String msisdn, OperatorCountry opco) throws Exception{
		ChatBundle chatBundle = new ChatBundle();
		Date expiryDate = timeZoneConverterEJB.convertDateToDestinationTimezone(new Date(), opco.getCountry().getTimeZone());
		chatBundle.setExpiryDate(expiryDate);
		chatBundle.setMsisdn(msisdn);
		chatBundle.setSms(20L);
		chatbundleDAO.save(chatBundle);
	}
	
	@Override
	public ChatBundle createChatBundle(Date expiryDate, String msisdn,Long bundlesize ) throws Exception{
		ChatBundle chatBundle = new ChatBundle();
		chatBundle.setExpiryDate(expiryDate);
		chatBundle.setMsisdn( msisdn );
		chatBundle.setSms( bundlesize );
		return chatbundleDAO.save(chatBundle);
	}
	
	
	
	@Override
	public ChatBundle createChatBundle(Subscription subscription) throws Exception{
		Long smsserviceid = subscription.getSms_service_id_fk();
		OpcoSMSService opcosmsservice = opcoSMSServiceDAO.getOpcoSMSService(smsserviceid, subscription.getOpco());
		ChatBundle chatbundle = chatbundleDAO.findBy("msisdn", subscription.getMsisdn());
		if(chatbundle==null){
			return createChatBundle(subscription.getExpiryDate(),subscription.getMsisdn(),opcosmsservice.getBundlesize());
		}else{
			Date expiryInServer = timeZoneConverterEJB.convertFromOneTimeZoneToAnother(chatbundle.getExpiryDate(), subscription.getOpco().getCountry().getTimeZone(), TimeZone.getDefault().getID());
			boolean bundleexpired = timeZoneConverterEJB.isDateInThePast( expiryInServer );//bundle has expired
			if(bundleexpired)
				chatbundle.setSms(opcosmsservice.getBundlesize());
			else
				chatbundle.setSms(chatbundle.getSms() + opcosmsservice.getBundlesize());
			chatbundle.setExpiryDate(subscription.getExpiryDate());
			return chatbundleDAO.save(chatbundle);
		}
	}
	
	

	@Override
	public ChatBundle saveOrUpdate(ChatBundle chatBundle) throws Exception{
		return chatbundleDAO.save(chatBundle);
	}
}
