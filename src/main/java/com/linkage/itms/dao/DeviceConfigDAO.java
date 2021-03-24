package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.HgwServUserObj;

/**
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2011-5-16 下午03:55:02
 * @category com.linkage.itms.dao
 * @copyright 南京联创科技 网管科技部
 */
public class DeviceConfigDAO {

	private static Logger logger = LoggerFactory
			.getLogger(UserInstReleaseDAO.class);

	/**
	 * 根据用户ID获取对应的业务信息数组
	 * 
	 * @author zhangshimin
	 * @date 2011-05-16
	 * @param
	 * @return HgwServUserObj[]
	 */
	public HgwServUserObj queryHgwcustServUserByDevId(long userId,
			long servTypeId) {
		logger.debug("queryHgwcustServUserByDevId({})", userId);
		HgwServUserObj hgwServUserObj = null;
		String strSQL = "select b.user_id, b.serv_type_id, b.username, b.open_status, b.vlanid, b.bind_port"
				+ " from hgwcust_serv_info b "
				+ " where b.serv_status=1 and b.user_id=? and serv_type_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);
		psql.setLong(2, servTypeId);
		// 执行查询
		List<HashMap<String, String>> resList = DBOperation.getRecords(psql
				.getSQL());
		// 查询结果非空
		if (null != resList && false == resList.isEmpty()) {
			hgwServUserObj = new HgwServUserObj();
			Map<String, String> rMap = (Map<String, String>) resList.get(0);
			hgwServUserObj.setUserId(StringUtil.getStringValue(rMap
					.get("user_id")));
			hgwServUserObj.setServTypeId(StringUtil.getStringValue(rMap
					.get("serv_type_id")));
			hgwServUserObj.setUsername(String.valueOf(rMap.get("username")));
			hgwServUserObj
					.setOpenStatus(String.valueOf(rMap.get("open_status")));
			hgwServUserObj.setVlanid(String.valueOf(rMap.get("vlanid")));
			hgwServUserObj.setBindPort(String.valueOf(rMap.get("bind_port")));
		}
		return hgwServUserObj;
	}

	public List<HashMap<String, String>> getUserVOIP(String userId) {
		String table_voip = "tab_voip_serv_param";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_voip = "tab_egw_voip_serv_param";
		}
		PrepareSQL sql = new PrepareSQL(
				"select a.voip_username,a.line_id,a.protocol,b.prox_serv,b.prox_port,b.regi_serv,b.regi_port,b.stand_regi_serv,b.stand_regi_port from "
						+ table_voip
						+ " a, tab_sip_info b where a. sip_id =b. sip_id  and a.user_id=?");
		sql.setLong(1, StringUtil.getLongValue(userId));
		return DBOperation.getRecords(sql.getSQL());
	}

