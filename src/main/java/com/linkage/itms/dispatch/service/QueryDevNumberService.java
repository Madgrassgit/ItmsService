package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryDevNumberChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QueryDevNumberService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDevNumberService.class);
	
	
	public String work(String inXml){
		
		logger.warn("queryDevNumber：inXml({})", inXml);
		
		QueryDevNumberChecker checker = new QueryDevNumberChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdnDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1)
		{
			userMap = qdnDao.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 2)
		{
			userMap = qdnDao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMap = qdnDao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMap = qdnDao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMap = qdnDao.queryUserByVoipAccount(checker.getUserInfo());
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
		
		// 判断设备是否在线，只有设备在线，才可以获取设备的节点信息
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
			logger.warn("设备在线，可以获取节点值，device_id={}", deviceId);
			
			String [] arr = new String[]{"InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.TotalAssociations"};
			
			// 调用Corba 获取节点的值
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr);
			
			if (null == objLlist || objLlist.isEmpty()) {
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			checker.setDevNumber(StringUtil.getIntegerValue(objLlist.get(0).getValue(), 0));
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"queryDevNumber");

			
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
			
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
