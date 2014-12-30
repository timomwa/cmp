package com.pixelandtag.subscription;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.SubscriptionLog;


public class SubscriptionWorker implements Runnable{
	
	private int serviceid;
	private String name;
	private String connstr;// = "jdbc:mysql://db/pixeland_content360?user=root&password=";
	private int subs = 0;
	private boolean busy;
	public static final int PROCESSING = 1;
	public static int BUSY = 2;
	public static int FINISHED = 0;
	public static int ERROR_OCCURRED = 4;
	private int status;
	private Logger logger = Logger.getLogger(SubscriptionWorker.class);
	private Connection conn;
	private int subscription_service_id = -1;
	private ArrayBlockingQueue<SubscriptionDTO> processors;
	private String server_tz;
	private String client_tz;
	private CMPResourceBeanRemote cmpbean;
	
	private SubscriptionWorker(){}
	
	public SubscriptionWorker(CMPResourceBeanRemote bean,String server_tz, String client_tz, String connStr, String name_,int service_id, int subscription_service_id_, ArrayBlockingQueue<SubscriptionDTO> processors){
		this.cmpbean = bean;
		this.server_tz = server_tz;
		this.client_tz = client_tz;
		logger.debug(" :::::::::: GUGAMUGA processing service : "+service_id);
		this.connstr = connStr;
		this.serviceid = service_id;
		this.name = name_;
		this.subs = 0;
		this.processors = processors;
		this.subscription_service_id = subscription_service_id_;
		logger.debug(" :::::::::: GUGAMUGA processors : "+processors);
	}
	
	
	private SubscriptionDTO getFreeDTOWithProcessor(){
		
		ServiceProcessorI processor = null;
		SubscriptionDTO dto = null;
		
		for(SubscriptionDTO p : processors){
			
			
			logger.debug(">>>> Getting free processor: "+p.toString());
			dto = p;
			processor = p.getProcessor();
			if(!processor.queueFull())
				break;
		}
			
		return dto;
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isBusy(){
		return this.busy;
	}
	
	private void setBusy(boolean busy_){
		this.busy = busy_;
	}
	
	public int getServiceid() {
		return serviceid;
	}


	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}


	@Override
	public void run() {
		
		
		
		try{
			
			logger.debug(getName() + " - started");
			
			setBusy(true);
			
			setStatus(BUSY);
			//TODO find a better way to process this. Probably process it in the bean instead
			//of carrying a big list of msisdn to this end..
			//Suggestion : make this one transaction in the EJB end
			List<com.pixelandtag.sms.producerthreads.Subscription> msisdns = cmpbean.listServiceMSISDN("confirmed", this.serviceid);
			
			for(com.pixelandtag.sms.producerthreads.Subscription sub : msisdns){
				
				
				try{
					
					SubscriptionDTO dto = getFreeDTOWithProcessor();
					
					MOSms mo = new MOSms();
					
					mo.setCMP_Txid(SubscriptionMain.generateNextTxId());
					mo.setMsisdn(sub.getMsisdn());
					mo.setCMP_AKeyword(dto.getCMP_AKeyword());
					mo.setCMP_SKeyword(dto.getCMP_SKeyword());
					mo.setSMS_SourceAddr(dto.getShortcode());
					mo.setPrice(BigDecimal.valueOf(dto.getPrice()));
					mo.setPriority(1);
					mo.setServiceid(dto.getServiceid());
					mo.setSMS_Message_String(dto.getCmd());
					mo.setProcessor_id(dto.getProcessor_id());
					mo.setSubscriptionPush(true);//flag that this is a subscription push. Will help system not do some querying later. Save processor 
					mo.setPricePointKeyword(dto.getPricePointKeyword());
					mo.setSubscription_id(sub.getId());
					dto.getProcessor().submit(mo);//submit msg to the processor
					

					
					if(sub.getId()!=null && sub.getId().compareTo(0L)>0){
						SubscriptionLog slog = new SubscriptionLog();
						slog.setMsisdn(sub.getMsisdn());
						slog.setService_subscription_id(this.serviceid);
						slog.setSubscription_id(sub.getId());
						slog.setTimeStamp(new Date());
						try{
							cmpbean.saveOrUpdate(slog);
						}catch(Exception e){
							logger.error(e.getMessage(),e);
						}
					}
					
				}catch(Exception e){
					log(e);
				}
				
			}
			
			
			boolean success = cmpbean.updateServiceSubscription(this.subscription_service_id);
			System.out.println(" success? "+success);
			//Shut down  your own processor here.
			shutdownProcessors();
			
			setStatus(FINISHED);
			
		}catch(Exception e){
			
			setStatus(ERROR_OCCURRED);
			log(e);
		
		}finally{
			
			setBusy(false);
			
			finalizeMe();
			
		}
		
		logger.debug(":::::::::::::::::: "+getName()+": terminated:");
		
		
		
	}

	
	/**
	 * Shuts down all service processors safely, waiting for them to finish processing their queue
	 */
	private void shutdownProcessors() {
		
		Iterator<SubscriptionDTO> it = processors.iterator();
		
		SubscriptionDTO dto;
		
		while(it.hasNext()){
			
			dto = it.next();
			
			try{
				MOSms mo = new MOSms();
				mo.setCMP_Txid(-1);
				dto.getProcessor().submit(mo);
			}catch(Exception e){
				log(e);
			}
			
		}
		
		
		Iterator<SubscriptionDTO> it2 = processors.iterator();
		
		int x = 0;
		
		boolean somebusy = true;
		
		while(somebusy){
			somebusy = false;
			while(it2.hasNext()){
				
				x++;
				if(x==1)
					logger.debug("waiting");
				else
					logger.debug("...");
				dto = it2.next();
				
				
				logger.debug(" DTO:::: "+dto);
				try{
					//
					if(dto.getProcessor().isRunning() ){// && dto.getProcessor().getQueueSize()>0){
						somebusy = true;
					}else{
						dto.getProcessor().setRun(false);
						dto.getProcessor().finalizeMe();
						
					}
				}catch(Exception e){
					log(e);
				}
			}
		}
		
	}

	private void finalizeMe() {
		
		try {
			
			if(this.conn!=null)
			this.conn.close();
		
		} catch (Exception e) {
			log(e);
		}finally{
			logger.debug(getName() + " - finished");
		}
		
	}

	private void log(Exception e) {
		
		logger.error(e.getMessage(),e);
		
	}

	private Connection getConn() {
		
		try {
			if(conn !=null)
				if(!conn.isClosed() && isConnOK())
					return conn;
		} catch (Exception e1) {
			log(e1);
		}
		

		while( true ) {
			try {
				while ( conn==null || conn.isClosed() ) {
					try {
						conn = DriverManager.getConnection(this.connstr);
					} catch ( Exception e ) {
						logger.warn(e,e);
						try { Thread.sleep(500); } catch ( Exception ee ) {}
					}
				}
				return conn;
			} catch ( Exception e ) {
				logger.error(e,e);
				try { Thread.sleep(500); } catch ( Exception ee ) {}
			}
		}
	}

	private  boolean isConnOK() {

		if (this.conn == null)
			return false;

		Statement stmt = null;
		ResultSet rs = null;
		boolean connOK = false;

		try {

			stmt = this.conn.createStatement();
			rs = stmt.executeQuery("SELECT now()");

			if (rs.next()) {
				logger.debug("conn ok!");
				connOK = true;
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}

			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}

		}

		return connOK;

	}
	
	
	
	
	


}
