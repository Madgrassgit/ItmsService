package com.linkage.stbms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.util.StringUtil;

/**
 * 采集结点信息的数据库表操作类
 * 
 * @author Jason(3412)
 * @date 2009-12-22
 */
public class GatherInfoDAO {

	private static Logger logger = LoggerFactory.getLogger(GatherInfoDAO.class);

	/**
	 * 获取STBDevice结点下频道号等信息
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getStbDeviceInfo(String deviceId) {
		logger.debug("getStbInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}

		String strSQL = "select c.service_name" + " from stb_audience_stats c"
				+ " where c.device_id='" + deviceId + "'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}

	/**
	 * 获取STB的MAC地址，地址方式等信息
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getStbLanInfo(String deviceId) {
		logger.debug("getStbLanInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}

		String strSQL = "select a.mac,a.address_type" + " from stb_lan a"
				+ " where a.device_id='" + deviceId + "'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}

	/**
	 * 获取STB媒体服务器地址等信息
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getStbUserInterfaceInfo(String deviceId) {
		logger.debug("getStbUserInterfaceInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}

		String strSQL = "select b.stream_serv_ip" + " from stb_user_itfs b"
				+ " where b.device_id='" + deviceId + "'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}

	/**
	 * 获取STB的IPTV业务账号和密码
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getStbX_CTC_IPTVInfo(String deviceId) {
		logger.debug("getStbInfo({})", deviceId);

		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("deviceId is empty!");
			return null;
		}

		String strSQL = "select d.user_id,d.user_pwd,auth_url"
				+ " from stb_x_serv_info d" + " where d.device_id='" + deviceId
				+ "'";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
}
