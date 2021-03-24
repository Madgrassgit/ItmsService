package com.linkage.itms.ids.obj;

import java.util.HashMap;
import java.util.Map;

import com.linkage.itms.ids.util.IdsUtil;

public class ReportPeroidOBJ {
	/**
	 * 接口调用唯一ID,每次调用此值不可重复
	 */
	private String cmdId;
	/**
	 * 接口类型 CX_01,固定
	 */
	private String cmdType;
	/**
	 * 客户端类型 1：BSS 2：IPOSS 3：综调 4：RADIUS 5：智能诊断系统
	 */
	private String clientType;
	/**
	 * 任务ID 对应表tab_status_report_task_dev
	 */
	private String taskId;
	/**
	 * 上报周期
	 */
	private String reportPeriod;
	/**
	 * 目标周期
	 */
	private String targetPeroid;
	/**
	 * 批量变更周期设备序列号集合
	 */
	private Map<String, String> devMap = new HashMap<String, String>();
	/**
	 * 返回错误码,默认成功
	 */
	private String resultNo = "0";
	/**
	 * 返回错误码对应的错误原因
	 */
	private String resultMsg = "成功";
	
	public void setResult(String resultNo, String resultMsg)
	{
		this.resultNo = resultNo;
		this.resultMsg = resultMsg;
	}
	
	public String getCmdId() {
		return cmdId;
	}
	
	public boolean valid()
	{
		return "0".equals(resultNo);
	}
	
	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}
	public String getCmdType() {
		return cmdType;
	}
	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getReportPeriod() {
		return reportPeriod;
	}
	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod = reportPeriod;
	}
	public String getTargetPeroid() {
		return targetPeroid;
	}
	public void setTargetPeroid(String targetPeroid) {
		this.targetPeroid = targetPeroid;
	}
	public Map<String, String> getDevMap() {
		return devMap;
	}
	public void setDevMap(Map<String, String> devMap) {
		this.devMap = devMap;
	}
	public String getResultNo() {
		return resultNo;
	}
	public void setResultNo(String resultNo) {
		this.resultNo = resultNo;
	}
	public String getResultMsg() {
		return resultMsg;
	}
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
	
	/**
	 * 将增加设备序列号，用于将设备状态信息下发给配置模块，默认该设备的执行状态为成功
	 * 
	 * @param deviceSn
	 *            设备序列号
	 */
	public void addDevice(String deviceSn)
	{
		setDeviceResult(deviceSn, IdsUtil.DEVICE_STATUS_SENDING);
	}

	/**
	 * 更新设备的状态信息执行状态
	 * 
	 * @param deviceSn
	 *            设备序列号
	 * @param resultNo
	 *            执行状态编码
	 */
	public void setDeviceResult(String deviceSn, String resultNo)
	{
		devMap.put(deviceSn, resultNo);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ReportPeroidOBJ [cmdId=");
		builder.append(cmdId);
		builder.append(", cmdType=");
		builder.append(cmdType);
		builder.append(", clientType=");
		builder.append(clientType);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append(", reportPeriod=");
		builder.append(reportPeriod);
		builder.append(", targetPeriod=");
		builder.append(targetPeroid);
		builder.append(", devMap=");
		builder.append(devMap);
		builder.append(", resultNo=");
		builder.append(resultNo);
		builder.append(", resultMsg=");
		builder.append(resultMsg);
		builder.append("]");
		return builder.toString();
	}
}
