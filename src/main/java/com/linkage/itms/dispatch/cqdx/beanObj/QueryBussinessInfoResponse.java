package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryBussinessInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//逻辑ID
	private String loId;
	//宽带帐号
	private String pppoe;
	//设备序列号
	private String serial_number;
	//操作结果
	private int result;
	//已开通业务的列表
	private String serviceList;
	//终端类型
	private String terminalType;
	//操作流水号
	private String workId;
    
    @Override
	public String toString() {
		return "Order [loId=" + loId + ", pppoe=" + pppoe + ", serial_number=" + serial_number 
				+ ", result=" + result + ", serviceList=" + serviceList+ ", terminalType=" + terminalType + ", workId=" + workId 
				+ "]";
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

	
	public String getSerial_number()
	{
		return serial_number;
	}

	
	public void setSerial_number(String serial_number)
	{
		this.serial_number = serial_number;
	}

	
	public int getResult()
	{
		return result;
	}

	
	public void setResult(int result)
	{
		this.result = result;
	}

	
	public String getServiceList()
	{
		return serviceList;
	}

	
	public void setServiceList(String serviceList)
	{
		this.serviceList = serviceList;
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