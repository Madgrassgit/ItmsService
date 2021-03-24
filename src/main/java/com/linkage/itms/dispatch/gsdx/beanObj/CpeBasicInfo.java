package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;


/**
 * 基本信息
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-14
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class CpeBasicInfo  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7532653349758819869L;
	//基本信息
	private String type;
	//用户信息
    private String id;
    //业务信息数组
    private String hwVersion;
    
    private String swVersion;

	
	@Override
	public String toString()
	{
		return "CpeBasicInfo [type=" + type + ", id=" + id + ", hwVersion=" + hwVersion
				+ ", swVersion=" + swVersion + "]";
	}


	public String getType()
	{
		return type;
	}

	
	public void setType(String type)
	{
		this.type = type;
	}

	
	public String getId()
	{
		return id;
	}

	
	public void setId(String id)
	{
		this.id = id;
	}

	
	public String getHwVersion()
	{
		return hwVersion;
	}

	
	public void setHwVersion(String hwVersion)
	{
		this.hwVersion = hwVersion;
	}

	
	public String getSwVersion()
	{
		return swVersion;
	}

	
	public void setSwVersion(String swVersion)
	{
		this.swVersion = swVersion;
	}
  
   
	
}
