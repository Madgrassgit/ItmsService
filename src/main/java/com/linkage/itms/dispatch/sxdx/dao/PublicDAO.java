package com.linkage.itms.dispatch.sxdx.dao;

import com.linkage.commons.db.DBAdapter;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DbUtils;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dispatch.sxdx.beanObj.Para;
import com.linkage.itms.dispatch.sxdx.beanObj.UserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicDAO {

	private static Logger logger = LoggerFactory.getLogger(PublicDAO.class);

	public static long MAX_UNUSED_DEVICEID = -1L;
	
	public static long MIN_UNUSED_DEVICEID = -1L;
	
	public static int SUM_UNUSED_DEVICEID = 50;
	
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	synchronized public long GetUnusedDeviceSerial(int count) {
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
	public long GetUnusedDeviceSerialOld(int count) {
		logger.debug("GetUnusedDeviceSerial({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		if( MIN_UNUSED_DEVICEID < 0 ){
						
			if (DBUtil.GetDB() == 1) {// oracle
				MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(SUM_UNUSED_DEVICEID) - 1;
			} else if (DBUtil.GetDB() == 2) {// sybase
				MIN_UNUSED_DEVICEID = getMaxDeviceId4Sybase(SUM_UNUSED_DEVICEID) - 1;
			}
			MAX_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + SUM_UNUSED_DEVICEID;
		}
		
		if( MAX_UNUSED_DEVICEID < (MIN_UNUSED_DEVICEID + count)){
			
			if(SUM_UNUSED_DEVICEID < count){

				if (DBUtil.GetDB() == 1) {// oracle
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(count) - 1;
				} else if (DBUtil.GetDB() == 2) {// sybase
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Sybase(count) - 1;
				}
				MAX_UNUSED_DEVICEID = MIN_UNUSED_DEVICEID + count;
			} else {
				
				if (DBUtil.GetDB() == 1) {// oracle
					MIN_UNUSED_DEVICEID = getMaxDeviceId4Oracle(SUM_UNUSED_DEVICEID) - 1;
				} else if (DBUtil.GetDB() == 2) {// sybase
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
	public static long getMaxDeviceId4Oracle(int count) {
		logger.debug("getMaxDeviceId4Oracle({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		CallableStatement cstmt = null;
		Connection conn = null;
		String sql = "{call maxTR069DeviceIdProc(?,?)}";

		try {
			conn = DBAdapter.getJDBCConnection();
			cstmt = conn.prepareCall(sql);
			cstmt.setInt(1, count);
			cstmt.registerOutParameter(2, Types.INTEGER);
			cstmt.execute();
			serial = cstmt.getLong(2);
		} catch (Exception e) {
			logger.error("GetUnusedDeviceSerial Exception:{}", e.getMessage());
		} finally {
			sql = null;

			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					logger.error("cstmt.close SQLException:{}", e.getMessage());
				}
				cstmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
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
	public static long getMaxDeviceId4Sybase(int count) {
		logger.debug("getMaxDeviceId4Sybase({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		String sql = "maxTR069DeviceIdProc ?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, count);

		return DBOperation.executeProcSelect(pSQL.getSQL());
	}

	
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


	/**
	 *
	 * 根据1设备唯一标识/2用户名称/3宽带账号查询用户、设备、业务信息
	 * @param type
	 * @param value
	 * @return
	 */
	public UserDetail queryUserDetail(int type, String value) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}
		
		// 查询用户信息
		String strSQL = "select a.oui,a.device_serialnumber sn,a.device_id,a.username as logic_id,a.user_id,d.devicetype_id,d.complete_time,t.softwareversion,d.cpe_allocatedstatus,s.last_time,s.online_status,c.city_name from " +
						table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join tab_city c on(c.city_id=d.city_id)" +
				" left join tab_devicetype_info t on (d.devicetype_id=t.devicetype_id) left join gw_devicestatus s on (d.device_id=s.device_id) where ";
		switch (type) {
		// 用户宽带帐号
		case 3:
			strSQL = "select a.oui,a.device_serialnumber sn,a.device_id,a.username as logic_id,a.user_id,d.devicetype_id,d.complete_time,t.softwareversion,d.cpe_allocatedstatus,s.last_time,s.online_status,c.city_name from " +
						table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id left join tab_gw_device d on a.device_id=d.device_id left join tab_city c on(c.city_id=d.city_id)" +
					"left join tab_devicetype_info t on (d.devicetype_id=t.devicetype_id) left join gw_devicestatus s on (d.device_id=s.device_id) where b.serv_type_id = 10 and b.username='" + value + "'";
			break;
		// 用户名
		case 2:
			strSQL += " a.username = '" + value + "'";
			break;
		// 终端唯一标识
		case 1:
			//select a.username,d.device_id,t.softwareversion from tab_hgwcustomer a left join tab_gw_device d on a.device_id=d.device_id left join tab_devicetype_info t on (d.devicetype_id=t.devicetype_id) where rownum<10
			//唯一终端标识，暂理解为oui-sn
			String oui = value.split("-")[0];
			String sn = value.split("-")[1];
			strSQL += " a.device_serialnumber ='" + sn + "' and a.oui='"+oui+"'";
			break;
		default:
			return null;
		}
		
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		UserDetail userDetail = new UserDetail();
		if(null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)){
			if(StringUtil.isEmpty(userIdList.get(0).get("cpe_allocatedstatus")) && !StringUtil.isEmpty(userIdList.get(0).get("user_id"))){
				userDetail.setCpe_status("入网#"+userIdList.get(0).get("user_id"));
			}
			else{
				userDetail.setCpe_status("注册#"+userIdList.get(0).get("user_id"));
				userDetail.setCpe_id(userIdList.get(0).get("oui")+userIdList.get(0).get("sn"));
				userDetail.setOnline_status("1".equals(userIdList.get(0).get("online_status"))?"在线":"不在线");
				userDetail.setVersion_status("1");//默认都存在
				userDetail.setCpe_version(userIdList.get(0).get("softwareversion"));
				userDetail.setLast_connect(new DateTimeUtil(userIdList.get(0).get("last_time")).getLongDate());
				userDetail.setCpe_domain(userIdList.get(0).get("city_name"));
			}
			
			
			String userId = userIdList.get(0).get("user_id");
			
			// 查询业务信息
			String servSQL = "select a.username as pppusename,a.serv_type_id,a.passwd,a.open_status,a.bind_port,b.voip_username,b.voip_port,b.parm_stat" +
					" from " + table_serv_info + " a left join " + table_voip + "" +
					" b on a.user_id=b.user_id where a.serv_status=1 and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());
			
			StringBuffer cpe_service = new StringBuffer();
			if(null != userServList && !userServList.isEmpty() && null != userServList.get(0)){
				for(HashMap<String, String> userinfoMap : userServList){			
					// 宽带
					if("10".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						userDetail.setAd_account(StringUtil.getStringValue(userinfoMap, "pppusename"));
						cpe_service.append(StringUtil.isEmpty(cpe_service.toString())?"wband":"^wband");
					}
					
					// IPTV
					if("11".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						cpe_service.append(StringUtil.isEmpty(cpe_service.toString())?"iptv":"^iptv");
					}
					
					// 语音
					if("14".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))){
						cpe_service.append(StringUtil.isEmpty(cpe_service.toString())?"voip":"^voip");
					}
				}
			}
			userDetail.setCpe_service(cpe_service.toString());
		}
		return userDetail;
	}
	
	
	/**
	 * @param type 用户信息类型
	 * @param value 业务号码
	 * @return
	 */
	public ArrayList<Para> queryUserDevServByOui_sn(int type, String value) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}
		
		// 查询用户信息
		String strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from " +
						table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id) left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
								" left join gw_device_model m on t.device_model_id = m.device_model_id where ";
		switch (type) {
			case 0:
				strSQL += " a.username ='" + value + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from " +
							table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id left join tab_gw_device d on a.device_id=d.device_id left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
						" left join gw_device_model m on t.device_model_id = m.device_model_id left join gw_devicestatus s on (d.device_id=s.device_id) where b.serv_type_id = 10 and b.username='" + value + "'";
				break;
			//语音号码
			case 2:
				strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from tab_voip_serv_param v," +
						table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id)  left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
								" left join gw_device_model m on t.device_model_id = m.device_model_id where v.user_id=a.user_id ";
				strSQL += "and v.voip_phone ='" + value + "'";
				break;
			// 终端唯一标识
			case 3:
				//select a.username,d.device_id,t.softwareversion from tab_hgwcustomer a left join tab_gw_device d on a.device_id=d.device_id left join tab_devicetype_info t on (d.devicetype_id=t.devicetype_id) where rownum<10
				//唯一终端标识，暂理解为oui-sn
				String oui = value.split("-")[0];
				String sn = value.split("-")[1];
				strSQL += " d.device_serialnumber ='" + sn + "' and d.oui='"+oui+"'";
				if(sn.length()>6){
					String devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			default:
				return null;
		}
		
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		ArrayList<Para> result = new ArrayList<Para>();
		UserDetail userDetail = new UserDetail();
		if(null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)){
			if(StringUtil.isEmpty(userIdList.get(0).get("cpe_allocatedstatus")) && !StringUtil.isEmpty(userIdList.get(0).get("user_id"))){
				result.add(setPara("iDegistState", "2"));
			}
			else{
				result.add(setPara("iDegistState", "1"));
				result.add(setPara("strCPEID", userIdList.get(0).get("oui")+userIdList.get(0).get("sn")));
				result.add(setPara("strCPEIP", userIdList.get(0).get("loopback_ip")));
				result.add(setPara("strLOID", userIdList.get(0).get("loid")));
				result.add(setPara("strMacAddr", userIdList.get(0).get("cpe_mac")));
				result.add(setPara("iOnlineState", "1".equals(userIdList.get(0).get("online_status"))?"1":"2"));
				result.add(setPara("strMtUser", StringUtil.isEmpty(userIdList.get(0).get("x_com_username"))?"-":userIdList.get(0).get("x_com_username")));
				result.add(setPara("strMtPass", StringUtil.isEmpty(userIdList.get(0).get("x_com_passwd"))?"-":userIdList.get(0).get("x_com_passwd")));
			}
			
			
			String userId = userIdList.get(0).get("user_id");
			
			// 查询业务信息
			String servSQL = "select b.protocol, b.voip_username, a.bind_port,b.voip_port,b.parm_stat,b.uri,b.reg_id,b.line_id" +
					" from " + table_serv_info + " a," + table_voip +
					" b where a.user_id=b.user_id and a.serv_status=1 and (a.serv_type_id=14 or a.serv_type_id=15) and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());
			
			StringBuffer cpe_service = new StringBuffer();
			if(null != userServList && !userServList.isEmpty() && null != userServList.get(0)){
				int index = 1;
				String strVoipInfo = "";
				String protocol = "";
				String voip_username = "";
				String uri = "";
				String voip_port = "";
				String DeviceID = "";
				String LinePhysicalTermID = "";
				String protocoltype="";
				for(HashMap<String, String> userinfoMap : userServList){
					protocoltype = StringUtil.getStringValue(userinfoMap, "protocol");
					if(index > 1){
						protocol += "^";
					}
					//sip
					if("1".equals(protocoltype) || "0".equals(protocoltype)){
						voip_username = StringUtil.getStringValue(userinfoMap, "voip_username");
						uri = StringUtil.getStringValue(userinfoMap, "uri");
						voip_port = StringUtil.getStringValue(userinfoMap, "voip_port");
						
						protocol += "Sip_UserName_" + index + "=" + voip_username + "^Sip_LineURI_" + index + "=" + uri +
								"^Sip_Port_" + index + "=" + voip_port;
					}
					else{
						DeviceID = StringUtil.getStringValue(userinfoMap, "reg_id");
						voip_port = StringUtil.getStringValue(userinfoMap, "line_id");
						LinePhysicalTermID = StringUtil.getStringValue(userinfoMap, "voip_port");
						
						protocol += "H248_DeviceID_" + index + "=" + DeviceID + "^H248_LinePhysicalTermID_" + index + "=" + LinePhysicalTermID +
								"^H248_Port_" + index + "=" + voip_port;
					}
					index++;
				}
				result.add(setPara("strVoipInfo", protocol));
			}
			userDetail.setCpe_service(cpe_service.toString());
			return result;
		}
		else{
			return null;
		}
	}
	
	
	
	/**
	 * @param type 用户信息类型
	 * @param value 业务号码
	 * @return
	 */
	public ArrayList<Para> queryUserDevByOui_sn(int type, String value) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}

		// 查询用户信息
		String strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from " +
				table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id) left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
				" left join gw_device_model m on t.device_model_id = m.device_model_id where ";
		switch (type) {
			case 0:
				strSQL += " a.username ='" + value + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from " +
						table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id left join tab_gw_device d on a.device_id=d.device_id left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
						" left join gw_device_model m on t.device_model_id = m.device_model_id left join gw_devicestatus s on (d.device_id=s.device_id) where b.serv_type_id = 10 and b.username='" + value + "'";
				break;
			//语音号码
			case 2:
				strSQL = "select a.user_id, d.oui,d.device_serialnumber sn,a.device_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,m.device_model from tab_voip_serv_param v," +
						table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id)  left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
						" left join gw_device_model m on t.device_model_id = m.device_model_id where v.user_id=a.user_id ";
				strSQL += "and v.voip_phone ='" + value + "'";
				break;
			// 终端唯一标识
			case 3:
				//select a.username,d.device_id,t.softwareversion from tab_hgwcustomer a left join tab_gw_device d on a.device_id=d.device_id left join tab_devicetype_info t on (d.devicetype_id=t.devicetype_id) where rownum<10
				//唯一终端标识，暂理解为oui-sn
				String oui = value.split("-")[0];
				String sn = value.split("-")[1];

				strSQL += " d.device_serialnumber ='" + sn + "' and d.oui='"+oui+"'";
				if(sn.length()>6){
					String devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			case 4:
				strSQL += " d.loopback_ip ='" + value + "'";
				break;
			case 5:
				strSQL += " d.device_serialnumber ='" + value + "'";
				if(value.length()>6){
					String devSubSn = value.substring(value.length() - 6, value.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			default:
				return null;
		}

		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		ArrayList<Para> result = new ArrayList<Para>();
		UserDetail userDetail = new UserDetail();
		if(null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)){
			if(StringUtil.isEmpty(userIdList.get(0).get("cpe_allocatedstatus")) && !StringUtil.isEmpty(userIdList.get(0).get("user_id"))){
				result.add(setPara("iDegistState", "2"));
			}
			else{
				result.add(setPara("iDegistState", "1"));
				result.add(setPara("strCPEID", userIdList.get(0).get("oui")+userIdList.get(0).get("sn")));
				result.add(setPara("strCPEIP", userIdList.get(0).get("loopback_ip")));
				result.add(setPara("strLOID", userIdList.get(0).get("loid")));
				result.add(setPara("strMacAddr", userIdList.get(0).get("cpe_mac")));
				result.add(setPara("iOnlineState", "1".equals(userIdList.get(0).get("online_status"))?"1":"2"));
				result.add(setPara("strMtUser", StringUtil.isEmpty(userIdList.get(0).get("x_com_username"))?"-":userIdList.get(0).get("x_com_username")));
				result.add(setPara("strMtPass", StringUtil.isEmpty(userIdList.get(0).get("x_com_passwd"))?"-":userIdList.get(0).get("x_com_passwd")));
			}


			String userId = userIdList.get(0).get("user_id");

			// 查询业务信息
			String servSQL = "select b.protocol, b.voip_username, a.bind_port,b.voip_port,b.parm_stat,b.uri,b.reg_id,b.line_id" +
					" from " + table_serv_info + " a," + table_voip +
					" b where a.user_id=b.user_id and a.serv_status=1 and (a.serv_type_id=14 or a.serv_type_id=15) and a.user_id=" + userId;
			ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(new PrepareSQL(servSQL).getSQL());

			StringBuffer cpe_service = new StringBuffer();
			if(null != userServList && !userServList.isEmpty() && null != userServList.get(0)){
				int index = 1;
				String strVoipInfo = "";
				String protocol = "";
				String voip_username = "";
				String uri = "";
				String voip_port = "";
				String DeviceID = "";
				String LinePhysicalTermID = "";
				String protocoltype="";
				for(HashMap<String, String> userinfoMap : userServList){
					protocoltype = StringUtil.getStringValue(userinfoMap, "protocol");
					if(index > 1){
						protocol += "^";
					}
					//sip
					if("1".equals(protocoltype) || "0".equals(protocoltype)){
						voip_username = StringUtil.getStringValue(userinfoMap, "voip_username");
						uri = StringUtil.getStringValue(userinfoMap, "uri");
						voip_port = StringUtil.getStringValue(userinfoMap, "voip_port");

						protocol += "Sip_UserName_" + index + "=" + voip_username + "^Sip_LineURI_" + index + "=" + uri +
								"^Sip_Port_" + index + "=" + voip_port;
					}
					else{
						DeviceID = StringUtil.getStringValue(userinfoMap, "reg_id");
						voip_port = StringUtil.getStringValue(userinfoMap, "line_id");
						LinePhysicalTermID = StringUtil.getStringValue(userinfoMap, "voip_port");

						protocol += "H248_DeviceID_" + index + "=" + DeviceID + "^H248_LinePhysicalTermID_" + index + "=" + LinePhysicalTermID +
								"^H248_Port_" + index + "=" + voip_port;
					}
					index++;
				}
				result.add(setPara("strVoipInfo", protocol));
			}
			userDetail.setCpe_service(cpe_service.toString());
			return result;
		}
		else{
			return null;
		}
	}
	
	/**
	 * @param type 用户信息类型
	 * @param value 业务号码
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDeviceInfo(int type, String value) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
			table_voip = "tab_egw_voip_serv_param";
		}
		
		// 查询用户信息
		String strSQL = "select d.oui,d.device_serialnumber sn,d.device_id,a.user_id,a.username as loid,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,t.softwareversion,m.device_model from " +
						 	"tab_gw_device d left join "+table_customer+" a on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id)  left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id left join gw_device_model m on t.device_model_id = m.device_model_id where ";
		switch (type) {
			case 0:
				strSQL += " a.username ='" + value + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL = "select d.oui,d.device_serialnumber sn,d.device_id,a.user_id,a.username as loid, b.username adAccount,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,t.softwareversion,m.device_model from " +
							table_customer + " a inner join " + table_serv_info + " b on a.user_id=b.user_id left join tab_gw_device d on a.device_id=d.device_id " +
						"left join gw_devicestatus s on (d.device_id=s.device_id) left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id left join gw_device_model m on t.device_model_id = m.device_model_id where b.serv_type_id = 10 and b.username='" + value + "'";
				break;
			
			case 2:
				strSQL = "select d.oui,d.device_serialnumber sn,d.device_id,a.user_id,a.username as loid,v.voip_phone adAccount,d.loopback_ip,d.cpe_mac,d.x_com_username,d.x_com_passwd,d.cpe_allocatedstatus,s.last_time,s.online_status,t.hardwareversion,t.softwareversion,m.device_model from tab_voip_serv_param v," +
						table_customer + " a left join tab_gw_device d on a.device_id=d.device_id left join gw_devicestatus s on (d.device_id=s.device_id)  left join tab_devicetype_info t on t.devicetype_id=d.devicetype_id " +
								" left join gw_device_model m on t.device_model_id = m.device_model_id where v.user_id=a.user_id ";
				strSQL += "and v.voip_phone ='" + value + "'";
				break;
			// 终端唯一标识
			case 3:
				//唯一终端标识，暂理解为oui-sn
				String oui = value.split("-")[0];
				String sn = value.split("-")[1];
				strSQL += " d.device_serialnumber ='" + sn + "' and d.oui='"+oui+"'";
				if(sn.length()>6){
					String devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}

				break;
			case 4:
				strSQL += " d.loopback_ip ='" + value + "'";
				break;
			case 5:
				strSQL += " d.device_serialnumber ='" + value + "'";
				if(value.length()>6){
					String devSubSn = value.substring(value.length() - 6, value.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			default:
				return null;
		}
		
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		return userIdList;
	}

	/**
	 * @param type 用户信息类型
	 * @param value 业务号码
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryUserDevByUser(int type, String value) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";

		// 查询用户信息
		String strSQL = "select a.device_id, b.username accessNo,a.user_id,b.serv_type_id from "+table_customer+" a inner join "+table_serv_info+" b on a.user_id=b.user_id  inner join tab_gw_device d on d.device_id = a.device_id ";
		switch (type) {
			case 0:
				strSQL += "where a.username ='" + value + "'";
				break;
			// 用户宽带帐号
			case 1:
				strSQL += "where b.serv_type_id = 10 and b.username='" + value + "'";
				break;

			case 2:
				strSQL += " left join TAB_VOIP_SERV_PARAM v on a.user_id = v.user_id  where v.voip_phone ='" + value + "'";
				break;
			// 终端唯一标识
			case 3:
				//唯一终端标识，暂理解为oui-sn
				String oui = value.split("-")[0];
				String sn = value.split("-")[1];
				strSQL += "  left join TAB_GW_DEVICE d on a.device_id = d.device_id where d.device_serialnumber ='" + sn + "' and d.oui='"+oui+"'";
				if(sn.length()>6){
					String devSubSn = sn.substring(sn.length() - 6, sn.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			case 4:
				strSQL += " d.loopback_ip ='" + value + "'";
				break;
			case 5:
				strSQL += " d.device_serialnumber ='" + value + "'";
				if(value.length()>6){
					String devSubSn = value.substring(value.length() - 6, value.length());
					strSQL +=" and d.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			default:
				return null;
		}

		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		return userIdList;
	}
	
	/**
	 * 根据id获取业务信息
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getServById(String userId){
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		String servSQL = "select a.username,a.serv_type_id,b.protocol, b.voip_username, a.bind_port,b.voip_port,b.parm_stat,b.uri,b.reg_id,b.line_id" +
				" from " + table_serv_info + " a left join " + table_voip +
				" b on  a.user_id=b.user_id  where a.serv_status=1 and a.user_id=?";
		PrepareSQL Psql = new PrepareSQL(servSQL);
		Psql.setInt(1, StringUtil.getIntegerValue(userId));
		ArrayList<HashMap<String, String>> userServList = DBOperation.getRecords(Psql.getSQL());
		return userServList;
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
	
	public ArrayList<HashMap<String, String>> getInfoByCPEID(String cpeID){
		String oui = cpeID.split("-")[0];
		String sn = cpeID.split("-")[1];
		String strSQL = "select a.user_id, a.username, a.city_id, d.device_id from tab_gw_device d left join tab_hgwcustomer a on a.device_id" +
				"=d.device_id where d.oui=? and d.device_serialnumber=?";
		PrepareSQL Psql = new PrepareSQL(strSQL);
		Psql.setString(1, oui);
		Psql.setString(2, sn);
		
		ArrayList<HashMap<String, String>> deviceList = DBOperation.getRecords(Psql.getSQL());
		return deviceList;
	}
	
	
	/**
	 * 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
	 * @param userId
	 */
	public void updateCustStatusFailure(long userId, int open_status) {
		String tableName= " hgwcust_serv_info ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableName = " egwcust_serv_info ";
		}
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("update ");
		pSql.append(tableName);
		pSql.append(" set open_status=?,updatetime=? where user_id=? and serv_status in (1,2) and open_status = 0");
		int index = 0;
		pSql.setInt(++index, open_status);
		pSql.setLong(++index, new DateTimeUtil().getLongTime());
		pSql.setLong(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update table[{}] rows[{}].", tableName, updateRows);
	}
	
	public int doBatch(String[] mysqls){
		return DBOperation.executeUpdate(mysqls);
	}
	
	private Para setPara(String name, String value){
		Para para = new Para();
		para.setName(name);
		para.setValue(value);
		return para;
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
	 * @param loid
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
		
		String strSQL = "select a.city_id,a.device_status,c.vendor_name,d.device_model,b.hardwareversion, e.last_time, " +
				" a.complete_time,b.access_style_relay_id,a.loopback_ip,a.cpe_mac,b.spec_id,a.oui " +
				" from tab_gw_device a " +
				" left join tab_devicetype_info b on a.devicetype_id = b.devicetype_id " +
				" left join tab_vendor c on a.vendor_id = c.vendor_id " +
				" left join gw_device_model d on a.device_model_id = d.device_model_id " +
				" left join gw_devicestatus e on a.device_id = e.device_id " +
				" where a.device_id = '" + deviceId + "'";
		
		return DBOperation.getRecords(strSQL);
	}
	
	/**
	 * 查询设备
	 * @param deviceId
	 * @return
	 */
	public boolean getDevice(String deviceId){
		
		String strSQL = "select count(1) as num from tab_bss_dev_port ";
		Map<String, String> deviceInfoMap = DBOperation.getRecord(strSQL);
		if(null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 根据设备序列号，厂商OUI检索设备信息
	 * 
	 * @param DevSn
	 * @param oui
	 * @return
	 */
	public boolean queryDevInfo(String DevSn, String oui)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfo({},{})", new Object[] { DevSn, oui });
		String devSubSn = DevSn.substring(DevSn.length() - 6, DevSn.length());
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select device_id, oui, device_serialnumber, city_id, loopback_ip, device_status, devicetype_id, cpe_mac, device_model_id, vendor_id, dev_sub_sn ");
		pSql.append("  from tab_gw_device ");
		pSql.append(" where 1=1 and dev_sub_sn = '" + devSubSn + "' ");
		pSql.append("   and device_serialnumber like  '%" + DevSn + "' ");
		pSql.append("   and oui = '" + oui + "' ");
		Map<String, String> deviceInfoMap = DBOperation.getRecord(pSql.getSQL());
		if(null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 入gw_wan表
	 * 
	 * @author gongsj
	 * @date 2010-7-19
	 * @param deviceId
	 * @param wanId
	 * @param accessType
	 * @return
	 */
	public boolean insertWan(String deviceId, String wanId, String accessType)
	{
		if (null == deviceId || null == wanId)
		{
			return false;
		}
		String gatherTime = String.valueOf(new DateTimeUtil().getLongTime());
		StringBuilder usBuilder = new StringBuilder();
		usBuilder.append("delete from gw_wan where device_id='").append(deviceId)
				.append("' and wan_id=").append(wanId);
		if (DBOperation.executeUpdate(usBuilder.toString()) > 0)
		{
			usBuilder = new StringBuilder();
			usBuilder
					.append("insert into gw_wan(device_id, wan_id, access_type, gather_time) values('");
			usBuilder.append(deviceId).append("',").append(wanId).append(",'")
					.append(accessType).append("',").append(gatherTime).append(")");
			return 1 == DBOperation.executeUpdate(usBuilder.toString());
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 查询设备规格信息
	 * @param specId
	 * @return
	 */
//	public List<HashMap<String, String>> getTabBssDevPortInfo(String specId){// 不被使用
//		String strSQL = "select * from tab_bss_dev_port where id=" + StringUtil.getStringValue(specId);
//		return DBOperation.getRecords(strSQL);
//	}
	
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
	 * @param  username:用户信息
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
//	public List<HashMap<String,String>> queryDeviceInfoByDeviceId(String deviceId){// 不被使用
//		
//		if(StringUtil.isEmpty(deviceId)){
//			return null;
//		}
//		
//		PrepareSQL psql = new PrepareSQL("select * from tab_gw_device where device_id = ?");
//		psql.setString(1, deviceId);
//		return DBOperation.getRecords(psql.getSQL());
//	}
	
	/**
	 * 根据oui查询厂商信息
	 * @param oui
	 * @return
	 */
//	public Map<String, String> queryVendorIdByOui(String oui){// 不被使用
//		
//		PrepareSQL psql = new PrepareSQL("select * from tab_vendor_oui where oui = ?");
//		psql.setString(1, oui);
//		return DBOperation.getRecord(psql.getSQL());
//	}
	
	/**
	 * 检查是否有符合要求版本
	 * @param device_model
	 * @param oui
	 * @param softwareversion
	 * @return
	 */
//	public boolean queryTypeInfoIdByOui(String device_model, String oui, String softwareversion, String hardwareversion)// 不被使用
//	{
//		PrepareSQL psql = new PrepareSQL("select * from tab_devicetype_info a, gw_device_model b, tab_vendor c, " +
//				"tab_vendor_oui d where a.device_model_id = b.device_model_id and b.vendor_id = c.vendor_id " +
//				"and c.vendor_id = d.vendor_id and b.device_model = ? and d.oui = ? and a.softwareversion = ?");
//		if(null != hardwareversion && !hardwareversion.isEmpty())
//		{
//			psql.append(" and a.hardwareversion = ? ");
//		}
//		psql.setString(1, device_model);
//		psql.setString(2, oui);
//		psql.setString(3, softwareversion);
//		if(null != hardwareversion && !hardwareversion.isEmpty())
//		{
//			psql.setString(4, hardwareversion);
//		}
//		Map<String, String> deviceInfoMap = DBOperation.getRecord(psql.getSQL());
//		if(null == deviceInfoMap || deviceInfoMap.isEmpty())
//		{
//			return false;
//		}
//		return true;
//	}
	
	/*
	public static String add(DelCpeInfo param, long time) {
		String sql = "insert into tab_gw_device (device_id,device_name,oui,"
				+ "device_serialnumber,loopback_ip,device_status,devicetype_id,cr_port,cr_path,"
				+ "cpe_currentupdatetime,gather_id,"
				+ "city_id,complete_time,cpe_allocatedstatus,"
				+ "gw_type,device_model_id,customer_id"
				+ ",device_type,vendor_id,x_com_passwd"
				+ ",cpe_username,cpe_passwd,acs_username,acs_passwd"
				+ ",dev_sub_sn,cpe_mac,x_com_username,buy_time)" + " values('"
				+ newDevObj.getDeviceId()
				+ "','"
				+ newDevObj.getOSN()
				+ "','"
				+ newDevObj.getOui()
				+ "','"
				+ newDevObj.getDevSn()
				+ "',?,"
				+ newDevObj.getDevAllStatusObj().getDeviceStatus()
				+ ","
				+ newDevObj.getDevicetypeId()
				+ ","
				+ devTr069OBJ.getPort()
				+ ",'"
				+ devTr069OBJ.getPath()
				+ "',"
				+ time
				+ ",'"
				+ newDevObj.getGatherId()
				+ "','"
				+ newDevObj.getCityOfficeZoneObj().getCityId()
				+ "',"
				+ time
				+ ","
				+ newDevObj.getDevAllStatusObj().getCpeAllocatedstatus()
				+ "," + newDevObj.getGwType() + ",?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1, newDevObj.getDevIp());
		pSQL.setString(2, newDevObj.getDeviceModelId());
		pSQL.setString(3, newDevObj.getCustomerId());
		pSQL.setString(4, newDevObj.getDeviceType());
		pSQL.setString(5, newDevObj.getVendorId());

		pSQL.setString(6, devAuthOBJ.getX_com_passwd());
		pSQL.setString(7, devTr069OBJ.getCpe_username());
		pSQL.setString(8, devTr069OBJ.getCpe_passwd());
		pSQL.setString(9, devTr069OBJ.getAcs_username());
		pSQL.setString(10, devTr069OBJ.getAcs_passwd());
		pSQL.setString(11, newDevObj.getDevSubSn());
		pSQL.setString(12, newDevObj.getCpeMac());
		pSQL.setString(13, devAuthOBJ.getX_com_username());
		pSQL.setLong(14, time);

		return pSQL.getSQL();
	}
	*/
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
	
	/**
	 * 用户销户
	 * @param _userId
	 * @return
	 */
	public List<String> stopUserSql(long _userId) {
		logger.debug("stopUserSql({})", new Object[]{_userId});

		long nowTime = System.currentTimeMillis() / 1000;
		
		List<String> sqlList = new ArrayList<String>();
		PrepareSQL psql = new PrepareSQL();
		String tabName = "tab_hgwcustomer";
		String strSQL = "insert into "+tabName+"_bak select * from " + tabName + " where user_id=?";
		psql.setSQL(strSQL);
		psql.setLong(1, _userId);
		sqlList.add(psql.getSQL());
		
		strSQL = "update "+tabName+"_bak set user_state='3',"
			+ " updatetime=?, closedate=? where user_id=? ";
		psql.setSQL(strSQL);
		psql.setLong(1, nowTime);
		psql.setLong(2, nowTime);
		psql.setLong(3, _userId);
		sqlList.add(psql.getSQL());
		
		strSQL = "delete from "+tabName+" where user_id=? ";
		psql.setSQL(strSQL);
		psql.setLong(1, _userId);
		sqlList.add(psql.getSQL());
		
		return sqlList;
	}
	
	/**
	 * 用户终端记录删除
	 * @param _userId
	 * @return
	 */
	public String delGwCustUserDevType(long _userId) {
		logger.debug("delGwCustUserDevType({})", new Object[]{ _userId});
		
		String strSQL = "delete from gw_cust_user_dev_type where user_id=? ";
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(strSQL);
		psql.setLong(1, _userId);

		return psql.getSQL();
	}
	
	/**
	 * 全业务用户销户
	 * @param _userId
	 * @return
	 */
	public List<String> stopAllServiceSql(long _userId) {
		logger.debug("stopAllServiceSql({})", _userId);
		List<String> sqlList = new ArrayList<String>();
		PrepareSQL psql = new PrepareSQL();
		String strSQL = "delete from tab_voip_serv_param where user_id=? ";
		psql.setSQL(strSQL);
		psql.setLong(1, _userId);
		sqlList.add(psql.getSQL());
		
		strSQL = "delete from hgwcust_serv_info where user_id=? ";
		psql.setSQL(strSQL);
		psql.setLong(1, _userId);
		sqlList.add(psql.getSQL());
		// 新表结构删除参数表
		/*if (!"0".equals(Global.sys.getIsNewTable()))
		{
			strSQL = "delete from tab_net_serv_param where user_id =? ";
			psql.setSQL(strSQL);
			psql.setLong(1, _userId);
			sqlList.add(psql.getSQL());
		}*/
		return sqlList;
	}
	
}
