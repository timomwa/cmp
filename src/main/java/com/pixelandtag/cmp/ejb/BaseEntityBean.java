package com.pixelandtag.cmp.ejb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericMessage;
import com.pixelandtag.cmp.entities.MOProcessorE;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.exceptions.TransactionIDGenException;
import com.pixelandtag.dating.entities.SubscriptionEvent;
import com.pixelandtag.dating.entities.SubscriptionHistory;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.mms.api.TarrifCode;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.Subscription;
import com.pixelandtag.subscription.SubscriptionMain;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.StopWatch;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class BaseEntityBean implements BaseEntityI {
	
	private Logger logger = Logger.getLogger(BaseEntityBean.class);
	private String server_tz = "-05:00";//TODO externalize
	private String client_tz = "+03:00";//TODO externalize
	private String mtUrl = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";//TODO externalize this
	private StopWatch watch;
	private volatile int recursiveCounter = 0;
	private List<NameValuePair> qparams = null;
	private ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
	private HttpClient httpsclient;
	
	@EJB
	private CMPResourceBeanRemote cmp_ejb;
	
	private  TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
    };
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void  mimicMO(String keyword, String msisdn){
		try {
			SMSService smsserv = getSMSService(keyword);
			Long processor_fk = smsserv.getMo_processorFK();
			MOProcessorE proc = find(MOProcessorE.class, processor_fk);
			
			MOSms mosm_ =  new MOSms();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
			mosm_.setMsisdn(msisdn);
			mosm_.setServiceid(smsserv.getId().intValue());
			mosm_.setSMS_Message_String(smsserv.getCmd());
			mosm_.setSMS_SourceAddr(proc.getShortcode());
			mosm_.setCMP_AKeyword(smsserv.getCmd());
			mosm_.setCMP_SKeyword(smsserv.getCmd());
			mosm_.setPrice(BigDecimal.valueOf(smsserv.getPrice()));
			mosm_.setCMP_Txid(BigInteger.valueOf(generateNextTxId()));
			mosm_.setEventType(EventType.get(smsserv.getEvent_type()));
			mosm_.setServiceid(smsserv.getId().intValue());
			mosm_.setPricePointKeyword(smsserv.getPrice_point_keyword());
			mosm_.setProcessor_id(processor_fk);
			
			logger.info("\n\n\n\n\n::::::::::::::::processor_fk.intValue() "+processor_fk.intValue()+"::::::::::::::\n\n\n");

			logMO(mosm_);
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
	}
    public boolean hasAnyActiveSubscription(String msisdn, List<String> services) throws Exception{
		
		boolean isAtive = false;
		
		if(msisdn==null || services==null || services.size()<1 )
			return false;
		
		for(String kwd: services){
			SMSService smsservice = getSMSService(kwd);
			if(subscriptionValid(msisdn, smsservice.getId()))
				return true;
			
		}
		return isAtive;
	}
    @SuppressWarnings("unchecked")
	public Subscription renewSubscription(String msisdn, SMSService smsService) throws Exception{
		Subscription sub = null;
		try{
			Query qry = em.createQuery("from Subscription where msisdn=:msisdn AND sms_service_id_fk=:sms_service_id_fk");
			qry.setParameter("sms_service_id_fk", smsService.getId());
			qry.setParameter("msisdn", msisdn);
			
			List<Subscription> scrl = qry.getResultList();
			
			if(scrl.size()>0){
				sub = scrl.get(0);
			}
			
			if(sub==null){//create new subscription
				sub = new Subscription();
				sub.setMsisdn(msisdn);
				sub.setSms_service_id_fk(smsService.getId());
				sub.setRenewal_count(0L);
				sub.setRequest_medium(MediumType.sms);
				sub.setSubActive(true);
				sub.setSmsmenu_levels_id_fk(-1);
			}
			
			qry = em.createNativeQuery("select DATE_ADD(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"'), INTERVAL :sub_length "+smsService.getSubscription_length_time_unit()+") ");
			qry.setParameter("sub_length", smsService.getSubscription_length());
			Object o = qry.getSingleResult();
			Date expiryDate = (Date) o;
			
		
			try{
				utx.begin();
				sub.setExpiryDate(expiryDate);
				sub.setRenewal_count(sub.getRenewal_count()+1);
				sub.setSubscription_status(SubscriptionStatus.confirmed);
				sub.setSubscription_timeStamp(new Date());
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
				throw exp;
			}
			
			
		}catch(Exception exp){
			throw exp;
		}
		return sub;
	}
	public BaseEntityBean() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException{
		watch = new StopWatch();
		SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy, 
		SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
						    
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 8443, sf));
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(2);
		cm.setMaxTotal(2);//http connections that are equal to the worker threads.
		httpsclient = new DefaultHttpClient(cm);
	}
	@Override
	public EntityManager getEM() {
		return em;
	}

	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	protected EntityManager em;
	

	@Resource
	protected UserTransaction utx;
	
	
	public void setServerTz(String server_tz)  throws Exception {
		this.server_tz = server_tz;
	}

	public void setClientTz(String client_tz)  throws Exception {
		this.client_tz = client_tz;
	}
	
	
	public String getServerTz()  throws Exception {
		return this.server_tz;
	}

	public String getClientTz()  throws Exception {
		return this.client_tz;
	}
	
	
	private String buildWhere(Map<String, Object> criteria)  throws Exception {
		StringBuffer sb = new StringBuffer();
		if (criteria.size() > 0)
			sb.append(" WHERE ");
		int counter2 = 0;
		for (String key : criteria.keySet()) {
			counter2++;
			sb.append(key).append("=:").append("param").append(String.valueOf(counter2))
					.append(criteria.size() == counter2 ? "" : " AND ");
		}
		return sb.toString();
	}
	

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> find(Class<T> entityClass,
			Map<String, Object> criteria, int start, int end)  throws Exception {
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName()
		
				+ buildWhere(criteria));
			int counter1 = 0;
			for (String key : criteria.keySet()){
				counter1++;
				query.setParameter("param"+String.valueOf(counter1), criteria.get(key));			
			}			
			return query.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			throw e;
		}
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> T saveOrUpdate(T t) throws Exception{
		try{
			utx.begin();
			t = em.merge(t);
			utx.commit();
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return t;
	}
	
/**
	 * saves and commits
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	public <T> T find(Class<T> entityClass, Long id) throws Exception {
		try{
			T t = em.find( entityClass,id);
			return t;
		}catch(Exception  e){
			throw e;
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean subscriptionValid(String msisdn, Long serviceid) throws Exception{
		boolean subValid = false;
		try{
			Query qry = em.createQuery("from Subscription sub WHERE sub.msisdn=:msisdn AND sms_service_id_fk=:serviceid AND expiryDate > convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') ");
			qry.setParameter("msisdn", msisdn);
			qry.setParameter("serviceid", serviceid);
			List<Subscription> sublist = qry.getResultList();
			if(sublist.size()>0){
				Subscription s = sublist.get(0);
				logger.info("\n\n\n\t\t\t GRRR{{}{}{}{}{}{}{}{}{}{}{}{{}{{}{}{} s.getSubscription_status() : "+s.getSubscription_status());
				logger.info("\n\n\n\t\t\t GRRR{{}{}{}{}{}{}{}{}{}{}{}{{}{{}{}{} (s.getSubscription_status() == SubscriptionStatus.confirmed) : "+(s.getSubscription_status() == SubscriptionStatus.confirmed)+" \n\n");
				subValid = (s.getSubscription_status() == SubscriptionStatus.confirmed);
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

	/**
	 * To statslog
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean toStatsLog(MOSms mo, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, mo.getServiceid());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getCMP_Txid());
			qry.setParameter(4, mo.getCMP_AKeyword());
			qry.setParameter(5, mo.getCMP_SKeyword());
			if(mo.getCMP_SKeyword().equals(TarrifCode.RM1.getCode()))
				qry.setParameter(6, 1d);
			else
				qry.setParameter(6, mo.getPrice().doubleValue());
			qry.setParameter(7, mo.isSubscriptionPush());
			
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return false;
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}

	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	

/**
	 * Logs in httptosend
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean sendMT(MOSms mo, String sql) throws Exception{
		boolean success = false;
		try{
		 
			utx.begin();
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, mo.getMt_Sent());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getSMS_SourceAddr());
			qry.setParameter(4, mo.getSMS_SourceAddr());
			
			qry.setParameter(5, mo.getCMP_AKeyword());
			qry.setParameter(6, mo.getCMP_SKeyword());
			qry.setParameter(7, mo.getPriority());
			
			if(!(mo.getCMP_Txid().compareTo(BigInteger.valueOf(-1))==0)){
				qry.setParameter(8, String.valueOf(mo.getCMP_Txid()));
			}
			qry.setParameter(9, (mo.isSplit_msg() ? 1 : 0));
			qry.setParameter(10, mo.getServiceid());
			qry.setParameter(11, String.valueOf(mo.getPrice()));
			qry.setParameter(12, mo.getSMS_DataCodingId());
			qry.setParameter(13, mo.getProcessor_id());
			qry.setParameter(14, mo.getBillingStatus().toString());
			logger.info("\n\n\n\n\n\t\t\t\t\t\t:::::: mo.getBillingStatus().toString() ::: "+mo.getBillingStatus().toString());
			qry.setParameter(15, mo.getPricePointKeyword()==null ? "NONE" :  mo.getPricePointKeyword());
			qry.setParameter(16, (mo.isSubscription() ? 1 : 0));
			qry.setParameter(17, ( mo.isSubscription() ? 1 : 0 ));
			
			int num =  qry.executeUpdate();
			utx.commit();
			success = num>0;
		
		}catch(Exception e){
			logger.info("\n\n\n\n\n\t\t\t\t\t\t:::::: mo.getBillingStatus().toString() ::: "+mo.getBillingStatus().toString());
			
			try {
				utx.rollback();
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error("\n\n\n\n\n\t\t\t\t\t\t:::::: mo.getBillingStatus().toString() ::: "+mo.getBillingStatus().toString() + e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}


	@SuppressWarnings("unchecked")
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception  {
		T t = null;
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
			if(query.getResultList().size()>0)
				t = (T) query.getResultList().get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception  e){
			throw e;
		}
		return t;
	}

	
	
	@Override
	public Billable charge(Billable billable) throws Exception {
		
		HttpPost httsppost = new HttpPost(this.mtUrl);

		HttpEntity resEntity = null;;
		
		boolean success = false;
		String message = "";
		
		try {
			
			String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
			String encoding = null;
			sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
			encoding = encoder.encode( usernamePassword.getBytes() ); 
			httsppost.setHeader("Authorization", "Basic " + encoding);
			httsppost.setHeader("SOAPAction","");
			httsppost.setHeader("Content-Type","text/xml; charset=utf-8");
			
			String xml = billable.getChargeXML(BillableI.plainchargeXML);
			logger.debug("BILLABLE: "+billable.toString());
			System.out.println(" \n\n\n>> XML SENT \n : "+xml + "\n\n");
			logger.info("XML SENT \n : "+xml + "\n");
			StringEntity se = new StringEntity(xml);
			httsppost.setEntity(se);
			
			
			watch.start();
			HttpResponse response = httpsclient.execute(httsppost);
			watch.stop();
			logger.debug("billable.getMsisdn()="+billable.getMsisdn()+" :::: Shortcode="+billable.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to bill via HTTP");
				
			 
			 final int RESP_CODE = response.getStatusLine().getStatusCode();
			 
			 resEntity = response.getEntity();
			 
			 String resp = convertStreamToString(resEntity.getContent());
			
			 logger.debug("RESP CODE : "+RESP_CODE);
			 logger.debug("RESP XML : "+resp);
			 
			
			
			 billable.setProcessed(1L);
			
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				
				billable.setRetry_count(billable.getRetry_count()+1);
				
				success  = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
				billable.setSuccess(success );
				
				if(!success){
					
					String err = getErrorCode(resp);
					String errMsg = getErrorMessage(resp);
					logger.debug("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
					logger.debug("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+errMsg+"]:::::::::::::::::::::: resp:");
					logger.info("FAILED TO BILL ERROR="+err+", ERROR_MESSAGE="+errMsg+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					billable.setResp_status_code(err);
				}else{
					
					billable.setResp_status_code("Success");
					logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
					logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					
					
					
				}
				
								
			}else if(RESP_CODE == 400){
				
				success  = false;
				
			}else if(RESP_CODE == 401){
				success  = false;
				
				logger.error("\nUnauthorized!");
				
			}else if(RESP_CODE == 404 || RESP_CODE == 403){
				
				success  = false;
				
			}else if(RESP_CODE == 503){

				success  = false;
				
			}else{
				
				success = false;
				
			}
			
					
			}catch (Exception ioe) {
				
				success  = false;
				
				message = ioe.getMessage();
				
				httsppost.abort();
				
				throw ioe;
				
			} finally{
				
				watch.reset();
				
				
				logger.debug(" ::::::: finished attempt to bill via HTTP");
				
				//removeAllParams(qparams);
				
				 // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
				try {
					
					if(resEntity!=null)
						EntityUtils.consume(resEntity);
				
				} catch (Exception e) {
					
					logger.error(e.getMessage(),e);
				
				}
				
				
				try{
					
					billable.setProcessed(1L);
					billable.setIn_outgoing_queue(0L);
					
					if(billable.isSuccess() ||  "Success".equals(billable.getResp_status_code()) ){
						billable.setResp_status_code(BillingStatus.SUCCESSFULLY_BILLED.toString());
						cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.SUCCESSFULLY_BILLED);
						cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.Success);
					}
					if("TWSS_101".equals(billable.getResp_status_code()) || "TWSS_114".equals(billable.getResp_status_code()) || "TWSS_101".equals(billable.getResp_status_code())){
						billable.setResp_status_code(BillingStatus.BILLING_FAILED_PERMANENTLY.toString());
						cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
						cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.InvalidSubscriber);
					}
					if("OL402".equals(billable.getResp_status_code()) || "OL404".equals(billable.getResp_status_code()) || "OL405".equals(billable.getResp_status_code())  || "OL406".equals(billable.getResp_status_code())){
						billable.setResp_status_code(BillingStatus.INSUFFICIENT_FUNDS.toString());
						cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.INSUFFICIENT_FUNDS);
						cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAInsufficientBalance);
					}
					
					if("TWSS_109".equals(billable.getResp_status_code())){
						billable.setResp_status_code(BillingStatus.BILLING_FAILED.toString());
						billable.setIn_outgoing_queue(0L);
						billable.setProcessed(0L);
						billable.setRetry_count( (billable.getRetry_count()+1 ) );
						billable.setMaxRetriesAllowed( (billable.getRetry_count()+2 ) );
						cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAChargeFailure);
						
					}
					
					//cmp_ejb.saveOrUpdate(billable);
					
					if(!success){//return back to queue if we did not succeed
						//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
						//cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
						
						//on third try, we abort
						httsppost.abort();
						
						
					}else{
						
						recursiveCounter = 0;
						//logger.warn(message+" >>MESSAGE_NOT_SENT> "+mt.toString());
					}
					
				
					
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
				
				watch.reset();
				
				
				logger.debug("DONE! ");
				
			}
		
		
		return billable;
	}
	
	
	
	
	private void removeAllParams(List<NameValuePair> params) {
		params.clear();
	}
	/**
	 * Utility method for converting Stream To String
	 * To convert the InputStream to String we use the
	 * BufferedReader.readLine() method. We iterate until the BufferedReader
	 * return null which means there's no more data to read. Each line will
	 * appended to a StringBuilder and returned as String.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public  String convertStreamToString(InputStream is)
			throws IOException {
		
		StringBuilder sb = null;
		BufferedReader reader = null;
		
		if (is != null) {
			sb = new StringBuilder();
			String line;

			try {
				reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateMO(String msg, Long msgId) throws TransactionIDGenException{
		try{
			utx.begin();
			Query qry2 = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog`  set MT_Sent=:sms WHERE  id=:id");
			qry2.setParameter("sms", msg);
			qry2.setParameter("id", msgId);
			qry2.executeUpdate();
			utx.commit();
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage(), nre);
		}catch(Exception exp){
			try{
				utx.rollback();
			}catch(Exception expz){}
			logger.error(exp.getMessage(), exp);
		}
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public MOSms updateMO(MOSms mo) throws TransactionIDGenException{
		try{
			utx.begin();
			Query qry2 = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog`  set MT_Sent=? WHERE  id=?");
			qry2.setParameter(1, mo.getMt_Sent());
			qry2.setParameter(2, mo.getId());
			qry2.executeUpdate();
			utx.commit();
			
		}catch(javax.persistence.NoResultException nre){
			logger.warn(nre.getMessage(), nre);
		}catch(Exception exp){
			try{
				utx.rollback();
			}catch(Exception expz){}
			logger.error(exp.getMessage(), exp);
		}
		
		return mo;
	}
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#logMO(com.pixelandtag.MO)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public MOSms logMO(MOSms mo) throws TransactionIDGenException{
		
		logger.debug("LOGGING_MO_CELCOM_");
		
		if(mo.getCMP_Txid().compareTo(BigInteger.ONE)<0){
			try{
				mo.setCMP_Txid(BigInteger.valueOf(SubscriptionMain.generateNextTxId()));
			}catch(InterruptedException exp){
				logger.error(exp.getMessage(), exp);
				throw new TransactionIDGenException("Couldn't acquire lock, so could not generate the transaction id!");
			}
		}
		
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId()   ["+mo.getSMS_DataCodingId()+"]");
		logger.debug("BEFORE_LOGGING_SMS : GenericMessage.NON_ASCII_SMS_ENCODING_ID   ["+mo.getSMS_DataCodingId()+"]");
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId()!=null  ["+(mo.getSMS_DataCodingId()!=null)+"]");
		logger.debug("BEFORE_LOGGING_SMS : mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID)  ["+(mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID))+"]");
		
		if((mo.getSMS_DataCodingId()!=null) && mo.getSMS_DataCodingId().trim().equals(GenericMessage.NON_ASCII_SMS_ENCODING_ID)){
			logger.debug("BEFORE_DECODING Data Encoding = Old sms = "+mo.getSMS_Message_String());
			mo.setSMS_Message_String(hexToString(mo.getSMS_Message_String().replaceAll("00","")));
			logger.debug("AFTER_DECODING new sms = "+mo.getSMS_Message_String());
		}
		
		try {
			
			mo = resolveKeywords(mo);//First resolve the keyword..
				
			utx.begin();
			Query qry = em.createNativeQuery("INSERT INTO `"+CelcomImpl.database+"`.`messagelog`(CMP_Txid,MO_Received,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,price,serviceid,mo_processor_id_fk,msg_was_split,event_type,price_point_keyword,MT_Sent,source) " +
						"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "); 
			
			logger.info("\n\n\n\n\n\nINCOMING WHOLE SMS ["+mo.getSMS_Message_String()+"] \n\n\n\n\n\n\n");
			qry.setParameter(1, mo.getCMP_Txid().toString());
			qry.setParameter(2, mo.getSMS_Message_String());
			qry.setParameter(3, mo.getSMS_SourceAddr());
			qry.setParameter(4, mo.getMsisdn());
			qry.setParameter(5, mo.getSMS_DataCodingId());
			qry.setParameter(6, mo.getCMPResponse());
			qry.setParameter(7, mo.getAPIType());
			qry.setParameter(8, mo.getCMP_AKeyword());
			qry.setParameter(9, mo.getCMP_SKeyword());
			
			qry.setParameter(10, mo.getPrice()!=null ? mo.getPrice().doubleValue() : 0.0d);
			qry.setParameter(11, mo.getServiceid());
			qry.setParameter(12, mo.getProcessor_id());
			qry.setParameter(13, mo.isSplit_msg());
			
			qry.setParameter(14, mo.getEventType()!=null ? mo.getEventType().getName() : EventType.CONTENT_PURCHASE.getName());
			qry.setParameter(15, mo.getPricePointKeyword());
			qry.setParameter(16, mo.getMt_Sent());
			qry.setParameter(17, mo.getMediumType().getType());
			logger.info("::::::::::::::::::: mo.getMt_Sent(): "+ mo.getMt_Sent());
			logger.info(":::::::::::::::::::mo.getCMP_Txid(): "+mo.getCMP_Txid().toString());
			logger.info(":::::::::::::::::::mo.getCMP_Keyword(): "+mo.getCMP_AKeyword());
			logger.info(":::::::::::::::::::mo.getCMP_SKeyword(): "+mo.getCMP_SKeyword());
			logger.info(":::::::::::::::::::mo.getPrice(): "+mo.getPrice());
			logger.info(":::::::::::::::::::mo.getProcessor_id(): "+mo.getProcessor_id());
			logger.info(":::::::::::::::::::mo.isSplit_msg(): "+mo.isSplit_msg());
			logger.info(":::::::::::::::::::mo.getSMS_DataCodingId(): "+mo.getSMS_DataCodingId());
			logger.info("::::::::::::::::::: mo.getPricePointKeyword(): "+ mo.getPricePointKeyword());
			logger.info("::::::::::::::::::: mo.getEventType(): "+ mo.getEventType());
			
			
			
			
			qry.executeUpdate();
			utx.commit();
			
			try{
				
				Query qry2 = em.createNativeQuery("SELECT id FROM `"+CelcomImpl.database+"`.`messagelog` WHERE  CMP_Txid=?");
				qry2.setParameter(1, mo.getCMP_Txid().toString());
				Object o = qry2.getSingleResult();
				Long l   =  ((BigInteger) o).longValue();
				mo.setId(l.longValue());
				
			}catch(javax.persistence.NoResultException nre){
				logger.warn(nre.getMessage(), nre);
			}catch(Exception exp){
				logger.error(exp.getMessage(), exp);
			}
			
		} catch (Exception e) {
			try{
				utx.rollback();
			}catch(Exception exp){}
			logger.error(e.getMessage(),e);
		}finally{
			
		}
		
		return mo;
		
	}
	
	@SuppressWarnings("unchecked")
	public MOSms resolveKeywords(MOSms mo) {
		logger.info(">>>>>>V.7>>>>>>>>>>>>>CELCOM_MO_SMS["+mo.getSMS_Message_String()+"]");
		
		
		
		if(mo.getSMS_Message_String()!=null){
			if(mo.getSMS_Message_String().isEmpty())
				return mo;
			
		}else{
			mo.setSMS_Message_String(mo.getSMS_Message_String().trim().toUpperCase());
			logger.info(">>>>>>>>>>>>>>>>>>>SMS["+mo.getSMS_Message_String()+"]");
		}
		
		
		try {
			
			Query qry = em.createNativeQuery("SELECT "
					+ "`mop`.id as 'mo_processor_id_fk', "//0
					+ "`sms`.CMP_Keyword, "//1
					+ "`sms`.CMP_SKeyword, "//2
					+ "`sms`.price as 'sms_price', "//3
					+ "`sms`.id as 'serviceid', "//4
					+ "`sms`.`split_mt` as 'split_mt', "//5
					+ "`sms`.`event_type` as 'event_type', "//6
					+ "`sms`.`price_point_keyword` as 'price_point_keyword' "//7
					+ "FROM `"+CelcomImpl.database+"`.`sms_service` `sms` LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` `mop` on `sms`.`mo_processorFK`=`mop`.`id` WHERE  `mop`.`shortcode`=? AND `sms`.`enabled`=1  AND  `mop`.`enabled`=1 AND `sms`.`CMD`= ?");
			qry.setParameter(1, mo.getSMS_SourceAddr());
			qry.setParameter(2, replaceAllIllegalCharacters(mo.getSMS_Message_String().split("[\\s]")[0].toUpperCase()));
			logger.info("Msg : "+mo.getSMS_Message_String());
			logger.info("Keyword : "+replaceAllIllegalCharacters(mo.getSMS_Message_String().split("[\\s]")[0].toUpperCase()));
			
			List<Object[]> resp = qry.getResultList();
			
			if(resp.size()>0){
				for(Object[] o: resp){
					mo.setCMP_AKeyword((String)o[1]);//rs.getString("CMP_Keyword"));
					mo.setCMP_SKeyword((String)o[2]);//rs.getString("CMP_SKeyword"));
					mo.setServiceid( ((Integer)o[4]).intValue());//rs.getInt("serviceid"));
					mo.setPrice(BigDecimal.valueOf((Double)o[3]));//BigDecimal.valueOf(rs.getDouble("sms_price")));
					Long proc_id = Long.valueOf( (  (Integer)o[0] )+"" );
					mo.setProcessor_id(proc_id);//rs.getInt("mo_processor_id_fk"));
					mo.setSplit_msg((Boolean)o[5]);//rs.getBoolean("split_mt"));
					mo.setPricePointKeyword((String)o[7]);//rs.getString("price_point_keyword"));
					mo.setEventType(com.pixelandtag.sms.producerthreads.EventType.get((String)o[6]));//rs.getString("event_type")));
				}
			}else{
				
				Query qry2 = em.createNativeQuery("SELECT "
						+ "`mop`.id as 'mo_processor_id_fk', "//0
						+ "`sms`.CMP_Keyword, `sms`.CMP_SKeyword, "//1
						+ "`sms`.price as 'sms_price', "//2
						+ "sms.id as 'serviceid', "//3
						+ "`sms`.`split_mt` as 'split_mt', "//4
						+ "`sms`.`event_type` as 'event_type', "//5
						+ "`sms`.`price_point_keyword` as 'price_point_keyword' "//6
						+ "FROM `"+CelcomImpl.database+"`.`sms_service` sms LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` mop ON mop.id = sms.mo_processorFK WHERE sms.cmd='DEFAULT' AND sms.enabled=1 AND mop.shortcode=?");
				
				qry2.setParameter(1,mo.getSMS_SourceAddr());
				
				List<Object[]> resp2 = qry2.getResultList();
				
				if(resp2.size()>0){
					
					for(Object[] o: resp2){
						/*mo.setCMP_AKeyword(rs.getString("CMP_Keyword"));
						mo.setCMP_SKeyword(rs.getString("CMP_SKeyword"));
						mo.setServiceid(rs.getInt("serviceid"));
						mo.setPrice(BigDecimal.valueOf(rs.getDouble("sms_price")));
						mo.setProcessor_id(rs.getInt("mo_processor_id_fk"));
						mo.setSplit_msg(rs.getBoolean("split_mt"));
						mo.setPricePointKeyword(rs.getString("price_point_keyword"));
						mo.setEventType(com.pixelandtag.sms.producerthreads.EventType.get(rs.getString("event_type")));*/
						mo.setCMP_AKeyword((String)o[1]);//rs.getString("CMP_Keyword"));
						mo.setCMP_SKeyword((String)o[2]);//rs.getString("CMP_SKeyword"));
						mo.setServiceid( ((Integer)o[4]).intValue());//rs.getInt("serviceid"));
						mo.setPrice(  BigDecimal.valueOf((Double)o[3])  );//BigDecimal.valueOf(rs.getDouble("sms_price")));
						Long proc_id = Long.valueOf( (  (Integer)o[0] )+"" );
						mo.setProcessor_id(proc_id);//rs.getInt("mo_processor_id_fk"));
						mo.setSplit_msg((Boolean)o[5]);//rs.getBoolean("split_mt"));
						mo.setPricePointKeyword((String)o[7]);//rs.getString("price_point_keyword"));
						mo.setEventType(com.pixelandtag.sms.producerthreads.EventType.get((String)o[6]));//rs.getString("event_type")));
					}
				
				}else{
					Query qry3 = em.createNativeQuery("SELECT `mop`.id as 'mo_processor_id_fk' FROM `"+CelcomImpl.database+"`.`mo_processors` mop WHERE mop.shortcode=?");
					
					qry3.setParameter(1,mo.getSMS_SourceAddr());
					
					List<Integer> resp3 = qry3.getResultList();
					
					for(Integer o: resp3){
						Long proc_id = Long.valueOf(  o.intValue() );
						mo.setProcessor_id(proc_id);//rs.getInt("mo_processor_id_fk"));
					}
					
				}
				
				
			}
		
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			e.printStackTrace();
		}finally{
			
		}
		
		
		return mo;
	}
	
	
	public static String hexToString(String txtInHex){
		
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }

	
	public String replaceAllIllegalCharacters(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.trim();
		
		return text;
		
	}
	@SuppressWarnings("unchecked")
	public SMSService getSMSService(String cmd)  throws Exception{
		SMSService sub = null;
		try{
			Query qry = em.createQuery("from SMSService sm WHERE sm.cmd=:cmd");
			qry.setParameter("cmd", cmd);
			List<SMSService> subl = qry.getResultList();
			if(subl.size()>0)
				sub = subl.get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			throw exp;
		}
		return sub;
	}
	
	private  String getErrorMessage(String resp) {
		int start = resp.indexOf("<errorMessage>")+"<errorMessage>".length();
		int end  = resp.indexOf("</errorMessage>");
		return resp.substring(start, end);
	}
	private String getErrorCode(String resp) {
		int start = resp.indexOf("<errorCode>")+"<errorCode>".length();
		int end  = resp.indexOf("</errorCode>");
		return resp.substring(start, end);
	}
	
	
	
	public boolean sendMT(MOSms mo) throws Exception{
		final String SEND_MT_1 = "insert into `"+CelcomImpl.database+"`.`httptosend`" +
				"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,CMP_TxID,split,serviceid,price,SMS_DataCodingId,mo_processorFK,billing_status,price_point_keyword,subscription) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE billing_status=?, re_tries=re_tries+1";

		return sendMT(mo,SEND_MT_1);
	}
	
	public long generateNextTxId(){
		try {
			Thread.sleep(112);
		} catch (InterruptedException e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return System.currentTimeMillis();
	}

}
