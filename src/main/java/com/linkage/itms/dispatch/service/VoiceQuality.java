
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
import com.linkage.itms.Global;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.VoiceQualityChecker;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;

public class VoiceQuality implements IService
{

	private static Logger logger = LoggerFactory.getLogger(VoiceQuality.class);
	
	private static String TestNode = "InternetGatewayDevice.Services.VoiceService.1.PhyInterface.1.X_CT-COM_Stats.PoorQualityList.1.";
	private static String StatTime = TestNode + "StatTime";   //生成记录的时间，UTC时间
	private static String TxPackets = TestNode + "TxPackets";//发送包数
	private static String RxPackets = TestNode + "RxPackets";//接收包数
	private static String MeanDelay = TestNode + "MeanDelay";//平均时延
	private static String MeanJitter = TestNode + "MeanJitter";//平均抖动
	private static String FractionLoss = TestNode + "FractionLoss";//丢包率，单位：%
	private static String LocalIPAddress = TestNode + "LocalIPAddress";//本端IP地址
	private static String LocalUDPPort = TestNode + "LocalUDPPort";//本端端口
	private static String FarEndIPAddress = TestNode + "FarEndIPAddress";//远端IP地址
	private static String FarEndUDPPort = TestNode + "FarEndUDPPort";//远端端口
	private static String MosLq = TestNode + "MosLq";//Mos值,单位0.1，可选
	private static String Codec = TestNode + "Codec";//编解码

	public String work(String inXml)
	{
				
		logger.warn("VoiceQuality==>inXml({})", inXml);
		VoiceQualityChecker checker = new VoiceQualityChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		// 根据设备序列号，厂商OUI检索设备
		Map<String, String> deviceInfoMap = deviceInfoDAO.queryDevInfo(
				checker.getDevSn(), checker.getOui());
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty())
		{
			logger.warn("查无此设备：" + checker.getOui() + "-" + checker.getDevSn());
			checker.setResult(1006);
			checker.setResultDesc("查无此设备：" + checker.getOui() + "-" + checker.getDevSn());
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 设备存在
		else
		{
			String deviceId = deviceInfoMap.get("device_id");
			/**
			 * z语音质量监控
			 */
			String returnXml = voiceDial("1", deviceId, checker);
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
	public String voiceDial(String gw_type, String deviceId, VoiceQualityChecker checker)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
//		AnyObject anyObject = new AnyObject();
		
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[12];
		parameterNamesArr[0] = StatTime;
		parameterNamesArr[1] = TxPackets;
		parameterNamesArr[2] = RxPackets;
		parameterNamesArr[3] = MeanDelay;
		parameterNamesArr[4] = MeanJitter;
		parameterNamesArr[5] = FractionLoss;
		parameterNamesArr[6] = LocalIPAddress;
		parameterNamesArr[7] = LocalUDPPort;
		parameterNamesArr[8] = FarEndIPAddress;
		parameterNamesArr[9] = FarEndUDPPort;
		parameterNamesArr[10] = MosLq;
		parameterNamesArr[11] = Codec;
		getParameterValues.setParameterNames(parameterNamesArr);
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "2";
		rpcArr[0].rpcName = "GetParameterValues";
		rpcArr[0].rpcValue = getParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager(gw_type);
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.RpcCmd_Type);
		String errMessage = "";
		Map VoiceMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10071);
			checker.setResultDesc(errMessage);
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			errMessage = "设备未知错误";
			checker.setResult(10072);
			checker.setResultDesc(errMessage);
			logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
				logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
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
									logger.debug("[{}]DevRpcCmdOBJ.value == null",
											deviceId);
									checker.setResult(1011);
									checker.setResultDesc("系统内部错误，无返回值");
									logger.warn("VoiceDial==>ReturnXml:"
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
												VoiceMap = new HashMap<String, String>();
												for (int j = 0; j < parameterValueStructArr.length; j++)
												{
													VoiceMap.put(
															parameterValueStructArr[j]
																	.getName(),
															parameterValueStructArr[j]
																	.getValue().para_value);
												}
											}
											else
											{
												checker.setResult(1008);
												checker.setResultDesc("系统内部错误，无返回值");
												logger.warn("VoiceDial==>ReturnXml:"
														+ checker.getReturnXml());
												return checker.getReturnXml();
											}
										}
										else
										{
											checker.setResult(1009);
											checker.setResultDesc("系统内部错误，无返回值");
											logger.warn("VoiceDial==>ReturnXml:"
													+ checker.getReturnXml());
											return checker.getReturnXml();
										}
									}
									else
									{
										checker.setResult(1010);
										checker.setResultDesc("系统内部错误，无返回值");
										logger.warn("VoiceDial==>ReturnXml:"
												+ checker.getReturnXml());
										return checker.getReturnXml();
									}
								}
							}
							// else {
							// checker.setResult(1012);
							// checker.setResultDesc("系统内部错误，无返回值");
							// logger.warn("DownLoadByHTTP==>ReturnXml:"+checker.getReturnXml());
							// return checker.getReturnXml();
							// }
						}
					}
					else
					{
						checker.setResult(1013);
						checker.setResultDesc("系统内部错误，无返回值");
						logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
						return checker.getReturnXml();
					}
				}
				if (VoiceMap == null)
				{
					checker.setResult(1014);
					checker.setResultDesc("语音质量监控失败：终端返回值为空。");
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					String statTime = "" + VoiceMap.get(StatTime);
					String txPackets = "" + VoiceMap.get(TxPackets);
					String rxPackets = "" + VoiceMap.get(RxPackets);
					String meanDelay = "" + VoiceMap.get(MeanDelay);
					String meanJitter = "" + VoiceMap.get(MeanJitter);
					String fractionLoss = "" + VoiceMap.get(FractionLoss);
					String localIPAddress = "" + VoiceMap.get(LocalIPAddress);
					String localUDPPort = "" + VoiceMap.get(LocalUDPPort);
					String farEndIPAddress = "" + VoiceMap.get(FarEndIPAddress);
					String farEndUDPPort = "" + VoiceMap.get(FarEndUDPPort);
					String mosLq = "" + VoiceMap.get(MosLq);
					String codec = "" + VoiceMap.get(Codec);
					
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(checker.getDevSn());
					checker.setStatTime(statTime);
					checker.setTxPackets(txPackets);
					checker.setRxPackets(rxPackets);
					checker.setMeanDelay(meanDelay);
					checker.setMeanJitter(meanJitter);
					checker.setFractionLoss(fractionLoss);
					checker.setLocalIPAddress(localIPAddress);
					checker.setLocalUDPPort(localUDPPort);
					checker.setFarEndIPAddress(farEndIPAddress);
					checker.setFarEndUDPPort(farEndUDPPort);
					checker.setMosLq(mosLq);
					checker.setCodec(codec);
					logger.warn("VoiceDial==>ReturnXml:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
	}
}
