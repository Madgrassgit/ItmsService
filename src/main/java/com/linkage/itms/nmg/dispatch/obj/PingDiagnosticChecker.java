
package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class PingDiagnosticChecker extends NmgBaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(PingDiagnosticChecker.class);
	/**
	 * Wan通道
	 */
	private String wanPassageWay = "";
	/**
	 * 包大小（Byte）
	 */
	private String packageByte = "";
	/**
	 * 测试IP或域名
	 */
	private String iPOrDomainName = "";
	/**
	 * 包数目
	 */
	private String packageNum = "";
	/**
	 * 超时时间（ms）
	 */
	private String timeOut = "";
	/**
	 * 成功数
	 */
	private String succesNum = "";
	/**
	 * 失败数
	 */
	private String failNum = "";
	/**
	 * 平均响应时间
	 */
	private String avgResponseTime = "";
	/**
	 * 最小响应时间
	 */
	private String minResponseTime = "";
	/**
	 * 最大响应时间
	 */
	private String maxResponseTime = "";
	/**
	 * 丢包率
	 */
	private String packetLossRate = "";

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PingDiagnosticChecker(String inXml)
	{
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
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			cityId = param.elementTextTrim("CityId");
			wanPassageWay = param.elementTextTrim("WanPassageWay");
			packageByte = param.elementTextTrim("PackageByte");
			iPOrDomainName = param.elementTextTrim("IPOrDomainName");
			packageNum = param.elementTextTrim("PackageNum");
			timeOut = param.elementTextTrim("TimeOut");
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		
		if(!"1".equals(wanPassageWay) && !"2".equals(wanPassageWay)){
			result = 1008;
			resultDesc = "Wan通道类型不对";
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("DevSn").addText(devSn == null ? "" : devSn);
		root.addElement("SuccesNum").addText(succesNum == null ? "" : succesNum);
		root.addElement("FailNum").addText(failNum == null ? "" : failNum);
		root.addElement("AvgResponseTime").addText(
				avgResponseTime == null ? "" : avgResponseTime);
		root.addElement("MinResponseTime").addText(
				minResponseTime == null ? "" : minResponseTime);
		root.addElement("MaxResponseTime").addText(
				maxResponseTime == null ? "" : maxResponseTime);
		root.addElement("PacketLossRate").addText(
				packetLossRate == null ? "" : packetLossRate);
		root.addElement("IPOrDomainName").addText(
				iPOrDomainName == null ? "" : iPOrDomainName);
		return document.asXML();
	}

	public String getWanPassageWay()
	{
		return wanPassageWay;
	}

	public void setWanPassageWay(String wanPassageWay)
	{
		this.wanPassageWay = wanPassageWay;
	}

	public String getPackageByte()
	{
		return packageByte;
	}

	public void setPackageByte(String packageByte)
	{
		this.packageByte = packageByte;
	}

	public String getiPOrDomainName()
	{
		return iPOrDomainName;
	}

	public void setiPOrDomainName(String iPOrDomainName)
	{
		this.iPOrDomainName = iPOrDomainName;
	}

	public String getPackageNum()
	{
		return packageNum;
	}

	public void setPackageNum(String packageNum)
	{
		this.packageNum = packageNum;
	}

	public String getTimeOut()
	{
		return timeOut;
	}

	public void setTimeOut(String timeOut)
	{
		this.timeOut = timeOut;
	}

	public String getSuccesNum()
	{
		return succesNum;
	}

	public void setSuccesNum(String succesNum)
	{
		this.succesNum = succesNum;
	}

	public String getFailNum()
	{
		return failNum;
	}

	public void setFailNum(String failNum)
	{
		this.failNum = failNum;
	}

	public String getAvgResponseTime()
	{
		return avgResponseTime;
	}

	public void setAvgResponseTime(String avgResponseTime)
	{
		this.avgResponseTime = avgResponseTime;
	}

	public String getMinResponseTime()
	{
		return minResponseTime;
	}

	public void setMinResponseTime(String minResponseTime)
	{
		this.minResponseTime = minResponseTime;
	}

	public String getMaxResponseTime()
	{
		return maxResponseTime;
	}

	public void setMaxResponseTime(String maxResponseTime)
	{
		this.maxResponseTime = maxResponseTime;
	}

	public String getPacketLossRate()
	{
		return packetLossRate;
	}

	public void setPacketLossRate(String packetLossRate)
	{
		this.packetLossRate = packetLossRate;
	}
}
