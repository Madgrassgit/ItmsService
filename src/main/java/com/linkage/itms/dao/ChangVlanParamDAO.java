package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-11
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangVlanParamDAO
{
	/**
	 * 根据userId和VlanId查询是否存在
	 * 如果存在的话，那么将原有的VLANId进行更新掉
	 * @author 岩 
	 * @date 2016-7-11
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getMapByUser( String userId, String oldVlanId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select serv_type_id, username, vlanid from hgwcust_serv_info ");
		psql.append(" where 1=1 and user_id = " + userId);
		psql.append(" and vlanid = '" + oldVlanId + "'");
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}
	
	/**
	 * 如果getMapByUser能查到唯一一个工单，则进行更新操作
	 * @author 岩 
	 * @date 2016-7-11
	 * @param userId
	 * @param oldVlanId
	 * @param newVlanId
	 * @return
	 */
	public int updateVlanId(String userId, String oldVlanId, String newVlanId){
		PrepareSQL psql = new PrepareSQL();
		psql.append("update hgwcust_serv_info ");
		psql.append(" set vlanid = '" + newVlanId +"'");
		psql.append(" where 1=1");
		psql.append(" and user_id = "+userId);
		psql.append(" and vlanid = '" + oldVlanId + "'");
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 获取userId 校验是否存在用户
	 * @author 岩 
	 * @date 2016-7-11
	 * @param loid
	 * @return
	 */
	public Map<String, String> getUserMapByLoid(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  * ");
		psql.append(" from tab_hgwcustomer ");
		psql.append(" where 1=1");
		psql.append(" and username = '"+loid+"'" );
		Map<String,String> userMap = DBOperation.getRecord(psql.getSQL());
		return userMap;
	}
	
	/**
	 * 根据userId查询是否存在
	 * @author chenxj6
	 * @date 2016-10-13
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getMapByUser4NX( String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select serv_type_id, username, vlanid from hgwcust_serv_info ");
		psql.append(" where 1=1 and user_id = " + userId);
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}
	
	/**
	 * 如果getMapByUser能查到唯一一个工单，则进行更新操作
	 * @author chenxj6
	 * @date 2016-10-13
	 * @param userId
	 * @param newVlanId
	 * @return
	 */
	public int updateVlanId4NX(String userId, String newVlanId){
		PrepareSQL psql = new PrepareSQL();
		psql.append(" update hgwcust_serv_info ");
		psql.append(" set vlanid = '" + newVlanId +"'");
		psql.append(" where 1=1");
		psql.append(" and user_id = "+userId);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 获得上行方式 NX
	 * @param deviceId
	 * @return
	 */
	public  String getAccessType(String deviceId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select access_type from gw_wan where device_id='").append(deviceId)
				.append("' and wan_id=1");
		Map<String, String> accessTypeMap = DBOperation.getRecord(sql.toString());
		if (null == accessTypeMap || null == accessTypeMap.get("access_type"))
		{
			return null;
		}
		else
		{
			return accessTypeMap.get("access_type");
		}
	}
}
