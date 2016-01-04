package com.pixelandtag.cmp.ejb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.ERROR;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.api.MOProcessorFactory;
import com.pixelandtag.api.ServiceProcessorI;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterEJB;
import com.pixelandtag.cmp.entities.ClassStatus;
import com.pixelandtag.cmp.entities.HttpToSend;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.ProcessorType;
import com.pixelandtag.cmp.entities.SMSMenuLevels;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.SMSServiceMetaData;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.SubscriptionEvent;
import com.pixelandtag.dating.entities.SubscriptionHistory;
import com.pixelandtag.dynamic.dto.NoContentTypeException;
import com.pixelandtag.entities.MTsms;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.serviceprocessors.dto.ServiceProcessorDTO;
import com.pixelandtag.serviceprocessors.dto.ServiceSubscription;
import com.pixelandtag.serviceprocessors.dto.SubscriptionDTO;
import com.pixelandtag.sms.producerthreads.Billable;
import com.pixelandtag.sms.producerthreads.CleanupDTO;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.sms.producerthreads.TopUpNumber;
import com.pixelandtag.sms.producerthreads.USSDSession;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.smsmenu.Session;
import com.pixelandtag.subscription.SubscriptionSource;
import com.pixelandtag.subscription.dto.MediumType;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;
import com.pixelandtag.web.beans.MessageType;
import com.pixelandtag.web.beans.RequestObject;

@Stateless
@Remote
public class CMPResourceBean extends BaseEntityBean implements CMPResourceBeanRemote {
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private DatingServiceI datingserviceEJB;
	
	@EJB
	private DNDListEJBI dndEJB;
	
	private Random rand = new Random();
	
	private SimpleDateFormat formatDayOfMonth  = new SimpleDateFormat("d");
	
