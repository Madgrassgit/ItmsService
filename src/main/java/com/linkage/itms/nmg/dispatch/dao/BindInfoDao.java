
package com.linkage.itms.nmg.dispatch.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-6-13
 * @category com.linkage.itms.nmg.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class BindInfoDao
{

	/**
	 * 
	 * @author 岩 
	 * @date 2016-6-17
	 * @param deviceId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceBindInfoByDeV( String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		
		// mysql db
		if (3 == DBUtil.GetDB()) 
		{
			psql.append("select l.device_id,l.device_serialnumber,l.cpe_mac,l.cpe_allocatedstatus,l.complete_time,l.device_type, l.device_status ,l.vendor_name, l.device_model,l.softwareversion,l.hardwareversion,l.username,l.lan_num,l.user_id,l.city_id,l.x_com_passwd,serv.wan_type,serv.passwd from(");
			psql.append(" select a.device_id,a.device_serialnumber,a.cpe_mac,a.cpe_allocatedstatus,a.complete_time,a.device_type, a.device_status ,b.vendor_name, c.device_model,d.softwareversion,d.hardwareversion,e.username,g.lan_num,e.user_id,e.city_id,a.x_com_passwd  ");
			psql.append(" from tab_gw_device a, tab_vendor b,gw_device_model c,tab_devicetype_info d, tab_hgwcustomer e ,tab_bss_dev_port g  ");
			psql.append(" where a.vendor_id = b.vendor_id and a.device_model_id = c.device_model_id and a.devicetype_id = d.devicetype_id and a.device_id =e.device_id   and d.spec_id = g.id ");
			psql.append(" and a.device_id = '" + deviceId + "'");
			psql.append(") l left join hgwcust_serv_info serv on l.user_id = serv.user_id");
		}
		else
		{
			psql.append("select l.*,serv.wan_type,serv.passwd from(");
			psql.append(" select a.device_id,a.device_serialnumber,a.cpe_mac,a.cpe_allocatedstatus,a.complete_time,a.device_type, a.device_status ,b.vendor_name, c.device_model,d.softwareversion,d.hardwareversion,e.username,g.lan_num,e.user_id,e.city_id,a.x_com_passwd  ");
			psql.append(" from tab_gw_device a, tab_vendor b,gw_device_model c,tab_devicetype_info d, tab_hgwcustomer e ,tab_bss_dev_port g  ");
			psql.append(" where a.vendor_id = b.vendor_id and a.device_model_id = c.device_model_id and a.devicetype_id = d.devicetype_id and a.device_id =e.device_id   and d.spec_id = g.id ");
			psql.append(" and a.device_id = '" + deviceId + "'");
			psql.append(") l left join hgwcust_serv_info serv on l.user_id = serv.user_id");
		}
		
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}

	public String getVoipPhone(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  voip_phone ");
		psql.append("  from tab_voip_serv_param ");
		psql.append(" where 1=1");
		psql.append("   and user_id = "+userId );
		List<HashMap<String,String>> voipPhoneList = DBOperation.getRecords(psql.getSQL());
		if(voipPhoneList != null && !voipPhoneList.isEmpty()){
			return StringUtil.getStringValue(voipPhoneList.get(0),"voip_phone", "");
		}else{
			return "";
		}
	}
	
	public List<HashMap<String,String>> getIptvAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from hgwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=11 ");
		psql.append("   and user_id = "+userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public String getVoipOpenStatus(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from hgwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=14 ");
		psql.append("   and user_id = "+userId);
		List<HashMap<String,String>> voipOpenStatusList = DBOperation.getRecords(psql.getSQL());
		if(voipOpenStatusList != null && !voipOpenStatusList.isEmpty()){
			return StringUtil.getStringValue(voipOpenStatusList.get(0),"open_status", "");
		}else{
			return "";
		}
	}
	
	public String getComepareStatus(String loid) {
		if(null ==loid || loid.isEmpty())
		{
			return "";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select comeparestatus from devsn_compare_log ");
		psql.append("where loid = '" + loid + "'");
		List<HashMap<String,String>> getComepareStatus = DBOperation.getRecords(psql.getSQL());
		if(getComepareStatus != null && !getComepareStatus.isEmpty()){
			return StringUtil.getStringValue(getComepareStatus.get(0),"comeparestatus", "");
		}else{
			return "1";
		}
	}
	
	public List<HashMap<String,String>> getPppoeAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from hgwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=10 ");
		psql.append("   and user_id = "+userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public String getAccessType(String deviceId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  f.type_name  ");
		psql.append("  from tab_gw_device a, tab_devicetype_info d,gw_access_type f");
		psql.append(" where 1=1 and a.devicetype_id = d.devicetype_id and d.access_style_relay_id = f.type_id ");
		psql.append(" and a.device_id = '" + deviceId + "'");
		List<HashMap<String,String>> AccessTypeList = DBOperation.getRecords(psql.getSQL());
		if(AccessTypeList != null && !AccessTypeList.isEmpty()){
			return StringUtil.getStringValue(AccessTypeList.get(0),"type_name", "");
		}else{
			return "";
		}
	}
}
