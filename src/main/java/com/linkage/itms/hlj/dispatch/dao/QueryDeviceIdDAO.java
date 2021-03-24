package com.linkage.itms.hlj.dispatch.dao;

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

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-28
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryDeviceIdDAO
{
	private static final Logger logger = LoggerFactory.getLogger(QueryDeviceIdDAO.class);
	
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		logger.debug("QueryDeviceIdDAO-->queryUserByLoid({})", loid);
		String sql = "select a.user_id,a.device_id,b.username,b.open_status,a.city_id,a.username loid from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 and a.username= ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息(政企)
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoidQiye(String loid)
	{
		logger.debug("QueryDeviceIdDAO-->queryUserByLoidQiye({})", loid);
		String sql = "select a.user_id,a.device_id,b.username,b.open_status,a.city_id,a.username loid from tab_egwcustomer a,egwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 and a.username= ?";
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
		String sql = "select a.user_id, b.device_id ,a.username,a.open_status,b.city_id,b.username loid "
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and b.user_state = '1' and a.username=? and b.device_id is not null  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户宽带账号查询用户信息(政企)
	 * 
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccountQiye(String netAccount)
	{
		String sql = "select a.user_id, b.device_id ,a.username,a.open_status,b.city_id,b.username loid "
				+ " from egwcust_serv_info a, tab_egwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and b.user_state = '1' and a.username=? and b.device_id is not null  order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户设备SN查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryDevice(String devSN)
	{
		logger.warn("QueryDeviceIdDAO-->queryUserByDevSN({})", devSN);
		String sql = "select  a.device_id,b.username loid " +
				" from tab_gw_device a left join tab_hgwcustomer b on a.device_id = b.device_id where a.device_serialnumber like '%" + devSN + "'";
		PrepareSQL pSql = new PrepareSQL(sql);
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
	public List<HashMap<String, String>> queryUserByDevSN(String devSN)
	{
		logger.warn("QueryDeviceIdDAO-->queryUserByDevSN({})", devSN);
	
		String sql = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username loid, b.userline"
				+ " from tab_gw_device a left join  tab_hgwcustomer  b on a.device_id=b.device_id"
				+ " where a.dev_sub_sn='"
				+ devSN.substring(devSN.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSN + "'" ;
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", devSN, result);
		return result;
	}
	
	/**
	 * 根据用户设备SN查询用户信息(政企)
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByDevSNQiye(String devSN)
	{
		logger.warn("QueryDeviceIdDAO-->queryUserByDevSN({})", devSN);
	
		String sql = "select a.device_id, a.device_serialnumber, a.oui, a.cpe_allocatedstatus,"
				+ " a.city_id,a.x_com_passwd,a.devicetype_id, b.user_id, b.username loid, b.userline"
				+ " from tab_gw_device a left join  tab_egwcustomer  b on a.device_id=b.device_id"
				+ " where a.dev_sub_sn='"
				+ devSN.substring(devSN.length() - 6)
				+ "' and a.device_serialnumber like '%" + devSN + "'" ;
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", devSN, result);
		return result;
	}
	
	
	public List<HashMap<String, String>> queryDevByLoid(String loid)
	{
		logger.debug("QueryDeviceIdDAO-->queryUserByLoid({})", loid);
		String sql = "select  a.device_id,a.username loid from tab_hgwcustomer a where a.user_state = '1' and a.username= ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	public Map<String, String> queryDevStatus(String devId)
	{
		logger.debug("QueryDevDAO-->queryDevStatus({})", devId);
		String sql = "select  online_status from gw_devicestatus a where device_id = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devId);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	/**
	 * 根据deviceId查设备所有信息
	 * @author 岩 
	 * @date 2016-8-5
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> queryInfoByDevId(String deviceId)
	{
		logger.warn("QueryDeviceIdDAO-->queryInfoByDevId({})", deviceId);
		String sql = "select a.*,b.online_status  from tab_gw_device a,gw_devicestatus b where a.device_id =b.device_id and a.device_id ='"+deviceId+"'";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select a.cpe_currentupdatetime,a.loopback_ip,a.cpe_mac,b.online_status  from tab_gw_device a,gw_devicestatus b where a.device_id =b.device_id and a.device_id ='"+deviceId+"'";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		logger.warn("[{}]-[{}]", deviceId, result);
		return result;
	}
	
	/**
	 * ping测试根据mac地址查询device_id
	 * @author 岩 
	 * @date 2016-8-12
	 * @param mac
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByMac(String mac)
	{
		logger.debug("QueryDeviceIdDAO-->queryUserByMac({})", mac);
		String sql = "select a.device_id from tab_gw_device a where a.cpe_mac = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, mac);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	public List<HashMap<String, String>> queryDeviceByVoipPhone(String voipPhone) {
		logger.debug("queryDeviceByVoipPhone({})", voipPhone);
		if (StringUtil.IsEmpty(voipPhone)) {
			logger.error("voipPhone is Empty");
			return null;
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_id,b.username loid from tab_gw_device a left join tab_hgwcustomer b on a.device_id=b.device_id ");
		psql.append(" left join tab_voip_serv_param c on b.user_id=c.user_id where c.voip_phone='" + voipPhone + "'");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据用户的宽带账号查询宽带用户、终端信息
	 * @param
	 * @param userName
	 * @return
	 */
	public List<HashMap<String, String>> qryUserByKdName(String userName) {
		logger.debug("qryUserByKdName({}{})",new Object[]{userName});
		if (StringUtil.IsEmpty(userName)) {
			logger.error("userName is Empty");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("select a.device_id,a.username loid from tab_hgwcustomer a, hgwcust_serv_info b where a.user_id=b.user_id and a.user_state = '1' and b.serv_status=1 and b.serv_type_id=10 and b.username='")
		.append(userName).append("'");
		sb.append(" order by a.updatetime desc");
		PrepareSQL psql = new PrepareSQL();
		psql.append(sb.toString());
		ArrayList<HashMap<String, String>> records = DBOperation.getRecords(psql.getSQL());
		if(null!=records&&records.size()>0){
			return records;
		}else{
			return null;
		}
		
	}
}
