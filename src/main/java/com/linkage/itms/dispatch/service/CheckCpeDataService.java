package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.CheckCpeDataDAO;
import com.linkage.itms.dispatch.obj.CheckCpeDataChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 新疆电信：ITMS检查光猫cpe口数据配置是否正确，采集cpe的vlan配置是否正确
 * 
 * @author chenxj6
 * @date 2016-11-08
 * @param param
 * @return
 */
public class CheckCpeDataService implements IService {

	private static final Logger logger = LoggerFactory.getLogger(CheckCpeDataService.class);

	@Override
	public String work(String inParam) {
		logger.warn("CheckCpeDataService==>inParam:" + inParam);
		CheckCpeDataChecker checker = new CheckCpeDataChecker(inParam);
		if (false == checker.check()) {
			logger.warn(
					"检查光猫cpe数据配置是否正确，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),
							checker.getUserInfo() });
			logger.warn("CheckCpeDataService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}

		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		String userId = "";

		List<HashMap<String, String>> userMapList = null;
		List<HashMap<String, String>> deviceMapList = null;
		CheckCpeDataDAO dao = new CheckCpeDataDAO();

		if (checker.getUserInfoType() == 1) {
			userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 2) {
			userMapList = dao.queryUserByLoid(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 3) {
			userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 4) {
			userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 5) {
			userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
		}

		if (userMapList == null || userMapList.isEmpty()) {
			logger.warn("查无此客户");
			checker.setResult(1000);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}

		String devSn = checker.getDevSn();
		if (devSn == null || devSn.trim().length() == 0) {
			if (userMapList.size() > 1) {
				logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			} else {
				deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
				userId = StringUtil.getStringValue(userMapList.get(0),"user_id", "");
				if (StringUtil.IsEmpty(deviceId)) {
					logger.warn("用户未绑定设备");
					checker.setResult(1002);
					checker.setResultDesc("用户未绑定设备");
					return checker.getReturnXml();
				}
			}
		} else {
			devSn = devSn.trim();
			if (devSn.length() < 6) {
				logger.warn("按设备序列号查询时，查询序列号字段少于6位");
				checker.setResult(1005);
				checker.setResultDesc("设备序列号非法");
				return checker.getReturnXml();
			} else {
				deviceMapList = dao.queryDeviceByDevSN(devSn);
				if (deviceMapList == null || deviceMapList.size() == 0) {
					logger.warn("没有查到设备");
					checker.setResult(1000);
					checker.setResultDesc("没有查到设备");
					return checker.getReturnXml();
				} else if (deviceMapList.size() > 1) {
					logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					checker.setResult(1006);
					checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					return checker.getReturnXml();
				} else {
					deviceId = StringUtil.getStringValue(deviceMapList.get(0),"device_id", "");
					if(dao.queryUserId(devSn) != null){
						userId = StringUtil.getStringValue(dao.queryUserId(devSn).get("user_id"));
					}else{
						logger.warn("根据用户设备SN查询user_id返回结果为空");
					}
					
					boolean flagTemp = false;
					for (HashMap<String, String> userMap : userMapList) {
						if (userMap.containsValue(deviceId)) {
							flagTemp = true;
							break;
						}
					}
					if (false == flagTemp) {
						logger.warn("用户未绑定该设备");
						checker.setResult(1000);
						checker.setResultDesc("用户未绑定该设备");
						return checker.getReturnXml();
					}
				}
			}
		}
		
		if (null == userId || userId.trim().length() == 0) {
			logger.warn("userId查询结果为空，userId[{}]", userId);
			checker.setResult(1000);
			checker.setResultDesc("userId查询结果为空");
			logger.warn("return=({})", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		List<HashMap<String, String>> servExistList = dao.getServExistList(StringUtil.getLongValue(userId));
		
		if (null == servExistList || servExistList.size() == 0) {
			logger.warn("用户没有开通 IPTV，宽带，语音业务，userId[{}]", userId);
			checker.setResult(0);
			checker.setResultDesc("用户没有开通 IPTV，宽带，语音业务");
			logger.warn("return=({})", checker.getReturnXml());
			return checker.getReturnXml();
		}

		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);

		logger.warn("设备[{}],在线状态[{}] ", new Object[] { deviceId, flag });

		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
			String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String vlan45Path = "";
			String vlan41Path = "";
			String vlan43Path = "";
			String errResult = "";
			String errResult41 = "";
			String errResult43 = "";

			List<String> iList = corba.getIList(deviceId, vlanPath);
			if (null == iList || iList.isEmpty()) {
				logger.warn("[{}]获取iList失败,iList为空", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			} else {
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}

			String accessType = dao.getAccessType(deviceId);
			logger.warn("[{}]数据库中，accessType为：[{}]",deviceId, accessType);

			// 以下获取的方式是新疆专用
			if (null == accessType) {
				String[] gatherPath = new String[] { "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType" };
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,gatherPath);

				if (objLlist != null && objLlist.size() != 0) {
					for (ParameValueOBJ pvobj : objLlist) {
						if (pvobj.getName().endsWith("WANAccessType")) {
							accessType = pvobj.getValue();
						}
					}
				}

				if (null != accessType && !"null".equals(accessType)
						&& !"".equals(accessType)) {
					dao.insertWan(deviceId, "1", accessType);
				}

				logger.warn("采集到的，accessType为：[{}]", accessType);
			}

			String checkAccessType = "";
			String checkAccessTypeVLANIDMARK = "";

			if ("EPON".equals(accessType)) {
				checkAccessType = ".X_CT-COM_WANEponLinkConfig.VLANID";
				checkAccessTypeVLANIDMARK = ".X_CT-COM_WANEponLinkConfig.VLANIDMark";
			} else if ("GPON".equals(accessType)) {
				checkAccessType = ".X_CT-COM_WANGponLinkConfig.VLANID";
				checkAccessTypeVLANIDMARK = ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
			} else {
				logger.warn("accessType既不是EPON也不是GPON");
				checker.setResult(1000);
				checker.setResultDesc("accessType既不是EPON也不是GPON");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}

			for (String i : iList) {

				String[] gatherPath = new String[] { "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + checkAccessType };

				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					gatherPath = new String[] { "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + checkAccessTypeVLANIDMARK };
					objLlist = corba.getValue(deviceId, gatherPath);
					if (null == objLlist || objLlist.isEmpty()) {
						continue;
					}
				}

				String vlanId = "";
				for (ParameValueOBJ pvobj : objLlist) {
					if (pvobj.getName().contains("VLANID") || pvobj.getName().contains("VLANIDMark")) {
						vlanId = pvobj.getValue();
					}
				}

				if ("45".equals(vlanId)) {
					vlan45Path = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + ".WANPPPConnection.";
				}

				if ("41".equals(vlanId)) {
					vlan41Path = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + ".WANPPPConnection.";
				}

				if ("43".equals(vlanId)) {
					vlan43Path = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + ".WANPPPConnection.";
				}
			}
			
			// New Begin
			
			// 做成单IPTV，单VOIP，单宽带模式 ; ( 宽带：10,41 ； iptv：11,45 ； voip：14,43 ) user_id, serv_type_id, username,vlanId 
			HashMap<String, String> mapIptv = null;
			HashMap<String, String> mapVoip = null;
			HashMap<String, String> mapNet = null;
			
			for(HashMap<String, String> map : servExistList){
				if(map!=null){
					if("11".equals(map.get("serv_type_id"))){
						mapIptv = map;
					} else if("14".equals(map.get("serv_type_id"))){
						mapVoip = map;
					} else if("10".equals(map.get("serv_type_id"))){
						mapNet = map;
					} 
				}
			}
			
			// vlan45 Begin
			if(mapIptv==null || mapIptv.size()==0){
				logger.warn("[{}]用户没有开通iptv业务，userId[{}]",deviceId, userId);
				checker.setErrCode45(4);
				errResult += "用户没有开通iptv业务; ";
			} else {
				if ("".equals(vlan45Path)) {
					logger.warn("[{}]VLAN节点采集结果都不是45",deviceId);
					errResult += "VLAN节点采集结果都不是45; ";
				}
//				else{
//					// "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.";
//					List<String> vlan45List = corba.getIList(deviceId, vlan45Path);
//					if (null == vlan45List || vlan45List.isEmpty()) {
//						logger.warn("[{}]获取vlan45List失败，vlan45Path:[{}]", deviceId,vlan45Path);
//						errResult += "vlan是45的节点采集为空，请确认节点路径是否正确 ; ";
//					} else {
//						logger.warn("[{}]获取vlan45List成功，vlan45List.size={}", deviceId,vlan45List.size());
//						String LanInterface = "";
//						String ServiceList = "";
//						String LanInterfaceDHCPEnable = "";
//
//						for (String i : vlan45List) {
//							String[] gatherPath = new String[] {
//									vlan45Path + i + ".X_CT-COM_LanInterface",
//									vlan45Path + i + ".X_CT-COM_ServiceList",
//									vlan45Path + i + ".X_CT-COM_LanInterface-DHCPEnable" };
//							ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,gatherPath);
//							if (null == objLlist || "".equals(objLlist)) {
//								continue;
//							}
//
//							for (ParameValueOBJ pvobj : objLlist) {
//								if (pvobj.getName().endsWith("LanInterface")) {
//									LanInterface = pvobj.getValue();
//								} else if (pvobj.getName().endsWith("ServiceList")) {
//									ServiceList = pvobj.getValue();
//								} else if (pvobj.getName().endsWith("LanInterface-DHCPEnable")) {
//									LanInterfaceDHCPEnable = pvobj.getValue();
//								}
//							}
//						}
//
//						logger.warn("LanInterface 采集结果为: [{}]", LanInterface);
//						logger.warn("ServiceList 采集结果为: [{}]", ServiceList);
//						logger.warn("LanInterfaceDHCPEnable 采集结果为: [{}]", LanInterfaceDHCPEnable);
//
//						
//						
//						if (LanInterface == null
//								|| !LanInterface
//										.contains("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2")) {
//							errResult += ".X_CT-COM_LanInterface采集结果有误; ";
//						}
//						if (!"OTHER".equals(ServiceList)) {
//							errResult += ".X_CT-COM_ServiceList采集结果有误; ";
//						}
//						if (!"0".equals(LanInterfaceDHCPEnable)) {
//							errResult += ".X_CT-COM_LanInterface-DHCPEnable采集结果有误; ";
//						}
//
//
//						String[] gatherPath = new String[] {
//								"InternetGatewayDevice.Services.X_CT-COM_IPTV.IGMPEnable",
//								"InternetGatewayDevice.Services.X_CT-COM_IPTV.ProxyEnable",
//								"InternetGatewayDevice.Services.X_CT-COM_IPTV.SnoopingEnable" };
//
//						ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,gatherPath);
//						if (null == objLlist || "".equals(objLlist)) {
//							errResult += ".X_CT-COM_IPTV.没有采到结果; ";
//						} else {
//							String iGMPEnable = "";
//							String proxyEnable = "";
//							String snoopingEnable = "";
//							
//							for (ParameValueOBJ pvobj : objLlist) {
//								if (pvobj.getName().endsWith("IGMPEnable")) {
//									iGMPEnable = pvobj.getValue();
//								} else if (pvobj.getName().endsWith("ProxyEnable")) {
//									proxyEnable = pvobj.getValue();
//								} else if (pvobj.getName().endsWith("SnoopingEnable")) {
//									snoopingEnable = pvobj.getValue();
//								}
//							}
//							
//							logger.warn("[{}] iGMPEnable 采集结果为: [{}]",deviceId, iGMPEnable);
////							logger.warn("proxyEnable 采集结果为: [{}]", proxyEnable);
//							logger.warn("[{}] snoopingEnable 采集结果为: [{}]", deviceId,snoopingEnable);
//
//							if (!"1".equals(iGMPEnable)) {
//								errResult += ".IGMPEnable未启用; ";
//							}
////							if (!"1".equals(proxyEnable)) {
////								errResult += ".ProxyEnable采集结果有误; ";
////							}
//							if (!"1".equals(snoopingEnable)) {
//								errResult += ".SnoopingEnable采集结果有误; ";
//							}
//						}
//					}
//				}
			}
			// vlan45 End
						
			// vlan41 Begin
			if(mapNet==null || mapNet.size()==0){
				logger.warn("用户没有开通宽带业务，userId[{}]", userId);
				checker.setErrCode41(4);
				errResult41 += "用户没有开通宽带业务; ";
			} else {
				if ("".equals(vlan41Path)) {
					logger.warn("VLAN节点采集结果都不是41");
					errResult41 += "VLAN节点采集结果都不是41; ";
				}
//				else{
//					List<String> vlan41List = corba.getIList(deviceId, vlan41Path);
//					boolean hasVlan41List = false;
//					if (null == vlan41List || vlan41List.isEmpty()) {
//						vlan41Path = vlan41Path.replaceAll("WANPPPConnection","WANIPConnection");
//						vlan41List = corba.getIList(deviceId, vlan41Path);
//						if (null == vlan41List || vlan41List.isEmpty()) {
//							logger.warn("[{}]获取vlan41List失败，vlan41Path:[{}]", deviceId,vlan41Path);
//							errResult41 += "vlan是41的节点采集为空，请确认节点路径是否正确; ";
//						} else {
//							logger.warn("[{}]获取vlan41List成功，vlan41List.size={}", deviceId, vlan41List.size());
//							hasVlan41List = true;
//						}
//					} else {
//						logger.warn("[{}]获取vlan41List成功，vlan41List.size={}", deviceId, vlan41List.size());
//						hasVlan41List = true;
//					}
//					
//					if(hasVlan41List){
//						String Mode41 = "";
//						String VLANIDMark41 = "";
//						String ConnectionType41 = "";
//						String LanInterface41 = "";
//						String ServiceList41 = "";
//						String Username41 = "";
//						String Password41 = "";
//						String Enable41 = "";
//						String WANConnectionDevice41 = "";
//						if (vlan41Path.contains("WANPPPConnection")) {
//							WANConnectionDevice41 = vlan41Path.replace(".WANPPPConnection.", "");
//						} else {
//							WANConnectionDevice41 = vlan41Path.replace(".WANIPConnection.","");
//						}
//						// checkAccessTypeVLANIDMARK =
//						// ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
//						String checkAccessType41 = checkAccessTypeVLANIDMARK.replace("VLANIDMark", "");
//
//						String[] gatherPath41Mode_VLANIDMark = new String[] {
//								WANConnectionDevice41 + checkAccessTypeVLANIDMARK,
//								WANConnectionDevice41 + checkAccessType,
//								WANConnectionDevice41 + checkAccessType41 + "Mode" };
//						ArrayList<ParameValueOBJ> objLlist41Mode_VLANIDMark = corba.getValue(deviceId, gatherPath41Mode_VLANIDMark);
//						if (null != objLlist41Mode_VLANIDMark && !objLlist41Mode_VLANIDMark.isEmpty()) {
//							for (ParameValueOBJ pvobj : objLlist41Mode_VLANIDMark) {
//								if (pvobj.getName().endsWith("Mode")) {
//									Mode41 = pvobj.getValue();
//								} else if (pvobj.getName().endsWith("VLANIDMark") || pvobj.getName().endsWith("VLANID")) {
//									if(pvobj.getValue()!=null && pvobj.getValue().trim().length()!=0){
//										VLANIDMark41 = pvobj.getValue();
//									}
//								}
//							}
//						}
//						// vlan41Path =
//						// "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.";
//						if (null != vlan41List && !vlan41List.isEmpty()) {
//							for (String i : vlan41List) {
//								String[] gatherPath41 = new String[] {
//										vlan41Path + i + ".ConnectionType",
//										vlan41Path + i + ".X_CT-COM_LanInterface",
//										vlan41Path + i + ".X_CT-COM_ServiceList",
//										vlan41Path + i + ".Username",
//										vlan41Path + i + ".Password",
//										vlan41Path + i + ".Enable" };
//								ArrayList<ParameValueOBJ> objLlist41 = corba.getValue(deviceId, gatherPath41);
//								if (null == objLlist41 || objLlist41.size()==0) {
//									continue;
//								}
//								for (ParameValueOBJ pvobj : objLlist41) {
//									if (pvobj.getName().endsWith("X_CT-COM_LanInterface")) {
//										LanInterface41 = pvobj.getValue();
//									} else if (pvobj.getName().endsWith("X_CT-COM_ServiceList")) {
//										ServiceList41 = pvobj.getValue();
//									} else if (pvobj.getName().endsWith("ConnectionType")) {
//										ConnectionType41 = pvobj.getValue();
//									} else if (pvobj.getName().endsWith("Username")) {
//										Username41 = pvobj.getValue();
//									} else if (pvobj.getName().endsWith("Password")) {
//										Password41 = pvobj.getValue();
//									} else if (pvobj.getName().endsWith("Enable")) {
//										Enable41 = pvobj.getValue();
//									}
//								}
//							}
//						}
//
//						logger.warn("[{}]Mode41 采集结果为: [{}]",deviceId, Mode41);
//						logger.warn("[{}]VLANIDMark41 采集结果为: [{}]",deviceId, VLANIDMark41);
//						logger.warn("[{}]LanInterface41 采集结果为: [{}]",deviceId, LanInterface41);
//						logger.warn("[{}]ServiceList41 采集结果为: [{}]",deviceId, ServiceList41);
//						logger.warn("[{}]ConnectionType41 采集结果为: [{}]",deviceId, ConnectionType41);
//						logger.warn("[{}]Username41 采集结果为: [{}]",deviceId, Username41);
//						logger.warn("[{}]Password41 采集结果为: [{}]", deviceId,Password41);
//						logger.warn("[{}]Enable41 采集结果为: [{}]", deviceId,Enable41);
//
//						if (Mode41 == null || "".equals(Mode41)) {
//							errResult41 += ".Mode采集结果为空; ";
//						} else if (!"1".equals(Mode41) && !"2".equals(Mode41)) {
//							errResult41 += ".Mode未启用（VLAN标志未启用）; ";
//						} 
//
//						if (!"41".equals(VLANIDMark41)) {
//							errResult41 += ".VLANIDMark采集结果有误; ";
//						}
//						
//						if (LanInterface41 == null || LanInterface41.isEmpty()) {
//							errResult41 += ".X_CT-COM_LanInterface采集结果为空; ";
//						} else {
//							HashMap<String, String> userNetInfoMap = dao.queryServParamInfo(StringUtil.getLongValue(userId),10);
//							if(userNetInfoMap==null || userNetInfoMap.isEmpty()){
//								errResult41 += ".宽带参数表中查询结果为空; ";
//							}else{
//								String bind_port = userNetInfoMap.get("bind_port");
//								logger.warn("[{}] bind_port 数据库查询结果: [{}]",deviceId, bind_port);
//								if(bind_port==null || bind_port.trim().length()==0){
//									errResult41 += "bind_port数据库查询结果为空; ";
//								}else{
//									String[] dataArr =  bind_port.split(",");
//									String[] gatherArr =  LanInterface41.split(",");
//									StringBuffer dataSb = new StringBuffer();
//									StringBuffer gatherSb = new StringBuffer();
//									String dataLanInterface = "";
//									String gatherLanInterface = "";
//									
//									for(int i=0;i<dataArr.length;i++){
//										if(dataArr[i].endsWith(".")){
//											dataArr[i] = dataArr[i].substring(0, dataArr[i].length()-1);
//										}
//									}
//									
//									for(int i=0;i<gatherArr.length;i++){
//										if(gatherArr[i].endsWith(".")){
//											gatherArr[i] = gatherArr[i].substring(0, gatherArr[i].length()-1);
//										}
//									}
//									
//									Arrays.sort(dataArr);
//									Arrays.sort(gatherArr);
//									
//									for(String str : dataArr){
//										dataSb.append(str + ",");
//									}
//									for(String str : gatherArr){
//										gatherSb.append(str + ",");
//									}
//									
//									dataLanInterface = dataSb.toString();
//									gatherLanInterface = gatherSb.toString();
//									
//									logger.warn("[{}]dataLanInterface: [{}]",deviceId,dataLanInterface);
//									logger.warn("[{}]gatherLanInterface: [{}]",deviceId,gatherLanInterface);
//									
//									if(!dataLanInterface.equals(gatherLanInterface)){
//										errResult41 += ".X_CT-COM_LanInterface采集结果有误; ";
//									}
//								}
//							}
//						}
//						
//						
//						if (!"INTERNET".equals(ServiceList41)) {
//							errResult41 += ".X_CT-COM_ServiceList采集结果有误; ";
//						}
//
//						// 桥接：1 路由：2
//						HashMap<String, String> wanTypeMap = dao.getWanType(deviceId);
//						String wanType = "";
//						if (null == wanTypeMap || null == wanTypeMap.get("wan_type") || wanTypeMap.get("wan_type").trim().length()==0) {
//							errResult41 += "用户信息表中没有wantype的查询结果; ";
//						} else {
//							if ("1".equals(wanTypeMap.get("wan_type"))) {
//								wanType = "PPPoE_Bridged";
//							} else if ("2".equals(wanTypeMap.get("wan_type"))) {
//								wanType = "IP_Routed";
//							}
//							if (!wanType.equals(ConnectionType41)) {
//								errResult41 += ".ConnectionType采集结果有误; ";
//							} else {
//								if("IP_Routed".equals(wanType)){
//									HashMap<String, String> netInfoMap = dao.getNetInfo(deviceId, userId); 
//									logger.warn("用户宽带账号，宽带密码数据库查询结果为：[{}]",netInfoMap);
//									if(null==netInfoMap){
//										errResult41 += "用户宽带账号，宽带密码数据库查询结果为空; ";
//									} else {
//										String netUsername = netInfoMap.get("username");
//										String netPassword = netInfoMap.get("passwd");
//										
//										if (Username41 == null || !Username41.equals(netUsername)) {
//											logger.warn("宽带账号采集结果为空或者与数据库不匹配，采集结果为:[{}]，数据库查询结果为：[{}]",new Object[]{Username41,netUsername});
//											errResult41 += "宽带账号采集结果为空或者与数据库不匹配; ";
//										}
//										
//										if (Password41 == null || !Password41.equals(netPassword)) {
//											logger.warn("宽带密码采集结果为空或者与数据库不匹配，采集结果为:[{}]，数据库查询结果为：[{}]",new Object[]{Password41,netPassword});
//											errResult41 += "宽带密码采集结果为空或者与数据库不匹配; ";
//										}
//									}
//								}
//							}
//						}
//						
//						logger.warn("数据库中 wanType:[{}],采集到的 ConnectionType41:[{}]",wanType,ConnectionType41);
//
//						if (!"1".equals(Enable41)) {
//							errResult41 += ".Enable采集结果不为true; ";
//						}
//					}
//				}
			}			
			// vlan41 End
						
			// vlan43 Begin
			if(mapVoip==null || mapVoip.size()==0){
				logger.warn("[{}]用户没有开通语音业务，userId[{}]",deviceId, userId);
				checker.setErrCode43(4);
				errResult43 += "用户没有开通语音业务; ";
			} else {
				if ("".equals(vlan43Path)) {
					logger.warn("[{}]VLAN节点采集结果都不是43",deviceId);
					errResult43 += "VLAN节点采集结果都不是43; ";
				}else{
					// vlan43Path =
					// "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.";
					List<String> vlan43List = corba.getIList(deviceId, vlan43Path);
					boolean hasVlan43List = false;
					if (null == vlan43List || vlan43List.isEmpty()) {
						vlan43Path = vlan43Path.replace("WANPPPConnection", "WANIPConnection");
						vlan43List = corba.getIList(deviceId, vlan43Path);
						if (null == vlan43List || vlan43List.isEmpty()) {
							logger.warn("[{}]获取vlan43List失败，vlan43Path:[{}]", deviceId,vlan43Path);
							errResult43 += "vlan是43的节点采集为空，请确认节点路径是否正确; ";
						} else {
							logger.warn("[{}]获取vlan43List成功，vlan43List.size={}",deviceId, vlan43List.size());
							hasVlan43List = true;
						}
					} else {
						logger.warn("[{}]获取vlan43List成功，vlan43List.size={}", deviceId,vlan43List.size());
						hasVlan43List = true;
					}

					if(hasVlan43List){
						String ServiceList43 = "";
						if (null != vlan43List && !vlan43List.isEmpty()) {
							for (String i : vlan43List) {
								String[] gatherPath43 = new String[] { vlan43Path + i + ".X_CT-COM_ServiceList" };
								ArrayList<ParameValueOBJ> objLlist43 = corba.getValue(deviceId, gatherPath43);
								if (null == objLlist43 || objLlist43.isEmpty()) {
									continue;
								}

								for (ParameValueOBJ pvobj : objLlist43) {
									if (pvobj.getName().endsWith("ServiceList")) {
										ServiceList43 = pvobj.getValue();
									}
								}
							}
						}

						logger.warn("[{}]ServiceList43 采集结果为: [{}]",deviceId, ServiceList43);

						if (!"VOIP".equals(ServiceList43)) {
							errResult43 += ".X_CT-COM_ServiceList采集结果有误; ";
						}

						HashMap<String, String> voipMap = (HashMap<String, String>) dao.getVoipUserInfo(deviceId);
						String voip_username = "";
						String voip_passwd = "";
						String sip_id = "";
						String uri = "";
						if (null == voipMap || voipMap.size() == 0) {
							logger.warn("设备没有VOIP查询结果");
							errResult43 += "设备没有VOIP查询结果; ";
						} else {
							voip_username = voipMap.get("voip_username"); // *
							voip_passwd = voipMap.get("voip_passwd"); // *
							sip_id = voipMap.get("sip_id");
							uri = voipMap.get("uri"); // *
						}

						HashMap<String, String> sipInfo = (HashMap<String, String>) dao.getSipInfo(sip_id);
						String prox_serv = "";
						String prox_port = "";
						String stand_prox_serv = "";
						String stand_prox_port = "";
						String regi_serv = "";
						String regi_port = "";
						String stand_regi_serv = "";
						String stand_regi_port = "";
						String out_bound_proxy = "";
						String out_bound_port = "";
						String stand_out_bound_proxy = "";
						String stand_out_bound_port = "";
						if (null == sipInfo || sipInfo.size() == 0) {
							logger.warn("设备没有SIP查询结果");
							errResult43 += "设备没有SIP查询结果; ";
						} else {
							prox_serv = sipInfo.get("prox_serv");
							prox_port = sipInfo.get("prox_port"); // 协议类型
							stand_prox_serv = sipInfo.get("stand_prox_serv");
							stand_prox_port = sipInfo.get("stand_prox_port");
							regi_serv = sipInfo.get("regi_serv");
							regi_port = sipInfo.get("regi_port");
							stand_regi_serv = sipInfo.get("stand_regi_serv");
							stand_regi_port = sipInfo.get("stand_regi_port");
							out_bound_proxy = sipInfo.get("out_bound_proxy");
							out_bound_port = sipInfo.get("out_bound_port");
							stand_out_bound_proxy = sipInfo.get("stand_out_bound_proxy");
							stand_out_bound_port = sipInfo.get("stand_out_bound_port");

						}

						// 采集 begin
						String voipServicePathI = "InternetGatewayDevice.Services.VoiceService.";

						// 6 业务电话号码
						// 7 属地
						// 8 VOIP认证帐号 *
						// 9 VOIP认证密码 *
						// 10 主ProxyServer *
						// 11 主ProxyServerPort *
						// 12 备ProxyServer *
						// 13 备ProxyServerPort *
						// 14 语音端口
						// 15 主RegistrarServer *
						// 16 主RegistrarServerPort *
						// 17 备RegistrarServer *
						// 18 备RegistrarServerPort *
						// 19 主OutboundProxy *
						// 20 主OutboundProxyPort *
						// 21 备OutboundProxy *
						// 22 备OutboundProxyPort *
						// 23 协议类型

						String ServerType43 = "";
						String ProxyServer43 = "";
						String ProxyServerPort43 = "";
						String StandbyProxyServer43 = "";
						String StandbyProxyServerPort43 = "";
						String RegistrarServer43 = "";
						String RegistrarServerPort43 = "";
						String StandbyRegistrarServer43 = "";
						String StandbyRegistrarServerPort43 = "";
						String OutboundProxy43 = "";
						String OutboundProxyPort43 = "";
						String StandbyOutboundProxy43 = "";
						String StandbyOutboundProxyPort43 = "";
						String Enable43 = "";
						String AuthUserName43 = "";
						String AuthPassword43 = "";
						String URI43 = "";

						List<String> voipServiceListI = corba.getIList(deviceId,voipServicePathI);
						if (null == voipServiceListI || voipServiceListI.isEmpty()) {
							logger.warn(voipServicePathI + "节点没有采集结果");
							errResult43 += voipServicePathI + "节点没有采集结果; ";
						} else {
							logger.warn("[{}]获取" + voipServicePathI
									+ "节点采集结果voipServiceListI成功，voipServiceListI.size=[{}]",
									deviceId, voipServiceListI.size());
							for (String j : voipServiceListI) {
								String voipServicePathJ = voipServicePathI + j + ".VoiceProfile.";
								List<String> voipServiceListJ = corba.getIList(deviceId,voipServicePathJ);
								if (null == voipServiceListJ || voipServiceListJ.isEmpty()) {
									logger.warn(voipServicePathJ + "节点没有采集结果");
									errResult43 += voipServicePathJ + "节点没有采集结果; ";
								} else {
									logger.warn("[{}]获取" + voipServicePathJ + "节点采集结果voipServiceListJ成功，voipServiceListJ.size={}",
											deviceId, voipServiceListJ.size());
									
									for (String k : voipServiceListJ) {
										String[] voipServicePathK = new String[] {
												voipServicePathJ + k + ".X_CT-COM_ServerType",
												voipServicePathJ + k + ".SIP.ProxyServer",
												voipServicePathJ + k + ".SIP.ProxyServerPort",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-ProxyServer",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-ProxyServerPort",
												voipServicePathJ + k + ".SIP.RegistrarServer",
												voipServicePathJ + k + ".SIP.RegistrarServerPort",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-RegistrarServer",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-RegistrarServerPort",
												voipServicePathJ + k + ".SIP.OutboundProxy",
												voipServicePathJ + k + ".SIP.OutboundProxyPort",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-OutboundProxy",
												voipServicePathJ + k + ".SIP.X_CT-COM_Standby-OutboundProxyPort"};
										ArrayList<ParameValueOBJ> voipServiceListK = corba.getValue(deviceId, voipServicePathK);
										if (null == voipServiceListK || voipServiceListK.size() == 0) {
											logger.warn("voipServiceListK为空，K值为：[{}]",k);
										} else {
											for (ParameValueOBJ pvobj : voipServiceListK) {
												if (pvobj.getName().endsWith("X_CT-COM_ServerType")) {
													ServerType43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.ProxyServer")) {
													ProxyServer43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.ProxyServerPort")) {
													ProxyServerPort43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-ProxyServer")) {
													StandbyProxyServer43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-ProxyServerPort")) {
													StandbyProxyServerPort43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.RegistrarServer")) {
													RegistrarServer43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.RegistrarServerPort")) {
													RegistrarServerPort43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-RegistrarServer")) {
													StandbyRegistrarServer43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-RegistrarServerPort")) {
													StandbyRegistrarServerPort43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.OutboundProxy")) {
													OutboundProxy43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.OutboundProxyPort")) {
													OutboundProxyPort43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-OutboundProxy")) {
													StandbyOutboundProxy43 = pvobj.getValue();
												} else if (pvobj.getName().endsWith("SIP.X_CT-COM_Standby-OutboundProxyPort")) {
													StandbyOutboundProxyPort43 = pvobj.getValue();
												} 
											}
											// break;
										}
										
										String voipServicePathK_line = voipServicePathJ + k + ".Line.";
										List<String> voipServiceListK_line = corba.getIList(deviceId, voipServicePathK_line);
										if (null == voipServiceListK_line || voipServiceListK_line.isEmpty()) {
											logger.warn(voipServicePathK_line + "节点没有采集结果");
										} else {
											logger.warn("[{}]获取" + voipServicePathK_line + "节点采集结果voipServiceListK成功，voipServiceListK_line.size=[{}]",
													deviceId, voipServiceListK_line.size());
											for (String l : voipServiceListK_line) {
												String[] voipServicePathL = new String[] {
														voipServicePathK_line + l + ".Enable",
														voipServicePathK_line + l + ".SIP.AuthUserName",
														voipServicePathK_line + l + ".SIP.AuthPassword",
														voipServicePathK_line + l + ".SIP.URI" };
												ArrayList<ParameValueOBJ> voipServiceListL = corba.getValue(deviceId, voipServicePathL);
												if (null == voipServiceListL || voipServiceListL.size() == 0) {
													logger.warn("voipServiceListL为空，L值为：[{}]，没有采集结果",l);
												} else {
													for (ParameValueOBJ pvobj : voipServiceListL) {
														if (pvobj.getName().endsWith("Enable")) {
															Enable43 = pvobj.getValue();
														} else if (pvobj.getName().endsWith("AuthUserName")) {
															AuthUserName43 = pvobj.getValue();
														} else if (pvobj.getName().endsWith("AuthPassword")) {
															AuthPassword43 = pvobj.getValue();
														} else if (pvobj.getName().endsWith("URI")) {
															URI43 = pvobj.getValue();
														}
													}
													// break;
												}
											}
										}
									}
								}
							}
						}
						
						logger.warn("数据库查询结果：voip_username:[{}]; voip_passwd:[{}]; ",voip_username,voip_passwd);
						logger.warn("数据库查询结果：sip_id:[{}]; uri:[{}]",sip_id,uri);
						logger.warn("数据库查询结果：prox_serv:[{}]; prox_port:[{}];",prox_serv,prox_port);
						logger.warn("数据库查询结果：stand_prox_serv:[{}]; stand_prox_port:[{}]",stand_prox_serv,stand_prox_port);
						logger.warn("数据库查询结果：regi_serv:[{}]; regi_port:[{}];",regi_serv,regi_port);
						logger.warn("数据库查询结果：stand_regi_serv:[{}]; stand_regi_port:[{}]",stand_regi_serv,stand_regi_port);
						logger.warn("数据库查询结果：out_bound_proxy:[{}]; out_bound_port:[{}];",	out_bound_proxy,out_bound_port);
						logger.warn("数据库查询结果：stand_out_bound_proxy:[{}]; stand_out_bound_port:[{}]",stand_out_bound_proxy,stand_out_bound_port);
						
						logger.warn("AuthUserName43采集结果:[{}];",AuthUserName43);
						logger.warn("AuthPassword43采集结果:[{}];",AuthPassword43);
						logger.warn("URI43采集结果:[{}];",URI43);
						logger.warn("ServerType43 = [{}]; ProxyServer43 = [{}]; ProxyServerPort43 = [{}]; "
										+ "StandbyProxyServer43 = [{}]; StandbyProxyServerPort43 = [{}]; "
										+ "RegistrarServer43 = [{}]; RegistrarServerPort43 = [{}]; StandbyRegistrarServer43 = [{}]; "
										+ "StandbyRegistrarServerPort43 = [{}]; OutboundProxy43 = [{}]; OutboundProxyPort43 = [{}]; "
										+ "StandbyOutboundProxy43 = [{}]; StandbyOutboundProxyPort43 = [{}];"
										+ "Enable43 = [{}]; AuthUserName43 = [{}]; "
										+ "AuthPassword43 = [{}]; URI43 = [{}]; ",
								new Object[] { ServerType43, ProxyServer43,
										ProxyServerPort43, StandbyProxyServer43,
										StandbyProxyServerPort43, RegistrarServer43,
										RegistrarServerPort43,
										StandbyRegistrarServer43,
										StandbyRegistrarServerPort43, OutboundProxy43,
										OutboundProxyPort43, StandbyOutboundProxy43,
										StandbyOutboundProxyPort43, Enable43,
										AuthUserName43, AuthPassword43, URI43 });
						
						if ("".equals(ServerType43)) {
							logger.warn("X_CT-COM_ServerType节点没有采集结果");
							errResult43 += "X_CT-COM_ServerType节点没有采集结果; ";
						}

						if ("2".equals(ServerType43)) {
							logger.warn("设备为H248，不做参数对比");
							errResult43 += "设备为H248，不做参数对比; ";
						}

						if (!"".equals(ServerType43) && !"2".equals(ServerType43)
								&& !"0".equals(ServerType43) && !"1".equals(ServerType43)) {
							logger.warn("X_CT-COM_ServerType有采集结果有误，不是0或1");
							errResult43 += "X_CT-COM_ServerType有采集结果有误，不是0或1; ";
						}
						
						
						if ("0".equals(ServerType43) || "1".equals(ServerType43)) {
							if (AuthUserName43 == null || !AuthUserName43.equals(voip_username)) {
								logger.warn("AuthUserName采集结果为空或者与语音用户账号不匹配");
								errResult43 += "AuthUserName采集结果为空或者与语音用户账号不匹配; ";
							}
							if (AuthPassword43 == null || !AuthPassword43.equals(voip_passwd)) {
								logger.warn("AuthPassword采集结果为空或者与语音用户密码不匹配");
								errResult43 += "AuthPassword采集结果为空或者与语音用户密码不匹配; ";
							}
							if (URI43 == null || !URI43.equals(uri)) {
								logger.warn("URI采集结果为空或者与语音用户电话号码不匹配");
								errResult43 += "URI采集结果为空或者与语音用户电话号码不匹配; ";
							}
							if (ProxyServerPort43 == null || !ProxyServerPort43.equals(prox_port)) {
								logger.warn("ProxyServerPort采集结果为空或者与工单资料不匹配");
								errResult43 += "ProxyServerPort采集结果为空或者与工单资料不匹配; ";
							}
							if (RegistrarServer43 == null || !"xj.ctcims.cn".equals(RegistrarServer43)) {
								logger.warn("RegistrarServer采集结果为空或者与工单资料不匹配");
								errResult43 += "RegistrarServer采集结果为空或者与工单资料不匹配; ";
							}
							if (RegistrarServerPort43 == null || !RegistrarServerPort43.equals(regi_port)) {
								logger.warn("RegistrarServerPort采集结果为空或者与工单资料不匹配");
								errResult43 += "RegistrarServerPort采集结果为空或者与工单资料不匹配; ";
							}
							if (OutboundProxy43 == null || !OutboundProxy43.equals(out_bound_proxy)) {
								logger.warn("OutboundProxy采集结果为空或者与工单资料不匹配");
								errResult43 += "OutboundProxy采集结果为空或者与工单资料不匹配; ";
							}
							if (OutboundProxyPort43 == null	|| !OutboundProxyPort43.equals(out_bound_port)) {
								logger.warn("OutboundProxyPort采集结果为空或者与工单资料不匹配");
								errResult43 += "OutboundProxyPort采集结果为空或者与工单资料不匹配; ";
							}
							if (StandbyOutboundProxy43 == null || !StandbyOutboundProxy43.equals(stand_out_bound_proxy)) {
								logger.warn("StandbyOutboundProxy采集结果为空或者与工单资料不匹配");
								errResult43 += "StandbyOutboundProxy采集结果为空或者与工单资料不匹配; ";
							}
							if (StandbyOutboundProxyPort43 == null || !StandbyOutboundProxyPort43.equals(stand_out_bound_port)) {
								logger.warn("StandbyOutboundProxyPort采集结果为空或者与工单资料不匹配");
								errResult43 += "StandbyOutboundProxyPort采集结果为空或者与工单资料不匹配; ";
							}
							
						}
						
					}
				}
			}			
			// vlan43 End
			// New End
			

			if (!"".equals(errResult) || !"".equals(errResult41) || !"".equals(errResult43)) {
				logger.warn(
						"比对结果，errResult45：[{}]；errResult41：[{}]；errResult43：[{}]；",
						new Object[] { errResult, errResult41, errResult43 });
				
				if(4==checker.getErrCode45() && 4==checker.getErrCode41() && 4==checker.getErrCode43()){
					checker.setResult(0);
					checker.setResultDesc("用户没有开通 IPTV，宽带，语音业务");
				}

				if (!"".equals(errResult) && 4!=checker.getErrCode45()) {
					checker.setErrCode45(1008);
				}
				checker.setErrResult45(errResult);
				
				if (!"".equals(errResult41) && 4!=checker.getErrCode41()) {
					checker.setErrCode41(1008);
				}
				checker.setErrResult41(errResult41);
				
				if (!"".equals(errResult43) && 4!=checker.getErrCode43()) {
					checker.setErrCode43(1008);
				}
				checker.setErrResult43(errResult43);
				
				if(4==checker.getErrCode45()){
					checker.setErrCode45(0);
					checker.setErrResult45("用户没有开通iptv业务");
				}else if(0==checker.getErrCode45()){
					checker.setErrResult45("");
				}
				
				if(4==checker.getErrCode41()){
					checker.setErrCode41(0);
					checker.setErrResult41("用户没有开通宽带业务");
				}else if(0==checker.getErrCode41()){
					checker.setErrResult41("");
				}
				
				if(4==checker.getErrCode43()){
					checker.setErrCode43(0);
					checker.setErrResult43("用户没有开通语音业务");
				}else if(0==checker.getErrCode43()){
					checker.setErrResult43("");
				}
				
				if(1008==checker.getErrCode43() || 1008==checker.getErrCode45() || 1008==checker.getErrCode41()){
					checker.setResult(1008);
					checker.setResultDesc("比对失败");
				}else{
					checker.setResult(0);
					checker.setResultDesc("成功");
				}
				
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}

		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}

		logger.warn("对比成功");
		checker.setResult(0);
		checker.setResultDesc("成功");

		checker.setErrCode41(0);
		checker.setErrResult41("");
		checker.setErrCode43(0);
		checker.setErrResult43("");
		checker.setErrCode45(0);
		checker.setErrResult45("");

		return checker.getReturnXml();
	}
}
