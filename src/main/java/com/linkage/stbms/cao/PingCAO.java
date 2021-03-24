package com.linkage.stbms.cao;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.obj.PingOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jason(3412)
 * @date 2009-12-17
 */
public class PingCAO {

	private static final Logger logger = LoggerFactory.getLogger(PingCAO.class);

	private PingOBJ pingObj;

	/**
	 * ping检测调用方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-16
	 * @return void
	 */
//	public void ping() {
//		int flag = -9;
//		logger.info("ping(): {}", pingObj);
//
//		String[] arrParam = null;
//		String deviceId = null;
//		if (null == pingObj
//				|| StringUtil.IsEmpty(deviceId = pingObj.getDeviceId())
//				|| null == (arrParam = createDevRPCArray())) {
//			logger.warn("deivceId or rpcArr is null");
//		}
//		ACSCorba acsCorba = new ACSCorba();
//		logger.info("00000000000000");
//		List<DevRpcCmdOBJ> devRPCRep = acsCorba.execRPC(deviceId, arrParam);
//		logger.info("1111111111aaaa");
//		String rpcRes = null;
//		if (null != devRPCRep) {
//			if (null != devRPCRep.get(0)) {
//				logger.info("ddddddddddddddddddddd{}",devRPCRep.size());
//				if (null != devRPCRep.get(0).getRpcList()) {
//					logger.info("sssssssssssssssssssss{}",devRPCRep.get(0).getRpcList().size());
//					ArrayList<Rpc> rpcList = devRPCRep.get(0).getRpcList();
//					if (!rpcList.isEmpty()) {
//						logger.info("3333333333333333{}",rpcList.size());
//						for(Rpc rpc : rpcList){
//							logger.info("2222222222",rpc);
//							if(rpc.getRpcName() != null && rpc.getRpcName().equals("GetParameterValuesResponse")){
//								rpcRes = rpc.getValue();
//								break;
//							}
//						}
//					}
//				} else {
//					logger.warn("ACS Reponse devRPCRep.get(0).getRpcList() is NULL...");
//				}
//			} else {
//				logger.warn("ACS Reponse devRPCRep.get(0) is NULL...");
//			}
//		} else {
//			logger.warn("ACS Reponse DevRPCRep is NULL...");
//		}
//		if (null != rpcRes) {
//			// "XXX-0" "XXX-1" "XXX-2" "XXX-9"
//			if ("XXX-0".equals(rpcRes)) {
//				logger.debug("XXX-0");
//				flag = -1;
//				pingObj.setSuccess(false);
//				pingObj.setFaultCode(flag);
//			} else if ("XXX-1".equals(rpcRes)) {
//				logger.debug("XXX-1");
//				flag = -7;
//				pingObj.setSuccess(false);
//				pingObj.setFaultCode(flag);
//			} else if ("XXX-2".equals(rpcRes)) {
//				logger.debug("XXX-2");
//				flag = -6;
//				pingObj.setSuccess(false);
//				pingObj.setFaultCode(flag);
//			} else {
//				Fault fault = null;
//				try {
//					SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(rpcRes)); 
//					fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
//				} catch (Exception e) {
//					logger.warn("{}", e.getMessage());
//				}
//
//				if (fault != null) {
//					flag = StringUtil.getIntegerValue(fault.getDetail()
//							.getFaultCode(), -9);
//					logger.warn("setValue({})={}", pingObj.getDeviceId(), fault
//							.getDetail().getFaultString());
//					pingObj.setSuccess(false);
//					pingObj.setFaultCode(flag);
//				} else if (null != rpcRes) {
//					// 获取ping测试结构Map
//					Map<String, String> pingMap = getDevParamMap(rpcRes);
//					if (null != pingMap && false == pingMap.isEmpty()) {
//						pingObj
//								.setSuccNum(StringUtil.getIntegerValue(pingMap
//												.get("Device.LAN.IPPingDiagnostics.SuccessCount")));
//						pingObj
//								.setFailNum(StringUtil.getIntegerValue(pingMap
//												.get("Device.LAN.IPPingDiagnostics.FailureCount")));
//						pingObj
//								.setDelayAvg(StringUtil.getIntegerValue(pingMap
//												.get("Device.LAN.IPPingDiagnostics.AverageResponseTime")));
//						pingObj
//								.setDelayMin(StringUtil.getIntegerValue(pingMap
//												.get("Device.LAN.IPPingDiagnostics.MinimumResponseTime")));
//						pingObj
//								.setDelayMax(StringUtil.getIntegerValue(pingMap
//												.get("Device.LAN.IPPingDiagnostics.MaximumResponseTime")));
//						pingObj.setSuccess(true);
//					} else {
//						pingObj.setSuccess(false);
//						pingObj.setFaultCode(flag);
//					}
//				} else {
//					pingObj.setSuccess(false);
//					pingObj.setFaultCode(flag);
//				}
//			}
//		}

//	}
	public void ping() {
		int flag = -9;
		logger.debug("ping()");
		
		if (null == pingObj || StringUtil.IsEmpty(pingObj.getDeviceId())) {
			logger.warn("deivceId or rpcArr is null");
			return;
		}
		
		DevRpc[] devRPCArr = createDevRPCArray(pingObj.getDeviceId());
		
		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(Global.CLIENT_ID, 
				Global.rpcType_DIAG, Global.ACS_PRIORITY, devRPCArr);
		if (null == list || list.isEmpty())
        {
			flag = -9;
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
			return;
        }
		if(1!=list.get(0).getStat()){
			flag = list.get(0).getStat();
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
			return;
		}
		String setRes = list.get(0).getRpcList().get(1).getValue();
		
        SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
        if (soapOBJ == null)
        {
        	flag = -9;
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
			return;
        }
        
        Map<String, String> pingMap = getDevParamMap(soapOBJ);
		if (null != pingMap && false == pingMap.isEmpty()) {
			pingObj.setSuccNum(StringUtil.getIntegerValue(pingMap
									.get("Device.LAN.IPPingDiagnostics.SuccessCount")));
			pingObj.setFailNum(StringUtil.getIntegerValue(pingMap
									.get("Device.LAN.IPPingDiagnostics.FailureCount")));
			pingObj.setDelayAvg(StringUtil.getIntegerValue(pingMap
									.get("Device.LAN.IPPingDiagnostics.AverageResponseTime")));
			pingObj.setDelayMin(StringUtil.getIntegerValue(pingMap
									.get("Device.LAN.IPPingDiagnostics.MinimumResponseTime")));
			pingObj.setDelayMax(StringUtil.getIntegerValue(pingMap
									.get("Device.LAN.IPPingDiagnostics.MaximumResponseTime")));
			pingObj.setSuccess(true);
		} else {
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
		}

	}
	/**
	 * 生成调用ACS的结构数组，用于ping检测
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-16
	 * @return DevRPC[]
	 */
	public DevRpc[] createDevRPCArray(String deviceId) {
		logger.debug("createDevRPCArray()");
		SetParameterValues setParameterValues = getSetParam();

		GetParameterValues getParameterValues = getResponseParam();

		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc1 = new Rpc();
		rpc1.rpcId = "1";
		rpc1.rpcName = setParameterValues.getClass().getSimpleName();
		rpc1.rpcValue = setParameterValues.toRPC();;
		
		Rpc rpc2 = new Rpc();
		rpc2.rpcId = "2";
		rpc2.rpcName = getParameterValues.getClass().getSimpleName();
		rpc2.rpcValue = getParameterValues.toRPC();;
		
		devRPCArr[0].rpcArr = new Rpc[] { rpc1,rpc2 };
		
		return devRPCArr;
	}
	/**
	 * 获取设备的采集结点信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return void
	 */
	public Map<String, String> getDevParamMap(SoapOBJ soapOBJ) {
		Map<String, String> paramMap = null;
		GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
		getParameterValuesResponse = XmlToRpc
				.GetParameterValuesResponse(soapOBJ.getRpcElement());
		int arrayLen = getParameterValuesResponse.getParameterList().length;
		ParameterValueStruct[] paramStruct = new ParameterValueStruct[arrayLen];
		paramStruct = getParameterValuesResponse.getParameterList();

		// 获取ping测试结构Map
		paramMap = new HashMap<String, String>();
		if (null != paramStruct && paramStruct.length > 0) {
			for (int i = 0; i < paramStruct.length; i++) {
				paramMap.put(paramStruct[i].getName(), paramStruct[i]
						.getValue().para_value);
			}
		}
		
		logger.debug("getDevParamMap(): return " + paramMap);
		return paramMap;
	}

