
package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevConfigInfoObj;
import com.linkage.itms.dispatch.obj.QueryDevConfigInfoServiceChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryDevConfigInfoService4NX implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryDevConfigInfoService4NX.class);
	private static String ACCESS_TYPE_PATH_DEFAULT = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();
	private HashMap<String, String> bindPortMap = new HashMap<String, String>();
	private static QueryDevConfigInfoService4NX instance = new QueryDevConfigInfoService4NX();
	private ACSCorba corba = new ACSCorba();
	private ArrayList<String> wlanNames = new ArrayList<String>();
	private ArrayList<String> gatherPath = new ArrayList<String>();
	private String checkAccessType = null;
	private ArrayList<String> lani = new ArrayList<String>();
	private String ststusPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
	private String txPowerPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TXPower";
	private String rxPowerEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.RXPower";
	private String ststusPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
	private String txPowerPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
	private String rxPowerGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
	private DevConfigInfoObj obj = new DevConfigInfoObj();

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
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			if (-3 == flag)
			{
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("设备正在被操作，不能正常交互");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}
			else if (1 == flag)
			{
				// 进行采集，获取WANDevice信息
				logger.warn(
						"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								deviceId });
				// int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); //
				// 0表示采集所有节点
				// 在原来基础上增加了一个参数(3)
				// 1.wlan
				// InternetGatewayDevice.LANDevice.{i}. WLANConfiguration.{i}.SSID
				// InternetGatewayDevice.LANDevice.{i}.
				// WLANConfiguration.{i}.TotalAssociations
				// 采集accessType
				String accessType = UserDeviceDAO.getAccType(deviceId);
				ArrayList<ParameValueOBJ> objLlist = null;
				if (null == accessType)
				{
					objLlist = corba.getValue(deviceId, ACCESS_TYPE_PATH_DEFAULT);
					if (null == objLlist || objLlist.isEmpty())
					{
						checker.setResult(1006);
						checker.setResultDesc("采集accessTypes失败");
						return checker.getReturnXml();
					}
					accessType = objLlist.get(0).getValue();
				}
				logger.warn("[{}]采集到的，accessType为：[{}]", deviceId, accessType);
				if ("EPON".equals(accessType))
				{
					checkAccessType = ".X_CT-COM_WANEponLinkConfig";
				}
				else if ("GPON".equals(accessType))
				{
					checkAccessType = ".X_CT-COM_WANGponLinkConfig";
				}
				else
				{
					logger.warn("accessType既不是EPON也不是GPON");
					checker.setResult(1000);
					checker.setResultDesc("accessType既不是EPON也不是GPON");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
				gatherWlan(deviceId);
				gatherInternet(deviceId);
				lanStatus(deviceId, checker);
				// 采集光功率
				if ("GPON".equals(accessType))
				{
					gatherPath.add(ststusPatchGPON);
					gatherPath.add(txPowerPatchGPON);
					gatherPath.add(rxPowerGPON);
				}
				else
				{
					gatherPath.add(ststusPatchEPON);
					gatherPath.add(txPowerPatchEPON);
					gatherPath.add(rxPowerEPON);
				}
				// 2.Internet
				// connectType:InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.ConnectionType
				// connectStatus:InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.ConnectionStatus
				// IPAddress：InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.ExternalIPAddress
				// DNSServer:InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.DNSServers
				// vlan:InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.X_CT-COM_WANGponLinkConfig.VLANIDMark
				// bindPort:InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.X_CT-COM_LanInterface
				// PPPoE:InternetGatewayDevice.WANDevice.1.WANConnectionDevice.{i}.WANPPPConnection.{i}.Username
				// InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+lani.get(p)+".Status
				// InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+lani.get(p)+".MaxBitRate
				// 3.PonInfo
				// private String ststusPatchGPON =
				// "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
				// private String txPowerPatchGPON =
				// "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
				// private String rxPowerGPON =
				// "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
				String[] gatherPathArray = new String[gatherPath.size()];
				gatherPath.toArray(gatherPathArray);
				// 处理设备采集结果
				objLlist = corba.getValue(deviceId, gatherPathArray);
				/*
				 * for(int i=0;i<gatherPathArray.length;i++){
				 * logger.warn("第"+(i+1)+"个参数："+gatherPathArray[i]); } for (ParameValueOBJ
				 * pvobj : objLlist) {
				 * logger.warn(pvobj.getName()+"---"+pvobj.getValue()); }
				 */
				if (null == objLlist || objLlist.isEmpty())
				{
					logger.warn(
							"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]getData sg fail",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1004);
					checker.setResultDesc("设备采集失败");
					return checker.getReturnXml();
				}
				else
				{
					String SSIDname = "";
					String TotalAssociations = "";
					for (int i = 0; i < wlanNames.size(); i++)
					{
						// logger.warn("["+deviceId+"]wlanNames.get("+i+")="+wlanNames.get(i));
						for (ParameValueOBJ pvobj : objLlist)
						{
							if (pvobj.getName().contains(wlanNames.get(i) + ".SSID"))
							{
								SSIDname = SSIDname + pvobj.getValue() + ",";
								// logger.warn("["+deviceId+"]SSIDname="+SSIDname);
							}
							else if (pvobj.getName().contains(
									wlanNames.get(i) + ".TotalAssociations"))
							{
								TotalAssociations = TotalAssociations + pvobj.getValue()
										+ ",";
								// logger.warn("["+deviceId+"]TotalAssociations="+TotalAssociations);
							}
						}
					}
					if (!StringUtil.IsEmpty(SSIDname))
					{
						obj.setsSIDname(SSIDname.substring(0, SSIDname.length() - 1));
					}
					else
					{
						obj.setsSIDname("");
					}
					if (!StringUtil.IsEmpty(TotalAssociations))
					{
						obj.setDevNumber(TotalAssociations.substring(0,
								TotalAssociations.length() - 1));
					}
					else
					{
						obj.setDevNumber("");
					}
					// logger.warn("["+deviceId+"]obj.setsSIDname="+obj.getsSIDname());
					// logger.warn("["+deviceId+"]obj.getDevNumber="+obj.getDevNumber());
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains("ConnectionType"))
						{
							obj.setConnectType(pvobj.getValue());
							// logger.warn("["+deviceId+"]ConnectionType="+obj.getConnectType());
						}
						else if (pvobj.getName().contains("ConnectionStatus"))
						{
							obj.setConnectStatus(pvobj.getValue());
							// logger.warn("["+deviceId+"]ConnectionStatus="+obj.getConnectStatus());
						}
						else if (pvobj.getName().contains("ExternalIPAddress"))
						{
							obj.setiPAddress(pvobj.getValue());
							// logger.warn("["+deviceId+"]ExternalIPAddress="+obj.getiPAddress());
						}
						else if (pvobj.getName().contains("DNSServers"))
						{
							obj.setdNSServer(pvobj.getValue());
							// logger.warn("["+deviceId+"]DNSServers="+obj.getdNSServer());
						}
						else if (pvobj.getName().contains("VLANIDMark"))
						{
							obj.setVlan(pvobj.getValue());
							// logger.warn("["+deviceId+"]VLANIDMark="+obj.getVlan());
						}
						else if (pvobj.getName().contains("X_CT-COM_LanInterface"))
						{
							obj.setBindPort(pvobj
									.getValue()
									.replace(
											"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.",
											"LAN")
									.replace(
											"InternetGatewayDevice.LANDevice.1.WLANConfiguration.",
											"WLAN").replace(".", ""));
							// logger.warn("["+deviceId+"]COM_LanInterface="+obj.getBindPort());
						}
						else if (pvobj.getName().contains("Username"))
						{
							obj.setpPPoE(pvobj.getValue());
							// logger.warn("["+deviceId+"]Username="+obj.getpPPoE());
						}
					}
					String status = "";
					String MaxBitRate = "";
					String lanName = "";
					for (int p = 0; p < lani.size(); p++)
					{
						lanName = lanName + "LAN " + lani.get(p) + ",";
						// logger.warn("["+deviceId+"]lani.get(p)="+lani.get(p));
						for (ParameValueOBJ pvobj : objLlist)
						{
							if (pvobj.getName().contains(
									"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
											+ lani.get(p) + ".Status"))
							{
								status = status + pvobj.getValue() + ",";
							}
							else if (pvobj.getName().contains(
									"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
											+ lani.get(p) + ".MaxBitRate"))
							{
								MaxBitRate = MaxBitRate + pvobj.getValue() + ",";
							}
						}
					}
					obj.setLanName(lanName.substring(0, lanName.length() - 1));
					obj.setLinkStats(status.substring(0, status.length() - 1));
					obj.setLinkRate(MaxBitRate.substring(0, MaxBitRate.length() - 1));
					// logger.warn("["+deviceId+"]setLanName="+obj.getLanName());
					// logger.warn("["+deviceId+"]setLinkStats="+obj.getLinkStats());
					// logger.warn("["+deviceId+"]setLinkRate="+obj.getLinkRate());
					double tx_power = 0;
					double rx_power = 0;
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains("InterfaceConfig.Status"))
						{
							obj.setPonStat(pvobj.getValue());
						}
						else if (pvobj.getName().contains("InterfaceConfig.TXPower"))
						{
							tx_power = StringUtil.getDoubleValue(pvobj.getValue());
						}
						else if (pvobj.getName().contains("InterfaceConfig.RXPower"))
						{
							rx_power = StringUtil.getDoubleValue(pvobj.getValue());
						}
					}
					// logger.warn("["+deviceId+"]tx_power="+tx_power);
					// logger.warn("["+deviceId+"]rx_power="+rx_power);
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
					// logger.warn("["+deviceId+"]tx_power="+tx_power);
					// logger.warn("["+deviceId+"]rx_power="+rx_power);
					obj.settXPower(tx_power + "");
					obj.setrXPower(rx_power + "");
				}
				checker.setObj(obj);
				// logger.warn("["+deviceId+"]obj="+obj.toString());
				String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"DiagnosticService");
				logger.warn(
						"servicename[QueryDevConfigInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								returnXml });
				return returnXml;
			}
			else
			{
				logger.warn("设备不在线，无法获取节点值,device_id={}", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("设备不在线");
				logger.warn("return=({})", checker.getReturnXml());
				return checker.getReturnXml();
			}
		}
	}

	private void gatherWlan(String deviceId)
	{
		String servListPathI = "InternetGatewayDevice.LANDevice.";
		// 不支持快速采集，获取全部路径
		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, servListPathI, 0);
		logger.warn("LANDevice.size:{}", wanConnPathsList.size());
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			return;
		}
		else
		{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(".SSID") >= 0
						&& namepath.indexOf(".SSIDIsolate") < 0
						&& namepath.indexOf("WLANConfiguration") >= 0)
				{
					wlanNames.add(namepath.substring(0, namepath.lastIndexOf(".")));
					paramNameList.add(namepath);
				}
				if (namepath.indexOf(".TotalAssociations") >= 0
						&& namepath.indexOf("WLANConfiguration") >= 0)
				{
					paramNameList.add(namepath);
				}
			}
			gatherPath.addAll(paramNameList);
		}
	}

	private void gatherInternet(String deviceId)
	{
		boolean servFlagIntnet = false;
		// 查询型号HG260GS HG261GS 不支持快速采集
		List<String> models = new ArrayList<String>();
		models.add("HG260GS");
		models.add("HG261GS");
		String model = userDevDao.isNotSuportFastGather(deviceId);
		// 普通采集
		List<String> servListJ = null;
		int countServB = 0;
		String servListPathI = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String servListPathJ = null;
		String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
		String wan_index_result = "";
		logger.warn("[{}]获取wan连接索引", deviceId);
		ArrayList<ParameValueOBJ> valueList = new ArrayList<ParameValueOBJ>();
		if (!models.contains(model))
		{
			valueList = corba.getValue(deviceId, wan_index);
		}
		// 查询型号HG260GS HG261GS 不支持快速采集
		if (!models.contains(model) && (valueList != null && valueList.size() != 0))
		{
			logger.warn("[{}]支持快速采集，获取全部路径", deviceId);
			for (ParameValueOBJ pvobj : valueList)
			{
				if (pvobj.getName().endsWith("X_CT-COM_WANIndex"))
				{
					wan_index_result = pvobj.getValue();
					break;
				}
			}
			// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
			if (!StringUtil.IsEmpty(wan_index_result))
			{
				String wan[] = wan_index_result.replace("\"", "").split(",");
				for (String wanPa : wan)
				{
					if ((wanPa.endsWith("INTERNET") || wanPa.endsWith("internet"))
							&& (wanPa.contains(".") && wanPa.contains(";")))
					{
						/* if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed")) { */
						String a = wanPa.split(";")[0].split("\\.")[0];
						String b = wanPa.split(";")[0].split("\\.")[1];
						String vlanid = wanPa.split(";")[2];
						String conntype = wanPa.split(";")[1].toLowerCase();
						// 桥接 路由：ppp ; dhcp static：ip
						if (conntype.contains("bridged"))
						{
							servListPathJ = servListPathI + a + ".WANPPPConnection.";
						}
						else if (conntype.contains("dhcp"))
						{
							servListPathJ = servListPathI + a + ".WANIPConnection.";
						}
						else if (conntype.contains("routed"))
						{
							servListPathJ = servListPathI + a + ".WANPPPConnection.";
						}
						else
						{
							servListPathJ = servListPathI + a + ".WANIPConnection.";
						}
						/*
						 * servListPathJ = servListPathI + a + ".WANPPPConnection.";
						 * servListJ = corba.getIList(deviceId, servListPathJ); if (null
						 * == servListJ || servListJ.isEmpty()) { servListJ =
						 * corba.getIList(deviceId, servListPathI + a +
						 * ".WANIPConnection."); if (null == servListJ ||
						 * servListJ.isEmpty()) { continue; } servListPathJ =
						 * servListPathI + a + ".WANIPConnection."; }
						 */
						countServB++;
						servFlagIntnet = true;
						logger.warn("[{}]获取INTERNET成功", deviceId);
						gatherPath.add(servListPathJ + b + ".ConnectionType");
						gatherPath.add(servListPathJ + b + ".ConnectionStatus");
						gatherPath.add(servListPathJ + b + ".ExternalIPAddress");
						gatherPath.add(servListPathJ + b + ".DNSServers");
						gatherPath.add(servListPathI + a + checkAccessType
								+ ".VLANIDMark");
						gatherPath.add(servListPathJ + b + ".X_CT-COM_LanInterface");
						gatherPath.add(servListPathJ + b + ".Username");
						break;
					}
				}
			}
		}
		else
		{
			logger.warn("[{}]不支持快速采集，获取全部路径", deviceId);
			// 不支持快速采集，获取全部路径
			ArrayList<String> wanConnPathsList = new ArrayList<String>();
			// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
			wanConnPathsList = corba.getParamNamesPath(deviceId, servListPathI, 0);
			logger.warn("wanConnPathsList.size:{}", wanConnPathsList.size());
			if (wanConnPathsList == null || wanConnPathsList.size() == 0
					|| wanConnPathsList.isEmpty())
			{
				return;
			}
			else
			{
				ArrayList<String> paramNameList = new ArrayList<String>();
				for (int i = 0; i < wanConnPathsList.size(); i++)
				{
					String namepath = wanConnPathsList.get(i);
					if (namepath.indexOf(".X_CT-COM_ServiceList") >= 0)
					{
						paramNameList.add(namepath);
					}
				}
				wanConnPathsList = new ArrayList<String>();
				wanConnPathsList.addAll(paramNameList);
			}
			if (wanConnPathsList.size() == 0)
			{
				logger.warn("[DevConfigServiceNew] [{}]无X_CT-COM_ServiceList节点：",
						deviceId);
			}
			else
			{
				Collections.reverse(wanConnPathsList);
			}
			String[] paramNametemp = new String[wanConnPathsList.size()];
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				paramNametemp[i] = wanConnPathsList.get(i);
			}
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
					paramNametemp);
			if (paramValueMap.isEmpty())
			{
				logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败", deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet())
			{
				logger.debug("[{}]{}={} ",
						new Object[] { deviceId, entry.getKey(), entry.getValue() });
				// InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CT-COM_ServiceList
				if (entry.getValue().indexOf("INTERNET") >= 0
						|| entry.getValue().indexOf("internet") >= 0)
				{
					String a = entry.getKey().split("\\.")[4];
					String b = entry.getKey().split("\\.")[6];
					int index = entry.getKey().indexOf(b + ".X_CT-COM_ServiceList");
					servListPathJ = entry.getKey().substring(0, index);
					countServB++;
					servFlagIntnet = true;
					logger.warn("[{}]获取INTERNET成功", deviceId);
					gatherPath.add(servListPathJ + b + ".ConnectionType");
					gatherPath.add(servListPathJ + b + ".ConnectionStatus");
					gatherPath.add(servListPathJ + b + ".ExternalIPAddress");
					gatherPath.add(servListPathJ + b + ".DNSServers");
					gatherPath.add(servListPathI + a + checkAccessType + ".VLANIDMark");
					gatherPath.add(servListPathJ + b + ".X_CT-COM_LanInterface");
					gatherPath.add(servListPathJ + b + ".Username");
					break;
				}
			}
		}
		if (!servFlagIntnet)
		{
			logger.warn("[{}]获取INTNET失败，没有得到值为INTNET的节点", deviceId);
		}
	}

	private void lanStatus(String deviceId, QueryDevConfigInfoServiceChecker checker)
	{
		String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		List<String> iList = corba.getIList(deviceId, lanPath);
		if (null == iList || iList.isEmpty())
		{
			return;
		}
		else
		{
			logger.warn("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
		}
		for (String i : iList)
		{
			lani.add(i);
			gatherPath
					.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
							+ i + ".Status");
			gatherPath
					.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
							+ i + ".MaxBitRate");
		}
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
