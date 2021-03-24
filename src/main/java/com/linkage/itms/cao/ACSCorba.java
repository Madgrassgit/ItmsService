/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */

package com.linkage.itms.cao;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.obj.AddOBJ;
import com.linkage.itms.obj.ParameValueOBJ;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.*;
import com.linkage.litms.acs.soap.service.*;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CORBA operation for ACS.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jun 21, 2009
 * @see
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class ACSCorba
{

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ACSCorba.class);
	private static final String SVR_OBJECT_NAME = "ACS";
	private String gw_type;
	
	
	public ACSCorba(){
		if(Global.SYSTEM_ITMS.equals(Global.SYSTEM_NAME)){
			gw_type = "1";
		} else if(Global.SYSTEM_BBMS.equals(Global.SYSTEM_NAME)){
			gw_type = "2";
		} 
		else if(Global.SYSTEM_GTMS.equals(Global.SYSTEM_NAME)){
			gw_type = "4";
		}
		else {
			gw_type = "1";
		}
	}
	
	public ACSCorba(String gw_type){
		this.gw_type = gw_type;
	}
	/**
	 * GetParameterValues
	 * 
	 * @param deviceId
	 * @param arr
	 * @return
	 */
	public ArrayList<ParameValueOBJ> getValue(String deviceId, String name)
	{
		logger.debug("getValue({},{}): ", deviceId, name);
		ArrayList<ParameValueOBJ> objList = null;
		if (deviceId == null || name == null)
		{
			logger.debug("deviceId == null");
			return objList;
		}
		return getValue(deviceId, new String[] { name });
	}

	/**
	 * GetParameterValues
	 *
	 * @param deviceId
	 * @param arr
	 * @return
	 */
	public ArrayList<ParameValueOBJ> getValue(String deviceId, String[] arr)
	{
		logger.debug("getValue({},{}): ", deviceId, arr);
		ArrayList<ParameValueOBJ> objList = null;
		if (deviceId == null || arr == null || arr.length == 0)
		{
			logger.debug("deviceId == null");
			return objList;
		}
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(arr);

		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "GetParameterValues";
		rpcArr[0].rpcValue = getParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;

		List<DevRpcCmdOBJ> respArr = execRPC(deviceId,devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("-9");
			return objList;
		}
		String resp = getRespStr(respArr, "GetParameterValuesResponse");
		Fault fault = null;
		GetParameterValuesResponse getParameterValuesResponse = null;
		try
		{
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
			if (soapOBJ == null)
			{
				return objList;
			}
			fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
		}
		catch (Exception e)
		{
			logger.debug("{}", e.getMessage());
		}
		if (fault != null)
		{
			logger.warn("setValue({})={}", deviceId, fault.getDetail().getFaultString());
			return objList;
		}
		try
		{
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
			if (soapOBJ == null)
			{
				return objList;
			}
			getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
					.getRpcElement());
		}
		catch (Exception e)
		{
			logger.debug("{}", e.getMessage());
		}
		if (getParameterValuesResponse != null)
		{
			ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
					.getParameterList();
			if(null == parameterValueStructArr)
			{
				return objList;
			}
			objList = new ArrayList<ParameValueOBJ>();
			ParameValueOBJ obj = null;
			for (int i = 0; i < parameterValueStructArr.length; i++)
			{
				obj = new ParameValueOBJ();
				obj.setName(parameterValueStructArr[i].getName());
				obj.setValue(parameterValueStructArr[i].getValue().para_value);
				obj.setType(parameterValueStructArr[i].getValue().para_type_id);
				objList.add(obj);
			}
		}
		return objList;
	}


	/**
	 * 常用：根据指定的路径获得下面的i
	 *
	 * @param paramPath
	 * @return
	 */
	public List<String> getIList(String deviceId,String paramPath)
	{
		logger.warn("[{}]获取节点{}", deviceId, paramPath);
		Map<String, String> connMap = getParaTreeMap(deviceId,paramPath);
		if (null == connMap || 0 == connMap.size())
		{
			logger.warn("[{}]获取节点{}失败", deviceId, paramPath);

			try
			{
				logger.warn("[{}]等待3s再次获取。[{}]", deviceId, paramPath);
				Thread.sleep(3000);
				connMap = getParaTreeMap(deviceId,paramPath);
				if (null == connMap || 0 == connMap.size())
				{
					logger.warn("[{}]再次获取节点{}失败。", deviceId, paramPath);
					return null;
				}
			}
			catch (Exception e)
			{
				logger.error("[{}]获取节点{}失败。异常：" , deviceId, paramPath,e);
				return null;
			}
		}
		logger.warn("[{}]getIListMap({})=[{}]", deviceId, paramPath, connMap);
		// 存放实际的pvc实例
		List<String> jList = new ArrayList<String>();
		Set<String> set = connMap.keySet();
		Iterator<String> iterator = set.iterator();
		// 去除空节点、与父节点同名的节点
		while (iterator.hasNext())
		{
			String name = iterator.next();
			String value = connMap.get(name);
			if (null == value)
			{
				continue;
			}
			// 分离出节点名
			value = value.substring(0, value.indexOf(","));
			if (null != value && !"".equals(value) && !paramPath.equals(value))
			{
				// 从节点名称中分离出索引号
				try {
					jList.add(value.substring(paramPath.length(), value.lastIndexOf(".")));
				} catch (StringIndexOutOfBoundsException e) {
					logger.error("[{}]getIListMap字符串越界[{}]", deviceId, value);
				}
				
			}
		}
		// 清空connMap
		connMap = null;
		return jList;
	}
	
	
	/**
	 * GetParameterValues
	 * 
	 * @param deviceId
	 * @param arr
	 * @return
	 */
	public Map<String, String> getParaTreeMap(String deviceId, String arrPath)
	{
		logger.debug("getParaTreeMap({},{}): ", deviceId, arrPath);
		Map<String, String> paramMap = new HashMap<String, String>();
		if (deviceId == null || arrPath == null || arrPath.length() == 0)
		{
			logger.debug("deviceId == null");
			return paramMap;
		}
		
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(arrPath);
		getParameterNames.setNextLevel(1);

		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "GetParameterValues";
		rpcArr[0].rpcValue = getParameterNames.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> respArr = execRPC(deviceId,devRpcArr);
		if (null == respArr || respArr.isEmpty())
		{
			return paramMap;
		}
		// 一个设备返回的命令
		
		String setRes = respArr.get(0).getRpcList().get(0).getValue();
		
		
		GetParameterNamesResponse getParameterNamesResponse = new GetParameterNamesResponse();
		SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
		if (soapOBJ == null)
		{
			return null;
		}
		getParameterNamesResponse = XmlToRpc.GetParameterNamesResponse(soapOBJ
				.getRpcElement());
		// 通过这个XML对象,获取参数列表
		if (null != getParameterNamesResponse)
		{
			ParameterInfoStruct[] pisArr = getParameterNamesResponse.getParameterList();
			if (null != pisArr)
			{
				String name = null;
				for (int i = 0; i < pisArr.length; i++)
				{
					name = pisArr[i].getName();
					String writable = pisArr[i].getWritable();
					paramMap.put(i + "", name + "," + writable);
				}
			}
			// 清空pisArr
			pisArr = null;
		}
		// 清空对象
		getParameterNames = null;
		devRpcArr = null;
		getParameterNamesResponse = null;
		return paramMap;
	}

	/**
	 * SetParameterValues
	 * 
	 * @param deviceId
	 * @param obj
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-7:系统参数错误</li>
	 *            <li>-6:设备正被操作</li>
	 *            <li> -1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int setValue(String deviceId, ParameValueOBJ obj)
	{
		logger.debug("setValue({},{}): ", deviceId, obj);
		int flag = -1;
		if (deviceId == null || obj == null)
		{
			logger.debug("deviceId == null");
			return flag;
		}
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		objList.add(obj);
		return setValue(deviceId, objList);
	}

	/**
	 * SetParameterValues
	 * 
	 * @param deviceId
	 * @param objList
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-7:系统参数错误</li>
	 *            <li>-6:设备正被操作</li>
	 *            <li> -1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int setValue(String deviceId, ArrayList<ParameValueOBJ> objList)
	{
		logger.debug("setValue({},{}): ", deviceId, objList);
		int flag = -9;
		if (deviceId == null || objList == null || objList.isEmpty())
		{
			logger.debug("deviceId == null");
			return flag;
		}
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] parameterValueStruct = new ParameterValueStruct[objList
				.size()];
		AnyObject anyObject = null;
		for (int i = 0; i < objList.size(); i++)
		{
			parameterValueStruct[i] = new ParameterValueStruct();
			parameterValueStruct[i].setName(objList.get(i).getName());
			anyObject = new AnyObject();
			anyObject.para_type_id = objList.get(i).getType();
			anyObject.para_value = objList.get(i).getValue();
			parameterValueStruct[i].setValue(anyObject);
		}
		setParameterValues.setParameterList(parameterValueStruct);
		setParameterValues.setParameterKey("GWMS");
		
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> respArr = execRPC(deviceId,devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("flag is -9");
			flag = -9;
			return flag;
		}
		flag = respArr.get(0).getStat();
		logger.debug("respArr getStat = {}",flag);
		return flag;
	}
	
	/**
	 * 下发参数(可以获取到失败节点相关信息)
	 * @param deviceId
	 * @param objList
	 * @param rpcObj 失败节点相关信息
	 * @return 
	 */
	public int setValue(String deviceId, ArrayList<ParameValueOBJ> objList, com.ailk.tr069.devrpc.obj.mq.Rpc rpcObj)
	{
		logger.debug("setValue({},{}): ", deviceId, objList);
		int flag = -9;
		if (deviceId == null || objList == null || objList.size() == 0)
		{
			logger.debug("deviceId == null");
			return flag;
		}
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] parameterValueStruct = new ParameterValueStruct[objList
				.size()];
		AnyObject anyObject = null;
		for (int i = 0; i < objList.size(); i++)
		{
			parameterValueStruct[i] = new ParameterValueStruct();
			parameterValueStruct[i].setName(objList.get(i).getName());
			anyObject = new AnyObject();
			anyObject.para_type_id = objList.get(i).getType();
			anyObject.para_value = objList.get(i).getValue();
			parameterValueStruct[i].setValue(anyObject);
		}
		setParameterValues.setParameterList(parameterValueStruct);
		setParameterValues.setParameterKey("GWMS");
		
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> respArr = execRPC(deviceId,devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("flag = -9");
			flag = -9;
			return flag;
		}
		flag = respArr.get(0).getStat();
		
		logger.warn("[{}]respArr: {}", deviceId, respArr);
		
		if(respArr == null||respArr.isEmpty()||respArr.get(0)==null||respArr.get(0).getRpcList()==null||
				respArr.get(0).getRpcList().isEmpty()||respArr.get(0).getRpcList().get(0)==null){
			return -9;
		}
		List<com.ailk.tr069.devrpc.obj.mq.Rpc> cmdList = respArr.get(0).getRpcList();
		com.ailk.tr069.devrpc.obj.mq.Rpc obj = null;
		for (int i = 0; i < cmdList.size(); i++)
		{
			obj = cmdList.get(i);
			if (obj == null)
			{
				continue;
			}
			rpcObj.setValue(obj.getValue());
		}
		logger.warn("[{}]调用ACS返回的结果 = {}",deviceId, rpcObj.getValue());
		return flag;
	}
	
	
	
	public String getRespStr(List<DevRpcCmdOBJ> devRPCRep,String cmdName){
		logger.debug("getRespStr()");
		String rpcRes = null;
		if (null != devRPCRep) {
			if (null != devRPCRep.get(0)) {
				ArrayList<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
				if (null != rpcList && !rpcList.isEmpty()) {
					for(com.ailk.tr069.devrpc.obj.mq.Rpc rpc : rpcList){
						if(rpc.getRpcName() != null && rpc.getRpcName().equals(cmdName)){
							rpcRes = rpc.getValue();
							break;
						}
					}
				} else {
					logger.warn("ACS Reponse devRPCRep.get(0).getRpcList() is NULL...");
				}
			} else {
				logger.warn("ACS Reponse devRPCRep.get(0) is NULL...");
			}
		} else {
			logger.warn("ACS Reponse DevRPCRep is NULL...");
		}
		logger.debug("getRespStr({}): return ", rpcRes);
		return rpcRes;
	}
	/**
	 * AddObject
	 * 
	 * @param deviceId
	 * @param name
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-1:设备参数为空</li>
	 *            <li>-2:设备正被操作</li>
	 *            <li> -3:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public AddOBJ add(String deviceId, String name)
	{
		logger.debug("add({},{}): ", deviceId, name);
		AddOBJ obj = new AddOBJ();
		if (deviceId == null || name == null)
		{
			logger.debug("deviceId == null");
			obj.setStatus(-9);
			return obj;
		}
		AddObject addObject = new AddObject();
		addObject.setObjectName(name);
		addObject.setParameterKey("GWMS");
		
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "AddObject";
		rpcArr[0].rpcValue = addObject.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> respArr = execRPC(deviceId, devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("flag = -9");
			obj.setStatus(-9);
			return obj;
		}
		int flag = respArr.get(0).getStat();
		if(flag == 1)
		{
			String resp = getRespStr(respArr,"AddObjectResponse");
	
			obj.setStatus(respArr.get(0).getStat());
			AddObjectResponse addObjectResponse = null;
			try
			{
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
				if (soapOBJ == null)
				{
					return obj;
				}
				addObjectResponse = XmlToRpc.AddObjectResponse(soapOBJ.getRpcElement());
			}
			catch (Exception e)
			{
				logger.debug("{}", e.getMessage());
			}
			if (addObjectResponse == null)
			{
				logger.debug("flag = -9");
				obj.setStatus(-9);
			}
			else
			{
				obj.setStatus(addObjectResponse.getStatus());
				obj.setInstance(addObjectResponse.getInstanceNumber());
			}
		}
		else
		{
			obj.setStatus(flag);
		}
		return obj;
	}

	/**
	 * DeleteObject
	 * 
	 * @param deviceId
	 * @param name
	 *            TR069节点
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-1:设备参数为空</li>
	 *            <li>-2:设备正被操作</li>
	 *            <li> -3:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int del(String deviceId, String name)
	{
		logger.debug("setValue({},{}): ", deviceId, name);
		int flag = -9;
		if (deviceId == null || name == null)
		{
			logger.debug("deviceId == null");
			return flag;
		}
		DeleteObject rpc = new DeleteObject();
		rpc.setObjectName(name);
		rpc.setParameterKey("GWMS");
		
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "DeleteObject";
		rpcArr[0].rpcValue = rpc.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> respArr = execRPC(deviceId, devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("flag = -9");
			flag = -9;
			return flag;
		}
		flag = respArr.get(0).getStat();
		logger.debug("respArr getStat={}",flag);
		return flag;
	}

	/**
	 * 调用ACS的RPC方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPCRep[]
	 */
	public List<DevRpcCmdOBJ> execRPC(String deviceId, DevRpc[] devRpcArr)
	{
		logger.debug("execRPC({},{}): ", deviceId, devRpcArr);
		if (null == deviceId || null == devRpcArr)
		{
			logger.warn("null == deviceId");
			return null;
		}
		return execRPC(devRpcArr);
	}

	/**
	 * 调用ACS的RPC方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPCRep[]
	 */
	private List<DevRpcCmdOBJ> execRPC(DevRpc[] devRpcArr)
	{
		logger.debug("execRPC({}):", devRpcArr);
		if (null == devRpcArr || devRpcArr.length == 0)
		{
			logger.warn("pnull == devRPCArr");
			return null;
		}
		List<DevRpcCmdOBJ> devRPCRep = null;
		AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(getPrefixName(gw_type)+Global.SYSTEM_ACS);
		devRPCRep = acsCorbaDAO.execRPC(Global.ClIENT_ID, Global.RpcCmd_Type, Global.Priority_Hig, devRpcArr);
		return devRPCRep;
	}
	/**
	 * 调用ACS的检测 RPC方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPCRep[]
	 */
	public List<DevRpcCmdOBJ> exectestRPC(DevRpc[] devRpcArr)
	{
		logger.debug("execRPC({}):", devRpcArr);
		if (null == devRpcArr || devRpcArr.length == 0)
		{
			logger.warn("pnull == devRPCArr");
			return null;
		}
		List<DevRpcCmdOBJ> devRPCRep = null;
		AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(getPrefixName(gw_type)+Global.SYSTEM_ACS);
		devRPCRep = acsCorbaDAO.execRPC(Global.ClIENT_ID, Global.RpcTest_Type, Global.Priority_Hig, devRpcArr);
		return devRPCRep;
	}
	/**
	 * 调用ACS的RPC方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPCRep[]
	 */
	public List<DevRpcCmdOBJ> execRPC(DevRpc[] devRpcArr,int rpcType)
	{
		logger.debug("execRPC({}):", devRpcArr);
		if (null == devRpcArr || devRpcArr.length == 0)
		{
			logger.warn("pnull == devRPCArr");
			return null;
		}
		List<DevRpcCmdOBJ> devRPCRep = null;
		AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(getPrefixName(gw_type)+Global.SYSTEM_ACS);
		devRPCRep = acsCorbaDAO.execRPC(Global.ClIENT_ID, rpcType,
				Global.Priority_Hig, devRpcArr);
		return devRPCRep;
	}

	/**
	 * 获取DevRPC[],操作设备属性数组
	 * 
	 * @param deviceId
	 *            设备id
	 * @param stringArr
	 *            和设备交互的参数
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPC[]
	 */
