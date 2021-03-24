package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryWorkTicketsInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//执行结果
	private String result;
	//工单流水号
	private String work_asgn_id;
    
    @Override
	public String toString() {
		return "Order [result=" + result + ", work_asgn_id=" + work_asgn_id + "]";
	}

	
	public String getResult()
	{
		return result;
	}

	
	public void setResult(String result)
	{
		this.result = result;
	}

	
	public String getWork_asgn_id()
	{
		return work_asgn_id;
	}

	
	public void setWork_asgn_id(String work_asgn_id)
	{
		this.work_asgn_id = work_asgn_id;
	}
    
}