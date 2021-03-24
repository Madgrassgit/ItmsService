package com.linkage.itms.dispatch.cqdx.respObj;

import java.io.Serializable;

import com.linkage.itms.dispatch.cqdx.beanObj.QueryActionInfoResponse;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class queryActionInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private QueryActionInfoResponse queryActionInfoReturn;
    
    @Override
	public String toString() {
		return "Order [queryActionInfoReturn=[" + queryActionInfoReturn + "]]";
	}

	
	public QueryActionInfoResponse getQueryActionInfoReturn()
	{
		return queryActionInfoReturn;
	}

	
	public void setQueryActionInfoReturn(QueryActionInfoResponse queryActionInfoReturn)
	{
		this.queryActionInfoReturn = queryActionInfoReturn;
	}
    
}
