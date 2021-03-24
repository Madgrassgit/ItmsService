package com.linkage.itms.rms.obj;

import java.util.Date;

import com.linkage.itms.rms.obj.domain.IParamTree;


public class WanConnSessObj {

	private String deviceId = null;

	private String wanId = null;

	private String wanConnId = null;

	private String wanConnSessId = null;

	private String gatherTime = null;

	private String sessType = null;
	
	private String enable = null;
	
	private String name = null;
	
	private String connType = null;
	
	private String servList = null;
	
	private String bindPort = null;
	
	private String username = null;
	
	private String password = null;
	
	private String ipType = null;
	
	private String ip = null;
	
	private String mask = null;
	
	private String gateway = null;
	
	private String dnsEnab = null;
	
	private String dns = null;
	
	private String status = null;
	
	private String connError = null;
	
	private String natEnable = null;
	
	private String pvc = null;
	
	private String pppAuthProtocol = null;
	
	private String dialNum = null;
	
	private String workMode = null;
	
	private String loadPercent = null;
	
	private String backupItfs = null;
	
	private String connTrigger = null;	
	
	private String vlanid = null;
	
	private String ipMode = null;
	
	private String ip_ipv6 = null;
	
	private String dns_ipv6 = null;
	
	/**
	 * 获取PPPConnect的ping端口
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-10-29
	 * @return String
	 */
	public String getPingInterface(){
		return IParamTree.WANDEVICE + "." + this.getWanId() + "."
		+ IParamTree.WANCONNDEVICE + "." + this.getWanConnId() + "."
		+ IParamTree.WANPPPCONN + "." + this.getWanConnSessId();
	}
	
	/**
	 * 获取IPConnect的ping端口
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-10-29
	 * @return String
	 */
	public String getPingIpConnInterface(){
		return IParamTree.WANDEVICE + "." + this.getWanId() + "."
		+ IParamTree.WANCONNDEVICE + "." + this.getWanConnId() + "."
		+ IParamTree.WANIPCONN + "." + this.getWanConnSessId();
	}
	
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getWanId() {
		return wanId;
	}

	public void setWanId(String wanId) {
		this.wanId = wanId;
	}

	public String getWanConnId() {
		return wanConnId;
	}

	public void setWanConnId(String wanConnId) {
		this.wanConnId = wanConnId;
	}

	public String getWanConnSessId() {
		return wanConnSessId;
	}

	public void setWanConnSessId(String wanConnSessId) {
		this.wanConnSessId = wanConnSessId;
	}

	public String getGatherTime() {
		if (gatherTime == null || gatherTime.length() == 0) {
			gatherTime = "" + (new Date()).getTime() / 1000;
		}
		return gatherTime;
	}

	public void setGatherTime(String gatherTime) {
		this.gatherTime = gatherTime;
	}

	public String getSessType() {
		return sessType;
	}

	public void setSessType(String sessType) {
		this.sessType = sessType;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnType() {
		return connType;
	}

	public void setConnType(String connType) {
		this.connType = connType;
	}

	public String getServList() {
		return servList;
	}

	public void setServList(String servList) {
		this.servList = servList;
	}

	public String getBindPort() {
		return bindPort;
	}

	public void setBindPort(String bindPort) {
		this.bindPort = bindPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIpType() {
		return ipType;
	}

	public void setIpType(String ipType) {
		this.ipType = ipType;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getDnsEnab() {
		return dnsEnab;
	}

	public void setDnsEnab(String dnsEnab) {
		this.dnsEnab = dnsEnab;
	}

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getConnError() {
		return connError;
	}

	public void setConnError(String connError) {
		this.connError = connError;
	}

	public String getNatEnable() {
		return natEnable;
	}

	public void setNatEnable(String natEnable) {
		this.natEnable = natEnable;
	}

	public String getPvc() {
		return pvc;
	}

	public void setPvc(String pvc) {
		this.pvc = pvc;
	}
	
	/**
	 * @return the backupItfs
	 */
	public String getBackupItfs() {
		return backupItfs;
	}

	/**
	 * @param backupItfs the backupItfs to set
	 */
	public void setBackupItfs(String backupItfs) {
		this.backupItfs = backupItfs;
	}

	/**
	 * @return the connTrigger
	 */
	public String getConnTrigger() {
		return connTrigger;
	}

	/**
	 * @param connTrigger the connTrigger to set
	 */
	public void setConnTrigger(String connTrigger) {
		this.connTrigger = connTrigger;
	}

	/**
	 * @return the dialNum
	 */
	public String getDialNum() {
		return dialNum;
	}

	/**
	 * @param dialNum the dialNum to set
	 */
	public void setDialNum(String dialNum) {
		this.dialNum = dialNum;
	}

	/**
	 * @return the loadPercent
	 */
	public String getLoadPercent() {
		return loadPercent;
	}

	/**
	 * @param loadPercent the loadPercent to set
	 */
	public void setLoadPercent(String loadPercent) {
		this.loadPercent = loadPercent;
	}

	/**
	 * @return the pppAuthProtocol
	 */
	public String getPppAuthProtocol() {
		return pppAuthProtocol;
	}

	/**
	 * @param pppAuthProtocol the pppAuthProtocol to set
	 */
	public void setPppAuthProtocol(String pppAuthProtocol) {
		this.pppAuthProtocol = pppAuthProtocol;
	}

	/**
	 * @return the workMode
	 */
	public String getWorkMode() {
		return workMode;
	}

	/**
	 * @param workMode the workMode to set
	 */
	public void setWorkMode(String workMode) {
		this.workMode = workMode;
	}

	/**
	 * LAN上行时连接的vlan_id
	 */
	public String getVlanid() {
		return vlanid;
	}

	/**
	 * lan上行时连接的vlan_id
	 */
	public void setVlanid(String vlanid) {
		this.vlanid = vlanid;
	}

	public String getIpMode() {
		return ipMode;
	}

	public void setIpMode(String ipMode) {
		this.ipMode = ipMode;
	}

	public String getIp_ipv6() {
		return ip_ipv6;
	}

	public void setIp_ipv6(String ip_ipv6) {
		this.ip_ipv6 = ip_ipv6;
	}

	public String getDns_ipv6() {
		return dns_ipv6;
	}

	public void setDns_ipv6(String dns_ipv6) {
		this.dns_ipv6 = dns_ipv6;
	}

	
}
