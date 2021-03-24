package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class GetWANConnectionStatusInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetWANConnectionStatusInfoDealXML.class);
	
	// 查询结果
	protected int resultCode;
	// 查询结果描述
	protected String resultDesc;
	
	protected String status;
		
	SAXReader reader = new SAXReader();
	
	public GetWANConnectionStatusInfoDealXML(String xml){
		this.inXml = xml;
	}
	
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(inXml));
			Element root = document.getRootElement();
			logicId = root.elementTextTrim("logic_id");
			pppUsename = root.elementTextTrim("ppp_username");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = "-99";
			errMsg = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename))
		{
			result = "-99";
			errMsg = "宽带帐号和逻辑ID均为空";
			return false;
		}
		result = "0";
		errMsg = "执行成功";
		return true;
	}

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("logic_id");

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
		//返回结果值
		root.addElement("result_code").addText(result);
		//返回结果说明
		root.addElement("result_desc").addText(errMsg);
		Element result_info = root.addElement("result_info");
		// 端口状态
		if(StringUtil.isEmpty(status)){
			result_info.addElement("status").addText("");
		}else{
			result_info.addElement("status").addText(status);
		}
		
		return document.asXML();
	}

	

	
	public int getResultCode()
	{
		return resultCode;
	}

	
	public void setResultCode(int resultCode)
	{
		this.resultCode = resultCode;
	}

	public String getResultDesc()
	{
		return resultDesc;
	}

	
	public void setResultDesc(String resultDesc)
	{
		this.resultDesc = resultDesc;
	}

	
	public String getStatus()
	{
		return status;
	}

	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
}
