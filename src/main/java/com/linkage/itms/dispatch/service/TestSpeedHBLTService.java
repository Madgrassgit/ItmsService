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
import com.linkage.itms.dispatch.obj.TestSpeedHBLTChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

/**
 * 河北联通端到端测速
 * @author jiafh
 *
 */
public class TestSpeedHBLTService implements IService{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedHBLTService.class);
		
	@Override
	public String work(String inParam) {
		logger.warn("TestSpeedHBLTService==>inParam({})",inParam);

		// 解析获得入参
		TestSpeedHBLTChecker checker = new TestSpeedHBLTChecker(inParam.trim());
		// 验证入参
		if (!checker.check()) {
			logger.warn("入参验证没通过,TestSpeedHBLTService==>{}",checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String deviceId = null;
		String userId = null;
		String devSn = null;
		
		ArrayList<HashMap<String, String>> userDevInfoList = new ArrayList<HashMap<String,String>>();
		if(1 == checker.getUserInfoType()){
			// 根据LOID查询用户设备信息
			userDevInfoList = userDevDao.queryDeviceInfoByLoid(checker.getUserInfo());
			if(null == userDevInfoList || userDevInfoList.isEmpty() || null == userDevInfoList.get(0) || userDevInfoList.get(0).isEmpty()){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1001);
				checker.setResultDesc("无此客户信息");
			}else if(userDevInfoList.size() > 1){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]LOID对应多条信息",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1006);
				checker.setResultDesc("LOID查询到多条信息");
			}else if(StringUtil.IsEmpty(userDevInfoList.get(0).get("device_id"))){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]用户未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("用户未绑定设备");
			}else{
				deviceId = userDevInfoList.get(0).get("device_id");
				userId = userDevInfoList.get(0).get("user_id");
				devSn = userDevInfoList.get(0).get("device_serialnumber");
			}
			
		}else{
			// 根据设备序列号查询设备用户信息
			userDevInfoList = userDevDao.queryDeviceInfoByDevSn(checker.getUserInfo());
			if(null == userDevInfoList || userDevInfoList.isEmpty() || null == userDevInfoList.get(0) || userDevInfoList.get(0).isEmpty()){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1003);
				checker.setResultDesc("无此设备信息");
			}else if(userDevInfoList.size() > 1){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]设备序列号对应多条信息",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1007);
				checker.setResultDesc("设备序列号查询到多条信息");
			}else if(StringUtil.IsEmpty(userDevInfoList.get(0).get("user_id"))){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]设备未绑定用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("设备未绑定用户");
			}else{
				deviceId = userDevInfoList.get(0).get("device_id");
				userId = userDevInfoList.get(0).get("user_id");
				devSn = userDevInfoList.get(0).get("device_serialnumber");
			}
		}
		if(!StringUtil.IsEmpty(deviceId)&&!StringUtil.IsEmpty(userId)){
			// 根据userId查询上网方式
			ArrayList<HashMap<String, String>> servInfoList = userDevDao.queryWanTypeByUserId(userId);
			if(null == servInfoList || servInfoList.isEmpty() || null == servInfoList.get(0) || servInfoList.get(0).isEmpty()){
				logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]无上网业务",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1008);
				checker.setResultDesc("无上网业务");
			}else{
				String wanType = servInfoList.get(0).get("wan_type");
				if("1".equals(wanType)){
					// 根据宽带账号查询测试用户名、密码
					ArrayList<HashMap<String, String>> speedInfoList = userDevDao.queryNet_account(devSn);
					if(null != speedInfoList&&!speedInfoList.isEmpty()){
						int num = speedInfoList.size();
						num=(int)(num*Math.random());
						if(null != speedInfoList.get(num)&&!speedInfoList.get(num).isEmpty()){
							logger.warn("net_account and net_password is :" + speedInfoList.get(num).get("net_account") +"---" + speedInfoList.get(num).get("net_password"));
							checker.setUserName(speedInfoList.get(num).get("net_account"));
							checker.setPassword(speedInfoList.get(num).get("net_password"));
						}else{
							
							logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]桥接方式查询不到测速账号",
									new Object[] { checker.getCmdId(), checker.getUserInfo()});
							checker.setResult(1009);
							checker.setResultDesc("桥接方式查询不到测速账号");
							return checker.getReturnXml();
						}
					}else{
						
						logger.warn("servicename[TestSpeedHBLTService]cmdId[{}]userinfo[{}]桥接方式查询不到测速账号",
								new Object[] { checker.getCmdId(), checker.getUserInfo()});
						checker.setResult(1009);
						checker.setResultDesc("桥接方式查询不到测速账号");
						return checker.getReturnXml();
					}
				}
				checker.setDevSn(devSn);
				this.doTest(checker, deviceId, wanType);	
			}
		}
		
		String returnXml = checker.getReturnXml();
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
	public void doTest(TestSpeedHBLTChecker checker,String deviceId,String wanType){
		logger.debug("doTest begin--------");
		
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
						if(!StringUtil.IsEmpty(checker.getVlanId()) && StringUtil.IsEmpty(checker.getConType())){
							if(checker.getVlanId().equals(key.split("###")[1]) 
									&& checker.getConType().equals(key.split("###")[2])){
								wanPassageWay = wanPassageWayMap.get(key);
							}						
							break;
						}else{
							
							// 如果没有多宽带，取第一个
							wanPassageWay = wanPassageWayMap.get(key);
							break;
						}
					}	
				}
				DevRpc[] devRPCArr = new DevRpc[1];
				SetParameterValues setParameterValues = new SetParameterValues();	
				ParameterValueStruct[] ParameterValueStruct = null;			
				if("1".equals(wanType)){
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
				
				if("1".equals(wanType)){
					ParameterValueStruct[4] = new ParameterValueStruct();
					ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
					anyObject = new AnyObject();
					anyObject.para_value = checker.getNetAccount();
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
					anyObject.para_value = checker.getTestSpeedReportUrl();
					anyObject.para_type_id = "1";
					ParameterValueStruct[7].setValue(anyObject);
				}
				else{
					ParameterValueStruct[4] = new ParameterValueStruct();
					ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
					anyObject = new AnyObject();
					anyObject.para_value = checker.getTestSpeedReportUrl();
					anyObject.para_type_id = "1";
					ParameterValueStruct[4].setValue(anyObject);
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
				}
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
				logger.warn("[TestSpeedHBLTService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
			}else{
				for (String j : jList) {
					wanConnPathsList.add(wanConnPath + j + wanVlan);
					
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
					if (null == kPPPList || kPPPList.isEmpty()) {
						logger.warn("[TestSpeedHBLTService] [{}]获取" + wanConnPath + wanConnPath + j + wanPPPConnection + "下实例号失败",deviceId);
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
				logger.warn("[TestSpeedHBLTService] [{}]获取ServiceList失败",deviceId);
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
