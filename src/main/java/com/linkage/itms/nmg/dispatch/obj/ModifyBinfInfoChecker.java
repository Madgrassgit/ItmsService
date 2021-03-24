
package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * release解绑接口的XML元素对象
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-30
 * @category com.linkage.itms.nmg.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ModifyBinfInfoChecker extends NmgBaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(ModifyBinfInfoChecker.class);

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public ModifyBinfInfoChecker(String inXml)
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
			loid = param.elementTextTrim("Loid");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if (StringUtil.IsEmpty(loid))
		{
			result = 1009;
			resultDesc = "Loid为空";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck())
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		return document.asXML();
	}
}
