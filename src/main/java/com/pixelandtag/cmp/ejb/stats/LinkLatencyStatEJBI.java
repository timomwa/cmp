package com.pixelandtag.cmp.ejb.stats;

import java.math.BigDecimal;
import java.util.Date;

public interface LinkLatencyStatEJBI {
	
	public String getLinksLatencyStats() throws Exception;

	public BigDecimal getAverageDailyRevenueForTheLast(int lastdays);
	
	public BigDecimal getAverageHourlyRevenueForTheLast(int lastdays);

	public BigDecimal getcurrentStats();

}
