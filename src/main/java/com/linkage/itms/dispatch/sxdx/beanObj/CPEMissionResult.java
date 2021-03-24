package com.linkage.itms.dispatch.sxdx.beanObj;

public class CPEMissionResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int iOperRst;
	private int strErrorDesc;
	public int getiOperRst() {
		return iOperRst;
	}
	public void setiOperRst(int iOperRst) {
		this.iOperRst = iOperRst;
	}
	public int getStrErrorDesc() {
		return strErrorDesc;
	}
	public void setStrErrorDesc(int strErrorDesc) {
		this.strErrorDesc = strErrorDesc;
	}
	@Override
	public String toString() {
		return "CPEMissionResult [iOperRst=" + iOperRst + ", strErrorDesc="
				+ strErrorDesc + "]";
	}
	
}
