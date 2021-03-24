package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;


/**
 * 
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-5
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class UserDetail  implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//用户名称
	private String user_name;
	//用户地址
    private String user_address;
    //宽带账号
    private String ad_account;
    //终端唯一标识
    private String cpe_id;
    //终端状态
    private String cpe_status;
    //终端在线状态，实时刷新该终端的在线状态
    private String online_status;
    //版本状态，表示该终端版本在TMS1000系统中是否存在
    private String version_status;
    //终端上报的版本
    private String cpe_version;
    //无线使能状态
    private String wifi_status;
    //终端和TMS1000最后一次连接时间
    private String last_connect;
    //终端初始域名称。显示为域路径，例如广东/深圳/南山
    private String cpe_domain;
    //用户开通的业务。如果有多个业务同时开通，使用’^’作为连接字符，例如wband^iptv
    private String cpe_service;
    

	
	@Override
	public String toString()
	{
		return "UserDetail [user_name=" + user_name + ", user_address=" + user_address
				+ ", ad_account=" + ad_account + ", cpe_id=" + cpe_id + ", cpe_status="
				+ cpe_status + ", online_status=" + online_status + ", version_status="
				+ version_status + ", cpe_version=" + cpe_version + ", wifi_status="
				+ wifi_status + ", last_connect=" + last_connect + ", cpe_domain="
				+ cpe_domain + ", cpe_service=" + cpe_service + "]";
	}



	
	public String getUser_name()
	{
		return user_name;
	}



	
	public void setUser_name(String user_name)
	{
		this.user_name = user_name;
	}



	
	public String getUser_address()
	{
		return user_address;
	}



	
	public void setUser_address(String user_address)
	{
		this.user_address = user_address;
	}



	
	public String getAd_account()
	{
		return ad_account;
	}



	
	public void setAd_account(String ad_account)
	{
		this.ad_account = ad_account;
	}

	
	public String getCpe_id()
	{
		return cpe_id;
	}

	
	public void setCpe_id(String cpe_id)
	{
		this.cpe_id = cpe_id;
	}

	
	public String getCpe_status()
	{
		return cpe_status;
	}


	public void setCpe_status(String cpe_status)
	{
		this.cpe_status = cpe_status;
	}

	
	public String getOnline_status()
	{
		return online_status;
	}

	
	public void setOnline_status(String online_status)
	{
		this.online_status = online_status;
	}

	
	public String getVersion_status()
	{
		return version_status;
	}


	public void setVersion_status(String version_status)
	{
		this.version_status = version_status;
	}
	
	public String getCpe_version()
	{
		return cpe_version;
	}

	
	public void setCpe_version(String cpe_version)
	{
		this.cpe_version = cpe_version;
	}

	
	public String getWifi_status()
	{
		return wifi_status;
	}


	public void setWifi_status(String wifi_status)
	{
		this.wifi_status = wifi_status;
	}

	
	public String getLast_connect()
	{
		return last_connect;
	}

	
	public void setLast_connect(String last_connect)
	{
		this.last_connect = last_connect;
	}


	public String getCpe_domain()
	{
		return cpe_domain;
	}

	public void setCpe_domain(String cpe_domain)
	{
		this.cpe_domain = cpe_domain;
	}

	
	public String getCpe_service()
	{
		return cpe_service;
	}

	
	public void setCpe_service(String cpe_service)
	{
		this.cpe_service = cpe_service;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}
    
	
}
