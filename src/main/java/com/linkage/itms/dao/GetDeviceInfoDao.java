package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * @author songxq
 * @version 1.0
 * @since 2020年1月8日 下午3:07:27
 * @category 
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetDeviceInfoDao
{
	public List<HashMap<String, String>> getDeviceInfoByAccount(String accountServ)
	{
		
		
		String sql = " select c.hardwareversion,c.softwareversion,e.device_model,d.vendor_name,f.device_serialnumber,f.cpe_mac"
				+ " from hgwcust_serv_info a,tab_hgwcustomer b ,tab_gw_device f,"
				+ "tab_devicetype_info c ,tab_vendor d,gw_device_model e "
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and b.device_id = f.device_id "
				+ "	and f.devicetype_id = c.devicetype_id and c.vendor_id = d.vendor_id and c.device_model_id = e.device_model_id "
				+ " and a.username=?  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, accountServ);
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	public List<HashMap<String, String>> getDeviceInfoByLoid(String loid)
	{
		
		String sql = " select c.hardwareversion,c.softwareversion,e.device_model,d.vendor_name,f.device_serialnumber,f.cpe_mac"
				+ " from tab_hgwcustomer b ,tab_gw_device f,"
				+ "tab_devicetype_info c ,tab_vendor d,gw_device_model e "
				+ " where b.device_id = f.device_id and f.devicetype_id = c.devicetype_id and c.vendor_id = d.vendor_id "
				+ "and c.device_model_id = e.device_model_id  and b.username=? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		return DBOperation.getRecords(pSql.getSQL());
	}
}

