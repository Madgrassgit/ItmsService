package com.linkage.itms.hlj.dispatch.obj;


import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class PingConnectivityChecker extends HljBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(PingConnectivityChecker.class);
	// IP正则表达式
	private static String ipPattern = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
		
	private String type = "";
	private String time = "";
	private String packageByte = "";
	private String packageNum = "";
	private String timeOut = "";
	
	private String succesNum = "";
	private String failNum = "";
	private String avgResponseTime = "";
	private String minResponseTime = "";
	private String maxResponseTime = "";
	private String packetLossRate = "";
	private String ip = "";
	private String callXml = null;
	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public PingConnectivityChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element publicElt = root.element("public");
			type = publicElt.elementTextTrim("type");
			time = publicElt.elementTextTrim("time");

			Element dataElt = root.element("data");
			ip = dataElt.elementTextTrim("ip");
			packageByte = dataElt.elementTextTrim("packageByte");
			packageNum = dataElt.elementTextTrim("packageNum");
			timeOut = dataElt.elementTextTrim("timeOut");
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == ipCheck() || false == pingConCheck()) {
			result = 0;
			return false;
		}
		result = 1;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		Element publicElt = root.addElement("public");
		publicElt.addElement("type").addText("RESPONSE");
		publicElt.addElement("time").addText(time);
		publicElt.addElement("success").addText(String.valueOf(result));
		publicElt.addElement("desc").addText(resultDesc);
		
		Element dataElt = root.addElement("data");
		dataElt.addElement("ip").addText(ip);
		// 成功数
		dataElt.addElement("succesNum").addText(succesNum);
		// 失败数
		dataElt.addElement("failNum").addText(failNum);
		// 平均响应时间
		dataElt.addElement("avgResponseTime").addText(avgResponseTime);
		// 最小响应时间
		dataElt.addElement("minResponseTime").addText(minResponseTime);
		// 最大响应时间
		dataElt.addElement("maxResponseTime").addText(maxResponseTime);
		// 丢包率
		dataElt.addElement("packetLossRate").addText(packetLossRate);
		return document.asXML();
	}

	/**
	 * IP 地址验证
	 * @return
	 */
	boolean ipCheck(){
		if(StringUtil.IsEmpty(ip)){
			result = 1004;
			resultDesc = "IP地址不合法";
			return false;
		}
		
		Pattern pattern = Pattern.compile(ipPattern); 
		Matcher matcher = pattern.matcher(ip);
		if (false == matcher.matches()) {
			result = 1004;
			resultDesc = "IP地址不合法";
			return false ;
		}
		return true;
	}

	/**
	 * 数据验证
	 * @return
	 */
	boolean pingConCheck(){	
		if(StringUtil.IsEmpty(type) || StringUtil.IsEmpty(time)
				|| StringUtil.IsEmpty(packageByte)
				|| StringUtil.IsEmpty(packageNum)
				|| StringUtil.IsEmpty(timeOut)){
			resultDesc = "数据不能为空";
			return false;
		}
		return true;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getPackageByte() {
		return packageByte;
	}

	public void setPackageByte(String packageByte) {
		this.packageByte = packageByte;
	}

	public String getPackageNum() {
		return packageNum;
	}

	public void setPackageNum(String packageNum) {
		this.packageNum = packageNum;
	}

	public String getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}

	public String getSuccesNum() {
		return succesNum;
	}

	public void setSuccesNum(String succesNum) {
		this.succesNum = succesNum;
	}

	public String getFailNum() {
		return failNum;
	}

	public void setFailNum(String failNum) {
		this.failNum = failNum;
	}

	public String getAvgResponseTime() {
		return avgResponseTime;
	}

	public void setAvgResponseTime(String avgResponseTime) {
		this.avgResponseTime = avgResponseTime;
	}

	public String getMinResponseTime() {
		return minResponseTime;
	}

	public void setMinResponseTime(String minResponseTime) {
		this.minResponseTime = minResponseTime;
	}

	public String getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(String maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	public String getPacketLossRate() {
		return packetLossRate;
	}

	public void setPacketLossRate(String packetLossRate) {
		this.packetLossRate = packetLossRate;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
