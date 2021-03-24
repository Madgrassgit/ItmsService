package com.linkage.itms.dispatch.cqdx.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;

public class PublicDAO {

	private static Logger logger = LoggerFactory.getLogger(PublicDAO.class);

	/**
	 * 根据用户的业务账号查询用户信息
	 * @param userType 用户信息类型
	 * @param username 业务号码
	 * @return
	 */
	public Map<String, String> queryUserInfo(int userType, String username) {
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
		
		// 查询用户信息
		String strSQL = "select device_id,user_id,username as logic_id,device_serialnumber,customer_id,spec_id from " + table_customer + " where ";
		switch (userType) {
		// 用户宽带帐号
		case 1:
			strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.customer_id,a.spec_id from " + table_customer + " a," + table_serv_info + " b where a.user_id = b.user_id and b.serv_type_id = 10 and b.username='" + username + "'";
			break;
		// loid
		case 2:
			strSQL += " username = '" + username + "'";
			break;
		// deviceId
		case 3:
			strSQL += " device_serialnumber like'%" + username + "'";
			break;
		// voip账号
		case 5:
			strSQL = "select a.device_id,a.user_id,a.username as logic_id,a.device_serialnumber,a.customer_id,a.spec_id from " + table_customer + " a," + table_voip + " b where a.user_id = b.user_id and b.voip_username='" + username + "'";
			break;
		// 客户号
		case 7:
			strSQL += " customer_id = '" + username + "'";
			break;
		// IP地址
		case 8:
			strSQL += "select a.device_id,a.user_id,a.username as logic_id,b.device_serialnumber,a.customer_id,a.spec_id from " + table_customer + " a, tab_gw_device b where a.device_id = b.device_id and b.loopback_ip='" + username + "'";
			break;
		default:
			return null;
		}
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		
		Map<String,String> userInfoMap = new HashMap<String, String>();
		if(null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)){
			userInfoMap.put("loid", userIdList.get(0).get("logic_id"));
			userInfoMap.put("device_serialnumber", userIdList.get(0).get("device_serialnumber"));
			userInfoMap.put("customer_id", userIdList.get(0).get("customer_id"));
			userInfoMap.put("spec_id", userIdList.get(0).get("spec_id"));
			userInfoMap.put("device_id", userIdList.get(0).get("device_id"));
			
			
			String userId = userIdList.get(0).get("user_id");
			
			// 查询业务信息
			String servSQL = "select a.username as pppusename,a.serv_type_id,a.passwd,a.open_status,a.bind_port,b.voip_username,b.voip_port,b.parm_stat" +
					" from " + table_serv_info + " a left join " + table_voip + "" +
					" b on a.user_id=b.user_id where a.serv_status=1 and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());
			if(null != userServList && !userServList.isEmpty() && null != userServList.get(0)){
				String netName = "";
				String netPassword = "";
				String netStatus = "";
				String netPort = "";
				
				String iptvName = "";
				String iptvStatus = "";
				String iptvPort = "";
				
				String voipName = "";
				String voipPort = "";
				String voipStatus = "";
				for(HashMap<String, String> userinfoMap : userServList){			
					// 宽带
					if("10".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						if(StringUtil.isEmpty(netName)){
							netName = StringUtil.getStringValue(userinfoMap, "pppusename");
							netPassword = StringUtil.getStringValue(userinfoMap, "passwd");
							netStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							netPort = StringUtil.getStringValue(userinfoMap, "bind_port");
						}
					}
					
					// IPTV
					if("11".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						if(StringUtil.isEmpty(iptvName)){
							iptvName = StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = StringUtil.getStringValue(userinfoMap, "real_bind_port");
						}else{
							iptvName = iptvName + "|" + StringUtil.getStringValue(userinfoMap, "pppusename");
							iptvStatus = iptvStatus + "|" + StringUtil.getStringValue(userinfoMap, "open_status");
							iptvPort = iptvPort + StringUtil.getStringValue(userinfoMap, "real_bind_port");
						}
					}
					
					// 语音
					if("14".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						if(StringUtil.isEmpty(voipName)){
							voipName = StringUtil.getStringValue(userinfoMap, "voip_username");
							voipPort = StringUtil.getStringValue(userinfoMap, "voip_port");
							voipStatus = StringUtil.getStringValue(userinfoMap, "parm_stat");
						}else{
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
				
				userInfoMap.put("iptv_name", iptvName);
				userInfoMap.put("iptv_status", iptvStatus);
				userInfoMap.put("iptv_port", iptvPort);
				
				userInfoMap.put("auth_username", voipName);
				userInfoMap.put("voip_port", voipPort);
				userInfoMap.put("voip_status", voipStatus);
			}
			
		}
		
		return userInfoMap;
	}
		
	public Map<String, String> queryUserInfoLan(int userType, String username) {
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
		psql.append("select a.user_id, a.device_id");
		switch (userType) {
		// 用户宽带帐号
		case 1:
			psql.append(" from " + table_customer + " a," + table_serv_info + " b");
			psql.append(" where a.user_id = b.user_id and b.serv_status = 1 and b.serv_type_id = 10");
			psql.append(" and b.username= '" + username + "'");
			break;
		// loid
		case 2:
			psql.append(" from " + table_customer + " a");
			psql.append(" where a.username = '" + username + "'");
			break;
		// 设备序列号
		case 6:
			psql.append(" from " + table_customer + " a," + table_serv_info + " b, tab_gw_device c");
			psql.append(" where a.user_id = b.user_id and a.device_id = c.device_id ");
			psql.append(" and b.serv_status = 1");
			if(username.trim().length() >= 6){
				psql.append(" and c.dev_sub_sn = '" + username.substring(username.length() - 6, username.length()) + "'");
			}
			psql.append(" and c.device_serialnumber like '%" + username + "'");
			break;
		default:
			return null;
		}
		psql.append(" order by a.updatetime desc");
		return DBOperation.getRecord(psql.getSQL());
	}

//	public Map<String, String> queryUserInfo(String deviceId) {
//		if (StringUtil.IsEmpty(deviceId)) {
//			logger.error("deviceId is Empty");
//			return null;
//		}
//		String table_customer = "tab_hgwcustomer";
//		String table_serv_info = "hgwcust_serv_info";
//		String table_voip = "tab_voip_serv_param";
//		if("BBMS".equals(Global.SYSTEM_NAME)){
//			table_customer = "tab_egwcustomer";
//			table_serv_info = "egwcust_serv_info";
//			table_voip = "tab_egw_voip_serv_param";
//		}
//
//		PrepareSQL psql = new PrepareSQL();
//		psql.append("select a.customer_id, a.user_id,");
//		psql.append(" a.username as loid, b.username as pppUsename,");
//		psql.append(" a.device_serialnumber, c.voip_username, b.passwd");
//		psql.append(" from " + table_customer + " a," + table_serv_info + " b," + table_voip + " c");
//		psql.append(" where a.user_id = b.user_id and b.user_id = c.user_id");
//		psql.append(" and b.serv_status = 1 and b.serv_type_id = 10");
//		psql.append(" and b.device_id= '" + deviceId + "'");
//		psql.append(" order by a.updatetime desc");
//		return DBOperation.getRecord(psql.getSQL());
//	}

//	public String queryDeviceId(String devSn) {
//		if (StringUtil.IsEmpty(devSn)) {
//			logger.error("devsn is Empty");
//			return null;
//		}
//
//		PrepareSQL psql = new PrepareSQL();
//		psql.append("select a.device_id from tab_gw_device a");
//		psql.append(" where a.dev_sub_sn = '" + devSn.substring(devSn.length() - 6) + "'");
//		psql.append(" and a.device_serialnumber like '%" + devSn + "'");
//		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
//		if (map == null || map.isEmpty()) {
//			return null;
//		}
//		
//		return StringUtil.getStringValue(map, "device_id");
//	}
		
	/**
	 * 工单查询
	 * @param userId
	 * @return
	 */
	public List<HashMap<String,String>> getBssSheetServInfo(String loid) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id, b.open_status, b.orderid ");
		psql.append(" from " + table_customer + " a left join " + table_serv_info + " b");
		psql.append(" on a.user_id = b.user_id");
		psql.append(" where a.username = '" + loid + "'");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 用户配置执行情况查询
	 * @param userType 用户信息类型
	 * @param username 业务号码
	 * @return
	 */
	public List<HashMap<String,String>> getBussinessInfo4net(int userType, String username,String servStatus) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id, a.username as loid, b.username as pppUsename, a.adsl_hl, b.open_status,b.serv_status,b.orderid, ");
		psql.append(" b.serv_type_id,b.wan_type, c.device_serialnumber,c.device_id, c.device_type");
		psql.append(" from " + table_customer + " a left join tab_gw_device c on a.device_id = c.device_id,");
		psql.append(" " + table_serv_info + " b");
		psql.append(" where a.user_id = b.user_id");
		if(!StringUtil.isEmpty(servStatus)){
			psql.append(" and b.serv_status = " + servStatus);
		}
		switch (userType) {
		// 宽带
		case 1:
			psql.append(" and b.username = '" + username + "'");
			break;
		// 逻辑id
		case 2:
			psql.append(" and a.username = '" + username + "' and b.serv_type_id='10' ");
			break;
		default:
			return null;
		}
		psql.append(" order by a.updatetime desc");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	
	/**
	 * 用户配置执行情况查询
	 * @param userType 用户信息类型
	 * @param username 业务号码
	 * @return
	 */
	public List<HashMap<String,String>> getBussinessInfo(int userType, String username,String servStatus) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id, a.username as loid, b.username as pppUsename, a.adsl_hl, b.open_status,b.serv_status,b.orderid, ");
		psql.append(" b.serv_type_id, c.device_serialnumber,c.device_id, c.device_type");
		psql.append(" from " + table_customer + " a left join tab_gw_device c on a.device_id = c.device_id,");
		psql.append(" " + table_serv_info + " b");
		psql.append(" where a.user_id = b.user_id");
		if(!StringUtil.isEmpty(servStatus)){
			psql.append(" and b.serv_status = " + servStatus);
		}
		switch (userType) {
		// 宽带
		case 1:
			psql.append(" and b.username = '" + username + "'");
			break;
		// 逻辑id
		case 2:
			psql.append(" and a.username = '" + username + "'");
			break;
		default:
			return null;
		}
		psql.append(" order by a.updatetime desc");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	
	/**
	 * 用户配置执行情况查询
	 * @param userType 用户信息类型
	 * @param username 业务号码
	 * @return
	 */
	public List<HashMap<String,String>> getVoipBussinessInfo(int userType, String username) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String tabVoipName = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			tabVoipName = "tab_egw_voip_serv_param";
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.username as loid, b.username as pppUsename, a.adsl_hl, b.open_status, ");
		psql.append(" b.serv_type_id, c.device_serialnumber, c.device_type,d.voip_username");
		psql.append(" from " + table_customer + " a left join tab_gw_device c on a.device_id = c.device_id,");
		psql.append(" " + table_serv_info + " b");
		psql.append(" left join " + tabVoipName + " d on b.user_id = d.user_id");
		psql.append(" where a.user_id = b.user_id and b.serv_status = 1 and b.serv_type_id=14");
		switch (userType) {
		// 宽带
		case 1:
			psql.append(" and b.username = '" + username + "'");
			break;
		// 逻辑id
		case 2:
			psql.append(" and a.username = '" + username + "'");
			break;
		default:
			return null;
		}
		psql.append(" order by a.updatetime desc");
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 查询设备信息
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getDeviceInfo(String deviceId){
		
		String strSQL = "select a.city_id,a.device_status,c.vendor_name,d.device_model,b.hardwareversion," +
				" a.complete_time,b.access_style_relay_id,a.loopback_ip,a.cpe_mac,b.spec_id,a.oui " +
				" from tab_gw_device a " +
				" left join tab_devicetype_info b on a.devicetype_id = b.devicetype_id " +
				" left join tab_vendor c on a.vendor_id = c.vendor_id " +
				" left join gw_device_model d on a.device_model_id = d.device_model_id " +
				" where a.device_id = '" + deviceId + "'";
		
		return DBOperation.getRecords(strSQL);
	}
	
	/**
	 * 查询设备规格信息
	 * @param specId
	 * @return
	 */
	public List<HashMap<String, String>> getTabBssDevPortInfo(String specId){
		String strSQL = "select * from tab_bss_dev_port where id=" + StringUtil.getStringValue(specId);
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select lan_num, wlan_num from tab_bss_dev_port where id=" + StringUtil.getStringValue(specId);
		}
		return DBOperation.getRecords(strSQL);
	}
	
	/**
	 * 获取user_id
	 * @param userType
	 * @param username
	 * @return
	 */
	public List<HashMap<String,String>> queryUserId(int userType, String username) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		
		String strSQL = "";
		if(1 == userType){
			strSQL = "select user_id from " + table_serv_info + " where username= '" + username + "'";
		}
		if(2 == userType){
			strSQL = "select user_id from " + table_customer + " where username= '" + username + "'";
		}
		
		if(!StringUtil.isEmpty(strSQL)){
			return DBOperation.getRecords(strSQL);
		}
		return null;
	}
	
