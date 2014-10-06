package com.inmobia.celcom.serviceprocessors.sms;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.axiata.connections.DriverUtilities;
import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.beans.RequestObject;
import com.inmobia.axiata.web.triviaI.MechanicsI;
import com.inmobia.celcom.api.CelcomHTTPAPI;
import com.inmobia.celcom.api.CelcomImpl;
import com.inmobia.celcom.api.GenericServiceProcessor;
import com.inmobia.celcom.entities.MOSms;
import com.inmobia.celcom.sms.application.HTTPMTSenderApp;
import com.inmobia.celcom.smsmenu.MenuController;
import com.inmobia.celcom.smsmenu.MenuItem;
import com.inmobia.celcom.smsmenu.Session;
import com.inmobia.celcom.staticcontent.ContentRetriever;
import com.inmobia.celcom.subscription.Subscription;
import com.inmobia.celcom.subscription.SubscriptionSource;
import com.inmobia.celcom.subscription.dto.SMSServiceDTO;
import com.inmobia.celcom.subscription.dto.SubscriptionDTO;
import com.inmobia.celcom.subscription.dto.SubscriptionStatus;
import com.inmobia.celcom.util.UtilCelcom;
import com.inmobia.luckydip.api.NoSettingException;

public class MoreProcessor extends GenericServiceProcessor {

	private static final int ENG = 1;//Language English
	private static final int MAL = 2;//Language Malay

	

	private final Logger mo_processor_logger = Logger.getLogger(MoreProcessor.class);
	//private final String RESET_SESSION_ENG = "\nREPLY with \"Number\" to get the list of services.";
	//private final String RESET_SESSION_MAL = "\nBALAS dengan \"Nombor\" untuk dapatkan senarai servis.";
	private ContentRetriever cr = null;
	private DBPoolDataSource ds;
	private MenuController menu_controller = null;
	private Subscription subscription = null;
	private CelcomHTTPAPI celcomAPI = null;
	
	
	public MoreProcessor(){
		init_datasource();
		menu_controller = new MenuController();
		subscription = new Subscription();
		cr = new ContentRetriever();
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName = HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		try {
			celcomAPI = new CelcomImpl(url, "MORE_PROC_API_DS");
		} catch (Exception e) {
			mo_processor_logger.error(e.getMessage(),e);
		}
	}
	
