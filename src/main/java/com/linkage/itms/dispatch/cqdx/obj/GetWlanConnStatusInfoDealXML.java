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

/**
 * 
 * @author fanjm (Ailk No.)
 * @version 1.0
 * @since 2017年11月23日
 * @category com.linkage.itms.dispatch.cqdx.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetWlanConnStatusInfoDealXML extends BaseDealXML 
{
	private static Logger logger = LoggerFactory.getLogger(GetWlanConnStatusInfoDealXML.class);
	SAXReader reader = new SAXReader();
	
	private List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		Element resultInfoparam = root.addElement("result_info");
		
		logger.warn("wanList="+wanList.size());
		if(wanList != null && wanList.size() > 0){
			HashMap<String,String> tmp = null;
			for(int i = 0; i < wanList.size(); i++){
				logger.warn("i="+i);
				tmp = wanList.get(i);
				
				String SSIDname = StringUtil.getStringValue(tmp.get("SSIDname"));
				String status = StringUtil.getStringValue(tmp.get("RstState"));
				String BytesReceived = StringUtil.getStringValue(tmp.get("BytesReceived"));
				String BytesSent = StringUtil.getStringValue(tmp.get("BytesSent"));
				Element wlanInfoparam = resultInfoparam.addElement("wlan_info");
				wlanInfoparam.addElement("name").addText(SSIDname);
				wlanInfoparam.addElement("status").addText(status);
				wlanInfoparam.addElement("ip_address").addText("");
				wlanInfoparam.addElement("mac_address").addText("");
				wlanInfoparam.addElement("connect_duration").addText("");
				wlanInfoparam.addElement("accept_packet_count").addText(BytesReceived);
				wlanInfoparam.addElement("send_packet_count").addText(BytesSent);
			}
		}
		
		String returnXML = document.asXML();
		logger.warn("GetWlanConnStatusInfoDealXML-returnXML:{}",returnXML);
		return returnXML;
	}

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
			logger.error("GetWlanConnStatusInfoDealXML.getXML() is error!", e);
			return null;
		}
	}
	

	
	public SAXReader getReader()
	{
		return reader;
	}

	
	public void setReader(SAXReader reader)
	{
		this.reader = reader;
	}

	
	public List<HashMap<String, String>> getWanList()
	{
		return wanList;
	}

	
	public void setWanList(List<HashMap<String, String>> wanList)
	{
		this.wanList = wanList;
	}
	
	
}
