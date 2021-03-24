package com.linkage.stbms.pic.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

/**
 * 数据库连接代理 主要负责实例化DBConnectionManager、获取连接，释放连接等
 * 控制了poolName是否为空，不空时才可操作DBConnectionManager
 * 
 * @author Admin
 * 
 */
public class DBConnectionProxy {

	final Logger logger = LoggerFactory.getLogger(DBConnectionProxy.class);

	private DBConnectionManager connMgr = null;

	private String poolName = null;

	public DBConnectionProxy(Properties props, String DB_TYPE) {
		// poolName = props.getProperty("poolName");
		poolName = DB_TYPE + "pool";
		// logger.debug("DBConnectionProxy构造函数.....");
		connMgr = DBConnectionManager.getInstance(props);
	}

	/**
	 * 删除连接池中的一个连接
	 * 
	 * @param conn
	 *            连接对象
	 */
	public void DistroyConnection(Connection conn) {
		if (connMgr == null || conn == null) {
			logger.debug("The Connection pool is not Inital");
			return;
		}
		connMgr.DistroyConnection(conn, poolName);
		return;
	}

	/**
	 * 向连接池申请一个连接
	 */
	public Connection getConnection() {
		Connection con = null;
		if (connMgr == null || poolName == null) {
			logger.debug("连接池没有初始化或属性文件有错");
			return null;
		}
		// logger.debug("connMgr=" + connMgr.toString());
		// logger.debug("向连接池申请一个连接.");
		// 从连接池获取一个连接
		con = connMgr.getConnection(poolName);
		if (con == null) {
			logger.debug("连接池中[" + poolName + "]没有空闲连接");
		}
		return con;
	}

	/**
	 * 将连接返还给连接池
	 */
	public void ReleaseConnection(Connection conn) {
		if (connMgr == null || poolName == null) {
			logger.debug("连接池没有初始化或属性文件有错");
			return;
		}
		connMgr.freeConnection(poolName, conn);
		return;

	}

	/**
	 * 复位连接池，释放连接池中的所有连接
	 */
	public void ResetPool() {
		if (connMgr == null || poolName == null) {
			logger.debug("连接池没有初始化或属性文件有错");
			return;
		}
		connMgr.releaseConnection();

	}
}
