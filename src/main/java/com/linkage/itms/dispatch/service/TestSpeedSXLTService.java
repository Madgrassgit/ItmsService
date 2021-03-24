package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.TestSpeedSXLTChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 *@描述 山西联通光猫测速接口
 *@参数
 *@返回值
 *@创建人  lsr
 *@创建时间  2019/8/20
 *@throws
 *@修改人和其它信息
 */
public class TestSpeedSXLTService implements IService{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedSXLTService.class);
	private long id = RecordLogDAO.getRandomId();	
	
	@Override
	public String work(String inParam) {
		logger.warn("TestSpeedSXLTService==>inParam({})",inParam);
		new RecordLogDAO().recordLog(id, inParam, "testSpeed");
		
		// 解析获得入参
		TestSpeedSXLTChecker checker = new TestSpeedSXLTChecker(inParam.trim());
		// 验证入参
		if (!checker.check()) {
			logger.warn("入参验证没通过,TestSpeedSXLTService==>{}",checker.getReturnXml());
			new RecordLogDAO().recordDispatchLog(checker,id,"");
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String deviceId = null;
		String userId = null;
		String devSn = null;
		
		ArrayList<HashMap<String, String>> userDevInfoList = new ArrayList<HashMap<String,String>>();
		// 根据LOID查询用户设备信息
		userDevInfoList = userDevDao.queryDeviceInfoByAccount(checker.getNetAccount());
		if(null == userDevInfoList || userDevInfoList.isEmpty() || null == userDevInfoList.get(0) || userDevInfoList.get(0).isEmpty()){
			logger.warn("servicename[TestSpeedSXLTService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(0);
			checker.setResultDesc("无此客户信息");
		}else if(userDevInfoList.size() > 1){
			logger.warn("servicename[TestSpeedSXLTService]cmdId[{}]userinfo[{}]LOID对应多条信息",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(userDevInfoList.size());
			checker.setResultDesc("LOID查询到多条信息");
		}else if(StringUtil.IsEmpty(userDevInfoList.get(0).get("device_id"))){
			logger.warn("servicename[TestSpeedSXLTService]cmdId[{}]userinfo[{}]用户未绑定设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(0);
			checker.setResultDesc("用户未绑定设备");
		}else{
			deviceId = userDevInfoList.get(0).get("device_id");
			userId = userDevInfoList.get(0).get("user_id");
			devSn = userDevInfoList.get(0).get("device_serialnumber");
			checker.setVlanId(userDevInfoList.get(0).get("vlanid"));
		}
			
		if(!StringUtil.IsEmpty(deviceId)&&!StringUtil.IsEmpty(userId)){
			ACSCorba acsCorba = new ACSCorba();
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			// 判断设备是否在线
			int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
			if(flag == 1){
				checker.setResult(1);
				checker.setResultDesc("成功");
				logger.error(Global.G_TestSpeedThreadPool.getThreadPoolMonitorInfo());
				Global.G_TestSpeedThreadPool.execute(new TestSpeedSXLTThread(checker, id, deviceId));
			}
			else{
				checker.setResult(-1);
				checker.setResultDesc("设备不在线");
			}
		}
		
		String returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,id,"");
		logger.warn("servicename[TestSpeedService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		return returnXml;
		
	}

	
	/**
	 * 仿真测速
	 * @param checker
	 * @param deviceId
	 * @param wanType
	 */
	public void doTest(TestSpeedSXLTChecker checker,String deviceId){
		
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		
		// 判断设备是否在线
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);	
		if(flag == 1){
			// 获取wan通道
			Map<String,String> wanPassageWayMap = gatherWanPassageWay(deviceId,acsCorba);
			
			if(null != wanPassageWayMap && !wanPassageWayMap.isEmpty()){
				String wanPassageWay = null;
				for(String key: wanPassageWayMap.keySet()){
					
					if(key.startsWith("INTERNET")){
						if(!StringUtil.IsEmpty(checker.getVlanId())){
							if(checker.getVlanId().equals(key.split("###")[1])){
								wanPassageWay = wanPassageWayMap.get(key);
								break;
							}
						}else{
							
							// 如果没有多宽带，取第一个
							wanPassageWay = wanPassageWayMap.get(key);
							break;
						}
					}	
				}
				
				logger.warn("wanPassageWay="+wanPassageWay);
				
				ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();
				ParameValueOBJ obj1 = new ParameValueOBJ();
				obj1.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
				obj1.setValue(checker.getSpeedTest_testMode());
				obj1.setType("1");
				
				ParameValueOBJ obj2 = new ParameValueOBJ();
				obj2.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
				obj2.setValue(checker.getSpeedTest_testURL());
				obj2.setType("1");
				
				ParameValueOBJ obj3 = new ParameValueOBJ();
				obj3.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
				obj3.setValue(wanPassageWay);
				obj3.setType("1");
				
				ParameValueOBJ obj4 = new ParameValueOBJ();
				obj4.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
				obj4.setValue("Requested");
				obj4.setType("1");
				
				ParameValueOBJ obj5 = new ParameValueOBJ();
				obj5.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
				obj5.setValue(checker.getPppoeUserName());
				obj5.setType("1");
				
				ParameValueOBJ obj6 = new ParameValueOBJ();
				obj6.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
				obj6.setValue(checker.getEupppoename());
				obj6.setType("1");
				
				ParameValueOBJ obj7 = new ParameValueOBJ();
				obj7.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
				obj7.setValue(checker.getEupassword());
				obj7.setType("1");
				
				ParameValueOBJ obj8 = new ParameValueOBJ();
				obj8.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
				obj8.setValue(checker.getSpeedTest_reportURL());
				obj8.setType("1");
				
				ParameValueOBJ obj9 = new ParameValueOBJ();
				obj9.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.backgroundsize");
				obj9.setValue(checker.getBackgroundsize());
				obj9.setType("1");
				
				parameList.add(obj1);
				parameList.add(obj2);
				parameList.add(obj3);
				parameList.add(obj4);
				parameList.add(obj5);
				parameList.add(obj6);
				parameList.add(obj7);
				parameList.add(obj8);
				parameList.add(obj9);
				
				int result = acsCorba.setValue(deviceId, parameList);
				if (0 == result || 1 == result)
				{
					checker.setResult(0);
					checker.setResultDesc("成功");
				}
				else{
					checker.setResult(1000);
					checker.setResultDesc("下发测速错误");
				}
				/*DevRpc[] devRPCArr = new DevRpc[1];
				SetParameterValues setParameterValues = new SetParameterValues();	
				ParameterValueStruct[] ParameterValueStruct = null;			
				ParameterValueStruct = new ParameterValueStruct[8];
					
				ParameterValueStruct[0] = new ParameterValueStruct();
				ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
				AnyObject anyObject = new AnyObject();
				anyObject.para_value = "serverMode";
				anyObject.para_type_id = "1";
				ParameterValueStruct[0].setValue(anyObject);
				
				ParameterValueStruct[1] = new ParameterValueStruct();
				ParameterValueStruct[1].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getTestSpeedDownUrl();
				anyObject.para_type_id = "1";
				ParameterValueStruct[1].setValue(anyObject);
				
				ParameterValueStruct[2] = new ParameterValueStruct();
				ParameterValueStruct[2].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
				anyObject = new AnyObject();
				anyObject.para_value = wanPassageWay;
				anyObject.para_type_id = "1";
				ParameterValueStruct[2].setValue(anyObject);
				
				ParameterValueStruct[3] = new ParameterValueStruct();
				ParameterValueStruct[3].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
				anyObject = new AnyObject();
				anyObject.para_value = "Requested";
				anyObject.para_type_id = "1";
				ParameterValueStruct[3].setValue(anyObject);
				
				ParameterValueStruct[4] = new ParameterValueStruct();
				ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getNetAccount();
				anyObject.para_type_id = "1";
				ParameterValueStruct[4].setValue(anyObject);
				
				ParameterValueStruct[5] = new ParameterValueStruct();
				ParameterValueStruct[5].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getEupppoename();
				anyObject.para_type_id = "1";
				ParameterValueStruct[5].setValue(anyObject);
				
				ParameterValueStruct[6] = new ParameterValueStruct();
				ParameterValueStruct[6].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getEupassword();
				anyObject.para_type_id = "1";
				ParameterValueStruct[6].setValue(anyObject);
				
				ParameterValueStruct[7] = new ParameterValueStruct();
				ParameterValueStruct[7].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
				anyObject = new AnyObject();
				anyObject.para_value = checker.getTestSpeedReportUrl();
				anyObject.para_type_id = "1";
				ParameterValueStruct[7].setValue(anyObject);*/
			
				
				/*setParameterValues.setParameterList(ParameterValueStruct);
				setParameterValues.setParameterKey("downLoad");
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
				
				devRPCArr[0] = new DevRpc();
				devRPCArr[0].devId = deviceId;
				Rpc[] rpcArr = new Rpc[1];
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
				devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
				
				//String errMessage = "";
				Map<String,String> PPPoEMap = null;
				if(null != devRPCRep && !devRPCRep.isEmpty() && null != devRPCRep.get(0)){

					int stat = devRPCRep.get(0).getStat();
					if (stat != 1)
					{
						checker.setResult(1000);
						checker.setResultDesc("未知错误，PPPoE拨号仿真测速失败");
//						checker.setResult(stat);
//						checker.setResultDesc(Global.G_Fault_Map.get(stat).getFaultDesc());
						
					}
					else
					{
						if(null != devRPCRep.get(0).getRpcList() || !devRPCRep.get(0).getRpcList().isEmpty()){
							List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
							if (rpcList != null && !rpcList.isEmpty())
							{
								for (int k = 0; k < rpcList.size(); k++)
								{
									if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName()))
									{
										String resp = rpcList.get(k).getValue();
										logger.warn("[{}]设备返回：{}", deviceId, resp);
										if (!StringUtil.IsEmpty(resp))
										{
											SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
											if (soapOBJ != null)
											{
												Element element = soapOBJ.getRpcElement();
												if (element != null)
												{
													GetParameterValuesResponse getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(element);
													if (getParameterValuesResponse != null)
													{
														ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
																.getParameterList();
														PPPoEMap = new HashMap<String, String>();
														for (int j = 0; j < parameterValueStructArr.length; j++)
														{
															PPPoEMap.put(parameterValueStructArr[j].getName(),parameterValueStructArr[j].getValue().para_value);
														}
													}
												}
											}									
										}
									}
								}
							}
						
						}else{
							logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
						}
						
						if(null != PPPoEMap && !PPPoEMap.isEmpty()){
							checker.setResult(0);
							checker.setResultDesc("成功");
							//checker.setDevSn(checker.getDevSn());
							checker.setNetAccount(StringUtil.getStringValue(PPPoEMap.get(pppoeName)));							
							checker.setTestIP(StringUtil.getStringValue(PPPoEMap.get(pppoeIP)));
							checker.setAvgSpeed(StringUtil.getStringValue(PPPoEMap.get(Aspeed)));
							checker.setSignSpeed(StringUtil.getStringValue(PPPoEMap.get(Bspeed)));
							checker.setMaxSpeed(StringUtil.getStringValue(PPPoEMap.get(maxspeed)));
							checker.setStarttime(StringUtil.getStringValue(PPPoEMap.get(starttime)));
							checker.setEndtime(StringUtil.getStringValue(PPPoEMap.get(endtime)));
							checker.setProcessState(StringUtil.getStringValue(PPPoEMap.get(DiagnosticsState)));
						}else{
							checker.setResult(1000);
							checker.setResultDesc("未知错误，PPPoE拨号仿真失败");
						}
					}
				
				}else{
					logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
				}*/	
			}
			else{
				checker.setResult(1000);
				checker.setResultDesc("获取不到wan通道");
			}
		}
		else{
			checker.setResult(1005);
			checker.setResultDesc("设备不在线");
		}
	}
	
	
	
	/**
	 * 获取测速路径
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> gatherWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		Map<String, String> restMap = new HashMap<String, String>();
		
		// logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String wanVlan = ".X_CU_VLAN";
		String connectionType = ".ConnectionType";

		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		 ArrayList<String> wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		
		if (null == wanConnPathsList  || wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.isEmpty()) {
				logger.warn("[TestSpeedSXLTService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
			}else{
				for (String j : jList) {
					wanConnPathsList.add(wanConnPath + j + wanVlan);
					
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
					if (null == kPPPList || kPPPList.isEmpty()) {
						logger.warn("[TestSpeedSXLTService] [{}]获取" + wanConnPath + wanConnPath + j + wanPPPConnection + "下实例号失败",deviceId);
					} else {
						for (String kppp : kPPPList) {
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + wanServiceList);
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + connectionType);
						}
					}
				}
			}	
		}
		
		if(null != wanConnPathsList && !wanConnPathsList.isEmpty()){
			
			List<String> tempWanConnPathsList = new ArrayList<String>();
			for(String wanConnPaths : wanConnPathsList){
				if(wanConnPaths.endsWith(".X_CU_ServiceList") || wanConnPaths.endsWith(".X_CU_VLAN")
						|| wanConnPaths.endsWith(".ConnectionType")){
					tempWanConnPathsList.add(wanConnPaths);
				}
			}
			
			String[] paramNameArr = new String[tempWanConnPathsList.size()];
			for(int index=0;index<tempWanConnPathsList.size();index++){
				paramNameArr[index] = tempWanConnPathsList.get(index);
			}
			
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,paramNameArr);
			if (null == paramValueMap || paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedSXLTService] [{}]获取ServiceList失败",deviceId);
			}else{
				for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
					logger.debug("[{}]{}={} ",new Object[]{deviceId, entry.getKey(), entry.getValue()});
					String paramName = entry.getKey();
					if (paramName.endsWith(wanServiceList)) {
						if (!StringUtil.IsEmpty(entry.getValue())) {
							String res = entry.getKey().substring(0,entry.getKey().indexOf(wanServiceList));
							String vlanKey = "";
							String vlanValue = "";
							String conTypeKey = "";
							String conTypeValue = "";
							
							if(entry.getKey().indexOf(wanPPPConnection) > 0){
								vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanPPPConnection)) + wanVlan;
								vlanValue = paramValueMap.get(vlanKey);
								conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
								conTypeValue = paramValueMap.get(conTypeKey);
							}else{
								vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanIPConnection)) + wanVlan;
								vlanValue = paramValueMap.get(vlanKey);
								conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
								conTypeValue = paramValueMap.get(conTypeKey);
							}
							
							if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
								restMap.put(SERV_LIST_INTERNET+"###"+vlanValue+"###"+conTypeValue, res);
							}
						}
					}
				}
			}
		}
		return restMap;
	}
}
