package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class StartRebootDiagDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(StartRebootDiagDealXML.class);
	SAXReader reader = new SAXReader();

	@Override
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_usename"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			customerId = StringUtil.getStringValue(inRoot.elementTextTrim("customer_id"));
			
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
			logger.error("StartRebootDiagDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	@Override
	public String returnXML() {
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[StartRebootDiagService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}
				// 终端重启失败
				else if ("1006".equals(result)) {
					result = "-4";
				}
			}
		}
		catch (Exception e) {
			logger.error("StartRebootDiagDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("op_id").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);

		return document.asXML();
	}
}
