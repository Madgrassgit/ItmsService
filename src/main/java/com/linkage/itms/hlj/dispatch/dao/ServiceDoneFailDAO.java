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
public class ServiceDoneFailDAO
{
	public String queryFailResult(String deviceId,String username)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select  a.result_desc,a.result_id, b.fault_reason ");
		psql.append("  from  gw_serv_strategy a,tab_cpe_faultcode b  ");
		psql.append(" where 1=1 and a.result_id = fault_code ");
		psql.append(" and service_id=1001 and username= '"+username+"'" );
		psql.append(" and device_id= '"+deviceId+"'" );
		List<HashMap<String,String>> failResultList = DBOperation.getRecords(psql.getSQL());
		if(failResultList != null && !failResultList.isEmpty()){
			return StringUtil.getStringValue(failResultList.get(0),"fault_reason", "");
		}else{
			return "";
		}
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * 
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByDeviceId(String deviceId, String loid)
	{
		String sql = "select a.device_id,b.username,b.open_status from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id and b.serv_type_id = 10 and a.device_id= ? and a.username = ? ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		pSql.setString(2, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
}
