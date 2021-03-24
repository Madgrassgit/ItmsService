package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * 江西电信：Itv业务组播vlan查询接口
 * @author chenxj6
 * @date 2017-03-24
 * @param param
 * @return
 */
public class QueryMulticastVlanDAO {
	
	/**
	 * 根据用户宽带账号查询用户信息
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		String sql = "select a.user_id,a.device_id from tab_hgwcustomer a where a.username= ? order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户IPTV宽带账号查询用户信息
	 * @param iptvAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByIptvAccount(String iptvAccount)
	{
		String sql = "select a.user_id, b.device_id "
				+ " from hgwcust_serv_info a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, iptvAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * @param voipPhone
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipPhone(String voipPhone)
	{
		String sql = "select a.user_id, b.device_id "
				+ " from tab_voip_serv_param a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.voip_phone = ? "
				+ " order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipPhone);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据VOIP认证账号查询用户信息
	 * @param voipAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipAccount(String voipAccount)
	{
		String sql = "select a.user_id, b.device_id "
				+ " from tab_voip_serv_param a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.voip_username = ? "
				+ " order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	/**
	 * 根据用户设备SN查询设备
	 * @param devSn
	 * @return
	 */
	public List<HashMap<String, String>> queryDeviceByDevSN(String devSN)
	{
		String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
		String sql = " select a.device_id, a.device_serialnumber, c.user_id from  tab_gw_device a, tab_hgwcustomer c " +
				" where a.device_id=c.device_id and a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' ";
		PrepareSQL pSql = new PrepareSQL(sql);
		
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	/**
	 * 根据用户设备SN查询user_id
	 * @param devSn
	 * @return
	 */
	public HashMap<String, String> queryUserId(String devSN)
	{
		String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
		String sql = " select a.device_id,a.device_serialnumber, b.user_id from hgwcust_serv_info b, tab_hgwcustomer c, tab_gw_device a " +
				" where b.user_id=c.user_id and c.device_id=a.device_id and b.serv_type_id=11 and a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		HashMap<String, String> result = (HashMap<String, String>) DBOperation.getRecord(pSql.getSQL());
		return result;
	}
	
}
	

