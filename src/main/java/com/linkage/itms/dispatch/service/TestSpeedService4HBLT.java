package com.linkage.itms.dispatch.service;

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
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.TestSpeedChecker4HBLT;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

/**
 * 河北联通测速接口 Service
 * @author fanjm 35572
 * @version 1.0
 * @since 2017年04月19日
 *
 */
public class TestSpeedService4HBLT implements IService{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedService4HBLT.class);
	@Override
	public String work(String inParam) {
		logger.warn("TestSpeedService4HBLT==>inParam({})",inParam);

		// 解析获得入参
		TestSpeedChecker4HBLT checker = new TestSpeedChecker4HBLT(inParam.trim());
		
		// 验证入参
		if (!checker.check()) {
			logger.warn("入参验证没通过,TestSpeedService4HBLT==>inParam({})",inParam);
			
			logger.warn("work==>inParam="+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		
		ArrayList<HashMap<String, String>> Infolist = userDevDao.queryDevInfo(checker.getDevSn());
		String deviceId = "";
		if(null!=Infolist && Infolist.size()!=0){
			deviceId = Infolist.get(0).get("device_id");
		}
		
		if(!"IP_Routed".equals(checker.getConnType())){
			ArrayList<HashMap<String, String>> list = userDevDao.queryNet_account(checker.getDevSn());
			if(null != list && list.size()>0){
				//如果页面有填值，直接以页面的pppoe页面为准
				if(StringUtil.IsEmpty(checker.getPppoeUserName())){
					checker.setPppoeUserName(list.get(0).get("pppoe_name"));
				}
				int num = list.size();
				num=(int)(num*Math.random());
				if(null != list.get(num)&&!list.get(num).isEmpty()){
					logger.warn("net_account and net_password is :" + list.get(num).get("net_account") +"---" + list.get(num).get("net_password"));
					checker.setUserName(list.get(num).get("net_account"));
					checker.setPassword(list.get(num).get("net_password"));
				}
			}
			else{
				logger.warn("[{}]仿真账号或密码为空！", checker.getDevSn());
				checker.setResult(1015);
				checker.setResultDesc("仿真账号或密码为空！");
				logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
			}
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
		logger.warn(
				"servicename[TestSpeedService4HBLT]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
	
		return returnXml;
		
	}

	
	
	public String doTest(TestSpeedChecker4HBLT checker,String deviceId) throws Exception{
		logger.warn("doTest begin--------");
		
		ACSCorba acsCorba = new ACSCorba();
		logger.warn("before testDeviceOnLineStatus--------");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		logger.warn("after testDeviceOnLineStatus--------");
		logger.warn("flag="+flag);
		if(flag==1){
				
			String wanWay = checker.getWanPassageWay();
			
			logger.warn("deviceId:"+ deviceId +"的WAN通道" + wanWay + "，开始下发测速！");
			
			DevRpc[] devRPCArr = new DevRpc[1];
			
			
			SetParameterValues setParameterValues = new SetParameterValues();
			
			ParameterValueStruct[] ParameterValueStruct = null;
			
			if(!"IP_Routed".equals(checker.getConnType())){
				ParameterValueStruct = new ParameterValueStruct[8];
			}
			else{
				ParameterValueStruct = new ParameterValueStruct[5];
			}
			
			
			ParameterValueStruct[0] = new ParameterValueStruct();
			ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
			AnyObject anyObject = new AnyObject();
			anyObject.para_value = "serverMode";
			anyObject.para_type_id = "1";
			ParameterValueStruct[0].setValue(anyObject);
			
			ParameterValueStruct[1] = new ParameterValueStruct();
			ParameterValueStruct[1].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
			anyObject = new AnyObject();
			anyObject.para_value = Global.TEST_SPEED_DOWN_URL;
			anyObject.para_type_id = "1";
			ParameterValueStruct[1].setValue(anyObject);
			
			ParameterValueStruct[2] = new ParameterValueStruct();
			ParameterValueStruct[2].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
			anyObject = new AnyObject();
			anyObject.para_value = checker.getWanPassageWay();
			anyObject.para_type_id = "1";
			ParameterValueStruct[2].setValue(anyObject);
			
			ParameterValueStruct[3] = new ParameterValueStruct();
			ParameterValueStruct[3].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
			anyObject = new AnyObject();
			anyObject.para_value = "Requested";
			anyObject.para_type_id = "1";
			ParameterValueStruct[3].setValue(anyObject);
			
			if(!"IP_Routed".equals(checker.getConnType())){
				ParameterValueStruct[4] = new ParameterValueStruct();
				ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getPppoeUserName();
				anyObject.para_type_id = "1";
				ParameterValueStruct[4].setValue(anyObject);
				
				ParameterValueStruct[5] = new ParameterValueStruct();
				ParameterValueStruct[5].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getUserName();
				anyObject.para_type_id = "1";
				ParameterValueStruct[5].setValue(anyObject);
				
				ParameterValueStruct[6] = new ParameterValueStruct();
				ParameterValueStruct[6].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getPassword();
				anyObject.para_type_id = "1";
				ParameterValueStruct[6].setValue(anyObject);
				
				ParameterValueStruct[7] = new ParameterValueStruct();
				ParameterValueStruct[7].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
				anyObject = new AnyObject();
				anyObject.para_value = Global.TEST_SPEED_REPORT_URL;
				anyObject.para_type_id = "1";
				ParameterValueStruct[7].setValue(anyObject);
				logger.warn(anyObject.para_value+"........");
			}
			else{
				ParameterValueStruct[4] = new ParameterValueStruct();
				ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
				anyObject = new AnyObject();
				anyObject.para_value = Global.TEST_SPEED_REPORT_URL;
				anyObject.para_type_id = "1";
				ParameterValueStruct[4].setValue(anyObject);
				logger.warn(anyObject.para_value+"........");
			}
			
			
			setParameterValues.setParameterList(ParameterValueStruct);
			setParameterValues.setParameterKey("downLoad");
			logger.warn("setParameterValues定义完毕--------,setParameterValues="+setParameterValues);
			GetParameterValues getParameterValues = new GetParameterValues();
			
			
			String pppoeName = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName";
			String pppoeIP = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeIP";
			String Aspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Aspeed";
			String Bspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Bspeed";
			String maxspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.maxspeed";
			String starttime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.starttime";
			String endtime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.endtime";
			String DiagnosticsState = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState";
			
			String[] parameterNamesArr = null;
			parameterNamesArr = new String[8];
			parameterNamesArr[0] = pppoeName;
			parameterNamesArr[1] = pppoeIP;
			parameterNamesArr[2] = Aspeed;
			parameterNamesArr[3] = Bspeed;
			parameterNamesArr[4] = maxspeed;
			parameterNamesArr[5] = starttime;
			parameterNamesArr[6] = endtime;
			parameterNamesArr[7] = DiagnosticsState;
			
			getParameterValues.setParameterNames(parameterNamesArr);
			logger.warn("getParameterValues定义完毕--------");
			
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
			DevRPCManager devRPCManager = new DevRPCManager("1");
			logger.warn("即将devRPCManager.execRPC");
			devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
			
			String errMessage = "";
			Map PPPoEMap = null;
			if (devRPCRep == null || devRPCRep.size() == 0)
			{
				logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(10071);
				checker.setResultDesc(errMessage);
				logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
				return checker.getReturnXml();
				
			}
			else if (devRPCRep.get(0) == null)
			{
				logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
				errMessage = "设备未知错误";
				checker.setResult(10072);
				checker.setResultDesc(errMessage);
				logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
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
						logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
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
										checker.setResult(1011);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
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
													checker.setResult(1008);
													checker.setResultDesc("系统内部错误，无返回值");
													logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
													return checker.getReturnXml();
												}
											} else {
												checker.setResult(1009);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										}else {
											checker.setResult(1010);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
								} 
//								else {
//									checker.setResult(1012);
//									checker.setResultDesc("系统内部错误，无返回值");
//									logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
//									return checker.getReturnXml();
//								}
							}
						}else {
							checker.setResult(1013);
							checker.setResultDesc("系统内部错误，无返回值");
							logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
							return checker.getReturnXml();
						}
					}
					
					if (PPPoEMap == null){
						checker.setResult(1014);
						checker.setResultDesc("返回值为空，PPPoE拨号仿真失败");
						logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}else {
						checker.setResult(0);
						checker.setResultDesc("成功");
						checker.setDevSn(checker.getDevSn());
						checker.setPppoeName(""+PPPoEMap.get(pppoeName));
						checker.setDiagnosticsState(""+PPPoEMap.get(DiagnosticsState));
						checker.setPppoeIP(""+PPPoEMap.get(pppoeIP));
						checker.setAspeed(""+PPPoEMap.get(Aspeed));
						checker.setBspeed(""+PPPoEMap.get(Bspeed));
						checker.setMaxspeed(""+PPPoEMap.get(maxspeed));
						checker.setStarttime(""+PPPoEMap.get(starttime));
						checker.setEndtime(""+PPPoEMap.get(endtime));
						logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
			}
				
		}
		else{
			checker.setResult(1);
			checker.setResultDesc("设备不在线");
			logger.warn("TestSpeedService4HBLT==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
		}
	}
	
	

}
