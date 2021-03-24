
package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbDeviceInfoCheck extends BaseChecker
{

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(GetStbDeviceInfoCheck.class);
	private String inParam = null;

	// 有参构造函数
	public GetStbDeviceInfoCheck(String inParam)
	{
		this.inParam = inParam;
	}

	@Override
	public boolean check()
	{
		logger.debug("GetStbBaseInfoChecker==>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			/**
			 * 查询类型 1：根据业务帐号查询 2：根据MAC地址查询 3：根据机顶盒序列号查询
			 */
			searchType = param.elementTextTrim("SearchType");
			/**
			 * 查询类型所对应的用户信息 SelectType为1时为itv业务账号 SelectType为2时为机顶盒MAC
			 * SelectType为3时为机顶盒序列号
			 */
			searchInfo = param.elementTextTrim("SearchInfo");
		}
		catch (Exception e)
		{
			logger.error("inParam format is err,msg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()
				|| false == searchInfoCheck())
		{
			return false;
		}
		rstCode = "0";
		rstMsg = "成功";
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
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		return document.asXML();
	}

	/**
	 * 生成接口回参
	 * 
	 * @param infoMap
	 * @return
	 */
	public String commonReturnParam(Map<String, String> infoMap)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		if (infoMap != null && !infoMap.isEmpty())
		{
			root.addElement("result_flag").addText(infoMap.get("result_flag"));
			root.addElement("result").addText(infoMap.get("result"));
			infoMap.remove("result_flag");
			infoMap.remove("result");
			Element sheets = root.addElement("Sheets");
			Element sheetInfo = sheets.addElement("sheetInfo");
			for (Object keyStr : infoMap.keySet())
			{
				sheetInfo.addElement(keyStr.toString()).addText(infoMap.get(keyStr));
			}
		}
		return document.asXML();
	}
}
