
package com.linkage.itms.dispatch.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.DownLoadByHTTPSpeadChecker;
import com.linkage.itms.ids.obj.ProccesDataThread;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

/**
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2016年6月26日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DownLoadByHTTPSpeadService implements IService
{

	private static Logger logger = LoggerFactory
			.getLogger(DownLoadByHTTPSpeadService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("DownLoadByHTTPSpeadService==>inXml({})", inXml);
		DownLoadByHTTPSpeadChecker checker = new DownLoadByHTTPSpeadChecker(inXml);
		if (false == checker.check())
		{
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getLoid(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		// 根据loid检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(
				checker.getDevSn(), checker.getOui(), checker.getLoid());
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			return checker.getReturnXml();
		}
		// 设备存在
		// 速率查询
		List<HashMap<String, String>> speadMap = deviceInfoDAO.getTestUserList(checker
				.getSpeed());
		if (null == speadMap || speadMap.size() == 0)
		{
			checker.setResult(1005);
			checker.setResultDesc("宽带速率非法");
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]宽带速率非法",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			return checker.getReturnXml();
		}
		HashMap<String, String> smap = speadMap.get(0);
		checker.setTestUserName(StringUtil.getStringValue(smap, "username", ""));
		checker.setTestPassword(StringUtil.getStringValue(smap, "password", ""));
		String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
		checker.setDeviceId(deviceId);
		// 获取设备序列号
		Map<String, String> devSnMap = deviceInfoDAO.queryDevSn(deviceId);
		checker.setDevSn(StringUtil.getStringValue(devSnMap, "device_serialnumber", ""));
		checker.setIp(StringUtil.getStringValue(devSnMap, "loopback_ip", ""));
		// 获取wan通道
		String Interface = getPingInterface(deviceId, "1");
		if (StringUtil.IsEmpty(Interface))
		{
			checker.setResult(1005);
			checker.setResultDesc("wan通道获取失败");
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]wan通道获取失败",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			return checker.getReturnXml();
		}
		logger.warn("DownLoadByHTTPSpeadService==>start device_id is:" + deviceId);
		// 开始PPPOE拨测
		return downLoadByHTTPSpead("1", deviceId, checker, Interface);
	}

	/**
	 * PPPoE 拨测
	 * 
	 * @param gw_type
	 *            1:家庭网关 2：政企网关
	 * @param deviceId
	 *            设备ID
	 * @param checker
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String downLoadByHTTPSpead(String gw_type, String deviceId,
			DownLoadByHTTPSpeadChecker checker, String Interface)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];

		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Username");
		anyObject.para_value = checker.getTestUserName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1]
				.setName("InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Password");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getTestPassword();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2]
				.setName("InternetGatewayDevice.DownloadDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = Interface;
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3]
				.setName("InternetGatewayDevice.DownloadDiagnostics.DownloadURL");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDownURL();
		anyObject.para_type_id = "1";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.DownloadDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "50";
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5]
				.setName("InternetGatewayDevice.DownloadDiagnostics.EthernetPriority");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPriority();
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6]
				.setName("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
		anyObject = new AnyObject();
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[6].setValue(anyObject);
		
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("downLoad");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[6];
		parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
		parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
		parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues";
		getParameterValues.setParameterNames(parameterNamesArr);
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[2];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		rpcArr[1] = new Rpc();
		rpcArr[1].rpcId = "2";
		rpcArr[1].rpcName = "GetParameterValues";
		rpcArr[1].rpcValue = getParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager(gw_type);
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		String errMessage = "";
		Map downByHTTPMap = null;
		// 将测速结果先置为失败 1：激活测速成功，2：结果测速失败，3：调用测速平台成功，4：调用测速平台失败
		checker.setSpeedStatus(2);
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]设备未知错误",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			return checker.getReturnXml();
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]设备未知错误",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			return checker.getReturnXml();
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			if (stat != 1)
			{
				errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
				checker.setResult(1007);
				checker.setResultDesc(errMessage);
				logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]{}",
						new Object[] { checker.getCmdId(), checker.getLoid(), errMessage });
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "系统内部错误";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					checker.setResult(1014);
					checker.setResultDesc(errMessage);
					logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误",
							new Object[] { checker.getCmdId(), checker.getLoid() });
					return checker.getReturnXml();
				}
				else
				{
					List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0)
							.getRpcList();
					if (rpcList != null && !rpcList.isEmpty())
					{
						for (int k = 0; k < rpcList.size(); k++)
						{
							if ("GetParameterValuesResponse".equals(rpcList.get(k)
									.getRpcName()))
							{
								String resp = rpcList.get(k).getValue();
								logger.warn("[{}]设备返回：{}", deviceId, resp);
								// Fault fault = null;
								if (resp == null || "".equals(resp))
								{
									logger.warn("[{}]DevRpcCmdOBJ.value == null",
											deviceId);
									checker.setResult(1011);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.error("servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误，无返回值",
											new Object[] { checker.getCmdId(),
													checker.getLoid() });
									return checker.getReturnXml();
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
										Element element = soapOBJ.getRpcElement();
										if (element != null)
										{
											GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
													.GetParameterValuesResponse(element);
											if (getParameterValuesResponse != null)
											{
												ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
														.getParameterList();
												downByHTTPMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													downByHTTPMap
															.put(parameterValueStructArr[j]
																	.getName(),
																	parameterValueStructArr[j]
																			.getValue().para_value);
												}
											}
											else
											{
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.error(
														"servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误，无返回值",
														new Object[] {
																checker.getCmdId(),
																checker.getLoid() });
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.error(
													"servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误，无返回值",
													new Object[] { checker.getCmdId(),
															checker.getLoid() });
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.error(
												"servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误，无返回值",
												new Object[] { checker.getCmdId(),
														checker.getLoid() });
										return checker.getReturnXml();
									}
								}
							}
						}
					}
					else
					{
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.error(
								"servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]系统内部错误，无返回值",
								new Object[] { checker.getCmdId(), checker.getLoid() });
						return checker.getReturnXml();
					}
				}
				if (downByHTTPMap == null)
				{
					checker.setResult(1015);
					checker.setResultDesc("返回值为空，HTTP下载仿真失败");
					logger.error(
							"servicename[DownLoadByHTTPSpeadService]cmdId[{}]loid[{}]返回值为空，HTTP下载仿真失败",
							new Object[] { checker.getCmdId(), checker.getLoid() });
					return checker.getReturnXml();
				}
				else
				{
					String transportStartTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.BOMTime");
					String transportEndTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.EOMTime");
					String receiveByte = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
					String tcpRequestTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
					String tcpResponseTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
					// 测速结果值
					String SampledTotalValues = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues");
					String[] sampledTotalValues = SampledTotalValues.split("\\|");
					checker.setAvgSampledTotalValues(getSampledValue(sampledTotalValues));
					checker.setMaxSampledTotalValues(getMaxValue(sampledTotalValues));
					;
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setSpeedStatus(1);
					checker.setDevSn(checker.getDevSn());
					checker.setTransportStartTime(transportStartTime);
					checker.setTransportEndTime(transportEndTime);
					checker.setReceiveByte(receiveByte);
					checker.setTcpRequestTime(tcpRequestTime);
					checker.setTcpResponseTime(tcpResponseTime);
					// 调用外部接口，不关心回参
					String url = Global.HTTPSPEED_URL;
					String method = Global.HTTPSPEED_METHOD;
					String nameSpace = Global.HTTPSPEED_NAMESPACE;
					logger.warn("[{}]  开始调用测速平台接口", new Object[] { checker.getLoid() });
					logger.warn("url=" + url);
					logger.warn("method=" + method);
					logger.warn("nameSpace=" + nameSpace);
					logger.warn("入参 : " + checker.getParam().toString());
					// 为了防止因网络原因造成猪程序卡住，等待对端回参， 这边做成多线程来操作
					ProccesDataThread procces = new ProccesDataThread(checker.getParam(), checker.getLoid());
					Thread thread = new Thread(procces);
					thread.start();
					return checker.getReturnXml();
				}
			}
		}
	}

	/**
	 * 获取平均值(保留两位小数)
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 平均值
	 */
	private String getSampledValue(String[] sampledValues)
	{
		// 保留小数点后两位
		DecimalFormat df = new DecimalFormat("######0.00");
		double sum = 0.0d;
		double result;
		boolean a = false;
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 15)
			{
				a = true;
				if (i == 0 || i == 1 || i == 2 || i == 13 || i == 14)
				{
					continue;
				}
			}
			sum += Double.parseDouble(sampledValues[i]);
		}
		if (a)
		{
			result = sum / 10;
		}
		else
		{
			result = sum / sampledValues.length;
		}
		return StringUtil.getStringValue(df.format(result));
	}

	/**
	 * 获取最大值
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 最大值
	 */
	private String getMaxValue(String[] sampledValues)
	{
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 15)
			{
				if (i == 0 || i == 1 || i == 2 || i == 13 || i == 14)
				{
					continue;
				}
			}
			list.add(Double.parseDouble(sampledValues[i]));
		}
		Collections.sort(list);
		return StringUtil.getStringValue(list.get(list.size() - 1));
	}

	/**
	 * 获取wan
	 * 
	 * @param device_id
	 * @param gw_type
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getPingInterface(String device_id, String gw_type)
	{
		logger.debug("getPingInterface({},{})", new Object[] { device_id, gw_type });
		String value = "";
		String wanConnDevice = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		SuperGatherCorba sgCorba = new SuperGatherCorba();
		// 获取Wan
		// 1、调用采集,采集InternetGatewayDevice.WANDevice下节点
		int irt = sgCorba.getCpeParams(device_id, 2, 1);
		logger.warn("[{}]调用采集获取Wan的结果：" + irt, device_id);
		String errorMsg = "";
		if (irt != 1)
		{
			errorMsg = "调用采集失败";
			logger.warn("[{}]" + errorMsg, device_id);
		}
		else
		{
			// 2、从数据库获取wan_conn_id/wan_conn_sess_id
			DeviceInfoDAO dao = new DeviceInfoDAO();
			List<Map> wanConnIds = dao.getWanConnIds(device_id);
			if (wanConnIds == null || wanConnIds.isEmpty())
			{
				errorMsg = "没有获取到Wan接口";
				logger.warn("[{}]" + errorMsg, device_id);
			}
			else
			{
				for (Map map : wanConnIds)
				{
					String wan_conn_id = StringUtil
							.getStringValue(map.get("wan_conn_id"));
					String wan_conn_sess_id = StringUtil.getStringValue(map
							.get("wan_conn_sess_id"));
					String serv_list = StringUtil.getStringValue(map.get("serv_list"));
					String vlanid = StringUtil.getStringValue(map.get("vlan_id"));
					if ("INTERNET".equals(serv_list) && "41".equals(vlanid))
					{
						value = wanConnDevice + wan_conn_id + ".WANPPPConnection."
								+ wan_conn_sess_id + ".";
						break;
					}
				}
			}
		}
		return value;
	}
}
