package com.pixelandtag.dating.entities;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pixelandtag.api.GenericServiceProcessor;
import com.pixelandtag.cmp.ejb.DatingServiceI;
import com.pixelandtag.cmp.ejb.ProfileCompletionReminderLogEJBI;
import com.pixelandtag.cmp.ejb.api.sms.MTCreatorEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSMSServiceEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OpcoSenderProfileEJBI;
import com.pixelandtag.cmp.ejb.api.sms.OperatorCountryRulesEJBI;
import com.pixelandtag.cmp.ejb.api.sms.QueueProcessorEJBI;
import com.pixelandtag.cmp.ejb.sequences.TimeStampSequenceEJBI;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI;
import com.pixelandtag.serviceprocessors.sms.DatingMessages;
import com.pixelandtag.util.FileUtils;


public class ProfileQuestionsPrompter {
	
	public Logger logger = Logger.getLogger(getClass());
	private  Context context = null;
	private Properties log4J;
	private Properties properties;
	private DatingServiceI datingserviceEJB = null;
	private TimezoneConverterI timezoneconverterEJB;
	private BigInteger records_per_run = BigInteger.valueOf(10000);
	private MTCreatorEJBI mtcreatorEJB;
	private ProfileCompletionReminderLogEJBI profilecompletionreminderLoggerEJB;
	private Long serviceid = -1L;
	
	
	private void initialize()  {
		
		try{
			
			log4J = FileUtils.getPropertyFile("log4j.dating.properties");
			properties= FileUtils.getPropertyFile("dating.properties");
			
			if(log4J!=null)
				PropertyConfigurator.configure(log4J);
			else
				BasicConfigurator.configure();
			
			if(properties!=null){
				try{
					records_per_run = BigInteger.valueOf(Integer.valueOf(properties.getProperty("records_per_run")));
				}catch(Exception exp){
					logger.error(exp.getMessage() , exp);
				}
				
				try{
					serviceid = Long.valueOf(properties.getProperty("serviceid"));
				}catch(Exception exp){
					logger.error(exp.getMessage(), exp);
				}
			}
			
			
			String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";;
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
			props.put(Context.PROVIDER_URL, "remote://localhost:4447");
			props.put(Context.SECURITY_PRINCIPAL, "testuser");
			props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
			props.put("jboss.naming.client.ejb.context", true);
			context = new InitialContext(props);
			datingserviceEJB = (DatingServiceI) context.lookup("cmp/DatingServiceBean!com.pixelandtag.cmp.ejb.DatingServiceI");
			profilecompletionreminderLoggerEJB = (ProfileCompletionReminderLogEJBI) context.lookup("cmp/ProfileCompletionReminderLogEJBImpl!com.pixelandtag.cmp.ejb.ProfileCompletionReminderLogEJBI");
			timezoneconverterEJB = (TimezoneConverterI) context.lookup("cmp/TimezoneConverterEJB!com.pixelandtag.cmp.ejb.timezone.TimezoneConverterI");
			mtcreatorEJB = (MTCreatorEJBI) context.lookup("cmp/MTCreatorEJBImpl!com.pixelandtag.cmp.ejb.api.sms.MTCreatorEJBI");
			logger.info("Successfully initialized EJBs..");
		
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
		}

	}
	
	SimpleDateFormat formatDayOfMonth  = new SimpleDateFormat("d");
	
	public String convertToPrettyFormat(Date date){
		int day = Integer.parseInt(formatDayOfMonth.format(date));
		String suff  = getDayNumberSuffix(day);
		DateFormat prettier_df = new SimpleDateFormat("d'"+suff+"' E MMM YYYY h:mm a ");
	    return prettier_df.format(date);
	}

	public static String getDayNumberSuffix(int day) {
	    if (day >= 11 && day <= 13) {
	        return "th";
	    }
	    switch (day % 10) {
	    case 1:
	        return "st";
	    case 2:
	        return "nd";
	    case 3:
	        return "rd";
	    default:
	        return "th";
	    }
	}
	private void sendReminders() {
		
		BigInteger count = datingserviceEJB.countIncompleteProfiles();
		
		BigInteger total_girls = datingserviceEJB.countAllProfiles(Gender.FEMALE);
		BigInteger total_boys = datingserviceEJB.countAllProfiles(Gender.MALE); 
		
		BigInteger start = BigInteger.ZERO;

		BigInteger remaining = count;
		
		while(remaining.compareTo(BigInteger.ZERO)>=0){
			
			try{
				
				logger.info(start+","+records_per_run);
				List<PersonDatingProfile> profiles = datingserviceEJB.listIncompleteProfiles(start,records_per_run);
				logger.info("profiles.size():: "+profiles.size());
				resumeQuestions(profiles,total_girls, total_boys);
				start = records_per_run.add(start);
				remaining = count.subtract(start);
				
			}catch(Exception exp){
				logger.error(exp.getMessage(), exp);
				exp.printStackTrace();
				break;
			}
		}
		
		
	}
	
