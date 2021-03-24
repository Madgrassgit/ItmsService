
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月22日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SetVoiceProfileChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(SetVoiceProfileChecker.class);
	/**
	 * 回声抑制
	 */
	private String echoCancellationEnable = "";
	/**
	 * 呼出增益
	 */
	private String transmitGain = "";
	/**
	 * 呼入增益
	 */
	private String receiveGain = "";
	/**
	 * 传真控制方式
	 */
	private String controlType = "";
	/**
	 * FaxT38传真模式
	 */
	private String faxT38 = "";
	/**
	 * 采集语音节点的线路
	 */
	private String line = "";

	public SetVoiceProfileChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("QueryVOIPWanInfoChecker -> check(),inXML ({})", callXml);
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
			echoCancellationEnable = param.elementTextTrim("EchoCancellationEnable");
			transmitGain = param.elementTextTrim("TransmitGain");
			receiveGain = param.elementTextTrim("ReceiveGain");
			controlType = param.elementTextTrim("ControlType");
			faxT38 = param.elementTextTrim("FaxT38");
			line = param.elementTextTrim("Line");
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
				|| false == userInfoCheck() || false == cityIdCheck()
				 || false == checkControlType() || false == checkLine()
				 )
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

//	private boolean checkEchoCancellationEnable()
//	{
//		if (StringUtil.IsEmpty(echoCancellationEnable))
//		{
//			result = 1000;
//			resultDesc = "回声抑制信息为空";
//			return false;
//		}
//		return true;
//	}

//	private boolean checkTransmitGain()
//	{
//		if (StringUtil.IsEmpty(transmitGain))
//		{
//			result = 1000;
//			resultDesc = "呼出增益信息为空";
//			return false;
//		}
//		return true;
//	}

//	private boolean checkReceiveGain()
//	{
//		if (StringUtil.IsEmpty(receiveGain))
//		{
//			result = 1000;
//			resultDesc = "呼入增益信息为空";
//			return false;
//		}
//		return true;
//	}

	private boolean checkControlType()
	{
		if (!StringUtil.IsEmpty(controlType) && !("all".equals(controlType)|| "other".equals(controlType)))
		{
			result = 1000;
			resultDesc = "ControlType参数仅支持'all' 或者'other' ";
			return false;
		}
		return true;
	}
	private boolean checkLine()
	{
		if ((!StringUtil.IsEmpty(echoCancellationEnable)|| !StringUtil.IsEmpty(transmitGain) ||
				!StringUtil.IsEmpty(receiveGain)) && StringUtil.IsEmpty(line))
		{
			result = 1000;
			resultDesc = "入参传了回声抑制、呼出增益、呼入增益时，必须指定Line的参数值";
			return false;
		}
		return true;
	}
	

//	private boolean checkFaxT38()
//	{
//		if (StringUtil.IsEmpty(faxT38))
//		{
//			result = 1000;
//			resultDesc = "FaxT38传真模式信息为空";
//			return false;
//		}
//		return true;
//	}

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
		root.addElement("RstMsg").addText("" + resultDesc);
		return document.asXML();
	}

	public String getEchoCancellationEnable()
	{
		return echoCancellationEnable;
	}

	public void setEchoCancellationEnable(String echoCancellationEnable)
	{
		this.echoCancellationEnable = echoCancellationEnable;
	}

	public String getTransmitGain()
	{
		return transmitGain;
	}

	public void setTransmitGain(String transmitGain)
	{
		this.transmitGain = transmitGain;
	}

	public String getReceiveGain()
	{
		return receiveGain;
	}

	public void setReceiveGain(String receiveGain)
	{
		this.receiveGain = receiveGain;
	}

	public String getControlType()
	{
		return controlType;
	}

	public void setControlType(String controlType)
	{
		this.controlType = controlType;
	}

	public String getFaxT38()
	{
		return faxT38;
	}

	public void setFaxT38(String faxT38)
	{
		this.faxT38 = faxT38;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
	}
}
