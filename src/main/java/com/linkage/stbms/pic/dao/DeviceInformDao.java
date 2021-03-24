package com.linkage.stbms.pic.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.util.StrUtil;

public class DeviceInformDao {

	final Logger logger = LoggerFactory.getLogger(DeviceInformDao.class);
	
	/**
	 * 插入机顶盒版本升级记录表
	 * @author gongsj
	 * @date 2010-11-8
	 * @param strategyId
	 * @param deviceId
	 * @param taskId
	 * @param deviceTypeIdOld
	 */
	public void insertGwSoftupRecord(Long strategyId, String deviceId, String taskId, int deviceTypeIdOld) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("insert into gw_softup_record(id, device_id, task_id, dev_type_id_old, start_time,result) values(?,?,?,?,?,0)");
		ppSQL.setLong(1, strategyId);
		ppSQL.setString(2, deviceId);
		ppSQL.setInt(3, Integer.parseInt(taskId));
		ppSQL.setInt(4, deviceTypeIdOld);
		ppSQL.setLong(5, new Date().getTime()/1000);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
		
	}

	/**
	 * 检查此设备是否已升级成功
	 * @author gongsj
	 * @date 2010-11-9
	 * @param deviceId
	 * @param taskId
	 * @return
	 */
	public boolean checkSoftupRecord(String deviceId, String taskId) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("select result from gw_softup_record where device_id=? and task_id=? ");
		ppSQL.setString(1, deviceId);
		ppSQL.setInt(2, Integer.parseInt(taskId));
		

		Map<String,String> map = DBOperation.getRecord(ppSQL.getSQL());
		if (null != map ) 
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 获得策略ID
	 * @author gongsj
	 * @date 2010-11-10
	 * @param deviceId
	 * @param tempId
	 * @param serviceId
	 * @return
	 */
	public Long getStrategyId(String deviceId, int tempId, int serviceId)
	{
		Long strategyId = 0L;
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("select id from " + Global.TABLENAME + " where device_id=? and temp_id=? and service_id=? ");
		ppSQL.setString(1, deviceId);
		ppSQL.setInt(2, tempId);
		ppSQL.setInt(3, serviceId);
		Map<String, String> map = DBOperation.getRecord(ppSQL.getSQL());
		if (null != map)
		{
			strategyId = Long.parseLong(map.get("id"));
		}
		return strategyId;
	}
	
	/**
	 * 更新机顶盒版本升级记录表(result, dev_type_id_new)
	 * @author gongsj
	 * @date 2010-11-10
	 * @param strategyId
	 * @param deviceTypeId
	 * @param result
	 */
	public void updateSoftupRecord(Long strategyId, int deviceTypeId, int result) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("update gw_softup_record set result=?, dev_type_id_new=?, end_time=? where id=? ");
		ppSQL.setInt(1, result);
		ppSQL.setInt(2, deviceTypeId);
		ppSQL.setLong(3, new Date().getTime()/1000);
		ppSQL.setLong(4, strategyId);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}
	
	/**
	 * 更新机顶盒版本升级记录表(result)
	 * @author gongsj
	 * @date 2010-11-17
	 * @param strategyId
	 * @param result
	 */
	public void updateSoftupRecord(Long strategyId, int result) {
		PrepareSQL ppSQL = new PrepareSQL();
		ppSQL.setSQL("update gw_softup_record set result=?, dev_type_id_new=?, end_time=? where id=? ");
		ppSQL.setInt(1, result);
		ppSQL.setLong(3, new Date().getTime()/1000);
		ppSQL.setLong(4, strategyId);
		
		DBOperation.executeUpdate(ppSQL.getSQL());
	}
	
	/**
	 * 填充IP 
	 */
	private String getFillIP(String ip)
	{
		String fillIP = ip;
		String[] ipArray = new String[4];
		ipArray = ip.split("\\.");
		for (int i = 0; i < 4; i++)
		{
			if (ipArray[i].length() == 1)
			{
				ipArray[i] = "00" + ipArray[i];
			}
			else if (ipArray[i].length() == 2)
			{
				ipArray[i] = "0" + ipArray[i];
			}
		}
		fillIP = ipArray[0] +"."+ ipArray[1] +"."+ ipArray[2] +"."+ ipArray[3];

		return fillIP;
	}
	
	public ResultSet getSoftRecord(long startId, long endId)
	{
		StringBuffer sql = new StringBuffer();
		sql
				.append(
						"select id,task_id,batch_id,device_id from gw_softup_record where id>")
				.append(startId).append(" and id<=").append(endId);
		PrepareSQL psql = new PrepareSQL(sql.toString());
		return getRecords(psql.getSQL());
	}
	
	private Connection conn = null;
	private Statement stmt = null;

	/**
	 * get list of records.
	 * 
	 * @param sql
	 * @return
	 */
	public ResultSet getRecords(String sql)
	{
		ResultSet rs = null;
		try
		{
			conn = com.linkage.stbms.pic.db.DBOperation.getConnect();
			stmt = conn.createStatement(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);
		}
		catch (SQLException sqle)
		{
			logger.error(StrUtil.formatDate("yyyy-MM-dd HH:mm:ss", (System
					.currentTimeMillis() / 1000))
					+ " SQL: " + sql + ". 错误信息: " + sqle.getMessage());
		}
		finally
		{
		}
		return rs;
	}
	
	public void closeConnection()
	{
		try
		{
			if (null != stmt)
			{
				stmt.close();
				stmt = null;
			}
		}
		catch (Exception e)
		{
			logger.error("close statement object error", e);
		}
		try
		{
			if (null != conn)
			{
				conn.close();
				conn = null;
			}
		}
		catch (Exception e)
		{
			logger.error("close connection object error", e);
		}
	}
	
	public long getMaxSoftRecordId() {
		logger.debug("getMaxSoftRecordId()");

		return DBOperation.getMaxId("id", "gw_softup_record");
	}
}












