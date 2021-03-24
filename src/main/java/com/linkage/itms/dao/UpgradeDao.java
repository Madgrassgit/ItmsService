package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月24日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpgradeDao
{
	/**
	 * 通过任务号，查询tab_version_upgrade_dev，获得设备id数组
	 * @param taskId
	 * @return
	 */
	public List<HashMap<String, String>> selectVersionUpgradeDevDeviceIdByTaskId(Long taskId){
		String sql = "select device_id from tab_version_upgrade_dev where task_id = ?";
		
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, taskId);

		// 执行查询
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 通过device_id查询tab_gw_device，获得对应的devicetype_id
	 * @param deviceId
	 * @return
	 */
	public Map selectDeviceTypeIdByDeviceId(String deviceId){
		String sql = "select devicetype_id from tab_gw_device where device_id = ?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 通过任务号修改任务信息
	 * @param taskId 任务号
	 * @param operator 操作人
	 * @param calldate 操作时间
	 * @param status 任务状态
	 * @return
	 */
	public int updateVersionUpgradeByTaskId(Long taskId, String operator, Long calldate, Integer status)
	{
		String sql = "update tab_version_upgrade set task_status = ?, operator = ?, calldate = ? where task_id = ?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setInt(1, status);
		psql.setString(2, operator);
		psql.setLong(3, calldate);
		psql.setLong(4, taskId);
		return DBOperation.executeUpdate(psql.getSQL());
	} 
}
