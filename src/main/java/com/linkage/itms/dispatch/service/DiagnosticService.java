/**
 * DiagnosticService.java
 * 业务诊断信息查询接口
 */
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DevIADDiagCAO;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DiagnosticInfoOBJ;
import com.linkage.itms.dispatch.obj.DiagnosticServiceChecker;

/**
 * 业务诊断信息查询接口
 * 
 * @author chenjie
 * @date 2011-12-5
 */
public class DiagnosticService implements IService{
	
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(DiagnosticService.class);
	
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
//	private ServiceHandle serviceHandle = new ServiceHandle();
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();

	public String work(String inXml)
	{	
		DiagnosticServiceChecker checker = new DiagnosticServiceChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[DiagnosticService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[DiagnosticService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[DiagnosticService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
			return checker.getReturnXml();
		}
		else
		{
//			String username = userInfoMap.get("username");
//			String userCityId = userInfoMap.get("city_id");
			String deviceId = userInfoMap.get("device_id");
//			String deviceSn = userInfoMap.get("device_serialnumber");
//			String userline = userInfoMap.get("userline");

			/*
			if (false == serviceHandle.cityMatch(checker.getCityId(),userCityId))
			{
				// 属地不匹配
				logger.warn("属地不匹配 查无此用户：" + checker.getUserInfo());
				checker.setResult(1007); //
				checker.setResultDesc("查无此用户");
				return checker.getReturnXml();
			}
			*/ 
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn(
						"servicename[DiagnosticService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1003);
				checker.setResultDesc("未绑定设备");
				return checker.getReturnXml();
			}
			
			DiagnosticInfoOBJ obj = new DiagnosticInfoOBJ();
			obj.setDeviceId(deviceId);
			obj.setUserId(userInfoMap.get("user_id"));
			
			// 进行采集，获取WANDevice信息
			logger.warn(
					"servicename[DiagnosticService]cmdId[{}]userinfo[{}]开始采集[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),deviceId});
			int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);  // 0表示采集所有节点 在原来基础上增加了一个参数(3)
			//int rsint = 1;
			logger.warn(
					"servicename[DiagnosticService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),rsint});
			
			// 采集失败
			if (rsint != 1)
			{
				logger.warn(
						"servicename[DiagnosticService]cmdId[{}]userinfo[{}]getData sg fail",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004); //xxx规范中定义下
				checker.setResultDesc("设备采集失败");
				return checker.getReturnXml();
			}
			// 采集成功获取需要的信息
			else
			{
				int accessType = getAccessType(deviceId);
				obj.setAccessType(String.valueOf(accessType));
				
				Map<String, List<HashMap<String,String>>> allServMap = getAllChannel(deviceId);
				
				/**
				 * 安徽电信目前只有VOIP语音业务，没有IPTV和宽带上网业务 
				 * modify by zhangchy 2013-04-07
				 */
				if ("ah_dx".equals(Global.G_instArea)) {
				}else {
					// 处理INTERNET信息
					dealInternetInfo(allServMap.get("INTERNET"),obj);
					// 处理IPTV信息
					dealIptvInfo(allServMap.get("IPTV"), obj);
				}
				
				// 处理VOIP信息
				dealVoipInfo(allServMap.get("VOIP"), obj);
			
				// voip line 信息
				obj.setVoipLineList(getVoipLineInfo(deviceId));
				
				/**
				 * 安徽电信不需要展示PON信息节点
				 * modify by zhangchy 2013-04-07
				 */
				if ("ah_dx".equals(Global.G_instArea)) {
				} else {
					// 处理PON口信息
					dealPonInfo(deviceId, obj);
				}
				
				
				// 处理VOIP IAD诊断信息
				/**
				 * 应严伟明要求，新疆IAD诊断的时候总是报错，为了不影响使用，
				 * 将此IAD诊断注释
				 */
				if(!"xj_dx".equals(Global.G_instArea)){
					logger.warn(
							"servicename[DiagnosticService]cmdId[{}]userinfo[{}]开始查询IAD诊断信息,调用ACS.",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					dealIADDiagnostic(deviceId, obj);
				}
			}
			checker.setObj(obj);
		}
		
		String returnXml = checker.getReturnXml();
		// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"DiagnosticService");
		logger.warn(
				"servicename[DiagnosticService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		return returnXml;
	}
	
	/**
	 * IAD诊断
	 * @param deviceId
	 * @param obj
	 */
	private void dealIADDiagnostic(String deviceId, DiagnosticInfoOBJ obj)
	{
		logger.debug("dealIADDiagnostic({},{})", deviceId, obj);
		// VOIP.IADDiagnostics.IADDiagnosticsState ??是查哪个字段？
		obj.setIADDiagnosticState("Complete");
		
		// 主用服务器
		obj.setIADDiagnosticsTestServer("1");
		DevIADDiagCAO iadCao = new DevIADDiagCAO(deviceId, 1);
		// 诊断成功
		if (iadCao.diagIAD())
		{
			// 成功
			if(iadCao.getResult().equals("0"))
			{
				logger.warn("[{}]IAD诊断成功", deviceId);
				obj.setIADDiagnosticsRegistResult("0");
				obj.setIADDiagnosticsReason("");
			}
			else
			{
				obj.setIADDiagnosticsRegistResult("1");
				obj.setIADDiagnosticsReason(iadCao.getReasonCode());
				logger.warn("[{}]诊断失败: {}", deviceId, iadCao.getReasonDesc());
			}
		}
		// 诊断失败
		else
		{
			obj.setIADDiagnosticsRegistResult("1");
			obj.setIADDiagnosticsReason("-1"); //诊断错误
			logger.warn("[{}]诊断失败: {}", deviceId, "诊断发生错误");
		}
	}

	/**
	 * 处理PON口信息
	 * @param deviceId
	 * @param obj
	 * 
	 * 以下由"注释by zhangchy 2012-03-06" 注释的代码是根据需求单JSDX_ITMS-REQ-20120220-LUHJ-005修改的
	 * 要求根据ItmsService接口文档中的要求，注释的字段信息不需要返回给综调 所以将不需要返回的字段注释
	 * 
	 */
	private void dealPonInfo(String deviceId, DiagnosticInfoOBJ obj)
	{
		logger.debug("dealPonInfo({},{})", deviceId, obj);
		Map<String,String> map = deviceConfigDao.getPonInfo(deviceId);
		if(map == null || map.isEmpty())
		{
			obj.setPonStatus(StringUtil.getStringValue(""));
			obj.setTxPower(StringUtil.getStringValue(""));
			obj.setRxPower(StringUtil.getStringValue(""));
			obj.setTransceiverTemperature("");
			obj.setSupplyVottage("");
			obj.setBiasCurrent("");
/**
 * 注释 by zhangchy 2012-03-06
 */
			//新疆要用   zhangsm  20120405
			if("xj_dx".equals(Global.G_instArea))
			{
				obj.setSentBytes("");
				obj.setReceivedBytes("");
				obj.setSentPackets("");
				obj.setReceivedPackets("");
				obj.setSUnicastPackets("");
				obj.setRUnicastPackets("");
				obj.setSMulticastPackets("");
				obj.setRMulticastPackets("");
				obj.setSBroadcastPackets("");
				obj.setRBroadcastPackets("");
				obj.setFecError("");
				obj.setHecError("");
				obj.setDropPackets("");
				obj.setSPausePackets("");
				obj.setRPausePackets("");
			}
			return;
		}
		obj.setPonStatus(StringUtil.getStringValue(map.get("status")));
		obj.setTxPower(StringUtil.getStringValue(map.get("tx_power")));
		obj.setRxPower(StringUtil.getStringValue(map.get("rx_power")));
		obj.setTransceiverTemperature(StringUtil.getStringValue(map.get("transceiver_temperature")));
		obj.setSupplyVottage(StringUtil.getStringValue(map.get("supply_vottage")));
		obj.setBiasCurrent(StringUtil.getStringValue(map.get("bias_current")));
/**
 * 注释 by zhangchy 2012-03-06
 */
		if("xj_dx".equals(Global.G_instArea))
		{
			obj.setSentBytes(StringUtil.getStringValue(map.get("bytes_sent")));
			obj.setReceivedBytes(StringUtil.getStringValue(map.get("bytes_received")));
			obj.setSentPackets(StringUtil.getStringValue(map.get("packets_sent")));
			obj.setReceivedPackets(StringUtil.getStringValue(map.get("packets_received")));
			obj.setSUnicastPackets(StringUtil.getStringValue(map.get("sunicast_packets")));
			obj.setRUnicastPackets(StringUtil.getStringValue(map.get("runicast_packets")));
			obj.setSMulticastPackets(StringUtil.getStringValue(map.get("smulticast_packets")));
			obj.setRMulticastPackets(StringUtil.getStringValue(map.get("rmulticast_packets")));
			obj.setSBroadcastPackets(StringUtil.getStringValue(map.get("sbroadcast_packets")));
			obj.setRBroadcastPackets(StringUtil.getStringValue(map.get("rbroadcast_packets")));
			obj.setFecError(StringUtil.getStringValue(map.get("fec_error")));
			obj.setHecError(StringUtil.getStringValue(map.get("hec_error")));
			obj.setDropPackets(StringUtil.getStringValue(map.get("drop_packets")));
			obj.setSPausePackets(StringUtil.getStringValue(map.get("spause_packets")));
			obj.setRPausePackets(StringUtil.getStringValue(map.get("rpause_packets")));
		}
	}
	
	/**
	 * 处理INTERNET信息
	 * @param list
	 * @param obj
	 */
	private void dealInternetInfo(List<HashMap<String, String>> list, DiagnosticInfoOBJ obj) {
		logger.debug("dealInternetInfo({},{})", list, obj);
		if(list == null || list.isEmpty())
		{
			obj.setInternetConnectType(getConnectType(""));
//			需求单JSDX_ITMS-REQ-20120220-LUHJ-005 要求根据ItmsService接口文档中的要求，某些字段信息不需要返回给综调 add by zhangchy 2012-03-06
			obj.setInternetIPAddress(StringUtil.getStringValue(""));
			obj.setInternetDNSServer(StringUtil.getStringValue(""));
			//新疆要用   zhangsm  20120405
			if("xj_dx".equals(Global.G_instArea))
			{
				obj.setInternetConnectStatus(getConnectStatus(""));
				obj.setInternetPVC(StringUtil.getStringValue(""));
				obj.setInternetVLAN(StringUtil.getStringValue(""));
				obj.setInternetUsername(StringUtil.getStringValue(""));
				obj.setInternetErrorCode(StringUtil.getStringValue(""));
				obj.setInternetMAC(StringUtil.getStringValue(""));
				obj.setInternetBindport(StringUtil.getStringValue(""));
			}
			return;
		}
		// 只取一条数据
		Map map = list.get(0);
		obj.setInternetConnectType(getConnectType(StringUtil.getStringValue(map.get("conn_type"))));
//		需求单JSDX_ITMS-REQ-20120220-LUHJ-005 要求根据ItmsService接口文档中的要求，某些字段信息不需要返回给综调 add by zhangchy 2012-03-06
		obj.setInternetIPAddress(StringUtil.getStringValue(map.get("ip")));
		obj.setInternetDNSServer(StringUtil.getStringValue(map.get("dns")));
		//新疆要用   zhangsm  20120405
		if("xj_dx".equals(Global.G_instArea))
		{
			List<HashMap<String,String>> netList = new DeviceConfigDAO().getInternet(StringUtil.getStringValue(map.get("device_id")));
			if(netList != null && !netList.isEmpty())
			{
				obj.setInternetPVC("PVC:"+StringUtil.getStringValue(netList.get(0).get("vpi_id"))+"/"+StringUtil.getStringValue(map.get("vci_id")));
				obj.setInternetVLAN(StringUtil.getStringValue(netList.get(0).get("vlan_id")));
			}
			else
			{
				obj.setInternetPVC(StringUtil.getStringValue(""));
				obj.setInternetVLAN(StringUtil.getStringValue(""));
			}
			obj.setInternetConnectStatus(getConnectStatus(StringUtil.getStringValue(map.get("conn_status"))));
			obj.setInternetUsername(StringUtil.getStringValue(map.get("username")));
			obj.setInternetErrorCode(StringUtil.getStringValue(map.get("last_conn_error")));
			obj.setInternetMAC(StringUtil.getStringValue(map.get("cpe_mac")));
			obj.setInternetBindport(StringUtil.getStringValue(map.get("bind_port")));
		}
	}

	/**
	 * 处理IPTV信息
	 * @param list
	 * @param obj
	 */
	private void dealIptvInfo(List<HashMap<String, String>> list, DiagnosticInfoOBJ obj) {
		logger.debug("dealIptvInfo({},{})", list, obj);
		if(list==null || list.isEmpty())
		{
			obj.setIptvConnectType("");
			obj.setIptvConnectStatus("");
			//新疆要用   zhangsm  20120405
			if("xj_dx".equals(Global.G_instArea))
			{
				obj.setIptvPVC(StringUtil.getStringValue(""));
				obj.setIptvVLAN(StringUtil.getStringValue(""));
				obj.setIptvBindport(StringUtil.getStringValue(""));
			}
			return;
		}
		// 只取一条数据
		Map map = list.get(0);
		obj.setIptvConnectType(getConnectType(StringUtil.getStringValue(map.get("conn_type"))));
		obj.setIptvConnectStatus(getConnectStatus(StringUtil.getStringValue(map.get("conn_status"))));
		//新疆要用   zhangsm  20120405
		if("xj_dx".equals(Global.G_instArea))
		{
			List<HashMap<String,String>> iptvList = new DeviceConfigDAO().getIPTV(StringUtil.getStringValue(map.get("device_id")));
			if(iptvList != null && !iptvList.isEmpty())
			{
				obj.setIptvPVC("PVC:"+StringUtil.getStringValue(iptvList.get(0).get("vpi_id"))+"/"+StringUtil.getStringValue(iptvList.get(0).get("vci_id")));
				obj.setIptvVLAN(StringUtil.getStringValue(iptvList.get(0).get("vlan_id")));
			}
			else
			{
				obj.setIptvPVC(StringUtil.getStringValue(""));
				obj.setIptvVLAN(StringUtil.getStringValue(""));	
			}
			obj.setIptvBindport(StringUtil.getStringValue(map.get("bind_port")));
		}
	}
	
	/**
	 * 处理VOIP信息
	 * @param list
	 * @param obj
	 */
	private void dealVoipInfo(List<HashMap<String, String>> list, DiagnosticInfoOBJ obj) {
		logger.debug("dealVoipInfo({},{})", list, obj);
		if(list==null || list.isEmpty())
		{
			obj.setVoipConnectType("");
			obj.setVoipConnectStatus("");
			obj.setVoipIPAddress("");
			obj.setVoipDNSServer("");
			
			// VOIP.registStatus 是哪个字段??
			obj.setVoipRegistStatus("");
			obj.setVoipSubnetMask("");
			obj.setDefaultGateWay("");
			
			//新疆要用   zhangsm  20120405
			if("xj_dx".equals(Global.G_instArea))
			{
				obj.setVoipPVC(StringUtil.getStringValue(""));
				obj.setVoipVLAN(StringUtil.getStringValue(""));
				obj.setVoipRegistErrorType(StringUtil.getStringValue(""));
				obj.setVoipProtocolType(StringUtil.getStringValue(""));
				obj.setVoipRegistrarServer(StringUtil.getStringValue(""));
				obj.setVoipRegistrarServerPort(StringUtil.getStringValue(""));
				obj.setVoipStandByRegistrarServer(StringUtil.getStringValue(""));
				obj.setVoipStandByRegistrarServerPort(StringUtil.getStringValue(""));
			}
			return;
		}
		Map map = list.get(0);
		obj.setVoipConnectType(getConnectType(StringUtil.getStringValue(map.get("conn_type"))));
		obj.setVoipConnectStatus(getConnectStatus(StringUtil.getStringValue(map.get("conn_status"))));
		obj.setVoipIPAddress(StringUtil.getStringValue(map.get("ip")));
		obj.setVoipDNSServer(StringUtil.getStringValue(map.get("dns")));
		
		// VOIP.registStatus 是哪个字段??
		obj.setVoipRegistStatus("1");
		obj.setVoipSubnetMask(StringUtil.getStringValue(map.get("mask")));
		obj.setDefaultGateWay(StringUtil.getStringValue(map.get("gateway")));
		
		//新疆要用   zhangsm  20120405
		if("xj_dx".equals(Global.G_instArea))
		{
			DeviceConfigDAO configDAO = new DeviceConfigDAO();
			List<HashMap<String,String>> voipList = configDAO.getIPTV(StringUtil.getStringValue(map.get("device_id")));
			if(voipList != null && !voipList.isEmpty())
			{
				obj.setVoipPVC("PVC:"+StringUtil.getStringValue(voipList.get(0).get("vpi_id"))+"/"+StringUtil.getStringValue(voipList.get(0).get("vci_id")));
				obj.setVoipVLAN(StringUtil.getStringValue(voipList.get(0).get("vlan_id")));
			}
			else
			{
				obj.setVoipPVC(StringUtil.getStringValue(""));
				obj.setVoipVLAN(StringUtil.getStringValue(""));	
			}
			obj.setVoipRegistErrorType("成功");
			List<HashMap<String,String>> voipParamList = configDAO.getUserVOIP(obj.getUserId()); 
			if(voipParamList != null && !voipParamList.isEmpty())
			{
				obj.setVoipProtocolType(voipParamList.get(0).get("protocol"));
				obj.setVoipRegistrarServer(voipParamList.get(0).get("regi_serv"));
				obj.setVoipRegistrarServerPort(voipParamList.get(0).get("regi_port"));
				obj.setVoipStandByRegistrarServer(voipParamList.get(0).get("stand_regi_serv"));
				obj.setVoipStandByRegistrarServerPort(voipParamList.get(0).get("stand_regi_port"));
			}
			else
			{
				obj.setVoipProtocolType(StringUtil.getStringValue(""));
				obj.setVoipRegistrarServer(StringUtil.getStringValue(""));
				obj.setVoipRegistrarServerPort(StringUtil.getStringValue(""));
				obj.setVoipStandByRegistrarServer(StringUtil.getStringValue(""));
				obj.setVoipStandByRegistrarServerPort(StringUtil.getStringValue(""));
			}
		}
	}


	/**
	 * 获取所有的session信息,根据serv_list分类
	 * @param device_id
	 * @return
	 */
	private Map<String, List<HashMap<String,String>>> getAllChannel(String device_id)
	{
		logger.debug("getAllChannel({})", device_id);
		
		List<HashMap<String,String>> list = deviceConfigDao.getAllChannel(device_id);
		
		List<HashMap<String,String>> internet_list = new ArrayList<HashMap<String,String>>();
		List<HashMap<String,String>> iptv_list = new ArrayList<HashMap<String,String>>();
		List<HashMap<String,String>> voip_list = new ArrayList<HashMap<String,String>>();
		List<HashMap<String,String>> tr069_list = new ArrayList<HashMap<String,String>>();
		
		HashMap map = null;
		String servList = null;
		for(int i=0; i<list.size(); i++)
		{
			map = list.get(i);
			if(map == null || map.size()==0)
				continue;
			servList = (String)map.get("serv_list");
			if(StringUtil.IsEmpty(servList))
				continue;
			else
			{
				if(servList.toUpperCase().indexOf("INTERNET") != -1)
				{
					internet_list.add(map);
				}
				// iptv
				else if(servList.toUpperCase().indexOf("OTHER") != -1)
				{
					iptv_list.add(map);
				}
				else if(servList.toUpperCase().indexOf("VOIP") != -1)
				{
					voip_list.add(map);
				}
				else if(servList.toUpperCase().indexOf("TR069") != -1)
				{
					tr069_list.add(map);
				}
			}
		}
		
		HashMap<String, List<HashMap<String,String>>> data = new HashMap<String, List<HashMap<String,String>>>();
		data.put("INTERNET", internet_list);
		data.put("IPTV", iptv_list);
		data.put("TR069", tr069_list);
		data.put("VOIP", voip_list);
		
		return data;
	}
	
	/**
	 * 获取VOIP line信息
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String,String>> getVoipLineInfo(String deviceId)
	{
		logger.debug("getVoipLineInfo({})", deviceId);
		return deviceConfigDao.getVoipLineInfo(deviceId);
	}

	/**
	 * 转化connectType
	 * @param connectType
	 * @param sess_type
	 * @return
	 */
	private String getConnectType(String connectType)
	{
		logger.debug("getConnectType({})", connectType);
		
		// 参数为空，
		if(StringUtil.IsEmpty(connectType))
		{
			logger.error("connectType is null");
			return ""; // -100
		}
		
		// 路由
		if(connectType.equalsIgnoreCase("IP_Routed"))
		{
			return "2";
		}
		
		// 桥接
		else if(connectType.equalsIgnoreCase("PPPoE_Bridged"))
		{
			return "1";
		}
		
		else
		{
			logger.error("connectType is not correct: [{}]", connectType);
			return ""; // 这个地方可能在规范中可能要新定义xxx  -100
		}
	}
	
	/**
	 * 转化connectStatus
	 * @param connectStatus
	 * @return
	 */
	private String getConnectStatus(String connectStatus)
	{
		/**  Demand                                             
			 Connected                                          
			 Connecting                                         
			 Disconnected                                       
			 Unconfigured                                       
			 Authenticating                                     
			 PendingDisconnect  需要在规范中增加几个   xxx    
		 */
		
		// 参数为空，
		if(StringUtil.IsEmpty(connectStatus))
		{
			logger.error("connectStatus is null");
			return ""; //-100
		}
		
		// 已连接
		if(connectStatus.equalsIgnoreCase("Connected"))
		{
			return "1";
		}
		
		// 未连接
		else if(connectStatus.equalsIgnoreCase("Disconnected"))
		{
			return "0";
		}
		
		// 未配置
		else if(connectStatus.equalsIgnoreCase("Unconfigured"))
		{
			return "-1";
		}
		
		else
		{
			logger.error("connectStatus is not correct: [{}]", connectStatus);
			return ""; // 这个地方可能在规范中可能要新定义xxx  -100
		}
	}
	
	public int getAccessType(String deviceId) {
		logger.debug("getAccessType({})", deviceId);
		String accessType = new DeviceConfigDAO().getAccessType(deviceId, 1);
		if (null != accessType) {
			if ("DSL".equals(accessType)) {
				return 1;
			} else if ("Ethernet".equals(accessType)) {
				return 2;
			} else if ("EPON".equalsIgnoreCase(accessType) || "PON".equalsIgnoreCase(accessType)) {
				return 3;
			} else if ("GPON".equalsIgnoreCase(accessType)) {
				return 4;
			} else {
				return -1;
			}
		} else {
			logger.warn("从数据库中未获取到设备的WAN结点信息");
			return -1;
		}
			
	}
}
