package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.SetGatewayTerminalNumberDealXML;
import com.linkage.itms.obj.ParameValueOBJ;

public class SetGatewayTerminalNumberService {
	private static Logger logger = LoggerFactory.getLogger(SetGatewayTerminalNumberService.class);

	public String work(String inXml) {
		logger.warn("servicename[SetGatewayTerminalNumberService]执行，入参为：{}", inXml);
		SetGatewayTerminalNumberDealXML deal = new SetGatewayTerminalNumberDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();
		String username = deal.getLogicId();
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		if(StringUtil.getIntegerValue(deal.getTerminalNumber()) > 30){
			logger.warn("servicename[SetGatewayTerminalNumberService]修改无线终端数最多30！");
			deal.setResult("-99");
			deal.setErrMsg("Terminal number can't more than 30!");
			return deal.returnXML();
		}
		//String customerId = deal.getCustomerId();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userMap = dao.queryUserInfo(2, logicId);
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userMap = dao.queryUserInfo(1, pppUsename);
		}
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn 
			//String deviceId = dao.queryDeviceId(serialNumber);
			userMap = dao.queryUserInfo(3,serialNumber);
			
		}
//		else if (!StringUtil.IsEmpty(customerId)) {
//			// 客户号
//			userMap = dao.queryUserInfo(7, customerId);
//		}
		else {
			logger.warn("servicename[SetGatewayTerminalNumberService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[SetGatewayTerminalNumberService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
//		else if(userMap.size() > 1){
//			logger.warn("servicename[StartGetUserInfoDiagService]查询到多个用户！");
//			deal.setResult("-1");
//			deal.setErrMsg("查询到多个用户！");
//			return deal.returnXML();
//		}
		
		String deviceId = StringUtil.getStringValue(userMap, "device_id");
		
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			//采集上网数量节点
			ParameValueOBJ pvOBJ = null;
			pvOBJ = new ParameValueOBJ();
//			pvOBJ.setName("InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.TotalAssociations");
			pvOBJ.setName("InternetGatewayDevice.Services.X_CT-COM_MWBAND.TotalTerminalNumber");
			pvOBJ.setType("2");
			pvOBJ.setValue(deal.getTerminalNumber());
			
			int retResult = corba.setValue(deviceId, pvOBJ);
			
			if (0 == retResult || 1 == retResult) {
				deal.setResult("0");
				deal.setErrMsg("成功");
			}else if (-1 == retResult) {
				deal.setResult("-99");
				deal.setErrMsg("执行失败,连接不上设备");
			}else if (-6 == retResult) {
				deal.setResult("-99");
				deal.setErrMsg("设备正在被操作");
			}else if (-7 == retResult) {
				deal.setResult("-99");
				deal.setErrMsg("系统参数错误");
			}else if (-9 == retResult) {
				deal.setResult("-99");
				deal.setErrMsg("系统内部错误");
			}else {
				deal.setResult("-99");
				deal.setErrMsg("TR069错误");
			}
			String ret = deal.returnXML();
			// 日志
			deal.recordLog("SetGatewayTerminalNumberService", username, serialNumber, inXml, ret);
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("[{}]设备不在线，无法获取节点值", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("SetGatewayTerminalNumberService", "", serialNumber, inXml, ret);
		return ret;
	}
}
