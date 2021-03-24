package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class Loid_resultType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//用户loid
	private String loid;
    //终端SN
    private String serial_number;
    //用户宽带密码
    private int service_status;
    //客户号
    private String service_list;
    //用户VOIP账号
    private String error_msg;
    
    @Override
	public String toString() {
		return "Order [loid=" + loid + ", serial_number=" + serial_number + ", service_status=" + service_status
				+  ", service_list=" + service_list + ", error_msg=" + error_msg + "]";
	}

	
	public String getLoid()
	{
		return loid;
	}
	
	public void setLoid(String loid)
	{
		this.loid = loid;
	}


	public String getSerial_number()
	{
		return serial_number;
	}

	
	public void setSerial_number(String serial_number)
	{
		this.serial_number = serial_number;
	}

	
	public int getService_status()
	{
		return service_status;
	}

	
	public void setService_status(int service_status)
	{
		this.service_status = service_status;
	}

	
	public String getService_list()
	{
		return service_list;
	}

	
	public void setService_list(String service_list)
	{
		this.service_list = service_list;
	}

	
	public String getError_msg()
	{
		return error_msg;
	}

	
	public void setError_msg(String error_msg)
	{
		this.error_msg = error_msg;
	}
    
}
