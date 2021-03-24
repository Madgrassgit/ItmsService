
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
 * @author zzs (Ailk No.78987)
 * @version 1.0
 * @since 2018-11-29
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OnuConnTypeChecker extends BaseChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(OnuConnTypeChecker.class);
	private int connType =0;

	public OnuConnTypeChecker(String inXml)
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
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck())
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
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		Element param = root.addElement("Param");
		param.addElement("ConnType").addText(connType+"");
		return document.asXML();
	}

	boolean userInfoTypeCheck()
	{
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
				&& 4 != userInfoType && 5 != userInfoType)
		{
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	/**
	 * @return the connType
	 */
	public int getConnType()
	{
		return connType;
	}

	/**
	 * @param connType the connType to set
	 */
	public void setConnType(int connType)
	{
		this.connType = connType;
	}
	
}
