package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetGatewayConfigInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 家庭网关配置稽核接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetGatewayConfigInfoService {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayConfigInfoService.class);
	UserDeviceDAO userDevDao = new UserDeviceDAO();

	@SuppressWarnings("static-access")
	public String work(String inXml) {
		logger.warn("servicename[GetGatewayPerformanceInfoService]执行，入参为：{}", inXml);
		GetGatewayConfigInfoDealXML deal = new GetGatewayConfigInfoDealXML();
		
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GetGatewayPerformanceInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		if("".equals(logicId) && "".equals(pppUsename)){
			logger.warn("servicename[GetGatewayPerformanceInfoService]宽带账号和逻辑账号不能同时为空！");
			deal.setResult("-99");
			deal.setErrMsg("宽带账号和逻辑账号不能同时为空！");
			return deal.returnXML();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba acsCorba = new ACSCorba();
		String deviceId = "";
		String userId = "";
		List<HashMap<String, String>> userMap = null;
		if (!"".equals(pppUsename)){
			userMap = qdDao.queryUserByNetAccount(pppUsename);
		}else{
			userMap = qdDao.queryUserByLoid2(logicId);
		}
		
		if (userMap.size() > 1)
		{
			deal.setResult("-99");
			deal.setErrMsg("数据不唯一，请使用逻辑SN查询");
			return deal.returnXML();
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			logger.warn("servicename[GetGatewayPerformanceInfoService]loid[{}]查无此用户",
					new Object[] { logicId });
			deal.setResult("-1");
			deal.setErrMsg("用户不存在");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{// 用户未绑定终端
			logger.warn("servicename[GetGatewayPerformanceInfoService]loid[{}]此客户未绑定",
					new Object[] { logicId });
			deal.setResult("-99");
			deal.setErrMsg("此客户未绑定");
			return deal.returnXML();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
		
		

		// 1.查询此用户开通的业务信息
		Map<String, String> userServMap = userDevDao.queryServForNet(userId);
		Map<String, String> userIptvMap = userDevDao.queryServForIptv(userId);
		boolean haveIptv = false;
		boolean haveInternet = false;
		if ((null == userServMap || userServMap.isEmpty()) && (null == userIptvMap || userIptvMap.isEmpty()))
		{
			// 没有开通业务
			logger.warn("servicename[QuerySheetDataService] userinfo[{}]此用户没有开通宽带业务和IPTV业务",
					new Object[] {  pppUsename });
			deal.setResult("-99");
			deal.setErrMsg("此用户没有开通任何宽带业务");
			return deal.returnXML();
		}
		else
		{
			// 工单值
			String userName = userServMap == null ? "" : userServMap.get("username");
			logger.warn("servicename[QuerySheetDataService] userinfo[{},{}]获取上网业务工单配置数据：", pppUsename, userName);
			if (null != userServMap && !userServMap.isEmpty())
			{
				// 开通宽带业务
				haveInternet = true;
				String kdWanType = StringUtil.getStringValue(userServMap, "wan_type","");
				
				if ("1".equals(kdWanType))
				{
					kdWanType = "PPPoE_Bridged";
				}
				else if ("2".equals(kdWanType))
				{
					kdWanType = "IP_Routed";
				}
				else if ("3".equals(kdWanType))
				{
					kdWanType = "静态IP";
				}
				else if ("4".equals(kdWanType))
				{
					kdWanType = "DHCP";
				}
				deal.setKdWanType_yingpei(kdWanType);

				deal.setKdVlanId_yingpei(StringUtil.getStringValue(userServMap, "vlanid", ""));
			}
			
			if (null != userIptvMap && !userIptvMap.isEmpty())
			{
				// 开通iptv业务
				haveIptv = true;
				String iptvWanType = StringUtil.getStringValue(userServMap, "wan_type","");
				if ("1".equals(iptvWanType))
				{
					iptvWanType = "PPPoE_Bridged";
				}
				else if ("2".equals(iptvWanType))
				{
					iptvWanType = "IP_Routed";
				}
				else if ("3".equals(iptvWanType))
				{
					iptvWanType = "静态IP";
				}
				else if ("4".equals(iptvWanType))
				{
					iptvWanType = "DHCP";
				}
				deal.setIptvWanType_yingpei(iptvWanType);
				deal.setIptvVlanId_yingpei(StringUtil.getStringValue(userIptvMap, "vlanid", ""));
			}
			
			int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
			// 设备正在被操作，不能获取节点值
			if (-6 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("设备正在被操作");
				logger.warn("return=({})", deal.returnXML());  // 打印回参
				return deal.returnXML();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
				
				String kdPathJ = "1";
				String kdPathK = "1";
				String iptvPathJ = "99";
				String iptvPathK = "99";
				//采集accessType
				String accessType = null;
				accessType = userDevDao.getAccType(deviceId);
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
				
				if("EPON".equals(accessType) || "X_BROADCOM_COM_PON".equals(accessType)){
					checkAccessType = ".X_CT-COM_WANEponLinkConfig";
				}else if("GPON".equals(accessType)){
					checkAccessType = ".X_CT-COM_WANGponLinkConfig";
				}else{
					logger.warn("accessType既不是EPON也不是GPON");
					deal.setResult("-99");
					deal.setErrMsg("上行方式既不是EPON也不是GPON");
					logger.warn("return=({})", deal.returnXML());  // 打印回参
					return deal.returnXML();
				}
				
				String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
				String wanServiceList = ".X_CT-COM_ServiceList";
				String wanPPPConnection = ".WANPPPConnection.";
				String wanIPConnection = ".WANIPConnection.";
				String INTERNET = "INTERNET";
				String IPTV = "IPTV";
				String OTHER = "OTHER";
				
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
						logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath + "下实例号失败，返回",
								deviceId);
						deal.setResult("-99");
						deal.setErrMsg("此路径下获取节点失败");
						return deal.returnXML();
					}
					for (String j : jList)
					{
						// 获取session，
						List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
								+ wanIPConnection);
						if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
						{
							logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
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
					logger.warn("[QuerySheetDataService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
					deal.setResult("0");
					deal.setErrMsg("成功");
					return deal.returnXML();
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
					logger.warn("[QuerySheetDataService] [{}]获取ServiceList失败", deviceId);
					deal.setResult("-99");
					deal.setErrMsg("获取ServiceList失败");
					return deal.returnXML();
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
						if (haveInternet == true && !StringUtil.IsEmpty(entry.getValue())
									&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
								
								kdPathJ = j;
								kdPathK = k;
						}
						
						if (haveIptv == true && !StringUtil.IsEmpty(entry.getValue())
									&& (entry.getValue().indexOf(IPTV) >= 0 || entry.getValue().indexOf(OTHER) >= 0)){//X_CT-COM_ServiceList的值为iptv的时候，此节点路径即为要删除的路径
								iptvPathJ = j;
								iptvPathK = k;
						}
					}
				}
					String connTypePath = wanConnPath + kdPathJ + ".WANPPPConnection." + kdPathK + ".ConnectionType";
					String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+kdPathJ+checkAccessType+".VLANIDMark";
					String iptvConnTypePath = wanConnPath + iptvPathJ + ".WANPPPConnection." + iptvPathK + ".ConnectionType";
					String iptvVlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+iptvPathJ+checkAccessType+".VLANIDMark";
					String[] gatherPath = null;
					if(haveInternet == true && haveIptv == true){
						gatherPath = new String[]{connTypePath, vlanPath, iptvConnTypePath, iptvVlanPath};
					}else if(haveInternet == true && haveIptv == false){
						gatherPath = new String[]{connTypePath, vlanPath};
					}
					else if(haveInternet == false && haveIptv == true){
						gatherPath = new String[]{iptvConnTypePath, iptvVlanPath};
					}
					
					ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
					if (null == objLlist || objLlist.isEmpty()) {
						// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
						if(haveInternet == true){
							ArrayList<ParameValueOBJ> connTypeList = acsCorba.getValue(deviceId, connTypePath);
							if (null == connTypeList || connTypeList.size()==0 || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
								deal.setKdWanType_shipei("");
								logger.warn("[{}]采集宽带ConnectionType失败或者值为空",	deviceId);
								deal.setResult("0");
								deal.setErrMsg("成功 ");
								logger.warn("return=({})", deal.returnXML()); // 打印回参
								return deal.returnXML();
							}else{
								deal.setKdWanType_shipei(connTypeList.get(0).getValue());
								logger.warn("[{}]采集宽带ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
							}
							
							// 采集 VLANIDMark，43等
							ArrayList<ParameValueOBJ> kdVlanList = acsCorba.getValue(deviceId, vlanPath);
							if (null == kdVlanList || kdVlanList.size()==0 || null==kdVlanList.get(0) || null==kdVlanList.get(0).getValue()) {
								deal.setKdVlanId_shipei("");
								logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
							}else{
								deal.setKdVlanId_shipei(kdVlanList.get(0).getValue());
								logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,kdVlanList.get(0).getValue());
							}
						}
						
						if(haveIptv == true){
							ArrayList<ParameValueOBJ> iptvConnTypeList = acsCorba.getValue(deviceId, iptvConnTypePath);
							if (null == iptvConnTypeList || iptvConnTypeList.size()==0 || null==iptvConnTypeList.get(0) || null==iptvConnTypeList.get(0).getValue()) {
								deal.setIptvWanType_shipei("");
								logger.warn("[{}]采集IPTV ConnectionType失败或者值为空",	deviceId);
								deal.setResult("0");
								deal.setErrMsg("成功 ");
								logger.warn("return=({})", deal.returnXML()); // 打印回参
								return deal.returnXML();
							}else{
								deal.setIptvWanType_shipei(iptvConnTypeList.get(0).getValue());
								logger.warn("[{}]采集IPTV ConnectionType成功，值为：[{}]",	deviceId,iptvConnTypeList.get(0).getValue());
							}
							// 采集 VLANIDMark，43等
							ArrayList<ParameValueOBJ> iptvVlanList = acsCorba.getValue(deviceId, vlanPath);
							if (null == iptvVlanList || iptvVlanList.size()==0 || null==iptvVlanList.get(0) || null==iptvVlanList.get(0).getValue()) {
								deal.setIptvVlanId_shipei("");
								logger.warn("[{}]采集IPTV VLANIDMark失败或者值为空",	deviceId);
							}else{
								deal.setIptvVlanId_shipei(iptvVlanList.get(0).getValue());
								logger.warn("[{}]采集IPTV VLANIDMark成功，值为：[{}]",	deviceId,iptvVlanList.get(0).getValue());
							}
						}
						
					}else{
						for(ParameValueOBJ pvobj : objLlist){
							if(pvobj.getName().contains("ConnectionType") && pvobj.getName().contains(kdPathJ)){
								deal.setKdWanType_shipei(pvobj.getValue());
							}else if(pvobj.getName().contains("VLANIDMark") && pvobj.getName().contains(kdPathJ)){
								deal.setKdVlanId_shipei(pvobj.getValue());
							}else if(pvobj.getName().contains("ConnectionType") && pvobj.getName().contains(iptvPathJ)){
								deal.setIptvWanType_shipei(pvobj.getValue());
							}else if(pvobj.getName().contains("VLANIDMark") && pvobj.getName().contains(iptvPathK)){
								deal.setIptvVlanId_shipei(pvobj.getValue());
							}
						}

					}
					
				if (deal.getKdVlanId_shipei().equals(deal.getKdVlanId_yingpei()) && deal.getKdWanType_shipei().equals(deal.getKdWanType_yingpei())
						&& deal.getIptvVlanId_shipei().equals(deal.getIptvVlanId_yingpei()) && deal.getIptvWanType_shipei().equals(deal.getIptvWanType_yingpei())){
					deal.setResult("0");
					deal.setErrMsg("成功");
					return deal.returnXML();
				}else{
					deal.setResult("-2");
					deal.setErrMsg("有指标不匹配");
					return deal.returnXML();
				}
					
			} else {// 设备不在线，不能获取节点值
				logger.warn("设备不在线，无法获取节点值");
				deal.setResult("-99");
				deal.setErrMsg("设备不能正常交互");
				logger.warn("return=({})", deal.returnXML()); // 打印回参
				return deal.returnXML();
			}
			 
		}	 
	}
}
