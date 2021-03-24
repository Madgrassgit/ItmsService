package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OperateSSIDChecker;
import com.linkage.itms.obj.ParameValueOBJ;


public class OperateSSID  {
	
	private static final Logger logger = LoggerFactory
			.getLogger(OperateSSID.class);
	public String work(String inXml,String SSIDType) {
		// TODO Auto-generated method stublogger.warn("setParameterValues：inXml=({})", new Object[]{inXml});
		
		OperateSSIDChecker checker = new OperateSSIDChecker(inXml,SSIDType);
		if (false == checker.check()) {
			logger.error(
					"servicename[OperateSSID]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[OperateSSID]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),
						inXml });
		//获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		//测试是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		
		String deviceId = "";
		

		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
				.getUserInfoType(), checker.getUsername());
		
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[OperateSSID]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUsername()});
			checker.setResult(1002);
			checker.setResultDesc("查不到对应的客户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 用户信息存在   再判断此用户是否绑定了设备
		else {
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
			
			if ("".equals(deviceId)) {
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]此用户没有绑定设备",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			// 判断设备是否在线，只有设备在线，才可以设置设备的节点信息
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			
			// 设备正在被操作
			if (-3 == flag) {
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]设备正在被操作，无法设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1008);
				checker.setResultDesc("设备正在被操作");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]设备在线，可以设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				ParameValueOBJ pvOBJ = checker.getPvOBJ();
				
				// 调用Corba 设置节点的值
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]调用Corba，设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				int retResult = corba.setValue(deviceId, pvOBJ);
				
				if (0 == retResult || 1 == retResult) {
					 checker.setResult(0);
					 checker.setIsSucc(0);
					 checker.setResultDesc("节点值设置成功");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-1 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("设备连接失败");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-6 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("设备正被操作");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-7 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("系统参数错误");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-9 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("系统内部错误");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("TR069错误");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
					logger.warn(
							"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}
			}
			// 设备不在线
			else {
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]设备不在线，无法设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1000);
				checker.setIsSucc(1);
				checker.setResultDesc("设备不在线，无法设置节点值");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "OperateSSID");
				logger.warn(
						"servicename[OperateSSID]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
				 return returnXml;
			}
		}
	}

}
