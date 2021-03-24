package com.linkage.itms.dispatch.sxdx.beanObj;

public class pingResult {
	public oResult result;

	public int IOpRst;
	public oResult getResult() {
		return result;
	}
	public void setResult(oResult result) {
		this.result = result;
	}

	public int getIOpRst() {
		return IOpRst;
	}

	public void setIOpRst(int IOpRst) {
		this.IOpRst = IOpRst;
	}

	@Override
	public String toString() {
		return "pingResult [result=" + result + ", IOpRst=" + IOpRst + "]";
	}
	
	
}
