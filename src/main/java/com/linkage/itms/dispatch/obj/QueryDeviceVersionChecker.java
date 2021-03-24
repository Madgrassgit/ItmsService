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


public class QueryDeviceVersionChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersionChecker.class);
	
	private String DevVendor = null;
	
	private String DevModel = null;
	
	private String DevSoftwareversion = null;
	
	private String IsStandard = null;
	
	private String specDesc = null;
	
	private String bps = null;
	
	public QueryDeviceVersionChecker(String inXml){
		this.callXml = inXml;
	}
	
	
	
	/**
	 * 入参验证
	 */
	public boolean check(){
		
		logger.debug("QueryDeviceVersionChecker==>check()");
		
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
			
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			
			userInfo = param.elementTextTrim("UserInfo");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if("js_dx".equals(Global.G_instArea)){
			if (false == baseCheck() || false == userInfoTypeCheck_jsdx()
					|| false == userInfoCheck()) {
				return false;
			}
		}
		else{
			if (false == baseCheck() || false == userInfoTypeCheck()
					|| false == userInfoCheck()) {
				return false;
			}
		}
			
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	/**
	 * 用户信息类型合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean userInfoTypeCheck_jsdx(){
		if(1 != userInfoType && 2 != userInfoType
				&& 3 != userInfoType && 4 != userInfoType
				&& 5 != userInfoType){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}
	
	public String getReturnXml(){
		
		logger.debug("QueryDeviceVersionChecker==>getReturnXml()");
		
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		// 厂商
		if (null !=  DevVendor) {
			root.addElement("DevVendor").addText(DevVendor);
		}else {
			root.addElement("DevVendor").addText("");
		}
		
		// 设备型号
		if (null != DevModel) {
			root.addElement("DevModel").addText(DevModel);
		}else {
			root.addElement("DevModel").addText("");
		}
		
		// 软件版本
		if (null != DevSoftwareversion) {
			root.addElement("DevSoftwareversion").addText(DevSoftwareversion);
		}else {
			root.addElement("DevSoftwareversion").addText("");
		}
		if("sd_lt".equals(Global.G_instArea)){
		//设备sn
			if(null!=devSn){
				root.addElement("DevSN").addText(devSn);
			}else{
				root.addElement("DevSN").addText("");
			}
		//ip
			if(null!=ip){
				root.addElement("Ip").addText(ip);
			}else{
				root.addElement("Ip").addText("");
			}
		//loid
			if(null!=loid){
				root.addElement("LOID").addText(loid);
			}else{
				root.addElement("LOID").addText("");
			}
		}else{
			//  是否是规范版本    1：规范   2：不规范
			if (null != IsStandard) {
				root.addElement("IsStandard").addText(IsStandard);
			}else {
				root.addElement("IsStandard").addText("");
			}
		}
		if("js_dx".equals(Global.G_instArea)){
			root.addElement("DeviceSpec").addText(null==specDesc?"":specDesc);
		}
		if("nx_dx".equals(Global.G_instArea)){
			root.addElement("bps").addText(null==bps?"":bps);
		}
		return document.asXML();
	}
	
	
	
	
	public String getDevVendor() {
		return DevVendor;
	}
	
	public void setDevVendor(String devVendor) {
		DevVendor = devVendor;
	}
	
	public String getDevModel() {
		return DevModel;
	}
	
	public void setDevModel(String devModel) {
		DevModel = devModel;
	}
	
	public String getDevSoftwareversion() {
		return DevSoftwareversion;
	}
	
	public void setDevSoftwareversion(String devSoftwareversion) {
		DevSoftwareversion = devSoftwareversion;
	}

	public String getIsStandard() {
		return IsStandard;
	}
	
	public void setIsStandard(String isStandard) {
		IsStandard = isStandard;
	}

	public String getSpecDesc() {
		return specDesc;
	}

	public void setSpecDesc(String specDesc) {
		this.specDesc = specDesc;
	}

	public String getBps()
	{
		return bps;
	}

	public void setBps(String bps)
	{
		this.bps = bps;
	}
	
}
