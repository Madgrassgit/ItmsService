package com.linkage.itms.dispatch.cqdx.service;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.QueryRgModeInfoDealXML;
import com.linkage.itms.dispatch.service.QueryWanTypeService;

public class QueryRgModeInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryRgModeInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryRgModeInfoService]执行，入参为：{}", inXml);
		QueryRgModeInfoDealXML deal = new QueryRgModeInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryRgModeInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		
		if(!StringUtil.IsEmpty(deal.getPppUsename())){
			// 宽带账号
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserInfo").addText(deal.getPppUsename());
		}
		else if(!StringUtil.IsEmpty(logicId)){
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText(logicId);
		}
		else{
			logger.warn("servicename[QueryRgModeInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("宽带帐号、逻辑id为空！");
			return deal.returnXML();
		}
	
		deal.setResultXML(new QueryWanTypeService().work(document.asXML()));
		return deal.returnXML();
	}
}
