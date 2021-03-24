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
import com.linkage.itms.dispatch.obj.CloudPingIPSecDiagnosticChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class CloudPingIPSecDiagnosticService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudPingIPSecDiagnosticService.class);

	@Override
	public String work(String inXml) {
		CloudPingIPSecDiagnosticChecker checker = new CloudPingIPSecDiagnosticChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudPingIPSecDiagnosticService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudPingIPSecDiagnosticService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
//			StringBuffer loidPrev = new StringBuffer();
//			int i = 0;
//			for (HashMap<String, String> m : userMap) {
//				if (i == 0) {
//					i ++;
//					continue;
//				}
//				loidPrev.append(StringUtil.getStringValue(m, "username"));
//				loidPrev.append(";");
//			}
			// LoidPrev 没有意义 置空
			checker.setLoidPrev("");

			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			// 根据设备序列号，厂商OUI检索设备
			Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfoByDeviceId(deviceId);
			// 设备不存在
			if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
				logger.warn("设备不存在：" + deviceId);
				checker.setResult(1004);
				checker.setResultDesc("设备不存在：" + deviceId);
				
				logger.warn("PingIPSecDiagnostic==>ReturnXml:"+checker.getReturnXml());
				
				return checker.getReturnXml();
			} 
			// 设备存在
			else{
				// 获取ping的源地址,目的地址
				getPingIp(checker, deviceId);
				
				if (StringUtil.isEmpty(checker.getSourceAddress()) || StringUtil.isEmpty(checker.getIpOrDomainName())) {
					logger.warn("servicename[CloudPingIPSecDiagnosticService][{}]参数sourceAddress，ipOrDomainName为空", deviceId);
					checker.setResult(1003);
					checker.setResultDesc("参数sourceAddress，ipOrDomainName为空或无法获取");
					return checker.getReturnXml();
				}
				
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
	private String getReturnXml(CloudPingIPSecDiagnosticChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudPingDiagnosticService", checker.getCmdId());
		logger.warn("servicename[CloudPingDiagnosticService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
	
	/**
	 * 获取ping的源地址,目的地址
	 * @param checker
	 * @param deviceId
	 */
	private void getPingIp(CloudPingIPSecDiagnosticChecker checker, String deviceId) {
		String sourceAddress = checker.getSourceAddress();
		String ipOrDomainName = checker.getIpOrDomainName(); 
		// 不为空直接返回
		if (!StringUtil.isEmpty(sourceAddress) && !StringUtil.isEmpty(ipOrDomainName)) {
			return ;
		}
		logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		// 传入的ip为空，采集设备ip
		ACSCorba acsCorba = new ACSCorba();
		ACSCorba corba = new ACSCorba();
		String ipsecPath = "InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN.";
		List<String> jList = acsCorba.getIList(deviceId, ipsecPath);
		if (null == jList || jList.size() == 0 || jList.isEmpty()) {
			logger.warn("servicename[CloudPingIPSecDiagnosticService][{}]获取" + ipsecPath + "下实例号失败", deviceId);
			return ;
		}
		ArrayList<String> ipsecList = new ArrayList<String>();
		String doIp = "";
		String soIp = "";
		for (String j : jList) {
			ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".RemoteSubnet");
			ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".LocalSubnet");

			String[] gatherPath = new String[2];
			ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, ipsecList.toArray(gatherPath));
			if (null == objList || objList.isEmpty()) {
				logger.warn("采集不到对应网关节点信息，device_id={}", deviceId);
				continue;
			}
			for (ParameValueOBJ pvobj : objList) {
				String value = pvobj.getValue();
				if (pvobj.getName().endsWith("RemoteSubnet")) {
					doIp = value;
				} 
				else if (pvobj.getName().endsWith("LocalSubnet")) {
					soIp = value;
				} 
			}
		}
		if (StringUtil.isEmpty(ipOrDomainName)) {
			ipOrDomainName = doIp.substring(0, doIp.lastIndexOf(".")) + ".1";
			checker.setIpOrDomainName(ipOrDomainName);
		}
		if (StringUtil.isEmpty(sourceAddress)) {
			sourceAddress = soIp.substring(0, soIp.lastIndexOf(".")) + ".1";
			checker.setSourceAddress(sourceAddress);
		}
		logger.warn("servicename[CloudPingIPSecDiagnosticService]获取源地址为：[{}],目的地址为[{}]", sourceAddress, ipOrDomainName);
		return ;
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
	public String PingList(String gw_type, String device_id, CloudPingIPSecDiagnosticChecker checker) {
		ACSCorba acsCorba = new ACSCorba();
		String ipsecPath = "InternetGatewayDevice.X_CT-COM_VLAN.VLANConfig.";
		List<String> jList = acsCorba.getIList(device_id, ipsecPath);
		if (null == jList || jList.size() == 0 || jList.isEmpty()) {
			logger.warn("servicename[CloudQueryConfigureService][{}]获取" + ipsecPath + "下实例号失败", device_id);
			checker.setResult(1003);
			checker.setResultDesc("获取InternetGatewayDevice.X_CT-COM_VLAN.VLANConfig.下实例号失败");
			return checker.getReturnXml();
		}
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[8];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.IPPingDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = ipsecPath + "1";
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.IPPingDiagnostics.SourceIpAddress");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getSourceAddress();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getIpOrDomainName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageNum();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getTimeOut();
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6].setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageByte();
		anyObject.para_type_id = "3";
		ParameterValueStruct[6].setValue(anyObject);
		
		ParameterValueStruct[7] = new ParameterValueStruct();
		ParameterValueStruct[7].setName("InternetGatewayDevice.IPPingDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "0";
		anyObject.para_type_id = "3";
		ParameterValueStruct[7].setValue(anyObject);
		
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
		Map<String, String> PingMap = null;
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
