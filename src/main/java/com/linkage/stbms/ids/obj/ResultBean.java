package com.linkage.stbms.ids.obj;

import java.io.Serializable;

/**
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月10日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ResultBean implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7719026508910384825L;
	private int returnCode;
	private String returnMessage;
	
	public int getReturnCode()
	{
		return returnCode;
	}
	
	public String getReturnMessage()
	{
		return returnMessage;
	}
	
	public void setReturnCode(int returnCode)
	{
		this.returnCode = returnCode;
	}
	
	public void setReturnMessage(String returnMessage)
	{
		this.returnMessage = returnMessage;
	}
}
