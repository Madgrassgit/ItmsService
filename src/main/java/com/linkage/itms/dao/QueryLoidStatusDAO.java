package com.linkage.itms.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-6-30
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryLoidStatusDAO
{
	
	public Map<String, String> getSnByUser( String userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_serialnumber,b.open_status from  tab_hgwcustomer a,hgwcust_serv_info b  ");
		psql.append(" where  a.user_id=b.user_id  and  b.serv_type_id = 10  and b.serv_status=1 ");
		psql.append(" and a.user_id = " + userId + "");
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
	
	/**
	 * 获取userId
	 * @author 岩 
	 * @date 2016-6-30
	 * @param loid
	 * @return
	 */
	public String getUserIdByLoid(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  user_id ");
		psql.append("  from tab_hgwcustomer ");
		psql.append(" where 1=1");
		psql.append(" and username = '"+loid+"'" );
		Map<String,String> userIdList = DBOperation.getRecord(psql.getSQL());
		if(userIdList != null && !userIdList.isEmpty()){
			return StringUtil.getStringValue(userIdList,"user_id", "");
		}else{
			return "";
		}
	}
}
