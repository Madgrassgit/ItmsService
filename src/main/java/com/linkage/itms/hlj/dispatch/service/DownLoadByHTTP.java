
package com.linkage.itms.hlj.dispatch.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.DownLoadByHTTPChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

public class DownLoadByHTTP implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTP.class);

	public String work(String inXml, String gw_type)
	{
		logger.warn("DownLoadByHTTP==>inXml({})", inXml);
		DownLoadByHTTPChecker checker = new DownLoadByHTTPChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("验证未通过[{}]，返回：" + checker.getReturnXml(), checker.getResultDesc());
			return checker.getReturnXml();
		}
		
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0)
		{
			if ("2".equals(gw_type)) {
				userMap = qdDao.queryUserByNetAccountQiye(checker.getQueryNum());
			}
			else {
				userMap = qdDao.queryUserByNetAccount(checker.getQueryNum());
			}
		}
		else if (checker.getQueryType() == 1)
		{
			if ("2".equals(gw_type)) {
				userMap = qdDao.queryUserByLoidQiye(checker.getQueryNum());
			}
			else {
				userMap = qdDao.queryUserByLoid(checker.getQueryNum());
			}
		}
		else if (checker.getQueryType() == 2) {
			if ("2".equals(gw_type)) {
				userMap = qdDao.queryUserByDevSNQiye(checker.getQueryNum());
			}
		}
		 
		// 设备不存在
		if (null == userMap || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(3);
			checker.setResultDesc("无设备信息");
			logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 设备存在
		else
		{
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			int flag = getStatus.testDeviceOnLineStatus(deviceId,
					corba);
			// 设备正在被操作，不能获取节点值
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}",
						deviceId);
				checker.setResult(10);
				checker.setResultDesc("设备忙，采集失败");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			} else if (1 == flag) {
				ArrayList<String> downloadHttpPathsList = null;
				downloadHttpPathsList = corba.getParamNamesPath(deviceId, "InternetGatewayDevice.DownloadDiagnostics.", 0);
				if (downloadHttpPathsList == null || downloadHttpPathsList.size() == 0
						|| downloadHttpPathsList.isEmpty())
				{
					logger.warn("[{}] [{}]获取InternetGatewayDevice.DownloadDiagnostics下所有节点路径失败",deviceId);
					checker.setResult(6);
					checker.setResultDesc("版本不支持");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
			} else {
				logger.warn("设备离线，device_id={}", deviceId);
				checker.setResult(4);
				checker.setResultDesc("设备离线");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();

			}
			/**
			 * PPPoE 拨测
			 */
			String returnXml = downLoadByHTTP(gw_type, deviceId, checker);
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String downLoadByHTTP(String gw_type, String deviceId,
			DownLoadByHTTPChecker checker)
	{
		int irt = new SuperGatherCorba().getCpeParams(deviceId, 2, 1);
		String waninterface = "";
		Map<String, String> wanConnIds = new HashMap<String,String>();
		if (irt != 1)
		{
			logger.warn("servicename[PingDiagnostic]QueryNum[{}]获取wan口失败",
					new Object[] { checker.getQueryNum() });
			checker.setResult(6);
			checker.setResultDesc("设备未知错误");
			logger.warn("PingDiagnostic==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}else{
			String vlanId = "41";
			if ("1".equals(checker.getWanPassageWay()))
			{
				// tr069 通道
				vlanId = "46";
			}
			DeviceConfigDAO dao = new DeviceConfigDAO();
			wanConnIds = dao.getWanInterface(deviceId, vlanId);
			if (wanConnIds == null || wanConnIds.isEmpty())
			{
				logger.warn("servicename[DownLoadByHTTP]QueryNum[{}]设备未获取到wan口",
						new Object[] { checker.getQueryNum() });
				checker.setResult(6);
				checker.setResultDesc("设备未获取到wan口");
				logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
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
							"servicename[DownLoadByHTTP]QueryNum[{}]设备获取到wan口sessType值不对",
							new Object[] { checker.getQueryNum() });
					checker.setResult(6);
					checker.setResultDesc("设备未知错误");
					logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
		// 2、从数据库获取wan_conn_id/wan_conn_sess_id
		
		logger.warn("servicename[DownLoadByHTTP]QueryNum[{}]设备获取到wan口[{}]",
				new Object[] { checker.getQueryNum(), waninterface });
		DevRpc[] devRPCArr = new DevRpc[1];
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[5];
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
		anyObject.para_value = waninterface;
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
		ParameterValueStruct[3].setName("InternetGatewayDevice.DownloadDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "50";
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4]
				.setName("InternetGatewayDevice.DownloadDiagnostics.EthernetPriority");
		anyObject = new AnyObject();
		anyObject.para_value = "1";
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("downLoad");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = null;
		parameterNamesArr = new String[7];
 
		parameterNamesArr[0] = "InternetGatewayDevice.DownloadDiagnostics.ROMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.DownloadDiagnostics.BOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.DownloadDiagnostics.EOMTime";
		parameterNamesArr[3] = "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived";
		parameterNamesArr[4] = "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived";
		parameterNamesArr[5] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[6] = "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime";
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
			errMessage = "设备未知错误";
			checker.setResult(6);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(6);
			checker.setResultDesc(errMessage);
			logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			if (stat != 1)
			{ 
				errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
				checker.setResult(6);
				checker.setResultDesc(errMessage);
				logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
				return checker.getReturnXml();
			}
			else
			{
				errMessage = "系统内部错误";
				if (devRPCRep.get(0).getRpcList() == null
						|| devRPCRep.get(0).getRpcList().size() == 0)
				{
					logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
					checker.setResult(6);
					checker.setResultDesc(errMessage);
					logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
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
									checker.setResult(6);
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
												checker.setResult(6);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("DownLoadByHTTP==>ReturnXml:"
														+ checker.getReturnXml());
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(6);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("DownLoadByHTTP==>ReturnXml:"
													+ checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(6);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("DownLoadByHTTP==>ReturnXml:"
												+ checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
							 
						}
					}
					else
					{
						checker.setResult(6);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("DownLoadByHTTP==>ReturnXml:"
								+ checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				if (downByHTTPMap == null)
				{
					checker.setResult(6);
					checker.setResultDesc("返回值为空，HTTP下载仿真失败");
					logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
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
					String SpeedResult = null;
					if ("0".equals(receiveByte)){
						SpeedResult = "0";
					}else{
						SpeedResult = getDownPert(transportStartTime, transportEndTime, receiveByte);
					}
					  
					JSONObject jo = new JSONObject();
					try
					{
						jo.put("resultCode", 0);
						jo.put("SpeedResult", SpeedResult);
						jo.put("streamingNum", checker.getStreamingNum());
						jo.put("startTime", transportStartTime);
						jo.put("endTime", transportEndTime);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					logger.warn("DownLoadByHTTP==>ReturnXml:" + checker.getReturnXml());
					return jo.toString();
 
				}
			}
		}
	}

	/**
	 * 下载速率
	 * 
	 * @param TransportStartTime
	 *            开始时间
	 * @param TransportEndTime
	 *            结束时间
	 * @param ReceiveByte
	 *            字节数
	 * @return
	 */
	private String getDownPert(String transportStartTime, String transportEndTime,
			String receiveByte)
	{
		float ff = 0;
		String strtime = transportStartTime;
		String endtime = transportEndTime;
		if (!StringUtil.IsEmpty(strtime) && !StringUtil.IsEmpty(endtime))
		{
			BigDecimal strTime = new BigDecimal(strtime.split(":")[2].split("[.]")[0])
					.add(new BigDecimal(strtime.split(":")[2].split("[.]")[1]).divide(
							new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP));
			BigDecimal endTime = new BigDecimal(endtime.split(":")[2].split("[.]")[0])
					.add(new BigDecimal(endtime.split(":")[2].split("[.]")[1]).divide(
							new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP));
			BigDecimal receiveBytes = new BigDecimal(receiveByte).divide(new BigDecimal(
					"1024"), 6, BigDecimal.ROUND_HALF_UP);// k
			BigDecimal mintue = (new BigDecimal(endtime.split(":")[1])
					.subtract(new BigDecimal(strtime.split(":")[1])))
					.multiply(new BigDecimal("60"));
			BigDecimal period = endTime.subtract(strTime).add(mintue);
			// k/s
			ff = receiveBytes.divide(period, 6, BigDecimal.ROUND_HALF_UP).floatValue();
			DecimalFormat df = new DecimalFormat("#0.00");
			return StringUtil.getStringValue(df.format(ff));
		}
		return StringUtil.getStringValue(ff);
	}

	@Override
	public String work(String jsonString) {
		return null;
	}
	
	
}