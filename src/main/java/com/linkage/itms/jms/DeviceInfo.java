package com.linkage.itms.jms;

public class DeviceInfo {
	// 回调类型    1: 开通,修改ipsec回调    2：开通,修改,删除vxlan回调   3：ip变动上报(ipsec) 4：ip变动上报(vxlan) 5：新装上报
	private String servType = null;
	private String requestId = null; 
	private String devId = null;
	// 业务类型
	private String servTypeId = null;
	// 业务账号
	private String servName = null;
	// 操作类型
	private String servStatus = null;
	// 结果
	private String openStatus = null;
	// 1：家庭网关，2：企业网关
	private String gwType = null;
	// vxlan实例
	private String vxlanConfigSequence = null;
	// 设备厂家
	private String deviceVendor = null;
	// 设备型号
	private String deviceModel = null;
	// 软件版本
	private String softwareversion = null;
	// 硬件版本
	private String hardwareversion = null;
	// 宽带ip
	private String ipAddress = null;
	// 逻辑id
	private String loid = null;
	private String oui = null;
	private String devSn = null;
	private String userInfo = null;
	private String param = null;
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getGwType() {
		return gwType;
	}

	public void setGwType(String gwType) {
		this.gwType = gwType;
	}

	public String getServTypeId() {
		return servTypeId;
	}

	public void setServTypeId(String servTypeId) {
		this.servTypeId = servTypeId;
	}

	public String getServName() {
		return servName;
	}

	public void setServName(String servName) {
		this.servName = servName;
	}

	public String getServStatus() {
		return servStatus;
	}

	public void setServStatus(String servStatus) {
		this.servStatus = servStatus;
	}

	public String getOpenStatus() {
		return openStatus;
	}

	public void setOpenStatus(String openStatus) {
		this.openStatus = openStatus;
	}

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getVxlanConfigSequence() {
		return vxlanConfigSequence;
	}

	public void setVxlanConfigSequence(String vxlanConfigSequence) {
		this.vxlanConfigSequence = vxlanConfigSequence;
	}

	public String getServType() {
		return servType;
	}

	public void setServType(String servType) {
		this.servType = servType;
	}

	public String getDeviceVendor() {
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor) {
		this.deviceVendor = deviceVendor;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getSoftwareversion() {
		return softwareversion;
	}

	public void setSoftwareversion(String softwareversion) {
		this.softwareversion = softwareversion;
	}

	public String getHardwareversion() {
		return hardwareversion;
	}

	public void setHardwareversion(String hardwareversion) {
		this.hardwareversion = hardwareversion;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}
}
