
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
 * release解绑接口的XML元素对象
 * 
 * @author Administrator(工号) Tel:78
 * @version 1.0
 * @since 2011-5-11 下午02:59:10
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 */
public class ReleaseChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BindChecker.class);

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public ReleaseChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
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
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");
			// 江西电信有查询类型，1 客户账户 2 设备序列号
			if ("jx_dx".equals(Global.G_instArea))
			{
				searchType = StringUtil.getIntegerValue(param
						.elementTextTrim("SearchType"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 江西电信有查询类型，和设备序列号,校验不同
		if ("jx_dx".equals(Global.G_instArea))
		{
			if (false == baseCheck() || false == searchTypeCheck())
			{
				return false;
			}
			else
			{
				if (searchType == 1)
				{
					// 参数合法性检查
					if (false == userInfoTypeCheck() || false == userInfoCheck()
							|| false == cityIdCheck())
					{
						return false;
					}
				}
				else if (searchType == 2)
				{
					if (false == devSnCheck())
					{
						return false;
					}
				}
			}
		}
		// 其他局点
		else
		{
			// 参数合法性检查
			if (false == baseCheck() || false == userInfoTypeCheck()
					|| false == userInfoCheck() || false == cityIdCheck())
			{
				return false;
			}
		}
		result = 0;
		resultDesc = "解绑成功";
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
		return document.asXML();
	}
}
