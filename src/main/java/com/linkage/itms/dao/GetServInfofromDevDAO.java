package com.linkage.itms.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

public class GetServInfofromDevDAO {

	/**
	 * 根据loid或deviceSN 查询业务记录
	 * @param queryType
	 * @param queryInfo
	 * @param servType
	 * @return
	 */
	public int queryRecordCount(String userId, int servType) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select count(1) as num from hgwcust_serv_info a");
		psql.append(" where a.user_id = ? and b.serv_type_id = ?");
		psql.setString(1, userId);
		psql.setInt(2, servType);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num");
	}

	public Map<String, String> queryUserInfor(int queryType, String queryInfo) {
		PrepareSQL psql = new PrepareSQL();
		switch (queryType) {
		// loid
		case 1:
			psql.append("select a.user_id,a.device_id,a.city_id,a.username from tab_hgwcustomer a");
			psql.append(" where a.username = '" + queryInfo + "'");
			break;
		// deviceSN
		case 2:
			psql.append("select a.user_id,a.device_id,a.city_id,a.username from tab_hgwcustomer a, tab_gw_device b");
			psql.append(" where a.device_id = b.device_id");
			psql.append(" and b.dev_sub_sn='" + queryInfo.substring(queryInfo.length() - 6) + "' ");
			psql.append(" and b.device_serialnumber like '%" + queryInfo + "'");
			break;
		default:
			break;
		}
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
}
