package com.inmobia.vouchersystem;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.inmobia.axiata.web.beans.MessageType;
import com.inmobia.axiata.web.triviaImpl.MechanicsS;
import com.inmobia.celcom.api.GenericServiceProcessor;
import com.inmobia.celcom.util.UtilCelcom;
import com.inmobia.luckydip.api.LuckyDipI;
import com.mysql.jdbc.Statement;

public class Consumer extends Thread {
	
	private Logger logger = Logger.getLogger(Consumer.class);
	private Connection conn = null;
	private String connstring = Producer.constr;
	private LuckyDipI processor;
	private String name = null;
	private boolean run = true;
	private ArrayBlockingQueue<Entry> queue;
	private int active_promo_id = -1;
	private int prize_id_fk = -1;
	private Prize prize = null;
	private final String VOUCHER_TAG = "<VOUCHER_NUMBER>";
	
	
	public Consumer(String name, String connstring, ArrayBlockingQueue<Entry> queue){
		super.setName(name);
		this.connstring = connstring;
		this.queue = queue;
	}
	
	public void run(){
		
		
		int x = 0;
		
		
		while(run){
			
			
			try{
				
				final Entry dto = queue.take();
				
				logger.debug("GUGAMUGA  " +dto);
				
				processDTO(dto);
				
				changeStatus(dto.getId(),Status.processed);
				
				if( ( x % 10 )==0 )
					logger.debug(String.format("Processed %s mt's so far. Queue size? %s", x, queue.size()));
				
					
				x++;
				
				//Thread.sleep(10);
				
			}catch(InterruptedException e){
				
				logger.debug(getName()+" INTERRUPTED !!!"+e.getMessage());
				this.run = false;
			
			}finally{
				
			}
			
			
		}
		
	}

	
	
	
	
	/**
	 * Gets a prize object belonging to a given promotion id
	 * @param promotion_id_fk
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public static Prize getPrize(int promotion_id_fk, Connection con)
			throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Prize prize = null;
		
		try {
			
			pstmt = con.prepareStatement("SELECT * FROM `voucher_system`.`prize` WHERE promotion_id_fk = ?");
			pstmt.setInt(1, promotion_id_fk);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				
				prize = new Prize();
				prize.setId(rs.getInt("id"));
				prize.setPromotion_id_fk(promotion_id_fk);
				prize.setName(rs.getString("name"));
				prize.setDescription(rs.getString("description"));
				
			}
			
			return prize;
			
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
	/**
	 * 
	 * @param dto
	 */
	private void processDTO(Entry dto) {
		
		if(active_promo_id==-1){
			try{
				active_promo_id = Integer.valueOf(VoucherUtils.getSetting("active_promo_id", getConnection()));
				prize = getPrize(active_promo_id, getConnection());
				prize_id_fk = prize.getId();
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		
		int language_id = UtilCelcom.getSubscriberLanguage(dto.getMsisdn(), conn);
		
		PreparedStatement pstmt = null;
		
		try{
			
			boolean isWinner = VoucherUtils.isWinner(dto.getId(), conn);
			
			String ticket_draw_msg = "";
			
			if(isWinner)
				ticket_draw_msg = UtilCelcom.getMessage(MessageType.WINNER_MESSAGE, getConnection(), language_id);
			else
				ticket_draw_msg = UtilCelcom.getMessage(MessageType.NON_WINNER_MESSAGE, getConnection(), language_id);
			
			ticket_draw_msg = ticket_draw_msg.replaceAll(VOUCHER_TAG , dto.getCmp_txid_fk());
			
			ticket_draw_msg = GenericServiceProcessor.RM.replaceAll(GenericServiceProcessor.PRICE_TG, "0")+ticket_draw_msg;
			
			MechanicsS.insertIntoHttpToSend(dto.getMsisdn(), ticket_draw_msg, MechanicsS.generateNextTxId(), -1, 0d, UtilCelcom.getConfigValue("default_free_shortcode",  getConnection()), UtilCelcom.getConfigValue("free_tarrif_code_cmp_AKeyword",  getConnection()), UtilCelcom.getConfigValue("free_tarrif_code_cmp_SKeyword",  getConnection()), false,getConnection());
			
			
			pstmt = getConnection().prepareStatement("INSERT INTO `voucher_system`.`voucher`(voucherNumber,msisdn,prize_id_fk,promotion_id_fk,winning,timeStamp_awarded) VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, dto.getCmp_txid_fk());
			pstmt.setString(2, dto.getMsisdn());
			pstmt.setInt(3, prize_id_fk);
			pstmt.setInt(4, active_promo_id);
			pstmt.setBoolean(5, isWinner);
			pstmt.setString(6, dto.getTimeStampAwarded());
			pstmt.executeUpdate();
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try{
				pstmt.close();
			}catch(Exception e){}
		
		}
		
		
		
		
	}
	
	
	
	

	private void changeStatus(int id, Status processed) {
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement("UPDATE `voucher_system`.`unprocessed_participant_batch` SET `status`=? WHERE id=?",Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, processed.toString());
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}finally{
			
			try{
				pstmt.close();
			}catch(Exception e){}
			try{
				conn.close();
			}catch(Exception e){}
		}
		
	}

	private Connection getConnection() {
		
		
		int i = 0;
		int maxTries = 100;
		
		while( true ) {
			try {
				while ( conn==null || conn.isClosed()  ) {
					try {
						conn = DriverManager.getConnection(this.connstring,"root","");
						//i=maxTries;
					} catch ( Exception e ) {
						e.printStackTrace();
						i++;
						try { Thread.sleep(500); } catch ( Exception ee ) {}
						if(i==maxTries)
							throw new Exception(String.format("Could not close the exception after %s tries",i));
					}
				}

				return conn;
			} catch ( Exception e ) {
				e.printStackTrace();
				try { Thread.sleep(1000); } catch ( Exception ee ) {}
			}
		}
	}

	
	
	
	/*public static String toHex(String arg) throws UnsupportedEncodingException {
		  return String.format("%x", new BigInteger(1, arg.getBytes("UTF-8")));
	}*/
	
	
	/*public static String toHex(String arg) throws UnsupportedEncodingException {
	    //return String.format("%x", new BigInteger(arg.getBytes("UTF8")));
		return Hex.encodeHexString(arg.getBytes("ISO-8859-2"));
	}
	
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(toHex("IS it guaranteed?"));
	}*/
}
