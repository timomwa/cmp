package com.pixelandtag.sms.smpp.workers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.smpp.Data;
import org.smpp.ServerPDUEvent;
import org.smpp.pdu.DataSM;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.PDU;

import com.pixelandtag.sms.smpp.beans.ReceiverBean;
import com.pixelandtag.util.Pair;

/**
 * Should run on their own
 * Listens SMSC for incomming messages
 * @author Paul
 *
 */
public class PDUWorker implements Runnable{
	
	private Logger LOGGER = Logger.getLogger(getClass());
	
	private BlockingQueue<Pair> queue;
	
	private Thread thread;
	
	private boolean running = true;

	public PDUWorker(BlockingQueue<Pair> queue) {

		this.queue = queue;
	}

	public void run() {

		Pair p = null;
		ReceiverBean receiverBean = null;
		ServerPDUEvent event = null;
		PDU pdu = null;
		DeliverSMResp response = null;
		DeliverSM sm = null;
		DataSM dataSM = null;
		while (running)
			try {
				p = this.queue.poll(500L, TimeUnit.SECONDS);
				if (null != p) {
					receiverBean = (ReceiverBean) p.getA();
					event = (ServerPDUEvent) p.getB();
					pdu = event.getPDU();
					this.LOGGER.debug("PDU Command " + pdu.getCommandId());
					switch (pdu.getCommandId()) {
						case Data.DELIVER_SM :
							response = (DeliverSMResp) ((DeliverSM) pdu)
									.getResponse();

							sm = (DeliverSM) response.getOriginalRequest();

							switch (sm.getEsmClass()) {
								case Data.ESME_ROK :
								case -128 :
								case 1 :
								case 3 :
									try {
										processMessage(sm,receiverBean);
									} catch (Exception e) {
										this.LOGGER.error(e);
									}
									break;
								case Data.SM_SMSC_DLV_RCPT_TYPE :
									try {
										processDeliveryRequest(sm,receiverBean);
									} catch (Exception e) {
										this.LOGGER.error(e);
									}
									break;
								default :
									this.LOGGER.debug("Unhandled EsmClass:"
											+ sm.getEsmClass());
									break;
							}
							break;

						case 259 :
							this.LOGGER.debug("DataSM: " + pdu.debugString());
							dataSM = (DataSM) pdu;
							switch (dataSM.getEsmClass()) {
								case -128 :
								case 0 :
								case 1 :
								case 3 :
									try {
										processMessage(dataSM,receiverBean);
									} catch (Exception e) {
										this.LOGGER.error(e);
									}
									break;
								case Data.SM_SMSC_DLV_RCPT_TYPE :
									try {
										processDeliveryRequest(sm,receiverBean);
									} catch (Exception e) {
										this.LOGGER.error(e);
									}
									break;
								default :
									this.LOGGER.debug("Unhandled EsmClass:"
											+ dataSM.getEsmClass());
									break;
							}
							break;

						default :
							this.LOGGER.debug("Unhandled commandId:"
									+ pdu.getCommandId());
							break;
					}

				}

			} catch (Exception e) {
				this.LOGGER.error(e);
			}
	}

	private void processMessage(DeliverSM sm,ReceiverBean receiverBean) {

		String shortcode = sm.getDestAddr().getAddress();
		if (shortcode.indexOf(43) >= 0) {
			shortcode = shortcode.substring(1);
		}
		String message = sm.getShortMessage();
		String msisdn = sm.getSourceAddr().getAddress();
		
		if (null == message)
			message = "";
		message = message.replace((char) 17, '_');
		message = message.trim();
		if (((message.contains("_")) && (message.length() == 18))
				|| ((message.startsWith("5")) && (message.length() == 19))) {
			message = message.replace("_", " ");
		}
		try {
			message = message.replace((char) 0, '@');
			message = message.replace((char) 5, 'é');
			message = message.replace((char) 2, '$');
			message = message.replace((char) 4, 'ê');
			message = message.replace((char) 6, 'ù');
			message = message.replace((char) 8, 'ò');
			message = message.replace((char) 9, 'ç');
			message = message.replace((char) 31, 'È');
		} catch (Exception e) {
		}
		message = message.replace("\n", "").replace("\r", "");
		
		LOGGER.info("Incoming from " + msisdn+" : "+message);
		LOGGER.info("This message needs to go to a service.");
	}

