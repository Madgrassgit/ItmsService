
package com.linkage.stbms.ids.bio;

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
import com.linkage.stbms.cao.SuperGatherCorba;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.obj.PingOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator(工号) Tel:78
 * @version 1.0
 * @since 2011-4-28 下午05:18:52
 * @category com.linkage.stbms.ids.bio
 * @copyright 南京联创科技 网管科技部
 */
public class StbTraceRouteBIO
{

	private static final Logger logger = LoggerFactory.getLogger(StbTraceRouteBIO.class);
	private PingOBJ pingObj;
//	private StbTraceRouteDAO dao;

	/**
	 * traceRoute检测调用方法
	 * 
	 * @param
	 * @author zhangshimin(67310)
	 * @date 2011-4-26
	 * @return void
	 */
	public void traceRoute()
	{
		int flag = -9;
		logger.debug("pingACS()");
		if (null == pingObj || StringUtil.IsEmpty(pingObj.getDeviceId()))
		{
			logger.warn("deivceId or rpcArr is null");
			return;
		}
		DevRpc[] devRPCArr = createDevRPCArray(pingObj.getDeviceId());
		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(
				Global.CLIENT_ID, Global.rpcType,
				Global.priority, devRPCArr);
		if (null == list || list.isEmpty())
		{
			flag = -9;
			pingObj.setSuccess(false);
			pingObj.setResult("0");
			pingObj.setFaultCode(flag);
			pingObj.setFaultStr(Global.G_Fault_Map.get(flag));
			return;
		}
		if (1 != list.get(0).getStat())
		{
			flag = list.get(0).getStat();
			pingObj.setSuccess(false);
			pingObj.setResult("0");
			pingObj.setFaultCode(flag);
			pingObj.setFaultStr(Global.G_Fault_Map.get(flag));
			return;
		}
		String setRes = list.get(0).getRpcList().get(1).getValue();
		logger.warn("setRes:"+setRes);
        SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
        if (soapOBJ == null)
        {
        	flag = -9;
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
			return;
        }
        
        
        //获取ping测试结构Map
        
		Map<String, String> pingMap = getDevParamMap(soapOBJ);
		logger.warn("pingMap:{}",pingMap);
		if (null != pingMap && !pingMap.isEmpty()) {
			pingObj.setResponseTime(pingMap.get("Device.LAN.TraceRouteDiagnostics.ResponseTime"));
			pingObj.setNumberOfRouteHops(pingMap.get("Device.LAN.TraceRouteDiagnostics.NumberOfRouteHops"));

			final int numb = Integer.parseInt(pingObj.getNumberOfRouteHops());
			List<String> hopHostIList = new ArrayList<String>();
			for(int i=1;i<=numb;i++)
			{
				String temp = pingMap.get("Device.LAN.TraceRouteDiagnostics.RouteHops."+i+".HopHost");
				if(null != temp && !"".equals(temp))
				{
					hopHostIList.add("Host-"+ i + ": " + temp);
				}
			}
			pingObj.setHopHostI(hopHostIList);
			pingObj.setSuccess(true);
			/**由于StbTraceRouteService代码中要判断pingOBJ.getFaultCode() != 1，如果不增加此段代码,回参将不完整*/
			pingObj.setFaultCode(1); 
		} else {
			pingObj.setSuccess(false);
			pingObj.setFaultCode(flag);
		}
		
		pingObj.setSuccess(true);
		pingObj.setResult("1");
		logger.warn("traceRoute success：" + pingObj.isSuccess());
//		// 如果调用ACS成功
//		if (pingObj.isSuccess())
//		{
//			// 进一步调用采集模块
//			pingSuperGather(pingObj);
//			// 采集成功，查询数据库数据
//			if (pingObj.isSuccess())
//			{
//				dao = new StbTraceRouteDAO();
//				dao.queryTraceRouteInfo(pingObj);
//			}
//		}
	}

