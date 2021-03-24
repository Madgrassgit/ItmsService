
package com.linkage.itms.dispatch.util;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterInfoStruct;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.*;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 调用ACS
 * 
 * @author 王森博
 */
public class AcsCorba
{

	static final Logger logger = LoggerFactory.getLogger(AcsCorba.class);
	private static final String SVR_OBJECT_NAME = "ACS";

	public List<DevRpcCmdOBJ> execRPC(String clientId, int i, int j, DevRpc[] devRPCArr)
	{
		logger.debug("execRPC()");
		List<DevRpcCmdOBJ> list = null;
		try
		{
			logger.warn("[{}]调用acs开始,systemKey={},devRPCArr={},devRPCArr.size={}",
					new Object[]{devRPCArr[0].devId,Global.getPrefixName(Global.SYSTEM_NAME)+SVR_OBJECT_NAME,devRPCArr,devRPCArr.length});
			/*AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(Global.SYSTEM_NAME+SVR_OBJECT_NAME);*/
			AcsCorbaDAO acsCorbaDAO = new AcsCorbaDAO(Global.getPrefixName(Global.SYSTEM_NAME)+SVR_OBJECT_NAME);
			list = acsCorbaDAO.execRPC(
					Global.ClIENT_ID, i, j, devRPCArr);
			logger.warn("[{}]调用acs结束，list.size={}",devRPCArr[0].devId,list.size());
		}
		catch (Exception e)
		{
			logger.error("[{}]调用ac发生异常",devRPCArr[0].devId);
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 自助绑定设置成功
	 *
	 * @author wangsenbo
	 * @date Jul 24, 2011
	 * @param 
	 * @return void
	 */
	public void selfBindSuccessCUC(String deviceId)
	{
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[1];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_UserInfo.Result");
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "1";
		anyObject.para_type_id = "3";
		ParameterValueStruct[0].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("bind");
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		execRPC(Global.ClIENT_ID, 1, 1, devRpcArr);
	}
	
	/**
	 * SetParameterValues
	 *
	 * @author liyl10
	 * @date Apr 10, 2018
	 * 
	 * @param deviceId
	 * @param setPath 设置路径
	 * @param setValue 设置值
	 * @param setType 设置类型  1.string;2.int;3.unsignedInt;4.boolean
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-7:系统参数错误</li>
	 *            <li>-6:设备正被操作</li>
	 *            <li> -1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 *            <li>其它:TR069错误</li>
	 */
	public int setValue(String deviceId, String setPath, String setValue, String setType)
	{
		int flag = -9;
		if (deviceId == null || setPath == null || setType == null)
		{
			logger.debug("deviceId == null");
			return flag;
		}
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[1];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName(setPath);
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = setValue;
		anyObject.para_type_id = setType;
		ParameterValueStruct[0].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
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
		List<DevRpcCmdOBJ> respArr = execRPC(Global.ClIENT_ID, 1, 1, devRpcArr);
		if (respArr == null || respArr.get(0) == null)
		{
			logger.debug("flag = -9");
			flag = -9;
			return flag;
		}
		flag = respArr.get(0).getStat();
		if(flag == 0){
			flag = 1;
		}
		logger.debug("flag = " + flag);
		return flag;
	}
	
	/**
	 * 自助绑定设置成功
	 *
	 * @author wangsenbo
	 * @date Jul 24, 2011
	 * @param 
	 * @return void
	 */
	public void selfBindSuccess(String deviceId)
	{
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[1];
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.X_CT-COM_UserInfo.Result");
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "1";
		anyObject.para_type_id = "3";
		ParameterValueStruct[0].setValue(anyObject);
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("bind");
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		execRPC(Global.ClIENT_ID, 1, 1, devRpcArr);
	}
	
	/**
	 * 自助绑定设置成功
	 *
	 * @author wangsenbo
	 * @date Jul 24, 2011
	 * @param 
	 * @return void
	 */
	public void selfBindSuccess_HB(String deviceId)
	{
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[2];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.X_CT-COM_UserInfo.Status");
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "0";
		anyObject.para_type_id = "3";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.X_CT-COM_UserInfo.Result");
		anyObject = new AnyObject();
		anyObject.para_value = "1";
		anyObject.para_type_id = "3";
		ParameterValueStruct[1].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("bind");
		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRpcArr[0].rpcArr = rpcArr;
		execRPC(Global.ClIENT_ID, 1, 1, devRpcArr);
	}
	
	/**
	 * 常用：根据参数名获取值
	 * 
	 * @param paramName
	 * @return
	 */
	public String getParamValueString(String deviceId,String paramName)
	{
		String[] paramNames = new String[] { paramName };
		Map<String, String> paraValues = getParaValueMap(deviceId,paramNames);
		if (null == paraValues || 0 == paraValues.size())
		{
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
	public Map<String, String> getParaValueMap(String deviceId,String[] para_name)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(para_name);
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
	 * 根据device_id得到长度1的DevRPC对象数组
	 * 
	 * @param device_id
	 *            设备id
	 * @param rpcObject
	 *            ----GetParameterValues/GetParameterNames/SetParameterValues
	 * @return
	 */
	public DevRpc[] getDevRPCArr(String deviceId,RPCObject[] rpcObject)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[rpcObject.length];
		for (int i = 0; i < rpcObject.length; i++)
		{
			Rpc rpc = new Rpc();
			rpc.rpcId = StringUtil.getStringValue(i + 1);
			if (rpcObject[i] == null)
			{
				rpc.rpcName = "";
				rpc.rpcValue = "";
			}
			else
			{
				rpc.rpcName = rpcObject[i].getClass().getSimpleName();
				rpc.rpcValue = rpcObject[i].toRPC();
			}
			rpcArr[i] = rpc;
		}
		devRPCArr[0].rpcArr = rpcArr;
		return devRPCArr;
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
	 * 常用：根据指定的路径获得下面的i
	 * 
	 * @param paramPath
	 * @return
	 */
	public List<String> getIList(String deviceId,String paramPath)
	{
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
				logger.error("[{}]获取节点{}失败。异常：" + e.getMessage(), deviceId, paramPath);
				return null;
			}
		}
		logger.warn("[{}]getIListMap[{}]=[{}]", new Object[]{deviceId, paramPath, connMap});
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
				jList.add(value.substring(paramPath.length(), value.lastIndexOf(".")));
			}
		}
		// 清空connMap
		connMap = null;
		return jList;
	}
	
