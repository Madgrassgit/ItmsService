package com.linkage.stbms.obj;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 设备对象类
 * 
 * @author Jason(3412)
 * @date 2009-12-17
 */
public class UserStbInfoOBJ {

	private static Logger logger = LoggerFactory
			.getLogger(UserStbInfoOBJ.class);

	// 设备ID
	private String deviceId;
	// 连接状态 1：连通 0：不通 -1：未检测
	private int connState;
	// ip地址
	private String ip;
	// 厂商
	private String vendor;
	// 型号
	private String model;
	// 序列号
	private String sn;
	// MAC地址
	private String mac;
	// 业务账号
	private String stbUsername;
	// 业务密码
	private String stbPasswd;
	// 媒体服务器地址
	private String mediaAddr;
	// 播放频道号
	private String servName;
	// 接入方式(设备采集值) Lan; PPPOE; DHCP
	private String accessType;

	// 软件版本
	private String softversion;
	// 最后一次业务认证的服务器URL
	private String authUrl;

	/**
	 * 接口对象返回XML字符串
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-17
	 * @return String
	 */
	public String obj2xml() {
		logger.debug("UserStbInfoOBJ.obj2Xml()");

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");

		root.addElement("stb_state").addText(
				StringUtil.getStringValue(connState));
		root.addElement("stb_ip").addText(ip == null ? "-1" : ip);
		root.addElement("stb_vendor").addText(vendor == null ? "-1" : vendor);

		root.addElement("stb_sn").addText(sn == null ? "-1" : sn);
		root.addElement("stb_type").addText(model == null ? "-1" : model);
		root.addElement("stb_mac").addText(mac == null ? "-1" : mac);
		root.addElement("stb_username").addText(
				stbUsername == null ? "-1" : stbUsername);
		root.addElement("stb_passwd").addText(
				stbPasswd == null ? "-1" : stbPasswd);
		root.addElement("media_ip").addText(
				mediaAddr == null ? "-1" : mediaAddr);
		root.addElement("play_state").addText(
				StringUtil.getStringValue(getPlayStat(servName)));
		root.addElement("access_type").addText(
				accessType == null ? "-1" : accessType);
		root.addElement("softversion").addText(
				softversion == null ? "-1" : softversion);
		root.addElement("auth_url").addText(
				authUrl == null ? "-1" : authUrl);
		
		return document.asXML();
	}

	/**
	 * 
	 * 采集到的Device.STBDevice.1.AVProfile.AudienceStats.ServiceName结点值
	 * 
	 * 直播：Channel NO. is x （x为频道号） 点播：Channel NO. is -1 其他状态： Not Play any
	 * Program
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-22
	 * @return String 播放状态 0：直播 1：点播 -1：其它
	 */
	String getPlayStat(String servName) {
		logger.debug("getPlayStat({})", servName);
		if (StringUtil.IsEmpty(servName) || servName.contains("Not")) {
			return "-1";
		} else if (servName.contains("-1")) {
			return "1";
		} else {
			return "0";
		}
	}

	/** getter, setter methods */
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getConnState() {
		return connState;
	}

	public void setConnState(int connState) {
		this.connState = connState;
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

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getStbUsername() {
		return stbUsername;
	}

	public void setStbUsername(String stbUsername) {
		this.stbUsername = stbUsername;
	}

	public String getStbPasswd() {
		return stbPasswd;
	}

	public void setStbPasswd(String stbPasswd) {
		this.stbPasswd = stbPasswd;
	}

	public String getMediaAddr() {
		return mediaAddr;
	}

	public void setMediaAddr(String mediaAddr) {
		this.mediaAddr = mediaAddr;
	}

	public String getServName() {
		return servName;
	}

	public void setServName(String servName) {
		this.servName = servName;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getSoftversion() {
		return softversion;
	}

	public void setSoftversion(String softversion) {
		this.softversion = softversion;
	}

	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

}