	private void init_datasource(){
		
		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName =  HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		
		ds = new DBPoolDataSource();
	    ds.setName("MORE_PROCESSOR_DS");
	    ds.setDescription("Processes the More Keyword. Thread datasource: "+ds.getName());
	    ds.setDriverClassName(driver);
	    ds.setUrl(url);
	    ds.setUser("root");
	    ds.setPassword("");
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
			
			String second_keyword = null;
			final RequestObject req = new RequestObject(mo);
			
			String KEYWORD = req.getKeyword().trim().toUpperCase();
			final int serviceid = 	mo.getServiceid();
			final String MSISDN = req.getMsisdn();
			int chosen = -1;
			boolean kw_is_digit = false;
			boolean second_keyword_is_digit = false;
			
			try{
				chosen = Integer.valueOf(KEYWORD);
				kw_is_digit = true;
			}catch(Exception e){
				
			}
			
			
			try{
				
				String msg[] = req.getMsg().trim().split("[\\s]");
				
				mo_processor_logger.debug(" GUGAMUG req.getMsg().trim() : = "+req.getMsg().trim());
				
				int msgL = msg.length;
				
				mo_processor_logger.debug(" GUGAMUG msgL : = "+msgL);
				
				try{
					second_keyword = msg[1].trim().toUpperCase();
				}catch(Exception e){
				}
				
				
				if(second_keyword==null){
					
					try{
						
						if(KEYWORD.indexOf("ON")>-1){
							msg = req.getMsg().toUpperCase().trim().split("ON");
							msgL = msg.length;
							mo_processor_logger.debug(" GUGAMUG msgL2 : = "+msgL);
							second_keyword = msg[1].trim().toUpperCase();
							KEYWORD = "ON";
						}
						
						if(KEYWORD.indexOf("STOP")>-1){
							msg = req.getMsg().toUpperCase().trim().split("STOP");
							msgL = msg.length;
							mo_processor_logger.debug(" GUGAMUG msgL2 : = "+msgL);
							second_keyword = msg[1].trim().toUpperCase();
							KEYWORD = "STOP";
						}
						
						if(KEYWORD.indexOf("BATAL")>-1){
							msg = req.getMsg().toUpperCase().trim().split("BATAL");
							msgL = msg.length;
							mo_processor_logger.debug(" GUGAMUG msgL2 : = "+msgL);
							second_keyword = msg[1].trim().toUpperCase();
							KEYWORD = "BATAL";
						}
						
					}catch(Exception e){
						
					}
					
				}
				
				mo_processor_logger.debug(" GUGAMUG second_keyword : = "+second_keyword);
				
				
				
				if(msgL>=2){
					try{
						chosen = Integer.valueOf(msg[1]);
						second_keyword_is_digit = true;
					}catch(Exception e){
						
					}
				}
				
				
				
			}catch(Exception e){
				
			}
			
			
			conn = getCon();
			
			mo_processor_logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			mo_processor_logger.info(" THE WORD AFTER KEYWORD ::::::::::::::::::::::::: ["+second_keyword+"]");
			mo_processor_logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			final Session sess = menu_controller.getSession(MSISDN, conn);
			
			int smsmenu_level_id_fk = -1;
			
			int language_id = MAL;//malay is default 
			
			if(sess!=null){
				smsmenu_level_id_fk = sess.getSmsmenu_level_id_fk();
				language_id = sess.getLanguage_id();
			}else{
				language_id = UtilCelcom.getSubscriberLanguage(mo.getSUB_Mobtel(), conn);//get from db
			}
			
			
			mo_processor_logger.debug("session :: "+sess);
			mo_processor_logger.debug("smsmenu_level_id_fk :: "+smsmenu_level_id_fk);
			mo_processor_logger.debug("language_id :: "+language_id);
			
			MenuItem current_menu = null;
			
			if(smsmenu_level_id_fk>-1)
				current_menu = sess.getMenu_item();//menu_controller.getMenuById(smsmenu_level_id_fk,conn);
			else
				current_menu = menu_controller.getMenuByParentLevelId(language_id,smsmenu_level_id_fk,conn);//get root menu
			
			mo_processor_logger.info("FROM SESSION___________________________"+current_menu);
			if(KEYWORD.equalsIgnoreCase("#")){
				
				menu_controller.updateSession(language_id,MSISDN, current_menu.getParent_level_id(), conn);//update session to upper menu.
				MenuItem item = menu_controller.getMenuByParentLevelId(language_id,current_menu.getParent_level_id(), conn);
				mo.setMt_Sent(item.enumerate()+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));//get all the sub menus there.
				
			}else if(KEYWORD.equalsIgnoreCase("0")){
				
				menu_controller.updateSession(language_id, MSISDN, -1, conn);//update session to upper menu.
				MenuItem item = menu_controller.getMenuByParentLevelId(language_id,-1, conn);
				mo.setMt_Sent(item.enumerate());//get all the sub menus there.
				
			}else if(KEYWORD.equalsIgnoreCase("GIFT") || KEYWORD.equalsIgnoreCase("HIDIAH") || KEYWORD.equalsIgnoreCase("HADIAH")){
				
				mo_processor_logger.debug("\nIN  GIFT OR ENG KWD\n");
				
				language_id = KEYWORD.equalsIgnoreCase("GIFT") ?  ENG : MAL;
				int menu_id = 1;
				
				UtilCelcom.updateProfile(mo.getSUB_Mobtel(),language_id,conn);
				
				menu_controller.updateSession(language_id,MSISDN, -1, conn);//update session to upper menu.
				current_menu = menu_controller.getTopMenu(menu_id, language_id, conn);
				
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+current_menu.enumerate()+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));
				
			}else if(KEYWORD.equalsIgnoreCase("AFCON")){
				
				language_id = MAL;
				int menu_id = 2;
				
				menu_controller.updateSession(language_id,MSISDN, -1, conn);//update session to upper menu.
				current_menu = menu_controller.getTopMenu(menu_id, language_id, conn);
				
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+current_menu.enumerate()+SPACE+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));
				
			}else if(kw_is_digit){
				
				
				
				mo_processor_logger.error("\n\n\nGUGAMUGA CURRENT MENU >> "+current_menu+"\n\n");
				LinkedHashMap<Integer,MenuItem> submenu = current_menu.getSub_menus();
				
				boolean submenus_have_sub_menus = false;
				
				for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
					if(entry.getValue().getSub_menus()!=null && entry.getValue().getSub_menus().size()>0){
						mo_processor_logger.error("GUGAMUGA  checking if we have sub menus>> "+entry.getValue().getName());
						submenus_have_sub_menus = true;
						break;
					}
				}
				
				mo_processor_logger.error("GUGAMUGA submenus_have_sub_menus >> "+submenus_have_sub_menus);
				
				MenuItem chosenMenu = current_menu.getMenuByPosition(chosen);
				//MenuItem chosenMenu = menu_controller.getMenuById(chosenMenu.getId(), conn);
				
				chosenMenu = menu_controller.getMenuById(chosenMenu.getId(), conn);
				
				
				if(chosenMenu!=null){
						submenu = chosenMenu.getSub_menus();
						
						
						if(submenu!=null)
						for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
							mo_processor_logger.debug("Checking if submenu has got other menus in it... >>>> "+entry.getKey()+ ". "+entry.getValue().toString());
							
							if(entry.getValue().getService_id()==-1)
							submenus_have_sub_menus = true;
							break;
						}
						
						
						mo_processor_logger.error("GUGAMUGA  >> "+submenus_have_sub_menus);
						
						mo_processor_logger.error("GUGAMUGA  submenu >> "+submenu);
						
						mo_processor_logger.debug(" Trying to print out the sub menu chosen. (chosen="+chosen+"");
						try{
							mo_processor_logger.debug(" \n\nCHOSEN MENU >>>>>>>>>>>>> (chosen="+chosen+")  : "+chosenMenu.toString()+"\n\n");
						}catch(Exception e){
							mo_processor_logger.error(e.getMessage(),e);
						}
				}else{
					logger.debug("\n\n\nchosen menu was null...\n\n\n");
					logger.debug("\n\n\ncurrent_menu = "+current_menu+"...\n\n\n");
					
					logger.debug("\n\n\nchosen menu was null...\n\n\n");
					mo_processor_logger.error("GUGAMUGA  >> "+submenus_have_sub_menus);
					mo_processor_logger.debug(" Chosen. (chosen="+chosen+"");
					
				}
				
			//	menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update sessi
				
				if( (submenu!=null && (chosen>submenu.size())) ){
					
					//chosenMenu = current_menu.getMenuByPosition(chosen);
					menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update session
					
					if(submenus_have_sub_menus){
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate() +UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));//get all the sub menus there.
					}else{
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate() + UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id));//get all the sub menus there.
					}
				}else{
					
					
					
					if(chosenMenu.getService_id()==-1){//if there are other items under this, update session
						
						menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update session
						
						chosenMenu = menu_controller.getMenuById(chosenMenu.getId(), conn);
						
						if(submenus_have_sub_menus)
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));//get all the sub menus there.
						else
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id));//get all the sub menus there.
					}else{
						
						//chosenMenu = menu_controller.getMenuById(current_menu.getId(), conn);//Get the current menu itself
						mo_processor_logger.debug("\n\n\n*******************************8\nRTFM subscriber!! You should reply with <ON No.> for example ON "+chosen+" !!! \n*******************************8\n\n\n\n" );
						//mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(MessageType.DOUBLE_CONFIRMATION_ADVICE, conn, language_id));//get all the sub menus there.
						String msg = UtilCelcom.getMessage(MessageType.DOUBLE_CONFIRMATION_ADVICE, conn, language_id);
						msg = msg.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
						msg = msg.replaceAll(CHOSEN, String.valueOf(chosen));
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+msg);//get all the sub menus there.
					
					}
				}
				
			}else if(KEYWORD.equalsIgnoreCase(SUBSCRIPTION_CONFIRMATION)){
				
				LinkedHashMap<Integer,MenuItem> submenu = current_menu.getSub_menus();
				
				boolean submenus_have_sub_menus = false;
				
				if(submenu!=null){
					for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
						if(entry.getValue().getSub_menus()!=null && entry.getValue().getSub_menus().size()>0){
							mo_processor_logger.error("GUGAMUGA  2 checking if we have sub menus >> "+entry.getValue().getName());
							submenus_have_sub_menus = true;
							break;
						}
					}
				}else{
					throw new NoSettingException("The menu with id "+current_menu.getId()+" Name=\""+current_menu.getName()+"\"has no children (sub menus)! Check the celcom_static_content.smsmenu_levels");
				}
				
				
				MenuItem chosenMenu = null;
				
				
				if(chosen>0){
					chosenMenu = current_menu.getMenuByPosition(chosen);
				}
				
				if((chosen>0) && chosenMenu!=null){
				
					chosenMenu = current_menu.getMenuByPosition(chosen);
					
					final MOSms mosm_ =  cr.getContentFromServiceId(chosenMenu.getService_id(),MSISDN,conn);
					final SubscriptionDTO subdto = subscription.getSubscriptionDTO(conn, MSISDN, chosenMenu.getService_id());
					
					if(chosenMenu.getService_id()<1){//if this still looks like a sub-menu, we send to subscriber the sub menu and tell them how to subscribe

						chosenMenu = menu_controller.getMenuById(chosenMenu.getId(), conn);
						
						menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update session
						
						submenu = chosenMenu.getSub_menus();
						
						if(submenu!=null){
							for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
								if(entry.getValue().getSub_menus()!=null && entry.getValue().getSub_menus().size()>0){
									mo_processor_logger.error("GUGAMUGA  2 checking if we have sub menus >> "+entry.getValue().getName());
									submenus_have_sub_menus = true;
									break;
								}
							}
						}else{
							throw new NoSettingException("The menu with id "+chosenMenu.getId()+" has no children (sub menus)! Check the celcom_static_content.smsmenu_levels");
						}
						
						
						
						if(submenus_have_sub_menus)
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));//get all the sub menus there.
						else
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id));//get all the sub menus there.
						
					}else{
					
						if(subdto==null || (subdto!=null && !subdto.getSubscription_status().equals(SubscriptionStatus.confirmed.toString()))){
						
							subscription.subscribe(conn, MSISDN, chosenMenu.getService_id(), chosenMenu.getId(),SubscriptionStatus.confirmed, SubscriptionSource.SMS);//subscribe but marks as "confirmed"
							
							String response = UtilCelcom.getMessage(CONFIRMED_SUBSCRIPTION_ADVICE, conn, language_id) ;
							if(response.indexOf(SERVICENAME_TAG)>=0)
								response = response.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
							if(response.indexOf(PRICE_TAG)>=0)
								response = response.replaceAll(PRICE_TAG, String.valueOf(mosm_.getPrice()));
							if(response.indexOf(KEYWORD_TAG)>=0)
								response = response.replaceAll(KEYWORD_TAG, mosm_.getSMS_Message_String());
							
							
							//this is sent out normally
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+response);//subscription confirmation
							
							if(!mosm_.getMt_Sent().startsWith("RM"))
								mosm_.setMt_Sent((RM.replaceAll(PRICE_TG, String.valueOf(mosm_.getPrice()))+mosm_.getMt_Sent()));//content piece
							
							mosm_.setSMS_DataCodingId(mo.getSMS_DataCodingId());
							
							sendMT(mosm_);
							
						}else{
							
							
							//Send them content for that service.
							//If celcom allows, we can send the content here, but it is Celcom's policy
							//that you don't charge a subscriber without warning. They must confim their subscription.
							
							//Already subscribed text
							String response = UtilCelcom.getMessage(MessageType.ALREADY_SUBSCRIBED_ADVICE, conn, language_id) ;
							if(response.indexOf(SERVICENAME_TAG)>=0)
								response = response.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
							if(response.indexOf(PRICE_TAG)>=0)
								response = response.replaceAll(PRICE_TAG, String.valueOf(mosm_.getPrice()));
							if(response.indexOf(KEYWORD_TAG)>=0)
								response = response.replaceAll(KEYWORD_TAG, mosm_.getSMS_Message_String());
							
							if(submenus_have_sub_menus)
								mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+response);//get all the sub menus there.
							else
								mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+response);//get all the sub menus there.
							
							
						}
					}
				
				}else{
					//Here check if subscriber sent valid keyword, fetch service, and subscribe then to that service.
					if(submenus_have_sub_menus)
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+current_menu.enumerate() +UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id));//get all the sub menus there.
					else
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+current_menu.enumerate() + UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id));//get all the sub menus there.
				
				}
				
				
			}else if(KEYWORD.equalsIgnoreCase(SUBSCRIPTION_CONFIRMATION+SPACE)){//This step is frozen by adding a space. Requested by Michael Juhl 20th June 2013
				
				
				//TODO - if a subscriber just sends "ON" or "BUY" Without the number, then, we give them the main menu, or the sub menu that they previously were in
				SubscriptionDTO sub = subscription.checkAnyPending(conn, MSISDN);
				
				if(sub!=null){
					
					subscription.updateSubscription(conn, sub.getId(), SubscriptionStatus.confirmed);
					
					MenuItem menu = menu_controller.getMenuById(sub.getSmsmenu_levels_id_fk(), conn);
					
					language_id = menu.getLanguage_id();
					
					mo_processor_logger.info("::::::::::::::::::::::::: serviceid:: "+menu.getService_id() + "\n\nmenu.toString():\n "+menu.toString()+"\n");
					final MOSms mosm_ =  cr.getContentFromServiceId(menu.getService_id(),MSISDN,conn);
					
					String response = UtilCelcom.getMessage(CONFIRMED_SUBSCRIPTION_ADVICE, conn, language_id) ;
					if(response.indexOf(SERVICENAME_TAG)>=0)
						response = response.replaceAll(SERVICENAME_TAG, menu.getName());
					if(response.indexOf(PRICE_TAG)>=0)
						response = response.replaceAll(PRICE_TAG, String.valueOf(mosm_.getPrice()));
					if(response.indexOf(KEYWORD_TAG)>=0)
						response = response.replaceAll(KEYWORD_TAG, mosm_.getSMS_Message_String());
					
					
					//this is sent out normally
					mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+SPACE+response);
					
					mosm_.setMt_Sent((RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+SPACE+mosm_.getMt_Sent()));
					
					sendMT(mosm_);
					
					
				}else{
					mo.setMt_Sent(UtilCelcom.getMessage(NO_PENDING_SUBSCRIPTION_ADVICE, conn, language_id));
				}
				
			}else if(KEYWORD.equals("STOP") || KEYWORD.equals("ST0P") || KEYWORD.equals("BATAL")){
				
				String msg = UtilCelcom.getMessage(MessageType.UNSUBSCRIBED_SINGLE_SERVICE_ADVICE, conn, language_id);
				
				int stop_number = -1;
				
				try{
					stop_number = Integer.valueOf(second_keyword);
				}catch(Exception e){}
				
				
				LinkedHashMap<Integer,SMSServiceDTO> allsubscribed = subscription.getAllSubscribedServices(mo.getSUB_Mobtel(),conn);
				
				if(allsubscribed!=null){
				
					if(second_keyword!=null && (second_keyword.equalsIgnoreCase("all") || second_keyword.equalsIgnoreCase("semua"))){
						subscription.unsubscribeAll(conn,MSISDN,SubscriptionStatus.unsubscribed);
						msg = UtilCelcom.getMessage(UNSUBSCRIBED_ALL_ADVICE, conn, language_id);
						msg = msg.replaceAll(SERVICENAME_TAG, UtilCelcom.getMessage(MessageType.ALL_SERVICES, conn, language_id));
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+SPACE+msg);
					}else if(second_keyword!=null || stop_number>-1){
						
						if(second_keyword_is_digit){
										
							SMSServiceDTO toUnsubscribe = allsubscribed.get(stop_number);
							
							//if(subscription.updateSubscription(conn, toUnsubscribe.getId(), MSISDN,SubscriptionStatus.unsubscribed)){
								subscription.updateSubscription(conn, toUnsubscribe.getId(), MSISDN,SubscriptionStatus.unsubscribed);
								msg = msg.replaceAll(SERVICENAME_TAG, toUnsubscribe.getService_name());
							/*}else{
								msg = UtilCelcom.getMessage(MessageType.UNABLE_TO_UNSUBSCRIBE_ADVICE, conn, language_id);//try again
							}*/
										
						}else if(second_keyword!=null){
										
							SMSServiceDTO smsservice = subscription.getSMSservice(second_keyword, conn);
							
							//if(subscription.updateSubscription(conn, smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed)){
								subscription.updateSubscription(conn, smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed);
								msg = msg.replaceAll(SERVICENAME_TAG, smsservice.getService_name());
							/*}else{
								msg = UtilCelcom.getMessage(MessageType.UNABLE_TO_UNSUBSCRIBE_ADVICE, conn, language_id);//try again
							}*/
										
						}
								
					}else {
						msg = subscription.stringFyServiceList(allsubscribed)+UtilCelcom.getMessage(MessageType.INDIVIDUAL_UNSUBSCRIBE_ADVICE, conn, language_id);
					}
							
				}else{
					msg = UtilCelcom.getMessage(MessageType.NOT_SUBSCRIBED_TO_ANY_SERVICE_ADVICE, conn, language_id);
				}
					
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+msg);
				

			}else if(KEYWORD.equals("HELP")){
				
				String msg =  UtilCelcom.getMessage(MessageType.HELP, conn, language_id);
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+msg);
				
			}else if(KEYWORD.equals("INFO")){
				
				String msg =  UtilCelcom.getMessage(MessageType.INFO, conn, language_id);
				mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+msg);
			
			}else{
				//Unknown keyword
				mo.setMt_Sent(UtilCelcom.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, conn, language_id));
			}
			
			mo_processor_logger.info(mo.toString());
			
		}catch(Exception e){
			
			mo_processor_logger.error(e.getMessage(),e);
		
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
			
			mo_processor_logger.error(e.getMessage(),e);
			
		}
		
	}
	
	
	
	
	
	

	@Override
	public Connection getCon() {
		
		try {
			
			return ds.getConnection();
		
		} catch (Exception e) {
			
			mo_processor_logger.error(e.getMessage(),e);
			
			return null;
		
		}finally{
		
		}
	}
	
	
	
	
	
	
	

}
