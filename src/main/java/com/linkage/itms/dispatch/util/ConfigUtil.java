package com.linkage.itms.dispatch.util;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterInfoStruct;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigUtil {

	static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

	
	private String device_serialnumber;
	private String loopback_ip;
	private String cr_port;
	private String cr_path;
	private String gather_id;
	private String acs_username;
	private String acs_passwd;
	private String oui;
	private String ior;
	private String deviceId;
	
	private String servDefaultId;
	
	private int acs_stat;
	
	private Map<String, String> devInfoMap = null;
	
	/**
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @param cr_path
	 * @return
	 */
	public static Map<String, String> getParaTreeMap(String cr_path, DevRpcObject devRpcObj) {
		Map<String, String> paramMap = new HashMap<String, String>();

		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(cr_path);
		getParameterNames.setNextLevel(1);

		DevRpc[] devRPCArr = getDevRPCArr(getParameterNames,devRpcObj);
		
		
//		String devRPCRep = getDevRPCResponse(devRPCArr,devRpcObj);
		List<DevRpcCmdOBJ> list = new AcsCorba().execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);

		if (null == list || list.isEmpty()) 
		{
			return paramMap;
		}

		// 一个设备返回的命令
//		String setRes = devRPCRep[0].rpcArr[0];
		String setRes = list.get(0).getRpcList().get(0).getValue();
		//TODO  根据响应消息 解析响应报文     已修改待测试
		
		

		GetParameterNamesResponse getParameterNamesResponse = new GetParameterNamesResponse();
		SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
        if (soapOBJ == null) {
        	return null;
        }
		getParameterNamesResponse = XmlToRpc.GetParameterNamesResponse(soapOBJ.getRpcElement());

		// 通过这个XML对象,获取参数列表
		if (null != getParameterNamesResponse) {

			ParameterInfoStruct[] pisArr = getParameterNamesResponse.getParameterList();
			if (null != pisArr) {

				String name = null;
				for (int i = 0; i < pisArr.length; i++) {
					name = pisArr[i].getName();

					String writable = pisArr[i].getWritable();
					paramMap.put(i + "", name + "," + writable);
				}
			}
			//清空pisArr
			pisArr = null;
		}
		
		//清空对象
		getParameterNames = null;
		devRPCArr = null;
		getParameterNamesResponse = null;
		
		return paramMap;
	}
	

	/**
	 * 获得上网方式
	 * @author gongsj
	 * @date 2009-9-1
	 * @return
	 */
	public String getAccessType(String deviceId, boolean havingDefaultValue) {
//		getDevInfo(deviceId);
		this.deviceId = deviceId;
		String accessType = getParamValueString("InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType");
		
		if (null == accessType) {
			logger.warn("[{}]取得accessType为null，休眠3秒再取一次", deviceId);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				logger.error("InterruptedException:",e);
				// Restore interrupted state...      
				Thread.currentThread().interrupt();
			}
			accessType = getParamValueString("InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType");
			logger.warn("[{}]第二次从设备上取得accessType为：{}",deviceId, accessType);
		}
		
		logger.warn("[{}]从设备上取得accessType为：{}",deviceId, accessType);
		
		return accessType;
	}
	
	/**
	 * 常用：根据参数名获取值
	 * @param paramName
	 * @return
	 */
	public String getParamValueString(String paramName) {
		
		String[] paramNames = new String[] { paramName };
		Map<String, String> paraValues = getParaValueMap(paramNames);
		logger.warn("paraValues;{}", paraValues);
		
		if (null == paraValues || 0 == paraValues.size()) {
			return null;
		}
		
		return paraValues.get(paramName);

	}
	
	/**
	 * 获取参数实例值的Map(tr069)
	 * 
	 * @param para_name
	 * @return Map paramMap
	 */
	private Map<String, String> getParaValueMap(String[] para_name) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		
		logger.warn("[{}]调用设备 configutil.getParaValueMap : para_name[{}]", deviceId, para_name[0]);
		
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(para_name);
		
		
		DevRpc[] devRPCArr = getDevRPCArr(getParameterValues);

		try {
			List<DevRpcCmdOBJ> list = new AcsCorba().execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);

			if (list == null || list.isEmpty()) 
			{
				return paramMap;
			}
			
			logger.debug("调用ACS返回的 结果状态： {}", list.get(0).getStat()); 
			logger.debug("调用ACS返回的 cmdList.size() = {}", list.get(0).getRpcList().size());
			
			if(list.get(0) == null){
				return paramMap;
			}
			
			acs_stat = list.get(0).getStat();
			
