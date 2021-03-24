package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class ZongDiaoServUserDao {

	private static Logger logger = LoggerFactory.getLogger(ZongDiaoServUserDao.class);
	
	
	/**
	 * 根据user_id 到客户终端表中获取终端类型
	 * 
	 * @param userId
	 * @return
	 */
	public String getDevType(String userId){
		
		logger.debug("ZongDiaoServUserDao==>getDevType({})", userId);
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select type_id ");
		psql.append("  from gw_cust_user_dev_type ");
		psql.append(" where 1=1 ");
		psql.append("   and user_id = "+userId);
		
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		
		String devType = "";
		if("1".equals(StringUtil.getStringValue(map.get("type_id")))){
			devType = "E8-B";
		}else {
			devType = "E8-C";
		}
		return devType;
	}
	
	
	/**
	 * 根据deviceId 查询该设备的版本是否是规范版本
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getVersionIsCheckOrNot(String deviceId) {
		
		logger.debug("ZongDiaoServUserDao==>getVersionIsCheckOrNot({})", new Object[]{deviceId});
		String table_customer = "tab_hgwcustomer";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id, a.device_serialnumber, a.city_id, b.is_check,b.spec_id d_spec_id,c.spec_id u_spec_id");
		psql.append("  from tab_gw_device a, tab_devicetype_info b, " + table_customer + " c");
		psql.append(" where 1=1 ");
		psql.append("   and a.devicetype_id = b.devicetype_id and a.device_id = c.device_id");
		psql.append("   and a.device_id = '");
		psql.append(deviceId);
		psql.append("'");
		
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		
		return map;
	}
	
}
