package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 家庭网关配置稽核接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetGatewayConfigInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayConfigInfoDealXML.class);
	SAXReader reader = new SAXReader();
	private String kdWanType_yingpei = "";
	private String kdWanType_shipei = "";
	private String iptvWanType_yingpei = "";
	private String iptvWanType_shipei = "";
	private String kdVlanId_yingpei = "";
	private String kdVlanId_shipei = "";
	private String iptvVlanId_yingpei = "";
	private String iptvVlanId_shipei = "";

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
		// 只有在不匹配的时候才会有result_info
		if ("-2".equals(result)){
			Element resultInfo = root.addElement("result_info");
			if(!kdVlanId_shipei.equals(kdVlanId_yingpei)){
				Element checkReponse = resultInfo.addElement("check_reponse");
				checkReponse.addElement("value_name").addText("宽带 VLAN");
				checkReponse.addElement("config_value").addText(kdVlanId_yingpei);
				checkReponse.addElement("terminal_value").addText(kdVlanId_shipei);
			}
			if(!kdWanType_shipei.equals(kdWanType_yingpei)){
				Element checkReponse = resultInfo.addElement("check_reponse");
				checkReponse.addElement("value_name").addText("宽带 上网方式");
				checkReponse.addElement("config_value").addText(kdWanType_yingpei);
				checkReponse.addElement("terminal_value").addText(kdWanType_shipei);
			}
			if(!iptvVlanId_shipei.equals(iptvVlanId_yingpei)){
				Element checkReponse = resultInfo.addElement("check_reponse");
				checkReponse.addElement("value_name").addText("IPTV VLAN");
				checkReponse.addElement("config_value").addText(iptvVlanId_yingpei);
				checkReponse.addElement("terminal_value").addText(iptvVlanId_shipei);
			}
			if(!iptvWanType_shipei.equals(iptvWanType_yingpei)){
				Element checkReponse = resultInfo.addElement("check_reponse");
				checkReponse.addElement("value_name").addText("IPTV 上网方式");
				checkReponse.addElement("config_value").addText(iptvWanType_yingpei);
				checkReponse.addElement("terminal_value").addText(iptvWanType_shipei);
			}
		}
		
		return document.asXML();
	}

	public String getKdWanType_yingpei() {
		return kdWanType_yingpei;
	}

	public void setKdWanType_yingpei(String kdWanType_yingpei) {
		this.kdWanType_yingpei = kdWanType_yingpei;
	}

	public String getKdWanType_shipei() {
		return kdWanType_shipei;
	}

	public void setKdWanType_shipei(String kdWanType_shipei) {
		this.kdWanType_shipei = kdWanType_shipei;
	}

	public String getIptvWanType_yingpei() {
		return iptvWanType_yingpei;
	}

	public void setIptvWanType_yingpei(String iptvWanType_yingpei) {
		this.iptvWanType_yingpei = iptvWanType_yingpei;
	}

	public String getIptvWanType_shipei() {
		return iptvWanType_shipei;
	}

	public void setIptvWanType_shipei(String iptvWanType_shipei) {
		this.iptvWanType_shipei = iptvWanType_shipei;
	}

	public String getKdVlanId_yingpei() {
		return kdVlanId_yingpei;
	}

	public void setKdVlanId_yingpei(String kdVlanId_yingpei) {
		this.kdVlanId_yingpei = kdVlanId_yingpei;
	}

	public String getKdVlanId_shipei() {
		return kdVlanId_shipei;
	}

	public void setKdVlanId_shipei(String kdVlanId_shipei) {
		this.kdVlanId_shipei = kdVlanId_shipei;
	}

	public String getIptvVlanId_yingpei() {
		return iptvVlanId_yingpei;
	}

	public void setIptvVlanId_yingpei(String iptvVlanId_yingpei) {
		this.iptvVlanId_yingpei = iptvVlanId_yingpei;
	}

	public String getIptvVlanId_shipei() {
		return iptvVlanId_shipei;
	}

	public void setIptvVlanId_shipei(String iptvVlanId_shipei) {
		this.iptvVlanId_shipei = iptvVlanId_shipei;
	}
	
}
