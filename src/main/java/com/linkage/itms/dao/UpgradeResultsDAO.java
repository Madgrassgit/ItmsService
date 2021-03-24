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
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.main.CallService;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月25日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpgradeResultsDAO
{
	private static Logger logger = LoggerFactory.getLogger(UpgradeResultsDAO.class);
	
	/**
	 * 查询指定属地，指定任务号下，升级成功的device_id 列表
	 * @param taskId 任务号
	 * @return 
	 */
	public ArrayList<HashMap<String, String>> selectSuccessDeviceListIdByTaskId(Long taskId, String cityIds){
		String sql = "select a.device_id from tab_version_upgrade_dev a left join gw_serv_strategy_soft b on a.device_id = convert(int,b.device_id) where b.result_id != -1 and b.status = 100 and a.city_id in ('"+cityIds+"') and a.task_id = "+taskId;
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select a.device_id from tab_version_upgrade_dev a left join gw_serv_strategy_soft b on a.device_id = cast(b.device_id as SIGNED INTEGER) where b.result_id != -1 and b.status = 100 and a.city_id in ('"+cityIds+"') and a.task_id = "+taskId;
		}
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 通过device_id，查询该设备对应的版本型号
	 * @param deviceId 设备id
	 * @return
	 */
	public Map<String, String> selectDeviceModelByDeviceId(String deviceId){
		String sql = "select a.device_id,b.device_model,a.device_model_id,a.vendor_id from tab_gw_device a left join gw_device_model b on a.device_model_id = b.device_model_id where a.device_id = ?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 通过device_id，查询对应的软件版本
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> selectDeviceTypeByDeviceId(String deviceId){
		String sql = "select a.device_id,b.reason,b.softwareversion from tab_gw_device a left join tab_devicetype_info b on a.devicetype_id = b.devicetype_id where device_id = ?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 通过厂商id和版本型号，查询软件版本
	 * @param vendor_id
	 * @param device_model_id
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryByVendorIdAndDeviceModelId(String vendor_id, String device_model_id)
	{
		String sql = "select devicetype_id,softwareversion from tab_devicetype_info where vendor_id = '"+vendor_id+"' and device_model_id = '"+device_model_id+"'";
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.getRecords(psql.getSQL());
	}
	/**
	 * 查询某任务号下，指定属地的设备数量
	 * @param taskId
	 * @param cityIds
	 * @return
	 */
	public int selectCountDeviceByTaskIdAndCityIds(Long taskId, String cityIds){
		String sql  = "select count(1) as num from tab_version_upgrade_dev where task_id = "+taskId+" and city_id in ('"+cityIds+"')";
		PrepareSQL psql = new PrepareSQL(sql);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
		
	}
	/**
	 * 通过属地名称，查询属地id
	 * @param name
	 * @return
	 */
	public String selectCityIdByCityName(String name){
		String sql = "select city_id from tab_city where city_name like '%"+name+"%'";
		PrepareSQL psql = new PrepareSQL(sql);
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		if(list!=null && !list.isEmpty()){
			HashMap<String, String> map = list.get(0);
			return map.get("city_id");
		}
		return null;
	}
}
