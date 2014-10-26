package com.pixelandtag.mms.soap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.pixelandtag.mms.api.StreamUtils;

public class AnotherWAY {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		
		String charset = "utf-8";
		String smilStr  = "<smil><head><layout><root-layout background-color=\"#cbcbcb\"/><region id=\"Image\" width=\"100%\" height=\"100%\" left=\"0\" top=\"0\"/><region id=\"Text\" width=\"100%\" height=\"100%\" left=\"0\" top=\"129\"/></layout></head><body><par dur=\"35s\"><img src=\"cid:MMSGreetingIMG\" region=\"Image\" fit=\"fill\"/><text src=\"cid:MMSGreetingTXT\" region=\"Text\"/></par></body></smil>";
		String param = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header><TransactionID xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2\" SOAP-ENV:mustUnderstand=\"1\">11278134</TransactionID></SOAP-ENV:Header><SOAP-ENV:Body><SubmitReq xmlns=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-3\"><MM7Version>5.3.0</MM7Version><SenderIdentification><VASPID>inmobia</VASPID><VASID>inmobia</VASID><SenderAddress><ShortCode>23355</ShortCode></SenderAddress></SenderIdentification><Recipients><To><Number>0193685271</Number></To></Recipients><ServiceCode>MMSCMPMMS0000</ServiceCode><MessageClass>Personal</MessageClass><DeliveryReport>true</DeliveryReport><ReadReply>false</ReadReply><Priority>Normal</Priority><Subject>TEST</Subject><DistributionIndicator>false</DistributionIndicator><Content allowAdaptations=\"false\" href=\"cid:23131_mms_start\" /></SubmitReq></SOAP-ENV:Body></SOAP-ENV:Envelope>";
		System.out.println(param);
		File textFile = new File("C:\\Users\\Paul\\Desktop\\MMS Pis\\holyGrail.xml");
		String filePath = "C:\\Users\\Paul\\Desktop\\MMS Pis\\mi_soap.jpg";
		File binaryFile = new File(filePath);
		final String B1 = "------=_Part_1_13676443.";
		final String B2 = "------=_Part_0_13676444.";
		String outerBoundary = B1+String.valueOf(System.currentTimeMillis()-12345);//"--=_Part_1_13676443.1242791008859";//"----=_Part_1_"+String.valueOf(System.currentTimeMillis());//.substring(0,10); // Just generate some unique random value.
		String innerBoundary = B2+String.valueOf(System.currentTimeMillis());
		
		String CRLF = "\r\n";//.getBytes("UTF-8"); ; // Line separator required by multipart/form-data.
		String endpointURL;
		boolean loc = false;
		//HttpURLConnection connection;
		URLConnection connection;
		OutputStream output = null;
		PrintWriter writer = null;
		OutputStreamWriter osWriter = null;
		
		//loc = true;
		
		if(args!=null){
			if(args.length>0){
				filePath = args[0];
				/*if(args[1]!=null)
					mms.setMsisdn(args[1]);
				if(args[2]!=null){
					addNH = Boolean.getBoolean(args[2]);*/	
				}
		}
		
