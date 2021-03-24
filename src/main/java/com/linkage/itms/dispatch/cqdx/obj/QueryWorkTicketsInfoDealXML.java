package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryWorkTicketsInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryWorkTicketsInfoDealXML.class);
	SAXReader reader = new SAXReader();

	String resultStr = "";
	String workAsgnId = "";
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567890987";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryWorkTicketsInfoDealXML.getXML() is error!", e);
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
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		root.addElement("result").addText(resultStr);
		root.addElement("work_asgn_id").addText(workAsgnId);
		return document.asXML();
	}

	public String getResultStr() {
		return resultStr;
	}

	public void setResultStr(String resultStr) {
		this.resultStr = resultStr;
	}

	public String getWorkAsgnId() {
		return workAsgnId;
	}

	public void setWorkAsgnId(String workAsgnId) {
		this.workAsgnId = workAsgnId;
	}
}
