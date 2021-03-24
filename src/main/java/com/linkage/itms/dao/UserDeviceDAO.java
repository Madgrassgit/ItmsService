package com.linkage.itms.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBAdapter;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.DbUtils;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.dispatch.obj.OpenFristChecker;
import com.linkage.system.utils.StringUtils;

/**
 * 用户表和设备表操作类
 * 
 * @author Jason(3412)
 * @date 2009-12-15
 */
public class UserDeviceDAO {

	private static Logger logger = LoggerFactory.getLogger(UserDeviceDAO.class);
	private static long MAX_UNUSED_DEVICEID = -1L;
	private static long MIN_UNUSED_DEVICEID = -1L;
	private static int SUM_UNUSED_DEVICEID = 50;



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

		String strSQL = "select a.user_id,b.device_id,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id,"
				+ " c.online_status, a.city_id"
				+ " from tab_hgwcustomer a left join tab_gw_device b on a.device_id=b.device_id"
				+ " left join gw_devicestatus c on a.device_id=c.device_id"
				+ " where a.user_state in ('1','2')"
				+ " and a.username='"
				+ username + "'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	/**
	 * 获取用户设备信息
	 * 
	 * @param 用户账号
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevStatus(String userId) {
		logger.debug("getDevStatus({})", userId);

		if (StringUtil.IsEmpty(userId)) {
			logger.warn("userId is empty!");
			return null;
		}
		
		String table_customer = "tab_hgwcustomer";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}

