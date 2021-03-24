
package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年6月12日
 * @category com.linkage.itms.nmg.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryPerformanceChecker extends NmgBaseChecker
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(QueryPerformanceChecker.class);
	private String TXPower = "";
	private String RXPower = "";
	private String bytesSent = "";
	private String bytesReceived = "";
	private String deviceTemperature = "";
	private String supplyVottage = "";
	private String biasCurrent = "";
	private int status = 1;
	// 正则，字符加数字
	private Pattern reg = Pattern.compile("\\w{1,}+");

	public QueryPerformanceChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
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
			userInfo = param.elementTextTrim("UserInfo");
			userInfoType = Integer.parseInt(param.elementTextTrim("UserInfoType"));
			devSn = param.elementTextTrim("DeviceInfo");
		}
		catch (Exception e)
		{
			logger.error("解析xml发生异常，e={}", e);
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck())
		{
			return false;
		}
		if (!StringUtil.IsEmpty(devSn)
				&& (false == reg.matcher(devSn).matches() || devSn.length() < 6))
		{
			result = 1005;
			resultDesc = "设备序列号不合法";
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
		document.setXMLEncoding("UTF-8");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		root.addElement("DeviceTemperature").addText(
				StringUtil.getStringValue(deviceTemperature));
		root.addElement("SupplyVottage")
				.addText(StringUtil.getStringValue(supplyVottage));
		root.addElement("BiasCurrent").addText(StringUtil.getStringValue(biasCurrent));
		root.addElement("Status").addText(StringUtil.getStringValue(status));
		root.addElement("TXPower").addText(StringUtil.getStringValue(TXPower));
		root.addElement("RXPower").addText(StringUtil.getStringValue(RXPower));
		if ("jl_dx".equals(Global.G_instArea)){
			root.addElement("BytesSent").addText(StringUtil.getStringValue(bytesSent));
			root.addElement("BytesReceived").addText(StringUtil.getStringValue(bytesReceived));
		}
		return document.asXML();
	}

	public String getTXPower()
	{
		return TXPower;
	}

	public void setTXPower(String tXPower)
	{
		TXPower = tXPower;
	}

	public String getRXPower()
	{
		return RXPower;
	}

	public void setRXPower(String rXPower)
	{
		RXPower = rXPower;
	}

	public String getDeviceTemperature()
	{
		return deviceTemperature;
	}

	public String getSupplyVottage()
	{
		return supplyVottage;
	}

	public String getBiasCurrent()
	{
		return biasCurrent;
	}

	public void setDeviceTemperature(String deviceTemperature)
	{
		this.deviceTemperature = deviceTemperature;
	}

	public void setSupplyVottage(String supplyVottage)
	{
		this.supplyVottage = supplyVottage;
	}

	public void setBiasCurrent(String biasCurrent)
	{
		this.biasCurrent = biasCurrent;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getBytesSent() {
		return bytesSent;
	}

	public void setBytesSent(String bytesSent) {
		this.bytesSent = bytesSent;
	}

	public String getBytesReceived() {
		return bytesReceived;
	}

	public void setBytesReceived(String bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
}
