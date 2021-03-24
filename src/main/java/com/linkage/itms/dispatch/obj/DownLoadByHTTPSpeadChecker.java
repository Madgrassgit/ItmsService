
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;

/**
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2016年6月26日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DownLoadByHTTPSpeadChecker extends BaseChecker
{

	/**
	 * 宽带速率
	 */
	private String speed = "";
	/**
	 * Wan通道
	 */
	private String wanPassageWay = "";
	/**
	 * 
	 */
	private String downURL = "";
	/**
	 * 报文设置的优先级
	 */
	private String priority = "";
	/**
	 * 宽带账号
	 */
	private String username = "";
	/**
	 * 测试账号
	 */
	private String testUserName = "";
	/**
	 * 测试密码
	 */
	private String testPassword = "";
	/**
	 * 传输开始时间
	 */
	private String transportStartTime = "";
	/**
	 * 传输结束时间
	 */
	private String transportEndTime = "";
	/**
	 * 接受字节数（包括控制头）
	 */
	private String receiveByteContainHead = "";
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
	/**
	 * 设备ID，入表的时候用
	 */
	private String deviceId = "";
	/**
	 * 设备ID，入表的时候用
	 */
	private int speedStatus = 0;

	public DownLoadByHTTPSpeadChecker(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(DownLoadByHTTPSpeadChecker.class);

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
			loid = param.elementTextTrim("Loid");
			username = param.elementTextTrim("UserName");
			speed = param.elementText("Speed");
			cityId = param.elementTextTrim("CityId");
			downURL = param.elementTextTrim("DownURL");
			priority = param.elementTextTrim("Priority");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == cityIdCheck())
		{
			return false;
		}
		if (StringUtil.IsEmpty(loid))
		{
			result = 1;
			resultDesc = "loid不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(username))
		{
			result = 1;
			resultDesc = "宽带账号不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(speed))
		{
			result = 1;
			resultDesc = "Spead不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(downURL))
		{
			result = 1;
			resultDesc = "DownURL不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(priority))
		{
			result = 1;
			resultDesc = "报文优先级不能为空";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	public String getParam()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("DevSn").addText(devSn == null ? "" : devSn);
		root.addElement("UserName").addText(username);
		root.addElement("CityId").addText(cityId);
		root.addElement("Speed").addText(speed);
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues);
		root.addElement("Avg2").addText("0");
		root.addElement("Max2").addText("0");
		root.addElement("TransportStartTime").addText(transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime);
		root.addElement("IP").addText(ip);
		root.addElement("ReceiveByte").addText(receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime);
		return document.asXML();
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
		root.addElement("UserName").addText(username);
		root.addElement("CityId").addText(cityId);
		root.addElement("Speed").addText(speed);
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues);
		root.addElement("Avg2").addText("0");
		root.addElement("Max2").addText("0");
		root.addElement("TransportStartTime").addText(transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime);
		root.addElement("IP").addText("" +ip);
		root.addElement("ReceiveByte").addText(receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime);
		insertTest();
		return document.asXML();
	}
	
	public void insertTest(){
		try{
			logger.warn("DownLoadByHTTPSpeadChecker==>insertTest(),开始入库");
			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			deviceInfoDAO.ahInsertTestSpeedDev(deviceId == null ? "" : deviceId, devSn == null ? "" : devSn, username, cityId, speed, avgSampledTotalValues,
					maxSampledTotalValues, transportStartTime, transportEndTime, ip, receiveByte, tcpRequestTime, tcpResponseTime, speedStatus);
		}catch (Exception e) {
			logger.error("insert table ahInsertTestSpeedDev error:{}",e.getMessage());
		}
		logger.warn("DownLoadByHTTPSpeadChecker==>insertTest(),结果入库完成");
	}

	public String getWanPassageWay()
	{
		return wanPassageWay;
	}

	public String getDownURL()
	{
		return downURL;
	}

	public String getPriority()
	{
		return priority;
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

	public void setWanPassageWay(String wanPassageWay)
	{
		this.wanPassageWay = wanPassageWay;
	}

	public void setDownURL(String downURL)
	{
		this.downURL = downURL;
	}

	public void setPriority(String priority)
	{
		this.priority = priority;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getTestUserName()
	{
		return testUserName;
	}

	public String getTestPassword()
	{
		return testPassword;
	}

	public void setTestUserName(String testUserName)
	{
		this.testUserName = testUserName;
	}

	public void setTestPassword(String testPassword)
	{
		this.testPassword = testPassword;
	}

	public String getTransportStartTime()
	{
		return transportStartTime;
	}

	public String getTransportEndTime()
	{
		return transportEndTime;
	}

	public String getReceiveByteContainHead()
	{
		return receiveByteContainHead;
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

	public void setReceiveByteContainHead(String receiveByteContainHead)
	{
		this.receiveByteContainHead = receiveByteContainHead;
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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getSpeedStatus() {
		return speedStatus;
	}

	public void setSpeedStatus(int speedStatus) {
		this.speedStatus = speedStatus;
	}
	
}
