/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.stbms.cao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.service.RPCObject;
import com.linkage.litms.acs.soap.service.Reboot;
import com.linkage.litms.acs.soap.service.SetParameterValues;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.itv.main.StbServGlobals;

/**
 * CORBA operation for ACS.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jun 21, 2009
 * @see
 * @since 1.0
 */

/**
 * 增加了方法getDeviceStatus(String deviceId)
 *          getDevRPCArr(String deviceId,RPCObject rpcObject)
 * 用于测试设备在线状态
 */

	/** log */
public class ACSCorba {
	private static final Logger logger = LoggerFactory
			.getLogger(ACSCorba.class);
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
		logger.debug("flag = " + flag);
		return flag;
	}
	/**
	 * reboot
	 * @param deviceId
	 * @author zhangshimin
	 * @date 2011-12-27
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-7:系统参数错误</li>
	 *            <li>-6:设备正被操作</li>
	 *            <li> -1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int reboot(String deviceId)
	{
		int flag = -9;
		logger.info("device reboot. deviceId:" + deviceId);
		Reboot reboot = new Reboot();
		reboot.setCommandKey("65535");

		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = reboot.getClass().getSimpleName();
		rpc.rpcValue = reboot.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		
		List<DevRpcCmdOBJ> list = execRPC(deviceId, devRPCArr);

		if (null == list || list.isEmpty())
		{
			flag = -9;
		    return flag;
		}
		flag = list.get(0).getStat();
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
	 * GetParameterValues
	 * 
	 * @param deviceId
	 * @param arr
	 * @return
	 */
	public ArrayList<ParameValueOBJ> getValue(String deviceId, String name) {
		logger.debug("getValue({},{}): ", deviceId, name);

		ArrayList<ParameValueOBJ> objList = null;
		if (deviceId == null || name == null) {
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
	public ArrayList<ParameValueOBJ> getValue(String deviceId, String[] arr) {
		logger.debug("getValue({},{}): ", deviceId, arr);

		ArrayList<ParameValueOBJ> objList = null;
//		if (deviceId == null || arr == null || arr.length == 0) {
//			logger.debug("deviceId == null");
//
//			return objList;
//		}
//
//		GetParameterValues getParameterValues = new GetParameterValues();
//		getParameterValues.setParameterNames(arr);
//		DevRPCRep[] respArr = execRPC(deviceId,
//				new String[] { getParameterValues.toRPC() });
//
//		if (respArr == null || respArr[0] == null) {
//			logger.debug("-7");
//
//			return objList;
//		}
//		String resp = respArr[0].rpcArr[0];
//		if ("XXX-0".equals(resp)) {
//			logger.debug("-1");
//
//			return objList;
//		}
//
//		if ("XXX-1".equals(resp)) {
//			logger.debug("-7");
//
//			return objList;
//		}
//
//		if ("XXX-2".equals(resp)) {
//			logger.debug("-6");
//
//			return objList;
//		}
//		Fault fault = null;
//		GetParameterValuesResponse getParameterValuesResponse = null;
//		try {
//			fault = XmlToRpc.Fault(XML.CreateXML(resp));
//		} catch (Exception e) {
//			logger.debug("{}", e.getMessage());
//		}
//
//		if (fault != null) {
//			logger.warn("setValue({})={}", deviceId, fault.getDetail().getFaultString());
//
//			return objList;
//		}
//		try {
//			getParameterValuesResponse = XmlToRpc
//					.GetParameterValuesResponse(XML.CreateXML(resp));
//		} catch (Exception e) {
//			logger.debug("{}", e.getMessage());
//		}
//		if (getParameterValuesResponse != null) {
//			ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
//					.getParameterList();
//			ParameValueOBJ obj = null;
//			for (int i = 0; i < parameterValueStructArr.length; i++) {
//				obj = new ParameValueOBJ();
//				obj.setName(parameterValueStructArr[i].getName());
//				obj.setValue(parameterValueStructArr[i].getValue().para_value);
//				obj.setType(parameterValueStructArr[i].getValue().para_type_id);
//				objList.add(obj);
//			}
//		}

		return objList;
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
	 *            <li>-1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int setValue(String deviceId, ParameValueOBJ obj) {
		logger.debug("setValue({},{}): ", deviceId, obj);

		int flag = -1;

		if (deviceId == null || obj == null) {
			logger.debug("deviceId == null");

			return flag;
		}

		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		objList.add(obj);

		return setValue(deviceId, objList);
	}

	

	/**
	 * 调用ACS的RPC方法
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return DevRPCRep[]
	 */
	public List<DevRpcCmdOBJ> execRPC(String deviceId, String[] stringArr) {
		logger.debug("execRPC({},{}): ", deviceId, stringArr);
		if (null == deviceId || null == stringArr) {
			logger.warn("null == deviceId");
			return null;
		}
//		DevRPC[] devRPCArr = getDevRpcArray(deviceId, stringArr);
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[2];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = stringArr[0];
		
		rpcArr[1] = new Rpc();
		rpcArr[1].rpcId = "2";
		rpcArr[1].rpcName = "GetParameterValues";
		rpcArr[1].rpcValue = stringArr[2];
		
		devRpcArr[0].rpcArr = rpcArr;
		return execRPC(devRpcArr);
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

		if (null == devRpcArr) {
			logger.warn("pnull == devRPCArr");
			return null;
		}
		List<DevRpcCmdOBJ> devRPCRep = null;
		
			AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(Global.ACS_OBJECT_NAME);
//			devRPCRep = acsCorbaDAO.execRPC(StbServGlobals.getLipossProperty("mq.clientId"), Global.rpcTestType, Global.priority, devRpcArr);
			devRPCRep = acsCorbaDAO.execRPC(Global.CLIENT_ID, Global.rpcTestType, Global.priority, devRpcArr);
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
	public List<DevRpcCmdOBJ> execRPC(DevRpc[] devRpcArr) {
		logger.debug("execRPC({}):", devRpcArr);

		if (null == devRpcArr) {
			logger.warn("pnull == devRPCArr");
			return null;
		}
//		DevRPCRep[] devRPCRep = null;
		List<DevRpcCmdOBJ> devRPCRep = null;
		
//		try {
			AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(Global.ACS_OBJECT_NAME);
			//devRPCRep = acsCorbaDAO.execRPC(StbServGlobals.getLipossProperty("mq.clientId"), Global.rpcType, Global.priority, devRpcArr);
			devRPCRep = acsCorbaDAO.execRPC(Global.CLIENT_ID, Global.rpcType, Global.priority, devRpcArr);
//			devRPCRep = Global.G_ACSManager.ExecRPC(devRPCArr);
			return devRPCRep;
//		} catch (Exception e) {
//			logger.warn("CORBA ACS Error:{},Rebind.", e.getMessage());
//			try {
//				InitDAO.initACS();
//				devRPCRep = Global.G_ACSManager.ExecRPC(devRPCArr);
//				return devRPCRep;
//			} catch (Exception e2) {
//				logger.warn("CORBA ACS Error:{},Rebind.", e2.getMessage());
//				return devRPCRep;
//			}
//		}
	}
	
	
	/**
	 * connect test
	 * 
	 * @param deviceId
	 * @param arr
	 * @return
	 */
	public int getDeviceStatus(String deviceId)
	{
		int flag = 0;
		logger.debug("getValue({}): ", deviceId);
		if (deviceId == null)
		{
			logger.debug("deviceId == null");
			return flag;
		}
		
		DevRpc[] devRPCArr = getDevRPCArr(deviceId,null);
		
		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(Global.CLIENT_ID, 
				Global.rpcType_TEST_CONNNECT, Global.ACS_PRIORITY, devRPCArr);
		
		if (null == list || list.isEmpty())
		{
			return flag;
		}
		
		flag = list.get(0).getStat();
		
		if(1!=flag){
			flag = 0;
		}
		return flag;
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
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		if (rpcObject == null)
		{
			rpc.rpcName = "";
			rpc.rpcValue = "";
		}
		else
		{
			rpc.rpcName = rpcObject.getClass().getSimpleName();
			rpc.rpcValue = rpcObject.toRPC();
		}
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		return devRPCArr;
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
//	private DevRPC[] getDevRpcArray(String deviceId, String[] stringArr) {
//		logger.debug("getDevRpcArray({},{}):", deviceId, stringArr);
//
//		DevRPC[] devRPCArr = null;
//
//		if (null != deviceId) {
//			String sqlDevInfo = "select loopback_ip, cr_port"
//					+ ",cr_path, acs_username, acs_passwd, device_id"
//					+ ",oui,device_serialnumber from tab_gw_device"
//					+ " where device_id=?";
//			PrepareSQL psql = new PrepareSQL(sqlDevInfo);
//			psql.setString(1, deviceId);
//			Map devMap = DBOperation.getRecord(psql.getSQL());
//			if (null != devMap && false == devMap.isEmpty()) {
//				devRPCArr = new DevRPC[1];
//				devRPCArr[0] = new DevRPC();
//				devRPCArr[0].DeviceId = String.valueOf(devMap.get("device_id"));
//				devRPCArr[0].OUI = String.valueOf(devMap.get("oui"));
//				devRPCArr[0].SerialNumber = String.valueOf(devMap
//						.get("device_serialnumber"));
//				devRPCArr[0].ip = String.valueOf(devMap.get("loopback_ip"));
//				devRPCArr[0].port = Integer.valueOf(devMap.get("cr_port")
//						.toString());
//				devRPCArr[0].path = String.valueOf(devMap.get("cr_path"));
//				devRPCArr[0].username = String.valueOf(devMap
//						.get("acs_username"));
//				devRPCArr[0].passwd = String.valueOf(devMap.get("acs_passwd"));
//
//				devRPCArr[0].rpcArr = stringArr;
//			}
//		}
//		return devRPCArr;
//	}

	
	/**
	 * 调用ACS执行工单
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-7-10
	 * @return Sheet[]
	 */
//	public Sheet[] execRpc(Sheet[] sheet) {
//		logger.debug("execRpc({})", sheet);
//		Sheet[] retSheet = null;
//		if (null == sheet) {
//			logger.warn("execRpc(sheet) is null");
//		} else {
//			try{
//				retSheet = Global.G_ACSManager.DoRPC(sheet);
//			}catch(Exception e){
//				logger.warn("CORBA ACS Error:{},Rebind.", e.getMessage());
//				InitDAO.initACS();
//				retSheet = Global.G_ACSManager.DoRPC(sheet);
//			}
//		}
//		return retSheet;
//	}
	
}
