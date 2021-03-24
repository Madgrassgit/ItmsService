package com.linkage.itms.rms.obj;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2015年3月19日
 * @category com.linkage.itms
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class JsonEntity
{
	private String code;
	private String detail;
	private Object value="";
	public String getCode()
	{
		return code;
	}

	
	public void setCode(String code)
	{
		this.code = code;
	}

	public String getDetail()
	{
		return detail;
	}
	
	public void setDetail(String detail)
	{
		this.detail = detail;
	}


	
	public Object getValue()
	{
		return value;
	}


	
	public void setValue(Object value)
	{
		this.value = value;
	}
	
}