	private void processMessage(DataSM dataSM,ReceiverBean receiverBean) {

		String shortcode = dataSM.getDestAddr().getAddress();
		if (shortcode.indexOf(43) >= 0) {
			shortcode = shortcode.substring(1);
		}

		String message = null;
		try {
			message = new String(dataSM.getMessagePayload().getBuffer());
		} catch (Exception ex) {
		}
		if (message == null)
			try {
				message = new String(dataSM.getData().getBuffer());
			} catch (Exception ex) {
			}
		else {
			this.LOGGER
					.debug("new String(sm2.getMessagePayload().getBuffer()):"
							+ message);
		}

		if (message == null)
			try {
				message = new String(dataSM.getBody().getBuffer());
			} catch (Exception ex) {
			}
		else {
			this.LOGGER.debug("new String(sm2.getData().getBuffer()):"
					+ message);
		}

		if (message == null) {
			this.LOGGER.error("message is null :-(");
		}

		String msisdn = dataSM.getSourceAddr().getAddress();
		
		
		if (null == message)
			message = "";
		message = message.replace((char) 17, '_');
		message = message.trim();
		if (((message.contains("_")) && (message.length() == 18))
				|| ((message.startsWith("5")) && (message.length() == 19))) {
			message = message.replace("_", " ");
		}
		try {
			message = message.replace((char) 0, '@');
			message = message.replace((char) 5, 'é');
			message = message.replace((char) 2, '$');
			message = message.replace((char) 4, 'ê');
			message = message.replace((char) 6, 'ù');
			message = message.replace((char) 8, 'ò');
			message = message.replace((char) 9, 'ç');
			message = message.replace((char) 31, 'È');
		} catch (Exception e) {
		}
		message = message.replace("\n", "").replace("\r", "");
		
		//process
		LOGGER.info("Incoming from " + msisdn+" : "+message);
		LOGGER.info("This message needs to go to a service.");
	}

	public void processDeliveryRequest(DeliverSM sm,ReceiverBean receiverBean) {

		String shortcode = sm.getDestAddr().getAddress();
		if (shortcode.indexOf(43) >= 0) {
			shortcode = shortcode.substring(1);
		}

		String str = sm.getShortMessage();
		LOGGER.info("Delivery request : >> " + str);
		String stat = "";
		if ((str.indexOf(" stat:") > 0)) {
			stat = str.substring(str.indexOf("stat:") + 5,
					str.indexOf("stat:") + 12);
		} else {
			try {
				stat = str.substring(36, 43);
			} catch (Exception e) {
			}
		}
		LOGGER.debug("delevery status: " + stat);
		String msgid = "";
		String msisdn = sm.getSourceAddr().getAddress();
		if (str.startsWith("id:")) {
			msgid = str.substring(3, str.indexOf(" ", 4));
			if (msgid != null)
				msgid = msgid.trim();
			LOGGER.debug("msgid before: " + msgid);
			// msgid =
			// Long.toHexString(Long.parseLong(msgid));
			while (msgid.length() < 8)
				msgid = "0" + msgid;
			LOGGER.debug("msgid after: " + msgid);
		} 

		LOGGER.info("Delivery request " + msisdn+" : "+msgid);
		//update delivery
	}
	
	public void start() {
		if (this.thread != null) {
			stop();
		}
		running = true;
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void stop() {
		running = false;
		try {
			synchronized (this.thread) {
				this.thread.wait();
			}
		} catch (InterruptedException e) {
			this.LOGGER.error(e, e);
		}
	}
}
