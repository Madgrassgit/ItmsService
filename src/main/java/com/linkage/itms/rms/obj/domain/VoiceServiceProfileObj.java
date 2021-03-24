
package com.linkage.itms.rms.obj.domain;

import java.util.Date;

/**
 * InternetGatewayDevice.Services.VoiceService.i.VoiceProfile.i.SIP.
 * 
 * @author gongsj
 * @date 2009-6-17
 */
public class VoiceServiceProfileObj
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
	 * 采集时间
	 */
	String gatherTime = null;
	/**
	 * 主地址
	 */
	private String proxServ = null;
	/**
	 * 主端口
	 */
	private String proxPort = null;
	/**
	 * 备用地址
	 */
	private String proxServ2 = null;
	/**
	 * 备用端口
	 */
	private String proxPort2 = null;
	/**
	 * 注册地址
	 */
	private String regiServ = null;
	/**
	 * 注册端口
	 */
	private String regiPort = null;
	/**
	 * 标准注册地址
	 */
	private String standRegiServ = null;
	/**
	 * 标准注册端口
	 */
	private String standRegiPort = null;
	/**
	 * 外部绑定地址
	 */
	private String outBoundProxy = null;
	/**
	 * 外部绑定端口
	 */
	private String outBoundPort = null;
	/**
	 * 标准绑定地址
	 */
	private String standOutBoundProxy = null;
	/**
	 * 标准绑定端口
	 */
	private String standOutBoundPort = null;
	// 局向ID
	private String officeId;
	//
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

	public String getProxServ()
	{
		return proxServ;
	}

	public void setProxServ(String proxServ)
	{
		this.proxServ = proxServ;
	}

	public String getProxPort()
	{
		return proxPort;
	}

	public void setProxPort(String proxPort)
	{
		this.proxPort = proxPort;
	}

	public String getProxServ2()
	{
		return proxServ2;
	}

	public void setProxServ2(String proxServ2)
	{
		this.proxServ2 = proxServ2;
	}

	public String getProxPort2()
	{
		return proxPort2;
	}

	public void setProxPort2(String proxPort2)
	{
		this.proxPort2 = proxPort2;
	}

	/**
	 * @return the outBoundPort
	 */
	public String getOutBoundPort()
	{
		return outBoundPort;
	}

	/**
	 * @param outBoundPort
	 *            the outBoundPort to set
	 */
	public void setOutBoundPort(String outBoundPort)
	{
		this.outBoundPort = outBoundPort;
	}

	/**
	 * @return the regiPort
	 */
	public String getRegiPort()
	{
		return regiPort;
	}

	/**
	 * @param regiPort
	 *            the regiPort to set
	 */
	public void setRegiPort(String regiPort)
	{
		this.regiPort = regiPort;
	}

	/**
	 * @return the regiServ
	 */
	public String getRegiServ()
	{
		return regiServ;
	}

	/**
	 * @param regiServ
	 *            the regiServ to set
	 */
	public void setRegiServ(String regiServ)
	{
		this.regiServ = regiServ;
	}

	/**
	 * @return the standOutBoundPort
	 */
	public String getStandOutBoundPort()
	{
		return standOutBoundPort;
	}

	/**
	 * @param standOutBoundPort
	 *            the standOutBoundPort to set
	 */
	public void setStandOutBoundPort(String standOutBoundPort)
	{
		this.standOutBoundPort = standOutBoundPort;
	}

	/**
	 * @return the standOutBoundProxy
	 */
	public String getStandOutBoundProxy()
	{
		return standOutBoundProxy;
	}

	/**
	 * @param standOutBoundProxy
	 *            the standOutBoundProxy to set
	 */
	public void setStandOutBoundProxy(String standOutBoundProxy)
	{
		this.standOutBoundProxy = standOutBoundProxy;
	}

	/**
	 * @return the standRegiPort
	 */
	public String getStandRegiPort()
	{
		return standRegiPort;
	}

	/**
	 * @param standRegiPort
	 *            the standRegiPort to set
	 */
	public void setStandRegiPort(String standRegiPort)
	{
		this.standRegiPort = standRegiPort;
	}

	/**
	 * @return the standRegiServ
	 */
	public String getStandRegiServ()
	{
		return standRegiServ;
	}

	/**
	 * @param standRegiServ
	 *            the standRegiServ to set
	 */
	public void setStandRegiServ(String standRegiServ)
	{
		this.standRegiServ = standRegiServ;
	}

	/**
	 * @return the utBoundProxy
	 */
	public String getOutBoundProxy()
	{
		return outBoundProxy;
	}

	/**
	 * @param utBoundProxy
	 *            the utBoundProxy to set
	 */
	public void setOutBoundProxy(String outBoundProxy)
	{
		this.outBoundProxy = outBoundProxy;
	}

	public String getOfficeId()
	{
		return officeId;
	}

	public void setOfficeId(String officeId)
	{
		this.officeId = officeId;
	}
}
