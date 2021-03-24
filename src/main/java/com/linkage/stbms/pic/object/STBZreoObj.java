
package com.linkage.stbms.pic.object;

/**
 * 零配置业务下发
 * 
 * @author 王森博
 */
public class STBZreoObj
{

	private String deviceId;
	private String authURL;
	private String servAccount;
	private String servPassword;
	private String PPPoEID;
	private String PPPoEPassword;
	private String AddressingType;
	private boolean needReboot = true;
	private String DHCPUsername;
	private String DHCPPassword;
	private String enable = "1";
	private String IPAddress;
	private String SubnetMask;
	private String DefaultGateway;
	private String DNSServers;

	
	public String getServAccount()
	{
		return servAccount;
	}

	
	public void setServAccount(String servAccount)
	{
		this.servAccount = servAccount;
	}

	
	public String getServPassword()
	{
		return servPassword;
	}

	
	public void setServPassword(String servPassword)
	{
		this.servPassword = servPassword;
	}

	
	public String getPPPoEID()
	{
		return PPPoEID;
	}

	
	public void setPPPoEID(String poEID)
	{
		PPPoEID = poEID;
	}

	
	public String getPPPoEPassword()
	{
		return PPPoEPassword;
	}

	
	public void setPPPoEPassword(String poEPassword)
	{
		PPPoEPassword = poEPassword;
	}

	
	public String getAddressingType()
	{
		return AddressingType;
	}

	
	public void setAddressingType(String addressingType)
	{
		AddressingType = addressingType;
	}

	
	public boolean isNeedReboot()
	{
		return needReboot;
	}

	
	public void setNeedReboot(boolean needReboot)
	{
		this.needReboot = needReboot;
	}

	
	public String getDHCPUsername()
	{
		return DHCPUsername;
	}

	
	public void setDHCPUsername(String username)
	{
		DHCPUsername = username;
	}

	
	public String getDHCPPassword()
	{
		return DHCPPassword;
	}

	
	public void setDHCPPassword(String password)
	{
		DHCPPassword = password;
	}

	
	public String getEnable()
	{
		return enable;
	}

	
	public void setEnable(String enable)
	{
		this.enable = enable;
	}

	
	public String getIPAddress()
	{
		return IPAddress;
	}

	
	public void setIPAddress(String address)
	{
		IPAddress = address;
	}

	
	public String getSubnetMask()
	{
		return SubnetMask;
	}

	
	public void setSubnetMask(String subnetMask)
	{
		SubnetMask = subnetMask;
	}

	
	public String getDefaultGateway()
	{
		return DefaultGateway;
	}

	
	public void setDefaultGateway(String defaultGateway)
	{
		DefaultGateway = defaultGateway;
	}

	
	public String getDNSServers()
	{
		return DNSServers;
	}

	
	public void setDNSServers(String servers)
	{
		DNSServers = servers;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getAuthURL()
	{
		return authURL;
	}

	public void setAuthURL(String authURL)
	{
		this.authURL = authURL;
	}
}
