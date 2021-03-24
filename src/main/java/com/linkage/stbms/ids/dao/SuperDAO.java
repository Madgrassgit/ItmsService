
package com.linkage.stbms.ids.dao;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.obj.StrategyOBJ;

public class SuperDAO
{

	private static final Logger logger = LoggerFactory.getLogger(SuperDAO.class);
	/**
	 * 增加策略
	 */
	public Boolean addStrategy(StrategyOBJ obj)
	{
		logger.debug("addStrategy({})", obj);
		if (obj == null)
		{
			logger.debug("obj == null");
			return false;
		}
		ArrayList<String> sqlList = strategySQL(obj);
		int result = doBatch(sqlList);
		if (result > 0)
		{
			logger.debug("策略入库：  成功");
			return true;
		}
		else
		{
			logger.debug("策略入库：  失败");
			return false;
		}
	}

	/**
	 * 执行批量SQL.
	 * 
	 * @param arrsql
	 *            SQL语句数组
	 * @return 返回操作的记录条数
	 */
	public int doBatch(ArrayList<String> sqlList)
	{
		int result = 0;
		if(sqlList != null && !sqlList.isEmpty())
		{
			result = DBOperation.executeUpdate(sqlList);
		}
		return result;
	}

	/**
	 * 生成入策略的sql语句
	 * 
	 * @author wangsenbo
	 * @date Jun 11, 2010
	 * @param
	 * @return List<String>
	 */
	public ArrayList<String> strategySQL(StrategyOBJ obj)
	{
		logger.debug("strategySQL({})", obj);
		if (obj == null)
		{
			return null;
		}
		ArrayList<String> sqlList = new ArrayList<String>();
		StringBuilder tempSql = new StringBuilder();
		tempSql.append("delete from gw_serv_strategy where device_id='").append(
				obj.getDeviceId()).append("' and temp_id=").append(obj.getTempId());
		// 生成入策略的sql语句
		StringBuilder sql = new StringBuilder();
		sql.append("insert into gw_serv_strategy (");
		sql
				.append("id,acc_oid,time,type,gather_id,device_id,oui,device_serialnumber,username"
						+ ",sheet_para,service_id,task_id,order_id,sheet_type,temp_id,is_last_one");
		sql.append(") values (");
		sql.append(obj.getId());
		sql.append("," + obj.getAccOid());
		sql.append("," + obj.getTime());
		sql.append("," + obj.getType());
		sql.append("," + StringUtil.getSQLString(obj.getGatherId()));
		sql.append("," + StringUtil.getSQLString(obj.getDeviceId()));
		sql.append("," + StringUtil.getSQLString(obj.getOui()));
		sql.append("," + StringUtil.getSQLString(obj.getSn()));
		sql.append("," + StringUtil.getSQLString(obj.getUsername()));
		sql.append("," + StringUtil.getSQLString(obj.getSheetPara()));
		sql.append("," + obj.getServiceId());
		sql.append("," + StringUtil.getSQLString(obj.getTaskId()));
		sql.append("," + obj.getOrderId());
		sql.append("," + obj.getSheetType());
		sql.append("," + obj.getTempId());
		sql.append("," + obj.getIsLastOne());
		sql.append(")");
		// 生成入策略日志的sql语句
		StringBuilder logsql = new StringBuilder();
		logsql.append("insert into gw_serv_strategy_log (");
		logsql
				.append("id,acc_oid,time,type,gather_id,device_id,oui,device_serialnumber,username"
						+ ",sheet_para,service_id,task_id,order_id,sheet_type,temp_id,is_last_one");
		logsql.append(") values (");
		logsql.append(obj.getId());
		logsql.append("," + obj.getAccOid());
		logsql.append("," + obj.getTime());
		logsql.append("," + obj.getType());
		logsql.append("," + StringUtil.getSQLString(obj.getGatherId()));
		logsql.append("," + StringUtil.getSQLString(obj.getDeviceId()));
		logsql.append("," + StringUtil.getSQLString(obj.getOui()));
		logsql.append("," + StringUtil.getSQLString(obj.getSn()));
		logsql.append("," + StringUtil.getSQLString(obj.getUsername()));
		logsql.append("," + StringUtil.getSQLString(obj.getSheetPara()));
		logsql.append("," + obj.getServiceId());
		logsql.append("," + StringUtil.getSQLString(obj.getTaskId()));
		logsql.append("," + obj.getOrderId());
		logsql.append("," + obj.getSheetType());
		logsql.append("," + obj.getTempId());
		logsql.append("," + obj.getIsLastOne());
		logsql.append(")");
		sqlList.add(tempSql.toString());
		sqlList.add(sql.toString());
		sqlList.add(logsql.toString());
		logger.debug("入策略的sql语句-->{}", tempSql.toString() + ";" + sql.toString() + ";"
				+ logsql.toString());
		PrepareSQL psql = new PrepareSQL(tempSql.toString());
		psql.getSQL();
		psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		psql = new PrepareSQL(logsql.toString());
		psql.getSQL();
		return sqlList;
	}
}
