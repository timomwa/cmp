package com.pixelandtag.cmp.ejb.api.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.dao.core.IncomingSMSDAOI;
import com.pixelandtag.cmp.dao.core.MessageLogDAOI;
import com.pixelandtag.cmp.dao.core.OutgoingSMSDAOI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MessageLog;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;

@Stateless
@Remote
public class QueueProcessorEJBImpl implements QueueProcessorEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private TimezoneConverterI timeconverterEJB;
	
	private Collection<BillingStatus> billingstatuses = new ArrayList<BillingStatus>();
	
	@PostConstruct
	public void init(){
		billingstatuses.add(BillingStatus.NO_BILLING_REQUIRED);
		billingstatuses.add(BillingStatus.INSUFFICIENT_FUNDS);
		billingstatuses.add(BillingStatus.SUCCESSFULLY_BILLED);
	}
	
	@Inject
	private OutgoingSMSDAOI smsoutDAO;
	
	@Inject
	private IncomingSMSDAOI incomingsmsDAO;
	
	
	@Inject
	private MessageLogDAOI messagelogDAO;
	
	@Override
	public boolean updateMessageLog(String tx_id, MessageStatus status)  throws Exception{
		
		try{
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("cmp_tx_id", tx_id);
			List<MessageLog> msglogs = messagelogDAO.findByNamedQuery(MessageLog.NQ_BY_CMP_TXID, params);
			
			if(msglogs==null || msglogs.size()<1){
				params.clear();
				params.put("opco_tx_id", tx_id);
				msglogs = messagelogDAO.findByNamedQuery(MessageLog.NQ_BY_OPCO_TXID, params);
			}
			
			if(msglogs!=null && msglogs.size()>0){
				MessageLog messagelog = msglogs.get(0);
				messagelog.setStatus(status.name());
				messagelog = messagelogDAO.save(messagelog);
				return true;
			}else{
				throw new Exception("No such record with cmp_tx_id / opco_tx_id = "+tx_id+" in message_log table");
			}
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			return false;
		}
	}
	
	
	@Override
	public boolean updateMessageLog(OutgoingSMS sms, MessageStatus status){
		try{
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("cmp_tx_id", sms.getCmp_tx_id());
			List<MessageLog> msglogs = messagelogDAO.findByNamedQuery(MessageLog.NQ_BY_CMP_TXID, params);
			
			if(msglogs==null || msglogs.size()<1){
				params.clear();
				params.put("opco_tx_id", sms.getOpco_tx_id());
				msglogs = messagelogDAO.findByNamedQuery(MessageLog.NQ_BY_OPCO_TXID, params);
			}
			MessageLog messagelog = null;
			Date mt_timestamp =new Date();
			if(msglogs!=null && msglogs.size()>0){
				messagelog = msglogs.get(0);
				messagelog.setStatus(status.name());
				messagelog.setMt_sms(sms.getSms());
				messagelog.setMt_timestamp(mt_timestamp);
			}else{
				logger.info("No such record with cmp_tx_id / opco_tx_id = "+sms.getCmp_tx_id()+"/"+sms.getOpco_tx_id()+"  in message_log table, so we create one");
				
				messagelog = new MessageLog();
				messagelog.setCmp_tx_id(sms.getCmp_tx_id());
				messagelog.setMo_processor_id_fk(sms.getMoprocessor().getId());
				messagelog.setMsisdn(sms.getMsisdn());
				messagelog.setMt_sms(sms.getSms());
				messagelog.setMt_timestamp(mt_timestamp);
				messagelog.setOpco_tx_id(sms.getOpco_tx_id());
				messagelog.setShortcode(sms.getShortcode());
				messagelog.setSource(sms.getMediumType().name());
				messagelog.setStatus(status.name());
				messagelog.setRetryCount(sms.getRe_tries());
			}
			
			messagelog = messagelogDAO.save(messagelog);
			
			return true;
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			return false;
		}
	}
	
	
	@Override
	public boolean deleteFromQueue(OutgoingSMS sms){
		
		try{
			Query qry = em.createQuery("delete from OutgoingSMS WHERE id=:id");
			qry.setParameter("id", sms.getId());
			qry.executeUpdate();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return true;
	}
	
	
	@Override
	public boolean deleteCorrespondingIncomingSMS(OutgoingSMS sms){
		boolean success = false;
		
		try{
			Query qry = em.createNamedQuery(IncomingSMS.NQ_DELETE_BY_TX_ID);
			qry.setParameter("opco_tx_id", sms.getOpco_tx_id());
			qry.setParameter("cmp_tx_id", sms.getCmp_tx_id());
			qry.executeUpdate();
			success = true;
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
		return success;
	}
	
	
	@Override
	public void updateQueueStatus(Long id, Boolean inqueue){
		try{
			Query qry = em.createNamedQuery(OutgoingSMS.NQ_LIST_UPDATE_QUEUE_STATUS_BY_ID);
			qry.setParameter("id", id);
			qry.setParameter("in_outgoing_queue", inqueue);
			qry.executeUpdate();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
	}
	
	
	@Override
	public OutgoingSMS saveOrUpdate(OutgoingSMS queue) throws Exception{
		return smsoutDAO.save(queue);
	}
	
	@Override
	public IncomingSMS saveOrUpdate(IncomingSMS incomingsms) throws Exception{
		return incomingsmsDAO.save(incomingsms);
	}
	 
	@SuppressWarnings("unchecked")
	@Override
	public List<OutgoingSMS> getUnsent(Long size){
		
		try{
			
			Query qry = em.createNamedQuery(OutgoingSMS.NQ_LIST_UNSENT_ORDER_BY_PRIORITY_DESC);
			qry.setParameter("billstatus", billingstatuses);
			qry.setFirstResult(0);
			qry.setMaxResults(size.intValue());
			return qry.getResultList();
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
		
		return new ArrayList<OutgoingSMS>();
	
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<OutgoingSMS> getUnsent(Long size, OpcoSenderReceiverProfile opcoSenderReceiverProfile){
		
		try{
			Query qry = em.createNamedQuery(OutgoingSMS.NQ_LIST_UNSENT_BY_PROFILE_ORDER_BY_PRIORITY_DESC);
			qry.setParameter("billstatus", billingstatuses);
			qry.setParameter("opcosenderprofile", opcoSenderReceiverProfile);
			qry.setParameter("timestamp", DateTime.now().toDate());
			qry.setFirstResult(0);
			qry.setMaxResults(size.intValue());
			return qry.getResultList();
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
		
		return new ArrayList<OutgoingSMS>();
	}
	
	
	@Override
	public List<IncomingSMS> getLatestMO(int size){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("processed", Boolean.FALSE);
		params.put("mo_ack", Boolean.FALSE);
		return incomingsmsDAO.findByNamedQuery(IncomingSMS.NQ_LIST_UNPROCESSED, params);
	}


}
