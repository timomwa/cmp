package com.pixelandtag.cmp.ejb.stats;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.TimerService;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.providers.TechSupportEJBI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;
import com.pixelandtag.cmp.entities.customer.configs.TechSupportMember;

@Singleton
public class PerformanceMonitorEJB {

	@Resource
	TimerService timerService;
	
	@EJB
	private LinkLatencyStatEJBI statsLinkLatEJB;
	
	@EJB
	private TimezoneConverterI timezoneconverterEJB;
	
	@EJB
	private QueueProcessorEJBI queueprocEJB;
	
	@EJB
	private TechSupportEJBI techsupportEJB;
	
	@EJB
	private OpcoSenderProfileEJBI opcosenderprofileEJB;
	
	private StringBuffer sb = new StringBuffer();
	
	private Logger logger = Logger.getLogger(getClass());
	
	private BigDecimal tolerance = new BigDecimal(7).multiply(new BigDecimal(-1));
	
	private boolean running = false;
	
	@Schedule(second = "0", minute = "/1", hour = "*", dayOfMonth="*", persistent = false, year="*" )
	public void checkHoulyAverage(){
		
		if(running)
			return;
		
		running = true;
		
		sb.setLength(0);
		
		BigDecimal averagehourly = statsLinkLatEJB.getAverageHourlyRevenueForTheLast( 7);
		
		
		BigDecimal currentstats = statsLinkLatEJB.getcurrentStats();
		BigDecimal decline = BigDecimal.ZERO;
		BigDecimal percentage_decline = BigDecimal.ZERO;
		if(averagehourly.compareTo(BigDecimal.ZERO)>0 & currentstats.compareTo(BigDecimal.ZERO)>0){
			decline = currentstats.subtract(averagehourly);
			percentage_decline = decline.divide(averagehourly, 6, 6).multiply(new BigDecimal(100) );
		}
		
		List<TechSupportMember> supportTeam = null;
		String message = null;
		
		try {
		
			
			
			if(percentage_decline.compareTo(BigDecimal.ZERO)!=0){
				boolean causeforalarm = (percentage_decline.compareTo(tolerance)<=0);
				String alarm = causeforalarm ? "Yes" : "No";
				message = "Average : "+averagehourly+" KES, \nCurrent Revenue: "+currentstats+" KES, \nDifference: "+percentage_decline+"% \n Cause for alarm ? "+alarm;
				supportTeam = techsupportEJB.getTechsupportWorkingAtThisHour();
				
				for(TechSupportMember member : supportTeam){
					
					OutgoingSMS outgoingsms = new OutgoingSMS();
					outgoingsms.setSms(message);
					outgoingsms.setCmp_tx_id("ALRM"+System.nanoTime());
					outgoingsms.setMsisdn(member.getMsisdn());
					OpcoSenderReceiverProfile opcosenderprofile = opcosenderprofileEJB.getActiveProfileForOpco(member.getOpco().getId());
					outgoingsms.setOpcosenderprofile(opcosenderprofile);
					
					try {
						queueprocEJB.saveOrUpdate(outgoingsms);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
				}

			}
			
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
		}finally{
			running  = false;
			sb.append("\t\t\t averagehourly \t : \t ").append(averagehourly).append("\n");
			sb.append("\t\t\t currentstats \t : \t ").append(currentstats).append("\n");
			sb.append("\t\t\t decline \t : \t ").append(decline).append("\n");
			sb.append("\t\t\t percentage_decline \t : \t ").append(percentage_decline).append("\n");
			sb.append("\t\t\t message \t : \t ").append(message).append("\n");
			sb.append("\t\t\t supportTeam \t : \t ").append(supportTeam).append("\n");
			
			logger.info("\n\n"+sb.toString()+"\n\n");
			
			
		}
		
		
	}

}
