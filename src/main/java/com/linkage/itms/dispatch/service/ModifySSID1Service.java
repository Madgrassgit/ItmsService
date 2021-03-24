package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.ModifySSID1Checker;
import com.linkage.itms.obj.ParameValueOBJ;


public class ModifySSID1Service implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(ModifySSID1Service.class);
	
	
	public String work(String inXml){
		
		logger.warn("ModifySSID1Service：inXml({})", inXml);
		
		ModifySSID1Checker checker = new ModifySSID1Checker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 2)
		{
			userMap = qdDao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMap = qdDao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMap = qdDao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMap = qdDao.queryUserByVoipAccount(checker.getUserInfo());
		}else{
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行修改操作，device_id={}", deviceId);
			
		    String ssidPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID";
		    
		    ParameValueOBJ pvOBJ = new ParameValueOBJ();
			
			pvOBJ.setName(ssidPath);
			pvOBJ.setValue(checker.getSsidName());
			pvOBJ.setType("1");
			
			int retResult = corba.setValue(deviceId, pvOBJ);
			
			pvOBJ = null;
			
			if (0 == retResult || 1 == retResult) {
				 checker.setResult(0);
				 checker.setResultDesc("成功");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}else if (-1 == retResult) {
				checker.setResult(1003);
				checker.setResultDesc("设备不能正常交互");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}else if (-6 == retResult) {
				checker.setResult(1003);
				checker.setResultDesc("设备不能正常交互");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}else if (-7 == retResult) {
				checker.setResult(1000);
				checker.setResultDesc("系统参数错误");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}else if (-9 == retResult) {
				checker.setResult(1000);
				checker.setResultDesc("系统内部错误");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}else {
				checker.setResult(1000);
				checker.setResultDesc("TR069错误");
				 String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "ModifySSID1Service");
				logger.warn(
						"servicename[ModifySSID1Service]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
				 return returnXml;
			}
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
