
package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyl (Ailk No.)
 * @version 1.0
 * @since 2015-3-10
 */
public class QueryDevDAO
{

	private static final Logger logger = LoggerFactory.getLogger(QueryDevDAO.class);
	
	
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		logger.debug("QueryDevDAO-->queryUserByLoid({})", loid);
		String subUserName = loid.substring(loid.length()-6);
		String sql = "select distinct(a.user_id),a.device_id,a.updatetime from tab_hgwcustomer a left join " +
				" hgwcust_serv_info b on a.user_id = b.user_id where a.username= ?  order by a.updatetime desc ";
		if ("ah_dx".equals(Global.G_instArea))
		{
			sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a left join " +
					" hgwcust_serv_info b on a.user_id = b.user_id where a.username= ?";
		}
		//河北会传设备序列号 而且可能存在多条一样的业务数据
		if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
			sql = "select * from (select  a.user_id,a.device_id,a.updatetime from tab_hgwcustomer a left join " +
					" hgwcust_serv_info b on a.user_id = b.user_id where " +
					" a.username like '%" + loid + "' and a.user_sub_name ='"+ subUserName+ "'  order by a.updatetime desc) where rownum<2";
			// mysql db
			if (3 == DBUtil.GetDB()) {
				sql = "select  a.user_id,a.device_id,a.updatetime from tab_hgwcustomer a left join " +
						" hgwcust_serv_info b on a.user_id = b.user_id where " +
						" a.username like '%" + loid + "' and a.user_sub_name ='"+ subUserName+ "'  order by a.updatetime desc limit 1";
			}
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息(北向云网关接口)
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoidCloud(String loid)
	{
		String table_customer = "tab_egwcustomer";
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_customer = "tab_hgwcustomer";
			table_serv_info = "hgwcust_serv_info";
		}

