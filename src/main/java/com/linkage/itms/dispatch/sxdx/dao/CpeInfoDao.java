package com.linkage.itms.dispatch.sxdx.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.sxdx.obj.StrategyOBJ;
import com.linkage.system.utils.database.Cursor;
import com.linkage.system.utils.database.DataSetBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpeInfoDao {
	private static Logger logger = LoggerFactory.getLogger(CpeInfoDao.class);
	
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
	 * 获得上行方式
	 * 
	 * @author gongsj
	 * @date 2010-7-20
	 * @param deviceId
	 * @return
	 */
	public String getAccessType(String deviceId)
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
	
	/**
	 * 
	 * 根据设备ID查询设备厂商，设备型号，软件版本等信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getDeviceVersion(String deviceId) {

		StringBuffer sql = new StringBuffer();

		sql.append(
				"select a.device_serialnumber, a.city_id, a.loopback_ip, a.cpe_currentstatus,a.complete_time, ")
				.append(" b.vendor_name, b.vendor_add, d.device_model, c.softwareversion,c.hardwareversion, c.is_check, c.rela_dev_type_id ")
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

	public Map<String, String> queryUserInfoNew(int userType, String username) {
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
			userInfoMap.put("user_id", userIdList.get(0).get("user_id"));
			
			
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


	public Map<String, String> queryUserInfo(int userType, String username) {
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String table_voip = "tab_voip_serv_param";
		//String table_device = "tab_gw_device";
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
			case 5:
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
						if (StringUtil.isEmpty(netName)) {
							netName = StringUtil.getStringValue(userinfoMap, "pppusename");
							netPassword = StringUtil.getStringValue(userinfoMap, "passwd");
							netStatus = StringUtil.getStringValue(userinfoMap, "open_status");
							netPort = StringUtil.getStringValue(userinfoMap, "bind_port");
							wanType = StringUtil.getStringValue(userinfoMap, "wan_type");
						}
					}

					// IPTV
					if ("11".equals(StringUtil.getStringValue(userinfoMap, "serv_type_id"))) {
						if (StringUtil.isEmpty(iptvName)) {
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
						if (StringUtil.isEmpty(voipName)) {
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
	
	/**
	 * 查询设备信息
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getDeviceInfo(String deviceId){
		
		String strSQL = "select a.device_id,a.device_serialnumber,a.city_id,a.device_status,a.vendor_id,a.devicetype_id,a.device_model_id,c.vendor_name,d.device_model,b.hardwareversion," +
				" a.complete_time,b.access_style_relay_id,a.loopback_ip,a.cpe_mac,b.spec_id,a.oui " +
				" from tab_gw_device a " +
				" left join tab_devicetype_info b on a.devicetype_id = b.devicetype_id " +
				" left join tab_vendor c on a.vendor_id = c.vendor_id " +
				" left join gw_device_model d on a.device_model_id = d.device_model_id " +
				" where a.device_id = '" + deviceId + "'";
		
		return DBOperation.getRecords(strSQL);
	}

	public Map<String, String> getDeviceTypeInfo(String vendor_id,String device_model_id, String fileName) {
		
		String strSQL = "select devicetype_id from gw_device_model where vendor_id = '"+vendor_id
				+"' and  device_model_id = '" + device_model_id + "' and softwareversion = '"+fileName+"'";
		
		return DBOperation.getRecord(strSQL);
		
	}
	
	/**
	 * 升级文件信息
	 * 
	 * @author wangsenbo
	 * @date Sep 20, 2011
	 * @param
	 * @return Map<String,Map<String,String>>
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Map<String, String>> getSoftFileInfo()
	{
		// 修改 by 王森博
		String strSQL = null;
			strSQL = "select devicetype_id,outter_url||'/'||server_dir||'/'||softwarefile_name as file_url"
					+ ",softwarefile_size,softwarefile_name"
					+ " from tab_software_file a, tab_file_server b where a.dir_id=b.dir_id"
					+ " and softwarefile_isexist=1";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select devicetype_id,concat(outter_url,'/',server_dir,'/',softwarefile_name) as file_url"
					+ ",softwarefile_size,softwarefile_name"
					+ " from tab_software_file a, tab_file_server b where a.dir_id=b.dir_id"
					+ " and softwarefile_isexist=1";
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		Cursor cursor = DataSetBean.getCursor(psql.toString());
		if (null == cursor)
		{
			return null;
		}
		Map<String, Map<String, String>> softFileMap = new HashMap<String, Map<String, String>>();
		Map<String, String> map = cursor.getNext();
		while (null != map)
		{
			softFileMap.put(StringUtil.getStringValue(map.get("devicetype_id")), map);
			map = cursor.getNext();
		}
		return softFileMap;
	}

		/**
		 * 增加策略
		 * 
		 * @author gongsj
		 * @date 2009-7-16
		 * @param obj
		 * @return Object
		 */
		public Boolean strategySQL(StrategyOBJ obj)
		{
			logger.debug("addStrategy({})", obj);
			StringBuilder sql = new StringBuilder();
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();
			if (obj == null)
			{
				logger.debug("obj == null");
				return false;
			}
			int result = 0;
			ArrayList<String> list = new ArrayList<String>();
			sql.append("delete from ").append("gw_serv_strategy_soft")
					.append(" where device_id='").append(obj.getDeviceId())
					.append("' and temp_id=").append(obj.getTempId());
			if (null != obj.getTaskId())
			{
				sql.append(" and task_id != '").append(obj.getTaskId()).append("'");
			}
			if (1 != DBOperation.executeUpdate(sql.toString()))
			{
				logger.warn("删除设备{}的策略失败", obj.getDeviceId());
			}
			// add by chenzhangjian at 20150612 start
			// 解决sheet_para超过4000无法入库问题
			String sheet_param = obj.getSheetPara();
			String sheet_para_str = sheet_param;
			// 判断字符串是否超过长度
			if (Global.SPIT_NUM_LARGE_VAL <= sheet_para_str.length())
			{
				// 是否开启大数据处理
				if (1 == Global.IS_LARGE_VALUE)
				{				
					sheet_para_str = sheet_para_str.substring(0, Global.SPIT_NUM_LARGE_VAL)
							+ "...";
					list.add("delete from tab_sheet_para_value where strategy_id="+obj.getId());
					StringBuilder sql3 = null;
					// 需截取次数
					int count = 0;
					if (0 == sheet_param.length() % Global.SPIT_NUM_LARGE_VAL)
					{
						count = sheet_param.length() / Global.SPIT_NUM_LARGE_VAL;
					}
					else
					{
						count = sheet_param.length() / Global.SPIT_NUM_LARGE_VAL + 1;
					}
					
					int subStrCount = 0;
					String sheet_para_val = "";
					// 循环截取待处理字符串
					for (int i = 0; i < count; i++)
					{
						sql3 = new StringBuilder();					
						if (((i + 1) * Global.SPIT_NUM_LARGE_VAL + subStrCount) < sheet_param.length())
						{
							sheet_para_val = sheet_param.substring(i* Global.SPIT_NUM_LARGE_VAL - subStrCount,
									(i + 1)* Global.SPIT_NUM_LARGE_VAL);
						}
						else
						{
							sheet_para_val = sheet_param.substring(i* Global.SPIT_NUM_LARGE_VAL - subStrCount);
						}
						
						// 结算结尾空格数
						subStrCount = sheet_para_val.length() - ("A" + sheet_para_val).trim().length() + 1;
						sheet_para_val = sheet_para_val.substring(0, sheet_para_val.length() - subStrCount);
						
						sql3.append(
								"insert into tab_sheet_para_value(strategy_id,order_id,para_value) values (")
								.append(obj.getId()).append(",").append(i + 1).append(",'")
								.append(sheet_para_val).append("')");
						list.add(sql3.toString().replaceAll("'null'", "null"));
					}
				}
			}
			// add by chenzhangjian at 20150612 end
			sql1.append("insert into ").append("gw_serv_strategy_soft").append(" (");
			sql1.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority,client_id");
			sql1.append(") values (");
			sql1.append(obj.getRedo());
			sql1.append(",");
			sql1.append(obj.getId());
			sql1.append("," + obj.getAccOid());
			sql1.append("," + obj.getTime());
			sql1.append("," + obj.getType());
			sql1.append(",'" + obj.getDeviceId());
			sql1.append("','" + obj.getOui());
			sql1.append("','" + obj.getSn());
			sql1.append("','" + obj.getUsername());
			sql1.append("','" + sheet_para_str);
			sql1.append("'," + obj.getServiceId());
			sql1.append(",'" + obj.getTaskId());
			sql1.append("'," + obj.getOrderId());
			sql1.append("," + obj.getSheetType());
			sql1.append("," + obj.getTempId());
			sql1.append("," + obj.getIsLastOne());
			sql1.append("," + obj.getPriority());
			sql1.append("," + obj.getClientId());
			sql1.append(")");
			sql2.append("insert into ").append("gw_serv_strategy_log_soft").append(" (");
			sql2.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority,client_id");
			sql2.append(") values (");
			sql2.append(obj.getRedo());
			sql2.append(",");
			sql2.append(obj.getId());
			sql2.append("," + obj.getAccOid());
			sql2.append("," + obj.getTime());
			sql2.append("," + obj.getType());
			sql2.append(",'" + obj.getDeviceId());
			sql2.append("','" + obj.getOui());
			sql2.append("','" + obj.getSn());
			sql2.append("','" + obj.getUsername());
			sql2.append("','" + sheet_para_str);
			sql2.append("'," + obj.getServiceId());
			sql2.append(",'" + obj.getTaskId());
			sql2.append("'," + obj.getOrderId());
			sql2.append("," + obj.getSheetType());
			sql2.append("," + obj.getTempId());
			sql2.append("," + obj.getIsLastOne());
			sql2.append("," + obj.getPriority());
			sql2.append("," + obj.getClientId());
			sql2.append(")");
			list.add(sql1.toString().replaceAll("'null'", "null"));
			list.add(sql2.toString().replaceAll("'null'", "null"));
			logger.debug("入策略表:{}", list);
			result = this.doBatch(list);
			sql = null;
			sql1 = null;
			sql2 = null;
			list = null;
			if (1 == result)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		/**
		 * 执行批量SQL.
		 * 
		 * @param sqlList
		 *            SQL语句数组
		 * @return 返回操作的记录条数
		 */
		public int doBatch(ArrayList<String> sqlList)
		{
			int result = 0;
			if(sqlList != null && !sqlList.isEmpty())
			{
				result = DBOperation.executeUpdate(sqlList);
			}
			return result;
		}

		public Map<String, String> getSoftResultInfo(String deviceId) {
			
			String sql = "select a.status,a.device_id,a.result_id,c.fault_desc from gw_serv_strategy_soft a,tab_gw_device b,tab_cpe_faultcode c "
					+ "where a.result_id = c.fault_code and a.service_id= 5 and a.is_last_one=1  where a.device_id = '" + deviceId+"'";
			return DBOperation.getRecord(sql);
		}
		/**
		 * 获取终端业务下发时间
		 * @param deviceId
		 * @return
		 */
		public Map<String, String> getServResultInfo(String deviceId) {
			
			String sql = "select end_time from gw_serv_strategy_serv  "
					+ " where result_id = 1 and is_last_one=1  and device_id = '" + deviceId+"'";
			return DBOperation.getRecord(sql);
		}

		public ArrayList<HashMap<String, String>> getVlanID(long userid, String servTypeId) {
			String sql = "select wan_type,vlanid from hgwcust_serv_info  "
					+ "where user_id = " + userid + " and serv_type_id = " +servTypeId;
			return DBOperation.getRecords(sql);
		}

		public String getAccessStyleType(String deviceId) {
			StringBuffer sql2 = new StringBuffer();
			sql2.append(
					"select access_style_relay_id from tab_gw_device a,tab_devicetype_info b where a.devicetype_id=b.devicetype_id and a.device_id='")
					.append(deviceId).append("'");
			Map<String, String> accessTypeMap2 = DBOperation.getRecord(sql2.toString());
			
			if (null == accessTypeMap2
					|| null == accessTypeMap2.get("access_style_relay_id"))
			{
			}
			else
			{
				String accessTypeId = accessTypeMap2.get("access_style_relay_id");
				if ("1".equals(accessTypeId))
				{
					logger.warn("[{}]采用设备版本表中的上行方式：DSL", deviceId);
					return "1";
				}
				if ("2".equals(accessTypeId))
				{
					logger.warn("[{}]采用设备版本表中的上行方式：Ethernet", deviceId);
					return "2";
				}
				if ("3".equals(accessTypeId))
				{
					logger.warn("[{}]采用设备版本表中的上行方式：EPON", deviceId);
					return "3";
				}
				if ("4".equals(accessTypeId))
				{
					logger.warn("[{}]采用设备版本表中的上行方式：GPON", deviceId);
					return "4";
				}
				logger.warn("[{}]设备版本表中的上行方式获取失败", deviceId);
			}
			String type = getAccessTypeFromTabCustomer(deviceId);
			if (!StringUtil.IsEmpty(type))
			{
				logger.warn("[{}]采用用户表中的上行方式：" + type, deviceId);
				return type;
			}
			else
			{
				logger.warn("[{}]用户表中的上行方式获取失败", deviceId);
			}
			return null;
		}
		
		
		public static String getAccessTypeFromTabCustomer(String deviceId)
		{
			String sql = "select access_style_id from tab_hgwcustomer where device_id='" + deviceId + "'";
			Map<String, String> numMap = DBOperation.getRecord(sql);
			//String type = "";
			if (null != numMap)
			{
				String id = numMap.get("access_style_id");
				return id;
			}
			return null;
		}
		
		/**
		 * 获得组装策略表需要的业务信息（此处是VOIP地址端口等信息）
		 * 
		 * @author gongsj
		 * @date 2009-9-1
		 * @param
		 * @return
		 */
		public Map<String, String> queryVoipInfo(String userId)
		{
			StringBuilder sql = new StringBuilder();
			if (3 == DBUtil.GetDB()) {
				sql.append(
						"select line_id from tab_voip_serv_param a, tab_sip_info b where a.sip_id=b.sip_id and a.user_id=")
						.append(userId).append(" order by a.line_id");
			} else {
				sql.append(
						"select * from tab_voip_serv_param a, tab_sip_info b where a.sip_id=b.sip_id and a.user_id=")
						.append(userId).append(" order by a.line_id");
			}
			return DBOperation.getRecord(sql.toString());
		}
		
		/**
		 * 根据用户id获取用户设备信息
		 * 
		 * @param userId
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
					+ " c.online_status, c.last_time,a.city_id"
					+ " from " + table_customer + " a left join tab_gw_device b on a.device_id=b.device_id"
					+ " left join gw_devicestatus c on  a.device_id=c.device_id "
					+ " where a.user_state in ('1','2')"
					+ " and a.user_id="
					+ userId ;
			logger.info(strSQL);
			return DBOperation.getRecord(strSQL);
		}

	public String getServTypeId(String user_id) {
		String sql = "select serv_type_id from HGWCUST_SERV_INFO  where user_id=" + user_id;
		Map<String, String> numMap = DBOperation.getRecord(sql);
		if (null != numMap)
		{
			String id = numMap.get("serv_type_id");
			return id;
		}
		return null;
	}

	public String getServTypeIdBytype(String user_id) {
		String sql = "select serv_type_id from HGWCUST_SERV_INFO  where user_id=" + user_id + " and serv_type_id = 14 ";
		Map<String, String> numMap = DBOperation.getRecord(sql);
		if (null != numMap)
		{
			String id = numMap.get("serv_type_id");
			return id;
		}
		return null;
	}

	public int getServOpenStatus(String user_id) {

		try {

			String sql = "update HGWCUST_SERV_INFO  set open_status  = 0 where user_id = " + user_id + " and serv_type_id = 14";
			logger.info(sql);
			return DBOperation.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

	/**
	 * 根据userid查询voip认证密码
	 * @param user_id
	 * @return
	 */
	public List<HashMap<String, String>> getVoipPWD(String user_id) {
		String sql = "select line_id, voip_passwd from tab_voip_serv_param where user_id=" + user_id + " order by line_id";
		List<HashMap<String, String>> numMap = DBOperation.getRecords(sql);
		if (null != numMap)
		{
			return numMap;
		}
		return null;
	}

}
