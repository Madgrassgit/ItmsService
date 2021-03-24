package com.linkage.itms.hlj.dispatch.service;

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
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.service.IService;
import com.linkage.itms.dispatch.service.WanConnService;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.PPPoEDialChecker4JL;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class PPPoEDial4JL implements IService{

	private static Logger logger = LoggerFactory.getLogger(PPPoEDial4JL.class);
	private static String DiagnosticsState = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.DiagnosticsState";
	private static String Username = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Username";
	private static String Password = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Password";
	private static String WANInterface = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.WANInterface";
	private static String PPPAuthenticationProtocol = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.PPPAuthenticationProtocol";
	private static String RetryTimes = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.RetryTimes";
	private static String Result = "InternetGatewayDevice.X_CT-COM_PPPOE_EMULATOR.Result";
	

	public String work(String inXml)
	{
		logger.warn("PPPoEDial4JL==>inXml({})",inXml);

		PPPoEDialChecker4JL checker = new PPPoEDialChecker4JL(inXml);
		if (false == checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		QueryDeviceIdDAO queryDeviceIdDAO = new QueryDeviceIdDAO();
		
		// 根据设备序列号，厂商OUI检索设备
		List<HashMap<String, String>> deviceInfoMap = queryDeviceIdDAO.queryDevByLoid(checker.getLoid());
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			logger.warn("查无此设备：" + checker.getLoid());
			checker.setResult(1006);
			checker.setResultDesc("查无此设备：" + checker.getLoid());
			
			logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
			
			return checker.getReturnXml();
		} 
		// 设备存在
		else{
			String deviceId = deviceInfoMap.get(0).get("device_id");
			
			ACSCorba acsCorba = new ACSCorba();
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			int flag = getStatus.testDeviceOnLineStatus(deviceId,
					acsCorba);
			if(flag==1){
				logger.warn("[{}]开始获取WAN通道信息", new Object[]{deviceId});
				WanConnService wanconnService = new WanConnService();
				//获得WAN通道 顺便取出IP节点
				Map<String,String> wanConnDeviceMap = wanconnService.getWanPassageWay(deviceId,acsCorba);
				if (null!= wanConnDeviceMap && !wanConnDeviceMap.isEmpty()) {
					logger.warn("---------------获取wan通道成功:"+wanConnDeviceMap.get("INTERNET"));
					checker.setWanPassageWay(wanConnDeviceMap.get("INTERNET"));
				}
				else{
					logger.warn("获取wan通道失败：" + checker.getLoid());
					checker.setResult(0);
					checker.setResultDesc("获取wan通道失败：" + checker.getLoid());
					
					logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
					
					return checker.getReturnXml();
				}
			}
			else{
				logger.warn("设备不在线：" + checker.getLoid());
				checker.setResult(0);
				checker.setResultDesc("设备不在线：" + checker.getLoid());
				
				logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			
			/**
			 * PPPoE  拨测
			 */
			String returnXml = PPPoEDial("1", deviceId, checker);
			
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
	public String PPPoEDial(String gw_type, String deviceId, PPPoEDialChecker4JL checker) {
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[6];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName(DiagnosticsState);
		anyObject.para_value = "Start";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName(Username);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPppoeUser();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName(Password);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPppoePassword();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName(WANInterface);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getWanPassageWay();
		anyObject.para_type_id = "1";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName(PPPAuthenticationProtocol);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getAuthenticationMode();
		anyObject.para_type_id = "1";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName(RetryTimes);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getRepeatTimes();
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("PPPoEDial4JL");
		GetParameterValues getParameterValues = new GetParameterValues();
		
		String[] parameterNamesArr = new String[2];
		parameterNamesArr[0] = DiagnosticsState;
		parameterNamesArr[1] = Result;
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
		Map PPPoEMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
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
				logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
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
												PPPoEMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													PPPoEMap.put(parameterValueStructArr[j].getName(),
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
				
				if (PPPoEMap == null){
					checker.setResult(1014);
					checker.setResultDesc("返回值为空，PPPoE拨号仿真失败");
					logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					// 成功数
					String diagnosticsState = ""+PPPoEMap.get(DiagnosticsState);
					String diagnosticResult = ""+PPPoEMap.get(Result);
					
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setDiagnosticStatus(diagnosticsState);
					checker.setDiagnosticResult(diagnosticResult);
					logger.warn("PPPoEDial4JL==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
					
				}
			}
		}
	}
	
	
}
