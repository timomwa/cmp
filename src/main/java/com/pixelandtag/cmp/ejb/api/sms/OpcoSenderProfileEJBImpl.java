package com.pixelandtag.cmp.ejb.api.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.opco.MOProcessorDAOI;
import com.pixelandtag.cmp.dao.opco.OpcoSenderProfileDAOI;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;


@Stateless
@Remote
public class OpcoSenderProfileEJBImpl implements OpcoSenderProfileEJBI {

	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private OpcoEJBI opcoEJB;
	
	@Inject
	private MOProcessorDAOI  moprocDAO;
	
	@Inject
	private OpcoSenderProfileDAOI opcosenderprofDAO;
	
	@PostConstruct
	private void init() {
		//opcosenderprofDAO.setEm(em);
	}
	
	
	@Override
	public List<OpcoSenderReceiverProfile> getAllActiveProfiles(){
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("active", Boolean.TRUE);
		return opcosenderprofDAO.findByNamedQuery(OpcoSenderReceiverProfile.NQ_LIST_ACTIVE, params);
	}
	
	@Override
	public List<OpcoSenderReceiverProfile> getAllActiveSenderOrTranceiverProfiles() throws Exception{ 
		return opcosenderprofDAO.getAllActiveSenderOrTranceiverProfiles();
	}
	
	@Override
	public OpcoSenderReceiverProfile getActiveProfileForOpco(String opcocode) throws ConfigurationException{
		OperatorCountry opco = opcoEJB.findOpcoByCode(opcocode);
		return opcosenderprofDAO.findActiveProfile(opco);
	}
	
	@Override
	public OpcoSenderReceiverProfile getActiveProfileForOpco(Long opcoid){
		OperatorCountry opco = opcoEJB.findOpcoById(opcoid);
		return opcosenderprofDAO.findBy("opco", opco);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public MOProcessor getMOProcessorByTelcoShortcodeAndKeyword(String keyword, String shortcode, OperatorCountry opco) {
		
		keyword = (keyword!=null && !keyword.isEmpty()) 
				? replaceAllIllegalCharacters(keyword.split("[\\s]")[0].toUpperCase()) : "DEFAULT";
				
		Query qry = em.createQuery("SELECT "
					+ "osms.moprocessor.id, "//0
					+ "osms.smsservice.price, "//1
					+ "osms.smsservice.id, "//2
					+ "osms.smsservice.split_mt, "//3
					+ "osms.smsservice.event_type, "//4
					+ "osms.smsservice.price_point_keyword  "//5
				+ "FROM OpcoSMSService osms"
				+ " WHERE osms.moprocessor.shortcode=:shortcode "
				+ " AND osms.moprocessor.enable=1 "
				+ " AND osms.smsservice.enabled=1 "
				+ " AND osms.smsservice.cmd=:keyword"
				+ " AND osms.opco=:opco");
		
		qry.setParameter("shortcode", shortcode);
		qry.setParameter("keyword", keyword);
		qry.setParameter("opco", opco);
		
		List<Object[]> rows = qry.getResultList();
		
		System.out.println("\n\n\n 1. rows.size()  :::::   "+rows.size());
		
		if(rows.size()<1){
			
			qry = em.createQuery("SELECT "
					+ "osms.moprocessor.id, "//0
					+ "osms.smsservice.price, "//1
					+ "osms.smsservice.id, "//2
					+ "osms.smsservice.split_mt, "//3
					+ "osms.smsservice.event_type, "//4
					+ "osms.smsservice.price_point_keyword  "//5
				+ "FROM OpcoSMSService osms"
				+ " WHERE osms.moprocessor.shortcode=:shortcode "
				+ " AND osms.moprocessor.enable=1 "
				+ " AND osms.smsservice.enabled=1 "
				+ " AND osms.smsservice.cmd=:keyword"
				+ " AND osms.opco=:opco");
		
			qry.setParameter("shortcode", shortcode);
			qry.setParameter("keyword", "DEFAULT");
			qry.setParameter("opco", opco);
			
			keyword = "DEFAULT";
			
			
			rows = qry.getResultList();
			System.out.println("\n\n\n 2. rows.size()  :::::   "+rows.size());
		}
		
		MOProcessor proc = null;
		
		if(rows!=null && rows.size()>0){
			Object[] row = rows.get(0);
			Long mo_processor_id = (Long) row[0];
			proc = moprocDAO.findById(mo_processor_id);		
		}
			
		return proc;
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

	
	
}
