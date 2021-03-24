
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

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月22日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryVOIPWanInfoChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QueryVOIPWanInfoChecker.class);
	
	/**
	 * 设备dhcp ip地址
	 */
	private String externalIPAddress = "";
	
	/**
	 * 设备连接状态
	 */
	private String connectionStatus = "";
	
	/**
	 * 设备物理标识
	 */
	private String interfaceID = "";
	
	//voip信息
	List<HashMap<String ,String>> voipInfos = new ArrayList<HashMap<String,String>>();

	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public QueryVOIPWanInfoChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("QueryVOIPWanInfoChecker -> check(),inXML ({})",callXml);
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
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck()
				|| false == cityIdCheck())
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
		root.addElement("RstMsg").addText("" + resultDesc);
		
		if(null != voipInfos && !voipInfos.isEmpty())
		{
			Element param = root.addElement("Param");
			for(HashMap<String ,String> map : voipInfos)
			{
				Element voipInfo =  param.addElement("VoipInfo");
				voipInfo.addElement("ExternalIPAddress").addText(map.get("ExternalIPAddress"));
				voipInfo.addElement("ConnectionStatus").addText(map.get("ConnectionStatus"));
				voipInfo.addElement("InterfaceID").addText(map.get("InterfaceID"));
			}
		}
		
		return document.asXML();
	}

	
	public String getExternalIPAddress()
	{
		return externalIPAddress;
	}

	
	public void setExternalIPAddress(String externalIPAddress)
	{
		this.externalIPAddress = externalIPAddress;
	}

	
	public String getConnectionStatus()
	{
		return connectionStatus;
	}

	
	public void setConnectionStatus(String connectionStatus)
	{
		this.connectionStatus = connectionStatus;
	}

	
	public String getInterfaceID()
	{
		return interfaceID;
	}

	
	public void setInterfaceID(String interfaceID)
	{
		this.interfaceID = interfaceID;
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
