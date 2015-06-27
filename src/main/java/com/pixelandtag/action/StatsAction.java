package com.pixelandtag.action;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterEJB;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

public class StatsAction extends BaseActionBean {
	
	private final String from_tz = "-04:00";
	private final String to_tz  = "+03:00";
	private Logger logger = Logger.getLogger(getClass());
	
SimpleDateFormat formatDayOfMonth  = new SimpleDateFormat("d");
	
	public String convertToPrettyFormat(Date date){
		int day = Integer.parseInt(formatDayOfMonth.format(date));
		String suff  = TimezoneConverterEJB.getDayNumberSuffix(day);
		DateFormat prettier_df = new SimpleDateFormat("d'"+suff+"' E");
	    return prettier_df.format(date);
	}
	
	@SuppressWarnings("unchecked")
	@DefaultHandler
	@RolesAllowed({"admin"})
	public Resolution getStats() throws JSONException{
		Query qry = cmp_dao.resource_bean.getEM().createNativeQuery("select date(convert_tz(timeStamp,'"+from_tz+"','"+to_tz+"')) dt, count(*) count, price, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit 20");
		
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

}
