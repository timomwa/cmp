package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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

import com.inmobia.util.StopWatch;
import com.pixelandtag.cmp.entities.audittools.LatencyLog;
import com.pixelandtag.cmp.entities.audittools.LatencyLogBuilder;
import com.pixelandtag.sms.core.OutgoingQueueRouter;

public class GenericHTTPClient implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6923599491151170899L;
	private Logger logger = Logger.getLogger(getClass());
	private CloseableHttpClient httpclient = null;
	private SSLContextBuilder builder = new SSLContextBuilder();
	private String name;
	private StopWatch watch;
	private boolean run = true;
	private static PoolingHttpClientConnectionManager cm;
	private volatile boolean success = true;
	private boolean finished = false;
	private boolean busy = false;
	private String protocol;
	private RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
	private TrustSelfSignedStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy(){
		@Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
		
	};

	@SuppressWarnings("unused")
	private GenericHTTPClient(){}
	
	public GenericHTTPClient(String proto) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException{
		this.protocol = proto;
		initHttpClient();
		init();
	}
	
	/**
	 * When we've been throttled
	 * we release all resources, close
	 * the connection and client
	 */
	public void releaseConnection(){
		finalizeMe();
	}
	
	
	public void initHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		if(protocol.trim().equalsIgnoreCase("http")){
			
			cm = new PoolingHttpClientConnectionManager();
			cm.setDefaultMaxPerRoute(1);
			cm.setMaxTotal(1);
			httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
		
		}else{
			
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
			 builder.loadTrustMaterial(null, trustSelfSignedStrategy);
			 SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(
			            builder.build());
			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			cm.setDefaultMaxPerRoute(1);
			cm.setMaxTotal(1);
			httpclient = HttpClientBuilder.create().setSslcontext( sslContext).setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
		}
		
	}
	

	public GenericHTTPClient(CloseableHttpClient httpclient_){
		this.httpclient = httpclient_;
		init();
	}
	
	

	private void init(){
		
		this.watch = new StopWatch();
		watch.start();
		
	}
	
	
	/**
	 * Sends the MT message
	 * @param mt - com.pixelandtag.MTsms
	 */
	public GenericHttpResp call(GenericHTTPParam genericparams){
		this.success = true;
		GenericHttpRespBuilder respBuilder = GenericHttpRespBuilder.create();
		setBusy(true);
		HttpRequestBase httpmethod = null;
		CloseableHttpResponse response = null;
		try {
			
			httpmethod = HttpMethodFactory.getMethod(genericparams);
			
			Map<String,String> headerparams = genericparams.getHeaderParams();
			
			if(headerparams!=null && headerparams.size()>0)
				for(String key : headerparams.keySet()){
					httpmethod.setHeader(key, headerparams.get(key));
				}
			
			if(genericparams.getStringentity()==null || genericparams.getStringentity().isEmpty()){
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(genericparams.getHttpParams(), "UTF-8");
				HttpEntityEnclosingRequestBase method = (HttpEntityEnclosingRequestBase) httpmethod;
				method.setEntity(entity);
				watch.start();
				response = httpclient.execute(method);
				watch.stop();
				httpmethod = (HttpRequestBase) method;
			}else if(genericparams.getStringentity()!=null){
				StringEntity se = new StringEntity(genericparams.getStringentity());
				HttpEntityEnclosingRequestBase method = (HttpEntityEnclosingRequestBase) httpmethod;
				method.setEntity(se);
				watch.start();
				response = httpclient.execute(method);
				watch.stop();
				httpmethod = (HttpRequestBase) method;
			}
			
			try{
				String link = genericparams.getUrl();
				Long latency = watch.elapsedTime(TimeUnit.MILLISECONDS);
				LatencyLog latencyLog = LatencyLogBuilder.create().latency(latency).link(link).build();
				respBuilder.latencyLog(latencyLog);
				logger.debug(getName()+" :::: LINK_LATENCY : ("+link+")::::::::::  "+ latency.longValue() + " mili-seconds");
				
				Header[] headers = response.getAllHeaders();
				if(headers!=null)
					for(int i=0;i<headers.length; i++){
						Header header = headers[i];
						if(header.getName().toLowerCase().contains("content")){
							respBuilder.contenttype(header.getValue());
						}
					}
			}catch(Exception exp){
				logger.error(exp.getMessage()+genericparams.getUrl(), exp);
			}
			watch.reset();
			
			int resp_code = response.getStatusLine().getStatusCode();
			respBuilder = respBuilder.respCode(resp_code);
			
			setBusy(false);
			
			String respose_msg  = convertStreamToString(response.getEntity().getContent());
			respBuilder.body(respose_msg);
			logger.debug(getName()+" PROXY ::::::: finished attempt to deliver SMS via HTTP :::: RESP::: "+respose_msg);
			try {
				
				EntityUtils.consume(response.getEntity());
			
			} catch (Exception e) {
				
				logger.error(e);
			
			}
		}catch(java.lang.IllegalStateException ise){
			try {
				initHttpClient();
			} catch (KeyManagementException e) {
				logger.error(e.getMessage(),e);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(),e);
			} catch (KeyStoreException e) {
				logger.error(e.getMessage(),e);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
			httpmethod.abort();
			this.success = false;
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			httpmethod.abort();
			this.success = false;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			httpmethod.abort();
			this.success = false;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			httpmethod.abort();
			this.success = false;
		}finally{
			setBusy(false);
			try {
				if(response!=null)
					response.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return respBuilder.build();
	}
	
	
	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		notify();
	}
	
	public void printHeader(HttpPost httppost) {
		logger.debug("\n===================HEADER=========================\n");
		try{
			
			for(org.apache.http.Header h : httppost.getAllHeaders()){
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

	public boolean isRunning() {
		return run;
	}
	
	public void setRun(boolean run) {
		this.run = run;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isBusy() {
		return busy;
	}
	
	public boolean isFinished() {
		return finished;
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
	private String convertStreamToString(InputStream is)
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
	
	/**
	 * Releases resources, never throws
	 * an exception
	 */
	public void finalizeMe(){
		try{
			if(httpclient!=null)
				httpclient.close();
			httpclient = null;
		}catch(Exception e){logger.error(e.getMessage(), e);}
		try{
			cm.close();
			if(cm!=null)
				cm.close();
		}catch(Exception e){logger.error(e.getMessage(), e);}
		try{
			if(cm!=null)
				cm.shutdown();
			cm = null;
		}catch(Exception e){logger.error(e.getMessage(), e);}
	}
	
}
