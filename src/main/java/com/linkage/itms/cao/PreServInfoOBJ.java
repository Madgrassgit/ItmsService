package com.linkage.itms.cao;

/**
 * 调用预读业务接口的结构对象
 * 
 * @author Jason(3412)
 * @date 2010-5-7
 */
public class PreServInfoOBJ {

	String userId; // user_id
	String gatherId;
	String deviceId;
	String oui;
	String deviceSn;
	String servTypeId;
	String operTypeId = "1"; // 默认传1

	/**
	 * 默认构造方法
	 */
	public PreServInfoOBJ() {

	}

	/**
	 * 默认构造方法
	 */
	public PreServInfoOBJ(String _userId, String _deviceId, String _oui,
			String _devSn, String _servTypeId, String _operTypeId) {
		userId = _userId;
		deviceId = _deviceId;
		oui = _oui;
		deviceSn = _devSn;
		servTypeId = _servTypeId;
		operTypeId = _operTypeId;
	}

	
	/**
	 * toString()
	 */
	@Override
	public String toString() {
		return new StringBuffer("userId:").append(userId).append(", deviceId:")
				.append(deviceId).append("servTypeId:").append(servTypeId)
				.toString();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGatherId() {
		return gatherId;
	}

	public void setGatherId(String gatherId) {
		this.gatherId = gatherId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	public String getDeviceSn() {
		return deviceSn;
	}

	public void setDeviceSn(String deviceSn) {
		this.deviceSn = deviceSn;
	}

	public String getServTypeId() {
		return servTypeId;
	}

	public void setServTypeId(String servTypeId) {
		this.servTypeId = servTypeId;
	}

	public String getOperTypeId() {
		return operTypeId;
	}

	public void setOperTypeId(String operTypeId) {
		this.operTypeId = operTypeId;
	}

}
