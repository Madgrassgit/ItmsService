package com.linkage.itms.dispatch.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.UpLoadByHTTPSpeadChecker;
import com.linkage.itms.obj.ParameValueOBJ;
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
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2019-2-27
 * @category com.linkage.itms.dispatch.service
 * @宁夏设备上传业务质量测试接口
 *
 */
public class UpLoadByHTTPSpeadService implements IService
{
	private static Logger logger = LoggerFactory
			.getLogger(UpLoadByHTTPSpeadService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("UpLoadByHTTPSpeadService==>inXml({})", inXml);
		UpLoadByHTTPSpeadChecker checker = new UpLoadByHTTPSpeadChecker(inXml);
		if (false == checker.check())
		{
			logger.warn(
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		List<HashMap<String, String>> deviceInfoList = null;
		if (1 == checker.getUserInfoType())
		{
			// 1：根据用户宽带帐号
			checker.setUsername(checker.getUserInfo());
			deviceInfoList = deviceInfoDAO.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (2 == checker.getUserInfoType())
		{
			// 2：根据逻辑SN号
			deviceInfoList = deviceInfoDAO.queryUserByLoidNx(checker.getUserInfo());
		}
		else if (3 == checker.getUserInfoType())
		{
			// 3：根据设备序列号后6位
			deviceInfoList = deviceInfoDAO.queryDeviceByDevSN(checker.getUserInfo());
		}
		if (null == deviceInfoList || deviceInfoList.size() == 0)
		{
			checker.setResult(1000);
			checker.setResultDesc("没有查到设备");
			logger.warn(
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]没有查到设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		if (deviceInfoList.size() > 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("查到多组设备，请输入更多位设备序列号进行查询");
			logger.warn(
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]查到多组设备，请输入更多位设备序列号进行查询",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		HashMap<String, String> deviceInfoMap = deviceInfoList.get(0);
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			logger.warn(
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
		checker.setDeviceId(deviceId);
		// 获取设备序列号
		Map<String, String> devSnMap = deviceInfoDAO.queryDevSn(deviceId);
		checker.setDevSn(StringUtil.getStringValue(devSnMap, "device_serialnumber", ""));
		// 获取宽带账号和Loid
		List<HashMap<String, String>> netAccounts = deviceInfoDAO
				.getNetAccountByDevSn(checker.getDevSn());
		if (null == netAccounts || netAccounts.size() == 0)
		{
			checker.setResult(1000);
			checker.setResultDesc("宽带账号为空");
			logger.warn(
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]宽带账号为空",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		HashMap<String, String> netAccount = netAccounts.get(0);
		checker.setUsername(StringUtil.getStringValue(netAccount, "netaccount", ""));
		// 获取wan通道
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		if (flag == 1)
		{
			String Interface = getPingInterface(deviceId, acsCorba);
			logger.warn("Interface=" + Interface);
			if (StringUtil.IsEmpty(Interface))
			{
				checker.setResult(1005);
				checker.setResultDesc("wan通道获取失败");
				logger.warn(
						"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]wan通道获取失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				return checker.getReturnXml();
			}
			// 开始PPPOE拨测
			return downLoadByHTTPSpead("1", deviceId, checker,Interface);
		}
		else
		{
			logger.warn("{}设备不在线... ...", deviceId);
			checker.setResult(1000);
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
			UpLoadByHTTPSpeadChecker checker, String Interface)
	{
		ACSCorba acsCorba = new ACSCorba();
		ArrayList<ParameValueOBJ> pvObjList= new ArrayList<ParameValueOBJ>();
		ParameValueOBJ pvObj = new ParameValueOBJ();
		pvObj.setName("InternetGatewayDevice.UploadDiagnostics.UploadURL");
		pvObj.setType("1");
		pvObj.setValue(checker.getUpLoadURL());
		pvObjList.add(pvObj);
		
		pvObj = new ParameValueOBJ();
		pvObj.setName("InternetGatewayDevice.UploadDiagnostics.Interface");
		pvObj.setType("1");
		pvObj.setValue(Interface);
		pvObjList.add(pvObj);
		
		pvObj = new ParameValueOBJ();
		pvObj.setName("InternetGatewayDevice.UploadDiagnostics.TestFileLength");
		pvObj.setType("3");
		pvObj.setValue(checker.getTestFileLength());
		pvObjList.add(pvObj);
		int result = acsCorba.setValue(deviceId, pvObjList);
		if(result != 1 && result != 0){
			logger.warn("[{}]下发参数失败[{}]", deviceId, result);
			checker.setResult(1000);
			checker.setResultDesc("终端设备不支持，无法测速！");
			return  checker.getReturnXml();
		}
		
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[1];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("InternetGatewayDevice.UploadDiagnostics.DiagnosticsState");
		anyObject = new AnyObject();
		anyObject.para_value = checker.getdState();
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("GWMS");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = null;
		parameterNamesArr = new String[5];
		parameterNamesArr[0] = "InternetGatewayDevice.UploadDiagnostics.BOMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.UploadDiagnostics.EOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.UploadDiagnostics.TotalBytesSent";
		parameterNamesArr[3] = "InternetGatewayDevice.UploadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[4] = "InternetGatewayDevice.UploadDiagnostics.TCPOpenResponseTime";
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
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]设备未知错误",
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
					"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]设备未知错误",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			if (stat != 1)
			{
				errMessage = "终端设备不支持，无法测速！";
				checker.setResult(1007);
				checker.setResultDesc(errMessage);
				logger.error(
						"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								errMessage });
				return checker.getReturnXml();
			}
			else
			{
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					errMessage = "终端设备不支持，无法测速！";
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					checker.setResult(1014);
					checker.setResultDesc(errMessage);
					logger.error(
							"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]终端不支持",
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
											"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
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
														"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
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
													"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
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
												"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
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
								"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]系统内部错误，无返回值",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						return checker.getReturnXml();
					}
				}
				if (downByHTTPMap == null)
				{
					checker.setResult(1015);
					checker.setResultDesc("终端设备不支持，无法测速！");
					logger.error(
							"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]返回值为空，HTTP下载仿真失败",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					return checker.getReturnXml();
				}
				else
				{
					String bOMTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.UploadDiagnostics.BOMTime");
					String eOMTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.UploadDiagnostics.EOMTime");
					String totalBytesSent = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.UploadDiagnostics.TotalBytesSent");
					String tCPOpenRequestTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.UploadDiagnostics.TCPOpenRequestTime");
					String tCPOpenResponseTime = ""
							+ downByHTTPMap
									.get("InternetGatewayDevice.UploadDiagnostics.TCPOpenResponseTime");
					if(null == totalBytesSent || bOMTime == null || eOMTime == null 
							|| "null".equals(totalBytesSent) || "null".equals(bOMTime) || "null".equals(eOMTime)
							|| totalBytesSent.isEmpty() || bOMTime.isEmpty() || eOMTime.isEmpty())
					{
						checker.setResult(1013);
						checker.setResultDesc("终端设备不支持，无法测速！");
						logger.error(
								"servicename[UpLoadByHTTPSpeadService]cmdId[{}]userInfo[{}]设备接受字节数异常",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						return checker.getReturnXml();
					}
					logger.warn("设备[{}]测速接受字节数为 [{}], 开始传输时间为[{}], 结束传输时间为[{}]",deviceId,totalBytesSent,bOMTime,eOMTime);
					Long timeInterval = time(bOMTime, eOMTime);
					double ff = 0.0000;
					if(timeInterval <= 0)
					{
						checker.setResult(1013);
						checker.setResultDesc("终端设备不支持，无法测速！");
						logger.error(
								"servicename[DownLoadByHTTPSpeadService4JX]cmdId[{}]userInfo[{}]设备日期节点值异常",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						return checker.getReturnXml();
					}
					ff = (Double.parseDouble(totalBytesSent) / timeInterval) * 1000 * 8 / 1024 / 1024;
					DecimalFormat df = new DecimalFormat("#0.00");
					String speed = StringUtil.getStringValue(df.format(ff));
					checker.setuSpeed(speed);
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setbOMTime(bOMTime);
					checker.seteOMTime(eOMTime);
					checker.settCPOpenRequestTime(tCPOpenRequestTime);
					checker.settCPOpenResponseTime(tCPOpenResponseTime);
					checker.setTotalBytesSent(totalBytesSent);
					String returnMessage = checker.getReturnXml();
					logger.warn("return message : " + returnMessage);
					return returnMessage;
				}
			}
		}
	}
	
	/**
	 * nx_dx根据设备开始下载时间、结束下载时间、下载字节数测出速率，时间格式类似2018-07-19T23:19:57.514956
	 * @param transportStartTime
	 * @param transportEndTime
	 * @return
	 */
	public Long time(String transportStartTime, String transportEndTime)
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
			return time;
		}
		return time;
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
				logger.warn("[getPingInterface] [{}]获取" + wanConnPath
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

}
