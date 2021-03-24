package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryActionInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryActionInfoDealXML.class);
	SAXReader reader = new SAXReader();

	String actionResult = "";
	String uplinkType = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("workId"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("loId"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("pppoe"));
			
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryActionInfoDealXML.getXML() is error!", e);
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
		root.addElement("pppoe").addText(pppUsename);
		root.addElement("loId").addText(logicId);
		root.addElement("actionResult").addText(actionResult);
		root.addElement("uplinkType").addText(uplinkType);
		return document.asXML();
	}

	public String getActionResult() {
		return actionResult;
	}

	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}

	public String getUplinkType() {
		return uplinkType;
	}

	public void setUplinkType(String uplinkType) {
		this.uplinkType = uplinkType;
	}
}
