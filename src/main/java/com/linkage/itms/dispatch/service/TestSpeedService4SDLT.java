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
import com.linkage.itms.dao.DeviceTypeDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DeviceOBJ;
import com.linkage.itms.dispatch.obj.TestSpeedChecker4SDLT;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

/**
 * 山东联通测速接口 Service
 * @author fanjm 35572
 * @version 1.0
 * @since 2017年08月10日
 *
 */
public class TestSpeedService4SDLT implements IService{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedService4SDLT.class);
	private DeviceOBJ deviceObj = null;
	
	@Override
	public String work(String inParam) {
		logger.warn("TestSpeedService4SDLT==>inParam({})",inParam);

		// 解析获得入参
		TestSpeedChecker4SDLT checker = new TestSpeedChecker4SDLT(inParam.trim());
		
		// 验证入参
		if (!checker.check()) {
			logger.warn("入参验证没通过,TestSpeedService4SDLT==>inParam({})",inParam);
			
			logger.warn("work==>inParam="+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		
		ArrayList<HashMap<String, String>> Infolist = userDevDao.queryDevUser4SD(checker.getDevSn(), checker.getTestType());
		logger.warn("InfoList.seiz="+Infolist.size());
		
		
		if (null==Infolist || Infolist.size()==0 || null == Infolist.get(0)) {
			logger.warn(
					"servicename[TestSpeedService4SDLT]cmdId[{}]devSn[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getDevSn()});
			checker.setResult(1001);
			checker.setResultDesc("无此客户信息");
		} 
		else{
			if (StringUtil.IsEmpty(Infolist.get(0).get("device_id"))) {
				// 未绑定设备
				logger.warn("servicename[TestSpeedService4SDLT]cmdId[{}]devSn[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getDevSn()});
				checker.setResult(1002);
				checker.setResultDesc("此用户未绑定设备");
			}
			else if(StringUtil.IsEmpty(Infolist.get(0).get("username"))){
				logger.warn("servicename[TestSpeedService4SDLT]cmdId[{}]devSn[{}]不存在对应[{}]测速类型账号",
						new Object[] { checker.getCmdId(), checker.getDevSn(), checker.getTestType()});
				checker.setResult(1);
				checker.setResultDesc("不存在对应测速类型账号");
			}
			else {
				logger.warn("准备测速");
				Map<String,String> info = Infolist.get(0);
				deviceObj = new DeviceOBJ();
				deviceObj.setDevId(info.get("device_id"));
				deviceObj.setUsername(info.get("username"));
				deviceObj.setTestType(StringUtil.getIntegerValue(info.get("serv_type_id")));
				deviceObj.setCityId(info.get("city_id"));
				deviceObj.setUserId(StringUtil.getLongValue(info.get("user_id")));
				checker.setCityName(Global.G_CityId_CityName_Map.get(info.get("city_id")));
				checker.setLoid(info.get("loid"));
				checker.setUserName(info.get("username"));
				
				DeviceTypeDAO deviceTypeDao = new DeviceTypeDAO();
				deviceTypeDao.queryDeviceType(StringUtil.getIntegerValue(info.get("devicetype_id")));
				checker.setDeviceVendor(deviceTypeDao.getDeviceVendor());
				checker.setDeviceModel(deviceTypeDao.getDeviceModel());
				checker.setSoftwareversion(deviceTypeDao.getDeviceSoftwareversion());
				
				
				String returnXml = "";
				try
				{
					returnXml = doTest(checker);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.warn(
						"servicename[TestSpeedService4SDLT]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getDevSn(),returnXml});
			
				return returnXml;
				
			}
		}
		
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[TestSpeedService]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(),returnXml});
	
		return returnXml;
		
	}

	
	public String doTest(TestSpeedChecker4SDLT checker){
		logger.warn("doTest()==>方法开始");
		// 查询用户设备信息
		UserDeviceDAO dao = new UserDeviceDAO();

		String cityId = deviceObj.getCityId();
		String deviceId = deviceObj.getDevId();
		long userId = deviceObj.getUserId();
		int servTypeId = deviceObj.getTestType();
		String username = deviceObj.getUsername();
		
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		logger.warn("before flag");
		int flag = getStatus.testDeviceOnLineStatus(deviceId,
				acsCorba);
		logger.warn("after flag="+flag);
		if(flag==1){
			logger.warn("[{}]start to testSpeed...", deviceId);
			String rate = dao.getRateFromUserId(userId, servTypeId, deviceObj.getUsername());
			logger.warn("[{}]根据用户id[{}]、测试类型[{}]、业务账号[{}]查询速率结果[{}]", new Object[]{deviceId, userId, servTypeId, username, rate});
			Map<String,String> netAccount = dao.getTestAccountPar(rate,cityId);//new HashMap<String,String>();//
			String netUsername = "";
			String netPassword = "";
			if(netAccount != null && netAccount.size() > 0){
				netUsername = StringUtil.getStringValue(netAccount, "net_account");
				netPassword = StringUtil.getStringValue(netAccount, "net_password");
				
				String wanWay = "";
				int result = -1;
				logger.warn("[{}]开始获取WAN通道信息", new Object[]{deviceId});
				//获得WAN通道 顺便取出IP节点
				Map<String,String> wanConnDeviceMap = this.gatherWanPassageWay(deviceId,acsCorba);
				if(wanConnDeviceMap == null || wanConnDeviceMap.isEmpty())
				{
					logger.warn("[{}]没有获取到WAN通道信息，结束线程",deviceId);
					checker.setResult(10071);
					checker.setResultDesc("没有获取到WAN通道信息，结束线程");
					logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}
				else//成功获取WAN通道信息
				{
					logger.warn("[{}]成功获取到WAN通道信息，结束线程",deviceId);
					String servList = "INTERNET";
					if(servTypeId != 10){
						servList = "IPTV";
					}
					for(String key: wanConnDeviceMap.keySet())
					{
						logger.warn("key:" +key);
						logger.warn("VALUE:" +wanConnDeviceMap.get(key));
						if(key.startsWith(servList))
						{
							wanWay = wanConnDeviceMap.get(key);
						}
					}
					logger.warn("deviceId:"+ deviceId +"获取到WAN通道信息成功" + wanWay + "，开始下发测速！");
					
					DevRpc[] devRPCArr = new DevRpc[1];
					
					SetParameterValues setParameterValues = new SetParameterValues();
					
					ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[8];
					
					ParameterValueStruct[0] = new ParameterValueStruct();
					ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
					AnyObject anyObject = new AnyObject();
					anyObject.para_value = "serverMode";
					anyObject.para_type_id = "1";
					ParameterValueStruct[0].setValue(anyObject);
					
					ParameterValueStruct[1] = new ParameterValueStruct();
					ParameterValueStruct[1].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
					anyObject = new AnyObject();
					anyObject.para_value = Global.testURL;
					anyObject.para_type_id = "1";
					ParameterValueStruct[1].setValue(anyObject);
					
					ParameterValueStruct[2] = new ParameterValueStruct();
					ParameterValueStruct[2].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
					anyObject = new AnyObject();
					anyObject.para_value = wanWay;
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
					anyObject.para_value = username;
					anyObject.para_type_id = "1";
					ParameterValueStruct[4].setValue(anyObject);
					
					ParameterValueStruct[5] = new ParameterValueStruct();
					ParameterValueStruct[5].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
					anyObject = new AnyObject();
					anyObject.para_value = netUsername;
					anyObject.para_type_id = "1";
					ParameterValueStruct[5].setValue(anyObject);
					
					ParameterValueStruct[6] = new ParameterValueStruct();
					ParameterValueStruct[6].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
					anyObject = new AnyObject();
					anyObject.para_value = netPassword;
					anyObject.para_type_id = "1";
					ParameterValueStruct[6].setValue(anyObject);
					
					ParameterValueStruct[7] = new ParameterValueStruct();
					ParameterValueStruct[7].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
					anyObject = new AnyObject();
					anyObject.para_value = Global.reportURL;
					anyObject.para_type_id = "1";
					ParameterValueStruct[7].setValue(anyObject);
					logger.warn(anyObject.para_value+"........");
					
					setParameterValues.setParameterList(ParameterValueStruct);
					setParameterValues.setParameterKey("downLoad");
					logger.warn("setParameterValues定义完毕--------,setParameterValues="+setParameterValues);
					GetParameterValues getParameterValues = new GetParameterValues();
					
					
					String Aspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Aspeed";
					String maxspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.maxspeed";
					
					String[] parameterNamesArr = null;
					parameterNamesArr = new String[2];
					parameterNamesArr[0] = Aspeed;
					parameterNamesArr[1] = maxspeed;
					
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
						logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
						
					}
					else if (devRPCRep.get(0) == null)
					{
						logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
						errMessage = "设备未知错误";
						checker.setResult(10072);
						checker.setResultDesc(errMessage);
						logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
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
							logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
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
								logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
								return checker.getReturnXml();
							}
							else
							{
								List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
								if (rpcList != null && !rpcList.isEmpty())
								{
									logger.warn("rpcList.size()="+rpcList.size());
									
									for (int k = 0; k < rpcList.size(); k++)
									{
										if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName()))
										{
											String resp = rpcList.get(k).getValue();
											logger.warn("[{}]设备返回：{}", deviceId, resp);
//											Fault fault = null;
											if (resp == null || "".equals(resp))
											{
												logger.debug("[{}]DevRpcCmdOBJ.value == null", deviceId);
												checker.setResult(1011);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("TestSpeedService4SDLT==>ReturnXml1:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
											else
											{
												SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
												if (soapOBJ != null)
												{
//													fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
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
															logger.warn("TestSpeedService4SDLT==>ReturnXml2:"+checker.getReturnXml());
															return checker.getReturnXml();
														}
													} else {
														checker.setResult(1009);
														checker.setResultDesc("系统内部错误，无返回值");
														logger.warn("TestSpeedService4SDLT==>ReturnXml3:"+checker.getReturnXml());
														return checker.getReturnXml();
													}
												}else {
													checker.setResult(1010);
													checker.setResultDesc("系统内部错误，无返回值");
													logger.warn("TestSpeedService4SDLT==>ReturnXml4:"+checker.getReturnXml());
													return checker.getReturnXml();
												}
											}
										} 
										/*else {
											checker.setResult(1012);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("TestSpeedService4SDLT==>ReturnXml5:"+checker.getReturnXml());
											return checker.getReturnXml();
										}*/
									}
								}else {
									checker.setResult(1013);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("TestSpeedService4SDLT==>ReturnXml6:"+checker.getReturnXml());
									return checker.getReturnXml();
								}
							}
							
							if (PPPoEMap == null){
								checker.setResult(1014);
								checker.setResultDesc("返回值为空，PPPoE拨号仿真失败");
								logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
								return checker.getReturnXml();
							}else {
								checker.setResult(0);
								checker.setResultDesc("成功");
								checker.setDevSn(checker.getDevSn());
								checker.setAspeed(""+PPPoEMap.get(Aspeed));
								checker.setMaxspeed(""+PPPoEMap.get(maxspeed));
								logger.warn("TestSpeedService4SDLT==>ReturnXml:"+checker.getReturnXml());
								return checker.getReturnXml();
							}
						}
					}
				}
				
				
			}else{
				checker.setResult(1);
				checker.setResultDesc("未获取到测速账号和密码，结束");
				logger.warn("[{}]未获取到测速账号和密码，结束", deviceId);
				return checker.getReturnXml();
			}
		}
		else{
			checker.setResult(1);
			checker.setResultDesc("设备不在线，未测速");
			logger.warn("[{}]device is not online，结束", deviceId);
			return checker.getReturnXml();
		}
	}
	
	
	/**
	 * 获取测速路径
	 * 
	 * @param deviceId
	 * @return
	 */
	private Map<String, String> gatherWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		String SERV_LIST_TR069 = "TR069";
		String SERV_LIST_VOIP = "VOIP";
		String SERV_LIST_IPTV = "IPTV";
		String SERV_LIST_OTHER = "OTHER";
		Map<String, String> restMap = new HashMap<String, String>();
		// logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";

		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		logger.warn("wanConnPathsList.size:{},wanConnPath:{},deviceId{}",wanConnPathsList.size(),wanConnPath,deviceId);
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",
					deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
			}
			for (String j : jList) {
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0
						|| kPPPList.isEmpty()) {
					logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败",
							deviceId);
				} else {
					for (String kppp : kPPPList) {
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
					}
				}
			}
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++) {
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0) {
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
			logger.warn(
					"[TestSpeedService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
					deviceId);
		} else {
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList) {
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++) {
				String[] paramNametemp = new String[paramNameArr.length
						- (k * 20) > 20 ? 20 : paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++) {
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty()) {
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedService] [{}]获取ServiceList失败",
						deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				logger.debug(
						"[{}]{}={} ",
						new Object[]{deviceId, entry.getKey(), entry.getValue()});
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0) {
				} else if (paramName.indexOf(wanIPConnection) >= 0) {
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0) {
					if (!StringUtil.IsEmpty(entry.getValue())) {// X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf(wanServiceList));
						if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
							restMap.put(SERV_LIST_INTERNET, res);
						} else if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
							restMap.put(SERV_LIST_VOIP, res);
						} else if (entry.getValue().indexOf(SERV_LIST_IPTV) >= 0) {
							restMap.put(SERV_LIST_IPTV, res);
						} else if (entry.getValue().indexOf(SERV_LIST_TR069) >= 0) {
							restMap.put(SERV_LIST_TR069, res);
						} else if (entry.getValue().indexOf(SERV_LIST_OTHER) >= 0) {
							restMap.put(SERV_LIST_OTHER, res);
						}
					}
				}
			}
		}
		return restMap;
	}
	

}
