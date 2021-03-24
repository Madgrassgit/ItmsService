package com.linkage.itms.dispatch.service;

import java.text.NumberFormat;
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
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.cloud.AsynchronousService;
import com.linkage.itms.dispatch.obj.CloudPingDiagnosticChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class CloudPingDiagnosticService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudPingDiagnosticService.class);

	@Override
	public String work(String inXml) {
		CloudPingDiagnosticChecker checker = new CloudPingDiagnosticChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudPingDiagnosticService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudPingDiagnosticService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
			
			QueryDevDAO qdDao = new QueryDevDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccountCloud(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoidCloud(checker.getUserInfo());
			}
			
			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("查询不到对应用户");
				return checker.getReturnXml();
			}

			String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return checker.getReturnXml();
			}
			
			// Loid
			checker.setLoid(StringUtil.getStringValue(userMap.get(0), "username"));
			StringBuffer loidPrev = new StringBuffer();
			int i = 0;
			for (HashMap<String, String> m : userMap) {
				if (i == 0) {
					i ++;
					continue;
				}
				loidPrev.append(StringUtil.getStringValue(m, "username"));
				loidPrev.append(";");
			}
			// LoidPrev 先设置为空
//			checker.setLoidPrev("");
			checker.setLoidPrev(loidPrev.toString());
			
			if (!"0".equals(checker.getCallBack())) {
				AsynchronousService as = new AsynchronousService(inXml, "PingDiagnostic");
				if ("4".equals(checker.getWanPassageWay())) {
					as = new AsynchronousService(inXml, "PingVXLANDiagnostic");
				}
				Global.G_AsynchronousThread.execute(as);
				return checker.getReturnXml();
			}
			
			if ("4".equals(checker.getWanPassageWay())) {
				return new CloudPingVXLANDiagnosticService().work(inXml);
			}
			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			// 根据设备序列号，厂商OUI检索设备
			Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfoByDeviceId(deviceId);
			// 设备不存在
			if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
				logger.warn("设备不存在：" + deviceId);
				checker.setResult(1004);
				checker.setResultDesc("设备不存在：" + deviceId);
				
				logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
				
				return checker.getReturnXml();
			} 
			// 设备存在
			else{
				// ping诊断
				checker.setDeviceSN(deviceInfoMap.get("device_serialnumber"));
				String returnXml = PingList("1", deviceId,  checker);
				return returnXml;
			}
		}
		catch (Exception e) {
			logger.warn("CloudPingDiagnosticService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudPingDiagnosticChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudPing", checker.getCmdId());
		logger.warn("servicename[CloudPingDiagnosticService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
	
	/**
	 * 返回Ping测试诊断结果
	 * 
	 * @param gw_type 1：家庭网关 2：政企网关
	 * @param device_id  设备ID
	 * 
	 * @param checker 接口回参
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String PingList(String gw_type, String device_id, CloudPingDiagnosticChecker checker) {
		
		String waninterface = "";
//		if("xj_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea)){
//			// 获取Wan
//			// 1、调用采集,采集InternetGatewayDevice.WANDevice下节点
//			int irt = new SuperGatherCorba().getCpeParams(device_id, 2, 1);
//			if (irt != 1)
//			{
//				logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]获取wan口失败",
//						new Object[] { checker.getCmdId(), checker.getUserInfo() });
//				checker.setResult(1005);
//				checker.setResultDesc("设备未知错误");
//				logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
//				return checker.getReturnXml();
//			}
//			else
//			{
//				// 2、从数据库获取wan_conn_id/wan_conn_sess_id
////				String vlanId = "41";
//				String serv_list = "INTERNET";
//				if ("1".equals(checker.getWanPassageWay()))
//				{
//					// tr069 通道
//					// vlanId = "46";
//					serv_list = "TR069";
//				}
//				DeviceConfigDAO dao = new DeviceConfigDAO();
//				Map<String, String> wanConnIds = dao.getWanInterfaceXJ(device_id, serv_list);
//				if (wanConnIds == null || wanConnIds.isEmpty())
//				{
//					logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]设备未获取到wan口",
//							new Object[] { checker.getCmdId(), checker.getUserInfo() });
//					checker.setResult(1005);
//					checker.setResultDesc("设备未获取到wan口");
//					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
//					return checker.getReturnXml();
//				}
//				else
//				{
//					String wanConnDevice = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
//					String wan_conn_id = StringUtil.getStringValue(wanConnIds
//							.get("wan_conn_id"));
//					String wan_conn_sess_id = StringUtil.getStringValue(wanConnIds
//							.get("wan_conn_sess_id"));
//					String sessType = StringUtil.getStringValue(wanConnIds.get("sess_type"));
//					if (sessType.equals("1"))
//					{
//						waninterface = wanConnDevice + wan_conn_id + ".WANPPPConnection."
//								+ wan_conn_sess_id + ".";
//					}
//					else if (sessType.equals("2"))
//					{
//						waninterface = wanConnDevice + wan_conn_id + ".WANIPConnection."
//								+ wan_conn_sess_id + ".";
//					}
//					else
//					{
//						logger.warn(
//								"servicename[PingDiagnostic]cmdId[{}]userinfo[{}]设备获取到wan口sessType值不对",
//								new Object[] { checker.getCmdId(), checker.getUserInfo() });
//						checker.setResult(1005);
//						checker.setResultDesc("设备未知错误");
//						logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
//						return checker.getReturnXml();
//					}
//				}
//			}
//		}//通过corba直接调用acs获取值，调用采集模块效率较低
//		else if("jl_dx".equals(Global.G_instArea) ||"cq_dx".equals(Global.G_instArea)){
			waninterface = gatherWanPath(device_id, checker);
			if(StringUtil.IsEmpty(waninterface)){
				checker.setResult(1005);
				checker.setResultDesc("设备未获取到wan口");
				logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
				return checker.getReturnXml();
			}
//		}
		
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.IPPingDiagnostics.Interface");
		anyObject = new AnyObject();
//		anyObject.para_value = checker.getWanPassageWay();
//		if ("xj_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea)|| "jl_dx".equals(Global.G_instArea)|| "cq_dx".equals(Global.G_instArea)) {
		anyObject.para_value = waninterface;
//		}
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getIpOrDomainName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageNum();
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getTimeOut();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageByte();
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6].setName("InternetGatewayDevice.IPPingDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "0";
		anyObject.para_type_id = "3";
		ParameterValueStruct[6].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("Ping");
		GetParameterValues getParameterValues = new GetParameterValues();
		
		String[] parameterNamesArr = new String[5];
		parameterNamesArr[0] = "InternetGatewayDevice.IPPingDiagnostics.SuccessCount";
		parameterNamesArr[1] = "InternetGatewayDevice.IPPingDiagnostics.FailureCount";
		parameterNamesArr[2] = "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime";
		parameterNamesArr[3] = "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime";
		parameterNamesArr[4] = "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime";
		getParameterValues.setParameterNames(parameterNamesArr);
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = device_id;
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
		Map PingMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			errMessage = "设备未知错误";
			checker.setResult(10051);
			checker.setResultDesc(errMessage);
			logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", device_id);
			errMessage = "设备未知错误";
			checker.setResult(10052);
			checker.setResultDesc(errMessage);
			logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			logger.warn("设备 stat：[{}] ", stat);
			if (stat != 1)
			{
				errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
				checker.setResult(1006);
				checker.setResultDesc(errMessage);
				logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "系统内部错误";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", device_id);
					checker.setResult(1007);
					checker.setResultDesc(errMessage);
					logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
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
								logger.warn("[{}]设备返回：{}", device_id, resp);
//								Fault fault = null;
								if (resp == null || "".equals(resp))
								{
									logger.debug("[{}]DevRpcCmdOBJ.value == null", device_id);
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
												PingMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													PingMap.put(parameterValueStructArr[j].getName(),
																	parameterValueStructArr[j].getValue().para_value);
												}
											} else {
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										} else {
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									} else {
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
				
				if (PingMap == null){
					checker.setResult(1014);
					checker.setResultDesc("返回值为空，Ping仿真失败");
					logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					// 成功数
					
					long FailureCount = StringUtil.getLongValue(PingMap.get("InternetGatewayDevice.IPPingDiagnostics.FailureCount"));
					long PackageCount = StringUtil.getIntegerValue(checker.getPackageNum());
					String packetLossRate = percent(FailureCount, PackageCount);
					String iPOrDomainName = checker.getIpOrDomainName();
					
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDeviceSN(checker.getDeviceSN());
					checker.setSuccesNum(StringUtil.getStringValue(PingMap, "InternetGatewayDevice.IPPingDiagnostics.SuccessCount"));
					checker.setFailNum(StringUtil.getStringValue(PingMap, "InternetGatewayDevice.IPPingDiagnostics.FailureCount"));
					checker.setAvgResponseTime(StringUtil.getStringValue(PingMap, "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime"));
					checker.setMinResponseTime(StringUtil.getStringValue(PingMap, "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime"));
					
					checker.setMaxResponseTime(StringUtil.getStringValue(PingMap, "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime"));
					checker.setPacketLossRate(packetLossRate);
					checker.setIpOrDomainName(iPOrDomainName);
					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
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
	private String gatherWanPath(String deviceId, CloudPingDiagnosticChecker checker) {
		ACSCorba corba = new ACSCorba();
		//logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String INTERNET = "INTERNET";
		if ("1".equals(checker.getWanPassageWay()))
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
	
	
	public String percent(long p1, long p2) {
		double p3;
		if (p2 == 0) {
			return "N/A";
		} else {
			p3 = (double) p1 / p2;
		}
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		String str = nf.format(p3);
		return str;
	}
}
