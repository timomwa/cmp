package com.pixelandtag.dynamic;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.pixelandtag.dynamic.dto.NewsDTO;
import com.pixelandtag.dynamic.dto.NoContentTypeException;

public class Update implements Runnable {
	public static ExecutorService service;
	static Logger log=Logger.getLogger(Update.class);
	
	private int smppid;
	private int telcoid;
	private String sms;
	private String msisdn;
	private String connstring;
	private boolean makeCount;
	private String payment;
	private String errBilling;
	private String preprocessor;
	private int typ;
	private String shortcode;
	private boolean hasMTBilling;
	public Update(int smppid,int telcoid ,String sms, String msisdn,String connstring,boolean makeCount,String payment,String errBilling,String preprocessor,int typ,String shortcode, boolean hasMTBilling) {
		this.smppid=smppid;
		this.telcoid=telcoid;
		this.sms=sms;
		this.msisdn=msisdn;
		this.connstring=connstring;
		this.makeCount=makeCount;
		this.payment=payment;
		this.errBilling=errBilling;
		this.preprocessor=preprocessor;
		this.typ=typ;
		this.shortcode=shortcode;
		this.hasMTBilling=hasMTBilling;	
	}
	public void run() {
		try {
			log.debug( "SMPPBearer baerer = new SMPPBearer(\n"+
					this.smppid+",\n"+
					this.telcoid+",\n"+
					this.sms+",\n"+
					this.msisdn+",\n"+
					this.connstring+",\n"+
					this.makeCount+",\n"+
					this.payment+",\n"+
					this.errBilling+",\n"+
					this.preprocessor+",\n"+
					this.typ+",\n"+
					this.shortcode+",\n"+
					this.hasMTBilling+"\n"+			
			");\n");
			/*SMPPBearer baerer = new SMPPBearer(
					this.smppid,
					this.telcoid,
					this.sms,
					this.msisdn,
					this.connstring,
					this.makeCount,
					this.payment,
					this.errBilling,
					this.preprocessor,
					this.typ,
					this.shortcode,
					this.hasMTBilling				
			);*/
			log.debug("done with constructor");
			/*baerer.setPriority(2);
			baerer.ProcessCmd();
			baerer.getContent();
			baerer.sendMessage();
			baerer.close();
			baerer=null;*/
		} catch ( Exception e ) {
			log.error(e,e);	
		}
	}
	