		try{
			
			
	        
	        if(!loc){
	        	endpointURL = "http://203.82.66.118:5777/mm7/mm7tomms.sh";
	        	//return;
	        }else{
	        	endpointURL = "http://localhost:8080/celcom/mm7/mm7.sh";
	        }
	        
	        connection = new URL(endpointURL).openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestProperty("Content-Type", "multipart/related;  type=\"text/xml\"; start=\"</celcom-200102/mm7-submit>\"; boundary=\"" +outerBoundary+"\"");
	        //connection.setRequestProperty("Content-Type", "multipart/related; type=text/xml; start=\"</celcom-200102/mm7-submit>\"; boundary=" + boundary);
	        
	        connection.setRequestProperty("User-Agent", "");
	        
	       // connection.setRequestProperty("POST", " /mm7/mm7tomms.sh HTTP/1.1");
	        connection.setRequestProperty("Authorization", "Basic aW5tb2JpYTppbm1vYmlhMTIz");
	        connection.setRequestProperty("Accept", "");
	        connection.setRequestProperty("Connection", "close");
	        connection.setRequestProperty("SOAPAction", "\"\"");
	        
	       
	        writer = null;

	        
	        output = connection.getOutputStream();
	        writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!

	     // Send normal param.
	        writer.append(outerBoundary).append(CRLF).flush();
	        //writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
	        writer.append("Content-Type: text/xml; charset=\"" + charset+"\"").append(CRLF);
	        writer.append("Content-ID: </celcom-200102/mm7-submit>").append(CRLF);
	        writer.append(CRLF);
	        writer.append(param).append(CRLF).flush();
	        
	        
	        writer.append(outerBoundary).append(CRLF).flush();
	        writer.append("Content-Type: multipart/related; start=\"<cid:partsmil>\"; type=\"application/smil\"; boundary=\"" + innerBoundary+"\"").append(CRLF);
	        writer.append("Content-ID: <23131_mms_start>").append(CRLF).append(CRLF);

	        
	        writer.append(innerBoundary).append(CRLF).flush();
	        writer.append("Content-Type: application/smil").append(CRLF);
	        writer.append("Content-ID: <partsmil>").append(CRLF);
	        writer.append(CRLF);
	        writer.append(smilStr).append(CRLF).append(CRLF).flush();
	        
	        writer.append(innerBoundary).append(CRLF).flush();
	        writer.append("Content-Type: image/jpeg").append(CRLF);
	        writer.append("Content-Transfer-Encoding: base64").append(CRLF);
	        writer.append("Content-ID: <MMSGreetingIMG>").append(CRLF).append(CRLF).flush();
	        
	        String fileBase64 = "/9j/4AAQSkZJRgABAgEASABIAAD/4Rx8RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEAAAEaAAUAAAABAAAAYgEbAAUAAAABAAAAagEoAAMAAAABAAIAAAExAAIAAAAcAAAAcgEyAAIAAAAUAAAAjodpAAQAAAABAAAApAAAANAACvyAAAAnEAAK/IAAACcQQWRvYmUgUGhvdG9zaG9wIENTMyBXaW5kb3dzADIwMTA6MDU6MzEgMTQ6MjQ6MjMAAAAAA6ABAAMAAAAB//8AAKACAAQAAAABAAAA3KADAAQAAAABAAAA3AAAAAAAAAAGAQMAAwAAAAEABgAAARoABQAAAAEAAAEeARsABQ+Vk8nNd10RSHC8OC8/TEnxhPU5cOW/GmYmtOLxpeF9F7O+9mBrx2bhGv/v+H1d/M1dbcVVfmwjMV3zsVdirsVY35tj56FdnvG0bj6HA/jikP2J/59teYJNV/wCcf7rR5ZC58seZb61gUmvGK4SK6AHtzlfPQvZnLxaSv5siP0/pfjn/AIN+hrmPid+/d6BneOodirsVdirsVdirzTzHoeoWnmuHzpptpJqkcmkjSNb0yKnr+lFM1xbz24YgMUaSQOlQWDArUrxblPajsTJ2hjjLF9UL27wf0ufptQPD8KW29j3kUb+QSy581aKissupfo6Yfat7xZLOUHwKTrG1fozz2PZuqwyqeOQPuLkeGZef2vNfMF6msB4tJS61ydh8EdhbzXVf9lGjKPpOd72BinCQJiQPcxOMx50PeQHnVj/AM43eZvOmrQX/mqc+U9ARw0lsjRzanOp/ZULzigqP2mLMP5Bnsmm9sJ6LTnFp4+s/wAUunuj1+PyceWTHH+kfs/a+0PLflvRfKWi2Hl/y9YR6ZpOmx+na2sdTSpqzMxJZmYklmYkkmp3zjsuWeWZnMkykbJPMlxJzMzZT3IMXYq0RXFXjH5mflPD5xnXXtFuItM80QRLDI0wP1a+hSpSO44AsrJU8JFBIGxVhQDO0PaGTSSPDvE8x+ORdZ2p2Vi7Qx8E9pD6ZDmPf3x8viHzbqGi675adovMeiXej8DT61IhltG90uouUVP9Yg+IGbfL2lhzQNGj3F8x1/svr9NOxDjj3x3+Y5j4j4ozT7i3uVAhniuYzQ0jdZF/AkZ5d7TGwXN7NhLHIcQIPmCGWpe2em2ct1qN1Bp+m2yl7m5uXWKBE7l2chQKeOfMntfhlmkYQBlI7ADc35Vv8n1XsEGUhQs/Mvd/yjubi88g6Ncy29xa2kj3R0SK7R4pv0aLmUWLNHIAyhoOBUMAQvGoB2z6i9jcetxdjaWGuvxxjiJ3zvpfnVX5ud2oIjUz4a6XXfQ4vtt6VnSuA7FXYq7FXYq7FXYq7FX/0vv0KUFPoxVjfmv/ABN+h5/8Kej+mPUj9L1+PHhyHOnP4a08e1ab0zTdv/yj+Ul/J3B49iuP6ascX2W5vZ/5bxh+Yvgo8u/oyKH1fSj9fj63Eerwrx5U+LjXelelc28brfm4Zq9uSpkkOxV2KuxVo074q1t7/jire2Ku2xVvFXYq7FXYq0ad8VabjQ8vs03r0p74Fed6z/yqX1n/AE//AIU+s8vj+u/UvU5e/P4q5Rl8GvXw/Gmfqvf7Unsv+VHfpGz+qf4S/SPqr+ja/VOfrV+H0Of7df5N8wdP/J3i/uvD8Ty4eL9bkfv6NXXl+x66Kds2ziN4q7FXYq7FXYq7FXYq7FX/2Q==";//toBASE64(filePath, 1);
	        writer.append(fileBase64).append(CRLF).flush();
	        
	        
	        writer.append(innerBoundary).append(CRLF).flush();
	        writer.append("Content-Type: text/plain; charset=utf-8").append(CRLF);
	        writer.append("Content-ID: <MMSGreetingTXT>").append(CRLF).append(CRLF);
	        writer.append("Hello There!").append(CRLF).append(CRLF).flush();
	       
	       
	        
	        writer.append(innerBoundary + "--").append(CRLF);
	        
	        writer.append(outerBoundary+"--").append(CRLF).flush();
		   
			InputStream response = connection.getInputStream();
			
			
			String resp = StreamUtils.convertStreamToString(response);
			
			System.out.println("resp: "+resp);
			
	        
	        
	        
	        
	        
	        // Send text file.
	       /* writer.append("--" + boundary).append(CRLF);
	        writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
	        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
	        writer.append(CRLF).flush();
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), charset));
	            for (String line; (line = reader.readLine()) != null;) {
	                writer.append(line).append(CRLF);
	            }
	        } finally {
	            if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
	        }
	        writer.flush();*/

	        // Send binary file.
	        writer.append("--" + innerBoundary).append(CRLF);
	        writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
	        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
	        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
	        writer.append(CRLF).flush();
	        InputStream input = null;
	        try {
	            input = new FileInputStream(binaryFile);
	            byte[] buffer = new byte[1024];
	            for (int length = 0; (length = input.read(buffer)) > 0;) {
	                output.write(buffer, 0, length);
	            }
	            output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
	        } finally {
	            if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
	        }
	        
	        
	       // writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.

	        // End of multipart/form-data.
	       
			
			
			    
			    
		}finally{
			
			try{
				if(writer!=null)
				writer.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				if(osWriter!=null)
					osWriter.close();
			
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("done!");
			
		}
		

	}
	
	
	public static String toBASE64(String filePath, int flag) throws IOException  {
		if (flag == 0) {
			byte byteArray[] = filePath.getBytes();
			Base64 encoder = new Base64();
			return encoder.encodeToString(byteArray);

		} else {
			File file2 = new File(filePath);
			FileInputStream fin2 = new FileInputStream(file2);
			byte byteArray[] = new byte[fin2.available()];
			int i = -1, k = 0;
			while ((i = fin2.read()) != -1) {
				byteArray[k++] = (byte) i;
			}
			//Base64 encoder = new Base64();
			return Base64.encodeBase64String(byteArray);//encoder.encodeBase64String(byteArray);

		}
	}
	

}