	public CMPResourceBean() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
	}
	
	
	private static int DEFAULT_LANGUAGE_ID = 1;
	private String MINUS_ONE = "-1";
	private final String RM1 = "RM1";
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@EJB
	private DatingServiceI datingBean;
	
	@EJB
	private SubscriptionBeanI subscriptionBean;
	
	
	@EJB
	private MessageEJBI messageEJB;
	
	public boolean markInQueue(Long http_to_send_id) throws Exception {
		
		boolean success = false;
		try {
			Query qry = em.createNativeQuery("UPDATE `"+database+"`.`httptosend` SET in_outgoing_queue = 1 WHERE id = :id_");
			qry.setParameter("id_", String.valueOf(http_to_send_id));
			qry.executeUpdate();
			success = true;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw new Exception(e.getMessage());
		
		}finally{
		}
		return success;
	}

	public MTsms getMTsms(Long id){
		MTsms mtsms = null;
		try {
			HttpToSend httpTosend = em.find(HttpToSend.class, id);
			mtsms = convertToLegacyMt(httpTosend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mtsms;
		
	}
	public MTsms convertToLegacyMt(HttpToSend httpTosend) {
		if(httpTosend==null)
			return null;
		MTsms mtsms = new MTsms();
		mtsms.setId(httpTosend.getId());
		mtsms.setSms(httpTosend.getSms());
		mtsms.setMsisdn(httpTosend.getMsisdn());
		mtsms.setType(httpTosend.getType());
		mtsms.setSendFrom(httpTosend.getSendfrom());
		mtsms.setPrice(httpTosend.getPrice());
		mtsms.setPriority(httpTosend.getPriority());
		mtsms.setServiceid(httpTosend.getServiceid().intValue());
		mtsms.setTimeStamp(sdf.format(httpTosend.getTimestamp()));
		mtsms.setFromAddr(httpTosend.getFromAddr());
		mtsms.setCharged(httpTosend.getCharged());
		mtsms.setCMP_AKeyword(httpTosend.getCMP_AKeyword());
		mtsms.setCMP_SKeyword(httpTosend.getCMP_SKeyword());
		mtsms.setAPIType(httpTosend.getApiType());
		mtsms.setNewCMP_Txid(httpTosend.getNewCMP_Txid());
		mtsms.setProcessor_id(httpTosend.getMo_processorFK());
		mtsms.setShortcode(httpTosend.getSendfrom());
		mtsms.setSubscription(httpTosend.getSubscription());
		//mtsms.setMT_STATUS(rs.getString("MT_STATUS"));
		
		//if(mtsms.getNewCMP_Txid().equals(mtsms.getCMP_Txid()))
		
		if(httpTosend.getSMS_DataCodingId()!=null)// && !rs.getString("apiType").equalsIgnoreCase("NULL"))
			mtsms.setSMS_DataCodingId(httpTosend.getSMS_DataCodingId()+"");
		
		if(httpTosend.getApiType()!=null)// && !rs.getString("apiType").equalsIgnoreCase("NULL"))
			mtsms.setAPIType(httpTosend.getApiType());
		
		
		//If receiver msisdn is null, then we take MSISDN as the msisdn to receive msg
		if(httpTosend.getSub_r_mobtel()==null){// || rs.getString("sub_r_mobtel").equalsIgnoreCase("NULL")){
			mtsms.setSUB_R_Mobtel(httpTosend.getMsisdn());
		}else{
			mtsms.setSUB_R_Mobtel(httpTosend.getSub_r_mobtel());
		}
		
		//If c_mobtel (msisdn to bill) is null, then we take MSISDN to be the msisdn to bill 
		if(httpTosend.getSub_c_mobtel()==null){// || rs.getString("sub_c_mobtel").equalsIgnoreCase("NULL")){
			mtsms.setSUB_C_Mobtel(httpTosend.getMsisdn());
		}else{
			mtsms.setSUB_C_Mobtel(httpTosend.getSub_c_mobtel());
		}
		
		
		if(httpTosend.getCMP_AKeyword()!=null)// && !rs.getString("CMP_AKeyword").equalsIgnoreCase("NULL") )
			mtsms.setCMP_AKeyword(httpTosend.getCMP_AKeyword());
		
		
		if(httpTosend.getCMP_SKeyword()!=null)// && !rs.getString("CMP_SKeyword").equalsIgnoreCase("NULL") )
			mtsms.setCMP_SKeyword(httpTosend.getCMP_SKeyword());
		
		
		if(httpTosend.getCMP_TxID()!=null){// && !rs.getString("CMP_TxID").equalsIgnoreCase("NULL") )
			mtsms.setCmp_tx_id(httpTosend.getCMP_TxID()); 
			logger.debug(">>>>>>>>>>>>>>>>>>>>>>: CMP_Txid "+httpTosend.getCMP_TxID());
		}
		
		
		if(httpTosend.getAction()!=null)// && !rs.getString("ACTION").equalsIgnoreCase("NULL") )
			mtsms.setAction(httpTosend.getAction());
		
		
		mtsms.setPricePointKeyword(httpTosend.getPrice_point_keyword());
		
		mtsms.setSplit_msg(httpTosend.getSplit());//whether to split msg or not..
		
		return mtsms;
	}
	/**
	 * 
	 * @param conn
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SMSServiceDTO getSMSservice(String keyword) throws Exception {
		
		SMSServiceDTO sm = null;
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `cmd`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, keyword);
			List<Object[]> obj = qry.getResultList();
			
			if(obj.size()>0){
				
				sm = new SMSServiceDTO();
				
				for(Object[] o : obj){
				
					sm.setId((Integer) o[0] );// rs.getInt("id"));//0
					sm.setMo_processor_FK(Long.valueOf(  ((Integer) o[1])).longValue() );//rs.getInt("mo_processorFK"));//1
					sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
					sm.setPush_unique( ((Integer) o[3]).compareTo(1)==0 );//rs.getBoolean("push_unique"));//3
					sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
					sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
					if(o[6]!=null)
						sm.setPrice(new BigDecimal((Double) (o[6])));//6
					//sm.setPrice((BigDecimal) (o[6]));//6
					sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
					sm.setCmp_keyword(  (o[8]!=null ? (String) o[8] : "IOD") );//rs.getString("CMP_Keyword"));//8
					sm.setCmp_skeyword( (o[9]!=null ? (String) o[9] : "IOD0000" ) );//rs.getString("CMP_SKeyword"));//9
					sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
					sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				}
				//service_id = rs.getInt(1);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
		}finally{}
		
		return sm;
	}
	
	
	public boolean unsubscribeAll(String msisdn, SubscriptionStatus status, AlterationMethod method)  throws Exception {
		
		boolean success = true;
		
		try{
			
			List<Subscription> subscriptions = subscriptionBean.listSubscriptions(msisdn);
			
			for(Subscription subscr : subscriptions){
				try{
					subscriptionBean.updateSubscription(subscr.getId().intValue(), status, method);
					subscr.setSubscription_status(status);
				}catch(Exception exp){
					logger.error(exp.getMessage());
					success = false;
				}
				
			}
			
			try{
				datingserviceEJB.deactivate(msisdn);
			}catch(Exception exp){
				logger.error(exp.getMessage(), exp);
			}
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
		
		return success;
		
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, SMSServiceDTO> getAllSubscribedServices(
			String msisdn) throws Exception{
		String subscribed_services = "";
		LinkedHashMap<Integer,SMSServiceDTO> services = null;
		try{
			String sql = "SELECT group_concat(sms_service_id_fk) as 'sms_services_subscribed' from `"+CelcomImpl.database+"`.`subscription` where  subscription_status='confirmed' AND msisdn=? ";
			Query qry1 = em.createNativeQuery(sql);
			
			qry1.setParameter(1, msisdn);
			Object rs = qry1.getSingleResult();
			
			if(rs!=null)
				subscribed_services =(String) rs;
			
			if(subscribed_services==null || subscribed_services.trim().isEmpty())
				return services;
			sql = String.format("SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `id` in(%s)",subscribed_services);
			Query qr2  = em.createNativeQuery(sql);
			List<Object[]> obj = qr2.getResultList();
			
			SMSServiceDTO sm = null;
			
			int i = 0;
			for(Object[] o : obj){
				
				if(i==0){
					services = new LinkedHashMap<Integer,SMSServiceDTO>();
				}
				i++;
				sm = new SMSServiceDTO();
				
				sm.setId((Integer) o[0] );// rs.getInt("id"));//0
				sm.setMo_processor_FK( Long.valueOf(((Integer) o[1]).longValue()) );//rs.getInt("mo_processorFK"));//1
				sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
				sm.setPush_unique( ((Integer) o[3]).compareTo(1)==0);//rs.getBoolean("push_unique"));//3
				sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
				sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
				if(o[6]!=null)
					sm.setPrice(new BigDecimal((Double) (o[6])));//6
				sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
				//sm.setCmp_keyword( (String) o[8] );//rs.getString("CMP_Keyword"));//8
				//sm.setCmp_skeyword((String) o[9] );//rs.getString("CMP_SKeyword"));//9
				sm.setCmp_keyword(  (o[8]!=null ? (String) o[8] : "IOD") );//rs.getString("CMP_Keyword"));//8
				sm.setCmp_skeyword( (o[9]!=null ? (String) o[9] : "IOD0000" ) );//rs.getString("CMP_SKeyword"));//9
				
				sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
				sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				
				services.put(i,sm);
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{}
		
		return services;
	}
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public com.pixelandtag.subscription.dto.SubscriptionDTO checkAnyPending(
			String msisdn) throws Exception {
		com.pixelandtag.subscription.dto.SubscriptionDTO subscription = null;
		
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`subscription` WHERE msisdn=? AND subscription_status='waiting_confirmation' ORDER BY subscription_timeStamp asc LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, msisdn);
				
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o: rs){
				
				subscription = new com.pixelandtag.subscription.dto.SubscriptionDTO();
					
				subscription.setId( (Integer) o[0] );// rs.getInt("id"));
				subscription.setSubscription_status( (String) o[1] );// rs.getString("subscription_status"));
				subscription.setSms_service_id_fk( (Integer) o[2] );//rs.getInt("sms_service_id_fk"));
				subscription.setMsisdn( (String) o[3] );//rs.getString("msisdn"));
				subscription.setSubscription_timeStamp(  sdf.format(( (java.sql.Timestamp) o[4]) ));//rs.getString("subscription_timeStamp"));
				subscription.setSmsmenu_levels_id_fk( (Integer) o[5] );// rs.getInt("smsmenu_levels_id_fk"));
				
					
			}
				
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return subscription;
		
	}
	
	@Override
	public SMSService getSMSservice(Long service_id) throws Exception{
		return em.find(SMSService.class, service_id);
	}
	
	@SuppressWarnings("unchecked")
	public SMSServiceDTO getSMSservice(int service_id) throws Exception{
		
		SMSServiceDTO sm = null;
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`sms_service` WHERE `id`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			List<Object[]> rs = qry.getResultList();
			
			int c = 0;
			if(rs.size()>0){
				for(Object[] o : rs){
					c++;
					if(c==1)
						sm = new SMSServiceDTO();
					
					sm.setId((Integer) o[0] );// rs.getInt("id"));//0
					sm.setMo_processor_FK(Long.valueOf( (Integer) o[1]).longValue() );//rs.getInt("mo_processorFK"));//1
					sm.setCmd((String) o[2] );//rs.getString("cmd"));//2
					sm.setPush_unique(((Integer) o[3]).compareTo(1)==0);//rs.getBoolean("push_unique"));//3
					sm.setService_name(  (String) o[4]  );//rs.getString("service_name"));//4
					sm.setService_description((String) o[5]  );//rs.getString("service_description"));//5
					if(o[6]!=null)
						sm.setPrice(new BigDecimal((Double) (o[6])));//6
					//sm.setPrice((BigDecimal) (o[6]));//6
					sm.setPricePointKeyword((String) o[7] );//rs.getString("price_point_keyword"));//7
					//sm.setCmp_keyword( (String) o[8] );//rs.getString("CMP_Keyword"));//8
					//sm.setCmp_skeyword((String) o[9] );//rs.getString("CMP_SKeyword"));//9
					sm.setCmp_keyword(  (o[8]!=null ? (String) o[8] : "IOD") );//rs.getString("CMP_Keyword"));//8
					sm.setCmp_skeyword( (o[9]!=null ? (String) o[9] : "IOD0000" ) );//rs.getString("CMP_SKeyword"));//9
					sm.setEnabled(((Boolean) o[10]));//rs.getBoolean("enabled"));//10
					sm.setSplit_mt((Boolean) o[11]);//rs.getBoolean("split_mt"));//11
				}
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return sm;
	}


	
	
	
	
	public boolean updateProfile(String msisdn, int language_id) throws Exception{
		boolean success = false;
		try {
			
			String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscriber_profile`(`msisdn`,`language_id`) VALUES(?,?) ON DUPLICATE KEY UPDATE `language_id`=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, language_id);
			qry.setParameter(3, language_id);
			success  = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try{
			
			}catch(Exception e1){}
			throw e;
			
		}finally{
			}
		return success;
	}
	
	public String getMessage(String key, int language_id, Long opcoid) throws Exception{//
		 Message message = messageEJB.getMessage(key, Long.valueOf(language_id), opcoid);
		 return message!=null ? message.getMessage() : null;
	}
	
	public int getSubscriberLanguage(String msisdn) throws Exception {
		
		int language_id = DEFAULT_LANGUAGE_ID;
		
		try {
			String sql = "SELECT language_id FROM `"+CelcomImpl.database+"`.`subscriber_profile` WHERE msisdn=?";
			Query qry = em.createNativeQuery(sql);
			
			qry.setParameter(1, msisdn);
			
			Object o = qry.getSingleResult();
			
			if(o!=null)
				language_id = ( (Integer) o ).intValue();
			else
				language_id =  DEFAULT_LANGUAGE_ID ;//default language
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage() + " no profile for subscriber"+msisdn);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw e;
			
		}finally{
		}
		
		return language_id;
	}
	
	public void printMenu(MenuItem menuItem, int level, int position)  throws Exception{
		String tab = "";
		for(int i=0;i<level;i++){
			tab +="\t"; //+ ( ( (i+1)==level   ) ? "+" : "") ;
		}
		
		String op = tab + position+ ". " + menuItem.getName();
		
		logger.debug(op);
		LinkedHashMap<Integer,MenuItem> mis = getSubMenus(menuItem.getId());
		if(mis!=null){
			level++;
			position = 1;
			for (Entry<Integer, MenuItem> entry : mis.entrySet()) {
				printMenu(entry.getValue(),level, position);
			 	position++;
			}
			
		}
	}
	public boolean updateSession(Long language_id, String msisdn,
			Long smsmenu_levels_id_fk) throws Exception{
		return updateSession(language_id.intValue(), msisdn,
				smsmenu_levels_id_fk.intValue());
	}
	
	public boolean updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk) throws Exception{
		boolean success = false;
		
		try{
			
			String UPDATE_SESSION = "INSERT INTO `"+CelcomImpl.database+"`.`smsmenu_session`(`msisdn`,`smsmenu_levels_id_fk`,`timeStamp`,`language_id`) VALUES(?,?,now(),?) ON DUPLICATE KEY UPDATE `smsmenu_levels_id_fk`=?,timeStamp=NOW(),language_id=?";
			Query qry = em.createNativeQuery(UPDATE_SESSION);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, smsmenu_levels_id_fk);
			qry.setParameter(3, language_id);
			qry.setParameter(4, smsmenu_levels_id_fk);
			qry.setParameter(5, language_id);
			
			success = (qry.executeUpdate()>0);
			
		}catch(Exception e){
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			
			throw e;
			
		}finally{
			
		}
		
		return success;
	}
	
	@SuppressWarnings("unchecked")
	public Session getSession(String msisdn) throws Exception{
		Session sess = null;
		
		try{
			
			final String GET_SESSION = "SELECT ses.*, sl.id as 'menu_level_id', sl.name, sl.language_id as 'language_id_',sl.parent_level_id, sl.menu_id, sl.serviceid, sl.visible FROM `"+CelcomImpl.database+"`.`smsmenu_session` ses LEFT JOIN `"+CelcomImpl.database+"`.`smsmenu_levels` sl ON sl.id = ses.smsmenu_levels_id_fk WHERE ses.`msisdn`=? AND ((TIMESTAMPDIFF(HOUR,ses.timeStamp,CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"')))<=24 )";
			Query qry = em.createNativeQuery(GET_SESSION);
			qry.setParameter(1, msisdn);
			List<Object[]> obj = qry.getResultList();
			
			MenuItem mi = null;
			
			int menu_level_id = -1;
			
			for(Object[] o : obj){
				sess = new Session();
				mi = new MenuItem();
				sess.setId((Integer) o[0]);//rs.getInt("id"));//0
				sess.setMsisdn((String) o[1]);//rs.getString("msisdn"));//1
				sess.setSmsmenu_level_id_fk((Integer) o[2]);//rs.getInt("smsmenu_levels_id_fk"));//2
				sess.setLanguage_id((Integer) o[3]);//rs.getInt("language_id"));//3
				sess.setTimeStamp( sdf.format(( (java.sql.Timestamp) o[4]) ));//4
				
				if(o[6]!=null)
					menu_level_id = (Integer) o[6];
				
				if(menu_level_id>0){//6
					mi.setId(menu_level_id);
					mi.setName((String) o[7]);//rs.getString("name"));//7
					mi.setLanguage_id((Integer) o[8]);//rs.getInt("language_id"));//8
					mi.setParent_level_id((Integer) o[9]);//rs.getInt("parent_level_id"));//9
					mi.setMenu_id((Integer) o[5]);//rs.getInt("menu_id"));//5
					mi.setService_id((Integer) o[11]);//rs.getInt("serviceid"));//11
					mi.setVisible(((Boolean) o[12]) );//rs.getBoolean("visible"));//12
					
					mi.setSub_menus(getSubMenus(menu_level_id));
					
					sess.setMenu_item(mi);
				}
				
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{}
		
		return sess;
	}
	
	
	@SuppressWarnings("unchecked")
	public MenuItem getMenuById(int menu_id) throws Exception{
		
		MenuItem mi = null;
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, menu_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(menu_id);
			
			//int x = 0;
			
			for(Object[] o : obj){
				//x++;
				
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		
		}
		
		return mi;
	}
	
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, MenuItem> getSubMenus(int parent_level_id_fk) throws Exception{
		
		LinkedHashMap<Integer,MenuItem> items = null;
		
		try{
			
			String GET_MENU_ITEMS = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id = ? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_ITEMS);
			qry.setParameter(1, parent_level_id_fk);
			
			List<Object[]> obj = qry.getResultList();
			
			int post = 0;
			
			MenuItem mi = null;
			
			for(Object[] o :obj ){
				post++;
				
				if(post==1)
					items = new LinkedHashMap<Integer,MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				items.put(post,mi);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
		}
		
		return items;
	}
	@SuppressWarnings("unchecked")
	public MenuItem getTopMenu(int menu_id, int language_id)  throws Exception{

		
		MenuItem mi = null;
		
		try{
		
			String GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE menu_id=? AND language_id=? AND  parent_level_id=-1 AND visible=1";
			Query qry = em.createNativeQuery(GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID);
			
			qry.setParameter(1, menu_id);
			qry.setParameter(2, language_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			
			for(Object[] o : obj){
				x++;
				
				if(x==1)
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				
				topMenus.put(x, mi);
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{}
		
		return mi;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public MenuItem getTopMenu(String keyword) throws Exception{
		
		MenuItem mi = null;
		try{
			String GET_TOP_MENU =  "select sml.* from `"+CelcomImpl.database+"`.`smsmenu_levels` sml left join `"+CelcomImpl.database+"`.`sms_service` sms on sms.id=sml.serviceid WHERE sms.cmd=?";
			Query qry = em.createNativeQuery(GET_TOP_MENU);
			qry.setParameter(1, keyword);
			
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o : rs){
				
				mi = new MenuItem();
				mi.setId(((Integer) o[0]).intValue());
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
			}
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return mi;
	}
	
	@SuppressWarnings("unchecked")
	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id, int menuid) throws Exception{
		
		MenuItem menuItem = null;
		try{
			String GET_TOP_MENU = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id=? AND language_id=? and menu_id=? and visible=1";
			Query qry = em.createNativeQuery(GET_TOP_MENU);
			qry.setParameter(1, parent_level_id);
			qry.setParameter(2, language_id);
			qry.setParameter(3, menuid);
			
			List<Object[]> rs = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			MenuItem mi = null;
			for(Object[] o : rs){
				x++;
				if(x==1){
					
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				}
				mi = new MenuItem();
				mi.setId(((Integer) o[0]).intValue());
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				topMenus.put(x, mi);
				
				
			}
			
			
				menuItem = getMenuById(parent_level_id);
				if(menuItem==null){
					menuItem = new MenuItem();
					menuItem.setId(parent_level_id);
				}
				menuItem.setParent_level_id(parent_level_id);
				menuItem.setLanguage_id(language_id);
				menuItem.setSub_menus(topMenus);
				menuItem.setMenu_id(menuid);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return menuItem;
	}
	
	/*@SuppressWarnings("unchecked")
	public MenuItem getMenuById(int parent_level_id) {
		MenuItem mi = null;
		
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, parent_level_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(parent_level_id);
			
			//int x = 0;
			
			for(Object[] o : obj){
				//x++;
				
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Integer) o[6]).compareTo(1)==0 );
				
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
		
			
		
		}
		
		return mi;
	}
*/
	

	
	public boolean deleteOldLogs() throws Exception{
		boolean success = false;
		
		try {
			
			String sql = "delete from `"+CelcomImpl.database+"`.`subscriptionlog` where date(CONVERT_TZ(timeStamp,'"+getServerTz()+"','"+getClientTz()+"'))<date(CONVERT_TZ(now(),'"+getServerTz()+"','"+getClientTz()+"'))";
			Query qry = em.createNativeQuery(sql);
			success = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return success;
	}
	
	public int getHourNow() throws Exception{
		int hour_now = -1;
		
		try {
			String sql = "SELECT hour(CONVERT_TZ(now(),'"+getServerTz()+"','"+getClientTz()+"')) as 'hour'";
			Query qry = em.createNativeQuery(sql);
			Object o  = qry.getSingleResult();
			hour_now = ( (BigInteger) o ).intValue();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return hour_now;
	}
	
	public int countSubscribers(int service_id) throws Exception{
		
		int count = -1;
		
		try {
			String sql = "SELECT count(*) FROM `"+CelcomImpl.database+"`.`subscription`  WHERE subscription_status='confirmed' AND sms_service_id_fk = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			Object o  = qry.getSingleResult();
			count = ( (BigInteger) o ).intValue();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return count;
	}
	
	
	public boolean logResponse(String msisdn, String responseText) throws Exception{
		boolean success = false;
		try {
			
			String sql = "INSERT DELAYED INTO `"+CelcomImpl.database+"`.`raw_out_log`(msisdn,response) VALUES(?,?)";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, responseText);
			success = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return success;
	}
	
	
	
	public boolean postponeMT(long http_to_send_id) throws Exception{
		
		boolean success = false;
		
		try {
			
			String sql = "UPDATE `"+CelcomImpl.database+"`.`httptosend` SET `sent` = 0, in_outgoing_queue=0 WHERE id = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, String.valueOf(http_to_send_id));
			success = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		return success;
	}
	
	
	
	public boolean logMT(MTsms mt) throws Exception{
		
		boolean success = false;
		
		try {
				
			
			 
			String sql = "INSERT INTO `"+CelcomImpl.database+"`.`messagelog`(CMP_Txid,MT_Sent,SMS_SourceAddr,SUB_Mobtel,SMS_DataCodingId,CMPResponse,APIType,CMP_Keyword,CMP_SKeyword,MT_STATUS,number_of_sms,msg_was_split,MT_SendTime,mo_ack,serviceid,price,newCMP_Txid,mo_processor_id_fk,price_point_keyword,subscription) " +
							"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'),1,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE MT_Sent = ?, mo_ack=1, MT_SendTime=CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'), MT_STATUS = ?, number_of_sms = ?, msg_was_split=?, serviceid=? , price=?, SMS_DataCodingId=?, CMPResponse=?, APIType=?, newCMP_Txid=?, CMP_SKeyword=?, mo_processor_id_fk=?, price_point_keyword=?,subscription=?";

			
			
			Query qry = em.createNativeQuery(sql);
			
			String txid = mt.getIdStr();
			if(!mt.getCmp_tx_id().isEmpty()){
				
				if(!(mt.getCmp_tx_id().equalsIgnoreCase("-1"))){
					txid = String.valueOf(mt.getCmp_tx_id());
					qry.setParameter(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
				}else{
					txid = mt.getIdStr();
					qry.setParameter(1, txid);//Since we're starting the transaction, what is in the httptosend as id now becomes the value of CMP_Txid.
				}
				
			}else{
				
				qry.setParameter(1, mt.getIdStr());
					txid = mt.getIdStr();
			}
			
			boolean isRetry = mt.getNewCMP_Txid()!=null ? (!mt.getNewCMP_Txid().equals(MINUS_ONE) ) : false;
			
			qry.setParameter(2, mt.getSms());
			qry.setParameter(3, mt.getFromAddr());//.getSMS_SourceAddr());//Shortcode sent with.
			qry.setParameter(4, mt.getMsisdn());//.getSUB_Mobtel());
			qry.setParameter(5, mt.getSMS_DataCodingId());//SMS_DataCodingId
			qry.setParameter(6, mt.getCMPResponse());//CMPResponse
			qry.setParameter(7, mt.getAPIType());//APIType,
			qry.setParameter(8, mt.getCMP_AKeyword());//CMP_Keyword
			qry.setParameter(9, mt.getCMP_SKeyword());//CMP_SKeyword
			qry.setParameter(10, mt.getMT_STATUS());//MT_STATUS
			qry.setParameter(11, mt.getNumber_of_sms());//number_of_sms
			qry.setParameter(12, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			qry.setParameter(13, mt.getServiceid());//serviceid
			if(mt.getPrice()==null)
				qry.setParameter(14, 1.0d);//price
			else
				qry.setParameter(14, mt.getPrice().doubleValue());//price
			qry.setParameter(15, mt.getNewCMP_Txid());//new CMPTxid
			qry.setParameter(16, mt.getProcessor_id());//processor id
			qry.setParameter(17, mt.getPricePointKeyword());//processor id
			qry.setParameter(18, (mt.isSubscription()  ? 1 : 0 ));// is subscription ?
			qry.setParameter(19, mt.getSms());//SMS
			
			if(isRetry)
				qry.setParameter(20, ERROR.PSAInsufficientBalance.toString());//MT_STATUS
			else
				qry.setParameter(20, mt.getMT_STATUS());//MT_STATUS
			
			qry.setParameter(21, mt.getNumber_of_sms());//number_of_sms
			qry.setParameter(22, (mt.isSplit_msg() ? 1 : 0));//number_of_sms
			qry.setParameter(23, mt.getServiceid());//serviceid
			
			if(mt.getPrice()==null)
				qry.setParameter(24, 1.0d);//price
			else
				qry.setParameter(24, mt.getPrice().doubleValue());//price
			
			qry.setParameter(25, mt.getSMS_DataCodingId());//SMS_DataCodingId
			qry.setParameter(26, mt.getCMPResponse());//CMPResponse
			qry.setParameter(27, mt.getAPIType());//APIType,
			qry.setParameter(28, mt.getNewCMP_Txid());//new CMPTxid
			
			qry.setParameter(29, mt.getCMP_SKeyword());//CMP_SKeyword
			
			qry.setParameter(30, mt.getProcessor_id());//CMP_SKeyword
			
			qry.setParameter(31, mt.getPricePointKeyword());//CMP_SKeyword
			qry.setParameter(32, (mt.isSubscription()  ? 1 : 0 ));// is subscription ?
			success = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			try{
				
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			
		}
		
		return success;
		
		
	}
	@Override
	public String getServiceMetaData(int serviceid, String meta_field)
			throws Exception {
		String value = "-1";
		
		try{
		
			String sql = "select meta_value from `"+CelcomImpl.database+"`.`sms_service_metadata` WHERE sms_service_id_fk=? AND meta_field=?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, serviceid);
			qry.setParameter(2, meta_field);
			Object rs = qry.getSingleResult();
		
			if(rs!=null){
				
				value = (String) rs;
				
			}else{
				throw new NoSettingException("No meta data with field name "+meta_field+", for serviceid "+serviceid);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
			throw e;
		
		}finally{
			
			
		}
		
		return value;
	}
	
	
	
	
	public boolean deleteMT(long id) throws Exception {
		
		boolean success = false;
		
		try {
			
			String sql = "DELETE FROM `"+CelcomImpl.database+"`.`httptosend` WHERE id = ?";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, String.valueOf(id));
			success = qry.executeUpdate()>0;
			
		} catch (Exception e) {
			try{
				
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
		return success;
		
		
	}
	

	@SuppressWarnings("unchecked")
	
	public String getUniqueFromCategory(String database_name,String table,
			String field,String idfield,String categoryfield,String categoryvalue,
			String msisdn,int serviceid,int size,Long processor_id) throws Exception{
		String retval=null;
		int contentid=0;
		
		try {
			
			String categories[] = categoryvalue.split(",");
			String where="";
			
			// Build the where statement for the prepared statement
			for( int i=0; i<categories.length; i++ ) {
				// Only use LIKE if there is a % in the category since "=" will use the Index on the table whereas LIKE will not 
				where += "s."+categoryfield+((categories[i].indexOf("%")>=0)?" LIKE ":"=")+"? " +
					( ( (i+1)<categories.length )?" OR ":"");
			}
			String sql = "SELECT s."+idfield+" AS id, s."+field+" AS txt, COUNT(log.id) AS cnt " +
					"FROM "+database_name+"."+table+" s "+
					"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id.intValue()+" AND log.serviceid = "+serviceid+" AND log.msisdn='"+msisdn+"' AND log.contentid=s.id ) " +
					"WHERE ( "+where+") "+
					"GROUP BY s.id "+
					"ORDER BY cnt ASC "+
					"LIMIT 0,1";
			Query qry = em.createNativeQuery(sql);
			for( int i=0; i<categories.length; i++ ) 
				qry.setParameter(i+1,categories[i]);
			
			List<Object[]> rs = qry.getResultList();
			if (rs!=null ) {
				for(Object[] o : rs){
					retval = (String) o[1];
					contentid = (Integer) o[0];
					int count = ( (BigInteger) o[2] ).intValue();
					if ( count>0 ) {
						String sql2 = "DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id.intValue()+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'";
						Query qry2 = em.createNativeQuery(sql2);
						qry2.executeUpdate();
					}
				}
				
			}
			
			if ( contentid>0 ) {
				String sql3="";
				try {
					sql3=(
						"INSERT DELAYED INTO "+database_name+".contentlog SET "+
						"processor_id="+processor_id.intValue()+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=now(), contentid="+contentid
					);
					Query qry3 = em.createNativeQuery(sql3);
					qry3.executeUpdate();
				} catch ( Exception e ) {
					logger.error(e+"\n"+sql+"\n",e);			
				}
			}
			
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch ( Exception e ) {
			try{
			
			}catch(Exception e1){}
			logger.error(e,e);
			throw e;
		} finally {
			
		}
		
		return retval;
	}
	
	
	public boolean subscribe(String msisdn, SMSService smsService, int smsmenu_levels_id_fk, SubscriptionStatus status,SubscriptionSource source, AlterationMethod method ) throws Exception{
		
		boolean success = false;
		
		try{
			
			if(smsService!=null && smsService.getId()>-1){
				
				final String TIMEUNIT = smsService.getSubscription_length_time_unit().toString();
				String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk, request_medium,subscription_status,expiryDate) VALUES(?,?,?,?,?, CONVERT_TZ(DATE_ADD(now(), INTERVAL ? "+TIMEUNIT+") , '"+getServerTz()+"','"+getClientTz()+"' ) ) ON DUPLICATE KEY UPDATE subscription_status = ?, renewal_count=renewal_count+1, expiryDate=CONVERT_TZ(DATE_ADD(now(), INTERVAL ?  "+TIMEUNIT+") , '"+getServerTz()+"','"+getClientTz()+"')";
				Query qry = em.createNativeQuery(sql);
				
				qry.setParameter(1, smsService.getId());
				qry.setParameter(2, msisdn);
				qry.setParameter(3, smsmenu_levels_id_fk);
				qry.setParameter(4, source.toString());
				qry.setParameter(5, status.toString());

				qry.setParameter(6, smsService.getSubscription_length());
				//qry.setParameter(7, smsService.getSubscription_length_time_unit().toString());
				
				qry.setParameter(7, status.toString());
				
				qry.setParameter(8, smsService.getSubscription_length());
				///qry.setParameter(10, smsService.getSubscription_length_time_unit().toString());
				
				
				if(qry.executeUpdate()>0)
					success = true;
				
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			try{
				
				}catch(Exception e1){}
			
			throw e;
		}finally{}
		
		
		return success;
	}
	
	
	
	
	public boolean subscribe(String msisdn, SMSService smsService, int smsmenu_levels_id_fk, MediumType medium, AlterationMethod method) throws Exception {
		boolean success = false;
		
		try{
			
			
			if(smsService!=null && smsService.getId()>-1){
				final String TIMEUNIT = smsService.getSubscription_length_time_unit().toString();
				String sql = "INSERT INTO `"+CelcomImpl.database+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk,expiryDate) VALUES(?,?,?,CONVERT_TZ(DATE_ADD(now(), INTERVAL ? "+TIMEUNIT+") , '"+getServerTz()+"','"+getClientTz()+"')) ON DUPLICATE KEY UPDATE subscription_status=?, renewal_count=renewal_count+1, expiryDate=CONVERT_TZ(DATE_ADD(now(), INTERVAL ? "+TIMEUNIT+") , '"+getServerTz()+"','"+getClientTz()+"')";
				Query qry = em.createNativeQuery(sql);
				
				qry.setParameter(1, smsService.getId());
				qry.setParameter(2, msisdn);
				qry.setParameter(3, smsmenu_levels_id_fk);
				qry.setParameter(4, smsService.getSubscription_length());
				//qry.setParameter(5, smsService.getSubscription_length_time_unit());
				qry.setParameter(5, SubscriptionStatus.waiting_confirmation.getStatus());
				qry.setParameter(6, smsService.getSubscription_length());
				//qry.setParameter(8, smsService.getSubscription_length_time_unit());
				
				
				if(qry.executeUpdate()>0){
					success = true;
					SubscriptionHistory sh = new SubscriptionHistory();
					sh.setAlteration_method(method);
					sh.setEvent(SubscriptionEvent.subscrition.getCode() );
					sh.setService_id(smsService.getId());
					sh.setTimeStamp(new Date());
					
					sh = em.merge(sh);
				}
				
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			try{
			
			}catch(Exception e1){}
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{
			
		}
		
		
		return success;
	}
	
	@SuppressWarnings("unchecked")
	public com.pixelandtag.subscription.dto.SubscriptionDTO getSubscriptionDTO(
			String msisdn, int sms_service_id_fk) throws Exception{
		
		com.pixelandtag.subscription.dto.SubscriptionDTO subscription = null;
		
		try{
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`subscription` WHERE msisdn=? AND sms_service_id_fk=? ORDER BY subscription_timeStamp asc LIMIT 1";
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, msisdn);
			qry.setParameter(2, sms_service_id_fk);
				
			List<Object[]> rs = qry.getResultList();
			
			for(Object[] o: rs){
				
				subscription = new com.pixelandtag.subscription.dto.SubscriptionDTO();
				subscription.setId( (Integer) o[0] );// rs.getInt("id"));
				subscription.setSubscription_status( (String) o[1] );// rs.getString("subscription_status"));
				subscription.setSms_service_id_fk( (Integer) o[2] );//rs.getInt("sms_service_id_fk"));
				subscription.setMsisdn( (String) o[3] );//rs.getString("msisdn"));
				subscription.setSubscription_timeStamp(  sdf.format(( (java.sql.Timestamp) o[4]) ));//rs.getString("subscription_timeStamp"));
				subscription.setSmsmenu_levels_id_fk( (Integer) o[5] );// rs.getInt("smsmenu_levels_id_fk"));
				subscription.setRenewal_count( (Integer) o[8] );// rs.getInt("renewal_count"));
					
			}
				
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			
			
		}
		
		
		return subscription;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAdditionalServiceInfo(int serviceid) throws Exception{
		Map<String, String> additionalInfo = null;
		
		
		try{
			String query = "SELECT "
					+ "service_name,"//0
					+ "subscriptionText,"//1
					+ "unsubscriptionText,"//2
					+ "tailText_subscribed,"//3
					+ "tailText_notsubscribed "//4
					+ "FROM `"+CelcomImpl.database+"`.`sms_service` WHERE id=?";
			Query qry = em.createNativeQuery(query);
			qry.setParameter(1, serviceid);
			
			List<Object[]> rs = qry.getResultList();
			int c = 0;
			for(Object[] o : rs){
			
				if(c==0)
					additionalInfo = new HashMap<String,String>();
				
				additionalInfo.put("subscriptionText", (String)o[1]);//rs.getString("subscriptionText"));
				additionalInfo.put("unsubscriptionText", (String)o[2]);//rs.getString("unsubscriptionText"));
				additionalInfo.put("tailText_subscribed", (String)o[3]);//rs.getString("tailText_subscribed"));
				additionalInfo.put("tailText_notsubscribed",(String)o[4]);//rs.getString("tailText_notsubscribed"));
				additionalInfo.put("service_name", (String)o[0]);//rs.getString("service_name"));
				c++;
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{
		
		}
		
		return additionalInfo;
	}
		
	public boolean testEJB(int k) throws Exception{
		if(k>0)
			return true;
		else
			throw new NoContentTypeException("No content Exception!! ");
	}
	
	
	public boolean updateServiceSubscription(int subscription_service_id)  throws Exception {
		boolean success = false;
		try{
			 
			 String sql = "UPDATE `"+GenericServiceProcessor.DB+"`.`ServiceSubscription` SET lastUpdated=convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') WHERE id =:id";
			Query qry = em.createNativeQuery(sql);	
			qry.setParameter("id", subscription_service_id);
			int num =  qry.executeUpdate();
			 
			 success = num>0;
		}catch(Exception e){
			try {
				
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return success;
	}
	
	
	
	
	public boolean shouldPushNow(int service_id) throws Exception{
		boolean resp = false;
		try {
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`ServiceSubscription` WHERE `serviceid`=? AND hour(`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `lastUpdated`<=convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND ExpiryDate>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";//first get services to be pushed now.
			Query qry = em.createNativeQuery(sql);
			qry.setParameter(1, service_id);
			if(qry.getResultList()!=null)
				resp = (qry.getResultList().size()>0 );//if a record comes, do push
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return resp;
	}
	
	@SuppressWarnings("unused")
	public int countPushesToday(int service_id) throws Exception{
		
		int count = -1;
		try {
			
			String sql = "SELECT "
					+ "count(*) as 'cnt'"
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status=:subscription_status "
							+ "AND sms_service_id_fk =:sms_service_id_fk "
							+ "AND id in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			String sqld = "SELECT "
					+ "count(*) as 'cnt'"
					+ "FROM `"+CelcomImpl.database+"`.`subscription` WHERE "
							+ "subscription_status='confirmed'"
							+ "AND sms_service_id_fk ='"+service_id+"'"
							+ "AND id in "
							+ "(SELECT subscription_id FROM `"+CelcomImpl.database+"`.`subscriptionlog` "
									+ "WHERE "
									+ "date(convert_tz(`timeStamp`,'"+getServerTz()+"','"+getClientTz()+"'))=date(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')))";
			
			
			Query qry = em.createNativeQuery(sql);
			qry.setParameter("subscription_status", "confirmed");
			qry.setParameter("sms_service_id_fk", service_id);
			Object res = qry.getSingleResult();
			count =  ( (BigInteger) res ).intValue();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return count;
	}
	
	
		
	@SuppressWarnings("unchecked")
	public List<ServiceSubscription> getServiceSubscription()  throws Exception {
		
		List<ServiceSubscription> list = new ArrayList<ServiceSubscription>();

		try {
			
			String sql = "SELECT * FROM `"+CelcomImpl.database+"`.`ServiceSubscription` WHERE hour(`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND `lastUpdated`<=convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND ExpiryDate>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";//first get services to be pushed now.
			//System.out.println(sql);
			Query qry = em.createNativeQuery(sql);
			List<Object[]> res = qry.getResultList();
			ServiceSubscription subdto;
			
			for(Object[] o : res){
				subdto = new ServiceSubscription();
				subdto.setId( ( (Integer) o[0]).intValue() );// rs.getInt("id"));
				subdto.setServiceid( ( (Integer) o[1]).intValue() );// rs.getInt("serviceid"));
				subdto.setSchedule( sdf.format(( (java.sql.Timestamp) o[2]) ));//rs.getString("schedule"));
				subdto.setLastUpdated( ( sdf.format((java.sql.Timestamp) o[3])) ) ;//rs.getString("lastUpdated"));
				subdto.setExpiryDate( sdf.format(( (java.sql.Timestamp) o[4]) ) );//rs.getString("ExpiryDate"));
				
				list.add(subdto);
			}
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally{
		}
		return list;
	}
	/**
	 * For Static content
	 * @param database_name
	 * @param table
	 * @param field
	 * @param idfield
	 * @param msisdn
	 * @param serviceid
	 * @param size
	 * @param processor_id
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	
	@SuppressWarnings("unchecked")
	public String getUnique(String database_name,String table,String field,String idfield,String msisdn,int serviceid,int size,int processor_id) throws Exception {
		
		
		String retval=null;
		int contentid=0;
		
		try {
			
			
			
			String[] fields = field.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fields.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append("s."+fields[i]+" AS " + fields[i]);
			}
			
			
			String sql = "SELECT s."+idfield+" AS id, "+sb.toString()+", COUNT(log.id) AS cnt " +
					"FROM "+database_name+"."+table+" s "+
					"LEFT JOIN "+database_name+".contentlog log ON ( log.processor_id = "+processor_id+" AND log.ServiceId = "+serviceid+" AND log.MSISDN='"+msisdn+"' AND log.contentid=s.id ) " +
					"GROUP BY s.id "+
					"ORDER BY cnt ASC "+
					"LIMIT 0,1";
	
			Query qry = em.createNativeQuery(sql);
			
			
			List<Object[]> objc = qry.getResultList();
			
			for(Object[] o : objc){
				for(int k = 0; k<o.length; k++){
					for (int i = 0; i < fields.length; i++)  {
						if (i == 0){
							retval = (String)o[k];
						}else{ 
							retval += " " + (String)o[k];
						}
					}
				}
				
				contentid = (Integer)o[0];
				int count =  (Integer)o[(o.length-1)];
				if ( count>0 ) {
					
					String sql2 = "DELETE FROM "+database_name+".contentlog WHERE processor_id="+processor_id+" AND serviceid="+serviceid+" AND msisdn='"+msisdn+"'";
					Query qry2 = em.createNativeQuery(sql2);
					qry2.executeUpdate();
				}
			}
			
			if ( contentid>0 ) {
				
				String sql3 = "INSERT DELAYED INTO "+database_name+".contentlog SET "+"processor_id="+processor_id+", serviceid="+serviceid+", msisdn='"+msisdn+"', timestamp=CURDATE(), contentid="+contentid;
				Query qry3 = em.createNativeQuery(sql3);
				qry3.executeUpdate();
			}
			
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch ( Exception e ) {
			try{
				
			}catch(Exception e1){
				//logger.error(e1,e1);
			}
			logger.debug("Language: " + database_name + " Table: " + table);
			logger.error(e,e);
			throw e;
		} finally {
			
		}
		
		return retval;		
	}

	private Logger logger = Logger.getLogger(CMPResourceBean.class);
	private String database = "pixeland_content360";
	
	
	@SuppressWarnings("unchecked")
	public <T> T find(Class<T> entityClass, String param_name, Object value) throws Exception  {
		T t = null;
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " WHERE "+param_name+" =:"+param_name+" ").setParameter(param_name, value);
			if(query.getResultList().size()>0)
				t = (T) query.getResultList().get(0);
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception  e){
			throw e;
		}
		return t;
	}
	
	
	public String getMessage(MessageType messageType,
			int language, Long opcoid) throws Exception {
		return getMessage(messageType.toString(), language, opcoid);
	}
	
	
	
	public boolean updateMessageInQueue(String cp_tx_id, BillingStatus billstatus) throws Exception{
		boolean success = false;
		try{
			 
			 Query qry = em.createNativeQuery("UPDATE httptosend set priority=:priority, charged=:charged, billing_status=:billing_status WHERE CMP_TxID=:CMP_TxID ")
			.setParameter("priority", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 0 :  3)
			.setParameter("charged", billstatus.equals(BillingStatus.SUCCESSFULLY_BILLED) ? 1 :  0)
			.setParameter("billing_status", billstatus.toString())
			.setParameter("CMP_TxID", cp_tx_id);
			int num =  qry.executeUpdate();
			 
			 success = num>0;
		}catch(Exception e){
			try {
				
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		return success;
	}
	
	
	
	
	@Override
	public boolean  acknowledge(long message_log_id) throws Exception{
		boolean success = false;
		try{
		 
			
			Query qry = em.createNativeQuery("UPDATE `"+CelcomImpl.database+"`.`messagelog` SET mo_ack=1 WHERE id=?");
			qry.setParameter(1, message_log_id);
			int num =  qry.executeUpdate();
			
			success = num>0;
		
		}catch(Exception e){
			try {
				
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
	}
	
	/**
	 * To statslog
	 */
	
	public boolean toStatsLog(IncomingSMS mo, String presql)  throws Exception {
		boolean success = false;
		try{
		 
			
			Query qry = em.createNativeQuery(presql);
			qry.setParameter(1, mo.getServiceid());
			qry.setParameter(2, mo.getMsisdn());
			qry.setParameter(3, mo.getCmp_tx_id());
			qry.setParameter(4, mo.getPrice().doubleValue());
			qry.setParameter(5, mo.getIsSubscription());
			
			int num =  qry.executeUpdate();
			
			success = num>0;
		
		}catch(Exception e){
			try {
				
			} catch (Exception e1) {
				logger.error(e1.getMessage(),e1);
			} 
			logger.error(e.getMessage(),e);
			throw e;
		}
		 
		return success;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<SubscriptionDTO> getSubscriptionServices()  throws Exception {
	
		String sub = "SELECT "
				+ "ss.id as 'service_subscription_id', "//0
				+ "pro.id as 'mo_processor_id_fk', "//1
				+ "pro.shortcode,"//2
				+ "pro.ServiceName,"//3
				+ "sm.cmd,"//4
				+ "sm.CMP_Keyword,"//5
				+ "sm.CMP_SKeyword,"//6
				+ "sm.price as 'price', "//7
				+ "sm.push_unique,"//8
				+ "ss.serviceid as 'sms_serviceid',"//9
				+ "pro.threads,"//10
				+ "pro.ProcessorClass as 'ProcessorClass',"//11
				+ "sm.price_point_keyword"//12
				+" FROM `"+CelcomImpl.database+"`.`ServiceSubscription` ss "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`sms_service` sm "
				+"ON sm.id = ss.serviceid "
				+"LEFT JOIN `"+CelcomImpl.database+"`.`mo_processors` pro "
				+"ON pro.id = sm.mo_processorFK WHERE pro.enabled=1 AND hour(`ss`.`schedule`)=hour(convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')) AND convert_tz(`ss`.`lastUpdated`,'"+getServerTz()+"','"+getClientTz()+"')<convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"') AND `ss`.`ExpiryDate`>convert_tz(now(),'"+getServerTz()+"','"+getClientTz()+"')";
	    logger.debug("\n\t"+sub+"\n");
		List<SubscriptionDTO> sub_services  = new ArrayList<SubscriptionDTO>();
	
		try {
			
			Query qry = em.createNativeQuery(sub);
			List<Object[]> list = qry.getResultList();
			SubscriptionDTO subdto = null;
			
			for(Object[] o : list){
				
				final int threads = ( (Integer) (o[10]) ).intValue() ;
				final int service_id = ( (Integer) (o[9]) ).intValue() ;
				final String service_processor_class_name = ( (String) (o[11]) );//rs.getString("ProcessorClass");
				
				for(int i = 0; i<threads; i++){
					
					
					logger.debug(" >>>>>>>>>>>>>>>>>>>>>>>>>>> service name >>>> "+(String) (o[3]));
				
					subdto = new SubscriptionDTO();
					subdto.setProcessor_id( Long.valueOf(( (Integer) (o[1]) ).longValue() +"") );//rs.getInt("mo_processor_id_fk"));
					subdto.setShortcode(   (String) (o[2]) );//rs.getString("shortcode"));
					subdto.setServiceName(  (String) (o[3]) );//rs.getString("ServiceName"));
					subdto.setCmd( (String) (o[4]) );//rs.getString("cmd"));
					subdto.setPrice(new Double((Double) (o[7])));
					subdto.setPush_unique(( (Integer) (o[8]) ).intValue());//rs.getInt("push_unique"));
					subdto.setServiceid(service_id);
					subdto.setThreads(threads);
					subdto.setProcessorClass(service_processor_class_name);
					subdto.setId( ( (Integer) (o[0]) ).intValue());//rs.getInt("service_subscription_id"));
					subdto.setPricePointKeyword((String) (o[12]) );//rs.getString("price_point_keyword"));
					
					
					sub_services.add(subdto);
					
				}
				
			
			}
			
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		} catch (Exception e) {
			try{
				
			}catch(Exception ex){}
			logger.error(e);
			throw e;
		}finally{
			
		}
		
		return sub_services;
	}

	public int invalidateSimilarBillables(Billable bill) throws Exception{
		int res = 0;
		try{
			
			Query qry =  em.createQuery("update Billable b set b.valid=:valid where "
					+ "b.msisdn=:msisdn "
					+ "AND b.service_id=:service_id  "
					+ "AND year(b.timeStamp)=year(:timestampC) AND month(b.timeStamp)=month(:timestampC) AND day(b.timeStamp)=day(:timestampC) AND b.success=1 "
					+ " AND b.id <> :id order by b.id desc");
			qry.setParameter("valid", Boolean.FALSE);
			qry.setParameter("msisdn", bill.getMsisdn());
			qry.setParameter("service_id", bill.getService_id());
			qry.setParameter("timestampC", bill.getTimeStamp());
			qry.setParameter("id", bill.getId());
			res = qry.executeUpdate();
			
			return res;
		}catch(javax.persistence.NoResultException ex){
			try{
				
			}catch(Exception exo){}
			logger.error(ex.getMessage());
			return 0;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<CleanupDTO> getCleanupDtos(Date date) throws Exception{
		
		List<CleanupDTO> cleanupDtos = new ArrayList<CleanupDTO>();
		
		try{
			Query qry =  em.createNativeQuery("select count(*) c, msisdn from billable_queue where date(timeStamp)=:dateS and success=1 and price>0 group by msisdn,service_id order by c desc");
			qry.setParameter("dateS", date);
			List<Object[]> dtoList = qry.getResultList();
			
			for(Object[] dtoObject : dtoList ){
				Long count = (Long) dtoObject[0];
				String msisdn = (String) dtoObject[1];
				Long service_id = (Long) dtoObject[2];
				
				CleanupDTO dto = new CleanupDTO();
				dto.setCount(count);
				dto.setMsisdn(msisdn);
				dto.setServiceid(service_id);
				cleanupDtos.add(dto);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
		return cleanupDtos;
		
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Billable> getBillableSForTransfer(Date date) throws Exception{
		try{
			Query qry =  em.createQuery("from Billable where year(timeStamp)=year(:timestampC) AND month(timeStamp)=month(:timestampC) AND day(timeStamp)=day(:timestampC) AND success=1 AND valid=1 AND price>0 order by msisdn desc");
			qry.setParameter("timestampC", date);
			return qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Billable> getBillableSForCleanup(Date date) throws Exception{
		try{
			Query qry =  em.createQuery("from Billable where year(timeStamp)=year(:timestampC) AND month(timeStamp)=month(:timestampC) AND day(timeStamp)=day(:timestampC) AND success=1 AND price>0 order by msisdn desc");
			qry.setParameter("timestampC", date);
			return qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
		}
		
	}

	@SuppressWarnings("unchecked")
	public List<Billable> getBillable(int limit) throws Exception{
		try{
			Query qry =  em.createQuery("from Billable where message_id IS NOT NULL AND message_id>-1 AND in_outgoing_queue=0 AND (retry_count<maxRetriesAllowed) AND resp_status_code is null AND price>0 AND  processed=0 and success=0 order by timeStamp desc,priority asc");
			qry.setFirstResult(0);
			qry.setMaxResults(limit);
			return qry.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			//close();
		}
		
	}


	public String topUp(RequestObject ro) throws USSDEception{
		//String kwd = "*222222222";
		final String keyword = ro.getKeyword();
		String cardnumber = keyword.split("\\*")[1];
		TopUpNumber tun = null;
		if(cardnumber==null || cardnumber.length()<9){
			return "";
		}
		
		String resp = "";
		try{
			Query query  = em.createQuery("from TopUpNumber tuc WHERE tuc.number=:number");
			query.setParameter("number", cardnumber);
			tun = (TopUpNumber) query.getSingleResult();
			
			if(tun.getDepleted()){
				resp = "Card number entered has been used already. Please purchase another voucher from our shops";
			}else{
				
				try{
					//make necessary call to operator
					if(tun.getTelco()==1){//Safaricom
						
					}else if(tun.getTelco()==2){//Airtel
						
					}else if(tun.getTelco()==3){//Orange
						
					}
					
					tun.setDepleted(true);
					
					tun = em.merge(tun);
					
					
					resp = "You have successfully topped up with KES "+tun.getValue()+". This is valid till xxxx";
				
				}catch(Exception exp){
					
					logger.error(exp.getMessage());
					resp = "Voucher not accepted at the moment, please try again.";
				
				}
				
			}
			
			
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new USSDEception("Problem processing card", exp);
		}
		
		return resp;
		
	}
	
	
	public String processUSSD(RequestObject req) throws USSDEception{
		String resp = "";
		
		try{
			
			String second_keyword = null;
			
			String KEYWORD = req.getKeyword().trim().toUpperCase();
			if(req.getMediumType() == MediumType.ussd){
				KEYWORD = req.getMsg().replaceAll(req.getCode(), "");
			}
			final String MSISDN = req.getMsisdn();
			int chosen = -1;
			boolean kw_is_digit = false;
			boolean second_keyword_is_digit = false;
			
			try{
				chosen = Integer.valueOf(KEYWORD);
				kw_is_digit = true;
			}catch(Exception e){
			}
			
			String msg[] = req.getMsg().trim().split("[\\s]");
			
			logger.debug(" GUGAMUG2 req.getMsg().trim() : = "+req.getMsg().trim());
			
			int msgL = msg.length;
			
			logger.debug(" GUGAMUG2 msgL : = "+msgL);
			
			try{
				second_keyword = msg[1].trim().toUpperCase();
			}catch(Exception e){
			}
			
			if(second_keyword==null){
				
				try{
					
					if(KEYWORD.indexOf("ON")>-1){
						msg = req.getMsg().toUpperCase().trim().split("ON");
						msgL = msg.length;
						logger.debug(" GUGAMUG msgL2 : = "+msgL);
						second_keyword = msg[1].trim().toUpperCase();
						KEYWORD = "ON";
					}
					
					if(KEYWORD.indexOf("STOP")>-1){
						msg = req.getMsg().toUpperCase().trim().split("STOP");
						msgL = msg.length;
						logger.debug(" GUGAMUG msgL2 : = "+msgL);
						second_keyword = msg[1].trim().toUpperCase();
						KEYWORD = "STOP";
					}
					
					if(KEYWORD.indexOf("BATAL")>-1){
						msg = req.getMsg().toUpperCase().trim().split("BATAL");
						msgL = msg.length;
						logger.debug(" GUGAMUG msgL2 : = "+msgL);
						second_keyword = msg[1].trim().toUpperCase();
						KEYWORD = "BATAL";
					}
					
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
				
			}
					
					logger.info("\t\t GUGAMUG second_keyword : = "+second_keyword);
					
					
					
					if(msgL>=2){
						try{
							chosen = Integer.valueOf(msg[1]);
							second_keyword_is_digit = true;
						}catch(Exception e){
							
						}
					}
					
					//conn = getCon();
					
					logger.info("\t\t KEYWORD ::::::::::::::::::::::::: ["+KEYWORD+"]");
					logger.info("\t\t MSG ::::::::::::::::::::::::: ["+msg+"]");
					logger.info("\t\t THE WORD AFTER KEYWORD ::::::::::::::::::::::::: ["+second_keyword+"]");
					logger.info("\t\t req.getMenuid() ::::::::::::::::::::::::: ["+req.getMenuid()+"]");
					
					USSDSession sess = getSession(req.getSessionid(),MSISDN);
					
					int smsmenu_level_id_fk = -1;
					
					int language_id = 1;// default language is always 1 
					int menuid = req.getMenuid()==-1 ? (req.getMediumType()==MediumType.ussd ? 2 : 1) : req.getMenuid();//first menu is always default
					logger.info("\t\t menuid ::::::::::::::::::::::::: ["+menuid+"]");
					if(sess!=null){
						smsmenu_level_id_fk = sess.getSmsmenu_levels_id_fk().intValue();
						language_id = sess.getLanguage_id().intValue();
						if(smsmenu_level_id_fk>-1){
							MenuItem mi = getMenuItem(Long.valueOf(smsmenu_level_id_fk+""));
							sess.setMenu_item(mi);
						}
					}else{
						try{
							language_id = getSubscriberLanguage(MSISDN);
						}catch(Exception exp){
							logger.error(exp);
						}
					}
					
					
					logger.info("\t\tsession :: "+sess);
					logger.info("\t\tsmsmenu_level_id_fk :: "+smsmenu_level_id_fk);
					logger.info("\t\tlanguage_id :: "+language_id);
					logger.info("\t\tmenuid :: "+menuid);
					
					MenuItem menu_from_session = null;
					
					if((smsmenu_level_id_fk>-1) && (language_id >-1) && sess.getMenu_item()!=null)
						menu_from_session = sess.getMenu_item();//menu_controller.getMenuById(smsmenu_level_id_fk,conn);
					else
						menu_from_session = getMenuByParentLevelId(language_id,smsmenu_level_id_fk,menuid);//get root menu
					
					logger.info("FROM SESSION___________________________"+menu_from_session);
					
					
					if( KEYWORD.contains("*") && req.getMediumType()==MediumType.ussd ){
					
						updateSession(language_id,MSISDN, menu_from_session.getMenu_id(),sess,menu_from_session.getParent_level_id(),req.getSessionid());//update session to upper menu.
						resp = menu_from_session.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE,language_id, req.getOpco().getId());
						return resp;
						
					}else if(KEYWORD.equalsIgnoreCase("") || KEYWORD.equalsIgnoreCase("MENU") ||  KEYWORD.equalsIgnoreCase("ORODHA")||  KEYWORD.equalsIgnoreCase("MORE") ||  KEYWORD.equalsIgnoreCase("ZAIDI")){
						
						updateSession(language_id,MSISDN, menu_from_session.getParent_level_id(),sess,menu_from_session.getMenu_id(),req.getSessionid());//update session to upper menu.
						MenuItem item = getMenuByParentLevelId(language_id,menu_from_session.getParent_level_id(),menuid);
						resp = item.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE,language_id, req.getOpco().getId());
						return resp;
					}else if(KEYWORD.equalsIgnoreCase("#")){
						
						updateSession(language_id,MSISDN, menu_from_session.getParent_level_id(),sess,menu_from_session.getMenu_id(),req.getSessionid());//update session to upper menu.
						MenuItem item = getMenuByParentLevelId(language_id,menu_from_session.getParent_level_id(),menuid);
						resp = (item.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId()));//get all the sub menus there.
						return resp;
					}else if(KEYWORD.equalsIgnoreCase("0")){
						
						updateSession(language_id, MSISDN, -1,sess,menu_from_session.getMenu_id(),req.getSessionid());//update session to upper menu.
						MenuItem item = getMenuByParentLevelId(language_id,-1,menuid);
						resp = (item.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId()));//get all the sub menus there.
						return resp;
					}else if(KEYWORD.equalsIgnoreCase("GIFT") || KEYWORD.equalsIgnoreCase("HIDIAH") || KEYWORD.equalsIgnoreCase("HADIAH")){
						
						logger.debug("\nIN  GIFT OR ENG KWD\n");
						
						language_id = KEYWORD.equalsIgnoreCase("GIFT") ?  1 : 2;
						int menu_id = 1;
						updateProfile(MSISDN,language_id);
						//UtilCelcom.updateProfile(mo.getMsisdn(),language_id,conn);
						
						updateSession(language_id,MSISDN, -1,sess,menu_from_session.getMenu_id(),req.getSessionid());//update session to upper menu.
						menu_from_session = getTopMenu(menu_id, language_id);
						
						resp = (menu_from_session.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId()));
						return resp;
					}else if(kw_is_digit){
						
						LinkedHashMap<Integer,MenuItem> submenus = menu_from_session.getSub_menus();
						
						boolean submenus_have_sub_menus = false;
						
						
						MenuItem chosenMenu = null;
						try{
							chosenMenu = menu_from_session.getMenuByPosition(chosen) ;
							chosenMenu = chosenMenu!=null ? getMenuById(chosenMenu.getId()) : null;
						}catch(ArrayIndexOutOfBoundsException arrin){}
						
						if(chosenMenu!=null && chosenMenu.getSub_menus()!=null && chosenMenu.getSub_menus().size()>0){
							LinkedHashMap<Integer,MenuItem>  submenu_ = chosenMenu.getSub_menus();
								if(submenu_!=null)
								for (Entry<Integer, MenuItem> entry : submenu_.entrySet()){
									if(!submenus_have_sub_menus)
									if(entry.getValue().getService_id()==-1){
										submenus_have_sub_menus = true;
									}
								}
								
								
						}else {
						
						
							
							if(KEYWORD.equals("1")  ){
								SMSService smsserv = em.find(SMSService.class, Long.valueOf(menu_from_session.getService_id()+""));
								if(smsserv!=null && 
										(smsserv.getCmd().equals("BILLING_SERV5")
										||
										smsserv.getCmd().equals("BILLING_SERV5")
										||
										smsserv.getCmd().equals("BILLING_SERV15")
										||
										smsserv.getCmd().equals("BILLING_SERV30")
										||
										smsserv.getCmd().equals("DATE")
										||
										smsserv.getCmd().equals("FIND")) ){
									
									MOProcessor proc = smsserv.getMoprocessor();
									
									IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
									incomingsms.setMsisdn(MSISDN);
									incomingsms.setServiceid(Long.valueOf(menu_from_session.getService_id()));
									incomingsms.setSms(smsserv.getCmd());
									incomingsms.setShortcode(proc.getShortcode());
									incomingsms.setPrice(smsserv.getPrice());
									incomingsms.setCmp_tx_id(generateNextTxId());
									incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
									incomingsms.setServiceid( Long.valueOf(smsserv.getId().intValue()));
									incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
									incomingsms.setOpco(req.getOpco());
									logger.info(">>>>>>>>>>>> req: "+req);
									logger.info(">>>>>>>>>>>> req.getMessageId(): "+req.getMessageId());
									incomingsms.setId(req.getMessageId());
									incomingsms.setMoprocessor(smsserv.getMoprocessor());
									
									
									logMO(incomingsms); 
									
									if(smsserv.getCmd().equals("BILLING_SERV5")
											||
											smsserv.getCmd().equals("BILLING_SERV5")
											||
											smsserv.getCmd().equals("BILLING_SERV15")
											||
											smsserv.getCmd().equals("BILLING_SERV30")
											||
											smsserv.getCmd().equals("DATE")){
										
										resp = "Your request to puchase chat bundles for one "+smsserv.getSubscription_length_time_unit().toString().toLowerCase()+" was received and will be processed shortly.";
										
										
									}else if(smsserv.getCmd().equals("FIND")){
										resp = "Request to find friend near your area received. You shall receive an sms shortly.";
									}else{
										resp = "Request received and is being processed.";
									}
									sess.setSessionId(req.getSessionid());
									clearUssdSesion(sess);
									//updateSession(language_id,MSISDN, -1,sess,menu_from_session.getMenu_id(),req.getSessionid());//update session to upper menu.
								
									return resp;
								}
							}/*else{
								sess.setSessionId(req.getSessionid());
								clearUssdSesion(sess);
								return "You can always purchase chat bundles any other time you like by dialing *329#";
							}*/
							
						}
						
					//	menu_controller.updateSession(language_id,MSISDN, chosenMenu.getId(), conn);//update sessi
						try{
						logger.info("submenus:: "+submenus);
						if(submenus!=null){
						logger.info("submenus.size():: "+submenus!=null?submenus.size():0);
						logger.info("submenus!=null && (chosen>submenus.size()) :: "+(submenus!=null && (chosen>submenus.size())));
						}else{
							logger.info("no submenus. that's ok");
						}
						}catch(Exception exp){}
						if( (submenus!=null && (chosen>submenus.size())) ){
							
							//chosenMenu = current_menu.getMenuByPosition(chosen);
							updateSession(language_id,MSISDN, chosenMenu.getId(),sess,menu_from_session.getMenu_id(),req.getSessionid());//update session
							
							if(submenus_have_sub_menus){
								resp = chosenMenu.enumerate() +getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId());//get all the sub menus there.
							}else{
								resp = chosenMenu.enumerate() +getMessage(GenericServiceProcessor.SUBSCRIPTION_ADVICE, language_id, req.getOpco().getId());//advice on how to subscribe
								
								SMSService smsserv = em.find(SMSService.class, Long.valueOf(chosenMenu.getId()+""));
								
								if(req.getMediumType()==MediumType.sms){
								
								}else if(req.getMediumType()==MediumType.ussd){
									resp = "THIS IS USSD";
								}
								subscribe(MSISDN, smsserv, chosenMenu.getId(), AlterationMethod.self_via_ussd);
								
							}
							
							return resp;
							
						}else{
							
							logger.info("chosenMenu:: "+chosenMenu);
							logger.info("chosenMenu.getService_id():: "+((chosenMenu!=null)?chosenMenu.getService_id():null));
							if(chosenMenu!=null){
								logger.info("(chosenMenu.getService_id()==-1 || chosenMenu.getService_id()<0 || chosenMenu.getId()==444 || chosenMenu.getId()==442) :: "+(chosenMenu.getService_id()==-1 || chosenMenu.getService_id()<0 || chosenMenu.getId()==444 || chosenMenu.getId()==442));
							}else{
								logger.info("chosen menu is null, that's ok too");
							}
							if( (chosenMenu!=null 
									&& (chosenMenu.getSub_menus()!=null)  
									&& 
									(chosenMenu.getService_id()<=0 
										|| (chosenMenu.getService_id()==444 
										||  (menu_from_session!=null && menu_from_session.getService_id()==444) ) 
									) 
								)
							 ){
								chosenMenu = getMenuById(chosenMenu.getId());
								String key = "";
								if(submenus_have_sub_menus){
									key = GenericServiceProcessor.MAIN_MENU_ADVICE;
								}else{
									key = GenericServiceProcessor.SUBSCRIPTION_ADVICE;
								}
								
								if(chosenMenu.getSub_menus()!=null){
									resp = chosenMenu.enumerate()+getMessage(key, language_id, req.getOpco().getId());//get all the sub menus there.
									updateSession(language_id,MSISDN, chosenMenu.getId(),sess,chosenMenu.getMenu_id(),req.getSessionid());//update session
								}else{
									resp =  menu_from_session.enumerate()+getMessage(key, language_id, req.getOpco().getId());//get all the sub menus there.
									updateSession(language_id,MSISDN, chosenMenu.getId(),sess,chosenMenu.getMenu_id(),req.getSessionid());//update session
								}
									
							
								
								return resp;
								
							}else{
								//Subscription subscr = getSubscription(MSISDN,Long.valueOf(chosenMenu.getService_id()));
								int serviceid = chosenMenu==null ? menu_from_session.getService_id() : chosenMenu.getService_id() ;
								int parent_level_id = chosenMenu==null ? menu_from_session.getParent_level_id() : chosenMenu.getId() ;
								int menuid_ = chosenMenu==null ? menu_from_session.getMenu_id() : chosenMenu.getMenu_id() ;
								SMSService smsserv = em.find(SMSService.class, Long.valueOf(serviceid+""));
								
								logger.info("\t\t\t:::::::::::::::::::::::::::::: serviceid:: "+serviceid+ " CMD :"+(smsserv!=null ? smsserv.getCmd() : null));
								
								if(smsserv.getCmd().equals("BILLING_SERV5")
										||
										smsserv.getCmd().equals("BILLING_SERV5")
										||
										smsserv.getCmd().equals("BILLING_SERV15")
										||
										smsserv.getCmd().equals("BILLING_SERV30")
										||
										smsserv.getCmd().equals("DATE")){
									//resp = "Your request to puchase chat bundles for one "+smsserv.getSubscription_length_time_unit().toString().toLowerCase()+" was received and will be processed shortly.";
									resp = "Please confirm purchase of one "+smsserv.getSubscription_length_time_unit().toString().toLowerCase()
											+" chat bundle @Sh"+smsserv.getPrice()
											+"\n1. Accept"
											+"\n2. Decline";
									
									
									
								}else{
									
									MOProcessor proc = smsserv.getMoprocessor();
									
									IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
									incomingsms.setMsisdn(MSISDN);
									incomingsms.setServiceid(Long.valueOf(menu_from_session.getService_id()));
									incomingsms.setSms(smsserv.getCmd());
									incomingsms.setShortcode(proc.getShortcode());
									incomingsms.setPrice(smsserv.getPrice());
									incomingsms.setCmp_tx_id(generateNextTxId());
									incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
									incomingsms.setServiceid(smsserv.getId());
									incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
									incomingsms.setId(req.getMessageId());
									incomingsms.setMoprocessor(proc);
									incomingsms.setOpco(req.getOpco());
									logger.info("\n\n\n\n\n::::::::::::::::proc.getId().intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");
									
									logMO(incomingsms);
									
									if(smsserv.getCmd().equals("FIND")){
										resp = "Request to find friend near your area received. You shall receive an sms shortly.";
									}else{
										resp = "Request received and is being processed.";
									}
								}
								updateSession(language_id,MSISDN, parent_level_id,sess,menuid_,req.getSessionid());//update session to upper menu.
							}
							
						}
						
					}else if(KEYWORD.equalsIgnoreCase(GenericServiceProcessor.SUBSCRIPTION_CONFIRMATION)){
						
						LinkedHashMap<Integer,MenuItem> submenu = menu_from_session.getSub_menus();
						
						boolean submenus_have_sub_menus = false;
						
						if(submenu!=null){
							for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
								if(entry.getValue().getSub_menus()!=null && entry.getValue().getSub_menus().size()>0){
									logger.error("GUGAMUGA  2 checking if we have sub menus >> "+entry.getValue().getName());
									submenus_have_sub_menus = true;
									break;
								}
							}
						}else{
							throw new NoSettingException("The menu with id "+menu_from_session.getId()+" Name=\""+menu_from_session.getName()+"\"has no children (sub menus)! Check the celcom_static_content.smsmenu_levels");
						}
						
						
						MenuItem chosenMenu = null;
						
						
						if(chosen>0){
							chosenMenu = menu_from_session.getMenuByPosition(chosen);
						}
						
						if((chosen>0) && chosenMenu!=null){
						
							chosenMenu = menu_from_session.getMenuByPosition(chosen);
							
							//final MOSms mosm_ =  cr.getContentFromServiceId(chosenMenu.getService_id(),MSISDN,conn);
						 
							SMSService smsserv = find(SMSService.class, Long.parseLong(chosenMenu.getService_id()+""));
							MOProcessor proc = smsserv.getMoprocessor();
							
							IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
							incomingsms.setMsisdn(MSISDN);
							incomingsms.setServiceid(Long.valueOf(chosenMenu.getService_id()));
							incomingsms.setSms(smsserv.getCmd());
							incomingsms.setShortcode(proc.getShortcode());
							incomingsms.setPrice(smsserv.getPrice());
							incomingsms.setMoprocessor(proc);
							incomingsms.setOpco(req.getOpco());
							logger.info("\n\n\n\n\n::::::::::::::::proc.getId().intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");
							
							logMO(incomingsms);
							//Mimic an MO
							
							final com.pixelandtag.subscription.dto.SubscriptionDTO subdto = getSubscriptionDTO(MSISDN, chosenMenu.getService_id());
							
							if(chosenMenu.getService_id()<1){//if this still looks like a sub-menu, we send to subscriber the sub menu and tell them how to subscribe

								chosenMenu = getMenuById(chosenMenu.getId());
								
								updateSession(language_id,MSISDN, chosenMenu.getId(),sess,menu_from_session.getMenu_id(),req.getSessionid());//update session
								
								submenu = chosenMenu.getSub_menus();
								
								if(submenu!=null){
									for (Entry<Integer, MenuItem> entry : submenu.entrySet()){
										if(entry.getValue().getSub_menus()!=null && entry.getValue().getSub_menus().size()>0){
											logger.error("GUGAMUGA  2 checking if we have sub menus >> "+entry.getValue().getName());
											submenus_have_sub_menus = true;
											break;
										}
									}
								}else{
									throw new NoSettingException("The menu with id "+chosenMenu.getId()+" has no children (sub menus)! Check the celcom_static_content.smsmenu_levels");
								}
								
								
								
								if(submenus_have_sub_menus)
									resp = chosenMenu.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId());//get all the sub menus there.
								else
									resp = chosenMenu.enumerate()+getMessage(GenericServiceProcessor.SUBSCRIPTION_ADVICE, language_id, req.getOpco().getId());//get all the sub menus there.
								
								
								return resp;
								
							}else{
								
								if(subdto==null || (subdto!=null && !subdto.getSubscription_status().equals(SubscriptionStatus.confirmed.toString()))){
									
									int service_id = chosenMenu.getService_id();
									
									SMSService smsService = find(SMSService.class, new Long(service_id));
									
									subscribe( MSISDN, smsService, chosenMenu.getId(),SubscriptionStatus.confirmed, SubscriptionSource.SMS,AlterationMethod.self_via_sms);//subscribe but marks as "confirmed"
									//subscription.subscribe(conn, MSISDN, chosenMenu.getService_id(), chosenMenu.getId(),SubscriptionStatus.confirmed, SubscriptionSource.SMS);//subscribe but marks as "confirmed"
									
									String response = getMessage(GenericServiceProcessor.CONFIRMED_SUBSCRIPTION_ADVICE, language_id, req.getOpco().getId()) ;
									if(response.indexOf(GenericServiceProcessor.SERVICENAME_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, chosenMenu.getName());
									if(response.indexOf(GenericServiceProcessor.PRICE_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.PRICE_TAG, String.valueOf(incomingsms.getPrice()));
									if(response.indexOf(GenericServiceProcessor.KEYWORD_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.KEYWORD_TAG, incomingsms.getSms());
									
									
									//this is sent out normally
									resp = response;//subscription confirmation
									
									return resp;
									
								}else{
									
									
									//Send them content for that service.
									//If celcom allows, we can send the content here, but it is Celcom's policy
									//that you don't charge a subscriber without warning. They must confim their subscription.
									
									//Already subscribed text
									String response = getMessage(MessageType.ALREADY_SUBSCRIBED_ADVICE, language_id, req.getOpco().getId()) ;
									if(response.indexOf(GenericServiceProcessor.SERVICENAME_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, chosenMenu.getName());
									if(response.indexOf(GenericServiceProcessor.PRICE_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.PRICE_TAG, String.valueOf(incomingsms.getPrice()));
									if(response.indexOf(GenericServiceProcessor.KEYWORD_TAG)>=0)
										response = response.replaceAll(GenericServiceProcessor.KEYWORD_TAG, incomingsms.getSms());
									
									if(submenus_have_sub_menus)
										resp = response;//get all the sub menus there.
									else
										resp = response;//get all the sub menus there.
									
									return resp;
								}
							}
						
						}else{
							//Here check if subscriber sent valid keyword, fetch service, and subscribe then to that service.
							if(submenus_have_sub_menus)
								resp = menu_from_session.enumerate() +getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE, language_id, req.getOpco().getId());//get all the sub menus there.
							else
								resp = menu_from_session.enumerate() + getMessage(GenericServiceProcessor.SUBSCRIPTION_ADVICE, language_id, req.getOpco().getId());//get all the sub menus there.
						
							return resp;
						}
						
					}else if(KEYWORD.equalsIgnoreCase(GenericServiceProcessor.SUBSCRIPTION_CONFIRMATION+GenericServiceProcessor.SPACE)){//This step is frozen by adding a space. Requested by Michael Juhl 20th June 2013
						
						
						//TODO - if a subscriber just sends "ON" or "BUY" Without the number, then, we give them the main menu, or the sub menu that they previously were in
						com.pixelandtag.subscription.dto.SubscriptionDTO sub =  checkAnyPending(MSISDN);
						
						if(sub!=null){
							
							subscriptionBean.updateSubscription(sub.getId(), SubscriptionStatus.confirmed, AlterationMethod.self_via_ussd);
							//subscription.updateSubscription(conn, sub.getId(), SubscriptionStatus.confirmed);
							
							MenuItem menu = getMenuById(sub.getSmsmenu_levels_id_fk());
							
							language_id = menu.getLanguage_id();
							
							logger.info("::::::::::::::::::::::::: serviceid:: "+menu.getService_id() + "\n\nmenu.toString():\n "+menu.toString()+"\n");
							final OutgoingSMS  outgoingsms  = getContentFromServiceId(menu.getService_id(),MSISDN,true);
							//final MOSms mosm_ =  cr.getContentFromServiceId(menu.getService_id(),MSISDN,conn);
							
							String response = getMessage(GenericServiceProcessor.CONFIRMED_SUBSCRIPTION_ADVICE, language_id, req.getOpco().getId()) ;
							if(response.indexOf(GenericServiceProcessor.SERVICENAME_TAG)>=0)
								response = response.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, menu.getName());
							if(response.indexOf(GenericServiceProcessor.PRICE_TAG)>=0)
								response = response.replaceAll(GenericServiceProcessor.PRICE_TAG, String.valueOf(outgoingsms.getPrice()));
							if(response.indexOf(GenericServiceProcessor.KEYWORD_TAG)>=0)
								response = response.replaceAll(GenericServiceProcessor.KEYWORD_TAG, outgoingsms.getSms());
							
							
							//this is sent out normally
							resp = response;
							
							return resp;
							
						}else{
							resp = getMessage(GenericServiceProcessor.NO_PENDING_SUBSCRIPTION_ADVICE,  language_id, req.getOpco().getId());
							return resp;
						}
						
					}else if(KEYWORD.equals("UNSUBSCRIBE") || KEYWORD.equals("STOP") || KEYWORD.equals("ST0P") || KEYWORD.equals("BATAL")){
						
						String msg1 = getMessage(MessageType.UNSUBSCRIBED_SINGLE_SERVICE_ADVICE, language_id, req.getOpco().getId());
						
						/*unsubscribeAll(MSISDN,SubscriptionStatus.unsubscribed,AlterationMethod.self_via_ussd);
						//subscription.unsubscribeAll(conn,MSISDN,SubscriptionStatus.unsubscribed);
						msg1 = getMessage(GenericServiceProcessor.UNSUBSCRIBED_ALL_ADVICE, language_id);
						msg1 = msg1.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, getMessage(MessageType.ALL_SERVICES, language_id));
						resp = GenericServiceProcessor.SPACE+msg1;
						return resp;
						*/
						/* Uncomment this block for graceful stop */
						int stop_number = -1;
						
						try{
							stop_number = Integer.valueOf(second_keyword);
						}catch(Exception e){}
						
						
						LinkedHashMap<Integer,SMSServiceDTO> allsubscribed  = getAllSubscribedServices(MSISDN);
						//LinkedHashMap<Integer,SMSServiceDTO> allsubscribed = subscription.getAllSubscribedServices(mo.getMsisdn(),conn);
						
						if(allsubscribed!=null){
						
							if(second_keyword!=null && (second_keyword.equalsIgnoreCase("all") || second_keyword.equalsIgnoreCase("semua"))){
								unsubscribeAll(MSISDN,SubscriptionStatus.unsubscribed,AlterationMethod.self_via_ussd);
								//subscription.unsubscribeAll(conn,MSISDN,SubscriptionStatus.unsubscribed);
								msg1 = getMessage(GenericServiceProcessor.UNSUBSCRIBED_ALL_ADVICE, language_id, req.getOpco().getId());
								msg1 = msg1.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, getMessage(MessageType.ALL_SERVICES, language_id, req.getOpco().getId()));
								resp = GenericServiceProcessor.SPACE+msg1;
								return resp;
								
							}else if(second_keyword!=null || stop_number>-1){
								
								if(second_keyword_is_digit){
												
									SMSServiceDTO toUnsubscribe = allsubscribed.get(stop_number);
									com.pixelandtag.subscription.dto.SubscriptionDTO subscription =  getSubscriptionDTO(MSISDN, toUnsubscribe.getId());
								   
									if(subscription!=null){
										subscriptionBean.updateSubscription(subscription.getId(), MSISDN,SubscriptionStatus.unsubscribed, AlterationMethod.self_via_ussd); 
										msg1 = msg1.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, toUnsubscribe.getService_name());
									}else{
										msg1 = getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, req.getOpco().getId());
									}
									
									resp = msg1;
												
								}else if(second_keyword!=null){
												
									SMSServiceDTO smsservice = getSMSservice(second_keyword);
									
									if(smsservice!=null){
									//SMSServiceDTO smsservice = subscription.getSMSservice(second_keyword, conn);
										com.pixelandtag.subscription.dto.SubscriptionDTO  subscription =  getSubscriptionDTO(MSISDN, smsservice.getId());
									    if(subscription!=null){
										    //if(subscription.updateSubscription(conn, smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed)){
									    	subscriptionBean.updateSubscription(subscription.getId(), MSISDN,SubscriptionStatus.unsubscribed, AlterationMethod.self_via_ussd);
										}else{
											msg1 = getMessage(MessageType.ALREADY_SUBSCRIBED_ADVICE, language_id, req.getOpco().getId()) ;
											}
									    
									
										//subscription.updateSubscription(conn, smsservice.getId(), MSISDN,SubscriptionStatus.unsubscribed);
									}else{
										msg1 = getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, req.getOpco().getId());
									}
									
									
									if(msg1.indexOf(GenericServiceProcessor.SERVICENAME_TAG)>=0)
										msg1 = msg1.replaceAll(GenericServiceProcessor.SERVICENAME_TAG, smsservice.getService_name());
									if(msg1.indexOf(GenericServiceProcessor.PRICE_TAG)>=0)
										msg1 = msg1.replaceAll(GenericServiceProcessor.PRICE_TAG, String.valueOf(smsservice.getPrice()));
									if(msg1.indexOf(GenericServiceProcessor.KEYWORD_TAG)>=0)
										msg1 = msg1.replaceAll(GenericServiceProcessor.KEYWORD_TAG, smsservice.getCmd());
								
												
								}
								
								resp = msg1;
										
							}else {
								msg1 = stringFyServiceList(allsubscribed)+getMessage(MessageType.INDIVIDUAL_UNSUBSCRIBE_ADVICE, language_id, req.getOpco().getId());
								resp = msg1;
							}
									
						}else{
							msg1 = getMessage(MessageType.NOT_SUBSCRIBED_TO_ANY_SERVICE_ADVICE, language_id, req.getOpco().getId());
							
						}
							
						resp = msg1;
						return resp;
						

					}else if(KEYWORD.equals("HELP")){
						
						String msg1 =  getMessage(MessageType.HELP, language_id, req.getOpco().getId());
						resp = msg1;
						
					}else if(KEYWORD.equals("INFO")){
						
						String msg1 =  getMessage(MessageType.INFO,language_id, req.getOpco().getId());
						resp = msg1;
					
					}else if(menu_from_session!=null && menu_from_session.getService_id()>-1){
						//updateSession(language_id,MSISDN, current_menu.getMenu_id(),sess,current_menu.getId(),req.getSessionid());//update session to upper menu.
						resp = menu_from_session.enumerate()+getMessage(GenericServiceProcessor.MAIN_MENU_ADVICE,language_id, req.getOpco().getId());
					}else{
						//Unknown keyword
						resp = getMessage(MessageType.UNKNOWN_KEYWORD_ADVICE, language_id, req.getOpco().getId());
					}

					
					logger.info("\n\n\t\t\tresp:::: "+resp);
				
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
	
		return resp;
		
	}
	
	
	
	public void clearUssdSesion(USSDSession sess) {
	
		if(sess.getId()!=null){
			try{
				
				Query qry = em.createQuery("from USSDSession s WHERE s.msisdn=:msisdn AND s.sessionId=:sessionId");
				qry.setParameter("msisdn", sess.getMsisdn());
				qry.setParameter("sessionId", sess.getSessionId());
				USSDSession s = (USSDSession) qry.getSingleResult();
				
				logger.debug(":::::::::::::::::::: Session : "+s);
				em.remove(s);
				
			}catch(Exception e){
				logger.error(e.getMessage(),e);
				try{
				
				}catch(Exception exp){}
			}
		}
		// TODO Auto-generated method stub
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public SMSMenuLevels getMenuItemFromUSSDTag(String ussdTag) throws Exception{
		SMSMenuLevels smLevels = null;
		Query qry = em.createQuery("from SMSMenuLevels sm WHERE sm.ussdTag=:ussdTag");
		qry.setParameter("ussdTag", ussdTag);
		List<SMSMenuLevels> lvs = qry.getResultList();
		if(lvs.size()>0)
			smLevels = lvs.get(0);
		return smLevels;
	}
	
	@SuppressWarnings("unchecked")
	public int getMenuIdFromUSSDTag(String ussdTag) throws Exception{
		int smLevels = 1;
		Query qry = em.createQuery("from SMSMenuLevels sm WHERE sm.ussdTag=:ussdTag");
		qry.setParameter("ussdTag", ussdTag);
		List<SMSMenuLevels> lvs = qry.getResultList();
		if(lvs.size()>0)
			smLevels = lvs.get(0).getMenu_id().intValue();
		return smLevels;
	}
	
	public void updateSession(int language_id, String msisdn,
			int smsmenu_levels_id_fk, USSDSession sess, int menuId, BigInteger sessionId) {
		if(sess==null)
			sess = new USSDSession();
		sess.setLanguage_id(Long.parseLong(language_id+""));
		sess.setMenuid(Long.parseLong(menuId+""));
		sess.setSessionId(sessionId);
		sess.setSmsmenu_levels_id_fk(Long.parseLong(smsmenu_levels_id_fk+""));
		sess.setMsisdn(msisdn);
		sess.setTimeStamp(new Date());
		try {
			sess = saveOrUpdate(sess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String stringFyServiceList(LinkedHashMap<Integer,SMSServiceDTO> allsubscribed) {
		StringBuffer sb = null;
		if(allsubscribed!=null && allsubscribed.size()>0){
			sb = new StringBuffer();
			for (Entry<Integer, SMSServiceDTO> entry : allsubscribed.entrySet())
				sb.append(entry.getKey()).append(GenericServiceProcessor.NUM_SEPERATOR).append(entry.getValue().getService_name()).append(GenericServiceProcessor.NEW_LINE);
		}else{
			return null;
		}
		
		return sb.toString();
	}
	
	
	@SuppressWarnings("unchecked")
	private MenuItem getMenuItem(Long smsmenu_level_id_fk) {
		MenuItem mi = null;
		try{
			Query qry = em.createNativeQuery("SELECT * from smsmenu_levels sm WHERE sm.id=:id");
			qry.setParameter("id", smsmenu_level_id_fk);
			List<Object[]> res = qry.getResultList();
			for(Object[] o : res){
				mi = new MenuItem();
				mi.setId(smsmenu_level_id_fk.intValue());;
				mi.setName((String)o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				mi.setSub_menus(getSubMenus(smsmenu_level_id_fk.intValue()) );
			}
		}catch(javax.persistence.NoResultException  nre){
			logger.error(nre.getMessage(),nre);
		}catch(Exception  exp){
			logger.error(exp.getMessage(),exp);
		}
		return mi;
	}
	@SuppressWarnings("unchecked")
	public USSDSession getSession(BigInteger sessionid, String msisdn) {
		USSDSession sess = null;
		try{
			Query qry = em.createQuery("from USSDSession s WHERE s.sessionId=:sessionId AND s.msisdn=:msisdn order by s.timeStamp desc");
			qry.setParameter("sessionId", sessionid);
			qry.setParameter("msisdn", msisdn);
			qry.setFirstResult(0);
			qry.setMaxResults(1);
			List<USSDSession> sessList = qry.getResultList();
			if(sessList.size()>0){
				sess = sessList.get(0);
			}
		}catch(javax.persistence.NoResultException  nre){
			logger.error(nre.getMessage(),nre);
		}catch(Exception  exp){
			logger.error(exp.getMessage(),exp);
		}
		return sess;
	}
	
	public boolean saveStaticSMS(String db_name, String table,
			String static_category_value, String sms) throws Exception{
		boolean success = false;
		try{
			
			Query qry2 = em.createNativeQuery("INSERT INTO `"+db_name+"`.`"+table+"`(Category,Text,timeStamp) VALUES(:category, :text, CONVERT_TZ(CURRENT_TIMESTAMP,'"+getServerTz()+"','"+getClientTz()+"'))");
			qry2.setParameter("category", static_category_value);
			qry2.setParameter("text", sms);
			
			success =  qry2.executeUpdate()>0;
			
		}catch(Exception exp){
			try{
				
			}catch(Exception ex){}
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
		return success;
	}
	@SuppressWarnings("unchecked")
	public List<SMSMenuLevels> listChildren(Long id)  throws Exception {
		List<SMSMenuLevels> list = null;
		try{
			Query qry = em.createQuery("from SMSMenuLevels sm where parent_level_id=:parent_level_id AND serviceid>-1");
			qry.setParameter("parent_level_id", id);
			list =  qry.getResultList();
		}catch(javax.persistence.NoResultException  nre){
			logger.error(nre.getMessage(),nre);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	
	public boolean updateSMSStatLog(String transaction_id, ERROR errorcode) throws Exception {
		
		boolean success = false;
		
		
		try {
			
			String sql;
			
			double priceTbc = 0.0d;
			boolean wasInLog = false;
			List<Object[]> rs2 =null;
			try{
			
				sql = "SELECT price,CMP_SKeyword FROM `"+CelcomImpl.database+"`.`SMSStatLog` WHERE transactionID = ?";
			  
				Query qry2 = em.createNativeQuery(sql);
				qry2.setParameter(1, transaction_id);
				
				rs2 = qry2.getResultList();
			
			}catch(javax.persistence.NoResultException ex){
				logger.warn(ex.getMessage() + " COULD NOT FIND SMSStatLog record with transactionID = "+transaction_id+")");
				
			}
			
			if(rs2!=null){
				
				for(Object[] o : rs2){
					priceTbc = new Double((Double) (o[0]));
					wasInLog  = true;
				}
				
				logger.debug("Statlog rec found transactionID = "+transaction_id);
			
			}else{
				
				logger.warn("Statlog rec with transactionID = "+transaction_id+ " NOT found! Try search celcom.messagelog");
				
				
				try {
					
					sql = "SELECT "
							+ "SUB_Mobtel,"//0
							+ "CMP_Keyword,"//1
							+ "CMP_SKeyword,"//2
							+ "serviceid,"//3
							+ "price "//4
							+ "from `"+CelcomImpl.database+"`.`messagelog` WHERE (CMP_Txid = ?) OR (newCMP_Txid = ?)";
					Query qry3 = em.createNativeQuery(sql);
					
					qry3.setParameter(1, transaction_id);
					qry3.setParameter(2, transaction_id);
					
					List<Object[]> rezults = qry3.getResultList();
					
					String msisdn = "UNKNOWN",CMP_Keyword= "UNKNOWN",CMP_SKeyword= "UNKNOWN";
					
					int serviceid = -1;
					
					if(rezults.size()>0){
						for(Object[] o :rezults){
							msisdn = (String) o[0];// rs.getString("SUB_Mobtel");
							CMP_Keyword = (String) o[1];//rs.getString("CMP_Keyword");
							CMP_SKeyword = (String) o[2];//rs.getString("CMP_SKeyword");
							serviceid =(Integer) o[3];// rs.getInt("serviceid");
							priceTbc = new Double((Double) o[4]);//TarrifCode.get(CMP_SKeyword.trim()).getPrice();
						}
					}
						
						String sql4 = "INSERT INTO `"+CelcomImpl.database+"`.`SMSStatLog`(SMSServiceID,msisdn,transactionID, CMP_Keyword, CMP_SKeyword,price,statusCode,charged) " +
								"VALUES(?,?,?,?,?,?,?,?)";
						Query qry4 = em.createNativeQuery(sql4);
						
						qry4.setParameter(1, serviceid);
						qry4.setParameter(2, msisdn);
						qry4.setParameter(3, transaction_id);
						qry4.setParameter(4, CMP_Keyword);
						qry4.setParameter(5, CMP_SKeyword);
						qry4.setParameter(6, priceTbc);
						qry4.setParameter(7, errorcode.toString());
						qry4.setParameter(8, (((priceTbc>0.0d) && errorcode.equals(ERROR.Success)) ? 1: 0 )   );
						
						success = qry4.executeUpdate()>0;
						
						logger.debug("______CMP_SKeyword="+CMP_SKeyword+"________cmpTxid = "+transaction_id+"________priceTbc = "+priceTbc+"_________________did not find in smsStatLog but no problem, we found in messagelog__________________________________________SUCCESSFULLY_INSERTED_INTO_SMSStatLog___________________________________________________________________________________");
						
						
					
					
				}catch(javax.persistence.NoResultException ex){
					try{
						
					}catch(Exception ex1){}
					logger.error(ex.getMessage(),ex);
				
				}catch (Exception e) {
					try{
						
					}catch(Exception ex1){}
					logger.error(e.getMessage(),e);
					throw e;
					
				}finally{}
				
			}
			
			
			
			//Only if the message exists in SMSStatLog
			if(wasInLog){
				
				
				String sql5 = "UPDATE `"+CelcomImpl.database+"`.`SMSStatLog` SET statusCode=?,  charged=?, statusCode=? WHERE  transactionID = ?";
				BillingStatus billStatus = BillingStatus.WAITING_BILLING;
				billStatus = errorcode.equals(ERROR.Success)  ? BillingStatus.SUCCESSFULLY_BILLED : billStatus;
				billStatus = errorcode.equals(ERROR.PSAInsufficientBalance)  ? BillingStatus.INSUFFICIENT_FUNDS : billStatus;
				billStatus = errorcode.equals(ERROR.InvalidSubscriber)  ? BillingStatus.BILLING_FAILED_PERMANENTLY : billStatus;
				billStatus = errorcode.equals(ERROR.PSAChargeFailure)  ? BillingStatus.BILLING_FAILED : billStatus;
				
				
				Query qry5 = em.createNativeQuery(sql5);
				
				qry5.setParameter(1, errorcode.toString());
				qry5.setParameter(2, (((priceTbc>0.0) && errorcode.equals(ERROR.Success)) ? 1: 0 )   );
				qry5.setParameter(3, billStatus.toString() );
				qry5.setParameter(4, transaction_id);
				//ERROR.PSAInsufficientBalance
				success = qry5.executeUpdate()>0;
				
				logger.debug("MT (transactionID = "+transaction_id+ ") " + (success ? "successfully logged into SMSStatLog" : "failed to log into SMSStatLog"));
				
			}
			
			
		}catch(javax.persistence.NoResultException ex){
			try{
				
			}catch(Exception e1){}
			logger.error(ex.getMessage(),ex);
		
		}  catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
			
		}finally{
		
		}
		
		return success;
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> find(Class<T> entityClass,
			Map<String, Object> criteria, int start, int end)  throws Exception {
		try{
			Query query = em.createQuery("from " + entityClass.getSimpleName()
		
				+ buildWhere(criteria));
			int counter1 = 0;
			for (String key : criteria.keySet()){
				counter1++;
				query.setParameter("param"+String.valueOf(counter1), criteria.get(key));			
			}			
			return query.getResultList();
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
			return null;
		}catch(Exception e){
			throw e;
		}
	}

	private String buildWhere(Map<String, Object> criteria)  throws Exception {
		StringBuffer sb = new StringBuffer();
		if (criteria.size() > 0)
			sb.append(" WHERE ");
		int counter2 = 0;
		for (String key : criteria.keySet()) {
			counter2++;
			sb.append(key).append("=:").append("param").append(String.valueOf(counter2))
					.append(criteria.size() == counter2 ? "" : " AND ");
		}
		return sb.toString();
	}
	
	

	public OutgoingSMS getContentFromServiceId(int service_id, String msisdn, boolean isSubscription) throws Exception {
		
		String s  = "::::::::::::::::::::::::::::::::::::::::::::::::::::";
		logger.info(s+" service_id["+service_id+"] msisdn["+msisdn+"]");
		SMSServiceDTO sm = getSMSservice(service_id);
		logger.info(s+sm);
		IncomingSMS incomingsms = null;
		OutgoingSMS outgoingsms = null;
		
		if(sm!=null){
			
			ServiceProcessorDTO procDTO = getServiceProcessor(sm.getMo_processor_FK());
			
			try {
				
				
				ServiceProcessorI processor =  MOProcessorFactory.getProcessorClass(procDTO.getProcessorClassName(), GenericServiceProcessor.class);
				incomingsms = new IncomingSMS();
				incomingsms.setCmp_tx_id(generateNextTxId());
				incomingsms.setMsisdn(msisdn);
				incomingsms.setPrice(sm.getPrice());
				incomingsms.setBilling_status(incomingsms.getPrice().compareTo(BigDecimal.ZERO)>0 ?  BillingStatus.WAITING_BILLING :   BillingStatus.NO_BILLING_REQUIRED);
				incomingsms.setShortcode(procDTO.getShortcode());
				incomingsms.setServiceid(Long.valueOf(sm.getId()));
				incomingsms.setSms(sm.getCmd());
				
				//added 22nd Dec 2014 - new customer requirement
				incomingsms.setPrice_point_keyword(sm.getPricePointKeyword());
				
				//added on 10th June 2013 but not tested
				incomingsms.setMoprocessor(em.find(MOProcessor.class, sm.getMo_processor_FK()));
				
				
				
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

	
	
	
	public static String hexToString(String txtInHex){
		
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
                txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }
	
	
	public String replaceAllIllegalCharacters(String text){
		
		if(text==null)
			return null;
		
		text = text.replaceAll("[\\r]", "");
		text = text.replaceAll("[\\n]", "");
		text = text.replaceAll("[\\t]", "");
		text = text.replaceAll("[.]", "");
		text = text.replaceAll("[,]", "");
		text = text.replaceAll("[?]", "");
		text = text.replaceAll("[@]", "");
		text = text.replaceAll("[\"]", "");
		text = text.replaceAll("[\\]]", "");
		text = text.replaceAll("[\\[]", "");
		text = text.replaceAll("[\\{]", "");
		text = text.replaceAll("[\\}]", "");
		text = text.replaceAll("[\\(]", "");
		text = text.replaceAll("[\\)]", "");
		text = text.trim();
		
		return text;
		
	}
	
	@Override
	public String getSubMenuString(String keyword, int language_id) throws Exception {
		MenuItem topMenu = getTopMenu(keyword);
		MenuItem item = null;
		if(topMenu!=null)
			item = getMenuByParentLevelId(language_id,topMenu.getId(),topMenu.getMenu_id());
		return item!=null ? item.enumerate() : null;
	}

	@Override
	public boolean subscribe(String mSISDN, SMSService smsService,
			int smsmenu_levels_id_fk, AlterationMethod method) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ServiceProcessorDTO> getServiceProcessors() {

		List<ServiceProcessorDTO>  services = null;
		
		try {

			Query qry = em.createQuery("SELECT "
					+ "osms.moprocessor.id, "//0
					+ "osms.moprocessor.serviceName, "//1
					+ "osms.moprocessor.processorClass, "//2
					+ "osms.moprocessor.enable, "//3
					+ "osms.moprocessor.class_status, "//4
					+ "osms.moprocessor.shortcode, "//5
					+ "osms.moprocessor.threads, "//6
					+ "osms.smsservice.subscriptionText , "//7
					+ "osms.smsservice.unsubscriptionText , "//8
					+ "osms.smsservice.tailText_subscribed , "//9
					+ "osms.smsservice.tailText_notsubscribed , "//10
					+ "osms.moprocessor.processor_type  , "//11
					+ "osms.moprocessor.forwarding_url  , "//12
					+ "osms.moprocessor.protocol , "//13
					+ "coalesce(osms.moprocessor.smppid,-1,osms.moprocessor.smppid)   "//14
				+ "FROM OpcoSMSService osms "
				+ "WHERE osms.moprocessor.enable=1 "
				+ "AND osms.moprocessor.processor_type <> :phantom_processor "
				+ "group by osms.moprocessor.id");
			
			qry.setParameter("phantom_processor", ProcessorType.PHANTOM);
			
			List<Object[]> rows = qry.getResultList();
			
			ServiceProcessorDTO service;
			
			int i = 0;
			
			for(Object[] row : rows){
				
				i++;
				
				service = new ServiceProcessorDTO();
				
				if(i==1){
					services = new ArrayList<ServiceProcessorDTO>();
				}
				
				
				service.setId( ((Long) row[0]).intValue()  );
				service.setServiceName(  (String) row[1] );
				service.setProcessorClass(  (String) row[2] );
				service.setActive(   ((Long) row[3]).intValue()==1 );
				service.setClass_status( ((ClassStatus) row[4]).name()  );
				service.setShortcode(  (String) row[5]  );
				service.setThreads( ((Long)row[6]).intValue() );
				service.setSubscriptionText( (String) row[7]  );
				service.setUnsubscriptionText( (String) row[8] );
				service.setTailTextSubscribed( (String) row[9] );
				service.setTailTextNotSubecribed(  (String) row[10] );
				service.setProcessor_type((ProcessorType) row[11]);
				service.setForwarding_url(   (String) row[12] );
				service.setProtocol(   (String) row[13] );
				service.setSmppid( (Long) row[14] );
				service.setServKey(service.getProcessorClassName()+"_"+service.getId()+"_"+"_"+service.getShortcode());
				
				services.add(service);
			}
			
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
		}
		
		return services;
	}

	@Override
	public List<SMSServiceMetaData> getSMSServiceMetaData(Long serviceid) {
		Query qry = em.createQuery("from SMSServiceMetaData smd WHERE sms_service_id_fk=:sms_service_id_fk");
		qry.setParameter("sms_service_id_fk", serviceid);
		List<SMSServiceMetaData> metaData = qry.getResultList();
		return metaData;
	}

	@Override
	public BigInteger count(String db_name, String table,
			String static_category_value) {
		Query qry2 = em.createNativeQuery("SELECT count(*) FROM `"+db_name+"`.`"+table+"` WHERE Category=:category");
		qry2.setParameter("category", static_category_value);
		Object obj = qry2.getSingleResult();
		BigInteger size = (BigInteger) obj;
		return size;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> listContent(String db_name, String table,
			String static_category_value, int start, int limit) {
		Query qry2 = em.createNativeQuery("SELECT id,Text,timeStamp FROM `"+db_name+"`.`"+table+"` WHERE Category=:category");
		qry2.setParameter("category", static_category_value);
		qry2.setFirstResult(start);
		qry2.setMaxResults(limit);
		List<Object[]> list = qry2.getResultList();
		return list; 
	}

	@Override
	@SuppressWarnings("unchecked")
	public Date listContent(String db_name, String table,
			String static_category_value, String sms) {
		Query qry2 = em.createNativeQuery("SELECT timeStamp FROM `"+db_name+"`.`"+table+"` WHERE Text=:text AND Category=:category");
		qry2.setParameter("text", sms);
		qry2.setParameter("category", static_category_value);
		List<Object> objects = qry2.getResultList();
		if(objects!=null && objects.size()>0)
			return (Date) objects.get(0);
		return null;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SMSMenuLevels> getSMSMenuLevels() {
		Query qry = em.createQuery("from SMSMenuLevels sm WHERE sm.serviceid=-1");
		return qry.getResultList();
	}

	@Override
	public String getBillingStats(String fromTz, String toTz) throws JSONException {

		Query qry = em.createNativeQuery("select date(convert_tz(timeStamp,'"+fromTz+"','"+toTz+"')) dt, count(*) count, price, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit 31");
		
		List<Object[]> recs = qry.getResultList();
		
		JSONObject mainObject = new JSONObject();
		
		JSONObject data = new JSONObject();
		JSONArray labels =  new JSONArray();
		JSONArray dataArray = new JSONArray();
		JSONArray datasets =  new JSONArray();
		
		for(Object[] o : recs){
			Date date = (Date) o[0];
			BigInteger count = (BigInteger) o[1];
			BigDecimal total_kshs = (BigDecimal) o[3];
			
			
			labels.put(convertToPrettyFormat(date));
			dataArray.put(total_kshs.longValue());
			
		}
		data.put("data", dataArray);
		data.put("fillColor", "rgba(151,187,205,1)");
		data.put("strokeColor", "rgba(151,187,205,0.8)");
		data.put("highlightFill", "rgba(151,187,205,0.75)");
		data.put("highlightStroke", "rgba(151,187,205,1)");
		datasets.put(data);
		
		

		mainObject.put("datasets", datasets);
		mainObject.put("labels", labels);
		return mainObject.toString();
	}
	
	
	public String convertToPrettyFormat(Date date){
		int day = Integer.parseInt(formatDayOfMonth.format(date));
		String suff  = TimezoneConverterEJB.getDayNumberSuffix(day);
		DateFormat prettier_df = new SimpleDateFormat("d'"+suff+"' E");
	    return prettier_df.format(date);
	}

	@Override
	public String getCurrentSubDistribution() throws Exception{

		Query qry = em.createNativeQuery("select  count(*) count, sms.service_name as 'service_name' from subscription sub left join sms_service sms on sms.id = sub.sms_service_id_fk where sub.subscription_status='confirmed' group by sms.service_name");
		
		List<Object[]> recs = qry.getResultList();
		
		JSONObject data = new JSONObject();
		JSONArray dataArray = new JSONArray();
		
		for(Object[] o : recs){
			
			BigInteger count = (BigInteger) o[0];
			String service_name = (String) o[1];
			
			String colorHex = getRandomHexColor();
			JSONObject dataPiece = new JSONObject();
			dataPiece.put("value", count);
			dataPiece.put("label", service_name);
			dataPiece.put("color", "#"+colorHex);
			dataPiece.put("highlight", "#"+incrementColor(colorHex,50));
			
			dataArray.put(dataPiece);
			
		}
		data.put("data", dataArray);
		
		return data.toString();
	}

	private String incrementColor(String hexVal, int increment) {
		int value = Integer.parseInt(hexVal, 16);
		value += increment;
		return Integer.toHexString(value);
	}
	

	private String getRandomHexColor() {
		
	    char letters[] = "0123456789ABCDEF".toCharArray();
	    String color = "";
	    for (int i = 0; i < 6; i++ ) {
	        color += letters[(rand.nextInt((15 - 0) + 1) + 0)];
	    }
	    return color;
	}

}
