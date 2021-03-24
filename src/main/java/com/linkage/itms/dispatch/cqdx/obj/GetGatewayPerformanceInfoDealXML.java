package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 家庭网关性能指标查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetGatewayPerformanceInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayPerformanceInfoDealXML.class);
	SAXReader reader = new SAXReader();
	private String TXPower = "";
	private String RXPower = "";
	private String deviceTemperature = "";
	private String supplyVottage = "";
	private String biasCurrent = "";

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("ppp_username");

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
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		Element resultInfo = root.addElement("result_info");
		resultInfo.addElement("circuit_evaluate").addText("");
		resultInfo.addElement("circuit_status").addText("");
		resultInfo.addElement("send_power").addText(TXPower);
		resultInfo.addElement("receive_power").addText(RXPower);
		resultInfo.addElement("temperature").addText(deviceTemperature);
		resultInfo.addElement("voltage").addText(supplyVottage);
		resultInfo.addElement("electricity").addText(biasCurrent);
		return document.asXML();
	}

	public String getTXPower() {
		return TXPower;
	}

	public void setTXPower(String tXPower) {
		TXPower = tXPower;
	}

	public String getRXPower() {
		return RXPower;
	}

	public void setRXPower(String rXPower) {
		RXPower = rXPower;
	}

	public String getDeviceTemperature() {
		return deviceTemperature;
	}

	public void setDeviceTemperature(String deviceTemperature) {
		this.deviceTemperature = deviceTemperature;
	}

	public String getSupplyVottage() {
		return supplyVottage;
	}

	public void setSupplyVottage(String supplyVottage) {
		this.supplyVottage = supplyVottage;
	}

	public String getBiasCurrent() {
		return biasCurrent;
	}

	public void setBiasCurrent(String biasCurrent) {
		this.biasCurrent = biasCurrent;
	}

	
}
