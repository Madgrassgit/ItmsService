package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryConnectionInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//结果值
	private int result;
	//结果说明
	private String err_msg;
	//拨号连接状态
	private String dial_connect_status;
	//DNS信息
	private String dns;
	//发送光功率
	private String send_power;
	//接收光功率
	private String receive_power;
	//DHCP信息
//	private DHCPAddressType[] address;
	
	private DHCPType dhcp;
    
    @Override
	public String toString() {
		return "Order [result=" + result + ", err_msg=" + err_msg + ", dial_connect_status=" + dial_connect_status 
				+ ", dns=" + dns + ", send_power=" + send_power+ ", receive_power=" + receive_power + ", dhcp=[" + dhcp + "]]";
	}

	
	public int getResult()
	{
		return result;
	}

	
	public void setResult(int result)
	{
		this.result = result;
	}

	
	public String getErr_msg()
	{
		return err_msg;
	}

	
	public void setErr_msg(String err_msg)
	{
		this.err_msg = err_msg;
	}

	
	public String getDial_connect_status()
	{
		return dial_connect_status;
	}

	
	public void setDial_connect_status(String dial_connect_status)
	{
		this.dial_connect_status = dial_connect_status;
	}

	
	public String getDns()
	{
		return dns;
	}

	
	public void setDns(String dns)
	{
		this.dns = dns;
	}

	
	public String getSend_power()
	{
		return send_power;
	}

	
	public void setSend_power(String send_power)
	{
		this.send_power = send_power;
	}

	
	public String getReceive_power()
	{
		return receive_power;
	}

	
	public void setReceive_power(String receive_power)
	{
		this.receive_power = receive_power;
	}


	
	public DHCPType getDhcp()
	{
		return dhcp;
	}


	
	public void setDhcp(DHCPType dhcp)
	{
		this.dhcp = dhcp;
	}
	
//	public DHCPAddressType[] getAddress()
//	{
//		return address;
//	}
//
//	public void setAddress(DHCPAddressType[] address)
//	{
//		this.address = address;
//	}
	
}