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
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年2月13日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class VoiceUserSetResultChecker extends BaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(VoiceUserSetResultChecker.class);
	private String inParam = "";
	private String failureReason = "";
	private String succStatus = "";
	private String digitCornet = "";
	private String digitMapValue = "";
	
	public VoiceUserSetResultChecker(String inParam){
		this.inParam = inParam;
	}

	@Override
	public boolean check()
	{
		logger.debug("VoiceUserSetResultChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			/**
			1：用户宽带帐号 	2：LOID	3：IPTV宽带帐号   4：VOIP业务电话号码	5：VOIP认证帐号
			*/
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserName"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("VoiceUserSetResultChecker==>getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(cmdId);
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("FailureReason").addText(failureReason);
		root.addElement("SuccStatus").addText(succStatus);
		root.addElement("DigitCornet").addText(digitCornet);
		root.addElement("digitMapValue").addText(digitMapValue);
		return document.asXML();
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getSuccStatus() {
		return succStatus;
	}

	public void setSuccStatus(String succStatus) {
		this.succStatus = succStatus;
	}

	public String getDigitCornet() {
		return digitCornet;
	}

	public void setDigitCornet(String digitCornet) {
		this.digitCornet = digitCornet;
	}

	public String getDigitMapValue() {
		return digitMapValue;
	}

	public void setDigitMapValue(String digitMapValue) {
		this.digitMapValue = digitMapValue;
	}
}
