/**
 * DBConnectionManager.java 1.00 2007-5-21
 *
 * Copyright 2006 联创网络科技.版权所有
 */
package com.linkage.stbms.pic.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * 
 * 
 * @author yuhaiteng
 * @version 1.00, 2007-5-21
 * @since CoreManager 1.0
 */
public class DBConnectionManager {

	final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);

	private static DBConnectionManager instance = null;

	private static int m_ActiveSize;

	private Vector<Driver> drivers = new Vector<Driver>();// JDBC驱动程序向量

	private Map<String, DBConnectionPool> pools = new HashMap<String, DBConnectionPool>();;

	/**
	 * 私有构造函数，初始化数据库连接池
	 * 
	 * @param props
	 *            连接数据库属性
	 */
	private DBConnectionManager(Properties props) {
		logger.debug("初始化数据库连接池");
		initialize(props);
	}

	/**
	 * 初始化数据库连接池
	 * 
	 * @param props
	 *            连接数据库属性
	 */
	private void initialize(Properties props) {
		loadDrivers(props);
		createPools(props);
	}

	/**
	 * 返回唯一实例，如果是第一次调用此方法,则创建实例
	 * 
	 * @return 返回DBConnectionManager实例
	 */
	public static DBConnectionManager getInstance(Properties props) {
		if (instance == null) {
			instance = new DBConnectionManager(props);
		}

		m_ActiveSize++;
		return instance;
	}

	/**
	 * 将连接对象返回给由名字指定的连接池
	 * 
	 * @param name
	 *            属性文件中定义的连接池名字
	 * @param con
	 *            连接对象
	 */
	public void freeConnection(String name, Connection con) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			pool.freeConnection(con);
		}
	}

	/**
	 * 创建一个新连接，连接的释放权交给用户
	 * 
	 * @param name
	 *            属性文件中定义的连接池名字
	 * @return Connection 返回连接对象
	 */
	public Connection CreateConnection(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			return pool.newConnection();
		} else {
			return null;
		}
	}

	/**
	 * 删除链接池中的一个链接
	 * 
	 * @param name
	 *            属性文件中定义的连接池名字
	 * @param conn
	 *            连接对象
	 */
	public void DistroyConnection(Connection conn, String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			pool.DistroyConnection(conn);
		}

		return;

	}

	/**
	 * 获得一个(空闲)连接,如果没有可用连接，且已有小于最大连接数， 则创建并返回新连接，否则放回null
	 * 
	 * @param name
	 *            属性文件中定义的连接池名字
	 * @return Connection 返回连接对象
	 */
	public Connection getConnection(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			// logger.debug("获得一个(空闲)连接.");
			return pool.getConnection();
		} else {
			logger.debug("pool is 空－－－－－－－－－－－）））））");
			return null;
		}
	}

	/**
	 * 获得一个(空闲)连接,如果没有可用连接，且已有小于最大连接数，则创建并返回新连接. 否则在指定时间内等待其他线程释放连接。
	 * 
	 * @param name
	 *            属性文件中定义的连接池名字
	 * @param time
	 *            以毫秒计的等待时间
	 * @return Connection 可用连接或空
	 */
	public Connection getConnection(String name, long time) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null)
			return pool.getConnection(time);
		else
			return null;
	}

	/**
	 * 关闭所有连接
	 */
	public synchronized void releaseConnection() {
		// 不等待
		Iterator it = pools.keySet().iterator();
		while (it.hasNext()) {
			DBConnectionPool pool = (DBConnectionPool) pools.get(it.next());
			pool.release();
		}
	}

	/**
	 * 关闭所有连接，撤消驱动程序的注册
	 * 
	 */
	public synchronized void release() {
		// 等待直到最后一个客户程序结束对对象的使用
		if (--m_ActiveSize != 0)
			return;
		releaseConnection();

		Enumeration allDrivers = drivers.elements();
		while (allDrivers.hasMoreElements()) {
			Driver driver = (Driver) allDrivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				logger.debug("撤销JDBC驱动程序" + driver.getClass().getName() + "注册");
			} catch (SQLException ex) {
				logger.debug("无法撤销下列JDBC驱动程序的注册" + driver.getClass().getName() + ":" + ex.getMessage());
			}
		}
	}

	/**
	 * 根据指定属性创建连接池实例
	 * 
	 * @param props
	 *            连接池属性
	 */
	private void createPools(Properties props) {
		String poolNames = props.getProperty("poolName");
		String[] poolName = poolNames.split(",");
		for (int i = 0; i < poolName.length; i++) {
			String pname = poolName[i];
			String url = props.getProperty(pname + ".url");
			String user = props.getProperty(pname + ".user");
			String password = props.getProperty(pname + ".password");
			String maxconn = props.getProperty(pname + ".maxconn", "1");

			int max = Integer.parseInt(maxconn);

			DBConnectionPool pool = new DBConnectionPool(pname, url, user, password, max);
			pools.put(pname, pool);
			logger.debug("成功创建连接池" + pname);
			logger.debug("URL=" + url);
			logger.debug("user=" + user + "  ,password=" + password + "  ,maxconn=" + max);
			logger.debug("DBConnectionManager Active Pool " + (++m_ActiveSize) + " Create!");
		}

	}

	/**
	 * 装载和注册所有JDBC驱动程序
	 * 
	 * @param props
	 *            连接池属性
	 */
	private void loadDrivers(Properties props) {
		String poolNames = props.getProperty("poolName");
		String[] poolName = poolNames.split(",");

		for (int i = 0; i < poolName.length; i++) {
			String driverClasses = props.getProperty(poolName[i].trim() + ".driver");
			logger.debug("driverClasses={}",driverClasses);
			Driver driver = null;
			try {
				driver = (Driver) Class.forName(driverClasses).newInstance();
				DriverManager.registerDriver(driver);
				drivers.addElement(driver);
				logger.debug("成功注册JDBC驱动程序:{}",driver.getClass().getName());
			} catch (Exception ex) {
				logger.debug("无法注册JDBC驱动程序:{},error:",driver == null ? "" : driver.getClass().getName(),ex);
			}
		}
	}

	/**
	 * 连接池类,管理具体的JDBC连接
	 */
	class DBConnectionPool {
		public int checkedOut = 0, CacheSize = 0;

		private Vector<Connection> freeConnections = new Vector<Connection>();

		private int maxConn;

		private String name;

		private String password;

		private String URL;

		private String user;

		/**
		 * 创建新的连接池
		 * 
		 * @param name
		 *            连接池名称
		 * @param URL
		 *            连接数据库URL
		 * @param user
		 *            用户帐号
		 * @param password
		 *            用户密码
		 * @param maxConn
		 *            此连接池允许建立的最大连接数
		 */
		public DBConnectionPool(String name, String URL, String user, String password, int maxConn) {
			this.name = name;
			this.URL = URL;
			this.user = user;
			this.password = password;
			this.maxConn = maxConn;
		}

		/**
		 * 
		 * 将不在使用的连接返回连接池
		 * 
		 * @param con
		 *            客户程序释放的连接
		 */
		public synchronized void freeConnection(Connection con) {
			synchronized (freeConnections) {
				try {
					// 判断连接是否有效
					if (con == null || con.isClosed() == true)
						return;
					Enumeration allConnections = freeConnections.elements();
					while (allConnections.hasMoreElements()) {
						Connection conn = (Connection) allConnections.nextElement();
						if (conn.equals(con))
							return;

					}

				} catch (Exception ex) {
					logger.debug("释放连接失败:" + ex.getMessage());

				}
				freeConnections.addElement(con);
				if (checkedOut > 0)
					checkedOut--;

				notifyAll();
			}
		}

		public void DistroyConnection(Connection conn) {
			try {
				if (CacheSize > 0){
					CacheSize--;
				}
				if (conn == null){
					return;
				}
				if (conn.isClosed()){
					conn = null;
				}
				if(conn != null){
					conn.close();
					conn = null;
				}

			} catch (SQLException ex) {
				logger.debug("撤销连接出错:",ex);
			}

		}

		/**
		 * 获得一个(空闲)连接,如果没有可用连接，且已有小于最大连接数，则创建并返回新连接
		 * 如原来登记为可用的连接不在有效，则从向量删除之，然后递归调用自己以尝试新的可用连接
		 * 
		 */
		public synchronized Connection getConnection() {
			synchronized (freeConnections) {
				Connection con = null;
				if (freeConnections.size() > 0) {
					// logger.debug("freeConnections.size() > 0");
					con = (Connection) freeConnections.firstElement();
					freeConnections.removeElementAt(0);

					try {
						if (con == null) {
							if (checkedOut > 0)
								checkedOut--;
							if (CacheSize > 0)
								CacheSize--;
							logger.debug("从连接池" + name + "删除一个无效连接");
							con = getConnection();

						}

						if (con.isClosed() == true) {
							if (checkedOut > 0)
								checkedOut--;
							if (CacheSize > 0)
								CacheSize--;
							logger.debug("从连接池" + name + "删除一个无效连接");
							// 递归调用自己，尝试再次获取可用连接
							return getConnection();
						}
					} catch (SQLException e) {
						if (checkedOut > 0)
							checkedOut--;
						if (CacheSize > 0)
							CacheSize--;
						logger.debug("从连接池" + name + "删除一个无效连接");
						// 递归调用自己，尝试再次获取可用连接
						return getConnection();
					}
				} else if (maxConn == 0 || checkedOut < maxConn) {
					con = newConnection();
					freeConnections.add(con);
				} else {
					logger.debug("连接池已满处理未实现");
				}

				if (con != null)
					checkedOut++;
				return con;
			}
		}

		/**
		 * 获得一个(空闲)连接,如果没有可用连接,可以指定客户程序能够等待的最长时间
		 * 
		 * @param timeout
		 *            以毫秒计的等待时间限制
		 */
		public synchronized Connection getConnection(long timeout) {
			long startTime = new Date().getTime();
			Connection con;
			while ((con = getConnection()) == null) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
					logger.error("InterruptedException error:",e);
					// Restore interrupted state...      
					Thread.currentThread().interrupt();
				}
				if ((new Date().getTime() - startTime) >= timeout){
					return null; // wait()返回的原因是超时
				}
			}
			return con;
		}

		/**
		 * 关闭所有连接
		 */
		public synchronized void release() {
			Enumeration allConnections = freeConnections.elements();
			int i = 0;

			while (allConnections.hasMoreElements()) {
				i++;
				Connection con = (Connection) allConnections.nextElement();
				try {
					con.close();
					logger.debug("关闭连接池" + name + "中的一个连接");
				} catch (SQLException ex) {
					logger.debug("无法关闭连接池" + name + "中钓连接:" + ex.getMessage());
				}
			}

			logger.debug("复位连接池" + name + "中的" + String.valueOf(i) + "连接");
			freeConnections.removeAllElements();
			checkedOut = 0;
			CacheSize = 0;
		}

		/**
		 * 创建新连接
		 * 
		 */
		public Connection newConnection() {
			Connection con = null;
			try {
				if (user == null) {
					con = DriverManager.getConnection(URL);
				} else {
					// logger.debug("URL=" + URL);
					// logger.debug("user=" + user);
					// logger.debug("password=" + password);
					con = DriverManager.getConnection(URL, user, password);
				}
				// logger.debug("连接池" + name + "创建一个新连接");
			} catch (SQLException ex) {
				logger.debug("无法创建下列 URL 的连接" + URL + ":" + ex.getStackTrace());
				return null;
			}
			CacheSize++;
			return con;
		}
	}
}
