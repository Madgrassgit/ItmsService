package com.linkage.itms.message;

/**
 * 绑定信息
 * 
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util.message
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class BindBean {

	// 账户ID
	private String accOid = null;

	// 账户名
	private String accName = null;

	// 用户名
	private String userName = null;

	// 设备ID
	private String deviceId = null;
	
	public String userId = null;

	private int userLine = 0;

	public String getAccOid() {
		return accOid;
	}

	public void setAccOid(String accOid) {
		this.accOid = accOid;
	}

	public String getAccName() {
		return accName;
	}

	public void setAccName(String accName) {
		this.accName = accName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getUserLine() {
		return userLine;
	}

	public void setUserLine(int userLine) {
		this.userLine = userLine;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
