package com.pixelandtag.cmp.ejb.subscription;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.cmp.dao.subscription.SubscriptionDAOI;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.SubscriptionEvent;
import com.pixelandtag.dating.entities.SubscriptionHistory;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

@Stateless
@Remote
public class SubscriptionEJB implements SubscriptionBeanI {
	

	public Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private SubscriptionDAOI subscriptionDAO;
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;

	@EJB
	private TimezoneConverterI timezoneEJB;
	
	@EJB
	private SubscriptionRenewalNotificationI subrenewalnotificationEJB;
	
	@EJB
	private SMSServiceBundleEJBI smsserviceBundleEJB;
	
	@EJB
	private OpcoSMSServiceEJBI opcosmsserviceejb;
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#updateCredibilityIndex(java.lang.String, java.lang.Long, int)
	 */
	@Override
	public void updateCredibilityIndex(String msisdn, Long service_id, int change, OperatorCountry opco){
		try{
			Subscription subsc = getSubscription(msisdn, service_id);
			if(subsc!=null){
				if(change>=1){
					int prev_index = subsc.getCredibility_index().intValue();
					subsc.setCredibility_index(prev_index>=0 ? (change+prev_index) : 0);//we re-set their index if they previously had a bad record, but now they have credit
				}else{
					subsc.setCredibility_index(subsc.getCredibility_index().intValue()+change);//keep sinking deep into bad credit
				}
				subsc = em.merge(subsc);
			}else{
				subsc = subscribe(msisdn,service_id,MediumType.ussd,AlterationMethod.system_autorenewal, opco);
				if(change>=1){
					int prev_index = subsc.getCredibility_index().intValue();
					subsc.setCredibility_index(prev_index>=0 ? (change+prev_index) : 0);//we re-set their index if they previously had a bad record, but now they have credit
				}else{
					subsc.setCredibility_index(subsc.getCredibility_index().intValue()+change);//keep sinking deep into bad credit
				}
				subsc = em.merge(subsc);
			}
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
	}
	

	@Override
	public Subscription subscribe(String msisdn, Long service_id,MediumType medium, AlterationMethod method,  SubscriptionStatus status, OperatorCountry opco) { 
		
		Subscription subscription = null;
		
		try{
			subscription = new Subscription();
			subscription.setMsisdn(msisdn);
			subscription.setSms_service_id_fk(service_id);
			subscription.setSubActive(Boolean.TRUE);
			subscription.setSmsmenu_levels_id_fk(-1);
			subscription.setRenewal_count(0L);
			subscription.setQueue_status(0L);
			subscription.setRequest_medium(medium);
			subscription.setOpco(opco);
			subscription.setSubscription_status(status);
			
			
			
			subscription = em.merge(subscription);
			
			SubscriptionHistory sh = new SubscriptionHistory();
			sh.setEvent(  (status ==  SubscriptionStatus.confirmed && subscription.getRenewal_count()<=0) ? SubscriptionEvent.subscrition.getCode() : 
				(status ==  SubscriptionStatus.unsubscribed ? SubscriptionEvent.unsubscrition.getCode() : SubscriptionEvent.renewal.getCode() ));
			sh.setMsisdn(msisdn);
			sh.setService_id(service_id);
			sh.setTimeStamp(new Date());
			sh.setAlteration_method(method);
			sh = em.merge(sh);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return subscription;
	}
	
	@Override
	public Subscription subscribe(String msisdn, Long service_id,MediumType medium, AlterationMethod method,  OperatorCountry opco) { 
		Subscription subscription = null;
		try{
			subscription = new Subscription();
			subscription.setMsisdn(msisdn);
			subscription.setSms_service_id_fk(service_id);
			subscription.setSubActive(Boolean.TRUE);
			subscription.setSmsmenu_levels_id_fk(-1);
			subscription.setRenewal_count(0L);
			subscription.setQueue_status(0L);
			subscription.setRequest_medium(medium);
			subscription.setOpco(opco);
			subscription.setSubscription_status(SubscriptionStatus.confirmed);
			
			
			
			subscription = em.merge(subscription);
			
			SubscriptionHistory sh = new SubscriptionHistory();
			sh.setEvent( SubscriptionEvent.subscrition.getCode() );
			sh.setMsisdn(msisdn);
			sh.setService_id(service_id);
			sh.setTimeStamp(new Date());
			sh.setAlteration_method(method);
			sh = em.merge(sh);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return subscription;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#getExpiredSubscriptions(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> getExpiredSubscriptions(Long size) {
		List<Subscription> expired = new ArrayList<Subscription>();
		try{
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			Query qry   = em.createQuery("from Subscription s WHERE s.subscription_status=:status  AND s.expiryDate<=:todaydate AND s.queue_status = 0 ORDER BY s.credibility_index desc");
			qry.setParameter("status", SubscriptionStatus.confirmed);
			qry.setParameter("todaydate", timeInNairobi);
			qry.setFirstResult(0);
			qry.setMaxResults(size.intValue());
			expired = qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		
		return expired;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#renewSubscription(java.lang.String, java.lang.Long)
	 */
	@Override
	public Subscription renewSubscription(OperatorCountry operatorCountry, String msisdn, Long serviceid, AlterationMethod method) throws Exception{
		Subscription sub = null;
		try{
			SMSService service = em.find(SMSService.class, serviceid);
			sub = renewSubscription(operatorCountry,msisdn, service, SubscriptionStatus.confirmed, method);
			updateQueueStatus(0L,sub.getId(),method);
			subrenewalnotificationEJB.sendSubscriptionRenewalMessage(operatorCountry,service,msisdn, sub); 
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return sub;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#updateQueueStatus(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public void updateQueueStatus(Long status, String msisdn, Long sms_service_id, AlterationMethod method, OperatorCountry opco) throws Exception{
		try{
			Subscription sub = getSubscription(msisdn, sms_service_id);
			if(sub==null)
				sub = subscribe(msisdn, sms_service_id,MediumType.ussd,method, opco);
			updateQueueStatus(status,sub.getId(), method);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
	}
	
	
	@Override
	
	public void updateQueueStatus(Long status, Long id, AlterationMethod method) throws Exception{
		try{
			Query qry = em.createQuery("UPDATE Subscription s SET s.queue_status=:queue_status WHERE s.id=:id");
			qry.setParameter("queue_status", status);
			qry.setParameter("id", id);
			qry.executeUpdate();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
	}
	
	
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Subscription renewSubscription(OperatorCountry operatorCountry, String msisdn, SMSService smsService, SubscriptionStatus substatus,  AlterationMethod method) throws Exception{
			Subscription sub = null;
			try{
				Query qry;
				try{
					qry = em.createQuery("from Subscription where msisdn=:msisdn AND sms_service_id_fk=:sms_service_id_fk");
					qry.setParameter("sms_service_id_fk", smsService.getId());
					qry.setParameter("msisdn", msisdn);
					qry.setFirstResult(0);
					qry.setMaxResults(1);
					sub = (Subscription) qry.getSingleResult();
				}catch(javax.persistence.NoResultException exp){
					logger.warn("No subscription wit sms_service_id_fk="+smsService.getId()+" and msisdn = "+msisdn);
				}
				
				operatorCountry = em.merge(operatorCountry);
				Date todaysdate = new Date();
				Date nowInNairobiTz = timezoneEJB.convertFromOneTimeZoneToAnother(todaysdate, "America/New_York", operatorCountry.getCountry().getTimeZone());//"Africa/Nairobi");
				
				if(sub==null){
				
					sub = new Subscription();
					sub.setMsisdn(msisdn);
					sub.setSms_service_id_fk(smsService.getId());
				
				}else{
				
					DateTime expiryDate = new DateTime(sub.getExpiryDate());
					DateTime timeNow = new DateTime(nowInNairobiTz); 
					
					if(expiryDate.compareTo(timeNow)>=0){
						logger.info("\n\t\tNot yet epired. We extend current subscription\n\n");
						nowInNairobiTz = sub.getExpiryDate();
					}
				}
				
				sub.setOpco(operatorCountry);
				
				qry = em.createNativeQuery("select DATE_ADD(:curdate_local, INTERVAL :sub_length "+smsService.getSubscription_length_time_unit()+") ");
				qry.setParameter("curdate_local", nowInNairobiTz);
				qry.setParameter("sub_length", smsService.getSubscription_length());
				Object o = qry.getSingleResult();
				String expiryDate = (String) o;
				
				logger.error(">>>>RENEWING SUBSCRIPTION OPCO_TZ:  NAIROBI_TIME :::"+nowInNairobiTz+"  expiryDate:: "+expiryDate);
				
				try{
					sub.setExpiryDate(timezoneEJB.stringToDate(expiryDate));
					sub.setRenewal_count(sub.getRenewal_count()+1);
					sub.setSubscription_status(substatus);
					sub.setRequest_medium(MediumType.sms);
					sub = em.merge(sub);
					
					SubscriptionHistory sh = new SubscriptionHistory();
					sh.setEvent(  (substatus ==  SubscriptionStatus.confirmed && sub.getRenewal_count()<=0) ? SubscriptionEvent.subscrition.getCode() : 
						(substatus ==  SubscriptionStatus.unsubscribed ? SubscriptionEvent.unsubscrition.getCode() : SubscriptionEvent.renewal.getCode() ));
					sh.setMsisdn(msisdn);
					sh.setService_id(smsService.getId());
					sh.setTimeStamp(new Date());
					sh.setAlteration_method(method);
					sh = em.merge(sh);
					
				}catch(Exception exp){
					logger.error(exp.getMessage());
				}
				
				return sub;
				
			}catch(Exception exp){
				logger.error(exp.getMessage());
				throw exp;
			}
		}
	
	
	
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Subscription> listServiceMSISDN(String sub_status, int serviceid) throws Exception {
		List<Subscription> msisdnL = new ArrayList<Subscription>();
		try {
			
			Date nowInNairobiTz = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");

			String sql = "SELECT "
					+ "id,"//0
					+ "subscription_status,"//1
					+ "sms_service_id_fk,"//2
					+ "msisdn,"//3
					+ "subscription_timeStamp,"//4
					+ "smsmenu_levels_id_fk,"//5
					+ "request_medium, "//6
					+ "(expiryDate > :opcotz ) as 'subActive'  "
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status=:subscription_status "
							+ "AND sms_service_id_fk =:sms_service_id_fk "
							+ "AND expiryDate > :opcotz "
							+ "AND id not in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(`timeStamp`)=date(now())   )";
			
			Query qry = em.createNativeQuery(sql);
			qry.setParameter("opcotz", nowInNairobiTz);
			qry.setParameter("subscription_status", sub_status);
			qry.setParameter("sms_service_id_fk", serviceid);
			
			List<Object[]> res = qry.getResultList();
			for(Object[] o : res){
				Subscription sub = new Subscription();
				sub.setId(((Integer)o[0] ).longValue());
				sub.setSubscription_status(SubscriptionStatus.get((String)o[1]));
				sub.setSms_service_id_fk(((Integer)o[2]).longValue());
				sub.setMsisdn((String)o[3]);
				sub.setSubscription_timeStamp(( (java.util.Date) o[4]) );
				sub.setSmsmenu_levels_id_fk((Integer)o[5]);
				sub.setRequest_medium(MediumType.get((String)o[6]));
				sub.setSubActive(((BigInteger)o[7]).intValue()>0 );
				msisdnL.add(sub);
			}
			
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
		}


		return msisdnL;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long countsearchSubscription(String msisdn) throws Exception{
		
		Long count = 0L;
		try{
			
			Query qry = em.createQuery("SELECT count(*) from Subscription sub WHERE lower(sub.msisdn) like lower(:msisdn) ");
			qry.setParameter("msisdn", "%"+msisdn+"%");
			count = (Long) qry.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> searchSubscription(String msisdn) throws Exception{
		
		List<Subscription> sublist = new ArrayList<Subscription>();
		
		try{
			
			Query qry = em.createQuery("from Subscription sub WHERE lower(sub.msisdn) like lower(:msisdn) ");
			qry.setParameter("msisdn", "%"+msisdn+"%");
			sublist = qry.getResultList();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		return sublist;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> searchSubscription(String msisdn, int start, int limit) throws Exception{
		
		List<Subscription> sublist = new ArrayList<Subscription>();
		
		try{
			
			Query qry = em.createQuery("from Subscription sub WHERE lower(sub.msisdn) like lower(:msisdn) ");
			qry.setParameter("msisdn", "%"+msisdn+"%");
			qry.setFirstResult(start);
			qry.setMaxResults(limit);
			sublist = qry.getResultList();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		return sublist;
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> listSubscriptions(String msisdn) throws Exception{
		
		List<Subscription> sublist = new ArrayList<Subscription>();
		try{
			Query qry = em.createQuery("from Subscription sub WHERE sub.msisdn=:msisdn ");
			qry.setParameter("msisdn", msisdn);
			sublist = qry.getResultList();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		return sublist;
	}
	
	
	
	@Override
	public List<Subscription> searchSubscriptions(String msisdn) throws Exception{
		
		return searchSubscription(msisdn);
		
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Subscription getSubscription(String msisdn, String cmd) throws Exception{
		
		Subscription subscr = null;
		
		try{
			
			Query qry = em.createQuery("SELECT "
					+ "sub "
					+ "from Subscription sub, SMSService smss "
					+ "WHERE smss.id = sub.sms_service_id_fk AND sub.msisdn=:msisdn AND smss.cmd=:cmd");
			
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("cmd", cmd);
			List<Subscription> sublist = qry.getResultList();
			if(sublist.size()>0){
				subscr = sublist.get(0);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
			
		}
		return subscr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Subscription getSubscription(String msisdn, Long serviceid) throws Exception{
		
		Subscription subscr = null;
		
		boolean subValid = false;
		
		try{
			
			Date opcotime = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			Query qry = em.createQuery("from Subscription sub WHERE sub.msisdn=:msisdn AND sms_service_id_fk=:serviceid AND expiryDate > :opcotime ");
			qry.setParameter("opcotime", opcotime);
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("serviceid", serviceid);
			List<Subscription> sublist = qry.getResultList();
			if(sublist.size()>0)
				subValid = true;
			
			
			qry = em.createQuery("from Subscription sub WHERE sub.msisdn=:msisdn AND sms_service_id_fk=:serviceid");
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("serviceid", serviceid);
			sublist = qry.getResultList();
			if(sublist.size()>0){
				subscr = sublist.get(0);
				subscr.setValid(subValid);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
			
		}
		return subscr;
	}
	
	/**
	 * Updates a subscriber's subscription status
	 * @param conn
	 * @param subscription_id
	 * @param status - com.inmobia.celcom.subscription.dto.SubscriptionStatus
	 * @return
	 */
	
	public boolean updateSubscription(int subscription_id, String msisdn, SubscriptionStatus status, AlterationMethod method) throws Exception {
		
		boolean success = false;
		
		try{
			
			Query qry = em.createQuery("from Subscription where id = :id AND msisdn = :msisdn");
			qry.setParameter("id", Long.valueOf(subscription_id));
			qry.setParameter("msisdn", msisdn);
			Subscription sub = (Subscription) qry.getSingleResult();
			sub.setSubscription_status(status);
			
			SubscriptionHistory sbh = new SubscriptionHistory();
			sbh.setEvent(  (status ==  SubscriptionStatus.confirmed && sub.getRenewal_count()<=0) ? SubscriptionEvent.subscrition.getCode() : 
				(status ==  SubscriptionStatus.unsubscribed ? SubscriptionEvent.unsubscrition.getCode() : SubscriptionEvent.renewal.getCode() ));
			sbh.setMsisdn(msisdn);
			sbh.setAlteration_method(method);
			sbh.setService_id(sub.getSms_service_id_fk());//TODO get the destination timezone from the country object
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			sbh.setTimeStamp(timeInNairobi);
			
			sub = em.merge(sub);
			sbh = em.merge(sbh);
			success = true;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{}
		
		
		return success;
	}
	
	
	public boolean updateSubscription(int subscription_id, SubscriptionStatus status, AlterationMethod method) throws Exception{

		boolean success = false;
		
		try{
			
			Query qry = em.createQuery("from Subscription where id = :id ");
			qry.setParameter("id", Long.valueOf(subscription_id));
			Subscription sub = (Subscription) qry.getSingleResult();
			sub.setSubscription_status(status);
			
			SubscriptionHistory sbh = new SubscriptionHistory();
			sbh.setEvent(  (status ==  SubscriptionStatus.confirmed && sub.getRenewal_count()<=0) ? SubscriptionEvent.subscrition.getCode() : 
				(status ==  SubscriptionStatus.unsubscribed ? SubscriptionEvent.unsubscrition.getCode() : SubscriptionEvent.renewal.getCode() ));
			sbh.setMsisdn(sub.getMsisdn());
			sbh.setAlteration_method(method);
			sbh.setService_id(sub.getSms_service_id_fk());//TODO get the destination timezone from the country object
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			sbh.setTimeStamp(timeInNairobi);
			
			
			sub = em.merge(sub);
			sbh = em.merge(sbh);
			success = true;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		
		return success;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean hasSubscribedToAnyOfTheseServices(String msisdn, List<String> keywords) throws Exception{
		boolean subValid = false;
		try{
			Query qry = em.createQuery("SELECT sub from Subscription sub, SMSService smss WHERE "
					+ "smss.id = sub.sms_service_id_fk AND sub.subscription_status=:subscription_status AND sub.msisdn=:msisdn AND "
					+ "smss.cmd in (:keywords)");
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("keywords", keywords);
			qry.setParameter("subscription_status", SubscriptionStatus.confirmed);
			List<Subscription> sublist = qry.getResultList();
			if(sublist.size()>0){
				Subscription s = sublist.get(0);
				subValid = true;//(s.getSubscription_status() == SubscriptionStatus.confirmed);
			}
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return false;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
			
		}
		return subValid;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean subscriptionValid(String msisdn, Long serviceid) throws Exception{
		boolean subValid = false;
		try{
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			Query qry = em.createQuery("from Subscription sub WHERE sub.subscription_status=:subscription_status AND sub.msisdn=:msisdn AND sms_service_id_fk=:serviceid AND expiryDate > :timeInNairobi ");
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("serviceid", serviceid);
			qry.setParameter("subscription_status", SubscriptionStatus.confirmed);
			qry.setParameter("timeInNairobi", timeInNairobi);
			List<Subscription> sublist = qry.getResultList();
			if(sublist.size()>0){
				Subscription s = sublist.get(0);
				subValid = true;//(s.getSubscription_status() == SubscriptionStatus.confirmed);
			}
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return false;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
			
		}
		return subValid;
	}

	@Override
	public void unsubscribe(String msisdn, OpcoSMSService opcosmsservice){
		try{
			opcosmsservice = em.merge(opcosmsservice);
			SMSService smsservice = opcosmsservice.getSmsservice();
			Subscription subscription = getSubscription(msisdn, smsservice.getId());
			unsubscribe(subscription);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
	}
	
	@Override
	public void unsubscribe(String msisdn, Long serviceid){
		try{
			Subscription subscription = getSubscription(msisdn, serviceid);
			unsubscribe(subscription);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
	}
	
	@Override
	public void unsubscribe(Subscription subscription) throws Exception{
		subscriptionDAO.delete(subscription);
	}
	
	
	@Override
	public boolean unsubscribe(String msisdn, List<String> services, OperatorCountry opco) throws Exception{
		
    	boolean isAtive = false;
		
		if(msisdn==null || services==null || services.size()<1 )
			return false;
		
		StringBuffer sb = new StringBuffer();
		for(String kwd: services){
			OpcoSMSService opcosmsservice = opcosmsserviceejb.getOpcoSMSService(kwd, opco);
			unsubscribe(msisdn,opcosmsservice);
		}
		return isAtive;
	}
}
