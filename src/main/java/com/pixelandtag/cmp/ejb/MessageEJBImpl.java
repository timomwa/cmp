package com.pixelandtag.cmp.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.MessageDAOI;
import com.pixelandtag.cmp.entities.Message;

@Stateful
@Remote
public class MessageEJBImpl implements MessageEJBI {
	
	private static final String LANG_ID = "language_id";

	private static final String PARAM_KEY_NAME = "key";

	private static final String PARAM_OPCO_ID = "opcoid";
	
	private int hitcount = 0;
	private static final int resetcacheon = 1000;//re-set cache on the Nth hit

	private static final String REGEX_K = "%s%s%s";

	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	
	@Inject
	private MessageDAOI messageDAO;
	private static Map<String,Message> message_cache = new HashMap<String,Message>();


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Message getMessage(String key, Long language_id, Long opcoid){
		
		hitcount++;
		if(hitcount>=resetcacheon)
			message_cache.clear();
		
		
		if(language_id<=0)
			language_id = 1L;
		
		String msg_cache_key = String.format(REGEX_K, key,language_id,opcoid);
		Message message = message_cache.get(msg_cache_key);
		if(message!=null)
			return message;
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(LANG_ID, language_id);
		params.put(PARAM_KEY_NAME, key);
		params.put(PARAM_OPCO_ID, opcoid);
		
		List<Message>  messages = messageDAO.findByNamedQuery(Message.NQ_FIND_BY_LANG_AND_KEY_AND_OPCOID, params);
	
		if(messages!=null && messages.size()>0){
			message = messages.get(0);
		}
	
		return message;
		
	}
	
	
	

}
