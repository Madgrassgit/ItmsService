package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class RESULT_ARRAYType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private Loid_resultType[] loid_result;
    
    @Override
	public String toString() {
		return "Order [loid_result=[" + loid_result.toString() + "]]";
	}

	
	public Loid_resultType[] getLoid_result()
	{
		return loid_result;
	}

	
	public void setLoid_result(Loid_resultType[] loid_result)
	{
		this.loid_result = loid_result;
	}
    
}
