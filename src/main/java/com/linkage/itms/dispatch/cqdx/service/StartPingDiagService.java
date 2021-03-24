package com.linkage.itms.dispatch.cqdx.service;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.StartPingDiagDealXML;
import com.linkage.itms.dispatch.service.PingDiagnostic;

public class StartPingDiagService {
	private static Logger logger = LoggerFactory.getLogger(StartPingDiagService.class);
	public String work(String inXml) {
		StartPingDiagDealXML deal = new StartPingDiagDealXML();
		logger.warn("servicename[StartPingDiagService]执行，入参为：{}", inXml);
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[StartPingDiagService]解析数据错误");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		if(StringUtil.isEmpty(deal.getOpId()) || StringUtil.isEmpty(deal.getPingSize()) ||
				StringUtil.isEmpty(deal.getPingTimes()) || StringUtil.isEmpty(deal.getPingHost())){
			logger.warn("servicename[StartPingDiagService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
	
		int userInfoType = 0;
		String userInfo = "";
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		String customerId = deal.getCustomerId();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userInfoType = 2;
			userInfo = logicId;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userInfoType = 1;
			userInfo = pppUsename;
		}
		else if(!StringUtil.IsEmpty(serialNumber) && serialNumber.length()>=6) {
			// 设备sn
			userInfoType = 6;
			userInfo = serialNumber;
		}
		else if(!StringUtil.IsEmpty(customerId)) {
			// 设备sn
			userInfoType = 7;
			userInfo = customerId;
		}
		else {
			logger.warn("servicename[StartPingDiagService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		param.addElement("UserInfoType").addText(StringUtil.getStringValue(userInfoType));
		param.addElement("UserInfo").addText(userInfo);
		
		// 属地默认
		param.addElement("CityId").addText("00");
		param.addElement("WanPassageWay").addText("2");//1是tr069
		param.addElement("PackageByte").addText(deal.getPingSize());
		param.addElement("IPOrDomainName").addText(deal.getPingHost());
		param.addElement("PackageNum").addText( deal.getPingTimes());
		param.addElement("TimeOut").addText("2000");
		String resultCode="0";
		String errMsg ="";
		try {
			SAXReader reader = new SAXReader();
			Document inDocument = reader.read(new StringReader(new PingDiagnostic().work(document.asXML())));
			Element inRoot = inDocument.getRootElement();
			
			resultCode = inRoot.elementTextTrim("RstCode");
			if("0".equals(resultCode)){
				errMsg = "成功！";
			}else if("1002".equals(resultCode) || "1004".equals(resultCode) || "1000".equals(resultCode)){
				resultCode = "-1";
				errMsg = "不存在用户！";
			}else if("1005".equals(resultCode) || "1006".equals(resultCode)){
				resultCode = "-2";
				errMsg = "终端不在线，无法连接！";
			}else{
				resultCode = "-10";
				errMsg = "平台异常或繁忙!";
			}
			
			deal.setSuccessCount(StringUtil.getStringValue(inRoot.elementTextTrim("SuccesNum")));
			deal.setFailureCount(StringUtil.getStringValue(inRoot.elementTextTrim("FailNum")));
			deal.setAverageResponsetime(StringUtil.getStringValue(inRoot.elementTextTrim("AvgResponseTime")));
			deal.setMinimumResponsetime(StringUtil.getStringValue(inRoot.elementTextTrim("MinResponseTime")));
			deal.setMaximumResponsetime(StringUtil.getStringValue(inRoot.elementTextTrim("MaxResponseTime")));	
		} catch (DocumentException e) {
			resultCode = "-10";
			errMsg = "平台异常或繁忙!";
		}
		deal.setResult(resultCode);
		deal.setErrMsg(errMsg);
		String ret = deal.returnXML();
		deal.recordLog("StartPingDiagService", userInfo, serialNumber, inXml, ret);
		return deal.returnXML();
	}
}
