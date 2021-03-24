package com.linkage.itms.dispatch.cqdx.service;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.QueryTerminalInfoDealXML;
import com.linkage.itms.dispatch.service.QueryDeviceInfoService;

public class QueryTerminalInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryTerminalInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryTerminalInfoService]执行，入参为：{}", inXml);
		QueryTerminalInfoDealXML deal = new QueryTerminalInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryTerminalInfoService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String netName = deal.getPppUsename();
		
		if(!StringUtil.IsEmpty(logicId)){
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserName").addText(logicId);	
		}else if(!StringUtil.IsEmpty(netName)){
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserName").addText(netName);	
		}else{
			logger.warn("servicename[QueryTerminalInfoService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		// 下发业务查询类型  1：根据用户信息下发 2：根据设备序列号下发  这里给1
		param.addElement("SearchType").addText("1");
		// TODO 入参会验证cityId，业务逻辑中用不到，先随意给个值
		param.addElement("CityId").addText("00");
		// TODO 入参会验证devSN，业务逻辑中用不到，先随意给个值
		param.addElement("DevSN").addText("00");

		deal.setResultXML(new QueryDeviceInfoService().work(document.asXML()));
		return deal.returnXML();
	}
}
