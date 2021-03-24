
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

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月26日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class VersionReleaseChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(VersionReleaseChecker.class);
	public VersionReleaseChecker(String param)
	{
		this.callXml = param;
	}

	@Override
	public boolean check()
	{
		try
		{
			SAXReader reader = new SAXReader();
			Document document = null;
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
		}
		catch (DocumentException e)
		{
			logger.warn("校验参数发生异常：{}", e);
			result = 1;
			resultDesc = "数据格式错误";
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
		
		return document.asXML();
	}
	
}
