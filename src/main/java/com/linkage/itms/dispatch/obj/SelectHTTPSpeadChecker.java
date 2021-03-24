
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 新疆宽带测速结果查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2018-5-29
 */
public class SelectHTTPSpeadChecker extends BaseChecker
{
	/**
	 * 宽带速率
	 */
	private String speed = "";
	/**
	 * 宽带账号
	 */
	private String username = "";
	/**
	 * 传输开始时间
	 */
	private String transportStartTime = "";
	/**
	 * 传输结束时间
	 */
	private String transportEndTime = "";
	/**
	 * 接受字节数
	 */
	private String receiveByte = "";
	/**
	 * TCP请求时间
	 */
	private String tcpRequestTime = "";
	/**
	 * 下行平均值
	 */
	private String avgSampledTotalValues = "";
	/**
	 * 下行最大值
	 */
	private String maxSampledTotalValues = "";
	/**
	 * TCP响应时间
	 */
	private String tcpResponseTime = "";

	public SelectHTTPSpeadChecker(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(SelectHTTPSpeadChecker.class);

	@Override
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
		
		
		if(3 != userInfoType && 2 != userInfoType && 1 != userInfoType){
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
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("DevSn").addText(devSn == null ? "" : devSn);
		// username 是宽带账号
		root.addElement("UserName").addText(username == null ? "" : username);
		root.addElement("Speed").addText(speed == null ? "" : speed);
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues == null ? "" : avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues == null ? "" : maxSampledTotalValues);
		root.addElement("TransportStartTime").addText(transportStartTime == null ? "" : transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime == null ? "" : transportEndTime);
		root.addElement("IP").addText(ip == null ? "" : ip);
		root.addElement("ReceiveByte").addText(receiveByte == null ? "" : receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime == null ? "" : tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime == null ? "" : tcpResponseTime);
		return document.asXML();
	}

	public String getUsername()
	{
		return username;
	}

	public String getSpeed()
	{
		return speed;
	}

	public void setSpeed(String speed)
	{
		this.speed = speed;
	}


	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getTransportStartTime()
	{
		return transportStartTime;
	}

	public String getTransportEndTime()
	{
		return transportEndTime;
	}

	public String getReceiveByte()
	{
		return receiveByte;
	}

	public String getTcpRequestTime()
	{
		return tcpRequestTime;
	}

	public String getTcpResponseTime()
	{
		return tcpResponseTime;
	}

	public void setTransportStartTime(String transportStartTime)
	{
		this.transportStartTime = transportStartTime;
	}

	public void setTransportEndTime(String transportEndTime)
	{
		this.transportEndTime = transportEndTime;
	}

	public void setReceiveByte(String receiveByte)
	{
		this.receiveByte = receiveByte;
	}

	public void setTcpRequestTime(String tcpRequestTime)
	{
		this.tcpRequestTime = tcpRequestTime;
	}

	public void setTcpResponseTime(String tcpResponseTime)
	{
		this.tcpResponseTime = tcpResponseTime;
	}

	
	public String getAvgSampledTotalValues()
	{
		return avgSampledTotalValues;
	}

	
	public String getMaxSampledTotalValues()
	{
		return maxSampledTotalValues;
	}

	
	public void setAvgSampledTotalValues(String avgSampledTotalValues)
	{
		this.avgSampledTotalValues = avgSampledTotalValues;
	}

	
	public void setMaxSampledTotalValues(String maxSampledTotalValues)
	{
		this.maxSampledTotalValues = maxSampledTotalValues;
	}

}
