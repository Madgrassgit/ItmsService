
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
public class QueryVoiceProfileChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory
			.getLogger(QueryVoiceProfileChecker.class);
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
//	private ArrayList<ParameValueOBJ> parameterValues = null;

	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public QueryVoiceProfileChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("QueryVoiceProfileChecker -> check(),inXML ({})", callXml);
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
			line = param.elementText("Line");
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
				|| false == lineCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	private boolean lineCheck()
	{
		if (StringUtil.IsEmpty(line))
		{
			result = 1000;
			resultDesc = "line参数为空";
			return false;
		}
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
		root.addElement("RstMsg").addText("" + resultDesc);
		
		if(!StringUtil.IsEmpty(echoCancellationEnable) || !StringUtil.IsEmpty(transmitGain) 
				|| !StringUtil.IsEmpty(receiveGain) || !StringUtil.IsEmpty(controlType) ||
				!StringUtil.IsEmpty(faxT38))
		{
			root.addElement("EchoCancellationEnable").addText(echoCancellationEnable);
			root.addElement("TransmitGain").addText(transmitGain);
			root.addElement("ReceiveGain").addText(receiveGain);
			root.addElement("ControlType").addText(controlType);
			root.addElement("FaxT38").addText(faxT38);
		}
		
		
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

//	public ArrayList<ParameValueOBJ> getParameterValues()
//	{
//		return parameterValues;
//	}
//
//	public void setParameterValues(ArrayList<ParameValueOBJ> parameterValues)
//	{
//		this.parameterValues = parameterValues;
//	}
}