//			String[] setRes_ = devRPCRep[0].rpcArr;
//			if (setRes_ == null) {
//				return paramMap;
//			}
//			String setRes = devRPCRep[0].rpcArr[0];
			if(list.get(0).getRpcList() ==null||list.get(0).getRpcList().isEmpty()){
				return paramMap;
			}
			if(list.get(0).getRpcList().get(0) ==null){
				return paramMap;
			}
			String setRes = list.get(0).getRpcList().get(0).getValue(); //TODO 根据返回消息解析响应报文  已修改待测试

			//暂时获取不到和设备不在线返回的是否一样？涉及到是否要等待3秒重新获取，暂时先打印看下
			logger.warn("[{}]调用设备返回 setRes:{}", deviceId, setRes);
			
			getParameterValues = null;
			devRPCArr = null;
			
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
	        if (soapOBJ == null) {
	        	return null;
	        }
			getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ.getRpcElement());

			if (null != getParameterValuesResponse) {
				ParameterValueStruct[] pisArr = getParameterValuesResponse.getParameterList();
				if (pisArr != null) {
					String name = null;
					String value = null;
					for (int i = 0; i < pisArr.length; i++) {
						name = pisArr[i].getName();
						value = pisArr[i].getValue().para_value;
						paramMap.put(name, value);
					}
				}
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
	private DevRpc[] getDevRPCArr(RPCObject rpcObject) {
		DevRpc[] devRPCArr = new DevRpc[1];

		if (rpcObject == null) {
			return null;
		}

		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = rpcObject.getClass().getSimpleName();
		rpc.rpcValue = rpcObject.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] {rpc};

		return devRPCArr;
	}
	
	/**
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @param cr_path
	 * @return
	 */
	public Map<String, String> getParaTreeMap(String cr_path) {
		Map<String, String> paramMap = new HashMap<String, String>();

		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(cr_path);
		getParameterNames.setNextLevel(1);

		DevRpc[] devRPCArr = getDevRPCArr(getParameterNames);
		
//		String devRPCRep = getDevRPCResponse(devRPCArr);
		List<DevRpcCmdOBJ> list = new AcsCorba().execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);

		if (null == list || list.isEmpty()) 
		{
			return paramMap;
		}

		// 一个设备返回的命令
//		String setRes = devRPCRep[0].rpcArr[0];
		String setRes = list.get(0).getRpcList().get(0).getValue(); //TODO 根据返回消息解析响应报文  已修改待测试

		GetParameterNamesResponse getParameterNamesResponse = new GetParameterNamesResponse();
		SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
        if (soapOBJ == null) {
        	return null;
        }
		getParameterNamesResponse = XmlToRpc.GetParameterNamesResponse(soapOBJ.getRpcElement());

		// 通过这个XML对象,获取参数列表
		if (null != getParameterNamesResponse) {

			ParameterInfoStruct[] pisArr = getParameterNamesResponse.getParameterList();
			if (null != pisArr) {

				String name = null;
				for (int i = 0; i < pisArr.length; i++) {
					name = pisArr[i].getName();

					String writable = pisArr[i].getWritable();
					paramMap.put(i + "", name + "," + writable);
				}
			}
			//清空pisArr
			pisArr = null;
		}
		
		//清空对象
		getParameterNames = null;
		devRPCArr = null;
		getParameterNamesResponse = null;
		
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
	public static DevRpc[] getDevRPCArr(RPCObject rpcObject, DevRpcObject devRpcObj) {
		DevRpc[] devRPCArr = new DevRpc[1];

		if (rpcObject == null) {
			return null;
		}
		String[] stringArr = new String[1];
		stringArr[0] = rpcObject.toRPC();

		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = devRpcObj.getDevice_id();
		Rpc rpc = new Rpc();
		rpc.rpcValue = rpcObject.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] {rpc};

		return devRPCArr;
	}
}
