package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.TerminalVersionAuditChecker;


public class TerminalVersionAuditDAO {
	
	private static final Logger logger = LoggerFactory
			.getLogger(TerminalVersionAuditDAO.class);
	
	
	/**
	 * 
	 * 查询设备厂商信息是否存在
	 * 
	 * @param vendor_name 厂商名称
	 * @return 厂商ID，没有结果返回null
	 */
	public String qryVendor(String vendor_name){
		logger.debug("TerminalVersionAuditDAO==>qryVendor({})",
				new Object[] { vendor_name });
		
		if(StringUtil.IsEmpty(vendor_name)){
			return null;
		}
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select vendor_id from tab_vendor where vendor_name = '").append(vendor_name).append("' or vendor_add='").append(vendor_name).append("'");
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list == null || list.size()<=0){
			return null;
		}
		else{
			return list.get(0).get("vendor_id");
		}
	}
	
	
	/**
	 * 
	 * 查询设备型号信息是否存在
	 * 
	 * @param device_model 型号
	 * @return 型号ID，没有结果返回null
	 */
	public String qryModel(String device_model){
		logger.debug("TerminalVersionAuditDAO==>qryModel({})",
				new Object[] { device_model});
		
		if(StringUtil.IsEmpty(device_model)){
			return null;
		}
		StringBuffer sql = new StringBuffer();
		
		sql.append("select device_model_id from gw_device_model where device_model = '").append(device_model).append("'");
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list == null || list.size()<=0){
			return null;
		}
		else{
			return list.get(0).get("device_model_id");
		}
		
	}
	
	
	
	/**
	 * 
	 * 查询设备规格信息是否存在
	 * 
	 * @param spec_name 规格名称
	 * @return 规格ID，没有结果返回null
	 */
	public String qrySpec(String spec_name){
		logger.debug("TerminalVersionAuditDAO==>qrySpec({})",
				new Object[] { spec_name});
		
		if(StringUtil.IsEmpty(spec_name)){
			return null;
		}
		StringBuffer sql = new StringBuffer();
		
		sql.append("select id from tab_bss_dev_port where spec_name = '").append(spec_name).append("'");
		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list == null || list.size()<=0){
			return null;
		}
		else{
			return list.get(0).get("id");
		}
		
	}
	
	
	
	/**
	 * 
	 * 查询软件版本是否存在
	 * 
	 * @param device_model_id 型号id
	 * @return 是否存在
	 */
	public boolean isSoftwareversionExist(String softwareversion){
		logger.debug("TerminalVersionAuditDAO==>isSoftwareversionExist({})",
				new Object[] { softwareversion });
		
		if(StringUtil.IsEmpty(softwareversion)){
			return false;
		}
		StringBuffer sql = new StringBuffer();

		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql.append("select softwareversion from tab_devicetype_info where softwareversion = '").append(softwareversion).append("'");
		} else {
			sql.append("select * from tab_devicetype_info where softwareversion = '").append(softwareversion).append("'");
		}		
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql.toString());
		
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list == null || list.size()<=0){
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 插入tab_devicetypetask_info记录
	 * @param checker 参数
	 */
	public void insertDeviceTypeTask(TerminalVersionAuditChecker checker){
		logger.debug("TerminalVersionAuditDAO==>insertDeviceTypeTask({})",
				new Object[] {checker});
		
		String vendor = checker.getVendor_name();
		String device_model = checker.getDevice_model();
		String softwareversion = checker.getSoftwareversion();
		String hardwareversion = checker.getHardwareversion();
		int rela_dev_type_id = StringUtil.getIntegerValue(checker.getRela_dev_type_id());
		int access_style_relay_id = StringUtil.getIntegerValue(checker.getAccess_style_relay_id());
		String spec_name = checker.getSpec_name();
		String reason = checker.getReason();
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("insert into tab_devicetypetask_info(vendor,device_model,hardwareversion,softwareversion,rela_dev_type_id,access_style_relay_id,spec,reason,time) values (?,?,?,?,?,?,?,?,?)");
		
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.setString(1, vendor);
		psql.setString(2, device_model);
		psql.setString(3, hardwareversion);
		psql.setString(4, softwareversion);
		psql.setInt(5, rela_dev_type_id);
		psql.setInt(6, access_style_relay_id);
		psql.setString(7, spec_name);
		psql.setString(8, reason);
		psql.setLong(9, new DateTimeUtil().getLongTime());

		DBOperation.executeUpdate(psql.getSQL());
	}
}
