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
 * bind方法接口的XML元素对象
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class BindInfoChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BindInfoChecker.class);
	
	private String username = "";
	private String deviceSn = "";
	private String oui = "";
	private String bindType = "";
	private String ip = "";
	private String vendor = "";
	private String devModel = "";
	private String handwareVersion = "";
	private String softwareVersion = "";
	
	private String isNormal="";
	private String accessStyleRelayId="";      
	private String ipModelType="";
	private String specId="";
	private String mbbroadband="";
	private String gbbroadband="";
	private String protocol="" ;
	// JXDX-ITMS-REQ-20170412-LINBX-001(ITMS平台与CRM实时查询光猫设备序列号信息接口）
	private String bindTime = "";
	
	// 新疆新增节点
	private String lanNum = "";
	private String voiceNum = "";
	private String deviceVersionType = "";
	private String isWifi = "";
	private String cpeMac = "";
	
	//新疆新增返回参数
    //gbbroadband是否有千兆口
	private String is_security_plugin = ""; //是否支持安审版本
	private String security_plugin_type = "";//安审版本类型
	private String isOld = ""; //是否为老旧终端
	private String getPast = ""; //新疆是否报废终端,0 正常，1 报废终端
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public BindInfoChecker(String inXml) {
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if("xj_dx".equals(Global.G_instArea))
		{
			if(!StringUtil.IsEmpty(devSn) && devSn.contains("-")){
				String[] devs = devSn.split("-");
				devSn = devs[1];  //针对新疆oui-devsn这种情况
				oui = devs[0];
			}
			if (false == baseCheck() || false == cityIdCheck()) {
				return false;
			}
			
			if(0 == userInfoType)
			{
				if( false == devSnCheck())
				{
					return false;
				}
				userInfo = devSn;
			}
			else
			{
				if(false == userInfoCheck())
				{
					return false;
				}
			}
		}
		else
		{
			if (false == baseCheck() || false == userInfoTypeCheck()
					|| false == userInfoCheck()
					|| (!"cq_dx".equals(Global.G_instArea) && false == cityIdCheck())) {
				return false;
			}
			if(!"cq_dx".equals(Global.G_instArea)){
				if((!"xj_dx".equals(Global.G_instArea)&& !"jl_dx".equals(Global.G_instArea) && false == devSnCheck())){
					return false;
				}
			}
		}
		result = 0;
		resultDesc = "成功";
		
		return true;
	}

	/**
	 * 返回绑定调用结果字符串
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
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

		root.addElement("Param").addElement("UserName").addText(username + "");
		root.element("Param").addElement("DevOUI").addText(oui + "");
		root.element("Param").addElement("BindType").addText(bindType + "");
		
		root.element("Param").addElement("ip").addText(ip + "");
		root.element("Param").addElement("vendor").addText(vendor + "");
		root.element("Param").addElement("DevModel").addText(devModel + "");
		root.element("Param").addElement("HandwareVersion").addText(handwareVersion + "");
		root.element("Param").addElement("SoftwareVersion").addText(softwareVersion + "");
		if("hb_dx".equals(Global.G_instArea)){
			root.element("Param").addElement("IsNormal").addText(isNormal);
			root.element("Param").addElement("AccessStyleRelayId").addText(accessStyleRelayId);
			root.element("Param").addElement("IpType").addText(softwareVersion);
			root.element("Param").addElement("voipProtocol").addText(protocol);
			root.element("Param").addElement("specId").addText(specId);
			root.element("Param").addElement("mbBroadband").addText(mbbroadband);
			root.element("Param").addElement("gbbroadband").addText(gbbroadband);
		}
		if("cq_dx".equals(Global.G_instArea)){
			root.element("Param").addElement("AccessStyleRelayId").addText(accessStyleRelayId);
		}
		
		// JXDX-ITMS-REQ-20170412-LINBX-001(ITMS平台与CRM实时查询光猫设备序列号信息接口）
		if ("jx_dx".equals(Global.G_instArea)) {
			root.element("Param").addElement("BindTime").addText(bindTime);
			root.element("Param").addElement("DevSN").addText(devSn);
		}
		else {
			root.element("Param").addElement("DevSN").addText(deviceSn);
		}
		
		if("xj_dx".equals(Global.G_instArea)){
			root.element("Param").addElement("lanNum").addText(lanNum + "");
			root.element("Param").addElement("voiceNum").addText(voiceNum + "");
			root.element("Param").addElement("versionType").addText(deviceVersionType + "");
			root.element("Param").addElement("isWifi").addText(isWifi + "");
			root.element("Param").addElement("mac").addText(cpeMac + "");
			//新加
			root.element("Param").addElement("gbbroadband").addText(gbbroadband + "");
			root.element("Param").addElement("isPlugin").addText(is_security_plugin + "");
			root.element("Param").addElement("pluginType").addText(security_plugin_type + "");
			root.element("Param").addElement("Isold").addText(isOld + "");
			root.element("Param").addElement("getPast").addText(getPast + "");
		}
		
		return document.asXML();
	}

	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		/*1：BSS
		2：IPOSS
		3：综调
		4：RADIUS
		5：爱运维
		6：预处理
		7：CRM
		8：CPMIS
		9：电渠微信公众号平台*/
		if ("jx_dx".equals(Global.G_instArea)) {
			if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType
					&& 8 != clientType && 9 != clientType){
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
		}else{
			if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType){
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}

	/**
	 * @return the bindType
	 */
	public String getBindType() {
		return bindType;
	}

	/**
	 * @param bindType the bindType to set
	 */
	public void setBindType(String bindType) {
		this.bindType = bindType;
	}

	/**
	 * @return the deviceSn
	 */
	public String getDeviceSn() {
		return deviceSn;
	}

	/**
	 * @param deviceSn the deviceSn to set
	 */
	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	
	public String getIp()
	{
		return ip;
	}

	
	public void setIp(String ip)
	{
		this.ip = ip;
	}

	
	public String getVendor()
	{
		return vendor;
	}

	
	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	
	public String getDevModel()
	{
		return devModel;
	}

	
	public void setDevModel(String devModel)
	{
		this.devModel = devModel;
	}

	
	public String getHandwareVersion()
	{
		return handwareVersion;
	}

	
	public void setHandwareVersion(String handwareVersion)
	{
		this.handwareVersion = handwareVersion;
	}

	
	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	
	public void setSoftwareVersion(String softwareVersion)
	{
		this.softwareVersion = softwareVersion;
	}
	
	public String getOui()
	{
		return oui;
	}

	
	public void setOui(String oui)
	{
		this.oui = oui;
	}

	
	public String getIsNormal()
	{
		return isNormal;
	}

	
	public void setIsNormal(String isNormal)
	{
		this.isNormal = isNormal;
	}

	
	public String getAccessStyleRelayId()
	{
		return accessStyleRelayId;
	}

	
	public void setAccessStyleRelayId(String accessStyleRelayId)
	{
		this.accessStyleRelayId = accessStyleRelayId;
	}

	
	public String getIpModelType()
	{
		return ipModelType;
	}

	
	public void setIpModelType(String ipModelType)
	{
		this.ipModelType = ipModelType;
	}

	
	public String getSpecId()
	{
		return specId;
	}

	
	public void setSpecId(String specId)
	{
		this.specId = specId;
	}

	
	public String getMbbroadband()
	{
		return mbbroadband;
	}

	
	public void setMbbroadband(String mbbroadband)
	{
		this.mbbroadband = mbbroadband;
	}

	
	public String getProtocol()
	{
		return protocol;
	}

	
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	public String getBindTime() {
		return bindTime;
	}

	public void setBindTime(String bindTime) {
		this.bindTime = bindTime;
	}

	public String getGbbroadband() {
		return gbbroadband;
	}

	public void setGbbroadband(String gbbroadband) {
		this.gbbroadband = gbbroadband;
	}

	public String getLanNum() {
		return lanNum;
	}

	public void setLanNum(String lanNum) {
		this.lanNum = lanNum;
	}

	public String getVoiceNum() {
		return voiceNum;
	}

	public void setVoiceNum(String voiceNum) {
		this.voiceNum = voiceNum;
	}

	public String getDeviceVersionType() {
		return deviceVersionType;
	}

	public void setDeviceVersionType(String deviceVersionType) {
		this.deviceVersionType = deviceVersionType;
	}

	public String getIsWifi() {
		return isWifi;
	}

	public void setIsWifi(String isWifi) {
		this.isWifi = isWifi;
	}

	public String getCpeMac() {
		return cpeMac;
	}

	public void setCpeMac(String cpeMac) {
		this.cpeMac = cpeMac;
	}
	 
	public String getIs_security_plugin()
	{
		return is_security_plugin;
	}

	
	public void setIs_security_plugin(String is_security_plugin)
	{
		this.is_security_plugin = is_security_plugin;
	}

	
	public String getSecurity_plugin_type()
	{
		return security_plugin_type;
	}

	
	public void setSecurity_plugin_type(String security_plugin_type)
	{
		this.security_plugin_type = security_plugin_type;
	}
	public String getIsOld()
	{
		return isOld;
	}

	
	public void setIsOld(String isOld)
	{
		this.isOld = isOld;
	}

	public String getGetPast()
	{
		return getPast;
	}

	public void setGetPast(String getPast)
	{
		this.getPast = getPast;
	}
}
