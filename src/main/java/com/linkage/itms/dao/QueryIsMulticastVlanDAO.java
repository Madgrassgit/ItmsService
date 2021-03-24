package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;

/**
 * (新疆电信)检查光猫版本是否支持组播接口
 * @author chenxj6
 * @since 2016-8-26
 * 
 */
public class QueryIsMulticastVlanDAO {
	/**
	 * 根据用户宽带账号查询用户信息
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = " select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by b.updatetime desc ";
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
		String sql = " select distinct(a.user_id),a.device_id from tab_hgwcustomer a where a.username= ? order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户customer_id查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByCustomerId(String customerId)
	{
		String sql = " select distinct(a.user_id),a.device_id from tab_hgwcustomer a where a.customer_id= ? order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, customerId);
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
		String sql = " select a.user_id, b.device_id "
				+ " from hgwcust_serv_info a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=?  order by b.updatetime desc ";
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
		String sql = " select a.user_id, b.device_id "
				+ " from tab_voip_serv_param a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.voip_phone = ? "
				+ " order by b.updatetime desc";
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
		String sql = " select a.user_id, b.device_id "
				+ " from tab_voip_serv_param a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and a.voip_username = ? "
				+ " order by b.updatetime desc";
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
		String sql = " select a.device_id,a.device_serialnumber from tab_gw_device a where a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' ";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	/**
	 * 根据用户设备 device_id 检查光猫版本是否支持组播 1：支持；2：不支持；
	 * @param device_id
	 * @return
	 */
	public List<HashMap<String, String>> queryIsMulticastByDevId(String deviceId)
	{
		String sql = "select a.is_multicast from tab_devicetype_info a,tab_gw_device b where a.DEVICETYPE_ID = b.DEVICETYPE_ID " +
				" and b.DEVICE_ID='"+deviceId+"'";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户设备 device_id 检查光猫版本是否支持百兆宽带 1：支持；2：不支持；
	 * @param device_id
	 * @return
	 */
	public List<HashMap<String, String>> queryIsMbBroadBandByDevId(String deviceId)
	{
		String sql = "select a.* from tab_devicetype_info a,tab_gw_device b where a.devicetype_id = b.devicetype_id " +
				" and b.device_id='"+deviceId+"'";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select a.mbbroadband, a.devicetype_id from tab_devicetype_info a,tab_gw_device b where a.devicetype_id = b.devicetype_id " +
					" and b.device_id='"+deviceId+"'";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据版本ID查询版本信息
	 * @param deviceTypeId
	 * @return
	 */
	public Map<String,String> getDeviceVersionInfo(String deviceTypeId){
		String strSQL = "select * from tab_devicetype_info a, tab_device_version_attribute b" +
				" where a.devicetype_id = b.devicetype_id and a.devicetype_id = " + deviceTypeId;
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select b.gigabit_port, b.wifi, b.download_max_wifi, b.wifi_frequency, b.gigabit_port_type, b.download_max_lan from tab_devicetype_info a, tab_device_version_attribute b" +
					" where a.devicetype_id = b.devicetype_id and a.devicetype_id = " + deviceTypeId;
		}
		return DBOperation.getRecord(strSQL);
	}
}
