package com.linkage.itms.dao;

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
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-5
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DeviceConfigNewDAO
{
	private static Logger logger = LoggerFactory.getLogger(DeviceConfigNewDAO.class);
	/**
	 * 快速采集 Internet 相关信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getInternet(String deviceId) {
		PrepareSQL sql = new PrepareSQL(
				"select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port,b.sess_type,b.ip_type,b.conn_type,b.conn_status from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='INTERNET' and a.device_id=?");
		if("BBMS".equals(Global.SYSTEM_NAME)){
			sql = new PrepareSQL(
					"select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port,b.sess_type,b.ip_type,b.conn_type from gw_wan_conn_bbms a,gw_wan_conn_session_bbms b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='INTERNET' and a.device_id=?");
		}
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
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
			psql.append("select a.vpi_id,a.vci_id,a.vlan_id,b.bind_port,b.conn_status,b.multicast_vlan from gw_wan_conn a,gw_wan_conn_session b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id ");
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
					"select a.vpi_id,a.vci_id,a.vlan_id,b.conn_type,b.bind_port,b.ip_type,b.sess_type,b.conn_status from gw_wan_conn_bbms a,gw_wan_conn_session_bbms b where a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id and b.serv_list='VOIP' and a.device_id=?");
		}
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
	}
	public List<HashMap<String, String>> getLink_status(String deviceId) {
		PrepareSQL sql=new PrepareSQL("select link_status from gw_wan_conn_bbms where device_id=?");
		sql.setString(1, deviceId);
		return DBOperation.getRecords(sql.getSQL());
	}
	/**
	 * VOIP 线路信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> getVoipLines(String deviceId) {
		logger.debug("DeviceConfigDAO==>getVoipLines({})",
				new Object[] { deviceId });
		PrepareSQL psql = new PrepareSQL();
		
		if (3 == DBUtil.GetDB()) {
			if("BBMS".equals(Global.SYSTEM_NAME)){
				psql.append("select username from gw_voip_prof_line_bbms where 1=1 and device_id = '"
						+ deviceId + "' ");
			}else{
				psql.append("select username from gw_voip_prof_line where 1=1 and device_id = '"
						+ deviceId + "' ");
			}
		}
		else {
			if("BBMS".equals(Global.SYSTEM_NAME)){
				psql.append("select * from gw_voip_prof_line_bbms where 1=1 and device_id = '"
						+ deviceId + "' ");
			}else{
				psql.append("select * from gw_voip_prof_line where 1=1 and device_id = '"
						+ deviceId + "' ");
			}
		}
		
		psql.append(" order by device_id, line_id ");
		return DBOperation.getRecords(psql.getSQL());
	}
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
}
