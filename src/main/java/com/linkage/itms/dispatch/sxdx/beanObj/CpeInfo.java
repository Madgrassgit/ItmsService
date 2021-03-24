package com.linkage.itms.dispatch.sxdx.beanObj;

import java.util.Arrays;

public class CpeInfo {
	/**
	 * 终端唯一标识
	 */
	private String deviceID;	
	/**
	 * 宽带账号
	 */
	private String accessNo;
	/**
	 * 激活码
	 */
	private String userId;
	/**
	 * 终端管理IP
	 */
	private String deviceIP;
	/**
	 * 统计参数列表
	 */
	private Para[] paraList;
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getAccessNo() {
		return accessNo;
	}
	public void setAccessNo(String accessNo) {
		this.accessNo = accessNo;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDeviceIP() {
		return deviceIP;
	}
	public void setDeviceIP(String deviceIP) {
		this.deviceIP = deviceIP;
	}
	public Para[] getParaList() {
		return paraList;
	}
	public void setParaList(Para[] paraList) {
		this.paraList = paraList;
	}
	@Override
	public String toString() {
		return "CpeInfo [deviceID=" + deviceID + ", accessNo=" + accessNo
				+ ", userId=" + userId + ", deviceIP=" + deviceIP
				+ ", paraList=" + Arrays.toString(paraList) + "]";
	}

}
