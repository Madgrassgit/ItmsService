
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.ZeroConfigReportChecker;

/**
 * 零配置报表dao
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年12月23日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ZeroConfigReportDAO
{

	private static Logger logger = LoggerFactory.getLogger(ZeroConfigReportDAO.class);
	/**
	 * 业务类型：家庭网关e8-b
	 */
	private static final int SERVTYPE_ITMS_E8B = 1;
	/**
	 * 业务类型：家庭网关e8-c
	 */
	private static final int SERVTYPE_ITMS_E8C = 2;
	/**
	 * 业务类型：政企网关
	 */
	private static final int SERVTYPE_BBMS = 3;
	/**
	 * 业务类型：机顶盒
	 */
	private static final int SERVTYPE_STB = 4;

	/**
	 * 查询用户信息
	 * 
	 * @param serviceType
	 *            业务类型
	 * @param userInfo
	 *            用户loid或业务账号
	 * @return
	 */
	public Map<String, String> getUserInfo(int serviceType, String userInfo)
	{
		logger.debug("ZeroConfigReportDAO==>getUserInfo({},{})", serviceType, userInfo);
		String query_userinfo_sql = "";
		if (SERVTYPE_ITMS_E8B == serviceType || SERVTYPE_ITMS_E8C == serviceType)
		{
			query_userinfo_sql = "select user_id from tab_hgwcustomer where username = ? ";
		}
		if (SERVTYPE_BBMS == serviceType)
		{
			query_userinfo_sql = "select user_id from tab_egwcustomer where username = ? ";
		}
		else if (SERVTYPE_STB == serviceType)
		{
			if("jl_dx".equals(Global.G_instArea)){
				query_userinfo_sql = "select customer_id, cpe_mac from stb_tab_customer where serv_account = ? ";
			}else{
				query_userinfo_sql = "select customer_id from stb_tab_customer where serv_account = ? ";
			}
		}
		PrepareSQL psql = new PrepareSQL(query_userinfo_sql);
		psql.setString(1, userInfo);
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		if (null != list && !list.isEmpty())
		{
			// 机顶盒用户表可能会查出多条用户，此时就返回第一条用户。家庭网关和政企网关只会查出一条用户。
			return list.get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 保存报表信息
	 * 
	 * @param checker
	 */
	public void saveZeroConfigReport(ZeroConfigReportChecker checker)
	{
		String sql = "insert into tab_zeroconfig_report (cmdid, cmd_type, client_type, service_type, operate_type, user_info,"
				+ "device_sn, update_time, city_id) values (?,?,?,?,?,?,?,?,?)";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, checker.getCmdId());
		psql.setString(2, checker.getCmdType());
		psql.setInt(3, checker.getClientType());
		psql.setInt(4, checker.getServiceType());
		psql.setInt(5, checker.getOperateType());
		psql.setString(6, checker.getUserInfo());
		psql.setString(7, checker.getDevSn());
		psql.setLong(8, checker.getUpdateTime());
		psql.setString(9, checker.getCityId());
		DBOperation.executeUpdate(psql.getSQL());
	}

	public void updateZeroConfigReport(int intfRs, String cmdID)
	{
		String sql = "update tab_zeroconfig_report set inft_result = ? where cmdid=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setInt(1, intfRs);
		psql.setString(2, cmdID);
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	public Map<String, String> getCmdIdCount(String cmdID)
	{
		String sql = "select count(1) cmdid_num from tab_zeroconfig_report where cmdid=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, cmdID);
		return DBOperation.getRecord(psql.getSQL());
		
	}
	
	public void updateCpeMac(String cpe_mac, String serv_account)
	{
		PrepareSQL pSQL = new PrepareSQL(" update stb_tab_customer set cpe_mac=?  where serv_account=? ");
		pSQL.setString(1, cpe_mac);
		pSQL.setString(2, serv_account);
		DBOperation.executeUpdate(pSQL.getSQL());
	}
}
