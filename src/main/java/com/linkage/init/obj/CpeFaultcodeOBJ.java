package com.linkage.init.obj;

public class CpeFaultcodeOBJ {
	
	private int faultCode = 0;
	
	private int faultType =0;
	
	private String faultName = null;
	
	private String faultDesc = null;
	
	private String faultReason = null;
	
	private String solutions = null;

	public int getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(int faultCode) {
		this.faultCode = faultCode;
	}

	public String getFaultDesc() {
		return faultDesc;
	}

	public void setFaultDesc(String faultDesc) {
		this.faultDesc = faultDesc;
	}

	public String getFaultName() {
		return faultName;
	}

	public void setFaultName(String faultName) {
		this.faultName = faultName;
	}

	public String getFaultReason() {
		return faultReason;
	}

	public void setFaultReason(String faultReason) {
		this.faultReason = faultReason;
	}

	public int getFaultType() {
		return faultType;
	}

	public void setFaultType(int faultType) {
		this.faultType = faultType;
	}

	public String getSolutions() {
		return solutions;
	}

	public void setSolutions(String solutions) {
		this.solutions = solutions;
	}
	
	
}
