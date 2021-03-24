package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class DHCPAddressType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //DHCP信息：IP地址
    private String ip_address;
    //DHCP信息：MAC地址
    private String mac_address;
    
    @Override
	public String toString() {
		return "Order [ip_address=" + ip_address + ",mac_address=" + mac_address + "]";
	}

	
	public String getIp_address()
	{
		return ip_address;
	}

	
	public void setIp_address(String ip_address)
	{
		this.ip_address = ip_address;
	}

	
	public String getMac_address()
	{
		return mac_address;
	}

	
	public void setMac_address(String mac_address)
	{
		this.mac_address = mac_address;
	}
    
}
