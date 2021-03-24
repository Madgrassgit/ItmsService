package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
		
public class VoipPortChecker extends BaseChecker 
{
	private static Logger logger = LoggerFactory.getLogger(VoipPortChecker.class);
	private String voipPort;
    private String RstCode;
    private String RstMsg;

	public VoipPortChecker(String inXml)
	{
			callXml=inXml;
			
	}

	/**
	 * @author cczhong
	 * @description 格式校验
	 */
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;
			try
			{
				document = reader.read(new StringReader(callXml));
				Element root = document.getRootElement();
				cmdId = root.elementTextTrim("CmdID");
				cmdType = root.elementTextTrim("CmdType");
				clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
				Element param = root.element("Param");
				userInfo  = param.elementTextTrim("UserInfo");
				userInfoType=Integer.parseInt(param.elementTextTrim("UserInfoType"));
			}
			catch (Exception e)
			{
				logger.error("解析xml发生异常，e={}",e);
				RstCode = "1";
				RstMsg = "数据格式错误";
				return false;
			}
			RstCode = "0";
			RstMsg = "成功";
			return true;
			
	}
/**
 * @author cczhong
 * @description 返回结果
 */
	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(RstCode));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(RstMsg));
		root.addElement("voipPort").addText(StringUtil.getStringValue(voipPort));
		
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
	}

	
	public String getVoipPort()
	{
		return voipPort;
	}

	
	public void setVoipPort(String voipPort)
	{
		this.voipPort = voipPort;
	}

	
	public String getRstCode()
	{
		return RstCode;
	}

	
	public void setRstCode(String rstCode)
	{
		RstCode = rstCode;
	}

	
	public String getRstMsg()
	{
		return RstMsg;
	}

	
	public void setRstMsg(String rstMsg)
	{
		RstMsg = rstMsg;
	}
	
}

	