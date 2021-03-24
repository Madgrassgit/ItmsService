package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryBussinessInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryBussinessInfoDealXML.class);
	SAXReader reader = new SAXReader();

	String terminalType = "";
	String serviceList = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("workId"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("loId"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("pppoe"));
			
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryBussinessInfoDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		root.addElement("result").addText(result);
		root.addElement("err_msg").addText(errMsg);
		root.addElement("serial_number").addText(serialNumber);
		root.addElement("pppoe").addText(pppUsename);
		root.addElement("loId").addText(logicId);
		root.addElement("terminalType").addText(terminalType);
		root.addElement("serviceList").addText(serviceList);
		return document.asXML();
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public String getServiceList() {
		return serviceList;
	}

	public void setServiceList(String serviceList) {
		this.serviceList = serviceList;
	}
}
