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
import com.pixelandtag.dating.entities.SubscriptionEvent;
import com.pixelandtag.dating.entities.SubscriptionHistory;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.StopWatch;

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
	
	//private StopWatch watch = new StopWatch();

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#getExpiredSubscriptions(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> getExpiredSubscriptions(Long size) {
		List<Subscription> expired = new ArrayList<Subscription>();
		try{
			Date timeInNairobi = timezoneEJB.convertFromOneTimeZoneToAnother(new Date(), "America/New_York", "Africa/Nairobi");
			Query qry   = em.createQuery("from Subscription s WHERE s.subscription_status=:status  AND s.expiryDate<=:todaydate AND s.queue_status = 0");
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
	public Subscription renewSubscription(String msisdn, Long serviceid) throws Exception{
		Subscription sub = null;
		try{
			SMSService service = em.find(SMSService.class, serviceid);
			sub = renewSubscription(msisdn, service);
			updateQueueStatus(0L,sub.getId());
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return sub;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI#updateQueueStatus(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public void updateQueueStatus(Long status, String msisdn, Long sms_service_id) throws Exception{
		try{
			Subscription sub = getSubscription(msisdn, sms_service_id);
			updateQueueStatus(status,sub.getId());
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
	}
	
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateQueueStatus(Long status, Long id) throws Exception{
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
	public Subscription renewSubscription(String msisdn, SMSService smsService) throws Exception{
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
					sub.setSubscription_status(SubscriptionStatus.confirmed);
					sub.setRequest_medium(MediumType.sms);
					sub = em.merge(sub);
					
					SubscriptionHistory sh = new SubscriptionHistory();
					sh.setEvent(sub.getRenewal_count()<=0 ? SubscriptionEvent.subscrition.getCode() : SubscriptionEvent.renewal.getCode());
					sh.setMsisdn(msisdn);
					sh.setService_id(smsService.getId());
					sh.setTimeStamp(new Date());
					
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

}
