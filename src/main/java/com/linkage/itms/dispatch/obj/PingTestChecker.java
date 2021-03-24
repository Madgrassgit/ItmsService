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


public class PingTestChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(PingTestChecker.class);

	/**
	 * 语音类型
	 */
	protected String voiceType = null;

	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	
	/**
	 * 测试IP或域名
	 */
	private String iPOrDomainName = null;
	
	/**
	 * 成功数
	 */
	private String succesNum = null;

	/**
	 * 失败数
	 */
	private String failNum = null;

	/**
	 * 平均响应时间
	 */
	private String avgResponseTime = null;

	/**
	 * 最小响应时间
	 */
	private String minResponseTime = null;

	/**
	 * 最大响应时间
	 */
	private String maxResponseTime = null;

	/**
	 * 丢包率
	 */
	private String packetLossRate = null;
	public PingTestChecker() {
	}

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PingTestChecker(String inXml) {
		callXml = inXml;
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

			// chenxj6 xj : 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			voiceType = param.elementTextTrim("VoiceType");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// zzd
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType ) {
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		if(1 != userInfoType&&2 != userInfoType&&3 != userInfoType&&4 != userInfoType&&5 != userInfoType){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if (false == baseCheck()) {
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
		root.addElement("IPOrDomainName").addText(iPOrDomainName==null?"":iPOrDomainName);

		return document.asXML();
	}

	public String getWanPassageWay() {
		return wanPassageWay;
	}

	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}

	public String getiPOrDomainName() {
		return iPOrDomainName;
	}

	public void setiPOrDomainName(String iPOrDomainName) {
		this.iPOrDomainName = iPOrDomainName;
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
	public String getVoiceType() {
		return voiceType;
	}
	public void setVoiceType(String voiceType) {
		this.voiceType = voiceType;
	}

}