	private void resumeQuestions(List<PersonDatingProfile> profiles,BigInteger total_girls,BigInteger total_boys) {
		
		for(PersonDatingProfile profile : profiles){
			try{
				
				Thread.sleep(1000);//Sleep for a second
				Person person = profile.getPerson();
				if(person.getOpco()==null)
					continue;
				ProfileQuestion previousQuestion = datingserviceEJB.getPreviousQuestion(profile.getId());
				QuestionLog question_log = datingserviceEJB.getLastQuestionLog(profile.getId());
				
				String username = profile.getUsername();
				
				if(username.equals(person.getMsisdn()))
					username = "";
				
				logger.info("username == "+username);
				
				BigInteger potentialMates = total_girls.add(total_boys);
				DatingMessages datingmessage = DatingMessages.REMINDER_COMPLETE_QUESTIONS;
				if(profile.getGender()!=null){
					datingmessage = profile.getGender()==Gender.FEMALE ? DatingMessages.REMINDER_COMPLETE_QUESTIONS_FEMALE: DatingMessages.REMINDER_COMPLETE_QUESTIONS_MALE;
					potentialMates = profile.getGender()==Gender.FEMALE ? total_boys : total_girls;
				}
				
				
				String message  = datingserviceEJB.getMessage(datingmessage,profile.getLanguage_id(), person.getOpco().getId());
				logger.info(" question_log : "+question_log);
				logger.info(" profile : "+profile);
				logger.info(" profile.getCreationDate() : "+profile.getCreationDate());
				Date lastseen = (question_log!=null ? question_log.getTimeStamp() : profile.getCreationDate());
				logger.info(" lastseen : "+lastseen);
				logger.info(" timezoneconverterEJB : "+timezoneconverterEJB);
				String prettyTime = timezoneconverterEJB==null ? convertToPrettyFormat(lastseen) : timezoneconverterEJB.convertToPrettyFormat( lastseen );
				
				message = message.replaceAll(GenericServiceProcessor.USERNAME_TAG, Matcher.quoteReplacement(username));
				message = message.replaceAll(GenericServiceProcessor.POTENTIAL_MATES_COUNT_TAG, Matcher.quoteReplacement(potentialMates.toString()));
				message = message.replaceAll(GenericServiceProcessor.LAST_QUESTION_DATE_TAG, Matcher.quoteReplacement(prettyTime));
				//You're missing out <USERNAME>! There are <POTENTIAL_MATES_COUNT> single ladies here waiting to chat but your profile is incomplete. 
				//Please complete the following questions sent to you on <LAST_QUESTION_DATE>.
				logger.info(" MSG:: "+message);
				mtcreatorEJB.sendMT(message,serviceid, person.getMsisdn(), person.getOpco(),0);
				
				String question = null;
				
				if(previousQuestion!=null){
					question = previousQuestion.getQuestion();
				}else{
					question = datingserviceEJB.startProfileQuestions(person.getMsisdn(), person);
				}
				
				question = question.replaceAll(GenericServiceProcessor.USERNAME_TAG, Matcher.quoteReplacement(username));
				
				logger.info((previousQuestion!=null ? "PREVIOUS ": "NEW") +" QUESTION ::: "+question + " msisdn : "+person.getMsisdn());
			
				mtcreatorEJB.sendMT(question,serviceid, person.getMsisdn(), person.getOpco(),5);
				
				profilecompletionreminderLoggerEJB.log(profile);
				
				
				
			}catch(Exception exp){
				logger.error(exp.getMessage(), exp);
			}
		}
		
	}

	private void cleanup() {
		try {
			if(context!=null)
				context.close();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	public static void main(String[] args) {
		
		ProfileQuestionsPrompter propter = new ProfileQuestionsPrompter();
		propter.initialize();
		propter.sendReminders();
		propter.cleanup();
	
	}

	

	

}
