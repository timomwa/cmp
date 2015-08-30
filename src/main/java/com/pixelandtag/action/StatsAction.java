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
		
		String billingStats = cmp_dao.resource_bean.getBillingStats(from_tz,to_tz);
		
		return sendResponse(billingStats);
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public Resolution currentSubscriberDistribution() throws Exception{
		
		
		String currentSubDistro = cmp_dao.resource_bean.getCurrentSubDistribution();
		return sendResponse(currentSubDistro);
	}

	

}
