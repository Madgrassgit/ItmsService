
package com.linkage.itms.dispatch.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.SpecialSpeedChecker4JX;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 江西电信ITMS+家庭网关互联网专线测速接口需求
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2019-2-27
 */
public class SpecialSpeedService4JX implements IService
{

	private static Logger logger = LoggerFactory
			.getLogger(SpecialSpeedService4JX.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("SpecialSpeedService4JX==>inXml({})", inXml);
		SpecialSpeedChecker4JX checker = new SpecialSpeedChecker4JX(inXml);
		checker.setIsInsert("0");
		if (false == checker.check())
		{
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 1：根据用户宽带帐号
		List<HashMap<String, String>> deviceInfoList = null;
		if (1 == checker.getUserInfoType())
		{
			// 1：根据用户宽带帐号
			checker.setUsername(checker.getUserInfo());
			deviceInfoList = deviceInfoDAO.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (2 == checker.getUserInfoType())
		{
			// 1：根据逻辑SN号
			deviceInfoList = deviceInfoDAO.queryUserByLoid(checker.getUserInfo());
		}
		else if (3 == checker.getUserInfoType())
		{
			// 1：根据设备序列号后6位
			deviceInfoList = deviceInfoDAO.queryDeviceByDevSN(checker.getUserInfo());
		}
		if (null == deviceInfoList || deviceInfoList.size() == 0)
		{
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]没有查到设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		if (deviceInfoList.size() > 1)
		{
			checker.setResult(1004);
			checker.setResultDesc("查到多组设备，请输入更多位设备序列号进行查询");
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]查到多组设备，请输入更多位设备序列号进行查询",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		HashMap<String, String> deviceInfoMap = deviceInfoList.get(0);
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			checker.setResult(1003);
			checker.setResultDesc("未绑定设备");
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		
		// 设备不存在
		if ("".equals(StringUtil.getStringValue(deviceInfoMap, "device_id"))
				|| null ==StringUtil.getStringValue(deviceInfoMap, "device_id"))
		{
			checker.setResult(1003);
			checker.setResultDesc("未绑定设备");
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}

		String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
		// 1.查询此用户开通的业务信息
		Map<String, String> userServMap = userDevDao.queryServForNet(deviceInfoMap
				.get("user_id"));
		if (null == userServMap || userServMap.isEmpty())
		{
			checker.setWanType("");
		}
		else
		{
			String wanType = userServMap.get("wan_type");
			if ("1".equals(wanType))
			{
				checker.setWanType("PPPoE_Bridged");
			}
			else if ("2".equals(wanType))
			{
				checker.setWanType("IP_Routed");
			}
			else if ("3".equals(wanType))
			{
				checker.setWanType("STATIC");
			}
			else if ("4".equals(wanType))
			{
				checker.setWanType("DHCP");
			}
			else
			{
				checker.setWanType("");
			}
		}
		checker.setDeviceId(deviceId);
		// 获取设备序列号
		Map<String, String> devSnMap = deviceInfoDAO.queryDevSn(deviceId);
		checker.setDevSn(StringUtil.getStringValue(devSnMap, "device_serialnumber", ""));
		checker.setIp(StringUtil.getStringValue(devSnMap, "loopback_ip", ""));
		// 获取宽带账号和Loid
		List<HashMap<String, String>> netAccounts = deviceInfoDAO
				.getNetAccountByDevSn(checker.getDevSn());
		if (null == netAccounts || netAccounts.size() == 0)
		{
			checker.setResult(1006);
			checker.setResultDesc("宽带账号为空");
			logger.warn(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]宽带账号为空",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		HashMap<String, String> netAccount = netAccounts.get(0);
		checker.setUsername(StringUtil.getStringValue(netAccount, "netaccount", ""));
		checker.setLoid(StringUtil.getStringValue(netAccount, "username", ""));
		// 获取wan通道
		ACSCorba acsCorba = new ACSCorba();
		logger.warn("use acs start ...");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		logger.warn("flag end ..." + flag);
		if (flag == 1)
		{
			String Interface = getPingInterface(deviceId, acsCorba);
			logger.warn("Interface=" + Interface);
			if (StringUtil.IsEmpty(Interface))
			{
				checker.setResult(1005);
				checker.setResultDesc("wan通道获取失败");
				logger.warn(
						"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]wan通道获取失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				return checker.getReturnXml();
			}
			String url = Global.SPECIALDOWNLOADURL;
			checker.setDownURL(url);
			logger.warn("SpecialSpeedService4JX==>downURL is:" + url);
			// 开始PPPOE拨测
			return downLoadByHTTPSpead("1", deviceId, checker, Interface);
		}
		else
		{
			logger.warn("{}设备不在线... ...", deviceId);
			checker.setResult(1007);
			checker.setResultDesc("设备不在线");
			return checker.getReturnXml();
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String downLoadByHTTPSpead(String gw_type, String deviceId,
			SpecialSpeedChecker4JX checker, String Interface)
	{
		// isInsert为1的时候，测试数据入库，同时入开始测试时间
		checker.setIsInsert("1");
		checker.setTestStartTime(System.currentTimeMillis() / 1000);
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1]
				.setName("InternetGatewayDevice.DownloadDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = Interface;
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2]
				.setName("InternetGatewayDevice.DownloadDiagnostics.DownloadURL");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDownURL();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.X_CT-COM_IPoEDiagnostics.ExternalIPAddress");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getIpAddress();
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4]
				.setName("InternetGatewayDevice.X_CT-COM_IPoEDiagnostics.SubnetMask");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getNetMask();
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5]
				.setName("InternetGatewayDevice.X_CT-COM_IPoEDiagnostics.DefaultGateway");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getGateWay();
		anyObject.para_type_id = "1";
		ParameterValueStruct[5].setValue(anyObject);
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6]
				.setName("InternetGatewayDevice.X_CT-COM_IPoEDiagnostics.DNSServers");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getDns();
		anyObject.para_type_id = "1";
		ParameterValueStruct[6].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("downLoad");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = null;
		parameterNamesArr = new String[7];
		parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
		parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
		parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues";
		parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.SampledValues";
		logger.warn("parameterNamesArr end");
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
		Map downByHTTPMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "终端设备不支持，无法测速！";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.error(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]设备未知错误",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "终端设备不支持，无法测速！";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.error(
					"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]设备未知错误",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
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
				logger.error(
						"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								errMessage });
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "终端设备不支持，无法测速！";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					checker.setResult(1014);
					checker.setResultDesc(errMessage);
					logger.error(
							"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]终端不支持",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
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
									logger.warn("[{}]DevRpcCmdOBJ.value == null",
											deviceId);
									checker.setResult(1011);
									checker.setResultDesc("终端设备不支持，无法测速！");
									logger.error(
											"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
											new Object[] { checker.getCmdId(),
													checker.getUserInfo() });
									return checker.getReturnXml();
								}
								else
								{
									SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
									if (soapOBJ != null)
									{
										Element element = soapOBJ.getRpcElement();
										if (element != null)
										{
											GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
													.GetParameterValuesResponse(element);
											if (getParameterValuesResponse != null)
											{
												ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
														.getParameterList();
												downByHTTPMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													downByHTTPMap
															.put(parameterValueStructArr[j]
																	.getName(),
																	parameterValueStructArr[j]
																			.getValue().para_value);
												}
											}
											else
											{
												checker.setResult(1008);
												checker.setResultDesc("终端设备不支持，无法测速！");
												logger.error(
														"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
														new Object[] {
																checker.getCmdId(),
																checker.getUserInfo() });
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(1009);
											checker.setResultDesc("终端设备不支持，无法测速！");
											logger.error(
													"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
													new Object[] { checker.getCmdId(),
															checker.getUserInfo() });
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(1010);
										checker.setResultDesc("终端设备不支持，无法测速！");
										logger.error(
												"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
												new Object[] { checker.getCmdId(),
														checker.getUserInfo() });
										return checker.getReturnXml();
									}
								}
							}
						}
					}
					else
					{
						checker.setResult(1013);
						checker.setResultDesc("终端设备不支持，无法测速！");
						logger.error(
								"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						return checker.getReturnXml();
					}
				}
				if (downByHTTPMap == null)
				{
					checker.setResult(1015);
					checker.setResultDesc("终端设备不支持，无法测速！");
					logger.error(
							"servicename[SpecialSpeedService4JX]cmdId[{}]userInfo[{}]返回值为空，HTTP下载仿真失败",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					return checker.getReturnXml();
				}
				else
				{
					String transportStartTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.BOMTime");
					String transportEndTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.EOMTime");
					String receiveByte = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
					String tcpRequestTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
					String tcpResponseTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
						// 测速结果值
						String SampledTotalValues = ""
								+ downByHTTPMap
										.get("InternetGatewayDevice.DownloadDiagnostics.SampledTotalValues");
						if ("".equals(SampledTotalValues))
						{
							checker.setAvgSampledTotalValues("");
							checker.setMaxSampledTotalValues("");
						}
						else
						{
							String[] sampledTotalValues = SampledTotalValues.split("\\|");
							checker.setAvgSampledTotalValues(getSampledValue(sampledTotalValues));
							checker.setMaxSampledTotalValues(getMaxValue(sampledTotalValues));
						}
						// 江西电信新增返回值字段
						if("jx_dx".equals(Global.G_instArea)){
							String SampledValues = ""
									+ downByHTTPMap
											.get("InternetGatewayDevice.DownloadDiagnostics.SampledValues");
							checker.setSampledValues(SampledValues);
							checker.setSampledTotalValues(SampledTotalValues);
						}           
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setTransportStartTime(transportStartTime);
					checker.setTransportEndTime(transportEndTime);
					checker.setReceiveByte(receiveByte);
					checker.setTcpRequestTime(tcpRequestTime);
					checker.setTcpResponseTime(tcpResponseTime);
					String returnMessage = checker.getReturnXml();
					logger.warn("return message : " + returnMessage);
					return returnMessage;
				}
			}
		}
	}

