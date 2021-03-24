
package com.linkage.itms.radius.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.radius
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusSyncDAO
{

	private static final Logger logger = LoggerFactory.getLogger(RadiusSyncDAO.class);

	/**
	 * 根据用户账号查询用户信息
	 * @param username 用户名，不能为空
	 * @return 如果用户不存在，返回null，如果存在，返回Map集合
	 */
	public Map<String, String> queryCustomer(String username)
	{
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select a.user_id,b.device_id,b.oui,b.device_serialnumber"
				+ " from hgwcust_serv_info a, tab_gw_device b"
				+ " where a.user_id = b.customer_id and a.username=? and a.serv_type_id=10 and a.wan_type = 2");
		int index = 0;
		pSql.setString(++index, username);
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * 根据用户账号更新用户密码,并将用户状态重置为未做
	 * @param username 用户名
	 * @param password 用户密码
	 * @return 返回更新数据条数
	 */
	public int updateCustomer(String username, String password,String wanType)
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
		pSql.setString(++index, password);
		pSql.setLong(++index, new  DateTimeUtil().getLongTime());
		pSql.setString(++index, username);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update customer by username[{}] and password[{}], update rows[{}]",
				new Object[] { username, password, updateRows });
		return updateRows;
	}

	/**
	 * 根据用户账号更新用户密码,并将用户状态重置为未做
	 * @param username 用户名
	 * @param password 用户密码
	 * @return 返回更新数据条数
	 */
	public int updateCustomerJxdx(String username, String password,String wanType,String userId)
	{
		// 如果接入方式为路由则用户状态重置为未做，否则只修改密码
		String sql = "update hgwcust_serv_info set passwd=?,updatetime=? ";
		if("2".equals(wanType))
		{
			sql += " , open_status=0  ";
		}
		sql += " where user_id=? and serv_type_id=10 ";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, password);
		pSql.setLong(++index, new  DateTimeUtil().getLongTime());
		pSql.setString(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update customer by username[{}] and password[{}], update rows[{}]",
				new Object[] { username, password, updateRows });
		return updateRows;
	}

	/**
	 * 根据用户帐号查询用户的接入方式
	 * @param account
	 * @return
	 */
	public Map<String,String> queryCustomerByAccout(String account)
	{
		String sql = "select user_id,wan_type,updatetime from hgwcust_serv_info where username=? and serv_type_id=10";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, account);
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * 根据用户帐号查询用户的接入方式
	 * @param account
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryCustomerByAccoutJxdx(String account)
	{
		String sql = "select user_id,wan_type,updatetime from hgwcust_serv_info where username=? and serv_type_id=10";
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setString(++index, account);
		return DBOperation.getRecords(pSql.getSQL());
	}

	/**
	 * 根据用户账号查询用户信息
	 * @param userId 用户名，不能为空
	 * @return 如果用户不存在，返回null，如果存在，返回Map集合
	 */
	public Map<String, String> queryCustomerJxdx(String userId)
	{
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select a.user_id,b.device_id,b.oui,b.device_serialnumber"
				+ " from hgwcust_serv_info a, tab_gw_device b"
				+ " where a.user_id = b.customer_id and a.user_id=? and a.serv_type_id=10 ");
		int index = 0;
		pSql.setString(++index, userId);
		return DBOperation.getRecord(pSql.getSQL());
	}
}
