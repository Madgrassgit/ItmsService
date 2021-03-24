package com.linkage.itms.dispatch.sxdx.beanObj;

public class oResult {
	private String maxResponseTime;
	private String minResponseTime;
	private String averageResponseTime;
	private String failureCount;
	private String successCount;
	@Override
	public String toString() {
		return "oResult [maxResponseTime=" + maxResponseTime
				+ ", minResponseTime=" + minResponseTime
				+ ", averageResponseTime=" + averageResponseTime
				+ ", failureCount=" + failureCount + ", successCount="
				+ successCount + "]";
	}
	public String getMaxResponseTime() {
		return maxResponseTime;
	}
	public void setMaxResponseTime(String maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}
	public String getMinResponseTime() {
		return minResponseTime;
	}
	public void setMinResponseTime(String minResponseTime) {
		this.minResponseTime = minResponseTime;
	}
	public String getAverageResponseTime() {
		return averageResponseTime;
	}
	public void setAverageResponseTime(String averageResponseTime) {
		this.averageResponseTime = averageResponseTime;
	}
	public String getFailureCount() {
		return failureCount;
	}
	public void setFailureCount(String failureCount) {
		this.failureCount = failureCount;
	}
	public String getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(String successCount) {
		this.successCount = successCount;
	}
	
	
}
