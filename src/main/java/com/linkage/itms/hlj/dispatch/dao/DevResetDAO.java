package com.linkage.itms.hlj.dispatch.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.itms.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-1
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DevResetDAO
{
	private static Logger logger = LoggerFactory.getLogger(DevResetDAO.class);
	
	public static void updateCustStatus(long userId)
	{
		String tableName= " hgwcust_serv_info ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableName = "egwcust_serv_info";
		}
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("update ");
		pSql.append(tableName);
		pSql.append(" set open_status=0,updatetime=? where user_id=? and serv_status in (1,2) and open_status!=0");
		int index = 0;
		pSql.setLong(++index, new DateTimeUtil().getLongTime());
		pSql.setLong(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update table[{}] rows[{}].", tableName, updateRows);
	}
	
	/**
	 * 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
	 * @param userId
	 */
	public static void updateCustStatusFailure(long userId) {
		String tableName= " hgwcust_serv_info ";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			tableName = " egwcust_serv_info ";
		}
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("update ");
		pSql.append(tableName);
		pSql.append(" set open_status=1,updatetime=? where user_id=? and serv_status in (1,2) and open_status = 0");
		int index = 0;
		pSql.setLong(++index, new DateTimeUtil().getLongTime());
		pSql.setLong(++index, userId);
		int updateRows = DBOperation.executeUpdate(pSql.getSQL());
		logger.info("update table[{}] rows[{}].", tableName, updateRows);
	}
	
	/**
	 * 下发
	 * @author 岩 
	 * @date 2016-8-4
	 * @param userId
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> getMapByUser( String userId )
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select serv_type_id, username, vlanid from hgwcust_serv_info ");
		psql.append(" where 1=1 and serv_type_id = 10 and user_id = " + userId);
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}
}
