package com.inmobia.dynamic.dto;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class NewsDTO {
	Logger log = Logger.getLogger(NewsDTO.class);
	public static final int STATUS_OK = 0;
	public static final int STATUS_PENDING = 1;
	public static final int STATUS_DELETING = 2;
	public static final int STATUS_PENDING_MODERATION = 4;
	public static final int STATUS_PENDING_PUSH = 8;
	public static final int STATUS_IGNORED = 16;

	private Connection conn;

	private int id;
	private ContenttypeDTO contenttype;

	private Date timestamp = Calendar.getInstance().getTime();
	private int userId;

	private int dirty = 0;
	private String serviceid = null;

	private String smsBody;

	private int contentItemId;
	private String wapAuthor;
	private String wapTitle;
	private String wapAbstract;
	private String wapBody;
	private int wapImage;
	private byte[] wapImageData;
	private String wapImageType;

	private String datePattern = "EEE, d MMM yyyy HH:mm:ss Z";

	private boolean futurepost = false;

	public NewsDTO(Connection conn) {
		this.conn = conn;
	}

	public NewsDTO(ResultSet rs) throws Exception {
		this.loadFromRs(rs);
	}

	public NewsDTO(Connection conn, int id) throws Exception {
		this(conn, id, -1);
	}

	public NewsDTO(Connection conn, int id, int status) throws Exception {
		this.conn = conn;
		if (id > 0) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				rs = stmt
						.executeQuery("SELECT c.`ID`, c.`contentid`, c.`timestamp`, c.`UserID`, IFNULL(c.`Text`,'') AS `Text`, c.`contentItemId` "
								+ ", IFNULL(sti.`author`, '') AS `wapAuthor`, IFNULL(sti.`title`,'') AS `wapTitle`, IFNULL(sti.`abstract`,'') AS `wapAbstract`, IFNULL(CONVERT(sti.`text` USING utf8),'') AS `wapBody`"
								+ ", stb.`id` AS `wapImage`, CASE LEFT(stb.`tdata`,2) WHEN 0xFFD8 THEN 'jpg' WHEN 0x4749 THEN 'gif' ELSE 'unk' END AS `type` "
								+ ", c.`dirty` "
								+ "FROM `celcom`.`dynamiccontent_content` c "
								+ "LEFT JOIN `icp`.`simple_text_item` sti ON ( c.`contentItemId` = sti.`id` ) "
								+ "LEFT JOIN `icp`.`simple_text_binary` stb ON ( stb.`simple_text_id`=sti.`id` )"
								+ "WHERE c.`ID`=" + id);
				if (rs.next())
					loadFromRs(rs);
				
				if (status >= 0)
					this.dirty = status;
				
			} catch (Exception e) {
				try {
					rs.close();
				} catch (Exception ee) {
				}
				try {
					stmt.close();
				} catch (Exception ee) {
				}
				throw e;
			} finally {
				try {
					rs.close();
				} catch (Exception ee) {
				}
				try {
					stmt.close();
				} catch (Exception ee) {
				}
			}
		}
	}

	private void loadFromRs(ResultSet rs) throws Exception {
		this.id = rs.getInt("ID");
		this.dirty = rs.getInt("dirty");
		this.contenttype = new ContenttypeDTO(conn, rs.getInt("contentid"));
		this.timestamp = rs.getTimestamp("timestamp");
		this.userId = rs.getInt("UserID");
		this.smsBody = rs.getString("Text");
		this.contentItemId = rs.getInt("contentItemId");
		if (this.contentItemId > 0) {
			this.wapAuthor = rs.getString("wapAuthor");
			this.wapTitle = rs.getString("wapTitle");
			this.wapAbstract = rs.getString("wapAbstract");
			this.wapBody = rs.getString("wapBody");
			this.wapImage = rs.getInt("wapImage");
			this.wapImageType = rs.getString("type");
		}
		this.futurepost = (this.timestamp.getTime() > Calendar.getInstance()
				.getTime().getTime());
	}

	public void save() throws Exception {
		boolean docopy = false;
		ArrayList<Integer> list = new ArrayList<Integer>();
		int coll;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;

		try {
			// Check if this is dublicate content
			if (this.dirty != STATUS_DELETING && this.id == 0
					&& this.smsBody != null && this.smsBody.length() > 0) {
				pstmt = conn
						.prepareStatement(
								"SELECT c.`ID` FROM `content` c WHERE c.`contentid`=? AND c.`Text`=? AND c.`dirty` <> 8 ORDER BY c.ID DESC",
								Statement.RETURN_GENERATED_KEYS);
				pstmt.setInt(1, this.contenttype.getId());
				pstmt.setString(2, this.smsBody);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					list.add(rs.getInt("id"));
				}
				rs.close();
				pstmt.close();

				if (list.size() > 0) {
					// this.dirty=STATUS_IGNORED; //Paul Kevin: Did not send
					// same
					// content as the case in some OPCOS where content is same
					// daily
					// but needs to be sent
				}
			}

			pstmt = conn
					.prepareStatement(
							"INSERT INTO `celcom`.`dynamiccontent_content` SET `ID`=?, `contentid`=? "
									+ ", `Category`=?, `Text`=?, `headline`=? "
									+ ", `dirty`=?, `contentItemId`=?, `UserID`=?, `localeid`=?, `timestamp`=? "
									+ "ON DUPLICATE KEY UPDATE `contentid`=? "
									+ ", `Category`=?, `Text`=?, `headline`=? "
									+ ", `dirty`=?, `contentItemId`=?, `UserID`=?, `localeid`=?, `timestamp`=? ",
							Statement.RETURN_GENERATED_KEYS);
			coll = 1;
			if (this.id > 0)
				pstmt.setInt(coll++, this.id);
			else
				pstmt.setNull(coll++, 0);

			pstmt.setInt(coll++, this.contenttype.getId());
			pstmt.setString(coll++, this.contenttype.getCategory());
			pstmt.setString(coll++, this.smsBody);
			pstmt.setString(coll++, ((null != this.wapTitle && this.wapTitle
					.length() > 100) ? this.wapTitle.substring(0, 100)
					: this.wapTitle));
			pstmt.setInt(coll++, this.dirty);
			pstmt.setInt(coll++, this.contentItemId);
			pstmt.setInt(coll++, this.userId);
			pstmt.setInt(coll++, this.contenttype.getLocaleId());
			pstmt.setTimestamp(coll++,
					new java.sql.Timestamp(this.timestamp.getTime()));

			pstmt.setInt(coll++, this.contenttype.getId());
			pstmt.setString(coll++, this.contenttype.getCategory());
			pstmt.setString(coll++, this.smsBody);
			pstmt.setString(coll++, ((null != this.wapTitle && this.wapTitle
					.length() > 100) ? this.wapTitle.substring(0, 100)
					: this.wapTitle));
			pstmt.setInt(coll++, this.dirty);
			pstmt.setInt(coll++, this.contentItemId);
			pstmt.setInt(coll++, this.userId);
			pstmt.setInt(coll++, this.contenttype.getLocaleId());
			pstmt.setTimestamp(coll++,
					new java.sql.Timestamp(this.timestamp.getTime()));
			pstmt.execute();
			if (this.id == 0) {
				docopy = true;
				rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					this.id = rs.getInt(1);
					rs.close();
					pstmt.close();
				} else {
					rs.close();
					pstmt.close();
					throw new Exception(
							"No record was generated for content.ID");
				}
			} else {
				pstmt.close();
			}
			// Save the WAP if needed
			if (this.contentItemId > 0
					|| (this.wapBody != null && this.wapBody.length() > 0)) {
				pstmt = conn
						.prepareStatement(
								"INSERT INTO `icp`.`simple_text_item` SET `id`=?, `author`=? "
										+ ", `text_label_id`=?, `title`=?, `text`=?, `locale_id`=? "
										+ ", `simpletext_pool_id`=?, `abstract`=?, `uid`=?, `tdate`=NOW() "
										+ "ON DUPLICATE KEY UPDATE `author`=? "
										+ ", `text_label_id`=?, `title`=?, `text`=?, `locale_id`=? "
										+ ", `simpletext_pool_id`=?, `abstract`=?, `uid`=?, `tdate`=? ",
								Statement.RETURN_GENERATED_KEYS);
				coll = 1;
				if (this.contentItemId > 0)
					pstmt.setInt(coll++, this.contentItemId);
				else
					pstmt.setNull(coll++, 0);

				pstmt.setString(coll++, "");
				pstmt.setInt(coll++, 0);
				pstmt.setString(coll++, this.wapTitle);
				pstmt.setString(coll++, this.wapBody);
				pstmt.setInt(coll++, this.contenttype.getLocaleId());
				pstmt.setInt(coll++, this.contenttype.getPoolId());
				pstmt.setString(coll++, this.wapAbstract);
				pstmt.setInt(coll++, this.userId);

				pstmt.setString(coll++, "");
				pstmt.setInt(coll++, 0);
				pstmt.setString(coll++, this.wapTitle);
				pstmt.setString(coll++, this.wapBody);
				pstmt.setInt(coll++, this.contenttype.getLocaleId());
				pstmt.setInt(coll++, this.contenttype.getPoolId());
				pstmt.setString(coll++, this.wapAbstract);
				pstmt.setInt(coll++, this.userId);

				pstmt.setTimestamp(coll++, new java.sql.Timestamp(
						this.timestamp.getTime()));

				pstmt.execute();

				if (this.contentItemId == 0) {
					rs = pstmt.getGeneratedKeys();
					if (rs.next()) {
						this.contentItemId = rs.getInt(1);
						rs.close();
						pstmt.close();
					} else {
						rs.close();
						pstmt.close();
						throw new Exception(
								"No key was generated for icp.simple_text_item.id");
					}
					stmt = conn.createStatement();
					stmt.execute("UPDATE `celcom`.`dynamiccontent_content` SET `contentItemId` = "
							+ this.contentItemId + " WHERE `ID`=" + this.id);
					stmt.close();
				} else {
					pstmt.close();
				}

				// save the icp.simple_text_binary ?
				if (this.wapImageData != null) {
					pstmt = conn
							.prepareStatement(
									"INSERT INTO `icp`.`simple_text_binary` SET `id`=?, `simple_text_id`=?, `tdata`=? "
											+ "ON DUPLICATE KEY UPDATE `simple_text_id`=?, tdata=?",
									Statement.RETURN_GENERATED_KEYS);
					coll = 1;
					if (this.wapImage > 0)
						pstmt.setInt(coll++, this.wapImage);
					else
						pstmt.setNull(coll++, 0);
					pstmt.setInt(coll++, this.contentItemId);
					pstmt.setBytes(coll++, this.wapImageData);
					pstmt.setInt(coll++, this.contentItemId);
					pstmt.setBytes(coll++, this.wapImageData);
					pstmt.execute();

					if (this.wapImage == 0) {
						rs = pstmt.getGeneratedKeys();
						if (rs.next()) {
							this.wapImage = rs.getInt(1);
							rs.close();
							pstmt.close();
						} else {
							rs.close();
							pstmt.close();
							throw new Exception(
									"No key was generated for icp.simple_text_binary.id");
						}
					} else {
						pstmt.close();
					}
				}
			}
			// Set the content as dirty

			pstmt = conn.prepareStatement(
					"INSERT INTO `celcom`.`dynamiccontent_dirtycontent` SET "
							+ "`telcoid`=?, `contentid`=?, `dirty`=? "
							+ "ON DUPLICATE KEY UPDATE `dirty`=?",
					Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, this.contenttype.getTelcoId());
			pstmt.setInt(2, this.id);
			pstmt.setInt(3, this.dirty);
			pstmt.setInt(4, this.dirty);
			pstmt.execute();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT `telcoid` FROM `celcom`.`dynamiccontent_mirror` WHERE `contenttypeid`="
							+ this.contenttype.getId());
			while (rs.next()) {
				log.debug("marking dirty: id:" + this.id + " contentid:"
						+ this.id + " telcoid:" + rs.getInt("telcoid"));
				pstmt.clearParameters();
				pstmt.setInt(1, rs.getInt("telcoid"));
				pstmt.setInt(2, this.id);
				pstmt.setInt(3, this.dirty);
				pstmt.setInt(4, this.dirty);
				pstmt.execute();
			}
			rs.close();
			pstmt.close();
			// log.debug("marking dirty: id:"+this.id+" contentitemid:"+this.contenttype.getId()+" telcoid:"+this.contenttype.getTelcoId());
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				pstmt.close();
			} catch (Exception e) {
			}
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		// Mark a copy dirty if needed?

		try {

			if (docopy) {
				stmt = conn.createStatement();
				rs = stmt
						.executeQuery("SELECT `toid` FROM `dynamiccontent`.`copycontent` WHERE `fromid`="
								+ this.contenttype.getId());
				while (rs.next()) {
					try {
						NewsDTO n = new NewsDTO(conn, this.id, -1);
						n.setId(0);
						n.setContenttype(new ContenttypeDTO(conn, rs
								.getInt("toid")));
						n.save();
					} catch (Exception e) {
						log.error(e, e);
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {

			try {
				rs.close();
			} catch (Exception e) {
			}

			try {
				stmt.close();
			} catch (Exception e) {
			}
		}

		if (list.size() > 0) {
			// Util.notifyDuplicateContent(conn,list,this);
		}
	}

	public void delete() {
		Statement stmt = null;

		try {
			try {
				if (this.wapImage > 0) {
					stmt = conn.createStatement();
					stmt.execute("DELETE FROM `icp`.`simple_text_binary` WHERE `id`="
							+ this.wapImage);
				}
			} catch (Exception e) {
				log.warn(e, e);
			}
			try {
				stmt = conn.createStatement();
				stmt.execute("DELETE FROM `icp`.`simple_text_item` WHERE `id`="
						+ this.contentItemId);
			} catch (Exception e) {
				log.warn(e, e);
			}
			try {
				conn.createStatement().execute(
						"DELETE FROM `celcom`.`dynamiccontent_content` WHERE ID="
								+ this.id);
			} catch (Exception e) {
				log.warn(e, e);
			}
			try {
				stmt = conn.createStatement();
				stmt.execute("DELETE FROM `celcom`.`dynamiccontent_dirtycontent` WHERE contentid="
						+ this.id);
			} catch (Exception e) {
			}

		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
			
		} finally {
			
			try {
				stmt.close();
			} catch (Exception e) {
			}
			
		}
	}

	public String toXML() throws UnsupportedEncodingException {
		return "<item " + "id=\""
				+ this.id
				+ "\" "
				+ "contenttype=\""
				+ this.getContenttype().getId()
				+ "\" "
				+ "userid=\""
				+ this.userId
				+ "\" "
				+ "status=\""
				+ this.dirty
				+ "\" "
				+ "serviceid=\""
				+ this.getContenttype().getServiceId()
				+ "\" "
				+ "timestamp=\""
				+ new SimpleDateFormat(this.datePattern, Locale.ENGLISH)
						.format(this.timestamp)
				+ "\" "
				+ "wapimageid=\""
				+ this.wapImage
				+ "\" "
				+ "contentitemid=\""
				+ this.contentItemId
				+ "\">"
				+ "<smsBody><![CDATA["
				+ this.smsBody
				+ "]]></smsBody>"
				+ ((this.contentItemId > 0 && this.dirty != 2) ? "<wapTitle><![CDATA["
						+ this.wapTitle
						+ "]]></wapTitle>"
						+ "<wapAbstract><![CDATA["
						+ this.wapAbstract
						+ "]]></wapAbstract>"
						+ "<wapBody><![CDATA["
						+ this.wapBody
						+ "]]></wapBody>"
						+ "<wapAuthor><![CDATA["
						+ this.wapAuthor
						+ "]]></wapAuthor>"
						+ "<wapImage><![CDATA["
						+ this.getWapImage() + "]]></wapImage>"
						: "") + "</item>";
	}

	public void fromXML(Element xml) throws NoContentTypeException, Exception {
		NamedNodeMap attr = xml.getAttributes();
		this.id = Integer.parseInt(attr.getNamedItem("id").getNodeValue());
		this.serviceid = attr.getNamedItem("serviceid").getNodeValue();
		this.dirty = Integer.parseInt(attr.getNamedItem("status")
				.getNodeValue());

		this.contenttype = new ContenttypeDTO(conn, Integer.parseInt(attr
				.getNamedItem("contenttype").getNodeValue()));
		this.userId = Integer.parseInt(attr.getNamedItem("userid")
				.getNodeValue());
		this.timestamp = new SimpleDateFormat(this.datePattern, Locale.ENGLISH)
				.parse(attr.getNamedItem("timestamp").getNodeValue());
		this.contentItemId = Integer.parseInt(attr
				.getNamedItem("contentitemid").getNodeValue());
		this.wapImage = Integer.parseInt(attr.getNamedItem("wapimageid")
				.getNodeValue());
		this.smsBody = xml.getElementsByTagName("smsBody").item(0)
				.getTextContent();
		if (this.contentItemId > 0 && this.dirty != 2) {

			this.wapTitle = xml.getElementsByTagName("wapTitle").item(0)
					.getTextContent();
			this.wapAbstract = xml.getElementsByTagName("wapAbstract").item(0)
					.getTextContent();
			this.wapBody = xml.getElementsByTagName("wapBody").item(0)
					.getTextContent();

			String image = xml.getElementsByTagName("wapImage").item(0)
					.getTextContent();
			if (image != null && image.length() > 0) {
				BASE64Decoder b64d = new BASE64Decoder();
				this.wapImageData = b64d.decodeBuffer(image);
			}
		}
	}

	public boolean getFuturepost() {
		return futurepost;
	}

	public int getWapImageId() {
		return this.wapImage;
	}

	public String getWapImageType() {
		return this.wapImageType;
	}

	public String getWapImage() {
		String retval = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT `tdata` AS `data` FROM `icp`.`simple_text_binary` WHERE `id`="
							+ this.wapImage);
			if (rs.next()) {
				BASE64Encoder b64d = new BASE64Encoder();
				retval = b64d.encode(rs.getBytes("data"));
			}
			try {
				stmt.close();
			} catch (Exception ee) {
			}
			try {
				rs.close();
			} catch (Exception ee) {
			}
		} catch (Exception e) {
			try {
				stmt.close();
			} catch (Exception ee) {
			}
			try {
				rs.close();
			} catch (Exception ee) {
			}
		}
		return retval;
	}

	public byte[] getWapImageData() {
		byte[] retval = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT `tdata` AS `data` FROM `icp`.`simple_text_binary` WHERE `id`="
							+ this.wapImage);
			if (rs.next()) {
				retval = rs.getBytes("data");
			}
			
			try {
				rs.close();
			} catch (Exception ee) {
			}
			try {
				stmt.close();
			} catch (Exception ee) {
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}finally{
			try {
				rs.close();
			} catch (Exception ee) {
			}
			try {
				stmt.close();
			} catch (Exception ee) {
			}
			
		}
		return retval;
	}

	public void setWapImage(byte[] image) {
		this.wapImageData = image;
	}

	public ContenttypeDTO getContenttype() {
		return contenttype;
	}

	public void setContenttype(ContenttypeDTO contenttype) {
		this.contenttype = contenttype;
	}

	public String getSmsBody() {
		return (smsBody == null) ? "" : smsBody;
	}

	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public UserDTO getUser() throws SQLException {
		return new UserDTO(conn, this.userId);
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getWapAbstract() {
		return wapAbstract == null ? "" : wapAbstract;
	}

	public void setWapAbstract(String wapAbstract) {
		this.wapAbstract = wapAbstract;
	}

	public String getWapAuthor() {
		return wapAuthor;
	}

	public void setWapAuthor(String wapAuthor) {
		this.wapAuthor = wapAuthor;
	}

	public String getWapBody() {
		return wapBody == null ? "" : wapBody;
	}

	public void setWapBody(String wapBody) {
		this.wapBody = wapBody;
	}

	public String getWapTitle() {
		return wapTitle == null ? "" : wapTitle;
	}

	public void setWapTitle(String wapTitle) {
		this.wapTitle = wapTitle;
	}

	public int getContentItemId() {
		return contentItemId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return this.dirty;
	}

	public void setStatus(int s) {
		this.dirty = s;
	}

	public String getServiceId() {
		return this.serviceid;
	}

	public int getDirty() {
		return dirty;
	}

	public void setDirty(int dirty) {
		this.dirty = dirty;
	}
}
