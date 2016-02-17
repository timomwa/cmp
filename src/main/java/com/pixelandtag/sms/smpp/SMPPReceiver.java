package com.pixelandtag.sms.smpp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.smpp.Connection;
import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.debug.LoggerDebug;
import org.smpp.pdu.BindReceiver;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.DataSM;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.pdu.SubmitSM;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.smssenders.Sender;

public class SMPPReceiver extends Thread implements ServerPDUEventListener {

	private Logger logger = Logger.getLogger(getClass());

	private LoggerDebug debug;

	private String serverip;
	private int serverport;
	private String type;//
	private String username;
	private String password;
	private String shortcode;
	private int version;
	private int smppid;

	private Session session;
	private Connection smppconn = null;

	public static boolean isalive = false;
	
	public static boolean running = true;

	public SMPPReceiver(SenderConfiguration configs) {
		serverip = configs.getOpcoconfigs().get(Sender.SMPP_IP).getValue();
		serverport = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_PORT).getValue());
		type = configs.getOpcoconfigs().get(Sender.SMPP_TYPE).getValue();
		username = configs.getOpcoconfigs().get(Sender.SMPP_USERNAME).getValue();
		password = configs.getOpcoconfigs().get(Sender.SMPP_PASSWORD).getValue();
		shortcode = configs.getOpcoconfigs().get(Sender.SMPP_SHORTCODE).getValue();
		version = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_VERSION).getValue());
		smppid = Integer.parseInt(configs.getOpcoconfigs().get(Sender.SMPP_ID).getValue());

		this.debug = new LoggerDebug("SMPPReceiver");
		load();
	}

	private void load() {
		this.debug.activate();
		Session.setDebug(this.debug);

		this.smppconn = new TCPIPConnection(serverip, serverport);
		try {
			this.smppconn.open();
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	public void run() {

		isalive = false;
		while (running) {
			if ((!isalive) || (!isSMPPAlive())) {
				unbind();
				boolean bound = bind();
				updateSMPP(bound);
			}
			try {
				sleep(10000L);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void handleEvent(ServerPDUEvent event) {
		PDU pdu = event.getPDU();
		logger.info("Got event - smppid: " + smppid + " - isRequest: " + pdu.isRequest() + " - type: "
				+ pdu.getClass().getSimpleName());

		Response response = null;
		try {
			if (pdu.isRequest()) {
				if (((pdu instanceof DeliverSM)) || ((pdu.isRequest()) && ((pdu instanceof DataSM)))) {
					if ((pdu instanceof DataSM))
						response = ((DataSM) pdu).getResponse();
					else {
						response = (DeliverSMResp) ((DeliverSM) pdu).getResponse();
					}

					response.setCommandId(-2147483643);
					response.setCommandStatus(0);
					response.setCommandLength(pdu.getCommandLength());
					response.setSequenceNumber(pdu.getSequenceNumber());
				} else if ((pdu.isRequest()) && ((pdu instanceof EnquireLink))) {
					response = (EnquireLinkResp) ((Request) pdu).getResponse();
					response.setCommandStatus(0);
					response.setCommandLength(pdu.getCommandLength());
					response.setSequenceNumber(pdu.getSequenceNumber());
				} else {
					response = ((SubmitSM) pdu).getResponse();
				}
				synchronized (this.session) {
					this.session.respond(response);
				}
			} else if ((pdu.isResponse()) && (pdu.getCommandId() == -2147483627)) {
				if (pdu.getCommandStatus() != 0)
					isalive = false;
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	private boolean isSMPPAlive() {

		boolean retval = false;
		try {
			synchronized (this.session) {
				if (!this.session.isOpened())
					throw new Exception("Connection is NOT OPEN!");
				this.session.enquireLink(new EnquireLink());
				retval = true;
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		return retval;
	}

	private void unbind() {

		logger.info("UNBinding receiver.....");
		try {
			if (this.smppconn != null)
				this.smppconn.close();
			this.smppconn = null;
			if (null != this.session) {
				synchronized (this.session) {
					this.session.unbind();
					this.session.close();
				}
			}
			isalive = false;
		} catch (Exception e) {
			logger.error(e);
		}
		updateSMPP(false);
	}

	public void kill() {

		interrupt();
		unbind();
	}

	private boolean bind() {

		boolean bound = false;
		try {
			logger.info("Binding receiver.....");

			if (smppconn.isOpened()) {
				this.session = new Session(this.smppconn);
				this.session.unbind();

				BindRequest request = new BindReceiver();
				request.setSystemId(username);
				request.setInterfaceVersion((byte) version);
				request.setAddressRange(shortcode);
				request.setPassword(password);
				request.setSystemType(type);

				BindResponse response = this.session.bind(request, this);
				if (null == response) {
					logger.error("BindException: Error:null from session.bind SMPPID:" + smppid + " SystemID:"
							+ username + " Password:" + password + " ShortCode:" + shortcode);
					try {
						this.session.close();
						this.session = null;
					} catch (Exception e) {
						logger.error(e);
					}
					try {
						this.smppconn.close();
						this.smppconn = null;
					} catch (Exception e) {
						logger.error(e);
					}
					bound = false;
					isalive = false;
				} else {
					logger.info("Receiver :: " + response.debugString());
					logger.info("Command Status :: " + response.getCommandStatus());
					if (0 != response.getCommandStatus()) {
						bound = false;
						isalive = false;
						logger.error("BindException: Error:" + response.getCommandStatus() + " SMPPID:" + smppid
								+ " SystemID:" + username + " Password:" + password + " ShortCode:" + shortcode
								+ " Type : " + type);
					} else {
						bound = true;
						isalive = true;
					}
				}
			}else{
				logger.error("SMPP Connection not open");
			}
		} catch (Exception e) {
			bound = false;
			logger.error("server(" + serverip + ":" + serverport + "): " + e, e);
		}

		return bound;
	}

	private void updateSMPP(boolean status) {
		
	}
}
