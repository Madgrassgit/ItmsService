package com.linkage.itms.dispatch.cqdx.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dispatch.cqdx.obj.RouteHopsOBJ;
import com.linkage.itms.dispatch.cqdx.obj.TraceRouteDealXML;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterInfoStruct;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.*;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DevTraceRouteService
{
	private static final Logger logger = LoggerFactory.getLogger(DevTraceRouteService.class);

	/**
	 * traceroute
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-7-1
	 * @return
	 */
	public TraceRouteDealXML traceRoute(String deviceId,TraceRouteDealXML deal)
	{
		logger.warn("device traceRoute begin. deviceId:" + deviceId);
		Map<String,String> resultMap = null;
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[5];
		//Requested
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("InternetGatewayDevice.TraceRouteDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		//接口，支持WAN和LAN
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1]
				.setName("InternetGatewayDevice.TraceRouteDiagnostics.Interface");
		anyObject = new AnyObject();
		
		String waninterface = gatherWanPath(deviceId);
		if(StringUtil.IsEmpty(waninterface)){
			deal.setResult("1005");
			deal.setErrMsg("设备未获取到wan口");
			logger.warn("DevTraceRouteService==>设备未获取到wan口,device_id=" + deviceId);
			return deal;
		}
		
		
		anyObject.para_value = waninterface;
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		//测试Host
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.TraceRouteDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = deal.getTraceHost();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		//最大跳转次数
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3]
				.setName("InternetGatewayDevice.TraceRouteDiagnostics.MaxHopCount");
		anyObject = new AnyObject();
		anyObject.para_value = deal.getMaxHopCount();
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		//超时时间
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4]
				.setName("InternetGatewayDevice.TraceRouteDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = deal.getTimeOut();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("TraceRoute");
		
		//获取参数
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[2];
		parameterNamesArr[0] = "InternetGatewayDevice.TraceRouteDiagnostics.DiagnosticsState";
		parameterNamesArr[1] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHopsNumberOfEntries";
		getParameterValues.setParameterNames(parameterNamesArr);
		
		DevRpc[] devRPCArr = new DevRpc[1];
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
		
		DevRPCManager devRPCManager = new DevRPCManager("1");
		List<DevRpcCmdOBJ> devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		
		int stat = -9;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
		}
		else
		{
			logger.warn("[{}]DevRpcCmdOBJ返不为空！", deviceId);
			stat = devRPCRep.get(0).getStat();
			logger.warn("stat={}", stat);
			if (stat != 1)
			{
				deal.setResult(String.valueOf(stat));
				deal.setErrMsg(Global.G_Fault_Map.get(stat).getFaultDesc());
				return deal;
			}
			else
			{
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					deal.setResult(String.valueOf(stat));
					deal.setErrMsg(Global.G_Fault_Map.get(stat).getFaultDesc());
					return deal;
				}
				else
				{
					logger.warn("devRPCRep.get(0).getRpcList().size() != 0");
					resultMap = getResultMap(deviceId,devRPCRep);
					long time = System.currentTimeMillis() /1000;
					deal.setResult("0");
					deal.setResponseTime(String.valueOf(time));
					deal.setRouteNumberOfentries(resultMap == null ? "" : resultMap.get("InternetGatewayDevice.TraceRouteDiagnostics.RouteHopsNumberOfEntries"));
					
					Map<String, Object> res = null;
					devRPCManager = new DevRPCManager("1");
					logger.warn("before getRouteHops");
					res = getRouteHops(devRPCManager, deviceId);
					logger.warn("after getRouteHops");
					
					if(null==res){
						logger.warn("[{}]获取InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.失败", deviceId);
						deal.setResult("-99");
						deal.setErrMsg("获取RouteHops节点失败");
						return deal;
					}
					
					//是否应该继续采集(默认不继续)
					boolean needContinue = false;
					String errMessage = (String)res.get("errMessage");
					logger.warn("errMessage={}",errMessage);
					//没有错误，则继续查询
					if(null == errMessage){
						//String.valueOf(res.get("routeHops"));
						needContinue = true;
					}
					logger.warn("needContinue={}",needContinue);
					int[] routeHops = null;
					
					if (needContinue){
						// 获取跳转数
						routeHops = (int[])res.get("routeHops");
						int queryType = 1;//查询类型(1:批量获取;2:分次获取)
						logger.warn("before getRouteHopResult");
						res = getRouteHopResult(routeHops, queryType,
								devRPCManager, deviceId);
						logger.warn("after getRouteHopResult");
						errMessage = (String)res.get("errMessage");
					}
					
					//没有错误，则继续查询
					if(null == errMessage)
					{
						// 获取跳转数
						//int[] routeHops = (int[])res.get("routeHops");
						
						resultMap = (Map<String, String>) res.get("resultMap");
						logger.warn("resultMap="+resultMap);
						/*
						
						devRPCManager = new DevRPCManager("1");
						res = getRouteHopResult(routeHops, 1,
								devRPCManager, deviceId);*/
						List<RouteHopsOBJ> routeHopsList = new ArrayList<RouteHopsOBJ>();
						for(int id : routeHops)
						{
							RouteHopsOBJ obj = new RouteHopsOBJ();
							obj.setHopRTTimes(resultMap.get("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops." + id + ".HopRTTimes"));
							obj.setHopHost(resultMap.get("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops." + id + ".HopHost"));
							obj.setHopHostAddress(resultMap.get("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops." + id + ".HopHostAddress"));
							obj.setHopErrorCode(resultMap.get("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops." + id + ".HopErrorCode"));
							routeHopsList.add(obj);
						}
						deal.setRouteHopsList(routeHopsList);
					}
				}
			}
			
		}
		return deal;
	}
	
	private static Map<String, String> getResultMap(String device_id,List<DevRpcCmdOBJ> devRPCRep)
	{
		Map<String, String> resMap = null;
		List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
		if (rpcList != null && !rpcList.isEmpty())
		{
			for (int k = 0; k < rpcList.size(); k++)
			{
				if ("GetParameterValuesResponse".equals(rpcList.get(k)
						.getRpcName()))
				{
					String resp = rpcList.get(k).getValue();
					logger.warn("[{}]设备返回：{}", device_id, resp);
					if (resp == null || "".equals(resp))
					{
						logger.debug("[{}]DevRpcCmdOBJ.value == null",
								device_id);
					} else
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
									resMap = new HashMap<String, String>();
									for (int j = 0; j < parameterValueStructArr.length; j++)
									{
										resMap.put(
												parameterValueStructArr[j]
														.getName(),
												parameterValueStructArr[j]
														.getValue().para_value);
									}
								}
							}
						}
					}
				}
			}
		}

		return resMap;
	}
	
	/**
	 * 获取路由跳转的结果集数目
	 * @param devRPCManager
	 * @param device_id
	 * @return
	 */
	private static Map<String,Object> getRouteHops(DevRPCManager devRPCManager,String device_id)
	{
		//动态获取节点值
		GetParameterNames getParameterNames = new GetParameterNames();
		String parameterNames = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.";
		getParameterNames.setParameterPath(parameterNames);
		getParameterNames.setNextLevel(1);
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = device_id;
		
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "GetParameterNames";
		rpcArr[0].rpcValue = getParameterNames.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		
		//结果个数获取完毕
		
		Map<String,Object> res = new HashMap<String,Object>();
		if (devRPCRep == null || devRPCRep.size() == 0 || devRPCRep.get(0) == null)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			res.put("errMessage","设备未知错误");
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			if (stat != 1)
			{
				res.put("errMessage",Global.G_Fault_Map.get(stat).getFaultDesc());
			}
			else
			{
				res.put("errMessage","系统内部错误");
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", device_id);
				}
				else
				{
					List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0)
							.getRpcList();
					if (rpcList != null && !rpcList.isEmpty())
					{
						for (int k = 0; k < rpcList.size(); k++)
						{
							if ("GetParameterNamesResponse".equals(rpcList.get(k)
									.getRpcName()))
							{
								String resp = rpcList.get(k).getValue();
								logger.warn("[{}]设备返回：{}", device_id, resp);
								if (resp == null || "".equals(resp))
								{
									logger.debug("[{}]DevRpcCmdOBJ.value == null",
											device_id);
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
										Element element = soapOBJ.getRpcElement();
										if (element != null)
										{
											GetParameterNamesResponse getParameterNamesResponse = XmlToRpc
													.GetParameterNamesResponse(element);
											if (getParameterNamesResponse != null)
											{
												ParameterInfoStruct[] parameterInfoStruct = getParameterNamesResponse.getParameterList();
												
												if(null==parameterInfoStruct){
													return null;
												}
												int[] routeHops = new int[parameterInfoStruct.length];
												
												for (int j = 0; j < parameterInfoStruct.length; j++)
												{
													//InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.
													String name = parameterInfoStruct[j].getName();
													
													routeHops[j] = StringUtil.getIntegerValue(name.split("\\.")[3]);
												}
												//排序
												Arrays.sort(routeHops);
												res.put("routeHops", routeHops);
												res.remove("errMessage");
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return res;
	}
	
	
	/**
	 * 获取节点下的参数值
	 * @param routeHops
	 * @param id
	 * @param queryType
	 * @param devRPCManager
	 * @param device_id
	 * @return
	 */
	private static Map<String,Object> getRouteHopResult(int[] routeHops,int queryType,DevRPCManager devRPCManager,String device_id)
	{
		//FIXME 长度有可能为0
		int size = (1 == queryType) ? 1:routeHops.length;
		
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = device_id;

		Rpc[] rpcArr = new Rpc[size];
		
		//1是全量查询，2是单次查询
		if(1 == queryType)
		{
			GetParameterValues getParameterValues = new GetParameterValues();
			String[] parameterNamesArr = new String[4*routeHops.length];

			for(int i = 0;i< routeHops.length ;i ++)
			{
				parameterNamesArr[0+ 4*i] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopHost";
				parameterNamesArr[1+ 4*i] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopHostAddress";
				parameterNamesArr[2+ 4*i] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopErrorCode";
				parameterNamesArr[3+ 4*i] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopRTTimes";
			}
			
			getParameterValues.setParameterNames(parameterNamesArr);
			
			rpcArr[0] = new Rpc();
			rpcArr[0].rpcId = "1";
			rpcArr[0].rpcName = "GetParameterValues";
			rpcArr[0].rpcValue = getParameterValues.toRPC();
		}else
		{
			for(int i = 0;i< routeHops.length ;i ++)
			{
				GetParameterValues getParameterValues = new GetParameterValues();
				String[] parameterNamesArr = new String[4];
				parameterNamesArr[0] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopHost";
				parameterNamesArr[1] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopHostAddress";
				parameterNamesArr[2] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopErrorCode";
				parameterNamesArr[3] = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+routeHops[i]+".HopRTTimes";

				getParameterValues.setParameterNames(parameterNamesArr);
				
				rpcArr[i] = new Rpc();
				rpcArr[i].rpcId = "" + (i + 1);
				rpcArr[i].rpcName = "GetParameterValues";
				rpcArr[i].rpcValue = getParameterValues.toRPC();
			}
		}
		
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		
		Map<String,Object> res = new HashMap<String,Object>();
		
		Map<String,String> resultMap = new HashMap<String, String>();
		
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			res.put("errMessage", "设备未知错误");
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", device_id);
			res.put("errMessage", "设备未知错误");
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			if (stat != 1)
			{
				res.put("errMessage", Global.G_Fault_Map.get(stat).getFaultDesc());
			}
			else
			{
				res.put("errMessage", "系统内部错误");
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", device_id);
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
								logger.warn("[{}]设备返回：{}", device_id, resp);
								if (resp == null || "".equals(resp))
								{
									logger.debug("[{}]DevRpcCmdOBJ.value == null",
											device_id);
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
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													String name = parameterValueStructArr[j].getName();
													String value = parameterValueStructArr[j].getValue().para_value;
													resultMap.put(name,value);
													res.remove("errMessage");
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//结果个数获取完毕
		}
		res.put("resultMap", resultMap);
		return res;
	}
	
	
	/**
	 * 获取相应InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.¬{i}.值
	 * @param deviceId 设备id
	 * @param checker 校验对象 ，用来判定INTERNET还是TR069
	 * @return String
	 */
	private String gatherWanPath(String deviceId) {
		ACSCorba corba = new ACSCorba();
		//logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String INTERNET = "INTERNET";
		
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
				logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath + "下实例号失败，返回",
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
					logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
					kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanIPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath
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
			logger.warn("[PingDiagnostic] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
			
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
				logger.warn("[PingDiagnostic] [{}]获取ServiceList失败", deviceId);
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
