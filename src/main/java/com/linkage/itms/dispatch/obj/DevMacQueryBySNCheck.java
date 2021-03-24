
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
 * 通过输入用户账号（LOID）将绑定的设备进行版本信息查询
 * 
 * @author zhaixx (Ailk No.)
 * @version 1.0
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DevMacQueryBySNCheck extends BaseChecker {

	private static final Logger logger = LoggerFactory.getLogger(DevMacQueryBySNCheck.class);
	/**
	 * 厂商
	 */
	private String devVendor;
	/**
	 * 型号
	 */
	private String devModel;
	/**
	 * 设备mac
	 */
	private String devMac;

	public DevMacQueryBySNCheck(String inxml) {
		callXml = inxml;
	}

	@Override
	public boolean check() {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (StringUtil.IsEmpty(cmdId)) {
			result = 3;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if (3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType) {
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		if (false == "CX_01".equals(cmdType)) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		// 用户账号合法性检查
		if (1 != userInfoType) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 厂商
		root.addElement("DevVendor").addText(StringUtil.getStringValue(devVendor));
		// 型号
		root.addElement("DevModel").addText(StringUtil.getStringValue(devModel));
		// 设备mac
		root.addElement("DevMAC").addText(StringUtil.getStringValue(devMac));

		return document.asXML();
	}

	public String getDevVendor() {
		return devVendor;
	}

	public void setDevVendor(String devVendor) {
		this.devVendor = devVendor;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getDevMac() {
		return devMac;
	}

	public void setDevMac(String devMac) {
		this.devMac = devMac;
	}
}
