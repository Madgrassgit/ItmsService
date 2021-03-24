package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (新疆电信)查询家庭网关最新在线时间接口
 */
public class LatestOnlineTimeDAO {
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
	 * @param
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
	 * @param
	 * @return
	 */
	public List<HashMap<String, String>> queryDeviceByDevSN(String devSN)
	{
		String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
		String sql = " select a.device_id,a.oui,a.device_serialnumber from tab_gw_device a where a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' ";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	

	
	/**
	 * 根据版本ID查询版本信息
	 * @param deviceTypeId
	 * @return
	 */
//	public Map<String,String> getDeviceVersionInfo(String deviceTypeId){//不被使用
//		String strSQL = "select * from tab_devicetype_info a, tab_device_version_attribute b" +
//				" where a.devicetype_id = b.devicetype_id and a.devicetype_id = " + deviceTypeId;
//		return DBOperation.getRecord(strSQL);
//	}

	public List<HashMap<String, String>> queryLatestOnlineTimeByDevId(String deviceId) {
		String sql = " select a.device_id,b.oui,b.device_serialnumber,a.last_time  from GW_DEVICESTATUS a  left join tab_gw_device b on a.device_id = b.device_id  where a.DEVICE_ID ='" + deviceId + "' ";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
}
