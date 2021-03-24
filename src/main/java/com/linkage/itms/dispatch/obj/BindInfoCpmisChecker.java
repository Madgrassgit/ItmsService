package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class BindInfoCpmisChecker extends BaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(BindInfoCpmisChecker.class);

	private String userName = "";
	private String bindType = "";
	private String vendor = "";
	private String devModel = "";
	private String handwareVersion = "";
	private String softwareVersion = "";
	private String bindTime = "";

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public BindInfoCpmisChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		ip = "";
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DevSN");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()) {
			return false;
		}
		if (1 == searchType) {
			if (false == userInfoTypeCheck() || false == userInfoCheck()) {
				return false;
			}
		}
		else if (2 == searchType) {
			if (false == devSnCheck()) {
				return false;
			}
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
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
	
		Element param = root.addElement("Param");
		param.addElement("UserName").addText(userName);
		param.addElement("DevSN").addText(devSn);
		param.addElement("BindType").addText(bindType);
		param.addElement("ip").addText(ip);
		param.addElement("vendor").addText(vendor);
		param.addElement("DevModel").addText(devModel);
		param.addElement("HandwareVersion").addText(handwareVersion);
		param.addElement("SoftwareVersion").addText(softwareVersion);
		param.addElement("BindTime").addText(bindTime);
		return document.asXML();
	}

	@Override
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if (8 != clientType && 7 != clientType 
				&& (!"jx_dx".equals(Global.G_instArea) || 6!=clientType)) 
		{
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
		}
		if (!"CX_01".equals(cmdType)) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		return true;
	}

	@Override
	public boolean devSnCheck(){
		if (2 == searchType || 0 == searchType) {
			// oui-串码
			if (!StringUtil.IsEmpty(devSn) && devSn.indexOf("-") != -1) {
				return true;
			}
			// 设备序列号
			if(false == pattern.matcher(devSn).matches() || devSn.length() < 6){
				result = 1005;
				resultDesc = "设备序列号不合法";
				return false;
			}
		}
		return true;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBindType() {
		return bindType;
	}

	public void setBindType(String bindType) {
		this.bindType = bindType;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getHandwareVersion() {
		return handwareVersion;
	}

	public void setHandwareVersion(String handwareVersion) {
		this.handwareVersion = handwareVersion;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getBindTime() {
		return bindTime;
	}

	public void setBindTime(String bindTime) {
		this.bindTime = bindTime;
	}
}
