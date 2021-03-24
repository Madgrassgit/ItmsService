
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.DeviceInfoDAO;

/**
 * @author chenxj6 (Ailk No.)
 * @version 1.0
 * @since 2016年10月31日
 * @江西预检预修HTTP下载宽带上网测速对外接口需求
 */
public class DownLoadByHTTPSpeadChecker4SD extends BaseChecker
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
	 * 是否入库
	 */
	private String isInsert = "";
	/**
	 * 设备id
	 */
	private String deviceId = "";
	/**
	 * 上网方式
	 */
	private String wanType = "";
	/**
	 * 测试开始时间
	 */
	private long testStartTime;
	/**
	 * 多宽带时根据vlanId决定采集哪条宽带
	 */
	private int vlanId;
	
	private String DSpeed;

	public DownLoadByHTTPSpeadChecker4SD(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(DownLoadByHTTPSpeadChecker4JX.class);

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
			// 测速时用来测速的宽带账号
			testUserName = param.elementTextTrim("UserName");
			// 测速时用来测速的宽带密码
			testPassword = param.elementTextTrim("PassWord");
			speed = param.elementText("Speed");
//			vlanId = Integer.parseInt(param.elementText("VlanId"));
//			cityId = param.elementTextTrim("CityId");
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
		if (false == baseCheck())// || false == cityIdCheck())
		{
			return false;
		}
		
		if (StringUtil.IsEmpty(speed))
		{
			if((StringUtil.IsEmpty(testUserName)||StringUtil.IsEmpty(testPassword)))
			{
				result = 1;
				resultDesc = "Speed为空时，测速时用来测速的宽带账号和宽带密码不能为空";
				return false;
			}
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
		// username 是宽带账号
		root.addElement("UserName").addText(username == null ? "" : username);
		root.addElement("Speed").addText(speed == null ? "" : speed);
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues == null ? "" : avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues == null ? "" : maxSampledTotalValues);
		root.addElement("Avg2").addText("0");
		root.addElement("Max2").addText("0");
		root.addElement("TransportStartTime").addText(transportStartTime == null ? "" : transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime == null ? "" : transportEndTime);
		root.addElement("IP").addText(ip == null ? "" : ip);
		root.addElement("ReceiveByte").addText(receiveByte == null ? "" : receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime == null ? "" : tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime == null ? "" : tcpResponseTime);
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
		// username 是宽带账号
		root.addElement("UserName").addText(username == null ? "" : username);
//		root.addElement("CityId").addText(cityId == null ? "" : cityId);
		
		root.addElement("DSpeed").addText(DSpeed == null ? "" : DSpeed);
		
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues == null ? "" : avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues == null ? "" : maxSampledTotalValues);
		root.addElement("TransportStartTime").addText(transportStartTime == null ? "" : transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime == null ? "" : transportEndTime);
		root.addElement("IP").addText(ip == null ? "" : ip);
		root.addElement("ReceiveByte").addText(receiveByte == null ? "" : receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime == null ? "" : tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime == null ? "" : tcpResponseTime);
		if ("1".equals(isInsert)){
			insertTest();
		}
		return document.asXML();
	}
	
	
	public void insertTest(){
		try{
			logger.debug("DownLoadByHTTPSpeadChecker4JX==>insertTest(),开始入库");
			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			deviceInfoDAO.jxInsertTestSpeedDev(deviceId == null ? "" : deviceId,cmdId,result,resultDesc, devSn == null ? "" : devSn, username, speed, avgSampledTotalValues,
					maxSampledTotalValues, transportStartTime, transportEndTime, ip, receiveByte, tcpRequestTime, tcpResponseTime, downURL, testStartTime, wanType, loid, testUserName,clientType);
		}catch (Exception e) {
			logger.error("insert table tab_http_diag_result_intf error:{}",e.getMessage());
		}
		logger.debug("DownLoadByHTTPSpeadChecker4JX==>insertTest(),结果入库完成");
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

	public int getVlanId() {
		return vlanId;
	}

	public void setVlanId(int vlanId) {
		this.vlanId = vlanId;
	}

	public String getIsInsert() {
		return isInsert;
	}

	public void setIsInsert(String isInsert) {
		this.isInsert = isInsert;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getWanType() {
		return wanType;
	}

	public void setWanType(String wanType) {
		this.wanType = wanType;
	}

	public long getTestStartTime() {
		return testStartTime;
	}

	public void setTestStartTime(long testStartTime) {
		this.testStartTime = testStartTime;
	}
	
	public String getDSpeed()
	{
		return DSpeed;
	}

	
	public void setDSpeed(String dSpeed)
	{
		DSpeed = dSpeed;
	}
	
}