//	private DevRPC[] getDevRpcArray(String deviceId, String[] stringArr)
//	{
//		logger.debug("getDevRpcArray({},{}):", deviceId, stringArr);
//		DevRPC[] devRPCArr = null;
//		if (null != deviceId)
//		{
//			String sqlDevInfo = "select loopback_ip, cr_port"
//					+ ",cr_path, acs_username, acs_passwd, device_id"
//					+ ",oui,device_serialnumber from tab_gw_device"
//					+ " where device_id=?";
//			PrepareSQL psql = new PrepareSQL(sqlDevInfo);
//			psql.setString(1, deviceId);
//			Map devMap = DataSetBean.getRecord(psql.getSQL());
//			if (null != devMap && false == devMap.isEmpty())
//			{
//				devRPCArr = new DevRPC[1];
//				devRPCArr[0] = new DevRPC();
//				devRPCArr[0].DeviceId = String.valueOf(devMap.get("device_id"));
//				devRPCArr[0].OUI = String.valueOf(devMap.get("oui"));
//				devRPCArr[0].SerialNumber = String.valueOf(devMap
//						.get("device_serialnumber"));
//				devRPCArr[0].ip = String.valueOf(devMap.get("loopback_ip"));
//				devRPCArr[0].port = Integer.valueOf(devMap.get("cr_port").toString());
//				devRPCArr[0].path = String.valueOf(devMap.get("cr_path"));
//				devRPCArr[0].username = String.valueOf(devMap.get("acs_username"));
//				devRPCArr[0].passwd = String.valueOf(devMap.get("acs_passwd"));
//				devRPCArr[0].rpcArr = stringArr;
//			}
//		}
//		return devRPCArr;
//	}

	/**
	 * 调用ACS， 1成功 0失败
	 * 
	 * @param infoType = 1    码流
		chgType=1    开启
		chgType=0    关闭
		data = device_id
		 
		chgType    设备版本变化
		chgType=1    增加、更新版本
		chgType=0    删除版本
		data =  version_id
		
		 
		chgType    ACS认证方式
		chgType=1     认证方式    tab_auth(access_flag)
		data=""           

	 * @author Jason(3412)
	 * @date 2009-7-29
	 * @return int
	 */
	public int chgInfo(int chgType, int infoType, String data)
	{
		logger.debug("chgInfo({},{},{})", new Object[]{chgType, infoType,data});
		return new AcsCorbaDAO(getPrefixName(gw_type)+Global.SYSTEM_ACS).chgInfo(chgType, infoType, data);
	}

	/**
	 * 更新设备
	 * 
	 * @param deviceIdArr
	 *            设备ID列表
	 * @author alex(yanhj@)
	 * @date 2010-4-14
	 * @return int
	 *         <UL>
	 *         <li>-1:通知失败</li>
	 *         <li>0:参数错误</li>
	 *         <li>1:通知成功</li>
	 *         </UL>
	 */
