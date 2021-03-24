package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class UpLoadByHTTPChecker extends BaseChecker{

	
	private static Logger logger = LoggerFactory.getLogger(UpLoadByHTTPChecker.class);
	
	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	
	/**
	 * 状态
	 */
	private String dState = "";
	
	/**
	 * 用于上传的URL
	 */
	private String upLoadURL = "";
	
	/**
	 * 上传文件大小
	 */
	private String testFileLength = "";
	
	/**
	 * 上线速率
	 */
	private String uSpeed = "";
	
	/**
	 * 传输开始时间
	 */
	private String bOMTime = "";
	
	/**
	 * 传输结束时间
	 */
	private String eOMTime = "";
	
	/**
	 * 传输开始时间
	 */
	private String tCPOpenRequestTime = "";
	
	/**
	 * 传输结束时间
	 */
	private String tCPOpenResponseTime = "";
	
	/**
	 * 设备IP
	 */
	private String iPAddress = "";
	
	/**
	 * 接受字节数
	 */
	private String totalBytesSent = "";
	
	/**
	 * 宽带账号
	 */
	private String username = "";
	
	/**
	 * 设备id
	 */
	private String deviceId = "";
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public UpLoadByHTTPChecker(String inXml) {
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
			upLoadURL = param.elementTextTrim("UpLoadURL"); 
			testFileLength=param.elementTextTrim("TestFileLength");
			
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
		root.addElement("USpeed").addText(uSpeed);
		root.addElement("BOMTime").addText(bOMTime);
		root.addElement("EOMTime").addText(eOMTime);
		root.addElement("TCPOpenRequestTime").addText(tCPOpenRequestTime);
		root.addElement("TCPOpenResponseTime").addText(tCPOpenResponseTime);
		root.addElement("TotalBytesSent").addText(totalBytesSent);
		return document.asXML();
	}
	public String getWanPassageWay() {
		return wanPassageWay;
	}
	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}
	public String getdState() {
		return dState;
	}
	public void setdState(String dState) {
		this.dState = dState;
	}
	public String getUpLoadURL() {
		return upLoadURL;
	}
	public void setUpLoadURL(String upLoadURL) {
		this.upLoadURL = upLoadURL;
	}
	public String getTestFileLength() {
		return testFileLength;
	}
	public void setTestFileLength(String testFileLength) {
		this.testFileLength = testFileLength;
	}
	public String getuSpeed() {
		return uSpeed;
	}
	public void setuSpeed(String uSpeed) {
		this.uSpeed = uSpeed;
	}
	public String getbOMTime() {
		return bOMTime;
	}
	public void setbOMTime(String bOMTime) {
		this.bOMTime = bOMTime;
	}
	public String geteOMTime() {
		return eOMTime;
	}
	public void seteOMTime(String eOMTime) {
		this.eOMTime = eOMTime;
	}
	public String gettCPOpenRequestTime() {
		return tCPOpenRequestTime;
	}
	public void settCPOpenRequestTime(String tCPOpenRequestTime) {
		this.tCPOpenRequestTime = tCPOpenRequestTime;
	}
	public String gettCPOpenResponseTime() {
		return tCPOpenResponseTime;
	}
	public void settCPOpenResponseTime(String tCPOpenResponseTime) {
		this.tCPOpenResponseTime = tCPOpenResponseTime;
	}
	public String getiPAddress() {
		return iPAddress;
	}
	public void setiPAddress(String iPAddress) {
		this.iPAddress = iPAddress;
	}
	public String getTotalBytesSent() {
		return totalBytesSent;
	}
	public void setTotalBytesSent(String totalBytesSent) {
		this.totalBytesSent = totalBytesSent;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	

	
}
