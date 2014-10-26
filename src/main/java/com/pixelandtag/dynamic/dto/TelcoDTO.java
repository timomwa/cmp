package com.pixelandtag.dynamic.dto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class TelcoDTO {
	Logger log=Logger.getLogger(TelcoDTO.class);
	
	private int id;
	private int languageid=0;
	private String name;

	public TelcoDTO(ResultSet rs ) throws Exception {
		this.loadFromRs(rs);
	}
	public TelcoDTO(Connection conn, int id ) throws Exception {
		if ( id>0 ) {
			Statement stmt=null;
			ResultSet rs=null;
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(
					"SELECT t.`id`, t.`telco` AS `name`, MIN( ct.`LanguageID` ) AS `LanguageID` "+
					"FROM `dynamiccontent`.`telco` t "+
					"LEFT JOIN `dynamiccontent`.`contenttype` ct ON ( t.`id` = ct.`telcoid` ) "+
					"WHERE t.`id`="+id+" "+
					"GROUP BY t.`id`"
				);
				if ( rs.next() )
					loadFromRs(rs);
				
				try { rs.close(); } catch ( Exception ee ){}
				try { stmt.close(); } catch ( Exception ee ){}
			} catch ( Exception e ) {
				try { rs.close(); } catch ( Exception ee ){}
				try { stmt.close(); } catch ( Exception ee ){}
				throw e;
			} finally {
				try { rs.close(); } catch ( Exception ee ){}				
				try { stmt.close(); } catch ( Exception ee ){}
			}
		}
	}
	public boolean selected(Object o) {
		boolean retval=false;
		try {
			retval = (Integer.parseInt((String)o)) == this.id;
		} catch ( Exception e ) {}
		return retval;
	}
	private void loadFromRs(ResultSet rs) throws Exception {
		this.id=rs.getInt("id");
		this.name=rs.getString("name");
		this.languageid=rs.getInt("LanguageID");
	}
	public int getId() {
		return id;
	}
	public int getLanguageId() {
		return languageid;
	}
	public String getName() {
		return name;
	}

}
