package com.pixelandtag.cmp.ejb.api.ussd;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.smsmenu.MenuItem;

@Stateless
@Remote
public class USSDMenuEJBImpl implements USSDMenuEJBI {
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	private Logger logger = Logger.getLogger(getClass());
	
	private XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1")) {
        @Override
        public String escapeElementEntities(String str) {
        	return str;
        }
    };
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI#getMenu(int, int, int)
	 */
	public String getMenu(String contextpath,int language_id, int parent_level_id, int menuid){
		
		language_id = language_id==-1 ? 1 : language_id;
		menuid = menuid==-1 ? 1 : menuid;
		
		Element rootelement = new Element("pages");
		rootelement.setAttribute("descr", "dating");
		
		Document doc = new Document(rootelement); 
		DocType doctype = new DocType("pages");
		doctype.setSystemID("cellflash-1.3.dtd");
		doc.setDocType(doctype);
		
		String xml = "";
		
		try {
			MenuItem menuitem = getMenuByParentLevelId(language_id, parent_level_id, menuid);
			LinkedHashMap<Integer, MenuItem> topMenus = menuitem.getSub_menus();
			
			Element page = new Element("page");
			StringBuffer sb = new StringBuffer();
			for (Entry<Integer, MenuItem> entry : topMenus.entrySet()){
				String menuname = entry.getValue().getName();
				int menuitemid = entry.getValue().getId();
				int languageid = entry.getValue().getLanguage_id();
				int serviceid = entry.getValue().getService_id();
				int menuid_ = entry.getValue().getMenu_id();
				int parent_level_id_ = entry.getValue().getParent_level_id();
				sb.append("<a href=\""+contextpath+"?menuitemid="
				+menuitemid+"&languageid="
				+languageid+"&serviceid="
				+serviceid+"&menuid="
				+menuid_+"&parent_level_id="
				+parent_level_id_+"\">"+menuname+"</a><br/>");
			}
			page.setText(sb.toString());
			sb.setLength(0);
			rootelement.addContent(page);
			
			xml = xmlOutput.outputString(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return xml;
	
	}
	
	
	

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
	
	

	public MenuItem getMenuById(int menu_id) throws Exception{
		
		MenuItem mi = null;
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, menu_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(menu_id);
			
			for(Object[] o : obj){
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

}
