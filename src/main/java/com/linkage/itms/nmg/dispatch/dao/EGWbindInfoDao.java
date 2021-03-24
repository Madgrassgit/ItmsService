
package com.linkage.itms.nmg.dispatch.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
*    
* 项目名称：ailk-itms-ItmsService   
* 类名称：EGWbindInfoDao   
* 类描述：   
* 创建人：guxl3   
* 创建时间：2019年3月27日 下午2:38:16   
* @version
 */
public class EGWbindInfoDao
{

	/**
	 * 
	 * @Description 查询政企网关设备信息
	 * @author guxl3
	 * @date 2019年3月27日
	 * @param deviceId
	 * @return  
	 * @throws
	 */
	public ArrayList<HashMap<String, String>> queryDeviceBindInfoByDeV( String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		
		// mysql db
		if (3 == DBUtil.GetDB()) {
			psql.append(" select l.device_id,l.device_serialnumber,l.cpe_mac,l.cpe_allocatedstatus,l.complete_time,l.device_type,l.device_status,l.x_com_passwd, ");
			psql.append(" l.vendor_name,l.device_model,l.softwareversion,l.hardwareversion,l.lan_num,l.username,l.user_id,l.voice_num,l.city_id,serv.wan_type,serv.passwd");
			psql.append("  			from (select a.device_id,                   "); 
			psql.append("               a.device_serialnumber,                  "); 
			psql.append("               a.cpe_mac,                              "); 
			psql.append("               a.cpe_allocatedstatus,                  "); 
			psql.append("               a.complete_time,                        "); 
			psql.append("               a.device_type,                          "); 
			psql.append("               a.device_status,                        "); 
			psql.append("               b.vendor_name,                          "); 
			psql.append("               c.device_model,                         "); 
			psql.append("               d.softwareversion,                      "); 
			psql.append("               d.hardwareversion,                      "); 
			psql.append("               e.username,                             "); 
			psql.append("               g.lan_num,                              "); 
			psql.append("               g.voice_num,                            "); 
			psql.append("               e.user_id,                              "); 
			psql.append("               e.city_id,                              "); 
			psql.append("               a.x_com_passwd                          "); 
			psql.append("          from tab_gw_device       a,                  "); 
			psql.append("               tab_vendor          b,                  "); 
			psql.append("               gw_device_model     c,                  "); 
			psql.append("               tab_devicetype_info d,                  "); 
			psql.append("               tab_egwcustomer     e,                  "); 
			psql.append("               tab_bss_dev_port    g                   "); 
			psql.append("         where a.vendor_id = b.vendor_id               "); 
			psql.append("           and a.device_model_id = c.device_model_id   "); 
			psql.append("           and a.devicetype_id = d.devicetype_id       "); 
			psql.append("           and a.device_id = e.device_id               "); 
			psql.append("           and d.spec_id = g.id                        "); 
			psql.append("           and a.device_id ='"+deviceId+"') l                     "); 
			psql.append("  left join egwcust_serv_info serv                     "); 
			psql.append("    on l.user_id = serv.user_id and serv.serv_type_id=10   "); 
		}
		else
		{
			psql.append("select l.*, serv.wan_type, serv.passwd                 "); 
			psql.append("  			from (select a.device_id,                   "); 
			psql.append("               a.device_serialnumber,                  "); 
			psql.append("               a.cpe_mac,                              "); 
			psql.append("               a.cpe_allocatedstatus,                  "); 
			psql.append("               a.complete_time,                        "); 
			psql.append("               a.device_type,                          "); 
			psql.append("               a.device_status,                        "); 
			psql.append("               b.vendor_name,                          "); 
			psql.append("               c.device_model,                         "); 
			psql.append("               d.softwareversion,                      "); 
			psql.append("               d.hardwareversion,                      "); 
			psql.append("               e.username,                             "); 
			psql.append("               g.lan_num,                              "); 
			psql.append("               g.voice_num,                            "); 
			psql.append("               e.user_id,                              "); 
			psql.append("               e.city_id,                              "); 
			psql.append("               a.x_com_passwd                          "); 
			psql.append("          from tab_gw_device       a,                  "); 
			psql.append("               tab_vendor          b,                  "); 
			psql.append("               gw_device_model     c,                  "); 
			psql.append("               tab_devicetype_info d,                  "); 
			psql.append("               tab_egwcustomer     e,                  "); 
			psql.append("               tab_bss_dev_port    g                   "); 
			psql.append("         where a.vendor_id = b.vendor_id               "); 
			psql.append("           and a.device_model_id = c.device_model_id   "); 
			psql.append("           and a.devicetype_id = d.devicetype_id       "); 
			psql.append("           and a.device_id = e.device_id               "); 
			psql.append("           and d.spec_id = g.id                        "); 
			psql.append("           and a.device_id ='"+deviceId+"') l                     "); 
			psql.append("  left join egwcust_serv_info serv                     "); 
			psql.append("    on l.user_id = serv.user_id and serv.serv_type_id=10   "); 
		}
		
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}

	public List<HashMap<String,String>> getVoipAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  voip_phone ");
		psql.append("  from tab_egw_voip_serv_param ");
		psql.append(" where 1=1");
		psql.append("   and user_id = "+userId );
		return DBOperation.getRecords(psql.getSQL());
		
	}
	
	public List<HashMap<String,String>> getIptvAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from egwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=11 ");
		psql.append("   and user_id = "+userId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public String getVoipOpenStatus(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from egwcust_serv_info ");
		psql.append("where 1=1 and serv_type_id=14 ");
		psql.append("   and user_id = "+userId);
		List<HashMap<String,String>> voipOpenStatusList = DBOperation.getRecords(psql.getSQL());
		if(voipOpenStatusList != null && !voipOpenStatusList.isEmpty()){
			return StringUtil.getStringValue(voipOpenStatusList.get(0),"open_status", "");
		}else{
			return "";
		}
	}
	
	
	public List<HashMap<String,String>> getPppoeAccount(String userId) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username,user_id,serv_type_id,open_status from egwcust_serv_info ");
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