//	public Map<String, List<Map<String, String>>> getPreConfInfo(String cityId,//不被使用
//			String shortName, String accessType) {
//		PrepareSQL sql = new PrepareSQL(
//				"select * from r_dev_pre_conf where shortname=? and city_id=? and access_type=?");
//		sql.setString(1, shortName);
//		sql.setString(2, cityId);
//		if ("EPON".equals(accessType) || "PON".equals(accessType)
//				|| "GPON".equals(accessType)) {
//			accessType = "PON";
//		}
//		sql.setString(3, accessType);
//		List<HashMap<String, String>> list = DBOperation.getRecords(sql
//				.getSQL());
//		Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
//		for (int i = 0; i < list.size(); i++) {
//			Map<String, String> tmap = (Map<String, String>) list.get(i);
//			String serv_name = StringUtil.getStringValue(tmap.get("serv_name"));
//			List<Map<String, String>> tlist = map.get(serv_name);
//			if (tlist == null) {
//				tlist = new ArrayList<Map<String, String>>();
//			}
//			tlist.add(tmap);
//			map.put(serv_name, tlist);
//		}
//		return map;
//	}

	/**
	 * 返回上行方式
	 * 
	 * @param deviceId
	 * @param i
	 * @return
	 */
	public String getAccessType(String deviceId, int i) {
		PrepareSQL sql = new PrepareSQL(
				"select access_type from gw_wan where device_id=? and wan_id=?");
		if("BBMS".equals(Global.SYSTEM_NAME)){
			sql = new PrepareSQL(
					"select access_type from gw_wan_bbms where device_id=? and wan_id=?");
		}
		sql.setString(1, deviceId);
		sql.setInt(2, i);
		Map<String, String> map = DBOperation.getRecord(sql.getSQL());
		return StringUtil.getStringValue(map.get("access_type"));
	}

	/**
	 * 快速采集 Internet 相关信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getInternet(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port,b.sess_type,b.ip_type,b.conn_type from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='INTERNET' and a.device_id=?");
		if("BBMS".equals(Global.SYSTEM_NAME)){
			sql = new PrepareSQL(
					"select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port,b.sess_type,b.ip_type,b.conn_type from gw_wan_conn_bbms a,gw_wan_conn_session_bbms b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='INTERNET' and a.device_id=?");
		}
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
	}

	/**
	 * 获取WLAN
	 * 
	 * @param device_id
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getWlan(String deviceId) {
		logger.debug("WanObj[] getWlan({})", deviceId);
		if (deviceId == null) {
			logger.debug("deviceId == null");
			return null;
		}
		String sql = "select * from gw_lan_wlan where device_id='" + deviceId
				+ "'";
		
		if (3 == DBUtil.GetDB()) {
			sql = "select ssid,associated_num from gw_lan_wlan where device_id='" + deviceId
					+ "'";
		}
		
		PrepareSQL psql = new PrepareSQL(sql);
		logger.warn("WLAN参数查询");
		psql.getSQL();
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 快速采集 IPTV 相关信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getIPTV(String deviceId) {
		PrepareSQL psql = new PrepareSQL();
		if("BBMS".equals(Global.SYSTEM_NAME)){
			psql.append("select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port from gw_wan_conn_bbms a,gw_wan_conn_session_bbms b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id ");
		}else{
			psql.append("select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id ");
		}
		psql.append(" and (b.serv_list='Other' or b.serv_list='OTHER') and a.device_id = '" + deviceId + "' ");
		if ("jl_dx".equals(Global.G_instArea)) {
			psql.append(" and  a.vlan_id ='99' and  b.bind_port is not null");
		}
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 快速采集 VOIP 相关信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getPVCVOIP(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select a.vpi_id,a.vci_id,a.vlan_id,b.conn_type,b.bind_port,b.ip_type,b.sess_type from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='VOIP' and a.device_id=?");
		if("BBMS".equals(Global.SYSTEM_NAME)){
			sql = new PrepareSQL(
					"select a.vpi_id,a.vci_id,a.vlan_id,b.conn_type,b.bind_port,b.ip_type,b.sess_type from gw_wan_conn_bbms a,gw_wan_conn_session_bbms b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='VOIP' and a.device_id=?");
		}
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
	}

	/**
	 * VOIP VoiceService
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getVoiceService(String deviceId) {// 所有字段都要用
		logger.debug("DeviceConfigDAO==>getVoiceService({})",
				new Object[] { deviceId });
		PrepareSQL psql = new PrepareSQL();
		if("BBMS".equals(Global.SYSTEM_NAME)){
			psql.append("select * from gw_voip_prof_bbms where 1=1 and device_id = '"
					+ deviceId + "'");
		}else{
			psql.append("select * from gw_voip_prof where 1=1 and device_id = '"
					+ deviceId + "'");
		}
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * VOIP 线路信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getVoipLines(String deviceId) {// 所有字段都要用
		logger.debug("DeviceConfigDAO==>getVoipLines({})",
				new Object[] { deviceId });
		PrepareSQL psql = new PrepareSQL();
		if("BBMS".equals(Global.SYSTEM_NAME)){
			psql.append("select * from gw_voip_prof_line_bbms where 1=1 and device_id = '"
					+ deviceId + "' ");
		}else{
			psql.append("select * from gw_voip_prof_line where 1=1 and device_id = '"
					+ deviceId + "' ");
		}
		psql.append(" order by device_id, line_id ");
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * IGMP Snooping
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getIGMPSnooping(String deviceId) {

		logger.debug("DeviceConfigDAO==>getIGMPSnooping({})",
				new Object[] { deviceId });
		PrepareSQL psql = new PrepareSQL();
		if("BBMS".equals(Global.SYSTEM_NAME)){
			psql.append("select igmp_enab, proxy_enable, snooping_enable from gw_iptv_bbms where device_id='"+ deviceId + "' ");
		}else{
			psql.append("select igmp_enab, proxy_enable, snooping_enable from gw_iptv where device_id='"+ deviceId + "' ");
		}
		return DBOperation.getRecords(psql.getSQL());
	}

	public String getIGMP(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select igmp_enab from gw_iptv where device_id=?");
		sql.setString(1, deviceId);
		Map<String, String> map = DBOperation.getRecord(sql.getSQL());
		return StringUtil.getStringValue(map.get("igmp_enab"));
	}

	public List<HashMap<String, String>> getDeviceVOIP(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select b.voip_id,b.prox_serv,b.prox_port,c.line_id,c.username from gw_voip_prof b,gw_voip_prof_line c where b.device_id=c.device_id and b.voip_id=c.voip_id and b.prof_id=c.prof_id and b.device_id=?");
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
	}

	public String getUserByDevice(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select user_id from tab_hgwcustomer where device_id=?");
		sql.setString(1, deviceId);
		List<HashMap<String, String>> list = DBOperation.getRecords(sql
				.getSQL());
		if (list.size() > 0) {
			Map<String, String> map = (Map<String, String>) list.get(0);
			return StringUtil.getStringValue(map.get("user_id"));
		} else {
			return null;
		}
	}

	/**
	 * 获取所有的session信息
	 * 
	 * @param device_id
	 */
	public List<HashMap<String, String>> getAllChannel(String device_id) {// 所有字段都要用
		logger.debug("getAllChannel({})", device_id);
		String table_gw_wan_conn_session = "gw_wan_conn_session";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_gw_wan_conn_session = "gw_wan_conn_session_bbms";
		}
		String sql = "select * from  "+table_gw_wan_conn_session+" where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		List<HashMap<String, String>> list = DBOperation.getRecords(psql
				.getSQL());
		return list;
	}

	/**
	 * 获取voip line信息
	 * 
	 * @param device_id
	 * @return
	 */
	public List<HashMap<String, String>> getVoipLineInfo(String device_id) {
		logger.debug("getVoipLineInfo({})", device_id);
		String sql = "select line_id, status,username,password from "
				+ StringUtil.getStringValue(Global.G_TABLENAME_MAP,
						"gw_voip_prof_line") + " where device_id=?";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			sql = "select line_id, status,username,password from  gw_voip_prof_line_bbms where device_id=?";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		List<HashMap<String, String>> list = DBOperation.getRecords(psql
				.getSQL());
		return list;
	}

	/**
	 * 获取PON口信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getPonInfo(String deviceId) {// 所有字段都要用
		logger.debug("getPonInfo({})", deviceId);
		String sql = "select * from "
				+ StringUtil.getStringValue(Global.G_TABLENAME_MAP,
						"gw_wan_wireinfo_epon")
				+ " where device_id=? and wan_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		psql.setInt(2, 1);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 获取LAN信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getLanInfos(String deviceId) {
		logger.debug("getLanInfos({})", deviceId);
		String sql = "select device_id,lan_id,lan_eth_id,status,byte_sent,byte_rece from "
				+ StringUtil.getStringValue(Global.G_TABLENAME_MAP,
						"gw_lan_eth") + "  where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 获取LAN侧信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryLanEth(String deviceId) {

		logger.debug("queryLanEth(deviceId:{})", deviceId);

		StringBuffer sql = new StringBuffer();
		sql.append("select lan_eth_id,status,max_bit_rate from gw_lan_eth where device_id='");
		sql.append(deviceId);
		sql.append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 获取WLAN信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getWlanInfosforPortPon(String deviceId) {
		logger.debug("getWlanInfos({})", deviceId);
		String sql = "select conn_status from "
				+ StringUtil.getStringValue(Global.G_TABLENAME_MAP,
						"gw_wan_conn_session") + " where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 获取WLAN信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getWlanInfos(String deviceId) {
		logger.debug("getWlanInfos({})", deviceId);
		String sql = "select conn_status from "
				+ StringUtil.getStringValue(Global.G_TABLENAME_MAP,
						"gw_wan_conn_session") + " where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * init static src
	 * 
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getStaticSrc() {
		logger.debug("getStaticSrc()");

		String sql = "select src_type,src_code,src_key,src_value from tab_static_src order by src_type,src_code ";
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.getRecords(psql.getSQL());
	}

	public ArrayList<HashMap<String, String>> getVoipInfos(String deviceId) {
		String sql = "select a.line_id,a.status,b.pending_timer_init,b.retran_interval_timer from"
				+ " gw_voip_prof_line a,gw_voip_prof_h248 b where a.device_id = b.device_id and a.device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 快速采集单宽带 Internet 相关信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getInternetSingle(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select b.username,b.password,a.vlan_id,b.bind_port,b.sess_type,b.ip_type,b.conn_type,b.ip,b.conn_status,b.serv_list,b.dns,b.nat_enab "
						+ "from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='INTERNET' and a.device_id=?");
		sql.setString(1, deviceId);
		return DBOperation.getRecord(sql.getSQL());
	}

	public Map<String, String> getWanInterface(String deviceId, String vlanId) {

		String table_gw_wan_conn = "gw_wan_conn";
		String table_gw_wan_conn_session = "gw_wan_conn_session";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_gw_wan_conn = "gw_wan_conn_bbms";
			table_gw_wan_conn_session = "gw_wan_conn_session_bbms";
		}
		
		PrepareSQL sql = new PrepareSQL(
				"select  a.wan_conn_id,b.wan_conn_sess_id,b.sess_type,b.conn_type,b.ip from " + table_gw_wan_conn + " a," + table_gw_wan_conn_session + " b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and a.device_id=? and a.vlan_id=?");
		sql.setString(1, deviceId);
		sql.setString(2, vlanId);
		return DBOperation.getRecord(sql.getSQL());

	}

	/*
	 * @author chenxj6
	 * 
	 * @date 20161008
	 * 
	 * @param serv_list , XJ
	 */
	public Map<String, String> getWanInterfaceXJ(String deviceId,
			String serv_list) {

		PrepareSQL sql = new PrepareSQL(
				"select  a.wan_conn_id,b.wan_conn_sess_id,b.sess_type,b.conn_type,b.ip from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and a.device_id=? and b.serv_list=?");
		sql.setString(1, deviceId);
		sql.setString(2, serv_list);
		return DBOperation.getRecord(sql.getSQL());

	}

}
