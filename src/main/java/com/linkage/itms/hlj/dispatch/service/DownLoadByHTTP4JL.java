package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
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
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.service.IService;
import com.linkage.itms.hlj.dispatch.obj.DownLoadByHTTPChecker4JL;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class DownLoadByHTTP4JL implements IService{

	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTP4JL.class);

	public String work(String inXml)
	{
		logger.warn("DownLoadByHTTP4JL==>inXml({})",inXml);

		DownLoadByHTTPChecker4JL checker = new DownLoadByHTTPChecker4JL(inXml);
		if (!checker.check()) {
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
			
			logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		} 
		// 设备存在
		else{
			
			String deviceId = StringUtil.getStringValue(deviceInfoMap,"device_id");
			
			/**
			 * PPPoE  拨测
			 */
			String returnXml = DownLoadByHTTP("1", deviceId, checker);
			
			logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+returnXml);
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
	public String DownLoadByHTTP(String gw_type, String deviceId, DownLoadByHTTPChecker4JL checker) {
		String waninterface = "";
		waninterface = gatherWanPath(deviceId , checker);
		
		if(StringUtil.IsEmpty(waninterface)){
			checker.setResult(1005);
			checker.setResultDesc("设备未获取到wan口");
			logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else{
			checker.setWanPassageWay(waninterface);
		}
		
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
		
		parameterNamesArr = new String[7];
		parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
		parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
		parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
		parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
			
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
			logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			logger.warn("stat="+stat);
			if (stat != 1)
			{
				errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
				checker.setResult(1007);
				checker.setResultDesc(errMessage);
				logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
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
									logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
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
												logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										}else {
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}else {
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
						}
					} else {
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				
				if (downByHTTPMap == null){
					checker.setResult(1015);
					checker.setResultDesc("返回值为空，HTTP下载仿真失败");
					logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					// 成功数
					String requestsReceivedTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.ROMTime");
					String transportStartTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.BOMTime");
					String transportEndTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.EOMTime");
					String receiveByteContainHead = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived");
					String receiveByte = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
					String tcpRequestTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
					String tcpResponseTime = ""+downByHTTPMap.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");

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
					
					logger.warn("DownLoadByHTTP4JL==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
					
				}
			}
		}
	}

	
	/**
	 * 获取相应InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.¬{i}.值
	 * @param deviceId 设备id
	 * @param checker 校验对象 ，用来判定INTERNET还是TR069
	 * @return String
	 */
	private String gatherWanPath(String deviceId, DownLoadByHTTPChecker4JL checker) {
		ACSCorba corba = new ACSCorba();
		//logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String INTERNET = "INTERNET";
		if ("46".equals(checker.getWanPassageWay()))
		{
			INTERNET = "TR069";
		}
		
		ArrayList<String> wanConnPathsList = null;
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("[DownLoadByHTTP4JL] [{}]获取" + wanConnPath + "下实例号失败，返回",
						deviceId);
				return "";
			}
			for (String j : jList)
			{
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
						+ wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
				{
					logger.warn("[DownLoadByHTTP4JL] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
					kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanIPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[DownLoadByHTTP4JL] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
					}else{
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
									+ wanServiceList);
						}
					}
				}
				else
				{
					for (String kppp : kPPPList)
					{
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanServiceList);
					}
				}
			}
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++)
		{
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0)
			{
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty())
		{
			logger.warn("[DownLoadByHTTP4JL] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
			
		}else{
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList)
			{
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++)
			{
				String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20
						: paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++)
				{
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty())
				{
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty())
			{
				logger.warn("[DownLoadByHTTP4JL] [{}]获取ServiceList失败", deviceId);
				return "";
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet())
			{
				logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
						entry.getValue() });
				String paramName = entry.getKey();
				/*if (paramName.indexOf(wanPPPConnection) >= 0)
				{
				}
				else if (paramName.indexOf(wanIPConnection) >= 0)
				{
					continue;
				}*/
				if (paramName.indexOf(wanServiceList) >= 0)
				{
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res=entry.getKey().substring(0, entry.getKey().indexOf("X_CT-COM_ServiceList"));
						logger.warn(res);
						return res;
					}
				}
			}
				
		}

		
		return "";
	}
	
}
