package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

public class KaidianOpenDao {
	private static Logger logger = LoggerFactory.getLogger(KaidianOpenDao.class);
	/**
	 * 
	 * @param userType
	 * @param username
	 * @return
	 */
	public Map<String, String> queryServInfo(int userType, String username){
		logger.debug("KaidianOpenDao({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id ");
		
		switch (userType) {
		case 1:
		case 3:
			psql.append("	from tab_hgwcustomer a, hgwcust_serv_info b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "'");
			break;
		case 4:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			break;
		case 5:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			break;
		default:
			psql.append(" from tab_hgwcustomer a where a.user_state = '1'");
			psql.append(" and a.username = '" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}
}
