package com.pixelandtag.cmp.ejb.stats;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Stateless
@Remote
public class LinkLatencyStatEJBImpl implements LinkLatencyStatEJBI {

public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	protected EntityManager em;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public String getLinksLatencyStats() throws Exception{
		try{
			Query query  = em.createNativeQuery("select link, count(*) count, avg(latency) avgLat, second(timeStamp) sec, minute(timeStamp) min from latency_log where date(timestamp)=curdate() group by link, sec , min");
		
			List<Object[]> recs = query.getResultList();
			
			JSONArray labels = new JSONArray();
			JSONArray data = new JSONArray();
			JSONArray datasets = new JSONArray();
			for(Object[] o : recs){
				String link = (String) o[0];
				BigDecimal averageLatencyPerSecond = (BigDecimal)  o[2];
				BigInteger second = (BigInteger) o[3];
				BigInteger minute = (BigInteger) o[4];
				String label = minute+":"+second;
				labels.add(label);
				data.add(averageLatencyPerSecond.doubleValue());
			}
			
			JSONObject mainObject = new JSONObject();
			mainObject.put("labels",labels);
			
			JSONObject dataset = new JSONObject();
			dataset.put("label", "Link Latency");
			dataset.put("fillColor", "rgba(220,220,220,0.2)");
			dataset.put("strokeColor", "rgba(220,220,220,1)");
			dataset.put("pointColor", "rgba(220,220,220,1)");
			dataset.put("pointStrokeColor", "#fff");
			dataset.put("pointHighlightFill", "#fff");
			dataset.put("pointHighlightStroke", "rgba(220,220,220,1)");
			dataset.put("data",data);
			mainObject.put("datasets",dataset);
			
			return mainObject.toString();
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
		
	}

}
