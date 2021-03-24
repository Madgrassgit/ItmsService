
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class FeedbackWorkTicketsInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//流水号
	private String work_id;
	//用户宽带账号
    private RESULT_ARRAYType RESULT_ARRAY;
    
    @Override
	public String toString() {
		return "Order [work_id=" + work_id + ", RESULT_ARRAY=" + RESULT_ARRAY + "]";
	}

	
	public String getWork_id()
	{
		return work_id;
	}

	
	public void setWork_id(String work_id)
	{
		this.work_id = work_id;
	}


	
	public RESULT_ARRAYType getRESULT_ARRAY()
	{
		return RESULT_ARRAY;
	}


	
	public void setRESULT_ARRAY(RESULT_ARRAYType rESULT_ARRAY)
	{
		RESULT_ARRAY = rESULT_ARRAY;
	}
	
}