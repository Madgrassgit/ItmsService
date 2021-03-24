
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
import com.linkage.itms.dispatch.obj.PPPoEDialChecker;
import com.linkage.itms.dispatch.obj.VoiceDialChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

public class VoiceDial implements IService
{

	private static Logger logger = LoggerFactory.getLogger(VoiceDial.class);
	private static String TestState = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.TestState";
	private static String TestSelector = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.TestSelector";
	private static String TestNode = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.X_CU_SimulateTest.";
	private static String TestType = TestNode + "TestType";
	private static String CalledNumber = TestNode + "CalledNumber";
	private static String CallHoldTimer = TestNode + "CallHoldTimer";
	private static String CalledWaitTimer = TestNode + "CalledWaitTimer";
	private static String DialDTMFConfirmEnable = TestNode + "DialDTMFConfirmEnable";
	private static String DialDTMFConfirmNumber = TestNode + "DialDTMFConfirmNumber";
	private static String DialDTMFConfirmResult = TestNode + "DialDTMFConfirmResult";
	private static String Status = TestNode + "Status";
	private static String Conclusion = TestNode + "Conclusion";
	private static String CallerFailReason = TestNode + "CallerFailReason";
	private static String CalledFailReason = TestNode + "CalledFailReason";
	private static String FailedResponseCode = TestNode + "FailedResponseCode";

	public String work(String inXml)
	{
		logger.warn("VoiceDial==>inXml({})", inXml);
		VoiceDialChecker checker = new VoiceDialChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(
				checker.getDevSn(), checker.getOui());
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			logger.warn("查无此设备：" + checker.getOui() + "-" + checker.getDevSn());
			checker.setResult(1006);
			checker.setResultDesc("查无此设备：" + checker.getOui() + "-" + checker.getDevSn());
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 设备存在
		else
		{
			String deviceId = deviceInfoMap.get("device_id");
			/**
			 * PPPoE 拨测
			 */
			String returnXml = voiceDial("1", deviceId, checker);
			// 回单
			return returnXml;
		}
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
	public String voiceDial(String gw_type, String deviceId, VoiceDialChecker checker)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[5];
		//吉林联通要求多下发一个节点 先下发 None,再下发Requested
		//InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.Tests.TestState
		if("jl_lt".equals(Global.G_instArea))
		{
			ParameterValueStruct = new ParameterValueStruct[7];
		}
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName(TestType);
		anyObject.para_value = checker.getTestType();
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName(CalledNumber);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getCalledNumber();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName(DialDTMFConfirmEnable);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDialDTMFConfirmEnable();
		anyObject.para_type_id = "4";
		ParameterValueStruct[2].setValue(anyObject);
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName(DialDTMFConfirmNumber);
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDialDTMFConfirmNumber();
		anyObject.para_type_id = "1";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName(CallHoldTimer);
		anyObject = new AnyObject();
		anyObject.para_value = "30";
		anyObject.para_type_id = "1";
		ParameterValueStruct[4].setValue(anyObject);
		if("jl_lt".equals(Global.G_instArea))
		{
			ParameterValueStruct[5] = new ParameterValueStruct();
			ParameterValueStruct[5].setName(TestSelector);
			anyObject = new AnyObject();
			anyObject.para_value = "X_CU_SimulateTest";
			anyObject.para_type_id = "1";
			ParameterValueStruct[5].setValue(anyObject);
			
			ParameterValueStruct[6] = new ParameterValueStruct();
			ParameterValueStruct[6].setName(TestState);
			anyObject = new AnyObject();
			anyObject.para_value = "Requested";
			anyObject.para_type_id = "1";
			ParameterValueStruct[6].setValue(anyObject);
			
			/*ParameterValueStruct[6] = new ParameterValueStruct();
			ParameterValueStruct[6].setName(TestState);
			anyObject = new AnyObject();
			anyObject.para_value = "Requested";
			anyObject.para_type_id = "1";
			ParameterValueStruct[6].setValue(anyObject);*/
		}
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("VoiceDial");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[9];
		parameterNamesArr[0] = TestType;
		parameterNamesArr[1] = CalledNumber;
		parameterNamesArr[2] = DialDTMFConfirmEnable;
		parameterNamesArr[3] = DialDTMFConfirmResult;
		parameterNamesArr[4] = Status;
		parameterNamesArr[5] = Conclusion;
		parameterNamesArr[6] = CallerFailReason;
		parameterNamesArr[7] = CalledFailReason;
		parameterNamesArr[8] = FailedResponseCode;
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
		Map VoiceMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
				logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
									logger.debug("[{}]DevRpcCmdOBJ.value == null",
											deviceId);
									checker.setResult(1011);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("VoiceDial==>ReturnXml:"
											+ checker.getReturnXml());
									return checker.getReturnXml();
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
										// fault =
										// XmlToRpc.Fault(soapOBJ.getRpcElement());
										Element element = soapOBJ.getRpcElement();
										if (element != null)
										{
											GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
													.GetParameterValuesResponse(element);
											if (getParameterValuesResponse != null)
											{
												ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
														.getParameterList();
												VoiceMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													VoiceMap.put(
															parameterValueStructArr[j]
																	.getName(),
															parameterValueStructArr[j]
																	.getValue().para_value);
												}
											}
											else
											{
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("VoiceDial==>ReturnXml:"
														+ checker.getReturnXml());
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("VoiceDial==>ReturnXml:"
													+ checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("VoiceDial==>ReturnXml:"
												+ checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
							// else {
							// checker.setResult(1012);
							// checker.setResultDesc("系统内部错误，无返回值");
							// logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
							// return checker.getReturnXml();
							// }
						}
					}
					else
					{
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				if (VoiceMap == null)
				{
					checker.setResult(1014);
					checker.setResultDesc("语音业务仿真失败：返回值为空。");
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					String testType = "" + VoiceMap.get(TestType);
					String calledNumber = "" + VoiceMap.get(CalledNumber);
					String dialDTMFConfirmEnable = "" + VoiceMap.get(DialDTMFConfirmEnable);
					String dialDTMFConfirmResult = "" + VoiceMap.get(DialDTMFConfirmResult);
					String status = "" + VoiceMap.get(Status);
					String conclusion = "" + VoiceMap.get(Conclusion);
					String callerFailReason = "" + VoiceMap.get(CallerFailReason);
					String calledFailReason = "" + VoiceMap.get(CalledFailReason);
					String failedResponseCode = "" + VoiceMap.get(FailedResponseCode);
					
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setTestType(testType);
					checker.setCalledNumber(calledNumber);
					checker.setDialDTMFConfirmEnable(dialDTMFConfirmEnable);
					checker.setDialDTMFConfirmResult(dialDTMFConfirmResult);
					checker.setStatus(status);
					checker.setConclusion(conclusion);
					checker.setCallerFailReason(callerFailReason);
					checker.setCalledFailReason(calledFailReason);
					checker.setFailedResponseCode(failedResponseCode);
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
	}
}
