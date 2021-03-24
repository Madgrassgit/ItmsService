package com.linkage.itms.dispatch.sxdx.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.oResult;
import com.linkage.itms.dispatch.sxdx.beanObj.pingResult;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.GetPingResultXML;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetPingResultService  extends ServiceFather {
	public GetPingResultService(String methodName)
	{
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(GetPingResultService.class);
	private pingResult result = new pingResult();
	private ACSCorba corba = new ACSCorba();
	private GetPingResultXML dealXML =  new GetPingResultXML(methodName);
	public pingResult work(String inXml) {
		// 检验入参
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		if(null == dealXML.getXML(inXml)){
			result.setIOpRst(-3);
			return result;
		}
		CpeInfoDao dao = new CpeInfoDao();
		 Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		
		if(null == queryUserInfo || queryUserInfo.size()==0){
			result.setIOpRst(0);
			return result;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		if(StringUtil.isEmpty(deviceId)){
			result.setIOpRst(0);
			return result;
		}
		//判断终端是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setIOpRst(0);
			return result;
		}
		// 设备存在
		else{
			// ping诊断结果
			PingList("1", deviceId);
			return result;
		}
		
	}
	
	/**
	 * 返回Ping测试诊断结果
	 * 
	 * @param gw_type 1：家庭网关 2：政企网关
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void PingList(String gw_type, String deviceId) {
		
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = null;
		parameterNamesArr = new String[5];
		parameterNamesArr[0] = "InternetGatewayDevice.IPPingDiagnostics.FailureCount";
		parameterNamesArr[1] = "InternetGatewayDevice.IPPingDiagnostics.SuccessCount";
		parameterNamesArr[2] = "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime";
		parameterNamesArr[3] = "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime";
		parameterNamesArr[4] = "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime";
		getParameterValues.setParameterNames(parameterNamesArr);
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "2";
		rpcArr[0].rpcName = "GetParameterValues";
		rpcArr[0].rpcValue = getParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager("1");
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持，无法进行ping测速！",deviceId);
			result.setIOpRst(-4);
		}
		Map downByHTTPMap = new HashMap<String, String>();;
		getRpcValue(devRPCRep,downByHTTPMap,deviceId);
		String FailureCount = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.FailureCount");
		String SuccessCount = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.SuccessCount");
		String MaximumResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime");
		String MinimumResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime");
		String AverageResponseTime =StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime");
		
		oResult oresult = new oResult();
		oresult.setAverageResponseTime(AverageResponseTime);
		oresult.setFailureCount(FailureCount);
		oresult.setMaxResponseTime(MaximumResponseTime);
		oresult.setMinResponseTime(MinimumResponseTime);
		oresult.setSuccessCount(SuccessCount);
	    logger.warn(methodName+"["+dealXML.getOpId()+"],采集结果{}",oresult.toString());
		result.setIOpRst(1);
		result.setResult(oresult);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getRpcValue(List<DevRpcCmdOBJ> devRPCRep,Map downByHTTPMap,String deviceId){
		List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
		if (rpcList != null && !rpcList.isEmpty())
		{
			for (int k = 0; k < rpcList.size(); k++)
			{
				if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName()))
				{
					String resp = rpcList.get(k).getValue();
					logger.warn("[{}]设备返回：{}", deviceId, resp);
					if (resp == null || "".equals(resp))
					{
						logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！",deviceId);
						logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
						result.setIOpRst(-4);
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
									ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse.getParameterList();

									for (int j = 0; j < parameterValueStructArr.length; j++)
									{
										downByHTTPMap.put(parameterValueStructArr[j].getName(),	
												parameterValueStructArr[j].getValue().para_value);
									}
								}
								else
								{
									logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型-！",deviceId);
									logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
									result.setIOpRst(-4);
								}
							}
							else
							{
								logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！--",deviceId);
								logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
								result.setIOpRst(-4);
							}
						}
						else
						{
							logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！---",deviceId);
							logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
							result.setIOpRst(-4);
						}
					}
				}else
				{
					logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！----",deviceId);
					logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
					result.setIOpRst(-4);
				}
			}
			if (downByHTTPMap == null)
			{
				logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！-----",deviceId);
				logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
				result.setIOpRst(-4);
			}
		}
	}
}
