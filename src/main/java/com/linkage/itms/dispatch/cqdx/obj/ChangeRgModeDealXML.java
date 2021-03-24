package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class ChangeRgModeDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ChangeRgModeDealXML.class);
	
	private String rgMode;
	private String broadbandPassword;
	private String offlineEnable;
	private SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567898765";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			rgMode = StringUtil.getStringValue(inRoot.elementTextTrim("rg_mode"));
			broadbandPassword = StringUtil.getStringValue(inRoot.elementTextTrim("broadband_password"));
			offlineEnable = StringUtil.getStringValue(inRoot.elementTextTrim("offline_enable"));
			
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
			logger.error("QueryTerminalInfoDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[QueryTerminalInfoService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}else if(!"0".equals(result)){
					result = "-99";
				}
			}
		}
		catch (Exception e) {
			logger.error("QueryTerminalInfoDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		return document.asXML();
	}
	
	public String getRgMode() {
		return rgMode;
	}

	public void setRgMode(String rgMode) {
		this.rgMode = rgMode;
	}

	public String getBroadbandPassword() {
		return broadbandPassword;
	}

	public void setBroadbandPassword(String broadbandPassword) {
		this.broadbandPassword = broadbandPassword;
	}

	public String getOfflineEnable() {
		return offlineEnable;
	}

	public void setOfflineEnable(String offlineEnable) {
		this.offlineEnable = offlineEnable;
	}
}