//	public int chgDev(String[] deviceIdArr, String[] devSnArr, String[] ouiArr)
//	{
//		logger.debug("chgDev({})", deviceIdArr);
//		logger.debug("deviceIdArr" + deviceIdArr.length);
//		int r = 0;
//		Dev[] devArr = new Dev[deviceIdArr.length];
//		if (null == deviceIdArr || null == devSnArr || null == ouiArr
//				|| deviceIdArr.length == 0)
//		{
//			logger.debug("null = deviceIdArr");
//			return r;
//		}
//		for (int i = 0; i < deviceIdArr.length; i++)
//		{
//			logger.debug("deviceIdArr====>" + deviceIdArr[i]);
//			logger.debug("devSnArr====>" + devSnArr[i]);
//			logger.debug("ouiArr====>" + ouiArr[i]);
//			if (deviceIdArr[i] == null || devSnArr[i] == null || ouiArr[i] == null)
//			{
//				logger.debug("null == deviceIdArr");
//				return r;
//			}
//			devArr[i] = new Dev();
//			devArr[i].devId = deviceIdArr[i];
//			devArr[i].devOui = ouiArr[i];
//			devArr[i].devSn = devSnArr[i];
//		}
//		RPCManager acsCorbaRpcManager = ACSDistributeUtil
//				.getRPCManagerBydeviceId(deviceIdArr[0]);
//		try
//		{
//			acsCorbaRpcManager.chgDev(Global.ClIENT_ID, devArr);
//		}
//		catch (Exception e)
//		{
//			logger.debug("CORBA ACS Error:{},Rebind.", e.getMessage());
//			acsCorbaRpcManager = ACSDistributeUtil
//					.getRPCManagerBydeviceIdSecond(deviceIdArr[0]);
//			try
//			{
//				acsCorbaRpcManager.chgDev(Global.ClIENT_ID, devArr);
//			}
//			catch (Exception e1)
//			{
//				logger.error("CORBA ACS Error,Rebind.");
//				acsCorbaRpcManager = ACSDistributeUtil
//						.getRPCManagerBydeviceIdThird(deviceIdArr[0]);
//				try
//				{
//					acsCorbaRpcManager.chgDev(Global.ClIENT_ID, devArr);
//				}
//				catch (Exception e2)
//				{
//					logger.error("CORBA ACS Error,Rebind.");
//					r = -1;
//					return r;
//				}
//			}
//		}
//		r = 1;
//		return r;
//	}

	/**
	 * 更新设备
	 * 
	 * @param deviceId
	 *            设备ID
	 * @author alex(yanhj@)
	 * @date 2010-4-14
	 * @return int
	 *         <UL>
	 *         <li>-1:通知失败</li>
	 *         <li>0:参数错误</li>
	 *         <li>1:通知成功</li>
	 *         </UL>
	 */
