package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class PingPara implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//用户loid
	private String ping_host;
	//用户宽带账号
    private String ping_times;
    //客户号
    private String block_size;
    
    @Override
	public String toString() {
		return "Order [ping_host=" + ping_host + ", ping_times=" + ping_times
				+ ", block_size=" + block_size + "]";
	}

	
	public String getPing_host()
	{
		return ping_host;
	}

	
	public void setPing_host(String ping_host)
	{
		this.ping_host = ping_host;
	}

	
	public String getPing_times()
	{
		return ping_times;
	}

	
	public void setPing_times(String ping_times)
	{
		this.ping_times = ping_times;
	}

	
	public String getBlock_size()
	{
		return block_size;
	}

	public void setBlock_size(String block_size)
	{
		this.block_size = block_size;
	}
    
}