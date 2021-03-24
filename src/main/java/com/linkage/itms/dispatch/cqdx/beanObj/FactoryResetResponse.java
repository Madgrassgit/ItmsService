package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class FactoryResetResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//重发失败原因
	private String failedReason;
	//逻辑loid
	private String loId;
	//宽带帐号
    private String pppoe;
    //下发状态
    private String status;
    //操作流水号
    private String workId;
    
    @Override
	public String toString() {
		return "Order [loId=" + loId + ", pppoe=" + pppoe + ", status=" + status 
				+ ", failedReason=" + failedReason + ", workId=" + workId + "]";
	}

	
	public String getFailedReason()
	{
		return failedReason;
	}

	
	public void setFailedReason(String failedReason)
	{
		this.failedReason = failedReason;
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

	
	public String getStatus()
	{
		return status;
	}

	
	public void setStatus(String status)
	{
		this.status = status;
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