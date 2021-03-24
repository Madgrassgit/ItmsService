
package com.linkage.stbms.ids.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;

/**
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbConfInfoDAO
{

	/**
	 * @param searchType
	 * @param searchInfo
	 * @return
	 */
	public Map<String, String> getStbConfInfo(String searchType, String searchInfo)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.serv_account,b.serv_pwd,b.auth_url,b.user_status");
		psql.append(" from " + CommonUtil.addPrefix("tab_gw_device") + " a, "
				+ CommonUtil.addPrefix("tab_customer") + " b");
		psql.append(" where a.customer_id = b.customer_id ");
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
		psql.append(" order by a.cpe_currentupdatetime desc");
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
