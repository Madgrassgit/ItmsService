package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class LOID_ARRAYType implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private String[] loid;
    
    @Override
	public String toString() {
		return "Order [loid=[" + loid + "]]";
	}

	
	public String[] getLoid()
	{
		return loid;
	}

	
	public void setLoid(String[] loid)
	{
		this.loid = loid;
	}
	
}
