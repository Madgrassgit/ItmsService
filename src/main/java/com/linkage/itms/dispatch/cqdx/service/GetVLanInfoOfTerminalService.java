package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetVLanInfoOfTerminalDealXML;
import com.linkage.itms.obj.ParameValueOBJ;

public class GetVLanInfoOfTerminalService {
	private static Logger logger = LoggerFactory.getLogger(GetVLanInfoOfTerminalService.class);

	public String work(String inXml) {
		logger.warn("servicename[GetVLanInfoOfTerminalService]执行，入参为：{}", inXml);
		GetVLanInfoOfTerminalDealXML deal = new GetVLanInfoOfTerminalDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GetVLanInfoOfTerminalService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		int userType = 0;
		String username = "";
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2;
			username = logicId;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1;
			username = pppUsename;
		}
		else {
			logger.warn("servicename[GetVLanInfoOfTerminalService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		userMap = dao.queryUserInfoLan(userType, username);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[GetVLanInfoOfTerminalService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get("device_id"))) {
			logger.warn("servicename[GetVLanInfoOfTerminalService]未绑定设备！");
			deal.setResult("-99");
			deal.setErrMsg("未绑定设备！");
			return deal.returnXML();
		}
		String deviceId = StringUtil.getStringValue(userMap, "device_id");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			ACSCorba acsCorba = new ACSCorba();
			
			
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
				logger.warn("[{}]accessType既不是EPON也不是GPON", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("上行方式既不是EPON也不是GPON");
				return deal.returnXML();
			}
			
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			String multicastVlan = ".X_CT-COM_MulticastVlan";
			String VOIP = "VOIP";
			String INTERNET = "INTERNET";
			String IPTV = "IPTV";
			String vlanPath = checkAccessType+".VLANIDMark";
			
			String netJ = "-1";
			String iptvJ = "-1";
			String iptvK = "-1";
			String voipJ = "-1";
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
					logger.warn("[GetVLanInfoOfTerminalService] [{}]获取" + wanConnPath + "下实例号失败，返回",
							deviceId);
					deal.setResult("-99");
					deal.setErrMsg("获取节点失败");
					return deal.returnXML();
				}
				for (String j : jList)
				{
					// 获取session，
					List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
							+ wanPPPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
						List<String> kipList = acsCorba.getIList(deviceId, wanConnPath + j + wanIPConnection);
						if(null == kipList || kipList.size() == 0 || kipList.isEmpty()){
							logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
									+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
							deal.setResult("-99");
							deal.setErrMsg("获取InternetGatewayDevice.WANDevice.1.WANConnectionDevice.下实例号失败");
							return deal.returnXML();
						}else{
							for (String kip : kipList)
							{
								wanConnPathsList.add(wanConnPath + j + wanIPConnection + kip
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
				deal.setResult("-99");
				deal.setErrMsg("不存在WANIP下的X_CT-COM_ServiceList节点");
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
				if (paramName.indexOf(wanServiceList) >= 0)
				{
					String k = paramName.substring(paramName.indexOf(wanServiceList) - 1,
							paramName.indexOf(wanServiceList));
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候j\k
						
						netJ = j;
					}
					if (!StringUtil.IsEmpty(entry.getValue())
							&& (entry.getValue().indexOf(IPTV) >= 0 || entry.getValue().indexOf("OTHER") >= 0 || entry.getValue().indexOf("Other") >= 0)){//X_CT-COM_ServiceList的值为IPTV的时候j\k
						
						iptvJ = j;
						iptvK = k;
					}
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(VOIP) >= 0){//X_CT-COM_ServiceList的值为VOIP的时候j\k
						
						voipJ = j;
					}
				}
			}
			
			if(!"-1".equals(netJ)){
				String[] gatherPath = new String[]{wanConnPath+netJ+vlanPath};
				ArrayList<ParameValueOBJ> objList = acsCorba.getValue(deviceId, gatherPath);
				if(null != objList && objList.size() > 0){
					deal.setBroadbandVlanId(objList.get(0).getValue());
				}
			}
			if(!"-1".equals(iptvJ) && !"-1".equals(iptvK)){
				String[] gatherPath  = new String[]{wanConnPath+netJ+vlanPath ,wanConnPath+iptvJ+wanPPPConnection+iptvK+multicastVlan};
				ArrayList<ParameValueOBJ> objList = acsCorba.getValue(deviceId, gatherPath);
				if(null != objList && objList.size() > 0){
					for(ParameValueOBJ pvobj : objList){
						if(pvobj.getName().contains("VLANIDMark")){
							deal.setIptvVlanId(pvobj.getValue());
						}
						if(pvobj.getName().contains("X_CT-COM_MulticastVlan")){
							deal.setMultiVlanMode(pvobj.getValue());
						}
					}
				}
			}
			if(!"-1".equals(voipJ)){
				String[] gatherPath  = new String[]{wanConnPath+voipJ+vlanPath};
				ArrayList<ParameValueOBJ> objList = acsCorba.getValue(deviceId, gatherPath);
				if(null != objList && objList.size() > 0){
					deal.setVoipVlanId(objList.get(0).getValue());
				}
			}
			String ret = deal.returnXML();
			// 日志
			deal.recordLog("GetVLanInfoOfTerminalService", username, "", inXml, ret);
			return ret;
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
	}
}
