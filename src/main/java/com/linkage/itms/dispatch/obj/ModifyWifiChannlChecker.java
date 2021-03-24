package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

public class ModifyWifiChannlChecker extends BaseChecker
{
	private String WIfiName = "";

	private int WIfiChannel;

	public ModifyWifiChannlChecker(String inXml)
	{
		this.callXml = inXml;
	}
	
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;

		String WIfiChannelString = "";
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			WIfiName = param.elementTextTrim("WIfiName");
			WIfiChannelString = param.elementTextTrim("WIfiChannel");
		}
		catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if (2 != userInfoType && 1 != userInfoType) {
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

		if (StringUtil.IsEmpty(WIfiName)) {
			result = 1;
			resultDesc = "Wifi名称不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(WIfiChannelString)) {
			result = 1;
			resultDesc = "Wifi信道不能为空";
			return false;
		}
		else {
			WIfiChannel = StringUtil.getIntegerValue(WIfiChannelString);
			if (WIfiChannel != 0 && WIfiChannel != 1 && WIfiChannel != 2 && WIfiChannel != 3 && WIfiChannel != 4 && WIfiChannel != 5 && WIfiChannel != 6 &&
					WIfiChannel != 7 && WIfiChannel != 8 && WIfiChannel != 9 && WIfiChannel != 10 && WIfiChannel != 11 && WIfiChannel != 12 && WIfiChannel != 13 &&
					WIfiChannel != 36 && WIfiChannel != 40 && WIfiChannel != 44 && WIfiChannel != 48 && WIfiChannel != 52 && WIfiChannel != 56 && WIfiChannel != 60 &&
					WIfiChannel != 64 && WIfiChannel != 149 && WIfiChannel != 153 && WIfiChannel != 157 && WIfiChannel != 161 && WIfiChannel != 165) {
				result = 1005;
				resultDesc = "2.4G信道是0-13;5G信道是0,36,40,44,48,52,56,60,64,149,153,157,161,165";
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

	public String getWIfiName() {
		return WIfiName;
	}

	public void setWIfiName(String WIfiName) {
		this.WIfiName = WIfiName;
	}

	public int getWIfiChannel() {
		return WIfiChannel;
	}

	public void setWIfiChannel(int WIfiChannel) {
		this.WIfiChannel = WIfiChannel;
	}
}
