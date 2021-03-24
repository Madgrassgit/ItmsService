
package com.linkage.itms.rms.obj;

import java.util.Date;

/**
 * InternetGatewayDevice.Services.VoiceService.i.VoiceProfile.i.Line.i.
 * 
 * @author gongsj
 * @date 2009-6-17
 */
public class VoiceServiceProfileLineObj
{

	/**
	 * 设备ID
	 */
	String deviceId = null;
	/**
	 * VOIP ID
	 */
	String voipId = null;
	/**
	 * Profile ID
	 */
	String profId = null;
	/**
	 * LINE ID
	 */
	String lineId = null;
	/**
	 * 采集时间
	 */
	String gatherTime = null;
	/**
	 * 开关
	 */
	String enable = null;
	/**
	 * 连接状态
	 */
	String status = null;
	/**
	 * 认证用户名
	 */
	String username = null;
	/**
	 * 认证用户名
	 */
	String password = null;
	/**
	 * 注册状态
	 */
	String registResult = null;
	/**
	 * 物理标识
	 * */
	String physicalTermId = null; 
	private String ip;

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getVoipId()
	{
		return voipId;
	}

	public void setVoipId(String voipId)
	{
		this.voipId = voipId;
	}

	public String getProfId()
	{
		return profId;
	}

	public void setProfId(String profId)
	{
		this.profId = profId;
	}

	public String getLineId()
	{
		return lineId;
	}

	public void setLineId(String lineId)
	{
		this.lineId = lineId;
	}

	public String getGatherTime()
	{
		if (gatherTime == null || gatherTime.length() == 0)
		{
			gatherTime = "" + (new Date()).getTime() / 1000;
		}
		return gatherTime;
	}

	public void setGatherTime(String gatherTime)
	{
		this.gatherTime = gatherTime;
	}

	public String getEnable()
	{
		return enable;
	}

	public void setEnable(String enable)
	{
		this.enable = enable;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getRegistResult()
	{
		return registResult;
	}

	public void setRegistResult(String registResult)
	{
		this.registResult = registResult;
	}

	public String getPhysicalTermId() {
		return physicalTermId;
	}

	public void setPhysicalTermId(String physicalTermId) {
		this.physicalTermId = physicalTermId;
	}
	
}