		logger.debug("QueryDevDAO-->queryUserByLoid({})", loid);
		String sql = "select a.user_id, a.device_id, a.username, a.oui, a.device_serialnumber, b.wan_type, b.username as account, b.open_status " +
				" from " + table_customer + " a" +" left join "+table_serv_info+" b on (a.user_id=b.user_id) "+
				" where a.username= ? and b.serv_type_id=10 order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 查询BindPort
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryBindPort(String user_id)
	{
		logger.debug("QueryDevDAO-->queryUserByLoid({})", user_id);
		String sql = "select distinct bind_port from hgwcust_serv_info where serv_type_id = 11 and user_id=? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setLong(1, StringUtil.getLongValue(user_id));
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @param cityIDs
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid,
			ArrayList<String> cityIDs)
	{
		logger.debug("QueryDevDAO-->queryUserByLoid({},{})", loid, cityIDs);
		String sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a where a.username= ? "
				+ "and a.city_id in ("
				+ com.linkage.itms.commom.StringUtil.weave(cityIDs)
				+ ") order by a.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户宽带账号查询用户信息
	 * 
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, a.username, b.device_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户宽带账号查询用户信息(北向云网关接口)
	 * 
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccountCloud(String netAccount)
	{
		String table_customer = "tab_egwcustomer";
		String table_serv_info = "egwcust_serv_info";
		
		if(1 == Global.IPSEC_ISFUSED){
			table_customer = "tab_hgwcustomer";
			table_serv_info = "hgwcust_serv_info";
		}

		String sql = "select a.user_id, b.device_id, b.username,b.oui, b.device_serialnumber,a.wan_type ,a.username as account "
				+ " from " + table_serv_info + " a, " + table_customer + " b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户宽带账号查询用户信息
	 * 
	 * @param netAccount
	 * @param cityId
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount,
			ArrayList<String> cityIDs)
	{
		String sql = "select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=? and "
				+ " b.city_id in ("
				+ com.linkage.itms.commom.StringUtil.weave(cityIDs)
				+ ") order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户IPTV宽带账号查询用户信息
	 * 
	 * @param iptvAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByIptvAccount(String iptvAccount)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, iptvAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户IPTV宽带账号查询用户信息
	 * 
	 * @param iptvAccount
	 * @param cityId
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByIptvAccount(String iptvAccount,
			ArrayList<String> cityIDs)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=? "
				+ "and b.city_id in ("
				+ com.linkage.itms.commom.StringUtil.weave(cityIDs)
				+ ") order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, iptvAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * 
	 * @param voipPhone
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipPhone(String voipPhone)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and voip_phone = ?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipPhone);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * 
	 * @param voipPhone
	 * @param cityId
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipPhone(String voipPhone,
			ArrayList<String> cityIDs)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.voip_phone = ? "
				+ "and b.city_id in ("
				+ com.linkage.itms.commom.StringUtil.weave(cityIDs)
				+ ") order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipPhone);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据VOIP认证账号查询用户信息
	 * 
	 * @param voipAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipAccount(String voipAccount)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and voip_username = ?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	/**
	 * 根据VOIP认证账号查询用户信息
	 * 
	 * @param voipAccount
	 * @param cityId
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipAccount(String voipAccount,
			ArrayList<String> cityIDs)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.voip_username = ? "
				+ "and b.city_id in ("
				+ com.linkage.itms.commom.StringUtil.weave(cityIDs)
				+ ") order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户设备SN查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByDevSN(String devSN, String devSubSN)
	{
		logger.warn("QueryDevDAO-->queryUserByDevSN({})", devSN);
		String sql = "select a.device_id,a.cpe_mac,a.customer_id user_id from tab_gw_device a where a.device_status = 1 and a.dev_sub_sn = ? and a.device_serialnumber like '%"
				+ devSN + "%'";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devSubSN);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", devSN, result);
		return result;
	}

	/**
	 * 根据用户设备SN查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserInfoByDevSN(String devSN,
			String devSubSN)
	{
		String table_customer = "tab_hgwcustomer";
		if ("BBMS".equals(Global.SYSTEM_NAME))
		{
			table_customer = "tab_egwcustomer";
		}
		String sql = "select a.device_id,b.user_id,b.username from tab_gw_device a,"+table_customer+
				" b where a.customer_id = b.user_id and a.device_status = 1 and a.dev_sub_sn = ? and a.device_serialnumber like '%"
				+ devSN + "%'";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devSubSN);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
/**
 * 根据用户设备SN查询用户信息
 * @param devSN
 * @param devSubSN
 * @return
 */
	public List<HashMap<String, String>> queryUserInfoByDevSNJX(String devSN,
			String devSubSN)
	{
		String table_customer = "tab_hgwcustomer";
		String sql = "select a.device_id,b.user_id,b.username from tab_gw_device a,"+table_customer+
				" b where a.device_id = b.device_id and a.device_status = 1 and a.device_serialnumber like '%"+devSubSN+"%' and a.dev_sub_sn=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devSN);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	
	
	/**
	 * 获取宽带对应的vlan
	 * 
	 * @param voipAccount
	 * @return
	 */
	public Map<String, String> queryVlanId(String userId)
	{
		String sql = "select wan_type,vlanid " + " from hgwcust_serv_info "
				+ " where user_id = ? and serv_type_id = 10 ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setInt(1, StringUtil.getIntegerValue(userId));
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", userId, result);
		return result;
	}

	/**
	 * 获取宽带对应的vlans
	 * 
	 * @param voipAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryWanTypeAndVlanIds(String userId)
	{
		String sql = "select wan_type,vlanid " + " from hgwcust_serv_info "
				+ " where user_id = ? and serv_type_id = 10 ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setInt(1, StringUtil.getIntegerValue(userId));
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", userId, result);
		return result;
	}

	/**
	 * 获取上行方式
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, String> queryAccessType(String userId)
	{
		String sql = "select adsl_hl " + " from tab_hgwcustomer " + " where user_id = ? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setInt(1, StringUtil.getIntegerValue(userId));
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", userId, result);
		return result;
	}

	/**
	 * 获取设备mac地址
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> queryDeviceMac(String deviceId)
	{
		String sql = "select cpe_mac ,device_serialnumber " + " from tab_gw_device " + " where device_id = ? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", deviceId, result);
		return result;
	}

	public void doConfig(long userId, String deviceId, String serviceId,
			String strategy_type, String vlanIdMark, String ssid, long time,
			int wireless_port, int buss_level)
	{
		ArrayList<String> sqllist = new ArrayList<String>();
		//
		PrepareSQL psql = new PrepareSQL(
				"insert into tab_wirelesst_task (task_id,acc_oid,add_time,service_id,vlan_id,ssid,strategy_type,wireless_port,buss_level,wireless_type) values(?,?,?,?,?,?,?,?,?,?)");
		psql.setLong(1, time);
		psql.setLong(2, userId);
		psql.setLong(3, time);
		psql.setInt(4, StringUtil.getIntegerValue(serviceId));
		psql.setInt(5, StringUtil.getIntegerValue(vlanIdMark));
		psql.setString(6, ssid);
		psql.setString(7, strategy_type);
		psql.setInt(8, wireless_port);
		psql.setInt(9, buss_level);
		if(wireless_port == 4){
			psql.setInt(10, 1);
		}else{
			psql.setInt(10, 2);
		}
		
		sqllist.add(psql.getSQL());
		String deviceIds = deviceId;
		StringBuffer sb = new StringBuffer();
		sb.append("select a.device_id ,a.oui,a.device_serialnumber,b.username as loid from tab_gw_device a ,tab_hgwcustomer b ");
		sb.append(" where a.device_id = b.device_id ");
		sb.append(" and a.device_id in('");
		sb.append(deviceIds).append("')");
		PrepareSQL sql = new PrepareSQL(sb.toString());
		List<HashMap<String, String>> lt = DBOperation.getRecords(sql.getSQL());
		if (null != lt && lt.size() > 0)
		{
			for (Map<String, String> map : lt)
			{
				StringBuffer insertSql = new StringBuffer();
				insertSql.append(" insert tab_wirelesst_task_dev values(");
				insertSql.append(time).append(",'");
				insertSql.append(StringUtil.getStringValue(map.get("device_id"))).append(
						"','");
				insertSql.append(StringUtil.getStringValue(map.get("oui"))).append("','");
				insertSql.append(
						StringUtil.getStringValue(map.get("device_serialnumber")))
						.append("','");
				insertSql.append(StringUtil.getStringValue(map.get("loid"))).append("',");
				insertSql.append(0).append(",");
				insertSql.append(0).append(")");
				sqllist.add(insertSql.toString());
			}
		}
		DBOperation.executeUpdate(sqllist.toArray(new String[0]));
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid2(String loid)
	{
		logger.debug("QueryDevDAO-->queryUserByLoid({})", loid);
		String sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id in (10,11) and a.username= ?  order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	/**
	 * 查询设备LOOPBACK_ip
	 * 
	 * @param devId
	 * @return
	 */
	public String queryLoopBackIp(String devId)
	{
		String sql = "select loopback_ip from tab_gw_device " + " where device_id = ? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devId);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		return StringUtil.getStringValue(result,
				"loopback_ip");
	}
	
	/**
	 * 查询tab_version_rate和tab_gw_device获取设备的LAN口连接速率
	 * 
	 * @param devId
	 * @return
	 */
	public Map<String, String> queryVersionRate(String deviceId)
	{
		String sql = "select v.* from tab_version_rate v,tab_gw_device d where d.device_id=? and d.vendor_id=v.vendor_id and d.device_model_id = v.device_model_id";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select v.ismaxbitrate,v.lan1,v.lan2,v.lan3,v.lan4 from tab_version_rate v,tab_gw_device d where d.device_id=? and d.vendor_id=v.vendor_id and d.device_model_id = v.device_model_id";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", deviceId, result);
		return result;
	}
	
	
	
