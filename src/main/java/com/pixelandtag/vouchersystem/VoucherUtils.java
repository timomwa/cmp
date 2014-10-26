package com.pixelandtag.vouchersystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class VoucherUtils {
	
	
	
	
	public static void setSetting(String name, String value, Connection con)
			throws Exception {
		
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("INSERT INTO `voucher_system`.`settings`(`key`,`value`) VALUES (?,?) ON DUPLICATE KEY UPDATE value = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, value);
			pstmt.setString(3, value);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {

			throw new Exception(e.getMessage());

		}finally{
			try{
				pstmt.close();
			}catch(Exception e){}
		}
	}
	
	
	public static String getSetting(String name, Connection con)
			throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			String setting = null;
			pstmt = con.prepareStatement("SELECT * FROM `voucher_system`.`settings` WHERE `key` = ?");
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				setting = rs.getString("value");
			}
			
			return setting;
			
		} catch (Exception e) {
			
			throw new Exception(e.getMessage());

		} finally{
			
			try{
				rs.close();
			}catch(Exception e){}
			try{
				pstmt.close();
			}catch(Exception e){}
		
		}

	}


	public static int getWinnersToday(Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			int winners_today = 0;
			pstmt = conn.prepareStatement("SELECT count(*) as 'c' FROM `voucher_system`.`drawn_vouchers` where timeStamp between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND)",Statement.RETURN_GENERATED_KEYS);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				winners_today = rs.getInt("c");
			}
			
			return winners_today;
			
		} catch (Exception e) {
			
			throw new Exception(e.getMessage());

		} finally{
			
			try{
				rs.close();
			}catch(Exception e){}
			try{
				pstmt.close();
			}catch(Exception e){}
		
		}
	}
	
	
	
	
	public static boolean isWinner(int unprocessed_participant_batch_id_fk,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			boolean isWinner = false;
			pstmt = conn.prepareStatement("SELECT * FROM `voucher_system`.`drawn_vouchers` where timeStamp between timestamp(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY)) AND ((CURRENT_DATE + INTERVAL 1 DAY) - INTERVAL 1 SECOND) AND unprocessed_participant_batch_id_fk=?",Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, unprocessed_participant_batch_id_fk);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				isWinner = true;
			}
			
			return isWinner;
			
		} catch (Exception e) {
			
			throw new Exception(e.getMessage());

		} finally{
			
			try{
				rs.close();
			}catch(Exception e){}
			try{
				pstmt.close();
			}catch(Exception e){}
		
		}
	}


	
	public static void insertToDrawTable(int winning_id, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("INSERT INTO `voucher_system`.`drawn_vouchers`(`unprocessed_participant_batch_id_fk`) VALUES (?)",Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, winning_id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {

			throw new Exception(e.getMessage());

		}finally{
			try{
				pstmt.close();
			}catch(Exception e){}
		}
		
	}


}
