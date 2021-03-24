package com.linkage.itms.dispatch.cqdx.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年6月25日
 * @category com.linkage.itms.dispatch.cqdx.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class OUIoperateDao
{
	
	public boolean chekOuiIn(String device_model,String oui){
		String sql =  "select count(1) num from tab_gw_device_init_oui where device_model='"+device_model+"' and oui='"+oui+"'";
		
		PrepareSQL pSQL = new PrepareSQL(sql);
		Map<String, String> maps = null;
		try
		{
		   maps = DBOperation.getRecord(pSQL.getSQL());
		}
		catch (Exception e)
		{
			return false;
		}
		
		if(null != maps && !maps.isEmpty() && Integer.valueOf(maps.get("num")) > 0){
			return true;
		}
		return false;
	}
	public int addOUI(int id, String ouiId, String name, String vendorName, String remark, String add_date, String device_model){
		
		StringBuffer sql = new StringBuffer();
		sql.append("insert into tab_gw_device_init_oui(id, add_date");
		if(!StringUtil.IsEmpty(ouiId)){
			sql.append(" ,oui");
		}
		if(!StringUtil.IsEmpty(name)){
			sql.append(" ,vendor_add");
		}
		if(!StringUtil.IsEmpty(vendorName)){
			sql.append(" ,vendor_name");
		}
		if(!StringUtil.IsEmpty(remark)){
			sql.append(" ,remark");
		}
		if(!StringUtil.IsEmpty(device_model)){
			sql.append(" ,device_model");
		}
		sql.append(" ) values(").append(id).append(",").append(add_date);
		if(!StringUtil.IsEmpty(ouiId)){
			sql.append(" ,'").append(ouiId).append("' ");
		}
		if(!StringUtil.IsEmpty(name)){
			sql.append(" ,'").append(name).append("' ");
		}
		if(!StringUtil.IsEmpty(vendorName)){
			sql.append(" ,'").append(vendorName).append("' ");
		}
		if(!StringUtil.IsEmpty(remark)){
			sql.append(" ,'").append(remark).append("' ");
		}
		if(!StringUtil.IsEmpty(device_model)){
			sql.append(" ,'").append(device_model).append("' ");
		}
		sql.append(")");
		
		PrepareSQL psql = new PrepareSQL(sql.toString());
		
		try
		{
			return DBOperation.executeUpdate(psql.getSQL());
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	public int delOUI(String oui,String name,String deviceModel,String vendorName){
		
		String sql = "delete  from  tab_gw_device_init_oui where oui='"+oui+"' ";
		if(!StringUtil.IsEmpty(name)){
			sql = sql + " and vendor_add='"+ name + "' ";
		}
		if(!StringUtil.IsEmpty(deviceModel)){
			sql = sql + " and device_model='"+ deviceModel + "' ";
		}
		if(!StringUtil.IsEmpty(vendorName)){
			sql = sql + " and vendor_name='"+ vendorName + "' ";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	public List<HashMap<String,String>> qryOUI(String oui,String deviceModel,String vendorName,String name){
		StringBuffer sql = new StringBuffer();
		sql.append("select id,oui,vendor_add,vendor_name,remark,add_date,device_model from tab_gw_device_init_oui  where  1=1  ");
		if(!StringUtil.IsEmpty(oui) && !"0".equals(oui) ){
			sql.append(" and oui='").append(oui).append("' ");
		}
		if(!StringUtil.IsEmpty(vendorName) && !"0".equals(vendorName)){
			sql.append(" and vendor_name='").append(vendorName).append("' ");
		}
		if(!StringUtil.IsEmpty(deviceModel)){
			sql.append(" and device_model='").append(deviceModel).append("' ");
		}
		sql.append(" order by add_date desc ");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public String getId(){
		PrepareSQL psql = null;
		if (3 == DBUtil.GetDB()) { 
			psql = new PrepareSQL("select max(CAST(t.id AS SIGNED INTEGER)) id from tab_gw_device_init_oui t");
		} else {
			psql = new PrepareSQL("select max(TO_NUMBER(t.id)) id from tab_gw_device_init_oui t");
		}
		Map map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getStringValue(map.get("id"));
	}
}
