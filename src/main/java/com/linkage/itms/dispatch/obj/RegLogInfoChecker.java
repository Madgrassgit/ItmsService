package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年5月26日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class RegLogInfoChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(RegLogInfoChecker.class);
	// 注册结果
	protected String regRst = "";
	// 处理意见
	protected String proOpin = "";
	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public RegLogInfoChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("RegLogInfoChecker -> check(),inXML ({})",callXml);
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
			cityId = param.elementTextTrim("CityId");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck()
				|| false == cityIdCheck())
		{
			return false;
		}
		
		if (1 == userInfoType && userInfo.length() < 6)
		{
			result = 1;
			resultDesc = "设备序列号至少6位";
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
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		root.addElement("SN").addText(devSn ==null?"":devSn);
		root.addElement("RegRst").addText(regRst);
		root.addElement("ProOpin").addText(proOpin);
		
		return document.asXML();
	}

	
	public String getRegRst()
	{
		return regRst;
	}

	
	public String getProOpin()
	{
		return proOpin;
	}

	
	public void setRegRst(String regRst)
	{
		this.regRst = regRst;
	}

	
	public void setProOpin(String proOpin)
	{
		this.proOpin = proOpin;
	}

	
	
	
}
