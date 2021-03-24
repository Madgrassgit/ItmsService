package com.linkage.itms.hlj.dispatch.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;


/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-25
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class BindInfoDAO
{
	
	public Map<String, String> queryDeviceBindInfoByDeV( String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_model_id,a.vendor_id,a.device_id,a.oui,a.device_serialnumber,a.cpe_allocatedstatus,a.complete_time,a.device_type, a.device_status ,b.vendor_name, ");
		psql.append(" a.cpe_mac,c.device_model,d.softwareversion,d.hardwareversion,d.is_check,e.username,f.type_name ,g.lan_num,g.wlan_num,e.user_id,e.city_id,h.online_status,d.access_style_relay_id  ");
		psql.append(" from tab_gw_device a, tab_vendor b,gw_device_model c,tab_devicetype_info d, tab_hgwcustomer e,gw_access_type f,tab_bss_dev_port g,gw_devicestatus h  ");
		psql.append(" where a.vendor_id = b.vendor_id and a.device_model_id = c.device_model_id and a.devicetype_id = d.devicetype_id and a.device_id =e.device_id and d.access_style_relay_id = f.type_id and d.spec_id = g.id and a.device_id =h.device_id");
		psql.append(" and a.device_id = '" + deviceId + "'");
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
	
	/***
	 * 政企相关信息查询
	 * @param deviceId
	 * @return
	 * 2019-10-29
	 */
	public Map<String, String> queryDeviceBindInfoByDeVQiye(String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_model_id,a.vendor_id,a.device_id,a.oui,a.device_serialnumber,a.cpe_allocatedstatus,a.complete_time,a.device_type, a.device_status ,b.vendor_name, ");
		psql.append(" a.cpe_mac,c.device_model,d.softwareversion,d.hardwareversion,d.is_check,e.username,f.type_name ,g.lan_num,g.wlan_num,e.user_id,e.city_id,h.online_status,d.access_style_relay_id  ");
		psql.append(" from tab_gw_device a, tab_vendor b,gw_device_model c,tab_devicetype_info d, tab_egwcustomer e,gw_access_type f,tab_bss_dev_port g,gw_devicestatus h  ");
		psql.append(" where a.vendor_id = b.vendor_id and a.device_model_id = c.device_model_id and a.devicetype_id = d.devicetype_id and a.device_id =e.device_id and d.access_style_relay_id = f.type_id and d.spec_id = g.id and a.device_id =h.device_id");
		psql.append(" and a.device_id = '" + deviceId + "'");
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
	
	/**
	 * 获取多语音
	 * @author 岩 
	 * @date 2016-8-11
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getVoipAccount(String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.voip_phone username from tab_hgwcustomer a, tab_voip_serv_param b ");
		psql.append("where a.user_id=b.user_id and a.user_id = ? ");
		psql.setString(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	/***
	 * 获取多语音(政企)
	 * @param userId
	 * @return
	 * 2019-10-29
	 */
	public ArrayList<HashMap<String, String>> getVoipAccountQiye(String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.voip_phone username from tab_egwcustomer a, tab_egw_voip_serv_param b ");
		psql.append("where a.user_id=b.user_id and a.user_id = ? ");
		psql.setString(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public Map<String, String> queryRateDeV( String device_model_id, String vendor_id)
	{
		PrepareSQL psql = new PrepareSQL();
		// mysql db
		if (3 == DBUtil.GetDB()) {
			psql.append(" select VENDOR_ID from tab_device_model_attribute where VENDOR_ID='"+vendor_id+"' and device_model_id='"+device_model_id+"'");
		} else {
			psql.append(" select * from tab_device_model_attribute where VENDOR_ID='"+vendor_id+"' and device_model_id='"+device_model_id+"'");
		}
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
	
	/**
	 * 获取宽带
	 * @author 岩 
	 * @date 2016-8-11
	 * @param userId
	 * @return
	 */
	public String getPppoeAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id from hgwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=10 ");
		psql.append("   and user_id = "+userId);
		List<HashMap<String,String>> PppoeList = DBOperation.getRecords(psql.getSQL());
		if(PppoeList != null && !PppoeList.isEmpty()){
			return StringUtil.getStringValue(PppoeList.get(0),"username", "");
		}else{
			return "";
		}
	}
	/***
	 * 获取宽带(政企)
	 * @param userId
	 * @return
	 * 2019-10-29
	 */
	public String getPppoeAccountQiye(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id from egwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=10 ");
		psql.append("   and user_id = "+userId);
		List<HashMap<String,String>> PppoeList = DBOperation.getRecords(psql.getSQL());
		if(PppoeList != null && !PppoeList.isEmpty()){
			return StringUtil.getStringValue(PppoeList.get(0),"username", "");
		}else{
			return "";
		}
	}
	
	/**
	 * 
	 * @author 岩 
	 * @date 2016-8-11
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getCpeActive(String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.username, b.open_status, c.result_id,d.fault_reason from tab_hgwcustomer a, hgwcust_serv_info b, gw_serv_strategy c ,tab_cpe_faultcode d");
		psql.append(" where a.device_id = c.device_id and b.username = c.username and a.user_id=b.user_id and c.result_id=d.fault_code");
		psql.append(" and c.service_id=1001 and b.serv_status=1 and b.serv_type_id = 10 and a.user_id = ? order by c.end_time");
		psql.setString(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/***
	 * 获取政企相关信息
	 * @param userId
	 * @return
	 * 2019-10-29
	 */
	public ArrayList<HashMap<String, String>> getCpeActiveQiye(String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.username, b.open_status, c.result_id,d.fault_reason from tab_egwcustomer a, egwcust_serv_info b, gw_serv_strategy c ,tab_cpe_faultcode d");
		psql.append(" where a.device_id = c.device_id and b.username = c.username and a.user_id=b.user_id and c.result_id=d.fault_code");
		psql.append(" and c.service_id=1001 and b.serv_status=1 and b.serv_type_id = 10 and a.user_id = ? order by c.end_time");
		psql.setString(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}
}
