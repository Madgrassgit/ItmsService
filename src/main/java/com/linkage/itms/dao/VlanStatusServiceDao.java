package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.dispatch.service.VlanStatusService;

public class VlanStatusServiceDao {
	
	/**
	 * 获得上行方式 
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
	private static Logger logger = LoggerFactory.getLogger(VlanStatusService.class);
	/**
	 * 获得上行方式 
	 * @param deviceId
	 * @return
	 */
	public  String selectLoidInTempTable(String DEVICE_MODEL)
	{
		PrepareSQL pSQL = new PrepareSQL();
		String sql = "select DEVICE_MODEL from tab_temp_vlanstatus where DEVICE_MODEL = ?";
		pSQL.setSQL(sql);
		pSQL.setString(1, DEVICE_MODEL);
		Map<String, String> modelMap = DBOperation.getRecord(pSQL.getSQL());
		if(null==modelMap)
			return "";
		return modelMap.get("device_model");
	}
	/**
	 * 获得上行方式 
	 * @param deviceId
	 * @return
	 */
	public  String selectWanType(String device_id)
	{
		String sql = " select a.wan_type from hgwcust_serv_info a, tab_hgwcustomer b "
				+ " where a.user_id = b.user_id and b.device_id=? and a.serv_type_id=10 ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, device_id);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		if(result == null || result.isEmpty()){
			return "";
		}else{
			if("1".equals(result.get("wan_type"))){
				return "桥接";
			}else if("2".equals(result.get("wan_type"))){
				return "路由";
			}
		}
		return "";
	}
}
