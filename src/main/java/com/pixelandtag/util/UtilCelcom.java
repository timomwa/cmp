package com.pixelandtag.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.pixelandtag.entities.MTsms;
import com.pixelandtag.exceptions.NoSettingException;
import com.pixelandtag.web.beans.MessageType;

public class UtilCelcom {

	final static String DB = "cmp";//HTTPMTSenderApp.props.getProperty("DATABASE");

	private static final String INMOBIA = "IIIIIIIIIIIIIIIIIII".replaceAll("I", "0");

	public static final int DEFAULT_LANGUAGE = 1;

	public static Logger logger = Logger.getLogger(UtilCelcom.class);

	private static int DEFAULT_LANGUAGE_ID = 1;

	public UtilCelcom() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public static String getCountryName(Connection con, int id)
			throws Exception {

		try {
			PreparedStatement pstmt = con.prepareStatement("SELECT name FROM "
					+ DB + ".country WHERE id = ?");
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			String country = rs.next() ? rs.getString("name") : "";
			rs.close();
			pstmt.close();
			return country;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}

	}

	public static void log(Connection con, int points, String msisdn,
			int correct, int question_id, String keyword, String username)
			throws Exception {
		try {
			PreparedStatement pstmt = con
					.prepareStatement("INSERT INTO "
							+ DB
							+ ".mamas_trivia_log (msisdn, correct,question_id,answer,name,points) VALUES (?,?,?,?,?,?)");
			int i = 0;
			pstmt.setString(++i, msisdn);
			pstmt.setInt(++i, correct);
			pstmt.setInt(++i, question_id);
			pstmt.setString(++i, keyword);
			pstmt.setString(++i, username);
			pstmt.setInt(++i, points);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static void sendMessage(Connection con, String msisdn, int smppid,
			String shortcode, String message) throws Exception {
		try {
			if (message.equalsIgnoreCase("error"))
				return;
			PreparedStatement pstmt = con
					.prepareStatement("INSERT INTO vas.SMPPToSend (Receiver,Timestamp,Priority,SMPPID,Type,SMS, sendfrom) VALUES (?,NOW(),0,?,?,?,?)");
			pstmt.setString(1, msisdn);
			pstmt.setInt(2, smppid);
			pstmt.setInt(3, 15);
			pstmt.setString(4, message);
			pstmt.setString(5, shortcode);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static String getUserName(String msisdn, Connection con)
			throws Exception {
		try {
			String name = null;
			PreparedStatement pstmt = con.prepareStatement("SELECT name FROM "
					+ DB + ".trivia_user WHERE msisdn = ?");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				name = rs.getString("name");
			}
			rs.close();
			pstmt.close();
			return name;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}
	
	/**
	 * request from UG.. teasers for each day..
	 * @param name
	 * @param msisdn
	 * @param con
	 * @param language
	 * @return
	 * @throws Exception
	 */
	private static String getMorningTeaser(String name, String msisdn,
			Connection con,
 int language) throws Exception {
		String message = null;
		try {
			
			PreparedStatement pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".messages WHERE language_id = ? AND name=concat('MORNING_TEASER_',day(now()))  LIMIT 1");
			pstmt.setInt(1, language);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				message = rs.getString("message");
			}

			rs.close();
			pstmt.close();

			if (message.indexOf("<DAILY_POINTS>") >= 0)
				message = message.replaceAll("<DAILY_POINTS>",
						Integer.toString(getDailyPoints(msisdn, con)));
			if (message.indexOf("<TOTAL_POINTS>") >= 0)
				message = message.replaceAll("<TOTAL_POINTS>",
						Integer.toString(getPoints(msisdn, con)));
			if (message.indexOf("<POINTS>") >= 0)
				message = message.replaceAll("<POINTS>",
						Integer.toString(getDailyPoints(msisdn, con)));
			try {
				if (message.indexOf("<NAME>") >= 0
						|| message.indexOf("<name>") >= 0) {
					String username = getUserName(msisdn, con);
					message = message.replaceAll("<NAME>",
							username == null ? "" : username);
					message = message.replaceAll("<name>",
							username == null ? "" : username);
					message = message.replaceAll("<nom>", username == null ? ""
							: username);
				}
			} catch (Exception e) {
			}

			logger.debug("looking for :[" + name + "], found [" + message + "]");
			
			return message;

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			return message;

		}
	}

	public static String getMessage(String name, String msisdn, Connection con,
			int language) throws Exception {
		try {
			
			/*
			 * if (name.equals("MORNING_TEASER")) if (getSetting("operator",
			 * con).equals("UG")) { String morningTeaser =
			 * getMorningTeaser(name, msisdn, con, language); if (morningTeaser
			 * != null) {
			 * logger.info(">>>>>>>>>>>SpecificDatemorningTeaser>>>>>> " +
			 * morningTeaser); return morningTeaser; } }
			 */
			String message = "Error";
			PreparedStatement pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".messages WHERE language_id = ? AND name = ? ORDER BY RAND() LIMIT 1");
			pstmt.setInt(1, language);
			pstmt.setString(2, name);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				message = rs.getString("message");
			}

			rs.close();
			pstmt.close();

			if (message.indexOf("<DAILY_POINTS>") >= 0)
				message = message.replaceAll("<DAILY_POINTS>",
						Integer.toString(getDailyPoints(msisdn, con)));
			if (message.indexOf("<TOTAL_POINTS>") >= 0)
				message = message.replaceAll("<TOTAL_POINTS>",
						Integer.toString(getPoints(msisdn, con)));
			if (message.indexOf("<POINTS>") >= 0)
				message = message.replaceAll("<POINTS>",
						Integer.toString(getDailyPoints(msisdn, con)));
			try {
				if (message.indexOf("<NAME>") >= 0
						|| message.indexOf("<name>") >= 0) {
					String username = getUserName(msisdn, con);
					message = message.replaceAll("<NAME>",
							username == null ? "" : username);
					message = message.replaceAll("<name>",
							username == null ? "" : username);
					message = message.replaceAll("<nom>", username == null ? ""
							: username);
				}
			} catch (Exception e) {
			}

			logger.debug("looking for :[" + name + "], found [" + message + "]");
			return message;

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}
	
	
	
	
	public static String getMessage(MessageType messageType, Connection con,
			int language) throws Exception {
		return getMessage(messageType.toString(), con, language);
	}
	
	public static String getMessage(String key, Connection con,
			int language_id) throws Exception {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			String message = "Error 130 :  Translation text not found. language_id = "+language_id+" key = "+key;
			pstmt = con
					.prepareStatement("SELECT * FROM "+DB+".message WHERE language_id = ? AND `key` = ? ORDER BY RAND() LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setString(2, key);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				message = rs.getString("message");
			}


			logger.debug("looking for :[" + key + "], found [" + message + "]");
			
			return message;

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}finally{

			try{rs.close();}catch(Exception e){}
			try{pstmt.close();}catch(Exception e){}
		}
	}

