package com.pixelandtag.cmp.ejb.api.ussd;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.pixelandtag.api.CelcomImpl;
import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.DatingServiceException;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.MessageEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.ProcessorResolverEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.IncomingSMS;
import com.pixelandtag.cmp.entities.MOProcessor;
import com.pixelandtag.cmp.entities.Message;
import com.pixelandtag.cmp.entities.OpcoSMSService;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.customer.OperatorCountry;
import com.pixelandtag.dating.entities.AlterationMethod;
import com.pixelandtag.dating.entities.Gender;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileAttribute;
import com.pixelandtag.dating.entities.ProfileQuestion;
import com.pixelandtag.dating.entities.QuestionLog;
import com.pixelandtag.mo.sms.OrangeUSSD;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.sms.producerthreads.EventType;
import com.pixelandtag.smsmenu.MenuItem;
import com.pixelandtag.subscription.dto.SubscriptionStatus;

@Stateless
@Remote
public class USSDMenuEJBImpl implements USSDMenuEJBI {
	
	private static final String BR_NEW_LINE = "<br/>";
	private static final String RETURN_CARRIAGE = "\n";
	private static final String PROBLEM_OCCURRED = "Sorry, a problem occurred. This has been noted and will be fixed as soon as possible.";

	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	private MessageEJBI messageEJB;
	
	@EJB
	private OpcoSMSServiceEJBI opcosmserviceEJB;
	
	@EJB
	private ProcessorResolverEJBI processorEJB;
	
	@EJB
	private DatingServiceI datingBean;
	
	@EJB
	private SubscriptionBeanI subscriptionBean;
	
	private String SPACE = " ";
	
	private Logger logger = Logger.getLogger(getClass());
	
