package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class PPPoEDialChecker extends BaseChecker{
	
	private static Logger logger = LoggerFactory.getLogger(PPPoEDialChecker.class);
	
	/**
	 * PPPoE用户名
	 */
	private String pPPoEUser = null;
	
	/**
	 * PPPoE密码
	 */
	private String pPPoEPassword = null;
	
	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	
	/**
	 * 认证模式
	 */
	private String authenticationMode = null;
	
	/**
	 * 重复次数
	 */
	private String repeatTimes = null;
	
	/**
	 * 诊断状态
	 */
	private String diagnosticStatus = null;
	
	/**
	 * 诊断结果
	 */
	private String diagnosticResult = null;
	
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PPPoEDialChecker(String inXml) {
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
			oui = param.elementTextTrim("OUI");
			cityId = param.elementTextTrim("CityId");
			pPPoEUser = param.elementTextTrim("PPPoEUser");
			pPPoEPassword = param.elementTextTrim("PPPoEPassword");
			wanPassageWay = param.elementTextTrim("WanPassageWay");
			authenticationMode = param.elementTextTrim("AuthenticationMode");
			repeatTimes = param.elementTextTrim("RepeatTimes");
			ip = param.elementTextTrim("IP");
			gateWay = param.elementTextTrim("GateWay");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck() || false == cityIdCheck()
				|| false == ouiCheck() ) {
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
		root.addElement("DiagnosticStatus").addText(diagnosticStatus==null?"":diagnosticStatus);
		root.addElement("DiagnosticResult").addText(diagnosticResult==null?"":diagnosticResult);
		
		return document.asXML();
	}
	
	
	
	
	public String getpPPoEUser() {
		return pPPoEUser;
	}
	
	public void setpPPoEUser(String pPPoEUser) {
		this.pPPoEUser = pPPoEUser;
	}
	
	public String getpPPoEPassword() {
		return pPPoEPassword;
	}
	
	public void setpPPoEPassword(String pPPoEPassword) {
		this.pPPoEPassword = pPPoEPassword;
	}
	
	
	public String getWanPassageWay() {
		return wanPassageWay;
	}
	
	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}
	public String getAuthenticationMode() {
		return authenticationMode;
	}
	
	public void setAuthenticationMode(String authenticationMode) {
		this.authenticationMode = authenticationMode;
	}
	
	public String getRepeatTimes() {
		return repeatTimes;
	}
	
	public void setRepeatTimes(String repeatTimes) {
		this.repeatTimes = repeatTimes;
	}
	
	public String getDiagnosticStatus() {
		return diagnosticStatus;
	}
	
	public void setDiagnosticStatus(String diagnosticStatus) {
		this.diagnosticStatus = diagnosticStatus;
	}
	
	public String getDiagnosticResult() {
		return diagnosticResult;
	}
	
	public void setDiagnosticResult(String diagnosticResult) {
		this.diagnosticResult = diagnosticResult;
	}
	
}
