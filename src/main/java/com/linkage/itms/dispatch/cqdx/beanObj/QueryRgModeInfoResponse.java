package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class QueryRgModeInfoResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
    //路由桥接模式
    private String rgMode;
    
    @Override
	public String toString() {
		return "QueryRgModeInfoResponse [rgMode=[" + rgMode + "]]";
	}

	
	public String getRgMode()
	{
		return rgMode;
	}

	
	public void setRgMode(String rgMode)
	{
		this.rgMode = rgMode;
	}
    
}
