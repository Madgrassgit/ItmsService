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
import com.linkage.commons.util.TimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.obj.CloudOpenVXLANCRMChecker;

public class IpsecServParamDAO {

	private static Logger logger = LoggerFactory.getLogger(IpsecServParamDAO.class);
	
	/**
	 * 业务状态查询
	 * @param userId
	 * @return
	 */
	public String queryServStatus(Long userId) {
		String sql = "select serv_status from tab_ipsec_serv_param where user_id=? and serv_type_id =27 ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "serv_status");
	}
	
	/**
	 * vxlan业务状态查询
	 * @param userId
	 * @return
	 */
	public String queryVxlanServStatus(Long userId) {
		String sql = "select serv_status from tab_vxlan_serv_param where user_id=? and serv_type_id =29 order by updatetime desc";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "serv_status");
	}

	/**
	 * 查询工单表记录数
	 * @param userId
	 * @return
	 */
	public int queryIpsecServCount(Long userId) {
		String sql = "select count(1) as num from tab_ipsec_serv_param where user_id=? and serv_type_id =27 ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}

	/**
	 * 查询工单表记录数
	 * @param userId
	 * @return
	 */
	public int queryVxlanServCount(Long userId) {
		String sql = "select count(1) as num from tab_vxlan_serv_param where user_id=? and serv_type_id =29 ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}

	/**
	 * 查询最新的vxlan业务 
	 * @param userId
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryVxlanConfigSequence(Long userId) {
		String sql = "select vxlanconfigsequence from tab_vxlan_serv_param where user_id=? and serv_type_id =29 order by updatetime desc";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}

	/**
	 * 查询开通的vxlan业务
	 * @param userId
	 * @return
	 */
	public int queryIsVxlan(Long userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(1) as num from tab_vxlan_serv_param ");
		sb.append("where user_id = ? and serv_type_id = 29 ");
		sb.append("and open_status <> 1");
		PrepareSQL psql = new PrepareSQL(sb.toString());
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}
	
	/**
	 * 查询vxlan业务
	 * @param userId
	 * @return
	 */
	public int queryVxlanServ(Long userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(1) as num from tab_vxlan_serv_param ");
		sb.append("where user_id = ? and serv_type_id = 29 ");
		PrepareSQL psql = new PrepareSQL(sb.toString());
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}

	/**
	 * 设置北向云网关开通状态为未做
	 * @param userId
	 * @return
	 */
	public int updateServStatus(Long userId) {
		logger.debug("updateServStatus({})", userId);

		// 更新SQL语句
		String strSQL = "update tab_ipsec_serv_param set open_status=0 ,serv_status=1,enable=1  "
				+ " where user_id=? and serv_type_id =27";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);

		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 设置北向云网关开通状态为未做
	 * @param userId
	 * @return
	 */
	public int updateVxlanServStatus(Long userId, int vxlanconfigsequence, String requestId) {
		logger.debug("updateVxlanServStatus({})", userId);

		String strSQL = "update tab_vxlan_serv_param set open_status = 0, serv_status = 1 , request_id = ?"
				+ " where user_id = ? and serv_type_id = 29 and vxlanconfigsequence = ?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, requestId);
		psql.setLong(2, userId);
		psql.setInt(3, vxlanconfigsequence);
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 是否有IPSecVpn业务
	 * @param userId
	 * @return
	 */
	public String queryEnable(Long userId) {
		String sql = "select enable from tab_ipsec_serv_param where user_id=? and serv_type_id =27 ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "enable");
	}
	
	/**
	 * 是否有vxlan业务
	 * @param userId
	 * @return
	 */
	public String queryVxlanEnable(Long userId) {
		String sql = "select serv_status from tab_vxlan_serv_param where user_id=? and serv_type_id =29 ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "serv_status");
	}

	/**
	 * 是否有宽带业务业务
	 * @param userId
	 * @return
	 */
	public String queryIsNet(Long userId) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		String sql = "select count(1) as num from " + table_serv_info + " where serv_type_id=10 and user_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		int num = StringUtil.getIntValue(map, "num");
		return num > 0 ? "1" : "0";
	}
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public String queryWanType(Long userId) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		String sql = "select wan_type from " + table_serv_info + " where user_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "wan_type");
	}
	
	/**
	 * 在线状态查询
	 * @param userId
	 * @return
	 */
	public String queryOnline(String deviceId) {
		String sql = "select online_status from gw_devicestatus where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		String ret = StringUtil.getStringValue(map, "online_status");
		if (!"1".equals(ret)) {
			return "-1";
		}
		return ret;
	}
	
	/**
	 * Ip类型查询
	 * @param userId
	 * @return
	 */
	public String queryIpType(Long userId) {
		String sql = "select ip_type from tab_net_serv_param where user_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "ip_type");
	}
	
	/**
	 * 获取采集的internet Ip信息
	 * 
	 * @param device_id
	 */
	public String getInternetIp(String device_id) {
		String table_gw_wan_conn_session = "gw_wan_conn_session";
		if ("BBMS".equals(Global.SYSTEM_NAME)) {
			table_gw_wan_conn_session = "gw_wan_conn_session_bbms";
		}
		String sql = "select ip from  " + table_gw_wan_conn_session + " where " +
				"serv_list = 'INTERNET' and device_id=? and rownum < 2";
		
		if (3 == DBUtil.GetDB()) {
			sql = "select ip from  " + table_gw_wan_conn_session + " where " +
					"serv_list = 'INTERNET' and device_id=? limit 1";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map, "ip");
	}

	/**
	 * 省服开开通vxlan工单中判断用户是否存在
	 * 
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> custIsExists(String loid) {
		String table_customer = "tab_egwcustomer";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_customer = "tab_hgwcustomer";
		}

		StringBuffer sbsql = new StringBuffer("select * from ")
			.append(table_customer)
			.append(" where (user_state='1' or user_state='2')")
			.append("and username=?");

		PrepareSQL pSql = new PrepareSQL(sbsql.toString());
		pSql.setString(1, loid);
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	/**
	 * 省服开开通vxlan工单中判断vxlan业务是否存在
	 * @param userId
	 * @return
	 */
	public boolean servIsExists(long userId, int servTypeId) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}

		StringBuffer sbsql = new StringBuffer("select 1 from ")
			.append(table_serv_info)
			.append(" where user_id = ? and serv_type_id = ? ");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);

		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map != null && map.size() > 0;
	}
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, String> getServInfo(long userId, int servTypeId) {
		String table_serv_info = "egwcust_serv_info";
		String table_customer = "tab_egwcustomer";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
			table_customer = "tab_hgwcustomer";
		}
		
		StringBuffer sbsql = new StringBuffer("select a.open_status, b.device_id from ")
			.append(table_serv_info).append(" a,")
			.append(table_customer).append(" b")
			.append(" where a.user_id = b.user_id and a.user_id = ? and a.serv_type_id = ? and a.serv_status = 1");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);

		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 省服开开通vxlan工单中判断vxlan业务关联表是否存在
	 * @param userId
	 * @return
	 */
	public boolean busIsExists(Long userId) {
		String table = "itms_bssuser_info";

		StringBuffer sbsql = new StringBuffer("select 1 from ")
			.append(table)
			.append(" where user_id = ?");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);

		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map != null && map.size() > 0;
	}

	/**
	 * 根据用户宽带账号查询用户信息(北向云网关接口)
	 * 
	 * @param netAccount
	 * @return
	 */
	public String saveServInfo(Long userId, CloudOpenVXLANCRMChecker checker) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		StringBuffer addSql = new StringBuffer("insert into ")
			.append(table_serv_info)
			.append(" (user_id, serv_type_id, username, serv_status, wan_type, ");
		if ("js_dx".equals(Global.G_instArea)) {
			addSql.append(" open_status, ip_type, vlanid, dealdate, opendate, updatetime, real_type_id)")
			.append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 45)");
		}
		else {
			addSql.append(" open_status, ip_type, vlanid, dealdate, opendate, updatetime)")
			.append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}

		int index = 0;
		PrepareSQL pSql = new PrepareSQL(addSql.toString());
		pSql.setLong(++index, userId);
		pSql.setInt(++index, 45);
		pSql.setString(++index, checker.getUserInfo());
		pSql.setInt(++index, 1);
		pSql.setInt(++index, Integer.parseInt(checker.getWanType()));
		pSql.setInt(++index, 1);
		pSql.setInt(++index, 1);
		//pSql.setInt(++index, 0);
		pSql.setString(++index, checker.getVlanId());
		pSql.setLong(++index, TimeUtil.GetCalendar(checker.getDealDate()).getTimeInMillis() / 1000);
		pSql.setLong(++index, System.currentTimeMillis()/1000);
		pSql.setLong(++index, System.currentTimeMillis()/1000);
		
		return pSql.getSQL();
	}
	
	/**
	 * 
	 * @param userId
	 * @param checker
	 * @return
	 */
	public String updateServInfo(Long userId, CloudOpenVXLANCRMChecker checker) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		StringBuffer addSql = new StringBuffer("update ")
			.append(table_serv_info)
			.append(" set username = ?, wan_type = ?, vlanid = ?, dealdate = ?, updatetime = ?,")
			.append(" serv_status = 1, open_status = 1 where user_id = ? and serv_type_id = 45");

		int index = 0;
		PrepareSQL pSql = new PrepareSQL(addSql.toString());
		pSql.setString(++index, checker.getUserInfo());
		pSql.setInt(++index, Integer.parseInt(checker.getWanType()));
		pSql.setString(++index, checker.getVlanId());
		pSql.setLong(++index, TimeUtil.GetCalendar(checker.getDealDate()).getTimeInMillis() / 1000);
		pSql.setLong(++index, System.currentTimeMillis()/1000);
		pSql.setLong(++index, userId);
		
		return pSql.getSQL();
	}
	
	/**
	 * 
	 * @param userId
	 * @param checker
	 * @return
	 */
	public String saveBusInfo(Long userId, CloudOpenVXLANCRMChecker checker) {
		String table = "itms_bssuser_info";
		
		StringBuffer addSql = new StringBuffer("insert into ")
			.append(table)
			.append(" (user_id, city_code, prov_code, contact, detail)")
			.append(" values(?, ?, ?, ?, ?)");

		int index = 0;
		PrepareSQL pSql = new PrepareSQL(addSql.toString());
		pSql.setLong(++index, userId);
		pSql.setString(++index, checker.getCityId());
		pSql.setString(++index, "00");
		pSql.setString(++index, checker.getRequId());
		pSql.setString(++index, checker.getProInstId());
		
		return pSql.getSQL();
	}
	
	/**
	 * 
	 * @param userId
	 * @param checker
	 * @return
	 */
	public String updateBusInfo(Long userId, CloudOpenVXLANCRMChecker checker) {
		String table = "itms_bssuser_info";
		
		StringBuffer addSql = new StringBuffer("update ")
			.append(table)
			.append(" set city_code = ?, contact = ?, detail = ? where user_id = ?");

		int index = 0;
		PrepareSQL pSql = new PrepareSQL(addSql.toString());
		pSql.setString(++index, checker.getCityId());
		pSql.setString(++index, checker.getRequId());
		pSql.setString(++index, checker.getProInstId());
		pSql.setLong(++index, userId);
		
		return pSql.getSQL();
	}
	
	/**
	 * 
	 * @param userId
	 * @param servTypeId
	 * @return
	 */
	public int updateServOpenStatus(long userId, int servTypeId) {
		logger.debug("updateServOpenStatus({}, {})", userId, servTypeId);
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}

		// 更新SQL语句
		String strSQL = "update " + table_serv_info + " set open_status=0 "
				+ " where serv_status=1 and open_status!=0 and user_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);
		if (0 != servTypeId) {
			psql.append(" and serv_type_id=" + servTypeId);
		}

		// 执行查询
		int reti = DBOperation.executeUpdate(psql.getSQL());

		return reti;
	}
	
	/**
	 * @param userId
	 * @param servTypeId
	 * @return
	 */
	public int updateVxlanServOpenStatus(long userId) {
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}

		// 更新SQL语句
		String strSQL = "update " + table_serv_info + " set open_status=1 "
				+ " where user_id = ? and serv_type_id = 45";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);

		// 执行查询
		int reti = DBOperation.executeUpdate(psql.getSQL());

		return reti;
	}
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, String> getSerBusInfo(Long userId) {
		String table_serv_info = "egwcust_serv_info";
		String table_bss = "itms_bssuser_info";
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		
		StringBuffer sbsql = new StringBuffer("select a.user_id, b.contact, b.detail,a.open_status from ")
		.append(table_serv_info).append(" a, ")
		.append(table_bss).append(" b ")
		.append(" where a.user_id = b.user_id and a.serv_status = 1 and a.serv_type_id = 45")
		.append(" and a.user_id = ?");
		PrepareSQL pSql = new PrepareSQL(sbsql.toString());
		pSql.setLong(1, userId);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	/**
	 * 
	 * @param userId
	 * @param pubIpv4
	 * @return
	 */
	public List<String> getNatExistPubIP(Long userId, String pubIpv4) {
		String table_nat = "tab_vxlan_nat_config";
	
		StringBuffer sbsql = new StringBuffer("select user_id, pub_ipv4 from ")
		.append(table_nat)
		.append(" where user_id = ? and pub_ipv4  in (" + pubIpv4 + ")");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		List<String> retlist = new ArrayList<String>();
		for (HashMap<String, String> map : list) {
			retlist.add(StringUtil.getStringValue(map, "pub_ipv4"));
		}
		return retlist;
	}
	
	/**
	 * 
	 * @param userId
	 * @param pubIpv4
	 */
	public void updateFailNatData(Long userId, String pubIpv4) {
		String table_nat = "tab_vxlan_nat_config";
	
		StringBuffer sbsql = new StringBuffer("update ")
		.append(table_nat)
		.append(" set state = 0 where user_id = ? and pub_ipv4  in (" + pubIpv4 + ")");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 
	 * @param userId
	 * @param pubIpv4
	 */
	public void deleteNatData(Long userId, String pubIpv4) {
		String table_nat = "tab_vxlan_nat_config";
	
		StringBuffer sbsql = new StringBuffer("delete from ")
		.append(table_nat)
		.append(" where user_id = ?");
		if (!"all".equals(pubIpv4)) {
			sbsql.append(" and pub_ipv4 = '").append(pubIpv4).append("'");
		}
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 
	 * @param userId
	 * @param nextHop
	 * @param desIp
	 * @return
	 */
	public Map<String, String> getForwardingExistPubIP(Long userId, String nextHop, String desIp) {
		String table_forwarding = "tab_vxlan_forwarding_config";
	
		StringBuffer sbsql = new StringBuffer("select user_id, rt_id, next_hop, des_ip, priority, state from ")
		.append(table_forwarding)
		.append(" where user_id = ? and next_hop = ? and des_ip = ?");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		psql.setString(2, nextHop);
		psql.setString(3, desIp);
		
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		
		return map;
	}
	
	/**
	 * 
	 * @param userId
	 * @param nextHop
	 * @param desIp
	 */
	public void updateFailForwardingData(Long userId, String nextHop, String desIp) {
		String table_forwarding = "tab_vxlan_forwarding_config";
	
		StringBuffer sbsql = new StringBuffer("update ")
		.append(table_forwarding)
		.append(" set state = 0 where user_id = ? and next_hop = ? and des_ip = ?");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		psql.setString(2, nextHop);
		psql.setString(3, desIp);
		
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 
	 * @param userId
	 * @param nextHop
	 * @param desIp
	 */
	public void deleteForwardingData(Long userId, String nextHop, String desIp) {
		String table_forwarding = "tab_vxlan_forwarding_config";
	
		StringBuffer sbsql = new StringBuffer("delete from ")
		.append(table_forwarding)
		.append(" where user_id = ? and next_hop = ? and des_ip = ?");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		psql.setString(2, nextHop);
		psql.setString(3, desIp);
		
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 查询是否有未开通的业务，用于重复注册/移机换机时先等其他业务下发成功后，再下发vxlan业务
	 * @param userId
	 * @return
	 */
	public int queryIsServ(Long userId) {
		String table_serv_info = "egwcust_serv_info";
		if(1 == Global.IPSEC_ISFUSED){
			table_serv_info = "hgwcust_serv_info";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("select count(1) as num from ").append(table_serv_info);
		sb.append(" where user_id = ? and open_status <> 1");
		PrepareSQL psql = new PrepareSQL(sb.toString());
		psql.setLong(1, userId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}
	
	/**
	 * 
	 * @param userId
	 * @param nextHop
	 * @param desIp
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getForwardingIP(Long userId) {
		String table_forwarding = "tab_vxlan_forwarding_config";
		StringBuffer sbsql = new StringBuffer("select user_id, rt_id, next_hop, des_ip, priority, state from ")
		.append(table_forwarding)
		.append(" where user_id = ? ");
		PrepareSQL psql = new PrepareSQL(sbsql.toString());
		psql.setLong(1, userId);
		return DBOperation.getRecords(psql.getSQL());
	}
}
