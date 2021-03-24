
package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevConfigCheckerNew;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 宁夏电信设备配置查询接口
 * 
 * @author chenxj6(工号) Tel:78
 * @version 1.0
 * @since 2016-10-13
 */
public class DevConfigServiceNew4NX implements IService
{
	
	// 日志记录
	private static final Logger logger = LoggerFactory.getLogger(DevConfigServiceNew4NX.class);
	
	private List<HashMap<String, String>> internetlist = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> iptvlist = new ArrayList<HashMap<String, String>>();
	
	private HashMap<String, String> bindPortMap = new HashMap<String, String>();
	
	private DeviceConfigDAO dao = new DeviceConfigDAO();
	private ACSCorba corba = new ACSCorba();
	
	private static String ACCESS_TYPE_PATH_DEFAULT = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
	private String ststusPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
	private String txPowerPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TXPower";
	private String rxPowerEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.RXPower";
	private String ststusPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
	private String txPowerPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
	private String rxPowerGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
	private ArrayList<String> gatherPath = new ArrayList<String>();
	private ArrayList<String> lani = new ArrayList<String>();
	private ArrayList<String> iptvPathList = new ArrayList<String>();
	private ArrayList<String> wbandPathList = new ArrayList<String>();
	
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
		Map<String, String> resultMap = new HashMap<String, String>();
		Map<String, List<HashMap<String, String>>> configInfo = new HashMap<String, List<HashMap<String, String>>>();
		
		DevConfigCheckerNew checker = new DevConfigCheckerNew(inXml);

