package com.linkage.itms.dispatch.sxdx.beanObj;

import java.io.Serializable;

public class SetParameterResult implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3928333226540500630L;

	private int status;
	
	private int errorCode;
	
	private String errorInfo;

	
	@Override
	public String toString()
	{
		return "SetParameterResult [status=" + status + ", errorCode=" + errorCode
				+ ", errorInfo=" + errorInfo + "]";
	}


	public int getStatus()
	{
		return status;
	}

	
	public void setStatus(int status)
	{
		this.status = status;
	}

	
	public int getErrorCode()
	{
		return errorCode;
	}

	
	public void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
	}

	
	public String getErrorInfo()
	{
		return errorInfo;
	}

	
	public void setErrorInfo(String errorInfo)
	{
		this.errorInfo = errorInfo;
	}

}
