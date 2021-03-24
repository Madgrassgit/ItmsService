package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class ArrayOf_tns1_UserDetail implements Serializable{
	private static final long serialVersionUID = -5256959617524642819L;
	
	//ping参数
    private UserDetail[] userDetail;
    
    @Override
	public String toString() {
		return "Order [userDetail=[" + userDetail.toString() + "]]";
	}

	
	public UserDetail[] getUserDetail()
	{
		return userDetail;
	}

	
	public void setUserDetail(UserDetail[] userDetail)
	{
		this.userDetail = userDetail;
	}
    
}
