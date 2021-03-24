
package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevConfigChecker4NX;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宁夏电信设备配置查询接口
 * 
 * @author chenxj6(工号) Tel:78
 * @version 1.0
 * @since 2016-10-13
 */
public class DevConfigService4NX implements IService
{
	
	// 日志记录
	private static final Logger logger = LoggerFactory.getLogger(DevConfigService4NX.class);
	
	private List internetlist = new ArrayList<HashMap<String, String>>();
	private Map<String, String> internetMap = new HashMap<String, String>();
	private List iptvlist = new ArrayList<HashMap<String, String>>();
	private Map<String, String> iptvMap = new HashMap<String, String>();
	
	private Map<String, String> bindPortMap = new HashMap<String, String>();
	
	private DeviceConfigDAO dao = new DeviceConfigDAO();
	
//	connectiontype
//	queryWanType 上网方式
//	queryAccessType 接入方式查询
//	queryWiFi 采集到的WIFI的状态
//
//	InternetGatewayDevice.WANDevice.1.WANConnectionDevice.{i}.WANPPPConnection.{i}.X_CT-COM_LanInterface  绑定端口
//	InternetGatewayDevice.WANDevice.1.WANConnectionDevice.{i}.WANPPPConnection.{i}.X_CT-COM_ServiceList   业务类型（“TR069”,“INTERNET”,“VOIP”,“OTHER”）
//	InternetGatewayDevice.WANDevice.1.WANConnectionDevice.{i}.WANPPPConnection.{i}.X_CT-COM_MulticastVlan  组播VLAN
//	InternetGatewayDevice.WANDevice.1.WANConnectionDevice.{i}.X_CT-COM_WANGponLinkConfig.VLANIDMark  业务VLID
//	WANIPConnection 和 WANPPPConnection 两个都要采集，会有一个为空，排除掉
//	{ inter_list: conn_type , bind_port , vlan_id , status  }	
//	{ iptv_list:  conn_type , bind_port , vlan_id , multicast_vlan  }
	
