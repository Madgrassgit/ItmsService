
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class setITVWIFIChecker extends BaseChecker
{

	public static final Logger logger = LoggerFactory.getLogger(setITVWIFIChecker.class);
	
	private String enable;
	
	public setITVWIFIChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("ModifyWifiChannelChecker>check()");
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			enable = param.elementTextTrim("Enable");
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck())
		{
			return false;
		}
		
		if(3==userInfoType && userInfo.length()<6)
		{
			result = 1005;
			resultDesc = "设备序列号非法，设备序列号不可少于6位";
			return false;
		}
		
		if (StringUtil.IsEmpty(enable) && !"0".equals(enable) && !"1".equals(enable))
		{
			result = 1;
			resultDesc = "开关操作类型不合法";
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}

	/**
	 * 用户信息类型合法性检查 此接口返回的错误码和basechecker里的不同，所以重写了方法
	 */
	boolean userInfoTypeCheck()
	{
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType)
		{
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		
		return document.asXML();
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}
	
	
}
