package com.pixelandtag.cmp.ejb.stats;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.pixelandtag.action.BaseActionBean;

@Stateless
@Remote
public class LinkLatencyStatEJBImpl implements LinkLatencyStatEJBI {


	public Logger logger = Logger.getLogger(getClass());
	
	@PersistenceContext(unitName = "EjbComponentPU4")
	protected EntityManager em;
	
	@Override
	public BigDecimal getAverageDailyRevenueForTheLast(int lastdays){
		
		BigDecimal averagecount = BigDecimal.ZERO;
		
		try{
			
			Query query  = em.createNativeQuery("select avg(total_kshs) as avgrev from (select date(convert_tz(timeStamp,'"+BaseActionBean.from_tz+"','"+BaseActionBean.to_tz+"')) dt, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit :lastdays) st ");
			query.setParameter("lastdays", lastdays);
			averagecount = (BigDecimal) query.getSingleResult();
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
		return averagecount;
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public BigDecimal getcurrentStats(){

		BigDecimal averagecount = BigDecimal.ZERO;
		
		try{
			
			Query query  = em.createNativeQuery("select date(convert_tz(timeStamp,'-05:00','+03:00')) dt, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit 1 ");
			
			List<Object[]> recs = query.getResultList();
			for(Object[] o : recs)
				averagecount = (BigDecimal) o[1];
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
		return averagecount;
	}
	
	@Override
	public BigDecimal getAverageHourlyRevenueForTheLast(int lastdays){
		
		BigDecimal averagecount = BigDecimal.ZERO;
		
		try{
			
			Query query  = em.createNativeQuery("select avg(total_kshs) as avgrev from (select date(convert_tz(timeStamp,'"+BaseActionBean.from_tz+"','"+BaseActionBean.to_tz+"')) dt, sum(price) total_kshs from  success_billing where success=1 AND  hour(timeStamp)<=hour(now())  group by dt order by dt desc limit :lastdays) st ");
			query.setParameter("lastdays", lastdays);
			averagecount = (BigDecimal) query.getSingleResult();
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}
		
		return averagecount;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getLinksLatencyStats() throws Exception{
		try{
			Query query  = em.createNativeQuery("select link, count(*) count, avg(latency) avgLat, date_format(convert_tz(timeStamp,'"+BaseActionBean.from_tz+"','"+BaseActionBean.to_tz+"'),'%Y-%m-%d %H:%i') ts  from latency_log where date(timestamp)=curdate() group by link, ts order by ts desc limit 10000");
		
			List<Object[]> recs = query.getResultList();
			
			JSONArray labels = new JSONArray();
			JSONArray data = new JSONArray();
			JSONArray datasets = new JSONArray();
			for(Object[] o : recs){
				String link = (String) o[0];
				BigDecimal averageLatencyPerSecond = (BigDecimal)  o[2];
				String timeStamp = (String) o[3];
				labels.put(timeStamp);
				data.put(averageLatencyPerSecond.doubleValue());
			}
			
			JSONObject mainObject = new JSONObject();
			mainObject.put("labels",labels);
			
			JSONObject dataset = new JSONObject();
			dataset.put("label", "Link Latency");
			dataset.put("fillColor", "#0080FF");
			dataset.put("strokeColor", "rgba(220,220,220,1)");
			dataset.put("pointColor", "rgba(220,220,220,1)");
			dataset.put("pointStrokeColor", "#fff");
			dataset.put("pointHighlightFill", "#fff");
			dataset.put("pointHighlightStroke", "rgba(220,220,220,1)");
			dataset.put("data",data);
			datasets.put(dataset);
			mainObject.put("datasets",datasets);
			
			return mainObject.toString();
			
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw exp;
		}
		
	}

}
