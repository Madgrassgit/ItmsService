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

import com.linkage.commons.util.StringUtil;

/**
 * 家庭网关LAN口连接状态查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetLanConnectionStatusInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetLanConnectionStatusInfoDealXML.class);
	SAXReader reader = new SAXReader();
	private List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();

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
		// 
		if(lanList != null && lanList.size() > 0){
			HashMap<String,String> tmp = null;
			for(int i = 0; i < lanList.size(); i++){
				tmp = lanList.get(i);
				String lanPort = StringUtil.getStringValue(tmp.get("LanPortNUM"));
				String name = StringUtil.getStringValue(tmp.get("name"));
				String status = StringUtil.getStringValue(tmp.get("status"));
				String macAddress = StringUtil.getStringValue(tmp.get("macAddress"));
				String PacketsReceived = StringUtil.getStringValue(tmp.get("PacketsReceived"));
				String PacketsSent = StringUtil.getStringValue(tmp.get("PacketsSent"));
				Element lanPortNums = resultInfo.addElement("lan_info");//.addAttribute("num", lanPort);
				lanPortNums.addElement("name").addText(name);
				lanPortNums.addElement("status").addText(status);
				lanPortNums.addElement("ip_address").addText("");
				lanPortNums.addElement("mac_address").addText(macAddress);
				lanPortNums.addElement("connect_duration").addText("");
				lanPortNums.addElement("accept_packet_count").addText(PacketsReceived);
				lanPortNums.addElement("send_packet_count").addText(PacketsSent);
			}
		}
		return document.asXML();
	}


	public List<HashMap<String, String>> getLanList() {
		return lanList;
	}

	public void setLanList(List<HashMap<String, String>> lanList) {
		this.lanList = lanList;
	}
	
	
}
