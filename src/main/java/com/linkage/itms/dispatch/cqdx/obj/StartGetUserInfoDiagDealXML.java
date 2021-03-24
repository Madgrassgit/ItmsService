package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class StartGetUserInfoDiagDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(StartGetUserInfoDiagDealXML.class);
	SAXReader reader = new SAXReader();

	String authUsername = "";
	String pppPassword = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_usename"));
			customerId = StringUtil.getStringValue(inRoot.elementTextTrim("customer_id"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			authUsername = StringUtil.getStringValue(inRoot.elementTextTrim("auth_username"));
			return inDocument;
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("op_id").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		
		Element userinfo = root.addElement("userinfo");
		userinfo.addElement("logic_id").addText(logicId);
		userinfo.addElement("ppp_usename").addText(pppUsename);
		userinfo.addElement("serial_number").addText(serialNumber);
		userinfo.addElement("ppp_password").addText(pppPassword);
		userinfo.addElement("customer_id").addText(customerId);
		userinfo.addElement("auth_username").addText(authUsername);

		return document.asXML();
	}

	public String getAuthUsername() {
		return authUsername;
	}

	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}

	public String getPppPassword() {
		return pppPassword;
	}

	public void setPppPassword(String pppPassword) {
		this.pppPassword = pppPassword;
	}
}
