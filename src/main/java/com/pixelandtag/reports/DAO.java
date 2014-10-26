/* Copyright (c) Inmobia Mobile Technology, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Inmobia 
 * Mobile Technology. ("Confidential Information").  You shall not disclose such 
 * Confidential Information and shall use it only in accordance with the terms 
 * of the license agreement you entered into with Inmobia Mobile Technology.
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.pixelandtag.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * 
 * DAO, Data Access Object
 * 
 * @author <a href="mailto:montell@inmobia.com">Montell Tome</a>
 * @version 1.0, August 29, 2011
 * @since jdk 1.6
 */
public class DAO {
	private static final Logger LOGGER = Logger.getLogger(DAO.class);
	/**
	 * connection, connection to the database
	 */
	private Connection connection;

	/**
	 * DAO, constructor instantiates connection
	 */
	public DAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Finds and returns a list of questions given the statusId,categoryId,date
	 * and msisdn
	 * 
	 * @param <code>String</code>date, date filter records by
	 *
	 */
	public List<ReportDTO> getReport(String dateSelection) {
		

		String dateFrom = "";
		String dateTo = "";
		String sql = "SELECT "
				 +" date(v.timeStamp_awarded) as 'timeAwarded', count(*) count, v.store_id_fk, IF(s.name is null,'Unclaimed',s.name) as 'store_name',"
				 +" sum(if(v.used=1, p.value,0)) as 'prize_value_claimed', sum(if(v.used=0, p.value,0)) as 'prize_value_unclaimed', if(v.used=1, v.used, 0) as 'claimed'"
				 +" FROM "
				 +" `voucher_system`.`voucher`  v  "
				 +" LEFT JOIN `voucher_system`.`store` s on s.id = v.store_id_fk "
				 +" LEFT JOIN `voucher_system`.`prize` p ON p.id = v.prize_id_fk"
				 +" WHERE v.winning=1"
				 +" group by date(v.timeStamp_awarded), v.store_id_fk, v.used ";
		
		
		LOGGER.info("phone_voucher_num>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+dateSelection);
		
		boolean dateRange = false;
		
		if(!dateSelection.equalsIgnoreCase(""))
		if(!dateSelection.equals("Choose a Date")){
			
			dateRange = true;
			
			int length = dateSelection.length();
			LOGGER.info("LENGTH OF DATE: "+length);
			
			if(length>10){
				dateFrom = dateSelection.split("[\\s(-)\\s]")[0] + " 00:00:00";
				dateTo = dateSelection.split("[\\s(-)\\s]")[2] +" 23:59:59";
			}else{
				dateFrom = dateSelection + " 00:00:00";
				dateTo = dateSelection + " 23:59:59";
			}
			
			sql += " AND v.timeStamp_awarded between ? and ?";
			
			
		}
		
		LOGGER.info("dateFrom::::::: "+dateFrom);
		LOGGER.info("dateTo::::::: "+dateTo);
		LOGGER.info("sql::::::: "+sql);
		
		LOGGER.info("Creating report...");
		
		
		List<ReportDTO> records = new ArrayList<ReportDTO>();
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		ReportDTO report = null;
		try {
			
			preparedStatement = connection.prepareStatement(sql,Statement.NO_GENERATED_KEYS);
			
			if(dateRange){
				preparedStatement.setString(1, dateFrom);
				preparedStatement.setString(2, dateTo);
			}
			resultSet = preparedStatement.executeQuery();
			String timeAwarded = null;
			String count = null;
			String store_id_fk  = null;
			String store_name = null;
			String prize_value = null;
			String claimed = null;
			while (resultSet.next()) {
				report = new ReportDTO();
				
				report.setTimeAwarded(resultSet.getString("timeAwarded"));
				report.setCount(resultSet.getInt("count"));
				report.setStore_id_fk(resultSet.getInt("store_id_fk"));
				report.setStore_name(resultSet.getString("store_name"));
				report.setPrize_value_claimed(resultSet.getDouble("prize_value_claimed"));
				report.setPrize_value_unclaimed(resultSet.getDouble("prize_value_unclaimed"));
				report.setClaimed(resultSet.getInt("claimed"));
				records.add(report);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return records;
	}

	/**
	 * Finds and returns a list of questions given the statusId,categoryId,date
	 * and msisdn
	 * 
	 * @param <code>int</code>statusId, question's status id
	 * @param <code>int</code>categoryId, question's category id
	 * @param <code>String</code>date, date filter records by
	 * @param <code>String</code>msisdn, subscriber's mobile number
	 * @return<code>List<Question><String></code>
	 */
	public List<ReportDTO> findQuestions(int statusId, int categoryId,
			int classId, int subCategoryId, String date, String msisdn) {
		List<ReportDTO> questions = new ArrayList<ReportDTO>();
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder
				.append("SELECT q.msisdn,q.question,c.Category_Name AS category,q.timestamp AS date_received,CONCAT(u.fname,' ',u.lname) AS answered_by, ans.timeStamp AS date_answered,ans.answer");
		queryBuilder.append(" FROM `nacc`.`questions` q");
		queryBuilder
				.append(" LEFT JOIN `nacc`.`assignments` asg ON asg.questionID=q.id");
		queryBuilder
				.append(" LEFT JOIN `nacc`.`users` u ON u.id=asg.specialistID");
		queryBuilder
				.append(" LEFT JOIN `nacc`.`category` c ON q.category=c.Category_id");
		queryBuilder
				.append(" LEFT JOIN `nacc`.`answers` ans ON q.id=ans.question_id");
		queryBuilder.append(" WHERE");
		queryBuilder.append(buildDateQuery(date));
		String statusQuery = buildStatusQuery(statusId);
		if (Utils.isParamSet(statusQuery)) {
			queryBuilder.append(" AND ");
			queryBuilder.append(statusQuery);
		}
		if (categoryId > -2) {
			queryBuilder.append(" AND q.category=");
			queryBuilder.append(categoryId);
		}
		if (Utils.isParamSet(msisdn)) {
			queryBuilder.append(" AND msisdn=");
			queryBuilder.append(" '");
			queryBuilder.append(msisdn);
			queryBuilder.append("'");
		}
		queryBuilder.append(" ORDER BY q.timestamp DESC");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		ReportDTO question = null;
		try {
			preparedStatement = connection.prepareStatement(queryBuilder
					.toString());
			resultSet = preparedStatement.executeQuery();
			String category = null;
			String answer = null;
			String answeredBy = null;
			String dateAnswered = null;
			while (resultSet.next()) {
				/*question = new Question();
				question.setMsisdn(resultSet.getString("msisdn"));
				question.setQuestion(resultSet.getString("question"));
				question.setDateReceived(resultSet.getString("date_received"));
				category = resultSet.getString("category");
				if (!Utils.isParamSet(category))
					category = "Not Categorized";
				question.setCategory(category);

				answer = resultSet.getString("answer");
				if (!Utils.isParamSet(answer))
					answer = "Not Answered";
				question.setAnswer(answer);

				answeredBy = resultSet.getString("answered_by");
				if (!Utils.isParamSet(answeredBy))
					answeredBy = "N/A";
				question.setAnsweredBy(answeredBy);

				dateAnswered = resultSet.getString("date_answered");
				if (!Utils.isParamSet(dateAnswered))
					dateAnswered = "N/A";
				question.setDateAnswered(dateAnswered);
				questions.add(question);*/
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return questions;
	}

	/**
	 * Builds and returns the date query given the dateString
	 * 
	 * @param <code>String</code>dateString, date string to derive query from
	 * @return<code>String</code>
	 */
	public String buildDateQuery(String dateString) {
		StringBuilder queryBuilder = new StringBuilder();
		if (Utils.isParamSet(dateString)) {
			String dates[] = dateString.split(" - ");
			if (dates != null) {
				if (dates.length > 1) {
					queryBuilder.append(" DATE(q.timestamp) BETWEEN");
					queryBuilder.append(" '");
					queryBuilder.append(dates[0]);
					queryBuilder.append("'");
					queryBuilder.append(" AND");
					queryBuilder.append(" '");
					queryBuilder.append(dates[1]);
					queryBuilder.append("'");
				} else {
					queryBuilder.append(" DATE(q.timestamp)=");
					queryBuilder.append("'");
					queryBuilder.append(dates[0]);
					queryBuilder.append("'");
				}
			}
		} else {
			queryBuilder.append(" WHERE DATE(q.timestamp)=");
			queryBuilder.append("'");
			queryBuilder.append(Utils.getCurDate());
			queryBuilder.append("'");
		}
		return queryBuilder.toString();
	}

	/**
	 * Builds and returns the status query given the statusId
	 * 
	 * @param <code>int</code>statusId, status id to derive query from
	 * @return<code>String</code>
	 */
	public String buildStatusQuery(int statusId) {
		StringBuilder queryBuilder = new StringBuilder();
		switch (statusId) {
		case 2:
			queryBuilder.append("q.answered=");
			queryBuilder.append(1);
			break;
		case 3:
			queryBuilder.append("q.answered=");
			queryBuilder.append(0);
			break;
		case 4:
			queryBuilder.append("q.assigned=");
			queryBuilder.append(1);
			break;
		case 5:
			queryBuilder.append("q.assigned=");
			queryBuilder.append(0);
			break;
		default:
			break;
		}
		return queryBuilder.toString();
	}
}
