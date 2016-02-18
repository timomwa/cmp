package com.pixelandtag.sms.smpp;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.smpp.Connection;
import org.smpp.Data;
import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.TimeoutException;
import org.smpp.WrongSessionStateException;
import org.smpp.debug.LoggerDebug;
import org.smpp.pdu.Address;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransciever;
import org.smpp.pdu.DataSM;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.Unbind;
import org.smpp.pdu.UnbindResp;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.util.ByteBuffer;

import com.pixelandtag.cmp.ejb.api.sms.SenderConfiguration;
import com.pixelandtag.cmp.entities.OutgoingSMS;
import com.pixelandtag.smssenders.Sender;
import com.pixelandtag.util.Concatenator;

public class Transceiver extends Thread implements ServerPDUEventListener{
	

	private Logger logger = Logger.getLogger(Transceiver.class);

	private Connection smppconn = null;

	private BindRequest request = null;

	private BindResponse response = null;

	private Session session;

	private LoggerDebug debug;
	
	private Concatenator concatenator = null;
	
	private int smscount;

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
	
	public static boolean running = true;
	
	public Transceiver(SenderConfiguration configs){
		debug = new LoggerDebug("smpp");
		concatenator = new Concatenator();
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
		this.smppconn = new TCPIPConnection(serverip, serverport);
		try {
			this.smppconn.open();
			bind();
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	private boolean createTCPIPConnection(){
		try{
			if(this.smppconn==null){
				this.smppconn = new TCPIPConnection(this.serverip, this.serverport);
			}
			this.smppconn.open();
			return true;
		}catch(Exception e){
			logger.error(e);
		}
		return false;
	}
	
	
	public void run() {

		reconnect();
		while (running) {
			isConnectionAlive();
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}
	
	private void bind() {
		reconnect();
	}
	
	private void reconnect(){
		disconnect();
		connect();
	}

	public boolean isConnectionAlive() {

		try {
			synchronized (this.session) {
				if (!this.session.isOpened()) {
					logger.debug("SMSC Connection is not open!!!!!");
					// update db
					return false;
				}
				EnquireLink link = new EnquireLink();
				this.session.enquireLink(link);
				return true;
			}
		} catch (Exception e) {
			logger.error("IsAlive Smpp Connection error:" + e.toString());
		}
		return false;
	}

	private void connect() {

		session = new Session(this.smppconn);
		this.debug.activate();
		Session.setDebug(this.debug);
		this.request = new BindTransciever();
		try {
			this.request.setSystemId(this.username);
			this.request.setPassword(this.password);
			this.request.setSystemType(this.type);
			this.request.setInterfaceVersion((byte) this.version);
			this.request.setAddressRange(this.shortcode);
			logger.info("Trying to bind " + " Username:" + getUsername() + " Password:" + getPassword() + " ShortCode:"
					+ getShortcode());
			this.response = this.session.bind(this.request, this);
			if ((this.response != null) && (this.response.getCommandStatus() == 0)) {
				logger.info("Binding successful :" + this.serverip);
				// Update DB bound
			} else {
				// update DB. No bound
				if (this.response == null) {
					logger.debug("org.smpp.pdu.BindResponse object is null.");
				}
				logger.error("BindException: Error:" + this.response.getCommandStatus() + " Username:" + getUsername()
						+ " Password:" + getPassword() + " ShortCode:" + getShortcode());
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
			if (this.session != null) {
				this.session.unbind();
				this.session.close();
			}
			this.session = null;
		} catch (ValueNotSetException e) {
		} catch (TimeoutException e) {
		} catch (PDUException e) {
		} catch (WrongSessionStateException e) {
		} catch (IOException e) {
		} finally {
			// Upate Database to not bound
		}
	}

	public void handleEvent(ServerPDUEvent event) {

		PDU pdu = event.getPDU();

		logger.debug("Got event " + " - isRequest: " + pdu.isRequest() + " - type: " + pdu.getClass().getSimpleName());

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
					if ((response != null) && (response.getCommandStatus() == 0)) {
						
						logger.debug("Is alive ");
						// update DB
					} else {
						logger.error("Sender is not alive " + response.getCommandStatus());

						// update DB
					}
				} else {
					response = ((SubmitSM) pdu).getResponse();
				}
				synchronized (this.session) {
					this.session.respond(response);
				}
			} else if ((pdu instanceof SubmitSMResp)) {
				SubmitSMResp resp = (SubmitSMResp) pdu;
				handleSubmitSMResp(resp); //delivery response from smsc
			} else if ((pdu instanceof EnquireLinkResp)) {
				EnquireLinkResp resp = (EnquireLinkResp) pdu;
				handleEnquireLinkResp(resp);
			} else {
				if ((pdu instanceof EnquireLink)) {
					EnquireLinkResp resp = (EnquireLinkResp) ((Request) pdu).getResponse();

					resp.setCommandStatus(0);
					resp.setCommandLength(pdu.getCommandLength());
					resp.setSequenceNumber(pdu.getSequenceNumber());

					if ((resp != null) && (resp.getCommandStatus() == 0)) {
						logger.debug("Is alive ");
						// update DB
					} else {
						logger.error("Sender is not alive " + resp.getCommandStatus());

						// update DB
					}

					synchronized (this.session) {
						this.session.respond(resp);
					}
					return;
				}

				if ((pdu instanceof Unbind)) {
					logger.debug("Been asked to unbind from the SMSC");
					UnbindResp resp = (UnbindResp) ((Request) pdu).getResponse();

					resp.setCommandStatus(0);
					resp.setCommandLength(pdu.getCommandLength());
					resp.setSequenceNumber(pdu.getSequenceNumber());

					synchronized (this.session) {
						this.session.respond(resp);
						this.session.close();
					}
					// not bound
					return;
				}

				logger.debug("Unhandled " + pdu.debugString());
				Response resp = ((SubmitSM) pdu).getResponse();
				synchronized (this.session) {
					this.session.respond(resp);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void handleEnquireLinkResp(EnquireLinkResp resp) {

		if (resp.getCommandStatus() == 0) {
			logger.debug("SMPP is alive");
		} else {
			logger.debug("Error link resp in receiver: " + resp.getCommandStatus());
		}
	}

	private Map<String, Object> handleSubmitSMResp(SubmitSMResp resp) {
		
		Map<String, Object> response = new HashMap<String, Object>();
		int id = resp.getSequenceNumber();
		String messageId = resp.getMessageId();
		int commandStatus = resp.getCommandStatus();

		int sent = 0;
		if ((messageId != null) && (messageId.length() != 0)) {
			sent = 1;
		}
		logger.info("<<< submit response for id:" + id + " with SMSC MSGID: " + messageId + " status:" + commandStatus);

		// update message status
		response.put("messageid", id);
		response.put("sent", sent);
		response.put("status", commandStatus);
		response.put("smscid", messageId);
		return response;
	}
	
	
	public boolean send(OutgoingSMS outgoingsms) {
		logger.info(" >>> outgoing msgid: "+outgoingsms.getId());
		String txt = outgoingsms.getSms();
		if (txt.trim().length() <= 160) {
			logger.debug("sendConcat: before session");
			try {
				
				if(this.session==null)
					if(createTCPIPConnection())
						connect();
				
				synchronized (this.session) {

					SubmitSM msg = new SubmitSM();
					msg.assignSequenceNumber(true);
					msg.setRegisteredDelivery((Data.SM_SMSC_RECEIPT_REQUESTED));
					msg.setValidityPeriod("000000020000000R");
					msg.setSequenceNumber(outgoingsms.getId().intValue());

					msg.setSourceAddr(new Address((byte) getTon(),
							(byte) getNpi(), getShortcode()));

					msg.setDestAddr(new Address((byte) getDestinationton(),
							(byte) getDestinationnpi(), outgoingsms.getMsisdn()));

					ByteBuffer buffer = new ByteBuffer();
					buffer.appendString(txt, "ASCII");
					msg.setShortMessageData(buffer);

					msg.setDataCoding((byte) 0);
					if (!this.session.isBound() || !this.session.isOpened()) {
						reconnect();
					}
					if(this.session.isOpened()){
						this.session.submit(msg);
						return true;
					}else{
						logger.info("\n\n\n\tSession isn't open... We are not sending message..\n\n");
						return false;
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			sendConcat(outgoingsms);
		}
		return false;
	}
	
	
	private boolean sendConcat(OutgoingSMS outgoingsms) {

		String txt = outgoingsms.getSms();
		try {
			synchronized (this.session) {
				byte[][] split = concatenator.splitMessage8Bit(txt.getBytes());
				for (int i = 0; i < split.length; i++) {
					smscount++;

					SubmitSM msg = new SubmitSM();
					if (i + 1 == split.length) {
						msg.setSequenceNumber(outgoingsms.getId().intValue());
					} else {
						msg.setSequenceNumber(smscount);
					}

					msg.setSourceAddr(new Address((byte) getTon(),
							(byte) getNpi(), getShortcode()));
					msg.setDestAddr(new Address((byte) getDestinationton(),
							(byte) getDestinationnpi(), outgoingsms.getMsisdn()));
					msg.setRegisteredDelivery((Data.SM_SMSC_RECEIPT_REQUESTED));
					msg.setValidityPeriod("000000020000000R");
					ByteBuffer buffer = new ByteBuffer();
					buffer.appendBytes(split[i]);
					msg.setData(buffer);

					msg.setEsmClass((byte) 64);
					msg.setDataCoding((byte) 0);
					if (!this.session.isBound() || !this.session.isOpened()) {
						reconnect();
					}
					if(this.session.isOpened()){
						this.session.submit(msg);
						session.submit(msg);
						logger.info(msg.debugString());
						logger.info("After submit concat " + (i + 1) + "/"
								+ split.length + " ref : "
								+ msg.getSequenceNumber());
						return true;
					}else{
						logger.info("\n\n\n\tSession isn't open... We are not sending message..\n\n");
						return false;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		return false;
	}

	public Connection getSmppconn() {
		return smppconn;
	}

	public void setSmppconn(Connection smppconn) {
		this.smppconn = smppconn;
	}

	public BindRequest getRequest() {
		return request;
	}

	public void setRequest(BindRequest request) {
		this.request = request;
	}

	public BindResponse getResponse() {
		return response;
	}

	public void setResponse(BindResponse response) {
		this.response = response;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public LoggerDebug getDebug() {
		return debug;
	}

	public void setDebug(LoggerDebug debug) {
		this.debug = debug;
	}

	public String getServerip() {
		return serverip;
	}

	public void setServerip(String serverip) {
		this.serverip = serverip;
	}

	public int getServerport() {
		return serverport;
	}

	public void setServerport(int serverport) {
		this.serverport = serverport;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTon() {
		return ton;
	}

	public void setTon(int ton) {
		this.ton = ton;
	}

	public int getNpi() {
		return npi;
	}

	public void setNpi(int npi) {
		this.npi = npi;
	}

	public int getDestinationton() {
		return destinationton;
	}

	public void setDestinationton(int destinationton) {
		this.destinationton = destinationton;
	}

	public int getDestinationnpi() {
		return destinationnpi;
	}

	public void setDestinationnpi(int destinationnpi) {
		this.destinationnpi = destinationnpi;
	}

	public String getShortcode() {
		return shortcode;
	}

	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
