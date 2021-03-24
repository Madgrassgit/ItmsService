package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevInternetIpv6InfoChecker;
import com.linkage.itms.nmg.dispatch.service.IService;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 光猫上网IPV6开启情况查询接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2019-5-17
 */
public class DevInternetIpv6InfoService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(DevInternetIpv6InfoService.class);
	
	
	public String work(String inXml){
		
		logger.warn("DevInternetIpv6InfoService：inXml({})", inXml);
		
		DevInternetIpv6InfoChecker checker = new DevInternetIpv6InfoChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba acsCorba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 2)
		{
			userMap = qdDao.queryUserByLoid(checker.getUserInfo());
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1)
		{
			checker.setResult(1006);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("此用户未绑定设备");
			return checker.getReturnXml();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		
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
			
			String pathJ = "1";
			String pathK = "1";
			//采集accessType
			String accessType = null;
			accessType = UserDeviceDAO.getAccType(deviceId);
			if (null == accessType || "null".equals(accessType) || "".equals(accessType))
			{
				String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
				ArrayList<ParameValueOBJ> accessTypeList = acsCorba.getValue(deviceId, accessTypePath);
				if (accessTypeList != null && accessTypeList.size() != 0) {
					for (ParameValueOBJ pvobj : accessTypeList) {
						if (pvobj.getName().endsWith("WANAccessType")) {
							accessType = pvobj.getValue();
						}
					}
				}
			}
			
			logger.warn("accessType为：[{}]", accessType);
			String checkAccessType = null;
			
			if("EPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANEponLinkConfig";
			}else if("GPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANGponLinkConfig";
			}else{
				logger.warn("accessType既不是EPON也不是GPON");
				checker.setResult(1012);
				checker.setResultDesc("上行方式既不是EPON也不是GPON");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			String INTERNET = "INTERNET";
			
			ArrayList<String> wanConnPathsList = null;
			// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
			wanConnPathsList = acsCorba.getParamNamesPath(deviceId, wanConnPath, 0);
			if (wanConnPathsList == null || wanConnPathsList.size() == 0
					|| wanConnPathsList.isEmpty())
			{
				logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
				wanConnPathsList = new ArrayList<String>();
				List<String> jList = acsCorba.getIList(deviceId, wanConnPath);
				if (null == jList || jList.size() == 0 || jList.isEmpty())
				{
					logger.warn("[DevInternetIpv6InfoService] [{}]获取" + wanConnPath + "下实例号失败，返回",
							deviceId);
					checker.setResult(1006);
					checker.setResultDesc("此路径下获取节点失败");
					return checker.getReturnXml();
				}
				for (String j : jList)
				{
					// 获取session，
					List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
							+ wanIPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[DevInternetIpv6InfoService] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
					}
					else
					{
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
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
				if (namepath.indexOf(wanServiceList) >= 0 && namepath.indexOf(wanPPPConnection)>=0)
				{
					serviceListList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			if (serviceListList.size() == 0 || serviceListList.isEmpty())
			{
				logger.warn("[DevInternetIpv6InfoService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
				checker.setResult(0);
				checker.setResultDesc("成功");
				return checker.getReturnXml();
			}
			
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
				Map<String, String> maptemp = acsCorba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty())
				{
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty())
			{
				logger.warn("[DevInternetIpv6InfoService] [{}]获取ServiceList失败", deviceId);
				checker.setResult(1007);
				checker.setResultDesc("获取ServiceList失败");
				return checker.getReturnXml();
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet())
			{
				logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
						entry.getValue() });
				String paramName = entry.getKey();
				String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
				if (paramName.indexOf(wanPPPConnection) >= 0)
				{
				}
				else if (paramName.indexOf(wanIPConnection) >= 0)
				{
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0)
				{
					String k = paramName.substring(paramName.indexOf(wanServiceList) - 1,
							paramName.indexOf(wanServiceList));
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						
						pathJ = j;
						pathK = k;
					}
				}
			}
				String servListPathJ = wanConnPath + pathJ + ".WANPPPConnection." + pathK;
				String connTypePath = servListPathJ + ".ConnectionType";
				String bindPortPath = servListPathJ + ".X_CT-COM_LanInterface";
				String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJ+checkAccessType+".VLANIDMark";
				String ipv6AddressPath = servListPathJ + ".X_CT-COM_IPv6IPAddress";
				String ipv6DnsServersPath = servListPathJ + ".X_CT-COM_IPv6DNSServers";
				
				
				String[] gatherPath = new String[]{connTypePath,bindPortPath,ipv6AddressPath,ipv6DnsServersPath,vlanPath};
				ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
					ArrayList<ParameValueOBJ> connTypeList = acsCorba.getValue(deviceId, connTypePath);
					if (null == connTypeList || connTypeList.isEmpty() || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
						checker.setWanType("");
						logger.warn("[{}]采集ConnectionType失败或者值为空",	deviceId);
						checker.setResult(0);
						checker.setResultDesc("成功 ");
						logger.warn("return=({})", checker.getReturnXml()); // 打印回参
						return checker.getReturnXml();
					}else{
						checker.setWanType(connTypeList.get(0).getValue());
						logger.warn("[{}]采集ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
					}
					
					// 采集 X_CT-COM_LanInterface,绑定端口
					ArrayList<ParameValueOBJ> bindPortList = acsCorba.getValue(deviceId, bindPortPath);
					if (null == bindPortList || bindPortList.size()==0 || null==bindPortList.get(0) || null==bindPortList.get(0).getValue()) {
						checker.setBindPort("");
						logger.warn("[{}]采集LanInterface失败或者值为空",	deviceId);
					}else{
						String portShiPei = bindPortList.get(0).getValue();
						if (!portShiPei.contains(",")) {
							checker.setBindPort(portShiPei);
						} else {
							String[] shipeiStr = portShiPei.split(",");
							Arrays.sort(shipeiStr);
							int i = 0;
							String newPortShiPei = "";
							for (String str : shipeiStr) {
								newPortShiPei += str;
								if (i < shipeiStr.length - 1) {
									newPortShiPei = newPortShiPei + ",";
								}
								i++;
							}
							checker.setBindPort(newPortShiPei);
						}
						logger.warn("[{}]采集LanInterface成功，值为：[{}]",	deviceId,bindPortList.get(0).getValue());
					}
					
					// 采集 VLANIDMark，43等
					ArrayList<ParameValueOBJ> vlanList = acsCorba.getValue(deviceId, vlanPath);
					if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
						checker.setVlanId("");
						logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
					}else{
						checker.setVlanId(vlanList.get(0).getValue());
						logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
					}
					
					// 采集 VLANIDMark，43等
					ArrayList<ParameValueOBJ> ipv6AddressList = acsCorba.getValue(deviceId, ipv6AddressPath);
					if (null == ipv6AddressList || ipv6AddressList.size()==0 || null==ipv6AddressList.get(0) || null==ipv6AddressList.get(0).getValue()) {
						checker.setIpv6Address("");
						logger.warn("[{}]采集ipv6Address失败或者值为空",	deviceId);
					}else{
						checker.setIpv6Address(ipv6AddressList.get(0).getValue());
						logger.warn("[{}]采集ipv6Address成功，值为：[{}]",	deviceId,ipv6AddressList.get(0).getValue());
					}
					
					// 采集 VLANIDMark，43等
					ArrayList<ParameValueOBJ> ipv6DnsServersList = acsCorba.getValue(deviceId, ipv6DnsServersPath);
					if (null == ipv6DnsServersList || ipv6DnsServersList.size()==0 || null==ipv6DnsServersList.get(0) || null==ipv6DnsServersList.get(0).getValue()) {
						checker.setIpv6DnsServers("");
						logger.warn("[{}]采集ipv6DnsServers失败或者值为空",	deviceId);
					}else{
						checker.setIpv6DnsServers(ipv6DnsServersList.get(0).getValue());
						logger.warn("[{}]采集ipv6DnsServers成功，值为：[{}]",	deviceId,ipv6DnsServersList.get(0).getValue());
					}
				}else{
					for(ParameValueOBJ pvobj : objLlist){
						if(pvobj.getName().contains("ConnectionType")){
							checker.setWanType(pvobj.getValue());
						}else if(pvobj.getName().contains("X_CT-COM_LanInterface")){
							String portShiPei = pvobj.getValue();
							
							if ("".equals(portShiPei) || portShiPei == null) {
								checker.setBindPort("");
							} else if (!portShiPei.contains(",")) {
								portShiPei = portShiPei.replaceAll("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN");
								portShiPei = portShiPei.replaceAll("InternetGatewayDevice.LANDevice.1.WLANConfiguration.", "WLAN");
								checker.setBindPort(portShiPei);
							} else {
								portShiPei = portShiPei.replaceAll("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN");
								portShiPei = portShiPei.replaceAll("InternetGatewayDevice.LANDevice.1.WLANConfiguration.", "WLAN");
								String[] shipeiStr = portShiPei.split(",");
								Arrays.sort(shipeiStr);
								int i = 0;
								String newPortShiPei = "";
								for (String str : shipeiStr) {
									newPortShiPei += str;
									if (i < shipeiStr.length - 1) {
										newPortShiPei = newPortShiPei + ",";
									}
									i++;
								}
								checker.setBindPort(newPortShiPei);
							}
						}else if(pvobj.getName().contains("X_CT-COM_IPv6IPAddress")){
							checker.setIpv6Address(pvobj.getValue());
						}
						else if(pvobj.getName().contains("X_CT-COM_IPv6DNSServers")){
							checker.setIpv6DnsServers(pvobj.getValue());
						}
						else if(pvobj.getName().contains("VLANIDMark")){
							checker.setVlanId(pvobj.getValue());
						}
					}

				}
				
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"DevInternetIpv6InfoService");
				return checker.getReturnXml();
				
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1008);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
