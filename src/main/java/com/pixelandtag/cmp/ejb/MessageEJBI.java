package com.pixelandtag.cmp.ejb;

import com.pixelandtag.cmp.entities.Message;

public interface MessageEJBI {

	public Message getMessage(String key, Long language_id);

}
