package com.linkage.itms.dispatch.cqdx.obj;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class RouteHopsOBJ
{
	//跳转主机
	private String hopHost;
	//跳转地址
	private String hopHostAddress;
	//错误码
	private String hopErrorCode;
	//次数
	private String hopRTTimes;
	
	public String getHopHost()
	{
		return hopHost;
	}
	
	public void setHopHost(String hopHost)
	{
		this.hopHost = hopHost;
	}
	
	public String getHopHostAddress()
	{
		return hopHostAddress;
	}
	
	public void setHopHostAddress(String hopHostAddress)
	{
		this.hopHostAddress = hopHostAddress;
	}
	
	public String getHopErrorCode()
	{
		return hopErrorCode;
	}
	
	public void setHopErrorCode(String hopErrorCode)
	{
		this.hopErrorCode = hopErrorCode;
	}
	
	public String getHopRTTimes()
	{
		return hopRTTimes;
	}
	
	public void setHopRTTimes(String hopRTTimes)
	{
		this.hopRTTimes = hopRTTimes;
	}
	
	
}
