package com.pixelandtag.serviceprocessors.sms;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.BaseEntityI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.staticcontent.ContentRetriever;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.util.FileUtils;
import com.pixelandtag.web.beans.RequestObject;

public class StaticContentProcessor extends GenericServiceProcessor{

	private final Logger static_content_processor_logger = Logger.getLogger(StaticContentProcessor.class);
	private SubscriptionOld subscription;
	private Properties mtsenderprop;
	private ContentRetriever cr = null;
	private String SPACE = " ";
	
	

	private  Context context = null;
	private CMPResourceBeanRemote cmpbean;
	public void initEJB() throws NamingException{
	    	String JBOSS_CONTEXT="org.jboss.naming.remote.client.InitialContextFactory";;
			 Properties props = new Properties();
			 props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			 props.put(Context.PROVIDER_URL, "remote://"+mtsenderprop.getProperty("ejbhost")+":"+mtsenderprop.getProperty("ejbhostport"));
			props.put(Context.SECURITY_PRINCIPAL, mtsenderprop.getProperty("SECURITY_PRINCIPAL"));
			props.put(Context.SECURITY_CREDENTIALS, mtsenderprop.getProperty("SECURITY_CREDENTIALS"));
			props.put("jboss.naming.client.ejb.context", true);
			 context = new InitialContext(props);
			 cmpbean =  (CMPResourceBeanRemote) 
	       		context.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
			 cr = new ContentRetriever(cmpbean);
			logger.info(getClass().getSimpleName()+" : Successfully initialized EJB CMPResourceBeanRemote !!");
	}
	
	public StaticContentProcessor() throws NamingException{
		init_datasource();
		initEJB();
		subscription = new SubscriptionOld();
	}
	
	
	
	
	
	
	private void init_datasource(){
		mtsenderprop = FileUtils.getPropertyFile("mtsender.properties");
		
		/*int vendor = DriverUtilities.MYSQL;
	    String driver = DriverUtilities.getDriver(vendor);
	    String host = mtsenderprop.getProperty("db_host");
	    String dbName = mtsenderprop.getProperty("DATABASE");
	    String username = mtsenderprop.getProperty("db_username");
	    String password = mtsenderprop.getProperty("db_password");
	    String url = DriverUtilities.makeURL(host, dbName, vendor, username, password);
	   
		
		ds = new DBPoolDataSource();
	    ds.setName("STATICCONTENT_PROCESSOR_DS");
	    ds.setDescription("Static Content thread datasource: "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	   // ds.setUser(username);
	   // ds.setPassword(password);
	    ds.setMinPool(1);
	    ds.setMaxPool(2);
	    ds.setMaxSize(3);
	    ds.setIdleTimeout(3600);  // Specified in seconds.
	    ds.setValidationQuery("SELECT 'Test'");*/
		
	}

	@Override
	public OutgoingSMS process(IncomingSMS incomingsms) {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		
		try {
			
			//conn = getCon();
			
			final RequestObject req = new RequestObject(incomingsms);
			final String KEYWORD = req.getKeyword().trim();
			final Long serviceid = 	incomingsms.getServiceid();
			final String MSISDN = req.getMsisdn();
			final Map<String,String> additionalInfo = cmpbean.getAdditionalServiceInfo(serviceid.intValue());
			
			
			
			final String static_categoryvalue = cmpbean.getServiceMetaData(serviceid.intValue(),"static_categoryvalue");//UtilCelcom.getServiceMetaData(conn,serviceid,"static_categoryvalue");
			final String table =  cmpbean.getServiceMetaData(serviceid.intValue(),"table");
			
			static_content_processor_logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			static_content_processor_logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			static_content_processor_logger.info(" static_categoryvalue ::::::::::::::::::::::::: ["+static_categoryvalue+"]");
			
			
			if(KEYWORD.equalsIgnoreCase("MORE")){
				
				String more = "";
				/*  "1. Berita (News)\n"+
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
				"15. Salam Pengantin (Marriage Wishes)";*/
				outgoingsms.setSms(more);

			}else if(!static_categoryvalue.equals("-1")){
				
				String tailMsg = "";
				
				if(!incomingsms.getIsSubscription()){//If this is a subscription push, then don't check if sub is subscribed.
					
					SubscriptionDTO sub = cmpbean.getSubscriptionDTO(MSISDN, serviceid.intValue());
					
					tailMsg = (sub==null ? additionalInfo.get("tailText_notsubscribed") : (SubscriptionStatus.confirmed==SubscriptionStatus.get(sub.getSubscription_status()) ? additionalInfo.get("tailText_subscribed") : additionalInfo.get("tailText_notsubscribed")));
							 
					if(tailMsg==null || tailMsg.equals(additionalInfo.get("tailText_notsubscribed"))){
						SMSService smsService = cmpbean.find(SMSService.class, new Long(serviceid));
						@SuppressWarnings("unused")
						boolean success = cmpbean.subscribe(MSISDN, smsService, -1,AlterationMethod.self_via_sms);
					}
					
				}else{
					tailMsg = additionalInfo.get("tailText_subscribed");
				}
				final String content = cmpbean.getUniqueFromCategory("pixeland_content360", table, "Text", "id", "Category", static_categoryvalue, MSISDN, serviceid.intValue(), 1, incomingsms.getMoprocessor().getId());
				
				if(content!=null)
					outgoingsms.setSms(content+SPACE+tailMsg);
				else
					outgoingsms.setSms(SPACE);//No content! Send blank msg.
						
				
				toStatsLog(incomingsms, null);
				static_content_processor_logger.debug("CONTENT FOR MSISDN["+MSISDN+"] ::::::::::::::::::::::::: ["+incomingsms.toString()+"]");
				
			}else{
				String unknown_keyword = cmpbean.getServiceMetaData(-1,"unknown_keyword");
				
				if(unknown_keyword==null)
					unknown_keyword = "Unknown Keyword.";
					outgoingsms.setSms(unknown_keyword);
			
			}
			
			static_content_processor_logger.debug(incomingsms.toString());
			
		}catch(Exception e){
			
			static_content_processor_logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				//conn.close();
			}catch(Exception e){}
		
		}
		
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {

		
		
		try{
			
			context.close();
		
		}catch(Exception e){
			
			static_content_processor_logger.error(e.getMessage(),e);
		
		}
		try {
			
			//ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			static_content_processor_logger.error(e.getMessage(),e);
			
		}
		
	}

	@Override
	public Connection getCon() {
		
		try {
			
			return null;//ds.getConnection();
		
		} catch (Exception e) {
			
			static_content_processor_logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}

	
	@Override
	public BaseEntityI getEJB() {
		return this.cmpbean;
	}

}
