package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月26日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryVOIPWanInfoDao
{
	private static Logger logger = LoggerFactory.getLogger(QueryVOIPWanInfoDao.class);
	
	/**
	 * 获取所有的session信息
	 * @param device_id
	 */
	public List<HashMap<String, String>> getAllChannel(String device_id)
	{
		logger.debug("getAllChannel({})", device_id);
		String sql ="select * from  gw_wan_conn_session where device_id=?";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql ="select serv_list from  gw_wan_conn_session where device_id=?";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
	
	public List<HashMap<String, String>> getVoipPort(String userId)
	{
		String sql = "select voip_port from tab_voip_serv_param where user_id= ?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setInt(1, StringUtil.getIntegerValue(userId));
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
}
