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
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.DownLoadByHTTPChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class DownLoadByHTTP implements IService{

	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTP.class);

	public String work(String inXml)
	{
		logger.warn("DownLoadByHTTP==>inXml({})",inXml);

		DownLoadByHTTPChecker checker = new DownLoadByHTTPChecker(inXml);
		if (false == checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(checker.getDevSn(),
				checker.getOui(),checker.getLoid());
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		} 
		// 设备存在
		else{
			
			String deviceId = StringUtil.getStringValue(deviceInfoMap,"device_id");
			if("ah_dx".equals(Global.G_instArea)){
				checker.setIp(StringUtil.getStringValue(deviceInfoMap,"loopback_ip", ""));
			}
			
			/**
			 * PPPoE  拨测
			 */
			String returnXml = downLoadByHTTP("1", deviceId, checker);
			
			logger.warn("DownLoadByHTTP==>ReturnXml:"+returnXml);
			return returnXml;
		}
	}
	
	/**
	 * 安徽电信桥接网路接入方式的HTTP下载业务质量测试接口
	 * @param param
	 * @return
	 */
	public String workForAH(String inXml)
	{
		logger.warn("DownLoadByHTTP==>inXml({})",inXml);

		DownLoadByHTTPChecker checker = new DownLoadByHTTPChecker(inXml);
		if (false == checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(checker.getDevSn(),
				checker.getOui(),checker.getLoid());
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		} 
		// 设备存在
		else{
			
			String deviceId = StringUtil.getStringValue(deviceInfoMap,"device_id");
			if("ah_dx".equals(Global.G_instArea)){
				checker.setIp(StringUtil.getStringValue(deviceInfoMap,"loopback_ip", ""));
			}
			
			/**
			 * PPPoE  拨测
			 */
			String returnXml = downLoadByHTTPForAH("1", deviceId, checker);
			
			// 回单
			return returnXml;
		}
	}
	
	
	/**
	 * PPPoE  拨测
	 * 
	 * @param gw_type  1:家庭网关    2：政企网关
	 * @param deviceId  设备ID
	 * @param checker
	 * @return
	 */
	public String downLoadByHTTPForAH(String gw_type, String deviceId, DownLoadByHTTPChecker checker) {
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.DownloadDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getWanPassageWay();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.DownloadDiagnostics.DownloadURL");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDownURL();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.DownloadDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "50";
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.DownloadDiagnostics.EthernetPriority");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPriority();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Username");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getUserName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[5].setValue(anyObject);
		
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6].setName("InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Password");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPassword();
		anyObject.para_type_id = "1";
		ParameterValueStruct[6].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("downLoad");
		GetParameterValues getParameterValues = new GetParameterValues();
		
		String[] parameterNamesArr = null;
		if("ah_dx".equals(Global.G_instArea) || "jx_dx".equals(Global.G_instArea)){
			parameterNamesArr = new String[10];
			parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
			parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
			parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
			parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
			parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
			parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
			parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
			parameterNamesArr[7] = "InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues";
			parameterNamesArr[8] = "InternetGatewayDevice.DownloadDiagnostics.SampledValues";
			parameterNamesArr[9] = "InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState";
		} else {
			parameterNamesArr = new String[7];
			parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
			parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
			parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
			parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
			parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
			parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
			parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
		}
		
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
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
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
				logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
					if (rpcList != null && !rpcList.isEmpty())
					{
						for (int k = 0; k < rpcList.size(); k++)
						{
							if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName()))
							{
								String resp = rpcList.get(k).getValue();
								logger.warn("[{}]设备返回：{}", deviceId, resp);
//								Fault fault = null;
								if (resp == null || "".equals(resp))
								{
									logger.warn("[{}]DevRpcCmdOBJ.value == null", deviceId);
									checker.setResult(1011);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
									return checker.getReturnXml();
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
//										fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
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
													downByHTTPMap.put(parameterValueStructArr[j].getName(),
																	parameterValueStructArr[j].getValue().para_value);
												}
											}else {
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										}else {
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}else {
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
//							else {
//								checker.setResult(1012);
//								checker.setResultDesc("系统内部错误，无返回值");
//								logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
//								return checker.getReturnXml();
//							}
						}
					} else {
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				
				if (downByHTTPMap == null){
					checker.setResult(1015);
					checker.setResultDesc("返回值为空，HTTP下载仿真失败");
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					// 成功数
					String diagnosticsState = "" +downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
					String requestsReceivedTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.ROMTime");
					String transportStartTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.BOMTime");
					String transportEndTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.EOMTime");
					String receiveByteContainHead = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived");
					String receiveByte = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
					String tcpRequestTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
					String tcpResponseTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
					if("ah_dx".equals(Global.G_instArea) || "jx_dx".equals(Global.G_instArea)){
						String SampledTotalValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues");
						String[] sampledTotalValues = SampledTotalValues.split("\\|");
						checker.setAvgSampledTotalValues(getSampledValue(sampledTotalValues));
						checker.setMaxSampledTotalValues(getMaxValue(sampledTotalValues));
					}
					if("jx_dx".equals(Global.G_instArea))
					{
						String SampledValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledValues");
						String[] sampledValues = SampledValues.split("\\|");
						checker.setAvgSampledValues(getSampledValue(sampledValues));
						checker.setDiagnosticsState(diagnosticsState);
					}
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setRequestsReceivedTime(requestsReceivedTime);
					checker.setTransportStartTime(transportStartTime);
					checker.setTransportEndTime(transportEndTime);
					checker.setReceiveByteContainHead(receiveByteContainHead);
					checker.setReceiveByte(receiveByte);
					checker.setTcpRequestTime(tcpRequestTime);
					checker.setTcpResponseTime(tcpResponseTime);
					
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
					
				}
			}
		}
	}
	
	/**
	 * PPPoE  拨测
	 * 
	 * @param gw_type  1:家庭网关    2：政企网关
	 * @param deviceId  设备ID
	 * @param checker
	 * @return
	 */
	public String downLoadByHTTP(String gw_type, String deviceId, DownLoadByHTTPChecker checker) {
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[5];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.DownloadDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getWanPassageWay();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.DownloadDiagnostics.DownloadURL");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDownURL();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.DownloadDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "50";
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.DownloadDiagnostics.EthernetPriority");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPriority();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("downLoad");
		GetParameterValues getParameterValues = new GetParameterValues();
		
		String[] parameterNamesArr = null;
		
		if("ah_dx".equals(Global.G_instArea) || "jx_dx".equals(Global.G_instArea)){
			parameterNamesArr = new String[10];
			parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
			parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
			parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
			parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
			parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
			parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
			parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
			parameterNamesArr[7] = "InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues";
			parameterNamesArr[8] = "InternetGatewayDevice.DownloadDiagnostics.SampledValues";
			parameterNamesArr[9] = "InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState";
		} else {
			parameterNamesArr = new String[7];
			if ("js_dx".equals(Global.G_instArea))
			{
				parameterNamesArr = new String[8];
			}
			parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
			parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
			parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
			parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
			parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
			parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
			parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
			if ("js_dx".equals(Global.G_instArea))
			{
				parameterNamesArr[7] = "InternetGatewayDevice.DownloadDiagnostics.SampledValues";
			}
			
		}
		
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
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
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
				logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
					if (rpcList != null && !rpcList.isEmpty())
					{
						for (int k = 0; k < rpcList.size(); k++)
						{
							if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName()))
							{
								String resp = rpcList.get(k).getValue();
								logger.warn("[{}]设备返回：{}", deviceId, resp);
//								Fault fault = null;
								if (resp == null || "".equals(resp))
								{
									logger.warn("[{}]DevRpcCmdOBJ.value == null", deviceId);
									checker.setResult(1011);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
									return checker.getReturnXml();
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
//										fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
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
													downByHTTPMap.put(parameterValueStructArr[j].getName(),
																	parameterValueStructArr[j].getValue().para_value);
												}
											}else {
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										}else {
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}else {
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
//							else {
//								checker.setResult(1012);
//								checker.setResultDesc("系统内部错误，无返回值");
//								logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
//								return checker.getReturnXml();
//							}
						}
					} else {
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				
				if (downByHTTPMap == null){
					checker.setResult(1015);
					checker.setResultDesc("返回值为空，HTTP下载仿真失败");
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					String diagnosticsState = "" +downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
					// 成功数
					String requestsReceivedTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.ROMTime");
					String transportStartTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.BOMTime");
					String transportEndTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.EOMTime");
					String receiveByteContainHead = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived");
					String receiveByte = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
					String tcpRequestTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
					String tcpResponseTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
					if("ah_dx".equals(Global.G_instArea) || "jx_dx".equals(Global.G_instArea))
					{
						String SampledTotalValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues");
						String[] sampledTotalValues = SampledTotalValues.split("\\|");
						checker.setAvgSampledTotalValues(getSampledValue(sampledTotalValues));
						checker.setMaxSampledTotalValues(getMaxValue(sampledTotalValues));
					}
					if("jx_dx".equals(Global.G_instArea))
					{
						String SampledValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledValues");
						String[] sampledValues = SampledValues.split("\\|");
						checker.setAvgSampledValues(getSampledValue(sampledValues));
						checker.setDiagnosticsState(diagnosticsState);
					}
					if("js_dx".equals(Global.G_instArea))
					{
						String SampledValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledValues");
						String[] sampledValues = SampledValues.split("\\|");
						checker.setAvgSampledValues(getSampledValueForJs(sampledValues));
						checker.setMaxSampledValues(getMaxValueForJs(sampledValues));
					}
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setRequestsReceivedTime(requestsReceivedTime);
					checker.setTransportStartTime(transportStartTime);
					checker.setTransportEndTime(transportEndTime);
					checker.setReceiveByteContainHead(receiveByteContainHead);
					checker.setReceiveByte(receiveByte);
					checker.setTcpRequestTime(tcpRequestTime);
					checker.setTcpResponseTime(tcpResponseTime);
					
					logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
					
				}
			}
		}
	}

	public String workSpead(String inXml) 
	{
		logger.warn("DownLoadByHTTP==>inXml({})",inXml);

		DownLoadByHTTPChecker checker = new DownLoadByHTTPChecker(inXml);
		if (false == checker.checkSpead()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(checker.getDevSn(),
				checker.getOui(),checker.getLoid());
		
		// 速率查询
		List<HashMap<String,String>> speadMap = deviceInfoDAO.getTestUserList(checker.getSpead());
		if(null == speadMap || speadMap.size()==0)
		{
			checker.setResult(1005);
			checker.setResultDesc("宽带速率非法");
			
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		else
		{
			HashMap<String,String> smap = speadMap.get(0); 
			checker.setUserName(StringUtil.getStringValue(smap, "username"));
			checker.setPassword(StringUtil.getStringValue(smap, "password"));
			logger.warn("[{}] downLoadBySpead username[{}],password[{}] ",new Object[]{checker.getLoid(),checker.getUserName(),checker.getPassword()});
		}
		
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			
			logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		// 设备存在
		else{
			logger.warn("DownLoadByHTTP==>start id is:"+StringUtil.getStringValue(deviceInfoMap,"device_id"));
			String deviceId = StringUtil.getStringValue(deviceInfoMap,"device_id");
			
			/**
			 * PPPoE  拨测
			 */
			String returnXml;
			try {
				returnXml = downLoadByHTTPForAH("1", deviceId, checker);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				checker.setResult(1013);
				checker.setResultDesc("系统内部错误，无返回值");
				logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
			}
			
			// 回单
			return returnXml;
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
	 * 获取平均值(保留两位小数)
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 平均值
	 */
	private String getSampledValueForJs(String[] sampledValues)
	{
		// 保留小数点后两位
		DecimalFormat df = new DecimalFormat("######0.00");
		double sum = 0.0d;
		double result;
		boolean a = false;
		if(sampledValues.length == 0){
			return "0";
		}
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 20)
			{
				a = true;
				if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4)
				{
					continue;
				}
			}
			sum += Double.parseDouble(sampledValues[i]);
		}
		if (a)
		{
			result = sum / 15;
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
	private String getMaxValueForJs(String[] sampledValues)
	{
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 20)
			{
				if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4)
				{
					continue;
				}
			}
			list.add(Double.parseDouble(sampledValues[i]));
		}
		Collections.sort(list);
		return StringUtil.getStringValue(list.get(list.size() - 1));
	}
}
