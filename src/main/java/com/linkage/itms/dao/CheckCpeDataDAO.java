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
 * 新疆电信：ITMS检查光猫cpe口数据配置是否正确，采集cpe的vlan配置是否正确
 * 
 * @author chenxj6
 * @date 2016-8-29
 * @param param
 * @return
 */
public class CheckCpeDataDAO {
	
	private static final Logger logger = LoggerFactory
			.getLogger(CheckCpeDataDAO.class);
	
	/**
	 * 根据用户宽带账号查询用户信息
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by a.updatetime desc";
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
		String sql = "select a.user_id,a.device_id from tab_hgwcustomer a where a.username= ? order by a.updatetime desc";
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
				+ " where a.user_id = b.user_id and a.serv_type_id=11 and a.username=?  order by a.updatetime desc";
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
				+ " order by a.updatetime desc";
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
				+ " order by a.updatetime desc";
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
		String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
		String sql = "select a.device_id,a.device_serialnumber from tab_gw_device a where a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' ";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户设备SN查询user_id
	 * @param devSn
	 * @return
	 */
	public HashMap<String, String> queryUserId(String devSN)
	{
		String devSubSn = devSN.substring(devSN.length() - 6, devSN.length());
		String sql = "select b.user_id from hgwcust_serv_info b, tab_hgwcustomer c, tab_gw_device a " +
				" where b.user_id=c.user_id and c.device_id=a.device_id and b.serv_type_id=10 and a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "' and a.dev_sub_sn='" + devSubSn + "' order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		HashMap<String, String> result = (HashMap<String, String>) DBOperation.getRecord(pSql.getSQL());
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
	
	/**
	 * 更新业务用户表的wan_type 新疆专用
	 * 
	 * @author chenxj
	 * @date 2016-11-07
	 * @param deviceId
	 */
	public HashMap<String, String> getWanType(String deviceId)
	{
		String sql = " select a.wan_type,a.user_id,b.device_id from hgwcust_serv_info a, tab_hgwcustomer b " +
				" where a.user_id=b.user_id and b.device_id ='"+deviceId+"' and a.serv_type_id=10 order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		if(null==result || result.isEmpty()){
			return null;
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * 查询tab_voip_serv_param表中的用户信息 新疆专用
	 * 
	 * @author chenxj
	 * @date 2016-11-08
	 * @param deviceId
	 */
	
	public Map<String, String> getVoipUserInfo(String deviceId)
	{
		String sql = " select a.user_id,a.username username,b.VOIP_USERNAME voip_username,b.VOIP_PASSWD voip_passwd,b.PROTOCOL protocol, " +
				" b.line_id line_id,b.sip_id sip_id,b.URI uri from tab_hgwcustomer a,tab_voip_serv_param b " +
				" where a.user_id=b.user_id  and a.device_id ='"+deviceId+"' order by b.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
			
	/**
	 * 查询tab_sip_info表中的用户信息 新疆专用
	 * 
	 * @author chenxj
	 * @date 2016-11-08
	 * @param sip_id
	 */
	public Map<String, String> getSipInfo(String sip_id)
	{
		String sql = " select sip_id, prox_serv, prox_port, stand_prox_serv, stand_prox_port,  regi_serv, regi_port, " +
				" stand_regi_serv, stand_regi_port,  out_bound_proxy, out_bound_port, stand_out_bound_proxy, stand_out_bound_port, remark " +
				" from tab_sip_info where sip_id ="+sip_id;
		PrepareSQL pSql = new PrepareSQL(sql);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
//	/**
//	 * 查询wifi功能
//	 * 
//	 * @author chenxj
//	 * @date 2016-11-08
//	 * @param userType username
//	 */
//	public Map<String, String> queryWifiInfo(int userType, String username){
//		logger.debug("queryWifiInfo({})", username);
//		
//		if (StringUtil.IsEmpty(username)) {
//			logger.error("username is Empty");
//			return null;
//		}
//		
//		PrepareSQL psql = new PrepareSQL();
//		
//		psql.append("select a.wlan_num, a.lan_num, d.city_id ");
//		
//		switch (userType)
//		{
//			case 1:
//				psql.append("	from  hgwcust_serv_info b, tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
//				psql.append("	where  b.serv_type_id=10  and b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id");
//				psql.append("	and b.username='" + username + "'");
//				break;
//			case 2:
//				psql.append("	from  tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
//				psql.append("	where c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
//				psql.append("	and  c.username='" + username + "'");
//				break;
//			case 3:
//				psql.append("	from hgwcust_serv_info b , tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
//				psql.append("	where  b.serv_type_id=11  and b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id");
//				psql.append("	and b.username='" + username + "'");
//				break;
//			case 4:
//				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c,tab_gw_device d,tab_devicetype_info e,tab_bss_dev_port a  ");
//				psql.append("	where b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
//				psql.append("	and   b.voip_phone='" + username + "'");
//				break;
//			case 5:
//				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c,tab_gw_device d,tab_devicetype_info e,tab_bss_dev_port a  ");
//				psql.append("	where b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
//				psql.append("	and   b.voip_username='" + username + "'");
//				break;
//			default:
//				psql.append(" from  tab_bss_dev_port a , tab_gw_device d  where 1=2");
//				break;
//		}
//		return DBOperation.getRecord(psql.getSQL());
//	}
	
	
	/**
	 * 查询宽带用户名密码新疆专用
	 * 
	 * @author chenxj
	 * @date 2016-11-25
	 * @param deviceId userId
	 */
	public HashMap<String, String> getNetInfo(String deviceId, String userId)
	{
		String sql = " select b.username,b.passwd from tab_hgwcustomer a ,hgwcust_serv_info b " +
				" where a.user_id = b.user_id and b.serv_type_id = 10 and  a.device_id='"+deviceId+"' and a.user_id=? order by b.updatetime desc ";
				
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setLong(1, StringUtil.getLongValue(userId));
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		if(null==result || result.isEmpty()){
			return null;
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * 根据 userId 查询用户所开业务
	 * @author zhangsm 2012-1-5
	 * @param _userId
	 * @param _servTypeId
	 * @param itvUsername
	 * @return
	 */
	public List<HashMap<String,String>> getServExistList(long userId) {  // 宽带：10 ； iptv：11 ； voip：14 
		logger.debug("getServExistList[{}]", new Object[]{userId});

		String strSQL = "select user_id, serv_type_id, username,vlanid from hgwcust_serv_info where serv_type_id in (10,11,14) and user_id=? and serv_status=1  order by updatetime desc ";
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 根据 userId 查询业务参数表
	 * @author chenxj6 2016-11-30
	 * @param userId
	 * @param servTypeId
	 * @return
	 */
	public HashMap<String,String> queryServParamInfo(long userId, int servTypeId) {  // 宽带：10 ； iptv：11 ； voip：14 
		logger.debug("getServExistList[{}]", new Object[]{userId});

		String strSQL = "select user_id,username,vlanId,bind_port from hgwcust_serv_info where user_id=? and serv_status=1 and serv_type_id=? " +
				" order by updatetime desc ";
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);
		
		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list==null || list.size()==0){
			return null;
		} 
		
		return list.get(0);
	}
}
	