	public static int getPoints(String msisdn, Connection con) throws Exception {
		try {
			int points = 0;
			PreparedStatement pstmt = con.prepareStatement(""
					+ "SELECT SUM(points) points " + "FROM " + DB
					+ ".mamas_trivia_log " + "WHERE msisdn = ? "
					+ "GROUP BY msisdn");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				points = rs.getInt("points");
			}
			rs.close();
			pstmt.close();

			return points;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static int getDailyPoints(String msisdn, Connection con)
			throws Exception {
		try {
			int points = 0;
			PreparedStatement pstmt = con.prepareStatement(""
					+ "SELECT SUM(points) points " + "FROM " + DB
					+ ".mamas_trivia_log "
					+ "WHERE msisdn = ? AND timestamp >= CURDATE() "
					+ "GROUP BY msisdn");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				points = rs.getInt("points");
			}
			rs.close();
			pstmt.close();

			return points;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static String getSetting(String name, Connection con)
			throws Exception {
		try {
			String setting = null;
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
					+ DB + ".settings WHERE name = ?");
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				setting = rs.getString("value");
			}
			rs.close();
			pstmt.close();
			return setting;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}

	public static void setSetting(String name, String value, Connection con)
			throws Exception {
		try {
			PreparedStatement pstmt = con
					.prepareStatement("INSERT INTO "
							+ DB
							+ ".settings (name,value) VALUES (?,?) ON DUPLICATE KEY UPDATE value = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, value);
			pstmt.setString(3, value);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static int getLastQuestionID(String msisdn, Connection con)
			throws Exception {
		try {
			int question_id = -1;
			PreparedStatement pstmt = con.prepareStatement(""
					+ "SELECT s.question_id question_id " + "FROM " + DB
					+ ".mamas_trivia_questions_send s " + "WHERE msisdn = ?");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				question_id = rs.getInt("question_id");
			}
			rs.close();
			pstmt.close();

			return question_id;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static boolean isPointRushActive(Connection con) throws Exception {
		try {
			boolean isPointRushActive = false;
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
					+ DB + ".point_rush WHERE NOW() > start AND NOW() < end");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				isPointRushActive = true;
			rs.close();
			pstmt.close();
			return isPointRushActive;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static String getQuestion(int id, Connection con) throws Exception {
		try {
			String question = "";
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
					+ DB + ".mamas_trivia_question WHERE id = ?");
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				question = rs.getString("question");
			}
			rs.close();
			pstmt.close();
			return question;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	private static boolean mustBeOne8(Connection con, int difficulty,
			String msisdn, int language_id) throws Exception {
		try {
			// Check if all he has received the last 4 amount of times has been
			// general questions.
			boolean allGeneral = true;
			PreparedStatement pstmt = con
					.prepareStatement("SELECT question_origin FROM "
							+ DB
							+ ".mamas_trivia_log l, "
							+ DB
							+ ".mamas_trivia_question q WHERE l.msisdn = ? AND l.question_id = q.id ORDER BY timestamp DESC LIMIT 4");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (rs.getInt("question_origin") == 3)
					allGeneral = false;
			}
			rs.close();
			pstmt.close();

			if (!allGeneral)
				return false;

			// Check if theres any mamas back in his category.
			pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".mamas_trivia_question q WHERE language_id = ? AND question_origin = 3 AND difficulty = ? AND id NOT IN (SELECT question_id FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ?) LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setInt(2, difficulty);
			pstmt.setString(3, msisdn);
			rs = pstmt.executeQuery();
			boolean mustBeMamas = rs.next();
			rs.close();
			pstmt.close();

			return mustBeMamas;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	private static Map<String, Integer> adjustMap(Map<String, Integer> map,
			String key) {
		int curval = map.get(key);
		map.put(key, (curval + 1));
		return map;
	}

	private static String getMinValue(Map<String, Integer> map) {

		Entry<String, Integer> min = null;
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (min == null || min.getValue() > entry.getValue()) {
				min = entry;
			}
		}

		return min.getKey();
	}

	private static String getMaxValue(Map<String, Integer> map) {

		Entry<String, Integer> min = null;
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (min == null || min.getValue() < entry.getValue()) {
				min = entry;
			}
		}

		return min.getKey();
	}

	private static int getOneNotIn(Map<String, Integer> map, int val1, int val2) {
		String toRemove = "";
		int valNotIn = -1;
		int n = 0;
		for (Entry<String, Integer> entry : map.entrySet()) {
			n++;
			if (entry.getValue() == val1) {
				toRemove += entry.getKey() + (n == map.size() ? "" : ",");
			}
			if (entry.getValue() == val2) {
				toRemove += entry.getKey() + (n == map.size() ? "" : ",");
			}
		}
		String[] removables = toRemove.split("[,]");
		int rl = removables.length;
		for (int k = 0; k < rl; k++) {
			map.remove(removables[k]);
		}
		for (Entry<String, Integer> entry : map.entrySet()) {
			valNotIn = entry.getValue();
		}
		return valNotIn;
	}

	// Feature modified 2011-11-23 on request by Bradly, Edited by Timothy
	// Mwangi
	private static int getQuestionOrigin(Connection con, int difficulty,
			String msisdn, int language_id) throws Exception {
		try {
			Map<String, Integer> questionsDistributed = new HashMap<String, Integer>();

			questionsDistributed.put("general", 0);
			questionsDistributed.put("manu", 0);
			questionsDistributed.put("one8", 0);

			// Check if all he has received the last 4 amount of times has been
			// general questions.
			boolean allGeneral = true;
			PreparedStatement pstmt = con
					.prepareStatement("SELECT question_origin FROM "
							+ DB
							+ ".mamas_trivia_log l, "
							+ DB
							+ ".mamas_trivia_question q WHERE l.msisdn = ? AND l.question_id = q.id ORDER BY timestamp DESC LIMIT 6");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {

				int prev_question_origin = rs.getInt("question_origin");

				if (prev_question_origin != -1) {

					if (prev_question_origin == 1) {

						questionsDistributed = adjustMap(questionsDistributed,
								"general");

					} else if (prev_question_origin == 2) {

						questionsDistributed = adjustMap(questionsDistributed,
								"manu");

					} else if (prev_question_origin == 3) {

						questionsDistributed = adjustMap(questionsDistributed,
								"one8");

					}

				}

			}
			rs.close();
			pstmt.close();

			String lowCategory = getMinValue(questionsDistributed);
			String highCategory = getMaxValue(questionsDistributed);

			int question_origin = 1;

			if (lowCategory != null) {

				if (lowCategory.equals("general"))// If the general category had
													// lest questions in the
													// last 6 questions sent,
													// then we send a question
													// from the general category
													// next
					question_origin = 1;
				if (lowCategory.equals("manu"))// same logic
					question_origin = 2;
				if (lowCategory.equals("one8"))// same logic
					question_origin = 3;

			}

			int lastToPickFrom = 0;
			if (highCategory != null) {

				if (highCategory.equals("general"))// If the general category
													// had
													// most questions in the
													// last 6 questions sent,
													// then we consider that the
													// last category we would
													// chose from
					lastToPickFrom = 1;
				if (highCategory.equals("manu"))// same logic
					lastToPickFrom = 2;
				if (highCategory.equals("one8"))// same logic
					lastToPickFrom = 3;

			}

			/*
			 * if (!allGeneral) return false;
			 */
			String category = question_origin == 1 ? "GENERAL"
					: (question_origin == 2 ? "MAN_U" : "ONE8");
			// StringBuffer sb = new StringBuffer();
			// sb.append("searching a question from the ["+category+"] category. \n");
			pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".mamas_trivia_question q WHERE language_id = ? AND question_origin = ? AND difficulty = ? AND id NOT IN (SELECT question_id FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ?) LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setInt(2, question_origin);
			pstmt.setInt(3, difficulty);
			pstmt.setString(4, msisdn);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				// logger.info(sb.toString()+"found a question from the ["+category+"] category, id "+question_origin);
				return question_origin;
			}

			// sb.append("found no questions in the ["+category+"] category. \n");
			int between = getOneNotIn(questionsDistributed, question_origin,
					lastToPickFrom);
			category = between == 1 ? "GENERAL" : (between == 2 ? "MAN_U"
					: "ONE8");

			// sb.append("looking into the "+category+ " category. \n");

			// Get him the next best question
			pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".mamas_trivia_question q WHERE language_id = ? AND question_origin = ? AND difficulty = ? AND id NOT IN (SELECT question_id FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ?) LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setInt(2, between);
			pstmt.setInt(3, difficulty);
			pstmt.setString(4, msisdn);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				// logger.info(sb.toString()+"found a question from the ["+category+"] category, id "+between);
				return between;
			}

			category = lastToPickFrom == 1 ? "GENERAL"
					: (lastToPickFrom == 2 ? "MAN_U" : "ONE8");
			// sb.append("looking into the "+category+ " category. \n");

			// You can get him a question from the highest category he has got
			// questions from..
			pstmt = con
					.prepareStatement("SELECT * FROM "
							+ DB
							+ ".mamas_trivia_question q WHERE language_id = ? AND question_origin = ? AND difficulty = ? AND id NOT IN (SELECT question_id FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ?) LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setInt(2, lastToPickFrom);
			pstmt.setInt(3, difficulty);
			pstmt.setString(4, msisdn);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				// logger.info(sb.toString()+"found a question from the ["+category+"] category, id "+lastToPickFrom);
				return lastToPickFrom;
			}

			// Get him/her any other question in his language that he has not
			// answered!
			pstmt = con
					.prepareStatement("SELECT question_origin FROM "
							+ DB
							+ ".mamas_trivia_question q WHERE language_id = ?  AND id NOT IN (SELECT question_id FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ?) LIMIT 1");
			pstmt.setInt(1, language_id);
			pstmt.setString(2, msisdn);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				int cat = rs.getInt(1);
				category = cat == 1 ? "GENERAL" : (cat == 2 ? "MAN_U" : "ONE8");
				// logger.info(sb.toString()+"found a question from the ["+category+"] category, id "+cat);
				return cat;
			}

			rs.close();
			pstmt.close();

			// if all above fails, return 1 - any general question

			return 1;

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}

	}

	public static int getNewQuestionID(String msisdn, int difficulty,
			Connection con, int language_id) throws Exception {
		try {
			int id = 0;
			String order = difficulty == 3 ? "DESC" : "ASC";

			String force_easy_questions = UtilCelcom.getSetting(
					"force_easy_questions", con);
			difficulty = force_easy_questions != null
					&& force_easy_questions.equalsIgnoreCase("TRUE") ? 1
					: difficulty;

			// boolean mustBeMamas = mustBeMamas(con, difficulty,
			// msisdn,language_id);
			int question_origin = getQuestionOrigin(con, difficulty, msisdn,
					language_id);
			StringBuffer sb = new StringBuffer();
			sb.append("0");
			PreparedStatement pstmt = con
					.prepareStatement("SELECT question_id FROM " + DB
							+ ".mamas_trivia_log WHERE msisdn = ?");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				sb.append("," + rs.getString("question_id"));
			}
			rs.close();
			pstmt.close();
			pstmt = con.prepareStatement(""
					+ "SELECT id, order_index, difficulty " + "FROM ( "
					+ "(SELECT id, 0 as order_index, difficulty FROM "
					+ DB
					+ ".mamas_trivia_question q WHERE "
					+ "question_origin = "
					+ question_origin
					+ " AND "
					+ " language_id = "
					+ language_id
					+ " AND difficulty = ? AND id NOT IN ("
					+ sb.toString()
					+ ") ORDER BY RAND() LIMIT 1) "
					+ "UNION ALL "
					+ "(SELECT id, 1 as order_index, difficulty FROM "
					+ DB
					+ ".mamas_trivia_question q WHERE "
					+ "question_origin = "
					+ question_origin
					+ " AND "
					+ " language_id = "
					+ language_id
					+ " AND difficulty != ? AND id NOT IN ("
					+ sb.toString()
					+ ") ORDER BY RAND() LIMIT 1) "
					+ ") AS t "
					+ "ORDER BY order_index ASC, difficulty " + order);
			int i = 0;
			pstmt.setInt(++i, difficulty);
			pstmt.setInt(++i, difficulty);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				id = rs.getInt("id");
			}
			rs.close();
			pstmt.close();

			return id;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	/*
	 * public static int getNewQuestionID(String msisdn, int difficulty,
	 * Connection con, int language_id) throws Exception{ int id = 0; String
	 * order = difficulty == 3 ? "DESC" : "ASC";
	 * 
	 * String force_easy_questions = Util.getSetting("force_easy_questions",
	 * con); difficulty = force_easy_questions != null &&
	 * force_easy_questions.equalsIgnoreCase("TRUE") ? 1 : difficulty;
	 * 
	 * boolean mustBeMamas = mustBeMamas(con, difficulty, msisdn,language_id);
	 * StringBuffer sb = new StringBuffer(); sb.append("0"); PreparedStatement
	 * pstmt = con.prepareStatement("SELECT question_id FROM "+DB+
	 * ".mamas_trivia_log WHERE msisdn = ?"); pstmt.setString(1, msisdn);
	 * ResultSet rs = pstmt.executeQuery(); while (rs.next()) { sb.append("," +
	 * rs.getString("question_id")); } rs.close(); pstmt.close(); pstmt =
	 * con.prepareStatement("" + "SELECT id, order_index, difficulty " +
	 * "FROM ( " + "(SELECT id, 0 as order_index, difficulty FROM "+DB+
	 * ".mamas_trivia_question q WHERE "+(mustBeMamas?" mamas = 1 AND " :
	 * "")+" language_id = "
	 * +language_id+" AND difficulty = ? AND id NOT IN ("+sb
	 * .toString()+") ORDER BY RAND() LIMIT 1) " + "UNION ALL " +
	 * "(SELECT id, 1 as order_index, difficulty FROM "
	 * +DB+".mamas_trivia_question q WHERE "+(mustBeMamas?" mamas = 1 AND " :
	 * "")
	 * +" language_id = "+language_id+" AND difficulty != ? AND id NOT IN ("+sb
	 * .toString()+") ORDER BY RAND() LIMIT 1) " + ") AS t " +
	 * "ORDER BY order_index ASC, difficulty " + order); int i = 0;
	 * pstmt.setInt(++i,difficulty); pstmt.setInt(++i,difficulty); rs =
	 * pstmt.executeQuery(); if (rs.next()) { id = rs.getInt("id"); }
	 * rs.close(); pstmt.close();
	 * 
	 * return id; }
	 */

	public static int getDifficulty(String msisdn, boolean correct,
			Connection con) throws Exception {
		try {
			int difficulty = 2;
			PreparedStatement pstmt = con.prepareStatement(""
					+ "SELECT difficulty FROM " + DB + ".mamas_trivia_log l "
					+ "LEFT JOIN " + DB
					+ ".mamas_trivia_question q ON (l.question_id = q.id) "
					+ "WHERE msisdn = ? ORDER BY l.id DESC LIMIT 1");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				difficulty = rs.getInt("difficulty");
				if (difficulty < 3 && correct)
					difficulty++;
				else if (difficulty > 1 && !correct)
					difficulty--;
			}
			rs.close();
			pstmt.close();
			return difficulty;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static boolean wasLastQuestionCorrect(String msisdn, Connection con)
			throws Exception {
		try {
			boolean correct = false;
			PreparedStatement pstmt = con
					.prepareStatement("SELECT correct FROM "
							+ DB
							+ ".mamas_trivia_log WHERE msisdn = ? ORDER BY id DESC LIMIT 1");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				correct = rs.getBoolean("correct");
			rs.close();
			pstmt.close();
			return correct;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	// public static int getNewQuestionID(String msisdn,Connection con) throws
	// Exception{
	// int id = 0;
	/*
	 * Ordered changed by Bradley and Titus 2010-09-23 PreparedStatement pstmt =
	 * con.prepareStatement("" + "SELECT id FROM ( " +
	 * "(SELECT id,mamas FROM "+DB
	 * +".mamas_trivia_question q WHERE id NOT IN (SELECT question_id FROM "+DB+
	 * ".mamas_trivia_log WHERE msisdn = ?) AND mamas = 1 ORDER BY RAND() LIMIT 1) "
	 * + "UNION ALL " + "(SELECT id,mamas FROM "+DB+
	 * ".mamas_trivia_question q WHERE id NOT IN (SELECT question_id FROM "+DB+
	 * ".mamas_trivia_log WHERE msisdn = ?) AND mamas = 0 ORDER BY RAND() LIMIT 1) "
	 * + ") AS t ORDER BY mamas DESC LIMIT 1");
	 */
	/*
	 * PreparedStatement pstmt = con.prepareStatement("SELECT id FROM "+DB+
	 * ".mamas_trivia_question q WHERE id NOT IN (SELECT question_id FROM "
	 * +DB+".mamas_trivia_log WHERE msisdn = ?) ORDER BY RAND() LIMIT 1 ");
	 * pstmt.setString(1,msisdn); //pstmt.setString(2,msisdn); ResultSet rs =
	 * pstmt.executeQuery(); if (rs.next()) { id = rs.getInt("id"); }
	 * rs.close(); pstmt.close();
	 * 
	 * return id; }
	 */

	public static int getLanguageID(String msisdn, Connection con)
			throws Exception {
		try {
			if (getSetting("ENABLE_SECONDARY_LANGUAGE", con) == null
					|| !getSetting("ENABLE_SECONDARY_LANGUAGE", con)
							.equalsIgnoreCase("TRUE"))
				return 1;
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
					+ DB + ".trivia_user WHERE msisdn =?");
			pstmt.setString(1, msisdn);
			ResultSet rs = pstmt.executeQuery();
			int language_id = rs.next() ? rs.getInt("language_id") : 1;
			rs.close();
			pstmt.close();
			return language_id;
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
	}

	public static void setLanguageID(String msisdn, int language_id,
			Connection con) throws Exception {
		try {
			PreparedStatement pstmt = con.prepareStatement("UPDATE " + DB
					+ ".trivia_user SET language_id = " + language_id
					+ " WHERE msisdn = ?");
			pstmt.setString(1, msisdn);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);

			throw new Exception(e.getMessage());

		}
		logger.info("Users language changed");
	}

	/**
	 * @author Timothy Mwangi Gikonyo Checks if a String has any other
	 *         characters other than a-aA-Z and 0-9
	 * 
	 *         Thhis avoids people sending characters that we are not interested
	 *         in...
	 * @param msg
	 * @return
	 */
	public static boolean isValidEntry(String msg) {

		char[] charz = msg.toCharArray();

		int charsize = charz.length;

		String ch = "";

		Pattern alphabet = Pattern.compile("[a-zA-Z]");
		Pattern numero = Pattern.compile("[0-9]");
		Pattern dot = Pattern.compile("[.]");
		Pattern whitespace = Pattern.compile("[\\s]");
		Pattern returnC = Pattern.compile("[\\r]");
		Pattern carr = Pattern.compile("[\\n]");
		Pattern tab = Pattern.compile("[\\t]");

		Matcher m = null;
		Matcher m2 = null;
		Matcher m3 = null;
		Matcher m4 = null;
		Matcher m5 = null;
		Matcher m6 = null;
		Matcher m7 = null;

		boolean nonalphanumeric_found = false;

		for (int i = 0; i < charsize; i++) {

			ch = String.valueOf(charz[i]);

			m = alphabet.matcher(ch);
			m2 = numero.matcher(ch);
			m3 = dot.matcher(ch);
			m4 = whitespace.matcher(ch);
			m5 = returnC.matcher(ch);
			m6 = carr.matcher(ch);
			m7 = tab.matcher(ch);

			if (((!m.find() && !m2.find() && !m4.find() && !m3.find())
					|| m6.find() || m5.find() || m7.find())) {

				nonalphanumeric_found = true;

			}

		}

		return !nonalphanumeric_found;
	}

	public static void toggleLanguage(String MSISDN, Connection con)
			throws Exception {
		// convert 1 to 2 and 2 to 1
		int newLanguageID = UtilCelcom.getLanguageID(MSISDN, con) ^ 3;
		try {
			PreparedStatement pstmt = con.prepareStatement("UPDATE " + DB
					+ ".trivia_user SET language_id = " + newLanguageID
					+ " WHERE msisdn = ?");
			pstmt.setString(1, MSISDN);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {

			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	// appended to point rush to remind user of last question
	public static String getLastQuestion(String msisdn, Connection con)
			throws Exception {
		String lastquestion = "";
		try {
			int lastquestionID = getLastQuestionID(msisdn, con);
			String query = "Select question from " + DB
					+ ".mamas_trivia_question where id=?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, lastquestionID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				lastquestion = rs.getString("question");
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		return lastquestion;
	}

	public static boolean isHappyHourActive(Connection con) throws Exception {
		try {
			boolean isHappyHourActive = false;
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
					+ DB + ".happy_hour WHERE NOW() > start AND NOW() < end");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				isHappyHourActive = true;
			rs.close();
			pstmt.close();
			return isHappyHourActive;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	public static boolean setHappyHourPrice(Connection con) throws Exception {
		boolean success = false;
		try {
			String query = "Update vas.SMSService set price = ? where ServiceID=?";
			PreparedStatement ps = con.prepareStatement(query);
			String happyHourPrice = getSetting("HAPPY_HOUR_PRICE", con);
			String serviceID = getSetting("ServiceID", con);
			ps.setString(1, happyHourPrice);
			ps.setInt(2, Integer.parseInt(serviceID));

			if (ps.executeUpdate() == 1) {
				success = true;
			}
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public static boolean removeHappyHourPrice(Connection con) throws Exception {
		logger.debug("removing happy hour price");
		boolean success = false;
		try {
			String query = "Update vas.SMSService set price = ? where ServiceID=?";
			PreparedStatement ps = con.prepareStatement(query);
			String normalPrice = getSetting("NORMAL_PRICE", con);
			String serviceID = getSetting("ServiceID", con);
			ps.setString(1, normalPrice);
			ps.setInt(2, Integer.parseInt(serviceID));

			if (ps.executeUpdate() == 1) {
				success = true;
			}
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public static String getEasyQuestion(String msisdn, Connection con)
			throws Exception {
		String easyQuestion = null;
		try {
			int questionId = getNewQuestionID(msisdn, 1, con,
					getLanguageID(msisdn, con));
			easyQuestion = getQuestion(questionId, con);
			String query = "INSERT INTO mamas_trivia_questions_send (msisdn,question_id) VALUES (?,?) ON DUPLICATE KEY UPDATE question_id =?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, msisdn);
			ps.setInt(2, questionId);
			ps.setInt(3, questionId);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return easyQuestion;
	}

	public static Set<String> resultSetToSet(PreparedStatement statement)
			throws SQLException {
		ResultSet results = statement.executeQuery();
		try {
			Set<String> strings = new HashSet<String>();
			while (results.next()) {
				strings.add(results.getString(1));
			}
			return strings;
		} finally {
			statement.close();
		}
	}

	public static Queue<String> resultSetToQueue(PreparedStatement statement)
			throws SQLException {
		try {
			ResultSet rs = statement.executeQuery();
			Queue<String> strings = new LinkedList<String>();
			while (rs.next()) {
				strings.offer(rs.getString(1));
			}
			logger.debug("queue size: " + strings.size());
			return strings;
		} finally {
			statement.close();
		}
	}
	
	
	
	

	public static String getServiceMetaData(Connection conn, int serviceid,
			String meta_field){
		
		String value = "-1";
		
		PreparedStatement pstmt = null;
		
		ResultSet rs = null;
		
		try{
		
			pstmt = conn.prepareStatement("select meta_value from `"+DB+"`.`sms_service_metadata` WHERE sms_service_id_fk=? AND meta_field=?");
			pstmt.setInt(1, serviceid);
			pstmt.setString(2, meta_field);
			rs = pstmt.executeQuery();
		
			if(rs.next()){
				
				value = rs.getString("meta_value");
				
			}else{
				throw new NoSettingException("No meta data with field name "+meta_field+", for serviceid "+serviceid);
			}
			
		}catch(NoSettingException e){
			
			logger.warn(e.getMessage());
		
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
		
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		
		return value;
	}

	
	/**
	 * Gets additional info about a keyword config, e.g tail text, unsubscription text, and subscription text
	 * @param serviceid
	 * @param conn
	 * @return
	 */
	public static Map<String, String> getAdditionalServiceInfo(int serviceid,
			Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> additionalInfo = null;
		
		
		try{
			pstmt = conn.prepareStatement("SELECT service_name,subscriptionText,unsubscriptionText,tailText_subscribed,tailText_notsubscribed FROM `"+DB+"`.`sms_service` WHERE id=?");
			pstmt.setInt(1, serviceid);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
			
				if(rs.isFirst())
					additionalInfo = new HashMap<String,String>();
				
				additionalInfo.put("subscriptionText", rs.getString("subscriptionText"));
				additionalInfo.put("unsubscriptionText", rs.getString("unsubscriptionText"));
				additionalInfo.put("tailText_subscribed", rs.getString("tailText_subscribed"));
				additionalInfo.put("tailText_notsubscribed", rs.getString("tailText_notsubscribed"));
				additionalInfo.put("service_name", rs.getString("service_name"));
			}
			
		}catch(Exception e){
			
			logger.error(e.getMessage(),e);
			
		}finally{
			
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
		
		return additionalInfo;
	}

	
	
	
	
	
	public static String generateNextTxId(){
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		return INMOBIA.substring(0, (19-timestamp.length())) + timestamp;//(String.valueOf(Long.MAX_VALUE).length()-timestamp.length())) + timestamp;
		
	}	
	
	
	
	
	
	
	    /**
	     * 
	     * @param conn - connection object. 
	     * @param shortcode - this is the shortcode , e.g 22222
	     * @param CMP_Keyword  - this is the primary keyword or CMP_A_keyword e.g IOD
	     * @param CMP_SKeyword - this is the secondary keyword mostly denoting the price of the service. e.g IOD0000
	     * @param msisdn - msisdn - the number of the subscriber
	     * @param message - the message to be sent
	     * @return true if msg has been successfully inserted into the outgoing queue for the platform to pick
	     * 
	     */
		public static boolean insertIntoHTTPToSend(Connection conn, String shortcode, String CMP_Keyword, String CMP_SKeyword, String msisdn, String message) {
			
			
				/*if(mo.getMt_Sent().trim().startsWith(RM1)){
					mo.setCMP_SKeyword(TarrifCode.RM1.getCode());//1 ringgit
				}else if(mo.getMt_Sent().trim().startsWith(RM0)){
					mo.setCMP_SKeyword(TarrifCode.RM0.getCode());//1 ringgit
				}else if(!mo.getMt_Sent().trim().startsWith(RM1.trim()) || !mo.getMt_Sent().trim().startsWith(RM0.trim())){
					mo.setMt_Sent(RM0+mo.getMt_Sent());
				}*/
				PreparedStatement pstmt = null;
				//TODO introduce sort of semaphore here if this method will be accessed by multiple threads..
				boolean success = false;
				
				try {
					
					
						pstmt = conn.prepareStatement("insert into `"+DB+"`.`httptosend`" +
						"(SMS,MSISDN,SendFrom,fromAddr,CMP_AKeyword,CMP_SKeyword,Priority,split,CMP_TxID) " +
						"VALUES(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
						////cmp_a_keyword,cmp_s_keyword
					
					
					
					pstmt.setString(1, message);
					pstmt.setString(2, msisdn);
					pstmt.setString(3, shortcode);
					pstmt.setString(4, shortcode);
					
					pstmt.setString(5, CMP_Keyword);
					
					/*if(message.startsWith("RM0"))
						pstmt.setString(6, TarrifCode.RM0.getCode());
					else if(message.startsWith("RM1"))
						pstmt.setString(6, TarrifCode.RM1.getCode());
					else
						*/
					pstmt.setString(6, CMP_SKeyword);
				
					
					pstmt.setInt(7, 1);//though useless and pointless.... not with this new version of platform. does make a difference. when retrieving messages to be sent, we order with priority now.
					
					//pstmt.setString(10, String.valueOf(mo.getCMP_Txid()));
					pstmt.setInt(8, 1);
					pstmt.setString(9,  generateNextTxId());
					
					pstmt.executeUpdate();
					
					success = true;
				
				} catch (SQLException e) {
					
					logger.error(e.getMessage(),e);
					
				}finally{
					
					try {
						
						if(pstmt!=null)
							pstmt.close();
					
					} catch (SQLException e) {
						
						logger.error(e.getMessage(),e);
					
					}
				
				}
				
				return success;
			
			
		}

		/**
		 * checks if an msisdn is in the zero billing list.
		 * @param msisdn
		 * @param con
		 * @return
		 */
		public static boolean isInZeroChargeList(String msisdn, Connection con) {
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			try {
				pstmt = con.prepareStatement("SELECT * FROM `"+DB+"`.`zero_billing_list` WHERE msisdn=?", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, msisdn);
				rs = pstmt.executeQuery();
				
				if(rs.next())
					return true;
			} catch (Exception e) {

				logger.error(e.getMessage(), e);
				
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){}
				try{
					pstmt.close();
				}catch(Exception e){}
				
			}
			return false;
		}

		
		/**
		 * Gets a configuration value from  the table `configuration`
		 * @param key
		 * @param conn
		 * @return
		 */
		public static String getConfigValue(String key, Connection conn) {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String configValue = null;
			
			try {
				pstmt = conn.prepareStatement("SELECT `value` FROM `"+DB+"`.`configuration` WHERE `key`=?", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, key);
				rs = pstmt.executeQuery();
				
				if(rs.next())
					configValue = rs.getString(1);
				
			} catch (Exception e) {

				logger.error(e.getMessage(), e);
				
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){}
				try{
					pstmt.close();
				}catch(Exception e){}
				
			}
			
			return configValue;
		}

		public static String getMessage(MessageType type, int i,
				Connection conn) throws Exception {
			return getMessage(type,conn,i);
		}

		
		
		/**
		 * Queues into voucher system table for later processing
		 * @param mt
		 * @param conn
		 */
		public static void queueIntoVoucherSystem(MTsms mt, Connection conn) {
			PreparedStatement pstmt = null;
			
			try {
				pstmt = conn.prepareStatement("INSERT INTO `voucher_system`.`unprocessed_participant_batch`(`msisdn`,`cmp_txid_fk`) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, mt.getMsisdn());
				pstmt.setString(2, mt.getCmp_tx_id());
				pstmt.executeUpdate();
				
			} catch (Exception e) {

				logger.error(e.getMessage(), e);
				
			}finally{
				
				try{
					pstmt.close();
				}catch(Exception e){}
				
			}
			
			
		}

		
		
		
		public static int getSubscriberLanguage(String msisdn, Connection conn) {
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			int language_id = DEFAULT_LANGUAGE_ID;
			
			try {
				pstmt = conn.prepareStatement("SELECT language_id FROM `"+DB+"`.`subscriber_profile` WHERE msisdn=?", Statement.NO_GENERATED_KEYS);
				pstmt.setString(1, msisdn);
				rs = pstmt.executeQuery();
				
				if(rs.next())
					language_id =  rs.getInt("language_id");
				else
					language_id =  DEFAULT_LANGUAGE_ID ;//default language
				
			} catch (Exception e) {

				logger.error(e.getMessage(), e);
				
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){}
				try{
					pstmt.close();
				}catch(Exception e){}
				
			}
			
			return language_id;
			
		}
	
		/**
		 * Saves the language id for a given subscriber
		 * @param msisdn
		 * @param language_id
		 * @param conn
		 */
		public static void updateProfile(String msisdn, int language_id, Connection conn) {
			
			PreparedStatement pstmt = null;
			
			try {
				pstmt = conn.prepareStatement("INSERT INTO `"+DB+"`.`subscriber_profile`(`msisdn`,`language_id`) VALUES(?,?) ON DUPLICATE KEY UPDATE `language_id`=?", Statement.NO_GENERATED_KEYS);
				pstmt.setString(1, msisdn);
				pstmt.setInt(2, language_id);
				pstmt.setInt(3, language_id);
				pstmt.executeUpdate();
				
			} catch (Exception e) {

				logger.error(e.getMessage(), e);
				
			}finally{
				
				try{
					pstmt.close();
				}catch(Exception e){}
				
			}
			
		}
	
}
