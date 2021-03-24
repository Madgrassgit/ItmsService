package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class IPTVLanDealXML  extends BaseDealXML 
{
	private static Logger logger = LoggerFactory.getLogger(IPTVLanDealXML.class);
	SAXReader reader = new SAXReader();
	//期望开通的IPTV端口
	private String expectIPTVPort="";
	//实际开通的IPTV端口
	private String actualIPTVPort="";
	
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
				return null;
			}
			return inDocument;
		} catch (Exception e) {
			logger.error("IPTVLanDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		
		Element resultInfoparam = root.addElement("result_info");
		expectIPTVPort = expectIPTVPort.replace("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.","Lan");
		actualIPTVPort = actualIPTVPort.replace("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.","Lan");
		resultInfoparam.addElement("expect_iptv_port").addText(expectIPTVPort);
		resultInfoparam.addElement("actual_iptv_port").addText(actualIPTVPort);
		
		
		String returnXML = document.asXML();
		logger.warn("IPTVLanDealXML-returnXML:{}",returnXML);
		return returnXML;
	}
	
	
	public String getExpectIPTVPort()
	{
		return expectIPTVPort;
	}

	
	public void setExpectIPTVPort(String expectIPTVPort)
	{
		this.expectIPTVPort = expectIPTVPort;
	}

	
	public String getActualIPTVPort()
	{
		return actualIPTVPort;
	}

	
	public void setActualIPTVPort(String actualIPTVPort)
	{
		this.actualIPTVPort = actualIPTVPort;
	}
	
	
}
