package com.pixelandtag.action;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

public class StatsAction extends BaseActionBean {
	
	@SuppressWarnings("unchecked")
	@DefaultHandler
	@RolesAllowed({"admin"})
	public Resolution getStats() throws JSONException{
		Query qry = cmp_dao.resource_bean.getEM().createNativeQuery("select date(timeStamp) dt, count(*) count, price, sum(price) total_kshs from  success_billing where success=1  group by dt order by dt desc limit 30");
				//"select date(timeStamp) dt, count(*) count, price, sum(price) total_kshs from  billable_queue where success=1 and in_outgoing_queue=0 and  processed=1 group by dt order by dt desc limit 30");
		List<Object[]> recs = qry.getResultList();
		
		JSONObject mainObject = new JSONObject();
		
		JSONArray data = new JSONArray();
		
		for(Object[] o : recs){
			JSONObject bilRec = new JSONObject();
			
			Date date = (Date) o[0];
			
			System.out.println("\n\n\n\t\t:::date: "+date);
			BigInteger count = (BigInteger) o[1];
			//String price = (String) o[2];
			BigDecimal total_kshs = (BigDecimal) o[3];
			
			bilRec.put("name", date);
			bilRec.put("hits", count.longValue());
			bilRec.put("revenue", total_kshs.longValue());
			
			data.put(bilRec);
			
		}
		
		//mainObject.put("fields", fields);
		mainObject.put("data", data);
		
		System.out.println("\n\n\n\t\t:::::"+mainObject.toString()+"\n\n\n");
		
		return sendResponse(mainObject.toString());
	}

}
