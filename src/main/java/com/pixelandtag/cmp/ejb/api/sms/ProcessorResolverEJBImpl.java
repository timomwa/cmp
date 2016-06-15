package com.pixelandtag.cmp.ejb.api.sms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.dao.core.IncomingSMSDAOI;
import com.pixelandtag.cmp.dao.core.MessageExtraParamsDAOI;
import com.pixelandtag.cmp.dao.core.MessageLogDAOI;
import com.pixelandtag.cmp.dao.opco.MOProcessorDAOI;
import com.pixelandtag.cmp.ejb.subscription.DNDListEJBI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.MessageExtraParams;
import com.pixelandtag.cmp.entities.MessageLog;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.cmp.entities.customer.configs.ConfigurationException;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.ProfileConfigs;
import com.pixelandtag.cmp.entities.customer.configs.ProfileType;
import com.pixelandtag.smssenders.JsonUtilI;
import com.pixelandtag.smssenders.Receiver;
import com.pixelandtag.subscription.dto.MediumType;

@Stateless
@Remote
public class ProcessorResolverEJBImpl implements ProcessorResolverEJBI {
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	private Logger logger = Logger.getLogger(getClass());
	
	
	@EJB
	private ConfigsEJBI configsEJB;
	
	@Inject
	private MOProcessorDAOI moprocDAO;
	
	@Inject
	private IncomingSMSDAOI incomingSMSDAO;
	
	@Inject
	private MessageLogDAOI messagelogDAO;
	
	@Inject
	private MessageExtraParamsDAOI extraparamsDAO;
	
	private List<String> mandatory = new ArrayList<String>();
	
	@PostConstruct
	public void init(){
		mandatory.add(Receiver.HTTP_RECEIVER_MSISDN_PARAM_NAME);
		mandatory.add(Receiver.HTTP_RECEIVER_SHORTCODE_PARAM_NAME);
		mandatory.add(Receiver.HTTP_RECEIVER_SMS_PARAM_NAME);
		mandatory.add(Receiver.HTTP_RECEIVER_HAS_PAYLOAD);
		mandatory.add(Receiver.HTTP_RECEIVER_EXPECTED_CONTENTTYPE);
		mandatory.add(Receiver.MO_MEDIUM_SOURCE);
	}
	
	@Inject
	private static JsonUtilI jsonutil;
	
	@EJB
	private OpcoEJBI opcoEJB;
	
