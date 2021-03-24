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
 * 设备重启接口的XML元素对象
 * @author zhangsm(工号) Tel:??
 * @version 1.0
 * @since 2012-2-25 下午02:59:10
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 *
 */
public class VoipRegistTestChecker extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(VoipRegistTestChecker.class);
	private String registerServer="";
	private String diagnosticResult="";
	private String diagnosticReason="";
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public VoipRegistTestChecker(String inXml) {
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			devSn = param.elementTextTrim("DevSn");
			oui = param.elementTextTrim("OUI");
			cityId = param.elementTextTrim("CityId");
			registerServer = param.elementTextTrim("RegisterServer");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		root.addElement("DevSn").addText("" + devSn);
		root.addElement("RegisterServer").addText("" + registerServer);
		root.addElement("DiagnosticResult").addText("" + diagnosticResult);
		root.addElement("DiagnosticReason").addText("" + diagnosticReason);
		return document.asXML();
	}
	public String getRegisterServer() {
		return registerServer;
	}
	public void setRegisterServer(String registerServer) {
		this.registerServer = registerServer;
	}
	public String getDiagnosticResult() {
		return diagnosticResult;
	}
	public void setDiagnosticResult(String diagnosticResult) {
		this.diagnosticResult = diagnosticResult;
	}
	public String getDiagnosticReason() {
		return diagnosticReason;
	}
	public void setDiagnosticReason(String diagnosticReason) {
		this.diagnosticReason = diagnosticReason;
	}
	
}
