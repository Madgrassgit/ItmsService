
package com.linkage.itms.dao;

import com.linkage.itms.dispatch.obj.BridgeToRoutChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.ct.obj.CtBaseChecker;
import com.linkage.itms.dispatch.obj.BaseChecker;
import com.linkage.itms.dispatch.obj.CloudBaseChecker;
import com.linkage.itms.dispatch.sxlt.obj.BaseDealXML;
import com.linkage.itms.nmg.dispatch.obj.NmgBaseChecker;
import com.linkage.itms.nx.dispatch.obj.NxBaseChecker;

import java.util.Map;

/**
 * 记录日志 1：BSS 2：IPOSS 3：综调 4：RADIUS 5:网厅 6:ITV
 * 
 * @author Jason(3412)
 * @date 2009-12-18
 */
public class RecordLogDAO
{

	private static Logger logger = LoggerFactory.getLogger(RecordLogDAO.class);

	/**
	 * 记录综调接口
	 * 
	 * @param dispatcher：综调接口对象基类
	 *            _methodName：调用方法名称 _username：用户账号
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordDispatchLog(BaseChecker dispatcher, String methodName,
			String userName)
	{
		recordLog(dispatcher.getCmdId(), dispatcher.getClientType(), methodName,
				userName, dispatcher.getDevSn(), dispatcher.getCityId(),
				dispatcher.getResult(), dispatcher.getCallXml(),
				dispatcher.getReturnXml(), System.currentTimeMillis() / 1000);
	}
	
	public void recordDispatchLog(BaseChecker dispatcher,int respCode, String userName,
			String methodName)
	{
		recordLog(dispatcher.getCmdId(), dispatcher.getClientType(), methodName,
				userName, dispatcher.getDevSn(), dispatcher.getCityId(),
				respCode, dispatcher.getCallXml(),
				dispatcher.getReturnXml(), System.currentTimeMillis() / 1000);
	}

	/**
	 * 记录综调接口
	 * 
	 * @param dispatcher：综调接口对象基类
	 *            _methodName：调用方法名称 _username：用户账号
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordDispatchLog(CloudBaseChecker dispatcher, String methodName,
			String userName)
	{
		recordLog(dispatcher.getCmdId(), dispatcher.getClientType(), methodName,
				userName, dispatcher.getDeviceSN(), dispatcher.getCityId(),
				dispatcher.getResult(), dispatcher.getCallXml(),
				dispatcher.getReturnXml(), System.currentTimeMillis() / 1000);
	}

	/**
	 * 记录综调接口
	 * 
	 * @param dispatcher：综调接口对象基类
	 *            _methodName：调用方法名称 _username：用户账号
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordDispatchLog(NmgBaseChecker dispatcher, String methodName,
			String userName)
	{
		recordLog(dispatcher.getCmdId(), dispatcher.getClientType(), methodName,
				userName, dispatcher.getDevSn(), dispatcher.getCityId(),
				dispatcher.getResult(), dispatcher.getCallXml(),
				dispatcher.getReturnXml(), System.currentTimeMillis() / 1000);
	}

	/**
	 * 记录综调接口
	 * 
	 * @param ctchecker：网厅接口对象基类
	 *            _methodName：调用方法名称 _username：用户账号
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordCtLog(CtBaseChecker ctchecker, String methodName)
	{
		recordLog(ctchecker.getCmdId(), ctchecker.getClientType(), methodName,
				ctchecker.getUsername(), ctchecker.getDevSn(), null,
				ctchecker.getResult(), ctchecker.getCallXml(), ctchecker.getReturnXml(),
				System.currentTimeMillis() / 1000);
	}

	/**
	 * 记录综调接口
	 * 
	 * @param dispatcher：综调接口对象基类
	 *            _methodName：调用方法名称 _username：用户账号
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordDispatchLog(NxBaseChecker dispatcher, String methodName,
			String userName)
	{
		recordLog(dispatcher.getCmdId(), dispatcher.getClientType(), methodName,
				userName, dispatcher.getDevSn(), dispatcher.getCityId(),
				dispatcher.getResult(), dispatcher.getCallXml(),
				dispatcher.getReturnXml(), System.currentTimeMillis() / 1000);
	}

	public void recordDispatchLog(BaseChecker dispatcher, long id, String userName)
	{
		updateRecordLog(dispatcher.getCmdId(), dispatcher.getClientType(), id, userName,
				dispatcher.getDevSn(), dispatcher.getCityId(), dispatcher.getResult(),
				dispatcher.getCallXml(), dispatcher.getReturnXml(),
				System.currentTimeMillis() / 1000);
	}

	/**
	 * serviceChange接口用
	 */
	public void recordDispatchLog(BaseDealXML dispatcher, long id, String userName)
	{
		updateRecordLog("-1", -1, id, userName, "-1", "0",
				StringUtil.getIntegerValue(dispatcher.getResult()), "",
				dispatcher.returnXML(), System.currentTimeMillis() / 1000);
	}

