package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.WSClientUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;

public class FactoryResetDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(FactoryResetDealXML.class);
	SAXReader reader = new SAXReader();
	private boolean iscommon = false;
	private String deviceId = "";

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			
			if(StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename) && StringUtil.isEmpty(serialNumber)){
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
			logger.error("FactoryResetDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		try {
			if (!StringUtil.IsEmpty(resultXML)) {
				logger.warn("servicename[FactoryResetService]解析回参：{}", resultXML);
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
			logger.error("FactoryResetDealXML.returnXML() is error!", e);
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
	
	/**
	 * 调用工厂复位返回接口
	 * @param opId
	 * @param result
	 * @param message
	 */
	public String returnXML(String opId,String result,String message) {
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		inParam.append("<ROOT>");
		inParam.append("<op_id>" + opId +"</op_id>");
		inParam.append("<time>" + new DateTimeUtil().getYYYY_MM_DD_HH_mm_ss() + "</time>");
		inParam.append("<result>" + result + "</result>");
		inParam.append("<err_msg>" + message + "</err_msg>");
		inParam.append("</ROOT>");
		
		String returnXml = WSClientUtil.callRemoteService(Global.G_ITMS_FINISH_URL, inParam.toString(),Global.G_ITMS_SERV_METHOD,
				Global.G_ITMS_NAME_SPACE, Global.G_ITMS_REQUEST_NAME, Global.G_ITMS_RESPONSE_NAME);
		logger.warn("调用工厂复位接口返回==[{}]",returnXml);
		return returnXml;
	}
}
