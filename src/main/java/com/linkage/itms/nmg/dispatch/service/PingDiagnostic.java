
package com.linkage.itms.nmg.dispatch.service;

import java.text.NumberFormat;
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
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.PingDiagnosticChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

public class PingDiagnostic implements IService
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(PingDiagnostic.class);

	public String work(String inXml)
	{
		logger.warn("PingDiagnostic==>inXml({})", inXml);
		PingDiagnosticChecker checker = new PingDiagnosticChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1009);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		else
		{
			// 用户存在
			String userDevId = userInfoMap.get("device_id");
			// 设备序列号
			String devSn = userInfoMap.get("device_serialnumber");
			checker.setDevSn(devSn);
			if (StringUtil.IsEmpty(userDevId))
			{
				// 用户未绑定终端
				logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1010);
				checker.setResultDesc("此客户未绑定");
				return checker.getReturnXml();
			}
			else
			{
				// ping诊断
				if ("BBMS".equals(Global.SYSTEM_NAME)) {
					return PingList("2", userDevId, checker);
				} else {
					return PingList("1", userDevId, checker);
				}
			}
		}
	}

	/**
	 * 返回Ping测试诊断结果
	 * 
	 * @param gw_type
	 *            1：家庭网关 2：政企网关
	 * @param device_id
	 *            设备ID
	 * @param checker
	 *            接口回参
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String PingList(String gw_type, String device_id, PingDiagnosticChecker checker)
	{
		// 获取Wan
		// 1、调用采集,采集InternetGatewayDevice.WANDevice下节点
		int irt = new SuperGatherCorba().getCpeParams(device_id, 2, 1);
		String waninterface = "";
		if (irt != 1)
		{
			logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]获取wan口失败",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1014);
			checker.setResultDesc("采集设备信息失败");
			logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			// 2、从数据库获取wan_conn_id/wan_conn_sess_id
			String vlanId = "41";
			if ("1".equals(checker.getWanPassageWay()))
			{
				// tr069 通道
				vlanId = "46";
			}
			DeviceConfigDAO dao = new DeviceConfigDAO();
			Map<String, String> wanConnIds = dao.getWanInterface(device_id, vlanId);
			if (wanConnIds == null || wanConnIds.isEmpty())
			{
				logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]设备未获取到wan口",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1012);
				checker.setResultDesc("未获取到设备wan口");
				logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				String wanConnDevice = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
				String wan_conn_id = StringUtil.getStringValue(wanConnIds
						.get("wan_conn_id"));
				String wan_conn_sess_id = StringUtil.getStringValue(wanConnIds
						.get("wan_conn_sess_id"));
				String sessType = StringUtil.getStringValue(wanConnIds.get("sess_type"));
				if (sessType.equals("1"))
				{
					waninterface = wanConnDevice + wan_conn_id + ".WANPPPConnection."
							+ wan_conn_sess_id + ".";
				}
				else if (sessType.equals("2"))
				{
					waninterface = wanConnDevice + wan_conn_id + ".WANIPConnection."
							+ wan_conn_sess_id + ".";
				}
				else
				{
					logger.warn(
							"servicename[PingDiagnostic]cmdId[{}]userinfo[{}]设备获取到wan口sessType值不对",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1013);
					checker.setResultDesc("设备wan口参数不对");
					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
		logger.warn("servicename[PingDiagnostic]cmdId[{}]userinfo[{}]设备获取到wan口[{}]",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), waninterface });
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1]
				.setName("InternetGatewayDevice.IPPingDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = waninterface;
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getiPOrDomainName();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3]
				.setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageNum();
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4]
				.setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getTimeOut();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5]
				.setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getPackageByte();
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6].setName("InternetGatewayDevice.IPPingDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "0";
		anyObject.para_type_id = "3";
		ParameterValueStruct[6].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("Ping");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[5];
		parameterNamesArr[0] = "InternetGatewayDevice.IPPingDiagnostics.SuccessCount";
		parameterNamesArr[1] = "InternetGatewayDevice.IPPingDiagnostics.FailureCount";
		parameterNamesArr[2] = "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime";
		parameterNamesArr[3] = "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime";
		parameterNamesArr[4] = "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime";
		getParameterValues.setParameterNames(parameterNamesArr);
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = device_id;
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
		Map PingMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0 || devRPCRep.get(0) == null)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			errMessage = "系统内部错误，无返回值";
			checker.setResult(1007);
			checker.setResultDesc(errMessage);
			logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
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
				logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "系统内部错误，无返回值";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", device_id);
					checker.setResult(1007);
					checker.setResultDesc(errMessage);
					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
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
								logger.warn("[{}]设备返回：{}", device_id, resp);
								// Fault fault = null;
								if (resp == null || "".equals(resp))
								{
									logger.debug("[{}]DevRpcCmdOBJ.value == null",
											device_id);
									checker.setResult(1007);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("DownLoadByHTTP==>ReturnXml:"
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
												PingMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													PingMap.put(
															parameterValueStructArr[j]
																	.getName(),
															parameterValueStructArr[j]
																	.getValue().para_value);
												}
											}
											else
											{
												checker.setResult(1007);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("PingDiagnostic==>ReturnXml:"
														+ checker.getReturnXml());
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(1007);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("PingDiagnostic==>ReturnXml:"
													+ checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(1007);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("PingDiagnostic==>ReturnXml:"
												+ checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
						}
					}
					else
					{
						checker.setResult(1007);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("PingDiagnostic==>ReturnXml:"
								+ checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				if (PingMap == null)
				{
					checker.setResult(1007);
					checker.setResultDesc("返回值为空，Ping仿真失败");
					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					// 成功数
					String succesNum = ""
							+ PingMap
									.get("InternetGatewayDevice.IPPingDiagnostics.SuccessCount");
//					String failNum = ""
//							+ PingMap
//									.get("InternetGatewayDevice.IPPingDiagnostics.FailureCount");
					
					String failNum =  StringUtil.getStringValue(StringUtil.getIntegerValue(checker.getPackageNum())
							- StringUtil.getIntegerValue(succesNum));
					
					String avgResponseTime = ""
							+ PingMap
									.get("InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime");
					String minResponseTime = ""
							+ PingMap
									.get("InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime");
					String maxResponseTime = ""
							+ PingMap
									.get("InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime");
//					long FailureCount = StringUtil.getLongValue(PingMap
//							.get("InternetGatewayDevice.IPPingDiagnostics.FailureCount"));
					long FailureCount = StringUtil.getLongValue(failNum);
					long PackageCount = StringUtil.getIntegerValue(checker
							.getPackageNum());
					String packetLossRate = percent(FailureCount, PackageCount);
					String iPOrDomainName = checker.getiPOrDomainName();
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setSuccesNum(succesNum);
					checker.setFailNum(failNum);
					checker.setAvgResponseTime(avgResponseTime);
					checker.setMinResponseTime(minResponseTime);
					checker.setMaxResponseTime(maxResponseTime);
					checker.setPacketLossRate(packetLossRate);
					checker.setiPOrDomainName(iPOrDomainName);
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"pingDiagnostic");
					logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
	}

	public String percent(long p1, long p2)
	{
		double p3;
		if (p2 == 0)
		{
			return "N/A";
		}
		else
		{
			p3 = (double) p1 / p2;
		}
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		String str = nf.format(p3);
		return str;
	}
}
