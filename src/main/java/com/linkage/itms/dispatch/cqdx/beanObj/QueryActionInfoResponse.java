package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryActionInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//执行结果
	private String actionResult;
	//逻辑ID
	private String loId;
	//宽带帐号
	private String pppoe;
	//上行方式
	private String uplinkType;
	//操作流水号
	private String workId;
    
    @Override
	public String toString() {
		return "Order [loId=" + loId + ", pppoe=" + pppoe + ", actionResult=" + actionResult 
				+ ", uplinkType=" + uplinkType + ", workId=" + workId + "]";
	}

	
	public String getActionResult()
	{
		return actionResult;
	}

	
	public void setActionResult(String actionResult)
	{
		this.actionResult = actionResult;
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

	public String getUplinkType()
	{
		return uplinkType;
	}

	public void setUplinkType(String uplinkType)
	{
		this.uplinkType = uplinkType;
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