package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetGatewayTerminalNumberDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class GetGatewayTerminalNumber {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayTerminalNumber.class);

	public String work(String inXml) {
		logger.warn("servicename[GetGatewayTerminalNumber]执行，入参为：{}", inXml);
		GetGatewayTerminalNumberDealXML deal = new GetGatewayTerminalNumberDealXML(inXml);
		if (false == deal.check()) {
			logger.warn("servicename[GetGatewayTerminalNumber]入参存在问题！");
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		int userType = 0;
		String username = "";
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2;
			username = logicId;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1;
			username = pppUsename;
		}
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn 
			userType = 6;
			username = serialNumber;
			
		}
		else {
			logger.warn("servicename[QueryLanInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		userMap = dao.queryUserInfoLan(userType, username);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[QueryLanInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get("device_id"))) {
			logger.warn("servicename[QueryLanInfoService]未绑定设备！");
			deal.setResult("-99");
			deal.setErrMsg("未绑定设备！");
			return deal.returnXML();
		}
		String deviceId = StringUtil.getStringValue(userMap, "device_id");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备正在被操作，不能获取节点值");
			deal.setTerminal_number_cpe(-1);
			return deal.returnXML();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			//采集上网数量节点
			String [] arr = new String[]{"InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.TotalAssociations"};
			// 调用Corba 获取节点的值
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr);
			
			if (null == objLlist || objLlist.isEmpty()) {
				deal.setResult("-99");
				deal.setErrMsg("节点值没有获取到，请确认终端是否支持查询");
				deal.setTerminal_number_cpe(-1);
				// 打印回参
				logger.warn("return=({})", deal.returnXML());
				return deal.returnXML();
			}
			int wlanNum = StringUtil.getIntegerValue(objLlist.get(0).getValue());
			deal.setTerminal_number_cpe(wlanNum);
			
			String ret = deal.returnXML();
			// 日志
			deal.recordLog("GetGatewayTerminalNumber", username, serialNumber, inXml, ret);
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("[{}]设备不在线，无法获取节点值", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			deal.setTerminal_number_cpe(-1);
			return deal.returnXML();
		}
		return deal.returnXML();
	}
}
