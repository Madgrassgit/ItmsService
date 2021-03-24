package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;

public class QueryRgModeInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryRgModeInfoDealXML.class);
	SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			
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
		String wanType = "";
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[QueryRgModeInfoService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				wanType = inRoot.elementTextTrim("WanType");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}else if("0".equals(result) && "-1".equals(result)){
					result = "-99";
				}
			}
		}
		catch (Exception e) {
			logger.error("QueryRgModeInfoDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		Element result = root.addElement("result_info");
		
		if("cq_dx".equals(Global.G_instArea)){
			if("1".equals(wanType)){
				wanType = "2";
			} else if("2".equals(wanType)){
				wanType = "1";
			}
		}
		result.addElement("terminal_rgmode").addText(wanType);
		return document.asXML();
	}
}
