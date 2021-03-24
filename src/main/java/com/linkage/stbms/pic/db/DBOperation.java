package com.linkage.stbms.pic.db;

import com.linkage.stbms.pic.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBOperation {
	static final Logger logger = LoggerFactory.getLogger(DBOperation.class);

	// private static Properties dbProps =
	// ConfReader.getInstance().getDbProps();

	/**
	 * 执行查询SQL语句，并返回结果集
	 * 
	 * @param selectSql
	 *            sql语句
	 * @return 回ResultSet结果集
	 */
//	public static ResultSet executeSelect(String selectSql, String dbType) {
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rst = null;
//		try {
//			// DBConnectionProxy connMgr = new DBConnectionProxy(dbProps,
//			// dbType);
//			// Connection conn = (Connection) connMgr.getConnection();
//			conn = getConnect();
//			if (conn == null) {
//				System.out.println("获取不到数据库连接");
//				return null;
//			}
//			stmt = conn.createStatement(
//					java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
//					java.sql.ResultSet.CONCUR_READ_ONLY);
//			// System.out.println("执行SQL【" + selectSql + "】");
//			rst = stmt.executeQuery(selectSql);
//			// System.out.println("执行SQL【" + selectSql + "】成功");
//			// connMgr.ReleaseConnection(conn);
//			// conn = null;
//			// connMgr = null;
//		} catch (SQLException ex) {
//			logger.error("执行SQL【" + selectSql + "】错误:" + ex);
//		} finally {
//			try {
//				if (stmt != null) {
//					stmt.close();
//					stmt = null;
//				}
//			} catch (Exception e) {
//				logger.error("close statement object error", e);
//			}
//			try {
//				conn.close();
//				conn = null;
//			} catch (Exception e) {
//				logger.error("close connection object error", e);
//			}
//		}
//		return rst;
//	}

//	public static ResultSet executeSelect(String selectSql) {
//		return executeSelect(selectSql, "sybase");
//	}

	/**
	 * 执行增、删、改SQL语句，并返回操作结果
	 * 
	 * @param sql
	 *            sql语句
	 * @return 返回操作结果 -1:没有获取到数据库连接;-2:更新数据库失败;-3:无更新字符串;1:更新数据库成功
	 */
	public static int executeUpdate(String sql, String dbType) {
		int result = 1;
		Connection conn = null;
		Statement st = null;
		if (sql == null || sql.length() == 0) {
			logger.error("SQL语句为空或长度为0");
			return -3;
		}
		// DBConnectionProxy connMgr = new DBConnectionProxy(dbProps, dbType);
		// Connection conn = (Connection) connMgr.getConnection();
		conn = getConnect();
		if (conn == null) {
			logger.error("获取不到数据库连接");
			return -1;
		}
		try {
			st = conn.createStatement();
			logger.info("update sql:" + sql);
			st.executeUpdate(sql);
			// System.out.println("执行SQL【" + sql + "】成功");
		} catch (SQLException ex) {
			result = -2;
			logger.error("执行SQL【" + sql + "】错误:", ex);
			// System.out.println("执行SQL【" + sql + "】错误:" + ex.getMessage());
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (Exception ex) {
					// System.out.println("关闭Statement错误:" + ex.getMessage());
					logger.error("关闭Statement错误:", ex);
				}
			}
			try {
				conn.close();
				conn = null;
			} catch (Exception e) {
				logger.error("close connection object error", e);
			}
		}
		return result;
	}

	public static int executeUpdate(String sql) {
		return executeUpdate(sql, "sybase");
	}

	/**
	 * 批量执行增、删、改SQL语句,并返回操作结果
	 * 
	 * @param list
	 *            SQL语句列表
	 * @return 返回操作结果 -1:没有获取到数据库连接;-2:更新数据库失败;-3:无更新字符串;1:更新数据库成功
	 */
	public static int executeBatch(ArrayList list, String dbType) {
		int result = 1;
		Connection conn = null;
		Statement stmt = null;
		if (list == null || list.size() == 0)
			return -3;
		// DBConnectionProxy connMgr = new DBConnectionProxy(dbProps, dbType);
		// Connection conn = connMgr.getConnection();
		conn = getConnect();
		if (conn == null) {
			logger.error("获取不到数据库连接");
			return -1;
		}
		try {
			stmt = conn.createStatement();
			logger.info("update sqlList:" + list);
			for (int i = 0; i < list.size(); i++) {
				stmt.addBatch((String) list.get(i));
				// System.out.println("加入SQL【" + list.get(i) + "】成功");
				if ((i + 1) % 200 == 0) {
					// System.out.println("执行上述200条SQL成功");
					stmt.executeBatch();
					stmt.clearBatch();
				}
			}
			if (list.size() % 200 != 0) {
				stmt.executeBatch();
				stmt.clearBatch();
				// System.out.println("执行上述多条SQL成功");
			}
			// System.out.println("执行 " + list.size() + " 条SQL成功");
		} catch (SQLException ex) {
			// System.out.println("批量执行SQL错误:" + ex.getMessage());
			logger.error("批量执行SQL错误:", ex);
			result = -2;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (Exception e) {
				logger.error("close statement object error", e);
			}
			try {
				conn.close();
				conn = null;
			} catch (Exception e) {
				logger.error("close connection object error", e);
			}
		}
		// connMgr.ReleaseConnection(conn);
		// conn = null;
		return result;
	}

	public static int executeBatch(ArrayList list) {
		return executeBatch(list, "sybase");
	}

	/**
	 * 根据sql语句获取数据中第一条记录。
	 * 
	 * @param sql
	 *            sql语句
	 * @return 以HashMap形式表示的数据库表中的记录， 若有多条记录，返回第一条 若没有对应记录，返回 null
	 */
	public static Map<String, String> getRecord(String sql) {
		Map<String, String> result = new HashMap<String, String>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData metadata;
		try {
			conn = getConnect();
			if (conn == null) {
				System.out.println("获取不到数据库连接");
				return null;
			}
			stmt = conn.createStatement(
					java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			logger.info("sql:" + sql);
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				metadata = rs.getMetaData();
				String value;
				for (int i = 1; i <= metadata.getColumnCount(); i++) {
					value = rs.getString(metadata.getColumnName(i));
					if (value == null)
						value = "";
					result.put(metadata.getColumnName(i).toLowerCase(), value
							.trim());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (null != rs) {
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				logger.error("close resultset object error", e);
			}
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (Exception e) {
				logger.error("close statement object error", e);
			}
			try {
				if (conn != null) {
				conn.close();
				conn = null;
				}
			} catch (Exception e) {
				logger.error("close connection object error", e);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		return result;
	}

	public static Cursor getCursor(String sql) {
		return getCursor(sql, "sybase");
	}

	/**
	 * 根据sql语句获取一批数据
	 * 
	 * @param sql
	 *            SQL语句字符串
	 * @return 返回自定义游标类
	 */
	public static Cursor getCursor(String sql, String dbType) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData metadata;
		Cursor cursor = new Cursor();
		try {
			// DBConnectionProxy connMgr = new DBConnectionProxy(dbProps,
			// dbType);
			// conn = (Connection) connMgr.getConnection();
			conn = getConnect();
			if (null == conn) {
				logger.error("getCursor:get connect error");
				return cursor;
			}
			stmt = conn.createStatement(
					java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			logger.info("sql:{}",sql);
			rs = stmt.executeQuery(sql);
			String value;
			while (rs.next()) {
				metadata = rs.getMetaData();
				HashMap fields = new HashMap();
				for (int i = 1; i <= metadata.getColumnCount(); i++) {
					value = rs.getString(metadata.getColumnName(i));
					if (value == null){
						value = "";
					}
					fields.put(metadata.getColumnName(i).toLowerCase(), value
							.trim());
				}
				cursor.add(fields);
			}
		} catch (SQLException sqle) {
			logger.error(StrUtil.formatDate("yyyy-MM-dd HH:mm:ss", (System
					.currentTimeMillis() / 1000))
					+ " SQL: " + sql + ". 错误信息: " + sqle.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} catch (Exception e) {
				logger.error("close resultset object error", e);
			}
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (Exception e) {
				logger.error("close statement object error", e);
			}
			try {
				if(conn != null){
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				logger.error("close connection object error", e);
			}
		}
		return cursor;
	}

	/**
	 * 
	 * 
	 * @param sql
	 *            sql语句
	 * @return 取出第一列生成List
	 */
	public static List<String> getListResult(String sql) {
		List<String> result = new ArrayList<String>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData metadata;
		try {
			conn = getConnect();
			if (conn == null) {
				System.out.println("获取不到数据库连接");
				return null;
			}
			stmt = conn.createStatement(
					java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			logger.info("sql:" + sql);
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				metadata = rs.getMetaData();
				result.add(rs.getString(metadata.getColumnName(1)));
//				String value;
//				for (int i = 1; i <= metadata.getColumnCount(); i++) {
//					value = rs.getString(metadata.getColumnName(i));
//					if (value == null)
//						value = "";
//					result.put(metadata.getColumnName(i).toLowerCase(), value
//							.trim());
//				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error("close resultset object error", e);
			}
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (Exception e) {
				logger.error("close statement object error", e);
			}
			try {
				if(conn != null){
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				logger.error("close connection object error", e);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		return result;
	}
	
	/**
	 * 获取连接
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2008-10-24
	 * @return Connection
	 */
	public static Connection getConnect() {

		Connection conn = null;
		try {
			conn = DriverManager.getConnection("proxool.xml-test");
			//logger.debug("get the connect");
		} catch (SQLException e) {
			logger.error("connect is not available", e);
		}
		return conn;
	}
}