	private XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1")) {
        @Override
        public String escapeElementEntities(String str) {
        	return str;
        }
    };
    
    @Override 
    public USSDResponseWrapper getNextQuestionOrange(Map<String,String> attribz, IncomingSMS incomingsms){
    	
    	String xml = "";
    	USSDResponseWrapper resp = new USSDResponseWrapper();
    	Element rootelement = new Element("pages");
    	Document doc = new Document(rootelement); 
		DocType doctype = new DocType("pages");
		doctype.setSystemID("cellflash-1.3.dtd");
		doc.setDocType(doctype);
		
		
		
		Element page = new Element("page");
		final StringBuffer sb = new StringBuffer();
		final StringBuffer loggingSB = new StringBuffer();
    	
    	try{
    		if(incomingsms.getMsisdn()==null || incomingsms.getMsisdn().isEmpty())
    			throw new OrangeUSSDException("Looks like this is not a genuine GSM call.");
    		
		    	String baseurl = attribz.get("contextpath");
				String finalquestion = attribz.get("finalquestion");
				String answers = attribz.get("answers");
				if(answers!=null && !answers.isEmpty())
					answers = answers.trim();
				int languageid_ =  OrangeUSSD.setdefaultifnull( attribz.get("languageid") );
				
				
				Person person =  datingBean.getPerson(incomingsms.getMsisdn(), incomingsms.getOpco());
				if(person==null)
					person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
				
				if(person==null)
					person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
				
				PersonDatingProfile profile = datingBean.getProfile(person);
				
				
				if(person.getId()>0 && profile==null){
					
					profile = datingBean.getProfile(person);
					
					if(profile==null){
						profile = new PersonDatingProfile();
						profile.setPerson(person);
						profile.setUsername(incomingsms.getMsisdn());
						profile = datingBean.saveOrUpdate(profile);
					}
					
					
					
					ProfileQuestion question = datingBean.getNextProfileQuestion(profile.getId());
					logger.debug("QUESTION::: "+question.getQuestion());
					
					
					QuestionLog ql = new QuestionLog();
					
					ql.setProfile_id_fk(profile.getId());
					ql.setQuestion_id_fk(question.getId());
					ql = datingBean.saveOrUpdate(ql);
					
					sb.append(question.getQuestion());
					sb.append(BR_NEW_LINE);
					sb.append("<a href=\""+baseurl+"?answers=1\">Yes</a>");
					sb.append(BR_NEW_LINE);
					sb.append("<a href=\""+baseurl+"?answers=2\">No</a>");
					
					
					loggingSB.append(question.getQuestion());
					loggingSB.append(RETURN_CARRIAGE);
					loggingSB.append("Yes");
					loggingSB.append(RETURN_CARRIAGE);
					loggingSB.append("No");
					
					page.setText(sb.toString());
					rootelement.addContent(page);
					sb.setLength(0);
					xml = xmlOutput.outputString(doc);
					
					resp.setLoggableMessage(loggingSB.toString());
					resp.setResponseMessage(xml);
					return resp;
					
				}
				
				if(profile==null){
					profile = new PersonDatingProfile();
					profile.setPerson(person);
					profile.setUsername(incomingsms.getMsisdn());
					profile = datingBean.saveOrUpdate(profile);
				}
				
				
				if(!profile.getProfileComplete()){//answers!=null && !answers.isEmpty() ){//We set the profile answer
					
					
					
					ProfileQuestion previousQuestion =  getPreviousQuestion(person, incomingsms);
					
					ProfileAttribute attr = previousQuestion.getAttrib();
					logger.debug("PREVIOUS QUESTION ::: "+previousQuestion.getQuestion() + " SUB ANSWER : "+answers);
					logger.debug("ATRIBUTE ADDRESSING ::: "+attr.toString());
					
					
					if(attr.equals(ProfileAttribute.DISCLAIMER)){
						boolean keywordIsNumber = false;
						int agreed = -1;
						try{
							agreed = Integer.parseInt(answers);
							keywordIsNumber = true;
						}catch(Exception exp){}
						
						if( (keywordIsNumber && agreed==1 ) || (answers!=null && (answers.trim().equalsIgnoreCase("A") || answers.trim().equalsIgnoreCase("Y") || answers.trim().equalsIgnoreCase("YES") || answers.trim().equalsIgnoreCase("YEP")
								|| answers.trim().equalsIgnoreCase("NDIO") || answers.trim().equalsIgnoreCase("NDIYO")  || answers.trim().equalsIgnoreCase("SAWA") || answers.trim().equalsIgnoreCase("OK") )) ){
							person.setAgreed_to_tnc(Boolean.TRUE);
						}else if((keywordIsNumber && agreed==1 ) || (answers!=null && (answers.trim().equalsIgnoreCase("A") || answers.trim().equalsIgnoreCase("N") || answers.trim().equalsIgnoreCase("NO")))){
							sb.append("Ok. Bye");
							loggingSB.append(sb.toString());
						}else{
							String msg = datingBean.getMessage(DatingMessages.MUST_AGREE_TO_TNC, languageid_, person.getOpco().getId());
							msg += "1. Agreed\n2.Disagreed"; 
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+msg+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(msg);
						}
					}
					
					if(attr.equals(ProfileAttribute.CHAT_USERNAME)){
						
						boolean isunique = datingBean.isUsernameUnique(answers);
						
						try{
							if(isunique)
								isunique = !(("0"+person.getMsisdn().substring(3)).equals(Integer.valueOf(answers).toString()));
						}catch(Exception exp){}
						
						if(isunique && answers!=null && !answers.trim().isEmpty()){
							profile.setUsername(answers);
						}else{
							String msg = "";
							if(answers!=null && answers.equalsIgnoreCase("329")){
								msg = datingBean.getMessage(DatingMessages.REPLY_WITH_USERNAME, languageid_,person.getOpco().getId());
							}else{
								msg = datingBean.getMessage(DatingMessages.USERNAME_NOT_UNIQUE_TRY_AGAIN, languageid_,person.getOpco().getId());
							}
							
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG, ( answers!=null ? answers : profile.getUsername()) );
							
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+msg+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.append(msg);
						}
					}
					
					if(attr.equals(ProfileAttribute.GENDER)){
						
						if(answers!=null && (answers.equalsIgnoreCase("2") || answers.equalsIgnoreCase("M") ||  answers.equalsIgnoreCase("MALE") ||  answers.equalsIgnoreCase("MAN") ||  answers.equalsIgnoreCase("BOY") ||  answers.equalsIgnoreCase("MUME") ||  answers.equalsIgnoreCase("MWANAMME")  ||  answers.equalsIgnoreCase("MWANAUME"))){ 
							profile.setGender(Gender.MALE);
							profile.setPreferred_gender(Gender.FEMALE);
						}else if(answers!=null && (answers.equalsIgnoreCase("1") || answers.equalsIgnoreCase("F") ||  answers.equalsIgnoreCase("FEMALE") ||  answers.equalsIgnoreCase("LADY") ||  answers.equalsIgnoreCase("GIRL") ||  answers.equalsIgnoreCase("MKE") ||  answers.equalsIgnoreCase("MWANAMKE")  ||  answers.equalsIgnoreCase("MWANAMUKE"))){ 
							profile.setGender(Gender.FEMALE);
							profile.setPreferred_gender(Gender.MALE);
						}else{
							String msg = null;
							try{
								msg = datingBean.getMessage(DatingMessages.GENDER_NOT_UNDERSTOOD, languageid_,person.getOpco().getId());
							}catch(DatingServiceException dse){
								logger.error(dse.getMessage(), dse);
							}
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+msg+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(msg);
						
							
						}
						
					}
					
					if(attr.equals(ProfileAttribute.AGE)){
						
						Date dob = new Date();
						BigDecimal age = BigDecimal.ONE;
						try{
							if(answers!=null)
								age = new BigDecimal(answers);
							else
								age = null;
						}catch(java.lang.NumberFormatException nfe){
							String msg = datingBean.getMessage(DatingMessages.AGE_NUMBER_INCORRECT, languageid_,person.getOpco().getId());
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG, profile.getUsername());
							msg = msg.replaceAll(GenericServiceProcessor.AGE_TAG, age.intValue()+"");
							sb.append(msg);
							loggingSB.append(msg);
							age = null;
						}
						
						if(age!=null && age.compareTo(new BigDecimal(100l))>=0){
							String msg = datingBean.getMessage(DatingMessages.UNREALISTIC_AGE, languageid_,person.getOpco().getId());
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							msg = msg.replaceAll(GenericServiceProcessor.AGE_TAG, age.intValue()+"");
							sb.append(msg);
							loggingSB.append(msg);
						}
						
						if(age!=null && age.compareTo(new BigDecimal(18l))<0){
							String msg = datingBean.getMessage(DatingMessages.SERVICE_FOR_18_AND_ABOVE, languageid_,person.getOpco().getId());
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.append(msg);
							loggingSB.append(msg);
							page.setAttribute( "nav", "end");
						}
						
						dob = datingBean.calculateDobFromAge(age);
						profile.setDob( dob );
						profile.setPreferred_age(BigDecimal.valueOf(18L));
						
					}
					
					if(attr.equals(ProfileAttribute.LOCATION)){
						boolean location_is_only_number = false;
						try{
							if(answers!=null){
								new BigDecimal(answers);
								location_is_only_number = true;
							}
						}catch(java.lang.NumberFormatException nfe){
						}
						if(answers!=null && (answers.contains("*") || answers.equalsIgnoreCase("329")  || location_is_only_number)){
							String msg = datingBean.getMessage(DatingMessages.LOCATION_INVALID, languageid_,person.getOpco().getId());
							msg = msg.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.append(msg);
							loggingSB.append(msg);
						}else{
							profile.setLocation(answers);
							
							profile.setProfileComplete(true);
							person.setActive(true);
							profile.setPerson(person);
							
							SMSService smsservice = datingBean.getSMSService("DATE",person.getOpco());
							
							subscriptionBean.renewSubscription(incomingsms.getOpco(), incomingsms.getMsisdn(), smsservice, SubscriptionStatus.confirmed,AlterationMethod.self_via_ussd);
						}
					}
					
					
					profile = datingBean.saveOrUpdate(profile);
					
				}
				
				//logger.info(">>>>>>>>>>> "+sb.toString());
				if(sb.toString()==null || sb.toString().isEmpty()){// we move to the next question
				
					ProfileQuestion profileQuestion = getNextQuestion(profile,incomingsms);
					if(profileQuestion!=null){
						
						String question = profileQuestion.getQuestion();
						ProfileAttribute attrib = profileQuestion.getAttrib();
						Long questionid = profileQuestion.getId();
						Long languageid = profileQuestion.getLanguage_id();
						
						
						baseurl = baseurl+"?attrib="+attrib+"&questionid="+questionid+"&languageid="+languageid;
						
						sb.append(question);
						sb.append(BR_NEW_LINE);
						
						loggingSB.append(question);
						loggingSB.append(RETURN_CARRIAGE);
						
						if(attrib==ProfileAttribute.DISCLAIMER){
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+question+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(question);
							
						}
						
						if(attrib==ProfileAttribute.CHAT_USERNAME){//Form
							question = question.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+question+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(question);
							
						}
						
						if(attrib==ProfileAttribute.GENDER){
							question = question.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+question+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(question);
						
						}
						if(attrib==ProfileAttribute.AGE){
							question = question.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+question+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(question);
						}
						if(attrib==ProfileAttribute.LOCATION){
							question = question.replaceAll(GenericServiceProcessor.USERNAME_TAG,  profile.getUsername());
							baseurl = baseurl+"&finalquestion=true";
							sb.setLength(0);
							sb.append("<form action=\""+baseurl+"\">");
							sb.append("<entry kind=\"digits\" var=\"answers\">");
							sb.append("<prompt>"+question+"</prompt>");
							sb.append("</entry></form>");
							
							loggingSB.setLength(0);
							loggingSB.append(question);
						}
						
						QuestionLog ql = new QuestionLog();
						
						ql.setProfile_id_fk(profile.getId());
						ql.setQuestion_id_fk(profileQuestion.getId());
						ql = datingBean.saveOrUpdate(ql);
						
					}else{
						
						String doubleconfirm = attribz.get("doubleconfirm");
						String serviceid = attribz.get("serviceid");
						String menuid = attribz.get("menuid");
						String waitingdoubleconfirm = attribz.get("waitingdoubleconfirm");
						String action = attribz.get("action");
						
						if((finalquestion!=null && finalquestion.equalsIgnoreCase("true")) 
								|| 
								(action!=null && action.equalsIgnoreCase("PURCHASE_CHAT_BUNDLES"))){
							
							baseurl = baseurl+"?bundlepurchase=true";
							
							sb.setLength(0);
							sb.append("Select chat bundle");
							sb.append(BR_NEW_LINE);
							sb.append("<a href=\""+baseurl+"&answers=1&menuid=148&serviceid=439&doubleconfirm=true\">5/- per day</a>");
							sb.append(BR_NEW_LINE);
							sb.append("<a href=\""+baseurl+"&answers=2&menuid=149&serviceid=440&doubleconfirm=true\">15/- per week</a>");
							sb.append(BR_NEW_LINE);
							sb.append("<a href=\""+baseurl+"&answers=3&menuid=150&serviceid=441&doubleconfirm=true\">30/- per month</a>");
							
							
							
							loggingSB.setLength(0);
							loggingSB.append("Select chat bundle");
							loggingSB.append(RETURN_CARRIAGE);
							loggingSB.append("1. 5/- per day");
							loggingSB.append(RETURN_CARRIAGE);
							loggingSB.append("2. 15/- per week");
							loggingSB.append(RETURN_CARRIAGE);
							loggingSB.append("3. 30/- per month");
							
							
							
							
						}else if(doubleconfirm!=null && doubleconfirm.equalsIgnoreCase("true")){
							
							baseurl = baseurl+"?bundlepurchase=true";
							String bundle = "";
							if(serviceid!=null && serviceid.equals("439")){
								bundle="5/- daily chat bundle."; // 40 cts per sms
							}
							if(serviceid!=null && serviceid.equals("440")){
								bundle="15/- weekly chat bundle."; // 50 cts per sms
							}
							if(serviceid!=null && serviceid.equals("441")){
								bundle="30/- monthly chat bundle."; // 60 cts per sms
							}
							sb.setLength(0);
							sb.append("Please confirm purchase of the ");
							sb.append(bundle);
							sb.append(BR_NEW_LINE);
							sb.append("<a href=\""+baseurl+"&answers=1&menuid="+menuid+"&serviceid="+serviceid+"&waitingdoubleconfirm=true\">Accept</a>");
							sb.append(BR_NEW_LINE);
							sb.append("<a href=\""+baseurl+"&answers=2&menuid="+menuid+"&serviceid="+serviceid+"&waitingdoubleconfirm=true\">Decline</a>");
							
							loggingSB.setLength(0);
							loggingSB.append("Please confirm purchase of the ");
							loggingSB.append(bundle);
							loggingSB.append(RETURN_CARRIAGE);
							loggingSB.append("1. Accept");
							loggingSB.append(RETURN_CARRIAGE);
							loggingSB.append("2. Decline");
							
							
							
							
						}else if(waitingdoubleconfirm!=null && waitingdoubleconfirm.equalsIgnoreCase("true")){
							
							if(answers!=null && answers.equalsIgnoreCase("1")){//Accepted
								
								String bundle = "";
								if(serviceid!=null && serviceid.equals("439")){
									bundle="5/- daily chat bundle."; // 40 cts per sms
								}
								if(serviceid!=null && serviceid.equals("440")){
									bundle="15/- weekly chat bundle."; // 50 cts per sms
								}
								if(serviceid!=null && serviceid.equals("441")){
									bundle="30/- monthly chat bundle."; // 60 cts per sms
								}
								try{
									
									mimicMO(serviceid,incomingsms);
									sb.setLength(0);
									sb.append("Request to purchase the ");
									sb.append(bundle);
									sb.append(" has been received and will be processed shortly. You'll receive a confirmation SMS.");
									sb.append(BR_NEW_LINE);
									page.setAttribute( "nav", "end");
									
									
									loggingSB.setLength(0);
									loggingSB.append("Request to purchase the ");
									loggingSB.append(bundle);
									loggingSB.append(" has been received and will be processed shortly. You'll receive a confirmation SMS.");
									
									
								}catch(Exception exp){
									logger.error(exp.getMessage(),exp);
									throw exp;
								}
								
								
							}else if(answers!=null && answers.equalsIgnoreCase("2")){//Accepted
								
								sb.setLength(0);
								sb.append("Thanks \"");
								sb.append(profile.getUsername());
								sb.append("\". Please note that off-bundles chatting is charged 1/- per SMS.");
								
								loggingSB.setLength(0);
								loggingSB.append(sb.toString());
								
								page.setAttribute( "nav", "end");
								
							}else{//Invalid input, ask the question again..
								
								baseurl = baseurl+"?bundlepurchase=true";
								String bundle = "";
								if(serviceid!=null && serviceid.equals("439")){
									bundle="5/- daily chat bundle."; // 40 cts per sms
								}
								if(serviceid!=null && serviceid.equals("440")){
									bundle="15/- weekly chat bundle."; // 50 cts per sms
								}
								if(serviceid!=null && serviceid.equals("441")){
									bundle="30/- monthly chat bundle."; // 60 cts per sms
								}
								
								sb.setLength(0);
								sb.append("Invalid input.");
								sb.append(BR_NEW_LINE);
								sb.append("Please confirm purchase of the ");
								sb.append(bundle);
								sb.append(BR_NEW_LINE);
								sb.append("<a href=\""+baseurl+"&answers=1&menuid="+menuid+"&serviceid="+serviceid+"&waitingdoubleconfirm=true\">Accept</a>");
								sb.append(BR_NEW_LINE);
								sb.append("<a href=\""+baseurl+"&answers=2&menuid="+menuid+"&serviceid="+serviceid+"&waitingdoubleconfirm=true\">Decline</a>");
								
								
								
								
								loggingSB.setLength(0);
								loggingSB.append("Invalid input.");
								loggingSB.append(RETURN_CARRIAGE);
								loggingSB.append("Please confirm purchase of the ");
								loggingSB.append(bundle);
								loggingSB.append(RETURN_CARRIAGE);
								loggingSB.append("1. Accept");
								loggingSB.append(RETURN_CARRIAGE);
								loggingSB.append("2. Decline");
								
								
							}
							
							
						}else{
							
							
							if(profile.getProfileComplete()){
								
								if(action!=null && action.equals("FIND_FRIEND")){
									
									mimicMO(serviceid,incomingsms);
									
									Message msg = messageEJB.getMessage(DatingMessages.FIND_FRND_RESPONSE.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId());
									sb.append(msg.getMessage());
									sb.append(BR_NEW_LINE);
									
									loggingSB.append(sb.toString());
									
									page.setAttribute( "nav", "end");
									
									
								}else if(action!=null && action.equals("LOGIN")){
									
									mimicMO(serviceid,incomingsms);
									
									Message msg = messageEJB.getMessage(DatingMessages.LOGIN_RESPONSE_MSG.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId());
									sb.append(msg.getMessage());
									sb.append(BR_NEW_LINE);
									
									loggingSB.append(sb.toString());
									
									page.setAttribute( "nav", "end");
									
								}else if(action!=null && action.equals("LOGOUT")){
									
									mimicMO(serviceid,incomingsms);
									
									Message msg = messageEJB.getMessage(DatingMessages.LOGOUT_RESPONSE_MSG.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId());
									sb.append(msg.getMessage());
									sb.append(BR_NEW_LINE);
									
									loggingSB.append(sb.toString());
									
									page.setAttribute( "nav", "end");
									
									
								}else{
								
									sb.setLength(0);
									loggingSB.setLength(0);
									
									String msg = messageEJB.getMessage(DatingMessages.PURCHASE_CHAT_BUNDLE.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId()).getMessage();
									
									sb.append("<a href=\""+baseurl+"?existingsub=true&action=PURCHASE_CHAT_BUNDLES&doubleconfirm=true\">"+msg+"</a>");
									sb.append(BR_NEW_LINE);
									
									loggingSB.append("1. "+msg);
									loggingSB.append(RETURN_CARRIAGE);
									
									msg = messageEJB.getMessage(DatingMessages.FIND_FRIEND_NEAR.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId()).getMessage();
									sb.append("<a href=\""+baseurl+"?existingsub=true&action=FIND_FRIEND&menuid=152&serviceid=442\">"+msg+"</a>");
									sb.append(BR_NEW_LINE);
									
									loggingSB.append("2. "+msg);
									loggingSB.append(RETURN_CARRIAGE);
									
									
									msg = messageEJB.getMessage(DatingMessages.LOGIN.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId()).getMessage();
									
									sb.append("<a href=\""+baseurl+"?existingsub=true&action=LOGIN&menuid=153&serviceid=447\">"+msg+"</a>");
									sb.append(BR_NEW_LINE);
									
									loggingSB.append("3. "+msg);
									loggingSB.append(RETURN_CARRIAGE);
									
									
									msg = messageEJB.getMessage(DatingMessages.LOGOUT.name(), Long.valueOf(profile.getLanguage_id()), incomingsms.getOpco().getId()).getMessage();
									sb.append("<a href=\""+baseurl+"?existingsub=true&action=LOGOUT&menuid=154&serviceid=448\">"+msg+"</a>");
									
									loggingSB.append("4. "+msg);
									loggingSB.append(RETURN_CARRIAGE);
									
								}
								
							
							}else{
								sb.setLength(0);
								sb.append("Hey \"");
								sb.append(profile.getUsername());
								sb.append("\". You can purchase bundles.");
								sb.append(BR_NEW_LINE);
								page.setAttribute( "nav", "end");
								
								
								loggingSB.setLength(0);
								loggingSB.append(sb.toString());
							}
						}
						
						
					}
				}
				
				page.setText(sb.toString());
				rootelement.addContent(page);
				sb.setLength(0);
				xml = xmlOutput.outputString(doc);
				
				resp.setLoggableMessage(loggingSB.toString());
				resp.setResponseMessage(xml);
				
				return resp;
				
    	}catch(NullPointerException exp){
    		
    		exp.printStackTrace();
    		logger.error(exp.getMessage(), exp);
			resp.setResponseMessage(PROBLEM_OCCURRED);
    		resp.setLoggableMessage(PROBLEM_OCCURRED);
    		
    		return resp;
    	}catch(OrangeUSSDException exp){
    		exp.printStackTrace();
    		sb.append("Looks like this is not a genuine GSM call. Use a handset to dial *329# to access the menu or mimic a call from the operator by setting the required header parameters.");
    		page.setAttribute( "nav", "end");
    		page.setText(sb.toString());
			rootelement.addContent(page);
			xml = xmlOutput.outputString(doc);
			
			logger.error(exp.getMessage(), exp);
			sb.setLength(0);
			
			resp.setLoggableMessage(sb.toString());
			resp.setResponseMessage(xml);
    		return resp;
    	}catch(Exception exp){
    		exp.printStackTrace();
    		logger.error(exp.getMessage(), exp);
			resp.setResponseMessage(PROBLEM_OCCURRED);
    		resp.setLoggableMessage(PROBLEM_OCCURRED);
    		
    		return resp;
    	}
	}
    
    
   

	private boolean mimicMO(String serviceid, IncomingSMS insms) throws Exception {
		OpcoSMSService smsservice  = opcosmserviceEJB.getOpcoSMSService(Long.valueOf(serviceid), insms.getOpco());
		SMSService smsserv = smsservice.getSmsservice();//em.find(SMSService.class, Long.valueOf(serviceid+""));
		logger.info("\t\t\t:::::::::::::::::::::::::::::: serviceid:: "+serviceid+ " CMD :"+(smsserv!=null ? smsserv.getCmd() : null));
		MOProcessor proc = smsserv.getMoprocessor();
		
		IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
		incomingsms.setMsisdn(insms.getMsisdn());
		incomingsms.setServiceid(smsserv.getId());
		incomingsms.setSms(smsserv.getCmd());
		incomingsms.setShortcode(proc.getShortcode());
		incomingsms.setPrice(smsserv.getPrice());
		incomingsms.setCmp_tx_id(generateNextTxId());
		incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
		incomingsms.setServiceid(smsserv.getId());
		incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
		incomingsms.setMoprocessor(proc);
		incomingsms.setOpco(insms.getOpco());
		logger.info("\n\n\n\n\n::::::::::::::::proc.getId().intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");
		
		incomingsms = processorEJB.processMo(incomingsms);
		return incomingsms.getId()!=null;
	}




	public String startDatingQuestions(IncomingSMS incomingsms) throws Exception{
    	
		try{
			Person person =  datingBean.getPerson(incomingsms.getMsisdn(), incomingsms.getOpco());
			if(person==null)
				person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
			
			if(person==null)
				person = datingBean.register(incomingsms.getMsisdn(), incomingsms.getOpco());
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			
			if(profile==null){
				profile = new PersonDatingProfile();
				profile.setPerson(person);
				profile.setUsername(incomingsms.getMsisdn());
				profile = datingBean.saveOrUpdate(profile);
			}
			
			
	    	ProfileQuestion profileQuestion = getNextQuestion(profile,incomingsms);
	    	
	    	String question = "";
	    	if(profileQuestion!=null){
	    		logger.debug("QUESTION::: "+profileQuestion.getQuestion());
	    		question =  SPACE +profileQuestion.getQuestion();
	    	}
	    	
	    	return question;	
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
    }
    
    
	 private ProfileQuestion getPreviousQuestion(Person person, IncomingSMS incomingsms) {
		 
		 try {
			
			PersonDatingProfile profile = datingBean.getProfile(person);
			return datingBean.getPreviousQuestion(profile.getId());
		 }catch (DatingServiceException e) {
			 logger.error(e.getMessage(), e);
		 }catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		 }
		 return null;
	
	 }

    public ProfileQuestion getNextQuestion(PersonDatingProfile profile, IncomingSMS incomingsms){
    	
		try {
			return datingBean.getNextProfileQuestion(profile.getId());
			
		}catch (DatingServiceException e) {
			logger.error(e.getMessage(), e);
		}catch(Exception exp){
			logger.error(exp.getMessage(), exp);
		}
		
		return null;
		
    }
    
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.api.ussd.USSDMenuEJBI#getMenu(int, int, int)
	 */
	public String getMenu(String contextpath, String msisdn, int language_id, int parent_level_id, int menuid, int menuitemid, OperatorCountry opco){
		
		language_id = language_id==-1 ? 1 : language_id;
		menuid = menuid==-1 ? 1 : menuid;
		
		Element rootelement = new Element("pages");
		rootelement.setAttribute("descr", "dating");
		
		Document doc = new Document(rootelement); 
		DocType doctype = new DocType("pages");
		doctype.setSystemID("cellflash-1.3.dtd");
		doc.setDocType(doctype);
		

		
		Element page = new Element("page");
		StringBuffer sb = new StringBuffer();
		
		String xml = "";
		
		
		try {
			MenuItem menuitem =   menuitemid>-1 ? getMenuById(menuitemid) : getMenuByParentLevelId(language_id, parent_level_id, menuid);
			LinkedHashMap<Integer, MenuItem> topMenus = menuitem.getSub_menus();
			
			int serviceid = menuitem.getService_id();
			sb.append(menuitem.getName()+"<br/>");
			
			
			if(topMenus!=null && topMenus.entrySet()!=null){
				
				
				for (Entry<Integer, MenuItem> entry : topMenus.entrySet()){
					String menuname = entry.getValue().getName();
					int menuitemid_ = entry.getValue().getId();
					int languageid = entry.getValue().getLanguage_id();
					int serviceid_ = entry.getValue().getService_id();
					int menuid_ = entry.getValue().getMenu_id();
					int parent_level_id_ = entry.getValue().getParent_level_id();
					sb.append("<a href=\""+contextpath+"?menuitemid="
					+menuitemid_+"&languageid="
					+languageid+"&serviceid="
					+serviceid_+"&menuid="
					+menuid_+"&parent_level_id="
					+parent_level_id_+"\">"+menuname+"</a><br/>");
				}
				
			
			}else if(serviceid>-1){
				
				OpcoSMSService smsservice  = opcosmserviceEJB.getOpcoSMSService(Long.valueOf(serviceid), opco);
				SMSService smsserv = smsservice.getSmsservice();//em.find(SMSService.class, Long.valueOf(serviceid+""));
				logger.info("\t\t\t:::::::::::::::::::::::::::::: serviceid:: "+serviceid+ " CMD :"+(smsserv!=null ? smsserv.getCmd() : null));
				
				
				MOProcessor proc = smsserv.getMoprocessor();
				
				IncomingSMS incomingsms =  new IncomingSMS();//getContentFromServiceId(chosenMenu.getService_id(),MSISDN,true);
				incomingsms.setMsisdn(msisdn);
				incomingsms.setServiceid(Long.valueOf( serviceid ));
				incomingsms.setSms(smsserv.getCmd());
				incomingsms.setShortcode(proc.getShortcode());
				incomingsms.setPrice(smsserv.getPrice());
				incomingsms.setCmp_tx_id(generateNextTxId());
				incomingsms.setEvent_type(EventType.get(smsserv.getEvent_type()).getName());
				incomingsms.setServiceid(smsserv.getId());
				incomingsms.setPrice_point_keyword(smsserv.getPrice_point_keyword());
				//incomingsms.setId(req.getMessageId());
				incomingsms.setMoprocessor(proc);
				incomingsms.setOpco(opco);
				logger.info("\n\n\n\n\n::::::::::::::::proc.getId().intValue() "+proc.getId().intValue()+"::::::::::::::\n\n\n");
				
				processorEJB.processMo(incomingsms);
				
				String resp = "";
				if(smsserv.getCmd().equals("FIND")){
					resp = "Request to find friend near your area received. You shall receive an sms shortly.";
				}else{
					resp = "Request received and is being processed.";
				}
				
				sb.append(resp);
				
			}
			
			page.setText(sb.toString()+"<br/>");
			sb.setLength(0);
			rootelement.addContent(page);
			xml = xmlOutput.outputString(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return xml;
	
	}
	
	
	

	@SuppressWarnings("unchecked")
	public MenuItem getMenuByParentLevelId(int language_id, int parent_level_id, int menuid) throws Exception{
		
		MenuItem menuItem = null;
		try{
			String GET_TOP_MENU = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id=? AND language_id=? and menu_id=? and visible=1";
			Query qry = em.createNativeQuery(GET_TOP_MENU);
			qry.setParameter(1, parent_level_id);
			qry.setParameter(2, language_id);
			qry.setParameter(3, menuid);
			
			List<Object[]> rs = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = null;
			
			int x = 0;
			MenuItem mi = null;
			for(Object[] o : rs){
				x++;
				if(x==1){
					
					topMenus = new LinkedHashMap<Integer, MenuItem>();
				}
				mi = new MenuItem();
				mi.setId(((Integer) o[0]).intValue());
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				topMenus.put(x, mi);
				
				
			}
			
			
				menuItem = getMenuById(parent_level_id);
				if(menuItem==null){
					menuItem = new MenuItem();
					menuItem.setId(parent_level_id);
				}
				menuItem.setParent_level_id(parent_level_id);
				menuItem.setLanguage_id(language_id);
				menuItem.setSub_menus(topMenus);
				menuItem.setMenu_id(menuid);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		}
		
		return menuItem;
	}
	
	

	@SuppressWarnings("unchecked")
	public MenuItem getMenuById(int menu_id) throws Exception{
		
		MenuItem mi = null;
		
		try{
		
			String GET_MENU_BY_ID = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE id=? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_BY_ID);
			qry.setParameter(1, menu_id);
			
			List<Object[]> obj = qry.getResultList();
			
			LinkedHashMap<Integer, MenuItem> topMenus = getSubMenus(menu_id);
			
			for(Object[] o : obj){
				mi = new MenuItem();
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
			}
			
			if(mi!=null)
				mi.setSub_menus(topMenus);
		
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			throw e;
		
		}finally{
		
		}
		
		return mi;
	}
	
	
	

	@SuppressWarnings("unchecked")
	public LinkedHashMap<Integer, MenuItem> getSubMenus(int parent_level_id_fk) throws Exception{
		
		LinkedHashMap<Integer,MenuItem> items = null;
		
		try{
			
			String GET_MENU_ITEMS = "SELECT * FROM `"+CelcomImpl.database+"`.`smsmenu_levels` WHERE parent_level_id = ? AND visible=1";
			Query qry = em.createNativeQuery(GET_MENU_ITEMS);
			qry.setParameter(1, parent_level_id_fk);
			
			List<Object[]> obj = qry.getResultList();
			
			int post = 0;
			
			MenuItem mi = null;
			
			for(Object[] o :obj ){
				post++;
				
				if(post==1)
					items = new LinkedHashMap<Integer,MenuItem>();
				
				mi = new MenuItem();
				
				mi.setId((Integer) o[0]);
				mi.setName((String) o[1]);
				mi.setLanguage_id((Integer) o[2]);
				mi.setParent_level_id((Integer) o[3]);
				mi.setMenu_id((Integer) o[4]);
				mi.setService_id((Integer) o[5]);
				mi.setVisible(((Boolean) o[6]) );
				items.put(post,mi);
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
		}
		
		return items;
	}
	
	
	
	public String generateNextTxId(){
		try {
			Thread.sleep(3);
		} catch (Exception e) {
			logger.warn("\n\t\t::"+e.getMessage());
		}
		return String.valueOf(System.nanoTime());
	}




	

}
