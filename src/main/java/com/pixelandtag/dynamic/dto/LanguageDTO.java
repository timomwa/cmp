package com.pixelandtag.dynamic.dto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class LanguageDTO {
	Logger log = Logger.getLogger(LanguageDTO.class);

	private int id;
	private String name;

	public LanguageDTO(ResultSet rs) throws Exception {
		this.loadFromRs(rs);
	}

	/**
	 * a good example of paranoia :)
	 * @param conn
	 * @param id
	 * @throws Exception
	 */
	public LanguageDTO(Connection conn, int id) throws Exception {
		if (id > 0) {
			Statement stmt = null;
			ResultSet rs = null;
			
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT t.`ID` AS `id`, t.`Language` AS `name` "
								+ "FROM `language` t " + "WHERE `ID`=" + id);
				if (rs.next())
					loadFromRs(rs);
				rs.close();
				stmt.close();
				try {
					stmt.close();
				} catch (Exception ee) {
				}
				try {
					rs.close();
				} catch (Exception ee) {
				}
			} catch (Exception e) {
				try {
					stmt.close();
				} catch (Exception ee) {
				}
				try {
					rs.close();
				} catch (Exception ee) {
				}
				throw e;
			} finally {
				try {
					stmt.close();
				} catch (Exception ee) {
				}
				try {
					rs.close();
				} catch (Exception ee) {
				}
			}
		}
	}

	public boolean selected(Object o) {
		boolean retval = false;
		try {
			retval = (Integer.parseInt((String) o)) == this.id;
		} catch (Exception e) {
		}
		return retval;
	}

	private void loadFromRs(ResultSet rs) throws Exception {
		this.id = rs.getInt("id");
		this.name = rs.getString("name");
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
