package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryTicketsDetailInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryTicketsDetailInfoDealXML.class);
	private List<Map<String,String>> ticketInfoList;
	private SAXReader reader = new SAXReader();
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567890987";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryWorkTicketsInfoDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		//root.addElement("op_id").addText(opId);
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		Element resultInfo = root.addElement("result_info");
		if(null != ticketInfoList && !ticketInfoList.isEmpty() && null != ticketInfoList.get(0)){
			for(Map<String,String> ticketInfoMap : ticketInfoList){
				Element ticketInfo = resultInfo.addElement("ticket_info");
				ticketInfo.addElement("work_asgn_id").addText(ticketInfoMap.get("work_asgn_id"));
				ticketInfo.addElement("service_type").addText(ticketInfoMap.get("service_type"));
				ticketInfo.addElement("service_opt").addText(ticketInfoMap.get("service_opt"));
				ticketInfo.addElement("account_name").addText(ticketInfoMap.get("account_name"));
				ticketInfo.addElement("serial_number").addText(ticketInfoMap.get("serial_number"));
				ticketInfo.addElement("logic_id").addText(ticketInfoMap.get("loid"));
				ticketInfo.addElement("exec_result").addText(ticketInfoMap.get("exec_result"));
			}
		}
		
		return document.asXML();
	}

	public List<Map<String, String>> getTicketInfoList() {
		return ticketInfoList;
	}

	public void setTicketInfoList(List<Map<String, String>> ticketInfoList) {
		this.ticketInfoList = ticketInfoList;
	}
}
