
package com.linkage.itms.ids.obj;

import java.util.HashMap;
import java.util.Map;

import com.linkage.itms.ids.util.IdsUtil;

/**
 * 设备状态信息上报功能开启和关闭接口POJO
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-10-17
 * @category com.linkage.itms.ids.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnosticEnableOBJ
{

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
	private String period;
	/**
	 * 是否开启 0:关闭 1：开启
	 */
	private String enable;
	/**
	 * 参数列表 1、物理状态 2、语音业务注册状态 3、语音业务注册失败原因 4、PPP拨号上网的连接状态 5、拨号错误代码
	 */
	private String paramList;
	/**
	 * 上传地址
	 */
	private String fileServerIp;
	/**
	 * 上传端口
	 */
	private String fileServerPort;
	/**
	 * 批量状态信息上报设备序列号集合
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

	public boolean valid()
	{
		return "0".equals(resultNo);
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

	public String getCmdId()
	{
		return cmdId;
	}

	public void setCmdId(String cmdId)
	{
		this.cmdId = cmdId;
	}

	public String getCmdType()
	{
		return cmdType;
	}

	public void setCmdType(String cmdType)
	{
		this.cmdType = cmdType;
	}

	public String getClientType()
	{
		return clientType;
	}

	public void setClientType(String clientType)
	{
		this.clientType = clientType;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod(String period)
	{
		this.period = period;
	}

	public String getEnable()
	{
		return enable;
	}

	public void setEnable(String enable)
	{
		this.enable = enable;
	}

	public String getParamList()
	{
		return paramList;
	}

	public void setParamList(String paramList)
	{
		this.paramList = paramList;
	}

	public String getFileServerIp()
	{
		return fileServerIp;
	}

	public void setFileServerIp(String fileServerIp)
	{
		this.fileServerIp = fileServerIp;
	}

	public String getFileServerPort()
	{
		return fileServerPort;
	}

	public void setFileServerPort(String fileServerPort)
	{
		this.fileServerPort = fileServerPort;
	}

	public String getResultNo()
	{
		return resultNo;
	}

	public void setResultNo(String resultNo)
	{
		this.resultNo = resultNo;
	}

	public String getResultMsg()
	{
		return resultMsg;
	}

	public void setResultMsg(String resultMsg)
	{
		this.resultMsg = resultMsg;
	}

	public Map<String, String> getDevMap()
	{
		return devMap;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public void setTaskId(String taskId)
	{
		this.taskId = taskId;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DiagnosticEnableOBJ [cmdId=");
		builder.append(cmdId);
		builder.append(", cmdType=");
		builder.append(cmdType);
		builder.append(", clientType=");
		builder.append(clientType);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append(", period=");
		builder.append(period);
		builder.append(", enable=");
		builder.append(enable);
		builder.append(", paramList=");
		builder.append(paramList);
		builder.append(", fileServerIp=");
		builder.append(fileServerIp);
		builder.append(", fileServerPort=");
		builder.append(fileServerPort);
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