	/**
	 * 获取平均值(保留两位小数)
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 平均值
	 */
	private String getSampledValue(String[] sampledValues)
	{
		// 保留小数点后两位
		DecimalFormat df = new DecimalFormat("######0.00");
		double sum = 0.0d;
		double result;
		boolean a = false;
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 15)
			{
				a = true;
				if (i == 0 || i == 1 || i == 2 || i == 13 || i == 14)
				{
					continue;
				}
			}
			sum += Double.parseDouble(sampledValues[i]);
		}
		if (a)
		{
			result = sum / 10;
		}
		else
		{
			result = sum / sampledValues.length;
		}
		return StringUtil.getStringValue(df.format(result));
	}

	/**
	 * 获取最大值
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 最大值
	 */
	private String getMaxValue(String[] sampledValues)
	{
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < sampledValues.length; i++)
		{
			if (sampledValues.length == 15)
			{
				if (i == 0 || i == 1 || i == 2 || i == 13 || i == 14)
				{
					continue;
				}
			}
			list.add(Double.parseDouble(sampledValues[i]));
		}
		Collections.sort(list);
		return StringUtil.getStringValue(list.get(list.size() - 1));
	}

	/**
	 * 获取wan
	 * 
	 * @param device_id
	 * @param gw_type
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getPingInterface(String device_id, String gw_type)
	{
		logger.debug("getPingInterface({},{})", new Object[] { device_id, gw_type });
		String value = "";
		String wanConnDevice = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		SuperGatherCorba sgCorba = new SuperGatherCorba();
		// 获取Wan
		// 1、调用采集,采集InternetGatewayDevice.WANDevice下节点
		int irt = sgCorba.getCpeParams(device_id, 2, 1);
		logger.warn("[{}]调用采集获取Wan的结果：" + irt, device_id);
		String errorMsg = "";
		if (irt != 1)
		{
			errorMsg = "调用采集失败";
			logger.warn("[{}]" + errorMsg, device_id);
		}
		else
		{
			// 2、从数据库获取wan_conn_id/wan_conn_sess_id
			DeviceInfoDAO dao = new DeviceInfoDAO();
			List<Map> wanConnIds = dao.getWanConnIds(device_id);
			if (wanConnIds == null || wanConnIds.isEmpty())
			{
				errorMsg = "没有获取到Wan接口";
				logger.warn("[{}]" + errorMsg, device_id);
			}
			else
			{
				for (Map map : wanConnIds)
				{
					String wan_conn_id = StringUtil
							.getStringValue(map.get("wan_conn_id"));
					String wan_conn_sess_id = StringUtil.getStringValue(map
							.get("wan_conn_sess_id"));
					String serv_list = StringUtil.getStringValue(map.get("serv_list"));
					// if ("INTERNET".equals(serv_list) && "41".equals(vlanid)) // 原AH
					if ("INTERNET".equals(serv_list)) // JX 存在一个用户多个宽带业务的，随机取通道。
					{
						logger.warn("设备[{}]采集的vlanId为[{}]", new Object[] { device_id,
								StringUtil.getStringValue(map.get("vlan_id")) });
						value = wanConnDevice + wan_conn_id + ".WANPPPConnection."
								+ wan_conn_sess_id + ".";
						break;
					}
				}
			}
		}
		return value;
	}

	/**
	 * 获取InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice. {i}.WANIPConnection.
	 * 
	 * @param deviceId
	 * @param corba
	 * @return
	 */
	private String getPingInterface(String deviceId, ACSCorba corba)
	{
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String SERV_LIST_VOIP = "INTERNET";
		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}", wanConnPathsList.size());
		// 直接采集路径名
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("[QueryVOIPWanInfoService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
				return null;
			}
			for (String j : jList)
			{
				// 获取wanPPPConnection下的k
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
						+ wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
				{
					wanConnPathsList.add(wanConnPath + j + wanIPConnection + "1"
							+ wanServiceList);
				}
				else
				{
					for (String kppp : kPPPList)
					{
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanServiceList);
					}
				}
			}
		}
		else
		{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(wanServiceList) >= 0)
				{
					paramNameList.add(namepath);
				}
			}
			wanConnPathsList = new ArrayList<String>();
			wanConnPathsList.addAll(paramNameList);
		}
		if (wanConnPathsList.size() == 0)
		{
			logger.warn("[QueryVOIPWanInfoService] [{}]无节点：" + wanConnPath
					+ ".j.wanPPPConnection/wanIPConnection." + wanServiceList
					+ "下实例号失败，返回", deviceId);
			return null;
		}
		String[] paramNametemp = new String[wanConnPathsList.size()];
		for (int i = 0; i < wanConnPathsList.size(); i++)
		{
			paramNametemp[i] = wanConnPathsList.get(i);
		}
		Map<String, String> paramValueMap = corba
				.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap.isEmpty())
		{
			logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败", deviceId);
		}
		for (Map.Entry<String, String> entry : paramValueMap.entrySet())
		{
			logger.debug("[{}]{}={} ",
					new Object[] { deviceId, entry.getKey(), entry.getValue() });
			// 语音节点
			if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0)
			{
				return entry.getKey()
						.substring(0, entry.getKey().indexOf(wanServiceList));
			}
		}
		return null;
	}

	/**
	 * nx_dx根据设备开始下载时间、结束下载时间、下载字节数测出速率，时间格式类似2018-07-19T23:19:57.514956
	 * @param transportStartTime
	 * @param transportEndTime
	 * @param receiveByte
	 * @return
	 */
	public Long time(String transportStartTime, String transportEndTime,
			String receiveByte)
	{
		String strTime = "";
		String endTime = "";
		long time = 0;
		if (!StringUtil.IsEmpty(transportStartTime))
		{
			strTime = (transportStartTime.replace("T", " ")).substring(0, 23);
			endTime = (transportEndTime.replace("T", " ")).substring(0, 23);
			if (strTime == null || "".equals(strTime))
			{
				strTime = null;
			}
			else
			{
				strTime = String.valueOf(datetimeToMillis(strTime));
			}
			if (endTime == null || "".equals(endTime))
			{
				endTime = null;
			}
			else
			{
				endTime = String.valueOf(datetimeToMillis(endTime));
			}
			time = Long.parseLong(endTime) - Long.parseLong(strTime);
			logger.warn("download time is :" + time);
			return time;
		}
		return time;
	}
	public static void main(String[] args)
	{
		String starttime = "2018-07-19T23:19:57.514956";
		String endtime = "2018-07-19T23:20:04.037384";
		String strTime = "";
		String endTime = "";
		strTime = (starttime.replace("T", " ")).substring(0, 23);
		endTime = (endtime.replace("T", " ")).substring(0, 23);
		if (strTime == null || "".equals(strTime))
		{
			strTime = null;
		}
		else
		{
			strTime = String.valueOf(datetimeToMillis(strTime));
		}
		if (endTime == null || "".equals(endTime))
		{
			endTime = null;
		}
		else
		{
			endTime = String.valueOf(datetimeToMillis(endTime));
		}
		Long time = Long.parseLong(endTime) - Long.parseLong(strTime);
		double ff = 0.0000;
		ff = (Double.parseDouble("237063420") / time) * 1000 * 8 / 1024 / 1024;
		DecimalFormat df = new DecimalFormat("#0.00");
		System.out.println(strTime);
		System.out.println(endTime);
		System.out.println(StringUtil.getStringValue(df.format(ff)));
	}
	
	/**
	 * 返回毫秒值
	 * @param DateTime
	 * @return
	 */
	private static long datetimeToMillis(String DateTime)
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		SimpleDateFormat s = null;
		Date date = null;
		try
		{
			s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			date = new Date();
			try
			{
				date = s.parse(DateTime);
			}
			catch (ParseException e)
			{
				logger.error("date message is error!");
			}
		}
		catch (Exception ee)
		{
			logger.error("date message is error!");
		}
		calendar.setTime(date);
		return calendar.getTimeInMillis();
	}
}
