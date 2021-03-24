package com.linkage.stbms.ids.obj;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-3-20
 * @category com.linkage.stbms.ids.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class BoxCheckObj
{
	private String cmdId;
	private String result;
	private String resultDesc;
	private String devSn="";
	private String mac="";
	private String vendor="";
	private String devModel="";
	private String hardwareVersion="";
	private String softwareVersion="";
	private String oui;
	
	
	public String getOui(){
		return oui;
	}
	
	public void setOui(String oui){
		this.oui = oui;
	}

	public String getCmdId(){
		return cmdId;
	}
	
	public void setCmdId(String cmdId){
		this.cmdId = cmdId;
	}
	
	public String getResult(){
		return result;
	}
	
	public void setResult(String result){
		this.result = result;
	}
	
	public String getResultDesc(){
		return resultDesc;
	}
	
	public void setResultDesc(String resultDesc){
		this.resultDesc = resultDesc;
	}
	
	public String getDevSn(){
		return devSn;
	}
	
	public void setDevSn(String devSn){
		this.devSn = devSn;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(String hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	
	
	
}
