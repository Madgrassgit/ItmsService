
package com.linkage.stbms.pic.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterObj
{

	private static final Logger log = LoggerFactory.getLogger(ParameterObj.class);
	private String name = null;
	private String value = null;
	private String type = null;

	public ParameterObj()
	{
		super();
	}
	
	public ParameterObj(String _name, String _value, String _type)
	{
		super();
		this.name = _name;
		this.value = _value;
		this.type = _type;
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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
