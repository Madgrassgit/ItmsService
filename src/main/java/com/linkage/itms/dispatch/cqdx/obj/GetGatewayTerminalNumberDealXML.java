package com.linkage.itms.dispatch.cqdx.obj;

import com.linkage.itms.commom.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class GetGatewayTerminalNumberDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayTerminalNumberDealXML.class);
	
	// 查询结果
	protected int resultCode;
	// 查询结果描述
	protected String resultDesc;
	
	protected int terminal_number_cpe = 0;
	
	protected int terminal_number_db = -1;
		
	private SAXReader reader = new SAXReader();
	
	public GetGatewayTerminalNumberDealXML(String xml){
		this.inXml = xml;
	}
	
	public boolean check()
	{
		logger.debug("check()");
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try
		{
			document = saxReader.read(new StringReader(inXml));
			Element root = document.getRootElement();
			logicId = StringUtil.getStringValue(root.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(root.elementTextTrim("ppp_username"));
			serialNumber = StringUtil.getStringValue(root.elementTextTrim("serial_number"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = "1";
			errMsg = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename) && StringUtil.isEmpty(serialNumber))
		{
			result = "-99";
			errMsg = "宽带帐号、逻辑ID和设备序列号均为空";
			return false;
		}
		result = "0";
		errMsg = "执行成功";
		return true;
	}

	@Override
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "12345678909876";
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("logic_id");

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

	@Override
	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		//返回结果值
		root.addElement("result_code").addText(result);
		//返回结果说明
		root.addElement("result_desc").addText(errMsg);
		Element resultInfo = root.addElement("result_info");
		// 上网个数
		resultInfo.addElement("terminal_number_cpe").addText(StringUtil.getStringValue(terminal_number_cpe));
		resultInfo.addElement("terminal_number_db").addText(StringUtil.getStringValue(terminal_number_db));
		logger.warn("return=({})", document.asXML());  
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

	
	public int getTerminal_number_cpe()
	{
		return terminal_number_cpe;
	}

	
	public void setTerminal_number_cpe(int terminal_number_cpe)
	{
		this.terminal_number_cpe = terminal_number_cpe;
	}

	
	public int getTerminal_number_db()
	{
		return terminal_number_db;
	}

	
	public void setTerminal_number_db(int terminal_number_db)
	{
		this.terminal_number_db = terminal_number_db;
	}
	
}
