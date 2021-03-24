package com.linkage.itms.dispatch.cqdx.service;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.StartRebootDiagDealXML;
import com.linkage.itms.dispatch.service.DevRebootService;

public class StartRebootDiagService {
	private static Logger logger = LoggerFactory.getLogger(StartRebootDiagService.class);

	public String work(String inXml) {
		logger.warn("servicename[StartRebootDiagService]执行，入参为：{}", inXml);
		StartRebootDiagDealXML deal = new StartRebootDiagDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[StartRebootDiagService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		String customerId = deal.getCustomerId();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText(logicId);
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserInfo").addText(pppUsename);
		}
		else if(!StringUtil.IsEmpty(serialNumber)&&serialNumber.length()>=6) {
			// 设备序列号
			param.addElement("UserInfoType").addText("6");
			param.addElement("UserInfo").addText(serialNumber);
		}
		else if (!StringUtil.IsEmpty(customerId)){
			// 客户号
			param.addElement("UserInfoType").addText("7");
			param.addElement("UserInfo").addText(customerId);
		}
		deal.setResultXML(new DevRebootService().work(document.asXML()));
		return deal.returnXML();
	}
}
