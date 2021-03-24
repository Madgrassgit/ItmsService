package com.linkage.itms.dispatch.sxdx.beanObj;

import java.io.Serializable;

public class ParaWithType implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7564440387588878530L;

	private String name;
	
	private String value;
	
	private Integer priority;

	
	@Override
	public String toString()
	{
		return "ParaWithType [name=" + name + ", value=" + value + ", priority="
				+ priority + "]";
	}


	public String getName()
	{
		return name;
	}

	
	public void setName(String name)
	{
		this.name = name;
	}

	
	public String getValue()
	{
		return value;
	}

	
	public void setValue(String value)
	{
		this.value = value;
	}


	
	public Integer getPriority()
	{
		return priority;
	}


	
	public void setPriority(Integer priority)
	{
		this.priority = priority;
	}
	
	
}
