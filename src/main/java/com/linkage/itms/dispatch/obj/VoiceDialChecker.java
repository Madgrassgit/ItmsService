
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class VoiceDialChecker extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(VoiceDialChecker.class);
	private String testType;
	private String calledNumber;
	private String callHoldTimer;
	private String calledWaitTimer;
	private String dialDTMFConfirmEnable;
	private String dialDTMFConfirmNumber;
	private String dialDTMFConfirmResult;
	private String status;
	private String conclusion;
	private String callerFailReason;
	private String calledFailReason;
	private String failedResponseCode;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public VoiceDialChecker(String inXml)
	{
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
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			devSn = param.elementTextTrim("DevSn");
			oui = param.elementTextTrim("OUI");
			cityId = param.elementTextTrim("CityId");
			testType = param.elementTextTrim("TestType");
			calledNumber = param.elementTextTrim("CalledNumber");
			dialDTMFConfirmEnable = param.elementTextTrim("DialDTMFConfirmEnable");
			dialDTMFConfirmNumber = param.elementTextTrim("DialDTMFConfirmNumber");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck() || false == cityIdCheck()
				|| false == ouiCheck())
		{
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
		root.addElement("TestType").addText(testType == null ? "" : testType);
		root.addElement("CalledNumber").addText(calledNumber == null ? "" : calledNumber);
		root.addElement("DialDTMFConfirmEnable").addText(dialDTMFConfirmEnable == null ? "" : dialDTMFConfirmEnable);
		root.addElement("DialDTMFConfirmResult").addText(dialDTMFConfirmResult == null ? "" : dialDTMFConfirmResult);
		root.addElement("Status").addText(status == null ? "" : status);
		root.addElement("Conclusion").addText(conclusion == null ? "" : conclusion);
		root.addElement("CallerFailReason").addText(callerFailReason == null ? "" : callerFailReason);
		root.addElement("CalledFailReason").addText(calledFailReason == null ? "" : calledFailReason);
		root.addElement("FailedResponseCode").addText(failedResponseCode == null ? "" : failedResponseCode);
		return document.asXML();
	}

	public String getTestType()
	{
		return testType;
	}

	public void setTestType(String testType)
	{
		this.testType = testType;
	}

	public String getCalledNumber()
	{
		return calledNumber;
	}

	public void setCalledNumber(String calledNumber)
	{
		this.calledNumber = calledNumber;
	}

	public String getCallHoldTimer()
	{
		return callHoldTimer;
	}

	public void setCallHoldTimer(String callHoldTimer)
	{
		this.callHoldTimer = callHoldTimer;
	}

	public String getCalledWaitTimer()
	{
		return calledWaitTimer;
	}

	public void setCalledWaitTimer(String calledWaitTimer)
	{
		this.calledWaitTimer = calledWaitTimer;
	}

	public String getDialDTMFConfirmEnable()
	{
		return dialDTMFConfirmEnable;
	}

	public void setDialDTMFConfirmEnable(String dialDTMFConfirmEnable)
	{
		this.dialDTMFConfirmEnable = dialDTMFConfirmEnable;
	}

	public String getDialDTMFConfirmNumber()
	{
		return dialDTMFConfirmNumber;
	}

	public void setDialDTMFConfirmNumber(String dialDTMFConfirmNumber)
	{
		this.dialDTMFConfirmNumber = dialDTMFConfirmNumber;
	}

	public String getDialDTMFConfirmResult()
	{
		return dialDTMFConfirmResult;
	}

	public void setDialDTMFConfirmResult(String dialDTMFConfirmResult)
	{
		this.dialDTMFConfirmResult = dialDTMFConfirmResult;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getConclusion()
	{
		return conclusion;
	}

	public void setConclusion(String conclusion)
	{
		this.conclusion = conclusion;
	}

	public String getCallerFailReason()
	{
		return callerFailReason;
	}

	public void setCallerFailReason(String callerFailReason)
	{
		this.callerFailReason = callerFailReason;
	}

	public String getCalledFailReason()
	{
		return calledFailReason;
	}

	public void setCalledFailReason(String calledFailReason)
	{
		this.calledFailReason = calledFailReason;
	}

	public String getFailedResponseCode()
	{
		return failedResponseCode;
	}

	public void setFailedResponseCode(String failedResponseCode)
	{
		this.failedResponseCode = failedResponseCode;
	}
}
