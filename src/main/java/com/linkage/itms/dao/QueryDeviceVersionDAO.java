package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;


public class QueryDeviceVersionDAO {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersionDAO.class);
	
	
	/**
	 * 
	 * 根据设备ID查询设备厂商，设备型号，软件版本等信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getDeviceVersion(String deviceId){
		logger.debug("QueryDeviceVersionDAO==>getDeviceVersion({})",
				new Object[] { deviceId });
		
		StringBuffer sql = new StringBuffer();
		
		if ("ah_dx".equals(Global.G_instArea)) {
			sql.append("select a.device_serialnumber, a.city_id, a.loopback_ip, f.online_status as cpe_currentstatus, ")
			   .append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion, c.rela_dev_type_id ")
			   .append(" from tab_gw_device a, tab_vendor b, tab_devicetype_info c, gw_device_model d ,gw_devicestatus f ")
			   .append("where 1=1 ")
			   .append("  and a.vendor_id = b.vendor_id ")
			   .append("  and a.device_model_id = d.device_model_id ")
			   .append("  and a.device_id = f.device_id ")
			   .append("  and a.devicetype_id = c.devicetype_id and a.device_id='"+deviceId+"'");
		}
		else if("js_dx".equals(Global.G_instArea)){
			sql.append("select x.*,f.spec_desc from( select c.spec_id, a.device_serialnumber,a.loopback_ip, a.city_id, a.cpe_currentstatus, ")
			   .append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion, c.is_check, c.rela_dev_type_id ")
			   .append(" from tab_gw_device a, tab_vendor b, tab_devicetype_info c, gw_device_model d  ")
			   .append("where 1=1 ")
			   .append("  and a.vendor_id = b.vendor_id ")
			   .append("  and a.device_model_id = d.device_model_id ")
			   .append("  and a.devicetype_id = c.devicetype_id and a.device_id='"+deviceId+"') x left join tab_bss_dev_port f on f.id = x.spec_id ");
		}
		else if("nx_dx".equals(Global.G_instArea)){
			sql.append(
					"select a.device_serialnumber, a.device_model_id, a.city_id, a.loopback_ip, a.cpe_currentstatus, ")
			   .append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion, c.is_check, c.rela_dev_type_id ")
			   .append(" from tab_gw_device a, tab_vendor b, tab_devicetype_info c, gw_device_model d  ")
			   .append("where 1=1 ")
			   .append("  and a.vendor_id = b.vendor_id ")
			   .append("  and a.device_model_id = d.device_model_id ")
			   .append("  and a.devicetype_id = c.devicetype_id and a.device_id='"+deviceId+"'");
		}
		else {
			sql.append("select a.device_serialnumber, a.city_id, a.loopback_ip, a.cpe_currentstatus, ")
			   .append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion, c.is_check, c.rela_dev_type_id ")
			   .append(" from tab_gw_device a, tab_vendor b, tab_devicetype_info c, gw_device_model d  ")
			   .append("where 1=1 ")
			   .append("  and a.vendor_id = b.vendor_id ")
			   .append("  and a.device_model_id = d.device_model_id ")
			   .append("  and a.devicetype_id = c.devicetype_id and a.device_id='"+deviceId+"'");
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 
	 * 根据设备型号ID查询网口数量和网口速率
	 * 
	 * @param 
	 * @return
	 */
	public Map<String, String> getEtherMessage(String deviceModelId){
		logger.debug("QueryDeviceVersionDAO==>getEtherMessage({})",
				new Object[] { deviceModelId });
		StringBuffer sql = new StringBuffer();
			sql.append("select ethernum, etherrate from ether_num_rate ")
			   .append("where device_model_id='"+deviceModelId+"'");
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		return DBOperation.getRecord(psql.getSQL());
	}
	/**
	 *@描述 根据设备序列号判断设备是否存在维护表中
	 *@参数  [deviceSerialnumber]
	 *@返回值  java.util.Map<java.lang.String,java.lang.String>
	 *@创建人  lsr
	 *@创建时间  2020/3/11
	 *@throws
	 *@修改人和其它信息
	 */
	public Map<String, String> getDeviceCountBySn(String deviceSerialnumber){
		logger.debug("QueryDeviceVersionDAO==>getDeviceCountBySn({})",
				new Object[] { deviceSerialnumber });
		PrepareSQL psql = new PrepareSQL("select count(*) as num from tab_device_e8c_remould where device_serialnumber= ?");
		psql.setString(1,deviceSerialnumber);
		return DBOperation.getRecord(psql.getSQL());
	}
//	/**
//	 * 查看设备是否在 tab_notsupport_200m 表里
//	 * @param deviceId
//	 * @return
//	 */
//	public Map<String, String> getIs200M(String deviceId){
//		logger.debug("getIs200M,deviceId:({})", new Object[] { deviceId });
//		
//		StringBuffer sql = new StringBuffer();
//		
//		sql.append("select a.device_id, b.devicetype_id ")
//		   .append(" from tab_gw_device a, tab_notsupport_200m b ")
//		   .append(" where 1=1 ")
//		   .append("  and a.devicetype_id = b.devicetype_id ")
//		   .append("  and a.device_id='"+deviceId+"'");
//		
//		PrepareSQL psql = new PrepareSQL();
//		psql.setSQL(sql.toString());
//		
//		return DBOperation.getRecord(psql.getSQL());
//	}
	
	/**
	 * 查看设备是否在 tab_device_version_attribute 表里
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getDevVersionAttribute(String deviceId){
		logger.debug("getDevVersionAttribute,deviceId:({})", new Object[] { deviceId });
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select a.device_id, b.is_support200, b.is_support500 from tab_gw_device a,tab_device_version_attribute b ")
		   .append(" where a.devicetype_id = b.devicetype_id and a.device_id='"+deviceId+"'");
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		return DBOperation.getRecord(psql.getSQL());
	}
}
