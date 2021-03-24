
package com.linkage.itms.dispatch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.ConfigObj;
import com.linkage.itms.dispatch.obj.ConfigRtn;
import com.linkage.itms.dispatch.obj.Node;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 山西 RMS 参数配置接口
 * 
 * @author HP (AILK No.)
 * @version 1.0
 * @since 2021-1-5
 * @category com.linkage.itms.dispatch.service
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class CpeConfigService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(CpeConfigService.class);
	private long id = RecordLogDAO.getRandomId();
	private String ouiSn = "";
	private String deviceId = "";
	private String serviceObject = "";
	private String servListPathI = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
	// 入参
	ConfigObj inObj = new ConfigObj();
	// 回参
	ConfigRtn rtn = new ConfigRtn();
	// 存放<业务类型, >
	Map<String, String> serviceMap = new HashMap<String, String>();
	Map<String, String> typeMap = new HashMap<String, String>();
	// 失败code
	Map<String, String> faultMap = new HashMap<String, String>();
	// rpc下发完成后的返回对象，包含错误节点
	private com.ailk.tr069.devrpc.obj.mq.Rpc rpcObj = new com.ailk.tr069.devrpc.obj.mq.Rpc();
	private static final String ERROR_CLIENT_MSG = "'Invalid arguments Client','参数不对'";

	public static void main(String[] args)
	{
		String responseStr = "<SOAP-ENV:Fault>        <faultcode>Client</faultcode>        <faultstring>CWMP fault</faultstring>          <detail>            <cwmp:Fault>              <FaultCode>9003</FaultCode>              <FaultString>Invalid arguments</FaultString>              <SetParameterValuesFault>                <ParameterName>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.29.WANPPPConnection.1.X_CU_IPMod</ParameterName>                <FaultCode>9005</FaultCode>                <FaultString>Invalid Parameter Name</FaultString>              </SetParameterValuesFault>            </cwmp:Fault>          </detail>      </SOAP-ENV:Fault>";
		com.ailk.tr069.devrpc.obj.mq.Rpc rpcObj = new com.ailk.tr069.devrpc.obj.mq.Rpc();
		if (null != rpcObj && null != responseStr
				&& responseStr.contains("<SetParameterValuesFault>"))
		{
			int begin = responseStr.indexOf("<SetParameterValuesFault>")
					+ "<SetParameterValuesFault>".length();
			int end = responseStr.indexOf("</SetParameterValuesFault>");
			responseStr = responseStr.substring(begin, end);
		}
		//System.out.println();
	}

	@Override
	public String work(String inParam)
	{
		logger.warn("CpeConfigService==>inParam({})", inParam);
		new RecordLogDAO().recordLog(id, inParam, "cpeConfig");
		try
		{
			inObj = JSONObject.parseObject(inParam, ConfigObj.class);
		}
		catch (Exception e)
		{
			rtn.setRstCode("1");
			rtn.setRstDesc("service_obj入参非Json格式");
			return JSON.toJSONString(rtn);
		}
		check();
		// 校验失败
		if (!"0".equals(rtn.getRstCode()))
		{
			logger.warn("入参验证没通过,CpeConfigService==>inParam({})", inParam);
			return JSON.toJSONString(rtn);
		}
		logger.warn("{}入参验证通过", inObj.getOui_sn());
		ouiSn = inObj.getOui_sn();
		serviceObject = inObj.getService_object();
		ArrayList<HashMap<String, String>> infolist = new ArrayList<HashMap<String, String>>();
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		infolist = userDevDao.qryIdbyOuiSN(ouiSn, serviceObject);
		if (null != infolist && infolist.size() == 1)
		{
			deviceId = infolist.get(0).get("device_id");
		}
		else
		{
			rtn.setRstCode("2");
			rtn.setRstDesc("查询不到设备/多个设备");
			return JSON.toJSONString(rtn);
		}
		ACSCorba acsCorba = new ACSCorba();
		if ("HGU".equals(inObj.getService_object()))
		{
			acsCorba = new ACSCorba("1");
		}
		else
		{
			acsCorba = new ACSCorba("4");
		}
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		logger.warn("[{}]在线状态=" + flag, deviceId);
		if (flag == 1)
		{
			// 光猫才需要预读
			if ("HGU".equals(inObj.getService_object()))
			{
				preRead(acsCorba);
			}
			doSetValue(acsCorba);
		}
		else
		{
			rtn.setRstCode("3");
			rtn.setRstDesc("设备不在线");
		}
		/*
		 * logger.warn(
		 * "servicename[CpeConfigService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}", new
		 * Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		 */
		return JSON.toJSONString(rtn);
	}

	/**
	 * 校验入参
	 */
	public void check()
	{
		serviceMap.put("TR069", "");
		serviceMap.put("INTERNET", "");
		serviceMap.put("IPTV", "");
		serviceMap.put("VOIP", "");
		serviceMap.put("VOICE", "");
		serviceMap.put("OTHER", "");
		serviceMap.put("NONE", "");
		serviceMap.put("STB", "");
		typeMap.put("string", "1");
		typeMap.put("int", "2");
		typeMap.put("unsignedint", "3");
		typeMap.put("boolean", "4");
		typeMap.put("datetime", "5");
		typeMap.put("signedint", "6");
		typeMap.put("strin", "7");
		faultMap.put("9003", ERROR_CLIENT_MSG);
		faultMap.put("9005", "'Invalid parameter name','节点不对'");
		faultMap.put("9006", "'Invalid parameter type','节点类型不对'");
		faultMap.put("9007", ERROR_CLIENT_MSG);
		faultMap.put("9008", ERROR_CLIENT_MSG);
		String ouiSn = inObj.getOui_sn();
		String serviceObj = inObj.getService_object();
		if (StringUtil.IsEmpty(ouiSn) || !ouiSn.contains("-") || ouiSn.length() != 19)
		{
			rtn.setRstDesc("oui_sn入参非法");
		}
		else if (StringUtil.IsEmpty(serviceObj))
		{
			rtn.setRstDesc("service_obj入参非法");
		}
		/* HGU：光猫，STB：机顶盒, 全部大写 */
		else if (!"HGU".equals(serviceObj) && !"STB".equals(serviceObj))
		{
			rtn.setRstDesc("service_obj入参非法");
		}
		else if (inObj.getService_name().size() == 0)
		{
			rtn.setRstDesc("service_name入参非法");
		}
		else if (inObj.getService_parameters().size() == 0)
		{
			rtn.setRstDesc("service_parameters入参非法");
		}
		for (ArrayList<Node> paramOne : inObj.getService_parameters())
		{
			if (paramOne.size() == 0)
			{
				rtn.setRstDesc("service_parameters入参非法");
				break;
			}
			else
			{
				for (Node node : paramOne)
				{
					if (StringUtil.IsEmpty(node.getPath())
							|| StringUtil.IsEmpty(node.getType()))
					{
						rtn.setRstDesc("service_parameters入参非法(path/type为空)");
						return;
					}
					if (!typeMap.containsKey(node.getType().toLowerCase()))
					{
						rtn.setRstDesc("service_parameters入参非法(type非法)");
						return;
					}
				}
			}
		}
		for (String paramOne : inObj.getService_name())
		{
			if (!serviceMap.containsKey(paramOne))
			{
				rtn.setRstDesc("service_name入参非法");
				break;
			}
		}
		if (inObj.getService_parameters().size() != inObj.getService_name().size())
		{
			rtn.setRstDesc("service_name与service_parameters数量不符");
		}
		if (StringUtil.IsEmpty(rtn.getRstDesc()))
		{
			rtn.setRstCode("0");
		}
	}

	/**
	 * 预读ijk
	 * 
	 * @param corba
	 */
	private void preRead(ACSCorba corba)
	{
		// InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CU_ServiceList
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		ArrayList<String> wanConnPathsList = corba.getParamNamesPath(deviceId,
				wanConnPath, 0);
		if (null == wanConnPathsList || wanConnPathsList.isEmpty())
		{
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取", deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.isEmpty())
			{
				logger.warn("[CpeConfigService] [{}]获取" + wanConnPath + "下实例号失败，返回",
						deviceId);
			}
			else
			{
				for (String j : jList)
				{
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanPPPConnection);
					if (null == kPPPList || kPPPList.isEmpty())
					{
						logger.warn("[CpeConfigService] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
						kPPPList = corba.getIList(deviceId, wanConnPath + j
								+ wanIPConnection);
						if (null == kPPPList || kPPPList.isEmpty())
						{
							logger.warn("[CpeConfigService] [{}]获取" + wanConnPath
									+ wanConnPath + j + wanIPConnection + "下实例号失败",
									deviceId);
						}
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
									+ wanServiceList);
						}
					}
					else
					{
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection
									+ kppp + wanServiceList);
						}
					}
				}
			}
		}
		if (null != wanConnPathsList && !wanConnPathsList.isEmpty())
		{
			List<String> tempWanConnPathsList = new ArrayList<String>();
			for (String wanConnPaths : wanConnPathsList)
			{
				if (wanConnPaths.endsWith(wanServiceList))
				{
					tempWanConnPathsList.add(wanConnPaths);
				}
			}
			String[] paramNameArr = new String[tempWanConnPathsList.size()];
			for (int index = 0; index < tempWanConnPathsList.size(); index++)
			{
				paramNameArr[index] = tempWanConnPathsList.get(index);
			}
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
					paramNameArr);
			if (null == paramValueMap || paramValueMap.isEmpty())
			{
				logger.warn("[CpeConfigService] [{}]获取ServiceList失败", deviceId);
			}
			else
			{
				for (Map.Entry<String, String> entry : paramValueMap.entrySet())
				{
					logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
							entry.getValue() });
					String paramName = entry.getKey();
					if (paramName.endsWith(wanServiceList))
					{
						// 多业务,只记录第一次
						if (serviceMap.containsKey(entry.getValue())
								&& !StringUtil.IsEmpty(entry.getValue())
								&& StringUtil.IsEmpty(serviceMap.get(entry.getValue())))
						{
							String res = entry.getKey().substring(0,
									entry.getKey().indexOf(wanServiceList));
							serviceMap.put(entry.getValue(), res);
						}
					}
				}
			}
		}
		logger.warn("[{}]wan预读结果", deviceId);
		for (Map.Entry<String, String> entry : serviceMap.entrySet())
		{
			logger.warn("[{}]<" + entry.getKey() + "," + entry.getValue() + ">", deviceId);
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		for (int i = 0; i < inObj.getService_name().size(); i++)
		{
			String servName = inObj.getService_name().get(i);
			ArrayList<Node> params = inObj.getService_parameters().get(i);
			String valueTmp = "";
			if ("VOIP".equals(servName) || "VOICE".equals(servName))
			{
				valueTmp = StringUtil.getStringValue(serviceMap, "VOICE", "");
				if (StringUtil.IsEmpty(valueTmp))
				{
					valueTmp = StringUtil.getStringValue(serviceMap, "VOIP", "");
				}
			}
			else if ("IPTV".equals(servName) || "OTHER".equals(servName))
			{
				valueTmp = StringUtil.getStringValue(serviceMap, "IPTV", "");
				if (StringUtil.IsEmpty(valueTmp))
				{
					valueTmp = StringUtil.getStringValue(serviceMap, "OTHER", "");
				}
			}
			else
			{
				valueTmp = StringUtil.getStringValue(serviceMap, servName, "");
			}
			if (!StringUtil.IsEmpty(valueTmp))
			{
				for (int j = 0; j < params.size(); j++)
				{
					Node node = params.get(j);
					String param = node.getPath();
					/*
					 * String paramLast = param.substring(param.lastIndexOf(".", 1000));
					 * params.set(j, StringUtil.getStringValue(serviceMap, servName ,"") +
					 * paramLast);
					 */
					if (param.contains(".VoiceProfile."))
					{
						logger.warn("VoiceProfile");
						if (param.contains(".Line."))
						{
							String line = "1";
							ArrayList<HashMap<String, String>> infolist = userDevDao
									.qryLindidByDeviceID(deviceId);
							if (null != infolist && infolist.size() == 1)
							{
								line = infolist.get(0).get("line_id");
							}
							param = param.replace(".Line.-1.", ".Line." + line + ".");
						}
						param = param.replace(".-1.", ".1.");
					}
					else
					{
						int lens = servListPathI.length();
						// String value = StringUtil.getStringValue(serviceMap, servName,
						// "");
						// String j_ = value.substring(lens, value.indexOf(".", lens));
						String jN = valueTmp.substring(lens, valueTmp.indexOf(".", lens));
						param = param.replace("WANConnectionDevice.-1.",
								"WANConnectionDevice." + jN + ".");
						param = param.replace(".-1.", ".1.");
					}
					node.setPath(param);
					params.set(j, node);
				}
			}
			else
			{
				// 不是none，当前光猫预读时又不存在该业务，参数列表设置为空，不配置
				if (!"NONE".equals(servName)){
					for (int j = 0; j < params.size(); j++){
						params.set(j, null);
					}
				}
			}
		}
		logger.warn("[{}]预读替换完成", deviceId);
		/*
		 * for(int i=0;i<inObj.getService_name().size();i++){ String servName =
		 * inObj.getService_name().get(i); ArrayList<Node> params =
		 * inObj.getService_parameters().get(i); logger.warn("[{}]",deviceId);
		 * if(!StringUtil.IsEmpty(StringUtil.getStringValue(serviceMap, servName ,""))){
		 * for(int j=0;j<params.size();j++){ String param = params.get(j).getPath();
		 * logger.warn("[{}]:{}",deviceId,param); } } }
		 */
	}

	/**
	 * 配置参数
	 * 
	 * @param corba
	 */
	private void doSetValue(ACSCorba corba)
	{
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		for (int i = 0; i < inObj.getService_name().size(); i++)
		{
			ArrayList<Node> params = inObj.getService_parameters().get(i);
			for (int j = 0; j < params.size(); j++)
			{
				// 没有这个业务的，又需要预读的情况，会是null，直接跳过，不下发
				if (null == params.get(j))
				{
					continue;
				}
				String param = params.get(j).getPath().replace(".-1.", ".1.");
				if (!StringUtil.IsEmpty(param))
				{
					ParameValueOBJ obj = new ParameValueOBJ();
					obj.setName(param);
					obj.setValue(params.get(j).getValue());
					obj.setType(typeMap.get(params.get(j).getType().toLowerCase()));
					logger.warn("[{}]配置参数:{}", deviceId, param);
					objList.add(obj);
				}
			}
		}
		int retResult = corba.setValue(deviceId, objList, rpcObj);
		if (retResult == 0 || retResult == 1)
		{
			logger.warn("[{}]成功", deviceId);
			rtn.setRstCode("0");
			rtn.setRstDesc("成功");
		}
		else
		{
			String responseStr = rpcObj.getValue();
			if (null != rpcObj && null != responseStr
					&& responseStr.contains("<SetParameterValuesFault>"))
			{
				int begin = responseStr.indexOf("<SetParameterValuesFault>")
						+ "<SetParameterValuesFault>".length();
				int end = responseStr.indexOf("</SetParameterValuesFault>");
				responseStr = responseStr.substring(begin, end);
			}
			String code = "";
			if (null != rpcObj && null != responseStr
					&& responseStr.contains("<FaultCode>"))
			{
				int left = responseStr.indexOf("<FaultCode>") + "<FaultCode>".length();
				int right = responseStr.indexOf("<", left);
				code = responseStr.substring(left, right);
			}
			String faultStr = "";
			if (null != rpcObj && null != responseStr
					&& responseStr.contains("<FaultString>"))
			{
				int left = responseStr.indexOf("<FaultString>")
						+ "<FaultString>".length();
				int right = responseStr.indexOf("<", left);
				faultStr = responseStr.substring(left, right);
			}
			String fault = "";
			if (null != rpcObj && null != responseStr
					&& responseStr.contains("<ParameterName>"))
			{
				int left = responseStr.indexOf("<ParameterName>")
						+ "<ParameterName>".length();
				int right = responseStr.indexOf("<", left);
				fault = responseStr.substring(left, right);
			}
			rtn.setRstCode(code);
			rtn.setRstDesc(faultStr);
			rtn.setFailedNode(fault);
			logger.warn(
					"[{}]返回:错误码：{}, 描述{}, 失败节点{}",
					new Object[] { deviceId, rtn.getRstCode(), rtn.getRstDesc(),
							rtn.getFailedNode() });
		}
	}
}
