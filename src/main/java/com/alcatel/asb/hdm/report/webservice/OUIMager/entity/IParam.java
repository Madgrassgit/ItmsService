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
public class IParam implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7202322521289557232L;
	private String operation_type;
	private String oui;
	private String device_model;
	private String vendor_name;
	private String name;
	
	public String getOperation_type()
	{
		return operation_type;
	}
	
	public void setOperation_type(String operation_type)
	{
		this.operation_type = operation_type;
	}
	
	public String getOui()
	{
		return oui;
	}
	
	public void setOui(String oui)
	{
		this.oui = oui;
	}
	
	public String getDevice_model()
	{
		return device_model;
	}
	
	public void setDevice_model(String device_model)
	{
		this.device_model = device_model;
	}
	
	public String getVendor_name()
	{
		return vendor_name;
	}
	
	public void setVendor_name(String vendor_name)
	{
		this.vendor_name = vendor_name;
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
		return "IParam [operation_type=" + operation_type + ", oui=" + oui
				+ ", device_model=" + device_model + ", vendor_name=" + vendor_name
				+ ", name=" + name + "]";
	}
}
