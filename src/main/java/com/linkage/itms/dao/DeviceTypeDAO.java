package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 终端类型数据库操作类
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class DeviceTypeDAO {

	private static Logger logger = LoggerFactory.getLogger(DeviceTypeDAO.class);
	//devicetype_id
	private int devicetypeId;
	//设备型号ID
	private int deviceModelId;
	//设备厂商ID
	private int deviceVendorId;
	//设备厂商名称
	private String deviceVendor;
	//设备型号
	private String deviceModel;
	
	private String deviceSoftwareversion;

	
	/**
	 * 根据设备类型ID获取设备类型对象的参数
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return void
	 */
	public void queryDeviceType(int _deviceTypeId){
		logger.debug("queryDeviceType({})", _deviceTypeId);
		
		devicetypeId = _deviceTypeId;
		
		String strSQL = "select a.softwareversion, b.device_model_id, b.device_model, "
			+ " c.vendor_id, c.vendor_add, c.vendor_name "
			+ " from tab_devicetype_info a, gw_device_model b, tab_vendor c"
			+ " where a.device_model_id=b.device_model_id and a.vendor_id=c.vendor_id"
			+ " and a.devicetype_id=?";
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setInt(1, _deviceTypeId);
		
		Map<String, String> devTypeMap = DBOperation.getRecord(psql.getSQL());
		
		if(null != devTypeMap && false == devTypeMap.isEmpty()){
			deviceModelId = StringUtil.getIntegerValue(devTypeMap.get("device_model_id"));
			deviceVendorId = StringUtil.getIntegerValue(devTypeMap.get("vendor_id"));
			deviceModel = devTypeMap.get("device_model");
			deviceVendor = devTypeMap.get("vendor_add");
			deviceSoftwareversion = devTypeMap.get("softwareversion");
		}
	}
	
	
	/**
	 * 终端类型是否支持路由下发
	 * 
	 * @param 
	 * 	_devTypeId： 终端类型
	 * 
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return boolean 支持返回1, 否则返回-1
	 */
	public static int routedSupported(int _devTypeId){
		logger.debug("routedSupported({}, {})", _devTypeId);

		return devTypeSupported(_devTypeId, 1);
	}
	
	
	
	/**
	 * 终端类型是否支持
	 * 
	 * @param 
	 * 	_devTypeId： 终端类型;
	 * 	supportedType： 支持类型
	 * 
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return boolean 
	 * 	支持返回1, 
	 * 	不支持返回-1
	 */
	public static int devTypeSupported(int _devTypeId, int _supportedType){
		logger.debug("devTypeSupported({}, {})", _devTypeId, _supportedType);
		
		String strSQL = "select devicetype_id from res_devtype_support "
			+ " where devicetype_id=? and sp_type_id=?";
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setInt(1, _devTypeId);
		psql.setInt(2, _supportedType);
		
		Map<String, String> devTypeMap = DBOperation.getRecord(psql.getSQL());
		
		if(null == devTypeMap || devTypeMap.isEmpty()){
			return -1;
		}
		
		return 1;
	}
	
	
	/** getter, setter methods */
	
	public int getDevicetypeId() {
		return devicetypeId;
	}

	public void setDevicetypeId(int devicetypeId) {
		this.devicetypeId = devicetypeId;
	}

	public int getDeviceModelId() {
		return deviceModelId;
	}

	public void setDeviceModelId(int deviceModelId) {
		this.deviceModelId = deviceModelId;
	}

	public int getDeviceVendorId() {
		return deviceVendorId;
	}

	public void setDeviceVendorId(int deviceVendorId) {
		this.deviceVendorId = deviceVendorId;
	}

	public String getDeviceVendor() {
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor) {
		this.deviceVendor = deviceVendor;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	
	public String getDeviceSoftwareversion()
	{
		return deviceSoftwareversion;
	}

	public void setDeviceSoftwareversion(String deviceSoftwareversion)
	{
		this.deviceSoftwareversion = deviceSoftwareversion;
	}
	
}
