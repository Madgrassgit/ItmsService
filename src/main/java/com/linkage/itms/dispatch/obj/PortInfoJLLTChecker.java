package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class PortInfoJLLTChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(PortInfoJLLTChecker.class);
	//0-在线 1-不在线
	private String deviceStatus;
    private String linkStatus;
    private String wanStatus;
    private String lanName;
    private String time;
    private String memRate;
    private String cpuRate;


	public PortInfoJLLTChecker(String inXml)
	{
		callXml=inXml;
	}

	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfo = param.elementTextTrim("UserInfo");
			userInfoType = Integer.parseInt(param.elementTextTrim("UserInfoType"));
			time = param.elementTextTrim("Time");
		} catch (Exception e) {
			logger.error("解析xml发生异常，e={}", e);
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		if(1 != userInfoType && 2 != userInfoType){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if(StringUtil.IsEmpty(userInfo) || StringUtil.IsEmpty(time)){
			result = 1;
			resultDesc = "参数为空";
			return false;
		}
		if(!baseCheck()){
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
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		root.addElement("Time").addText(StringUtil.getStringValue(time));
		root.addElement("DeviceStatus").addText(StringUtil.getStringValue(deviceStatus));
		root.addElement("WanStatus").addText(StringUtil.getStringValue(wanStatus));
		root.addElement("LanName").addText(StringUtil.getStringValue(lanName));
		root.addElement("LinkStatus").addText(StringUtil.getStringValue(linkStatus));
		root.addElement("MemRate").addText(StringUtil.getStringValue(memRate));
		root.addElement("CpuRate").addText(StringUtil.getStringValue(cpuRate));
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
			
	}


	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public String getLinkStatus() {
		return linkStatus;
	}

	public void setLinkStatus(String linkStatus) {
		this.linkStatus = linkStatus;
	}

	public String getWanStatus() {
		return wanStatus;
	}

	public void setWanStatus(String wanStatus) {
		this.wanStatus = wanStatus;
	}

	public String getLanName() {
		return lanName;
	}

	public void setLanName(String lanName) {
		this.lanName = lanName;
	}

	public String getMemRate() {
		return memRate;
	}

	public void setMemRate(String memRate) {
		this.memRate = memRate;
	}

	public String getCpuRate() {
		return cpuRate;
	}

	public void setCpuRate(String cpuRate) {
		this.cpuRate = cpuRate;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}

	