package com.linkage.stbms.obj;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备Ping操作对象类
 * 
 * @author Jason(3412)
 * @date 2009-12-17
 */
public class PingOBJ {

	private static Logger logger = LoggerFactory.getLogger(PingOBJ.class);

	// 设备ID
	private String deviceId;
	// ping目的地址
	private String pingAddr;
	// 包大小
	private int packSize;
	// 包数目
	private int packNum;
	// 超时时间ms
	private int timeout;
	// DSCP值
	private int dscp;
	// 平均时延
	private int delayAvg;
	// 最大时延
	private int delayMax;
	// 最小时延
	private int delayMin;
	// 成功数
	private int succNum;
	// 失败数
	private int failNum;
	// PING操作是否成功
	private boolean isSuccess = false;
	//PING操作结果值
	private int faultCode;
	//响应时间
	private String responseTime = null;
	//跳数
	private String numberOfRouteHops = null;
	//每一跳的IP
	private List hopHostI; 
	//最大跳数
	private int maxHopCount;
	// 错误信息
	private String faultStr;
	// PING操作结果，如果成功，是"1"，不成功是"0"
	private String result;
	public String toString(){
		return "PingOBJ(device_id:" + deviceId + " ip:" + pingAddr + ")";
	}
	
	
	/** getter, setter methods */
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getPingAddr() {
		return pingAddr;
	}

	public void setPingAddr(String pingAddr) {
		this.pingAddr = pingAddr;
	}

	public int getPackSize() {
		return packSize;
	}

	public void setPackSize(int packSize) {
		this.packSize = packSize;
	}

	public int getPackNum() {
		return packNum;
	}

	public void setPackNum(int packNum) {
		this.packNum = packNum;
	}

	public int getDscp() {
		return dscp;
	}

	public void setDscp(int dscp) {
		this.dscp = dscp;
	}

	public int getDelayAvg() {
		return delayAvg;
	}

	public void setDelayAvg(int delayAvg) {
		this.delayAvg = delayAvg;
	}

	public int getDelayMax() {
		return delayMax;
	}

	public void setDelayMax(int delayMax) {
		this.delayMax = delayMax;
	}

	public int getDelayMin() {
		return delayMin;
	}

	public void setDelayMin(int delayMin) {
		this.delayMin = delayMin;
	}

	public int getSuccNum() {
		return succNum;
	}

	public void setSuccNum(int succNum) {
		this.succNum = succNum;
	}

	public int getFailNum() {
		return failNum;
	}

	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(int faultCode) {
		this.faultCode = faultCode;
	}


	
	public String getResponseTime()
	{
		return responseTime;
	}


	
	public void setResponseTime(String responseTime)
	{
		this.responseTime = responseTime;
	}


	
	public String getNumberOfRouteHops()
	{
		return numberOfRouteHops;
	}


	
	public void setNumberOfRouteHops(String numberOfRouteHops)
	{
		this.numberOfRouteHops = numberOfRouteHops;
	}


	
	public List getHopHostI()
	{
		return hopHostI;
	}


	
	
	public int getMaxHopCount()
	{
		return maxHopCount;
	}


	
	public void setMaxHopCount(int maxHopCount)
	{
		this.maxHopCount = maxHopCount;
	}


	public void setHopHostI(List hopHostI)
	{
		this.hopHostI = hopHostI;
	}


	
	public String getFaultStr()
	{
		return faultStr;
	}


	
	public void setFaultStr(String faultStr)
	{
		this.faultStr = faultStr;
	}


	
	public String getResult()
	{
		return result;
	}


	
	public void setResult(String result)
	{
		this.result = result;
	}

}
