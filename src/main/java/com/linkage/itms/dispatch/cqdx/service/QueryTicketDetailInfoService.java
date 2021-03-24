package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryTicketsDetailInfoDealXML;

public class QueryTicketDetailInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryTicketDetailInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryTicketDetailInfoService]执行，入参为：{}", inXml);
		QueryTicketsDetailInfoDealXML deal = new QueryTicketsDetailInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryWorkTicketsInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		PublicDAO dao = new PublicDAO();

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		
		List<HashMap<String, String>> sheetInfos = new ArrayList<HashMap<String,String>>();
		if (!StringUtil.IsEmpty(logicId)) {
			sheetInfos = dao.getBussinessInfo(2, logicId, null);
		}else if(!StringUtil.IsEmpty(pppUsename)){
			sheetInfos = dao.getBussinessInfo(1, pppUsename, null);
		}else{
			logger.warn("servicename[QueryLanInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		if (null == sheetInfos || sheetInfos.isEmpty() || null == sheetInfos.get(0)) {
			logger.warn("servicename[QueryLanInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		List<Map<String,String>> ticketInfoList = new ArrayList<Map<String,String>>();
		for (HashMap<String, String> map : sheetInfos) {
			Map<String,String> ticketInfoMap = new HashMap<String, String>();
			ticketInfoMap.put("work_asgn_id", StringUtil.getStringValue(map.get("orderid")));
			ticketInfoMap.put("service_type", getServTypeId(StringUtil.getStringValue(map.get("serv_type_id"))));
			ticketInfoMap.put("service_opt", StringUtil.getStringValue(map.get("serv_status")));
			ticketInfoMap.put("account_name", StringUtil.getStringValue(map.get("pppusename")));
			ticketInfoMap.put("serial_number", StringUtil.getStringValue(map.get("device_serialnumber")));
			ticketInfoMap.put("loid", StringUtil.getStringValue(map.get("loid")));
			ticketInfoMap.put("exec_result", StringUtil.getStringValue(map.get("open_status")));
			ticketInfoList.add(ticketInfoMap);
		}
		deal.setTicketInfoList(ticketInfoList);
		deal.setResult("0");
		deal.setErrMsg("执行成功!");
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("QueryTicketDetailInfoService", logicId, "", inXml, ret);
		return ret;
	}
	
	private String getServTypeId(String servTypeId){
		if("10".equals(servTypeId)){
			return "21";
		}
		if("11".equals(servTypeId)){
			return "22";
		}
		if("14".equals(servTypeId)){
			return "23";
		}
		return servTypeId;
	}
}
