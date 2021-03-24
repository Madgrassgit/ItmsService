
package com.linkage.itms.oss.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-4-2
 * @category com.linkage.itms.oss.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SpecInfoDAO
{

	private static final Logger logger = LoggerFactory.getLogger(SpecInfoDAO.class);

	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public Map<String, String> queryUserByLoid(String loid)
	{
		String sql = "select user_id,spec_id,device_id,city_id from tab_hgwcustomer where user_state = '1' and username=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", loid, result);
		return result;
	}

	/**
	 * 根据用户宽带账号查询用户信息
	 * 
	 * @param netAccount
	 * @return
	 */
	public Map<String, String> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.spec_id, b.device_id, b.city_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and b.user_state = '1'  and a.serv_type_id=10 and a.username=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", netAccount, result);
		return result;
	}

	/**
	 * 根据用户IPTV宽带账号查询用户信息
	 * 
	 * @param iptvAccount
	 * @return
	 */
	public Map<String, String> queryUserByIptvAccount(String iptvAccount)
	{
		String sql = "select a.user_id, b.spec_id, b.device_id, b.city_id"
				+ " from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and b.user_state = '1'  and a.serv_type_id=11 and a.username=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, iptvAccount);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", iptvAccount, result);
		return result;
	}

	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * 
	 * @param voipPhone
	 * @return
	 */
	public Map<String, String> queryUserByVoipPhone(String voipPhone)
	{
		String sql = "select a.user_id, b.spec_id, b.device_id, b.city_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and b.user_state = '1' and voip_phone = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipPhone);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", voipPhone, result);
		return result;
	}

	/**
	 * 根据VOIP认证账号查询用户信息
	 * 
	 * @param voipAccount
	 * @return
	 */
	public Map<String, String> queryUserByVoipAccount(String voipAccount)
	{
		String sql = "select a.user_id, b.spec_id, b.device_id, b.city_id"
				+ " from tab_voip_serv_param a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and b.user_state = '1' and voip_username = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, voipAccount);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", voipAccount, result);
		return result;
	}

	public Map<String, String> queryDeviceSpec(String deviceId)
	{
		String sql = "select a.device_serialnumber, b.spec_id, a.city_id from tab_gw_device a, tab_devicetype_info b"
				+ " where a.devicetype_id=b.devicetype_id and a.device_id = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		logger.warn("[{}]-[{}]", deviceId, result);
		return result;
	}
}
