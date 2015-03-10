package com.pixelandtag.smsmenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.dynamic.dbutils.DBConnection;

public class MenuController {
	
	private Logger logger = Logger.getLogger(MenuController.class);
	
	
	private final String DB_NAME = "pixeland_content360";
	public static final String NUM_SPACER = ". ";
	private CMPResourceBeanRemote cmpbean;
	
	public MenuController(CMPResourceBeanRemote cmpbean) {
		this.cmpbean = cmpbean;
	}



	/**
	 * Gets top Menu.
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id,int menuid) throws Exception{
		return cmpbean.getMenuByParentLevelId(language_id, parent_level_id,menuid);
	}
	
	
	

	public MenuItem getTopMenu(int menu_id, int language_id) throws Exception {
		return cmpbean.getTopMenu(menu_id, language_id);
	}
	/**
	 * 
	 * @param id
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public MenuItem getMenuById(int menu_id) throws Exception{
		return cmpbean.getMenuById(menu_id);
		
	}
	
	
	
	
	/**
	 * Gets sub menu items
	 * @param parent_level_id_fk
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public LinkedHashMap<Integer,MenuItem> getSubMenus(int parent_level_id_fk) throws Exception{
		
		return cmpbean.getSubMenus(parent_level_id_fk);
		
	}
	
	
	
	/**
	 * Gets a session using msisdn.
	 * @param msisdn  - the msisdn
	 * @param conn - java.sql.Connection
	 * @return sess - com.inmobia.celcom.smsmenu.Session
	 * @throws Exception 
	 */
	public Session getSession(String msisdn) throws Exception{
		return cmpbean.getSession(msisdn);
		
	}
	
	
	
	/**
	 * Updates a session
	 * @param msisdn
	 * @param smsmenu_levels_id_fk
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public boolean updateSession(int language_id, String msisdn, int smsmenu_levels_id_fk) throws Exception{
		
		logger.debug("\n\n***************************************\nUPDATING THE SESSION language_id="+language_id+", msisdn="+msisdn+", smsmenu_levels_id_fk="+smsmenu_levels_id_fk+"\n====================================================\n\n\n");
		
		return cmpbean.updateSession(language_id, msisdn, smsmenu_levels_id_fk);
		
		
		
	}
	
	public void printMenu(MenuItem menuItem,int level, int position) throws Exception{
		
		cmpbean.printMenu(menuItem,level, position);
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		
		try{
			
			//System.out.println(URLEncoder.encode("#","UTF8"));
			
///			conn = DBConnection.createFromConnString(DBConnection.CONSTR);
//			MenuController mc = new MenuController();
			//MenuItem mi = mc.getMenuByParentLevelId(-1,conn);
			//MenuItem chosenSub = mi.getMenuByPosition(1);
			//String msg = chosenSub.getName();//.enumerate();
			//System.out.println(msg);
			//Session sess = mc.getSession("254734821158", conn);
			//int level_id = sess.getSmsmenu_level_id_fk();
			//System.out.println(level_id);
			//int start = 2;
			//args[0] = "1";
//			MenuItem mi = mc.getTopMenu(1, 2, conn);
			
			//mc.printMenu(mi, 0, 1, conn);
			
			
//			LinkedHashMap<Integer,MenuItem> st = mc.getSubMenus(1,conn);
//			for (Entry<Integer, MenuItem> entry : st.entrySet()) {
			 	//System.out.println(entry.getValue().getName());
//				mc.printMenu(entry.getValue(), 0, 1, conn);
//			}
			//System.out.println(sess.toString());
			
		}catch(Exception e){
			e.printStackTrace();
			//logger.error(e.getMessage(),e);
		
		}finally{
			
			try {
				conn.close();
			} catch (Exception e) {
				//logger.error(e.getMessage(),e);
			}
			
		}
		
		
	}



	

}
