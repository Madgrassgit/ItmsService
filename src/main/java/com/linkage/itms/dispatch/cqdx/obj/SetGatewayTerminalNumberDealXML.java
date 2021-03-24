package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class SetGatewayTerminalNumberDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(SetGatewayTerminalNumberDealXML.class);
	SAXReader reader = new SAXReader();

	String terminalNumber = "";
	String offlineEnable = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			terminalNumber = StringUtil.getStringValue(inRoot.elementTextTrim("terminal_number"));
			if(StringUtil.IsEmpty(terminalNumber)){
				terminalNumber = "4";
			}
			offlineEnable = inRoot.elementTextTrim("offline_enable");
			return inDocument;
		} catch (Exception e) {
			logger.error("SetGatewayTerminalNumberDealXML.getXML() is error!", e);
			result = "-99";
			errMsg = "解析入参错误！";
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("result_code").addText(result);
		//返回结果说明
		root.addElement("result_desc").addText(errMsg);

		return document.asXML();
	}

	public String getTerminalNumber() {
		return terminalNumber;
	}

	public void setTerminalNumber(String terminalNumber) {
		this.terminalNumber = terminalNumber;
	}

	public String getOfflineEnable() {
		return offlineEnable;
	}

	public void setOfflineEnable(String offlineEnable) {
		this.offlineEnable = offlineEnable;
	}
}
