package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class ChangeBroadbandInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ChangeBroadbandInfoDealXML.class);
	
	private String userAddress;
	private String areaCode;
	private String subareaCode;
	private String vlanId;
	private SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567898765";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			userAddress = StringUtil.getStringValue(inRoot.elementTextTrim("user_address"));
			areaCode = StringUtil.getStringValue(inRoot.elementTextTrim("area_code"));
			subareaCode = StringUtil.getStringValue(inRoot.elementTextTrim("subarea_code"));
			vlanId = StringUtil.getStringValue(inRoot.elementTextTrim("vlan_id"));
			
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
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		return document.asXML();
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getSubareaCode() {
		return subareaCode;
	}

	public void setSubareaCode(String subareaCode) {
		this.subareaCode = subareaCode;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

	public SAXReader getReader() {
		return reader;
	}

	public void setReader(SAXReader reader) {
		this.reader = reader;
	}
}
