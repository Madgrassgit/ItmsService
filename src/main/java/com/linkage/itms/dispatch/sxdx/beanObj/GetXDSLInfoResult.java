package com.linkage.itms.dispatch.sxdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;

public class GetXDSLInfoResult implements Serializable{
	private static final long serialVersionUID = -5256959617524642819L;
	
	//ping参数
    private Para[] paraList;
    private int IOpRst;
	@Override
	public String toString() {
		return "GetXDSLInfoResult [paraList=" + Arrays.toString(paraList)
				+ ", IOpRst=" + IOpRst + "]";
	}
	public Para[] getParaList() {
		return paraList;
	}
	public void setParaList(Para[] paraList) {
		this.paraList = paraList;
	}
	public int getIOpRst() {
		return IOpRst;
	}
	public void setIOpRst(int iOpRst) {
		IOpRst = iOpRst;
	}
    
}
