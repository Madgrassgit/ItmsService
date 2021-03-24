package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;


public class XJVoipProtocolDao {
	
	private static final Logger logger = LoggerFactory.getLogger(XJVoipProtocolDao.class);
	
	/**
	 * 根据用户ID(user_id)查询VOIP语音协议类型
	 * 
	 * @param user_id
	 * @return
	 */
	public Map<String, String> queryXJVoipProtocol(String user_id) {
		
		logger.debug("XJVoipProtocolDao==>queryXJVoipProtocol({})", user_id);


		PrepareSQL psql = new PrepareSQL();
		
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_voip = "tab_egw_voip_serv_param";
		}
		
		// mysql db
		if (3 == DBUtil.GetDB()) {
			psql.append("select protocol from " + table_voip + " where 1=1 and user_id = ");
		}
		else
		{
			psql.append("select * from " + table_voip + " where 1=1 and user_id = ");
		}
		psql.append(user_id);
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
}
