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


public class DownLoadByHTTPChecker extends BaseChecker{

	
	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTPChecker.class);
	
	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	
	/**
	 * 
	 */
	private String downURL = null;
	
	/**
	 * 报文设置的优先级
	 */
	private String priority = null;
	
	/**
	 * 请求收到时间
	 */
	private String requestsReceivedTime = null;
	
	/**
	 * 传输开始时间
	 */
	private String transportStartTime  = null;
	
	/**
	 * 传输结束时间
	 */
	private String transportEndTime = null;
	
	/**
	 * 接受字节数（包括控制头）
	 */
	private String receiveByteContainHead = null;
	
	/**
	 * 接受字节数
	 */
	private String receiveByte = null;
	
	/**
	 * TCP请求时间
	 */
	private String tcpRequestTime = null;
	
	/**
	 * TCP响应时间
	 */
	private String tcpResponseTime = null;
	private String userName=null;
	private String password=null;
	/**
	 * 宽带速率
	 */
	private String spead;

	/**
	 * 下行平均值
	 */
	private String avgSampledTotalValues = "";
	/**
	 * 下行最大值
	 */
	private String maxSampledTotalValues = "";
	
	/**
	 * 平均速率
	 */
	private String avgSampledValues = "";

	private String diagnosticsState = "";
	
	/**
	 * 最大速率
	 */
	private String maxSampledValues = "";

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DownLoadByHTTPChecker(String inXml) {
		this.callXml = inXml;
	}
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			devSn = param.elementTextTrim("DevSn");
			loid=param.elementTextTrim("Loid");
			oui = param.elementTextTrim("OUI");
			cityId = param.elementTextTrim("CityId");
			wanPassageWay = param.elementTextTrim("WanPassageWay");
			downURL = param.elementTextTrim("DownURL"); 
			priority = param.elementTextTrim("Priority");
			userName=param.elementTextTrim("UserName");
			password=param.elementTextTrim("Password");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck() || false == cityIdCheck()) {
			return false;
		}
		
		
		result = 0;
		resultDesc = "成功";
		
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
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("DevSn").addText(devSn==null?"":devSn);
		root.addElement("RequestsReceivedTime").addText(requestsReceivedTime==null?"":requestsReceivedTime);
		root.addElement("TransportStartTime").addText(transportStartTime==null?"":transportStartTime);
		root.addElement("TransportEndTime ").addText(transportEndTime ==null?"":transportEndTime );
		root.addElement("ReceiveByteContainHead").addText(receiveByteContainHead==null?"":receiveByteContainHead);
		root.addElement("ReceiveByte").addText(receiveByte==null?"":receiveByte);
		root.addElement("TCPRequestTime").addText(tcpRequestTime==null?"":tcpRequestTime);
		root.addElement("TCPResponseTime").addText(tcpResponseTime==null?"":tcpResponseTime);
		if("ah_dx".equals(Global.G_instArea))
		{
			root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues);
			root.addElement("MaxSampledTotalValues").addText(maxSampledTotalValues);
			root.addElement("ip").addText(ip);
		}
		if("jx_dx".equals(Global.G_instArea))
		{
			root.addElement("AvgSampledTotalValues").addText(avgSampledTotalValues);
			root.addElement("AvgSampledValues").addText(avgSampledValues);
			root.addElement("DiagnosticsState").addText(diagnosticsState);
		}
		if("js_dx".equals(Global.G_instArea))
		{
			root.addElement("AvgSampledValues").addText(avgSampledValues);
			root.addElement("MaxSampledValues").addText(maxSampledValues);
			
		}

		return document.asXML();
	}
	
	public String getDownURL() {
		return downURL;
	}
	
	
	public String getWanPassageWay() {
		return wanPassageWay;
	}
	
	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}
	public void setDownURL(String downURL) {
		this.downURL = downURL;
	}
	
	public String getPriority() {
		return priority;
	}
	
	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public String getRequestsReceivedTime() {
		return requestsReceivedTime;
	}
	
	public void setRequestsReceivedTime(String requestsReceivedTime) {
		this.requestsReceivedTime = requestsReceivedTime;
	}
	
	public String getTransportStartTime() {
		return transportStartTime;
	}
	
	public void setTransportStartTime(String transportStartTime) {
		this.transportStartTime = transportStartTime;
	}
	
	public String getTransportEndTime() {
		return transportEndTime;
	}
	
	public void setTransportEndTime(String transportEndTime) {
		this.transportEndTime = transportEndTime;
	}
	
	public String getReceiveByteContainHead() {
		return receiveByteContainHead;
	}
	
	public void setReceiveByteContainHead(String receiveByteContainHead) {
		this.receiveByteContainHead = receiveByteContainHead;
	}
	
	public String getReceiveByte() {
		return receiveByte;
	}
	
	public void setReceiveByte(String receiveByte) {
		this.receiveByte = receiveByte;
	}
	
	public String getTcpRequestTime() {
		return tcpRequestTime;
	}
	
	public String getSpead() {
		return spead;
	}
	public void setSpead(String spead) {
		this.spead = spead;
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
	public String getUserName()
	{
		return
				userName;
	}
	public void setUserName(String userName)
	{
		this.userName =
				userName;
	}
	public String getPassword()
	{
		return
				password;
	}
	public void setPassword(String password)
	{
		this.password =
				password;
	}
	public boolean checkSpead() 
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			loid=param.elementTextTrim("Loid");
			cityId = param.elementTextTrim("CityId");
			wanPassageWay = param.elementTextTrim("WanPassageWay");
			downURL = param.elementTextTrim("DownURL"); 
			priority = param.elementTextTrim("Priority");
			spead = param.elementText("Spead");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck()  || false == cityIdCheck()) {
			return false;
		}
		if(StringUtil.IsEmpty(loid))
		{
			result = 1;
			resultDesc = "loid不能为空";
			return false;
		}
		if(StringUtil.IsEmpty(spead))
		{
			result = 1;
			resultDesc = "Spead不能为空";
			return false;
		}
		
		
		result = 0;
		resultDesc = "成功";
		
		return true;
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
	
	public String getAvgSampledValues()
	{
		return avgSampledValues;
	}
	
	public void setAvgSampledValues(String avgSampledValues)
	{
		this.avgSampledValues = avgSampledValues;
	}
	
	public String getDiagnosticsState()
	{
		return diagnosticsState;
	}
	
	public void setDiagnosticsState(String diagnosticsState)
	{
		this.diagnosticsState = diagnosticsState;
	}
	
	public String getMaxSampledValues()
	{
		return maxSampledValues;
	}
	
	public void setMaxSampledValues(String maxSampledValues)
	{
		this.maxSampledValues = maxSampledValues;
	}

}
