package com.linkage.itms.dispatch.sxdx;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.dispatch.sxdx.obj.WanConnObj;
import com.linkage.itms.dispatch.sxdx.obj.WanConnSessObj;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.ParameterInfoStruct;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.*;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class PvcVlanIdTool {
	private static Logger logger = LoggerFactory.getLogger(PvcVlanIdTool.class);
	private String deviceId = "";
	private int isDSL = 1;
	private String vlanidOrMark = "VLANID";
	private String wanId = "";
	private String Vlanid = "";
	
	public boolean isInteger(String str){
		return str.matches("[0-9]+");   
	}
	
	
	public String getServiceListAndVlanId(){
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanPathH = "InternetGatewayDevice.WANDevice.";
		String wanPathM = ".WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanConnectionType = ".ConnectionType";
		String wanAddressingType = ".AddressingType";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		// String INTERNET = "INTERNET";
		String wanPathPvcT = ".WANDSLLinkConfig.DestinationAddress";
		String wanPathVlanT = ".WANEthernetLinkConfig.X_CT-COM_VLANIDMark";
		String wanPathEponT1 = ".X_CT-COM_WANEponLinkConfig.VLANIDMark";
		String wanPathEponT2 = ".X_CT-COM_WANEponLinkConfig.VLANID";
		String wanPathGponT1 = ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
		String wanPathGponT2 = ".X_CT-COM_WANGponLinkConfig.VLANID";
		String wanPathEponLinkConfig = ".X_CT-COM_WANEponLinkConfig.";
		String wanPathGponLinkConfig = ".X_CT-COM_WANGponLinkConfig.";
		logger.warn("{}开始预读PVC或VLANID：{}", deviceId,Vlanid);
		ArrayList<String> wanConnPathsList = null;
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = getParamNamesPath(deviceId, wanConnPath, 0);
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			logger.warn("[{}]获取WANConnectionDevice下所有节点路径失败，逐层获取", deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = getIList(wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("[{}]获取" + wanConnPath + "下实例号失败，返回",
						deviceId);
				return null;
			}
			if (jList.size() > 10)
			{
				logger.warn(" [{}]获取" + wanConnPath
						+ "下实例号,实例数大于10", deviceId);
				return null;
			}
			for (String j : jList)
			{
				if (!this.isInteger(j))
				{
					logger.warn("[{}]获取" + wanConnPath
							+ "下实例号,实例数不是整数[{}]", deviceId, j);
					return null;
				}
				if (isDSL == 1)
				{
					wanConnPathsList.add(wanConnPath + j + wanPathPvcT);
				}
				else if (isDSL == 2)
				{
					wanConnPathsList.add(wanConnPath + j + wanPathVlanT);
				}
				else if (isDSL == 3)
				{
					List<String> childNodeList = getChildNodeList(wanConnPath + j
							+ wanPathEponLinkConfig);
					if (null == childNodeList || childNodeList.size() == 0
							|| childNodeList.isEmpty())
					{
						logger.warn("[{}]获取" + wanConnPath + j
								+ wanPathEponLinkConfig + "下节点路径失败，返回", deviceId);
						return null;
					}
					else
					{
						wanConnPathsList.addAll(childNodeList);
					}
				}
				else if (isDSL == 4)
				{
					List<String> childNodeList = getChildNodeList(wanConnPath + j
							+ wanPathGponLinkConfig);
					if (null == childNodeList || childNodeList.size() == 0
							|| childNodeList.isEmpty())
					{
						logger.warn("[{}]获取" + wanConnPath + j
								+ wanPathGponLinkConfig + "下节点路径失败，返回", deviceId);
						return null;
					}
					else
					{
						wanConnPathsList.addAll(childNodeList);
					}
				}
				else
				{
					logger.warn("[{}]无法确定上行方式，返回", deviceId);
					return null;
				}
				// 获取session，
				List<String> kPPPList = getIList(wanConnPath + j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
				{
					logger.warn(" [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
					List<String> kIPList = getIList(wanConnPath + j + wanIPConnection);
					if (null == kIPList || kIPList.size() == 0 || kIPList.isEmpty())
					{
						logger.warn(" [{}]获取" + wanConnPath
								+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
					}
					else
					{
						for (String kip : kIPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kip
									+ wanServiceList);
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kip
									+ wanConnectionType);
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kip
									+ wanAddressingType);
						}
					}
				}
				else
				{
					for (String kppp : kPPPList)
					{
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanServiceList);
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanConnectionType);
					}
				}
			}
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// pvc和vlanid节点
		ArrayList<String> pvcVlanList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++)
		{
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0)
			{
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
			if (namepath.indexOf(wanConnectionType) >= 0)
			{
				paramNameList.add(namepath);
				continue;
			}
			if (namepath.indexOf(wanAddressingType) >= 0)
			{
				paramNameList.add(namepath);
				continue;
			}
			if (isDSL == 1)
			{
				if (namepath.indexOf(wanPathPvcT) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			else if (isDSL == 2)
			{
				if (namepath.indexOf(wanPathVlanT) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			else if (isDSL == 3)
			{
				if (namepath.indexOf(wanPathEponT1) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			else if (isDSL == 4)
			{
				if (namepath.indexOf(wanPathGponT1) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			else
			{
				return null;
			}
		}
		if (isDSL == 3 && (pvcVlanList.size() == 0 || pvcVlanList.isEmpty()))
		{
			for (int j = 0; j < wanConnPathsList.size(); j++)
			{
				String namepath = wanConnPathsList.get(j);
				if (namepath.indexOf(wanPathEponT2) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
		}
		if (isDSL == 4 && (pvcVlanList.size() == 0 || pvcVlanList.isEmpty()))
		{
			for (int j = 0; j < wanConnPathsList.size(); j++)
			{
				String namepath = wanConnPathsList.get(j);
				if (namepath.indexOf(wanPathGponT2) >= 0)
				{
					pvcVlanList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty())
		{
			return null;
		}
		if (pvcVlanList.size() == 0 || pvcVlanList.isEmpty())
		{
			return null;
		}
		String[] paramNameArr = new String[paramNameList.size()];
		int arri = 0;
		for (String paramName : paramNameList)
		{
			paramNameArr[arri] = paramName;
			arri = arri + 1;
		}
		Map<String, String> paramValueMap = new HashMap<String, String>();
		int rpcNum = paramNameArr.length%20==0 ? paramNameArr.length/20:(paramNameArr.length/20)+1;
		for(int k = 0;k<rpcNum;k++)
		{// 分批获取获取节点值，默认一次20个节点
			String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20: paramNameArr.length - (k * 20)];
			for (int m = 0; m < paramNametemp.length; m++)
			{
				paramNametemp[m] = paramNameArr[k * 20 + m];
			}
			Map<String, String> maptemp = getParaValueMap(paramNametemp);
			if (maptemp != null && !maptemp.isEmpty())
			{
				paramValueMap.putAll(maptemp);
			}
		}
		if (paramValueMap.isEmpty())
		{
			logger.warn(" [{}]获取PVC或者VLANID节点失败", deviceId);
			return null;
		}
		HashMap<String, WanConnObj> wanConnMap = new HashMap<String, WanConnObj>();
		for (Map.Entry<String, String> entry : paramValueMap.entrySet())
		{// 将获取的参数值进行过滤和组装，放入集合中，其数据结构为wanConnMap<j,WanConnObj.WanConnSessMap<k#ipType,WanConnSessMap>>
			logger.warn("采集结果：[{}]{}={} ",new Object[] { deviceId, entry.getKey(), entry.getValue() });
			String paramName = entry.getKey();
			String j = paramName.substring(wanConnPath.length(),
					paramName.indexOf(".", wanConnPath.length()));
			WanConnObj tempWanConnObj = wanConnMap.get(j);
			if (null == tempWanConnObj)
			{
				tempWanConnObj = new WanConnObj();
				tempWanConnObj.setWanId(wanId);
				tempWanConnObj.setWanConnId(j);
				tempWanConnObj.setWanConnSessMap();
				wanConnMap.put(j, tempWanConnObj);
			}

			String ipType = "";
			if (paramName.indexOf(wanPPPConnection) >= 0)
			{
				ipType = "PPP";
			}
			else if (paramName.indexOf(wanIPConnection) >= 0)
			{
				ipType = "IP";
			}
			if (paramName.indexOf(wanServiceList) >= 0)
			{
				String k = paramName.substring(paramName.indexOf(wanServiceList) - 1,
						paramName.indexOf(wanServiceList));
				WanConnSessObj tempWanConnSessObj = tempWanConnObj.getWanConnSessMap()
						.get(k + "#" + ipType);
				if (null == tempWanConnSessObj)
				{
					tempWanConnSessObj = new WanConnSessObj();
					tempWanConnSessObj.setWanId(wanId);
					tempWanConnSessObj.setWanConnId(j);
					tempWanConnSessObj.setWanConnSessId(k);
					tempWanConnSessObj.setIpType(ipType);
					tempWanConnObj.getWanConnSessMap().put(k + "#" + ipType,
							tempWanConnSessObj);
				}
				tempWanConnSessObj.setServicelist(entry.getValue());
				logger.warn("entryValue={}",entry.getValue());
				if("INTERNET".equals(entry.getValue())){
					logger.warn("捕捉到INTERNET节点，返回[]",j+"#"+k + "#" + ipType);
					return j+"#"+k + "#" + ipType;
				}
				continue;
			}
			if (paramName.indexOf(wanConnectionType) >= 0)
			{
				String k = paramName.substring(paramName.indexOf(wanConnectionType) - 1,
						paramName.indexOf(wanConnectionType));
				WanConnSessObj tempWanConnSessObj = tempWanConnObj.getWanConnSessMap()
						.get(k + "#" + ipType);
				if (null == tempWanConnSessObj)
				{
					tempWanConnSessObj = new WanConnSessObj();
					tempWanConnSessObj.setWanId(wanId);
					tempWanConnSessObj.setWanConnId(j);
					tempWanConnSessObj.setWanConnSessId(k);
					tempWanConnSessObj.setIpType(ipType);
					tempWanConnObj.getWanConnSessMap().put(k + "#" + ipType,
							tempWanConnSessObj);
				}
				tempWanConnSessObj.setConnectionType(entry.getValue());

				continue;
			}
			if (paramName.indexOf(wanAddressingType) >= 0)
			{
				String k = paramName.substring(paramName.indexOf(wanAddressingType) - 1,
						paramName.indexOf(wanAddressingType));
				WanConnSessObj tempWanConnSessObj = tempWanConnObj.getWanConnSessMap()
						.get(k + "#" + ipType);
				if (null == tempWanConnSessObj)
				{
					tempWanConnSessObj = new WanConnSessObj();
					tempWanConnSessObj.setWanId(wanId);
					tempWanConnSessObj.setWanConnId(j);
					tempWanConnSessObj.setWanConnSessId(k);
					tempWanConnSessObj.setIpType(ipType);
					tempWanConnObj.getWanConnSessMap().put(k + "#" + ipType,
							tempWanConnSessObj);
				}
				tempWanConnSessObj.setAddressingType(entry.getValue());
				continue;
			}
			if (isDSL == 1)
			{
				if (paramName.indexOf(wanPathPvcT) >= 0)
				{
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
			}
			else if (isDSL == 2)
			{
				if (paramName.indexOf(wanPathVlanT) >= 0)
				{
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
			}
			else if (isDSL == 3)
			{
				if (paramName.indexOf(wanPathEponT1) >= 0)
				{
					vlanidOrMark = "VLANIDMark";
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
				else if (paramName.indexOf(wanPathEponT2) >= 0
						&& !"VLANIDMark".equals(vlanidOrMark))
				{
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
			}
			else if (isDSL == 4)
			{
				if (paramName.indexOf(wanPathGponT1) >= 0)
				{
					vlanidOrMark = "VLANIDMark";
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
				else if (paramName.indexOf(wanPathGponT2) >= 0
						&& !"VLANIDMark".equals(vlanidOrMark))
				{
					tempWanConnObj.setVlanid(entry.getValue());
					continue;
				}
			}
			else
			{
				logger.warn("[{}]无法确定上行方式，返回", deviceId);
				return null;
			}
		}
		// 遍历获取业务wan连接
		for (Map.Entry<String, WanConnObj> entryConn : wanConnMap.entrySet())
		{
			// 从所有wan连接和session信息中将需要业务下发的过滤出来，放入servWanConnList中
			WanConnObj tempWanConnObj111 = entryConn.getValue();
			logger.warn("tempWanConnObj111 is " + tempWanConnObj111.toString());
			String jkey = entryConn.getKey();
			HashMap<String, WanConnSessObj> tempwanConnSessMap = tempWanConnObj111.getWanConnSessMap();

			if(Vlanid.equals(tempWanConnObj111.getVlanid())){
				for (Map.Entry<String, WanConnSessObj> entryConnSess : tempwanConnSessMap.entrySet())
				{
					String kkey = entryConnSess.getKey();
					return jkey+"#"+kkey;
				}
			}
		}
		return null;
	}
	/**
	 * 获取参数实例值的Map(tr069)
	 * 
	 * @param para_name
	 * @return Map paramMap
	 */
	private Map<String, String> getParaValueMap(String[] para_name)
	{
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.clear();
		GetParameterValues getParameterValues = new GetParameterValues();
		getParameterValues.setParameterNames(para_name);
		DevRpc[] devRPCArr = getDevRPCArr(getParameterValues);

		try
		{
			// String devRPCRep = getDevRPCResponse(devRPCArr);
			List<DevRpcCmdOBJ> list = invokeAcsCorba(devRPCArr);
			// 一个设备返回的命令
			if (list == null)
			{
				return paramMap;
			}
			// String setRes = devRPCRep[0].rpcArr[0];
			String setRes = list.get(0).getRpcList().get(0).getValue(); // TODO 处理调用后
																		// 响应的消息 已修改 待测试
			logger.warn("[{}]getParaValueMap的Stat={}", deviceId,
					list.get(0).getStat());
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
						logger.warn("[{}]getParaValueMap-Name:{}, Value={}",new Object[] { deviceId, name, value });
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
			// e.printStackTrace();
		}
		return paramMap;
	}

	
	/**
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @param cr_path
	 * @return
	 */
	private List<String> getChildNodeList(String cr_path)
	{
		List<String> nodeList = new ArrayList<String>();
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(cr_path);
		getParameterNames.setNextLevel(1);
		DevRpc[] devRPCArr = getDevRPCArr(getParameterNames);
		// DevRPCRep[] devRPCRep = getDevRPCResponse(devRPCArr);
		List<DevRpcCmdOBJ> list = invokeAcsCorba(devRPCArr);
		if (null == list || list.isEmpty())
		{
			return nodeList;
		}
		// 一个设备返回的命令
		// String setRes = devRPCRep[0].rpcArr[0];
		String setRes = list.get(0).getRpcList().get(0).getValue();
		logger.warn("[{}]getChildNodeList的Stat={}", deviceId, list
				.get(0).getStat());
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
				for (int i = 0; i < pisArr.length; i++)
				{
					String name = pisArr[i].getName();
					nodeList.add(name);
				}
			}
			// 清空pisArr
			pisArr = null;
		}
		// 清空对象
		getParameterNames = null;
		devRPCArr = null;
		getParameterNamesResponse = null;
		return nodeList;
	}
	
	
	/**
	 * 常用：根据指定的路径获得下面的i
	 * 
	 * @param paramPath
	 * @return
	 */
	private List<String> getIList(String paramPath)
	{
		logger.warn("[{}]获取节点{}", deviceId, paramPath);
		Map<String, String> connMap = getParaTreeMap(paramPath);
		if (null == connMap || 0 == connMap.size())
		{
			logger.warn("[{}]获取节点{}失败", deviceId, paramPath);
			try
			{
				logger.warn("[{}]等待3s再次获取。[{}]", deviceId,
						paramPath);
				Thread.sleep(3000);
				connMap = getParaTreeMap(paramPath);
				if (null == connMap || 0 == connMap.size())
				{
					logger.warn(" [{}]再次获取节点{}失败。", deviceId,
							paramPath);
					return null;
				}
			}
			catch (Exception e)
			{
				logger.error("[{}]获取节点{}失败。异常：" + e.getMessage(),
						deviceId, paramPath);
				return null;
			}
		}
		logger.warn(" [{}]getIListMap({})=[{}]", new Object[] {
				deviceId, paramPath, connMap });
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
				try
				{
					jList.add(value.substring(paramPath.length(), value.lastIndexOf(".")));
				}
				catch (StringIndexOutOfBoundsException e)
				{
					logger.error("[{}]getIListMap字符串越界[{}]",
							deviceId, value);
				}
			}
		}
		// 清空connMap
		connMap = null;
		return jList;
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
	private ArrayList<String> getParamNamesPath(String deviceId, String path,
			int nextLevel)
	{
		ArrayList<String> pislist = null;
		ParameterInfoStruct[] pisArr = null;
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(path);
		getParameterNames.setNextLevel(nextLevel);
		DevRpc[] realTimeObj = getDevRPCArr(getParameterNames);
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
		if (pisArr != null && pisArr.length > 0)
		{
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
	 * 获取节点下子节点名称和读写属性
	 * 
	 * @param cr_path
	 * @return
	 */
	private Map<String, String> getParaTreeMap(String cr_path)
	{
		logger.debug(" [{}] call  acscorba, path:[{}]",
				this.deviceId, cr_path);
		Map<String, String> paramMap = new HashMap<String, String>();
		GetParameterNames getParameterNames = new GetParameterNames();
		getParameterNames.setParameterPath(cr_path);
		getParameterNames.setNextLevel(1);
		DevRpc[] devRPCArr = getDevRPCArr(getParameterNames);
		// String devRPCRep = getDevRPCResponse(devRPCArr);
		List<DevRpcCmdOBJ> list = invokeAcsCorba(devRPCArr);
		if (null == list || list.isEmpty())
		{
			return paramMap;
		}
		// 一个设备返回的命令
		// String setRes = devRPCRep[0].rpcArr[0];
		String setRes = list.get(0).getRpcList().get(0).getValue();
		logger.warn("[{}] call  acscorba response rpcvalue:"
				+ setRes, this.deviceId);
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
	 * 根据device_id得到长度为1的DevRPC对象数组
	 * 
	 * @param device_id
	 *            设备id
	 * @param rpcObject
	 *            ----GetParameterValues/GetParameterNames/
	 * @return
	 */
	private DevRpc[] getDevRPCArr(RPCObject rpcObject)
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
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		return devRPCArr;
	}
	
	
	/**
	 * @param devRPCArr
	 * @return
	 */
	private List<DevRpcCmdOBJ> invokeAcsCorba(DevRpc[] devRPCArr)
	{
		List<DevRpcCmdOBJ> list = new ACSCorba().execRPC(Global.ClIENT_ID, 1, 1,
				devRPCArr);
		if (list == null || list.isEmpty())
		{
			return null;
		}
		if (list.get(0).getRpcList() == null || list.get(0).getRpcList().isEmpty())
		{
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


	public String getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}


	public int getIsDSL() {
		return isDSL;
	}


	public void setIsDSL(int isDSL) {
		this.isDSL = isDSL;
	}


	public String getVlanidOrMark() {
		return vlanidOrMark;
	}


	public void setVlanidOrMark(String vlanidOrMark) {
		this.vlanidOrMark = vlanidOrMark;
	}


	public String getWanId() {
		return wanId;
	}


	public void setWanId(String wanId) {
		this.wanId = wanId;
	}


	public String getVlanid() {
		return Vlanid;
	}


	public void setVlanid(String vlanid) {
		Vlanid = vlanid;
	}

}
