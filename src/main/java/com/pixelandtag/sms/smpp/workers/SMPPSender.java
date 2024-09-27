package com.pixelandtag.sms.smpp.workers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.smpp.Connection;
import org.smpp.Data;
import org.smpp.ServerPDUEvent;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.TimeoutException;
import org.smpp.WrongSessionStateException;
import org.smpp.pdu.Address;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.util.ByteBuffer;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.util.StringUtils;

public class SMPPSender extends Thread {

	private Logger logger = Logger.getLogger(getClass());

	private String serverip;
	private int serverport;
	private String type;
	private String username;
	private String password;
	private int ton;
	private int npi;
	private int destinationton;
	private int destinationnpi;
	private String shortcode;
	private int version;
	private int smppid;

	private Session session;
	private Connection smppconn = null;

	public static boolean isalive = false;

	private BindRequest request = null;
	private BindResponse response = null;
	
	public static boolean running = true;

	public SMPPSender(SenderConfiguration configs) {
		serverip = configs.getOpcoconfigs().get(Sender.SMPP_IP).getValue();
		serverport = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_PORT).getValue());
		type = configs.getOpcoconfigs().get(Sender.SMPP_TYPE).getValue();
		username = configs.getOpcoconfigs().get(Sender.SMPP_USERNAME).getValue();
		password = configs.getOpcoconfigs().get(Sender.SMPP_PASSWORD).getValue();
		ton = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_TON).getValue());
		npi = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_NPI).getValue());
		destinationton = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_DESTON).getValue());
		destinationnpi = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_DESNPI).getValue());
		shortcode = configs.getOpcoconfigs().get(Sender.SMPP_SHORTCODE).getValue();
		version = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_VERSION).getValue());
		smppid = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_ID).getValue());

		load();
	}

	public void run() {

		reconnect();
		while (running) {
			isConnectionAlive();
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}

	private void load() {

		this.smppconn = new TCPIPConnection(this.serverip, this.serverport);
		this.session = new Session(this.smppconn);
	}

	public void reconnect() {

		connect();
	}

	public void connect() {

		this.request = new BindTransmitter();
		try {
			this.request.setSystemId(this.username);
			this.request.setPassword(this.password);
			this.request.setSystemType(this.type);
			this.request.setInterfaceVersion((byte) this.version);
			this.request.setAddressRange(this.shortcode);
			this.response = this.session.bind(this.request);
			if ((this.response != null) && (this.response.getCommandStatus() == 0)) {
				logger.debug("Binding successful :" + this.serverip);
				updateSMPP(true);
			} else {
				updateSMPP(false);
				if (this.response == null) {
					logger.debug("org.smpp.pdu.BindResponse object is null.");
				}
				logger.error("BindException: Error:" + this.response.getCommandStatus() + " ID :" + smppid
						+ " Username:" + username + " Password:" + password + " ShortCode:" + shortcode);
			}

		} catch (WrongLengthOfStringException e) {
			logger.error(e);
		} catch (ValueNotSetException e) {
			logger.error(e);
		} catch (TimeoutException e) {
			logger.error(e);
		} catch (PDUException e) {
			logger.error(e);
		} catch (WrongSessionStateException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void disconnect() {

		try {
			this.session.unbind();
			this.session.close();

			if (this.smppconn != null) {
				this.smppconn.close();
			}
			this.smppconn = null;
			this.session = null;
		} catch (ValueNotSetException e) {
			logger.error(e);
		} catch (TimeoutException e) {
			logger.error(e);
		} catch (PDUException e) {
			logger.error(e);
		} catch (WrongSessionStateException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			updateSMPP(false);
		}
	}

	private void updateSMPP(boolean connected) {

	}

	public String resolvePDU(int id) {

		String name = "Unknown PDU";
		switch (id) {
		case -2147483647:
			name = "BindReceiverResp";
			break;
		case -2147483646:
			name = "BindTransmitterResp";
			break;
		case -2147483627:
			name = "EnquirelinkResp";
			break;
		case -2147483389:
			name = "DataSMResp";
			break;
		case -2147483641:
			name = "ReplaceSMResp";
			break;
		case -2147483644:
			name = "SubmitSMResp";
			break;
		default:
			name = "Unknown PDU";
		}

		return name;
	}

	public String send(OutgoingSMS outgoingsms) {

		if (outgoingsms.getSms().length() <= 160) {
			return sendSingle(outgoingsms);
		} else
			return sendConcat(outgoingsms);
	}

	public String sendConcat(OutgoingSMS outgoingsms) {

		String messageId = "-2";
		String txt = outgoingsms.getSms();
		logger.debug("sendConcat: before sendConcat session");
		try {
			synchronized (this.session) {
				if (!this.session.isOpened()) {
					reconnect();
				}
				int cmdStatus = 99;
				SubmitSM msg = new SubmitSM();
				msg.assignSequenceNumber(true);
				msg.setRegisteredDelivery((Data.SM_SMSC_RECEIPT_REQUESTED));
				msg.setValidityPeriod("000000020000000R");
				msg.setSequenceNumber(outgoingsms.getId().intValue());

				msg.setSourceAddr(new Address((byte) ton, (byte) npi, shortcode));

				msg.setDestAddr(new Address((byte) destinationton, (byte) destinationnpi, outgoingsms.getMsisdn()));
				SubmitSMResp msgResp = null;
				if (txt.trim().length() <= 254) {
					ByteBuffer buffer = new ByteBuffer();
					buffer.appendString(txt, "ASCII");
					msg.setShortMessageData(buffer);

					msg.setDataCoding((byte) 0);
					if (!this.session.isBound() && !this.session.isOpened()) {
						reconnect();
					}
					msgResp = this.session.submit(msg);
				} else {
					Vector<String> string = StringUtils.splitText(txt);
					for (int i = 0; i < string.size(); i++) {
						ByteBuffer buffer = new ByteBuffer();
						buffer.appendString(i + 1 + ". " + (String) string.get(i), "ASCII");
						msg.setShortMessageData(buffer);

						msg.setDataCoding((byte) 0);
						msgResp = this.session.submit(msg);
					}
				}

				if (msgResp == null) {
					logger.warn("sendConcat: NULL response from submit()");
				} else {
					cmdStatus = msgResp.getCommandStatus();
					messageId = msgResp.getMessageId();

					logger.debug("sendConcat: Concat status: " + cmdStatus + " MSGID: " + messageId + " to:"
							+ outgoingsms.getMsisdn());

					if ((cmdStatus == 88) || (cmdStatus == 20)) {
						logger.info("sendConcat: Sending to fast, wait");
						try {
							Thread.sleep(500L);
						} catch (Exception e) {
						}
					}
				}

			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		return messageId;
	}

	private String sendSingle(OutgoingSMS outgoingsms) {
		String txt = outgoingsms.getSms();
		String messageId = "0000";
		try {
			synchronized (this.session) {
				int cmdStatus = 99;
				if (!this.session.isBound() && !this.session.isOpened()) {
					reconnect();
				}
				SubmitSM msg = new SubmitSM();
				SubmitSMResp msgResp = null;
				msg.assignSequenceNumber(true);
				msg.setRegisteredDelivery((Data.SM_SMSC_RECEIPT_REQUESTED));
				msg.setValidityPeriod("000000020000000R");
				msg.setSequenceNumber(outgoingsms.getId().intValue());

				msg.setSourceAddr(new Address((byte) ton, (byte) npi, shortcode));

				msg.setDestAddr(new Address((byte) destinationnpi, (byte) destinationnpi, outgoingsms.getMsisdn()));

				ByteBuffer buffer = new ByteBuffer();
				buffer.appendString(txt, "ASCII");
				msg.setShortMessageData(buffer);

				msg.setDataCoding((byte) 0);

				msgResp = this.session.submit(msg);

				if (msgResp == null) {
					logger.warn("sendConcat: NULL response from submit()");
				} else {
					cmdStatus = msgResp.getCommandStatus();
					messageId = msgResp.getMessageId();

					logger.debug("sendConcat: Concat status: " + cmdStatus + " MSGID: " + messageId + " to:"
							+ outgoingsms.getMsisdn());

					if ((cmdStatus == 88) || (cmdStatus == 20)) {
						logger.info("sendConcat: Sending to fast, wait");
						try {
							Thread.sleep(500L);
						} catch (Exception e) {
						}
					}
				}

			}
		} catch (Exception e) {
			logger.error("SmppSender sendTxt:" + txt + e.toString());
		}

		return messageId;
	}

	public void handleEvent(ServerPDUEvent event) {

		PDU pdu = event.getPDU();
		logger.debug("Got event - smppid: " + smppid + " - isRequest: " + pdu.isRequest() + " - type: "
				+ pdu.getClass().getSimpleName());

		logger.debug(resolvePDU(pdu.getCommandId()) + " status: " + pdu.getCommandStatus());
		try {
			if ((pdu instanceof SubmitSMResp)) {
				SubmitSMResp resp = (SubmitSMResp) pdu;
				handleSubmitSMResp(resp);
			} else if ((pdu instanceof EnquireLinkResp)) {
				EnquireLinkResp resp = (EnquireLinkResp) pdu;
				handleEnquireLinkResp(resp);
			}
		} catch (Exception e) {
			logger.error(resolvePDU(pdu.getCommandId()) + " status: " + pdu.getCommandStatus());
		}
	}

	private void handleEnquireLinkResp(EnquireLinkResp resp) {

		logger.debug("Async Sender ID: " + smppid + " Is alive...");
		if (resp.getCommandStatus() == 0) {
			isalive = true;
		} else {
			isalive = false;
			logger.debug("Error link resp in receiver: " + resp.getCommandStatus());
		}
	}

	private Map<String, Object> handleSubmitSMResp(SubmitSMResp resp) {
		Map<String, Object> response = new HashMap<String, Object>();
		if ((resp.getSequenceNumber() > 0) && (resp.getCommandStatus() == 0)) {
			int id = resp.getSequenceNumber();
			String msgID = resp.getMessageId();
			int commandStatus = resp.getCommandStatus();

			int send = 0;
			if ((msgID != null) && (msgID.length() != 0))
				send = 1;
			else {
				msgID = "0000";
			}

			logger.info("submit response for id:" + id + " with SMSC MSGID: " + msgID + " status:" + commandStatus);
			response.put("messageid", id);
			response.put("sent", send);
			response.put("status", commandStatus);
			response.put("smscid", msgID);
		} else {
			logger.error("Received Submit Response without a sequence number - SMSC MSGID: " + resp.getMessageId());
			response.put("messageid", resp.getMessageId());
		}
		return response;
	}

	public boolean isConnectionAlive() {

		try {
			synchronized (this.session) {
				if (!this.session.isOpened()) {
					logger.debug("Connection is NOT OPEN!!!!!");
					updateSMPP(false);
					logger.debug("Will try rebind");
					reconnect();
					return false;
				}
				EnquireLink link = new EnquireLink();
				EnquireLinkResp res = this.session.enquireLink(link);
				if ((res != null) && (res.getCommandStatus() == 0)) {
					return true;
				} else {
					logger.error("Sender is not alive " + res.getCommandStatus());
					reconnect();
				}
			}
		} catch (Exception e) {
			logger.error("IsAlive Smpp Connection error:" + e.toString());
		}
		return false;
	}
}
