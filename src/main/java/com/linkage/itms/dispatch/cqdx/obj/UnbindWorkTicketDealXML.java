package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;

public class UnbindWorkTicketDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(UnbindWorkTicketDealXML.class);
	SAXReader reader = new SAXReader();

	String keepUserInfo = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			keepUserInfo = StringUtil.getStringValue(inRoot.elementTextTrim("keep_user_info"));

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
			logger.error("UnbindWorkTicketDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[UnbindWorkTicketService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}
			}
		}
		catch (Exception e) {
			logger.error("UnbindWorkTicketDealXML.returnXML() is error!", e);
		}
		
		// 是否删除用户
		if("0".equals(keepUserInfo) && "0".equals(result)){
			if(StringUtil.isEmpty(logicId)){
				new UserDeviceDAO().recordDeleteCustomer(logicId, 1);
			}else if(StringUtil.isEmpty(serialNumber)){
				new UserDeviceDAO().recordDeleteCustomer(serialNumber, 3);
			}
		}
		
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		return document.asXML();
	}

	public String getKeepUserInfo() {
		return keepUserInfo;
	}

	public void setKeepUserInfo(String keepUserInfo) {
		this.keepUserInfo = keepUserInfo;
	}
}
