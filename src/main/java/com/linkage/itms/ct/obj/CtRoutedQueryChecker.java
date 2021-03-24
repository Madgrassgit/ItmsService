package com.linkage.itms.ct.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtRoutedQueryChecker extends CtBaseChecker {

	private static Logger logger = LoggerFactory
			.getLogger(CtRoutedQueryChecker.class);

	// 业务下发结果代码
	private int servResult;
	// 业务下发结果描述
	private String servResultDesc;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public CtRoutedQueryChecker(String inXml) {
		logger.debug("CtRoutedQueryChecker()");
		callXml = inXml;
	}

	@Override
	public boolean check() {
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

			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == usernameCheck()
				|| false == devSnCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

	
	@Override
	boolean cmdTypeCheck() {
		logger.debug("cmdTypeCheck()");
		return "CX_03".equals(cmdType);
	}

	
	@Override
	public String getReturnXml() {
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

		if (0 == result) {
			Element param = root.addElement("Param");
			param.addElement("Result").addText("" + servResult);
			param.addElement("ResultDesc").addText("" + servResultDesc);
		}

		return document.asXML();
	}

	
	/** getter, setter methods */

	public int getServResult() {
		return servResult;
	}

	public void setServResult(int servResult) {
		this.servResult = servResult;
	}

	public String getServResultDesc() {
		return servResultDesc;
	}

	public void setServResultDesc(String servResultDesc) {
		this.servResultDesc = servResultDesc;
	}

}
