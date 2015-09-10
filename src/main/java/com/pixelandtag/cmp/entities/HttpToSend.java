package com.pixelandtag.cmp.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.pixelandtag.api.BillingStatus;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.sms.mt.ACTION;
import com.pixelandtag.sms.mt.CONTENTTYPE;

@Entity
@Table(name = "httptosend")
public class HttpToSend implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2630133842622450790L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="in_outgoing_queue")
	private Boolean in_outgoing_queue;
	
	@Column(name="SMS",length=1000)
	private String sms;

	@Column(name="MSISDN")
	private String msisdn;
	
	@Column(name="Type")
	@Enumerated(EnumType.STRING)
	private CONTENTTYPE type;
	

	@Column(name="SendFrom")
	private String sendfrom;

	@Column(name="price")
	private BigDecimal price;

	@Column(name="price_point_keyword")
	private String price_point_keyword;

	@Column(name="Priority")
	private Integer priority;

	@Column(name="serviceid")
	private Long serviceid;

	@Column(name="re_tries")
	private Long re_tries;

	@Column(name="ttl")
	private Long ttl;
	
	
	@Index(name="TimeStamp")
	private Date timestamp;

	@Column(name="fromAddr")
	private String fromAddr;

	@Column(name="charged")
	private Boolean charged;

	@Column(name="sent")
	private Boolean sent;

	@Column(name="subscription")
	private Boolean subscription;

	@Column(name="CMP_AKeyword")
	private String CMP_AKeyword;

	@Column(name="CMP_SKeyword")
	private String CMP_SKeyword;

	@Column(name="sub_deviceType")
	private String sub_deviceType;

	@Column(name="sub_r_mobtel")
	private String sub_r_mobtel;

	@Column(name="sub_c_mobtel")
	private String sub_c_mobtel;
	
	@Index(name="CMP_TxID")
	private String CMP_TxID;

	@Column(name="newCMP_Txid")
	private String newCMP_Txid;

	@Column(name="apiType")
	private String apiType;
	
	@Column(name="ACTION")
	@Enumerated(EnumType.STRING)
	private ACTION action;
	

	@Column(name="split")
	private Boolean split;

	@Column(name="mo_processorFK")
	private Long mo_processorFK;

	@Column(name="SMS_DataCodingId")
	private Long SMS_DataCodingId;
	
	@Column(name="billing_status")
	@Enumerated(EnumType.STRING)
	private BillingStatus billing_status;
	
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "opco_id_fk")
	@Index(name="httsopcoidx")
	private OperatorCountry opco;
	
	
	@PrePersist
	@PreUpdate
	public void onCreate(){
		if(timestamp==null)
			timestamp = new Date();
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Boolean getIn_outgoing_queue() {
		return in_outgoing_queue;
	}


	public void setIn_outgoing_queue(Boolean in_outgoing_queue) {
		this.in_outgoing_queue = in_outgoing_queue;
	}


	public String getSms() {
		return sms;
	}


	public void setSms(String sms) {
		this.sms = sms;
	}


	public String getMsisdn() {
		return msisdn;
	}


	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}


	public CONTENTTYPE getType() {
		return type;
	}


	public void setType(CONTENTTYPE type) {
		this.type = type;
	}


	public String getSendfrom() {
		return sendfrom;
	}


	public void setSendfrom(String sendfrom) {
		this.sendfrom = sendfrom;
	}


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public String getPrice_point_keyword() {
		return price_point_keyword;
	}


	public void setPrice_point_keyword(String price_point_keyword) {
		this.price_point_keyword = price_point_keyword;
	}


	public Integer getPriority() {
		return priority;
	}


	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	public Long getServiceid() {
		return serviceid;
	}


	public void setServiceid(Long serviceid) {
		this.serviceid = serviceid;
	}


	public Long getRe_tries() {
		return re_tries;
	}


	public void setRe_tries(Long re_tries) {
		this.re_tries = re_tries;
	}


	public Long getTtl() {
		return ttl;
	}


	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}


	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public String getFromAddr() {
		return fromAddr;
	}


	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}


	public Boolean getCharged() {
		return charged;
	}


	public void setCharged(Boolean charged) {
		this.charged = charged;
	}


	public Boolean getSent() {
		return sent;
	}


	public void setSent(Boolean sent) {
		this.sent = sent;
	}


	public Boolean getSubscription() {
		return subscription;
	}


	public void setSubscription(Boolean subscription) {
		this.subscription = subscription;
	}


	public String getCMP_AKeyword() {
		return CMP_AKeyword;
	}


	public void setCMP_AKeyword(String cMP_AKeyword) {
		CMP_AKeyword = cMP_AKeyword;
	}


	public String getCMP_SKeyword() {
		return CMP_SKeyword;
	}


	public void setCMP_SKeyword(String cMP_SKeyword) {
		CMP_SKeyword = cMP_SKeyword;
	}


	public String getSub_deviceType() {
		return sub_deviceType;
	}


	public void setSub_deviceType(String sub_deviceType) {
		this.sub_deviceType = sub_deviceType;
	}


	public String getSub_r_mobtel() {
		return sub_r_mobtel;
	}


	public void setSub_r_mobtel(String sub_r_mobtel) {
		this.sub_r_mobtel = sub_r_mobtel;
	}


	public String getSub_c_mobtel() {
		return sub_c_mobtel;
	}


	public void setSub_c_mobtel(String sub_c_mobtel) {
		this.sub_c_mobtel = sub_c_mobtel;
	}


	public String getCMP_TxID() {
		return CMP_TxID;
	}


	public void setCMP_TxID(String cMP_TxID) {
		CMP_TxID = cMP_TxID;
	}


	public String getNewCMP_Txid() {
		return newCMP_Txid;
	}


	public void setNewCMP_Txid(String newCMP_Txid) {
		this.newCMP_Txid = newCMP_Txid;
	}


	public String getApiType() {
		return apiType;
	}


	public void setApiType(String apiType) {
		this.apiType = apiType;
	}


	public ACTION getAction() {
		return action;
	}


	public void setAction(ACTION action) {
		this.action = action;
	}


	public Boolean getSplit() {
		return split;
	}


	public void setSplit(Boolean split) {
		this.split = split;
	}


	public Long getMo_processorFK() {
		return mo_processorFK;
	}


	public void setMo_processorFK(Long mo_processorFK) {
		this.mo_processorFK = mo_processorFK;
	}


	public Long getSMS_DataCodingId() {
		return SMS_DataCodingId;
	}


	public void setSMS_DataCodingId(Long sMS_DataCodingId) {
		SMS_DataCodingId = sMS_DataCodingId;
	}


	public BillingStatus getBilling_status() {
		return billing_status;
	}


	public void setBilling_status(BillingStatus billing_status) {
		this.billing_status = billing_status;
	}


	@Override
	public String toString() {
		return "HttpToSend [id=" + id + ", in_outgoing_queue="
				+ in_outgoing_queue + ", sms=" + sms + ", msisdn=" + msisdn
				+ ", type=" + type + ", sendfrom=" + sendfrom + ", price="
				+ price + ", price_point_keyword=" + price_point_keyword
				+ ", priority=" + priority + ", serviceid=" + serviceid
				+ ", re_tries=" + re_tries + ", ttl=" + ttl + ", timestamp="
				+ timestamp + ", fromAddr=" + fromAddr + ", charged=" + charged
				+ ", sent=" + sent + ", subscription=" + subscription
				+ ", CMP_AKeyword=" + CMP_AKeyword + ", CMP_SKeyword="
				+ CMP_SKeyword + ", sub_deviceType=" + sub_deviceType
				+ ", sub_r_mobtel=" + sub_r_mobtel + ", sub_c_mobtel="
				+ sub_c_mobtel + ", CMP_TxID=" + CMP_TxID + ", newCMP_Txid="
				+ newCMP_Txid + ", apiType=" + apiType + ", action=" + action
				+ ", split=" + split + ", mo_processorFK=" + mo_processorFK
				+ ", SMS_DataCodingId=" + SMS_DataCodingId
				+ ", billing_status=" + billing_status + ", opco="+opco+"]";
	}


	public OperatorCountry getOpco() {
		return opco;
	}


	public void setOpco(OperatorCountry opco) {
		this.opco = opco;
	}
	
	
	
	
	
}
