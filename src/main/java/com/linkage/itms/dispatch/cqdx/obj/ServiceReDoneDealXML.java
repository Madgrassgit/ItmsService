package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;

public class ServiceReDoneDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ServiceReDoneDealXML.class);
	SAXReader reader = new SAXReader();
	private boolean iscommon = false;
	private String deviceId = "";

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("workId"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("loId"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("pppoe"));
			
			if(StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename)){
				return null;
			}
			
			
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
			logger.error("ServiceReDoneDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[ServiceReDoneService]解析回参：{}", resultXML);
				Document inDocument = reader.read(new StringReader(resultXML));
				Element inRoot = inDocument.getRootElement();
				opId = inRoot.elementTextTrim("CmdID");
				result = inRoot.elementTextTrim("RstCode");
				errMsg = inRoot.elementTextTrim("RstMsg");
				// 不存在用户
				if ("1002".equals(result)) {
					result = "-1";
				}
			}
		}
		catch (Exception e) {
			logger.error("ServiceReDoneDealXML.returnXML() is error!", e);
		}
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		
		// 原因
		root.addElement("result_code").addText(result);
		
		// 下发状态
		root.addElement("result_desc").addText(errMsg);
		
		//非通用接口需要多解析一些回參
		if(!iscommon){
			root.addElement("loId").addText(logicId);
			root.addElement("pppoe").addText(pppUsename);
			// 接口调用唯一ID
			root.addElement("workId").addText(opId);
		}
		
		if("0".equals(result) && iscommon){
			// 如果调用成功，需将结果入数据库
			new PublicDAO().recordFactoryResetReturnDiag(opId, result, errMsg, deviceId, logicId);
		}
		
		return document.asXML();
	}

	
	public boolean isIscommon()
	{
		return iscommon;
	}

	
	public void setIscommon(boolean iscommon)
	{
		this.iscommon = iscommon;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
}
