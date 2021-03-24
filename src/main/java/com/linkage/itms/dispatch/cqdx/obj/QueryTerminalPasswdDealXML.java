package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryTerminalPasswdDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryTerminalPasswdDealXML.class);
	
	private String terminalPassword = "";
	
	SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("workId"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("loId"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("pppoe"));
			
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryTerminalPasswdDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		root.addElement("loId").addText(logicId);
		root.addElement("pppoe").addText(pppUsename);
		root.addElement("terminalPassword").addText(terminalPassword);
		return document.asXML();
	}

	public String getTerminalPassword() {
		return terminalPassword;
	}

	public void setTerminalPassword(String terminalPassword) {
		this.terminalPassword = terminalPassword;
	}
}
