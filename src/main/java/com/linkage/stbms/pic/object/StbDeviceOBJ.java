package com.linkage.stbms.pic.object;


/**
 * @author zhangsm 
 * @version 1.0
 * @since 2011-12-6 下午02:17:19
 * @category com.linkage.bl.obj<br>
 * @copyright 亚信联创 网管产品部
 */
public class StbDeviceOBJ
{
	/*机顶盒状态
	0，新机顶盒，需要下发配置，
	1，机顶盒内配置正常的老机顶盒，不需下发配置，
	2，老机顶盒维修，需要下发配置，
	3，特殊机顶盒，不需要下发配置，
	4，软件版本升级失败机顶盒，不能下发配置，(重复三次)
	5，需要进行串号配置的机顶盒
	6，零配置流程失败机顶盒，需要现场装维人员手工配置。
	7，机顶盒移机，装维人员进入综合网管触发改A=7，radius重新绑定一下，机顶盒下配置。（bss工单提供产品id，唯一标示同个用户）
	8，机顶盒销户。
	9，正在自动配置中的机顶盒。
	**/
	private int status;
	private String deviceId = null;
	/** oui */
	private String oui = null;
	/** serialnumber */
	private String sn = null;
	private String cityId = null;
	/** user_id */
	private long userId = -1;
	//串号
	private String zeroCount;
	//是否零配置版本
	private int isZeroCfgVersion;
	private String vendorId;
	private String deviceModelId;
	private String stbMac;
	private long deviceTypeId;
	/**注册时间，设备首次上报时间*/
	private long completetime = 0;
	/**
	 * 是否是新设备：1是； 0否
	 */
	private int stbIsNew;
	/**
	 * 设备的IP地址
	 */
	private String ipAddress;
	
	/** 机顶盒与用户关联状态  1：绑定  0：未绑定 */
	private String cpe_allocatedstatus;
	
	/** 硬件版本 */
	private String hardwareversion ;
	/** 软件版本 */
	private String softwareversion;
	/** 业务账号*/
	private String servAccount;
	
	/** 绑定状态*/
	private int bindState;
	
	/** 绑定方式*/
	private int bindWay;
	
	/** 设备序列号*/
	private String deviceSerialnumber;
	
	/**绑定时间/失败时间*/
	private long currTime;
	
	/**
	 * 设备绑定记录流水
	 */
	private StbZeroconfigFailObj stbZeroconfigFailObj;
	
	
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


	public String getSn() {
		return sn;
	}


	public void setSn(String sn) {
		this.sn = sn;
	}

	
	public String getCityId() {
		return cityId;
	}
	
	

	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public void setCityId(String cityId) {
		this.cityId = cityId;
	}


	public String getServAccount() {
		return servAccount;
	}


	public void setServAccount(String servAccount) {
		this.servAccount = servAccount;
	}


	public String getVendorId()
	{
		return vendorId;
	}

	
	public void setVendorId(String vendorId)
	{
		this.vendorId = vendorId;
	}

	
	public String getDeviceModelId()
	{
		return deviceModelId;
	}

	
	public void setDeviceModelId(String deviceModelId)
	{
		this.deviceModelId = deviceModelId;
	}

	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String getZeroCount()
	{
		return zeroCount;
	}
	
	public void setZeroCount(String zeroCount)
	{
		this.zeroCount = zeroCount;
	}

	
	public int getIsZeroCfgVersion()
	{
		return isZeroCfgVersion;
	}

	
	public void setIsZeroCfgVersion(int isZeroCfgVersion)
	{
		this.isZeroCfgVersion = isZeroCfgVersion;
	}


	
	public String getStbMac()
	{
		return stbMac;
	}


	
	public void setStbMac(String stbMac)
	{
		this.stbMac = stbMac;
	}


	
	public long getDeviceTypeId()
	{
		return deviceTypeId;
	}

	
	public void setDeviceTypeId(long deviceTypeId)
	{
		this.deviceTypeId = deviceTypeId;
	}


	public String getIpAddress()
	{
		return ipAddress;
	}


	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}


	
	public String getCpe_allocatedstatus() {
		return cpe_allocatedstatus;
	}


	
	public void setCpe_allocatedstatus(String cpe_allocatedstatus) {
		this.cpe_allocatedstatus = cpe_allocatedstatus;
	}

	
	public String getHardwareversion() {
		return hardwareversion;
	}

	
	public void setHardwareversion(String hardwareversion) {
		this.hardwareversion = hardwareversion;
	}

	
	public String getSoftwareversion() {
		return softwareversion;
	}


	
	public void setSoftwareversion(String softwareversion) {
		this.softwareversion = softwareversion;
	}


	public int getBindState() {
		return bindState;
	}


	public void setBindState(int bindState) {
		this.bindState = bindState;
	}


	public int getBindWay() {
		return bindWay;
	}


	public void setBindWay(int bindWay) {
		this.bindWay = bindWay;
	}


	public String getDeviceSerialnumber() {
		return deviceSerialnumber;
	}


	public void setDeviceSerialnumber(String deviceSerialnumber) {
		this.deviceSerialnumber = deviceSerialnumber;
	}


	public long getCurrTime() {
		return currTime;
	}


	public void setCurrTime(long currTime) {
		this.currTime = currTime;
	}


	public StbZeroconfigFailObj getStbZeroconfigFailObj() {
		return stbZeroconfigFailObj;
	}


	public void setStbZeroconfigFailObj(StbZeroconfigFailObj stbZeroconfigFailObj) {
		this.stbZeroconfigFailObj = stbZeroconfigFailObj;
	}


	public long getCompletetime() {
		return completetime;
	}


	public void setCompletetime(long completetime) {
		this.completetime = completetime;
	}


	public int getStbIsNew() {
		return stbIsNew;
	}


	public void setStbIsNew(int stbIsNew) {
		this.stbIsNew = stbIsNew;
	}
	
}
