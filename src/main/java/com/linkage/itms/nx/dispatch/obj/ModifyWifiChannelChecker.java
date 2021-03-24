
package com.linkage.itms.nx.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author songxq (Ailk No.)
 * @version 1.0
 * @since 2018-7-26 下午2:29:59
 * @category 
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ModifyWifiChannelChecker extends NxBaseChecker
{

	public static final Logger logger = LoggerFactory
			.getLogger(ModifyWifiChannelChecker.class);
	// 客户端调用XML字符串
	protected String callXml;
	/**
	 * 失败描述
	 */
	private String failReason = "";
	/**
	 * 操作类型
	 */
	private String operateType;
	/**
	 * channel
	 */
	private String channel;
	
	/*
	 * ChannelsInUse
	 */
	
	private String channelsInUse;
	
	public ModifyWifiChannelChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("ModifyWifiChannelChecker>check()");
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
			channel = param.elementTextTrim("Channel");
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == channelCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	/**
	 * 用户信息类型合法性检查 此接口返回的错误码和basechecker里的不同，所以重写了方法
	 */
	boolean userInfoTypeCheck()
	{
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType)
		{
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
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
		root.addElement("UserInfo").addText("" + userInfo);
		root.addElement("Channel").addText("" + channel);
		
		
		return document.asXML();
	}
	
	
	/*
	 * 
	 * channel 合法性校验
	 */
	protected boolean channelCheck()
	{
		int channelNum = Integer.parseInt(channel);
		if(channelNum < 0 || channelNum > 13)
		{
			result = 1010;
			resultDesc = "chanel 非法";
			return false;
		}
		
		return true;
	}
	
	public String getOperateType()
	{
		return operateType;
	}

	
	public void setOperateType(String operateType)
	{
		this.operateType = operateType;
	}

	
	

	
	
	public String getChannel()
	{
		return channel;
	}

	
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getFailReason()
	{
		return failReason;
	}

	
	public void setFailReason(String failReason)
	{
		this.failReason = failReason;
	}

	
	public String getChannelsInUse()
	{
		return channelsInUse;
	}

	
	public void setChannelsInUse(String channelsInUse)
	{
		this.channelsInUse = channelsInUse;
	}
	
	
	
}
