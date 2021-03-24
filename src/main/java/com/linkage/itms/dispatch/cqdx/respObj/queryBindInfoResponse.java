package com.linkage.itms.dispatch.cqdx.respObj;

import java.io.Serializable;

import com.linkage.itms.dispatch.cqdx.beanObj.QueryBindInfoResponse;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class queryBindInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private QueryBindInfoResponse queryBindInfoReturn;
    
    @Override
	public String toString() {
		return "Order [queryBindInfoReturn=[" + queryBindInfoReturn.toString() + "]]";
	}

	
	public QueryBindInfoResponse getQueryBindInfoReturn()
	{
		return queryBindInfoReturn;
	}
	
	public void setQueryBindInfoReturn(QueryBindInfoResponse queryBindInfoReturn)
	{
		this.queryBindInfoReturn = queryBindInfoReturn;
	}
    
}
