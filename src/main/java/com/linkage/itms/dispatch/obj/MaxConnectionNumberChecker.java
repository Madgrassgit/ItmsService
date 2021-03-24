package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;

public class MaxConnectionNumberChecker extends BaseChecker
{
	private String TotalTerminalNumber="";
	
	public MaxConnectionNumberChecker(String inXml)
	{
		this.callXml = inXml;
	}
	
	@Override
	public boolean check()
	{
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			TotalTerminalNumber = param.elementTextTrim("TotalTerminalNumber");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if(2 != userInfoType && 1 != userInfoType){
			result = 2;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1;
			resultDesc = "用户信息不能为空";
			return false;
		}
		if(3==userInfoType && userInfo.length()<6){
			result = 1005;
			resultDesc = "设备序列号非法，设备序列号不可少于6位";
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

	public String getTotalTerminalNumber() {
		return TotalTerminalNumber;
	}

	public void setTotalTerminalNumber(String totalTerminalNumber) {
		TotalTerminalNumber = totalTerminalNumber;
	}
	
}
