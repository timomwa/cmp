package com.pixelandtag.dynamic.dto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ContenttypeDTO {
	Logger log=Logger.getLogger(ContenttypeDTO.class);
	private Connection conn;
	private int id;
	private String content;
	private String category;
	private int telcoId;
	private int poolId;
	private int localeId;
	private int LanguageID;
	public boolean usexhtml=false;
	private String serviceid="0";
	private boolean instantdelete=false;
	private boolean waponly=false;
	private int maxlength=0;
	
	private int maxDowntime=0;
	private int downtime=0;
	private Date lastUpdated=Calendar.getInstance().getTime();
	
	private int goal=0;
	private int items=0;
	
	public ContenttypeDTO() {}
	public ContenttypeDTO(Connection conn,ResultSet rs ) throws Exception {
		this.conn=conn;
		this.loadFromRs(rs);
	}
	public ContenttypeDTO(Connection conn, int id ) throws NoContentTypeException,Exception {
		
		
		this.conn=conn;
		
		ResultSet rs = null;
		Statement stmt = null;
		
		try{
		if ( id>0 ) {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT c.* "+
				", MAX(cc.`timestamp`) AS `lastUpdated`, UNIX_TIMESTAMP()-UNIX_TIMESTAMP(MAX(cc.`timestamp`)) AS `downtime` "+
				", SUM(IF(cc.`timestamp`>=CURDATE(),1,0)) AS `items` "+
				"FROM `celcom`.`dynamiccontent_contenttype` c " +
				"LEFT JOIN `celcom`.`dynamiccontent_content` cc ON ( c.`ID`=cc.`contentid` )"+
				"WHERE c.`ID`="+id+" "+
				"GROUP BY c.`ID`"
			);
			if ( rs.next() ) {
				loadFromRs(rs);
				rs.close();
			} else {
				rs.close();
				throw new NoContentTypeException("No such ContentType.ID - "+id);
			}
		}
		}catch(Exception e){
			throw e;
		}finally{
			try{
				rs.close();
			}catch(Exception e){}
			try{
				stmt.close();
			}catch(Exception e){}
		}
	}
	private void loadFromRs(ResultSet rs) throws Exception {
		this.id=rs.getInt("ID");
		this.content=rs.getString("Content");
		this.category=rs.getString("Category");
		this.telcoId=rs.getInt("telcoid");
		this.poolId=rs.getInt("poolId");
		this.localeId=rs.getInt("localeId");
		this.LanguageID=rs.getInt("LanguageID");
		try { this.usexhtml=rs.getBoolean("usexhtml"); } catch ( Exception e ) {}
		try { this.serviceid=rs.getString("serviceid").trim(); } catch ( Exception e ) {}
		try { this.instantdelete=rs.getBoolean("instantdelete"); } catch ( Exception e ) {}
		try { this.waponly=rs.getBoolean("waponly"); } catch ( Exception e ) {}
		try { this.maxlength=rs.getInt("maxlength"); } catch ( Exception e ) {}
		
			if ( this.instantdelete ) {
				
				Statement stmt_ = null;
				
				try{
					
					stmt_ = conn.createStatement();
					
					rs=stmt_.executeQuery("SELECT COUNT(*) AS c FROM `celcom`.`dynamiccontent_dirtycontent` WHERE `telcoid`="+this.telcoId+" AND `contentid`="+this.id);
					
					if ( rs.next() && rs.getInt("c")>0 ) {
						this.instantdelete=false;					
					}
					
				}catch ( Exception e ){
					log.error(e,e); 
				}finally{
					
					try{
						rs.close();
					}catch(Exception e){
						
					}
					
					try{
						stmt_.close();
					}catch(Exception e){
						
					}
				}
				
			}
			
			try { this.maxDowntime=rs.getInt("maxdowntime"); } catch ( Exception e ) {}
			try { this.downtime=rs.getInt("downtime"); } catch ( Exception e ) {}
			try { this.lastUpdated=rs.getTimestamp("lastUpdated"); } catch ( Exception e ) {}
			try { this.items=rs.getInt("items"); } catch ( Exception e ) {}
			try { this.goal=rs.getInt("goal"); } catch ( Exception e ) {}
	}
	
	public ArrayList<HashMap<String,Object>> getArchives() {
		ArrayList<HashMap<String,Object>> retval = new ArrayList<HashMap<String,Object>>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT c.`ID`, CAST(c.`timestamp` AS CHAR) AS `timestamp`, c.`dirty` "+
				", IF( sti.`title` IS NOT NULL AND sti.`title`<>\"\", sti.`title`, IF(c.`headline` IS NOT NULL AND c.`headline`<>\"\", c.`headline`, c.`Text`) ) AS `news` "+
				"FROM `celcom`.`dynamiccontent_content` c "+
				"LEFT JOIN `icp`.`simple_text_item` sti ON ( c.`contentItemId`=sti.`id` ) "+
				"WHERE c.`contentid`="+this.id+" AND c.`dirty` <> 2 "+
				"ORDER BY c.`ID` DESC"
			);
			while( rs.next() ) {
				HashMap<String,Object> row = new HashMap<String,Object>();
				row.put("id",rs.getInt("ID"));
				row.put("timestamp",rs.getString("timestamp"));
				row.put("dirty",rs.getInt("dirty"));
				row.put("status",rs.getInt("dirty"));
				row.put("news",( rs.getString("news").length()>50 )?rs.getString("news").substring(0,50)+"..":rs.getString("news"));
				retval.add(row);
			}
				
		
		} catch ( Exception e ) {
			log.warn(e,e);
		}finally{
			try {
				if(rs!=null)
				rs.close();
			} catch (Exception e) {}
			try {
				if(stmt!=null)
				stmt.close();
			} catch (Exception e) {}
		}
		return retval;
	}
	public boolean selected(Object o) {
		boolean retval=false;
		try {
			retval = (Integer.parseInt((String)o)) == this.id;
		} catch ( Exception e ) {}
		return retval;
	}
	public String getCategory() {
		return category;
	}
	public String getContent() {
		return content;
	}
	public int getId() {
		return id;
	}
	public int getLocaleId() {
		return localeId;
	}
	public int getPoolId() {
		return poolId;
	}
	public int getTelcoId() {
		return telcoId;
	}
	public int getLanguageId() {
		return LanguageID;
	}
	public TelcoDTO getTelco() throws Exception {
		return new TelcoDTO(conn,this.telcoId);
	}
	public LanguageDTO getLanguage() throws Exception {
		return new LanguageDTO(conn,this.LanguageID);
	}
	public String getServiceId() {
		if ( null == serviceid )
			return "0";
		return serviceid;
	}
	public boolean isUsexhtml() {
		return usexhtml;
	}
	public boolean isWaponly() {
		return waponly;
	}
	public boolean isInstantdelete() {
		return instantdelete;
	}
	public int getMaxLength() {
		return maxlength;
	}
	public int getDowntime() {
		return downtime;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public int getMaxDowntime() {
		return maxDowntime;
	}
	public int getGoal() {
		return goal;
	}
	public int getItems() {
		return items;
	}
	
}
