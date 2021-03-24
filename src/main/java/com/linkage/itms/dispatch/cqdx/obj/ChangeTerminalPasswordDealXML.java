package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 修改或重置终端超级密码
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class ChangeTerminalPasswordDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ChangeTerminalPasswordDealXML.class);
	SAXReader reader = new SAXReader();
	// 宽带账号
	String terminalPassword = "";
	String returnPwd = "";

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("ppp_username");
			terminalPassword = inRoot.elementTextTrim("terminal_password");

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
			logger.error("QueryRgModeInfoDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
//		try {
//			if (!StringUtil.IsEmpty(resultXML)) {
//				logger.warn("servicename[QueryRgModeInfoService]解析回参：{}", resultXML);
//				Document inDocument = reader.read(new StringReader(resultXML));
//				Element inRoot = inDocument.getRootElement();
//				opId = inRoot.elementTextTrim("CmdID");
//				result = inRoot.elementTextTrim("RstCode");
//				errMsg = inRoot.elementTextTrim("RstMsg");
//				// 不存在用户
//				if ("1002".equals(result)) {
//					result = "-1";
//				}
//			}
//		}
//		catch (Exception e) {
//			logger.error("QueryRgModeInfoDealXML.returnXML() is error!", e);
//		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		Element resultInfo = root.addElement("result_info");
		// 终端超级密码
		resultInfo.addElement("terminal_password").addText(returnPwd);
		return document.asXML();
	}

	public String getTerminalPassword() {
		return terminalPassword;
	}

	public void setTerminalPassword(String terminalPassword) {
		this.terminalPassword = terminalPassword;
	}

	public String getReturnPwd() {
		return returnPwd;
	}

	public void setReturnPwd(String returnPwd) {
		this.returnPwd = returnPwd;
	}
	
	
}
