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

/**
 * 
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2019-5-17
 */
public class DevInternetIpv6InfoChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DevInternetIpv6InfoChecker.class);
	
	private List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();
	private String wanType;
	private String vlanId;
	private String bindPort;
	private String ipv6Address;
	private String ipv6DnsServers;
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public DevInternetIpv6InfoChecker(String inXml){
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
		
		Element wanInfo = root.addElement("wanInfo");
		Element vlan = wanInfo.addElement("vlan");
		vlan.addElement("wanType").addText("" + wanType);
		vlan.addElement("vlanId").addText("" + vlanId);
		vlan.addElement("bindPort").addText("" + bindPort);
		vlan.addElement("ipv6Address").addText("" + ipv6Address);
		vlan.addElement("ipv6DnsServers").addText("" + ipv6DnsServers);
		return document.asXML();
	}
	
	public List<HashMap<String, String>> getWanList() {
		return wanList;
	}

	public void setWanList(List<HashMap<String, String>> wanList) {
		this.wanList = wanList;
	}


	public String getWanType() {
		return wanType;
	}


	public void setWanType(String wanType) {
		this.wanType = wanType;
	}


	public String getVlanId() {
		return vlanId;
	}


	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}


	public String getBindPort() {
		return bindPort;
	}


	public void setBindPort(String bindPort) {
		this.bindPort = bindPort;
	}


	public String getIpv6Address() {
		return ipv6Address;
	}


	public void setIpv6Address(String ipv6Address) {
		this.ipv6Address = ipv6Address;
	}


	public String getIpv6DnsServers() {
		return ipv6DnsServers;
	}


	public void setIpv6DnsServers(String ipv6DnsServers) {
		this.ipv6DnsServers = ipv6DnsServers;
	}
	
}
