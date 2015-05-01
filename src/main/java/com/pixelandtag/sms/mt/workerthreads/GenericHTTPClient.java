package com.pixelandtag.sms.mt.workerthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.producerthreads.MTProducer;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class GenericHTTPClient implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6923599491151170899L;
	private Logger logger = Logger.getLogger(getClass());
	private HttpClient httpclient = null;
	private String name;
	private StopWatch watch;
	private String respose_msg = "";
	private boolean run = true;
	private volatile static ThreadSafeClientConnManager cm;
	private volatile boolean success = true;
	private boolean finished = false;
	private boolean busy = false;

	
	
	public GenericHTTPClient(){
		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
	    cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(10);
		cm.setMaxTotal(10);//http connections that are equal to the worker threads.
		httpclient = new DefaultHttpClient(cm);
		init();
	}
	
	public GenericHTTPClient(HttpClient httpclient_){
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
		HttpResponse response = null;
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
			
		}
		return resp_code;
	}
	
	
	private synchronized void setBusy(boolean busy) {
		this.busy = busy;
		notify();
	}
	
	private void printHeader(HttpPost httppost) {
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

	private void logAllParams(List<NameValuePair> params) {
		
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

	private void setFinished(boolean finished) {
		this.finished = finished;
	}

	private Vector<String> splitText(String input){
  		int maxSize = 136;
		Vector<String> ret=new Vector<String>();
  		
  		while(true){
  			input=input.trim();
  			if (input.length()<=maxSize){
  				ret.add(input);
  				break;
  			}
  			int pos=maxSize;
  			
            while(input.charAt(pos)!=' ' && input.charAt(pos)!='\n')
  				pos--;
  			String tmp=input.substring(0,pos);
  			ret.add(tmp);
  			input=input.substring(pos);
  			
  		}
  		return ret;
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
	
	
	
}
