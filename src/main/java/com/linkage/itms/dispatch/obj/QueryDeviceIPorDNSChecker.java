package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class QueryDeviceIPorDNSChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceTempChecker.class);
	
	private List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryDeviceIPorDNSChecker(String inXml){
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
			logger.warn("devSn=" + devSn);
			
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
	 * deviceinfo合法性检查
	 * 
	 * @return boolean
	 */
	protected boolean devSNCheck(){
		if("null".equals(devSn)){
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
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
		
		Element intelnets = root.addElement("Intelnet");
		if(wanList != null && wanList.size() > 0){
			for(Map<String,String> tmp : wanList){
				String num = StringUtil.getStringValue(tmp,"num");
				Element intelnet = intelnets.addElement("Intelnet").addAttribute("num", num);
				intelnet.addElement("IntelnetVlan").addText(StringUtil.getStringValue(tmp,"IntelnetVlan",""));
				intelnet.addElement("IPv4IPAddress").addText(StringUtil.getStringValue(tmp,"IPv4IPAddress",""));
				intelnet.addElement("IPv4DNSAddress").addText(StringUtil.getStringValue(tmp,"IPv4DNSAddress",""));
				intelnet.addElement("IPv6IPAddress").addText(StringUtil.getStringValue(tmp,"IPv6IPAddress",""));
				intelnet.addElement("IPv6DNSAddress").addText(StringUtil.getStringValue(tmp,"IPv6DNSAddress",""));
			}
			
		}
		return document.asXML();
	}

	public List<HashMap<String, String>> getWanList() {
		return wanList;
	}


	public void setWanList(List<HashMap<String, String>> wanList) {
		this.wanList = wanList;
	}

}
