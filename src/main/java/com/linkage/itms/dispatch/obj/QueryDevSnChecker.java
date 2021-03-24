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


public class QueryDevSnChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(QueryDevSnChecker.class);
	private String devSn = "";
	private String macaddress = "";
	private String vendor = "";
	private String devModel = "";
	private String hardwareVersion = "";
	private String softwareVersion = "";
	//新疆新增：终端是否支持当前带宽值 0-不支持 1-支持
	private int isSupport = 0;


	public QueryDevSnChecker(String inXml) {
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
		if (!baseCheck() ||!userInfoTypeCheck()
				||!userInfoCheck()) {
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);

		if("xj_dx".equals(Global.G_instArea))
		{
			Element param = root.addElement("Param");
			param.addElement("DevSN").addText(devSn);
			param.addElement("mac").addText(macaddress);
			param.addElement("Vendor").addText(vendor);
			param.addElement("DevModel").addText(devModel);
			param.addElement("HardwareVersion").addText(hardwareVersion);
			param.addElement("SoftwareVersion").addText(softwareVersion);
			param.addElement("IsSupport").addText(String.valueOf(isSupport));
		}
		else
		{
			root.addElement("DevSN").addText(""+ devSn);
		}
		
		return document.asXML();
	}
	
	
	public String getDevSn(){
		return devSn;
	}
	
	public void setDevSn(String devSn){
		this.devSn = devSn;
	}
	
	public String getOui(){
		return oui;
	}
	
	public void setOui(String oui){
		this.oui = oui;
	}

	public String getMacaddress() {
		return macaddress;
	}

	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
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

	public String getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(String hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public int getIsSupport() {
		return isSupport;
	}

	public void setIsSupport(int isSupport) {
		this.isSupport = isSupport;
	}
}
