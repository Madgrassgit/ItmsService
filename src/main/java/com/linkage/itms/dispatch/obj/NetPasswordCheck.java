
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

/**
 * @author Administrator (Ailk No.)
 * @version 1.0
 * @since 2013-12-5
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class NetPasswordCheck extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(NetPasswordCheck.class);
	private String netUsername = "";
	private String netPassword = "";

	public NetPasswordCheck()
	{
	}

	public NetPasswordCheck(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("check()");
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck() )
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea))
		{
			document.setXMLEncoding(Global.codeTypeValue);
		}
		else
		{
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		// 返回用户名与密码
		Element RstInfo = root.addElement("RstInfo");
		RstInfo.addElement("netUsername").addText(netUsername);
		RstInfo.addElement("netPassword").addText(netPassword);
		return document.asXML();
	}

	public String getNetUsername()
	{
		return netUsername;
	}

	public void setNetUsername(String netUsername)
	{
		this.netUsername = netUsername;
	}

	public String getNetPassword()
	{
		return netPassword;
	}

	public void setNetPassword(String netPassword)
	{
		this.netPassword = netPassword;
	}
}
