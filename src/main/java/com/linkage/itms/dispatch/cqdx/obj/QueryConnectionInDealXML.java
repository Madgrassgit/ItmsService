package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryConnectionInDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryConnectionInDealXML.class);
	SAXReader reader = new SAXReader();

	String sendPower = "";
	String receivePower = "";
	List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678987";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			
			return inDocument;
		} catch (Exception e) {
			logger.error("QueryConnectionInDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("workId").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		root.addElement("send_power").addText(sendPower);
		root.addElement("receive_power").addText(receivePower);
		
		Element dhcp = root.addElement("dhcp");
		if (null == list || list.isEmpty()) {
			root.addElement("dial_connect_status").addText("");
			root.addElement("dns").addText("");
			Element address = dhcp.addElement("address");
			address.addElement("ip_address").addText("");
			address.addElement("mac_address").addText("");
		}
		else {
			root.addElement("dial_connect_status").addText(StringUtil.getStringValue(list.get(0), "conn_status"));
			root.addElement("dns").addText(StringUtil.getStringValue(list.get(0), "dns"));
			for (HashMap<String,String> map : list) {
				Element address = dhcp.addElement("address");
				address.addElement("ip_address").addText(StringUtil.getStringValue(map, "ip"));
				address.addElement("mac_address").addText(StringUtil.getStringValue(map, "cpe_mac"));
			}
		}
		return document.asXML();
	}

	public String getSendPower() {
		return sendPower;
	}

	public void setSendPower(String sendPower) {
		this.sendPower = sendPower;
	}

	public String getReceivePower() {
		return receivePower;
	}

	public void setReceivePower(String receivePower) {
		this.receivePower = receivePower;
	}

	public List<HashMap<String, String>> getList() {
		return list;
	}

	public void setList(List<HashMap<String, String>> list) {
		this.list = list;
	}
}
