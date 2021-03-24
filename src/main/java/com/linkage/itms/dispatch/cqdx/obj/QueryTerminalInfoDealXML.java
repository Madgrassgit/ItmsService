package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryTerminalInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryTerminalInfoDealXML.class);
	SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567898765";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_usename"));
			
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
		String vendor = "";
		String devModel = "";
		String version = "";
		String oui = "";
		String devSn = "";
		String online = "";
		String accessTypeId = "";
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
				}
				Element param = inRoot.element("Param");
				vendor = param.elementTextTrim("vendor");
				devModel = param.elementTextTrim("DevModel");
				// TODO 版本是硬件版本还是软件版本
//				version = param.elementTextTrim("HandwareVersion");
				version = param.elementTextTrim("SoftwareVersion");
				oui = param.elementTextTrim("DevOUI");
				devSn = param.elementTextTrim("DevSN");
				online = param.elementTextTrim("online");
				accessTypeId = param.elementTextTrim("accessTypeId");
			}
		}
		catch (Exception e) {
			logger.error("QueryTerminalInfoDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("queryTerminalInfoReturn");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		// 厂家
		root.addElement("manufactory").addText(vendor);
		// 型号
		root.addElement("device_type").addText(devModel);
		// 版本
		root.addElement("version").addText(version);
		// OUI
		root.addElement("OUI").addText(oui);
		// 序列号
		root.addElement("serial_number").addText(devSn);
		root.addElement("terminal_type").addText(accessTypeId);
		root.addElement("is_online").addText(getOnline(online));
		return document.asXML();
	}
	
	private String getOnline(String online){
		if(StringUtil.isEmpty(online)){
			return "";
		}
		if("1".equals(online)){
			return "true";
		}
		return "false";
	}
	
	private String getAccessName(String accessTypeId){
		if("1".equals(accessTypeId)){
			return "ADSL";
		}
		if("2".equals(accessTypeId)){
			return "LAN";
		}
		if("3".equals(accessTypeId)){
			return "EPON";
		}
		if("4".equals(accessTypeId)){
			return "GPON";
		}
		return "";
	}
}
