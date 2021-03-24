package com.linkage.stbms.ids.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2018-1-23
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbLastTimeDAO
{
	private static Logger logger = LoggerFactory.getLogger(GetStbLastTimeDAO.class);
	
	
	/**
	 * 根据业务账号查询最后一次上线时间
	 * @param searchType
	 * @param userInfo
	 * @return
	 */
	public List<HashMap<String, String>> queryLastTime(int searchType,String userInfo)
	{
		StringBuffer sql=new StringBuffer();
		 sql.append("select c.last_time from stb_tab_customer a,stb_tab_gw_device b, stb_gw_devicestatus c where a.customer_id=b.customer_id and b.device_id=c.device_id  ");
			sql.append(" and a.serv_account='"+userInfo+"'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		return DBOperation.getRecords(psql.getSQL());
	}
	public List<HashMap<String, String>> querycustomer(int searchType,String userInfo)
	{
		StringBuffer sql=new StringBuffer();
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql.append("select a.serv_account from stb_tab_customer a where a.serv_account='"+userInfo+"'");
		}
		else
		{
			sql.append("select * from stb_tab_customer a where a.serv_account='"+userInfo+"'");
		}
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		return DBOperation.getRecords(psql.getSQL());
	}
	/**
	 * 根据机顶盒mac或机顶盒序列号查询机顶盒一次上线时间
	 * @param searchType 查询类型 1、业务账号 2、机顶盒mac 3、机顶盒序列号
	 * @param userInfo
	 * @return
	 */
	public List<HashMap<String, String>> queryLastTime1(int searchType,String userInfo)
	{
		StringBuffer sql=new StringBuffer();
		 sql.append("select c.last_time from stb_tab_gw_device b,stb_gw_devicestatus c where    b.device_id=c.device_id  ");
		 if(searchType==2)
		{
			sql.append(" and b.cpe_mac='"+userInfo+"'");
		}else {
			String devSubSn = userInfo.substring(userInfo.length() - 6, userInfo.length());
			sql.append(" and b.dev_sub_sn='"+devSubSn+"'");
			sql.append(" and b.device_serialnumber like '%" + userInfo + "' ");
		}
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		return DBOperation.getRecords(psql.getSQL());
	}
}
