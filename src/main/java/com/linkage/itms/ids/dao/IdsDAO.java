package com.linkage.itms.ids.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-10-18
 * @category com.linkage.itms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class IdsDAO
{
	
	private static final Logger logger = LoggerFactory.getLogger(IdsDAO.class);
	
	/**
	 * 根据设备序列号查询设备ID
	 * @param devSn 设备序列号
	 * @return 如果设备序列号在设备表中不存在，返回null，否则返回设备ID
	 */
	public String getDeviceId(String devSn)
	{
		String sql = "select device_id from tab_gw_device where device_serialnumber=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, devSn);
		Map<String, String> map = DBOperation.getRecord(pSql.getSQL());
		String result = StringUtil.getStringValue(map, "device_id");
		logger.info("get device_id by device_serialnumber[{}] is [{}]", devSn, result);
		return result;
	}
}
