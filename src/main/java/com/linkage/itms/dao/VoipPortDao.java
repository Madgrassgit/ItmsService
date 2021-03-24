package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
		
public class VoipPortDao
{
	
	private static Logger logger = LoggerFactory.getLogger(VoipPortDao.class);
	/**
	 * @description 可通过输入物理号码（语音号码），查询该物理号码在工单中的端口信息
	 * @param  userInfo:电话号码
	 * @author cczhong
	 * @return Map<String,String>
	 */
	public Map<String, String> queryVoipPort(String userInfo)
	{
		logger.debug("queryVoipPort({})", userInfo);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select line_id from tab_voip_serv_param where voip_phone=? ");
		psql.setString(1, userInfo);
		return DBOperation.getRecord(psql.getSQL());
			
	}
}

	