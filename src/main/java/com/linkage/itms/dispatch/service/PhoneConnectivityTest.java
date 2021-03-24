package com.linkage.itms.dispatch.service;

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
import com.linkage.itms.dao.QueryIsMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.PhoneConnectivityTestChecker;
import com.linkage.itms.dispatch.obj.PingDiagnosticChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class PhoneConnectivityTest implements IService{

	private static Logger logger = LoggerFactory.getLogger(PhoneConnectivityTest.class);
	private static String TestSelector = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.TestSelector";
	private static String TestState = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.TestState";
	private static String PhoneConnectivity = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.PhoneConnectivity";

	public String work(String inXml)
	{
		logger.warn("PingDiagnostic==>inXml({})",inXml);

		PhoneConnectivityTestChecker checker = new PhoneConnectivityTestChecker(inXml);
		if (!checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();

		String deviceId = "";
		List<HashMap<String, String>> userMapList = null;
		QueryIsMulticastVlanDAO dao = new QueryIsMulticastVlanDAO();

		// 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
		if (checker.getUserInfoType() == 1) {
			userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 2) {
			userMapList = dao.queryUserByLoid(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 3) {
			userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 4) {
			userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 5) {
			userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
		}

		if (userMapList == null || userMapList.isEmpty()) {
			logger.warn("查无用户设备信息");
			checker.setResult(1000);
			checker.setResultDesc("查无用户设备信息");
			return checker.getReturnXml();
		}

		if (userMapList.size() > 1) {
			logger.warn("查到多条信息,请输入LOID进行查询");
			checker.setResult(1000);
			checker.setResultDesc("查到多条信息,请输入LOID进行查询");
			return checker.getReturnXml();
		} else {
			deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("设备不存在");
				checker.setResult(1004);
				checker.setResultDesc("设备不存在");
				return checker.getReturnXml();
			}
		}

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

			deviceId = deviceInfoMap.get("device_id");

			/**
			 * PPPoE  拨测
			 */
			String returnXml = phoneConnectivityTest("1", deviceId, checker);

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
	public String phoneConnectivityTest(String gw_type, String deviceId, PhoneConnectivityTestChecker checker) {

		DevRpc[] devRPCArr = new DevRpc[1];

		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();

		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[2];

		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName(TestSelector);
		anyObject.para_value = "PhoneConnectivityTest";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);

		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName(TestState);
		anyObject = new AnyObject();
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("PhoneConnectivityTest");
		
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[1];
		parameterNamesArr[0] = PhoneConnectivity;
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
		Map PhoneConnectityMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();

		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
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
				logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "系统内部错误";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					checker.setResult(1015);
					checker.setResultDesc(errMessage);
					logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
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
									logger.debug("[{}]DevRpcCmdOBJ.value == null", deviceId);
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
												PhoneConnectityMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													PhoneConnectityMap.put(parameterValueStructArr[j].getName(),
															parameterValueStructArr[j].getValue().para_value);
												}
											}else {
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
					}else {
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}

				if (PhoneConnectityMap == null){
					checker.setResult(1014);
					checker.setResultDesc("返回值为空，语音POTS口查询终端下挂的话机状态失败");
					logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					String phoneConnectivity = ""+PhoneConnectityMap.get(PhoneConnectivity);
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setPhoneConnectivity(phoneConnectivity);
					checker.setVoipPortNUM("1");
					logger.warn("PhoneConnectivityTest==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();

				}
			}
		}
	}

}
