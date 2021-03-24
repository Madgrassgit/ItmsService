package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryLanInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryLanInfoDealXML.class);
	SAXReader reader = new SAXReader();

	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567890987";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryLanInfoDealXML.getXML() is error!", e);
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
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		Element lanInfos = root.addElement("lan_infos");
		if(list.size() > 0){
			for (Map<String, String> map : list) {
				Element lanInfo = lanInfos.addElement("lan_info");
				lanInfo.addElement("name").addText(StringUtil.getStringValue(map, "name"));
				lanInfo.addElement("status").addText(StringUtil.getStringValue(map, "RstState"));
				lanInfo.addElement("accept_byte_count").addText(StringUtil.getStringValue(map, "BytesReceived"));
				lanInfo.addElement("send_byte_count").addText(StringUtil.getStringValue(map, "BytesSent"));
				lanInfo.addElement("accept_package_count").addText(StringUtil.getStringValue(map, "PacketsReceived"));
				lanInfo.addElement("send_package_count").addText(StringUtil.getStringValue(map, "PacketsSent"));
				lanInfo.addElement("connection_rate").addText(StringUtil.getStringValue(map, "MaxBitRate"));
			}
		}
		logger.warn("XML return=......"+document.asXML());
		return document.asXML();
	}

	public List<Map<String, String>> getList() {
		return list;
	}

	public void setList(List<Map<String, String>> list) {
		this.list = list;
	}
}
