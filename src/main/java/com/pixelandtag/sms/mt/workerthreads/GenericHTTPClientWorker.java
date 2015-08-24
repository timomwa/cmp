package com.pixelandtag.sms.mt.workerthreads;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.inmobia.util.StopWatch;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.MTStatus;
import com.pixelandtag.bulksms.BulkSMSQueue;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.sms.core.OutgoingQueueRouter;
import com.pixelandtag.sms.producerthreads.BulkSMSProducer;

public class GenericHTTPClientWorker implements Runnable{
	
	private static final int msg_part_wait = 0;//Time to wait in mills before sending broken message
	private Logger logger = Logger.getLogger(getClass());
	private CloseableHttpClient httpclient = null;
	private String MINUS_ONE = "-1";
	private String name;
	private StopWatch watch;
	private boolean run = true;
	private volatile PoolingHttpClientConnectionManager cm;
	private volatile int sms_idx = 0;
	private CMPResourceBeanRemote cmpbean;
	private volatile boolean success = true;
	private boolean finished = false;
	private boolean busy = false;
	private volatile String message = "";

	private volatile int recursiveCounter = 0;
	
	
	public GenericHTTPClientWorker(CMPResourceBeanRemote cmpbean){
		this.cmpbean = cmpbean;
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
		cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(1);
		httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
		init();
	}
	
	public GenericHTTPClientWorker(CMPResourceBeanRemote cmpbean, CloseableHttpClient httpclient_){
		this.cmpbean = cmpbean;
		this.httpclient = httpclient_;
		init();
	}
	
	private synchronized void setSms_idx(int i) {
		this.sms_idx = i;
		notify();
	}

	private synchronized int getSms_idx() {
		return sms_idx;
		
	}

	
	
