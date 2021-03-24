package com.linkage.itms.dispatch.sxlt.service;

import com.linkage.itms.dispatch.sxlt.beanObj.Para;

public class ServiceFather
{
	protected String methodName = "";
	
	
	public ServiceFather(String methodName)
	{
		super();
		this.methodName = methodName;
	}


	public String getMethodName()
	{
		return methodName;
	}

	
	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}
	
	public static Para setPara(String name,String value){
		Para para = new Para();
		para.setName(name);
		para.setValue(value);
		return para;
	}
}
