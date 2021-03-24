package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

public class ModifyWifiPowerChecker extends BaseChecker
{

	// wifi名称
	private String WifiName = "";

	// wifi强度
	private int WifiPower;

	public ModifyWifiPowerChecker(String inXml)
	{
		this.callXml = inXml;
	}
	
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;

		String WifiPowerString = "";

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
			WifiName = param.elementTextTrim("WifiName");
			WifiPowerString = param.elementTextTrim("WifiPower");
		}
		catch (Exception e) {
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

		if (StringUtil.IsEmpty(WifiName)) {
			result = 1;
			resultDesc = "Wifi名称不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(WifiPowerString)) {
			result = 1;
			resultDesc = "Wifi强度不能为空";
			return false;
		}
		else {
			WifiPower = StringUtil.getIntegerValue(WifiPowerString);
			if (WifiPower != 1 && WifiPower != 2 && WifiPower != 3 && WifiPower != 4 && WifiPower != 5) {
				result = 1005;
				resultDesc = "Wifi强度要求范围1-5";
				return false;
			}
		}

		if (3 == userInfoType && userInfo.length() < 6) {
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

	public String getWifiName() {
		return WifiName;
	}

	public void setWifiName(String wifiName) {
		WifiName = wifiName;
	}

	public int getWifiPower() {
		return WifiPower;
	}

	public void setWifiPower(int wifiPower) {
		WifiPower = wifiPower;
	}
}