	@Override
	public void run() {
		
		
		try{
			
			pauze();//wait while producer gets ready
			
			watch.stop();
			logger.info(getName()+" STARTED AFTER :::::RELEASED_BY_PRODUCER after "+(Double.parseDouble(watch.elapsedTime(TimeUnit.MILLISECONDS)+"")) + " mili-seconds");
			watch.reset();
			
			
			StringBuffer sb = new StringBuffer();
		
		
			while(run){
				
				try{
					
					final GenericHTTPParam httpparams = BulkSMSProducer.getGenericHttp();
					
					final BulkSMSQueue bulksms = httpparams!=null ? httpparams.getBulktext() : null;
					
					
					if(bulksms!=null){
						if(bulksms.getId()>-1){
							try{
								
								final int RESP_CODE_  = call(httpparams);//send SMS the way it is
								
								if(RESP_CODE_==HttpStatus.SC_OK){
									bulksms.setStatus(MTStatus.SENT_SUCCESSFULLY);
								}else{
									bulksms.setStatus(MTStatus.FAILED_TEMPORARILY);
								}
								}catch(Exception exp){
									logger.error(exp.getMessage(),exp);
								}finally{
									try{
										cmpbean.saveOrUpdate(bulksms);
									}catch(Exception exp){
										logger.error(exp.getMessage(),exp);
									}
								}
							}else{
								setRun(false);// we assume it's a poison pill
							}
					}else{
						try{
							Thread.sleep(2500);//sleep 2.5 seconds
						}catch(Exception exp){
							
						}
					}
					
					
					final MTsms mtsms = httpparams!=null ? (httpparams.getMtsms()==null ? cmpbean.getMTsms(Long.valueOf(httpparams.getId()))  : httpparams.getMtsms()) : null;
					
					if(mtsms!=null){
						if(mtsms.getId()>-1){
							
							setSms_idx(0);//reset the sms index counter each time we get a brand new message to send.
							//String originalTXID = mtsms.getCMP_Txid();
							
							if(mtsms.getSms()!=null)
							if(mtsms.getSms().length()>140 && mtsms.isSplit_msg()){
							
								Vector<String> msg = splitText(mtsms.getSms());
								
								mtsms.setNumber_of_sms(msg.size());
								
								
								for(int x=0; x<msg.size();x++){
									
									mtsms.setMsg_part(sb.append((x+1)).append("/").append(msg.size()).append(" ").append(msg.get(x)).toString());
									
									sb.setLength(0);
									
									try{
										final int RESP_CODE  = call(httpparams);//send SMS the way it is
										updateStatus(RESP_CODE,httpparams,mtsms);
									}catch(Exception esp){
										logger.error(esp.getMessage(),esp);
									}finally{
										
										if(!this.success){//return back to queue if we did not succeed
											
											
											if((mtsms.getNumber_of_sms()>1) && run && (recursiveCounter<=3)){//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
												
												setSms_idx((getSms_idx()-1));//we did not send successfully, so it does not count!
												call(httpparams); //try to send re-cursively instead of putting it back in the queue..
												logger.warn("SENDING RE_CURSIVELY!!!!!!!!!!!!! we've been here "+recursiveCounter+ " more time(s) we'll give up on the 3rd try, and queue the message to the database");
											
												recursiveCounter++;
												
											}else{
												
												logger.info(message+" Putting back MT into queue! "+mtsms.toString());
												try {
													cmpbean.postponeMT(mtsms.getId());//If we timeout, we postpone the MT
												} catch (Exception e) {
													logger.error(e.getMessage(),e);
												}
											}
											
											
										}else{
											
											recursiveCounter = 0;
										}
									}
									
								}
								
							}else{
								
								mtsms.setNumber_of_sms(1);
								
								try{
									final int RESP_CODE  = call(httpparams);
									updateStatus(RESP_CODE,httpparams,mtsms);
								}catch(Exception esp){
									logger.error(esp.getMessage(),esp);
								}finally{
									
									if(!this.success){//return back to queue if we did not succeed
										
										
										if((mtsms.getNumber_of_sms()>1) && run && (recursiveCounter<=3)){//We only try 3 times recursively if we've not been poisoned and its one part of a multi-part message, we try to re-send, but no requeuing
											
											setSms_idx((getSms_idx()-1));//we did not send successfully, so it does not count!
											call(httpparams); //try to send re-cursively instead of putting it back in the queue..
											logger.warn("SENDING RE_CURSIVELY!!!!!!!!!!!!! we've been here "+recursiveCounter+ " more time(s) we'll give up on the 3rd try, and queue the message to the database");
										
											recursiveCounter++;
											
										}else{
											
											logger.info(message+" Putting back MT into queue! "+mtsms.toString());
											try {
												cmpbean.postponeMT(mtsms.getId());//If we timeout, we postpone the MT
											} catch (Exception e) {
												logger.error(e.getMessage(),e);
												e.printStackTrace();
											}
										}
										
										
									}else{
										
										recursiveCounter = 0;
										//logger.warn(message+" >>MESSAGE_NOT_SENT> "+mt.toString());
									}
								}
							}
						}else{
							setRun(false);//we assume it's a poison pill
						}
					}else{
						try{
							Thread.sleep(2500);//sleep 2.5 seconds
						}catch(Exception exp){
							
						}
					}
					
					
					
				} catch (InterruptedException e) {
					
					logger.error(e.getMessage(),e);
				
				}catch (Exception e){
					
					logger.error(e.getMessage(),e);
					
				}finally{
					
					setSms_idx(0);
				
				}
							
				
			}
			
			setFinished(true);
			
			setBusy(false);
			
			logger.info(getName()+": worker shut down safely!");
		
		}catch(OutOfMemoryError e){
			
			logger.fatal("NEEDS RESTART: MEM_USAGE: "+OutgoingQueueRouter.getMemoryUsage() +" >> "+e.getMessage(),e);
			//Hasn't happened so far during testing. Not expected to happen during runtime
			//please send alarm
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}finally{
		} 
				
	}
	
