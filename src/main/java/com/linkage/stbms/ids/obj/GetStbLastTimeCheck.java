package com.linkage.stbms.ids.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2018-1-23
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbLastTimeCheck extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(GetStbLastTimeCheck.class);
	
	public String LastTime;
	
	public GetStbLastTimeCheck(String inXml){
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
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			
			Element param = root.element("Param");
			searchType =StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			userInfo =param.elementTextTrim("SearchInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if(1 !=clientType&&2 !=clientType&&3 !=clientType&&4 !=clientType&&5 !=clientType&&6 !=clientType&&7 !=clientType)
		{
			result=2;
			resultDesc="客户端类型非法";
			return false;
		}
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		if(1 !=searchType&&2 !=searchType&&3 !=searchType)
		{
			result=1001;
			resultDesc="查询类型非法";
			return false;
		}
		if(StringUtil.IsEmpty(userInfo))
		{
			result=1002;
			resultDesc="业务帐号或MAC地址不能为空";
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
		// SSID名称
		if(!StringUtil.IsEmpty(LastTime))
		{
		root.addElement("LastTime").addText("" + LastTime);
		}
		return document.asXML();
	}
	
	public String getLastTime()
	{
		return LastTime;
	}
	
	public void setLastTime(String lastTime)
	{
		LastTime = lastTime;
	}
	
}
