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
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.GetSpeedResultChecker4JLLT;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;

/**
 * 吉林联通测速结果 Service
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年11月28日
 *
 */
public class GetSpeedResultService4JLLT implements IService{

	private static Logger logger = LoggerFactory.getLogger(GetSpeedResultService4JLLT.class);
	private final String methodName = "GetSpeedResultService4JLLT";
	
	@Override
	public String work(String inParam) {
		logger.warn("GetSpeedResultService4JLLT==>inParam({})",inParam);

		// 解析获得入参
		GetSpeedResultChecker4JLLT checker = new GetSpeedResultChecker4JLLT(inParam.trim());
		
		// 验证入参
		if (!checker.check()) {
			logger.warn("入参验证没通过,GetSpeedResultService4JLLT==>inParam({})",inParam);
			logger.warn("work==>inParam="+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO dao = new UserDeviceDAO();

		//web会直接传值device_id
		Map<String, String> queryUserInfo = dao.queryUserInf(StringUtil.getIntegerValue(checker.getType()), checker.getIndex());
		logger.warn(methodName+"["+checker.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		
		if(null == queryUserInfo || queryUserInfo.size()==0){
			checker.setResult(0);
			checker.setResultDesc("没有找到符合条件的终端");
			return checker.getReturnXml();
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		
		if(StringUtil.IsEmpty(deviceId)){
			logger.warn(methodName+"["+checker.getOpId()+"],deviceId{}",deviceId);
			checker.setResult(0);
			checker.setResultDesc("没有找到符合条件的终端");
			return checker.getReturnXml();
		}
		logger.warn("deviceId="+deviceId);
		String returnXml = "";
		try
		{
			returnXml = doTest(checker, deviceId);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]处理结束，返回响应信息:{}", new Object[] {returnXml});
	
		return returnXml;
		
	}

	
	
	public String doTest(GetSpeedResultChecker4JLLT checker,String deviceId) throws Exception{
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		if(flag==1){
			logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]开始获取测速结果！");
			
			GetParameterValues getParameterValues = new GetParameterValues();
			
			//本次测试PPPOE账号
			String pppoeName = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName";
			//本次测试IP
			String pppoeIP = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeIP";
			//平均下载速率，单位是M，小数点两位
			String Aspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Aspeed";
			//用户签约速率，单位是M，小数点两位
			String Bspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Bspeed";
			//当前下载速率，单位是M，小数点两位
			String Cspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Cspeed";
			//最大下载速率，单位是M，小数点两位
			String maxspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.maxspeed";
			//开始下载时间，时间戳格式，精确到秒
			String starttime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.starttime";
			//结束下载时间，格式同开始下载时间
			String endtime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.endtime";
			
			String DiagnosticsState = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState";
			//测速状态
			String status = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Status";
			//从第5秒后，第6秒到第15秒，下载的文件大小，单位是K，小数点两位
			String totalsize = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Totalsize";
			//测速期内的背景流量大小，单位是K，小数点两位（宽带WAN流量-下载文件大小）
			String backgroundsize = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.backgroundsize";
			//“serverMode”模式时，出错时服务器返回的错误码 正常时值为测速平台反馈（或上报测速结果平台）反馈的code值。另外， failcode=1，代表获取签约速率和下载服务器地址无响应，failcode=2，代表下载测速过程中无响应，failcode=3，代表上报测试结果无响应，failcode=4，代表没有PPPOE连接或者PPPOE拨号失败
			String failcode = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Failcode";
			//仿真账号
			String Eupppoename = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename";
			//仿真密码
			String Eupassword = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword";
			
			String[] parameterNamesArr = null;
			parameterNamesArr = new String[15];
			parameterNamesArr[0] = pppoeName;
			parameterNamesArr[1] = pppoeIP;
			parameterNamesArr[2] = Aspeed;
			parameterNamesArr[3] = Bspeed;
			parameterNamesArr[4] = Cspeed;
			parameterNamesArr[5] = maxspeed;
			parameterNamesArr[6] = starttime;
			parameterNamesArr[7] = endtime;
			parameterNamesArr[8] = DiagnosticsState;
			parameterNamesArr[9] = status;
			parameterNamesArr[10] = totalsize;
			parameterNamesArr[11] = backgroundsize;
			parameterNamesArr[12] = failcode;
			parameterNamesArr[13] = Eupppoename;
			parameterNamesArr[14] = Eupassword;
			
			getParameterValues.setParameterNames(parameterNamesArr);
			logger.warn("getParameterValues定义完毕--------");
			
			DevRpc[] devRPCArr = new DevRpc[1];
			devRPCArr[0] = new DevRpc();
			devRPCArr[0].devId = deviceId;
			Rpc[] rpcArr = new Rpc[1];
			rpcArr[0] = new Rpc();
			rpcArr[0].rpcId = "2";
			rpcArr[0].rpcName = "GetParameterValues";
			rpcArr[0].rpcValue = getParameterValues.toRPC();
			devRPCArr[0].rpcArr = rpcArr;
			
			List<DevRpcCmdOBJ> devRPCRep = null;
			DevRPCManager devRPCManager = new DevRPCManager("1");
			logger.warn("即将devRPCManager.execRPC");
			devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
			
			
			String errMessage = "";
			Map PPPoEMap = null;
			if (devRPCRep == null || devRPCRep.size() == 0)
			{
				logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(-1000);
				checker.setResultDesc(errMessage);
				logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]"+errMessage+" ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
				
			}
			else if (devRPCRep.get(0) == null)
			{
				logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(-1000);
				checker.setResultDesc(errMessage);
				logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]"+errMessage+" ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				int stat = devRPCRep.get(0).getStat();
				if (stat != 1)
				{
					errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
					checker.setResult(-1000);
					checker.setResultDesc(errMessage);
					logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]"+errMessage+"， ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					errMessage = "系统内部错误";
					if (devRPCRep.get(0).getRpcList() == null
							|| devRPCRep.get(0).getRpcList().size() == 0)
					{
						logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
						checker.setResult(-1000);
						checker.setResultDesc(errMessage);
						logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]"+errMessage+"，ReturnXml:"+checker.getReturnXml());
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
//									Fault fault = null;
									if (resp == null || "".equals(resp))
									{
										logger.debug("[{}]DevRpcCmdOBJ.value == null", deviceId);
										checker.setResult(-1000);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
										return checker.getReturnXml();
									}
									else
									{
										SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
										if (soapOBJ != null)
										{
//											fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
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
													checker.setResult(-1000);
													checker.setResultDesc("系统内部错误，无返回值");
													logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
													return checker.getReturnXml();
												}
											} else {
												checker.setResult(-1000);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										}else {
											checker.setResult(-1000);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
								}
								/*else {
									checker.setResult(-1000);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
									return checker.getReturnXml();
								}*/
							}
						}else {
							checker.setResult(-1000);
							checker.setResultDesc("系统内部错误，无返回值");
							logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]系统内部错误，无返回值， ReturnXml:"+checker.getReturnXml());
							return checker.getReturnXml();
						}
					}
					
