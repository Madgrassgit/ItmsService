package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryTerminalPasswdDealXML;

public class QueryTerminalPasswdService {
	private static Logger logger = LoggerFactory.getLogger(QueryTerminalPasswdService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryTerminalPasswdService]执行，入参为：{}", inXml);
		QueryTerminalPasswdDealXML deal = new QueryTerminalPasswdDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryTerminalPasswdService]解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String userInfo = "";
		int userType = 0;
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userInfo = logicId;
			userType = 2;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userInfo = pppUsename;
			userType = 1;
		}else {
			logger.warn("servicename[QueryTerminalPasswdService]入参格式错误！");
			return deal.returnXML();
		}
		PublicDAO publicDAO = new PublicDAO();
		List<HashMap<String, String>> deviceList = publicDAO.queryDeviceInfo(userInfo, userType);
		if(null == deviceList || deviceList.isEmpty() || null == deviceList.get(0)){
			logger.warn("servicename[QueryTerminalPasswdService]不存在对应的额设备信息！");
			return deal.returnXML();
		}
		
		HashMap<String,String> deviceMap = deviceList.get(0);
		deal.setLogicId(StringUtil.getStringValue(deviceMap, "loid"));
		deal.setPppUsename(StringUtil.getStringValue(deviceMap,"pppoe"));
		deal.setTerminalPassword(StringUtil.getStringValue(deviceMap,"x_com_passwd"));
		
		//deal.setResultXML(new NetPasswordService().work(document.asXML()));
		return deal.returnXML();
	}
}
