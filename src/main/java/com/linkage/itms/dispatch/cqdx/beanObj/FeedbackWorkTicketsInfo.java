
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class FeedbackWorkTicketsInfo implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//流水号
	private String work_id;
	//用户宽带账号
    private List<LOID_ARRAYType> LOID_ARRAY;
    
    @Override
	public String toString() {
		return "Order [work_id=" + work_id + ", LOID_ARRAY=" + LOID_ARRAY + "]";
	}

	
	public String getWork_id()
	{
		return work_id;
	}

	
	public void setWork_id(String work_id)
	{
		this.work_id = work_id;
	}

	
	public List<LOID_ARRAYType> getLOID_ARRAY()
	{
		return LOID_ARRAY;
	}

	
	public void setLOID_ARRAY(List<LOID_ARRAYType> lOID_ARRAY)
	{
		LOID_ARRAY = lOID_ARRAY;
	}
    
}