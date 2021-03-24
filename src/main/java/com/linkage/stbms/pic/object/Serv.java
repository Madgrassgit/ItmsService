package com.linkage.stbms.pic.object;

import java.util.ArrayList;
import java.util.List;

import com.linkage.commons.xml.Bean2XML;
import com.linkage.commons.xml.XML2Bean;


/**
 * 
 * @author 王森博(66168) Tel:
 * @version 1.0
 * @since Sep 10, 2013 10:17:48 AM
 * @category com.linkage.litms.preprocess.object
 * @copyright 南京联创科技 网管科技部
 *
 */
public class Serv
{
	private String userId;
	private String deviceId;
	private String serviceId;
	private String oui;
	private String deviceSn;

	public String getUserId()
	{
		return userId;
	}
	
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	
	public String getDeviceId()
	{
		return deviceId;
	}
	
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}
	
	public String getServiceId()
	{
		return serviceId;
	}
	
	public void setServiceId(String serviceId)
	{
		this.serviceId = serviceId;
	}
	
	public String getOui()
	{
		return oui;
	}
	
	public void setOui(String oui)
	{
		this.oui = oui;
	}
	
	public String getDeviceSn()
	{
		return deviceSn;
	}
	
	public void setDeviceSn(String deviceSn)
	{
		this.deviceSn = deviceSn;
	}
}
