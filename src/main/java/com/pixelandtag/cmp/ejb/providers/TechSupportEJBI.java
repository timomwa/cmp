package com.pixelandtag.cmp.ejb.providers;

import java.util.List;

import com.pixelandtag.cmp.entities.customer.configs.TechSupportMember;

public interface TechSupportEJBI {

	public List<TechSupportMember> getTechsupportWorkingAtThisHour() throws Exception;

}
