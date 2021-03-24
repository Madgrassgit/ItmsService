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
import com.linkage.itms.Global;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.PPPoEDialChecker;
import com.linkage.itms.dispatch.obj.VoipRegistTestChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.Fault;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;


public class VoipRegistTestService implements IService{
	
	private static Logger logger = LoggerFactory.getLogger(VoipRegistTestService.class);
	//request 参数
	private static final String REQUEST = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.IADDiagnosticsState";
	
	//测试服务器 参数
	private static final String TESTSERVER = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.TestServer";
	
	//测试结果 参数
	private static final String TESTRESULT = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.RegistResult";
	
	//失败原因 参数
	private static final String TESTREASON = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.Reason";
	public String work(String inXml)
	{
		String returnXml = null;
		VoipRegistTestChecker checker = new VoipRegistTestChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getDevSn(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(),
						inXml });
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(checker.getDevSn(),
				checker.getOui());
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
			logger.warn(
					"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getDevSn()});
			checker.setResult(1006);
			checker.setResultDesc("查无此设备：" + checker.getOui()+"-"+checker.getDevSn());
			returnXml = checker.getReturnXml();
			//记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getDevSn(), "VoipRegistTestService");
			logger.warn(
					"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
					new Object[] { checker.getCmdId(), checker.getDevSn(),returnXml});
			
			return returnXml;
		} 
		// 设备存在
		else{
			
			String deviceId = deviceInfoMap.get("device_id");
			try {
			
			/**
			 * PPPoE  拨测
			 */
				logger.warn(
						"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]start VoipRegistTestService...",
						new Object[] { checker.getCmdId(), checker.getDevSn()});
			returnXml = voipRegistTest("1", deviceId, checker);
		} catch (Exception e) {
			logger.warn(
					"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]VoipRegistTest Exception",
					new Object[] { checker.getCmdId(), checker.getDevSn()});
			e.printStackTrace();
		}
			//记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getDevSn(), "VoipRegistTestService");
			logger.warn(
					"servicename[VoipRegistTestService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
					new Object[] { checker.getCmdId(), checker.getDevSn(),returnXml});
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
	public String voipRegistTest(String gw_type, String deviceId, VoipRegistTestChecker checker) {
		
			DevRpc[] devRPCArr = new DevRpc[1];
			
			AnyObject anyObject = new AnyObject();
			SetParameterValues setParameterValues = new SetParameterValues();
			
			ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[2];
			
			ParameterValueStruct[0] = new ParameterValueStruct();
			ParameterValueStruct[0].setName(REQUEST);
			anyObject.para_value = "Requested";
			anyObject.para_type_id = "1";
			ParameterValueStruct[0].setValue(anyObject);
			
			ParameterValueStruct[1] = new ParameterValueStruct();
			ParameterValueStruct[1].setName(TESTSERVER);
			anyObject = new AnyObject();
			anyObject.para_value = "" + checker.getRegisterServer();
			anyObject.para_type_id = "2";
			ParameterValueStruct[1].setValue(anyObject);
			
			
			
			setParameterValues.setParameterList(ParameterValueStruct);
			setParameterValues.setParameterKey("IAD");
			GetParameterValues getParameterValues = new GetParameterValues();
			
			String[] parameterNamesArr = new String[2];
			parameterNamesArr[0] = TESTRESULT;
			parameterNamesArr[1] = TESTREASON;
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
			Map<String,String> VoipRegistTestMap = null;
			if (devRPCRep == null || devRPCRep.size() == 0)
			{
				logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(10071);
				checker.setResultDesc(errMessage);
				logger.warn("VoipRegistTestService==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
				
			}
			else if (devRPCRep.get(0) == null)
			{
				logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(10072);
				checker.setResultDesc(errMessage);
				logger.warn("VoipRegistTestService==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("VoipRegistTestService==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					errMessage = "系统内部错误";
					if (devRPCRep.get(0).getRpcList() == null
							|| devRPCRep.get(0).getRpcList().size() == 0)
					{
						logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
						checker.setResult(1007);
						checker.setResultDesc(errMessage);
						logger.warn("VoipRegistTestService==>ReturnXml:"+checker.getReturnXml());
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
									Fault fault = null;
									if (resp == null || "".equals(resp))
									{
										logger.debug("[{}]DevRpcCmdOBJ.value == null", deviceId);
									}
									else
									{
										SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
										if (soapOBJ != null)
										{
											fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
											Element element = soapOBJ.getRpcElement();
											if (element != null)
											{
												GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
														.GetParameterValuesResponse(element);
												if (getParameterValuesResponse != null)
												{
													ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
															.getParameterList();
													VoipRegistTestMap = new HashMap<String, String>();
													for (int j = 0; j < parameterValueStructArr.length; j++)
													{
														VoipRegistTestMap.put(parameterValueStructArr[j].getName(),
																		parameterValueStructArr[j].getValue().para_value);
													}
												}
											}
										}
									}
								}
							}
						}
					}
					
					if (VoipRegistTestMap == null){
						checker.setResult(1007);
						checker.setResultDesc("系统内部错误");
						logger.warn("PPPoEDial==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}else {
						// 成功数
						String diagnosticsState = ""+VoipRegistTestMap.get(TESTRESULT);
						String diagnosticResult = ""+VoipRegistTestMap.get(TESTREASON);
						
						checker.setResult(0);
						checker.setResultDesc("成功");
						checker.setDevSn(checker.getDevSn());
						checker.setDiagnosticResult(diagnosticsState);
						checker.setDiagnosticReason(diagnosticResult);
						logger.warn("PPPoEDial==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
						
					}
				}
			}
		
		
	}
	
}
