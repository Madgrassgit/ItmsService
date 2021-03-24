package com.linkage.itms.message;
/**
 * 用户信息
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util.message
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UserBean {
	
	// 设备ID
	private String deviceId;
	
	// 设备序列号
	private String deviceSn;
	
	// gatherId
	private String gatherId;
	
	// operTypeId
	private String operTypeId;
	
	// oui
	private String oui;
	
	// servTypeId
	private String servTypeId;
	
	private String userId;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceSn() {
		return deviceSn;
	}

	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}

	public String getGatherId() {
		return gatherId;
	}

	public void setGatherId(String gatherId) {
		this.gatherId = gatherId;
	}

	public String getOperTypeId() {
		return operTypeId;
	}

	public void setOperTypeId(String operTypeId) {
		this.operTypeId = operTypeId;
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	public String getServTypeId() {
		return servTypeId;
	}

	public void setServTypeId(String servTypeId) {
		this.servTypeId = servTypeId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
