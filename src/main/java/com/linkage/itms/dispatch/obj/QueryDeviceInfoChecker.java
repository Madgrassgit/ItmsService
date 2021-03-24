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

/**
 * @author Jason(3412)
 * @date 2010-4-1
 */
public class QueryDeviceInfoChecker extends BaseQueryChecker {

	private static Logger logger = LoggerFactory.getLogger(QueryDeviceInfoChecker.class);

	private String username = "";
	private String deviceSn = "";
	private String oui = "";
	private String bindType = "";
	private String ip = "";
	private String vendor = "";
	private String devModel = "";
	private String handwareVersion = "";
	private String softwareVersion = "";
	private String online = "";
	private String accessTypeId = "";

	/**
	 * 构造方法
	 * 
	 * @param _callXml
	 *            客户端查询XML字符串
	 */
	public QueryDeviceInfoChecker(String _callXml) {
		logger.debug("CallChecker({})", _callXml);
		callXml = _callXml;
	}

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
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck()
				|| (!"cq_dx".equals(Global.G_instArea) && false == cityIdCheck()) || false == searchTypeCheck() || false == userInfoTypeCheck()) {
			return false;
		}
		// 用户账号合法性检查
		if (1 == searchType && StringUtil.IsEmpty(username)) {
			result = 1002;
			resultDesc = "用户帐号不合法";
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}
	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果

		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		root.addElement("Param").addElement("UserName").addText(username);
		root.element("Param").addElement("DevOUI").addText(oui);
		root.element("Param").addElement("DevSN").addText(deviceSn);
		root.element("Param").addElement("BindType").addText(bindType);
		root.element("Param").addElement("ip").addText(ip);
		root.element("Param").addElement("vendor").addText(vendor);
		root.element("Param").addElement("DevModel").addText(devModel);
		root.element("Param").addElement("HandwareVersion").addText(handwareVersion);
		root.element("Param").addElement("SoftwareVersion").addText(softwareVersion);
		if ("hb_lt".equals(Global.G_instArea)){
			root.element("Param").addElement("cityId").addText(cityId);
		}
		if ("cq_dx".equals(Global.G_instArea)){
			root.element("Param").addElement("online").addText(StringUtil.getStringValue(online));
			root.element("Param").addElement("accessTypeId").addText(StringUtil.getStringValue(accessTypeId));
		}
		return document.asXML();
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getDeviceSn() {
		return deviceSn;
	}


	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}


	public String getOui() {
		return oui;
	}


	public void setOui(String oui) {
		this.oui = oui;
	}


	public String getBindType() {
		return bindType;
	}


	public void setBindType(String bindType) {
		this.bindType = bindType;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
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

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public String getAccessTypeId() {
		return accessTypeId;
	}

	public void setAccessTypeId(String accessTypeId) {
		this.accessTypeId = accessTypeId;
	}
}
