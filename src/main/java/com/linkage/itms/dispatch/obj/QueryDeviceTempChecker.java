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


public class QueryDeviceTempChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceTempChecker.class);
	
	
	private String temperature = "";
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryDeviceTempChecker(String inXml){
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
		
		if ("nx_dx".equals(Global.G_instArea)) {
			// 参数合法性检查
			if (false == baseCheckNX()) {
				return false;
			}
			
			if(1 != userInfoType && 2 != userInfoType
					&& 3 != userInfoType && 4 != userInfoType
					&& 5 != userInfoType ){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			
			if(StringUtil.IsEmpty(userInfo) && StringUtil.IsEmpty(devSn)){
				result = 1000;
				resultDesc = "用户信息和设备序列号不可同时为空";
				return false;
			}
			
			if (!StringUtil.IsEmpty(devSn)) {
				if (devSn.length() < 6) {
					result = 1007;
					resultDesc = "设备序列号长度不能小于6位";
					return false;
				}
			}
			
		}else{
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
		
		//root.addElement("SSIDname").addText("" + SSIDname);
		
		root.addElement("DeviceTemperature").addText("" + temperature);
		
		return document.asXML();
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

}
