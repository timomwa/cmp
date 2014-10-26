package com.pixelandtag.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;


/**
 * 
 * Add a brief description of MailSender
 * Send mails incase of errors
 * 
 * @author <a href="mailto:enter email address">Paul</a>
 * @version enter version, 16 May 2013
 * @since  enter jdk version
 */
public class MailSender {

	public static String FROM_PARAMETER_NAME = "from";
	public static String TO_PARAMETER_NAME = "to";
	public static String SUBJECT_PARAMETER_NAME = "subject";
	public static String BODY_PARAMETER_NAME = "body";

	private StringPart from = null;
	private StringPart to = null;
	private StringPart subject = null;
	private StringPart body = null;
	private FilePart filename = null;
	private ArrayList<Part> parts = null;

	public MailSender(String from, String to, String subject, String body, String file) throws FileNotFoundException {

		this.from = new StringPart(MailSender.FROM_PARAMETER_NAME, from);
		this.to = new StringPart(MailSender.TO_PARAMETER_NAME, to);
		this.subject = new StringPart(MailSender.SUBJECT_PARAMETER_NAME,
				subject);
		this.body = new StringPart(MailSender.BODY_PARAMETER_NAME, body);
		this.filename = new FilePart(file, new File(file));
	}

	public boolean sendEmail() {

		boolean success = false;

		PostMethod post = new PostMethod("http://m.inmobia.com/sendmail/");
		HttpMethodParams params = post.getParams();
		Part[] parts = new Part[getParts().size()];
		for (int i = 0; i < getParts().size(); i++) {
			if (getParts().get(i) instanceof StringPart) {
				parts[i] = (StringPart) getParts().get(i);
			}
			if (getParts().get(i) instanceof FilePart) {
				parts[i] = (FilePart) getParts().get(i);
			}
		}

		MultipartRequestEntity mre = new MultipartRequestEntity(parts, params);
		post.setRequestEntity(mre);

		HttpClient httpclient = new HttpClient();
		int result;
		try {
			result = httpclient.executeMethod(post);
			String body = post.getResponseBodyAsString();
			success = (result == 200 && "OK".equalsIgnoreCase(body));
		} catch (HttpException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		return success;
	}

	public ArrayList<Part> getParts() {

		if (parts == null) {
			parts = new ArrayList<Part>();
			parts.add(getFrom());
			parts.add(getTo());
			parts.add(getSubject());
			parts.add(getBody());
			parts.add(getFilename());
		}
		return parts;
	}

	public void addFile(File f) {

		try {
			FilePartSource fps = new FilePartSource(f);
			FilePart filePart = new FilePart(f.getName(), fps);
			getParts().add(filePart);
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}
	public StringPart getFrom() {

		return from;
	}
	public StringPart getTo() {

		return to;
	}
	public StringPart getSubject() {

		return subject;
	}
	public StringPart getBody() {

		return body;
	}

	
	/**
	 * @return the filename
	 */
	public FilePart getFilename() {
	
		return filename;
	}




}