	/**
	 * 根据设备SN后6位查询用户信息
	 * @param devSN
	 * @param devSubSN
	 * @return
	 */
	public List<HashMap<String, String>> queryUserInfoByDevSNNX(String devSN)
	{
		String table_customer = "tab_hgwcustomer";
		String sql = "select a.device_id,b.user_id,b.username from tab_gw_device a,"+table_customer+
				" b where a.device_id = b.device_id and a.device_status = 1 and a.device_serialnumber like '%"+devSN+"'";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 查询设备解绑信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryDevRegInfo(String loid) {
		String sql = "select a.device_serialnumber, c.device_model, b.binddate " + 
		  " from tab_gw_device a, bind_log b, gw_device_model c " + 
		  " where a.device_id = b.device_id and a.device_model_id = c.device_model_id " + 
		  " and b.oper_type=2 and b.username=? order by binddate desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}


	/**
	 * 根据mac查询设备
	 * 2020/10/30
	 * @param mac
	 * @return
	 */
	public List<HashMap<String, String>> queryDevByMac(String mac)
	{
		String sql = "select device_id, device_serialnumber, cpe_mac, customer_id from tab_gw_device where cpe_mac = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, mac);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户id查用户信息
	 * @param customer_id
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByCustomerId(String customer_id) 
	{
		String sql = "select a.user_id, a.username, b.device_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.user_id=?  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, customer_id);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
}
