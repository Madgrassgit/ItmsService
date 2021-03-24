package com.linkage.itms.dispatch.cqdx.service;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.ChangeWifiPasswordDealXML;
import com.linkage.itms.dispatch.service.MoSSIDPW;

public class ChangeWifiPasswordService {
	private static Logger logger = LoggerFactory.getLogger(ChangeWifiPasswordService.class);

	public String work(String inXml) {
		logger.warn("servicename[ChangeWifiPasswordService]执行，入参为：{}", inXml);
		ChangeWifiPasswordDealXML deal = new ChangeWifiPasswordDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[ChangeWifiPasswordService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserName").addText(logicId);
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserName").addText(pppUsename);
		}

		// 无线索引号  取值为SSID1，目前不支持修改公众WiFi的密码
		String ssid = deal.getSsid();
		if ("SSID1".equals(ssid)) {
			param.addElement("SSIDType").addText("1");
		}
		else {
			logger.warn("servicename[ChangeWifiPasswordService]入参ssid取值{}不正确", ssid);
			deal.setResult("-11");
			deal.setErrMsg("入参ssid取值不正确");
			return deal.returnXML();
		}
		// Wifi密码
		param.addElement("SSIDPW").addText(deal.getWifiPassword());

		deal.setResultXML(new MoSSIDPW().work(document.asXML()));
		return deal.returnXML();
	}
}
