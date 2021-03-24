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
public class Request implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String interface_name;
	private IParam interface_param;
	
	public String getInterface_name()
	{
		return interface_name;
	}
	
	public void setInterface_name(String interface_name)
	{
		this.interface_name = interface_name;
	}

	public IParam getInterface_param()
	{
		return interface_param;
	}

	public void setInterface_param(IParam interface_param)
	{
		this.interface_param = interface_param;
	}

	@Override
	public String toString()
	{
		return "Request [interface_name=" + interface_name + ", interface_param="
				+ interface_param + "]";
	}
}
