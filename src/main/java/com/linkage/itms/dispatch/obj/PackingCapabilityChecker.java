package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;

public class PackingCapabilityChecker extends BaseChecker
{
	private String cpe_onlinestatus = "";
	private String cpetype = "";
	private String cpemodle = "";
	private String cpewifi = "";
	private String cpemaxspeed = "";
	private String cpelannum = "";
	private String cpeonlinetime = "";
	private String cepmaxuser = "";
	private List<HashMap<String, String>> lansList = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> wansList = new ArrayList<HashMap<String, String>>();
	
	public PackingCapabilityChecker(String inXml)
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
		if (0==result) {
			root.addElement("CPE_ONLINESTATUS").addText(cpe_onlinestatus);
			root.addElement("CPETYPE").addText(cpetype);
			root.addElement("CPEMODLE").addText(cpemodle);
			root.addElement("CPEWIFI").addText(cpewifi);
			root.addElement("CPEMAXSPEED").addText(cpemaxspeed);
			root.addElement("CPELANNUM").addText(cpelannum);
			root.addElement("CPEONLINETIME").addText(cpeonlinetime);
			root.addElement("CEPMAXUSER").addText(cepmaxuser);
			Element CPELANIFNO = root.addElement("CPELANIFNO");
			if (lansList != null && lansList.size() > 0)
			{
				HashMap<String, String> tmp = null;
				for (int i = 0; i < lansList.size(); i++)
				{
					tmp = lansList.get(i);
					String Status = StringUtil.getStringValue(tmp.get("Status"));
					String MaxBitRate = StringUtil.getStringValue(tmp.get("MaxBitRate"));
					String num = StringUtil.getStringValue(tmp.get("num"));
					Element CPELANIFNONUM = CPELANIFNO.addElement("CPELANIFNONUM");
					CPELANIFNONUM.addElement("CPELAN_NAME").addText(num);
					CPELANIFNONUM.addElement("CPELAN_STATUS").addText(Status);
					CPELANIFNONUM.addElement("CPELAN_MAXSPEED").addText(MaxBitRate);
				}
			}
			
			Element CPEWIFIIFNO = root.addElement("CPEWIFIIFNO");
			if (wansList != null && wansList.size() > 0)
			{
				HashMap<String, String> tmp = null;
				for (int i = 0; i < wansList.size(); i++)
				{
					tmp = wansList.get(i);
					String SSID = StringUtil.getStringValue(tmp.get("SSID"));
					String ChannelsInUse = StringUtil.getStringValue(tmp.get("ChannelsInUse"));
					String PowerValue = StringUtil.getStringValue(tmp.get("X_CT-COM_PowerValue"));
					String Status = StringUtil.getStringValue(tmp.get("Status"));
					String TotalAssociations = StringUtil.getStringValue(tmp.get("TotalAssociations"));
					Element CPEWIFIIFNONUM = CPEWIFIIFNO.addElement("CPEWIFIIFNONUM");
					CPEWIFIIFNONUM.addElement("WLAN_NAME").addText(SSID);
					CPEWIFIIFNONUM.addElement("WLAN_CHANNEL").addText(ChannelsInUse);
					CPEWIFIIFNONUM.addElement("WLAN_POWER").addText(PowerValue);
					CPEWIFIIFNONUM.addElement("WLAN_STATUS").addText(Status);
					CPEWIFIIFNONUM.addElement("WLAN_CONNUSERNUM").addText(TotalAssociations);
				}
			}
		}
		
		return document.asXML();
	}

	public String getCpe_onlinestatus() {
		return cpe_onlinestatus;
	}

	public void setCpe_onlinestatus(String cpe_onlinestatus) {
		this.cpe_onlinestatus = cpe_onlinestatus;
	}

	public String getCpetype() {
		return cpetype;
	}

	public void setCpetype(String cpetype) {
		this.cpetype = cpetype;
	}

	public String getCpemodle() {
		return cpemodle;
	}

	public void setCpemodle(String cpemodle) {
		this.cpemodle = cpemodle;
	}

	public String getCpewifi() {
		return cpewifi;
	}

	public void setCpewifi(String cpewifi) {
		this.cpewifi = cpewifi;
	}

	public String getCpemaxspeed() {
		return cpemaxspeed;
	}

	public void setCpemaxspeed(String cpemaxspeed) {
		this.cpemaxspeed = cpemaxspeed;
	}

	public String getCpelannum() {
		return cpelannum;
	}

	public void setCpelannum(String cpelannum) {
		this.cpelannum = cpelannum;
	}

	public String getCpeonlinetime() {
		return cpeonlinetime;
	}

	public void setCpeonlinetime(String cpeonlinetime) {
		this.cpeonlinetime = cpeonlinetime;
	}

	public String getCepmaxuser() {
		return cepmaxuser;
	}

	public void setCepmaxuser(String cepmaxuser) {
		this.cepmaxuser = cepmaxuser;
	}

	public List<HashMap<String, String>> getLansList() {
		return lansList;
	}

	public void setLansList(List<HashMap<String, String>> lansList) {
		this.lansList = lansList;
	}

	public List<HashMap<String, String>> getWansList() {
		return wansList;
	}

	public void setWansList(List<HashMap<String, String>> wansList) {
		this.wansList = wansList;
	}


}
