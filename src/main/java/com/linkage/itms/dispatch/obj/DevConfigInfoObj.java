
package com.linkage.itms.dispatch.obj;

/**
 * @author chenzhangjian (Ailk No.)
 * @version 1.0
 * @since 2015-6-24
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DevConfigInfoObj
{

	private String sSIDname;
	private String devNumber;
	private String connectType;
	private String connectStatus;
	private String iPAddress;
	private String dNSServer;
	private String vlan;
	private String bindPort;
	private String pPPoE;
	private String ponStat;
	private String tXPower;
	private String rXPower;
	private String lanName;
	private String linkRate;
	private String linkStats;
	
	public String getLanName()
	{
		return lanName;
	}

	
	public void setLanName(String lanName)
	{
		this.lanName = lanName;
	}

	
	public String getLinkRate()
	{
		return linkRate;
	}

	
	public void setLinkRate(String linkRate)
	{
		this.linkRate = linkRate;
	}

	
	public String getLinkStats()
	{
		return linkStats;
	}

	
	public void setLinkStats(String linkStats)
	{
		this.linkStats = linkStats;
	}

	public String getsSIDname()
	{
		return sSIDname;
	}

	public void setsSIDname(String sSIDname)
	{
		this.sSIDname = sSIDname;
	}

	public String getDevNumber()
	{
		return devNumber;
	}

	public void setDevNumber(String devNumber)
	{
		this.devNumber = devNumber;
	}

	public String getConnectType()
	{
		return connectType;
	}

	public void setConnectType(String connectType)
	{
		this.connectType = connectType;
	}

	public String getConnectStatus()
	{
		return connectStatus;
	}

	public void setConnectStatus(String connectStatus)
	{
		this.connectStatus = connectStatus;
	}

	public String getiPAddress()
	{
		return iPAddress;
	}

	public void setiPAddress(String iPAddress)
	{
		this.iPAddress = iPAddress;
	}

	public String getdNSServer()
	{
		return dNSServer;
	}

	public void setdNSServer(String dNSServer)
	{
		this.dNSServer = dNSServer;
	}

	public String getVlan()
	{
		return vlan;
	}

	public void setVlan(String vlan)
	{
		this.vlan = vlan;
	}

	public String getBindPort()
	{
		return bindPort;
	}

	public void setBindPort(String bindPort)
	{
		this.bindPort = bindPort;
	}

	public String getpPPoE()
	{
		return pPPoE;
	}

	public void setpPPoE(String pPPoE)
	{
		this.pPPoE = pPPoE;
	}

	public String getPonStat()
	{
		return ponStat;
	}

	public void setPonStat(String ponStat)
	{
		this.ponStat = ponStat;
	}

	public String gettXPower()
	{
		return tXPower;
	}

	public void settXPower(String tXPower)
	{
		this.tXPower = tXPower;
	}

	public String getrXPower()
	{
		return rXPower;
	}

	public void setrXPower(String rXPower)
	{
		this.rXPower = rXPower;
	}
}
