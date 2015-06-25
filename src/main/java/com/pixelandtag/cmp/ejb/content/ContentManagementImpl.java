package com.pixelandtag.cmp.ejb.content;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.pixelandtag.cmp.entities.SMSServiceMetaData;


@Stateless
@Remote
public class ContentManagementImpl implements ContentManagementI {
	

	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	protected EntityManager em;
	

	@SuppressWarnings("unchecked")
	@Override
	public boolean deleteContent(Long serviceid, Long contentId) throws Exception{
		
		try{
			
			Query qry = em.createQuery("from SMSServiceMetaData smd WHERE sms_service_id_fk=:sms_service_id_fk");
			qry.setParameter("sms_service_id_fk", serviceid);
			List<SMSServiceMetaData> metaData = qry.getResultList();
			
			String db_name = null;
			String table = null;
			String static_category_value = null;
			for(SMSServiceMetaData mtda : metaData){
				if(mtda.getMeta_field().equals("static_categoryvalue"))
					static_category_value = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("table"))
					table = mtda.getMeta_value();
				if(mtda.getMeta_field().equals("db_name")){
					db_name = mtda.getMeta_value();
				}
			}
			logger.info("db_name : "+db_name);
			logger.info("table : "+table);
			logger.info("static_category_value : "+static_category_value);
			logger.info("serviceid : "+serviceid);
			
			
			Query qry2 = em.createNativeQuery("DELETE FROM `"+db_name+"`.`"+table+"` WHERE id=:id");
			qry2.setParameter("id", contentId);
			qry2.executeUpdate();
			
		}catch(Exception exp){
			
			logger.error(exp.getMessage(),exp);
			
			throw exp;
			
		}
		
		return true;
	}

}
