package com.pixelandtag.cmp.ejb.subscription;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.cmp.ejb.DatingServiceBean;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.SubscriptionEvent;
import com.pixelandtag.dating.entities.SubscriptionHistory;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class SubscriptionEJB implements SubscriptionBeanI {
	
public Logger logger = Logger.getLogger(DatingServiceBean.class);
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@Resource
	private UserTransaction utx;

	@EJB
	TimezoneConverterI timezoneEJB;
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#updateCredibilityIndex(java.lang.String, java.lang.Long, int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateCredibilityIndex(String msisdn, Long service_id, int change){
		try{
			Subscription subsc = getSubscription(msisdn, service_id);
			if(subsc!=null){
				utx.begin();
				if(change>=1){
					int prev_index = subsc.getCredibility_index().intValue();
					subsc.setCredibility_index(prev_index>=0 ? (change+prev_index) : 0);//we re-set their index if they previously had a bad record, but now they have credit
				}else{
					subsc.setCredibility_index(subsc.getCredibility_index().intValue()+change);//keep sinking deep into bad credit
				}
				subsc = em.merge(subsc);
				utx.commit();
			}else{
				utx.begin();
				subsc = subscribe(msisdn,service_id,MediumType.ussd,AlterationMethod.system_autorenewal);
				if(change>=1){
					int prev_index = subsc.getCredibility_index().intValue();
					subsc.setCredibility_index(prev_index>=0 ? (change+prev_index) : 0);//we re-set their index if they previously had a bad record, but now they have credit
				}else{
					subsc.setCredibility_index(subsc.getCredibility_index().intValue()+change);//keep sinking deep into bad credit
				}
				subsc = em.merge(subsc);
				utx.commit();
			}
			
		}catch(Exception exp){
			try{
				utx.rollback();
			}catch(Exception esp){
			}
			logger.error(exp.getMessage(),exp);
		}
		
	}
	

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public Subscription subscribe(String msisdn, Long service_id,MediumType medium, AlterationMethod method) {
		Subscription subscription = null;
		try{
			utx.begin();
			subscription = new Subscription();
			subscription.setMsisdn(msisdn);
			subscription.setSms_service_id_fk(service_id);
			subscription.setSubActive(Boolean.TRUE);
			subscription.setSmsmenu_levels_id_fk(-1);
			subscription.setRenewal_count(0L);
			subscription.setQueue_status(0L);
			subscription.setRequest_medium(medium);
			subscription.setSubscription_status(SubscriptionStatus.confirmed);
			
			
			
			subscription = em.merge(subscription);
			utx.commit();
		}catch(Exception e){
			try{
				utx.rollback();
			}catch(Exception exp){}
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
	public Subscription renewSubscription(String msisdn, Long serviceid, AlterationMethod method) throws Exception{
		Subscription sub = null;
		try{
			SMSService service = em.find(SMSService.class, serviceid);
			sub = renewSubscription(msisdn, service, SubscriptionStatus.confirmed, method);
			updateQueueStatus(0L,sub.getId(),method);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return sub;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#updateQueueStatus(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public void updateQueueStatus(Long status, String msisdn, Long sms_service_id, AlterationMethod method) throws Exception{
		try{
			Subscription sub = getSubscription(msisdn, sms_service_id);
			if(sub==null)
				sub = subscribe(msisdn, sms_service_id,MediumType.ussd,method);
			updateQueueStatus(status,sub.getId(), method);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
	}
	
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateQueueStatus(Long status, Long id, AlterationMethod method) throws Exception{
		try{
			utx.begin();
			Query qry = em.createQuery("UPDATE Subscription s SET s.queue_status=:queue_status WHERE s.id=:id");
			qry.setParameter("queue_status", status);
			qry.setParameter("id", id);
			qry.executeUpdate();
			utx.commit();
		}catch(Exception exp){
			try{
				utx.rollback();
			}catch(Exception exp2){
			}
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
	}
	
	
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Subscription renewSubscription(String msisdn, SMSService smsService, SubscriptionStatus substatus,  AlterationMethod method) throws Exception{
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
					
				}
				
				if(sub==null){
					sub = new Subscription();
					sub.setMsisdn(msisdn);
					sub.setSms_service_id_fk(smsService.getId());
				}
				
				Date nowInNairobiTz = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
				qry = em.createNativeQuery("select DATE_ADD(:curdate_local, INTERVAL :sub_length "+smsService.getSubscription_length_time_unit()+") ");
				qry.setParameter("curdate_local", nowInNairobiTz);
				qry.setParameter("sub_length", smsService.getSubscription_length());
				Object o = qry.getSingleResult();
				String expiryDate = (String) o;
				
				logger.error(">>>>RENEWING SUBSCRIPTION OPCO_TZ:  NAIROBI_TIME :::"+nowInNairobiTz+"  expiryDate:: "+expiryDate);
				
				try{
					utx.begin();
					sub.setExpiryDate(timezoneEJB.stringToDate(expiryDate));
					sub.setRenewal_count(sub.getRenewal_count()+1);
					sub.setSubscription_status(substatus);
					sub.setRequest_medium(MediumType.sms);
					sub = em.merge(sub);
					
					SubscriptionHistory sh = new SubscriptionHistory();
					sh.setEvent(sub.getRenewal_count()<=0 ? SubscriptionEvent.subscrition.getCode() : SubscriptionEvent.renewal.getCode());
					sh.setMsisdn(msisdn);
					sh.setService_id(smsService.getId());
					sh.setTimeStamp(new Date());
					sh.setAlteration_method(method);
					sh = em.merge(sh);
					
					utx.commit();
				}catch(Exception exp){
					try{
						utx.rollback();
					}catch(Exception expr){}
					logger.error(exp.getMessage());
				}
				
				
			}catch(Exception exp){
				//throw exp;
				logger.error(exp.getMessage());
			}
			return sub;
		}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Subscription> listServiceMSISDN(String sub_status, int serviceid) throws Exception {
		List<Subscription> msisdnL = new ArrayList<Subscription>();
		try {
			
			Date nowInNairobiTz = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			
			utx.begin();
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
			
			
			
			utx.commit();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception e1){}
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSubscription(int subscription_id, String msisdn, SubscriptionStatus status, AlterationMethod method) throws Exception {
		
		boolean success = false;
		
		try{
			
			Query qry = em.createQuery("from Subscription where id = :id AND msisdn = :msisdn");
			qry.setParameter("id", Long.valueOf(subscription_id));
			qry.setParameter("msisdn", msisdn);
			Subscription sub = (Subscription) qry.getSingleResult();
			sub.setSubscription_status(status);
			
			SubscriptionHistory sbh = new SubscriptionHistory();
			sbh.setEvent(sub.getRenewal_count()<=0 ? SubscriptionEvent.subscrition.getCode() : SubscriptionEvent.renewal.getCode());
			sbh.setMsisdn(msisdn);
			sbh.setAlteration_method(method);
			sbh.setService_id(sub.getSms_service_id_fk());//TODO get the destination timezone from the country object
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			sbh.setTimeStamp(timeInNairobi);
			
			utx.begin();
			sub = em.merge(sub);
			sbh = em.merge(sbh);
			success = true;
			utx.commit();
		}catch(Exception e){
			try{
			utx.rollback();
			}catch(Exception ee){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		
		return success;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean updateSubscription(int subscription_id, SubscriptionStatus status, AlterationMethod method) throws Exception{

		boolean success = false;
		
		try{
			
			Query qry = em.createQuery("from Subscription where id = :id AND msisdn = :msisdn");
			qry.setParameter("id", Long.valueOf(subscription_id));
			Subscription sub = (Subscription) qry.getSingleResult();
			utx.begin();
			sub.setSubscription_status(status);
			
			SubscriptionHistory sbh = new SubscriptionHistory();
			sbh.setEvent(sub.getRenewal_count()<=0 ? SubscriptionEvent.subscrition.getCode() : SubscriptionEvent.renewal.getCode());
			sbh.setMsisdn(sub.getMsisdn());
			sbh.setAlteration_method(method);
			sbh.setService_id(sub.getSms_service_id_fk());//TODO get the destination timezone from the country object
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			sbh.setTimeStamp(timeInNairobi);
			
			
			sub = em.merge(sub);
			sbh = em.merge(sbh);
			success = true;
			utx.commit();
		}catch(Exception e){
			try{
			utx.rollback();
			}catch(Exception ee){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		
		return success;
	}

}
