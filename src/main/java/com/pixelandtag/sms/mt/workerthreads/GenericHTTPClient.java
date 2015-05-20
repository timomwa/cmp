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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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

import com.inmobia.util.StopWatch;
import com.pixelandtag.sms.producerthreads.MTProducer;

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
	private String respose_msg = "";
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
	public int call(GenericHTTPParam genericparams){
		this.respose_msg = "";
		this.success = true;
		int resp_code = 0;
		setBusy(true);
		HttpPost httppost = null;
		CloseableHttpResponse response = null;
		try {
			httppost = new HttpPost(genericparams.getUrl());
			
			Map<String,String> headerparams = genericparams.getHeaderParams();
			
			if(headerparams!=null && headerparams.size()>0)
				for(String key : headerparams.keySet()){
					httppost.setHeader(key, headerparams.get(key));
				}
			
			if(genericparams.getStringentity()==null || genericparams.getStringentity().isEmpty()){
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(genericparams.getHttpParams(), "UTF-8");
				httppost.setEntity(entity);
			}else{
				StringEntity se = new StringEntity(genericparams.getStringentity());
				httppost.setEntity(se);
			}
			watch.start();
			response = httpclient.execute(httppost);
			watch.stop();
			logger.info(getName()+" :::: LINK_LATENCY : ("+genericparams.getUrl()+")::::::::::  "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			watch.reset();
			
			resp_code = response.getStatusLine().getStatusCode();
			
			setBusy(false);
			
			this.respose_msg  = convertStreamToString(response.getEntity().getContent());
			
			logger.info(getName()+" PROXY ::::::: finished attempt to deliver SMS via HTTP :::: RESP::: "+respose_msg);
			try {
				
				EntityUtils.consume(response.getEntity());
			
			} catch (Exception e) {
				
				logger.error(e);
			
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
			httppost.abort();
			this.success = false;
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			httppost.abort();
			this.success = false;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			httppost.abort();
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
		return resp_code;
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
		
		logger.debug(">>>>>>>>>>>>>>>>> ||||||||||||| MEM_USAGE: " + MTProducer.getMemoryUsage()+" |||||||||||||||| <<<<<<<<<<<<<<<<<<<<<<<< ");
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

	public String getRespose_msg() {
		return respose_msg;
	}

	public void setRespose_msg(String respose_msg) {
		this.respose_msg = respose_msg;
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
		}catch(Exception e){}
		try{
			if(cm!=null)
				cm.shutdown();
			cm = null;
		}catch(Exception e){}
	}
	
}