	private void updateStatus(int RESP_CODE, GenericHTTPParam httpparams, MTsms mt) throws Exception {
		
		if (RESP_CODE == HttpStatus.SC_OK) {
			
			mt.setCMPResponse(ERROR.Success.toString());
			
			if(getSms_idx()==mt.getNumber_of_sms()){//If we've sent ALL, then we delete the MT from queue, then do other logs.. 
				cmpbean.deleteMT(mt.getId());
				mt.setMT_STATUS(ERROR.WaitingForDLR.toString());
				cmpbean.logMT(mt);
				
			}else{
				logger.debug("PARANOIA: WAITING FOR "+msg_part_wait+" milliseconds before sending the next message segment");
				Thread.sleep(msg_part_wait);
			}
			
			if(mt.getNumber_of_sms()>1){
				cmpbean.logResponse(mt.getMsisdn(),mt.getMsg_part());//log each segment of an SMS..
			}else{
				cmpbean.logResponse(mt.getMsisdn(),mt.getSms());//log each segment of an SMS..
			}
				
			//TODO and postponed - introduce postpone functionality
			
			watch.stop();
			
			
			if(mt.getNewCMP_Txid()!=null)
				if(!mt.getNewCMP_Txid().equals(MINUS_ONE)){
					logger.info("mt.getSUB_R_Mobtel()="+mt.getSUB_R_Mobtel()+" ::::: MT_Sent="+mt.getSms()+":::: Shortcode="+mt.getShortcode()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to clear deliver SMS via HTTP");
				}else{
					logger.debug("RETRY__ mt.getSUB_R_Mobtel()="+mt.getSUB_R_Mobtel()+" ::::: MT_Sent="+mt.getSms()+":::: Shortcode="+mt.getSendFrom()+" :::< . >< . >< . >< . >< . it took "+(Double.valueOf(watch.elapsedTime(TimeUnit.MILLISECONDS)/1000d)) + " seconds to clear deliver SMS via HTTP");
				}
			
			
			this.success  = true;
			
		}else if(RESP_CODE == 400){
			
			mt.setMT_STATUS(ERROR.FailedToSend.toString());
			mt.setCMPResponse(ERROR.PCM400.toString());
			
			cmpbean.logMT(mt);//insert into msglog table
			
			logger.error("\nCP_Id is Null or blank"
			+"\nCP_UserId Null or blank"
			+"\nCP_Password Null or blank"
			+"\nCMP_A_Keyword =Null or blank"
			+"\nCMP_Txid = Null or blank"
			+"\nSMS_Msgdata is not Valid.");
			
			logAllParams(httpparams.getHttpParams());
			
			
			
		}else if(RESP_CODE == 401){
			
			mt.setCMPResponse(ERROR.ContentType_Is_Null.toString());
			
			mt.setMT_STATUS(ERROR.FailedToSend.toString());
			
			cmpbean.logMT(mt);//insert into msglog table
			
			logger.error("\nCMP_ContentType is Null or not defined as per CMP_ContentType in above table..");
			
		}else if(RESP_CODE == 404 || RESP_CODE == 403){
			
			
			mt.setCMPResponse(ERROR.PCM404.toString()); 
			
			mt.setMT_STATUS(ERROR.FailedToSend.toString());
			
			cmpbean.logMT(mt);//insert into msglog table
			
			logger.error("SMS_SourceAddr is Null or blank"
						+"\nSUB_R_Mobtel is Null or blank"
						+"\nSUB_C_Mobtel is Null or blank"
						+"\nSMS_Msgdata is Null for CMP_ContentType not equal to TM"
						+"\nSMS_Msgtxt is Null for CMP_ContentType equal to TM");
			
			logAllParams(httpparams.getHttpParams());
			
		}else if(RESP_CODE == 503){

			mt.setMT_STATUS(ERROR.FailedToSend.toString());
			
			mt.setCMPResponse(ERROR.SERVER_INTERNAL_QUEUE_FULL.toString());
			
			logger.error("CMP Server Internal Queue full for this Service. CP needs to try again later");
			
			this.success  = false;
			
			logAllParams(httpparams.getHttpParams());
			
		}else{
			
			this.success = false;
			
			logAllParams(httpparams.getHttpParams());
			
		}
		
		
	}
	
	
	

	private void init(){
		
		this.watch = new StopWatch();
		watch.start();
		
	}
	
	
	/**
	 * Sends the MT message
	 * @param mt - com.pixelandtag.MTsms
	 */
	private int call(GenericHTTPParam genericparams){
		this.success = true;
		setBusy(true);
		setSms_idx((getSms_idx()+1));
		int status = 200;
		HttpPost httppost = null;
		CloseableHttpResponse response = null;
		try {
			httppost = new HttpPost(genericparams.getUrl());
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(genericparams.getHttpParams(), "UTF-8");
			httppost.setEntity(entity);
			response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			printHeader(httppost);
			status =  response.getStatusLine().getStatusCode();
			watch.reset();
			
			setBusy(false);
			
			logger.debug(getName()+" ::::::: finished attempt to deliver SMS via HTTP");
			
			try {
				
				EntityUtils.consume(resEntity);
			
			} catch (IOException e) {
				
				logger.error(e.getMessage(),e);
			
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
			status = 500;
			httppost.abort();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			status = 500;
			httppost.abort();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			status = 500;
			httppost.abort();
		}finally{
			try {
				response.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return status;
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
	

	public void finalizeMe(){
		try{
			if(cm!=null)
				cm.shutdown();
		}catch(Exception e){}
	}
	
}
