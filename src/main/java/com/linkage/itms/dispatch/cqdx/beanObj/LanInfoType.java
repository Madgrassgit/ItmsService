package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class LanInfoType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//成功次数
	private String name;
	//失败次数
    private String connection_rate;
    //平均响应时间
    private String status;
    //最小响应时间
    private String accept_byte_count;
    //最大响应时间
    private String send_byte_count;
    //最小响应时间
    private String accept_package_count;
    //最大响应时间
    private String send_package_count;
    
    @Override
	public String toString() {
		return "Order [name=" + name + ", connection_rate=" + connection_rate
				+ ", status=" + status+ ", accept_byte_count=" + accept_byte_count+ ", send_byte_count=" + send_byte_count
				+ ", accept_package_count=" + accept_package_count+ ", send_package_count=" + send_package_count + "]";
	}

	
	public String getName()
	{
		return name;
	}

	
	public void setName(String name)
	{
		this.name = name;
	}

	
	public String getConnection_rate()
	{
		return connection_rate;
	}

	
	public void setConnection_rate(String connection_rate)
	{
		this.connection_rate = connection_rate;
	}

	
	public String getStatus()
	{
		return status;
	}

	
	public void setStatus(String status)
	{
		this.status = status;
	}

	
	public String getAccept_byte_count()
	{
		return accept_byte_count;
	}

	
	public void setAccept_byte_count(String accept_byte_count)
	{
		this.accept_byte_count = accept_byte_count;
	}

	
	public String getSend_byte_count()
	{
		return send_byte_count;
	}

	
	public void setSend_byte_count(String send_byte_count)
	{
		this.send_byte_count = send_byte_count;
	}

	
	public String getAccept_package_count()
	{
		return accept_package_count;
	}

	
	public void setAccept_package_count(String accept_package_count)
	{
		this.accept_package_count = accept_package_count;
	}

	
	public String getSend_package_count()
	{
		return send_package_count;
	}

	
	public void setSend_package_count(String send_package_count)
	{
		this.send_package_count = send_package_count;
	}
    
}