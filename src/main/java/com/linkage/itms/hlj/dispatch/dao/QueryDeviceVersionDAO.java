
package com.linkage.itms.hlj.dispatch.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

public class QueryDeviceVersionDAO
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersionDAO.class);

	/**
	 * 根据设备ID查询设备厂商，设备型号，软件版本等信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getDeviceVersion(String deviceId)
	{
		logger.debug("QueryDeviceVersionDAO==>getDeviceVersion({})",
				new Object[] { deviceId });
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select a.device_serialnumber,a.loopback_ip, a.city_id, a.loopback_ip, a.cpe_currentstatus, ")
				.append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion, c.is_check, c.rela_dev_type_id ")
				.append(" from tab_gw_device a, tab_vendor b, tab_devicetype_info c, gw_device_model d  ")
				.append("where 1=1 ")
				.append("  and a.vendor_id = b.vendor_id ")
				.append("  and a.device_model_id = d.device_model_id ")
				.append("  and a.devicetype_id = c.devicetype_id and a.device_id='"
						+ deviceId + "'");
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		return DBOperation.getRecord(psql.getSQL());
	}
}
