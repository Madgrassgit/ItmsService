package com.linkage.itms.dispatch.cqdx.dao;

import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class IPTVLanDAO
{
	/**
	 * 
	 * @param userName userName
	 * @return
	 */
	public List<HashMap<String,String>> iptvPortByUserName(String userName)
	{
		String strSQL = "select b.username,b.real_bind_port,b.bind_port from tab_hgwcustomer a,hgwcust_serv_info b "
				+ "where a.user_id=b.user_id and b.serv_type_id=11 and a.username=? ";
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, userName);
		return DBOperation.getRecords(psql.getSQL());
	}
}
