package com.pixelandtag.cmp.ejb.sequences;

import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Remote
public class TimeStampSequenceEJBImpl implements TimeStampSequenceEJBI {

	@Override
	public Long getNextTimeStampNano() {
		return System.nanoTime();
	}

}
