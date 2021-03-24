
package com.linkage.stbms.pic.dao;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBAdapter;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DbUtils;
import com.linkage.stbms.pic.Global;

/**
 * 开机画面运营
 * 
 * @author 王森博
 */
@SuppressWarnings("unchecked")
public class LogoConDao
{

	final static Logger logger = LoggerFactory.getLogger(StrategyDao.class);

	public String queryHaveTask(String deviceId)
	{
		StringBuffer sql = new StringBuffer();
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql.append("select task_id from stb_logo_recent where device_id='").append(deviceId).append("' and status=0");
		}
		else
		{
			sql.append("select * from stb_logo_recent where device_id='").append(deviceId).append("' and status=0");
		}
		PrepareSQL psql = new PrepareSQL(sql.toString());
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if(map==null||map.isEmpty()){
			return null;
		}
		return map.get("task_id");
	}

	public void insertRecord(long strategyId, String deviceId, long taskId)
	{
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("insert into stb_logo_record(ucid, strategy_id, device_id, task_id, result_id, start_time) values(?,?,?,?,0,?)");
		ppSQL.setLong(1, createId());
		ppSQL.setLong(2, strategyId);
		ppSQL.setString(3, deviceId);
		ppSQL.setLong(4, taskId);
		ppSQL.setLong(5, new Date().getTime()/1000);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}
	
	public void updateRecord(long taskId, String deviceId,long strategyId, int result) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("update stb_logo_record set strategy_id=?, result_id=?, end_time=? where task_id=? and device_id=? ");
		ppSQL.setLong(1, strategyId);
		ppSQL.setInt(2, result);
		ppSQL.setLong(3, new Date().getTime()/1000);
		ppSQL.setLong(4, taskId);
		ppSQL.setString(5, deviceId);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}
	
	public long createId()
	{
		if(DBUtil.GetDB() == 3) {
			return DbUtils.getUnusedID("sql_stb_logo_record", 1);
		}
		return LogoConDao.createUcid(1);
	}

	public synchronized static long createUcid(int count)
	{
		logger.debug("createUcid({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		if (Global.MIN_UNUSED_PICUCID < 0)
		{
			if (Global.DB_ORACLE.equals(Global.DB_TYPE))
			{// oracle
			 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(Global.SUM_UNUSED_PICUCID) - 1;
			}
			else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
			{// sybase
				Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(Global.SUM_UNUSED_PICUCID) - 1;
			}
			Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID
					+ Global.SUM_UNUSED_PICUCID;
		}
		if (Global.MAX_UNUSED_PICUCID < (Global.MIN_UNUSED_PICUCID + count))
		{
			if (Global.SUM_UNUSED_PICUCID < count)
			{
				if (Global.DB_ORACLE.equals(Global.DB_TYPE))
				{// oracle
				 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(count) - 1;
				}
				else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
				{// sybase
					Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(count) - 1;
				}
				Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID + count;
			}
			else
			{
				if (Global.DB_ORACLE.equals(Global.DB_TYPE))
				{// oracle
				 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(Global.SUM_UNUSED_PICUCID);
				// - 1;
				}
				else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
				{// sybase
					Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(Global.SUM_UNUSED_PICUCID) - 1;
				}
				Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID
						+ Global.SUM_UNUSED_PICUCID;
			}
		}
		serial = Global.MIN_UNUSED_PICUCID + 1;
		Global.MIN_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID + count;
		logger.debug("ID={}", serial);
		return serial;
	}

	public static long getMaxId4Sybase(int count)
	{
		logger.debug("getMaxId4Sybase({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		String sql = "maxPicUpRecordIdProc ?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, count);
		return DBOperation.executeProcSelect(pSQL.getSQL());
	}
	
	public static long getMaxId4Oracle(int count) {
		logger.debug("getMaxId4Oracle({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		CallableStatement cstmt = null;
		Connection conn = null;
		String sql = "{call maxPicUpRecordIdProc(?,?)}";

		try {
			conn = DBAdapter.getJDBCConnection();
			cstmt = conn.prepareCall(sql);
			cstmt.setInt(1, count);
			cstmt.registerOutParameter(2, Types.INTEGER);
			cstmt.execute();
			serial = cstmt.getLong(2);
		} catch (Exception e) {
			logger.error("getMaxId4Oracle Exception:{}", e.getMessage());
		} finally {
			sql = null;

			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					logger.error("cstmt.close SQLException:{}", e.getMessage());
				}
				cstmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					logger.error("conn.close error:{}", e.getMessage());
				}

				conn = null;
			}
		}

		return serial;
	}
	
	public Map<String, String> getBatchConTaskInfo(String taskId)
	{
		StringBuffer sql = new StringBuffer();
		// mysql db
		if (3 == DBUtil.GetDB()) 
		{
			sql.append("select sd_qd_pic_url,sd_kj_pic_url,sd_rz_pic_url from stb_logo_task where ").append(" task_id=").append(taskId);
		}
		else
		{
			sql.append("select * from stb_logo_task where ").append(" task_id=").append(taskId);
		}
		
		PrepareSQL psql = new PrepareSQL(sql.toString());
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}

	public void updateRecord(long strategyId, int result) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("update stb_logo_record set result_id=?, end_time=? where strategy_id=? ");
		ppSQL.setInt(1, result);
		ppSQL.setLong(2, new Date().getTime()/1000);
		ppSQL.setLong(3, strategyId);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}

	public void updateRecent(String deviceId, long taskId)
	{
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("update stb_logo_recent set status=1 where device_id=? and task_id=? ");
		ppSQL.setString(1, deviceId);
		ppSQL.setLong(2, taskId);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}

}
