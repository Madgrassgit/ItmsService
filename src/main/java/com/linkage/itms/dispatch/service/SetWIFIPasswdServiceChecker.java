package com.linkage.itms.dispatch.service;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.Base64;
import com.linkage.itms.dispatch.obj.BaseChecker;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月24日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetWIFIPasswdServiceChecker extends BaseChecker
{
	
	private String wifiPasswd;
	private String ssid;

	
	public SetWIFIPasswdServiceChecker(String inXml){
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
			wifiPasswd = param.elementTextTrim("WifiPasswd");
			ssid = param.elementTextTrim("SSID");
			
			if(StringUtil.IsEmpty(wifiPasswd) && StringUtil.IsEmpty(ssid)){
				result = 1011;
				resultDesc = "密码和SSID不能同时为空";
				return false;
			}
  		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if(3 != userInfoType && 2 != userInfoType && 1 != userInfoType){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1002;
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

	public String getWifiPasswd()
	{
		return wifiPasswd;
	}

	public void setWifiPasswd(String wifiPasswd)
	{
		this.wifiPasswd = wifiPasswd;
	}

	public String getSsid()
	{
		return ssid;
	}

	public void setSsid(String ssid)
	{
		this.ssid = ssid;
	}

}