	/**
	 * 记录日志
	 * 
	 * @param itfsId:
	 *            调用接口唯一ID _clientTypeId：客户端ID类型 _cmdName：调用方法名称 _username：用户账号
	 *            _devSn：终端序列号 _cityId：属地ID _respCode：回复结果ID _reqInfo：调用接口字符串
	 *            _respInfo：回复字符串 _itfsTime：接口调用时间
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return void
	 */
	public void recordLog(String itfsId, int clientTypeId, String cmdName,
			String userName, String devSn, String cityId, int respCode,
			String reqInfo, String respInfo, long itfsTime)
	{
		logger.debug("recordLog({},{},{},{},{},{},{},{},{},{})",
				new Object[] { itfsId, clientTypeId, cmdName, userName, devSn,
						cityId, respCode, reqInfo, respInfo, itfsTime });
		String strSQL = "insert into log_gtms_service ("
				+ " serv_id,itfs_id,client_type_id,cmd_name,username,"
				+ " device_sn,city_id,resp_code,req_info,resp_info,"
				+ " itfs_time) values " + " (?,?,?,?,?,   ?,?,?,?,?,   ?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, getRandomId());
		psql.setString(2, itfsId);
		psql.setInt(3, clientTypeId);
		psql.setString(4, cmdName);
		psql.setString(5, userName);
		psql.setString(6, devSn);
		psql.setString(7, cityId);
		psql.setInt(8, respCode);
		psql.setString(9, reqInfo);
		psql.setString(10, respInfo);
		psql.setLong(11, itfsTime);
		DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 根据id更新结果
	 * 
	 * @param itfsId
	 * @param clientTypeId
	 * @param userName
	 * @param devSn
	 * @param cityId
	 * @param respCode
	 * @param reqInfo
	 * @param respInfo
	 * @param itfsTime
	 */
	public void updateRecordLog(String itfsId, int clientTypeId, long id,
			String userName, String devSn, String cityId, int respCode,
			String reqInfo, String respInfo, long itfsTime)
	{
		logger.debug("recordLog({},{},{},{},{},{},{},{},{},{})",
				new Object[] { itfsId, clientTypeId, id, userName, devSn, cityId,
						respCode, reqInfo, respInfo, itfsTime });
		String strSQL = "update log_gtms_service set itfs_id=?,client_type_id=?,username=?,"
				+ " device_sn=?,city_id=?,resp_code=?,resp_info=? where serv_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, itfsId);
		psql.setInt(2, clientTypeId);
		psql.setString(3, userName);
		psql.setString(4, devSn);
		psql.setString(5, cityId);
		psql.setInt(6, respCode);
		psql.setString(7, respInfo);
		psql.setLong(8, id);
		DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 入口日志
	 * 
	 * @param id
	 * @param inParam
	 * @param methodName
	 */
	public void recordLog(long id, String inParam, String methodName)
	{
		String strSQL = "insert into log_gtms_service (serv_id,itfs_id,client_type_id,cmd_name,req_info,itfs_time) values (?,?,?,?,?,?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, id);
		psql.setString(2, "-1");
		psql.setInt(3, -1);
		psql.setString(4, methodName);
		psql.setString(5, inParam);
		psql.setLong(6, System.currentTimeMillis() / 1000);
		DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 生成最大9位的随机数
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-15
	 * @return long
	 */
	public static long getRandomId()
	{
		return Math.round(Math.random() * 1000000000);
	}

	/***
	 * 路由桥接变更入表记录
	 * 
	 * @param dispatcher
	 * @param userInfoMap
	 * @param result
	 */
	public void recordRouteAndBridge(BridgeToRoutChecker dispatcher,
			Map<String, String> userInfoMap, String result, String resultDesc,
			String operType)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("insert into bridge_route_oper_log "
				+ " (loid,username,oper_action,oper_origon,oper_staff,"
				+ "  add_time,oper_result,result_desc) " + " values(?,?,?,?,?"
				+ "       ,?,?,?)");
		int clientType = dispatcher.getClientType();
		String clientTypeString = "";
		switch (clientType)
		{
			case 1:
				clientTypeString = "BSS";
				break;
			case 2:
				clientTypeString = "IPOSS";
				break;
			case 3:
				clientTypeString = "综调";
				break;
			case 4:
				clientTypeString = "RADIUS";
				break;
			case 5:
				clientTypeString = "爱运维";
				break;
			case 6:
				clientTypeString = "预处理";
				break;
			case 7:
				clientTypeString = "网监";
				break;
			default:
				break;
		}
		psql.setString(1, StringUtil.getStringValue(userInfoMap.get("username")));
		psql.setString(2, StringUtil.getStringValue(userInfoMap.get("netaccount")));
		// 1 桥接 路由改桥 2路由 桥改路由 3 桥改桥 4 路由改路由
		psql.setString(3, operType);
		psql.setString(4, clientTypeString);
		psql.setString(5, dispatcher.getCmdId());
		psql.setLong(6, System.currentTimeMillis() / 1000);
		psql.setString(7, result);
		psql.setString(8, resultDesc);
		DBOperation.executeUpdate(psql.getSQL());
	}
}
