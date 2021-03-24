
package com.linkage.stbms.ioperation.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.util.CommonUtil;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-6-5
 * @category com.linkage.stbms.ioperation.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class IOperationDAO
{

	/**
	 * 判断mac地址是否在数据库中存在。
	 */
	public boolean isMacExist(String mac)
	{
		String sql = "select count(1) count_mac from "+CommonUtil.addPrefix("tab_devmac_init")+" where cpe_mac = ? ";
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(sql);
		pSql.setString(1, mac);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		return result == null ? false
				: StringUtil.getIntValue(result, "count_mac", 0) > 0;
	}
	
}
