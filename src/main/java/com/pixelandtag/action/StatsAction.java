package com.pixelandtag.action;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.stats.LinkLatencyStatEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterEJB;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

public class StatsAction extends BaseActionBean {
	
	private Logger logger = Logger.getLogger(getClass());
	private Random rand = new Random();
	
	@EJB(mappedName =  "java:global/cmp/LinkLatencyStatEJBImpl")
	private LinkLatencyStatEJBI statsLinkLatEJB;

	SimpleDateFormat formatDayOfMonth  = new SimpleDateFormat("d");
	
	public String convertToPrettyFormat(Date date){
		int day = Integer.parseInt(formatDayOfMonth.format(date));
		String suff  = TimezoneConverterEJB.getDayNumberSuffix(day);
		DateFormat prettier_df = new SimpleDateFormat("d'"+suff+"' E");
	    return prettier_df.format(date);
	}
	
	
	public Resolution getLinksLatencyStats(){
		String resp = "";
		try {
			resp = statsLinkLatEJB.getLinksLatencyStats();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		logger.info("\n\n\nSTATS RESP:::: "+resp+"\n\n\n");
		return sendResponse(resp);
	}
	
	@SuppressWarnings("unchecked")
	@DefaultHandler
	@RolesAllowed({"admin"})
	public Resolution getStats() throws JSONException{
		
		Query qry = cmp_dao.resource_bean.getEM().createNativeQuery("select date(convert_tz(timeStamp,'"+from_tz+"','"+to_tz+"')) dt, count(*) count, price, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit 31");
		
		List<Object[]> recs = qry.getResultList();
		
		JSONObject mainObject = new JSONObject();
		
		JSONObject data = new JSONObject();
		JSONArray labels =  new JSONArray();
		JSONArray dataArray = new JSONArray();
		JSONArray datasets =  new JSONArray();
		
		for(Object[] o : recs){
			Date date = (Date) o[0];
			BigInteger count = (BigInteger) o[1];
			BigDecimal total_kshs = (BigDecimal) o[3];
			
			
			labels.put(convertToPrettyFormat(date));
			dataArray.put(total_kshs.longValue());
			
		}
		data.put("data", dataArray);
		data.put("fillColor", "rgba(151,187,205,1)");
		data.put("strokeColor", "rgba(151,187,205,0.8)");
		data.put("highlightFill", "rgba(151,187,205,0.75)");
		data.put("highlightStroke", "rgba(151,187,205,1)");
		datasets.put(data);
		
		

		mainObject.put("datasets", datasets);
		mainObject.put("labels", labels);
		return sendResponse(mainObject.toString());
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public Resolution currentSubscriberDistribution() throws JSONException{
		Query qry = cmp_dao.resource_bean.getEM().createNativeQuery("select  count(*) count, sms.service_name as 'service_name' from subscription sub left join sms_service sms on sms.id = sub.sms_service_id_fk where sub.subscription_status='confirmed' group by sms.service_name");
		
		List<Object[]> recs = qry.getResultList();
		
		JSONObject data = new JSONObject();
		JSONArray dataArray = new JSONArray();
		
		for(Object[] o : recs){
			
			BigInteger count = (BigInteger) o[0];
			String service_name = (String) o[1];
			
			String colorHex = getRandomHexColor();
			JSONObject dataPiece = new JSONObject();
			dataPiece.put("value", count);
			dataPiece.put("label", service_name);
			dataPiece.put("color", "#"+colorHex);
			dataPiece.put("highlight", "#"+incrementColor(colorHex,50));
			
			dataArray.put(dataPiece);
			
		}
		data.put("data", dataArray);
		
		return sendResponse(data.toString());
	}

	
	private String incrementColor(String hexVal, int increment) {
		int value = Integer.parseInt(hexVal, 16);
		value += increment;
		return Integer.toHexString(value);
	}
	

	private String getRandomHexColor() {
	    char letters[] = "0123456789ABCDEF".toCharArray();
	    String color = "";
	    for (int i = 0; i < 6; i++ ) {
	        color += letters[(rand.nextInt((15 - 0) + 1) + 0)];
	    }
	    return color;
	}
}
