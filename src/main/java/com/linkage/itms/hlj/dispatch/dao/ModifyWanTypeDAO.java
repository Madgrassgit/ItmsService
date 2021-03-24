package com.linkage.itms.hlj.dispatch.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ModifyWanTypeDAO
{
	public ArrayList<HashMap<String, String>> getMapByUser( String userId )
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select serv_type_id, username, vlanid, wan_type from hgwcust_serv_info ");
		psql.append(" where 1=1 and serv_type_id = 10 and user_id = " + userId);
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}
	
	public int updateWanType(String userId, String opTask){
		PrepareSQL psql = new PrepareSQL();
		psql.append("update hgwcust_serv_info ");
		if ("0".equals(opTask)){
			psql.append(" set wan_type = 2" );
		}else if ("1".equals(opTask)) {
			psql.append(" set wan_type = 1" );
		}
		psql.append(" where 1=1");
		psql.append(" and user_id = "+userId);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
}
