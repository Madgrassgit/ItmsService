package com.linkage.itms.hlj.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;


public class DownLoadByHTTPChecker4JL extends BaseChecker{

	
	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTPChecker4JL.class);
	
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
	 * 请求收到时间
	 */
	private String requestsReceivedTime  = "";
	
	/**
	 * 传输开始时间
	 */
	private String transportStartTime  = "";
	
	/**
	 * 传输结束时间
	 */
	private String transportEndTime  = "";
	
	/**
	 * 接受字节数（包括控制头）
	 */
	private String receiveByteContainHead  = "";
	
	/**
	 * 接受字节数
	 */
	private String receiveByte = "";
	
	/**
	 * TCP请求时间
	 */
	private String tcpRequestTime = "";
	
	/**
	 * TCP响应时间
	 */
	private String tcpResponseTime = "";
	
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
	 * 接口调用时间
	 */
	private String time;

	/**
	 * 速率
	 */
	private String speed;

	/**
	 * REQUEST
	 */
	private String type; 

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DownLoadByHTTPChecker4JL(String inXml) {
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
			Element p = root.element("public");
			type = p.elementTextTrim("type");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			//speed = data.elementTextTrim("speed");
			wanPassageWay = data.elementTextTrim("wanPassageWay");
			downURL = data.elementTextTrim("downURL");
			priority = data.elementTextTrim("priority");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (!checkParam()) {
			return false;
		}
		
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	public boolean checkParam(){
		logger.debug("checkParam()");
		
		if(StringUtil.IsEmpty(type)||!"REQUEST".equals(type)){
			result = 0;
			resultDesc = "接口类型非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(time)){
			result = 0;
			resultDesc = "接口调用时间非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(wanPassageWay)||(!"46".equals(wanPassageWay)&&!"41".equals(wanPassageWay))){
			result = 0;
			resultDesc = "Wan通道非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(downURL)){
			result = 0;
			resultDesc = "URL非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(priority)){
			result = 0;
			resultDesc = "报文设置的优先级非法";
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
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		p.addElement("time").addText(new DateTimeUtil().getLongDate());
		if (0==result) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(resultDesc);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("requestsReceivedTime").addText(requestsReceivedTime);
		data.addElement("transportStartTime").addText(transportStartTime);
		data.addElement("transportEndTime").addText(transportEndTime);
		data.addElement("receiveByteContainHead").addText(receiveByteContainHead);
		data.addElement("receiveByte").addText(receiveByte);
		data.addElement("tcpRequestTime").addText(tcpRequestTime);
		data.addElement("tcpResponseTime").addText(tcpResponseTime);
		
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
	
	
	public void setTcpRequestTime(String tcpRequestTime) {
		this.tcpRequestTime = tcpRequestTime;
	}
	
	public String getTcpResponseTime() {
		return tcpResponseTime;
	}
	
	public void setTcpResponseTime(String tcpResponseTime) {
		this.tcpResponseTime = tcpResponseTime;
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
	public String getTime() {
		return time;
	}
	public String getSpeed() {
		return speed;
	}
	public String getType() {
		return type;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public void setType(String type) {
		this.type = type;
	}

}
