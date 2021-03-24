package com.linkage.itms.hlj.dispatch.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 宽带上网信息查询
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-9
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryNetDAO
{
	private static final Logger logger = LoggerFactory.getLogger(QueryNetDAO.class);
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		logger.debug("QueryDevDAO-->queryUserByLoid({})", loid);
		String sql = "select distinct(a.user_id),a.device_id,b.username,b.open_status from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 and a.username= ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByDeviceId(String deviceId)
	{
		logger.debug("QueryDevDAO-->queryUserByDeviceId({})", deviceId);
		String sql = "select distinct(a.user_id),a.device_id,b.username,b.open_status from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 and a.device_id= ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户宽带账号查询用户信息
	 * 
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id ,a.username,a.open_status"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户设备SN查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByDevSN(String devSN, String devSubSN)
	{
		logger.warn("QueryDevDAO-->queryUserByDevSN({})", devSN);
		String sql = "select a.device_id,a.cpe_mac,a.customer_id user_id from tab_gw_device a where a.device_status = 1 and a.dev_sub_sn = ? and a.device_serialnumber like '%"
				+ devSN + "%'";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devSubSN);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", devSN, result);
		return result;
	}
	
	public String queryFailResult(String deviceId,String username)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  result_desc ");
		psql.append("  from  gw_serv_strategy  ");
		psql.append(" where 1=1");
		psql.append(" and service_id=1001 and username= '"+username+"'" );
		psql.append(" and device_id= '"+deviceId+"'" );
		List<HashMap<String,String>> failResultList = DBOperation.getRecords(psql.getSQL());
		if(failResultList != null && !failResultList.isEmpty()){
			return StringUtil.getStringValue(failResultList.get(0),"result_desc", "");
		}else{
			return "";
		}
	}
}
