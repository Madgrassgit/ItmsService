package com.linkage.itms.dispatch.gsdx.obj;

import java.util.HashMap;

/**
 * @author Jason(3412)
 * @date 2009-3-12
 */
public class WanConnObj {

	private String deviceId;
	private String wanId;
	private String wanConnId;
	private String gatherTime;
	private String ipConnNum;
	private String pppConnNum;
	private String vpi;
	private String vci;
	private String vlanid;
	
	//key=k#IP 、k#PPP
	private HashMap<String,WanConnSessObj> wanConnSessMap = null;
	
	public HashMap<String, WanConnSessObj> getWanConnSessMap()
	{
		return wanConnSessMap;
	}
	
	public void setWanConnSessMap(HashMap<String, WanConnSessObj> wanConnSessMap)
	{
		this.wanConnSessMap = wanConnSessMap;
	}
	
	public void setWanConnSessMap()
	{
		this.wanConnSessMap = new HashMap<String, WanConnSessObj>();
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
	public String getGatherTime() {
		return gatherTime;
	}
	public void setGatherTime(String gatherTime) {
		this.gatherTime = gatherTime;
	}
	public String getIpConnNum() {
		return ipConnNum;
	}
	public void setIpConnNum(String ipConnNum) {
		this.ipConnNum = ipConnNum;
	}
	public String getPppConnNum() {
		return pppConnNum;
	}
	public void setPppConnNum(String pppConnNum) {
		this.pppConnNum = pppConnNum;
	}
	public String getVpi() {
		return vpi;
	}
	public void setVpi(String vpi) {
		this.vpi = vpi;
	}
	public String getVci() {
		return vci;
	}
	public void setVci(String vci) {
		this.vci = vci;
	}
	public String getVlanid() {
		return vlanid;
	}
	public void setVlanid(String vlanid) {
		this.vlanid = vlanid;
	}

	@Override
	public String toString()
	{
		return "WanConnObj [deviceId=" + deviceId + ", wanId=" + wanId + ", wanConnId="
				+ wanConnId + ", gatherTime=" + gatherTime + ", ipConnNum=" + ipConnNum
				+ ", pppConnNum=" + pppConnNum + ", vpi=" + vpi + ", vci=" + vci
				+ ", vlanid=" + vlanid + ", wanConnSessMap=" + wanConnSessMap + "]";
	}
	
	
}