		if (false == checker.check()) {
			logger.error(
					"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();

		// 查询用户设备信息
		Map<String, String> userDevInfo = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());

		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(
					"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			return checker.getReturnXml();
		} else {
			Map<String, String> devMap = userDevDao.getDevStatus(userDevInfo
					.get("user_id"));
			String deviceId = userDevInfo.get("device_id");
			String complete_time = devMap.get("complete_time");

			try {
				long time = Long.parseLong(complete_time + "000");
				// 注册时间:YYYY-MM-DD hh:mm:ss, 样例：2016-03-11 12:33:00
				Date date = new Date();
				date.setTime(time);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				checker.setRegTime(sdf.format(date));
			} catch (Exception e) {
				checker.setRegTime(complete_time + "（时间格式转换失败）");
			}

			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(
						"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { checker.getCmdId(),
								checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
			} else {
				// 采集WiFiState chenxj6 beigin
				try {
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();

					int flag = getStatus
							.testDeviceOnLineStatus(deviceId, corba);
					checker.setOnlineStatus(-1);
					// 设备正在被操作，不能获取节点值
					if (-3 == flag) {
						logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
						checker.setResult(1003);
						checker.setResultDesc("设备不能正常交互");
						logger.warn("return=({})", checker.getReturnXml()); // 打印回参
						return checker.getReturnXml();
					}
					// 设备在线
					if (1 == flag) {
						logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
						checker.setOnlineStatus(1);
						// 采集accessType
						String accessType = UserDeviceDAO.getAccType(deviceId);
						ArrayList<ParameValueOBJ> objLlist = null;
						if (null == accessType) {

							objLlist = corba.getValue(deviceId,
									ACCESS_TYPE_PATH_DEFAULT);
							if (null == objLlist || objLlist.isEmpty()) {
								checker.setResult(1006);
								checker.setResultDesc("采集accessTypes失败");
								return checker.getReturnXml();
							}
							accessType = objLlist.get(0).getValue();
						}
						logger.warn("[{}]采集到的，accessType为：[{}]", deviceId,
								accessType);

						String checkAccessType = null;

						if ("EPON".equals(accessType)) {
							checkAccessType = ".X_CT-COM_WANEponLinkConfig";
						} else if ("GPON".equals(accessType)) {
							checkAccessType = ".X_CT-COM_WANGponLinkConfig";
						} else {
							logger.warn("accessType既不是EPON也不是GPON");
							checker.setResult(1000);
							checker.setResultDesc("accessType既不是EPON也不是GPON");
							logger.warn("return=({})", checker.getReturnXml()); // 打印回参
							return checker.getReturnXml();
						}
						
						// 采集光功率
						if ("GPON".equals(accessType)) {
							gatherPath.add(ststusPatchGPON);
							gatherPath.add(txPowerPatchGPON);
							gatherPath.add(rxPowerGPON);
						}
						else{
							gatherPath.add(ststusPatchEPON);
							gatherPath.add(txPowerPatchEPON);
							gatherPath.add(rxPowerEPON);
						}
						
						// 采集LAN口
						lanStatus(accessType, deviceId, checker);
						String serv = null;
						boolean servFlagIntnet = false;
						boolean servFlagIptv = false;

						//查询型号HG260GS HG261GS 不支持快速采集
						List<String> models = new ArrayList<String>();
						models.add("HG260GS");
						models.add("HG261GS");
						String model = userDevDao.isNotSuportFastGather(deviceId);
						//普通采集
						List<String> servListJ = null;
						int countServB = 0;
						String servListPathI = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
						String servListPathJ = null;
						String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
						String wan_index_result = "";
						
						logger.warn("[{}]获取wan连接索引", deviceId);
						ArrayList<ParameValueOBJ> valueList = new ArrayList<ParameValueOBJ>();
						if (!models.contains(model)){
							valueList = corba.getValue(deviceId, wan_index);
						}
						
						//查询型号HG260GS HG261GS 不支持快速采集
						if (!models.contains(model) && (valueList != null && valueList.size() != 0)) {
							logger.warn("[{}]支持快速采集，获取全部路径",deviceId);
							for (ParameValueOBJ pvobj : valueList) {
								if (pvobj.getName().endsWith("X_CT-COM_WANIndex")) {
									wan_index_result = pvobj.getValue();
									break;
								}
							}
							// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
							if (!StringUtil.IsEmpty(wan_index_result)) {
								String wan[] = wan_index_result.replace("\"", "")
										.split(",");
								for (String wanPa : wan) {
									if((wanPa.endsWith("OTHER") || wanPa.endsWith("IPTV")) && (wanPa.contains(".") && wanPa.contains(";"))){
										String a = wanPa.split(";")[0].split("\\.")[0];
										String b = wanPa.split(";")[0].split("\\.")[1];
										//String vlanid = wanPa.split(";")[2];
										//桥接 路由：ppp ; dhcp static：ip
										String conntype = wanPa.split(";")[1].toLowerCase();
										if(conntype.contains("bridged")){
											servListPathJ = servListPathI + a + ".WANPPPConnection.";
										}
										else if(conntype.contains("dhcp")){
											servListPathJ = servListPathI + a + ".WANIPConnection.";
										}
										else if(conntype.contains("routed")){
											servListPathJ = servListPathI + a + ".WANPPPConnection.";
										}
										else{
											servListPathJ = servListPathI + a + ".WANIPConnection.";
										}
										/*servListPathJ = servListPathI + a + ".WANPPPConnection.";
										
										servListJ = corba.getIList(deviceId, servListPathJ);
										if (null == servListJ || servListJ.isEmpty()) {
											servListJ = corba
													.getIList(deviceId, servListPathI + a
															+ ".WANIPConnection.");
											if (null == servListJ || servListJ.isEmpty()) {
												continue;
											}
											servListPathJ = servListPathI + a + ".WANIPConnection.";
										}*/
										
										countServB++;
										servFlagIptv = true;
										logger.warn("[{}]获取OTHER成功", deviceId);

										// 采集 X_CT-COM_MulticastVlan
										String mulPath = servListPathJ + b
												+ ".X_CT-COM_MulticastVlan";
										String bindPortPath = servListPathJ + b
												+ ".X_CT-COM_LanInterface";
										String connTypePath = servListPathJ + b
												+ ".ConnectionType";
										String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
												+ a
												+ checkAccessType
												+ ".VLANIDMark";

										gatherPath.add(mulPath); 
										gatherPath.add(bindPortPath);
										gatherPath.add(connTypePath);
										gatherPath.add(vlanPath);
										iptvPathList.add(servListPathI + a);
									}
									else if ((wanPa.endsWith("INTERNET") || wanPa.endsWith("internet")) && (wanPa.contains(".") && wanPa.contains(";"))) {
											/*if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed")) {*/
											String a = wanPa.split(";")[0].split("\\.")[0];
											String b = wanPa.split(";")[0].split("\\.")[1];
											String vlanid = wanPa.split(";")[2];
											//桥接 路由：ppp ; dhcp static：ip
											String conntype = wanPa.split(";")[1].toLowerCase();
											if(conntype.contains("bridged")){
												servListPathJ = servListPathI + a + ".WANPPPConnection.";
											}
											else if(conntype.contains("dhcp")){
												servListPathJ = servListPathI + a + ".WANIPConnection.";
											}
											else if(conntype.contains("routed")){
												servListPathJ = servListPathI + a + ".WANPPPConnection.";
											}
											else{
												servListPathJ = servListPathI + a + ".WANIPConnection.";
											}
											
											/*servListPathJ = servListPathI + a + ".WANPPPConnection.";
											servListJ = corba.getIList(deviceId, servListPathJ);
											if (null == servListJ || servListJ.isEmpty()) {
												servListJ = corba
														.getIList(deviceId, servListPathI + a
																+ ".WANIPConnection.");
												if (null == servListJ || servListJ.isEmpty()) {
													continue;
												}
												servListPathJ = servListPathI + a + ".WANIPConnection.";
											}*/
											
											countServB++;
											servFlagIntnet = true;
											logger.warn("[{}]获取INTERNET成功",
													deviceId);

											// 采集 WifiState
											String wifiStatePath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_CT-COM_APModuleEnable";
											
											// 采集 X_CT-COM_LanInterface,绑定端口
											String bindPortPath = servListPathJ + b
													+ ".X_CT-COM_LanInterface";
											// 采集ConnectionType，WanType，上网方式：PPPoE_Bridged
											
											String connTypePath = servListPathJ + b
													+ ".ConnectionType";
											// 采集 VLANIDMark，43等
											String vlanPath = servListPathI
													+ a
													+ checkAccessType
													+ ".VLANIDMark";
											
											gatherPath.add(wifiStatePath); 
											gatherPath.add(bindPortPath); 
											gatherPath.add(connTypePath);
											gatherPath.add(vlanPath);
											wbandPathList.add(servListPathI + a);
											if(servFlagIptv) break;
									}
								}
							}
						}
						else{
							logger.warn("[{}]不支持快速采集，获取全部路径",deviceId);
							//不支持快速采集，获取全部路径
							ArrayList<String> wanConnPathsList = new ArrayList<String>();
							// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
							wanConnPathsList = corba.getParamNamesPath(deviceId, servListPathI, 0);
							logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
							if (wanConnPathsList == null || wanConnPathsList.size() == 0
									|| wanConnPathsList.isEmpty()) {
								return null;
							}
							else{
								ArrayList<String> paramNameList = new ArrayList<String>();
								for (int i = 0; i < wanConnPathsList.size(); i++) {
									String namepath = wanConnPathsList.get(i);
									if (namepath.indexOf(".X_CT-COM_ServiceList") >= 0) {
										paramNameList.add(namepath);
									}
								}
								wanConnPathsList = new ArrayList<String>();
								wanConnPathsList.addAll(paramNameList);
							}
							
							if(wanConnPathsList.size()==0){
								logger.warn("[DevConfigServiceNew] [{}]无X_CT-COM_ServiceList节点：", deviceId);
							}
							
							String[] paramNametemp = new String[wanConnPathsList.size()];
							for(int i=0;i<wanConnPathsList.size();i++){
								paramNametemp[i] = wanConnPathsList.get(i);
							}
							
							Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
									paramNametemp);
							
							if (paramValueMap.isEmpty()) {
								logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败",
										deviceId);
							}
							for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
								logger.debug("[{}]{}={} ",
										new Object[]{deviceId, entry.getKey(), entry.getValue()});
								//InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CT-COM_ServiceList
								if (entry.getValue().indexOf("OTHER") >= 0 || entry.getValue().indexOf("IPTV") >= 0) {
									String a = entry.getKey().split("\\.")[4];
									String b = entry.getKey().split("\\.")[6];
									int index = entry.getKey().indexOf(b+".X_CT-COM_ServiceList");
									servListPathJ = entry.getKey().substring(0, index);
									
									countServB++;
									servFlagIptv = true;
									logger.warn("[{}]获取OTHER成功", deviceId);

									// 采集 X_CT-COM_MulticastVlan
									String mulPath = servListPathJ + b
											+ ".X_CT-COM_MulticastVlan";
									String bindPortPath = servListPathJ + b
											+ ".X_CT-COM_LanInterface";
									String connTypePath = servListPathJ + b
											+ ".ConnectionType";
									String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
											+ a
											+ checkAccessType
											+ ".VLANIDMark";

									gatherPath.add(mulPath); 
									gatherPath.add(bindPortPath);
									gatherPath.add(connTypePath);
									gatherPath.add(vlanPath);
									iptvPathList.add(servListPathI + a);
								}
								else if (entry.getValue().indexOf("INTERNET") >= 0 || entry.getValue().indexOf("internet") >= 0){
									String a = entry.getKey().split("\\.")[4];
									String b = entry.getKey().split("\\.")[6];
									int index = entry.getKey().indexOf(b+".X_CT-COM_ServiceList");
									servListPathJ = entry.getKey().substring(0, index);
									
									countServB++;
									servFlagIntnet = true;
									logger.warn("[{}]获取INTERNET成功",
											deviceId);

									// 采集 WifiState
									String wifiStatePath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_CT-COM_APModuleEnable";
									
									// 采集 X_CT-COM_LanInterface,绑定端口
									String bindPortPath = servListPathJ + b
											+ ".X_CT-COM_LanInterface";
									// 采集ConnectionType，WanType，上网方式：PPPoE_Bridged
									
									String connTypePath = servListPathJ + b
											+ ".ConnectionType";
									// 采集 VLANIDMark，43等
									String vlanPath = servListPathI
											+ a
											+ checkAccessType
											+ ".VLANIDMark";
									
									gatherPath.add(wifiStatePath); 
									gatherPath.add(bindPortPath); 
									gatherPath.add(connTypePath);
									gatherPath.add(vlanPath);
									wbandPathList.add(servListPathI + a);
									if(servFlagIptv) break;
								}
							}
						}
						
						
						if (!servFlagIptv) {
							logger.warn("[{}]获取OTHER失败，没有得到值为OTHER的节点",
									deviceId);
						}

						if (!servFlagIntnet) {
							logger.warn("[{}]获取INTNET失败，没有得到值为INTNET的节点",
									deviceId);
						}

						// 接入方式
						if ("DSL".equals(accessType)) {
							resultMap.put("AccessType", "1");
						} else if ("Ethernet".equals(accessType)) {
							resultMap.put("AccessType", "2");
						} else if ("EPON".equalsIgnoreCase(accessType)
								|| "PON".equalsIgnoreCase(accessType)) {
							resultMap.put("AccessType", "3");
						} else if ("GPON".equalsIgnoreCase(accessType)) {
							resultMap.put("AccessType", "4");
						} else {
							resultMap.put("AccessType", "");
						}

						String[] gatherPathArray = new String[gatherPath.size()];
						gatherPath.toArray(gatherPathArray);
						//处理设备采集结果
						objLlist = corba.getValue(deviceId, gatherPathArray);
						/*for(int i=0;i<gatherPathArray.length;i++){
							logger.warn("第"+(i+1)+"个参数："+gatherPathArray[i]);
						}
						for (ParameValueOBJ pvobj : objLlist) {
							logger.warn(pvobj.getName()+"---"+pvobj.getValue());
						}*/
						
						if (null == objLlist || objLlist.isEmpty()) {
							HashMap<String, String> iptvMap = new HashMap<String, String>();
							iptvMap.put("multicast_vlan", "");
							iptvMap.put("bind_port", "");
							iptvMap.put("conn_type", "");
							iptvMap.put("vlan_id", "");
							iptvlist.add(iptvMap);
							
							HashMap<String, String> internetMap = new HashMap<String, String>();
							internetMap.put("bind_port", "");
							internetMap.put("conn_type", "");
							internetMap.put("vlan_id", "");
							internetMap.put("status", "");
							internetlist.add(internetMap);
							
							logger.warn("[{}]采集失败", new Object[] { deviceId });
							checker.setResult(1003);
							checker.setResultDesc("设备采集失败");
							logger.warn("return=({})", checker.getReturnXml()); // 打印回参
							
							configInfo.put("inter_list", internetlist);

							// IPTV
							configInfo.put("iptv_list", iptvlist);

							checker.setResultMap(resultMap);
							checker.setConfigInfo(configInfo);
							return checker.getReturnXml();
						}
						
						//解析光功率
						ponInfo(accessType, objLlist, deviceId, checker);
						
						//采集lan口
						List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();
						String status = "";
						String received = "";
						String sent = "";
						
						for(int p = 0; p<lani.size(); p++){
							for(ParameValueOBJ pvobj : objLlist){
								if(pvobj.getName().contains("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+lani.get(p)+".Status")){
									status = pvobj.getValue();
								}else if(pvobj.getName().contains("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+lani.get(p)+".Stats.BytesReceived")){
									received = pvobj.getValue();
								}else if(pvobj.getName().contains("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+lani.get(p)+".Stats.BytesSent")){
									sent = pvobj.getValue();
								}
							}
							HashMap<String,String> tmp = new HashMap<String,String>();
							tmp.put("LanPortNUM", lani.get(p));
							tmp.put("RstState", status);
							tmp.put("BytesReceived", received);
							tmp.put("BytesSent", sent);
							lanList.add(tmp);
							tmp = null;
							status = null;
							received = null;
							sent = null;
						}
						checker.setLanList(lanList);
						
						//区分节点名属于iptv还是宽带（单iptv 单宽带）
						
						for(int i=0; i<iptvPathList.size(); i++){
							String iptvPath = iptvPathList.get(i);
							HashMap<String, String> iptvMap = new HashMap<String, String>();
							for (ParameValueOBJ pvobj : objLlist) {
								if(!pvobj.getName().contains(iptvPath)){
									continue;
								}
								if (pvobj.getName().contains("X_CT-COM_MulticastVlan")) {
									iptvMap.put("multicast_vlan", pvobj.getValue());
								}else if (pvobj.getName().contains("X_CT-COM_LanInterface")) {
									iptvMap.put("bind_port", pvobj.getValue());
								}else if (pvobj.getName().contains("ConnectionType")) {
									iptvMap.put("conn_type", pvobj.getValue());
								}else if (pvobj.getName().contains("VLANIDMark")) {
									iptvMap.put("vlan_id", pvobj.getValue());
								}
							}
							iptvlist.add(iptvMap);
						}
						
						for(int i=0; i<wbandPathList.size(); i++){
							String wbandPath = wbandPathList.get(i);
							HashMap<String, String> internetMap = new HashMap<String, String>();
							for (ParameValueOBJ pvobj : objLlist) {
								if(!pvobj.getName().contains(wbandPath)){
									continue;
								}
								if (pvobj.getName().contains("X_CT-COM_LanInterface")) {
									internetMap.put("bind_port", pvobj.getValue());
								}else if (pvobj.getName().contains("ConnectionType")) {
									internetMap.put("conn_type", pvobj.getValue());
								}else if (pvobj.getName().contains("VLANIDMark")) {
									internetMap.put("vlan_id", pvobj.getValue());
								}else if (pvobj.getName().contains("X_CT-COM_APModuleEnable")) {
									internetMap.put("status", pvobj.getValue());
								}
								
							}
							internetlist.add(internetMap);
						}
						
						
						// 上网业务
						configInfo.put("inter_list", internetlist);

						// IPTV
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[DevConfigService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						returnXml });

		return returnXml;
	}
	
	private void ponInfo(String accessType, ArrayList<ParameValueOBJ> objLlist, String deviceId, DevConfigCheckerNew checker) {
		String status = "";
		String tx_power = "0";
		String rx_power = "0";

		logger.warn("ponInfo|device_id={},objList:{}",deviceId,objLlist);
		if (null == objLlist || objLlist.isEmpty()) {
			checker.setResult(1006);
			checker.setResultDesc("设备采集失败");
			return;
		}

		if ("GPON".equals(accessType)){
			for (ParameValueOBJ pvobj : objLlist) {
				if (pvobj.getName().contains(ststusPatchGPON)) {
					// 线路状态
					status = pvobj.getValue();
				} else if (pvobj.getName().contains(txPowerPatchGPON)) {
					// 发射光功率
					tx_power = pvobj.getValue();
				} else if (pvobj.getName().contains(rxPowerGPON)) {
					// 接收光功率
					rx_power = pvobj.getValue();
				}
			}
		}
		else{
			for (ParameValueOBJ pvobj : objLlist) {
				if (pvobj.getName().contains(ststusPatchEPON)) {
					// 线路状态
					status = pvobj.getValue();
				} else if (pvobj.getName().contains(txPowerPatchEPON)) {
					// 发射光功率
					tx_power = pvobj.getValue();
				} else if (pvobj.getName().contains(rxPowerEPON)) {
					// 接收光功率
					rx_power = pvobj.getValue();
				}
			}
		}
			
		
		logger.warn("[{}]status[{}]txpower[{}]rxPower[{}]", new Object[] {
				deviceId, status, tx_power, rx_power });
		double tx_powerdouble = StringUtil.getDoubleValue(tx_power);
		double rx_powerdouble = StringUtil.getDoubleValue(rx_power);
		// 发射光功率
		if (tx_powerdouble > 30) {
			double temp_tx_power = (Math.log(tx_powerdouble / 10000) / Math
					.log(10)) * 10;
			tx_powerdouble = (int) temp_tx_power;
			if (tx_powerdouble % 10 >= 5) {
				tx_powerdouble = (tx_powerdouble / 10 + 1) * 10;
			} else {
				tx_powerdouble = tx_powerdouble / 10 * 10;
			}
		}
		// 接受功率判断
		if (rx_powerdouble > 30) {
			double temp_rx_power = (Math.log(rx_powerdouble / 10000) / Math
					.log(10)) * 10;
			rx_powerdouble = (int) temp_rx_power;
			if (rx_powerdouble % 10 >= 5) {
				rx_powerdouble = (rx_powerdouble / 10 + 1) * 10;
			} else {
				rx_powerdouble = rx_powerdouble / 10 * 10;
			}
		}
		checker.setStatus(status);
		checker.setRXPower(StringUtil.getStringValue(rx_powerdouble));
		checker.setTXPower(StringUtil.getStringValue(tx_powerdouble));
	}
	
	private void lanStatus(String accessType, String deviceId,DevConfigCheckerNew checker)
	{
		    String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		    List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return;
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}

			for(String i : iList){
				lani.add(i);
				gatherPath.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status");
				gatherPath.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesReceived");
				gatherPath.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesSent");
				
				/*ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || "".equals(objLlist)) {
					continue;
				}
				
				String status = "";
				String received = "";
				String sent = "";
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						status = pvobj.getValue();
					}else if(pvobj.getName().contains("BytesReceived")){
						received = pvobj.getValue();
					}else if(pvobj.getName().contains("BytesSent")){
						sent = pvobj.getValue();
					}
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp.put("LanPortNUM", i);
				tmp.put("RstState", status);
				tmp.put("BytesReceived", received);
				tmp.put("BytesSent", sent);
				lanList.add(tmp);
				tmp = null;
				status = null;
				received = null;
				sent = null;*/
			}
			//checker.setLanList(lanList);
	}
		
	
		
		
			
	
	

	
	private String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
	private String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
	private String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
	private String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
	private String WLAN1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
	private String WLAN2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
	private String WLAN3 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3";
	private String WLAN4 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4";

	public DevConfigServiceNew4NX()
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
	
	private String getPingInterface(String deviceId)
	{
		ACSCorba corba = new ACSCorba();
		String SERV_LIST_INTERNET = "INTERNET";
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String SERV_LIST_VOIP = "INTERNET";
		
		
		// 江苏可以根据wan连接索引节点来生成上网通道
		String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
		String wan_index_result = "";
		logger.warn("[{}]获取wan连接索引", deviceId);
		ArrayList<ParameValueOBJ> valueList = corba.getValue(deviceId,
				wan_index);
		if (valueList != null && valueList.size() != 0) {
			for (ParameValueOBJ pvobj : valueList) {
				if (pvobj.getName().endsWith("X_CT-COM_WANIndex")) {
					wan_index_result = pvobj.getValue();
					break;
				}
			}
			// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
			if (!StringUtil.IsEmpty(wan_index_result)) {
				String wan[] = wan_index_result.replace("\"", "")
						.split(",");
				for (String wanPa : wan) {
					if (wanPa.endsWith(SERV_LIST_INTERNET)
							|| wanPa.endsWith("internet")) {
						if (wanPa.contains(".") && wanPa.contains(";")) {
							if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed")) {
								String a = wanPa.split(";")[0].split("\\.")[0];
								String b = wanPa.split(";")[0].split("\\.")[1];
								String vlanid = wanPa.split(";")[2];
								String result = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
										+ a
										+ ".WANPPPConnection."
										+ b
										+ "";
								return result;
							}
							
						}

					}
				}
			}
		}
		
		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
		
		//直接采集路径名
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QueryVOIPWanInfoService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
				return null;
			}
			
			
			for (String j : jList) {
				// 获取wanPPPConnection下的k
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0
						|| kPPPList.isEmpty()) {
					
					wanConnPathsList.add(wanConnPath + j + wanIPConnection
									+ "1" + wanServiceList);
				} else {
					for (String kppp : kPPPList) {
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
					}
				}
			}
		}
		else{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++) {
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(wanServiceList) >= 0 ) {
					paramNameList.add(namepath);
				}
			}
			wanConnPathsList = new ArrayList<String>();
			wanConnPathsList.addAll(paramNameList);
		}
		
		if(wanConnPathsList.size()==0){
			logger.warn("[QueryVOIPWanInfoService] [{}]无节点：" + wanConnPath+".j.wanPPPConnection/wanIPConnection."+wanServiceList
					+ "下实例号失败，返回", deviceId);
			return null;
		}
		
		String[] paramNametemp = new String[wanConnPathsList.size()];
		for(int i=0;i<wanConnPathsList.size();i++){
			paramNametemp[i] = wanConnPathsList.get(i);
		}
		
		Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
				paramNametemp);
		
		if (paramValueMap.isEmpty()) {
			logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败",
					deviceId);
		}
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			logger.debug("[{}]{}={} ",
					new Object[]{deviceId, entry.getKey(), entry.getValue()});
			//语音节点
			if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
				return entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList));
			}
		}
		return null;
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