	/**
	 * 更新用户表
	 * @param userId
	 * @param userAddress
	 * @param areaCode
	 * @return
	 */
	public int updateCustomerInfo(long userId,String userAddress,String areaCode){
		String table_customer = "tab_hgwcustomer";;
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
		}
		PrepareSQL psql = new PrepareSQL("update " + table_customer + " set");
		if(!StringUtil.isEmpty(areaCode)){
			psql.append("  city_id='" + areaCode + "'");
		}
		if(!StringUtil.isEmpty(userAddress)){
			if(!StringUtil.isEmpty(areaCode)){
				psql.append(",");
			}
			psql.append("linkaddress='" + userAddress + "'");
		}
		psql.append(",updatetime=" + System.currentTimeMillis()/1000);
		psql.append(" where user_id=" + userId);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 更新业务信息
	 * @param userId
	 * @param vlanId
	 * @return
	 */
	public int updateHgwcustInfo(long userId,String vlanId){
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		String strSQL = "update " + table_serv_info + " set vlanid =?,updatetime=? where user_id=? and serv_type_id = 10 ";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, vlanId);
		psql.setLong(2, System.currentTimeMillis()/1000);
		psql.setLong(3, userId);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 路由改桥更新业务信息
	 * @param userId
	 * @param wanType
	 * @param passwd
	 * @return
	 */
	public int updateHgwcustRgModleInfo(long userId,String wanType,String passwd,int serv_type_id){
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}
		String strSQL = "update " + table_serv_info + " set wan_type= ?";
		if(!StringUtil.isEmpty(passwd)){
			strSQL += ",passwd='" + passwd + "'";
		}
		
