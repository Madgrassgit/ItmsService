
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年10月19日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RoutToBridgeDAO
{

	public static final Logger logger = LoggerFactory.getLogger(RoutToBridgeDAO.class);

	/**
	 * 获取设备信息，包含状态信息
	 * 
	 * @param devId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getDevStatusInfo(String devId)
	{
		logger.debug("RoutToBridgeDAO==>getDevStatusInfo({})", devId);
		if (StringUtil.IsEmpty(devId))
		{
			logger.warn("devId is empty!");
			return null;
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.device_id,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id, c.online_status, b.city_id");
		psql.append(" from tab_gw_device b left join gw_devicestatus c on b.device_id = c.device_id ");
		psql.append(" where 1=1 ");
		psql.append("   and b.device_id = '" + devId + "'");
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 用于判断设备是否绑定用户，以及通过serv_type_id判断该用户是否有宽带业务
	 */
	public List<HashMap<String, String>> checkService(String device_id)
	{
		logger.debug("BridgeToRoutDAO==>checkService({})", device_id);
		if (null == device_id || "".equals(device_id))
		{
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		// List<Map<String, String>> rList = new ArrayList<Map<String,String>>();
		// Map<String, String> rsMap = null;
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.city_id,a.access_style_id, a.oui, a.device_serialnumber, b.vpiid, b.vciid, b.vlanid, b.username, b.passwd, b.wan_type, b.user_id ");
		psql.append("  from " + table_customer + " a, " + table_serv_info + " b ");
		psql.append(" where 1=1 ");
		psql.append("   and a.user_id = b.user_id ");
		psql.append("   and b.serv_type_id = 10 ");
		psql.append("   and a.device_id = '");
		psql.append(device_id);
		psql.append("'");
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		// for (HashMap<String, String> rs : list) {
		//
		// rsMap = new HashMap<String, String>();
		//
		// rsMap.put("access_style_id",
		// StringUtil.getStringValue(rs.get("access_style_id")));
		// rsMap.put("vpiid", StringUtil.getStringValue(rs.get("vpiid")));
		// rsMap.put("vciid", StringUtil.getStringValue(rs.get("vciid")));
		// rsMap.put("vlanid", StringUtil.getStringValue(rs.get("vlanid")));
		// rsMap.put("username", StringUtil.getStringValue(rs.get("username")));
		// rsMap.put("passwd", StringUtil.getStringValue(rs.get("passwd")));
		// rsMap.put("wan_type", StringUtil.getStringValue(rs.get("wan_type")));
		// rsMap.put("user_id", StringUtil.getStringValue(rs.get("user_id")));
		// rsMap.put("city_id", StringUtil.getStringValue(rs.get("city_id")));
		// rList.add(rsMap);
		// }
		return list;
	}

	/**
	 * 更新业务表的open_status、wlan_type字段
	 * @param userId
	 * @return
	 */
	public int updateServInfo(long userId)
	{
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		String sql = "update " + table_serv_info
				+ " set open_status=0, wan_type=1 where user_id=? and serv_type_id=10";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		int rs = DBOperation.executeUpdate(psql.getSQL());
		return rs;
	}
	
	/**
	 * 更新宽带密码，重庆路由改桥可能下发密码
	 * @param userId
	 * @param broadbandPassword
	 * @return
	 */
	public int updatePasswordServInfo(long userId,String broadbandPassword)
	{
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		String sql = "update " + table_serv_info
				+ " set passwd= '" + broadbandPassword + "' where user_id=? and serv_type_id=10";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		int rs = DBOperation.executeUpdate(psql.getSQL());
		return rs;
	}
}
