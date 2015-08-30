package com.pixelandtag.cmp.ejb.bulksms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.bulksms.BulkSMSAccount;
import com.pixelandtag.bulksms.BulkSMSPlan;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.bulksms.BulkSMSText;
import com.pixelandtag.bulksms.IPAddressWhitelist;
import com.pixelandtag.cmp.ejb.sequences.SequenceGenI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterEJB;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.CMPSequence;
import com.pixelandtag.cmp.exceptions.CMPSequenceException;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class BulkSmsMTEJB implements BulkSmsMTI {

	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;

	@Resource
	private UserTransaction utx;

	@EJB
	private BulkSMSUtilBeanI util_ejb;
	
	@EJB
	private SequenceGenI sequence_ejb;
	
	@EJB
	private TimezoneConverterI timezoneBean;
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI#getUnprocessed(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<BulkSMSQueue> getUnprocessed(Long size){
		List<BulkSMSQueue> unprocessed_queue = new ArrayList<BulkSMSQueue>();
		
		try{
			Query qry = em.createNamedQuery(BulkSMSQueue.NAMED_QUERY);
			Collection<MessageStatus> names = new ArrayList<MessageStatus>();
			names.add(MessageStatus.RECEIVED);
			names.add(MessageStatus.FAILED_TEMPORARILY);
			qry.setParameter("statuses", names);
			qry.setParameter("sheduledate", new Date());
			unprocessed_queue = qry.getResultList();
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		return unprocessed_queue;
	}
	
	

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.bulksms.BulkSmsMTI#enqueue(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public String enqueue(String sourceIp, String apiKey, String username,
			String password, String jsonString)
			throws APIAuthenticationException, ParameterException,
			PlanException, PersistenceException, JSONException,
			QueueFullException, PlanBalanceException, CMPSequenceException {

		String cptxid = "";
		JSONObject inJso = new JSONObject(jsonString);
		String planid = null;
		String text = null;
		String schedule = null;
		String telcoid = null;
		String senderid = null;
		String price = "0";
		String timezone = null;
		int priority = 1;
		try {
			planid = inJso.getString("planid");
		} catch (JSONException jse) {
			logger.warn("planid not provided");
		}
		try {
			text = inJso.getString("text");
		} catch (JSONException jse) {
			logger.warn("text not provided");
		}
		try {
			schedule = inJso.getString("schedule");
		} catch (JSONException jse) {
			logger.warn("schedule not provided");
		}
		try {
			telcoid = inJso.getString("telcoid");
		} catch (JSONException jse) {
			logger.warn("telcoid not provided");
		}
		try {
			senderid = inJso.getString("senderid");
		} catch (JSONException jse) {
			logger.warn("senderid not provided");
		}
		try {
			price = inJso.getString("price");
		} catch (JSONException jse) {
			logger.warn("price not provided");
		}

		try {
			priority = inJso.getInt("priority");
		} catch (JSONException jse) {
			logger.warn("priority not provided");
		}

		String msisdn = null;
		try {
			msisdn = inJso.getString("msisdn");
		} catch (JSONException jse) {
			logger.warn("msisdnlist not provided");
		}

		try {
			timezone = inJso.getString("timezone");
		} catch (JSONException jse) {
			logger.warn("priority not provided");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("planid - (Mandatory) -").append(" : ").append(planid).append("\n");
		sb.append("text - (Mandatory) -").append(" : ").append(text).append("\n");
		sb.append("telcoid - (Mandatory) -").append(" : ").append(telcoid).append("\n");
		sb.append("senderid - (Mandatory) -").append(" : ").append(senderid).append("\n");
		sb.append("price - (Optional) -").append(" : ").append(price).append("\n");
		sb.append("msisdn - (Mandatory) -").append(" : ").append(msisdn).append("\n");
		sb.append("priority - (Optional) -").append(" : ").append(priority).append("\n");
		sb.append("timezone - (Optional) -").append(" : ").append(timezone).append("\n");
		sb.append("schedule - (Optional) -").append(" : ").append(schedule).append("\n");

		if ((planid == null || planid.isEmpty())
				|| (text == null || text.isEmpty())
				|| (telcoid == null || telcoid.isEmpty())
				|| (senderid == null || senderid.isEmpty())
				|| (msisdn == null || msisdn.isEmpty()) ) {
			throw new ParameterException("You have some missing. Parameters: ["
					+ sb.toString() + "]");
		}
		boolean tz_valid = timezoneBean.validateTimezone(timezone);
		if (!tz_valid) {
			throw new ParameterException(
					"Timezone format wrong. Examples of timezone. \"America/New_York\", \"Africa/Nairobi\"");
		}
		Date sheduledate_server_time = null;

		if (schedule == null || schedule.isEmpty())
			sheduledate_server_time = new Date();
		try {
			if (timezone == null || timezone.isEmpty())
				throw new ParameterException(
						"Scheule found without timezone. Please supply timezone. Timezone example : Africa/Nairobi");

			sheduledate_server_time =  timezoneBean.convertFromOneTimeZoneToAnother(timezoneBean.stringToDate(schedule), timezone,"America/New_York");
			boolean isinthepast = timezoneBean.isDateInThePast(sheduledate_server_time);
			if (isinthepast)
				throw new ParameterException(
						"The schedule date is in the past.");
		} catch (ParseException e) {
			throw new ParameterException(
					"Could not parse the scheduledate. Check if your date format and timezone is correct. Timezone example : Africa/Nairobi . Date format should be yyyy-MM-dd HH:mm:ss where"
							+ "\n yyyy – The year, e.g 2015 "
							+ "\n MM – The date, e,g 01 for January "
							+ "\n dd – The day of the month e.g 31 "
							+ "\n HH – the hour of the day between 0 and 23 "
							+ "\n mm – the minute, e,g 03. between 0 and 59 ");
		}

		sb.append("sheduledate").append(" : ").append(sheduledate_server_time).append("\n");

		logger.info("\n\n incoming batch: " + sb.toString());

		BulkSMSAccount account = util_ejb.getAccout(apiKey, username, password);
		if (!account.getActive())
			throw new APIAuthenticationException(
					"This plan is not active. Contact support.");
		boolean hostAllowed = util_ejb.hostAllowed(account, sourceIp);
		if (!hostAllowed)
			throw new APIAuthenticationException("Host not allowed.");

		BulkSMSPlan plan = util_ejb.getPlan(account, planid);
		BigInteger planBalance = util_ejb.getPlanBalance(plan);
		BigInteger thisBatch = BigInteger.ONE;
		
		logger.info(" PLAN BALANCE BEFORE ::::::: " + planBalance.intValue());
		logger.info(" THIS BATCH BEFORE ::::::: " + thisBatch.intValue());
		logger.info(" NOT ENOUGH BALANCE ?::::::: "
				+ (planBalance.compareTo(thisBatch) < 0));

		if (planBalance.compareTo(thisBatch) < 0) {// if we don't have enough
													// balance.
			throw new PlanBalanceException(
					"This plan doesn't have enough credits. Current plan balance is "
							+ planBalance.intValue() + ", this batch: "
							+ thisBatch.intValue());
		}

		BigInteger currentoutgoingsize = util_ejb.getCurrentOutgoingQueue(plan,
				MessageStatus.RECEIVED);

		if (plan.getMaxoutqueue().compareTo(currentoutgoingsize) <= 0)// queue
																		// full
			throw new QueueFullException("Queue is full, please try again.");

		BulkSMSText textb = new BulkSMSText();
		textb.setContent(text);
		textb.setPlan(plan);
		textb.setSenderid(senderid);
		textb.setQueueSize(BigInteger.ONE);
		textb.setSheduledate(sheduledate_server_time);
		textb.setTimezone(timezone);
		textb.setPrice(new BigDecimal(price));
		CMPSequence seq;
		
		seq = sequence_ejb.getOrCreateNextSequence("BLK");
		
		cptxid = seq.getSeqNumber();
		try {
			utx.begin();

			textb = em.merge(textb);

			BulkSMSQueue queue = new BulkSMSQueue();
			queue.setMsisdn(msisdn);
			queue.setPriority(priority);
			queue.setStatus(MessageStatus.RECEIVED);
			queue.setText(textb);
			queue.setBulktxId(cptxid);
			queue = em.merge(queue);
			

			utx.commit();
		}catch (Exception exp) {
			logger.error(exp.getMessage(), exp);
			try {
				utx.rollback();
			} catch (Exception esp) {
			}
			throw new PersistenceException(
					"Could not enqueue this batch. Please try again.", exp);
		} finally {

		}

		return cptxid;

	}

}
