package com.pixelandtag.cmp.ejb.api.sms;

import java.util.Date;

import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.ChatBundle;
import com.pixelandtag.dating.entities.Person;

public interface ChatCounterEJBI {

	public void updateBundles(Person person, Long value);

	public boolean isoffBundle(Person person);
	
	public boolean isoffBundle(String mSISDN, OperatorCountry opco) throws Exception;
	
	public ChatBundle saveOrUpdate(ChatBundle chatBundle) throws Exception;
	
	public ChatBundle createChatBundle(Date expiryDate, String msisdn,Long bundlesize ) throws Exception;
	
	public ChatBundle createChatBundle(Subscription subscription) throws Exception;
	
	public void createChatBundle(String msisdn, OperatorCountry opco) throws Exception;

	public void removeAllBundles(String msisdn);

}
