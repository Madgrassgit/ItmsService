
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
 * 江西电信ITMS+家庭网关互联网专线测速接口需求
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2019-2-27
 */
public class SpecialSpeedChecker4JX extends BaseChecker
{
	/**
	 * 
	 */
	private String downURL = "";
//	/**
//	 * 报文设置的优先级
//	 */
//	private String priority = "";
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
	
	/**
	 * 测速结果值
	 */
	private String sampledValues;
	
	/**
	 * 总体速率采样
	 */
	private String sampledTotalValues;
	
	/**
	 * 静态IP
	 */
	private String ipAddress;
	
	/**
	 * 子网掩码
	 */
	private String netMask;
	
	/**
	 * 默认网关
	 */
	private String gateWay;
	
	/**
	 * DNS
	 */
	private String dns;

	public SpecialSpeedChecker4JX(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(SpecialSpeedChecker4JX.class);

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
			// 入参解析
			userInfo = param.elementTextTrim("UserInfo");
			ipAddress = param.elementTextTrim("IpAddress");
			netMask = param.elementTextTrim("NetMask");
			gateWay = param.elementText("GateWay");
			dns = param.elementText("Dns");
//			vlanId = Integer.parseInt(param.elementText("VlanId"));
//			cityId = param.elementTextTrim("CityId");
//			downURL = param.elementTextTrim("DownURL");
//			priority = param.elementTextTrim("Priority");
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
		// 参数合法性检查
		if (false == baseCheck())
		{
			return false;
		}
		if (StringUtil.IsEmpty(ipAddress))
		{
			result = 1;
			resultDesc = "静态ip地址不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(netMask))
		{
			result = 1;
			resultDesc = "静态ip的掩码不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(gateWay))
		{
			result = 1;
			resultDesc = "静态ip的网关不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(dns))
		{
			result = 1;
			resultDesc = "静态ip的dns不能为空";
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
		root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues == null ? "" : avgSampledTotalValues);
		root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues == null ? "" : maxSampledTotalValues);
		root.addElement("TransportStartTime").addText(transportStartTime == null ? "" : transportStartTime);
		root.addElement("TransportEndTime").addText(transportEndTime == null ? "" : transportEndTime);
		root.addElement("IpAddress").addText(ipAddress == null ? "" : ipAddress);
		root.addElement("ReceiveByte").addText(receiveByte == null ? "" : receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime == null ? "" : tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime == null ? "" : tcpResponseTime);
		root.addElement("SampledValues").addText(sampledValues == null ? "" : sampledValues);
		root.addElement("SampledTotalValues").addText(sampledTotalValues == null ? "" : sampledTotalValues);
		root.addElement("GateWay").addText(gateWay == null ? "" : gateWay);
		root.addElement("NetMasK").addText(netMask == null ? "" : netMask);
		root.addElement("Dns").addText(dns == null ? "" : dns);
		if ("1".equals(isInsert)){
			insertTest();
		}
		return document.asXML();
	}
	
	
	public void insertTest(){
		try{
			logger.warn("SpecialSpeedService4JX==>insertTest(),开始入库");
			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			deviceInfoDAO.jxInsertSpecialSpeed(deviceId == null ? "" : deviceId,cmdId,result,resultDesc, devSn == null ? "" : devSn, username, netMask, avgSampledTotalValues,
					maxSampledTotalValues, transportStartTime, transportEndTime, ipAddress, receiveByte, tcpRequestTime, tcpResponseTime, downURL, testStartTime, wanType, loid, gateWay,clientType,dns);
		}catch (Exception e) {
			logger.error("insert table tab_http_diag_result_intf error:{}",e.getMessage());
		}
		logger.debug("DownLoadByHTTPSpeadChecker4JX==>insertTest(),结果入库完成");
	}


	public String getDownURL()
	{
		return downURL;
	}

	public String getUsername()
	{
		return username;
	}


	public void setDownURL(String downURL)
	{
		this.downURL = downURL;
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

	public String getReceiveByteContainHead()
	{
		return receiveByteContainHead;
	}

	public String getReceiveByte()
	{
		return receiveByte;
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

	public String getSampledValues() {
		return sampledValues;
	}

	public void setSampledValues(String sampledValues) {
		this.sampledValues = sampledValues;
	}

	public String getSampledTotalValues() {
		return sampledTotalValues;
	}

	public void setSampledTotalValues(String sampledTotalValues) {
		this.sampledTotalValues = sampledTotalValues;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getNetMask() {
		return netMask;
	}

	public void setNetMask(String netMask) {
		this.netMask = netMask;
	}

	public String getGateWay() {
		return gateWay;
	}

	public void setGateWay(String gateWay) {
		this.gateWay = gateWay;
	}

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getTcpRequestTime() {
		return tcpRequestTime;
	}

	public void setTcpRequestTime(String tcpRequestTime) {
		this.tcpRequestTime = tcpRequestTime;
	}

	public String getTcpResponseTime() {
		return tcpResponseTime;
	}

	public void setTcpResponseTime(String tcpResponseTime) {
		this.tcpResponseTime = tcpResponseTime;
	}
	
}
