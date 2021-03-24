package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.MaxConnectionNumberChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class MaxConnectionNumberService implements IService {
	private static Logger logger = LoggerFactory.getLogger(MaxConnectionNumberService.class);

	@Override
	public String work(String inXml) {
		logger.warn("maxConnectionNumberService==>inXml({})", inXml);
		MaxConnectionNumberChecker checker = new MaxConnectionNumberChecker(inXml);
		if (false == checker.check()) {
			logger.warn("servicename[maxConnectionNumberService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		Map<String, String> userInfoMap = deviceInfoDAO.queryUserInfoForGS(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.size() == 0) {
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			logger.warn("servicename[maxConnectionNumberService]cmdId[{}]userInfo[{}]查无此客户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		// 设备不存在
		if (StringUtil.IsEmpty(StringUtil.getStringValue(userInfoMap, "device_id", ""))) {
			checker.setResult(1003);
			checker.setResultDesc("未绑定设备");
			logger.warn("servicename[maxConnectionNumberService]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}

		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
		long user_id = StringUtil.getLongValue(userInfoMap, "user_id");

		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (-6 == flag) {
			checker.setResult(1008);
			checker.setResultDesc("设备正在被操作");
			logger.warn("servicename[maxConnectionNumberService]cmdId[{}]userInfo[{}]设备正在被操作，无法获取节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
		}
		else if (1 == flag) {
			logger.warn("[{}]设备在线，可以进行操作", deviceId);
			String path = "InternetGatewayDevice.Services.X_CT-COM_MWBAND.TotalTerminalNumber";
			ParameValueOBJ obj = new ParameValueOBJ();
			obj.setName(path);
			obj.setValue(checker.getTotalTerminalNumber());
			obj.setType("1");
			int retResult = corba.setValue(deviceId, obj);
			if (0 == retResult || 1 == retResult) {
				String username="";
				if (1 == checker.getUserInfoType()) {
					username=checker.getUserInfo();
				}
				deviceInfoDAO.updateMaxNetNum(checker.getTotalTerminalNumber(), user_id, username);
				
				checker.setResult(0);
				checker.setResultDesc("成功");
			}else {
				checker.setResult(1000);
				checker.setResultDesc("修改最大连接数失败");
			}
			
		}else {
			checker.setResult(0);
			checker.setResultDesc("设备不在线");
			logger.warn("servicename[maxConnectionNumberService]cmdId[{}]userInfo[{}]设备不在线",new Object[] { checker.getCmdId(), checker.getUserInfo() });
		}
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "maxConnectionNumber");
		return checker.getReturnXml();
	}

}
