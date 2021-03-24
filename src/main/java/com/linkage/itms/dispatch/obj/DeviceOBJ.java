/**
 * 
 */
package com.linkage.itms.dispatch.obj;

/**
 * @author liyl
 * @version 1.0
 * @since 2016-6-18
 */
public class DeviceOBJ {

	private long taskId;//任务id
	private String devId;//设备id
	private String cityId;//设备属地
	private long userId;//用户id
	private String username;//业务账号
	private String deviceSN;//设备序列号
	private int testType;//测试类型   宽带10或iptv11
	private int processTimes;
	
	public String getDevId() {
		return devId;
	}
	public void setDevId(String devId) {
		this.devId = devId;
	}
	
	public String getDeviceSN()
	{
		return deviceSN;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	public void setDeviceSN(String deviceSN)
	{
		this.deviceSN = deviceSN;
	}
	
	
	public int getTestType()
	{
		return testType;
	}
	
	public void setTestType(int testType)
	{
		this.testType = testType;
	}
	public int getProcessTimes()
	{
		return processTimes;
	}
	
	public void setProcessTimes(int processTimes)
	{
		this.processTimes = processTimes;
	}
	
	public long getTaskId()
	{
		return taskId;
	}
	
	public void setTaskId(long taskId)
	{
		this.taskId = taskId;
	}
	
	public long getUserId()
	{
		return userId;
	}
	
	public void setUserId(long userId)
	{
		this.userId = userId;
	}
	
	public String getCityId()
	{
		return cityId;
	}
	
	public void setCityId(String cityId)
	{
		this.cityId = cityId;
	}
	
}
