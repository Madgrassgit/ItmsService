package com.linkage.itms.dispatch.sxdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;

public class DiagnoseResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7412535894021086858L;
	private int iOpRst;
	private Para[] paraList;
	public int getiOpRst() {
		return iOpRst;
	}
	public void setiOpRst(int iOpRst) {
		this.iOpRst = iOpRst;
	}
	public Para[] getParaList() {
		return paraList;
	}
	public void setParaList(Para[] paraList) {
		this.paraList = paraList;
	}
	@Override
	public String toString() {
		return "DiagnoseResult [iOpRst=" + iOpRst + ", paraList="
				+ Arrays.toString(paraList) + "]";
	}
	
	
}