	@Override
	public String work(String inXml)
	{
		Map<String,String> resultMap = new HashMap<String, String>();
		Map<String,List<HashMap<String,String>>> configInfo = new HashMap<String, List<HashMap<String,String>>>();
		
		DevConfigChecker4NX checker = new DevConfigChecker4NX(inXml);
		
		if (false == checker.check()) {
			logger.error(
					"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户设备信息
		Map<String,String> userDevInfo = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		
		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(
					"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
		} else {
			
			String deviceId = userDevInfo.get("device_id");
			
			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(
						"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
			}  else {
					// 采集WiFiState chenxj6 beigin
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					ACSCorba corba = new ACSCorba();
					
					int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
					// 设备正在被操作，不能获取节点值
					if (-3 == flag) {
						logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
						checker.setResult(1003);
						checker.setResultDesc("设备不能正常交互");
						logger.warn("return=({})", checker.getReturnXml());  // 打印回参
						return checker.getReturnXml();
					}
					// 设备在线
					else if (1 == flag) {
						logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
						
						//采集accessType
						String accessType = null;
						String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
						ArrayList<ParameValueOBJ> accessTypeList = corba.getValue(deviceId, accessTypePath);
						if (accessTypeList != null && accessTypeList.size() != 0) {
							for (ParameValueOBJ pvobj : accessTypeList) {
								if (pvobj.getName().endsWith("WANAccessType")) {
									accessType = pvobj.getValue();
								}
							}
						}
						logger.warn("采集到的，accessType为：[{}]", accessType);
						
						String checkAccessType = null;
						
						if("EPON".equals(accessType)){
							checkAccessType = ".X_CT-COM_WANEponLinkConfig";
						}else if("GPON".equals(accessType)){
							checkAccessType = ".X_CT-COM_WANGponLinkConfig";
						}else{
							logger.warn("accessType既不是EPON也不是GPON");
							checker.setResult(1000);
							checker.setResultDesc("accessType既不是EPON也不是GPON");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}

						
						String serv = null;
						boolean servFlagIntnet = false;
						boolean servFlagIptv = false;
						
						String servListPathI = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
						
					    List<String> servListI = corba.getIList(deviceId, servListPathI);
						if (null == servListI || servListI.isEmpty()) {
							logger.warn("[{}]获取servListI失败", deviceId);
							checker.setResult(1000);
							checker.setResultDesc("获取servListI失败");
							return checker.getReturnXml();
						} else {
							logger.warn("[{}]获取servListI成功，servListI.size={}",	deviceId, servListI.size());
						}
						
						int countServB = 0;
						
						for(String i : servListI){
							String servListPathJ = servListPathI + i + ".WANPPPConnection.";
							
							List<String> servListJ = corba.getIList(deviceId, servListPathJ);
							if (null == servListJ || servListJ.isEmpty()) {
								servListJ = corba.getIList(deviceId, servListPathI + i + ".WANIPConnection.");
								if(null == servListJ || servListJ.isEmpty()){
									continue;
								}
							}
							for(String j : servListJ){
								String servListPathK = servListPathJ + j + ".X_CT-COM_ServiceList";
							    ArrayList<ParameValueOBJ> servList = corba.getValue(deviceId, servListPathK);
								if (null==servList || servList.size()==0 || null==servList.get(0) || null==servList.get(0).getValue()) {
									continue;
								}else{
									serv = servList.get(0).getValue();// 
									if("OTHER".equalsIgnoreCase(serv)){
										countServB++;
										servFlagIptv = true;
										logger.warn("[{}]获取OTHER成功",	deviceId);
										
										// 采集 X_CT-COM_MulticastVlan
										String mulPath = servListPathJ + j + ".X_CT-COM_MulticastVlan";
										ArrayList<ParameValueOBJ> mulList = corba.getValue(deviceId, mulPath);
										if (null == mulList || mulList.size()==0 || null==mulList.get(0) || null==mulList.get(0).getValue()) {
											iptvMap.put("multicast_vlan", "");
											logger.warn("[{}]采集multicast_vlan失败或者值为空",	deviceId);
										}else{
											iptvMap.put("multicast_vlan", mulList.get(0).getValue());
											logger.warn("[{}]采集multicast_vlan成功，值为：[{}]",	new Object[]{deviceId,mulList.get(0).getValue()});
										}
										
										// 采集 X_CT-COM_LanInterface,绑定端口
										String bindPortPath = servListPathJ + j + ".X_CT-COM_LanInterface";
										ArrayList<ParameValueOBJ> bindPortList = corba.getValue(deviceId, bindPortPath);
										if (null == bindPortList || bindPortList.size()==0 || null==bindPortList.get(0) || null==bindPortList.get(0).getValue()) {
											iptvMap.put("bind_port", "");
											logger.warn("[{}]采集LanInterface失败或者值为空",	deviceId);
										}else{
											iptvMap.put("bind_port", bindPortList.get(0).getValue());
											logger.warn("[{}]采集LanInterface成功，值为：[{}]",	deviceId,bindPortList.get(0).getValue());
										}
										
										// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
										String connTypePath = servListPathJ + j + ".ConnectionType";
										ArrayList<ParameValueOBJ> connTypeList = corba.getValue(deviceId, connTypePath);
										if (null == connTypeList || connTypeList.size()==0 || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
											iptvMap.put("conn_type", "");
											logger.warn("[{}]采集ConnectionType失败或者值为空",	deviceId);
										}else{
											iptvMap.put("conn_type", connTypeList.get(0).getValue());
											logger.warn("[{}]采集ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
										}
										
										// 采集 VLANIDMark，43等
										String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+checkAccessType+".VLANIDMark";
										ArrayList<ParameValueOBJ> vlanList = corba.getValue(deviceId, vlanPath);
										if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
											iptvMap.put("vlan_id", "");
											logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
										}else{
											iptvMap.put("vlan_id", vlanList.get(0).getValue());
											logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
										}
									}else if("INTERNET".equalsIgnoreCase(serv)){
										countServB++;
										servFlagIntnet = true;
										logger.warn("[{}]获取INTERNET成功",	deviceId);
										
										// 采集 WifiState
										String wifiStatePath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_CT-COM_APModuleEnable";
										ArrayList<ParameValueOBJ> wifiStateLlist = corba.getValue(deviceId, wifiStatePath);
										if (null == wifiStateLlist || wifiStateLlist.isEmpty()) {
											logger.warn("wifiStatus节点值没有获取到，请确认节点路径是否正确，deviceId:({})", deviceId);
											internetMap.put("status", "");
										}else{
											internetMap.put("status", wifiStateLlist.get(0).getValue());
										}
										
										// 采集 X_CT-COM_LanInterface,绑定端口
										String bindPortPath = servListPathJ + j + ".X_CT-COM_LanInterface";
										ArrayList<ParameValueOBJ> bindPortList = corba.getValue(deviceId, bindPortPath);
										if (null == bindPortList || bindPortList.size()==0 || null==bindPortList.get(0) || null==bindPortList.get(0).getValue()) {
											internetMap.put("bind_port", "");
											logger.warn("[{}]采集LanInterface失败或者值为空",	deviceId);
										}else{
											internetMap.put("bind_port", bindPortList.get(0).getValue());
											logger.warn("[{}]采集LanInterface成功，值为：[{}]",	deviceId,bindPortList.get(0).getValue());
										}
										
										// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
										String connTypePath = servListPathJ + j + ".ConnectionType";
										ArrayList<ParameValueOBJ> connTypeList = corba.getValue(deviceId, connTypePath);
										if (null == connTypeList || connTypeList.size()==0 || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
											internetMap.put("conn_type", "");
											logger.warn("[{}]采集ConnectionType失败或者值为空",	deviceId);
										}else{
											internetMap.put("conn_type", connTypeList.get(0).getValue());
											logger.warn("[{}]采集ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
										}
										
										// 采集 VLANIDMark，43等
										String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+checkAccessType+".VLANIDMark";
										ArrayList<ParameValueOBJ> vlanList = corba.getValue(deviceId, vlanPath);
										if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
											internetMap.put("vlan_id", "");
											logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
										}else{
											internetMap.put("vlan_id", vlanList.get(0).getValue());
											logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
										}
									}
								}
								if(countServB==2){
									break;
								}
							}
							if(countServB==2){
								break;
							}
						}
						
						if(!servFlagIptv){
							logger.warn("[{}]获取OTHER失败，没有得到值为OTHER的节点",	deviceId);
						}
						
						if(!servFlagIntnet){
							logger.warn("[{}]获取INTNET失败，没有得到值为INTNET的节点",	deviceId);
						}
						
						
					// 接入方式
					if ("DSL".equals(accessType)) {
						resultMap.put("AccessType", "1");
					} else if ("Ethernet".equals(accessType)) {
						resultMap.put("AccessType", "2");
					} else if ("EPON".equalsIgnoreCase(accessType) || "PON".equalsIgnoreCase(accessType)) {
						resultMap.put("AccessType", "3");
					} else if ("GPON".equalsIgnoreCase(accessType)) {
						resultMap.put("AccessType", "4");
					} else {
						resultMap.put("AccessType", "");
					}
					
					//上网业务
					internetlist.add(internetMap);
					configInfo.put("inter_list", internetlist);
					
					//IPTV
					iptvlist.add(iptvMap);
					configInfo.put("iptv_list", iptvlist);
					
					checker.setResult(0);
					checker.setResultDesc("成功");
					
					checker.setResultMap(resultMap);
					checker.setConfigInfo(configInfo);
				} else {// 设备不在线，不能获取节点值
					logger.warn("设备不在线，无法获取节点值");
					checker.setResult(1003);
					checker.setResultDesc("设备不能正常交互");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
		}
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
	
		return returnXml;
	}

	
	private String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
	private String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
	private String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
	private String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
	private String WLAN1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
	private String WLAN2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
	private String WLAN3 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3";
	private String WLAN4 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4";

	public DevConfigService4NX()
	{
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
	}
	public DeviceConfigDAO getDao()
	{
		return dao;
	}

	public void setDao(DeviceConfigDAO dao)
	{
		this.dao = dao;
	}

	public List<HashMap<String, String>> getInternetlist()
	{
		return internetlist;
	}

	public void setInternetlist(List<HashMap<String, String>> internetlist)
	{
		this.internetlist = internetlist;
	}

	public List<HashMap<String, String>> getIptvlist()
	{
		return iptvlist;
	}

	public void setIptvlist(List<HashMap<String, String>> iptvlist)
	{
		this.iptvlist = iptvlist;
	}

}