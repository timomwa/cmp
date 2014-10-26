package com.pixelandtag.mms.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import snaq.db.DBPoolDataSource;

import com.pixelandtag.connections.DriverUtilities;
import com.pixelandtag.mms.apiImpl.MMSApiImpl;
import com.pixelandtag.sms.application.HTTPMTSenderApp;
import com.pixelandtag.web.triviaI.MechanicsI;
import com.pixelandtag.web.triviaImpl.MechanicsS;

public class AxiataMMSReader {

	static Set<String> filesInMMSFile;
	// private static String mmsFile = "C:\\mfg\\mms.txt";
	private static Set<String> filesInFolder;
	private static String folder = "/root/sms/mms/imgs"; //"C:\\Users\\Paul\\Desktop\\MMS Pis\\MMS FROM AMANDA\\life-categorie\\wallpapers";
	private DBPoolDataSource ds;
	private Connection conn;
	private Logger logger = Logger.getLogger(AxiataMMSReader.class);

	public AxiataMMSReader() throws Exception {

		int vendor = DriverUtilities.MYSQL;
		String driver = DriverUtilities.getDriver(vendor);
		String host = "db";
		String dbName =  HTTPMTSenderApp.props.getProperty("DATABASE");
		String url = DriverUtilities.makeURL(host, dbName, vendor);
		String username = "root";
		String password = "";

		ds = new DBPoolDataSource();
		ds.setValidatorClassName("snaq.db.Select1Validator");
		ds.setName("mms-file-iterator-ds");
		ds.setDescription("MMS folder iterator Pooling DataSource");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl(url);
		ds.setUser("root");
		ds.setPassword("");
		ds.setMinPool(1);
		ds.setMaxPool(1);
		ds.setMaxSize(2);
		ds.setIdleTimeout(3600); // Specified in seconds.

		BasicConfigurator.configure();

	}

	public void myfinalize() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ds.releaseConnectionPool();
	}

	public static void main(String[] args) throws Exception {

		AxiataMMSReader fileReader = new AxiataMMSReader();

		if (args.length > 0) {

			filesInFolder = fileReader.readFolder(args[0]);

		} else {

			filesInFolder = fileReader.readFolder(folder);

		}

		fileReader.myfinalize();

	}

	private Set<String> readFolder(String folder2) {
		Set<String> filesInFolder = new HashSet<String>();
		File folder = new File(folder2);

		try {
			conn = ds.getConnection();

			MMS mms;

			for (File file : folder.listFiles()) {

				if (file.isDirectory()) {

					mms = new MMS();

					
					int c = 0;
					for (File f : file.listFiles()) {

						if (f.isFile()) {
							
							if (f.getAbsolutePath().endsWith(".sms") || f.getAbsolutePath().endsWith(".txt")) {

								mms = readMMPropertySFile(mms,
										f.getAbsolutePath());

							} else {

								mms.setMediaPath(f.getAbsolutePath());

							}
						
							
						}
						
						c++;
						
						if(c==2)
							MechanicsS.addToDbWhereNotExist(mms, conn);
						
						
					}

				}

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return filesInFolder;
	}

	private MMS readMMPropertySFile(MMS mms, String pathname) {

		Set<String> filesInMMSFile = new HashSet<String>();
		File mmsFile = new File(pathname);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new java.io.FileReader(mmsFile));
			String line;

			while ((line = reader.readLine()) != null) {
				filesInMMSFile.add(line);

				try{
					
					try{	
					
						if (line.toUpperCase().startsWith("SUBJECT=")) {
							mms.setSubject(line.split("SUBJECT=")[1].trim());
						}
					
					}catch(Exception e){
						//logger.warn(">>> WHAT THE FUCK!!!!!! "+pathname+" what>>> "+line + " ERROR>>>>>>>>>>>>>>>>>>>>"+e.getMessage());
					}
				
				
					try{
						
						if (line.toUpperCase().startsWith("TEXT=")) {
							mms.setMms_text(line.split("TEXT=")[1].trim());
						}
						
					}catch(Exception e){
						
							try{
								if (line.toUpperCase().startsWith("QUOTE=")) {
									mms.setMms_text(line.split("QUOTE=")[1].trim());
								}
							
							}catch(Exception e2){
								//logger.warn("????????????????WHAT THE FUCK!!!!!! "+pathname+" what>>> "+line + " ERROR>>>>>>>>>>>>>>>>>>>>"+e.getMessage());
							}
					
					}
				
				
				
				try{
					
					if(pathname.endsWith(".txt"));
					logger.info("["+line.toUpperCase()+"]");
					if (line.toUpperCase().startsWith("SUBJECT:")) {
						mms.setSubject(line.split("Subject:")[1].trim());
					}
					
				}catch(Exception e){
					
					logger.warn(">>> WHAT THE FUCK!!!!!! "+pathname+" what ? >>> "+line + " ERROR>>>>>>>>>>>>>>>>>>>>"+e.getMessage());
				
				}
				
				
				
				try{
					
					if (line.toUpperCase().startsWith("TEXT:")) {
						mms.setMms_text(line.split("TEXT:")[1].trim());
					}
				
				}catch(Exception e){
					
					try{
						
						if (line.toUpperCase().startsWith("QUOTE:")) {
							mms.setMms_text(line.split("Quote:")[1].trim());
						}
						
					}catch(Exception e2){
						//logger.warn("????????????????WHAT THE FUCK!!!!!! "+pathname+" what>>> "+line + " ERROR>>>>>>>>>>>>>>>>>>>>"+e.getMessage());
					}
					
					
				
				}
				
				
				}catch(Exception e){
					logger.warn("??? >>>>>>>>>>>>>>>>>> PARSE_ERROR!!!!!! : "+e.getMessage());
				}
			
			
			}
			
			
			if(mms.getSubject()==null || mms.getSubject().isEmpty())
				throw new Exception("Could not read the subject value form the file: "+pathname);
			if(mms.getMms_text()==null || mms.getMms_text().isEmpty())
				throw new Exception("Could not read the text value form the file: "+pathname);

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}catch (Exception e) {
			
			logger.warn("??? >>>>>>>>>>>>>>>>>> PARSE_ERROR!!!!!! : "+e.getMessage());
			
		} finally {

			try {

				if (reader != null)
					reader.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mms;
	}

	/**
	 * Gets the connection. If it is not closed or null, return the existing
	 * connection object, else create one and return it
	 * 
	 * @return java.sql.Connection object
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private Connection getConnection() throws InterruptedException,
			SQLException {

		try {

			return ds.getConnection();

		} finally {

		}

	}
}
