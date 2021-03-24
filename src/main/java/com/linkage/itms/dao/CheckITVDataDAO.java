package com.linkage.itms.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;

/**
 * 新疆电信：ITMS检查光猫ITV口数据配置是否正确，采集itv的vlan配置是否正确
 * 
 * @author chenxj6
 * @date 2016-8-29
 * @param param
 * @return
 */
public class CheckITVDataDAO {
	
	private static final Logger logger = LoggerFactory
			.getLogger(CheckITVDataDAO.class);
	
	/**
	 * 根据用户宽带账号查询用户信息
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		String sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a where a.username= ? order by a.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户IPTV宽带账号查询用户信息
	 * @param iptvAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByIptvAccount(String iptvAccount)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=?  order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, iptvAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * @param voipPhone
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipPhone(String voipPhone)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.voip_phone = ? "
				+ " order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipPhone);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据VOIP认证账号查询用户信息
	 * @param voipAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByVoipAccount(String voipAccount)
	{
		String sql = "select a.user_id, b.device_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.voip_username = ? "
				+ " order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	/**
	 * 根据用户设备SN查询设备
	 * @param devSn
	 * @return
	 */
	public List<HashMap<String, String>> queryDeviceByDevSN(String devSN)
	{
		String sql = "select a.device_id,a.device_serialnumber from tab_gw_device a where a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "'";
		if (!StringUtil.IsEmpty(devSN) && devSN.length() >= 6) {
			String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
			sql = sql + " and dev_sub_sn='" + devSubSn + "'";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	
	/**
	 * 根据用户设备 device_id 检查光猫版本是否支持组播
	 * @param device_id
	 * @return
	 */
	public List<HashMap<String, String>> queryIsMulticastByDevId(String deviceId)
	{
		String sql = "select a.is_multicast from tab_devicetype_info a,tab_gw_device b where a.DEVICETYPE_ID = b.DEVICETYPE_ID " +
				" and b.DEVICE_ID='"+deviceId+"'";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 获得上行方式 XJ
	 * @param deviceId
	 * @return
	 */
	public  String getAccessType(String deviceId)
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
	 * 入gw_wan表
	 * 
	 * @author gongsj
	 * @date 2010-7-19
	 * @param deviceId
	 * @param wanId
	 * @param accessType
	 * @return
	 */
	public  boolean insertWan(String deviceId, String wanId, String accessType)
	{
		if (null == deviceId || null == wanId)
		{
			return false;
		}
		String gatherTime = String.valueOf(new DateTimeUtil().getLongTime());
		StringBuilder usBuilder = new StringBuilder();
		usBuilder.append("delete from gw_wan where device_id='").append(deviceId)
				.append("' and wan_id=").append(wanId);
		
		
		if (DBOperation.executeUpdate(usBuilder.toString()) >= 0) // 原为 > 0
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
	 * 更新业务用户表的open_status 新疆专用
	 * 
	 * @author gongsj
	 * @date 2011-06-28
	 * @param userId
	 * @param openStatus
	 */
	public void updateHgwcustServInfoVOIP_XJ(String deviceId, int openStatus)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("update ").append(" hgwcust_serv_info ")
				.append(" set open_status=").append(openStatus).append(", updatetime=")
				.append(new Date().getTime() / 1000)
				.append(" where user_id=(select user_id from ")
				.append(" tab_hgwcustomer ").append(" where device_id='")
				.append(deviceId).append("') and serv_type_id=14");
		logger.debug("更新业务用户表SQL：{}", sql.toString());
		int result = DBOperation.executeUpdate(sql.toString());
		if (result != 1)
		{
			logger.warn("更新  hgwcust_serv_info 的open_status失败");
		}
	}
}