	/**
	 * ping检测需要设备的结点参数
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-16
	 * @return SetParameterValues
	 */
	public SetParameterValues getSetParam() {
		logger.debug("getSetParam()");
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[6];

		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("Device.LAN.IPPingDiagnostics.DiagnosticsState");
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);

		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("Device.LAN.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = pingObj.getPingAddr();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);

		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2]
				.setName("Device.LAN.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getPackNum());
		anyObject.para_type_id = "3";
		ParameterValueStruct[2].setValue(anyObject);

		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("Device.LAN.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getTimeout());
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);

		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4]
				.setName("Device.LAN.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getPackSize());
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);

		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("Device.LAN.IPPingDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getDscp());
		anyObject.para_type_id = "1";
		ParameterValueStruct[5].setValue(anyObject);

		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("Ping");

		return setParameterValues;
	}

	/**
	 * ping检测设备返回检测结果结点树
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-16
	 * @return GetParameterValues
	 */
	public GetParameterValues getResponseParam() {
		logger.debug("getResponseParam()");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[5];
		parameterNamesArr[0] = "Device.LAN.IPPingDiagnostics.SuccessCount";
		parameterNamesArr[1] = "Device.LAN.IPPingDiagnostics.FailureCount";
		parameterNamesArr[2] = "Device.LAN.IPPingDiagnostics.AverageResponseTime";
		parameterNamesArr[3] = "Device.LAN.IPPingDiagnostics.MaximumResponseTime";
		parameterNamesArr[4] = "Device.LAN.IPPingDiagnostics.MinimumResponseTime";
		getParameterValues.setParameterNames(parameterNamesArr);

		return getParameterValues;

	}

