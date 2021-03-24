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
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.QueryIsMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.PingTestChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

public class PingTest {
	private static Logger logger = LoggerFactory.getLogger(PingDiagnostic.class);

	public String work(String inXml){
		logger.warn("PingTest==>inXml({})",inXml);
		PingTestChecker checker = new PingTestChecker(inXml);
		if (!checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		DeviceInfoDAO dao = new DeviceInfoDAO();
		String deviceId = "";
		List<HashMap<String, String>> userMapList = null;

		QueryIsMulticastVlanDAO qdao = new QueryIsMulticastVlanDAO();
		// 用户信息类型:1：VOIP业务电话号码;2：LOID;3：IPTV宽带帐号;4：用户宽带帐号;5：VOIP认证帐号
		if (checker.getUserInfoType() == 1) {
			userMapList = qdao.queryUserByVoipPhone(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 2) {
			userMapList = qdao.queryUserByLoid(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 3) {
			userMapList = qdao.queryUserByIptvAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 4) {
			userMapList = qdao.queryUserByNetAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 5) {
			userMapList = qdao.queryUserByVoipAccount(checker.getUserInfo());
		}
		if (userMapList == null || userMapList.isEmpty()) {
			logger.warn("查无用户设备信息");
			checker.setResult(1000);
			checker.setResultDesc("查无用户设备信息");
			return checker.getReturnXml();
		}

		if (userMapList.size() > 1) {
			logger.warn("查到多条信息,请输入LOID进行查询");
			checker.setResult(1008);
			checker.setResultDesc("语音账号对应多个LOID，请根据LOID查询");
			return checker.getReturnXml();
		} else {
			deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("用户未绑定设备");
				checker.setResult(1004);
				checker.setResultDesc("用户未绑定设备");
				return checker.getReturnXml();
			}
		}
		Map<String, String> deviceInfoMap = dao.queryDevInfoByDeviceId(deviceId);
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			logger.warn("用户未绑定设备");
			checker.setResult(1004);
			checker.setResultDesc("用户未绑定设备");

			logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());

			return checker.getReturnXml();
		} 
		// 设备存在
		else{

			deviceId = deviceInfoMap.get("device_id");
			String oui = deviceInfoMap.get("oui");
			String devSN = deviceInfoMap.get("device_serialnumber");

			if (StringUtil.IsEmpty(deviceId) || StringUtil.IsEmpty(oui) || StringUtil.IsEmpty(devSN)) {
				logger.warn(
						"servicename[PingTest]cmdId[{}]netUserName[{}]netUserPwd[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getDevSn()});
				checker.setResult(4);
				checker.setResultDesc("没有语音业务");
				return checker.getReturnXml();
			}else{

				// 校验设备是否在线
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba acsCorba = new ACSCorba();

				int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
				// 设备正在被操作，不能获取节点值
				if (-3 == flag) {
					logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
					checker.setResult(1006);
					checker.setResultDesc("无法与终端完成交互");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag) {

					deviceId = StringUtil.getStringValue(deviceInfoMap,"device_id", "");
					logger.warn("");
					String cityId = StringUtil.getStringValue(deviceInfoMap,"city_id", "");
					/**
					 * ping诊断
					 */
					String returnXml = PingList("1", deviceId,  checker, cityId);

					// 回单
					return returnXml;
				}else {// 设备不在线，不能获取节点值
					logger.warn("设备不在线，无法获取节点值，device_id={}", deviceId);
					checker.setResult(1006);
					checker.setResultDesc("无法与终端完成交互");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
			}
		}
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
	public String PingList(String gw_type, String device_id, PingTestChecker checker,String cityId) {
		String waninterface = "";
		waninterface = gatherWanPath(device_id, checker);
		if(StringUtil.IsEmpty(waninterface)){
			checker.setResult(1005);
			checker.setResultDesc("设备未获取到wan口");
			logger.warn("PingTest==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}

		DeviceInfoDAO dao = new DeviceInfoDAO();
		List<HashMap<String, String>> list = dao.queryIpByCityIdandProtocol(cityId, checker.getVoiceType());
		checker.setiPOrDomainName(list.get(0).get("ping_ip"));
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
		anyObject.para_value = waninterface;
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);

		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getiPOrDomainName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);

		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = list.get(0).get("package_num");
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);

		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = list.get(0).get("timeout");
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);

		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = list.get(0).get("package_byte");   
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
		PingTestChecker checker_a = ping(devRPCArr, gw_type,  device_id);
		//ping第二个url
		checker.setiPOrDomainName(list.get(1).get("ping_ip"));
		anyObject = new AnyObject();
		anyObject.para_value = checker.getiPOrDomainName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		PingTestChecker checker_b = ping(devRPCArr, gw_type, device_id);



		if(0!=checker_a.getResult()&&0!=checker_b.getResult()){
			checker_b.setiPOrDomainName(list.get(0).get("ping_ip")+";"+list.get(1).get("ping_ip"));
			return checker_b.getReturnXml();
		}else if(0!=checker_a.getResult()){
			checker_a.setiPOrDomainName(list.get(0).get("ping_ip"));
			return checker_a.getReturnXml();
		}else if(0!=checker_b.getResult()){
			checker_b.setiPOrDomainName(list.get(1).get("ping_ip"));
			return checker_b.getReturnXml();
		}else{
			return checker_b.getReturnXml();
		}

	}

	private PingTestChecker ping(DevRpc[] devRPCArr,String gw_type,String device_id) {
		List<DevRpcCmdOBJ> devRPCRep = null;
		PingTestChecker checker = new PingTestChecker();
		DevRPCManager devRPCManager = new DevRPCManager(gw_type);
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);

		String errMessage = "";
		Map PingMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			errMessage = "设备未知错误";
			checker.setResult(1005);
			checker.setResultDesc(errMessage);
			logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());
			return checker;

		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", device_id);
			errMessage = "设备未知错误";
			checker.setResult(1005);
			checker.setResultDesc(errMessage);
			logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());
			return checker;
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
				logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());
				return checker;
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
					logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());
					return checker;
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
									checker.setResult(1007);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
									return checker;
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
												logger.debug("[{}]GetParameterValuesResponse == null", device_id);
												checker.setResult(1007);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
												return checker;
											}
										} else {
											logger.debug("[{}]soapOBJ.getRpcElement() == null", device_id);
											checker.setResult(1007);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
											return checker;
										}
									} else {
										logger.debug("[{}]XML.getSoabOBJ(XML.CreateXML(resp)) == null", device_id);
										checker.setResult(1007);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
										return checker;
									}
								}
							} 
						}
					} else {
						logger.debug("[{}]devRPCRep.get(0).getRpcList() == null", device_id);

						checker.setResult(1007);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
						return checker;
					}
				}

				if (PingMap == null){
					checker.setResult(1002);
					checker.setResultDesc("语音ping测试失败");
					logger.warn("PingTest==>ReturnXml:"+checker.getReturnXml());
					return checker;
				}else {
					// 成功数
					String succesNum = ""+PingMap.get("InternetGatewayDevice.IPPingDiagnostics.SuccessCount");
					String failNum = ""+PingMap.get("InternetGatewayDevice.IPPingDiagnostics.FailureCount");
					String avgResponseTime = ""+PingMap.get("InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime");
					String minResponseTime = ""+PingMap.get("InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime");
					String maxResponseTime = ""+PingMap.get("InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime");
					long FailureCount = StringUtil.getLongValue(PingMap.get("InternetGatewayDevice.IPPingDiagnostics.FailureCount"));
					long PackageCount = StringUtil.getIntegerValue("2");
					String packetLossRate = percent(FailureCount, PackageCount);
					String iPOrDomainName = checker.getiPOrDomainName();
					if(1<=FailureCount){
						checker.setResult(1002);
						checker.setResultDesc("语音ping测试失败");
						logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
						return checker;
					}else{
						checker.setResult(0);
						checker.setResultDesc("成功");
						checker.setDevSn(checker.getDevSn());
						logger.warn("PingDiagnostic==>ReturnXml:"+checker.getReturnXml());
						return checker;
					}
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
	private String gatherWanPath(String deviceId, PingTestChecker checker) {
		ACSCorba corba = new ACSCorba();
		//logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String INTERNET = "VOIP";
		//		if ("1".equals(checker.getWanPassageWay()))
		//		{
		//			INTERNET = "TR069";
		//		}

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
				logger.warn("[PingTest] [{}]获取" + wanConnPath + "下实例号失败，返回",
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
					logger.warn("[PingTest] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
					kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanIPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[PingTest] [{}]获取" + wanConnPath
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
			logger.warn("[PingTest] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);

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
				logger.warn("[PingTest] [{}]获取ServiceList失败", deviceId);
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
						logger.warn("[{}]{}={} ",res,deviceId);
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
