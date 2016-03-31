package com.pixelandtag.cmp.ejb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.net.ssl.SSLContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ServiceNotLinkedToOpcoException;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.ProcessorType;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dao.generic.GenericDAO;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.SuccessfullyBillingRequests;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.StopWatch;

@Stateless
@Remote
public class BaseEntityBean implements BaseEntityI {
	
	private Logger logger = Logger.getLogger(BaseEntityBean.class);
	private String server_tz = "-04:00";//TODO externalize
	private String client_tz = "+03:00";//TODO externalize
	private String mtUrl = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";//TODO externalize this
	private StopWatch watch;
	private int recursiveCounter = 0;
	private SSLContextBuilder builder = new SSLContextBuilder();
	private static PoolingHttpClientConnectionManager cm;
	private CloseableHttpClient httpclient = null;
	private TrustSelfSignedStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy(){
		@Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
		
	};
	
	
	@EJB
	private CMPResourceBeanRemote cmp_ejb;
	
	@EJB
	private TimezoneConverterI timeZoneEjb;
	
	@EJB
	private SubscriptionBeanI subscriptionEjb;
	
	@EJB
	private OpcoSenderProfileEJBI opcosenderprofileEJB;
	
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	@EJB
	private QueueProcessorEJBI queueprocEJB;
	
	@EJB
	private OpcoSMSServiceEJBI opcosmsserviceejb;
	
	@Override
	public OpcoSenderReceiverProfile getopcosenderProfileFromOpcoId(Long opcoid){
		return opcosenderprofileEJB.getActiveProfileForOpco(opcoid);
	}
	
    
	@SuppressWarnings("unchecked")
	public ServiceProcessorDTO getServiceProcessor(Long processor_id_fk) throws Exception{

		ServiceProcessorDTO service = null;
		
		try {
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`mo_processors` WHERE `id`=?";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, processor_id_fk);
			
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o : rs){
				
				service = new ServiceProcessorDTO();
				
				service.setId((Integer) o[0] );//rs.getInt("id"));//0
				service.setServiceName((String) o[1] );//rs.getString("ServiceName"));//1
				service.setShortcode((String) o[2] );//rs.getString("shortcode"));//2
				service.setThreads((Integer) o[3] );//rs.getInt("threads"));//3
				service.setProcessorClass((String) o[4] );//rs.getString("ProcessorClass"));//4
				service.setActive(((Boolean) o[5]));//rs.getBoolean("enabled"));//5
				service.setClass_status((String) o[6] );//rs.getString("class_status"));//6
				service.setForwarding_url((o[8]!=null ? (String) o[8] : ""));
				service.setProcessor_type(ProcessorType.fromString((String)o[9]));
				//private String protocol;
				//private Long smppid;
				service.setProtocol((String) o[10] );
				if((o[10])!=null)
					service.setSmppid((  ((BigInteger) o[11]) ).longValue());
				service.setServKey(service.getProcessorClassName()+"_"+service.getId()+"_"+service.getShortcode());
				
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		return service;
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void createSuccesBillRec(Billable billable){
    	try{
    		
    		SuccessfullyBillingRequests successfulBill = new SuccessfullyBillingRequests();
    		successfulBill.setCp_tx_id(billable.getCp_tx_id());
    		successfulBill.setKeyword(billable.getKeyword());
    		successfulBill.setMsisdn(billable.getMsisdn());
    		successfulBill.setOperation(billable.getOperation());
    		successfulBill.setPrice(billable.getPrice());
    		successfulBill.setPricePointKeyword(billable.getPricePointKeyword());
    		successfulBill.setResp_status_code(billable.getResp_status_code());
    		successfulBill.setShortcode(billable.getShortcode());
    		successfulBill.setSuccess(billable.getSuccess());
    		successfulBill.setTimeStamp(billable.getTimeStamp());
    		successfulBill.setTransactionId(billable.getTransactionId());
    		successfulBill.setTransferin(billable.getTransferIn());
    		successfulBill.setOpco(em.merge(billable.getOpco()));
    		successfulBill = em.merge(successfulBill);
    		
    	}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
    }
    

	@Override
	public void  mimicMO(String keyword, String msisdn, OperatorCountry operatorCountry){
		
		try {
			
			SMSService smsserv = getSMSService(keyword,operatorCountry);
			
			MOProcessor proc = smsserv.getMoprocessor();
			
			IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
			incomingsms.setMsisdn(msisdn);
			incomingsms.setServiceid(smsserv.getId());
			incomingsms.setSms(smsserv.getCmd());
			incomingsms.setShortcode(proc.getShortcode());
			incomingsms.setPrice(smsserv.getPrice());
			incomingsms.setCmp_tx_id(generateNextTxId());
			incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
			incomingsms.setServiceid(smsserv.getId());
			incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
			incomingsms.setMoprocessor(proc);  
			incomingsms.setOpco(operatorCountry);
			logger.info("\n\n\n\n\n::::::::::::::::processor_fk.intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");

			logMO(incomingsms);
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
	}
	
	
	public boolean changeStatusIfSubscribed(String msisdn, List<String> services, SubscriptionStatus status) {
		
		boolean success = false;
		
		if(msisdn==null || services==null || services.size()<1 )
			return false;
		
		try{
			
			for(String kwd: services){
				Subscription subscription = subscriptionEjb.getSubscription(msisdn, kwd);
				if(subscription!=null){
					subscription.setSubscription_status(status);
					cmp_ejb.saveOrUpdate(subscription);
				}
				
			}
		
		}catch(Exception exp){
			
			logger.error(exp.getMessage(),exp);
		
		}
		
		return success;
	}



	


	@Override
    public boolean hasAnyActiveSubscription(String msisdn, List<String> services, OperatorCountry opco) throws Exception{
		
    	boolean isAtive = false;
		
		if(msisdn==null || services==null || services.size()<1 )
			return false;
		
		StringBuffer sb = new StringBuffer();
		for(String kwd: services){
			OpcoSMSService opcosmsservice = opcosmsserviceejb.getOpcoSMSService(kwd, opco);
			SMSService smsservice = opcosmsservice.getSmsservice();
			boolean subvalid = subscriptionEjb.subscriptionValid(msisdn, smsservice.getId());
			sb.append("\n\n\t\t opcosmsservice.getPrice():: "+opcosmsservice.getPrice()+" opcosmsservice : "+opcosmsservice.getId());
			sb.append("\n\t\t subvalid :: "+subvalid+" msisdn: "+msisdn+" cmd:"+smsservice.getCmd());
			sb.append("\n\t\t (subvalid || opcosmsservice.getPrice().compareTo(BigDecimal.ZERO)<=0) :: "+((subvalid || opcosmsservice.getPrice().compareTo(BigDecimal.ZERO)<=0)));
			logger.debug(sb.toString());
			sb.setLength(0);
			if(subvalid){//  || opcosmsservice.getPrice().compareTo(BigDecimal.ZERO)<=0){
				return true;
			}
		}
		return isAtive;
	}
  
	public BaseEntityBean() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException{
		
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();

		watch = new StopWatch();
		builder.loadTrustMaterial(null, trustSelfSignedStrategy);
		
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	            return true;
	        }
	    }).build();
	 org.apache.http.conn.ssl.X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	 SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
	    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
	            .register("http", PlainConnectionSocketFactory.getSocketFactory())
	            .register("https", sslSocketFactory)
	            .build();
		cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(1);
		httpclient = HttpClientBuilder.create().setSslcontext( sslContext).setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();

	}

	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	
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


