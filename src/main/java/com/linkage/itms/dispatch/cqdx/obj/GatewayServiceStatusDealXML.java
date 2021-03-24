package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 网关业务查询
 * @author jiafh
 *
 */
public class GatewayServiceStatusDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GatewayServiceStatusDealXML.class);
	SAXReader reader = new SAXReader();
	
	private String voipNumber;
	private String ipAddress;
	
	private String netExpectStatus;
	private String iptvExpectStatus;
	private String voipExpectStatus;
	
	private String netActualStatus;
	private String iptvActualStatus;
	private String voipActualStatus;
	
	private String netActualPort;
	private String iptvActualPort;
	private String voipActualPort;
	

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			pppUsename = inRoot.elementTextTrim("ppp_username");
			logicId = inRoot.elementTextTrim("logic_id");
			serialNumber = inRoot.elementTextTrim("serial_number");
			voipNumber = inRoot.elementTextTrim("voip_number");
			ipAddress = inRoot.elementTextTrim("ip_address");
			return inDocument;
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
		resultInfo.addElement("ppp_username").addText(StringUtil.getStringValue(pppUsename));
		resultInfo.addElement("logic_id").addText(StringUtil.getStringValue(logicId));
		resultInfo.addElement("serial_number").addText(StringUtil.getStringValue(serialNumber));
		resultInfo.addElement("ip_address").addText(StringUtil.getStringValue(ipAddress));
		
		Element netReponse = resultInfo.addElement("wband_info");
		netReponse.addElement("expect_status").addText(StringUtil.getStringValue(netExpectStatus));
		netReponse.addElement("actual_status").addText(StringUtil.getStringValue(netActualStatus));
		netReponse.addElement("port").addText(StringUtil.getStringValue(netActualPort));
		
		Element iptvReponse = resultInfo.addElement("iptv_info");
		iptvReponse.addElement("expect_status").addText(StringUtil.getStringValue(iptvExpectStatus));
		iptvReponse.addElement("actual_status").addText(StringUtil.getStringValue(iptvActualStatus));
		iptvReponse.addElement("port").addText(StringUtil.getStringValue(iptvActualPort));
		
		Element voipReponse = resultInfo.addElement("voip_info");
		voipReponse.addElement("expect_status").addText(StringUtil.getStringValue(voipExpectStatus));
		voipReponse.addElement("actual_status").addText(StringUtil.getStringValue(voipActualStatus));
		voipReponse.addElement("port").addText(StringUtil.getStringValue(voipActualPort));
		return document.asXML();
	}

	public String getVoipNumber() {
		return voipNumber;
	}

	public void setVoipNumber(String voipNumber) {
		this.voipNumber = voipNumber;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getNetExpectStatus() {
		return netExpectStatus;
	}

	public void setNetExpectStatus(String netExpectStatus) {
		this.netExpectStatus = netExpectStatus;
	}

	public String getIptvExpectStatus() {
		return iptvExpectStatus;
	}

	public void setIptvExpectStatus(String iptvExpectStatus) {
		this.iptvExpectStatus = iptvExpectStatus;
	}

	public String getVoipExpectStatus() {
		return voipExpectStatus;
	}

	public void setVoipExpectStatus(String voipExpectStatus) {
		this.voipExpectStatus = voipExpectStatus;
	}

	public String getNetActualStatus() {
		return netActualStatus;
	}

	public void setNetActualStatus(String netActualStatus) {
		this.netActualStatus = netActualStatus;
	}

	public String getIptvActualStatus() {
		return iptvActualStatus;
	}

	public void setIptvActualStatus(String iptvActualStatus) {
		this.iptvActualStatus = iptvActualStatus;
	}

	public String getVoipActualStatus() {
		return voipActualStatus;
	}

	public void setVoipActualStatus(String voipActualStatus) {
		this.voipActualStatus = voipActualStatus;
	}

	public String getNetActualPort() {
		return netActualPort;
	}

	public void setNetActualPort(String netActualPort) {
		this.netActualPort = netActualPort;
	}

	public String getIptvActualPort() {
		return iptvActualPort;
	}

	public void setIptvActualPort(String iptvActualPort) {
		this.iptvActualPort = iptvActualPort;
	}

	public String getVoipActualPort() {
		return voipActualPort;
	}

	public void setVoipActualPort(String voipActualPort) {
		this.voipActualPort = voipActualPort;
	}
}
