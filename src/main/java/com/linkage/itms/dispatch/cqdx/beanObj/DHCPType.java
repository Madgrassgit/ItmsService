
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class DHCPType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//DHCP 地址信息数组
    private DHCPAddressType[] address;
    
    @Override
	public String toString() {
		return "DHCPType [address=" + address + "]";
	}

	
	public DHCPAddressType[] getAddress()
	{
		return address;
	}

	
	public void setAddress(DHCPAddressType[] address)
	{
		this.address = address;
	}
    
}