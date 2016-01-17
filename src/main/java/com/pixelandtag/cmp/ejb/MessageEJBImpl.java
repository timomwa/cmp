package com.pixelandtag.cmp.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.dao.core.MessageDAOI;
import com.pixelandtag.cmp.entities.Message;

@Stateless
@Remote
public class MessageEJBImpl implements MessageEJBI {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	
	@Inject
	private MessageDAOI messageDAO;


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Message getMessage(String key, Long language_id, Long opcoid){
		
		Message message = null;
		
		if(language_id<=0)
			language_id = 1L;
	
		String msg = "State 130 :  language_id = "+language_id+" key = "+key+" opcoid = "+opcoid;
		logger.debug(msg);
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("language_id", language_id);
		params.put("key", key);
		params.put("opcoid", opcoid);
		
		List<Message>  messages = messageDAO.findByNamedQuery(Message.NQ_FIND_BY_LANG_AND_KEY_AND_OPCOID, params);
	
		if(messages!=null && messages.size()>0){
			message = messages.get(0);
			msg = message.getMessage();
		}
	
		logger.debug("looking for :[" + key + "], found [" + msg + "]");
	
		return message;
		
	}
	
	
	

}
