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
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.BillableI;
import com.pixelandtag.sms.producerthreads.BillingService;
import com.pixelandtag.sms.producerthreads.MTProducer;
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
		cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(1);
		httpsclient = HttpClientBuilder.create().setSSLSocketFactory(sf).setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
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

	private String mtUrl = "https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1";
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
		
		String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
		 Properties props = new Properties();
		 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		 props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		 props.put(Context.SECURITY_PRINCIPAL, "testuser");
		 props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		 props.put("jboss.naming.client.ejb.context", true);
		 context = new InitialContext(props);
		 this.cmp_ejb  =  (CMPResourceBeanRemote) 
      		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		
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
			
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+MTProducer.getMemoryUsage() +" >> "+e.getMessage(),e);
			
			
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
		
		httsppost = new HttpPost(this.mtUrl);

		HttpEntity resEntity = null;
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
			logger.debug("BILLABLE: "+billable.toString());
			logger.info("XML SENT \n : "+xml + "\n");
			StringEntity se = new StringEntity(xml);
			httsppost.setEntity(se);
			
			
			watch.start();
			response = httpsclient.execute(httsppost);
			watch.stop();
			logger.debug("billable.getMsisdn()="+billable.getMsisdn()+" :::: Shortcode="+billable.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to bill via HTTP");
				
			 
			 final int RESP_CODE = response.getStatusLine().getStatusCode();
			 
			 resEntity = response.getEntity();
			 
			 String resp = convertStreamToString(resEntity.getContent());
			
			 logger.debug("RESP CODE : "+RESP_CODE);
			 logger.debug("RESP XML : "+resp);
			 
			 billable.setResp_status_code(String.valueOf(RESP_CODE));
			
			
			billable.setProcessed(1L);
			if (RESP_CODE == HttpStatus.SC_OK) {
				
				billable.setRetry_count(billable.getRetry_count()+1);
				Boolean success = resp.toUpperCase().split("<STATUS>")[1].startsWith("SUCCESS");
				billable.setSuccess(success );
				
				if(!success.booleanValue()){
					
					String err = getErrorCode(resp);
					String errMsg = getErrorMessage(resp);
					logger.debug("resp: :::::::::::::::::::::::::::::ERROR_CODE["+err+"]:::::::::::::::::::::: resp:");
					logger.debug("resp: :::::::::::::::::::::::::::::ERROR_MESSAGE["+errMsg+"]:::::::::::::::::::::: resp:");
					logger.info("FAILED TO BILL ERROR="+err+", ERROR_MESSAGE="+errMsg+" msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
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
					cmp_ejb.createSuccesBillRec(billable);
					logger.debug("resp: :::::::::::::::::::::::::::::SUCCESS["+billable.isSuccess()+"]:::::::::::::::::::::: resp:");
					logger.info("SUCCESS BILLING msisdn="+billable.getMsisdn()+" price="+billable.getPrice()+" pricepoint keyword="+billable.getPricePointKeyword()+" operation="+billable.getOperation());
					
					
					
				}
				
								
			}else if(RESP_CODE == 400){
				
				
			}else if(RESP_CODE == 401){
				
				logger.error("\nUnauthorized!");
				
			}else if(RESP_CODE == 404 || RESP_CODE == 403){
				
			}else if(RESP_CODE == 503){

			}else{
				
			}
			
					
			
		}catch(ConnectException ce){
				
				//this.success  = false;
				
				message = ce.getMessage();
				
				logger.error(message, ce);
				
				httsppost.abort();
				
			
		}catch(SocketTimeoutException se){
				
			message = se.getMessage();
			
			httsppost.abort();
			
			logger.error(message, se);
				
			
		} catch (IOException ioe) {
				
				
			message = ioe.getMessage();
			
			httsppost.abort();
			
			logger.error("\n\n==============================================================\n\n"+message+" CONNECTION TO OPERATOR FAILED. WE SHALL TRY AGAIN. Re-tries so far "+recursiveCounter+"\n\n==============================================================\n\n");
				
			
		} catch (Exception ioe) {
				
			message = ioe.getMessage();
		
			httsppost.abort();
			
			logger.error(message, ioe);
				
			
		} finally{
				
			watch.reset();
				
			setBusy(false);
				
			logger.debug(getName()+" ::::::: finished attempt to bill via HTTP");
				
			// When HttpClient instance is no longer needed,
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
			
			try {
			
				if(resEntity!=null)
					EntityUtils.consume(resEntity);
				
			} catch (Exception e) {
				
				log(e);
				
			}
				
				
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
					
				cmp_ejb.saveOrUpdate(billable);
					
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
			
			try {
				response.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
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
		
		logger.debug(">>>>>>>>>>>>>>>>> ||||||||||||| MEM_USAGE: " + MTProducer.getMemoryUsage()+" |||||||||||||||| <<<<<<<<<<<<<<<<<<<<<<<< ");
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
