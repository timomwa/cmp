package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
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
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.smssenders.SenderResp;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.util.StopWatch;

public class HttpBillingWorker implements Runnable {
	
	private Logger logger = Logger.getLogger(HttpBillingWorker.class);
	private CloseableHttpClient httpsclient;
	private  Context context;
	private StopWatch watch;
	private boolean run = true;
	private boolean finished = false;
	private String name;
	private boolean busy = false;
	//private List<NameValuePair> qparams = null;
	private String message = "";
	private HttpPost httsppost = null;
	private int recursiveCounter = 0;
	private SSLContextBuilder builder = new SSLContextBuilder();
	private PoolingHttpClientConnectionManager cm;
	private SubscriptionBeanI subscriptionejb;
	private BillingGatewayEJBI billingGatewyEJB;
	private Properties mtsenderprop;
	private TrustSelfSignedStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy(){
		@Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
		
	};
	
    private void initHttpClient() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    	RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
    	builder.loadTrustMaterial(null, trustSelfSignedStrategy);
		SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(builder.build());
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
		httpsclient = HttpClientBuilder.create().setSslcontext( sslContext).setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
	}
    
    
	public boolean isBusy() {
		return busy;
	}

	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		notify();
	}

	public boolean isFinished() {
		return finished;
	}

	private void setFinished(boolean finished) {
		this.finished = finished;
	}

	private CMPResourceBeanRemote cmp_ejb;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRunning() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
	public synchronized void rezume(){
		this.notify();
	}
	
	public synchronized void pauze(){
		try {
			
			this.wait();
		
		} catch (InterruptedException e) {
			
			logger.debug(getName()+" we now run!");
		
		}
	}

	public HttpBillingWorker(String server_tz,String client_tz, String name_, CMPResourceBeanRemote cmpbean) throws Exception{
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
		 props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
		 props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 this.cmp_ejb  =  (CMPResourceBeanRemote) 
      		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		 
		 this.subscriptionejb =  (SubscriptionBeanI) 
		      		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		 
		 
		 this.billingGatewyEJB  = (BillingGatewayEJBI) 
		      		context.lookup("cmp/BillingGatewayEJBImpl!com.pixelandtag.cmp.ejb.api.billing.BillingGatewayEJBI");
		
		 this.watch = new StopWatch();
		
		
		 this.name = name_;
		
		watch.start();
		
		//qparams = new LinkedList<NameValuePair>();
		
		initHttpClient();
  
	}


	

	public void run() {
		
		try{
			
			pauze();//wait while producer gets ready
			
			watch.stop();
			
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			
			watch.reset();
			
			while(run){
				
				try {
					
					Billable billable = BillingService.getBillable();
					
					if(billable!=null){
						if(billable.getId()>-1){
							logger.debug(":the service id in worker!::::: mtsms.getServiceID():: "+billable.toString());
							charge(billable);
						}else{
							setRun(false);//poison pill
						}
					}else{
						try{
							Thread.sleep(10000);//Wait 10 seconds before trying again
						}catch(Exception esp){}
					}
					
					
				}catch (Exception e){
					
					log(e);
					
				}finally{
					
					setFinished(true);
					
					setBusy(false);
				
				}
				
			}
			
			logger.info(getName()+": worker shut down safely!");
		
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			
			
		}catch(OutOfMemoryError e){
			
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+OutgoingQueueRouter.getMemoryUsage() +" >> "+e.getMessage(),e);
			
			
		}finally{
			
		    if(context!=null) 
		    	try { 
		    		context.close(); 
		    	}catch(Exception ex) { ex.printStackTrace(); }
		    
		    finalizeMe();
		    
		} 
		
	}
	
	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	/**
	 * Sends the MT message
	 * @param billable - com.pixelandtag.MTsms
	 */
	private void charge(Billable  billable){
		
		setBusy(true);
		SenderResp response = null;
		try {
			
			watch.start();
			response = billingGatewyEJB.bill(billable);
			watch.stop();
			logger.debug("billable.getMsisdn()="+billable.getMsisdn()+" :::: Shortcode="+billable.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to bill via HTTP");
				
			 
			 final int RESP_CODE = Integer.valueOf(response.getRespcode());
			 
			 billable.setResp_status_code(response.getRespcode());
			
			
			billable.setProcessed(1L);
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				billable.setRetry_count(billable.getRetry_count()+1);
				billable.setSuccess( response.getSuccess() );
				
				if(!response.getSuccess()){
					
					logger.info("FAILED TO BILL, ERROR_MESSAGE="+response.getResponseMsg()+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					try{
						billable.setTransactionId(response.getRefvalue());
					}catch(Exception exp){
						logger.warn("No transaction id found");
					}
					if(response.getResponseMsg().toUpperCase().contains("Insufficient".toUpperCase())){
						try{
							subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),-1, billable.getOpco());
						}catch(NumberFormatException nfe){
							logger.warn("Number format exception. billable.getService_id() : "+billable.getService_id()+ " billable : "+billable.toString());
						}
					}
				}else{
					billable.setTransactionId(response.getRefvalue());
					billable.setResp_status_code("Success");
					cmp_ejb.createSuccesBillRec(billable);
					logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
					logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					
					subscriptionejb.updateCredibilityIndex(billable.getMsisdn(),Long.valueOf(billable.getService_id()),1, billable.getOpco());
					
					
					
				}
				
								
			}else if(RESP_CODE == 400){
				
				logger.error("\nResp 400");
			}else if(RESP_CODE == 401){
				
				logger.error("\nUnauthorized!");
				
			}else if(RESP_CODE == 404 || RESP_CODE == 403){
				
			}else if(RESP_CODE == 503){

			}else{
				
			}
			
					
			
		} catch (Exception ioe) {
				
			logger.error(message, ioe);
			message = ioe.getMessage();
			httsppost.abort();
			
			
				
			
		} finally{
				
			watch.reset();
				
			setBusy(false);
				
			logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
				
			// When HttpClient instance is no longer needed,
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
				
			try{
					
				billable.setProcessed(1L);
				billable.setIn_outgoing_queue(0L);
					
				if(billable.isSuccess() ||  "Success".equals(billable.getResp_status_code()) ){
					cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.SUCCESSFULLY_BILLED);
					cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.Success);
					billable.setResp_status_code(BillingStatus.SUCCESSFULLY_BILLED.toString());
				}
				if("TWSS_101".equals(billable.getResp_status_code()) || "TWSS_114".equals(billable.getResp_status_code()) || "TWSS_101".equals(billable.getResp_status_code())){
					cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
					cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.InvalidSubscriber);
					billable.setResp_status_code(BillingStatus.BILLING_FAILED_PERMANENTLY.toString());
				}
				if("OL402".equals(billable.getResp_status_code()) || "OL404".equals(billable.getResp_status_code()) || "OL405".equals(billable.getResp_status_code())  || "OL406".equals(billable.getResp_status_code())){
					cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.INSUFFICIENT_FUNDS);
					cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAInsufficientBalance);
					billable.setResp_status_code(BillingStatus.INSUFFICIENT_FUNDS.toString());
				}
					
				if("TWSS_109".equals(billable.getResp_status_code())){
					cmp_ejb.updateSMSStatLog(billable.getCp_tx_id(),ERROR.PSAChargeFailure);
					billable.setIn_outgoing_queue(0L);
					billable.setProcessed(0L);
					billable.setRetry_count( (billable.getRetry_count()+1 ) );
					billable.setMaxRetriesAllowed(5L);
					billable.setResp_status_code(BillingStatus.BILLING_FAILED.toString());
				}
					
				subscriptionejb.saveOrUpdate(billable);
					
				if(billable.isSuccess()){//return back to queue if we did not succeed
					//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
					//cmp_ejb.updateMessageInQueue(billable.getCp_tx_id(),BillingStatus.BILLING_FAILED_PERMANENTLY);
						
					//on third try, we abort
					httsppost.abort();
						
						
				}else{
						
					recursiveCounter = 0;
				}
				
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
				
			watch.reset();
			
			logger.debug("DONE! ");
				
		}
	
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

	public void printHeader() {
		logger.debug("\n===================HEADER=========================\n");
	
	try{
			for(org.apache.http.Header h : httsppost.getAllHeaders()){
			
			if(h!=null){
				
				logger.debug("name: "+h.getName());
				logger.debug("value: "+h.getValue());
				
				
				for(org.apache.http.HeaderElement hl : h.getElements()){
					if(hl!=null){
						logger.debug("\tname: "+hl.getName());
						logger.debug("\tvalue: "+hl.getValue());
						
						if(hl.getParameters()!=null)
						for(NameValuePair nvp : hl.getParameters()){
							if(nvp!=null){
								logger.debug("\t\tname: "+nvp.getName());
								logger.debug("\t\tvalue: "+nvp.getValue());
							}
						}
					}
					
				}
			}
		}
		
	}catch(Exception e){
		
		logger.warn(e.getMessage(),e);
	}
	
	logger.debug("\n===================HEADER END======================\n");
		
	}

	public void logAllParams(List<NameValuePair> params) {
		
		for(NameValuePair np: params){
			
			if(np.getName().equals("SMS_MsgTxt"))
				logger.debug(np.getName()+ "=" + np.getValue()+" Length="+np.getValue().length());
			else
				logger.debug(np.getName() + "=" + np.getValue());
			
		}
		
		logger.debug(">>>>>>>>>>>>>>>>> ||||||||||||| MEM_USAGE: " + OutgoingQueueRouter.getMemoryUsage()+" |||||||||||||||| <<<<<<<<<<<<<<<<<<<<<<<< ");
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
	
	
	public void finalizeMe(){
		try{
			if(cm!=null)
				cm.shutdown();
		}catch(Exception e){}
	}

}
