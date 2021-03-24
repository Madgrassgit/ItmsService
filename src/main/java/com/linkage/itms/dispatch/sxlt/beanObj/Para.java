package com.linkage.itms.dispatch.sxlt.beanObj;

import java.io.Serializable;

public class Para implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7783287601041227160L;
	private String name;
	
	private String value;

	
	@Override
	public String toString()
	{
		return "Para [name=" + name + ", value=" + value + "]";
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
	
	
}
