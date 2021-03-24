
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class ChangeWifiPasswordResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//逻辑ID
	private String logic_id;
	//宽带账号
	private String ppp_username;
	//执行结果
	private int result;
	//错误原因
    private String failed_reason;
    
    @Override
	public String toString() {
		return "ChangeWifiPasswordResponse [logic_id=" + logic_id + ", ppp_username=" + ppp_username + ", result=" + result + ", failed_reason=" + failed_reason + "]";
	}

	
	public String getLogic_id()
	{
		return logic_id;
	}

	
	public void setLogic_id(String logic_id)
	{
		this.logic_id = logic_id;
	}

	
	public String getPpp_username()
	{
		return ppp_username;
	}

	
	public void setPpp_username(String ppp_username)
	{
		this.ppp_username = ppp_username;
	}

	
	public int getResult()
	{
		return result;
	}

	
	public void setResult(int result)
	{
		this.result = result;
	}

	
	public String getFailed_reason()
	{
		return failed_reason;
	}

	
	public void setFailed_reason(String failed_reason)
	{
		this.failed_reason = failed_reason;
	}
    
}