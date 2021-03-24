package com.linkage.itms.dispatch.sxdx.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.StartPingDealXML;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.service.SetParameterValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 甘肃电信对终端的TR069节点下发配置值
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月21日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class StartPingService extends ServiceFather {
	public StartPingService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(StartPingService.class);
	private ACSCorba corba = new ACSCorba();
	private StartPingDealXML dealXML;
	
	public int work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new StartPingDealXML(methodName);
		
		int chekres = dealXML.checkXML(inXml);

		String inftype = dealXML.getInftype();
		// 验证入参
		if (1 != chekres) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return chekres;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> userDevList = dao.queryUserDevByUser(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}", userDevList.toString());

		if(userDevList.size() >= 1){
			int typeNum = 0;
			String serv_type_id = "";
			if("1".equals(inftype)){
				serv_type_id = "10";
			}else if("2".equals(inftype)){
				serv_type_id = "11";
			}else if("3".equals(inftype)){
				serv_type_id = "14";
			}
			for (int i = 0; i < userDevList.size(); i++) {
				if(serv_type_id.equals(userDevList.get(i).get("serv_type_id"))){
					typeNum++;
				}
			}
			if(typeNum<=0){
				logger.warn(methodName+"["+dealXML.getOpId()+"],所查询的通道不存在");
				return 0;
			}

		}else if(null == userDevList || userDevList.size()==0 || StringUtil.IsEmpty(StringUtil.getStringValue(userDevList.get(0), "device_id"))){
			logger.warn(methodName+"["+dealXML.getOpId()+"],未查询到终端");
			return 0;
		}

		String deviceId = StringUtil.getStringValue(userDevList.get(0), "device_id");


		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，返回-1");
			return -1;
		}
		
		logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线，准备ping测试。");
		int retResult = PingList("1", deviceId);
		if (retResult == 0 || retResult == 1)
		{
			logger.warn(methodName+"["+dealXML.getOpId()+"],Ping测试成功");
			return 1;
		}
		else{
			logger.warn(methodName+"["+dealXML.getOpId()+"],Ping测试失败");
			return -2;
		}
	}

	/**
	 * 返回Ping测试诊断结果
	 * 
	 * @param gw_type 1：家庭网关 2：政企网关
	 * @param device_id  设备ID
	 * 
	 * @param gw_type
	 * @return
	 */
	public int PingList(String gw_type, String device_id) {
		String waninterface = "";
		
		waninterface = gatherWanPath(device_id);
		if(StringUtil.IsEmpty(waninterface)){
			logger.warn(methodName+"["+dealXML.getOpId()+"],获取Wan连接失败");
			return -2;
		}
		
		DevRpc[] devRPCArr = new DevRpc[1];
		
		AnyObject anyObject = new AnyObject();
		SetParameterValues setParameterValues = new SetParameterValues();
		
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[7];
		
		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0].setName("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState");
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);
		
		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1].setName("InternetGatewayDevice.IPPingDiagnostics.Interface");
		anyObject = new AnyObject();
		anyObject.para_value = waninterface;
				
		anyObject.para_type_id = "1";
		ParameterValueStruct[1].setValue(anyObject);
		
		ParameterValueStruct[2] = new ParameterValueStruct();
		ParameterValueStruct[2].setName("InternetGatewayDevice.IPPingDiagnostics.Host");
		anyObject = new AnyObject();
		anyObject.para_value = dealXML.getIp();
		anyObject.para_type_id = "1";
		ParameterValueStruct[2].setValue(anyObject);
		
		ParameterValueStruct[3] = new ParameterValueStruct();
		ParameterValueStruct[3].setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
		anyObject = new AnyObject();
		anyObject.para_value = StringUtil.getStringValue(dealXML.getNum());
		anyObject.para_type_id = "3";
		ParameterValueStruct[3].setValue(anyObject);
		
		ParameterValueStruct[4] = new ParameterValueStruct();
		ParameterValueStruct[4].setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
		anyObject = new AnyObject();
		anyObject.para_value = StringUtil.getStringValue(dealXML.getOvertime());
		anyObject.para_type_id = "3";
		ParameterValueStruct[4].setValue(anyObject);
		
		ParameterValueStruct[5] = new ParameterValueStruct();
		ParameterValueStruct[5].setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
		anyObject = new AnyObject();
		anyObject.para_value = StringUtil.getStringValue(dealXML.getSize());
		anyObject.para_type_id = "3";
		ParameterValueStruct[5].setValue(anyObject);
		
		ParameterValueStruct[6] = new ParameterValueStruct();
		ParameterValueStruct[6].setName("InternetGatewayDevice.IPPingDiagnostics.DSCP");
		anyObject = new AnyObject();
		anyObject.para_value = "0";
		anyObject.para_type_id = "3";
		ParameterValueStruct[6].setValue(anyObject);
		
		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("Ping");
		
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = device_id;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		
		List<DevRpcCmdOBJ> devRPCRep = null;
		DevRPCManager devRPCManager = new DevRPCManager(gw_type);
		devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);
		
		String errMessage = "";
		Map PingMap = null;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", device_id);
			errMessage = "设备未知错误";
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备未知错误");
			return -2;
			
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", device_id);
			errMessage = "设备未知错误";
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备未知错误");
			return -2;
		}
		else
		{
			int stat = devRPCRep.get(0).getStat();
			logger.warn("设备 stat：[{}] ", stat);
			if (stat != 1)
			{
				errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
				logger.warn(methodName+"["+dealXML.getOpId()+"],设备未知错误,"+errMessage);
				return -2;
			}
			return 1;
		}
	}
	
	/**
	 * 获取相应InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.¬{i}.值
	 * @param deviceId 设备id
	 * @return String
	 */
	private String gatherWanPath(String deviceId) {
		ACSCorba corba = new ACSCorba();
		//logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String INTERNET = "INTERNET";
		if ("0".equals(dealXML.getInftype())){
			INTERNET = "TR069";
		}
		else if ("1".equals(dealXML.getInftype())){
			INTERNET = "INTERNET";
		}
		else if ("2".equals(dealXML.getInftype())){
			INTERNET = "IPTV";
		}
		else if ("3".equals(dealXML.getInftype())){
			INTERNET = "VOIP";
		}
		
		ArrayList<String> wanConnPathsList = null;
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath + "下实例号失败，返回",
						deviceId);
				return "";
			}
			for (String j : jList)
			{
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
						+ wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
				{
					logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
					kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanIPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[PingDiagnostic] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
					}else{
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
									+ wanServiceList);
						}
					}
				}
				else
				{
					for (String kppp : kPPPList)
					{
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanServiceList);
					}
				}
			}
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
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
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty())
		{
			logger.warn("[PingDiagnostic] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
			
		}else{
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList)
			{
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++)
			{
				String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20
						: paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++)
				{
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty())
				{
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty())
			{
				logger.warn("[PingDiagnostic] [{}]获取ServiceList失败", deviceId);
				return "";
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet())
			{
				logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
						entry.getValue() });
				String paramName = entry.getKey();
				/*if (paramName.indexOf(wanPPPConnection) >= 0)
				{
				}
				else if (paramName.indexOf(wanIPConnection) >= 0)
				{
					continue;
				}*/
				if (paramName.indexOf(wanServiceList) >= 0)
				{
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(INTERNET) >= 0){
						String res=entry.getKey().substring(0, entry.getKey().indexOf("X_CT-COM_ServiceList"));
						logger.warn(res);
						return res;
					}
				}
			}
				
		}
		

		
		return "";
	}
	
	
	public String percent(long p1, long p2) {
		double p3;
		if (p2 == 0) {
			return "N/A";
		} else {
			p3 = (double) p1 / p2;
		}
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		String str = nf.format(p3);
		return str;
	}
	
}
