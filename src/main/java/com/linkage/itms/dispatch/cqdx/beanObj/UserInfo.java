package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;


/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class UserInfo  implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//用户loid
	private String logic_id;
	//用户宽带账号
    private String ppp_usename;
    //终端SN
    private String serial_number;
    //用户宽带密码
    private String ppp_password;
    //客户号
    private String customer_id;
    //用户VOIP账号
    private String auth_username;
    
    @Override
	public String toString() {
		return "Order [logic_id=" + logic_id + ", ppp_usename=" + ppp_usename 
				+ ", serial_number=" + serial_number + ", ppp_password=" + ppp_password
				+  ", customer_id=" + customer_id + ", auth_username=" + auth_username
				+ "]";
	}

	
	public String getLogic_id()
	{
		return logic_id;
	}

	
	public void setLogic_id(String logic_id)
	{
		this.logic_id = logic_id;
	}

	
	public String getPpp_usename()
	{
		return ppp_usename;
	}

	
	public void setPpp_usename(String ppp_usename)
	{
		this.ppp_usename = ppp_usename;
	}

	
	public String getSerial_number()
	{
		return serial_number;
	}

	
	public void setSerial_number(String serial_number)
	{
		this.serial_number = serial_number;
	}

	
	public String getPpp_password()
	{
		return ppp_password;
	}

	
	public void setPpp_password(String ppp_password)
	{
		this.ppp_password = ppp_password;
	}

	
	public String getCustomer_id()
	{
		return customer_id;
	}

	
	public void setCustomer_id(String customer_id)
	{
		this.customer_id = customer_id;
	}

	
	public String getAuth_username()
	{
		return auth_username;
	}

	
	public void setAuth_username(String auth_username)
	{
		this.auth_username = auth_username;
	}
    
    
}
