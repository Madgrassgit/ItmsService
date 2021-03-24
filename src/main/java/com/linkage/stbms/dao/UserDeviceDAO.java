package com.linkage.stbms.dao;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * @author Jason(3412)
 * @date 2009-12-15
 */
public class UserDeviceDAO {

	private static Logger logger = LoggerFactory.getLogger(UserDeviceDAO.class);

	/**
	 * 获取用户设备信息
	 * 
	 * @param 用户账号
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getUserDevInfo(String username) {
		logger.debug("getUserDevInfo({})", username);

		if (StringUtil.IsEmpty(username)) {
			logger.warn("username is empty!");
			return null;
		}

		String strSQL = "select b.device_id,b.vendor_id,b.device_model_id,b.devicetype_id,b.device_serialnumber,b.loopback_ip,c.online_status"
				+ " from tab_customer a left join tab_gw_device b on a.customer_id=b.customer_id"
				+ " left join gw_devicestatus c on b.device_id=c.device_id"
				+ " where a.cust_stat in ('1','2') and a.cust_account='"
				+ username + "'";
		PrepareSQL psql = new PrepareSQL(strSQL);
		return DBOperation.getRecord(psql.getSQL());
	}
	/**
	 * oui+设备sn获取设备信息
	 * @author zhangsm
	 * @date 2011-02-23
	 * @param devSn
	 * @param devOui
	 * @return Map<String, String>
	 */
	public Map<String, String> getStbDeviceInfo(String devSn, String devOui) {

		logger.debug("getUserDevInfo()", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.warn("devSn is empty!");
			return null;
		}
		if (StringUtil.IsEmpty(devOui)) {
			logger.warn("devSn is empty!");
			return null;
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.device_id,b.vendor_id,b.device_model_id,b.devicetype_id,b.device_serialnumber,b.loopback_ip,c.online_status");
		psql.append(" from stb_tab_gw_device b left join stb_gw_devicestatus c on b.device_id=c.device_id");
		psql.append(" where b.device_serialnumber='");
		psql.append(devSn);
		psql.append("'");
		psql.append(" and b.oui='");
		psql.append(devOui);
		psql.append("'");
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据型号ID获取设备的厂商，型号的名称
	 * 
	 * @param 型号ID
	 * @author Jason(3412)
	 * @date 2009-12-16
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevVendorModelVersion(String deviceTypeId) {
		logger.debug("getDevVendorModel({})", deviceTypeId);
		String strSQL = "select a.vendor_name, a.vendor_add, b.device_model, c.softwareversion "
				+ " from gw_vendor a, gw_device_model b, tab_devicetype_info c"
				+ " where c.devicetype_id="
				+ deviceTypeId
				+ " and b.device_model_id=c.device_model_id and a.vendor_id=b.vendor_id";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setStringExt(1, deviceTypeId, false);
		return DBOperation.getRecord(psql.getSQL());
	}
}
