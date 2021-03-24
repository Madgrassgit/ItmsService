package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class ChangeWifiPasswordDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ChangeWifiPasswordDealXML.class);
	SAXReader reader = new SAXReader();

	String ssid = "";
	String wifiPassword = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			ssid = StringUtil.getStringValue(inRoot.elementTextTrim("ssid"));
			wifiPassword = StringUtil.getStringValue(inRoot.elementTextTrim("wifi_password"));

			Document document = DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element root = document.addElement("root");
			// 接口调用唯一ID
			root.addElement("CmdID").addText(opId);
			// 结果代码
			root.addElement("CmdType").addText("CX_01");
			// 结果描述
			root.addElement("ClientType").addText("3");
			return document;
		} catch (Exception e) {
			logger.error("ChangeWifiPasswordDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		String isSuccess = "";
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[ChangeWifiPasswordService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("NoReason");
				isSuccess = inRoot.elementTextTrim("IsSuccess");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}
				if("0".equals(isSuccess)){
					result = "0";
				}
			}
		}
		catch (Exception e) {
			logger.error("ChangeWifiPasswordDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		// 结果代码
		root.addElement("RstCode").addText(result);
		// 结果描述
		root.addElement("failed_reason").addText(errMsg);
		// 修改密码是否成功
		root.addElement("result").addText(result);
		root.addElement("logic_id").addText(logicId);
		root.addElement("ppp_username").addText(pppUsename);
		return document.asXML();
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getWifiPassword() {
		return wifiPassword;
	}

	public void setWifiPassword(String wifiPassword) {
		this.wifiPassword = wifiPassword;
	}
}
