package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;


/**
 * 
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-11
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class UserIndex  implements Serializable{
	private static final long serialVersionUID = -1321473195434428183L;
	//对应于Type参数，根据其参数取值含义填写对应值。
	//例如Type取值1时，该参数需输入宽带帐号
	private String index;
	
	//0：逻辑ID，即激活码
	//1：宽带帐号，即Order结构中的ad_account字段。
	//2：Device ID(OUI-SN)
	//3：Device ID(OUI-SN)
    private int type;

	
	@Override
	public String toString()
	{
		return "UserIndex [index=" + index + ", type=" + type + "]";
	}


	public String getIndex()
	{
		return index;
	}

	
	public void setIndex(String index)
	{
		this.index = index;
	}


	
	public int getType()
	{
		return type;
	}


	
	public void setType(int type)
	{
		this.type = type;
	}

	
    
    
	
}
