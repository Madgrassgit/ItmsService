package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-12-4
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class A8setVoipDAO
{
	private static Logger logger = LoggerFactory.getLogger(A8setVoipDAO.class);
	
	
	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid,String voipphone,String voipport)
	{
		String table_customer="";
		String table_voip="";
		table_customer = "tab_egwcustomer";
		table_voip="tab_egw_voip_serv_param";
		String sql = "select a.user_id,a.device_id from "+table_customer+" a where  a.username= ?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	/**
	 * 判断用户是否开通语音业务
	 * @param loid
	 * @param voipphone
	 * @param voipport
	 * @return
	 */
	public List<HashMap<String, String>> queryvoip(String userid,String voipphone,String voipport)
	{
		String table_customer="";
		String table_voip="";
		table_customer = "tab_egwcustomer";
		table_voip="tab_egw_voip_serv_param";
		String sql = "select * from "+table_customer+" a ,"+table_voip+" b where a.user_id=b.user_id and a.user_id= ? and b.voip_phone=?";
		if (3 == DBUtil.GetDB()) {
			sql = "select a.user_id from "+table_customer+" a ,"+table_voip+" b where a.user_id=b.user_id and a.user_id= ? and b.voip_phone=?";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, userid);
		pSql.setString(2, voipphone);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	/**
	 * 判断同loid下该端口是否被占用
	 * @param loid
	 * @return
	 */
	public int querysum(String loid,String voipphone,String voipport)
	{
		String table_customer="";
		String table_voip="";
		table_customer = "tab_egwcustomer";
		table_voip="tab_egw_voip_serv_param";
		String sql="select count(b.line_id) as num from "+table_customer+" a ,"+table_voip+" b where a.user_id = b.user_id  and a.username= ? and b.voip_phone=? and b.line_id=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		pSql.setString(2, voipphone);
		pSql.setString(3, voipport);
		Map<String, String> map = DBOperation.getRecord(pSql.getSQL());
		return StringUtil.getIntValue(map,"num");
	}
	/**
	 * 修改语音端口
	 * @param userid
	 * @param voipphone
	 * @param voipport
	 * @return
	 */
	public int UpdateVoipprot(String userid,String voipphone,String voipport)
	{
		String table_voip="";
		table_voip="tab_egw_voip_serv_param";
		
		String sql="update "+table_voip+" set line_id=? where user_id=?  and voip_phone=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, voipport);
		psql.setString(2, userid);
		psql.setString(3, voipphone);
		return DBOperation.executeUpdate(psql.getSQL());
	}
}
