package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryWorkTicketsInfoDealXML;

public class QueryWorkTicketsInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryWorkTicketsInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryWorkTicketsInfoService]执行，入参为：{}", inXml);
		QueryWorkTicketsInfoDealXML deal = new QueryWorkTicketsInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryWorkTicketsInfoService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		PublicDAO dao = new PublicDAO();

		String logicId = deal.getLogicId();
		if (StringUtil.IsEmpty(logicId)) {
			logger.warn("servicename[QueryWorkTicketsInfoService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}

		List<HashMap<String, String>> sheetInfos = dao.getBssSheetServInfo(logicId);
		if (null == sheetInfos || sheetInfos.isEmpty()) {
			logger.warn("servicename[QueryLanInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		
		String rest = "OK";
		StringBuffer workAsgnId = new StringBuffer();
		for (HashMap<String, String> map : sheetInfos) {
			if (!"1".equals(StringUtil.getStringValue(map, "open_status"))) {
				rest = "ERROR";
				workAsgnId.append(StringUtil.getStringValue(map, "orderid") + ",");
			}
		}
		deal.setResultStr(rest);
		deal.setWorkAsgnId(workAsgnId.toString());
		deal.setResult("0");
		deal.setErrMsg("成功！");
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("QueryWorkTicketsInfoService", logicId, "", inXml, ret);
		return ret;
	}
}
