package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @since 2017-10-17
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ActivationOfVoiceChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(ActivationOfVoiceChecker.class);
	private String userType;
	private List<HashMap<String,String>> voiplist = new ArrayList<HashMap<String,String>>();
	private List<HashMap<String,String>> devicelist = new ArrayList<HashMap<String,String>>();
	public ActivationOfVoiceChecker(String inXml){
		callXml=inXml;
	}

	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType=StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo=param.elementTextTrim("UserInfo");
			userType=param.elementTextTrim("UserType");
		}catch(Exception e){
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if (false == baseCheck()) {
			return false;
		}
		if ( 2 != userInfoType && 4 != userInfoType && 5 != userInfoType) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if( !userType.equals("1")&& !userType.equals("2"))
		{
			result = 1007;
			resultDesc = "用户类型非法";
			return false;
		}
		if(StringUtil.IsEmpty(userType))
		{
			result = 1008;
			resultDesc = "用户信息为空";
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
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		Map tempMap = null;
		if(voiplist!=null&&voiplist.size()>0)
		{
			root.addElement("DeviceSerialnumber").addText(devicelist.get(0).get("device_serialnumber"));
			root.addElement("DeviceModel").addText(devicelist.get(0).get("device_model"));
			Element sheets = root.addElement("sheets");
			for(int i=0;i<voiplist.size();i++){
			tempMap=voiplist.get(i);
			String voipphone ="";
			String voipport  ="";
			if(null!=tempMap){
				voipphone=StringUtil.getStringValue(tempMap.get("voip_phone"));
				if(userType.equals("1"))
				{
				voipport=StringUtil.getStringValue(tempMap.get("voip_port"));
				}else
				{
				voipport=StringUtil.getStringValue(tempMap.get("line_id"));	
				}
			}
			Element internet = sheets.addElement("sheet");
			internet.addElement("VoipPort").addText(voipport);
			internet.addElement("VoipPhone").addText(voipphone);
			}
		}
		return document.asXML();
	}

	
	public String getUserType()
	{
		return userType;
	}

	
	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	
	public List<HashMap<String, String>> getVoiplist()
	{
		return voiplist;
	}
	public void setVoiplist(List<HashMap<String, String>> voiplist)
	{
		this.voiplist = voiplist;
	}

	
	public List<HashMap<String, String>> getDevicelist()
	{
		return devicelist;
	}

	
	public void setDevicelist(List<HashMap<String, String>> devicelist)
	{
		this.devicelist = devicelist;
	}
	
}
