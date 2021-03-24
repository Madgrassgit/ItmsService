package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryTerminalInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//厂家
	private String manufactory;
	//型号
	private String device_type;
	//版本
	private String version;
	//OUI
	private String OUI;
	//序列号
	private String serial_number;
	//终端接入方式
	private String terminal_type;
	//终端状态
	private String is_online;
    
    @Override
	public String toString() {
		return "Order [manufactory=" + manufactory + ", device_type=" + device_type + ", version=" + version 
				+ ", OUI=" + OUI + ", serial_number=" + serial_number+ ", terminal_type=" + terminal_type + ", is_online=" + is_online 
				+ "]";
	}

	
	public String getManufactory()
	{
		return manufactory;
	}

	
	public void setManufactory(String manufactory)
	{
		this.manufactory = manufactory;
	}

	
	public String getDevice_type()
	{
		return device_type;
	}

	
	public void setDevice_type(String device_type)
	{
		this.device_type = device_type;
	}

	
	public String getVersion()
	{
		return version;
	}

	
	public void setVersion(String version)
	{
		this.version = version;
	}

	
	public String getOUI()
	{
		return OUI;
	}

	
	public void setOUI(String oUI)
	{
		OUI = oUI;
	}

	
	public String getSerial_number()
	{
		return serial_number;
	}

	
	public void setSerial_number(String serial_number)
	{
		this.serial_number = serial_number;
	}

	
	public String getTerminal_type()
	{
		return terminal_type;
	}

	
	public void setTerminal_type(String terminal_type)
	{
		this.terminal_type = terminal_type;
	}

	
	public String getIs_online()
	{
		return is_online;
	}

	
	public void setIs_online(String is_online)
	{
		this.is_online = is_online;
	}
    
}