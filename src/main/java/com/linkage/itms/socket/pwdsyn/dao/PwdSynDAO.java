
package com.linkage.itms.socket.pwdsyn.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.socket.pwdsyn.bean.PwdSynBean;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.pwdsyn.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PwdSynDAO
{

	private static final Logger logger = LoggerFactory.getLogger(PwdSynDAO.class);

	public Map<String, String> queryCustomer(String account)
	{
		String sql = "select user_id,wan_type,updatetime from hgwcust_serv_info where username=? and serv_type_id=10";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, account);
		return DBOperation.getRecord(pSql.getSQL());
	}

	public Map<String, String> queryDevice(String userId)
	{
		String sql = "select device_id,oui,device_serialnumber from tab_gw_device where customer_id = ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, userId);
		return DBOperation.getRecord(pSql.getSQL());
	}

	public boolean updateCustomerPwd(PwdSynBean bean,String wanType)
	{
		// 如果接入方式为路由则用户状态重置为未做，否则只修改密码
		String sql = "update hgwcust_serv_info set passwd=?,updatetime=? ";
		if("2".equals(wanType))
		{
			sql += " , open_status=0  ";
		}
		sql += " where username=? and serv_type_id=10 ";
		
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, bean.getUserPwd());
		pSql.setLong(++index, bean.getTimestamp());
		pSql.setString(++index, bean.getAccount());
		int rows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update rows[{}]", rows);
		return rows > 0;
	}
}
