package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryTerminalPasswordResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//操作流水号
	private String loId;
	//宽带帐号
    private String pppoe;
    //终端超级管理密码
    private String terminalPassword;
    //操作流水号
    private String workId;
    
    @Override
	public String toString() {
		return "Order [loId=" + loId + ", pppoe=" + pppoe+ ", terminalPassword=" + terminalPassword + ", workId=" + workId + "]";
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

	
	public String getTerminalPassword()
	{
		return terminalPassword;
	}

	
	public void setTerminalPassword(String terminalPassword)
	{
		this.terminalPassword = terminalPassword;
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