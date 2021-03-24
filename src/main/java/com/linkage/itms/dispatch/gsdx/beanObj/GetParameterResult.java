package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;

public class GetParameterResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4028759513099906910L;
	private Integer errorCode;
	private String errorInfo;
	private Para[] paras;
	@Override
	public String toString() {
		return "GetParameterResult [errorCode=" + errorCode + ", errorInfo="
				+ errorInfo + ", paras=" + Arrays.toString(paras) + "]";
	}
	public Integer getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorInfo() {
		return errorInfo;
	}
	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	public Para[] getParas() {
		return paras;
	}
	public void setParas(Para[] paras) {
		this.paras = paras;
	}

}