	public <T> T saveOrUpdate(T t) throws Exception{
		try{
			t = em.merge(t);
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
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
	@Override
	public <T> Collection<T> listAll(Class<T> entityClass) throws Exception{
		return em.createQuery("from "+entityClass.getSimpleName()).getResultList();
	}
	

	/**
	 * To statslog
	 */
	
	@Override
	public boolean toStatsLog(IncomingSMS incomingsms, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, incomingsms.getServiceid());
			qry.setParameter(2, incomingsms.getMsisdn());
			qry.setParameter(3, incomingsms.getCmp_tx_id());
			qry.setParameter(4, incomingsms.getPrice().doubleValue());
			qry.setParameter(5, incomingsms.getIsSubscription());
			
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return false;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}

	
	
	
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	

	
	public boolean sendMTSMPP(Long sppid,String msisdn,String shortcode,String sms,String mo_text,Integer priority) throws Exception{
		boolean success = false;
		try{
			String insertQ = "insert into "
					+ "ismpp.messages(smppid,msisdn,shortcode,content,priority,timestamp,received) "
					+ "VALUES(?, ?, ?, ?, ?, now(), ?);";
			Query qry = em.createNativeQuery(insertQ);
			qry.setParameter(1, sppid.intValue());
			qry.setParameter(2, msisdn);
			qry.setParameter(3, shortcode);
			qry.setParameter(4, sms);
			
			qry.setParameter(5, priority.intValue());
			qry.setParameter(6, mo_text);
			
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	
	/**
	 * Logs in smpp to send
	 */
	@Override
	public boolean sendMTSMPP(OutgoingSMS outgoingsms, Long sppid) throws Exception{
		boolean success = false;
		try{
			String insertQ = "insert into "
					+ "ismpp.messages(smppid,msisdn,shortcode,content,priority,timestamp,received) "
					+ "VALUES(?, ?, ?, ?, 0, now(), ?);";
			Query qry = em.createNativeQuery(insertQ);
			qry.setParameter(1, sppid.intValue());
			qry.setParameter(2, outgoingsms.getMsisdn());
			qry.setParameter(3, outgoingsms.getShortcode());
			qry.setParameter(4, outgoingsms.getSms());
			
			qry.setParameter(5, "");
			
			int num =  qry.executeUpdate();
			success = num>0;
		
		}catch(Exception e){
			logger.error(e.getMessage(),e);
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
		
		CloseableHttpResponse response = null;
		
		try {
			
			String usernamePassword = "CONTENT360_KE" + ":" + "4ecf#hjsan7"; // Username and password will be provided by TWSS Admin
			String encoding = null;
			sun.misc.BASE64Encoder encoder = (sun.misc.BASE64Encoder) Class.forName( "sun.misc.BASE64Encoder" ).newInstance(); 
			encoding = encoder.encode( usernamePassword.getBytes() ); 
			httsppost.setHeader("Authorization", "Basic " + encoding);
			httsppost.setHeader("SOAPAction","");
			httsppost.setHeader("Content-Type","text/xml; charset=utf-8");
			String xml = billable.getChargeXML(BillableI.plainchargeXML);
			//billable.gets
			StringEntity se = new StringEntity(xml);
			httsppost.setEntity(se);
			
			
			watch.start();
			response = httpclient.execute(httsppost);
			watch.stop();
			logger.info("billable.getMsisdn()="+billable.getMsisdn()+" :::: Shortcode="+billable.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to bill via HTTP");
				
			 
			 final int RESP_CODE = response.getStatusLine().getStatusCode();
			 
			 resEntity = response.getEntity();
			 
			 String resp = convertStreamToString(resEntity.getContent());
			
			 logger.info("RESP CODE : "+RESP_CODE);
			 logger.info("RESP XML : "+resp);
			 
			
			
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
					
					if(resp.toUpperCase().contains("Insufficient".toUpperCase())){
						subscriptionEjb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),-1, billable.getOpco());
					}
					try{
						String transactionId = getTransactionId(resp);
						billable.setTransactionId(transactionId);
					}catch(Exception exp){
						logger.warn("No transaction id found");
					}
					
				}else{
					String transactionId = getTransactionId(resp);
					billable.setTransactionId(transactionId);
					billable.setResp_status_code("Success");
					logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
					logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					
					subscriptionEjb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),1, billable.getOpco());
					cmp_ejb.createSuccesBillRec(billable);
					
					
					
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
				
				logger.error(ioe.getMessage(), ioe);
				
				success  = false;
				
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
				
				try {
					response.close();
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
				
				logger.debug("DONE! ");
				
			}
		
		
		return billable;
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
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.CelcomHTTPAPI#logMO(com.pixelandtag.MO)
	 */
	
	@Override
	public IncomingSMS logMO(IncomingSMS incomingsms) { 
		return processorEJB.processMo(incomingsms);
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
	public SMSService getSMSService(String cmd, OperatorCountry opco)  throws Exception{
		SMSService sub = null;
		try{
			OpcoSMSService opcosmsservice = opcosmsserviceejb.getOpcoSMSService(cmd, opco);
			sub = opcosmsservice.getSmsservice();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
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
	private String getTransactionId(String resp) {
		int start = resp.indexOf("<transactionId>")+"<transactionId>".length();
		int end  = resp.indexOf("</transactionId>");
		return resp.substring(start, end);
	}
	
	
	public String generateNextTxId(){
		try {
			Thread.sleep(3);
		} catch (Exception e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return String.valueOf(System.nanoTime());
	}


	@Override
	public OutgoingSMS sendMT(OutgoingSMS outgoingsms) throws Exception {
		return queueprocEJB.saveOrUpdate(outgoingsms);
	}


	@Override
	public OpcoSMSService getOpcoSMSService(Long serviceid, OperatorCountry opco) throws  ServiceNotLinkedToOpcoException {
		return opcosmsserviceejb.getOpcoSMSService(serviceid, opco); 
	}


	
	
}
