
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class QueryWiFiDeviceMACChecker extends BaseChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceTempChecker.class);
	private String ssidNum = "";
	private String wifiDeviceMAC = "";
	private String cityId = "";
	private List<HashMap<String, String>> macList = new ArrayList<HashMap<String, String>>();

	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public QueryWiFiDeviceMACChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 参数合法性检查
	 */
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			ssidNum = param.elementTextTrim("SSIDnum");
			// 江西电信的接口入参还有其他信息
			if ("jx_dx".equals(Global.G_instArea))
			{
				cityId = param.elementTextTrim("CityId");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == this.userInfoTypeCheck()
				|| false == userInfoCheck() || false == ssidNumCheck()
				|| false == cityIdCheck())
		{
			return false;
		}
		// 表示 userInfo 入的是设备序列号
		if (6 == userInfoType)
		{
			if (userInfo.length() < 6)
			{
				result = 1007;
				resultDesc = "设备序列号长度不能小于6位";
				return false;
			}
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	/**
	 * 用户信息合法性检查
	 * 
	 * @return boolean
	 */
	protected boolean ssidNumCheck()
	{
		if (StringUtil.IsEmpty(ssidNum))
		{
			result = 1001;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}

	protected boolean cityIdCheck()
	{
		// 江西电信的入参有cityid,需要校验
		if ("jx_dx".equals(Global.G_instArea))
		{
			if (StringUtil.IsEmpty(cityId))
			{
				result = 1007;
				resultDesc = "属地非法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验用户信息查询类型
	 */
	protected boolean userInfoTypeCheck()
	{
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
				&& 4 != userInfoType && 5 != userInfoType && 6 != userInfoType)
		{
			if ("jx_dx".equals(Global.G_instArea))
			{
				result = 1001;
				resultDesc = "查询类型非法";
				return false;
			}
			else
			{
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 回参
	 */
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
		Element macPorts = root.addElement("WiFiMAC");
		if (macList != null && macList.size() > 0)
		{
			HashMap<String, String> tmp = null;
			for (int i = 0; i < macList.size(); i++)
			{
				tmp = macList.get(i);
				String macId = StringUtil.getStringValue(tmp.get("macId"));
				String mac = StringUtil.getStringValue(tmp.get("mac"));
				Element macNums = macPorts.addElement("WiFiDEV").addAttribute("num",
						macId);
				macNums.addElement("WiFiDEVNUM").addText(macId);
				macNums.addElement("WIFIDeviceMAC").addText(mac);
			}
		}
		return document.asXML();
	}

	public String getSsidNum()
	{
		return ssidNum;
	}

	public void setSsidNum(String ssidNum)
	{
		this.ssidNum = ssidNum;
	}

	public String getWifiDeviceMAC()
	{
		return wifiDeviceMAC;
	}

	public void setWifiDeviceMAC(String wifiDeviceMAC)
	{
		this.wifiDeviceMAC = wifiDeviceMAC;
	}

	public List<HashMap<String, String>> getMacList()
	{
		return macList;
	}

	public void setMacList(List<HashMap<String, String>> macList)
	{
		this.macList = macList;
	}
}
