package com.pixelandtag.cmp.ejb.api.sms;

import java.util.List;

import com.pixelandtag.api.MessageStatus;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.cmp.entities.customer.configs.OpcoSenderReceiverProfile;


public interface QueueProcessorEJBI  {

	public List<OutgoingSMS> getUnsent(Long size);
	public List<OutgoingSMS> getUnsent(Long size, OpcoSenderReceiverProfile opcoSenderReceiverProfile);
	public OutgoingSMS saveOrUpdate(OutgoingSMS queue) throws Exception;
	public IncomingSMS saveOrUpdate(IncomingSMS incomingsms) throws Exception;
	public boolean deleteFromQueue(OutgoingSMS sms);
	public boolean updateMessageLog(String cmp_tx_id, MessageStatus status)  throws Exception;
	public boolean updateMessageLog(OutgoingSMS sms, MessageStatus status);
	public void updateQueueStatus(Long id, Boolean inqueue);
	public List<IncomingSMS> getLatestMO(int size);
	public boolean deleteCorrespondingIncomingSMS(OutgoingSMS sms);
	

}
