package com.pixelandtag.subscription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.inmobia.util.SMSService;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.subscription.dto.SMSServiceDTO;
import com.pixelandtag.subscription.dto.SubscriptionDTO;
import com.pixelandtag.subscription.dto.SubscriptionStatus;


public class Subscription {
	
	private static final String DB_NAME = "pixeland_content360";
	private Logger logger  = Logger.getLogger(Subscription.class);
	private final String SPACE = " ";
	private final String NUM_SEPERATOR = "."+SPACE;
	private Object NEW_LINE = "\n";
	
	
	
	
	
	/**
	 * Updates a subscriber's subscription status
	 * @param conn
	 * @param subscription_id
	 * @param status - com.inmobia.celcom.subscription.dto.SubscriptionStatus
	 * @return
	 */
	public boolean updateSubscription(Connection conn, int subscription_id, SubscriptionStatus status){
		
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DB_NAME+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE id=?");
			pstmt.setString(1, status.getStatus());
			pstmt.setInt(2, subscription_id);
				
			success = pstmt.executeUpdate()>0;
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
	}
	
	
	/**
	 * Gets a subscription service dto for the given id.
	 * @param conn
	 * @param msisdn
	 * @param sms_service_id_fk
	 * @return
	 */
	public SubscriptionDTO getSubscriptionDTO(Connection conn, String msisdn, int sms_service_id_fk){
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		SubscriptionDTO subscription = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DB_NAME+"`.`subscription` WHERE msisdn=? AND sms_service_id_fk=? ORDER BY subscription_timeStamp asc LIMIT 1");
			pstmt.setString(1, msisdn);
			pstmt.setInt(2, sms_service_id_fk);
				
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				
				subscription = new SubscriptionDTO();
				subscription.setId(rs.getInt("id"));
				subscription.setSubscription_status(rs.getString("subscription_status"));
				subscription.setSms_service_id_fk(rs.getInt("sms_service_id_fk"));
				subscription.setMsisdn(rs.getString("msisdn"));
				subscription.setSubscription_timeStamp(rs.getString("subscription_timeStamp"));
				subscription.setSmsmenu_levels_id_fk(rs.getInt("smsmenu_levels_id_fk"));
					
			}
				
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return subscription;
	}
	
	
	
	/**
	 * checks if subscriber has any pending subscriptions
	 * @param conn
	 * @param msisdn
	 * @return
	 */
	public SubscriptionDTO checkAnyPending(Connection conn, String msisdn){
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		SubscriptionDTO subscription = null;
		
		try{
			
			pstmt = conn.prepareStatement("SELECT * FROM `"+DB_NAME+"`.`subscription` WHERE msisdn=? AND subscription_status='waiting_confirmation' ORDER BY subscription_timeStamp asc LIMIT 1");
			pstmt.setString(1, msisdn);
				
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				
				subscription = new SubscriptionDTO();
				subscription.setId(rs.getInt("id"));
				subscription.setSubscription_status(rs.getString("subscription_status"));
				subscription.setSms_service_id_fk(rs.getInt("sms_service_id_fk"));
				subscription.setMsisdn(rs.getString("msisdn"));
				subscription.setSubscription_timeStamp(rs.getString("subscription_timeStamp"));
				subscription.setSmsmenu_levels_id_fk(rs.getInt("smsmenu_levels_id_fk"));
					
			}
				
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return subscription;
	}
	
	
	
	/**
	 * 
	 * @param conn
	 * @param msisdn
	 * @param service_id
	 * @return
	 */
	public boolean subscribe(Connection conn,String msisdn, int service_id, int smsmenu_levels_id_fk, SubscriptionSource source){
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			if(service_id>-1){
				
				pstmt = conn.prepareStatement("INSERT INTO `"+DB_NAME+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk, request_medium,subscription_status) VALUES(?,?,?,?,?)");
				pstmt.setInt(1, service_id);
				pstmt.setString(2, msisdn);
				pstmt.setInt(3, smsmenu_levels_id_fk);
				pstmt.setString(4, source.toString());
				if(source==SubscriptionSource.WAP)
					pstmt.setString(5, SubscriptionStatus.confirmed.toString());
				else
					pstmt.setString(5, SubscriptionStatus.waiting_confirmation.toString());
					
				
				
				if(pstmt.executeUpdate()>0)
					success = true;
				
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
	}
	
	
	
	
	
	/**
	 * Inserts into the subscription table with the supplied status
	 * Status can be 
	 * SubscriptionStatus.waiting_confirmation
	 * SubscriptionStatus.confirmed
	 * SubscriptionStatus.unsubscribed
	 * 
	 * Watched a documentary once on aircraft and they said that an aircraft has very many redundant controls.... this makes me see this code as an aircraft.
	 * and I am an aircraft engineer... F*** yea!
	 * 
	 * @param conn
	 * @param msisdn
	 * @param service_id
	 * @return
	 */
	public boolean subscribe(Connection conn,String msisdn, int service_id, int smsmenu_levels_id_fk, SubscriptionStatus status,SubscriptionSource source){
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			if(service_id>-1){
				
				pstmt = conn.prepareStatement("INSERT INTO `"+DB_NAME+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk, request_medium,subscription_status) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE subscription_status = ?");
				pstmt.setInt(1, service_id);
				pstmt.setString(2, msisdn);
				pstmt.setInt(3, smsmenu_levels_id_fk);
				pstmt.setString(4, source.toString());
				pstmt.setString(5, status.toString());
				pstmt.setString(6, status.toString());
				
				if(pstmt.executeUpdate()>0)
					success = true;
				
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
	}
	
	/**
	 * Inserts record to subscription table, but subscription isn't confirmed untill subscriber sends "buy"
	 * @param conn
	 * @param msisdn
	 * @param service_id
	 * @return
	 */
	public boolean subscribe(Connection conn,String msisdn, int service_id, int smsmenu_levels_id_fk){
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			if(service_id>-1){
				
				pstmt = conn.prepareStatement("INSERT INTO `"+DB_NAME+"`.`subscription`(sms_service_id_fk,msisdn,smsmenu_levels_id_fk) VALUES(?,?,?) ON DUPLICATE KEY UPDATE subscription_status=?");
				pstmt.setInt(1, service_id);
				pstmt.setString(2, msisdn);
				pstmt.setInt(3, smsmenu_levels_id_fk);
				pstmt.setString(4, SubscriptionStatus.waiting_confirmation.getStatus());
				
				if(pstmt.executeUpdate()>0)
					success = true;
				
			}else{
				success = false;
			}
				
			
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
	}
	
	/**
	 * Gets SMSServiceDTO using primary key (id)
	 * @param service_id
	 * @param conn
	 * @return
	 */
	public SMSServiceDTO getSMSservice(int service_id,Connection conn){
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		SMSServiceDTO sm = null;
		try{
			pstmt = conn.prepareStatement("SELECT * FROM `"+DB_NAME+"`.`sms_service` WHERE `id`=?", Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, service_id);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				if(rs.isFirst())
					sm = new SMSServiceDTO();
				
				sm.setId(rs.getInt("id"));
				sm.setMo_processor_FK(rs.getInt("mo_processorFK"));
				sm.setCmd(rs.getString("cmd"));
				sm.setPush_unique(rs.getBoolean("push_unique"));
				sm.setService_name(rs.getString("service_name"));
				sm.setService_description(rs.getString("service_description"));
				sm.setPrice(rs.getDouble("price"));
				sm.setCmp_keyword(rs.getString("CMP_Keyword"));
				sm.setCmp_skeyword(rs.getString("CMP_SKeyword"));
				sm.setEnabled(rs.getBoolean("enabled"));
				sm.setSplit_mt(rs.getBoolean("split_mt"));
				sm.setPricePointKeyword(rs.getString("price_point_keyword"));
				
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		return sm;
	}
	
	
	/**
	 * 
	 * @param conn
	 * @param keyword
	 * @return
	 */
	public SMSServiceDTO getSMSservice(String keyword,Connection conn){
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		SMSServiceDTO sm = null;
		try{
			pstmt = conn.prepareStatement("SELECT * FROM `"+DB_NAME+"`.`sms_service` WHERE `cmd`=?", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, keyword);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				if(rs.isFirst())
					sm = new SMSServiceDTO();
				
				sm.setId(rs.getInt("id"));
				sm.setMo_processor_FK(rs.getInt("mo_processorFK"));
				sm.setCmd(rs.getString("cmd"));
				sm.setPush_unique(rs.getBoolean("push_unique"));
				sm.setService_name(rs.getString("service_name"));
				sm.setService_description(rs.getString("service_description"));
				sm.setPrice(rs.getDouble("price"));
				sm.setCmp_keyword(rs.getString("CMP_Keyword"));
				sm.setCmp_skeyword(rs.getString("CMP_SKeyword"));
				sm.setEnabled(rs.getBoolean("enabled"));
				sm.setSplit_mt(rs.getBoolean("split_mt"));
				
				//service_id = rs.getInt(1);
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		return sm;
	}
	
	
	
	/**
	 * For use mostly by WAP developers.
	 * @param conn
	 * @param msisdn
	 * @param service_id
	 * @return
	 */
	public boolean subscribe(Connection conn, String msisdn, int service_id){
		return subscribe(conn, msisdn, service_id, -1);
	}
	
	
	/**
	 * <code>For use mostly by WAP developers.
	 * @param conn
	 * @param msisdn
	 * @param service_id
	 * @param sub_source - com.inmobia.celcom.subscription.SubscriptionSource can be SubscriptionSource.SMS or SubscriptionSource.WAP
	 * @return true or false </code>
	 */
	public boolean subscribe(Connection conn, String msisdn, int service_id, SubscriptionSource sub_source){
		return subscribe(conn, msisdn, service_id, -1, sub_source);
	}
	
	/**
	 * Subscribes using keyword
	 * @param conn
	 * @param msisdn
	 * @param keyword
	 * @return
	 */
	public boolean subscribe(Connection conn, String msisdn, String keyword){
		
		SMSServiceDTO sm = getSMSservice(keyword,conn);
		
		boolean success = false;
		
		if(sm==null){
			
			success =  false;
		
		}else{
			
			int service_id = sm.getId();
			
			success =  subscribe(conn, msisdn, service_id, -1);
		
		}
		
		return success;
		
		
	}
	
	
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Subscription sub = new Subscription();
		boolean success = sub.subscribe(null,"012702341", "News");
	}





	public boolean unsubscribeAll(Connection conn, String msisdn, SubscriptionStatus status) {
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DB_NAME+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE MSISDN=?");
			pstmt.setString(1, status.getStatus());
			pstmt.setString(2, msisdn);
				
			success = pstmt.executeUpdate()>0;
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
		
	}


	public LinkedHashMap<Integer,SMSServiceDTO> getAllSubscribedServices(String msisdn, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String subscribed_services = "";
		LinkedHashMap<Integer,SMSServiceDTO> services = null;
		try{
			
			pstmt = conn.prepareStatement("SELECT group_concat(sms_service_id_fk) as 'sms_services_subscribed' from `"+DB_NAME+"`.`subscription` where  subscription_status='confirmed' AND msisdn=? ",Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, msisdn);
			rs = pstmt.executeQuery();
			
			if(rs.next())
				subscribed_services = rs.getString("sms_services_subscribed");
			
			rs.close();
			pstmt.close();
			
		
			pstmt = conn.prepareStatement(String.format("SELECT * FROM `"+DB_NAME+"`.`sms_service` WHERE `id` in(%s)",subscribed_services), Statement.RETURN_GENERATED_KEYS);
			rs = pstmt.executeQuery();
			
			SMSServiceDTO sm = null;
			
			int i = 0;
			while(rs.next()){
				
				if(rs.isFirst()){
					services = new LinkedHashMap<Integer,SMSServiceDTO>();
				}
				i++;
				sm = new SMSServiceDTO();
				
				sm.setId(rs.getInt("id"));
				sm.setMo_processor_FK(rs.getInt("mo_processorFK"));
				sm.setCmd(rs.getString("cmd"));
				sm.setPush_unique(rs.getBoolean("push_unique"));
				sm.setService_name(rs.getString("service_name"));
				sm.setService_description(rs.getString("service_description"));
				sm.setPrice(rs.getDouble("price"));
				sm.setCmp_keyword(rs.getString("CMP_Keyword"));
				sm.setCmp_skeyword(rs.getString("CMP_SKeyword"));
				sm.setEnabled(rs.getBoolean("enabled"));
				sm.setSplit_mt(rs.getBoolean("split_mt"));
				
				services.put(i,sm);
				
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		return services;
	}


	public String stringFyServiceList(LinkedHashMap<Integer,SMSServiceDTO> allsubscribed) {
		StringBuffer sb = null;
		if(allsubscribed!=null && allsubscribed.size()>0){
			sb = new StringBuffer();
			for (Entry<Integer, SMSServiceDTO> entry : allsubscribed.entrySet())
				sb.append(entry.getKey()).append(NUM_SEPERATOR).append(entry.getValue().getService_name()).append(NEW_LINE);
		}else{
			return null;
		}
		
		return sb.toString();
	}


	/**
	 * 
	 * Updates subscription using msisdn and service id
	 * @param conn
	 * @param sms_service_id_fk
	 * @param mSISDN
	 * @param status
	 * @return
	 */
	public boolean updateSubscription(Connection conn, int sms_service_id_fk, String msisdn,
			SubscriptionStatus status) {
		PreparedStatement pstmt = null;
		boolean success = false;
		
		try{
			
			pstmt = conn.prepareStatement("UPDATE `"+DB_NAME+"`.`subscription` SET subscription_status=?, subscription_timeStamp=CURRENT_TIMESTAMP WHERE sms_service_id_fk =? AND msisdn=?");
			pstmt.setString(1, status.getStatus());
			pstmt.setInt(2, sms_service_id_fk);
			pstmt.setString(3, msisdn);
			
			success = pstmt.executeUpdate()>0;
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			
		}
		
		
		return success;
	}

}