	public static void ProcessConentTypes(Connection conn, Document doc) throws NoContentTypeException,Exception {
		PreparedStatement pstmt=null;
		try {
			NodeList items = doc.getElementsByTagName("contenttype");
			log.debug("found "+items.getLength()+" content types");
			
			pstmt=conn.prepareStatement(
				"INSERT INTO `celcom`.`dynamiccontent_contenttype` SET " +
				"`ID`=?, `poolId`=?, `localeId`=?, `LanguageID`=?, `Content`=?, `Category`=?, `telcoid`=? " +
				"ON DUPLICATE KEY UPDATE " +
				"`poolId`=?, `localeId`=?, `LanguageID`=?, `Content`=?, `Category`=?, `telcoid`=?",Statement.RETURN_GENERATED_KEYS
			);
			// <contenttype id="5811" poolid="0" localeid="0" languageid="1" serviceid="0" content="Roaming" category="Roam Data Uganda"/>
			int i=0;
			for ( i=0; i<items.getLength(); i++ ) {
				int coll=1;
				NamedNodeMap attr = ((Element)items.item(i)).getAttributes();
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("id").getNodeValue()) );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("poolid").getNodeValue()) );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("localeid").getNodeValue()) );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("languageid").getNodeValue()) );
				pstmt.setString(coll++, attr.getNamedItem("content").getNodeValue() );
				pstmt.setString(coll++, attr.getNamedItem("category").getNodeValue() );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("telcoid").getNodeValue()) );
				
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("poolid").getNodeValue()) );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("localeid").getNodeValue()) );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("languageid").getNodeValue()) );
				pstmt.setString(coll++, attr.getNamedItem("content").getNodeValue() );
				pstmt.setString(coll++, attr.getNamedItem("category").getNodeValue() );
				pstmt.setInt(coll++, Integer.parseInt(attr.getNamedItem("telcoid").getNodeValue()) );
				
				pstmt.addBatch();
			}
			if ( i>0 ) {
				log.debug("Updating "+i+" contettypes");
				pstmt.executeBatch();
			}
		} finally {
			try { pstmt.close(); } catch ( Exception e ) {}
		}
	}
	public static int Process(Connection conn, Document doc,int maxpush, CallBackInterface obj) throws NoContentTypeException,Exception {
		service = Executors.newFixedThreadPool(10);
		NodeList items = doc.getElementsByTagName("item");
		log.debug("found "+items.getLength()+" dirty items");
		int i=0;
		for ( i=0; i<items.getLength(); i++ ) {
			try {
				NewsDTO news = new NewsDTO(conn);
				news.fromXML( (Element)items.item(i) );
				
				switch( news.getStatus() ) {
				case 1: 
					news.save();
					obj.ping(news.getId());
					log.debug("saved news id:"+news.getId());
					break;
				case 2: 
					news.delete();
					obj.ping(news.getId());
					log.debug("deleted news id:"+news.getId());
					break;
				case 8: 
					news.save();
					obj.ping(news.getId());
					log.debug("pushing news id:"+news.getId());
					if ( null != news.getServiceId() && !"0".equals(news.getServiceId()) && news.getServiceId().trim().length()>0 ) {
						obj.setPushing(true);
						// Spawn a thread that will push this message to all subscribed to getServiceId()
						// Hmmm how do we make sure that when we push this messages, it's the one being sent?
						// Can we somehow put the id in the content URL and have the servlet grab it? DONE :) 
						// keyword [NewsID:12345]
						
						// Also make sure that we don't push a news id more than once... perhaps check for the serviceid & subscription=1 in SMPPMsgLog? - using contentlog instead - DONE
						PreparedStatement pstmt=null;
						ResultSet rs=null;
						try {
							pstmt=conn.prepareStatement(
								"SELECT * FROM (( SELECT * FROM (( "+
								"SELECT DISTINCT s.id, s.msisdn, CONCAT(ss.CMD, \" [NewsID:\", ?, \"]\" ) AS CMD, ss.Telco, sp.ID AS SMPPID, sp.ServiceConnString, sp.PaymentClass, sp.ErrorBilling, sp.preprocessor, ss.ContentType, sp.Shortcode, sp.HasMTBilling "+
								"FROM `vas`.`subscription` s "+
								"LEFT JOIN `vas`.`SMSService` ss ON ( s.serviceid=ss.ServiceID ) "+
								"LEFT JOIN `vas`.`SMPP` sp ON ( sp.TelcoID=ss.Telco ) "+
								"LEFT JOIN `dynamiccontent`.`contentlog` l ON ( l.`TelcoId`=ss.Telco AND l.`ServiceId`=s.serviceid AND l.`MSISDN`=s.msisdn AND l.`contentID`=? ) "+
								"WHERE s.serviceid IN ("+news.getServiceId()+") AND NOW()<s.ExpireDate AND l.`id` IS NULL "+
								")) AS t GROUP BY t.msisdn )) AS tt",Statement.RETURN_GENERATED_KEYS
							);
							pstmt.setString(1,""+news.getId());
							pstmt.setInt(2,news.getId());
							rs=pstmt.executeQuery();
							while ( rs.next() ) {
								service.submit(
									new Update(
											rs.getInt("SMPPID"),
											rs.getInt("Telco"),
											rs.getString("CMD"),
											rs.getString("msisdn"),
											rs.getString("ServiceConnString"),
											true,
											rs.getString("PaymentClass"),
											rs.getString("ErrorBilling"),
											rs.getString("preprocessor"),
											rs.getInt("ContentType"),
											rs.getString("shortcode"),
											rs.getBoolean("HasMTBilling")
									)
								);
							}
							rs.close();
							log.debug("done looping msisdns");
						} catch ( Exception e ) {
							log.warn(e,e);
						}finally{
							try{
								rs.close();
							}catch(Exception e){}
							try{
								pstmt.close();
							}catch(Exception e){}
						}
					}
				}
			} catch ( NoContentTypeException e ) {
				throw e;
			} catch ( Exception e ) {
				log.warn(e,e);
			}
		}
		// Wait for the service to finish
		log.debug("shutting down service");
		service.shutdown();
		try {
			service.awaitTermination(1800, TimeUnit.SECONDS);
		} catch ( Exception e ) {
			log.error("Error while waiting for threads to finish",e);
		}
		obj.setPushing(false);
		log.debug("all done");
		return i;
	}
}
