package com.pixelandtag.smsmenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.pixelandtag.dynamic.dbutils.DBConnection;

public class MenuController {
	
	private Logger logger = Logger.getLogger(MenuController.class);
	
	
	private final String DB_NAME = "celcom_static_content";
	public static final String NUM_SPACER = ". ";
	private final String GET_SESSION = "SELECT ses.*, sl.id as 'menu_level_id', sl.name, sl.language_id as 'language_id_',sl.parent_level_id, sl.menu_id, sl.serviceid, sl.visible FROM `"+DB_NAME+"`.`smsmenu_session` ses LEFT JOIN `"+DB_NAME+"`.`smsmenu_levels` sl ON sl.id = ses.smsmenu_levels_id_fk WHERE ses.`msisdn`=? AND ((TIMESTAMPDIFF(HOUR,ses.timeStamp,CONVERT_TZ(CURRENT_TIMESTAMP,'+8:00','+8:00')))<=24 )";
	private final String GET_MENU_ITEMS = "SELECT * FROM `"+DB_NAME+"`.`smsmenu_levels` WHERE parent_level_id = ? AND visible=1";
	private final String UPDATE_SESSION = "INSERT INTO `"+DB_NAME+"`.`smsmenu_session`(`msisdn`,`smsmenu_levels_id_fk`,`timeStamp`,`language_id`) VALUES(?,?,now(),?) ON DUPLICATE KEY UPDATE `smsmenu_levels_id_fk`=?,timeStamp=NOW(),language_id=?";
	private final String GET_MENU_BY_ID = "SELECT * FROM `"+DB_NAME+"`.`smsmenu_levels` WHERE id=? AND visible=1";
	private final String GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID = "SELECT * FROM `"+DB_NAME+"`.`smsmenu_levels` WHERE menu_id=? AND language_id=? AND  parent_level_id=-1 AND visible=1";
	private final String GET_TOP_MENU = "SELECT * FROM `"+DB_NAME+"`.`smsmenu_levels` WHERE parent_level_id=? AND language_id=? and visible=1";
	
	
	/**
	 * Gets top Menu.
	 * @param conn
	 * @return
	 */
	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id,Connection conn){
		
		MenuItem menuItem = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement(GET_TOP_MENU,Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, parent_level_id);
			pstmt.setInt(2, language_id);
			
			rs = pstmt.executeQuery();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			MenuItem mi = null;
			while(rs.next()){
				x++;
				if(rs.isFirst()){
					
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				}
				mi = new MenuItem();
				mi.setId(rs.getInt("id"));
				mi.setName(rs.getString("name"));
				mi.setLanguage_id(rs.getInt("language_id"));
				mi.setParent_level_id(rs.getInt("parent_level_id"));
				mi.setMenu_id(rs.getInt("menu_id"));
				mi.setService_id(rs.getInt("serviceid"));
				mi.setVisible(rs.getBoolean("visible"));
				topMenus.put(x, mi);
				
				
			}
			
			
				menuItem = getMenuById(parent_level_id, conn);
				if(menuItem==null){
					menuItem = new MenuItem();
					menuItem.setId(parent_level_id);
					menuItem.setParent_level_id(parent_level_id);
					menuItem.setLanguage_id(language_id);
				}
				menuItem.setSub_menus(topMenus);
			
		
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
		
		return menuItem;
	}
	
	
	
	/*public MenuItem getMenuByParentLevelId(){
		
	}*/
	
	
	public MenuItem getTopMenu(int menu_id, int language_id, Connection conn) {
		MenuItem mi = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement(GET_TOP_MENU_BY_MENU_ID_AND_LANGUAGE_ID,Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, menu_id);
			pstmt.setInt(2, language_id);
			
			rs = pstmt.executeQuery();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			
			while(rs.next()){
				x++;
				
				if(rs.isFirst())
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				
				mi = new MenuItem();
				mi.setId(rs.getInt("id"));
				mi.setName(rs.getString("name"));
				mi.setLanguage_id(rs.getInt("language_id"));
				mi.setParent_level_id(rs.getInt("parent_level_id"));
				mi.setMenu_id(rs.getInt("menu_id"));
				mi.setService_id(rs.getInt("serviceid"));
				mi.setVisible(rs.getBoolean("visible"));
				
				topMenus.put(x, mi);
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
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
		
		return mi;
	}
	/**
	 * 
	 * @param id
	 * @param conn
	 * @return
	 */
	public MenuItem getMenuById(int id, Connection conn){
		
		MenuItem mi = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement(GET_MENU_BY_ID,Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, id);
			
			rs = pstmt.executeQuery();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(id, conn);
			
			//int x = 0;
			
			while(rs.next()){
				//x++;
				
				mi = new MenuItem();
				mi.setId(rs.getInt("id"));
				mi.setName(rs.getString("name"));
				mi.setLanguage_id(rs.getInt("language_id"));
				mi.setParent_level_id(rs.getInt("parent_level_id"));
				mi.setMenu_id(rs.getInt("menu_id"));
				mi.setService_id(rs.getInt("serviceid"));
				mi.setVisible(rs.getBoolean("visible"));
				
				
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
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
		
		return mi;
	}
	
	
	
	
	/**
	 * Gets sub menu items
	 * @param parent_level_id_fk
	 * @param conn
	 * @return
	 */
	public LinkedHashMap<Integer,MenuItem> getSubMenus(int parent_level_id_fk, Connection conn){
		LinkedHashMap<Integer,MenuItem> items = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement(GET_MENU_ITEMS,Statement.RETURN_GENERATED_KEYS);
			
			pstmt.setInt(1, parent_level_id_fk);
			
			rs = pstmt.executeQuery();
			
			int post = 0;
			
			MenuItem mi = null;
			
			while(rs.next()){
				post++;
				
				if(rs.isFirst())
					items = new LinkedHashMap<Integer,MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId(rs.getInt("id"));
				mi.setName(rs.getString("name"));
				mi.setLanguage_id(rs.getInt("language_id"));
				mi.setParent_level_id(rs.getInt("parent_level_id"));
				mi.setMenu_id(rs.getInt("menu_id"));
				mi.setService_id(rs.getInt("serviceid"));
				mi.setVisible(rs.getBoolean("visible"));
				items.put(post,mi);
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		}
		
		return items;
	}
	
	
	
	/**
	 * Gets a session using msisdn.
	 * @param msisdn  - the msisdn
	 * @param conn - java.sql.Connection
	 * @return sess - com.inmobia.celcom.smsmenu.Session
	 */
	public Session getSession(String msisdn, Connection conn){
		
		Session sess = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement(GET_SESSION,Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, msisdn);
			
			rs = pstmt.executeQuery();
			
			MenuItem mi = null;
			
			int menu_level_id = -1;
			
			while(rs.next()){
				sess = new Session();
				mi = new MenuItem();
				sess.setId(rs.getInt("id"));
				sess.setMsisdn(rs.getString("msisdn"));
				sess.setSmsmenu_level_id_fk(rs.getInt("smsmenu_levels_id_fk"));
				sess.setTimeStamp(rs.getString("timeStamp"));
				sess.setLanguage_id(rs.getInt("language_id"));
				
				if((rs.getInt("menu_level_id")>0)){
					menu_level_id = rs.getInt("menu_level_id");
					mi.setId(menu_level_id);
					mi.setName(rs.getString("name"));
					mi.setLanguage_id(rs.getInt("language_id"));
					mi.setParent_level_id(rs.getInt("parent_level_id"));
					mi.setMenu_id(rs.getInt("menu_id"));
					mi.setService_id(rs.getInt("serviceid"));
					mi.setVisible(rs.getBoolean("visible"));
					
					mi.setSub_menus(getSubMenus(menu_level_id,conn));
					
					sess.setMenu_item(mi);
				}
				
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		}
		
		return sess;
	}
	
	
	
	/**
	 * Updates a session
	 * @param msisdn
	 * @param smsmenu_levels_id_fk
	 * @param conn
	 * @return
	 */
	public boolean updateSession(int language_id, String msisdn, int smsmenu_levels_id_fk, Connection conn){
		
		
		logger.debug("\n\n***************************************\nUPDATING THE SESSION language_id="+language_id+", msisdn="+msisdn+", smsmenu_levels_id_fk="+smsmenu_levels_id_fk+"\n====================================================\n\n\n");
		
		boolean success = false;
		
		PreparedStatement pstmt = null;
		
		try{
		
			pstmt = conn.prepareStatement(UPDATE_SESSION,Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, msisdn);
			pstmt.setInt(2, smsmenu_levels_id_fk);
			pstmt.setInt(3, language_id);
			pstmt.setInt(4, smsmenu_levels_id_fk);
			pstmt.setInt(5, language_id);
			
			success = (pstmt.executeUpdate()>0);
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				pstmt.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		}
		
		return success;
	}
	
	public void printMenu(MenuItem menuItem,int level, int position, Connection conn){
		String tab = "";
		for(int i=0;i<level;i++){
			tab +="\t"; //+ ( ( (i+1)==level   ) ? "+" : "") ;
		}
		
		String op = tab + position+ ". " + menuItem.getName();
		
		System.out.println(op);
		LinkedHashMap<Integer,MenuItem> mis = getSubMenus(menuItem.getId(), conn);
		if(mis!=null){
			level++;
			position = 1;
			for (Entry<Integer, MenuItem> entry : mis.entrySet()) {
				printMenu(entry.getValue(),level, position, conn);
			 	position++;
			}
			
		}
		
		
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		
		try{
			
			//System.out.println(URLEncoder.encode("#","UTF8"));
			
			conn = DBConnection.createFromConnString(DBConnection.CONSTR);
			MenuController mc = new MenuController();
			//MenuItem mi = mc.getMenuByParentLevelId(-1,conn);
			//MenuItem chosenSub = mi.getMenuByPosition(1);
			//String msg = chosenSub.getName();//.enumerate();
			//System.out.println(msg);
			//Session sess = mc.getSession("254734821158", conn);
			//int level_id = sess.getSmsmenu_level_id_fk();
			//System.out.println(level_id);
			//int start = 2;
			//args[0] = "1";
			MenuItem mi = mc.getTopMenu(1, 2, conn);
			
			//mc.printMenu(mi, 0, 1, conn);
			
			
			LinkedHashMap<Integer,MenuItem> st = mc.getSubMenus(1,conn);
			for (Entry<Integer, MenuItem> entry : st.entrySet()) {
			 	//System.out.println(entry.getValue().getName());
				mc.printMenu(entry.getValue(), 0, 1, conn);
			}
			//System.out.println(sess.toString());
			
		}catch(Exception e){
			e.printStackTrace();
			//logger.error(e.getMessage(),e);
		
		}finally{
			
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				//logger.error(e.getMessage(),e);
			}
			
		}
		
		
	}



	

}