					if (PPPoEMap == null){
						checker.setResult(-1000);
						checker.setResultDesc("返回值为空");
						logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]返回值为空, ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}else {
						checker.setResult(1);
						checker.setResultDesc("成功");
						checker.setDevSn(checker.getDevSn());
						checker.setStatus(""+PPPoEMap.get(status));
						checker.setPppoeName(""+PPPoEMap.get(pppoeName));
						checker.setDiagnosticsState(""+PPPoEMap.get(DiagnosticsState));
						checker.setPppoeIP(""+PPPoEMap.get(pppoeIP));
						checker.setAspeed(""+PPPoEMap.get(Aspeed));
						checker.setBspeed(""+PPPoEMap.get(Bspeed));
						checker.setCspeed(""+PPPoEMap.get(Cspeed));
						checker.setMaxspeed(""+PPPoEMap.get(maxspeed));
						checker.setStarttime(""+PPPoEMap.get(starttime));
						checker.setEndtime(""+PPPoEMap.get(endtime));
						checker.setTotalsize(""+PPPoEMap.get(totalsize));
						checker.setBackgroundsize(""+PPPoEMap.get(backgroundsize));
						checker.setFailcode(""+PPPoEMap.get(failcode));
						checker.setUserName(""+PPPoEMap.get(Eupppoename));
						checker.setPassword(""+PPPoEMap.get(Eupassword));
						
						logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]成功, ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
			}
		}
		else{
			checker.setResult(-1);
			checker.setResultDesc("终端不在线");
			logger.warn(methodName+"["+checker.getOpId()+"]" + "["+ deviceId +"]终端不在线, ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
		}
	}
}
