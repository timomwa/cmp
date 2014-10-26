package com.pixelandtag.reports;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;





//Mail sender code
public final class Emailer {
	
	private static final String usrName = "";
    private static final String pWd  = "";
    private static final Pattern rfc2822 = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
    

  public static void main(String args[])
  { 
	  
	try
	{
				
	/*System.out.println("Attempting Connection to the Mail Server.........");
    Emailer emailer = new Emailer();
    emailer.sendEmail("timo@timo.com","timothy@inmobia.com","Mail Test", "Hi! Do not reply to this mail,Just Testing if the Email application am working on is working. contact me on timothy@inmobia.com.Thanks, Peter.",null);
    
    System.out.println("Mail Successfully Sent Out");*/
	}
	catch(Exception e)
	
	{
		System.err.println(e.getMessage());
	}
   }
  
  /*
	 * get timestamp so we know when..
	 */
	private String getDateTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		return df.format(Calendar.getInstance().getTime());	
	}
	
  
  /**
   * Sends mail
   * @param aFromEmailAddr an email address to show as to have come from
   * @param aToEmailAddr the receipient email address
   * @param aSubject subject of the mail
   * @param aBody the body of the mail
   */
  public void sendEmail(
    String aFromEmailAddr, String aToEmailAddr,
    String aSubject, String aBody, String filename){
	  
	try{
		/*MultiPartEmail email = new MultiPartEmail();
		email.setHostName("172.30.66.111");
		email.setFrom((aFromEmailAddr == null ? "noreply@inmobia.com" : aFromEmailAddr), "Inmobia Email Service");*/
		String to,subject,body;
		String[] sendto;
		to = aToEmailAddr;
		subject = aSubject;
		body = aBody;
		
		if ( to == null || to.trim().length()==0 )
			throw new Exception("no 'to' found in request");
		if ( to.contains(",") )
			sendto = to.split(",");
		else if ( to.contains(";") )
			sendto = to.split(";");
		else
			sendto = new String[]{to};
		
		
		
		

	
			
		
		if ( subject == null || subject.trim().length()==0 )
			throw new Exception("no 'subject' found in request");
		
		if ( body == null || body.trim().length()==0 )
			throw new Exception("no 'body' found in request");
		
		
		
		
		
		//email.addTo("techsuport@inmobia.com");
			
		Set<String> fakeEmails = new HashSet<String>();
		for( int i=0; i<sendto.length; i++ )
			if (!rfc2822.matcher(sendto[i].toLowerCase()).matches()){
				System.out.println("email '"+sendto[i]+"' is invalid");
			}else{
				try{
					
					MultiPartEmail email = new MultiPartEmail();
					
					email.setHostName("172.30.66.111");
					
					email.setFrom((aFromEmailAddr == null ? "noreply@inmobia.com" : aFromEmailAddr), "Inmobia Email Service");
					
					EmailAttachment attachment;
					
					if ( filename != null )	{
						
						File file = new File(filename);
						
						attachment = new EmailAttachment();
						attachment.setPath(file.getPath());
						attachment.setDisposition(EmailAttachment.ATTACHMENT);
						attachment.setName(file.getName());

						email.attach(attachment);
						
						
					} 
					
					email.setSubject(subject);
					email.setMsg(body);
					
					System.out.println(">>>attempting to send to "+sendto[i]);
					email.addBcc(sendto[i]);
					email.send();
					System.out.println("OK");
					System.out.println("sent to "+sendto[i]);
					
				}catch(Exception e){
					System.out.println("sendint to "+sendto[i]+"failed!");
					fakeEmails.add(sendto[i]);
					e.printStackTrace();
				}
			}
		
		
		if(fakeEmails!=null)
		if(fakeEmails.size()>0){
			sendEmail("noreply@inmobia.com","timothy@inmobia.com,anthony@inmobia.com","FF - Email Sending Partial Failure "+getDateTime()+"", "Hi,\n\" The following emails did not receive the FF report. Please send to them manually. \n"+fakeEmails.toString(),filename,false);
			System.out.println("fakeEmails>>>>>>>>>>>>>"+getDateTime()+">>>>>>>>>"+fakeEmails.toString());
		}
		
	} catch ( EmailException e ) {
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}

  }  
  
  
 /**
   * Sends mail
   * @param aFromEmailAddr an email address to show as to have come from
   * @param aToEmailAddr the receipient email address
   * @param aSubject subject of the mail
   * @param aBody the body of the mail
   */
  public void sendEmail(
    String aFromEmailAddr, String aToEmailAddr,
    String aSubject, String aBody, String filename, boolean you){
	  fetchConfig();
	//Added here
	SMTPAuthenticator auth = new SMTPAuthenticator();
	auth.getPasswordAuthentication();

  
    Session session = Session.getDefaultInstance( fMailServerConfig,auth);
    MimeMessage message = new MimeMessage( session );
    
    try {
    	File file = null;
    	EmailAttachment attachment = null;
    	
    	
    	if(filename != null){
    		file = new File(filename);
    		
    	}
    	String[] emails = aToEmailAddr.split("[,]");
    	
    	for(int i = 0; i<emails.length; i++){	
    	    System.out.println(emails[i]);
        	message.addRecipient(
    	        Message.RecipientType.TO, new InternetAddress(emails[i])
    	      );
        
        }
    	
      InternetAddress inetAdd = new InternetAddress(aFromEmailAddr);
      try {
		inetAdd.setPersonal("Statistics", "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      message.setFrom(inetAdd);
      message.setSubject( aSubject );
      message.setText( aBody );
      Transport.send( message );
    }
    catch (MessagingException ex){
      System.err.println("Cannot send email. " + ex);
    }
  }  
  
  
  
  
  public static void refreshConfig() {
    fMailServerConfig.clear();
    fetchConfig();
  }
 // PRIVATE //

  private static Properties fMailServerConfig = new Properties();

  static {
    //fetchConfig();
  }

   
  private static void fetchConfig() {
    
    try {
      
       	
    	
    	/*email.setHostName("websmtp");
		email.setSmtpPort(6666);*/
		
    	fMailServerConfig.setProperty("mail.smtp.host", "172.30.66.111");
    	
    	//fMailServerConfig.setProperty("mail.smtp.port", "6666");
    	
    }
    catch (Exception ex ){
      System.err.println("Server Not Found....  " + ex.getMessage());
    }

  }
 // Authentication code begins here
  private class SMTPAuthenticator extends javax.mail.Authenticator {
      public PasswordAuthentication getPasswordAuthentication() {
         String username = usrName;
         String password = pWd;
         return new PasswordAuthentication(username, password);
      }
  }

  // it ends here
} 

