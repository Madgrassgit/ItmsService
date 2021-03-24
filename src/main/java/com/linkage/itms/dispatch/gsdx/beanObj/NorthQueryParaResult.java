package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-11
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class NorthQueryParaResult implements Serializable{
	private static final long serialVersionUID = -5256959617524642819L;
	
	//ping参数
    private Para[] paraList;
    private int IOpRst;
    
	@Override
	public String toString()
	{
		return "NorthQueryParaResult [paraList=" + Arrays.toString(paraList)
				+ ", IOpRst=" + IOpRst + "]";
	}

	
	public Para[] getParaList()
	{
		return paraList;
	}

	
	public void setParaList(Para[] paraList)
	{
		this.paraList = paraList;
	}

	
	public int getIOpRst()
	{
		return IOpRst;
	}

	
	public void setIOpRst(int iOpRst)
	{
		IOpRst = iOpRst;
	}
	
}