		String strSQL = "select a.user_id,b.device_id,b.complete_time,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id,"
				+ " c.online_status, a.city_id"
				+ " from " + table_customer + " a left join tab_gw_device b on a.device_id=b.device_id"
				+ " left join gw_devicestatus c on a.device_id=c.device_id"
				+ " where a.user_state in ('1','2')"
				+ " and a.user_id="
				+ userId ;
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	/**
	 * 获取设备WAN口状态
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevWanInfo(String deviceId) {
		logger.debug("getDevWanInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}
		
		PrepareSQL pSQL = new PrepareSQL();
		
		pSQL.setSQL("select a.access_type,b.vpi_id,b.vci_id,b.vlan_id,c.conn_status," +
				"c.last_conn_error from gw_wan a,gw_wan_conn b,gw_wan_conn_session c " +
				" where a.device_id=b.device_id and a.wan_id=b.wan_id and " +
				" b.device_id=c.device_id and b.wan_conn_id=c.wan_conn_id " +
				" and c.serv_list='OTHER' and a.device_id=?");
		pSQL.setString(1, deviceId);
		
		return DBOperation.getRecord(pSQL.getSQL());
	}

	/**
	 * 获取设备WAN口状态
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevQosInfo(String deviceId) {
		logger.debug("getDevQosInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}

		PrepareSQL sql = new PrepareSQL("select qos_mode, enable from gw_qos where device_id=?");
		sql.setString(1, deviceId);
		
		return DBOperation.getRecord(sql.getSQL());
		
	}
	
	/**
	 * 根据型号ID获取设备的厂商，型号的名称
	 * 
	 * @param 型号ID
	 * @author Jason(3412)
	 * @date 2009-12-16
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevVendorModel(String deviceModelId) {
		logger.debug("getDevVendorModel({})", deviceModelId);
		String strSQL = "select a.vendor_name, a.vendor_add, b.device_model from tab_vendor a, gw_device_model b"
				+ " where b.device_model_id='"
				+ deviceModelId
				+ "' and a.vendor_id=b.vendor_id";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}

	/**
	 * 根据用户帐号获取其绑定设备的电信维护密码
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public Map<String, String> getTelePasswdByUsername(String userId) {
		logger.debug("getTelePasswdByUsername({})", userId);
		String table_customer = "tab_hgwcustomer";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		String strSQL = "select b.device_serialnumber,b.oui, b.cpe_allocatedstatus,a.user_id, b.device_id, b.x_com_passwd, a.city_id, b.devicetype_id";
		if("cq_dx".equals(Global.G_instArea)){
			strSQL += ",u.online_status cpe_currentstatus";
		}
		strSQL = strSQL +  " from " + table_customer + " a left join tab_gw_device b on a.device_id=b.device_id";
		if("cq_dx".equals(Global.G_instArea)){
			strSQL += " left join gw_devicestatus u on a.device_id=u.device_id";
		}
		
		strSQL = strSQL	+ " where a.user_state='1' and a.user_id=" + userId;
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}

	
	/**
	 * 根据序列号获取设备信息
	 * 
	 * @param
	 * @author fanjm(35572)
	 * @date 2017-8-1
	 * @return Map<String, String>
	 */
	public Map<String, String> getDevInfoBySn(String sn) {
		logger.debug("getDevInfoBySn({})", sn);
		String table_customer = "tab_hgwcustomer";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		String strSQL = "select b.device_serialnumber,b.oui, b.cpe_allocatedstatus,a.user_id, b.device_id, b.x_com_passwd, a.city_id, b.devicetype_id"
				+ " from " + table_customer + " a left join tab_gw_device b on a.device_id=b.device_id"
				+ " where a.user_state='1' and b.device_serialnumber='" + sn +"'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	
	/**
	 * 根据设备序列号(至少最后6位)获取其电信维护密码
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public ArrayList<HashMap<String, String>> getTelePasswdByDevSn(String devSn) {
		logger.debug("getTelePasswdByDevSn({})", devSn);
		String table_customer = "tab_hgwcustomer";
		String gw_type = " and gw_type = 1 ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			gw_type = " and gw_type = 2 ";
		}
		String strSQL = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,a.loopback_ip,";
		if("cq_dx".equals(Global.G_instArea)){
			strSQL += "a.cpe_currentstatus,b.adsl_hl,";
		}
		strSQL = strSQL	+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
				+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id"
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	
	/**
	 * 根据设备序列号(至少最后6位)获取其电信维护密码
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public ArrayList<HashMap<String, String>> getTelePasswdByDevSn1(String devSn,String oui) {
		logger.debug("getTelePasswdByDevSn({})", devSn);
		String table_customer = "tab_hgwcustomer";
		String gw_type = " and gw_type = 1 ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			gw_type = " and gw_type = 2 ";
		}
		String strSQL = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,a.loopback_ip,";
		if("cq_dx".equals(Global.G_instArea)){
			strSQL += "a.cpe_currentstatus,b.adsl_hl,";
		}
		if("xj_dx".equals(Global.G_instArea) && !"BBMS".equals(Global.SYSTEM_NAME)){
			strSQL = strSQL	+ " gg.device_id scrap_dev,c.username netuser, a.complete_time,a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
					+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id "
					+ " left join tab_gw_device_scrap c on b.device_id = c.device_id "
					+ " left join tab_device_model_scrap gg on b.device_id = gg.device_id "
					+ " where a.dev_sub_sn='"
					+ devSn.substring(devSn.length() - 6)
					+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		}else{
			if("xj_dx".equals(Global.G_instArea)){
				strSQL = strSQL	+ " a.complete_time,";
			}
			strSQL = strSQL	+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
					+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id"
					+ " where a.dev_sub_sn='"
					+ devSn.substring(devSn.length() - 6)
					+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		}
		
		if(!StringUtil.IsEmpty(oui)){
			strSQL = strSQL + " and a.oui='"+oui+"'";
		}
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
/**
 * 企业网关根据设备序列号查询
 */
	public ArrayList<HashMap<String, String>> getTelePasswdByDevSn2(String devSn) {
		logger.debug("getTelePasswdByDevSn({})", devSn);
		String	table_customer = "tab_egwcustomer";
		String	gw_type = " and gw_type = 2 ";
		String strSQL = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
				+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id"
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	/**
	 * 根据最后6位及以上序列号，查询终端信息
	 * 
	 * @param 终端序列号
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryDevInfo(String devSn) {
		logger.debug("queryDevInfo({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}

		String strSQL = "select a.device_id, a.device_type,a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id, a.devicetype_id,a.loopback_ip "
				+ " from tab_gw_device a "
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'";
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}

	
	
	/**
	 * 根据最后6位及以上序列号，查询设备id、user_id、业务账号等
	 * 
	 * @param 终端序列号,业务类型
	 * @author fanjm(35572)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryDevUser4SD(String devSn, String testType) {
		logger.debug("queryDevUser4SD({})", devSn);

		if (StringUtil.IsEmpty(devSn)||StringUtil.IsEmpty(testType)) {
			logger.error("devSn or testType is Empty");
			return null;
		}
		
		String serv_type_id = "10";
		if("iptv".equals(testType)){
			serv_type_id = "11";
		}
		
		String strSQL = "select a.username loid, d.device_id, d.device_type,d.device_serialnumber, d.oui, d.cpe_allocatedstatus,"
				+ " d.city_id, d.devicetype_id,d.loopback_ip,a.user_id, b.username ,b.serv_type_id"
				+ " from tab_gw_device d,tab_hgwcustomer a,hgwcust_serv_info b"
				+ " where d.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and d.device_serialnumber like '%" + devSn + "' and a.user_id=b.user_id and a.device_id = d.device_id and b.serv_type_id ="+serv_type_id;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	
	
	/**
	 * 根据最后6位及以上序列号，查询终端信息及绑定情况（家庭网关）
	 * 
	 * @param 终端序列号
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryDevInfo2(String devSn) {
		logger.debug("queryDevInfo({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}

		String strSQL = "select a.device_id,a.device_serialnumber,b.user_id"
				+ " from tab_gw_device a left join tab_hgwcustomer b on a.device_id = b.device_id "
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'";
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	
	/**
	 * 根据最后6位及以上序列号，查询终端信息及绑定情况（政企网关）
	 * 
	 * @param 终端序列号
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryDevInfo3(String devSn) {

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}
		String strSQL = "select a.device_id,a.device_serialnumber,b.user_id"
				+ " from tab_gw_device a left join tab_egwcustomer b on a.device_id = b.device_id "
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'";
		return DBOperation.getRecords(strSQL);
	}

	public Map<String,String> getDeviceTypeInfo(String deviceId)
	{
		logger.debug("getDeviceTypeInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.error("deviceTypeId is Empty");
			return null;
		}

		String strSQL = "select a.is_normal,a.access_style_relay_id,a.ip_model_type,a.spec_id,a.mbbroadband,a.hardwareversion,a.softwareversion,a.vendor_id,a.device_model_id,b.vendor_name,c.device_model,d.loopback_ip,d.device_name " +
				" from tab_devicetype_info a, tab_vendor b, gw_device_model c,tab_gw_device d where a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id " +
				" and a.devicetype_id= d.devicetype_id and d.device_id='" + deviceId + "'";
		if("hb_dx".equals(Global.G_instArea)){
			strSQL = "select a.is_normal,a.access_style_relay_id,a.ip_model_type,a.spec_id,a.mbbroadband,a.hardwareversion,a.softwareversion,a.vendor_id,a.device_model_id,b.vendor_name,c.device_model,d.loopback_ip,d.device_name,e.gbbroadband  " +
					" from tab_devicetype_info a, tab_vendor b, gw_device_model c,tab_gw_device d,tab_device_version_attribute e where a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id " +
					" and a.devicetype_id= d.devicetype_id and a.devicetype_id= e.devicetype_id and d.device_id='" + deviceId + "'";
		}
		
	/*	if("xj_dx".equals(Global.G_instArea)){
			strSQL = "select d.complete_time, a.devicetype_id,a.is_awifi,a.is_normal,a.access_style_relay_id,a.ip_model_type,a.spec_id,a.mbbroadband,a.hardwareversion,a.softwareversion,a.vendor_id,a.device_model_id,b.vendor_name,c.device_model,d.loopback_ip,d.device_name,d.cpe_mac " +
					" from tab_devicetype_info a, tab_vendor b, gw_device_model c,tab_gw_device d where a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id " +
					" and a.devicetype_id= d.devicetype_id and d.device_id='" + deviceId + "'";
		}*/
		
		if("xj_dx".equals(Global.G_instArea)){
			strSQL = "select gg.device_id scrap_dev,ff.username, d.complete_time, a.devicetype_id,a.is_awifi,a.is_normal,a.access_style_relay_id,a.ip_model_type,a.spec_id,"+
		             "a.mbbroadband,a.hardwareversion,a.softwareversion,a.vendor_id,a.device_model_id,b.vendor_name,"+
					 " c.device_model,d.loopback_ip,d.device_name,d.cpe_mac" +
		             " from tab_gw_device d left join  tab_devicetype_info a on a.devicetype_id= d.devicetype_id"+
					 " left join tab_vendor b on d.vendor_id=b.vendor_id left join gw_device_model c on d.device_model_id=c.device_model_id"+
					 " left join tab_gw_device_scrap ff on d.device_id=ff.device_id"+
					 " left join tab_device_model_scrap gg on d.device_id=gg.device_id"+
					 " where d.device_id='" + deviceId + "'";
		}
		
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	
	/**
	 * 根据版本ID查询版本信息
	 * @param deviceTypeId
	 * @return
	 */
	public Map<String,String> getDeviceVersionInfo(String deviceTypeId){
		String strSQL = "select c.lan_num,c.voice_num,b.device_version_type,b.wifi from tab_devicetype_info a left join tab_device_version_attribute b" +
				" on a.devicetype_id = b.devicetype_id,tab_bss_dev_port c where a.spec_id = c.id and a.devicetype_id = " + deviceTypeId;
		if("xj_dx".equals(Global.G_instArea)){
			strSQL = "select c.lan_num,c.voice_num,b.device_version_type,b.wifi ,b.gigabit_port,b.is_security_plugin,b.security_plugin_type "
				    +"from tab_devicetype_info a left join tab_device_version_attribute b" 
					+" on a.devicetype_id = b.devicetype_id,tab_bss_dev_port c where a.spec_id = c.id and a.devicetype_id = " + deviceTypeId;
		}
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	
	public String getUserType(long userId)
	{
		logger.debug("getUserType({})", userId);

		String strSQL = "select b.type_name from gw_cust_user_dev_type a,gw_dev_type b where a.type_id=b.type_id and a.user_id="+userId;
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL).get("type_name");
	}
	/**
	 * 根据逻辑SN查询用户的所有业务账号，并用'|||'隔开返回
	 * 逻辑SN|||宽带 |||IPTV |||VOIP
	 * @param username 逻辑SN
	 * @return
	 */
	public String getAllUsername(String username) {
		
		logger.debug("getAllUsername({})", username);
		
		String userId = "";
		int servTypeId = 0;
		
		StringBuffer sb = new StringBuffer();
		
		// add by zhangchy 2012-03-09 为了能够按照接口文档总字符串的组装顺序，在SQL语句中增加了 order by c.serv_type_id 
//		String strSQL = "select c.username,c.serv_type_id,d.voip_phone from (select a.username sn ,b.username,b.serv_type_id,b.user_id " +
//				      " from tab_hgwcustomer a , hgwcust_serv_info b where a.user_id=b.user_id and b.serv_status=1 and a.username='"+ username +"') c " +
//				       "left join tab_voip_serv_param d on c.user_id=d.user_id  order by c.serv_type_id ";
		// 宽带，IPTV
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.username sn ,b.username,b.serv_type_id,b.user_id ");
		psql.append("  from tab_hgwcustomer a , hgwcust_serv_info b ");
		psql.append(" where 1=1");
		psql.append("   and a.user_id = b.user_id ");
		psql.append("   and b.serv_status = 1 ");
		psql.append("   and a.username = '"+username+"'");
		
		List<HashMap<String,String>> usernameList = DBOperation.getRecords(psql.getSQL());
		List<Integer> tempList = new ArrayList<Integer>();
		sb.append(username).append("|||");
		
		if(usernameList != null && !usernameList.isEmpty()) {
			
			StringBuffer itv = new StringBuffer();
			
			for(HashMap<String,String> map : usernameList) {
				
				if(map != null && !usernameList.isEmpty()) {
					
					servTypeId = StringUtil.getIntValue(map,"serv_type_id");
					userId = StringUtil.getStringValue(map,"user_id");
					tempList.add(servTypeId);
					if(servTypeId == 10) {  // 宽带帐号
						if (StringUtil.IsEmpty(map.get("username"))) {
							sb.append("").append("|||");
						} else {
							sb.append(map.get("username")).append("|||");
						}
					} else if(servTypeId == 11) {  // IPTV帐号
						if(!tempList.contains(10))
						{
							itv.append("|||");
						}
						if (StringUtil.IsEmpty(map.get("username"))) {
							itv.append("").append("|||");
						} else {
							itv.append(map.get("username")).append("|||");
						}
					} else {
					}
				}
			}
			sb.append(itv.toString());
		}
		
		// VOIP 电话号码
		if(!StringUtil.IsEmpty(userId)){
			PrepareSQL psql2 = new PrepareSQL();
			psql2.append("select user_id, line_id, voip_username, voip_passwd, sip_id, voip_phone, protocol ");
			psql2.append("  from tab_voip_serv_param ");
			psql2.append(" where 1=1");
			psql2.append("   and user_id = "+userId);
			
			List<HashMap<String,String>> voipPhoneList = DBOperation.getRecords(psql2.getSQL());
			
			if(voipPhoneList != null && !voipPhoneList.isEmpty()) {
				
				StringBuffer voip = new StringBuffer();
				if(!tempList.contains(11))
				{
					voip.append("|||");
				}
				for(HashMap<String,String> voipMap : voipPhoneList) {
					
					if(voipMap != null && !voipPhoneList.isEmpty()) {
						
						servTypeId = StringUtil.getIntegerValue(voipMap.get("serv_type_id"));
							
						voip.append(voipMap.get("voip_phone")).append("|||");
					}
				}
				sb.append(voip.toString()) ;
			}
		}
		return sb.toString().substring(0, sb.toString().length()-3);
	}
	
	public String getAllUsername2(String username) {
		
		logger.debug("getAllUsername({})", username);
		
		String userId = "";
		int servTypeId = 0;
		
		StringBuffer sb = new StringBuffer();
		
		// add by zhangchy 2012-03-09 为了能够按照接口文档总字符串的组装顺序，在SQL语句中增加了 order by c.serv_type_id 
//		String strSQL = "select c.username,c.serv_type_id,d.voip_phone from (select a.username sn ,b.username,b.serv_type_id,b.user_id " +
//				      " from tab_hgwcustomer a , hgwcust_serv_info b where a.user_id=b.user_id and b.serv_status=1 and a.username='"+ username +"') c " +
//				       "left join tab_voip_serv_param d on c.user_id=d.user_id  order by c.serv_type_id ";
		// 宽带，IPTV
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.username sn ,b.username,b.serv_type_id,b.user_id ");
		psql.append("  from tab_egwcustomer a , egwcust_serv_info b ");
		psql.append(" where 1=1");
		psql.append("   and a.user_id = b.user_id ");
		psql.append("   and b.serv_status = 1 ");
		psql.append("   and a.username = '"+username+"'");
		
		List<HashMap<String,String>> usernameList = DBOperation.getRecords(psql.getSQL());
		List<Integer> tempList = new ArrayList<Integer>();
		sb.append(username).append("|||");
		
		if(usernameList != null && !usernameList.isEmpty()) {
			
			StringBuffer itv = new StringBuffer();
			
			for(HashMap<String,String> map : usernameList) {
				
				if(map != null && !usernameList.isEmpty()) {
					
					servTypeId = StringUtil.getIntValue(map,"serv_type_id");
					userId = StringUtil.getStringValue(map,"user_id");
					tempList.add(servTypeId);
					if(servTypeId == 10) {  // 宽带帐号
						if (StringUtil.IsEmpty(map.get("username"))) {
							sb.append("").append("|||");
						} else {
							sb.append(map.get("username")).append("|||");
						}
					} else if(servTypeId == 11) {  // IPTV帐号
						if(!tempList.contains(10))
						{
							itv.append("|||");
						}
						if (StringUtil.IsEmpty(map.get("username"))) {
							itv.append("").append("|||");
						} else {
							itv.append(map.get("username")).append("|||");
						}
					} else {
					}
				}
			}
			sb.append(itv.toString());
		}
		
		// VOIP 电话号码
		if(!StringUtil.IsEmpty(userId)){
			PrepareSQL psql2 = new PrepareSQL();
			psql2.append("select user_id, line_id, voip_username, voip_passwd, sip_id, voip_phone, protocol ");
			psql2.append("  from tab_egw_voip_serv_param ");
			psql2.append(" where 1=1");
			psql2.append("   and user_id = "+userId);
			
			List<HashMap<String,String>> voipPhoneList = DBOperation.getRecords(psql2.getSQL());
			
			if(voipPhoneList != null && !voipPhoneList.isEmpty()) {
				
				StringBuffer voip = new StringBuffer();
				if(!tempList.contains(11))
				{
					voip.append("|||");
				}
				for(HashMap<String,String> voipMap : voipPhoneList) {
					
					if(voipMap != null && !voipPhoneList.isEmpty()) {
						
						servTypeId = StringUtil.getIntegerValue(voipMap.get("serv_type_id"));
							
						voip.append(voipMap.get("voip_phone")).append("|||");
					}
				}
				sb.append(voip.toString()) ;
			}
		}
		
		return sb.toString().substring(0, sb.toString().length()-3);
	}
	public Map<String, String> qryDeviceSerial(String username){
		logger.warn("qryDeviceSerial[{}]",username);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select device_serialnumber from tab_speed_dev_rate where pppoe_name=? ");
		psql.setString(1, username);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		
		return map;
	}
	
	/**
	 * 根据用户的业务账号查询用户信息
	 * 
	 * @param userType:用户信息类型
	 *            username:业务号码
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 */
	public Map<String, String> queryUserInfo(int userType, String username) {
		logger.debug("queryUserInfo({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		PrepareSQL psql = new PrepareSQL();
		String spec_id ="";
		if ("js_dx".equals(Global.G_instArea))
		{
			spec_id = ",a.spec_id";
		}
		String adslHl="";
		if ("cq_dx".equals(Global.G_instArea))
		{
			adslHl = ",a.adsl_hl";
		}
		psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id" + spec_id + adslHl);
		
		switch (userType) {
		case 1:
			if("xj_dx".equals(Global.G_instArea)){
				psql.append(",c.down_bandwidth from " + table_customer + " a, " + table_serv_info + " b " + ", tab_net_serv_param c");
				psql.append(" where a.user_id=b.user_id  and a.user_id=c.user_id");
			}else {
				psql.append(" from " + table_customer + " a, " + table_serv_info + " b ");
				psql.append(" where a.user_id=b.user_id");
			}

			psql.append(" and b.serv_status= 1 and b.username='" + username + "' and b.serv_type_id = 10  order by a.updatetime desc ");
			break;
		case 3:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b ");
			psql.append(" where a.user_id=b.user_id and b.serv_status= 1");
			psql.append(" and b.username='" + username + "' and b.serv_type_id = 11  order by a.updatetime desc ");
			break;
		case 4:
			psql.append(" from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'  order by a.updatetime desc ");
			break;
		case 5:
			psql.append(" from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'  order by a.updatetime desc ");
			break;
		case 2:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
				String subUserName = username.substring(username.length()-6);
				if ("sx_lt".equals(Global.G_instArea) || "nx_lt".equals(Global.G_instArea)){
					psql.append(" and a.username like '%" + username + "' order by a.updatetime desc ");
				}
				else{
					psql.append(" and a.username like '%" + username + "' and a.user_sub_name ='"+ subUserName+ "' order by a.updatetime desc ");
				}
			}else{
				psql.append(" and a.username = '" + username + "'  order by a.updatetime desc ");
			}
			break;
		default:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.device_serialnumber like '%" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}

	
	
	
	public Map<String, String> queryUserInfoOrderLastTime_XJ(int userType, String username) {
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,a.username,a.device_id ");
		
		switch (userType) {
		case 1:
			psql.append(" from " + table_customer + " a  inner join  " + table_serv_info + " b on a.user_id=b.user_id ");
			psql.append(" left join  gw_devicestatus c on  a.device_id=c.device_id ");
			psql.append(" where  b.serv_status= 1 and b.username='" + username + "' and b.serv_type_id = 10  ");
			if (DBUtil.GetDB()==3) {
				psql.append(" order by IF(ISNULL(c.last_time),1,0),c.last_time desc");
			}else {
				psql.append(" order by c.last_time desc nulls last  ");
			}
			break;
		case 2:
			psql.append(" from " + table_customer + " a where a.user_state = '1' ");
			psql.append(" and a.username = '" + username + "'  ");
			break;
		case 3:
			psql.append(" from " + table_customer + " a  inner join  " + table_serv_info + " b on a.user_id=b.user_id ");
			psql.append(" left join  gw_devicestatus c on  a.device_id=c.device_id ");
			psql.append(" where b.serv_status= 1 and b.username='" + username + "' and b.serv_type_id = 11  ");
			if (DBUtil.GetDB()==3) {
				psql.append(" order by IF(ISNULL(c.last_time),1,0),c.last_time desc");
			}else {
				psql.append(" order by c.last_time desc nulls last  ");
			}
			break;
		case 4:
			psql.append(" from " + table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id inner join " + table_voip + " c on b.user_id=c.user_id left join gw_devicestatus d ");
			psql.append(" on  a.device_id=d.device_id ");
			psql.append(" where  c.voip_phone='" + username + "'  ");
			if (DBUtil.GetDB()==3) {
				psql.append(" order by IF(ISNULL(d.last_time),1,0),d.last_time desc");
			}else {
				psql.append(" order by d.last_time desc nulls last  ");
			}
			break;
		case 5:
			psql.append(" from " + table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id inner join " + table_voip + " c on b.user_id=c.user_id left join gw_devicestatus d ");
			psql.append(" on  a.device_id=d.device_id ");
			psql.append(" where c.voip_username='" + username + "' ");
			if (DBUtil.GetDB()==3) {
				psql.append(" order by IF(ISNULL(d.last_time),1,0),d.last_time desc");
			}else {
				psql.append(" order by d.last_time desc nulls last  ");
			}
			break;
		default:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.device_serialnumber like '%" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	/**
	 * 根据用户的业务账号查询用户信息
	 * 
	 * @param userType:用户信息类型
	 *            username:业务号码
	 *            cityId : 属地
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 */
	public ArrayList<HashMap<String, String>> queryUserInfoList(int userType, String username, String cityId) {
		logger.debug("queryUserInfo({})", username);

		
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id");
		
		switch (userType) {
			// 用户宽带帐号
		case 1:
			psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
			if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
			{
				psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
			}
			psql.append(" 	order by a.updatetime desc");
			break;
			// IPTV宽带帐号
		case 3:
			psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 11");
			if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
			{
				psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
			}
			break;
			// VOIP业务电话号码
		case 4:
			psql.append(" from " + table_customer + " a,"  + table_voip + " c");
			psql.append("	where a.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			if (("jx_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
			{
				psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
			}
			break;
			// VOIP认证帐号
		case 5:
			psql.append(" from " + table_customer + " a,"  + table_voip + " c");
			psql.append("	where a.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
			{
				psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
			}
			break;
			// 逻辑SN号
		case 2:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.username = '" + username + "'");
			break;
		default:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.device_serialnumber like '%" + username + "'");
		}
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 江苏桥改路由和路由改桥，根据宽带账号和loid查询
	 * @return ArrayList<HashMap<String,String>>
	 */
	public ArrayList<HashMap<String, String>> queryUserList(int userType, String username, String loid) {
		logger.debug("queryUserInfo({})", username);

		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		PrepareSQL psql = new PrepareSQL();
		
		String str_a="select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id";
		String str_b=",b.wan_type,b.passwd,c.devicetype_id";
		psql.append(str_a);
		switch (userType) {
			// 用户宽带帐号
		case 1:
			psql.append(str_b);
			psql.append("	from " + table_customer + " a left join tab_gw_device c on (a.device_id=c.device_id), " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
			// loid为空的情况，即综调第一次调接口进行巧改路由，此时只传宽带账号
			// loid不为空的情况，即第一次通过宽带账号找到多个loid， 综调那边会选择一个loid再次调用我们呢的接口
			if(!StringUtil.IsEmpty(loid)){
				psql.append(" and a.username = '" + loid + "'");
			}
			psql.append(" 	order by a.updatetime desc");
			break;
			// IPTV宽带帐号
		case 3:
			psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 11");
			
			break;
			// VOIP业务电话号码
		case 4:
			psql.append(" from " + table_customer + " a,"  + table_voip + " c");
			psql.append("	where a.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			
			break;
			// VOIP认证帐号
		case 5:
			psql.append(" from " + table_customer + " a,"  + table_voip + " c");
			psql.append("	where a.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			
			break;
			// 逻辑SN号
		case 2:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.username = '" + username + "'");
			break;
		default:
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.device_serialnumber like '%" + username + "'");
		}
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	
	
	
	/**
	 * 根据用户的业务账号查询BBMS用户信息
	 * 
	 * @param userType:用户信息类型
	 *            username:业务号码
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 */
	public Map<String, String> queryUserInfoForBBMS(int userType, String username) {
		logger.debug("queryUserInfo({})", username);

		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id");
		
		switch (userType) {
		case 1:
		case 3:
			psql.append("	from tab_egwcustomer a, egwcust_serv_info b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "'");
			break;
		case 4:
			psql.append(" from tab_egwcustomer a,egwcust_serv_info b,tab_egw_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			break;
		case 5:
			psql.append(" from tab_egwcustomer a,egwcust_serv_info b,tab_egw_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			break;
		default:
			psql.append(" from tab_egwcustomer a where a.user_state = '1'");
		psql.append(" and a.username = '" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 获取设备信息，包含状态信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return Map<String,String>
	 */
	public ArrayList<HashMap<String, String>> getDevStatusInfo(String devSn) {
		logger.debug("getDevStatusInfo({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.warn("devSn is empty!");
			return null;
		}

		String strSQL = "select b.device_id,b.complete_time,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id,"
				+ " c.online_status, b.city_id"
				+ " from tab_gw_device b left join gw_devicestatus c on b.device_id=c.device_id"
				+ " where b.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and b.device_serialnumber like '%" + devSn + "'";
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}

	
	/**
	 * 根据用户账号获取多PVC部署状态，属地等信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return int
	 */
	public Map<String, String> queryPvcReformed(String username) {
		logger.debug("queryPvcReformed({})", username);
		String strSQL = "select reform_flag, city_id from itv_customer_info"
				+ " where username=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, username);
		return DBOperation.getRecord(psql.getSQL());
	}
	/**
	 * 根据userid查询该用户拥有的业务
	 * @param userid
	 * @return
	 */
	public List<HashMap<String,String>> queryUserServList(String userid)
	{
		logger.debug("queryUserServList({})", userid);

		if (StringUtil.IsEmpty(userid)) {
			logger.warn("userid is empty!");
			return null;
		}
		
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		
		PrepareSQL pSQL = new PrepareSQL();
		
		pSQL.setSQL("select serv_type_id,username,user_id from " + table_serv_info + " where user_id=? and serv_status=1");
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		return DBOperation.getRecords(pSQL.getSQL());
	}
	/**
	 * 查询业务工单的配置数据
	 * @param userid
	 * @param servTypeid
	 * @param username
	 * @return
	 */
	public Map<String,String> queryServSheetData(String userid,String servTypeid,String username)
	{
		logger.debug("queryServSheetData({},{},{})", new Object[] {userid,servTypeid,username});
		PrepareSQL pSQL = new PrepareSQL();
		
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}

		
		pSQL.setSQL("select serv_type_id,wan_type,vciid,vpiid,username,passwd,vlanid,bind_port,open_status from " + table_serv_info + " where user_id=? and serv_type_id=? and username=?");
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		pSQL.setLong(2, StringUtil.getLongValue(servTypeid));
		pSQL.setString(3, username);
		return DBOperation.getRecord(pSQL.getSQL());
	}
	/**
	 * 获取VOIP业务参数信息
	 * @param userid
	 * @return
	 */
	public List<HashMap<String,String>> queryVoipParam(String userid)
	{
		logger.debug("queryVoipParam({})", userid);

		if (StringUtil.IsEmpty(userid)) {
			logger.warn("userid is empty!");
			return null;
		}
		
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_voip = "tab_egw_voip_serv_param";
		}
		
		PrepareSQL pSQL = new PrepareSQL();
		
		// mysql
		if (3 == DBUtil.GetDB()) {
			pSQL.setSQL("select b.prox_serv,b.prox_port,b.stand_prox_serv,b.stand_prox_port,b.regi_serv,b.regi_port,b.stand_regi_serv,b.stand_regi_port,b.out_bound_proxy,b.out_bound_port,b.stand_out_bound_proxy,b.stand_out_bound_port,a.voip_phone,a.parm_stat,a.voip_username,a.voip_passwd,a.protocol from " + table_voip + " a,tab_sip_info b where user_id=? and a.sip_id=b.sip_id order by a.line_id");
		} else {
			pSQL.setSQL("select * from " + table_voip + " a,tab_sip_info b where user_id=? and a.sip_id=b.sip_id order by a.line_id");
		}
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		return DBOperation.getRecords(pSQL.getSQL());
	}
	/**
	 * 修改宽带业务的上网方式，并更新业务状态未做。
	 * @param operateType
	 * @param user_id
	 */
	public void updateWanType(String operateType, String user_id)
	{
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.setSQL("update " + table_serv_info + " set wan_type = ?,open_status=0 where user_id = ? and serv_type_id = 10");
		pSQL.setInt(1, StringUtil.getIntegerValue(operateType));
		pSQL.setLong(2, StringUtil.getLongValue(user_id));
		
		DBOperation.executeUpdate(pSQL.getSQL());
	}
	
	/**
	 * 修改宽带业务的上网方式，更新vlanid
	 * @param operateType
	 * @param user_id
	 * @param vlanId
	 */
	public int updateWanTypeAndVlanId(String operateType, String user_id, String vlanId) {
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.setSQL("update " + table_serv_info + " set wan_type = ?, open_status=0 where vlanid=? and user_id = ? and serv_type_id = 10");
		pSQL.setInt(1, StringUtil.getIntegerValue(operateType));
		pSQL.setString(2, vlanId);
		pSQL.setLong(3, StringUtil.getLongValue(user_id));
		
		return DBOperation.executeUpdate(pSQL.getSQL());
	}

	/**
	 * 修改宽带业务的上网方式，并更新业务状态未做，下发宽带密码。
	 * @param operateType
	 * @param user_id
	 */
	public void updateWanTypeAndPwd(String operateType, String user_id, String pwd)
	{
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.setSQL("update " + table_serv_info + " set wan_type = ?, passwd = ?,open_status=0 where user_id = ? and serv_type_id = 10");
		pSQL.setInt(1, StringUtil.getIntegerValue(operateType));
		pSQL.setString(2, StringUtil.getStringValue(pwd));
		pSQL.setLong(3, StringUtil.getLongValue(user_id));
		
		DBOperation.executeUpdate(pSQL.getSQL());
	}
	/**
	 * 设备恢复出厂设置后更新用户状态
	 * 
	 * @param gwType
	 * @param userId
	 */
	public void updateCustStatus(long userId)
	{
		String tableName= " hgwcust_serv_info ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableName = "egwcust_serv_info";
		}
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("update ");
		pSql.append(tableName);
		pSql.append(" set open_status=0,updatetime=? where user_id=? and serv_status in (1,2) and open_status!=0");
		int index = 0;
		pSql.setLong(++index, new DateTimeUtil().getLongTime());
		pSql.setLong(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update table[{}] rows[{}].", tableName, updateRows);
	}
	/**
	 * 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
	 * @param userId
	 */
	public void updateCustStatusFailure(long userId) {
		String tableName= " hgwcust_serv_info ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableName = " egwcust_serv_info ";
		}
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("update ");
		pSql.append(tableName);
		pSql.append(" set open_status=1,updatetime=? where user_id=? and serv_status in (1,2) and open_status = 0");
		int index = 0;
		pSql.setLong(++index, new DateTimeUtil().getLongTime());
		pSql.setLong(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update table[{}] rows[{}].", tableName, updateRows);
	}
	
	//获取用户上网方式，首先须知道用户user_id
		public Map<String, String> getUserId_WanType(int userType,String userInfo) {
			logger.debug("getUserId({})",userType, userInfo);

			String sql="";
			if(userType==2){
				//2：逻辑SN号
				sql="select user_id from tab_hgwcustomer where username = '"+userInfo+"'";
			}else if(userType==4){
				//4：VOIP业务电话号码
				sql="select user_id from tab_voip_serv_param where voip_phone = '"+userInfo+"'";
			}
			logger.info(sql);
			Map<String,String> m=DBOperation.getRecord(sql);
			if(m==null || m.isEmpty()){
				return null;
			}
			return m;
			
		}
		
		/**
		 * 获取用户上网方式
		 * 
		 * @param 用户账号
		 * @author wanghong5(72780)
		 * @date 2015-03-10
		 * @return Map<String,String>
		 */
		public List<HashMap<String,String>> getUserWanType(int userType,String userInfo) {
			logger.debug("getUserWanType({})",userType, userInfo);

			PrepareSQL pSql = new PrepareSQL();
			if(userType==1){
				//1：用户宽带帐号
				pSql.append("select wan_type from hgwcust_serv_info where serv_type_id = 10 ");
				pSql.append("and username = '"+userInfo+"' ");
			}else if(userType==3){
				//3：IPTV宽带帐号
				pSql.append("select wan_type from hgwcust_serv_info where user_id = (");
				pSql.append("select user_id from hgwcust_serv_info where serv_type_id = 11 ");
				pSql.append("and username = '"+userInfo+"' ) ");
				pSql.append(" and serv_type_id = 10 ");
			}else if(userType==2){
				//2：逻辑SN号
				pSql.append("select b.user_id, a.wan_type from tab_hgwcustomer b left join ");
				pSql.append(" (select wan_type, user_id from hgwcust_serv_info where serv_type_id = 10) a on b.user_id = a.user_id ");
				pSql.append(" where b.username = '"+ userInfo + "'");
//				pSql.append("select a.wan_type  from tab_hgwcustomer b left join hgwcust_serv_info a ");
//				pSql.append("on b.user_id = a.user_id where a.serv_type_id = 10 and b.username = '"+ userInfo + "'");
			}else if(userType==4){
				//4：VOIP业务电话号码
				pSql.append("select b.wan_type  from tab_voip_serv_param a,hgwcust_serv_info b ");
				pSql.append("where a.user_id = b.user_id and b.serv_type_id = 10 and a.voip_phone = '"+ userInfo + "'");
			}else if(userType==5){
				//5：VOIP认证账号
				pSql.append("select b.wan_type  from tab_voip_serv_param a,hgwcust_serv_info b ");
				pSql.append("where a.user_id = b.user_id and b.serv_type_id = 10 and a.voip_username = '"+ userInfo + "'");
			}
			
			logger.info(pSql.getSQL());
			List<HashMap<String,String>> map=DBOperation.getRecords(pSql.getSQL());
			if(null==map || map.isEmpty()){
				return null;
			}else{
				return map;
			}
			
		}
		
		//获取用户上网方式，首先须知道用户user_id
		public Map<String, String> getUserId_AccessType(int userType,String userInfo) {
			logger.debug("getUserId_AccessType({})",userType, userInfo);

			String sql="";
			switch(userType){
				//1：用户宽带帐号
				case 1:
					sql="select user_id from hgwcust_serv_info where serv_type_id = 10 and username = '"+userInfo+"'";
					break;
				//3：IPTV业务帐号
				case 3: 
					sql="select user_id from hgwcust_serv_info where serv_type_id = 11 and username = '"+userInfo+"'";
					break;
				//4：VOIP业务电话号码
				case 4: 
					sql="select user_id from tab_voip_serv_param where voip_phone = '"+userInfo+"'";
					break;
			}
			logger.info(sql);
			Map<String,String> ms=DBOperation.getRecord(sql);
			if(ms==null || ms.isEmpty()){
				return null;
			}
			return ms;	
			
		}
		
		
		/**
		 * 获取用户接入方式
		 * 
		 * @param 用户账号
		 * @author wanghong5(72780)
		 * @date 2015-03-10
		 * @return Map<String,String>
		 */
		public List<HashMap<String,String>> getUserAccessType(int userType,String userInfo) {
			logger.debug("getUserAccessType({})",userType, userInfo);

			StringBuffer pSql = new StringBuffer();;
			
			if(userType==1){
				//1：用户宽带帐号
				pSql.append("select a.adsl_hl,a.device_id from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 ");
				pSql.append("and b.username = '"+userInfo+"' ");
				pSql.append(" order by a.updatetime desc");
			}else if(userType==2){
				//2：逻辑SN号
				pSql.append("select a.adsl_hl,a.device_id from tab_hgwcustomer a where a.username = '");
				pSql.append(userInfo);
				pSql.append("'");
			}else if(userType==3){
				//3：IPTV宽带帐号
				pSql.append("select a.adsl_hl,a.device_id  from tab_hgwcustomer a,hgwcust_serv_info b ");
				pSql.append("where a.user_id = b.user_id and b.serv_type_id = 11 and b.username = '"+ userInfo + "'");
			}else if(userType==4){
				//4：VOIP业务电话号码
				pSql.append("select a.adsl_hl,a.device_id  from tab_hgwcustomer a,tab_voip_serv_param b ");
				pSql.append("where a.user_id = b.user_id and b.voip_phone = '"+ userInfo + "'");
			}else if(userType==5){
				//5：VOIP认证账号
				pSql.append("select a.adsl_hl,a.device_id  from tab_hgwcustomer a,tab_voip_serv_param b ");
				pSql.append("where a.user_id = b.user_id and b.voip_username = '"+ userInfo + "'");
			}
			logger.info(pSql.toString());
			List<HashMap<String,String>> m=DBOperation.getRecords(pSql.toString());
			if(null==m || m.isEmpty()){
				return null;
			}else{
				return m;
			}
			
		}
		
		//获取用户awifi开通状态，首先须知道用户设备序列号
		public List<HashMap<String,String>> getUserDeviceSerialNumber(int userType,String userInfo) {
			logger.debug("getUseIsAwifi({})",userType, userInfo);

			StringBuffer sql= new StringBuffer("");
			switch(userType){
				//1：用户宽带帐号
				case 1:
					sql.append("select a.user_id,a.device_id from tab_hgwcustomer a,hgwcust_serv_info b ");
					sql.append("where a.user_id = b.user_id and b.serv_type_id = 10 and b.username = '"+userInfo+"' ");
					sql.append(" order by a.updatetime desc");
					break;
				//2：LOID	
				case 2:
					sql.append("select a.user_id,a.device_id from tab_hgwcustomer a where a.username = '"+userInfo+"' ");
					break;
				//3：IPTV宽带帐号	
				case 3: 
					sql.append("select a.user_id,a.device_id from tab_hgwcustomer a,hgwcust_serv_info b ");
					sql.append("where a.user_id = b.user_id and b.serv_type_id = 11 and b.username = '"+userInfo+"' ");
					break;
				//4：VOIP业务电话号码		
				case 4: 
					sql.append("select a.user_id,a.device_id from tab_hgwcustomer a,tab_voip_serv_param b ");
					sql.append("where a.user_id = b.user_id and b.voip_phone = '"+userInfo+"' ");
					break;
				//4：VOIP业务电话号码		
				case 5: 
					sql.append("select a.user_id,a.device_id from tab_hgwcustomer a,tab_voip_serv_param b ");
					sql.append("where a.user_id = b.user_id and b.voip_username = '"+userInfo+"' ");
					break;
			}
			logger.info(sql.toString());
			List<HashMap<String,String>> ms=DBOperation.getRecords(sql.toString());
			if(ms==null || ms.isEmpty()){
				return null;
			}
			return ms;
			
		}
		
		/**
		 * 获取用户awifi开通状态
		 * 
		 * @param 用户账号
		 * @author wanghong5(72780)
		 * @date 2015-03-10
		 * @return Map<String,String>
		 */
		public List<HashMap<String,String>> getUserIsAwifi(String device_id) {
			logger.debug("getUseIsAwifi({})", device_id);

			String pSql="select service_id,end_time from gw_serv_strategy_batch where service_id in (2001,2003) and device_id ='"+device_id+"'";
			logger.info(pSql);
			List<HashMap<String,String>> m=DBOperation.getRecords(pSql);
			return m;
			
		}

		/**
		 * 根据用户的业务账号查询用户信息
		 * 
		 * @param userType:用户信息类型
		 *            username:业务号码
		 *            cityId:属地
		 * @author Jason(3412)
		 * @date 2010-6-22
		 * @return ArrayList<HashMap<String,String>>
		 */
		public Map<String, String> queryUserInfo(int userType, String username, String cityId) {
			logger.debug("queryUserInfo({})", username);

			if (StringUtil.IsEmpty(username)) {
				logger.error("username is Empty");
				return null;
			}
			String table_customer = "tab_hgwcustomer";
			String table_serv_info = "hgwcust_serv_info";
			String table_voip = "tab_voip_serv_param";
			
			if("BBMS".equals(Global.SYSTEM_NAME)){
				table_customer = "tab_egwcustomer";
				table_serv_info = "egwcust_serv_info";
				table_voip = "tab_egw_voip_serv_param";
			}

			PrepareSQL psql = new PrepareSQL();
			
			psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id,a.updatetime");
			switch (userType) {
			case 1:
				psql.append(",b.passwd from " + table_customer + " a, " + table_serv_info + " b");
				psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
				psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 3:
				psql.append(",b.passwd from " + table_customer + " a, " + table_serv_info + " b");
				psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
				psql.append("	and b.username='" + username + "' and b.serv_type_id = 11");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 4:
				psql.append(",b.passwd,b.open_status from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
				psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
				psql.append(" and c.voip_phone='" + username + "'");
				if (("jx_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 5:
				psql.append(",b.passwd from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
				psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
				psql.append(" and c.voip_username='" + username + "'");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 2:
				if("jx_dx".equals(Global.G_instArea))
				{
					psql.append(" from " + table_customer + " a left join " + table_serv_info + " b on a.user_id = b.user_id  where a.user_state = '1'  ");
				}
				else
				{
					psql.append(" from " + table_customer + " a where a.user_state = '1'");
				}
				if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
					String subUserName = username.substring(username.length()-6);
					psql.append(" and a.username like '%" + username + "' and a.user_sub_name ='"+ subUserName+ "' ");
				}else{
					psql.append(" and a.username = '" + username + "'");
				}
				break;
			case 6:
				if ("jx_dx".equals(Global.G_instArea)
						|| "hb_dx".equals(Global.G_instArea)
						|| "cq_dx".equals(Global.G_instArea)){
					// mysql
					if (3 == DBUtil.GetDB()) {
						psql.append(" from tab_gw_device b left join " + table_customer + " a on a.device_id=b.device_id ");
					}else{
						psql.append(" from " + table_customer + " a  right join tab_gw_device b on a.device_id=b.device_id ");
					}
					psql.append(" where b.dev_sub_sn='"+username.substring(username.length() - 6)+"' ");
					psql.append(" and b.device_serialnumber like '%" + username + "'");
					break;
				}
//				if ("hb_dx".equals(Global.G_instArea)){
//					psql.append(" from " + table_customer + " a  right join tab_gw_device b on a.device_id=b.device_id ");
//					psql.append(" where b.dev_sub_sn='"+username.substring(username.length() - 6)+"' ");
//					psql.append(" and b.device_serialnumber like '%" + username + "'");
//					break;
//				}
				break;
			// 重庆新接口  customer_id 走7分支
			case 7:
				psql.append(" from " + table_customer + " a where a.user_state = '1'");
				psql.append(" and a.customer_id = '" + username + "'");
				break;
			default:
				psql.append(" from " + table_customer + " a where a.user_state = '1'");
				psql.append(" and a.device_serialnumber like '%" + username + "'");
			}
			return DBOperation.getRecord(psql.getSQL());
		}
		public void prossBssSheet(String loid,String city_id,String context, String returntCode,String resultdesc)
		{
			logger.debug("prossBssSheet()");
			PrepareSQL psql = new PrepareSQL();
			psql.append("insert into tab_bss_sheet (bss_sheet_id,username,product_spec_id,city_id,order_id, "
					+ "type,order_type,receive_date,remark,servUsername,   sheet_context,returnt_context,result,gw_type) values(?,?,?,?,?,   ?,?,?,?,?,  ?,?,?,?)");
			psql.setString(
					1,
					new Date().getTime() / 1000
							+ StringUtil.getStringValue(Math.round(Math.random() * 1000000L))
							+ "-" + new DateTimeUtil().getYYYYMMDDHHMMSS());
			psql.setString(2, loid);
			psql.setInt(3, 10);
			psql.setString(4, city_id);
			psql.setString(5, new DateTimeUtil().getYYYYMMDDHHMMSS());
			psql.setString(6, "2");
			psql.setInt(7, 0);
			psql.setLong(8, new Date().getTime() / 1000);
			psql.setString(9, "AAA调用");
			psql.setString(10, "");
			psql.setString(11, context);
			psql.setString(12, resultdesc);
			if ("1".equals(returntCode))
			{
				psql.setInt(13, 0);
			}
			else
			{
				psql.setInt(13, 1);
			}
			psql.setInt(14, 1);
			DBOperation.executeUpdate(psql.getSQL());
		}

	/**
	 * 根据vlanid查询用户
	 * @param userType
	 * @param username
	 * @param vanlId
	 * @return
	 */
	public Map<String, String> queryUserInfoAndVanlId(int userType, String username, String vanlId) {
		logger.debug("queryUserInfoAndVanlId({})", username);

		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select b.passwd, a.user_id, a.username, a.device_id, a.oui, a.device_serialnumber,");
		psql.append(" a.city_id, a.userline, a.access_style_id, a.updatetime");
		
		switch (userType) {
		case 1:
			psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
			psql.append("	and b.vlanid='" + vanlId + "'");
			psql.append("	order by a.updatetime desc");
			break;
		case 2:
			psql.append(" from " + table_customer + " a , " + table_serv_info + " b where a.user_state = '1'");
			psql.append(" and a.user_id=b.user_id and b.serv_status= 1 and b.serv_type_id = 10");
			psql.append(" and b.vlanid='" + vanlId + "'");
			if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
				String subUserName = username.substring(username.length()-6);
				psql.append(" and a.username like '%" + username + "' and a.user_sub_name ='"+ subUserName+ "' ");
			}else{
				psql.append(" and a.username = '" + username + "'");
			}
			break;
		case 3:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b");
			psql.append(" where a.user_id=b.user_id and b.serv_status= 1");
			psql.append(" and b.username='" + username + "' and b.serv_type_id = 11");
			psql.append(" order by a.updatetime desc");
			break;
		case 4:
			psql.append(",b.open_status from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			psql.append(" and b.vlanid='" + vanlId + "'");
			psql.append("	order by a.updatetime desc");
			break;
		case 5:
			psql.append(" from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			psql.append(" and b.vlanid='" + vanlId + "'");
			psql.append("	order by a.updatetime desc");
			break;
		default:
			return null;
		}
		return DBOperation.getRecord(psql.getSQL());
	}

		/**
		 * 查询企业网关
		 */
		public Map<String, String> queryUserInfo2(int userType, String username, String cityId) {
			logger.debug("queryUserInfo({})", username);

			
			
			if (StringUtil.IsEmpty(username)) {
				logger.error("username is Empty");
				return null;
			}
			String table_customer = "tab_egwcustomer";
			String table_serv_info = "egwcust_serv_info";
			String table_voip = "tab_egw_voip_serv_param";
			
			PrepareSQL psql = new PrepareSQL();
			
			psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id,a.updatetime");
			
			switch (userType) {
			case 1:
				psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
				psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
				psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 3:
				psql.append("	from " + table_customer + " a, " + table_serv_info + " b");
				psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
				psql.append("	and b.username='" + username + "' and b.serv_type_id = 11");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 4:
				psql.append(",b.open_status from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
				psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
				psql.append(" and c.voip_phone='" + username + "'");
				if (("jx_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 5:
				psql.append(" from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
				psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
				psql.append(" and c.voip_username='" + username + "'");
				if (("jx_dx".equals(Global.G_instArea)) && (!StringUtil.IsEmpty(cityId)))
				{
					psql.append("	and a.city_id in (select city_id from tab_city where city_id='"+cityId+"' or parent_id='"+cityId+"')");
				}
				psql.append("	order by a.updatetime desc");
				break;
			case 2:
				psql.append(" from " + table_customer + " a where a.user_state = '1'");
				psql.append(" and a.username = '" + username + "'");
				break;
			case 6:
				if ("jx_dx".equals(Global.G_instArea)){
					// mysql
					if (3 == DBUtil.GetDB()) {
						psql.append(" from tab_gw_device b left join " + table_customer + " a on a.device_id=b.device_id ");
					} else {
						psql.append(" from " + table_customer + " a  right join tab_gw_device b on a.device_id=b.device_id ");
					}
					psql.append(" where b.dev_sub_sn='"+username.substring(username.length() - 6)+"' ");
					psql.append(" and b.device_serialnumber like '%" + username + "'");
					break;
				}
				if ("hb_dx".equals(Global.G_instArea)){
					// mysql
					if (3 == DBUtil.GetDB()) {
						psql.append(" from tab_gw_device b left join " + table_customer + " a on a.device_id=b.device_id ");
					}
					else {
						psql.append(" from " + table_customer + " a  right join tab_gw_device b on a.device_id=b.device_id ");
					}
					psql.append(" where b.dev_sub_sn='"+username.substring(username.length() - 6)+"' ");
					psql.append(" and b.device_serialnumber like '%" + username + "'");
					break;
				}
				break;
			default:
				psql.append(" from " + table_customer + " a where a.user_state = '1'");
				psql.append(" and a.device_serialnumber like '%" + username + "'");
			}
			return DBOperation.getRecord(psql.getSQL());
		}
		/**
		 *      查询oui是否存在
				* @param oui
				* @return
		 */
		public Map<String, String> getDevOui(String oui)
		{
            PrepareSQL psql = new PrepareSQL();
			psql.append("select id,oui from tab_gw_device_init_oui where oui=? ");
			psql.setString(1, oui);
			return DBOperation.getRecord(psql.getSQL());
				
		}
		/**
		        * 查询语音协议类型  
				* @param userId
				* @return
		 */
		public List<HashMap<String,String>> getVoipProtocol(String devId){
			PrepareSQL psql = new PrepareSQL();
			psql.append("select a.device_id ,b.server_type from tab_gw_device a,tab_devicetype_info_servertype b ");
			psql.append("where a.devicetype_id=b.devicetype_id and a.device_id=? ");
			psql.setString(1, devId);
			return DBOperation.getRecords(psql.getSQL());
		}
		/**
		 *       查厂家
				* @param vendor
				* @return
		 */
		public Map<String, String> getVendor(String vendor){
			PrepareSQL psql = new PrepareSQL();
			psql.append("select vendor_id from tab_vendor where vendor_name=? ");
			psql.setString(1, vendor);
			return DBOperation.getRecord(psql.getSQL());
		}
		/**
		 *       查型号
				* @param model
				* @return
		 */
		public Map<String, String> getDevModel(String model){
			PrepareSQL psql = new PrepareSQL();
			psql.append("select device_model_id from gw_device_model where device_model=? ");
			psql.setString(1, model);
			return DBOperation.getRecord(psql.getSQL());
		}
		/**
		        * 查规范版本信息       
				* @param venderId
				* @param modelId
				* @return
		 */
		public List<HashMap<String,String>> queryVersion(String vendorId, String modelId)
		{
			PrepareSQL psql = new PrepareSQL();
			psql.append("select hardwareversion ,softwareversion ,is_normal ,access_style_relay_id ,ip_type ,spec_id ,mbbroadband ,devicetype_id ");
			psql.append("from tab_devicetype_info where vendor_id=? and device_model_id=? ");
			psql.setString(1, vendorId);
			psql.setString(2, modelId);
			return DBOperation.getRecords(psql.getSQL());
		}
		/**
		 *       查语音协议
				* @param venderId
				* @param modelId
				* @return
		 */
		public List<HashMap<String,String>> queryPotocol(String vendorId, String modelId){
			PrepareSQL psql = new PrepareSQL();
			psql.append("select a.devicetype_id,b.server_type ");
			psql.append("from tab_devicetype_info a,tab_devicetype_info_servertype b where a.vendor_id=? and a.device_model_id=? and a.devicetype_id=b.devicetype_id ");
			psql.setString(1, vendorId);
			psql.setString(2, modelId);
			return DBOperation.getRecords(psql.getSQL());
		}

	/**
	 * 根据设备查端口数
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, String> getCountLanAndVoice(int id)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select spec_name,lan_num,voice_num from tab_bss_dev_port where id=? ");
		psql.setInt(1, id);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据用户Id查语音端口数
	 * 
	 * @param userId
	 * @return
	 */
	public int getCountVoiceByUserId(long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select count(1) as num from tab_voip_serv_param where user_id=? ");
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map,"num");
	}

	/**
	 * 根据用户Id查询IPTV业务数
	 * 
	 * @param userId
	 * @return
	 */
	public List<HashMap<String,String>> getIptvByUserId(long userId)
	{
		String table_serv_info = "hgwcust_serv_info";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}

		PrepareSQL psql = new PrepareSQL();
		psql.append("select username, serv_num from " + table_serv_info + " where serv_type_id =11 and user_id =? ");
		psql.setLong(1, userId);
		List<HashMap<String,String>> mapList =  DBOperation.getRecords(psql.getSQL());
		if(mapList == null){
			mapList = new ArrayList<HashMap<String,String>>();
		}
		return mapList;
	}

	/**
	 * 根据用户Id查询宽带信息
	 * 
	 * @param userId
	 * @return
	 */
	public List<HashMap<String,String>> getNetByUserId(long userId)
	{
		String table_serv_info = "hgwcust_serv_info";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.username,b.passwd,b.vlanid,a.ip_type,b.wan_type,b.ipaddress,b.ipmask,b.gateway,b.adsl_ser ");
		psql.append(" from tab_net_serv_param a,"+ table_serv_info +" b where a.user_id = b.user_id and  a.username = b.username and a.serv_type_id = b.serv_type_id");
		psql.append(" and b.user_id=? and b.serv_type_id=10" );
		psql.setLong(1, userId);
		List<HashMap<String,String>> mapList =  DBOperation.getRecords(psql.getSQL());
		if(mapList == null){
			mapList = new ArrayList<HashMap<String,String>>();
		}
		return mapList;
	}

	/**
	 * 获取用户对应的终端类型(e8-b/e8-c)
	 * 
	 * @param userId
	 * @return
	 */
	public String getTypeIdByUserId(long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select type_id from gw_cust_user_dev_type where user_id=? order by time desc");
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "type_id");
	}

	/**
	 * 更新用户表的终端类型
	 * @param userId
	 * @param specId
	 * @return
	 */
	public int updateSpecIdByUserId(long userId, int specId)
	{
		String table_customer = "tab_hgwcustomer";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("update "+ table_customer + " set spec_id=?  where user_id =? ");
		psql.setInt(1, specId);
		psql.setLong(2, userId);
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 查询是否单宽带
	 * @param username
	 * @return  没有查到默认返回单宽带。
	 * 1:多宽带
	 * 2：单宽带
	 */
	public String getUniqueNetMark(long userId)
	{
		String result = "2";
		String sql = "select is_unique_net from  user_attribute where user_id =  " + userId ;
		PrepareSQL psql = new PrepareSQL(sql);
	
		Map<String,String> resultMap = DBOperation.getRecord(psql.getSQL());
		if(resultMap != null && !resultMap.isEmpty())
		{
			result = resultMap.get("is_unique_net");
		}
		
		return result;
	}
	
	public Map<String,String> getUserByID(long userId)
	{
		String table_customer = "tab_hgwcustomer";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_customer = "tab_egwcustomer";
		}
		String sql = "select user_id,username,device_id,city_id from "+table_customer +" where user_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据最后6位及以上序列号，查询终端信息预置表
	 * 
	 * @param 终端序列号
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryDevInfoInit(String devSn) {
		logger.debug("queryDevInfoInit({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}

		String strSQL = "select a.device_id,device_serialnumber from tab_gw_device_init a "
				+ " where a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'";
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}

	/**
	 * 获取下发结果
	 * @param 用户ID
	 * @return ArrayList<HashMap<String,String>>
	 *         loid为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryServResult(String userId)
	{
		if (StringUtil.IsEmpty(userId))
		{
			logger.error("userId is Empty");
			return null;
		}
		String strSQL = "select serv_type_id, open_status,username from  hgwcust_serv_info where  serv_type_id not in (17)"
				+ " and user_id = " + userId;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}

	public Map<String, String> queryStrategyResult(String device_id, String username,
			String service_id)
	{
		String sql = "select result_id from gw_serv_strategy_serv where device_id=? and username=?  and service_id =? ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		psql.setString(2, username);
		psql.setInt(3, StringUtil.getIntegerValue(service_id));
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty())
		{
			map = new HashMap<String, String>();
		}
		return map;
	}

	/**
	 * 根据userid查询该用户拥有的宽带业务
	 * @param userid
	 * @return
	 */
	public Map<String,String> queryServForNet(String userid)
	{
		logger.debug("queryServForNet({})", userid);
		PrepareSQL pSQL = new PrepareSQL();
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		pSQL.setSQL("select serv_type_id,wan_type,username,passwd,vlanid,bind_port from "
				+ table_serv_info
				+ " where user_id=? and serv_type_id=10 and serv_status=1");
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		return DBOperation.getRecord(pSQL.getSQL());
	}

	/**
	 * 根据宽带账号查询宽带密码
	 * @param username
	 * @return
	 */
	public Map<String,String> queryPasswdByUsername(String username)
	{
		logger.warn("queryPasswdByUsername({})", username);
		PrepareSQL pSQL = new PrepareSQL();
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		pSQL.setSQL("select username,passwd from "
				+ table_serv_info
				+ " where username=? and serv_type_id=10");
		pSQL.setString(1, username);
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 根据userid查询该用户拥有的语音业务
	 *
	 * @author wangyan
	 * @date 2016-11-22
	 * @param userid
	 * @return
	 */
	public Map<String,String> queryVoipByUserId(String userid)
	{
		logger.debug("queryVoipByUserId({})", userid);
		PrepareSQL pSQL = new PrepareSQL();
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		pSQL.setSQL("select serv_type_id,wan_type,username,passwd,vlanid,bind_port from "
				+ table_serv_info
				+ " where user_id=? and serv_type_id=14 and serv_status=1");
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 设备预置表导入
	 * 
	 * @param oui
	 *            oui
	 * @param device_serialnumber
	 *            设备序列号
	 * @param mac
	 *            mac
	 * @param city_id
	 *            属地Id
	 * @param buy_time
	 *            购买时间
	 * @param saveTime
	 *            导入时间
	 * @param vendor
	 *            厂商
	 * @param model
	 *            型号
	 * @param operaUser
	 *            操作人
	 * @param operaTime
	 * 	操作时间
	 */
	public void insertDevInit(String oui, String device_serialnumber, String mac,
			String city_id, long buyTime, long saveTime, String vendor, String model,
			String operaUser)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("insert into tab_gw_device_init (device_id,oui,device_serialnumber,city_id,buy_time,staff_id"
				+ ",cpe_mac,gw_type, dev_sub_sn, vendor_name, model_name,add_date,remark) values(?,?,?,?,?,?,?,?,?,?,?,?,? )");
		psql.setString(1, String.valueOf(GetUnusedDeviceSerial(1)));
		psql.setString(2, oui);
		psql.setString(3, device_serialnumber);
		psql.setString(4, city_id);
		psql.setLong(5, buyTime);
		psql.setString(6, operaUser);
		psql.setString(7, StringUtil.IsEmpty(mac) ? "" : mac);
		psql.setInt(8, 1);
		psql.setString(9, device_serialnumber.substring(device_serialnumber.length() - 6,
				device_serialnumber.length()));
		psql.setString(10, vendor);
		psql.setString(11, model);
		psql.setLong(12, saveTime);
		psql.setString(13, "光猫管控设备序列号添加接口导入");
		DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	public static long getMaxDeviceId4Oracle(int count)
	{
		logger.debug("getMaxDeviceId4Oracle({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		CallableStatement cstmt = null;
		Connection conn = null;
		String sql = "{call maxTR069DeviceIdProc(?,?)}";
		try
		{
			conn = DBAdapter.getJDBCConnection();
			cstmt = conn.prepareCall(sql);
			cstmt.setInt(1, count);
			cstmt.registerOutParameter(2, Types.INTEGER);
			cstmt.execute();
			serial = cstmt.getLong(2);
		}
		catch (Exception e)
		{
			logger.error("GetUnusedDeviceSerial Exception:{}", e.getMessage());
		}
		finally
		{
			sql = null;
			if (cstmt != null)
			{
				try
				{
					cstmt.close();
				}
				catch (SQLException e)
				{
					logger.error("cstmt.close SQLException:{}", e.getMessage());
				}
				cstmt = null;
			}
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch (Exception e)
				{
					logger.error("conn.close error:{}", e.getMessage());
				}
				conn = null;
			}
		}
		return serial;
	}
	
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	synchronized static public long GetUnusedDeviceSerial(int count) {
		if(DBUtil.GetDB() == 1 || DBUtil.GetDB() == 2) {
			return GetUnusedDeviceSerialOld(count);
		}
		
		// TELEDB
		return DbUtils.getUnusedID("sql_tab_gw_device", count);
	}

	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	static public long GetUnusedDeviceSerialOld(int count)
	{
		logger.debug("GetUnusedDeviceSerial({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		if (MIN_UNUSED_DEVICEID < 0)
		{
			if (DBUtil.GetDB() == 1)
			{// oracle
				MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(SUM_UNUSED_DEVICEID) - 1;
			}
			else if (DBUtil.GetDB() == 2)
			{// sybase
				MIN_UNUSED_DEVICEID = getMaxDeviceId4Sybase(SUM_UNUSED_DEVICEID) - 1;
			}
			MAX_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + SUM_UNUSED_DEVICEID;
		}
		if (MAX_UNUSED_DEVICEID < (MIN_UNUSED_DEVICEID + count))
		{
			if (SUM_UNUSED_DEVICEID < count)
			{
				if (DBUtil.GetDB() == 1)
				{// oracle
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(count) - 1;
				}
				else if (DBUtil.GetDB() == 2)
				{// sybase
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Sybase(count) - 1;
				}
				MAX_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + count;
			}
			else
			{
				if (DBUtil.GetDB() == 1)
				{// oracle
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(SUM_UNUSED_DEVICEID) - 1;
				}
				else if (DBUtil.GetDB() == 2)
				{// sybase
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Sybase(SUM_UNUSED_DEVICEID) - 1;
				}
				MAX_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + SUM_UNUSED_DEVICEID;
			}
		}
		serial = MIN_UNUSED_DEVICEID + 1;
		MIN_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + count;
		logger.debug("ID={}", serial);
		return serial;
	}

	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	public static long getMaxDeviceId4Sybase(int count)
	{
		logger.debug("getMaxDeviceId4Sybase({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		String sql = "maxTR069DeviceIdProc ?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, count);
		return DBOperation.executeProcSelect(pSQL.getSQL());
	}
	
	
	/**
	 * 获取用户上网方式
	 * 
	 * @param 用户账号
	 * @author chenxj6(75081)
	 * @date 2015-10-13
	 * @return Map<String,String>
	 */
	public List<HashMap<String,String>> getUserWanTypeIptv(int userType,String userInfo) {
		logger.debug("getUserWanTypeIptv({})",userType, userInfo);

		PrepareSQL pSql = new PrepareSQL();
		if(userType==1){
			//1：用户宽带帐号
			pSql.append("select wan_type from hgwcust_serv_info where serv_type_id = 11 ");
			pSql.append("and username = '"+userInfo+"' ");
		}else if(userType==3){
			//3：IPTV宽带帐号
			pSql.append("select wan_type from hgwcust_serv_info where user_id = (");
			pSql.append("select user_id from hgwcust_serv_info where serv_type_id = 11 ");
			pSql.append("and username = '"+userInfo+"' ) ");
			pSql.append(" and serv_type_id = 11 ");
		}else if(userType==2){
			//2：逻辑SN号
			pSql.append("select a.wan_type  from hgwcust_serv_info a,tab_hgwcustomer b ");
			pSql.append("where a.user_id = b.user_id and a.serv_type_id = 11 and b.username = '"+ userInfo + "'");
		}else if(userType==4){
			//4：VOIP业务电话号码
			pSql.append("select b.wan_type  from tab_voip_serv_param a,hgwcust_serv_info b ");
			pSql.append("where a.user_id = b.user_id and b.serv_type_id = 11 and a.voip_phone = '"+ userInfo + "'");
		}else if(userType==5){
			//5：VOIP认证账号
			pSql.append("select b.wan_type  from tab_voip_serv_param a,hgwcust_serv_info b ");
			pSql.append("where a.user_id = b.user_id and b.serv_type_id = 11 and a.voip_username = '"+ userInfo + "'");
		}
		
		logger.info(pSql.getSQL());
		List<HashMap<String,String>> map=DBOperation.getRecords(pSql.getSQL());
		if(null==map || map.isEmpty()){
			return null;
		}else{
			return map;
		}
		
	}
	
	/**
	 * IPTV业务绑定端口
	 * 
	 * @author chenxj6
	 * @param
	 * @return List<String>
	 */
	public static List<String> getUserIptvPort(String deviceId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select b.bind_port from ").append(" tab_gw_device ").append(" a,")
				.append(" hgwcust_serv_info ")
				.append(" b where a.user_id=b.user_id and a.device_id='")
				.append(deviceId).append("' and b.serv_type_id=11");
		List<String> portList = new ArrayList<String>();
		Map<String, String> map = DBOperation.getRecord(sql.toString());
		if (null == map)
		{
			return null;
		}
		String[] portArray = map.get("bind_port").split(",");
		for (String port : portArray)
		{
			if (!StringUtil.IsEmpty(port))
			{
				portList.add(port);
			}
		}
		sql = null;
		map = null;
		portArray = null;
		return portList;
	}
	
	
	/**
	 * 宽带业务绑定端口
	 * 
	 * @author chenxj6
	 * @param
	 * @return List<String>
	 */
	public static List<String> getUserIntnetPort(String deviceId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select b.bind_port from ").append(" tab_gw_device ").append(" a,")
				.append(" hgwcust_serv_info ")
				.append(" b where a.user_id=b.user_id and a.device_id='")
				.append(deviceId).append("' and b.serv_type_id=10");
		List<String> portList = new ArrayList<String>();
		Map<String, String> map = DBOperation.getRecord(sql.toString());
		if (null == map)
		{
			return null;
		}
		String[] portArray = map.get("bind_port").split(",");
		for (String port : portArray)
		{
			if (!StringUtil.IsEmpty(port))
			{
				portList.add(port);
			}
		}
		sql = null;
		map = null;
		portArray = null;
		return portList;
	}
	
	public static String getAccType(String deviceId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select access_type from gw_wan where device_id='").append(deviceId)
				.append("' and wan_id=1");
		logger.info(sql.toString()); 
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


	/**
	 * 根据用户的业务账号查询用户信息(山东联通hgu单个用户测速接口)
	 * 
	 * @param userType:用户信息类型
	 *            username:业务号码
	 * @author fanjm 35572
	 * @date 2016-11-29
	 * @return Map<String,String>
	 */
	public Map<String, String> qryUserSDTestSpeed(int userType, String username) {
		logger.debug("qryUserSDTestSpeed({}{})",new Object[]{userType, username});

		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";

		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,b.username,c.device_id,c.oui,c.device_serialnumber,a.city_id,b.serv_type_id");
		
		switch (userType) {
		case 1://用户宽带帐号
			psql.append(" from tab_gw_device c, " + table_customer + " a, " + table_serv_info + " b");
			psql.append(" where c.device_id = a.device_id and a.user_id=b.user_id and b.serv_status= 1");
			psql.append(" and b.username='" + username + "' and b.serv_type_id = 10 order by a.updatetime desc");
			break;
		case 2://用户iptv账号
			psql.append(" from tab_gw_device c, " + table_customer + " a, " + table_serv_info + " b");
			psql.append(" where c.device_id = a.device_id and a.user_id=b.user_id and b.serv_status= 1");
			psql.append(" and b.username='" + username + "' and b.serv_type_id = 11 order by a.updatetime desc");
			break;
		default:
			logger.error("userType illegal");
			return null;
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	
	public String getRateFromUserId(long userId,int servTypeId,String username){
		logger.debug("getRateFromUserId");
		String rate = "";
		String sql = "select speed from tab_netspeed_param where user_id = ? and serv_type_id = ? and username = ? ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);
		psql.setString(3, username);
		Map<String,String> tmp = DBOperation.getRecord(psql.getSQL());
		if(tmp != null && tmp.size() > 0){
			rate = StringUtil.getStringValue(tmp,"speed");
		}
		return rate;
	}
	
	public Map<String,String> getTestAccount(String test_rate,String city_id){
		logger.debug("getTestAccount");
		String p_city_id = CityDAO.getCityIdPidMap().get(city_id);
		if("00".equals(p_city_id)){
			p_city_id = city_id;
		}
		String sql = "select * from test_speed_net where test_rate = ? and city_id = ? ";
		// mysql
		if (3 == DBUtil.GetDB()) {
			sql = "select net_account,net_password from test_speed_net where test_rate = ? and city_id = ? ";		
		}
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, test_rate);
		psql.setString(2, p_city_id);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public Map<String,String> getTestAccountPar(String test_rate,String city_id){
		logger.debug("getTestAccount");
		String p_city_id = CityDAO.getCityIdPidMap().get(city_id);
		if("00".equals(p_city_id)){
			p_city_id = city_id;
		}
		String sql = "select * from test_speed_net where test_rate = ? and (city_id in (select parent_id from tab_city where city_id=?) or city_id = ?)";
		// mysql
		if (3 == DBUtil.GetDB()) {
			sql = "select net_account,net_password from test_speed_net where test_rate = ? and (city_id in (select parent_id from tab_city where city_id=?) or city_id = ?)";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, test_rate);
		psql.setString(2, p_city_id);
		psql.setString(3, p_city_id);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	/**
	 * @return
	 */
	public int updateStatus(int status,String device_id,int times,long task_id,int test_type)
	{
		logger.debug("updateStatus()");
		String sql = "update test_speed_dev set process_status = " + status + ",update_time = " + System.currentTimeMillis() / 1000 
				+ ",process_times = " + ++times + " where device_id = " + device_id + " and task_id = ? and test_type = ? ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, task_id);
		psql.setInt(2, test_type);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	/**
	 * 测速相关设备信息入库
	 * @param userDevInfo
	 */
	public int insertTestSpeedDev(Map<String, String> userDevInfo)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("insert into test_speed_dev(task_id,device_id,city_id ,device_serialnumber,user_id," +
					"serv_name,create_time,update_time,test_type,process_status,process_times) values (?,?,?,?,?,?,?,?,?,?,?)");
		int index = 1;
		
		//生成3位随机数,task_id为当前时间后加三位随机数
		int numplus = (int)(1+Math.random()*(1000-101+1));
		
		psql.setLong(index++, StringUtil.getLongValue(System.currentTimeMillis()/1000)*1000+numplus);
		String city_id = Global.G_City_Pcity_Map.get(StringUtil.getStringValue(userDevInfo, "city_id"));
		if("00".equals(city_id)){
			city_id = StringUtil.getStringValue(userDevInfo, "city_id");
		}
		psql.setString(index++, StringUtil.getStringValue(userDevInfo, "device_id"));
		psql.setString(index++, city_id);
		psql.setString(index++, StringUtil.getStringValue(userDevInfo, "device_serialnumber"));
		psql.setLong(index++, StringUtil.getLongValue(userDevInfo, "user_id"));
		psql.setString(index++, StringUtil.getStringValue(userDevInfo, "username"));
		psql.setLong(index++, System.currentTimeMillis()/1000);
		psql.setLong(index++, System.currentTimeMillis()/1000);
		psql.setInt(index++, StringUtil.getIntValue(userDevInfo,"serv_type_id",10));
		psql.setInt(index++, 0);
		psql.setInt(index++, 0);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 根据用户的loid、宽带账号查询宽带用户、终端信息
	 * 
	 * @param loid:用户逻辑标识
	 * @param username:业务账号(宽带)
	 * @author fanjm 35572
	 * @date 2016-12-2
	 * @return Map<String,String>
	 */
	public Map<String, String> qryUserByNameAndLoid(String loid, String userName) {
		logger.debug("qryUserByNameAndLoid({}{})",new Object[]{loid, userName});
		if(!"sd_lt".equals(Global.G_instArea))
		{
		if (StringUtil.IsEmpty(loid)) {
			logger.error("loid is Empty");
			return null;
		}
		}
		else if (StringUtil.IsEmpty(userName)) {
			logger.error("userName is Empty");
			return null;
		}
		

		StringBuffer sb = new StringBuffer();
		sb.append("select a.user_id, a.device_id,a.oui,a.device_serialnumber,b.wan_type from tab_hgwcustomer a left join hgwcust_serv_info b on (a.user_id=b.user_id and b.serv_type_id=10) where b.username='")
		.append(userName).append("'");
		if(!StringUtil.IsEmpty(loid))
		{
			sb.append(" and a.username='").append(loid).append("'");
		}
		sb.append(" order by a.updatetime desc");
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		ArrayList<HashMap<String, String>> records = DBOperation.getRecords(psql.getSQL());
		if(null!=records&&records.size()>0){
			return records.get(0);
		}else{
			return null;
		}
		
	}
	
	
	/**
	 * 根据用户的loid、宽带账号查询宽带用户、终端信息(BBMS)
	 * @Description TODO
	 * @author guxl3
	 * @date 2019年3月27日
	 * @param loid
	 * @param userName
	 * @return  
	 * @throws
	 */
	public Map<String, String> qryUserByNameAndLoid_BMMS(String loid, String userName) {
			
		if (StringUtil.IsEmpty(loid)) {
			logger.error("loid is Empty");
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("select a.user_id, a.device_id,a.oui,a.device_serialnumber,b.wan_type from tab_egwcustomer a left join egwcust_serv_info b on (a.user_id=b.user_id and b.serv_type_id=10) where b.username='")
		.append(userName).append("'");
		if(!StringUtil.IsEmpty(loid))
		{
			sb.append(" and a.username='").append(loid).append("'");
		}
		sb.append(" order by a.updatetime desc");
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		ArrayList<HashMap<String, String>> records = DBOperation.getRecords(psql.getSQL());
		if(null!=records&&records.size()>0){
			return records.get(0);
		}else{
			return null;
		}
		
	}
	
	/**
	 * 根据用户的loid、宽带账号/iptv账号查询宽带用户、终端信息
	 * 
	 * @param loid:用户逻辑标识
	 * @param username:业务账号(宽带iptv)
	 * @param servType:业务类型
	 * @author fanjm 35572
	 * @date 2016-12-2
	 * @return Map<String,String>
	 */
	public Map<String, String> qryUserByNameAndLoid(String loid, String userName, String servType) {
		logger.debug("qryUserByNameAndLoid({}{}{})",new Object[]{loid, userName, servType});
		if (StringUtil.IsEmpty(userName)) {
			logger.error("userName is Empty");
			return null;
		}else if (StringUtil.IsEmpty(servType)) {
			logger.error("servType is Empty");
			return null;
		}
		if("1".equals(servType)){
			servType = "10";
		}else if("2".equals(servType)){
			servType = "11";
		}else{
			logger.error("servType is Error");
			return null;
		}
		

		StringBuffer sb = new StringBuffer();
		sb.append("select a.user_id, a.device_id,a.oui,a.device_serialnumber,b.wan_type from tab_hgwcustomer a left join hgwcust_serv_info b on (a.user_id=b.user_id and b.serv_type_id='")
		.append(servType).append("') where b.username='").append(userName).append("'");
		if(!StringUtil.IsEmpty(loid))
		{
			sb.append(" and a.username='").append(loid).append("'");
		}
		sb.append(" order by a.updatetime desc");
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		ArrayList<HashMap<String, String>> records = DBOperation.getRecords(psql.getSQL());
		if(null!=records&&records.size()>0){
			return records.get(0);
		}else{
			return null;
		}
		
	}
	
	/**
	 * 根据用户的loid、宽带账号修改密码
	 * @param user_id 用户ID
	 * @param userName 上网账号
	 * @param pwd 上网密码
	 * @return 数据库更新结果
	 */
	public int modCustomerPwd(String user_id, String userName, String pwd)
	{
		logger.debug("modCustomerPwd({}{}{})",new Object[]{user_id, userName, pwd});
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("update hgwcust_serv_info set passwd = ? where serv_status=1 and serv_type_id=10 and user_id = ? ");
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.append(" and username=? ");
		}
		psql.setString(1, pwd);
		psql.setLong(2, StringUtil.getLongValue(user_id));
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.setString(3, userName);
		}
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	
	public int modCustomerPwd_BMMS(String user_id, String userName, String pwd)
	{
		logger.debug("modCustomerPwd({}{}{})",new Object[]{user_id, userName, pwd});
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("update egwcust_serv_info set passwd = ? where serv_status=1 and serv_type_id=10 and user_id = ? ");
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.append(" and username=? ");
		}
		psql.setString(1, pwd);
		psql.setLong(2, StringUtil.getLongValue(user_id));
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.setString(3, userName);
		}
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 根据用户的loid、账号修改密码
	 * @param user_id 用户ID
	 * @param userName 账号
	 * @param pwd 上网密码
	 * @param servType 业务类型
	 * @return 数据库更新结果
	 */
	public int modCustomerPwd(String user_id, String userName, String pwd,String servType)
	{
		logger.debug("modCustomerPwd({}{}{}{})",new Object[]{user_id, userName, pwd, servType});
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("update hgwcust_serv_info set passwd = ? where serv_status=1 and serv_type_id=? and user_id = ? ");
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.append(" and username=? ");
		}
		psql.setString(1, pwd);
		psql.setString(2, servType);
		psql.setLong(3, StringUtil.getLongValue(user_id));
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.setString(4, userName);
		}
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 根据用户的loid、宽带账号修改密码
	 * @param user_id 用户ID
	 * @param userName 上网账号
	 * @param userName 上网密码
	 * @return 数据库更新结果
	 */
	public int modPwdAndStatus(String user_id, String userName, String pwd)
	{
		logger.debug("modCustomerPwd({}{}{})",new Object[]{user_id, userName, pwd});
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("update hgwcust_serv_info set open_status=0,passwd = ? where serv_status=1 and serv_type_id=10 and user_id = ? ");
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.append(" and username=? ");
		}
		psql.setString(1, pwd);
		psql.setLong(2, StringUtil.getLongValue(user_id));
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.setString(3, userName);
		}
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 根据用户的loid、宽带账号修改密码
	 * @param user_id 用户ID
	 * @param userName 上网账号
	 * @param userName 上网密码
	 * @return 数据库更新结果
	 */
	public int modPwdAndStatus(String user_id, String userName, String pwd, String servType)
	{
		logger.debug("modCustomerPwd({}{}{})",new Object[]{user_id, userName, pwd});
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("update hgwcust_serv_info set open_status=0,passwd = ? where serv_status=1 and serv_type_id=? and user_id = ? ");
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.append(" and username=? ");
		}
		psql.setString(1, pwd);
		psql.setString(2, servType);
		psql.setLong(3, StringUtil.getLongValue(user_id));
		if(!StringUtil.IsEmpty(userName) && !"null".equalsIgnoreCase(userName)){
			psql.setString(4, userName);
		}
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	
	
	/**
	 * 根据devSN查询设备名称(支持后六位)
	 * 
	 * @param devSn:设备SN号
	 * @author fanjm 35572
	 * @date 2016-12-6
	 * @return ArrayList<HashMap<String, String>> 结果集合
	 */
	public ArrayList<HashMap<String, String>> qryDevNameByDevSN(String devSn) {
		logger.debug("countDevByDevSN({}{})",new Object[]{devSn});

		if (StringUtil.IsEmpty(devSn) || devSn.length()<6) {
			logger.error("devSn is Empty or length less than 6.");
			return null;
		}
		
		String serial = "";
		//devSn是否是oui-序列号的格式
		int index = devSn.indexOf("-");
		if(-1!=index){
			serial = devSn.substring(index+1);
		}
		else{
			serial = devSn;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("select d.device_name from tab_gw_device d where d.dev_sub_sn='")
		.append(serial.substring(serial.length()-6)).append("' and d.device_serialnumber like '%").append(serial).append("%'");
		if(-1!=index){
			sb.append(" and d.device_name='").append(devSn).append("'");
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据LOID查询用户设备信息
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfoByLoid(String loid) {
		logger.debug("queryDeviceInfoByLoid,loid({})", loid);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,b.device_id,b.device_serialnumber from tab_hgwcustomer a ");
		psql.append(" left join tab_gw_device b on a.device_id = b.device_id");
		psql.append(" where a.username = '" + loid + "'");
		psql.append(" and a.user_state in ('1','2')");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据LOID，账号查询用户设备信息
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfoByAccount(String account) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select c.vlanid,a.user_id,b.device_id,b.device_serialnumber from tab_hgwcustomer a ");
		psql.append(" left join tab_gw_device b on a.device_id = b.device_id");
		psql.append(" left join hgwcust_serv_info c on c.user_id = a.user_id");
		psql.append(" where a.user_state in ('1','2')");
		psql.append(" and c.serv_type_id=10 and c.username='"+account+"'");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 根据LOID查询用户设备信息
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfoByLoidWithModel(String loid) {
		logger.debug("queryDeviceInfoByLoid,loid({})", loid);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,e.device_model,b.device_id,b.device_serialnumber from tab_hgwcustomer a ");
		psql.append(" left join tab_gw_device b on a.device_id = b.device_id left join gw_device_model e on (b.device_model_id =e.device_model_id) ");
		psql.append(" where a.username = '" + loid + "'");
		psql.append(" and a.user_state in ('1','2')");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据设备序列号查询设备用户信息
	 * @param devSn
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfoByDevSn(String devSn){
		logger.debug("queryDeviceInfoByDevSn,devSn({})", devSn);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,a.device_serialnumber,b.user_id from tab_gw_device a ");
		psql.append(" left join tab_hgwcustomer b on a.device_id = b.device_id");
		psql.append(" where a.dev_sub_sn = '" + devSn.substring(devSn.length()-6) + "'");
		psql.append(" and a.device_serialnumber like '%" + devSn + "'");
		psql.append(" and b.user_state in ('1','2')");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 根据设备序列号查询设备用户信息
	 * @param devSn
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfoByDevSn(String devSn, String account){
		logger.debug("queryDeviceInfoByDevSn,devSn({})", devSn);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select c.vlanid,a.device_id,a.device_serialnumber,b.user_id from tab_gw_device a ");
		psql.append(" left join tab_hgwcustomer b on a.device_id = b.device_id");
		psql.append(" left join hgwcust_serv_info c on b.user_id = c.user_id");
		psql.append(" where a.dev_sub_sn = '" + devSn.substring(devSn.length()-6) + "'");
		psql.append(" and a.device_serialnumber like '%" + devSn + "'");
		psql.append(" and b.user_state in ('1','2') and c.username='"+account + "' and c.serv_type_id=10");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	
	/**
	 * 根据userId查询上网方式
	 * @param userId
	 * @return
	 */
	public  ArrayList<HashMap<String, String>> queryWanTypeByUserId(String userId){
		logger.debug("queryWanTypeByUserId,userId({})", userId);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select wan_type from hgwcust_serv_info");
		psql.append(" where user_id = " + userId);
		psql.append(" and serv_type_id = 10");
		psql.append(" and serv_status = 1");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 根据devSN查询相关业务信息（业务ID属于10.11.14）
	 * 
	 * @param devSn:设备SN号
	 * @author fanjm 35572
	 * @date 2016-12-6
	 * @return ArrayList<HashMap<String, String>> 结果集
	 */
	public ArrayList<HashMap<String, String>> qryServByDevSN(String devSn) {
		logger.debug("qryServByDevSN({})",new Object[]{devSn});

		if (StringUtil.IsEmpty(devSn) || devSn.length()<6) {
			logger.error("devSn is Empty or length less than 6.");
			return null;
		}

		String serial = "";
		//devSn是否是oui-序列号的格式
		int index = devSn.indexOf("-");
		if(-1!=index){
			serial = devSn.substring(index+1);
		}
		else{
			serial = devSn;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("select a.user_id, b.serv_type_id, b.username, b.passwd from tab_gw_device d, tab_hgwcustomer a, hgwcust_serv_info b ")
		.append("where d.device_id = a.device_id and a.user_id=b.user_id and b.serv_type_id in (10,11,14) and d.dev_sub_sn='")
		.append(serial.substring(serial.length()-6)).append("' and d.device_serialnumber like '%").append(serial).append("%'");
		
		if(-1!=index){
			sb.append(" and d.device_name='").append(devSn).append("'");
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
	
	
	
	/**
	 * 根据user_id查询语音业务VoIP认证账号、VoIP认证密码
	 * 
	 * @param user_id:客户ID
	 * @author fanjm 35572
	 * @date 2016-12-6
	 * @return ArrayList<HashMap<String, String>> 结果集
	 */
	public ArrayList<HashMap<String, String>> qryAuthByUserID(String user_id) {
		logger.debug("qryAuthByUserID({})",new Object[]{user_id});

		if (StringUtil.IsEmpty(user_id)) {
			logger.error("user_id is Empty.");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("select voip_username, voip_passwd from tab_voip_serv_param where user_id=").append(user_id);
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
	
	/**
	 * 根据loid查询是否存在
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> isLoidExisted(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		// mysql
		if (3 == DBUtil.GetDB()) {
			psql.setSQL("select username from tab_hgwcustomer where username=?");		
		} else {
			psql.setSQL("select * from tab_hgwcustomer where username=?");
		}
		psql.setString(1, loid);
		ArrayList<HashMap<String, String>> devMap = DBOperation.getRecords(psql.getSQL());
		if (null == devMap )
		{
			return null;
		}
		else
		{
			return devMap;
		}
	}
	
	
	
	/**
	 * 根据loid查询48小时内解绑记录
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getBindLog(String loid)
	{
		long nowTime = DateUtil.currentTimeInSecond();
		//48小时前
		long smlTime = nowTime - 172800;
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("select device_id from bind_log where username=? and oper_type=2 and binddate >? and binddate < ? order by binddate desc");
		psql.setString(1, loid);
		psql.setLong(2, smlTime);
		psql.setLong(3, nowTime);
		ArrayList<HashMap<String, String>> devMap = DBOperation.getRecords(psql.getSQL());
		if (null == devMap )
		{
			return null;
		}
		else
		{
			return devMap;
		}
	}
	/**
	 * 根据设备ID，查询设备序列号
	 * @param loid
	 * @return
	 */
	public String getDeviceName(List<String> devidList)
	{
		String result = "";
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("select device_name from tab_gw_device where device_id in (" + StringUtils.weave(devidList) + ") " );
		
		ArrayList<HashMap<String, String>> devMap = DBOperation.getRecords(psql.getSQL());
		if (null == devMap )
		{
			return result;
		}
		else
		{
			for (HashMap<String, String> hashMap : devMap) {
				result += hashMap.get("device_name")+";";
			}
			return result.substring(0, result.length()-1);
		}
	}

	/**
	 * 根据用户的业务账号查询loid
	 * 
	 */
	public ArrayList<HashMap<String, String>> queryLoid(String netAccount) {
		logger.debug("maxSpeed,avgSpeed({},{})", netAccount);
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select distinct loid from tab_http_diag_result where 1=1");
		psql.append(" and netaccount ='" + netAccount+ "'");

		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 根据用户的业务账号查询宽带测速结果
	 * 
	 */
	public Map<String, String> queryHttpResult(String netAccount, String loid) {
		logger.debug("maxSpeed,avgSpeed({},{})", netAccount,loid);

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select status,loid, device_serialnumber,maxspeed,avgspeed from tab_http_diag_result where 1=1");
		if(StringUtil.IsEmpty(loid)){
			psql.append(" and netaccount ='" + netAccount+ "'");
		} else {
			psql.append(" and loid ='" + loid+ "'");
		}
		psql.append(" order by test_time desc");
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 安徽电信: 根据loid查询设备id
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceIdByLoid(String loid) {
		logger.info("queryDeviceIdByLoid,loid({})", loid);
		PrepareSQL psql = new PrepareSQL();
		psql.append("  select a.device_id,a.user_id from tab_hgwcustomer a ");
		psql.append(" left join tab_gw_device_init b on a.DEVICE_ID = b.DEVICE_ID");
		psql.append(" where a.username = '" + loid + "'");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 安徽电信: 根据设备id查询串号和序列号
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> querySerialNoByDeviceId(String deviceId) {
		logger.info("querySerialNoByDeviceId,deviceId({})", deviceId);
		PrepareSQL psql = new PrepareSQL();
		psql.append("  select a.device_serialnumber,a.serial_no from tab_gw_device_init a ");
		psql.append(" where a.device_id = '" + deviceId + "'");
		return DBOperation.getRecords(psql.getSQL());
	}
	

	/**
	 * 对于桥接设备，根据设备序列号查询tab_speed_dev_rate、 tab_speed_net 得测试账号密码以及pppoe账号
	 * 1.父属地为00，取自己属地的测试账号 
	 * 2.父属地不是00，取父属地对应的测试账号
	 * 
	 * @param 终端序列号
	 * @author fanjm 
	 * @date 2017-7-4
	 * @return ArrayList<HashMap<String,String>>
	 *         devSn为空，则返回null，其他返回DBOperation.getRecords(sql)
	 */
	public ArrayList<HashMap<String, String>> queryNet_account(String devSn) {
		logger.debug("queryNet_account({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}

		PrepareSQL psql = new PrepareSQL();
		// mysql
		if (3 == DBUtil.GetDB()) {
			psql.append("select case when c.parent_id = '00' then c.city_id else c.parent_id end as city_id,concat(a.pppoe_name,IFNULL(a.account_suffix,'')) pppoe_name,a.rate from tab_speed_dev_rate a,tab_gw_device d,tab_city c where a.device_serialnumber = ? and a.device_serialnumber = d.device_serialnumber and d.city_id=c.city_id order by rate desc");
		} else {
			psql.append("select case when c.parent_id = '00' then c.city_id else c.parent_id end as city_id,a.pppoe_name||nvl(a.account_suffix,'') pppoe_name,a.rate from tab_speed_dev_rate a,tab_gw_device d,tab_city c where a.device_serialnumber = ? and a.device_serialnumber = d.device_serialnumber and d.city_id=c.city_id order by rate desc");
		}
//		psql.append("select x.pppoe_name||nvl(x.su,'') pppoe_name,y.net_account,y.net_password from (select case when c.parent_id = '00' then c.city_id else c.parent_id end as city_id,a.rate,a.account_suffix su,a.pppoe_name from tab_speed_dev_rate a,tab_gw_device d,tab_city c where a.device_serialnumber = ? and a.device_serialnumber = d.device_serialnumber and d.city_id=c.city_id) x ,tab_speed_net y where x.rate=y.test_rate and y.city_id = x.city_id");
		psql.setString(1, devSn);
		logger.info(psql.getSQL());
		ArrayList<HashMap<String, String>> rateList = DBOperation.getRecords(psql.getSQL());
		if (rateList.size()>0){
			String cityId = rateList.get(0).get("city_id");
			int rate = StringUtil.getIntegerValue(rateList.get(0).get("rate"));
			String pppoe_name = rateList.get(0).get("pppoe_name");
			psql = new PrepareSQL();
			psql.append("select y.test_rate as rate,y.net_account,y.net_password from tab_speed_net y where y.test_rate = ? and y.city_id = '");
			psql.append(cityId+"'");
			psql.setInt(1, rate);
			ArrayList<HashMap<String, String>> accountList = DBOperation.getRecords(psql.getSQL());
			if (accountList.size()>0 && !accountList.isEmpty()){
				accountList.get(0).put("pppoe_name", pppoe_name);
				return accountList;
			}else{
				return null;
			}
			 
		}else{
			return null;
		}
	}
	
	
	/**
	 * 设备带宽表，根据devsn查询用户账号(河北联通)
	 * @param devSn
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getUserNameByDevSn(String devSn)
	{
		logger.info("getUserNameByDevSn,devSn({})", devSn);
		PrepareSQL psql = new PrepareSQL();
		// mysql
		if (3 == DBUtil.GetDB()) {
			psql.append("select concat(pppoe_name,IFNULL(account_suffix,'')) pppoe_name from tab_speed_dev_rate ");
		} else {
			psql.append("select pppoe_name||nvl(account_suffix,'') pppoe_name from tab_speed_dev_rate ");
		}
		psql.append(" where device_serialnumber = '" + devSn + "'");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据业务宽带账号查询测速账号和密码
	 * @param netAccount
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getSpeedInfoByNetAccount(String netAccount)
	{
		logger.info("getSpeedInfoByNetAccount,netAccount({})", netAccount);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.net_account,b.net_password from tab_speed_dev_rate a,tab_speed_net b ");
		psql.append(" where a.rate=b.test_rate and a.parent_id = b.city_id");
		psql.append(" and a.pppoe_name = '" + netAccount + "'");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据loid查询是否存在
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> checkUserByDevId(String DeviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("select online_status from tab_hgwcustomer where device_id=?");
		psql.setString(1, DeviceId);
		ArrayList<HashMap<String, String>> devMap = DBOperation.getRecords(psql.getSQL());
		if (null == devMap )
		{
			return null;
		}
		else
		{
			return devMap;
		}
	}
	
	/**
	 * 根据oui和设备序列号查询
	 * @param param
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getDeviceByOui(String param) {
		logger.debug("getDeviceByOui({})", param);
		int n = param.indexOf("-");
		String oui = param.substring(0, n);
		String devSn = param.substring(n + 1);
		String table_customer = "tab_hgwcustomer";
		String gw_type = " and gw_type = 1 ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			gw_type = " and gw_type = 2 ";
		}
		String strSQL = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
				+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id"
				+ " where a.oui = '" + oui
				+ "' and a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	/**
	 * 企业网关根据oui和设备序列号查询
	 */
	public ArrayList<HashMap<String, String>> getDeviceByOui2(String param) {
		logger.debug("getDeviceByOui({})", param);
		int n = param.indexOf("-");
		String oui = param.substring(0, n);
		String devSn = param.substring(n + 1);
		String	table_customer = "tab_egwcustomer";
		String	gw_type = " and gw_type = 2 ";
		String strSQL = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username, b.userline, b.updatetime"
				+ " from tab_gw_device a left join " + table_customer + " b on a.device_id=b.device_id"
				+ " where a.oui = '" + oui
				+ "' and a.dev_sub_sn='"
				+ devSn.substring(devSn.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSn + "'" + gw_type;
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}
	/**
	 * 根据deviceId查询业务下发信息
	 * @param param
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getServResultList(String deviceId) {
		logger.debug("getServResultList({})", deviceId);
		String strSQL = "select a.service_id, b.fault_desc from " + Global.STRATEGY_TABNAME + " a, tab_cpe_faultcode b " +
				" where a.result_id=b.fault_code and a.device_id = '" + deviceId + "'";
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}

	/**
	 * 根据宽带账号或者逻辑ID查询VLAN信息
	 * @param param
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getVlanInfoList(String userName,String loid) 
	{   String strSQL = null;
		if(!StringUtil.IsEmpty(userName) )
	    {
		   strSQL = "select serv_type_id,vlanid,multicast_vlanid from hgwcust_serv_info a"+
				" where a.username='" + userName + "' order by serv_type_id";
	    }else 
	    {
	       strSQL = "select serv_type_id,vlanid,multicast_vlanid from hgwcust_serv_info a where a.user_id in (select b.user_id from tab_hgwcustomer b where b.username = '"+loid+"')  order by serv_type_id"	;
	    }
		logger.info(strSQL);
		return DBOperation.getRecords(strSQL);
	}	
	

	
	/**
	 * 增加用户销户表
	 * @param account
	 * @param type
	 * @return
	 */
	public int recordDeleteCustomer(String account,int type){
		ArrayList<String> sqlList = new ArrayList<String>();
		String deleteSQL = "delete from tab_delete_customer where account='"+account+"' and type=" + type;
		sqlList.add(deleteSQL);
		PrepareSQL insertPsql = new PrepareSQL("insert into tab_delete_customer(account,type,result,update_time) values(?,?,?,?)");
		insertPsql.setString(1, account);
		insertPsql.setInt(2, type);
		insertPsql.setInt(3, 0);
		insertPsql.setLong(4, System.currentTimeMillis()/1000);
		sqlList.add(insertPsql.getSQL());
		return DBOperation.executeUpdate(sqlList);
	}
	
	/**
	 * 查询设备类型
	 * @param account
	 * @param type
	 * @return
	 */
	public String queryDeviceType(String account,String type){
		String tableCustoemr = "tab_hgwcustomer";
		String tableServinfo = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableCustoemr = "tab_egwcustomer";
			tableServinfo = "egwcust_serv_info";
		}
		String strSQL = "select c.type_name from " + tableCustoemr + " a,gw_cust_user_dev_type b,gw_dev_type c " +
				"where a.user_id = b.user_id and b.type_id = c.type_id  and a.username='" + account + "'";
		if("1".equals(type)){
			strSQL = "select c.type_name from " + tableServinfo + " a,gw_cust_user_dev_type b,gw_dev_type c " +
					"where a.user_id = b.user_id and b.type_id = c.type_id and a.serv_type_id = 10 and a.username='" + account + "'";
		}
		List<HashMap<String, String>>  deviceTypeList =  DBOperation.getRecords(strSQL);
		if(null != deviceTypeList && !deviceTypeList.isEmpty() && null != deviceTypeList.get(0)){
			return deviceTypeList.get(0).get("type_name");
		}
		return "";
	}
	
	/**
	 * 查询设备类型名称
	 * @param devSn
	 * @return
	 */
	public String queryDeviceTypeName(String devSn){
		
		if(StringUtil.IsEmpty(devSn)){
			return "";
		}	
		
		String strSQL = "select a.oui,c.vendor_name,d.device_model,b.access_style_relay_id " +
				" from tab_gw_device a " +
				" left join tab_devicetype_info b on a.devicetype_id = b.devicetype_id" +
				" left join tab_vendor c on a.vendor_id = c.vendor_id" +
				" left join gw_device_model d on a.device_model_id = d.device_model_id" +
				" where a.device_serialnumber like '%" + devSn + "'" +
						" and a.dev_sub_sn='" + devSn.substring(devSn.length() - 6) + "'";
		List<HashMap<String, String>> devieInfoList =  DBOperation.getRecords(strSQL);
		if(null != devieInfoList && !devieInfoList.isEmpty() && null != devieInfoList.get(0)){
			HashMap<String, String> deviceInfoMap = devieInfoList.get(0);
			String accessType = deviceInfoMap.get("access_style_relay_id");
			if("1".equals(accessType)){//ADSL
				accessType="ADSL";
			}else if("2".equals(accessType)){//LAN
				accessType="LAN";
			}else if("3".equals(accessType)){//EPON
				accessType="EPON";
			}else if("4".equals(accessType)){
				accessType="GPON";
			}else{
				accessType="";
			}
			return  StringUtil.getStringValue(deviceInfoMap.get("vendor_name")) + "-" + accessType + 
					"-" + StringUtil.getStringValue(deviceInfoMap.get("oui")) + "-" + StringUtil.getStringValue(deviceInfoMap.get("device_model"));
		}
		return "";		
	}
	
	/**
	 * 根据userid查询该用户拥有的IPTV业务
	 * @param userid
	 * @return
	 */
	public Map<String,String> queryServForIptv(String userid)
	{
		logger.debug("queryServForNet({})", userid);
		PrepareSQL pSQL = new PrepareSQL();
		String table_serv_info = "hgwcust_serv_info";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_serv_info = "egwcust_serv_info";
		}
		pSQL.setSQL("select serv_type_id,wan_type,username,passwd,vlanid,bind_port,multicast_vlanid from "
				+ table_serv_info
				+ " where user_id=? and serv_type_id=11 and serv_status=1");
		pSQL.setLong(1, StringUtil.getLongValue(userid));
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 根据deviceId查询设备表
	 * @param loid
	 * @return
	 */
	public ArrayList<HashMap<String, String>> qryDevId(String DeviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("select oui,device_serialnumber from tab_gw_device where device_id=?");
		psql.setString(1, DeviceId);
		ArrayList<HashMap<String, String>> devMap = DBOperation.getRecords(psql.getSQL());
		if (null == devMap )
		{
			return null;
		}
		else
		{
			return devMap;
		}
	}
	
	
	public Map<String, String> queryUserInfo4CQ(int userType, String username) {
		logger.debug("queryUserInfo({})", username);

		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.device_id,a.user_id,a.username as loid,b.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id,a.updatetime ");
		
		switch (userType) {
		case 1://宽带账号
			psql.append("from " + table_customer + " a, " + table_serv_info + " b");
			psql.append("	where a.user_id=b.user_id and b.serv_status= 1");
			psql.append("	and b.username='" + username + "' and b.serv_type_id = 10");
			psql.append("	order by a.updatetime desc");
			break;
		case 2://loid
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b where a.user_state = '1' and a.user_id=b.user_id ");
			psql.append(" and a.username = '" + username + "' and b.serv_type_id='10'");
			break;
		case 3://devsn
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b ,tab_gw_device d where a.user_state = '1' and a.user_id=b.user_id and a.device_id = d.device_id");
			psql.append(" and d.device_serialnumber like '%" + username + "'");
			if(username.length() >= 6){
				psql.append(" and d.dev_sub_sn = '" + username.substring(0, 6) + "'");
			}
			break;
		default:
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 根据设备序列号查找userId
	 * @param
	 * @param devSn
	 * @return
	 */
	public Map<String, String> queryUserIdByDevSn( String devSn) {
		logger.debug("queryUserIdByDevSn({})", devSn);

		if (StringUtil.IsEmpty(devSn)) {
			logger.error("devSn is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.device_id,a.user_id,a.username as loid ");
		
		psql.append(" from " + table_customer + " a, tab_gw_device d where a.user_state = '1' and a.device_id = d.device_id");
		psql.append(" and d.device_serialnumber like '%" + devSn + "'");
		if(devSn.length() >= 6){
			psql.append(" and d.dev_sub_sn = '" + devSn.substring(devSn.length()-6,devSn.length()) + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 根据用户ID获取宽带账号以及loid
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryUserInfoByUserId(String userId){
		
		if (StringUtil.IsEmpty(userId)) {
			logger.error("userId is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		
		String sql = "select a.username loid ,b.username netaccount from "+table_customer+" a , "
		             +" hgwcust_serv_info b where 1=1 and a.user_id = b.user_id "
				     +" and a.user_id = "+userId
				     +" and b.serv_type_id = 10";
		PrepareSQL psql = new PrepareSQL(sql);
		
		try
		{
			return DBOperation.getRecords(psql.getSQL());
		}
		catch (Exception e)
		{
			logger.error("queryUserInfoByUserId error msgs:"+e.getMessage());
			return null;
		}
	}

  public int queryUserNmaeExist(int userType, String username){
	  if(StringUtil.IsEmpty(username)){
		  logger.error("userName is null");
	  }
	  
	  String querySql = "";
	  if(userType == 1){
		  querySql = "select count(1) as num from hgwcust_serv_info where username= '" + username + "' and serv_type_id=10";
	  }else{
		  querySql = "select count(1) as num from tab_hgwcustomer where username= '" + username + "'";
	  }
	  
	  PrepareSQL psql = new PrepareSQL(querySql);
	  try
	 {
		  Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		  return StringUtil.getIntValue(map,"num");
	 }
	 catch (Exception e)
	 {
		 logger.error("queryUserNmaeExist error ,msgs:"+e.getMessage());
		 return 0;
	 }
  }
	
  public ArrayList<HashMap<String, String>> querySnByUserInfo(int userType, String username){
	  
	  if(StringUtil.IsEmpty(username)){
		  logger.error("userName is null");
	  }
	  
	  String querySql = "";
	  if(userType == 1){
		  querySql = "select b.device_serialnumber,c.device_name from hgwcust_serv_info a,tab_hgwcustomer b,tab_gw_device c "+
	                 "where a.user_id=b.user_id and a.serv_type_id = 10 "+
		              " and a.username='" + username +"' and b.device_id =c.device_id and b.device_id is not null order by b.binddate desc";
	  }else{
		  querySql = "select b.device_serialnumber,b.device_name from tab_hgwcustomer a ,tab_gw_device b where a.username='" + username + "' and a.device_id=b.device_id and a.device_id is not null order by a.binddate desc";
	  }
	  
	  PrepareSQL psql = new PrepareSQL(querySql);
	  try
	 {
		 return DBOperation.getRecords(psql.getSQL());
		 
		// return StringUtil.getStringValue(map,"device_serialnumber");
	 }
	 catch (Exception e)
	 {
		 logger.error("querySnByUserInfo error ,msgs:"+e.getMessage());
		 return null;
	 }
  }
  
  /**
   * 查看当前设备是否是百兆光猫
   * tab_gw_ht_megabytes中存的都是百兆光猫的设备，由现场导入
   * @param devSN
   * @return
   */
public int queryHTMegabytes(String devSN){
	  
	  if(StringUtil.IsEmpty(devSN)){
		  logger.error("userName is null");
	  }
	  
	  String querySql = "select count(1) as num from tab_gw_ht_megabytes where device_sn='"+ devSN + "'";
	  
	  PrepareSQL psql = new PrepareSQL(querySql);
	  try
	 {
		  Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		  return StringUtil.getIntValue(map,"num");
	 }
	 catch (Exception e)
	 {
		 logger.error("queryHTMegabytes error ,msgs:"+e.getMessage());
		 return 0;
	 }
  }

  /**
   * 从数据库中查询并判断当前设备是否千/百兆光猫
   * @param devSN
   * @return
   */
  public int queryHTMegabyteInfo(String devSN){
	  
	  if(StringUtil.IsEmpty(devSN)){
		  logger.error("userName is null");
	  }
	  
	  String querySql = "select a.GBBROADBAND from tab_device_version_attribute a, tab_gw_device b where "
			  +" a.devicetype_id = b.devicetype_id and b.device_serialnumber='"+devSN+"'";
	  
	  PrepareSQL psql = new PrepareSQL(querySql);
	  try
	 {
		  Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		  return StringUtil.getIntValue(map,"gbbroadband");
	 }
	 catch (Exception e)
	 {
		 logger.error("queryHTMegabyteInfo error ,msgs:"+e.getMessage());
		 return 0;
	 }
 }
  public String isNotSuportFastGather(String deviceId)
  {
  	String querySql = "select m.device_model from gw_device_model m, tab_gw_device d ,tab_devicetype_info t where d.device_id='"+deviceId+"' and d.devicetype_id = t.devicetype_id and t.device_model_id=m.device_model_id";
  	PrepareSQL psql = new PrepareSQL(querySql);
  	  try
  	 {
  		 Map<String, String> map = DBOperation.getRecord(psql.getSQL());
  		 return StringUtil.getStringValue(map,"device_model");
  	 }
  	 catch (Exception e)
  	 {
  		 logger.error("querySnByUserInfo error ,msgs:"+e.getMessage());
  		 return "";
  	 }
  }

	/**
	 * 根据设备类型id 查询设备是否有千兆口
	 * @param deviceTypeId
	 * @return
	 */
	public int queryGigabitPort(String deviceTypeId){

		if(StringUtil.IsEmpty(deviceTypeId)){
			logger.error("queryGigabitPort with deviceTypeId is null");
		}

		String querySql = "select GIGABIT_PORT from tab_device_version_attribute where devicetype_id=" + StringUtil.getIntegerValue(deviceTypeId);

		PrepareSQL psql = new PrepareSQL(querySql);
		try
		{
			Map<String, String> map = DBOperation.getRecord(psql.getSQL());
			logger.warn("queryGigabitPort with result:{}",StringUtil.getIntValue(map,"gigabit_port"));
			return StringUtil.getIntValue(map,"gigabit_port");
		}
		catch (Exception e)
		{
			logger.error("queryGigabitPort error ,msgs:"+e.getMessage());
			return 0;
		}
	}
	
	
	public Map<String, String> queryUserInf(int userType, String username) {
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		String table_device = "tab_gw_device";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}
		String devSubSn = "";
		// 查询用户信息
		String strSQL = "select t.device_id,\n" +
				"       t.user_id,\n" +
				"       t.username as logic_id,\n" +
				"       t1.oui,\n" +
				"       t1.device_serialnumber,\n" +
				"       t.spec_id,\n" +
				"       t.binddate\n" +
				"  from tab_hgwcustomer t left join tab_gw_device t1 on t1.device_id = t.device_id where ";
		switch (userType) {
			//逻辑ID
			case 0:
				strSQL += " username = '" + username + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.spec_id from " + table_customer + " a," + table_serv_info + " b where a.user_id = b.user_id and b.serv_type_id = 10 and b.username='" + username + "'";
				break;
			// 电话号码
			case 2:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.spec_id from " + table_customer + " a," + table_voip + " b where a.user_id = b.user_id and b.voip_username='" + username + "'";
				break;
			// Device ID
			case 3:
				String oui = username.split("-")[0];
				String sn = username.split("-")[1];



				strSQL += " t1.device_serialnumber ='" + sn + "' and t1.oui='" + oui + "' ";
				if(sn.length()>6){
					devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL += " and t1.dev_sub_sn = '"+devSubSn+"'";
				}

				break;
			// IP地址
			case 4:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,b.device_serialnumber,a.spec_id from " + table_customer + " a, tab_gw_device b where a.device_id = b.device_id and b.loopback_ip='" + username + "'";
				break;
			// SN
			case 6:
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
				}
				strSQL += " t1.device_serialnumber like'%" + username + "' ";
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
					strSQL +=" and t1.dev_sub_sn = '"+devSubSn+"'";
				}

				break;
			default:
				return null;
		}
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());

		Map<String, String> userInfoMap = new HashMap<String, String>();
		if (null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)) {
			userInfoMap.put("loid", userIdList.get(0).get("logic_id"));
			userInfoMap.put("device_serialnumber", userIdList.get(0).get("device_serialnumber"));
			userInfoMap.put("oui", userIdList.get(0).get("oui"));
			userInfoMap.put("spec_id", userIdList.get(0).get("spec_id"));
			userInfoMap.put("user_id", userIdList.get(0).get("user_id"));
			userInfoMap.put("device_id", userIdList.get(0).get("device_id"));
			userInfoMap.put("binddate", userIdList.get(0).get("binddate"));
			String userId = userIdList.get(0).get("user_id");

			// 查询业务信息
			String servSQL = "select a.username as pppusename,a.wan_type,a.serv_type_id,a.passwd,a.open_status,a.bind_port,b.voip_username,b.voip_port,b.parm_stat" +
					" from " + table_serv_info + " a left join " + table_voip + "" +
					" b on a.user_id=b.user_id where a.serv_status=1 and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());
			if (null != userServList && !userServList.isEmpty() && null != userServList.get(0)) {
				String netName = "";
				String netPassword = "";
				String netStatus = "";
				String netPort = "";
				String wanType = "";

				String iptvName = "";
				String iptvStatus = "";
				String iptvPort = "";

				String voipName = "";
				String voipPort = "";
				String voipStatus = "";
				for (HashMap<String, String> userinfoMap : userServList) {
					// 宽带
					if ("10".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(netName)) {
							netName = StringUtil.getStringValue(userinfoMap, "pppusename");
							netPassword = StringUtil.getStringValue(userinfoMap, "passwd");
							netStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							netPort = StringUtil.getStringValue(userinfoMap, "bind_port");
							wanType = StringUtil.getStringValue(userinfoMap, "wan_type");
						}
					}

					// IPTV
					if ("11".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(iptvName)) {
							iptvName = StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = StringUtil.getStringValue(userinfoMap, "bind_port");
						} else {
							iptvName = iptvName + "|" + StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = iptvStatus + "|" + StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = iptvPort + StringUtil.getStringValue(userinfoMap, "bind_port");
						}
					}

					// 语音
					if ("14".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(voipName)) {
							voipName = StringUtil.getStringValue(userinfoMap, "voip_username");
							voipPort = StringUtil.getStringValue(userinfoMap, "voip_port");
							voipStatus = StringUtil.getStringValue(userinfoMap, "parm_stat");
						} else {
							voipName = voipName + "|" + StringUtil.getStringValue(userinfoMap, "voip_username");
							voipPort = voipPort + "|" + StringUtil.getStringValue(userinfoMap, "voip_port");
							voipStatus = voipStatus + "|" + StringUtil.getStringValue(userinfoMap, "parm_stat");
						}
					}
				}
				userInfoMap.put("ppp_usename", netName);
				userInfoMap.put("ppp_password", netPassword);
				userInfoMap.put("net_status", netStatus);
				userInfoMap.put("net_port", netPort);
				userInfoMap.put("wan_type", wanType);

				userInfoMap.put("iptv_name", iptvName);
				userInfoMap.put("iptv_status", iptvStatus);
				userInfoMap.put("iptv_port", iptvPort);

				userInfoMap.put("auth_username", voipName);
				userInfoMap.put("voip_port", voipPort);
				userInfoMap.put("voip_status", voipStatus);
			}

			String deviceSql  = "select * from tab_gw_device where device_id='"+StringUtil.getStringValue(userInfoMap, "device_id")+"'";
			if (3 == DBUtil.GetDB()) {
				deviceSql  = "select loopback_ip from tab_gw_device where device_id='"+StringUtil.getStringValue(userInfoMap, "device_id")+"'";
			}
			ArrayList<HashMap<String, String>> deviceList = DBOperation.getRecords(new PrepareSQL(deviceSql).getSQL());
			if (null != deviceList && !deviceList.isEmpty() && null != deviceList.get(0)) {
				userInfoMap.put("loopback_ip", StringUtil.getStringValue(deviceList.get(0), "loopback_ip"));
			}else{
				userInfoMap.put("loopback_ip","");
			}
		}

		return userInfoMap;
	}


	public Map<String, String> queryUserInfoByLasttime(int userType, String username) {
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		String table_device = "tab_gw_device";
		String table_device_status = "gw_devicestatus ";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
			table_device_status = "stb_gw_devicestatus  ";
		}
		String devSubSn = "";
		// 查询用户信息
		String strSQL = "select t.device_id,\n" +
				"       t.user_id,\n" +
				"       t.username as logic_id,\n" +
				"       t1.oui,\n" +
				"       t1.device_serialnumber,\n" +
				"       t.spec_id,\n" +
				"       t.binddate\n" +
				"  from tab_hgwcustomer t left join tab_gw_device t1 on t1.device_id = t.device_id where ";
		switch (userType) {
			//逻辑ID
			case 0:
				strSQL += " username = '" + username + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.spec_id,a.city_id from " + table_customer + " a  left join "+table_device_status+" c on a.device_id = c.device_id ," + table_serv_info + " b where a.user_id = b.user_id and b.serv_type_id = 10 and b.username='" + username + "' order by c.last_time desc";
				break;
			// 电话号码
			case 2:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.spec_id from " + table_customer + " a," + table_voip + " b where a.user_id = b.user_id and b.voip_username='" + username + "'";
				break;
			// Device ID
			case 3:
				String oui = username.split("-")[0];
				String sn = username.split("-")[1];



				strSQL += " t1.device_serialnumber ='" + sn + "' and t1.oui='" + oui + "' ";
				if(sn.length()>6){
					devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL += " and t1.dev_sub_sn = '"+devSubSn+"'";
				}

				break;
			// IP地址
			case 4:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id,b.device_serialnumber,a.spec_id from " + table_customer + " a, tab_gw_device b where a.device_id = b.device_id and b.loopback_ip='" + username + "'";
				break;
			// SN
			case 6:
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
				}
				strSQL += " t1.device_serialnumber like'%" + username + "' ";
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
					strSQL +=" and t1.dev_sub_sn = '"+devSubSn+"'";
				}

				break;
			default:
				return null;
		}
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());

		Map<String, String> userInfoMap = new HashMap<String, String>();
		if (null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)) {
			userInfoMap.put("loid", userIdList.get(0).get("logic_id"));
			userInfoMap.put("device_serialnumber", userIdList.get(0).get("device_serialnumber"));
			userInfoMap.put("oui", userIdList.get(0).get("oui"));
			userInfoMap.put("spec_id", userIdList.get(0).get("spec_id"));
			userInfoMap.put("user_id", userIdList.get(0).get("user_id"));
			userInfoMap.put("device_id", userIdList.get(0).get("device_id"));
			userInfoMap.put("binddate", userIdList.get(0).get("binddate"));
			userInfoMap.put("city_id",  userIdList.get(0).get("city_id"));
			String userId = userIdList.get(0).get("user_id");

			// 查询业务信息
			String servSQL = "select a.username as pppusename,a.wan_type,a.serv_type_id,a.passwd,a.open_status,a.bind_port,b.voip_username,b.voip_port,b.parm_stat" +
					" from " + table_serv_info + " a left join " + table_voip + "" +
					" b on a.user_id=b.user_id where a.serv_status=1 and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());
			if (null != userServList && !userServList.isEmpty() && null != userServList.get(0)) {
				String netName = "";
				String netPassword = "";
				String netStatus = "";
				String netPort = "";
				String wanType = "";

				String iptvName = "";
				String iptvStatus = "";
				String iptvPort = "";

				String voipName = "";
				String voipPort = "";
				String voipStatus = "";
				for (HashMap<String, String> userinfoMap : userServList) {
					// 宽带
					if ("10".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(netName)) {
							netName = StringUtil.getStringValue(userinfoMap, "pppusename");
							netPassword = StringUtil.getStringValue(userinfoMap, "passwd");
							netStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							netPort = StringUtil.getStringValue(userinfoMap, "bind_port");
							wanType = StringUtil.getStringValue(userinfoMap, "wan_type");
						}
					}

					// IPTV
					if ("11".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(iptvName)) {
							iptvName = StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = StringUtil.getStringValue(userinfoMap, "bind_port");
						} else {
							iptvName = iptvName + "|" + StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = iptvStatus + "|" + StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = iptvPort + StringUtil.getStringValue(userinfoMap, "bind_port");
						}
					}

					// 语音
					if ("14".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.IsEmpty(voipName)) {
							voipName = StringUtil.getStringValue(userinfoMap, "voip_username");
							voipPort = StringUtil.getStringValue(userinfoMap, "voip_port");
							voipStatus = StringUtil.getStringValue(userinfoMap, "parm_stat");
						} else {
							voipName = voipName + "|" + StringUtil.getStringValue(userinfoMap, "voip_username");
							voipPort = voipPort + "|" + StringUtil.getStringValue(userinfoMap, "voip_port");
							voipStatus = voipStatus + "|" + StringUtil.getStringValue(userinfoMap, "parm_stat");
						}
					}
				}
				userInfoMap.put("ppp_usename", netName);
				userInfoMap.put("ppp_password", netPassword);
				userInfoMap.put("net_status", netStatus);
				userInfoMap.put("net_port", netPort);
				userInfoMap.put("wan_type", wanType);

				userInfoMap.put("iptv_name", iptvName);
				userInfoMap.put("iptv_status", iptvStatus);
				userInfoMap.put("iptv_port", iptvPort);

				userInfoMap.put("auth_username", voipName);
				userInfoMap.put("voip_port", voipPort);
				userInfoMap.put("voip_status", voipStatus);

			}

			String deviceSql  = "select * from tab_gw_device where device_id='"+StringUtil.getStringValue(userInfoMap, "device_id")+"'";
			ArrayList<HashMap<String, String>> deviceList = DBOperation.getRecords(new PrepareSQL(deviceSql).getSQL());
			if (null != deviceList && !deviceList.isEmpty() && null != deviceList.get(0)) {
				userInfoMap.put("loopback_ip", StringUtil.getStringValue(deviceList.get(0), "loopback_ip"));
			}else{
				userInfoMap.put("loopback_ip","");
			}
		}

		return userInfoMap;
	}
	
	public ArrayList<HashMap<String, String>> qryIdbyOuiSN(String devSn,String type) {
		logger.debug("qryServByDevSN({})",new Object[]{devSn});

		if (StringUtil.IsEmpty(devSn) || devSn.length()<6) {
			logger.error("oui_sn is Empty or length less than 6.");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		
		if (3 == DBUtil.GetDB()) 
		{
			sb.append("select a.device_id,b.vendor_add,c.device_model,d.softwareversion from tab_gw_device a,tab_vendor b,gw_device_model c," +
					"tab_devicetype_info d where 1= 1  and a.device_status=1 and a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id and a.devicetype_id=d.devicetype_id  and a.device_name='")
					.append(devSn).append("' and a.gw_type = 1 order by a.complete_time");
		} 
		else 
		{
			sb.append("select a.*,b.vendor_add,c.device_model,d.softwareversion from tab_gw_device a,tab_vendor b,gw_device_model c," +
					"tab_devicetype_info d where 1= 1  and a.device_status=1 and a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id and a.devicetype_id=d.devicetype_id  and a.device_name='")
					.append(devSn).append("' and a.gw_type = 1 order by a.complete_time");
		}
		
		//机顶盒
		if("STB".equals(type)){
			sb = new StringBuffer();
			if (3 == DBUtil.GetDB()) 
			{
				sb.append("select a.device_id,b.addressing_type,a.serv_account,b.cust_account,b.pppoe_user from stb_tab_gw_device a left join stb_tab_customer b on a.customer_id=b.customer_id where a.dev_sub_sn ='")
				.append(devSn.substring(devSn.length()-6)).append("' and a.device_serialnumber ='").append(devSn.substring(devSn.indexOf("-")+1)).append("'");
			} 
			else 
			{
				sb.append("select a.*,b.addressing_type,a.serv_account,b.cust_account,b.pppoe_user from stb_tab_gw_device a left join stb_tab_customer b on a.customer_id=b.customer_id where a.dev_sub_sn ='")
				.append(devSn.substring(devSn.length()-6)).append("' and a.device_serialnumber ='").append(devSn.substring(devSn.indexOf("-")+1)).append("'");
			}
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
	
	public ArrayList<HashMap<String, String>> qryIdbyOuiSN(String devSn) {
		logger.debug("qryServByDevSN({})",new Object[]{devSn});

		if (StringUtil.IsEmpty(devSn) || devSn.length()<6) {
			logger.error("oui_sn is Empty or length less than 6.");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		if (3 == DBUtil.GetDB()) {
			sb.append("select a.device_id,b.vendor_add,c.device_model,d.softwareversion from tab_gw_device a,tab_vendor b,gw_device_model c," +
					"tab_devicetype_info d where 1= 1  and a.device_status=1 and a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id and a.devicetype_id=d.devicetype_id  and a.device_name='")
					.append(devSn).append("' a.gw_type = 1 order by a.complete_time");
		}
		else {
			sb.append("select a.*,b.vendor_add,c.device_model,d.softwareversion from tab_gw_device a,tab_vendor b,gw_device_model c," +
					"tab_devicetype_info d where 1= 1  and a.device_status=1 and a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id and a.devicetype_id=d.devicetype_id  and a.device_name='")
					.append(devSn).append("' a.gw_type = 1 order by a.complete_time");
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		list = DBOperation.getRecords(psql.getSQL());
		return list;
	}
	
	public ArrayList<HashMap<String, String>> qryLindidByDeviceID(String deviceId) {


		StringBuffer sb = new StringBuffer();
		sb.append("select v.line_id from tab_hgwcustomer a,tab_gw_device d,tab_voip_serv_param v where a.user_id=v.user_id and a.device_id=d.device_id and d.device_id='" +
				deviceId + "'");
		
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		list = DBOperation.getRecords(psql.getSQL());
		return list;
	}

	public Map queryDevType(String deviceSN) {
		PrepareSQL psql =  new PrepareSQL();
		psql.append(" select a.is_normal,a.access_style_relay_id,b.device_version_type from tab_devicetype_info a,tab_device_version_attribute b,tab_gw_device c " +
				" where a.devicetype_id = b.devicetype_id and c.devicetype_id = b.devicetype_id and  c.device_serialnumber= ? ");
		psql.setString(1,deviceSN);
		Map result = new HashMap();
		result = DBOperation.getRecord(psql.getSQL());
		return result;
	}

	public void insertSchoolDevLog(OpenFristChecker checker) {
		StringBuffer strSql = new StringBuffer();
		strSql.append("INSERT INTO tab_service_school (cmdID, userInfoType, UserInfo, SDNenable, controllerAddress ");
		strSql.append("	, backupEnable, backupControllerAddress, intime, resultCode, resultDesc) ");
		strSql.append(" VALUES (?, ?, ?, ?, ? ");
		strSql.append(" , ?, ?, ?, ?, ?) ");
		PrepareSQL psql = new PrepareSQL(strSql.toString());
		psql.setString(1,StringUtil.getStringValue(checker.getCmdId()));
		psql.setString(2,StringUtil.getStringValue(checker.getUserInfoType()));
		psql.setString(3,StringUtil.getStringValue(checker.getUserInfo()));
		psql.setString(4,StringUtil.getStringValue(checker.getEnable()));
		psql.setString(5,StringUtil.getStringValue(checker.getControllerAddress()));
		psql.setString(6,StringUtil.getStringValue(checker.getBackupEnable()));
		psql.setString(7,StringUtil.getStringValue(checker.getBackupControllerAddress()));
		psql.setString(8,StringUtil.getStringValue(DateUtil.currentTimeInSecond()));
		psql.setString(9,StringUtil.getStringValue(checker.getResult()));
		psql.setString(10,StringUtil.getStringValue(checker.getResultDesc()));

		try {
			DBOperation.getRecords(psql.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("校园宽带接口结果入库失败");
		}

	}


	/**
	 * 仿真账号查询
	 * @param userSpeed
	 * @param city_id
	 * @return
	 */
	public static Map getEuUser(String userSpeed, String city_id)
	{
		Map map = new HashMap();
		PrepareSQL psql = new PrepareSQL("select net_account,net_password from tab_speed_net where test_rate= ? and city_id= ? ");
		psql.setInt(1,(Integer.parseInt(userSpeed)*1024));
		psql.setString(2,city_id);
		try {
			map =  DBOperation.getRecord(psql.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
		}
 		return map;
	}
	
	
	
	public  Map<String, String> queryDeviceSNInfo(String deviceId)
	{
		Map<String, String> map = new HashMap<String, String>();
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("b.cpe_mac,");
		sql.append("e.vendor_add,");
		sql.append("f.device_model,");
		sql.append("d.device_version_type,");
		sql.append("c.wan_type ");
		sql.append("from tab_hgwcustomer a ");
		sql.append("inner join tab_gw_device b on a.device_id=b.device_id ");
		sql.append("inner join hgwcust_serv_info c on c.user_id=a.user_id and c.serv_type_id=10 ");
		sql.append("left join tab_device_version_attribute d on b.devicetype_id=d.devicetype_id ");
		sql.append("inner join tab_vendor e on e.vendor_id=b.vendor_id ");
		sql.append("inner join gw_device_model f on f.device_model_id=b.device_model_id ");
		sql.append("where b.device_id=? ");
		
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.setString(1,deviceId);
		try {
			map = DBOperation.getRecord(psql.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
		}
 		return map;
	}

	public HashMap<String, String> getNetInfo(long userId)
	{
		PrepareSQL prepareSql = new PrepareSQL();
		prepareSql.append("select username as netaccount,wan_type from hgwcust_serv_info where user_id = ? and serv_type_id = 10 ");
		prepareSql.setLong(1, userId);
		return (HashMap)DBOperation.getRecord(prepareSql.getSQL());
	}
}
