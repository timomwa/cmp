package com.pixelandtag.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

public class BillingMonitor extends BaseActionBean {

	@DefaultHandler
	public Resolution show(){
		return statsMonitorPage;
	}
}
