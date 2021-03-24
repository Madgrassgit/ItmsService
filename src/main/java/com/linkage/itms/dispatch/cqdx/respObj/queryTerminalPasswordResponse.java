package com.linkage.itms.dispatch.cqdx.respObj;

import java.io.Serializable;

import com.linkage.itms.dispatch.cqdx.beanObj.QueryTerminalPasswordResponse;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class queryTerminalPasswordResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private QueryTerminalPasswordResponse queryTerminalPasswordReturn;
    
    @Override
	public String toString() {
		return "Order [queryTerminalPasswordReturn=[" + queryTerminalPasswordReturn + "]]";
	}

	
	public QueryTerminalPasswordResponse getQueryTerminalPasswordReturn()
	{
		return queryTerminalPasswordReturn;
	}

	
	public void setQueryTerminalPasswordReturn(
			QueryTerminalPasswordResponse queryTerminalPasswordReturn)
	{
		this.queryTerminalPasswordReturn = queryTerminalPasswordReturn;
	}
    
}
