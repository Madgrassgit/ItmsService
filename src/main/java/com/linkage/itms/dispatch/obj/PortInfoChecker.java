package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
		
public class PortInfoChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(PortInfoChecker.class);
    private String LanStatus;
    private String WanStatus;
	

	public PortInfoChecker(String inXml)
	{
		callXml=inXml;
	}

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
				result = 1;
				resultDesc = "数据格式错误";
				return false;
			}
			//参数合法性检查
			if (false == baseCheck() || false == userInfoTypeCheck()
					|| false == userInfoCheck()) {
				return false;
			}
			result = 0;
			resultDesc = "成功";
			return true;
			
	}

	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		root.addElement("LanStatus").addText(StringUtil.getStringValue(LanStatus));
		root.addElement("WanStatus").addText(StringUtil.getStringValue(WanStatus));
		
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
			
	}

	/**
	 * 用户信息类型合法性检查 此接口返回的错误码和basechecker里的不同，所以重写了方法
	 */
	boolean userInfoTypeCheck()
	{
		if (1 != userInfoType)
		{
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	
	public String getLanStatus()
	{
		return LanStatus;
	}

	
	public void setLanStatus(String lanStatus)
	{
		LanStatus = lanStatus;
	}

	
	public String getWanStatus()
	{
		return WanStatus;
	}

	
	public void setWanStatus(String wanStatus)
	{
		WanStatus = wanStatus;
	}
	
}

	