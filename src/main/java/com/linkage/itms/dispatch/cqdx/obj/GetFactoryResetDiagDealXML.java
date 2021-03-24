package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.WSClientUtil;

public class GetFactoryResetDiagDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetFactoryResetDiagDealXML.class);
	SAXReader reader = new SAXReader();

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
			logger.error("GetFactoryResetDiagDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	/**
	 * 调用工厂复位返回接口
	 * @param opId
	 * @param result
	 * @param message
	 */
	public String returnXML(String opId,String result,String message) {
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		inParam.append("<ROOT>");
		inParam.append("<op_id>" + opId +"</op_id>");
		inParam.append("<time>" + new DateTimeUtil().getYYYY_MM_DD_HH_mm_ss() + "</time>");
		inParam.append("<result>" + result + "</result>");
		inParam.append("<err_msg>" + message + "</err_msg>");
		inParam.append("</ROOT>");
		
		String returnXml = WSClientUtil.callRemoteService(Global.G_ITMS_FINISH_URL, inParam.toString(),Global.G_ITMS_SERV_METHOD,
				Global.G_ITMS_NAME_SPACE, Global.G_ITMS_REQUEST_NAME, Global.G_ITMS_RESPONSE_NAME);
		logger.warn("调用工厂复位接口返回==[{}]",returnXml);
		return returnXml;
	}
}