	@EJB
	private DNDListEJBI dndEJB;
	
	
	@Override
	public IncomingSMS processMo(Map<String, String> incomingparams) throws ConfigurationException{
		
		String ip_address = incomingparams.get(Receiver.IP_ADDRESS);
		
		String opcocode = incomingparams.get(Receiver.HTTP_RECEIVER_OPCO_CODE);
		
		OperatorCountry opco = ( opcocode!=null && !opcocode.isEmpty() ) ? opcoEJB.findOpcoByCode(opcocode) : null;
		
		if(opco==null)
			opco = configsEJB.getOperatorByIpAddress(ip_address);
		
		if(opco==null)
			throw new ConfigurationException("No operator identified by the IP address => "+ip_address);
		
	
		OpcoSenderReceiverProfile profile = configsEJB.findActiveProfileByTypeOrTranceiver(opco, ProfileType.RECEIVER);
		
		if(profile==null)
			throw new ConfigurationException(" The operator \""+opco.toString()
					+"\" doesn't seem to have any sender and receiver profiles. Please create those then try again");
		
		Map<String, ProfileConfigs> profileconfigs = configsEJB.getAllConfigs(profile.getProfile());
		
		if(profileconfigs==null || profileconfigs.size()==0)
			throw new ConfigurationException(" The operator "+opco.toString()
					+" doesn't have any configurations. Please set these!");
		
		ProfileConfigs msisdn_param_name_cfg = profileconfigs.get(Receiver.HTTP_RECEIVER_MSISDN_PARAM_NAME);
		ProfileConfigs shortcode_param_name_cfg = profileconfigs.get(Receiver.HTTP_RECEIVER_SHORTCODE_PARAM_NAME);
		ProfileConfigs sms_param_name_cfg = profileconfigs.get(Receiver.HTTP_RECEIVER_SMS_PARAM_NAME);
		ProfileConfigs txid_param_name_cfg = profileconfigs.get(Receiver.HTTP_RECEIVER_OPCO_TX_ID_PARAM_NAME);
		ProfileConfigs receiver_has_payload = profileconfigs.get(Receiver.HTTP_RECEIVER_HAS_PAYLOAD);
		ProfileConfigs expectedcontenttype = profileconfigs.get(Receiver.HTTP_RECEIVER_EXPECTED_CONTENTTYPE);
	    ProfileConfigs mo_medium_source = profileconfigs.get(Receiver.MO_MEDIUM_SOURCE);
	    ProfileConfigs strippable_string = profileconfigs.get(Receiver.STRIPPABLE_STRING);
		
		
		for(String param : mandatory)
			if(profileconfigs.get(param)==null)
				throw new ConfigurationException("The operator "+opco +"doesn't have the \""+param+"\" param set");
	
		
		
		String msisdn = null;
		String shortcode = null;
		String sms = "";
		String opcotxid = null;
		String type_ = incomingparams.get(Receiver.HTTP_RECEIVER_TYPE);
		MediumType type = ( type_!=null && !type_.isEmpty() ) ? MediumType.get(type_) : MediumType.sms;
		
		if(receiver_has_payload.getValue().equalsIgnoreCase("yes")){
		
			if(expectedcontenttype==null || 
					(expectedcontenttype.getValue()==null ||  
							expectedcontenttype.getValue().isEmpty())) 
				throw new ConfigurationException(" The operator "+opco
						+" doesn't have the \""+Receiver.HTTP_RECEIVER_EXPECTED_CONTENTTYPE+"\" config set");
			
			String payload = incomingparams.get(Receiver.HTTP_RECEIVER_PAYLOAD);
			
			String contenttype = expectedcontenttype.getValue();
			if(contenttype.equalsIgnoreCase("xml")){
				msisdn = getValue(payload, msisdn_param_name_cfg.getValue());
				shortcode = getValue(payload, shortcode_param_name_cfg.getValue());
				sms = getValue(payload, sms_param_name_cfg.getValue());
				if(sms!=null)
					sms = sms.trim();
				if(txid_param_name_cfg!=null && (txid_param_name_cfg.getValue()!=null || 
						!txid_param_name_cfg.getValue().isEmpty())){
					opcotxid = getValue(payload, txid_param_name_cfg.getValue() );
				}
			}
			
			if(contenttype.equalsIgnoreCase("json")){
				try{
					JSONObject jsob = new JSONObject(payload);
					jsonutil.loadJson(jsob);
					msisdn = (String)jsonutil.getValue(msisdn_param_name_cfg.getValue());
					shortcode = (String)jsonutil.getValue(shortcode_param_name_cfg.getValue());
					sms = (String)jsonutil.getValue(sms_param_name_cfg.getValue());
					
					if(txid_param_name_cfg!=null && (txid_param_name_cfg.getValue()!=null || 
							!txid_param_name_cfg.getValue().isEmpty())){
						opcotxid = (String)jsonutil.getValue(txid_param_name_cfg.getValue());
					}
					
				}catch(JSONException jse){
					logger.error(jse.getMessage(), jse);
					throw new ConfigurationException(" incorrect json payload => "+payload);
				}
				
			}
			
		}else{
		
			msisdn =  incomingparams.get(msisdn_param_name_cfg.getValue());
			shortcode =  incomingparams.get(shortcode_param_name_cfg.getValue());
			sms  =  incomingparams.get(sms_param_name_cfg.getValue());
			if(txid_param_name_cfg!=null && txid_param_name_cfg.getValue()!=null || 
					!txid_param_name_cfg.getValue().isEmpty()){
				opcotxid = incomingparams.get(txid_param_name_cfg.getValue());
			}
		}
		
		if(msisdn==null || msisdn.isEmpty())
			throw new  ConfigurationException("msisdn not provided in request. Please provide this");
		if(shortcode==null || shortcode.isEmpty())
			throw new  ConfigurationException("shortcode not provided in request. Please provide this");
		
		opcotxid = stripStrippables(opcotxid,strippable_string);
		msisdn = stripStrippables(msisdn,strippable_string);
		shortcode = stripStrippables(shortcode,strippable_string);
		
		dndEJB.removeFromDNDList(msisdn);
		
		IncomingSMS incomingsms = new IncomingSMS();
		incomingsms.setMediumType(type);
		incomingsms.setMsisdn(msisdn);
		incomingsms.setShortcode(shortcode);
		incomingsms.setSms(sms);
		incomingsms.setOpco_tx_id(opcotxid);
		incomingsms.setOpco(opco);
		incomingsms = populateProcessorDetails(incomingsms);
		
		if(incomingsms.getMoprocessor()==null)
			throw new ConfigurationException("Couldn't find processor for shortcode =\""+incomingsms.getShortcode()+"\", opcoid=\""+incomingsms.getOpco().getId()+"\"");
		
		incomingsms.setCmp_tx_id( UUID.randomUUID().toString()  );
		
		incomingsms.setIsSubscription(Boolean.FALSE);
		
		
		try {
			
			incomingsms = incomingSMSDAO.save(incomingsms);
			
			MessageLog messagelog = new MessageLog();
			messagelog.setCmp_tx_id(incomingsms.getCmp_tx_id());
			messagelog.setOpco_tx_id(incomingsms.getOpco_tx_id());
			messagelog.setMo_processor_id_fk(incomingsms.getMoprocessor().getId());
			messagelog.setMo_sms(incomingsms.getSms());
			messagelog.setMo_timestamp(incomingsms.getTimestamp());
			messagelog.setShortcode(incomingsms.getShortcode());
			messagelog.setSource(mo_medium_source.getValue());
			messagelog.setStatus(MessageStatus.RECEIVED.name());
			messagelog.setMsisdn(msisdn);
			messagelog = messagelogDAO.save(messagelog);
			
			
			for(Map.Entry<String,String> incoming : incomingparams.entrySet()){
				
				boolean isinMandatory = false;
				for(String param : mandatory){
					if( profileconfigs.get(param).getValue().equalsIgnoreCase(  incoming.getKey() ) )
						isinMandatory = true;
				}
				
				if(!isinMandatory){
					MessageExtraParams msgparams  = new MessageExtraParams();
					msgparams.setTransactionid(incomingsms.getCmp_tx_id());
					msgparams.setParamKey(incoming.getKey());
					msgparams.setParamValue(incoming.getValue());
					msgparams = extraparamsDAO.save(msgparams);
				}
				
				
			}
			
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
		return incomingsms;
	
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public IncomingSMS populateProcessorDetails(IncomingSMS incomingsms) {
		String keyword = incomingsms.getSms();
		
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
		
		qry.setParameter("shortcode", incomingsms.getShortcode());
		qry.setParameter("keyword", keyword);
		qry.setParameter("opco", incomingsms.getOpco());
		
		List<Object[]> rows = qry.getResultList();
		
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
		
			qry.setParameter("shortcode", incomingsms.getShortcode());
			qry.setParameter("keyword", "DEFAULT");
			qry.setParameter("opco", incomingsms.getOpco());
			
			keyword = "DEFAULT";
			
			rows = qry.getResultList();
			
		}
			
		
		for(Object[] row : rows){
			
			Long mo_processor_id = (Long) row[0];
			BigDecimal price = (BigDecimal) row[1];
			Long sms_ervice_id = (Long) row[2];
			Boolean split_mt = (Boolean) row[3];
			String event_type = (String) row[4];
			String price_point_keyword = (String) row[5];
						
			MOProcessor proc = moprocDAO.findById(mo_processor_id);		
			
			incomingsms.setMoprocessor(proc);
			incomingsms.setPrice(price);
			incomingsms.setServiceid(sms_ervice_id);
			incomingsms.setSplit(split_mt);
			incomingsms.setEvent_type(event_type);
			incomingsms.setPrice_point_keyword(price_point_keyword);
		}
		
		return incomingsms;
	}
		
	
	
	@Override
	public IncomingSMS processMo(IncomingSMS incomingsms) {
		try {
			if(incomingsms.getCmp_tx_id()==null || incomingsms.getCmp_tx_id().isEmpty())
				incomingsms.setCmp_tx_id( UUID.randomUUID().toString() );
			return incomingSMSDAO.save(incomingsms);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	

	@Override
	public MOProcessor getMOProcessor(String shortcode){
		return moprocDAO.findBy("shortcode", shortcode);
	}
	
	
	public MessageLog saveMessageLog(MessageLog messagelog) throws Exception{
		return messagelogDAO.save(messagelog);
	}
	
	/**
	 * 
	 * @param xml
	 * @param tagname
	 * @return
	 */
	private String getValue(String xml,String tagname) {
		if(xml==null || xml.isEmpty() || tagname==null || tagname.isEmpty())
			return "";
		xml = xml.toLowerCase();
		tagname = tagname.toLowerCase();
		String startTag = "<"+tagname+">";
		String endTag = "</"+tagname+">";
		int start = xml.indexOf(startTag)+startTag.length();
		int end  = xml.indexOf(endTag);
		if(start<0 || end<0)
			return "";
		return xml.substring(start, end);
	}
	
	
	private String stripStrippables(String originalStr, ProfileConfigs strippable_string){
		if((originalStr==null || originalStr.isEmpty()))
			return  originalStr;
		if(strippable_string!=null && strippable_string.getValue()!=null && !strippable_string.getValue().isEmpty()){
			String[] strippables = strippable_string.getValue().split(",");
			for(String strippable : strippables){
				originalStr = originalStr.replaceAll(  Matcher.quoteReplacement(strippable), Matcher.quoteReplacement("") )   ;
			}
		}
		return originalStr.trim();
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
