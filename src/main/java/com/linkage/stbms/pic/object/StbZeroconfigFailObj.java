package com.linkage.stbms.pic.object;

/**
 * 失败原因BEAN
 * @author Administrator
 *
 */
public class StbZeroconfigFailObj {

	/** 开始时间*/
	private long startTime;
	
	/** 唯一编号*/
	private String bussId;
	
	/** 失败原因id*/
	private int failReasonId;
	
	private String returnValue;

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getBussId() {
		return bussId;
	}

	public void setBussId(String bussId) {
		this.bussId = bussId;
	}

	public int getFailReasonId() {
		return failReasonId;
	}

	public void setFailReasonId(int failReasonId) {
		this.failReasonId = failReasonId;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
}