		strSQL += ",updatetime=? where user_id=? and serv_type_id = ? ";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, wanType);
		psql.setLong(2, System.currentTimeMillis()/1000);
		psql.setLong(3, userId);
		psql.setInt(4, serv_type_id);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 根据用户的宽带账号或者loid或者设备序列号查询用户信息
	 * 
	 * @param userType:用户信息类型
	 *            username:用户信息
	 * @author liyl10 
	 * @date 2017-11-26
	 * @return Map<String,String>
	 */
	public Map<String, String> qryUserDevice(String username, String netAccount, String deviceSerialnumber, String customer_id) {
		logger.warn("qryUserDevice({},{},{},{})",new Object[]{username, netAccount, deviceSerialnumber, customer_id});

		if (StringUtil.IsEmpty(username) && StringUtil.IsEmpty(netAccount) && StringUtil.IsEmpty(deviceSerialnumber) && StringUtil.IsEmpty(customer_id)) {
			logger.error("username || netAccount || deviceSerialnumber || customer_id can't both empty");
			return null;
		}
		
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";

		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,a.username,b.username as netAccount,c.device_id,c.oui,c.device_serialnumber,a.city_id,b.serv_type_id,c.x_com_passwd");
		psql.append(" from tab_gw_device c, " + table_customer + " a, " + table_serv_info + " b");
		psql.append(" where c.device_id = a.device_id and a.user_id=b.user_id and b.serv_type_id = 10 ");
		if(!StringUtil.isEmpty(username)  && !"null".equalsIgnoreCase(username)){
			psql.append(" and a.username='" + username + "' ");
		}
		if(!StringUtil.isEmpty(netAccount)  && !"null".equalsIgnoreCase(netAccount)){
			psql.append(" and b.username='" + netAccount + "' ");
		}
		if(!StringUtil.isEmpty(deviceSerialnumber) && !"null".equalsIgnoreCase(deviceSerialnumber)){
			if(deviceSerialnumber.length() >= 6){
				String dev_sub_sn = deviceSerialnumber;
				dev_sub_sn = deviceSerialnumber.substring(deviceSerialnumber.length()-6, deviceSerialnumber.length());
				psql.append(" and c.dev_sub_sn = '" + dev_sub_sn + "' ");
			}
			psql.append(" and c.device_serialnumber='" + deviceSerialnumber + "' ");
		}
		if(!StringUtil.isEmpty(customer_id)  && !"null".equalsIgnoreCase(customer_id)){
			psql.append(" and a.customer_id='" + customer_id + "' ");
		}
		psql.append(" order by a.updatetime desc");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 查询设备超级密码
	 * @param userInfo
	 * @param userType
	 * @return
	 */
	public List<HashMap<String, String>> queryDeviceInfo(String userInfo,int userType){
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		// 查询设备信息
		String strSQL = "select c.x_com_passwd,a.username as loid,b.username as pppoe from " + table_customer + " a left join " + table_serv_info + " b on a.user_id = b.user_id, tab_gw_device c where a.device_id = c.device_id ";
		PrepareSQL psql = new PrepareSQL(strSQL);
		if(1 == userType){
			psql.append(" and b.username='" + userInfo + "'");
		}else if(2 == userType){
			psql.append(" and a.username='" + userInfo + "'");
		}else{
			return null;
		}
		return  DBOperation.getRecords(psql.getSQL());		
	}
	
	/**
	 * 根据设备ID查询设备信息
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String,String>> queryDeviceInfoByDeviceId(String deviceId){
		
		if(StringUtil.isEmpty(deviceId)){
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL("select * from tab_gw_device where device_id = ?");
		// mysql db
		if (3 == DBUtil.GetDB()) {
			psql = new PrepareSQL("select loopback_ip from tab_gw_device where device_id = ?");
		}
		psql.setString(1, deviceId);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 记录工厂服务接口操作信息
	 * @param opId
	 * @param result
	 * @param message
	 * @param deviceId
	 * @param username
	 * @return
	 */
	public int recordFactoryResetReturnDiag(String opId,String result,String message,String deviceId,String username){
		
		PrepareSQL psql = new PrepareSQL("insert into tab_factory_reset_return_diag(op_id,username,device_id,status," +
				"err_msg,record_time,update_time) values(?,?,?,?,?,?,?)");
		psql.setString(1,opId);
		psql.setString(2, username);
		psql.setString(3, deviceId);
		psql.setInt(4, 0);
		psql.setString(5, message);
		psql.setLong(6, System.currentTimeMillis()/1000);
		psql.setLong(7, System.currentTimeMillis()/1000);
		return DBOperation.executeUpdate(psql.getSQL());
	}
}
