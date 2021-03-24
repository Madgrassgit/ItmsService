package com.linkage.itms.dispatch.sxdx.beanObj;

import java.io.Serializable;


/**
 * 用户信息
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-14
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class UserInfo  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1775280850557084238L;
	//逻辑id
	private String userid;
	
	
	@Override
	public String toString()
	{
		return "UserInfo [userid=" + userid + "]";
	}

	public String getUserid()
	{
		return userid;
	}
	
	public void setUserid(String userid)
	{
		this.userid = userid;
	}
	
}
