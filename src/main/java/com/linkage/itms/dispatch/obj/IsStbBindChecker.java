
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
 * 是否支持零配置开通设备版本查询接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-5-12
 */
public class IsStbBindChecker extends BaseChecker
{

	public static final Logger logger = LoggerFactory
			.getLogger(IsStbBindChecker.class);
	private String zeroConf;

	/**
	 * 构造函数 入参 inXml XML格式
	 * 
	 * @param inXml
	 */
	public IsStbBindChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查调用接口入参的合法性
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
		if (StringUtil.IsEmpty(loid)) {
			result = 1002;
			resultDesc = "用户信息不合法";
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

	/**
	 * 组装XML字符串，并返回
	 */
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
		// 结果描述
		root.addElement("Isstbbind").addText("" + zeroConf);

		return document.asXML();
	}

	public String getZeroConf() {
		return zeroConf;
	}

	public void setZeroConf(String zeroConf) {
		this.zeroConf = zeroConf;
	}
	
}
