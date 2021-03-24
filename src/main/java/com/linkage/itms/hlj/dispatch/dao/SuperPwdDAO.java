package com.linkage.itms.hlj.dispatch.dao;

import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-28
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SuperPwdDAO
{
	public String querySuperPwd(String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  x_com_passwd ");
		psql.append("  from  tab_gw_device  ");
		psql.append(" where 1=1");
		psql.append(" and device_id= '"+deviceId+"'" );
		List<HashMap<String,String>> superPwdList = DBOperation.getRecords(psql.getSQL());
		if(superPwdList != null && !superPwdList.isEmpty()){
			return StringUtil.getStringValue(superPwdList.get(0),"x_com_passwd", "");
		}else{
			return "";
		}
	}
	
	public int updateSuperPwd(String newSuperPwd,String deviceId){
		PrepareSQL psql = new PrepareSQL();
		psql.append("update tab_gw_device ");
			psql.append(" set x_com_passwd = '"+newSuperPwd+"'" );
		psql.append(" where 1=1");
		psql.append(" and device_id = '"+deviceId+"'" );
		return DBOperation.executeUpdate(psql.getSQL());
	}
}
