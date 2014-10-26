package com.pixelandtag.vouchersystem;

public class DrawApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String constr = "jdbc:mysql://db/voucher_system?user=root&password=";
		
		Producer prodt = new Producer(1,constr);
		Thread prod = new Thread(prodt);
		prod.start();

	}

}
