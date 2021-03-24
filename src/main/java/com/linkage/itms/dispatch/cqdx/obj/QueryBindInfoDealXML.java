package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryBindInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryBindInfoDealXML.class);
	SAXReader reader = new SAXReader();
	
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			String workId = StringUtil.getStringValue(inRoot.elementTextTrim("work_id"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			Document document = DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element root = document.addElement("root");
			// 接口调用唯一ID
			root.addElement("CmdID").addText(StringUtil.isEmpty(workId)?"12345678":workId);
			// 结果代码
			root.addElement("CmdType").addText("CX_01");
			// 结果描述
			root.addElement("ClientType").addText("3");
			return document;
		} catch (Exception e) {
			logger.error("QueryBindInfoDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	public String returnXML() {
		String handwareVersion = "";
		String ip = "";
		String devSN = "";
		String softwareVersion = "";
		String vendorName = "";
		String accessType = "";
		String oui = "";
		String model = "";
		String workId = "";
		logger.warn("result="+result);
		logger.warn("resultXML="+resultXML+"isnull?"+StringUtil.IsEmpty(resultXML));
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[QueryBindInfoService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				workId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}
				Element param = inRoot.element("Param");
				handwareVersion = param.elementTextTrim("HandwareVersion");
				ip = param.elementTextTrim("ip");
				devSN = param.elementTextTrim("DevSN");
				softwareVersion = param.elementTextTrim("SoftwareVersion");
				vendorName = param.elementTextTrim("vendor");
				accessType = param.elementTextTrim("AccessStyleRelayId");
				oui = param.elementTextTrim("DevOUI");
				model = param.elementTextTrim("DevModel");
			}
		}
		catch (Exception e) {
			logger.error("QueryBindInfoDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		Element result_info = root.addElement("result_info");//.addText(workId);
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		
		result_info.addElement("work_id").addText(workId);
		result_info.addElement("hardware_version").addText(handwareVersion);
		result_info.addElement("ip_address").addText(ip);
		result_info.addElement("logic_id").addText(logicId);
		result_info.addElement("ppp_username").addText(pppUsename);
		result_info.addElement("serial_number").addText(devSN);
		result_info.addElement("software_version").addText(softwareVersion);
		// TODO 不知道怎么取
		result_info.addElement("terminal_name").addText(vendorName + "-" + getAccessType(accessType) + "-" + oui + "-" +model);
		
		result_info.addElement("terminal_type").addText(oui + "-" +model);
		/*if(!StringUtil.isEmpty(logicId)){
			root.addElement("terminalType").addText(userDeviceDAO.queryDeviceType(logicId, "2"));
		}else{
			root.addElement("terminalType").addText(userDeviceDAO.queryDeviceType(pppUsename, "1"));
		}*/
		
		return document.asXML();
	}
	
	private String getAccessType(String accessType){
		if("1".equals(accessType)){//ADSL
			return "ADSL";
		}
		if("2".equals(accessType)){//LAN
			return "LAN";
		}
		if("3".equals(accessType)){//EPON
			return "EPON";
		}
		if("4".equals(accessType)){//EPON
			return "GPON";
		}
		return "EPON";
	}
}
