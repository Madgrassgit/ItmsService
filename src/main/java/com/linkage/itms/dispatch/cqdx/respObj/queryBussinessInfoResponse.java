package com.linkage.itms.dispatch.cqdx.respObj;

import java.io.Serializable;

import com.linkage.itms.dispatch.cqdx.beanObj.QueryBussinessInfoResponse;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class queryBussinessInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private QueryBussinessInfoResponse queryBussinessInfoReturn;
    
    @Override
	public String toString() {
		return "Order [queryBussinessInfoReturn=[" + queryBussinessInfoReturn + "]]";
	}

	
	public QueryBussinessInfoResponse getQueryBussinessInfoReturn()
	{
		return queryBussinessInfoReturn;
	}

	
	public void setQueryBussinessInfoReturn(
			QueryBussinessInfoResponse queryBussinessInfoReturn)
	{
		this.queryBussinessInfoReturn = queryBussinessInfoReturn;
	}
    
}
