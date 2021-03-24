package com.alcatel.asb.hdm.report.webservice.OUIMager.entity;

import java.io.Serializable;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年6月25日
 * @category com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class OUIRes implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3712497536629059008L;
	private String oui;
	private String vendor_name;
	private String device_model;
	private String name;
	public OUIRes(){
		
	}
	
	public OUIRes(String oui,String vendorName,String deviceModel, String name){
		this.oui = oui;
		this.vendor_name = vendorName;
		this.device_model = deviceModel;
		this.name = name;
	}
	public String getOui()
	{
		return oui;
	}
	
	public void setOui(String oui)
	{
		this.oui = oui;
	}
	
	public String getVendor_name()
	{
		return vendor_name;
	}
	
	public void setVendor_name(String vendor_name)
	{
		this.vendor_name = vendor_name;
	}
	
	public String getDevice_model()
	{
		return device_model;
	}
	
	public void setDevice_model(String device_model)
	{
		this.device_model = device_model;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "OUIRes [oui=" + oui + ", vendor_name=" + vendor_name + ", device_model="
				+ device_model + ", name=" + name + "]";
	}
    
	
}
