package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

public class QueryGatewayBasicInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryGatewayBasicInfoDealXML.class);
	SAXReader reader = new SAXReader();

	private String ppp_username="";
	private String voip_name="";
	private String area_code="";
	private String register_status="";
	private String manufacturer="";
	private String device_type="";
	private String hardware_version="";
	private String access_time="";
	private String serial_number="";
	private String uplink_type="";
	private	String lan_count="";
	private String wireless_type="";
	private String terminal_type="";
	private String ip_address="";
	private String mac_address="";
	
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = "1234567898765";
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_username"));
			
			Document document = DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element root = document.addElement("root");
			// 接口调用唯一ID
			root.addElement("CmdID").addText(opId);
			// 结果代码
			root.addElement("CmdType").addText("CX_01");
			// 结果描述
			root.addElement("ClientType").addText("3");
			return document;
		} catch (Exception e) {
			logger.error("QueryTerminalInfoDealXML.getXML() is error!", e);
			return null;
		}
	}

	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("result_code").addText(result);
		// 结果代码
		root.addElement("result_desc").addText(errMsg);
		
		Element resultInfo = root.addElement("result_info");	
		resultInfo.addElement("ppp_username").addText(ppp_username);
		resultInfo.addElement("voip_number").addText(voip_name);
		resultInfo.addElement("area_code").addText(area_code);
		resultInfo.addElement("register_status").addText(register_status);
		resultInfo.addElement("manufacturer").addText(manufacturer);
		resultInfo.addElement("device_type").addText(device_type);
		resultInfo.addElement("hardware_version").addText(access_time);
		resultInfo.addElement("access_time").addText(access_time);
		resultInfo.addElement("serial_number").addText(serial_number);
		resultInfo.addElement("uplink_type").addText(uplink_type);
		resultInfo.addElement("lan_count").addText(lan_count);
		resultInfo.addElement("wireless_type").addText(wireless_type);
		resultInfo.addElement("terminal_type").addText(terminal_type);
		resultInfo.addElement("ip_address").addText(ip_address);
		resultInfo.addElement("mac_address").addText(mac_address);
		return document.asXML();
	}

	public SAXReader getReader() {
		return reader;
	}

	public void setReader(SAXReader reader) {
		this.reader = reader;
	}

	public String getPpp_username() {
		return ppp_username;
	}

	public void setPpp_username(String ppp_username) {
		this.ppp_username = ppp_username;
	}

	public String getVoip_name() {
		return voip_name;
	}

	public void setVoip_name(String voip_name) {
		this.voip_name = voip_name;
	}

	public String getArea_code() {
		return area_code;
	}

	public void setArea_code(String area_code) {
		this.area_code = area_code;
	}

	public String getRegister_status() {
		return register_status;
	}

	public void setRegister_status(String register_status) {
		this.register_status = register_status;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getDevice_type() {
		return device_type;
	}

	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}

	public String getHardware_version() {
		return hardware_version;
	}

	public void setHardware_version(String hardware_version) {
		this.hardware_version = hardware_version;
	}

	public String getAccess_time() {
		return access_time;
	}

	public void setAccess_time(String access_time) {
		this.access_time = access_time;
	}

	public String getSerial_number() {
		return serial_number;
	}

	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}

	public String getUplink_type() {
		return uplink_type;
	}

	public void setUplink_type(String uplink_type) {
		this.uplink_type = uplink_type;
	}

	public String getLan_count() {
		return lan_count;
	}

	public void setLan_count(String lan_count) {
		this.lan_count = lan_count;
	}

	public String getWireless_type() {
		return wireless_type;
	}

	public void setWireless_type(String wireless_type) {
		this.wireless_type = wireless_type;
	}

	public String getTerminal_type() {
		return terminal_type;
	}

	public void setTerminal_type(String terminal_type) {
		this.terminal_type = terminal_type;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public String getMac_address() {
		return mac_address;
	}

	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
}
