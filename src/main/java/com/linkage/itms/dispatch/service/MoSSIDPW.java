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
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.MoSSIDPWChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;


public class MoSSIDPW implements IService {

	private static final Logger logger = LoggerFactory.getLogger(MoSSIDPW.class);
	
	private MoSSIDPWChecker checker;
	
	private Map<String,String> map;
	
	public String work(String inXml) {
		checker = new MoSSIDPWChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),
						inXml });
		//获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		//测试是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		
		String deviceId = "";

		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
				.getUserInfoType(), checker.getUsername());
		
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUsername()});
			checker.setResult(1002);
			checker.setResultDesc("查不到对应的客户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 用户信息存在   再判断此用户是否绑定了设备
		else {
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
			
			if ("".equals(deviceId)) {
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]此用户未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			// 判断设备是否在线，只有设备在线，才可以设置设备的节点信息
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			
			// 设备正在被操作
			if (-3 == flag) {
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]设备正在被操作，无法设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1008);
				checker.setResultDesc("设备正在被操作");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]设备在线，可以设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				//获取设备无线的加密方式
				String beaconType = this.getSSIDPWType(deviceId, checker.getSsidType());
				ParameValueOBJ pvOBJ = checker.getPvOBJ();
				map = checker.getMap();
				if("WPA".equals(beaconType)){
					pvOBJ.setName(map.get("WPA"+checker.getSsidType()));
				}else if("Basic".equals(beaconType)){
					pvOBJ.setName(map.get("WEP"+checker.getSsidType()));
				}else if("None".equals(beaconType)){
					checker.setResultDesc("该设备加密方式为None");
				}else{
					
				}
				checker.setPvOBJ(pvOBJ);
				// 调用Corba 设置节点的值
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]调用Corba，设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				int retResult = corba.setValue(deviceId, checker.getPvOBJ());
				
				if (0 == retResult || 1 == retResult) {
					 checker.setResult(0);
					 checker.setIsSucc(0);
					 checker.setResultDesc("节点值设置成功");
					 String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-1 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("设备连接失败");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-6 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("设备正被操作");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-7 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("系统参数错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else if (-9 == retResult) {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("系统内部错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}else {
					checker.setResult(1000);
					checker.setIsSucc(1);
					checker.setResultDesc("TR069错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
					logger.warn(
							"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
					 return returnXml;
				}
			}
			// 设备不在线
			else {
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]设备不在线，无法设置节点值",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1000);
				checker.setIsSucc(1);
				checker.setResultDesc("设备不在线，无法设置节点值");
				String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "MoSSIDPW");
				logger.warn(
						"servicename[MoSSIDPW]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
				 return returnXml;
			}
		}
	}
	
	/**
	 * 获取设备的加密方式
	 * @param deviceId
	 * @param ssidType
	 * @return
	 */
	public String getSSIDPWType(String deviceId,int ssidType){
		
		logger.warn("getSSIDPWType()");
	    
		//加密节点路径
		String nodePath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+ ssidType +".BeaconType";
	    
		GetParameterValues getParameterValues = new GetParameterValues();
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		String[] parameterNamesArr = new String[1];
		parameterNamesArr[0] = nodePath;
		getParameterValues.setParameterNames(parameterNamesArr);
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "GetParameterValues";
		rpcArr[0].rpcValue = getParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager("1");
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		
		String errMessage = "";
		Map<String,String> resultMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
			return checker.getReturnXml();
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
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
				logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
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
					logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
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
									logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
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
												resultMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													resultMap.put(parameterValueStructArr[j].getName(),
																	parameterValueStructArr[j].getValue().para_value);
												}
											}else {
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
												return checker.getReturnXml();
											}
										} else {
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
											return checker.getReturnXml();
										}
									}else {
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							} 
						}
					}else {
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				
				if (null == resultMap ){
					checker.setResult(1014);
					checker.setResultDesc("返回值为空，获取加密方式失败");
					logger.warn("MoSSIDPW==>ReturnXml:"+checker.getReturnXml());
					return checker.getReturnXml();
				}else {
					//加密方式
					return  resultMap.get(nodePath);
				}
			}
		}
	}
}