	/**
	 * 调用采集模块采集设备
	 */
	public void pingSuperGather(PingOBJ pingObj)
	{
		logger.debug("pingSuperGather({})", new Object[] { pingObj.getDeviceId() });
		// 返回结果
		int result = new SuperGatherCorba().getCpeParams(pingObj.getDeviceId(),
				Global.GATHER_TRACEROUTE);
		// 成功
		if (result == 1)
		{
			pingObj.setSuccess(true);
			pingObj.setResult("1");
		}
		else
		{
			pingObj.setSuccess(false);
			pingObj.setResult("0");
			pingObj.setFaultCode(result);
			pingObj.setFaultStr(Global.G_Fault_Map.get(result));
		}
		logger.debug("pingSuperGather success：" + pingObj.isSuccess());
	}

	/**
	 * ping检测需要设备的结点参数
	 * 
	 * @param
	 * @author zhangshimin
	 * @date 2011-5-3
	 * @return SetParameterValues
	 */
	public SetParameterValues getSetParam()
	{
		logger.debug("getSetParam()");
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[6];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName("Device.LAN.TraceRouteDiagnostics.DiagnosticsState");
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("Device.LAN.TraceRouteDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = pingObj.getPingAddr();
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("Device.LAN.TraceRouteDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getTimeout());
		anyObject.para_type_id = "3";
		ParameterValueStruct[2].setValue(anyObject);
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("Device.LAN.TraceRouteDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getPackSize());
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("Device.LAN.TraceRouteDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getDscp());
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("Device.LAN.TraceRouteDiagnostics.MaxHopCount");
		anyObject = new AnyObject();
		anyObject.para_value = String.valueOf(pingObj.getMaxHopCount());
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("Ping");
		return setParameterValues;
	}

	/**
	 * ping检测设备返回检测结果结点树
	 * 
	 * @param
	 * @author zhangshimin
	 * @date 2011-5-3
	 * @return GetParameterValues
	 */
	public GetParameterValues getResponseParam()
	{
		logger.debug("getResponseParam()");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[2];
		parameterNamesArr[0] = "Device.LAN.TraceRouteDiagnostics.NumberOfRouteHops";
		parameterNamesArr[1] = "Device.LAN.TraceRouteDiagnostics.ResponseTime";
		getParameterValues.setParameterNames(parameterNamesArr);
		return getParameterValues;
	}

	/**
	 * 生成调用ACS的结构数组，用于ping检测
	 * 
	 * @param
	 * @author zhangshimin
	 * @date 2011-5-3
	 * @return DevRPC[]
	 */
	public DevRpc[] createDevRPCArray(String deviceId)
	{
		logger.debug("createDevRPCArray()");
		SetParameterValues setParameterValues = getSetParam();
		GetParameterValues getParameterValues = getResponseParam();
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc1 = new Rpc();
		rpc1.rpcId = "1";
		rpc1.rpcName = "SetParameterValues";
		rpc1.rpcValue = setParameterValues.toRPC();
		Rpc rpc2 = new Rpc();
		rpc2.rpcId = "2";
		rpc2.rpcName = "GetParameterValues";
		rpc2.rpcValue = getParameterValues.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] { rpc1, rpc2 };
		return devRPCArr;
	}

	/**
	 * 获取设备的采集结点信息
	 * 
	 * @param
	 * @author zhangshimin
	 * @date 2011-5-3
	 * @return void
	 */
	public Map<String, String> getDevParamMap(SoapOBJ soapOBJ)
	{
		Map<String, String> paramMap = null;
		GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
		getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
				.getRpcElement());
		int arrayLen = getParameterValuesResponse.getParameterList().length;
		ParameterValueStruct[] paramStruct = new ParameterValueStruct[arrayLen];
		paramStruct = getParameterValuesResponse.getParameterList();
		// 获取ping测试结构Map
		paramMap = new HashMap<String, String>();
		if (null != paramStruct && paramStruct.length > 0)
		{
			for (int i = 0; i < paramStruct.length; i++)
			{
				paramMap.put(paramStruct[i].getName(),
						paramStruct[i].getValue().para_value);
			}
		}
		logger.warn("getDevParamMap(): return " + paramMap);
		return paramMap;
	}

	public PingOBJ getPingObj()
	{
		return pingObj;
	}

	public void setPingObj(PingOBJ pingObj)
	{
		this.pingObj = pingObj;
	}
}
