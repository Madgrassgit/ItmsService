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
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2018年9月10日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 根据用户设备的序列号查询对应的LOID和宽带账号
 *
 */
public class QueryLoidNetServiceChecker extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QueryLoidNetServiceChecker.class);
	private String netAccount;
	
	public QueryLoidNetServiceChecker(String inXml)
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
			devSn = param.elementTextTrim("DevSN");
			searchType = 2;  //根据设备序列号查询
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck())
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
		document.setXMLEncoding(Global.codeTypeValue);
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		//参数值
		Element param = root.addElement("Param");
		param.addElement("DevSN").addText(""+devSn);
		param.addElement("Loid").addText(""+loid);
		param.addElement("NetAccount").addText(""+userInfo);
		
		return document.asXML();
	}
}