	/**
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @param cr_path
	 * @return
	 */
	private Map<String, String> getParaTreeMap(String deviceId,String cr_path)
	{
		logger.debug("[{}] call  acscorba, path:[{}]", deviceId, cr_path);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(cr_path);
		getParameterNames.setNextLevel(1);
		DevRpc[] devRPCArr = getDevRPCArr(deviceId,getParameterNames);
		
		List<DevRpcCmdOBJ> list = invokeAcsCorba(deviceId,devRPCArr);
		if (null == list || list.isEmpty())
		{
			return paramMap;
		}
		// 一个设备返回的命令
		
		String setRes = list.get(0).getRpcList().get(0).getValue();
		
		logger.debug("[{}] call  acscorba response rpcvalue:" +setRes, deviceId);
		
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
		devRPCArr = null;
		getParameterNamesResponse = null;
		return paramMap;
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
	public int delPara(String deviceId,String paraV)
	{
		int flag = -9;
		DeleteObject delObject = new DeleteObject();
		delObject.setObjectName(paraV);
		delObject.setParameterKey("");
		DevRpc[] devRPCArr = getDevRPCArr(deviceId,delObject);
		try
		{
			List<DevRpcCmdOBJ> list = invokeAcsCorba(deviceId,devRPCArr);
			if (list == null || list.isEmpty())
			{
				return -9;
			}
			flag = list.get(0).getStat();
			logger.warn("[{}]delPara:flag = " + flag,deviceId);
			
//			String setRes = list.get(0).getRpcList().get(0).getValue(); 
			
//			DeleteObjectResponse delObjectResponse = new DeleteObjectResponse();
//			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(setRes));
//			if (soapOBJ == null)
//			{
//				return 0;
//			}
//			delObjectResponse = XmlToRpc.DeleteObjectResponse(soapOBJ.getRpcElement());
//			if (null != delObjectResponse)
//			{
//				flag = 1;
//				logger.warn("[{}]delObjectResponse={}", deviceId,
//						delObjectResponse.getStatus());
//			}
//			// 清空对象
//			delObject = null;
//			devRPCArr = null;
//			delObjectResponse = null;
		}
		catch (Exception e)
		{
			flag = -9;
			logger.error("{}", e.getStackTrace());
		}
		return flag;
	}
	
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
		List<DevRpcCmdOBJ> devRpcCmdOBJList = invokeAcsCorba(realTimeObj);
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
	 * @param devRPCArr
	 * @return
	 */
	private List<DevRpcCmdOBJ> invokeAcsCorba(DevRpc[] devRPCArr)
	{
		List<DevRpcCmdOBJ> list = new AcsCorba().execRPC(Global.ClIENT_ID, 1, 1, devRPCArr);
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

	public SetParameterValues getSetParameterValues(String[] paramNames,
			String[] paramValues, String[] paramTypeIds)
	{
		if (paramNames.length == paramValues.length
				&& paramValues.length == paramTypeIds.length)
		{
			SetParameterValues setParameterValues = new SetParameterValues();
			ParameterValueStruct[] parameterListArr = new ParameterValueStruct[paramNames.length];
			for (int i = 0; i < paramNames.length; i++)
			{
				parameterListArr[i] = getParameterValueStruct(paramNames[i],
						paramValues[i], paramTypeIds[i]);
			}
			setParameterValues.setParameterList(parameterListArr);
			setParameterValues.setParameterKey("ITMS+");
			return setParameterValues;
		}
		return null;
	}
	
	public AddObject getAddObject(String paramName)
	{
		AddObject addObject = new AddObject();
		addObject.setObjectName(paramName);
		addObject.setParameterKey("ITMS+");
		return addObject;
	}

	private ParameterValueStruct getParameterValueStruct(String paramName,
			String paramValue, String paramTypeId)
	{
		ParameterValueStruct parameter = new ParameterValueStruct();
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = paramValue;
		anyObject.para_type_id = paramTypeId;
		parameter = new ParameterValueStruct();
		parameter.setName(paramName);
		parameter.setValue(anyObject);
		return parameter;
	}
	
	/**
	 * 增加参数实例，成功返回实例结点值(对应的i) ；失败 返回0
	 * 
	 * @param paraName
	 * @param ior
	 * @param device_id
	 * @param gather_id
	 * @return instanceNum
	 */
	public int addPara(String paraName, String device_id)
	{
		logger.debug("paraName={}", paraName);
		int instanceNum = 0;
		AddObject addObject = new AddObject();
		addObject.setObjectName(paraName);
		addObject.setParameterKey("");
		DevRpc[] devRPCArr = this.getDevRPCArr(device_id, addObject);
		//得到设备类型
		List<DevRpcCmdOBJ> devRPCRep = invokeAcsCorba(device_id,devRPCArr);
		// 一个设备返回的命令
		if (devRPCRep == null || devRPCRep.size() == 0 || devRPCRep.get(0) == null)
		{
			return instanceNum;
		}
		// 一个设备返回的命令
		if (devRPCRep.get(0).getStat() == 1)
		{
			Element element = dealDevRPCResponse("AddObjectResponse", devRPCRep,
					device_id);
			if (element == null)
			{
				return instanceNum;
			}
			AddObjectResponse addObjectResponse = XmlToRpc.AddObjectResponse(element);
			if (null != addObjectResponse)
			{
				instanceNum = addObjectResponse.getInstanceNumber();
			}
		}
		return instanceNum;
	}
}
