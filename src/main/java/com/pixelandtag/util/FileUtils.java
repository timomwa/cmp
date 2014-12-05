package com.pixelandtag.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class FileUtils {
	
	
	/**
	 * Creates a java.util.Properties object from 
	 * the file specified by the param filename
	 * @param filename the name of the properties file
	 * @return java.util.Properties ojbect created and populated
	 *          with the property-values set on the file "filename"
	 */
	public static Properties getPropertyFile(String filename) {

		Properties prop = null;
		InputStream inputStream = null;
		
		String path;
		try {
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + filename;
			inputStream = new FileInputStream(path);
		} catch (Exception e) {
			URL urlpath = new String().getClass().getResource(filename);
			try {
				inputStream = new FileInputStream(urlpath.getPath());
			} catch (Exception exb) {
				System.out.println(filename + " not found!");
			}
		}
		try {
			if (inputStream != null) {
				prop = new Properties();
				prop.load(inputStream);
				inputStream.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}

}
