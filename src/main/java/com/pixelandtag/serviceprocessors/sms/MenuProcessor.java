package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.web.beans.RequestObject;

public class MenuProcessor extends GenericServiceProcessor{

	private final Logger logger = Logger.getLogger(MenuProcessor.class);
	private DBPoolDataSource ds;
	
	
	public MenuProcessor() throws Exception{
		
	}
    

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		Connection conn = null;
		
		
		try {
			
			final RequestObject req = new RequestObject(incomingsms);
			
			final String KEYWORD = req.getKeyword().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			
			conn = null;//getCon();
			
			
			logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			
			
			if(KEYWORD.equalsIgnoreCase("ENG")){
				String more =   "1. News\n"+
								"2. Prayer Times\n"+
								"3. Sports\n"+
								"4. What's up\n"+
								"5. Fun & Inspiration\n"+
								"6. Love & Family\n"+
								"7. Sports";
				outgoingsms.setSms(more);
				
			
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
				outgoingsms.setSms(unknown_keyword);
			
			}
			
			logger.info(outgoingsms.toString());
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {


		try{
			
			if(context!=null)
				context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
		
	}

}
