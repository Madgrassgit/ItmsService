package com.linkage.itms.dispatch.util;

/**
 * @author Jason(3412)
 * @date 2009-3-4
 */
public class DevRpcObject {

	private String device_id;
	private String device_serialnumber;
	private String devicetype_id;
	private String loopback_ip;
	private String cr_port;
	private String cr_path;
	private String gather_id;
	private String acs_username;
	private String acs_passwd;
	private String oui;
	private String ior;
	
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getDevice_serialnumber() {
		return device_serialnumber;
	}
	public void setDevice_serialnumber(String device_serialnumber) {
		this.device_serialnumber = device_serialnumber;
	}
	public String getDevicetype_id() {
		return devicetype_id;
	}
	public void setDevicetype_id(String devicetype_id) {
		this.devicetype_id = devicetype_id;
	}
	public String getLoopback_ip() {
		return loopback_ip;
	}
	public void setLoopback_ip(String loopback_ip) {
		this.loopback_ip = loopback_ip;
	}
	public String getPort() {
		return cr_port;
	}
	public void setPort(String cr_port) {
		this.cr_port = cr_port;
	}
	public String getPath() {
		return cr_path;
	}
	public void setPath(String cr_path) {
		this.cr_path = cr_path;
	}
	public String getGather_id() {
		return gather_id;
	}
	public void setGather_id(String gather_id) {
		this.gather_id = gather_id;
	}
	public String getAcs_username() {
		return acs_username;
	}
	public void setAcs_username(String acs_username) {
		this.acs_username = acs_username;
	}
	public String getAcs_passwd() {
		return acs_passwd;
	}
	public void setAcs_passwd(String acs_passwd) {
		this.acs_passwd = acs_passwd;
	}
	public String getOui() {
		return oui;
	}
	public void setOui(String oui) {
		this.oui = oui;
	}
	public String getIor() {
		return ior;
	}
	public void setIor(String ior) {
		this.ior = ior;
	}
	
}
