package com.linkage.itms.dispatch.sxdx.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.DiagnoseResult;
import com.linkage.itms.dispatch.sxdx.beanObj.Para;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.GetDiagnoseResultXML;
import com.linkage.itms.obj.ParameValueOBJ;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDiagnoseResultService  extends ServiceFather {

	public GetDiagnoseResultService(String methodName) {
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(GetDiagnoseResultService.class);
	private ACSCorba corba = new ACSCorba();
	private DiagnoseResult result = new DiagnoseResult();
	private GetDiagnoseResultXML dealXML;
	public DiagnoseResult work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		dealXML = new GetDiagnoseResultXML(methodName);
		if(null == dealXML.getXML(inXml)){
			result.setiOpRst(-3);
			return result;
		}
		
		CpeInfoDao dao = new CpeInfoDao();

		 Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		
		if(null == queryUserInfo || queryUserInfo.size()==0){
			result.setiOpRst(0);
			return result;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		if(StringUtil.isEmpty(deviceId)){
			result.setiOpRst(0);
			return result;
		}
		//判断终端是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setiOpRst(0);
			return result;
		}
		//procName =DOWNLOAD_SPEED_TEST
		logger.warn(methodName+"["+dealXML.getOpId()+"],开始查询{}",dealXML.getProcName());
		if("DOWNLOAD_SPEED_TEST".equals(dealXML.getProcName())){
			downloadSpeedTest(deviceId);
		}
		//procName =TRACEROUTETEST
		if("TRACEROUTETEST".equals(dealXML.getProcName())){
			traceroutetest(deviceId);
		}
		//procName =PINGTEST
		if("PINGTEST".equals(dealXML.getProcName())){
			pingTest(deviceId);
		}
		//procName =UPLOAD_SPEED_TEST
		if("UPLOAD_SPEED_TEST".equals(dealXML.getProcName())){
			uploadSpeedTest(deviceId);
		}
		return result;
	}
	
	private void traceroutetest(String deviceId) {
		String lanPath = "InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.";
	    List<String> iList = corba.getIList(deviceId, lanPath);
		if (null == iList || iList.isEmpty())
		{
			logger.warn("[{}]获取iList失败，返回", deviceId);
			logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备返回错误！",deviceId);
			result.setiOpRst(-4);
		}else{
			logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
		}
		ArrayList<Para> paralist = new ArrayList<Para>();
		for(String i : iList){
			String[] gatherPath = new String[]{
					"InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+i+".HopHost",
					"InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+i+".HopHostAddress",
					"InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+i+".HopErrorCode",
					"InternetGatewayDevice.TraceRouteDiagnostics.RouteHops."+i+".HopRTTimes"};
			
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
			if (null == objLlist || objLlist.isEmpty()) {
				continue;
			}
			
			String HopHost = "";
			String HopHostAddress = "";
			String HopErrorCode = "";
			String HopRTTimes = "";
			
			for(ParameValueOBJ pvobj : objLlist){
				if(pvobj.getName().contains("HopHost")){
					HopHost = pvobj.getValue();
				}
				if(pvobj.getName().contains("HopHostAddress")){
					HopHostAddress = pvobj.getValue();
				}
				if(pvobj.getName().contains("HopErrorCode")){
					HopErrorCode = pvobj.getValue();
				}
				if(pvobj.getName().contains("HopRTTimes")){
					HopRTTimes = pvobj.getValue();
				}
			}
			//拼接返回值
			paralist.add(setPara("HopHost_"+i,HopHost));
			paralist.add(setPara("HopHostAddress_"+i,HopHostAddress));
			paralist.add(setPara("HopErrorCode_"+i,HopErrorCode));
			paralist.add(setPara("HopRTTimes_"+i,HopRTTimes));
			
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"],采集结果{}",paralist.toString());
		
		if(null == paralist || paralist.size()==0){
			result.setiOpRst(0);
		}else{
			result.setiOpRst(1);
			Para[] array = (Para[])paralist.toArray(new Para[paralist.size()]);
			result.setParaList(array);
		}
	}







	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void pingTest(String deviceId) {
	
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
			result.setiOpRst(-4);
		}
		Map downByHTTPMap = new HashMap<String, String>();
		getRpcValue(devRPCRep,downByHTTPMap,deviceId);
		String FailureCount = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.FailureCount");
		String SuccessCount = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.SuccessCount");
		String MaximumResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.MaximumResponseTime");
		String MinimumResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.MinimumResponseTime");
		String AverageResponseTime =StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.IPPingDiagnostics.AverageResponseTime");
		ArrayList<Para> paralist = new ArrayList<Para>();
		//拼接返回值
		paralist.add(setPara("FailureCount",FailureCount));
		paralist.add(setPara("SuccessCount",SuccessCount));
		paralist.add(setPara("MaximumResponseTime",MaximumResponseTime));
		paralist.add(setPara("MinimumResponseTime",MinimumResponseTime));
		paralist.add(setPara("AverageResponseTime",AverageResponseTime));
		
	    logger.warn(methodName+"["+dealXML.getOpId()+"],采集结果{}",paralist.toString());
		
		if(null == paralist || paralist.size()==0){
			result.setiOpRst(0);
		}else{
			result.setiOpRst(1);
			Para[] array = (Para[])paralist.toArray(new Para[paralist.size()]);
			result.setParaList(array);
		}
		
	}







	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void downloadSpeedTest(String deviceId) {
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
			logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持，无法下载测速！",deviceId);
			result.setiOpRst(-4);
		}
		Map downByHTTPMap = new HashMap<String, String>();
		getRpcValue(devRPCRep,downByHTTPMap,deviceId);
		logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]downByHTTPMap！",downByHTTPMap.toString());
		String rOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.ROMTime");
		String bOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.BOMTime");
		String eOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.EOMTime");
		String testtBytesReceived = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived");
		String totalBytesReceived =StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
		String tCPOpenRequestTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
		String tCPOpenResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
		ArrayList<Para> paralist = new ArrayList<Para>();
		//拼接返回值
		paralist.add(setPara("ROMTime",rOMTime));
		paralist.add(setPara("BOMTime",bOMTime));
		paralist.add(setPara("EOMTime",eOMTime));
		paralist.add(setPara("TestBytesReceived",testtBytesReceived));
		paralist.add(setPara("TotalBytesReceived",totalBytesReceived));
		paralist.add(setPara("TCPOpenRequestTime",tCPOpenRequestTime));
		paralist.add(setPara("TCPOpenResponseTime",tCPOpenResponseTime));
		
	    logger.warn(methodName+"["+dealXML.getOpId()+"],采集结果{}",paralist.toString());
		
		if(null == paralist || paralist.size()==0){
			result.setiOpRst(0);
		}else{
			result.setiOpRst(1);
			Para[] array = (Para[])paralist.toArray(new Para[paralist.size()]);
			result.setParaList(array);
		}
	}







	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void uploadSpeedTest(String deviceId) {
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = null;
		parameterNamesArr = new String[5];
		parameterNamesArr[0] = "InternetGatewayDevice.UploadDiagnostics.BOMTime";
		parameterNamesArr[1] = "InternetGatewayDevice.UploadDiagnostics.EOMTime";
		parameterNamesArr[2] = "InternetGatewayDevice.UploadDiagnostics.TotalBytesSent";
		parameterNamesArr[3] = "InternetGatewayDevice.UploadDiagnostics.TCPOpenRequestTime";
		parameterNamesArr[4] = "InternetGatewayDevice.UploadDiagnostics.TCPOpenResponseTime";
		parameterNamesArr[5] = "InternetGatewayDevice.UploadDiagnostics.ROMTime";
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
			logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持，无法上行测速！",deviceId);
			result.setiOpRst(-4);
		}
		Map downByHTTPMap = new HashMap<String, String>();
		getRpcValue(devRPCRep,downByHTTPMap,deviceId);
		String bOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.BOMTime");
		String eOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.EOMTime");
		String totalBytesSent =StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.TotalBytesSent");
		String tCPOpenRequestTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.TCPOpenRequestTime");
		String tCPOpenResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.TCPOpenResponseTime");
		String rOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.UploadDiagnostics.ROMTime");
		logger.warn("设备[{}]测速接受字节数为 [{}], 开始传输时间为[{}], 结束传输时间为[{}]",deviceId,totalBytesSent,bOMTime,eOMTime);
		ArrayList<Para> paralist = new ArrayList<Para>();
		//拼接返回值
		paralist.add(setPara("ROMTime",rOMTime));
		paralist.add(setPara("BOMTime",bOMTime));
		paralist.add(setPara("EOMTime",eOMTime));
		paralist.add(setPara("TotalBytesSent",totalBytesSent));
		paralist.add(setPara("TCPOpenRequestTime",tCPOpenRequestTime));
		paralist.add(setPara("TCPOpenResponseTime",tCPOpenResponseTime));
		
	    logger.warn(methodName+"["+dealXML.getOpId()+"],采集结果{}",paralist.toString());
		
		if(null == paralist || paralist.size()==0){
			result.setiOpRst(0);
		}else{
			result.setiOpRst(1);
			Para[] array = (Para[])paralist.toArray(new Para[paralist.size()]);
			result.setParaList(array);
		}
		
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
						result.setiOpRst(-4);
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
									result.setiOpRst(-4);
								}
							}
							else
							{
								logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！--",deviceId);
								logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
								result.setiOpRst(-4);
							}
						}
						else
						{
							logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！---",deviceId);
							logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
							result.setiOpRst(-4);
						}
					}
				}else
				{
					logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！----",deviceId);
					logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
					result.setiOpRst(-4);
				}
			}
			if (downByHTTPMap == null)
			{
				logger.warn(methodName+"["+dealXML.getOpId()+"],[{}]终端设备不支持诊断类型！-----",deviceId);
				logger.warn("[{}]DevRpcCmdOBJ.value == null",deviceId);
				result.setiOpRst(-4);
			}
		}
	}

	public static void main(String[] args) {
		String resp = "<cwmp:GetParameterValuesResponse>\n" +
				"\t<ParameterList SOAP-ENC:arrayType=\"cwmp:ParameterValueStruct[7]\">\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.ROMTime</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:dateTime\">2019-08-06T16:37:40.077121</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.BOMTime</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:dateTime\">2019-08-06T16:37:40.077121</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.EOMTime</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:dateTime\">2019-08-06T16:37:49.009166</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:unsignedInt\">1073741824</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:unsignedInt\">1073741824</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:dateTime\">2019-08-06T16:37:40.077121</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t\t<ParameterValueStruct>\n" +
				"\t\t\t<Name>InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime</Name>\n" +
				"\t\t\t<Value xsi:type=\"xsd:dateTime\">2019-08-06T16:37:40.077121</Value>\n" +
				"\t\t</ParameterValueStruct>\n" +
				"\t</ParameterList>\n" +
				"</cwmp:GetParameterValuesResponse>";
		SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
		Element element = soapOBJ.getRpcElement();
		if (element != null) {
			System.out.println(element);
			GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
					.GetParameterValuesResponse(element);

			ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse.getParameterList();
			Map downByHTTPMap = new HashMap<String, String>();
			for (int j = 0; j < parameterValueStructArr.length; j++) {
				downByHTTPMap.put(parameterValueStructArr[j].getName(),
						parameterValueStructArr[j].getValue().para_value);
			}
			String rOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.ROMTime");
			String bOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.BOMTime");
			String eOMTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.EOMTime");
			String testtBytesReceived = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TestBytesReceived");
			String totalBytesReceived =StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TotalBytesReceived");
			String tCPOpenRequestTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TCPOpenRequestTime");
			String tCPOpenResponseTime = StringUtil.getStringValue(downByHTTPMap, "InternetGatewayDevice.DownloadDiagnostics.TCPOpenResponseTime");
			System.out.println(rOMTime);
		}
	}

}
