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

/**
 * 采集接口参数校验类
 * 
 * @author xuzhicheng
 * @version 1.0
 * @since 2015年3月24日
 */
public class DevInfoGatherCheck extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(DevInfoGatherCheck.class);
	
	// 查询用户帐号
	private String userName;
	
	//发送光功率
	private String ponSend;
	
	//接收光功率
	private String ponReceive;
	
	//LAN信息
	List<HashMap<String ,String>> lanInfos = new ArrayList<HashMap<String,String>>();
	
	//voip信息
	List<HashMap<String ,String>> voipInfos = new ArrayList<HashMap<String,String>>();
	
	public DevInfoGatherCheck(String inXml)
	{
		callXml = inXml;
	}
	
	@Override
	public boolean check() 
	{	

		logger.debug("check() ----DevInfoGather的入参");
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
			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userName = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (!baseCheck() 
				|| !devSnCheck()
				|| ! cityIdCheck() 
				|| ! searchTypeCheck() 
				|| ! userInfoTypeCheck()) 
		{
					return false;
		}
		// 用户账号合法性检查
		if (1 == searchType && StringUtil.IsEmpty(userName)) 
		{
			result = 1002;
			resultDesc = "用户帐号不合法";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;

	}
	
	boolean userInfoTypeCheck(){
		if(1 != userInfoType && 2 != userInfoType
				&& 3 != userInfoType && 4 != userInfoType
				&& 5 != userInfoType ){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 采集接口返回组装返回XML
	 */
	@Override
	public String getReturnXml() 
	{
		logger.debug("getReturnXml()---组装采集接口返回XML");
		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea))
		{
			document.setXMLEncoding(Global.codeTypeValue);
		}
		else
		{
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		root.addElement("CmdID").addText(cmdId);
		// 返回结果集
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		if(0 == result)
		{
			Element param = root.addElement("Param");
			//还未赋值
			param.addElement("PonSend").addText(ponSend);
			param.addElement("PonReceive").addText(ponReceive);
			for(int i = 0;i<lanInfos.size();i++)
			{
				Element lanInfo = param.addElement("LanInfo");
				lanInfo.addElement("lan").addText(lanInfos.get(i).get("lan"));
				lanInfo.addElement("status").addText(lanInfos.get(i).get("status"));
				lanInfo.addElement("lanSend").addText(lanInfos.get(i).get("lanSend"));
				lanInfo.addElement("lanReceive").addText(lanInfos.get(i).get("lanReceive"));
			}	
			
			for(HashMap<String ,String> tempMap : voipInfos)
			{
				Element voipInfo = param.addElement("VoipInfo");
				voipInfo.addElement("voip").addText(tempMap.get("voip"));
				voipInfo.addElement("status").addText(tempMap.get("status"));
				voipInfo.addElement("PendingTimerInit").addText(tempMap.get("PendingTimerInit"));
				voipInfo.addElement("RetranIntervalTimer").addText(tempMap.get("RetranIntervalTimer"));
			}
		}
		String returnXML = document.asXML();
		logger.warn("返回参数：[{}]",returnXML);
		return returnXML;
	}

	public String getUserName() 
	{
		return userName;
	}

	public void setUserName(String userName) 
	{
		this.userName = userName;
	}

	public String getPonSend() 
	{
		return ponSend;
	}

	public void setPonSend(String ponSend) 
	{
		this.ponSend = ponSend;
	}

	public String getPonReceive() 
	{
		return ponReceive;
	}

	public void setPonReceive(String ponReceive) 
	{
		this.ponReceive = ponReceive;
	}

	public List<HashMap<String, String>> getLanInfos() 
	{
		return lanInfos;
	}

	public void setLanInfos(List<HashMap<String, String>> lanInfos) 
	{
		this.lanInfos = lanInfos;
	}

	
	public List<HashMap<String, String>> getVoipInfos()
	{
		return voipInfos;
	}

	
	public void setVoipInfos(List<HashMap<String, String>> voipInfos)
	{
		this.voipInfos = voipInfos;
	}

	
	
}
