package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class DeviceSNChecker extends BaseChecker {

	private static Logger logger = LoggerFactory.getLogger(DeviceSNChecker.class);

	private String devMac;
	private String devVendor;
	private String devModel;
	private String devVersiontype;
	private String devWantype;
	
	
	public DeviceSNChecker(String inXml) {
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
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

	public String getReturnXml() {
		Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");
        Element root =  document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        root.addElement("RstCode").addText(StringUtil.getStringValue(result));
        root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
       
        root.addElement("DevMAC").addText(StringUtil.getStringValue(devMac));
        root.addElement("DevVendor").addText(StringUtil.getStringValue(devVendor));
        root.addElement("DevModel").addText(StringUtil.getStringValue(devModel));
        root.addElement("DevVersiontype").addText(StringUtil.getStringValue(devVersiontype));
        root.addElement("DevWantype").addText(StringUtil.getStringValue(devWantype));
        
		return document.asXML();
	}

	public boolean baseCheck() {
		logger.debug("baseCheck()");

		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType && 5 != clientType) {
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}

		if (false == "CX_01".equals(cmdType)) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}

		return true;
	}

	boolean userInfoTypeCheck() {
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		DeviceSNChecker.logger = logger;
	}

	public String getDevMac() {
		return devMac;
	}

	public void setDevMac(String devMac) {
		this.devMac = devMac;
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

	public String getDevVersiontype() {
		return devVersiontype;
	}

	public void setDevVersiontype(String devVersiontype) {
		this.devVersiontype = devVersiontype;
	}

	public String getDevWantype() {
		return devWantype;
	}

	public void setDevWantype(String devWantype) {
		this.devWantype = devWantype;
	}
	
}
