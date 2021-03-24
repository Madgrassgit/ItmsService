
package com.linkage.stbms.ids.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;
import com.linkage.stbms.itv.main.Global;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-18
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbConfigResultDAO
{

	public Map<String, String> getDeviceIdStr(String searchType, String searchInfo)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id, a.oui, a.device_serialnumber, a.customer_id, a.serv_account, a.cpe_allocatedstatus ");
		psql.append(" from " + CommonUtil.addPrefix("tab_gw_device") + " a ");
		psql.append(" where 1 = 1 ");
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType))
		{
			psql.append("   and a.serv_account = '" + searchInfo + "' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("2".equals(searchType))
		{
			psql.append("   and a.cpe_mac = '" + searchInfo + "' ");
		}
		// searchType=3时searchInfo=机顶盒序列号
		else if ("3".equals(searchType))
		{
			psql.append("   and a.dev_sub_sn = '"
					+ searchInfo.substring(searchInfo.length() - 6) + "' ");
			psql.append("   and a.device_serialnumber like '%" + searchInfo + "' ");
		}
		// else // searchType=1时searchInfo=接入帐号
		// if ("4".equals(searchType)) {
		// psql.append("   and b.pppoe_user = '"+searchInfo+"@itv' ");
		// }
		// 江西电信需求 按照最近一次上报时间倒序，也就是取最近一次连接时间的 数据
		if ("jx_dx".equals(Global.G_instArea))
		{
			psql.append(" order by a.cpe_currentupdatetime desc");
		}
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty())
		{
			return null;
		}
		else
		{
			return map;
		}
	}
	
	public Map<String, String> getReturnValue( String deviceId){
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select device_id, return_value, bind_way ");
		psql.append(" from stb_tab_zeroconfig_fail where 1=1 ");
		psql.append(" and device_id= "+deviceId);
		psql.append(" order by start_time desc");
		
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty())
		{
			return null;
		}
		else
		{
			return map;
		}
	}
}
