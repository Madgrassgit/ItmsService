package com.linkage.itms.itv.obj;

/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-9-23 上午10:46:59
 * @category com.linkage.itms.itv.obj<br>
 * @copyright 亚信联创 网管产品部
 */
public class AccountOBJ
{
	private String userName;
	private String iptvRealBindPort;
	
	public String getUserName()
	{
		return userName;
	}

	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}


	public String getIptvRealBindPort() {
		return iptvRealBindPort;
	}


	public void setIptvRealBindPort(String iptvRealBindPort) {
		this.iptvRealBindPort = iptvRealBindPort;
	}

}