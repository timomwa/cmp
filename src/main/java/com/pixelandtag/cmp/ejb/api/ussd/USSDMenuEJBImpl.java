package com.pixelandtag.cmp.ejb.api.ussd;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.ejb.EJB;
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
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileAttribute;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.smsmenu.MenuItem;

@Stateless
@Remote
public class USSDMenuEJBImpl implements USSDMenuEJBI {
	
	private static final Object BR_NEW_LINE = "<br/>";

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private OpcoSMSServiceEJBI opcosmserviceEJB;
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	@EJB
	private DatingServiceI datingBean;
	
	private String SPACE = " ";
	
	private Logger logger = Logger.getLogger(getClass());
	
	private XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1")) {
        @Override
        public String escapeElementEntities(String str) {
        	return str;
        }
    };
    
    
    public String getNextQuestionOrange(String baseurl, IncomingSMS incomingsms){
    	
    	Element rootelement = new Element("pages");
		//rootelement.setAttribute("descr", "dating");
		
		Document doc = new Document(rootelement); 
		DocType doctype = new DocType("pages");
		doctype.setSystemID("cellflash-1.3.dtd");
		doc.setDocType(doctype);
		
		String xml = "";
		
		
		Element page = new Element("page");
		StringBuffer sb = new StringBuffer();
		
		
		ProfileQuestion profileQuestion = getNextQuestion(incomingsms);
		
		
		if(profileQuestion!=null){
			
			String question = profileQuestion.getQuestion();
			ProfileAttribute attrib = profileQuestion.getAttrib();
			Long questionid = profileQuestion.getId();
			Long languageid = profileQuestion.getLanguage_id();
			
			baseurl = baseurl+"?attrib="+attrib+"&questionid="+questionid+"&languageid="+languageid;
			
			sb.append(question);
			sb.append(BR_NEW_LINE);
			
			if(attrib==ProfileAttribute.DISCLAIMER){
				sb.append("<a href=\""+baseurl+"?questionid=4\">1. Yes</a>");
				sb.append(BR_NEW_LINE);
				sb.append("<a href=\"test.php?item=4\">2. No</a>");
			}
			
			if(attrib==ProfileAttribute.CHAT_USERNAME){//Form
				sb.setLength(0);
				sb.append("<form action=\"test.php\">");
				sb.append("<entry kind=\"digits\" var=\"answers\">");
				sb.append("<prompt>"+question+"</prompt>");
				sb.append("</entry></form>");
			}
			
			if(attrib==ProfileAttribute.GENDER){
				sb.append("<a href=\""+baseurl+"&answer=female\">1. Female</a>");
				sb.append(BR_NEW_LINE);
				sb.append("<a href=\""+baseurl+"&answer=male\">2. Male</a>");
			}
			if(attrib==ProfileAttribute.AGE){
				sb.setLength(0);
				sb.append("<form action=\""+baseurl+"\">");
				sb.append("<entry kind=\"digits\" var=\"answer\">");
				sb.append("<prompt>"+question+"</prompt>");
				sb.append("</entry></form>");
			}
			if(attrib==ProfileAttribute.LOCATION){
				sb.setLength(0);
				sb.append("<form action=\""+baseurl+"\">");
				sb.append("<entry kind=\"alpha\" var=\"answer\">");
				sb.append("<prompt>"+question+"</prompt>");
				sb.append("</entry></form>");
			}
			if(attrib==ProfileAttribute.PREFERRED_AGE){
				sb.setLength(0);
				sb.append("<form action=\""+baseurl+"\">");
				sb.append("<entry kind=\"digits\" var=\"answer\">");
				sb.append("<prompt>"+question+"</prompt>");
				sb.append("</entry></form>");
			}
			if(attrib==ProfileAttribute.PREFERRED_GENDER){
				sb.append("<a href=\""+baseurl+"&answer=female\">1. Female</a>");
				sb.append(BR_NEW_LINE);
				sb.append("<a href=\""+baseurl+"&answer=male\">2. Male</a>");
			}
			
		}else{
			
		}
		
		/*sb.setLength(0);
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"); 
		sb.append("<!DOCTYPE pages SYSTEM \"cellflash-1.3.dtd\">");
		sb.append("<pages>");
		
		sb.append("<page>");
		sb.append("<form action=\"test.php\">");
		sb.append("<entry kind=\"digits\" var=\"answers\">");
		sb.append("<prompt>There there?</prompt>");
		sb.append("</entry>");
		sb.append("</form>");
		sb.append("</page>");
		
		sb.append("</pages>");
		xml = sb.toString();*/
		page.setText(sb.toString());
		rootelement.addContent(page);
		sb.setLength(0);
		xml = xmlOutput.outputString(doc);
		
		return xml;
	}
    
    
    public String startDatingQuestions(IncomingSMS incomingsms){
    	
    	ProfileQuestion profileQuestion = getNextQuestion(incomingsms);
    	
    	String question = "";
    	if(profileQuestion!=null){
    		logger.debug("QUESTION::: "+profileQuestion.getQuestion());
    		question =  SPACE +profileQuestion.getQuestion();
    	}
    	
    	return question;
    }
    
    
    public ProfileQuestion getNextQuestion(IncomingSMS incomingsms){
    	
		try {
			Person person =  datingBean.getPerson(incomingsms.getMsisdn(), incomingsms.getOpco());
			
			if(person==null)
				person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			
			if(profile==null){
				profile = new PersonDatingProfile();
				profile.setPerson(person);
				profile.setUsername(incomingsms.getMsisdn());
				profile = datingBean.saveOrUpdate(profile);
			}
			
			return datingBean.getNextProfileQuestion(profile.getId());
			
		}catch (DatingServiceException e) {
			logger.error(e.getMessage(), e);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
		return null;
		
    }
    
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI#getMenu(int, int, int)
	 */
	public String getMenu(String contextpath, String msisdn, int language_id, int parent_level_id, int menuid, int menuitemid, OperatorCountry opco){
		
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
			MenuItem menuitem =   menuitemid>-1 ? getMenuById(menuitemid) : getMenuByParentLevelId(language_id, parent_level_id, menuid);
			LinkedHashMap<Integer, MenuItem> topMenus = menuitem.getSub_menus();
			
			int serviceid = menuitem.getService_id();
			
			Element page = new Element("page");
			StringBuffer sb = new StringBuffer();
			sb.append(menuitem.getName()+"<br/>");
			
			
			if(topMenus!=null && topMenus.entrySet()!=null){
				
				
				for (Entry<Integer, MenuItem> entry : topMenus.entrySet()){
					String menuname = entry.getValue().getName();
					int menuitemid_ = entry.getValue().getId();
					int languageid = entry.getValue().getLanguage_id();
					int serviceid_ = entry.getValue().getService_id();
					int menuid_ = entry.getValue().getMenu_id();
					int parent_level_id_ = entry.getValue().getParent_level_id();
					sb.append("<a href=\""+contextpath+"?menuitemid="
					+menuitemid_+"&languageid="
					+languageid+"&serviceid="
					+serviceid_+"&menuid="
					+menuid_+"&parent_level_id="
					+parent_level_id_+"\">"+menuname+"</a><br/>");
				}
				
			
			}else if(serviceid>-1){
				
				OpcoSMSService smsservice  = opcosmserviceEJB.getOpcoSMSService(Long.valueOf(serviceid), opco);
				SMSService smsserv = smsservice.getSmsservice();//em.find(SMSService.class, Long.valueOf(serviceid+""));
				logger.info("\t\t\t:::::::::::::::::::::::::::::: serviceid:: "+serviceid+ " CMD :"+(smsserv!=null ? smsserv.getCmd() : null));
				
				
				MOProcessor proc = smsserv.getMoprocessor();
				
				IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
				incomingsms.setMsisdn(msisdn);
				incomingsms.setServiceid(Long.valueOf( serviceid ));
				incomingsms.setSms(smsserv.getCmd());
				incomingsms.setShortcode(proc.getShortcode());
				incomingsms.setPrice(smsserv.getPrice());
				incomingsms.setCmp_tx_id(generateNextTxId());
				incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
				incomingsms.setServiceid(smsserv.getId());
				incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
				//incomingsms.setId(req.getMessageId());
				incomingsms.setMoprocessor(proc);
				incomingsms.setOpco(opco);
				logger.info("\n\n\n\n\n::::::::::::::::proc.getId().intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");
				
				processorEJB.processMo(incomingsms);
				
				String resp = "";
				if(smsserv.getCmd().equals("FIND")){
					resp = "Request to find friend near your area received. You shall receive an sms shortly.";
				}else{
					resp = "Request received and is being processed.";
				}
				
				sb.append(resp+"<br/>");
				
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
	
	
	
	public String generateNextTxId(){
		try {
			Thread.sleep(3);
		} catch (Exception e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return String.valueOf(System.nanoTime());
	}

}
