package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.StartGetUserInfoDiagDealXML;

public class StartGetUserInfoDiagService {
	private static Logger logger = LoggerFactory.getLogger(StartGetUserInfoDiagService.class);

	public String work(String inXml) {
		logger.warn("servicename[StartGetUserInfoDiagService]执行，入参为：{}", inXml);
		StartGetUserInfoDiagDealXML deal = new StartGetUserInfoDiagDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[StartGetUserInfoDiagService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		if(StringUtil.isEmpty(deal.getOpId())){
			logger.warn("servicename[StartGetUserInfoDiagService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		String authUsername = deal.getAuthUsername();
		String customerId = deal.getCustomerId();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userMap = dao.queryUserInfo(2, logicId);
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userMap = dao.queryUserInfo(1, pppUsename);
		}
		else if(!StringUtil.IsEmpty(serialNumber) && serialNumber.length() >=6) {
			// 设备sn 
			//String deviceId = dao.queryDeviceId(serialNumber);
			userMap = dao.queryUserInfo(3,serialNumber);
		}
		else if(!StringUtil.IsEmpty(authUsername)) {
			// 用户VOIP账号
			userMap = dao.queryUserInfo(5, authUsername);
		}
		else if (!StringUtil.IsEmpty(customerId)) {
			// 客户号
			userMap = dao.queryUserInfo(7, customerId);
		}
		else {
			logger.warn("servicename[StartGetUserInfoDiagService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[StartGetUserInfoDiagService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		
		deal.setResult("0");
		deal.setErrMsg("成功");
		deal.setLogicId(StringUtil.getStringValue(userMap, "loid"));
		deal.setSerialNumber(StringUtil.getStringValue(userMap, "device_serialnumber"));		
		deal.setCustomerId(StringUtil.getStringValue(userMap, "customer_id"));
		deal.setPppUsename(StringUtil.getStringValue(userMap, "ppp_usename"));
		deal.setPppPassword(StringUtil.getStringValue(userMap, "ppp_password"));
		deal.setAuthUsername(StringUtil.getStringValue(userMap, "auth_username"));
		
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("StartGetUserInfoDiagService", "", serialNumber, inXml, ret);
		return ret;
	}
}