	/**
	 * 生成调用ACS的结构数组，用于ping检测
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-16
	 * @return DevRPC[]
	 */
	public String[] createDevRPCArray() {
		logger.debug("createDevRPCArray()");
		SetParameterValues setParameterValues = getSetParam();

		GetParameterValues getParameterValues = getResponseParam();

		String[] stringArr = new String[3];
		stringArr[0] = setParameterValues.toRPC();
		stringArr[1] = "";
		stringArr[2] = getParameterValues.toRPC();
		return stringArr;
	}

	/**
	 * 获取设备的采集结点信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return void
	 */
	public Map<String, String> getDevParamMap(String getRes) {
		logger.debug("getDevParamMap({})", getRes);
		Map<String, String> paramMap = null;
		if (false == StringUtil.IsEmpty(getRes)) {
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(getRes)); 
			getParameterValuesResponse = XmlToRpc
					.GetParameterValuesResponse(soapOBJ.getRpcElement());
			int arrayLen = getParameterValuesResponse.getParameterList().length;
			ParameterValueStruct[] paramStruct = new ParameterValueStruct[arrayLen];
			paramStruct = getParameterValuesResponse.getParameterList();

			// 获取ping测试结构Map
			paramMap = new HashMap<String, String>();
			if (null != paramStruct && paramStruct.length > 0) {
				for (int i = 0; i < paramStruct.length; i++) {
					paramMap.put(paramStruct[i].getName(), paramStruct[i]
							.getValue().para_value);
				}
			}
		} else {
			logger.warn("ACS Reponse getRes is NULL...");
		}

		logger.debug("getDevParamMap(): return " + paramMap);
		return paramMap;
	}

	public PingOBJ getPingObj() {
		return pingObj;
	}

	public void setPingObj(PingOBJ pingObj) {
		logger.debug("setPingObj({})", pingObj);
		this.pingObj = pingObj;
	}
}
