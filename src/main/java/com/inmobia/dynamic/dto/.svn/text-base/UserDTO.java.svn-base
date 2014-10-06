package com.inmobia.dynamic.dto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class UserDTO {
	Logger log=Logger.getLogger(UserDTO.class);
	private Connection conn;
	
	private int id;
	private String name;
	private boolean master;

	public UserDTO(ResultSet rs ) throws Exception {
		this.loadFromRS(rs);
	}
	public UserDTO(Connection conn, int id ) throws SQLException {
		this.conn=conn;
		ResultSet rs=null;
		Statement stmt=null;
		try {
			stmt=conn.createStatement();
			rs=stmt.executeQuery(
				"SELECT * FROM `icp`.`users` WHERE `id`="+id
			);
			if ( rs.next() ) {
				this.loadFromRS(rs);
			}
			
		} catch ( Exception e ) {
			log.warn(e.getMessage(),e);
		} finally { 
			try { rs.close(); } catch ( Exception e ) {}
			try { stmt.close(); } catch ( Exception e ) {}
		}
	}
	public UserDTO loadFromRS( ResultSet rs ) throws SQLException {
		this.id = rs.getInt("id");
		this.name = rs.getString("name");
		this.master = rs.getInt("dynamic_master") > 0;
		return this;
	}
	public boolean isModerator() {
		return this.master;
	}
	public String getName() {
		return name;
	}
	public ArrayList<TelcoDTO> getTelcos() {
		ArrayList<TelcoDTO> retval=new ArrayList<TelcoDTO>();
		ResultSet rs=null;
		Statement stmt = null;
		try {
			
			stmt = conn.createStatement();
			
			if ( master ) {
				rs = stmt.executeQuery(
					"SELECT t.`id`, t.`telco` AS `name`, l.`ID` AS `LanguageID` "+
					"FROM `dynamiccontent`.`contenttype` ct "+
					"LEFT JOIN `dynamiccontent`.`telco` t ON ( ct.`telcoid`=t.`id` ) "+
					"LEFT JOIN `dynamiccontent`.`language` l ON ( ct.`LanguageID`=l.`ID` ) "+
					"GROUP BY t.`id` "+
					"ORDER BY t.`telco`"
				);				
			} else {
				rs = stmt.executeQuery(
					"SELECT t.`id`, t.`telco` AS `name`, l.`ID` AS `LanguageID` "+
					"FROM `dynamiccontent`.`allowedcontent` ac "+
					"LEFT JOIN `dynamiccontent`.`contenttype` ct ON ( ac.`contentid`=ct.`ID` ) "+
					"LEFT JOIN `dynamiccontent`.`telco` t ON ( ct.`telcoid`=t.`id` ) "+
					"LEFT JOIN `dynamiccontent`.`language` l ON ( ct.`LanguageID`=l.`ID` ) "+
					"WHERE ac.`userid`="+this.id+" "+
					"GROUP BY t.`id` "+
					"ORDER BY t.`telco`"
				);
			}
			while ( rs.next() ) {
				retval.add(new TelcoDTO(rs));
			}
			
			
		} catch ( Exception e ) {
			log.error(e,e);
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		return retval;
	}
	public ArrayList<LanguageDTO> getLanguages(String telcoid) {
		ArrayList<LanguageDTO> retval=null;
		try {
			retval = getLanguages(Integer.parseInt(telcoid));
		} catch ( Exception e ){}
		return retval;
	}
	public ArrayList<LanguageDTO> getLanguages(int telcoid) {
		ArrayList<LanguageDTO> retval=new ArrayList<LanguageDTO>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			if ( master ) {
				rs = stmt.executeQuery(
						"SELECT l.`ID` AS `id`, l.`Language` AS `name` "+
						"FROM `dynamiccontent`.`contenttype` ct "+
						"LEFT JOIN `dynamiccontent`.`telco` t ON ( ct.`telcoid`=t.`id` ) "+
						"LEFT JOIN `dynamiccontent`.`language` l ON ( ct.`LanguageID`=l.`ID` ) "+
						"WHERE t.`id`="+telcoid+" "+
						"GROUP BY l.`ID` "+
						"ORDER BY l.`Language`"
					);				
			} else {
				rs = stmt.executeQuery(
					"SELECT l.`ID` AS `id`, l.`Language` AS `name` "+
					"FROM `dynamiccontent`.`allowedcontent` ac "+
					"LEFT JOIN `dynamiccontent`.`contenttype` ct ON ( ac.`contentid`=ct.`ID` ) "+
					"LEFT JOIN `dynamiccontent`.`telco` t ON ( ct.`telcoid`=t.`id` ) "+
					"LEFT JOIN `dynamiccontent`.`language` l ON ( ct.`LanguageID`=l.`ID` ) "+
					"WHERE ac.`userid`="+this.id+" AND t.`id`="+telcoid+" "+
					"GROUP BY l.`ID` "+
					"ORDER BY l.`Language`"
				);
			}
			while ( rs.next() ) {
				retval.add(new LanguageDTO(rs));
			}
			
			
		} catch ( Exception e ) {
			log.error(e,e);
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		return retval;
	}
	public ArrayList<ContenttypeDTO> getContentTypes(String telcoid, String languageid) {
		ArrayList<ContenttypeDTO> retval=null;
		try {
			retval = getContentTypes(
				Integer.parseInt(telcoid),
				Integer.parseInt(languageid)
			);
		} catch ( Exception e ){}
		return retval;
	}
	public ArrayList<ContenttypeDTO> getContentTypes(int telcoid, int languageid) {
		ResultSet rs = null;
		Statement stmt = null;
		ArrayList<ContenttypeDTO> retval = new ArrayList<ContenttypeDTO>();
		try {
			stmt = conn.createStatement();
			if ( master ) {
				rs = stmt.executeQuery(
					"SELECT ct.`ID` AS `id` "+
					"FROM `dynamiccontent`.`contenttype` ct "+
					"WHERE ct.`telcoid`="+telcoid+" AND ct.`LanguageID`="+languageid+" "+
					"GROUP BY ct.`ID` "+
					"ORDER BY ct.`Content`, ct.`Category`"
				);
			} else {
				rs = stmt.executeQuery(
					"SELECT ct.`ID` AS `id` "+
					"FROM `dynamiccontent`.`allowedcontent` ac "+
					"LEFT JOIN `dynamiccontent`.`contenttype` ct ON ( ac.`contentid`=ct.`ID` ) "+
					"WHERE ac.`userid`="+this.id+" AND ct.`telcoid`="+telcoid+" AND ct.`LanguageID`="+languageid+" "+
					"GROUP BY ct.`ID` "+
					"ORDER BY ct.`Content`, ct.`Category`"
				);
			}
			while ( rs.next() ) {
				retval.add(new ContenttypeDTO(conn,rs.getInt("id")));
			}
		} catch ( Exception e ) {
			log.error(e,e);
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		return retval;
	}
}
