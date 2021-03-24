package com.linkage.itms.dispatch.sxdx.beanObj;

public class CpeInfoRst {

	private int iOpRst;
	private com.linkage.itms.dispatch.sxdx.beanObj.CpeInfo CpeInfo;
	public int getiOpRst() {
		return iOpRst;
	}
	public void setiOpRst(int iOpRst) {
		this.iOpRst = iOpRst;
	}
	public com.linkage.itms.dispatch.sxdx.beanObj.CpeInfo getCpeInfo() {
		return CpeInfo;
	}
	public void setCpeInfo(com.linkage.itms.dispatch.sxdx.beanObj.CpeInfo cpeInfo) {
		CpeInfo = cpeInfo;
	}
	@Override
	public String toString() {
		return "CpeInfoRst [iOpRst=" + iOpRst + ", CpeInfo=" + CpeInfo + "]";
	}
}
