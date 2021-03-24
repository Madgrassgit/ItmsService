package com.linkage.itms.mq.servinfo.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

public class servInfoToXML {

	private static Logger logger = LoggerFactory.getLogger(servInfoToXML.class);
	
	
	/**
	 * 通过设备ID将业务信息，设备信息组装成XML格式
	 * 
	 * @param deviceId
	 * @return
	 */
	public String getXML(String deviceId) {
		
		logger.debug("getXML({})", new Object[]{deviceId}); 
		
		StringBuffer sbXML = new StringBuffer("");
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.city_id, a.device_id, a.device_serialnumber, a.devicetype_id, a.vendor_id, a.device_model_id, a.devicetype_id, b.user_id, b.username");
		psql.append("  from tab_gw_device a, tab_hgwcustomer b");
		psql.append(" where 1=1");
		psql.append("   and a.device_id = b.device_id");
		psql.append("   and b.user_state in('1','2')");
		psql.append("   and a.device_id = '");
		psql.append(deviceId);
		psql.append("'");
		
		List<HashMap<String,String>> deviceList = DBOperation.getRecords(psql.getSQL()); 
		
		if (null != deviceList && false == deviceList.isEmpty()) {
			Map<String, String> map = (Map<String, String>)deviceList.get(0);
			
			/** 逻辑SN */
			String userId = StringUtil.getStringValue(map.get("user_id"));
			/** 厂商ID */
			String vendorId = StringUtil.getStringValue(map.get("vendor_id"));
			/** 型号ID */
			String deviceModelId = StringUtil.getStringValue(map.get("device_model_id"));
			/** 版本ID */
			String devicetypeId = StringUtil.getStringValue(map.get("devicetype_id"));
			
			/** 厂商名称 */
			String vendorName = StringUtil.getStringValue(getVendorName(vendorId).get("vendor_name"));
			if(StringUtil.IsEmpty(vendorName)){
				vendorName = StringUtil.getStringValue(getVendorName(vendorId).get("vendor_add"));
			}
			/** 设备型号 */
			String deviceModel = StringUtil.getStringValue(getDeviceModel(vendorId, deviceModelId).get("device_model"));
			
			Map<String, String> map2 = getDeviceType(vendorId, deviceModelId, devicetypeId);
			/** 设备版硬件本 */
			String hardwareversion = StringUtil.getStringValue(map2.get("hardwareversion"));
			/** 设备软件版本 */
			String softwareversion = StringUtil.getStringValue(map2.get("softwareversion"));
			
			sbXML.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
			sbXML.append("<root>");
			sbXML.append("<CmdID>").append(StringUtil.getStringValue(map.get("user_id"))).append("</CmdID>");
			sbXML.append("<Sheets>");
			sbXML.append("<Loid>").append(StringUtil.getStringValue(map.get("username"))).append("</Loid>");
			sbXML.append("<CityId>").append(StringUtil.getStringValue(map.get("city_id"))).append("</CityId>");
			sbXML.append("<DevSN>").append(StringUtil.getStringValue(map.get("device_serialnumber"))).append("</DevSN>");
			sbXML.append("<DevType>").append(StringUtil.getStringValue(map.get("devicetype_id"))).append("</DevType>");
			sbXML.append("<Vendor>").append(vendorName).append("</Vendor>");
			sbXML.append("<DevModel>").append(deviceModel).append("</DevModel>");
			sbXML.append("<HandwareVersion>").append(hardwareversion).append("</HandwareVersion>");
			sbXML.append("<SoftwareVersion>").append(softwareversion).append("</SoftwareVersion>");
			
			/** 根据user_id 查询业务信息 将业务信息组装成XML格式*/
			PrepareSQL psql_1 = new PrepareSQL();
			psql_1.append("select c.user_id, c.dealdate, c.serv_type_id, c.serv_status, c.open_status ");
			psql_1.append("  from hgwcust_serv_info c");
			psql_1.append(" where 1=1");
			psql_1.append("   and c.user_id = ");
			psql_1.append(userId);
			
			List<HashMap<String, String>> servInfoList = DBOperation.getRecords(psql_1.getSQL());
			
			if(null != servInfoList && !servInfoList.isEmpty()){
				for (HashMap<String, String> hashMap : servInfoList) {
					sbXML.append("<SheetInfo>");
					sbXML.append("<DealDate>").append(StringUtil.getStringValue(hashMap.get("dealdate"))).append("</DealDate>");
					sbXML.append("<ServiceType>").append(StringUtil.getStringValue(hashMap.get("serv_type_id"))).append("</ServiceType>");
					sbXML.append("<OperateType>").append(StringUtil.getStringValue(hashMap.get("serv_status"))).append("</OperateType>");
					sbXML.append("<OpenStatus>").append(StringUtil.getStringValue(hashMap.get("open_status"))).append("</OpenStatus>");
					sbXML.append("<Desc>").append("").append("</Desc>");
					sbXML.append("</SheetInfo>");
				}
			}
			sbXML.append("</Sheets>");
			sbXML.append("</root>");
		}
		return sbXML.toString();
	}
	
	
	/**
	 * 获取设备厂商
	 * 
	 * @param vendor_id
	 * @return
	 */
	public Map<String, String> getVendorName(String vendor_id) {
		
		logger.debug("infoToXML==>getVendorName({})", new Object[]{vendor_id});
		
		if(StringUtil.IsEmpty(vendor_id)){
			logger.error("vendor_id is empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select vendor_id, vendor_name, vendor_add ");
		psql.append("  from tab_vendor ");
		psql.append(" where 1=1");
		psql.append("   and vendor_id = '");
		psql.append(vendor_id);
		psql.append("'");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	/**
	 * 获取设备型号
	 * 
	 * @param vendor_id
	 * @param device_model_id
	 * @return
	 */
	public Map<String, String> getDeviceModel(String vendor_id, String device_model_id) {
		
		logger.debug("infoToXML==>getDeviceModel({},{})", new Object[]{vendor_id, device_model_id});
		
		if(StringUtil.IsEmpty(device_model_id)){
			logger.error("device_model_id is empty");
			return null;
		}
		if(StringUtil.IsEmpty(vendor_id)){
			logger.error("vendor_id is empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select device_model ");
		psql.append("  from gw_device_model ");
		psql.append(" where 1=1");
		psql.append("   and vendor_id = '");
		psql.append(vendor_id);
		psql.append("'");
		psql.append("   and device_model_id = '");
		psql.append(device_model_id);
		psql.append("'");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	/**
	 * 获取设备版本
	 * 
	 * @param vendor_id
	 * @param device_model_id
	 * @param devicetype_id
	 * @return
	 */
	public Map<String,String> getDeviceType(String vendor_id, String device_model_id, String devicetype_id){
		
		logger.debug("infoToXML==>getDeviceType({},{},{})", new Object[]{vendor_id, device_model_id, devicetype_id});

		if(StringUtil.IsEmpty(device_model_id)){
			logger.error("device_model_id is empty");
			return null;
		}
		if(StringUtil.IsEmpty(vendor_id)){
			logger.error("vendor_id is empty");
			return null;
		}
		if(StringUtil.IsEmpty(devicetype_id)){
			logger.error("devicetype_id is empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select hardwareversion, softwareversion ");
		psql.append("  from tab_devicetype_info ");
		psql.append(" where 1=1");
		psql.append("   and devicetype_id = ");
		psql.append(devicetype_id);
		psql.append("   and vendor_id = '");
		psql.append(vendor_id);
		psql.append("'");
		psql.append("   and device_model_id = '");
		psql.append(device_model_id);
		psql.append("'");

		return DBOperation.getRecord(psql.getSQL());
	}
}
