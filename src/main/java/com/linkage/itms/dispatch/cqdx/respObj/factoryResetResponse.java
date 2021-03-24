package com.linkage.itms.dispatch.cqdx.respObj;

import java.io.Serializable;

import com.linkage.itms.dispatch.cqdx.beanObj.FactoryResetResponse;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class factoryResetResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //ping参数
    private FactoryResetResponse factoryResetReturn;
    
    @Override
	public String toString() {
		return "Order [factoryResetReturn=[" + factoryResetReturn + "]]";
	}

	
	public FactoryResetResponse getFactoryResetReturn()
	{
		return factoryResetReturn;
	}

	
	public void setFactoryResetReturn(FactoryResetResponse factoryResetReturn)
	{
		this.factoryResetReturn = factoryResetReturn;
	}
    
}
