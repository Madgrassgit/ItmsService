package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

public class QueryMultiInterPortDAO {
	private static Logger logger = LoggerFactory.getLogger(QueryMultiInterPortDAO.class);
	/**
	 * 
	 * @param userType
	 * @param username
	 * @return
	 */
	public Map<String, String> queryServInfo(int userType, String username){
		logger.debug("QueryMultiInterPortDAO({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username ");
		
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
			psql.append(" from tab_hgwcustomer a left join hgwcust_serv_info b on a.user_id= b.user_id ");
			psql.append(" where  a.user_state = '1' ");
			psql.append(" and a.username = '" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	public List<HashMap<String, String>> queryMultiNetInfo(int userType,
			String username) {
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select b.vlanid,b.bind_port,b.username ");
		
		switch (userType) {
		case 1:
		case 3:
			psql.append("	from tab_hgwcustomer a, hgwcust_serv_info b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1 ");
			psql.append("	and b.username='" + username + "' ");
			psql.append("	and b.serv_type_id= 10 ");
			break;
		case 4:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append("  and c.voip_phone='" + username + "'");
			psql.append("	and b.serv_type_id= 10 ");
			break;
		case 5:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			psql.append("	and b.serv_type_id= 10 ");
			break;
		default:
			psql.append(" from tab_hgwcustomer a , hgwcust_serv_info b where a.user_state = '1' ");
			psql.append(" and a.user_id= b.user_id ");
			psql.append(" and a.username = '" + username + "'");
			psql.append(" and b.serv_type_id= 10 ");
		}
		return DBOperation.getRecords(psql.getSQL());
	}
}
