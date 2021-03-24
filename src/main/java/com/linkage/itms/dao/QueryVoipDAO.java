
package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryVoipDAO
{

	private static Logger logger = LoggerFactory.getLogger(QueryVoipDAO.class);

	/**
	 * 根据voip和user_id，查询企业网关语音信息
	 * 
	 * @param voip
	 * @param user_id
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryVoipInfo(String voip, String userId)
	{
		logger.debug("QueryVoipDAO==>queryVoipInfo({},{})", new Object[] { voip, userId });
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select a.vlanid ,b.protocol,b.voip_username,b.voip_passwd,c.prox_serv,c.prox_port,c.stand_prox_serv,c.stand_prox_port," + 
				"c.regi_serv,c.regi_port,c.stand_regi_serv,c.stand_regi_port,c.out_bound_proxy,c.out_bound_port,c.stand_out_bound_proxy," + 
				"c.stand_out_bound_port from egwcust_serv_info a,tab_egw_voip_serv_param b,tab_sip_info c " + 
				"where a.user_id=b.user_id and b.sip_id=c.sip_id and a.serv_type_id = '14' and b.user_id=");
		pSql.append(userId);
		pSql.append(" and b.voip_username='" + voip + "' ");
		return DBOperation.getRecords(pSql.getSQL());
	}
	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryegwUserByLoid(String loid)
	{
		String sql = " select distinct(a.user_id),a.device_id from tab_egwcustomer a where a.username= ? order by a.updatetime desc ";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
}
