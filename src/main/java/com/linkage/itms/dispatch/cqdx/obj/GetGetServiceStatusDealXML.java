package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class GetGetServiceStatusDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetGetServiceStatusDealXML.class);
	SAXReader reader = new SAXReader();

	private String netStatus = "0";
	private String iptvStatus = "0";
	private String voip1Status = "0";
	private String voip2Status = "0";
	
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
			logger.error("GetGetServiceStatusDealXML.getXML() is error!", e);
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
		Element serviceStatus = root.addElement("service_status");
		serviceStatus.addElement("wband_status").addText(netStatus);
		serviceStatus.addElement("iptv_status").addText(iptvStatus);
		// TODO 两个voip端口取值是否正确
		serviceStatus.addElement("voip1_status").addText(voip1Status);
		serviceStatus.addElement("voip2_status").addText(voip2Status);
		return document.asXML();
	}

	public String getNetStatus() {
		return netStatus;
	}

	public void setNetStatus(String netStatus) {
		this.netStatus = netStatus;
	}

	public String getIptvStatus() {
		return iptvStatus;
	}

	public void setIptvStatus(String iptvStatus) {
		this.iptvStatus = iptvStatus;
	}

	public String getVoip1Status() {
		return voip1Status;
	}

	public void setVoip1Status(String voip1Status) {
		this.voip1Status = voip1Status;
	}

	public String getVoip2Status() {
		return voip2Status;
	}

	public void setVoip2Status(String voip2Status) {
		this.voip2Status = voip2Status;
	}
}
