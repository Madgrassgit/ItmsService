
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevConfigInfoObj;
import com.linkage.itms.dispatch.obj.QueryDevConfigInfoServiceChecker;

public class QueryDevConfigInfoService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(QueryDevConfigInfoService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();
	private HashMap<String, String> bindPortMap = new HashMap<String, String>();
	private static QueryDevConfigInfoService instance = new QueryDevConfigInfoService();

	private QueryDevConfigInfoService()
	{
		initBindPortMap();
	}

	public static QueryDevConfigInfoService getInstance()
	{
		if (null == instance)
		{
			instance = new QueryDevConfigInfoService();
		}
		return instance;
	}

	private void initBindPortMap()
	{
		String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
		String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
		String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
		String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
		String WLAN1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
		String WLAN2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
		String WLAN3 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3";
		String WLAN4 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4";
		// 兼容带点的
		String LAN1_DOT = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.";
		String LAN2_DOT = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2.";
		String LAN3_DOT = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3.";
		String LAN4_DOT = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4.";
		String WLAN1_DOT = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.";
		String WLAN2_DOT = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.";
		String WLAN3_DOT = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3.";
		String WLAN4_DOT = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4.";
		bindPortMap.put("LAN1", LAN1);
		bindPortMap.put("LAN2", LAN2);
		bindPortMap.put("LAN3", LAN3);
		bindPortMap.put("LAN4", LAN4);
		bindPortMap.put("WLAN1", WLAN1);
		bindPortMap.put("WLAN2", WLAN2);
		bindPortMap.put("WLAN3", WLAN3);
		bindPortMap.put("WLAN4", WLAN4);
		bindPortMap.put("SSID2", WLAN2);
		bindPortMap.put(LAN1, "LAN1");
		bindPortMap.put(LAN2, "LAN2");
		bindPortMap.put(LAN3, "LAN3");
		bindPortMap.put(LAN4, "LAN4");
		bindPortMap.put(WLAN1, "WLAN1");
		bindPortMap.put(WLAN2, "WLAN2");
		bindPortMap.put(WLAN3, "WLAN3");
		bindPortMap.put(WLAN4, "WLAN4");
		bindPortMap.put(LAN1_DOT, "LAN1");
		bindPortMap.put(LAN2_DOT, "LAN2");
		bindPortMap.put(LAN3_DOT, "LAN3");
		bindPortMap.put(LAN4_DOT, "LAN4");
		bindPortMap.put(WLAN1_DOT, "WLAN1");
		bindPortMap.put(WLAN2_DOT, "WLAN2");
		bindPortMap.put(WLAN3_DOT, "WLAN3");
		bindPortMap.put(WLAN4_DOT, "WLAN4");
	}

	@Override
	public String work(String inXml)
	{
		QueryDevConfigInfoServiceChecker checker = new QueryDevConfigInfoServiceChecker(
				inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = userInfoMap.get("device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn(
						"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("未绑定设备");
				return checker.getReturnXml();
			}
			DevConfigInfoObj obj = new DevConfigInfoObj();
			// 进行采集，获取WANDevice信息
			logger.warn(
					"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), deviceId });
			int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); // 0表示采集所有节点
																				// 在原来基础上增加了一个参数(3)
			// int rsint = 1;
			logger.warn(
					"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), rsint });
			// 采集失败
			if (rsint != 1)
			{
				logger.warn(
						"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]getData sg fail",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1004);
				checker.setResultDesc("设备采集失败");
				return checker.getReturnXml();
			}
			// 采集成功获取需要的信息
			else
			{
				Map<String, List<HashMap<String, String>>> allServMap = getAllChannel(deviceId);
				// 处理INTERNET信息
				dealInternetInfo(allServMap.get("INTERNET"), obj);
				// 处理Pon口信息
				dealPonInfo(deviceId, obj);
				// 处理WLan信息
				dealWlan(deviceId, obj);
			}
			checker.setObj(obj);
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"DiagnosticService");
		logger.warn(
				"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}

	/**
	 * 处理WLAN信息
	 * 
	 * @param deviceId
	 * @param obj
	 */
	private void dealWlan(String deviceId, DevConfigInfoObj obj)
	{
		logger.debug("dealPonInfo({},{})", deviceId, obj);
		ArrayList<HashMap<String,String>> wlanLisy = deviceConfigDao.getWlan(deviceId);
		if (wlanLisy == null || wlanLisy.isEmpty())
		{
			obj.setsSIDname(StringUtil.getStringValue(""));
			obj.setDevNumber("");
			return;
		}
		String ssid = "";
		String associated_num = "";
		for(HashMap<String,String> map:wlanLisy){
			ssid = ssid + StringUtil.getStringValue(map, "ssid") + ",";
			associated_num = associated_num + StringUtil.getStringValue(map, "associated_num") + ",";
		}
		if(ssid.endsWith(",")){
			ssid = ssid.substring(0,ssid.length()-1);
		}
		if(associated_num.endsWith(",")){
			associated_num = associated_num.substring(0,associated_num.length()-1);
		}
		obj.setsSIDname(ssid);
		obj.setDevNumber(associated_num);
	}

	/**
	 * 处理PON口信息
	 * 
	 * @param deviceId
	 * @param obj
	 */
	private void dealPonInfo(String deviceId, DevConfigInfoObj obj)
	{
		logger.debug("dealPonInfo({},{})", deviceId, obj);
		Map<String, String> map = deviceConfigDao.getPonInfo(deviceId);
		if (map == null || map.isEmpty())
		{
			obj.setPonStat(StringUtil.getStringValue(""));
			obj.settXPower(StringUtil.getStringValue(""));
			obj.setrXPower(StringUtil.getStringValue(""));
			return;
		}
		obj.setPonStat(StringUtil.getStringValue(map, "status"));
		double tx_power = StringUtil.getDoubleValue(map.get("tx_power"));
		double rx_power = StringUtil.getDoubleValue(map.get("rx_power"));
		if (tx_power > 30)
		{
			double temp_tx_power = (Math.log(tx_power / 10000) / Math.log(10)) * 10;
			tx_power = (int) temp_tx_power;
			if (tx_power % 10 >= 5)
			{
				tx_power = (tx_power / 10 + 1) * 10;
			}
			else
			{
				tx_power = tx_power / 10 * 10;
			}
		}
		if (rx_power > 30)
		{
			double temp_rx_power = (Math.log(rx_power / 10000) / Math.log(10)) * 10;
			rx_power = (int) temp_rx_power;
			if (rx_power % 10 >= 5)
			{
				rx_power = (rx_power / 10 + 1) * 10;
			}
			else
			{
				rx_power = rx_power / 10 * 10;
			}
		}
		obj.settXPower(tx_power + "");
		obj.setrXPower(rx_power + "");
	}

	/**
	 * 处理INTERNET信息
	 * 
	 * @param list
	 * @param obj
	 */
	@SuppressWarnings("rawtypes")
	private void dealInternetInfo(List<HashMap<String, String>> list, DevConfigInfoObj obj)
	{
		logger.debug("dealInternetInfo({},{})", list, obj);
		if (list == null || list.isEmpty())
		{
			obj.setConnectType(getConnectType(""));
			obj.setiPAddress(StringUtil.getStringValue(""));
			obj.setdNSServer(StringUtil.getStringValue(""));
			obj.setConnectStatus(getConnectStatus(""));
			obj.setVlan(StringUtil.getStringValue(""));
			obj.setBindPort(StringUtil.getStringValue(""));
			obj.setpPPoE(StringUtil.getStringValue(""));
			return;
		}
		// 只取一条数据
		Map map = list.get(0);
		obj.setiPAddress(StringUtil.getStringValue(map, "ip"));
		obj.setdNSServer(StringUtil.getStringValue(map, "dns"));
		List<HashMap<String, String>> netList = deviceConfigDao
				.getInternet(StringUtil.getStringValue(map, "device_id"));
		if (netList != null && !netList.isEmpty())
		{
			obj.setVlan(StringUtil.getStringValue(netList.get(0), "vlan_id"));
		}
		else
		{
			obj.setVlan(StringUtil.getStringValue(""));
		}
		obj.setConnectType(
				StringUtil.getStringValue(map, "conn_type"));
		obj.setConnectStatus(
				StringUtil.getStringValue(map, "conn_status"));
		obj.setBindPort(getBindPort(StringUtil.getStringValue(map, "bind_port")));
		obj.setpPPoE(StringUtil.getStringValue(map, "username"));
		
		ArrayList<HashMap<String, String>> lanList = deviceConfigDao.queryLanEth(StringUtil.getStringValue(map, "device_id"));
		if(null == lanList || lanList.isEmpty()){
			obj.setLanName("");
			obj.setLinkRate("");
			obj.setLinkStats("");
			return;
		}
		String lanName = "";
		String linkRate = "";
		String linkStats = "";
		for(HashMap<String,String> lanMap : lanList){
			lanName = lanName + "LAN " + StringUtil.getStringValue(lanMap, "lan_eth_id") + ",";
			linkRate = linkRate + StringUtil.getStringValue(lanMap, "max_bit_rate") + ",";
			linkStats = linkStats + StringUtil.getStringValue(lanMap, "status") + ",";
		}
		if(lanName.endsWith(",")){
			lanName = lanName.substring(0,lanName.length()-1);
		}
		if(linkRate.endsWith(",")){
			linkRate = linkRate.substring(0,linkRate.length()-1);
		}
		if(linkStats.endsWith(",")){
			linkStats = linkStats.substring(0,linkStats.length()-1);
		}
		obj.setLanName(lanName);
		obj.setLinkRate(linkRate);
		obj.setLinkStats(linkStats);
	}

	/**
	 * 获取bindPort
	 * 
	 * @param bindPort
	 * @return
	 */
	private String getBindPort(String bindPort)
	{
		String[] bindPortArr = bindPort.split(",");
		StringBuffer bindPortSB = new StringBuffer();
		logger.debug("bind_all_split_port:{}", bindPortArr);
		for (int m = 0; m < bindPortArr.length; m++)
		{
			if (0 != m)
			{
				bindPortSB.append(",");
			}
			String temStr = bindPortArr[m];
			logger.debug("bind_port_first:{}", temStr);
			if (".".equals(temStr.substring(temStr.length() - 1, temStr.length())))
			{
				temStr = temStr.substring(0, temStr.length() - 1);
			}
			logger.debug("bind_port_two:{}", temStr);
			bindPortSB.append(bindPortMap.get(temStr));
		}
		return bindPortSB.toString();
	}

	/**
	 * 获取所有的session信息,根据serv_list分类
	 * 
	 * @param device_id
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, List<HashMap<String, String>>> getAllChannel(String device_id)
	{
		logger.debug("getAllChannel({})", device_id);
		List<HashMap<String, String>> list = deviceConfigDao.getAllChannel(device_id);
		List<HashMap<String, String>> internet_list = new ArrayList<HashMap<String, String>>();
		HashMap map = null;
		String servList = null;
		for (int i = 0; i < list.size(); i++)
		{
			map = list.get(i);
			if (map == null || map.size() == 0)
				continue;
			servList = (String) map.get("serv_list");
			if (StringUtil.IsEmpty(servList))
				continue;
			else
			{
				if (servList.toUpperCase().indexOf("INTERNET") != -1)
				{
					internet_list.add(map);
				}
			}
		}
		HashMap<String, List<HashMap<String, String>>> data = new HashMap<String, List<HashMap<String, String>>>();
		data.put("INTERNET", internet_list);
		return data;
	}

	/**
	 * 转化connectType
	 * 
	 * @param connectType
	 * @param sess_type
	 * @return
	 */
	private String getConnectType(String connectType)
	{
		logger.debug("getConnectType({})", connectType);
		// 参数为空，
		if (StringUtil.IsEmpty(connectType))
		{
			logger.error("connectType is null");
			return ""; // -100
		}
		// 路由
		if (connectType.equalsIgnoreCase("IP_Routed"))
		{
			return "2";
		}
		// 桥接
		else if (connectType.equalsIgnoreCase("PPPoE_Bridged"))
		{
			return "1";
		}
		else
		{
			logger.error("connectType is not correct: [{}]", connectType);
			return ""; // 这个地方可能在规范中可能要新定义xxx -100
		}
	}

	/**
	 * 转化connectStatus
	 * 
	 * @param connectStatus
	 * @return
	 */
	private String getConnectStatus(String connectStatus)
	{
		/**
		 * Demand Connected Connecting Disconnected Unconfigured Authenticating
		 * PendingDisconnect 需要在规范中增加几个 xxx
		 */
		// 参数为空，
		if (StringUtil.IsEmpty(connectStatus))
		{
			logger.error("connectStatus is null");
			return ""; // -100
		}
		// 已连接
		if (connectStatus.equalsIgnoreCase("Connected"))
		{
			return "1";
		}
		// 未连接
		else if (connectStatus.equalsIgnoreCase("Disconnected"))
		{
			return "0";
		}
		// 未配置
		else if (connectStatus.equalsIgnoreCase("Unconfigured"))
		{
			return "-1";
		}
		else
		{
			logger.error("connectStatus is not correct: [{}]", connectStatus);
			return ""; // 这个地方可能在规范中可能要新定义xxx -100
		}
	}
}
