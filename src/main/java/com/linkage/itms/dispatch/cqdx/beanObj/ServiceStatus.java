package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class ServiceStatus implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//宽带业务开通状态
	private int wband_status;
	//Iptv业务开通状态
    private int iptv_status;
    //Voip1口开通状态
    private int voip1_status;
    //Voip2口开通状态
    private int voip2_status;
    
    @Override
	public String toString() {
		return "Order [wband_status=" + wband_status + ", iptv_status=" + iptv_status
				+ ", voip1_status=" + voip1_status+ ", voip2_status=" + voip2_status + "]";
	}

	
	public int getWband_status()
	{
		return wband_status;
	}

	
	public void setWband_status(int wband_status)
	{
		this.wband_status = wband_status;
	}

	
	public int getIptv_status()
	{
		return iptv_status;
	}

	
	public void setIptv_status(int iptv_status)
	{
		this.iptv_status = iptv_status;
	}

	
	public int getVoip1_status()
	{
		return voip1_status;
	}

	
	public void setVoip1_status(int voip1_status)
	{
		this.voip1_status = voip1_status;
	}

	
	public int getVoip2_status()
	{
		return voip2_status;
	}

	
	public void setVoip2_status(int voip2_status)
	{
		this.voip2_status = voip2_status;
	}
    
}