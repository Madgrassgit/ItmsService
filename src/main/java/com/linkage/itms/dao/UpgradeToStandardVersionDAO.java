package com.linkage.itms.dao;

import java.awt.image.DataBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;


public class UpgradeToStandardVersionDAO {
	
	private static final Logger logger = LoggerFactory
			.getLogger(UpgradeToStandardVersionDAO.class);
	
	
	/**
	 * 查询版本信息
	 * 
	 * 综调软件功能接口，机制需要调整下，升级通过版本对应关系<br>
	 * （以前是通过一个型号一个规范版本，但是发现现网一个型号存在江苏版本和苏州版本）<br>
	 * modify by zhangchy 20130115
	 * 
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> queryStandardVersionByDevSn(String deviceId) {
		
		logger.debug("UpgradeToStandardVersionDAO==>queryStandardVersionByDevSn()");
		
		PrepareSQL psql = new PrepareSQL();
		// 综调软件功能接口，机制需要调整下，升级通过版本对应关系（以前是通过一个型号一个规范版本，但是发现现网一个型号存在江苏版本和苏州版本）
		// modify by zhangchy 20130115
//		psql.append("select a.device_id, a.device_serialnumber,a.devicetype_id, b.devicetype_id as normal_devicetype_id ");
//		psql.append("  from tab_gw_device a, tab_devicetype_info b ");
//		psql.append(" where 1=1 ");
//		psql.append("   and a.device_model_id = b.device_model_id ");
//		psql.append("   and b.is_normal = 1 ");  // 1 表示规范版本，0 表示不规范版本
//		psql.append("   and a.device_id = '"+deviceId+"' ");
		
		psql.append("select a.device_id, a.device_serialnumber,a.devicetype_id ");
		psql.append("  from tab_gw_device a ");
		psql.append(" where 1=1 ");
		psql.append("   and a.device_id = '"+deviceId+"' ");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 根据 原始版本ID 查询 目标版本ID
	 * @param devicetype_id_old
	 * @return
	 */
	public List<HashMap<String, String>> queryDestinationVersion(String devicetype_id_old) {
	
		PrepareSQL psql = new PrepareSQL();
		
		if (3 == DBUtil.GetDB()) {
			psql.append("select devicetype_id from gw_soft_upgrade_temp_map");
		} else {
			psql.append("select * from gw_soft_upgrade_temp_map");
		}
		psql.append(" where 1=1 ");
		psql.append("   and devicetype_id_old = "+devicetype_id_old);
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 根据版本对应关系表中的目标版本id 查询  版本文件
	 * @param devicetype_id
	 * @return
	 */
	public List<HashMap<String, String>> querySoftwareFile(String devicetype_id){
		
		PrepareSQL psql = new PrepareSQL();
		
		// oracle
		if (1 == DBUtil.GetDB()) {
			psql.append("select devicetype_id, outter_url||'/'||server_dir||'/'||softwarefile_name as file_url");
			psql.append(" , softwarefile_size, softwarefile_name");
			psql.append("  from tab_software_file a, tab_file_server b ");
			psql.append(" where a.dir_id=b.dir_id and a.softwarefile_isexist=1 ");
			psql.append("   and devicetype_id = "+devicetype_id);
		}
		// Sybase
		else if (2 == DBUtil.GetDB()) {
			psql.append("select devicetype_id, outter_url+'/'+server_dir+'/'+softwarefile_name as file_url");
			psql.append(" , softwarefile_size, softwarefile_name");
			psql.append("  from tab_software_file a, tab_file_server b ");
			psql.append(" where a.dir_id=b.dir_id and a.softwarefile_isexist=1 " );
			psql.append("   and devicetype_id = "+devicetype_id);
		}
		// mysql
		else if (3 == DBUtil.GetDB()) {
			psql.append("select devicetype_id, outter_url+'/'+server_dir+'/'+softwarefile_name as file_url");
			psql.append(" , softwarefile_size, softwarefile_name");
			psql.append("  from tab_software_file a, tab_file_server b ");
			psql.append(" where a.dir_id=b.dir_id and a.softwarefile_isexist=1 " );
			psql.append("   and devicetype_id = "+devicetype_id);
		}
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	/**
	 * 查询规范版本文件
	 * 
	 * @param deviceTypeId
	 * @return
	 */
	public int countStandardVersionFile(String deviceTypeId) {
		
		logger.debug("UpgradeToStandardVersionDAO==>countStandardVersionFile({})", deviceTypeId);
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select count(1) as num ");
		psql.append("  from tab_software_file ");
		psql.append(" where 1=1 ");
		psql.append("   and softwarefile_isexist = 1 ");
		psql.append("   and devicetype_id = "+deviceTypeId);
		
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		
		return StringUtil.getIntValue(map, "num");
		
	}
	
	
	
	
}
