package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class GetVLanInfoOfTerminalDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetVLanInfoOfTerminalDealXML.class);
	SAXReader reader = new SAXReader();

	String broadbandVlanId = "";
	String iptvVlanId = "";
	String voipVlanId = "";
	String multiVlanMode = "";
	
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("ppp_username");
			
			if(StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename))
			{
				result = "1001";
				errMsg="宽带帐号或逻辑id为空";
				logger.warn("宽带帐号或逻辑id为空");
				return null;
			}
			
			return inDocument;
		} catch (Exception e) {
			logger.error("GetVLanInfoOfTerminalDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		
		Element resultInfo = root.addElement("result_info");
		resultInfo.addElement("broadband_vlanid").addText(broadbandVlanId);
		resultInfo.addElement("iptv_vlanid").addText(iptvVlanId);
		resultInfo.addElement("voip_vlanid").addText(voipVlanId);
		resultInfo.addElement("multi_vlan_mode").addText(multiVlanMode);
	
		return document.asXML();
	}

	public String getBroadbandVlanId() {
		return broadbandVlanId;
	}

	public void setBroadbandVlanId(String broadbandVlanId) {
		this.broadbandVlanId = broadbandVlanId;
	}

	public String getIptvVlanId() {
		return iptvVlanId;
	}

	public void setIptvVlanId(String iptvVlanId) {
		this.iptvVlanId = iptvVlanId;
	}

	public String getVoipVlanId() {
		return voipVlanId;
	}

	public void setVoipVlanId(String voipVlanId) {
		this.voipVlanId = voipVlanId;
	}

	public String getMultiVlanMode() {
		return multiVlanMode;
	}

	public void setMultiVlanMode(String multiVlanMode) {
		this.multiVlanMode = multiVlanMode;
	}
}
