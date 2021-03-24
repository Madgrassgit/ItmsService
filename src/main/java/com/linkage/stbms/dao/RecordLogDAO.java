package com.linkage.stbms.dao;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * @author Jason(3412)
 * @date 2009-12-18
 */
public class RecordLogDAO {

	private static Logger logger = LoggerFactory.getLogger(RecordLogDAO.class);
	
	
	public void recordDispatchLog(String inParam, String _methodName,
			String retParam) {
		recordLog("admin", inParam, null, retParam, 1);
	}
	
	
	/**
	 * 
	 * @param username  操作员
	 * @param call      入参
	 * @param deviceId
	 * @param ret       回参
	 * @param retId
	 * @param method    方法名
	 */
	public void recordLog(String username, String call, String deviceId, String ret, int retId, String method) {
		logger.debug("recordLog()");
		//操作员IP, 日志类型：4接口, 操作时间s, 操作名称:1查询, 操作对象：2username, 操作内容, 
		//操作终端:device_id, 结果描述, 操作结果：1成功 0失败, 子类型
		String strSQL = "insert into tab_oper_log (acc_oid,acc_login_ip,operationlog_type,"
			+ "operation_time,operation_name,operation_object,operation_content,operation_device,"
			+ "operation_result,result_id)"
			+ " values (?,?,?,?,?,  ?,?,?,?,?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setInt(1, 1);
		psql.setString(2, "192.168.0.1");
		psql.setInt(3, 4);  // 4 表示接口日志
		psql.setLong(4, new Date().getTime()/1000);
		psql.setString(5, method);
		psql.setString(6, username);
		psql.setString(7, call);
		psql.setString(8, deviceId);
		psql.setString(9, ret);
		psql.setInt(10, retId);

		DBOperation.executeUpdate(psql.getSQL());
	}
	
	
	/**
	 * 记录接口调用日志
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-18
	 * @return void
	 */
	public void recordLog(String username, String call, String deviceId, String ret, int retId) {
		logger.debug("recordLog()");
		//操作员IP, 日志类型：4接口, 操作时间s, 操作名称:1查询, 操作对象：2username, 操作内容, 
		//操作终端:device_id, 结果描述, 操作结果：1成功 0失败, 子类型
		String strSQL = "insert into tab_oper_log (acc_oid,acc_login_ip,operationlog_type,"
			+ "operation_time,operation_name,operation_object,operation_content,operation_device,"
			+ "operation_result,result_id)"
			+ " values (?,?,?,?,?,  ?,?,?,?,?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setInt(1, 1);
		psql.setString(2, "192.168.0.1");
		psql.setInt(3, 4);
		psql.setLong(4, new Date().getTime()/1000);
		psql.setString(5, "1");
		psql.setString(6, username);
		psql.setString(7, call);
		psql.setString(8, deviceId);
		psql.setString(9, ret);
		psql.setInt(10, retId);

		DBOperation.executeUpdate(psql.getSQL());
	}

}