//	public int chgDev(String deviceId)
//	{
//		logger.debug("chgDev({})", deviceId);
//		return this.chgDev(new String[] { deviceId });
//	}
	/**
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @date 2009-7-8
	 * @param deviceId
	 * @param gatherId
	 * @param path
	 * @param nextLevel
	 * @return
	 */
	public ArrayList<String> getParamNamesPath(String deviceId,
			String path, int nextLevel)
	{
		ArrayList<String> pislist = null;
		ParameterInfoStruct[] pisArr = null;
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(path);
		getParameterNames.setNextLevel(nextLevel);
		DevRpc[] realTimeObj = getDevRPCArr(deviceId, getParameterNames);
		List<DevRpcCmdOBJ> devRpcCmdOBJList = invokeAcsCorba(deviceId ,realTimeObj);
		Element element = dealDevRPCResponse("GetParameterNamesResponse",
				devRpcCmdOBJList, deviceId);
		if (element == null)
		{
			return null;
		}
		GetParameterNamesResponse getParameterNamesResponse = null;
		getParameterNamesResponse = XmlToRpc.GetParameterNamesResponse(element);
		if (getParameterNamesResponse != null)
		{
			pisArr = getParameterNamesResponse.getParameterList();
		}
		if(pisArr!=null && pisArr.length>0){
			pislist = new ArrayList<String>();
			for (int i = 0; i < pisArr.length; i++)
			{
				pislist.add(pisArr[i].getName());
			}
		}
		// 清空对象
		getParameterNames = null;
		realTimeObj = null;
		getParameterNamesResponse = null;
		pisArr = null;
		
		return pislist;
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
	private DevRpc[] getDevRPCArr(String deviceId,RPCObject rpcObject)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		String[] stringArr = new String[1];
		String stringRpcName = "";
		if (rpcObject == null)
		{
			stringArr[0] = "";
			stringRpcName = "";
		}
		else
		{
			stringArr[0] = rpcObject.toRPC();
			stringRpcName = rpcObject.getClass().getSimpleName();
		}
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = stringRpcName;
		rpc.rpcValue = stringArr[0];
		
		logger.debug("[{}] call  acscorba rpc.rpcName:[{}]", deviceId, rpc.rpcName);
		
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		
		logger.debug("[{}] call  acscorba :", deviceId);
		
		return devRPCArr;
	}
	
	/**
	 * 单台设备单条命令返回的RPC结果处理
	 * 
	 * @author wangsenbo
	 * @date Mar 22, 2011
	 * @param
	 * @return RPCObject
	 */
	private Element dealDevRPCResponse(String stringRpcName,
			List<DevRpcCmdOBJ> devRpcCmdOBJList, String deviceId)
	{
		if (devRpcCmdOBJList == null || devRpcCmdOBJList.size() == 0)
		{
			logger.error("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
			return null;
		}
		DevRpcCmdOBJ devRpcCmdOBJ = devRpcCmdOBJList.get(0);
		if (devRpcCmdOBJ == null)
		{
			logger.error("[{}]DevRpcCmdOBJ返回为空！", deviceId);
			return null;
		}
		List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcCmdObjList = devRpcCmdOBJ.getRpcList();
		if (rpcCmdObjList == null || rpcCmdObjList.size() == 0)
		{
			logger.error("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
			return null;
		}
		com.ailk.tr069.devrpc.obj.mq.Rpc acsRpcCmdObj = rpcCmdObjList.get(0);
		if (acsRpcCmdObj == null)
		{
			logger.error("[{}]ACSRpcCmdOBJ返回为空！", deviceId);
			return null;
		}
		if (stringRpcName == null)
		{
			logger.error("[{}]stringRpcName为空！", deviceId);
			return null;
		}
		if (stringRpcName.equals(acsRpcCmdObj.getRpcName()))
		{
			String resp = acsRpcCmdObj.getValue();
			logger.debug("[{}]设备返回：{}", deviceId, resp);
			if (resp == null || "".equals(resp))
			{
				logger.error("[{}]DevRpcCmdOBJ.value == null", deviceId);
			}
			else
			{
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
				if (soapOBJ != null)
				{
					return soapOBJ.getRpcElement();
				}
			}
		}
		return null;
	}
	
	/**
	 * @param devRPCArr
	 * @return
	 */
	private List<DevRpcCmdOBJ> invokeAcsCorba(DevRpc[] devRPCArr)
	{
		List<DevRpcCmdOBJ> list = new ACSCorba().execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);
		if(list == null || list.isEmpty())
		{
			return null;
		}
		
		if(list.get(0).getRpcList() == null || list.get(0).getRpcList().isEmpty())
		{
			logger.warn("[{}] call  acscorba paraList:[{}]", devRPCArr[0].devId, list.get(0).getRpcList());
			return null;
		}
		
		return list;
	}
	
	public List<DevRpcCmdOBJ> execRPC(String clientId, int i, int j, DevRpc[] devRPCArr)
	{
		logger.debug("execRPC()");
//		return new AcsCorbaDAO(Global.SYSTEM_NAME + SVR_OBJECT_NAME).execRPC(
//				Global.CLIENT_ID, i, j, devRPCArr);
		if (true)
		{
			try
			{
				if(devRPCArr!=null&&devRPCArr.length>0){
					logger.warn("[{}]调用acs休眠  500 毫秒",devRPCArr[0].devId);
				}
				Thread.sleep(500);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		List<DevRpcCmdOBJ> list = null;
		String device = (devRPCArr == null || devRPCArr[0] == null)? "" : devRPCArr[0].devId;
		try
		{

			logger.warn("[{}]调用acs开始,systemKey={},devRPCArr={},devRPCArr.size={}",
					device,(Global.SYSTEM_NAME + SVR_OBJECT_NAME),devRPCArr,devRPCArr == null ? 0 : devRPCArr.length);
			AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(getPrefixName(gw_type)+SVR_OBJECT_NAME);
			list = acsCorbaDAO.execRPC(
					Global.ClIENT_ID, i, j, devRPCArr);
			logger.warn("[{}]调用acs结束，list.size={}",device,list.size());
		}
		catch (Exception e)
		{
			logger.error("[{}]调用ac发生异常,error:",device,e);
		}
		return list;
	}
	
	/**
	 * 获取参数实例值的Map(tr069)
	 * 
	 * @param para_name
	 * @return Map paramMap
	 */
	public Map<String, String> getParaValueMap(String deviceId,String[] paraName)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(paraName);
		DevRpc[] devRPCArr = getDevRPCArr(deviceId,getParameterValues);
		try
		{
			List<DevRpcCmdOBJ> list = invokeAcsCorba(deviceId,devRPCArr);
			// 一个设备返回的命令
			if (list == null)
			{
				return paramMap;
			}
			String setRes = list.get(0).getRpcList().get(0).getValue(); 
			paramMap.put("status", StringUtil.getStringValue(list.get(0).getStat()));
			getParameterValues = null;
			devRPCArr = null;
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			try
			{
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
				if (soapOBJ == null)
				{
					return null;
				}
				getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
						.getRpcElement());
			}
			catch (Exception e)
			{
				logger.error("[{}]设备返回的数据有误，仅提示，不处理", deviceId);
			}
			if (null != getParameterValuesResponse)
			{
				ParameterValueStruct[] pisArr = getParameterValuesResponse
						.getParameterList();
				if (pisArr != null)
				{
					String name = null;
					String value = null;
					for (int i = 0; i < pisArr.length; i++)
					{
						name = pisArr[i].getName();
						value = pisArr[i].getValue().para_value;
						paramMap.put(name, value);
					}
				}
				// 清空pisArr, getParameterValuesResponse
				pisArr = null;
				getParameterValuesResponse = null;
			}
			else
			{
				return paramMap;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return paramMap;
	}
	
	
	
	public Map<String, String> getParaValueMapStat(String deviceId,String[] paraName)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(paraName);
		DevRpc[] devRPCArr = getDevRPCArr(deviceId,getParameterValues);
		try
		{
			List<DevRpcCmdOBJ> list = invokeAcsCorba(deviceId,devRPCArr);
			// 一个设备返回的命令
			if (list == null)
			{
				logger.error("[{}]设备返回的list=null", deviceId);
				return paramMap;
			}
			String setRes = list.get(0).getRpcList().get(0).getValue(); 
			paramMap.put("status", StringUtil.getStringValue(list.get(0).getStat()));
			logger.warn("[{}]获取参数返回status={}",deviceId,paramMap.get("status"));
			
			if(list.get(0).getStat()!=0 && list.get(0).getStat()!=1){
				return paramMap;
			}
			getParameterValues = null;
			devRPCArr = null;
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			try
			{
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
				if (soapOBJ == null)
				{
					return null;
				}
				getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
						.getRpcElement());
			}
			catch (Exception e)
			{
				logger.error("[{}]设备返回的数据有误，仅提示，不处理", deviceId);
			}
			if (null != getParameterValuesResponse)
			{
				ParameterValueStruct[] pisArr = getParameterValuesResponse
						.getParameterList();
				if (pisArr != null)
				{
					String name = null;
					String value = null;
					for (int i = 0; i < pisArr.length; i++)
					{
						name = pisArr[i].getName();
						value = pisArr[i].getValue().para_value;
						paramMap.put(name, value);
						logger.warn("getParaValueMap-Name:{}, Value={}", name, value);
					}
				}
				// 清空pisArr, getParameterValuesResponse
				pisArr = null;
				getParameterValuesResponse = null;
			}
			else
			{
				return paramMap;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return paramMap;
	}
	
	/**
	 * @param devRPCArr
	 * @return
	 */
	private List<DevRpcCmdOBJ> invokeAcsCorba(String deviceId,DevRpc[] devRPCArr)
	{
		List<DevRpcCmdOBJ> list = execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);
		if(list == null || list.isEmpty())
		{
			logger.debug("[{}] call  acscorba response list:[{}]", deviceId, list);
			return null;
		}
		
		if(list.get(0).getRpcList() == null || list.get(0).getRpcList().isEmpty())
		{
			logger.warn("[{}] call  acscorba paraList:[{}]", deviceId, list.get(0).getRpcList());
			return null;
		}
		
		return list;
	}
	
	/**
	 * add by zhangchy 2013-01-22
	 * 
	 * 调ACS 获取设备在线状态（实时）
	 * 
	 * @param deviceId
	 * @param gwType
	 * @return
	 */
	public int testConnection(String deviceId, String gwType)
	{

		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "";
		rpcArr[0].rpcValue = "";
		devRPCArr[0].rpcArr = rpcArr;
		// corba
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager(gw_type);
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.RpcTest_Type);
		int flag = 0;
		if (devRPCRep == null || devRPCRep.isEmpty())
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
		}
		else
		{
			flag = devRPCRep.get(0).getStat();
		}
		return flag;
	}
	
	
	public static String getPrefixName (String gwType){
		if(Global.GW_TYPE_ITMS.equals(gwType)){
			return Global.SYSTEM_ITMS_PREFIX;
		} else if(Global.GW_TYPE_BBMS.equals(gwType)){
			return Global.SYSTEM_BBMS_PREFIX;
		} else if (Global.GW_TYPE_STB.equals(gwType)) {
			return Global.SYSTEM_STB_PREFIX;
		}else {
			return "";
		}
	}
}
