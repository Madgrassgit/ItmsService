package com.linkage.stbms.pic.util.corba;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.*;
import com.linkage.stbms.pic.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPCManagerClient {

	final Logger logger = LoggerFactory.getLogger(RPCManagerClient.class);
	
	/**
	 * 常用：实时设置批量参数(TR069)
	 * @param paramNames
	 * @param paramValues
	 * @param paraTypeIds
	 * @return true 设置成功 false 设置失败
	 */
	public boolean realSetParamsArr(String[] paramNames, String[] paramValues, String[] paramTypeIds, String deviceId) {
		// 最外层结构:SetParameterValues
		SetParameterValues setParameterValues = new SetParameterValues();
		setParameterValues.setParameterKey("ss");
		
		for (String para_type : paramTypeIds) {
			logger.debug("[{}]paraTypeIds: {}", deviceId, para_type);
		}

		ParameterValueStruct[] parameterValueStructArr = new ParameterValueStruct[paramNames.length];

		for (int i = 0; i < paramNames.length; i++) {
			AnyObject anyObject = new AnyObject();
			anyObject.para_value = paramValues[i];
			anyObject.para_type_id = paramTypeIds[i];
			parameterValueStructArr[i] = new ParameterValueStruct();
			parameterValueStructArr[i].setName(paramNames[i]);
			parameterValueStructArr[i].setValue(anyObject);
		}

		setParameterValues.setParameterList(parameterValueStructArr);

		DevRpc[] devRPCArr = getDevRPCArr(setParameterValues,deviceId);
		logger.debug("[{}]devRPCArr: {}", deviceId, devRPCArr);
		
		try {
			List<DevRpcCmdOBJ> list = getDevRPCResponse(devRPCArr);
			// 一个设备返回的命令
			logger.debug("[{}]devRPCRep: {}", deviceId, list);
			
			if(list == null||list.size()==0||list.get(0)==null||list.get(0).getRpcList()==null||list.get(0).getRpcList().get(0)==null){
				return false;
			}
			String setRes = list.get(0).getRpcList().get(0).getValue();
			
			// 转换成SetParameterValuesResponse格式，如转换成功，说明是设置参数的返回命令，表示执行成功，否则失败。
			SetParameterValuesResponse setParameterValuesResponse = new SetParameterValuesResponse();
			// 把SOAP形式的文件转换成标准的XML,便于通信
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
			if (soapOBJ == null)
			{
				return false;
			}
			setParameterValuesResponse = XmlToRpc.SetParameterValuesResponse(soapOBJ
					.getRpcElement());
			
			logger.debug("[{}]setParameterValuesResponse:{}", deviceId, setParameterValuesResponse);
			
			//清空对象
			setParameterValues = null;
			parameterValueStructArr = null;
			devRPCArr = null;
			
			if (null != setParameterValuesResponse) {
				//清空对象
				setParameterValuesResponse = null;
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("[{}]实时设置参数失败", deviceId);
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 常用：实时设置批量参数(TR069)
	 * @param paramNames
	 * @param paramValues
	 * @param paraTypeIds
	 * @return true 设置成功 false 设置失败
	 */
	public DevRpc[] realSetParamsArrInt(String[] paramNames, String[] paramValues, String[] paramTypeIds, String deviceId) {
		// 最外层结构:SetParameterValues
		SetParameterValues setParameterValues = new SetParameterValues();
		setParameterValues.setParameterKey("ss");
		
		for (String para_type : paramTypeIds) {
			logger.warn("[{}]paraTypeIds: {}", deviceId, para_type);
		}

		ParameterValueStruct[] parameterValueStructArr = new ParameterValueStruct[paramNames.length];

		for (int i = 0; i < paramNames.length; i++) {
			AnyObject anyObject = new AnyObject();
			anyObject.para_value = paramValues[i];
			anyObject.para_type_id = paramTypeIds[i];
			parameterValueStructArr[i] = new ParameterValueStruct();
			parameterValueStructArr[i].setName(paramNames[i]);
			parameterValueStructArr[i].setValue(anyObject);
		}

		setParameterValues.setParameterList(parameterValueStructArr);

		DevRpc[] devRPCArr = getDevRPCArr(setParameterValues,deviceId);
		logger.debug("[{}]devRPCArr: {}", deviceId, devRPCArr);
		return  devRPCArr;
	}
	
	/**
	 * 获取参数实例值的Map(tr069)
	 * 
	 * @param para_name
	 * @return Map paramMap
	 */
	public Map<String, String> getParaValueMap(String[] para_name,String deviceId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(para_name);

		DevRpc[] devRPCArr = getDevRPCArr(getParameterValues,deviceId);
		// 为NULL，则直接返回
		if (devRPCArr == null)
			return paramMap;

		try {
//			DevRPCRep[] devRPCRep = getDevRPCResponse(devRPCArr);
			List<DevRpcCmdOBJ> list = getDevRPCResponse(devRPCArr);
			// 一个设备返回的命令
			if (list == null)
			{
				return paramMap;
			}

//			String[] setRes_ = devRPCRep[0].rpcArr;
			String setRes = list.get(0).getRpcList().get(0).getValue(); //TODO 处理调用后 响应的消息  已修改 待测试
			
//			String setRes = devRPCRep[0].rpcArr[0];

			getParameterValues = null;
			devRPCArr = null;
			
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			try {
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
				if (soapOBJ == null)
				{
					return null;
				}
				getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
						.getRpcElement());
			} catch (Exception e) {
				logger.error("[{}]设备返回的数据有误，仅提示，不处理", deviceId);
			}

			if (null != getParameterValuesResponse) {
				ParameterValueStruct[] pisArr = getParameterValuesResponse.getParameterList();
				if (pisArr != null) {
					String name = null;
					String value = null;
					for (int i = 0; i < pisArr.length; i++) {
						name = pisArr[i].getName();
						value = pisArr[i].getValue().para_value;
						paramMap.put(name, value);
						logger.debug("getParaValueMap-Name:{}, Value={}", name, value);
					}
				}
				//清空pisArr, getParameterValuesResponse
				pisArr = null;
				getParameterValuesResponse = null;
				
			} else {
				return paramMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return paramMap;
	}
	
	/**
	 * 根据device_id得到长度为1的DevRPC对象数组
	 * 
	 * @param device_id
	 *            设备id
	 * @param rpcObject
	 *            ----GetParameterValues/GetParameterNames/
	 * @return
	 */
	public DevRpc[] getDevRPCArr(RPCObject rpcObject,String deviceId) 
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		String[] stringArr = new String[1];
		if (rpcObject == null)
		{
			stringArr[0] = "";
		}
		else
		{
			stringArr[0] = rpcObject.toRPC();
		}
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = rpcObject == null ? "" : rpcObject.getClass().getSimpleName();
		rpc.rpcValue = stringArr[0];
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		return devRPCArr;
	}
	
	/**
	 * 删除参数实例，成功返回 1 ；失败 0
	 * 
	 * @param paraV
	 * @param ior
	 * @param device_id
	 * @param gather_id
	 * @return int flag
	 */
	public int delPara(String paraV,String deviceId) {

		int flag = 0;

		DeleteObject delObject = new DeleteObject();
		delObject.setObjectName(paraV);
		delObject.setParameterKey("");

		DevRpc[] devRPCArr = getDevRPCArr(delObject,deviceId);
		try {
//			DevRPCRep[] devRPCRep = getDevRPCResponse(devRPCArr);
			List<DevRpcCmdOBJ> list = getDevRPCResponse(devRPCArr);
			if (list == null || list.isEmpty())
			{
				return 0;
			}

			// 一个设备返回的命令
//			String setRes = devRPCRep[0].rpcArr[0];
			String setRes = list.get(0).getRpcList().get(0).getValue(); 

			DeleteObjectResponse delObjectResponse = new DeleteObjectResponse();
			// 把SOAP形式的文件转换成标准的XML,便于通信
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
			if (soapOBJ == null)
			{
				return 0;
			}
			delObjectResponse = XmlToRpc.DeleteObjectResponse(soapOBJ.getRpcElement());
			
			if (null != delObjectResponse) {
				flag = 1;
				logger.warn("[{}]delObjectResponse={}", deviceId, delObjectResponse.getStatus());
			}
			
			//清空对象
			delObject = null;
			devRPCArr = null;
			delObjectResponse = null;
			
		} catch (Exception e) {
			flag = 0;
			logger.error("{}", e.getStackTrace());
		}
		
		return flag;
	}

	/**
	 * bind corba
	 * 
	 * @param devRPCRepObj
	 * @param ior
	 * @return
	 */
	public List<DevRpcCmdOBJ> realTimeInvokeACS(DevRpc[] devRPCArr) 
		throws Exception 
	{
		
		List<DevRpcCmdOBJ> list = null;

		if (null != devRPCArr && devRPCArr.length > 0) 
		{
			if (Global.IS_SLEEP == 1) {
				try {
					logger.warn("休眠"+Global.SLEEP_TIME+"毫秒");
					Thread.sleep(Global.SLEEP_TIME);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			list = new AcsCorbaDAO(Global.SYSTEM_NAME+"ACS").execRPC(Global.CLIENT_ID, 1, 1, devRPCArr);
		}
		
		return list;
	}
	
	/**
	 * 根据得到DevRPC[]对象和ior开始调用corba接口通知后台
	 * 
	 * @param ior
	 *            ior字符串user.getIor()
	 * @param devRPCArr
	 *            DevRpc[]数组对象
	 * @return 返回执行后的RemoteDB.DevRPCRep[]
	 */
	public List<DevRpcCmdOBJ> getDevRPCResponse(DevRpc[] devRPCArr) {
		if (devRPCArr == null)
			return null;
		// 调用corba接口通知后台
		List<DevRpcCmdOBJ> list = null;
		
		try {
			list = realTimeInvokeACS(devRPCArr);
		} catch (Exception e) {
			logger.error("[{}]第一次调用CORBA失败", devRPCArr[0].devId);
			e.printStackTrace();
			
			try {
				logger.warn("[{}]开始第二次调用CORBA", devRPCArr[0].devId);
				list = realTimeInvokeACS(devRPCArr);
			} catch (Exception e2) {
				logger.error("[{}]第二次调用CORBA失败", devRPCArr[0].devId);
				//logger.error("异常：{}" + e2.getStackTrace());
				e2.printStackTrace();
			}
		}
		
		return list;
	}
	
	/**
	 * 重启设备
	 * @param deviceId
	 */
	public void reboot(String deviceId)
	{
		Reboot reboot = new Reboot();
		reboot.setCommandKey("Reboot");
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "Reboot";
		rpcArr[0].rpcValue = reboot.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> respArr = getDevRPCResponse(devRpcArr);
	}
	
}
