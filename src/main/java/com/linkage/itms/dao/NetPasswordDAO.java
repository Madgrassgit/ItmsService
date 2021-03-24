package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author Administrator (Ailk No.)
 * @version 1.0
 * @since 2013-12-5
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class NetPasswordDAO
{
	private static Logger logger = LoggerFactory.getLogger(NetPasswordDAO.class);
	
	public Map<String,String> queryNetPassword(int userType, String username){
		logger.debug("queryNetPassword({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		PrepareSQL psql = new PrepareSQL();
		
		switch (userType)
		{   
			// loid
			case 1:
				// mysql db
				if (3 == DBUtil.GetDB()) {
					psql.append("select a.user_id, b.username, b.passwd from tab_hgwcustomer a left join (select user_id,username,passwd from hgwcust_serv_info where serv_type_id = 10) b on a.user_id = b.user_id where a.username = '"+username+"'");
				}
				else
				{
					psql.append("select a.user_id, b.username, b.passwd from tab_hgwcustomer a left join (select * from hgwcust_serv_info where serv_type_id = 10) b on a.user_id = b.user_id where a.username = '"+username+"'");
				}
				break;
			// 宽带账号
			case 2:
				psql.append("select username,passwd from hgwcust_serv_info where username='" + username + "'");
				break;
			default:
				psql.append("select username,passwd from hgwcust_serv_info where 1=2");
				break;
		}
		return DBOperation.getRecord(psql.getSQL());
	}
}
