package com.pixelandtag.serviceprocessors.sms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.inmobia.luckydip.api.NoSettingException;
import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomHTTPAPI;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.smsmenu.MenuController;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionOld;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

public class GenericMenuProcessor extends GenericServiceProcessor  {

	
	private static final int ENG = 1;//Language English
	private static final int MAL = 2;//Language Malay

	private final Logger mo_processor_logger = Logger.getLogger(GenericMenuProcessor.class);
	//private final String RESET_SESSION_ENG = "\nREPLY with \"Number\" to get the list of services.";
	//private final String RESET_SESSION_MAL = "\nBALAS dengan \"Nombor\" untuk dapatkan senarai servis.";
	//private ContentRetriever cr = null;
	private DBPoolDataSource ds;
	private MenuController menu_controller = null;
	private SubscriptionOld subscription = null;
	private CelcomHTTPAPI celcomAPI = null;
	
	private InitialContext context;
	private CMPResourceBeanRemote cmpbean;
	
	private SubscriptionBeanI subscriptionBean;
    
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
		 subscriptionBean = (SubscriptionBeanI) 
    		context.lookup("cmp/SubscriptionEJB!com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI");
		 
		 logger.info("Successfully initialized EJB CMPResourceBeanRemote !!");
    }
	public GenericMenuProcessor(){
		init_datasource();
		menu_controller = new MenuController(getEJB());
		subscription = new SubscriptionOld();
	}
	
	private void init_datasource(){
			
	}
	
	@Override
	public OutgoingSMS process(IncomingSMS  incomingsms) {
		//Connection conn = null;
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		try {
			
			String second_keyword = null;
			final RequestObject req = new RequestObject(incomingsms); 
			
			String KEYWORD = req.getKeyword().trim().toUpperCase();
			final Long serviceid = 	incomingsms.getServiceid();
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
						mo_processor_logger.error(e.getMessage(),e);
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
			
			
			//conn = getCon();
			
			mo_processor_logger.info(" KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
			mo_processor_logger.info(" THE WORD AFTER KEYWORD ::::::::::::::::::::::::: ["+second_keyword+"]");
			mo_processor_logger.info(" SERVICEID ::::::::::::::::::::::::: ["+serviceid+"]");
			
			final Session sess = menu_controller.getSession(MSISDN);
			
			int smsmenu_level_id_fk = -1;
			
			int language_id = ENG;//eng is default 
			
			if(sess!=null){
				smsmenu_level_id_fk = sess.getSmsmenu_level_id_fk();
				language_id = sess.getLanguage_id();
			}else{
				language_id = getEJB().getSubscriberLanguage(incomingsms.getMsisdn());//get from db
			}
			
			
			mo_processor_logger.debug("session :: "+sess);
			mo_processor_logger.debug("smsmenu_level_id_fk :: "+smsmenu_level_id_fk);
			mo_processor_logger.debug("language_id :: "+language_id);
			
			MenuItem current_menu = null;
			int menuid =  1;
			if(smsmenu_level_id_fk>-1)
				current_menu = sess.getMenu_item();//menu_controller.getMenuById(smsmenu_level_id_fk,conn);
			else
				current_menu = menu_controller.getMenuByParentLevelId(language_id,smsmenu_level_id_fk,menuid);//get root menu
			
			mo_processor_logger.info("FROM SESSION___________________________"+current_menu);
			if(KEYWORD.equalsIgnoreCase("MENU") ||  KEYWORD.equalsIgnoreCase("ORODHA")){
				
				menu_controller.updateSession(language_id,MSISDN, current_menu.getParent_level_id());//update session to upper menu.
				MenuItem item = menu_controller.getMenuByParentLevelId(language_id,current_menu.getParent_level_id(),menuid);
				outgoingsms.setSms(item.enumerate()+cmpbean.getMessage(MAIN_MENU_ADVICE, language_id, incomingsms.getOpco().getId()));//get all the sub menus there.
				
			}else if(KEYWORD.equalsIgnoreCase("0")){
				
				menu_controller.updateSession(language_id, MSISDN, -1);//update session to upper menu.
				MenuItem item = menu_controller.getMenuByParentLevelId(language_id,-1,menuid);
				outgoingsms.setSms(item.enumerate());//get all the sub menus there.
				
			}else if(KEYWORD.equalsIgnoreCase("GIFT") || KEYWORD.equalsIgnoreCase("HIDIAH") || KEYWORD.equalsIgnoreCase("HADIAH")){
				
				mo_processor_logger.debug("\nIN  GIFT OR ENG KWD\n");
				
				language_id = KEYWORD.equalsIgnoreCase("GIFT") ?  ENG : MAL;
				int menu_id = 1;
				
				cmpbean.updateProfile(incomingsms.getMsisdn(),language_id);
				
				menu_controller.updateSession(language_id,MSISDN, -1);//update session to upper menu.
				current_menu = menu_controller.getTopMenu(menu_id, language_id );
				
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+current_menu.enumerate()+cmpbean.getMessage(MAIN_MENU_ADVICE, language_id, incomingsms.getOpco().getId() ));
				
			}else if(KEYWORD.equalsIgnoreCase("AFCON")){
				
				language_id = MAL;
				int menu_id = 2;
				
				menu_controller.updateSession(language_id,MSISDN, -1);//update session to upper menu.
				current_menu = menu_controller.getTopMenu(menu_id, language_id );
				
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+current_menu.enumerate()+SPACE+cmpbean.getMessage(MAIN_MENU_ADVICE, language_id, incomingsms.getOpco().getId() ));
				
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
				
				mo_processor_logger.error("\n\n\n *********** GUGAMUGA submenus_have_sub_menus >> "+submenus_have_sub_menus);
				
				mo_processor_logger.error("\n\n\n *********** GUGAMUGA submenus_have_sub_menus >> "+submenus_have_sub_menus);
				
				
				MenuItem chosenMenu = null;
						
				if(chosen>0){
					chosenMenu = current_menu.getMenuByPosition(chosen);
				}
				
				mo_processor_logger.info("\n\n\n\tGUGAMUGA chosenMenu >> "+chosenMenu+"\n\n");
				mo_processor_logger.info("\n\n\n *********** GUGAMUGA (chosenMenu!=null && (chosenMenu.getSub_menus()==null || chosenMenu.getSub_menus().size()==0))   :"
						+( (chosenMenu!=null && (chosenMenu.getSub_menus()==null || chosenMenu.getSub_menus().size()==0) ) ) );
				
				
				if(chosenMenu!=null && (chosenMenu.getSub_menus()==null || chosenMenu.getSub_menus().size()==0)){//no submenu, so we send content!
					
					
					outgoingsms = sendContent(incomingsms, language_id, language_id, submenus_have_sub_menus, MSISDN, submenu, current_menu, chosenMenu);
					
					
					//SubscriptionDTO sub = subscription.checkAnyPending(conn, MSISDN);
					
					outgoingsms =  getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
						
					if(outgoingsms!=null){
						String response = cmpbean.getMessage(CONFIRMED_SUBSCRIPTION_ADVICE, language_id, incomingsms.getOpco().getId() ) ;
						if(response.indexOf(SERVICENAME_TAG)>=0)
							response = response.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
						if(response.indexOf(PRICE_TAG)>=0)
							response = response.replaceAll(PRICE_TAG, String.valueOf(outgoingsms.getPrice()));
						if(response.indexOf(KEYWORD_TAG)>=0)
							response = response.replaceAll(KEYWORD_TAG, outgoingsms.getSms());
							
							
						//this is sent out normally
						outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+SPACE+response);
							
						outgoingsms.setSms((RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+SPACE+outgoingsms.getSms()));
							
						sendMT(outgoingsms);
					}
					/*	
						
					}else{
						mo.setMt_Sent(UtilCelcom.getMessage(NO_PENDING_SUBSCRIPTION_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));
					}
					*/
					
					
					
				}else if(chosenMenu!=null){
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
				
				/*if( (submenu!=null && (chosen>submenu.size())) ){
					
					//chosenMenu = current_menu.getMenuByPosition(chosen);
					menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update session
					
					if(submenus_have_sub_menus){
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate() +UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
					}else{
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate() +UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
					}
				}else{
					
					
					
					if(chosenMenu.getService_id()==-1){//if there are other items under this, update session
						
						menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update session
						
						chosenMenu = menu_controller.getMenuById(chosenMenu.getId(), conn);
						
						if(submenus_have_sub_menus)
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(MAIN_MENU_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
						else
							mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(SUBSCRIPTION_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
					}else{
						
						//chosenMenu = menu_controller.getMenuById(current_menu.getId(), conn);//Get the current menu itself
						mo_processor_logger.debug("\n\n\n*******************************8\nRTFM subscriber!! You should reply with <ON No.> for example ON "+chosen+" !!! \n*******************************8\n\n\n\n" );
						//mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+chosenMenu.enumerate()+UtilCelcom.getMessage(MessageType.DOUBLE_CONFIRMATION_ADVICE, conn, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
						String msg = UtilCelcom.getMessage(MessageType.DOUBLE_CONFIRMATION_ADVICE, conn, language_id, incomingsms.getOpco().getId() );
						msg = msg.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
						msg = msg.replaceAll(CHOSEN, String.valueOf(chosen));
						mo.setMt_Sent(RM.replaceAll(PRICE_TG, String.valueOf(mo.getPrice()))+msg);//get all the sub menus there.
					
					}
				}*/
				
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
				
				outgoingsms = sendContent(incomingsms, language_id, language_id, submenus_have_sub_menus, MSISDN, submenu, chosenMenu, chosenMenu);
				
				
			}else if(KEYWORD.equalsIgnoreCase(SUBSCRIPTION_CONFIRMATION+SPACE)){//This step is frozen by adding a space. Requested by Michael Juhl 20th June 2013
				
				
				//TODO - if a subscriber just sends "ON" or "BUY" Without the number, then, we give them the main menu, or the sub menu that they previously were in
				SubscriptionDTO sub = cmpbean.checkAnyPending(MSISDN);
				
				if(sub!=null){
					
					subscriptionBean.updateSubscription(sub.getId(), SubscriptionStatus.confirmed, AlterationMethod.self_via_sms);
					
					MenuItem menu = menu_controller.getMenuById(sub.getSmsmenu_levels_id_fk());
					
					language_id = menu.getLanguage_id();
					
					mo_processor_logger.info("::::::::::::::::::::::::: serviceid:: "+menu.getService_id() + "\n\nmenu.toString():\n "+menu.toString()+"\n");
					outgoingsms =  getContentFromServiceId(menu.getService_id(),MSISDN,true);
					
					String response = cmpbean.getMessage(CONFIRMED_SUBSCRIPTION_ADVICE,language_id, incomingsms.getOpco().getId()) ;
					if(response.indexOf(SERVICENAME_TAG)>=0)
						response = response.replaceAll(SERVICENAME_TAG, menu.getName());
					if(response.indexOf(PRICE_TAG)>=0)
						response = response.replaceAll(PRICE_TAG, String.valueOf(outgoingsms.getPrice()));
					if(response.indexOf(KEYWORD_TAG)>=0)
						response = response.replaceAll(KEYWORD_TAG, outgoingsms.getSms());
					
					
					//this is sent out normally
					outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+SPACE+response);
					
				    //outgoingsms.setSms((RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+SPACE+outgoingsms.getMt_Sent()));
					
					sendMT(outgoingsms);
					
					
				}else{
					outgoingsms.setSms(cmpbean.getMessage(NO_PENDING_SUBSCRIPTION_ADVICE, language_id, incomingsms.getOpco().getId() ));
				}
				
			}else if(KEYWORD.equals("STOP") || KEYWORD.equals("ST0P") || KEYWORD.equals("BATAL")){
				
				String msg = cmpbean.getMessage(MessageType.UNSUBSCRIBED_SINGLE_SERVICE_ADVICE, language_id, incomingsms.getOpco().getId() );
				
				int stop_number = -1;
				
				try{
					stop_number = Integer.valueOf(second_keyword);
				}catch(Exception e){}
				
				
				LinkedHashMap<Integer,SMSServiceDTO> allsubscribed = cmpbean.getAllSubscribedServices(incomingsms.getMsisdn());
				
				if(allsubscribed!=null){
				
					if(second_keyword!=null && (second_keyword.equalsIgnoreCase("all") || second_keyword.equalsIgnoreCase("semua"))){
						cmpbean.unsubscribeAll(MSISDN,SubscriptionStatus.unsubscribed,AlterationMethod.self_via_sms);
						msg = cmpbean.getMessage(UNSUBSCRIBED_ALL_ADVICE, language_id, incomingsms.getOpco().getId() );
						msg = msg.replaceAll(SERVICENAME_TAG, cmpbean.getMessage(MessageType.ALL_SERVICES, language_id, incomingsms.getOpco().getId() ));
						outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+SPACE+msg);
					}else if(second_keyword!=null || stop_number>-1){
						
						if(second_keyword_is_digit){
										
							SMSServiceDTO toUnsubscribe = allsubscribed.get(stop_number);
							
							//if(subscription.updateSubscription(conn, toUnsubscribe.getId(), MSISDN,SubscriptionStatus.unsubscribed)){
							subscriptionBean.updateSubscription(toUnsubscribe.getId(), MSISDN,SubscriptionStatus.unsubscribed, AlterationMethod.self_via_sms);
								msg = msg.replaceAll(SERVICENAME_TAG, toUnsubscribe.getService_name());
							/*}else{
								msg = UtilCelcom.getMessage(MessageType.UNABLE_TO_UNSUBSCRIBE_ADVICE, conn, language_id, incomingsms.getOpco().getId() );//try again
							}*/
										 
						}else if(second_keyword!=null){
										
							SMSServiceDTO smsservice = cmpbean.getSMSservice(second_keyword);
							
							//if(subscription.updateSubscription(conn, smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed)){
							subscriptionBean.updateSubscription(smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed, AlterationMethod.self_via_sms);
								msg = msg.replaceAll(SERVICENAME_TAG, smsservice.getService_name());
							/*}else{
								msg = UtilCelcom.getMessage(MessageType.UNABLE_TO_UNSUBSCRIBE_ADVICE, conn, language_id, incomingsms.getOpco().getId() );//try again
							}*/
										
						}
								
					}else {
						msg = subscription.stringFyServiceList(allsubscribed)+cmpbean.getMessage(MessageType.INDIVIDUAL_UNSUBSCRIBE_ADVICE, language_id, incomingsms.getOpco().getId() );
					}
							
				}else{
					msg = cmpbean.getMessage(MessageType.NOT_SUBSCRIBED_TO_ANY_SERVICE_ADVICE, language_id, incomingsms.getOpco().getId() );
				}
					
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+msg);
				

			}else if(KEYWORD.equals("HELP")){
				
				String msg =  cmpbean.getMessage(MessageType.HELP,language_id, incomingsms.getOpco().getId());
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+msg);
				
			}else if(KEYWORD.equals("INFO")){
				
				String msg =  cmpbean.getMessage(MessageType.INFO, language_id, incomingsms.getOpco().getId() );
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+msg);
			
			}else{
				//Unknown keyword
				outgoingsms.setSms(cmpbean.getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, incomingsms.getOpco().getId() ));
			}
			
			mo_processor_logger.info(incomingsms.toString());
			
		}catch(Exception e){
			
			mo_processor_logger.error(e.getMessage(),e);
		
		}finally{
			
		
		}
		
		return outgoingsms;
	}

	
	/**
	 * 
	 * @param conn
	 * @param incomingsms
	 * @param chosen
	 * @param language_id
	 * @param submenus_have_sub_menus
	 * @param MSISDN
	 * @param submenu
	 * @param current_menu
	 * @param chosenMenu
	 * @return
	 * @throws Exception
	 */
	private OutgoingSMS sendContent(IncomingSMS incomingsms, int chosen, int language_id, boolean submenus_have_sub_menus, String MSISDN, LinkedHashMap<Integer,MenuItem> submenu, MenuItem current_menu, MenuItem chosenMenu) throws Exception {
		
		OutgoingSMS outgoingsms = incomingsms.convertToOutgoing();
		
		if((chosen>0) && chosenMenu!=null){
			
			chosenMenu = current_menu.getMenuByPosition(chosen);
			outgoingsms = getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
			//final MOSms mosm_ =  cr.getContentFromServiceId(chosenMenu.getService_id(),MSISDN,conn);
			final SubscriptionDTO subdto = cmpbean.getSubscriptionDTO( MSISDN, chosenMenu.getService_id());
			
			if(chosenMenu.getService_id()<1){//if this still looks like a sub-menu, we send to subscriber the sub menu and tell them how to subscribe

				chosenMenu = menu_controller.getMenuById(chosenMenu.getId());
				
				menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId());//update session
				
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
					outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+chosenMenu.enumerate()+cmpbean.getMessage(MAIN_MENU_ADVICE, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
				else
					outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+chosenMenu.enumerate()+cmpbean.getMessage(SUBSCRIPTION_ADVICE, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
				
			}else{
				
				
				logger.debug("subdto : "+subdto);
				if(subdto!=null)
					logger.debug("subdto : "+subdto.getSubscription_status());
				
				if(subdto==null || (subdto!=null && !subdto.getSubscription_status().equals(SubscriptionStatus.confirmed.toString()))){
				
					SMSService smsService = cmpbean.find(SMSService.class, new Long(chosenMenu.getService_id()));
					cmpbean.subscribe(MSISDN, smsService, chosenMenu.getId(),SubscriptionStatus.confirmed, SubscriptionSource.SMS,AlterationMethod.self_via_sms);//subscribe but marks as "confirmed"
					
					
					String response = cmpbean.getMessage(CONFIRMED_SUBSCRIPTION_ADVICE, language_id, incomingsms.getOpco().getId() ) ;
					if(response.indexOf(SERVICENAME_TAG)>=0)
						response = response.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
					if(response.indexOf(PRICE_TAG)>=0)
						response = response.replaceAll(PRICE_TAG, String.valueOf(outgoingsms.getPrice()));
					if(response.indexOf(KEYWORD_TAG)>=0)
						response = response.replaceAll(KEYWORD_TAG, outgoingsms.getSms());
					
					
					//this is sent out normally
					outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+response);//subscription confirmation
					
					if(!outgoingsms.getSms().startsWith("RM"))
						outgoingsms.setSms((RM.replaceAll(PRICE_TG, String.valueOf(outgoingsms.getPrice()))+outgoingsms.getSms()));//content piece
					
					//mosm_.setSMS_DataCodingId(incomingsms.getSMS_DataCodingId());
					
					sendMT(outgoingsms);
					
				}else{
					
					
					//Send them content for that service.
					//If celcom allows, we can send the content here, but it is Celcom's policy
					//that you don't charge a subscriber without warning. They must confim their subscription.
					
					//Already subscribed text
					String response = cmpbean.getMessage(MessageType.ALREADY_SUBSCRIBED_ADVICE, language_id, incomingsms.getOpco().getId() ) ;
					if(response.indexOf(SERVICENAME_TAG)>=0)
						response = response.replaceAll(SERVICENAME_TAG, chosenMenu.getName());
					if(response.indexOf(PRICE_TAG)>=0)
						response = response.replaceAll(PRICE_TAG, String.valueOf(outgoingsms.getPrice()));
					if(response.indexOf(KEYWORD_TAG)>=0)
						response = response.replaceAll(KEYWORD_TAG, outgoingsms.getSms());
					
					if(submenus_have_sub_menus)
						outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+response);//get all the sub menus there.
					else
						outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+response);//get all the sub menus there.
					
					
				}
			}
		
		}else{
			//Here check if subscriber sent valid keyword, fetch service, and subscribe then to that service.
			if(submenus_have_sub_menus)
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+current_menu.enumerate() + cmpbean.getMessage(MAIN_MENU_ADVICE, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
			else
				outgoingsms.setSms(RM.replaceAll(PRICE_TG, String.valueOf(incomingsms.getPrice()))+current_menu.enumerate() + cmpbean.getMessage(SUBSCRIPTION_ADVICE, language_id, incomingsms.getOpco().getId() ));//get all the sub menus there.
		
		}
		
		
		return outgoingsms;
	}

	@Override
	public void finalizeMe() {
		

		try{
			
			context.close();
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}
		
		try {
			
			ds.releaseConnectionPool();
			
		
		} catch (Exception e) {
			
			mo_processor_logger.error(e.getMessage(),e);
			
		}
		
	}
	
	
	
	
	

	public OutgoingSMS getContentFromServiceId(int service_id, String msisdn, boolean isSubscription) throws Exception {
		
		String s  = "::::::::::::::::::::::::::::::::::::::::::::::::::::";
		logger.info(s+" service_id["+service_id+"] msisdn["+msisdn+"]");
		SMSServiceDTO sm = cmpbean.getSMSservice(service_id);
		logger.info(s+sm);
		IncomingSMS incomingsms = null;
		OutgoingSMS outgoingsms = null;
		
		if(sm!=null){
			
			ServiceProcessorDTO procDTO = cmpbean.getServiceProcessor(sm.getMo_processor_FK());
			
			try {
				
				
				ServiceProcessorI processor =  MOProcessorFactory.getProcessorClass(procDTO.getProcessorClassName(), GenericServiceProcessor.class);
				incomingsms = new IncomingSMS();
				incomingsms.setCmp_tx_id(cmpbean.generateNextTxId()); 
				incomingsms.setMsisdn(msisdn);
				incomingsms.setPrice(sm.getPrice());
				incomingsms.setBilling_status(incomingsms.getPrice().compareTo(BigDecimal.ZERO)>0 ?  BillingStatus.WAITING_BILLING :   BillingStatus.NO_BILLING_REQUIRED);
				incomingsms.setShortcode(procDTO.getShortcode());
				incomingsms.setServiceid(Long.valueOf(sm.getId()));
				incomingsms.setSms(sm.getCmd());
				//added 22nd Dec 2014 - new customer requirement
				incomingsms.setPrice_point_keyword(sm.getPricePointKeyword());
				
				//added on 10th June 2013 but not tested
				incomingsms.setMoprocessor(cmpbean.find(MOProcessor.class, sm.getMo_processor_FK()));
				
				
				
				// **** Below is a Dirty hack. *****
				//To 
				//cheat the content processor 
				//that this is a subscription push, 
				//so that it does not subscribe 
				//this subscriber to the service. 
				//We handle subscription elsewhere, 
				//this is solely for content fetcnhing 
				//and not subscribing.
				incomingsms.setIsSubscription(isSubscription);
				
				outgoingsms = processor.process(incomingsms);
				
				
			}catch(Exception e) {
				logger.error(e.getMessage(),e);
			}
		}else{
			logger.info(s+" sm is null!");
		}
		
		
		return outgoingsms;
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
	
	

	
	@Override
	public CMPResourceBeanRemote getEJB() {
		return this.cmpbean;
	}
	
	
	
}
