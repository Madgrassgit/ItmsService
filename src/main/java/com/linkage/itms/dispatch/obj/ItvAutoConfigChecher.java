package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-21
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ItvAutoConfigChecher extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(ItvAutoConfigChecher.class);
	private List<Map<String,String>> gatherMacMapList = null;
	private String zeroConf;
	public ItvAutoConfigChecher(String inXml)
	{
		callXml = inXml;
	}
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
						|| false == userInfoCheck()) {
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

	
	public List<Map<String, String>> getGatherMacMapList()
	{
		return gatherMacMapList;
	}

	
	public void setGatherMacMapList(List<Map<String, String>> gatherMacMapList)
	{
		this.gatherMacMapList = gatherMacMapList;
	}
	
	public String getZeroConf()
	{
		return zeroConf;
	}
	
	public void setZeroConf(String zeroConf)
	{
		this.zeroConf = zeroConf;
	}
	
}
