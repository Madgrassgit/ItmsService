package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.service.JXReleaseService;


public class QueryDeviceMACChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceTempChecker.class);
	
	
	private String deviceMAC = "";
	private String netAccount = "";
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryDeviceMACChecker(String inXml){
		callXml = inXml;
	}
	
	
	/**
	 * 参数合法性检查
	 */
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DeviceInfo");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		// 表示 userInfo 入的是设备序列号
		if (6 == userInfoType) {
			if (userInfo.length() < 6) {
				result = 1007;
				resultDesc = "设备序列号长度不能小于6位";
				return false;
			}
		}

		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	/**
	 * 回参
	 */
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
		
		root.addElement("DeviceMAC").addText("" + deviceMAC);
		
		if ("hlj_dx".equals(Global.G_instArea)){
			root.addElement("NetAccount").addText("" + netAccount);
		}
		
		return document.asXML();
	}


	public String getDeviceMAC() {
		return deviceMAC;
	}


	public void setDeviceMAC(String deviceMAC) {
		this.deviceMAC = deviceMAC;
	}


	public String getNetAccount() {
		return netAccount;
	}


	public void setNetAccount(String netAccount) {
		this.netAccount = netAccount;
	}

	
}
