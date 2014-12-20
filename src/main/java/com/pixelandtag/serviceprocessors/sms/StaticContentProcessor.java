package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.util.UtilCelcom;
import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.entities.MOSms;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.staticcontent.ContentRetriever;
import com.pixelandtag.subscription.Subscription;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.RequestObject;
import com.pixelandtag.web.triviaI.MechanicsI;

public class StaticContentProcessor extends GenericServiceProcessor{

	private final Logger static_content_processor_logger = Logger.getLogger(StaticContentProcessor.class);
	private DBPoolDataSource ds;
	private Subscription subscription;
	
	private ContentRetriever cr = new ContentRetriever();
	private String SPACE = " ";
	
	public StaticContentProcessor(){
		init_datasource();
		subscription = new Subscription();
	}
	
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host =  HTTPMTSenderApp.props.getProperty("db_host");
	    String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
	    String url = DriverUtilities.makeURL(host, dbName, vendor);
	    String username = HTTPMTSenderApp.props.getProperty("db_username");
	    String password = HTTPMTSenderApp.props.getProperty("db_password");
		
		ds = new DBPoolDataSource();
	    ds.setName("STATICCONTENT_PROCESSOR_DS");
	    ds.setDescription("Static Content thread datasource: "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser(username);
	    ds.setPassword(password);
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");
		
	}

	@Override
	public MOSms process(MOSms mo) {
		
		Connection conn = null;
		
		try {
			
			conn = getCon();
			
			final RequestObject req = new RequestObject(mo);
			final String KEYWORD = req.getKeyword().trim();
			final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			final Map<String,String> additionalInfo = UtilCelcom.getAdditionalServiceInfo(serviceid,conn);
			
			
			
			final String static_categoryvalue = UtilCelcom.getServiceMetaData(conn,serviceid,"static_categoryvalue");
			final String table = UtilCelcom.getServiceMetaData(conn,serviceid,"table");
			
			static_content_processor_logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			static_content_processor_logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			static_content_processor_logger.info(" static_categoryvalue ::::::::::::::::::::::::: ["+static_categoryvalue+"]");
			
			
			if(KEYWORD.equalsIgnoreCase("MORE")){
				
				String more =   "1. Berita (News)\n"+
				"2. Waktu Solat (Prayer Times)\n"+
				"3. Salam Raya (Raya Wishes)\n"+
				"4. Salam Maaf (Forgiveness)\n"+
				"5. Tip Cinta (Love Tips)\n"+
				"6. Acara-acara (Events)\n"+
				"7. Fakta Menakjubkan: Kota Suci(Amazing Facts: Holy City)\n"+
				"8. Keajaiban Dunia (World Wonders)\n"+
				"9. Tapak-tapak Muslim Terkenal (Famous Islamic Sites)\n"+
				"10. Inspirasi (Inspiration)\n"+
				"11. Lawak (Jokes)\n"+
				"12. Pantun (Rhymes)\n"+
				"13. Teka-teki (Riddles)\n"+
				"14. Ucapan Hari Lahir (Bday Wishes)\n"+
				"15. Salam Pengantin (Marriage Wishes)";
				mo.setMt_Sent(more);

			}else if(!static_categoryvalue.equals("-1")){
				
				String tailMsg = "";
				
				if(!mo.isSubscriptionPush()){//If this is a subscription push, then don't check if sub is subscribed.
					
					SubscriptionDTO sub = subscription.getSubscriptionDTO(conn, MSISDN, serviceid);
					
					tailMsg = (sub==null ? additionalInfo.get("tailText_notsubscribed") : (SubscriptionStatus.confirmed==SubscriptionStatus.get(sub.getSubscription_status()) ? additionalInfo.get("tailText_subscribed") : additionalInfo.get("tailText_notsubscribed")));
							 
					if(tailMsg==null || tailMsg.equals(additionalInfo.get("tailText_notsubscribed"))){
						subscription.subscribe(conn, MSISDN, serviceid, -1);
					}
					
				}else{
					tailMsg = additionalInfo.get("tailText_subscribed");
				}
				
				final String content = cr.getUniqueFromCategory("pixeland_content360", table, "Text", "id", "Category", static_categoryvalue, MSISDN, serviceid, 1, mo.getProcessor_id(), conn);
				
				if(content!=null)
					mo.setMt_Sent(content+SPACE+tailMsg);
				else
					mo.setMt_Sent(SPACE);//No content! Send blank msg.
						
				static_content_processor_logger.info("CONTENT FOR MSISDN["+MSISDN+"] ::::::::::::::::::::::::: ["+mo.toString()+"]");
				
			}else{
				
				String unknown_keyword = UtilCelcom.getServiceMetaData(conn,-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
					mo.setMt_Sent(unknown_keyword);
			
			}
			
			static_content_processor_logger.info(mo.toString());
			
		}catch(Exception e){
			
			static_content_processor_logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				conn.close();
			}catch(Exception e){}
		
		}
		
		return mo;
	}

	@Override
	public void finalizeMe() {

		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			static_content_processor_logger.error(e.getMessage(),e);
			
		}
		
	}

	@Override
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			static_content_processor_logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}
	

}