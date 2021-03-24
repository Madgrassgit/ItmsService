package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryBindInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	
	private String hardwareVersion;
	private String ipAddress;
	private int result;
	private String loId;
	private String pppoe;
	private String serialNumber;
	private String softwareVersion;
	private String terminalName;
	private String terminalType;
	private String workId;
    
    @Override
	public String toString() {
		return "Order [hardwareVersion=" + hardwareVersion + ", ipAddress=" + ipAddress + ", result=" + result 
				+ ", loId=" + loId + ", pppoe=" + pppoe+ ", serialNumber=" + serialNumber + ", softwareVersion=" + softwareVersion 
				+ ", terminalName=" + terminalName + ", terminalType=" + terminalType + ", workId=" + workId + "]";
	}

	
	public String getHardwareVersion()
	{
		return hardwareVersion;
	}

	
	public void setHardwareVersion(String hardwareVersion)
	{
		this.hardwareVersion = hardwareVersion;
	}

	
	public String getIpAddress()
	{
		return ipAddress;
	}

	
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	
	public int getResult()
	{
		return result;
	}

	
	public void setResult(int result)
	{
		this.result = result;
	}

	
	public String getLoId()
	{
		return loId;
	}

	
	public void setLoId(String loId)
	{
		this.loId = loId;
	}

	
	public String getPppoe()
	{
		return pppoe;
	}

	
	public void setPppoe(String pppoe)
	{
		this.pppoe = pppoe;
	}

	
	public String getSerialNumber()
	{
		return serialNumber;
	}

	
	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	
	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	
	public void setSoftwareVersion(String softwareVersion)
	{
		this.softwareVersion = softwareVersion;
	}

	
	public String getTerminalName()
	{
		return terminalName;
	}

	
	public void setTerminalName(String terminalName)
	{
		this.terminalName = terminalName;
	}

	
	public String getTerminalType()
	{
		return terminalType;
	}

	
	public void setTerminalType(String terminalType)
	{
		this.terminalType = terminalType;
	}

	
	public String getWorkId()
	{
		return workId;
	}

	
	public void setWorkId(String workId)
	{
		this.workId = workId;
	}
    
}