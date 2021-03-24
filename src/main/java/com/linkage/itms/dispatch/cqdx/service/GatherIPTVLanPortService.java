package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.IPTVLanDAO;
import com.linkage.itms.dispatch.cqdx.obj.IPTVLanDealXML;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GatherIPTVLanPortService
{
	private static Logger logger = LoggerFactory.getLogger(GatherIPTVLanPortService.class);
	private HashMap<String, String> iptvMap = new HashMap<String, String>();
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;
	
	public String work(String inXml) {
		logger.warn("servicename[GatherIPTVLanPortService]执行，入参为：{}", inXml);
		IPTVLanDealXML deal = new IPTVLanDealXML();
		//校验入参
		Document document = deal.getXML(inXml);
		if (document == null) 
		{
			logger.warn("servicename[GatherIPTVLanPortService]解析入参错误！");
			return deal.returnXML();
		}
		else
		{
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			Map<String, String> userInfoMap  = null;
			if(!StringUtil.IsEmpty(deal.getPppUsename()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_1, deal.getPppUsename(), null);
			}
			else if(!StringUtil.IsEmpty(deal.getLogicId()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_2, deal.getLogicId(), null);
			}
			
			//用户不存在
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn(
						"servicename[GatherIPTVLanPortService] ppp_username[{}] , logic_id[{}]查无此用户",
						new Object[] {deal.getPppUsename(), deal.getLogicId()});
				deal.setResult("-1");
				deal.setErrMsg("无此用户信息");
			} 
			//用户存在
			else
			{
				IPTVLanDAO dao = new IPTVLanDAO();
				List<HashMap<String,String>> iptvInfo = dao.iptvPortByUserName(userInfoMap.get("username"));
				if(null == iptvInfo)
				{
					deal.setResult("-99");
					deal.setErrMsg("iptv信息为空");
				}
				else
				{
					deal.setResult("0");
					for(HashMap<String,String> map : iptvInfo)
					{
						if(!StringUtil.IsEmpty(StringUtil.getStringValue(map, "bind_port")))
						{
							deal.setExpectIPTVPort(StringUtil.getStringValue(map, "bind_port"));
						}
					}
					
					//采集设备获取实时端口
					deal = gatherDev(userInfoMap.get("device_id"), deal);
					if("0".equals(deal.getResult())){
						deal.setActualIPTVPort(iptvMap.get("bind_port"));
					}
				}
			}
		}
		return deal.returnXML();
	}
	
	private IPTVLanDealXML gatherDev(String deviceId, IPTVLanDealXML deal)
	{
		ACSCorba acsCorba = new ACSCorba();
		
		
		//先检测设备是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
			return deal;
		}
		// 设备在线
		if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			String lanInterface = ".X_CT-COM_LanInterface";
			String IPTV = "IPTV";
			
			String iptvJ = "-1";
			String iptvK = "-1";
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
					logger.warn("[GatherIPTVLanPortService] [{}]获取" + wanConnPath + "下实例号失败，返回",
							deviceId);
					deal.setResult("-99");
					deal.setErrMsg("获取节点失败");
					return deal;
				}
				for (String j : jList)
				{
					// 获取session，
					List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
							+ wanPPPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[GatherIPTVLanPortService] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
						List<String> kipList = acsCorba.getIList(deviceId, wanConnPath + j + wanIPConnection);
						if(null == kipList || kipList.size() == 0 || kipList.isEmpty()){
							logger.warn("[GatherIPTVLanPortService] [{}]获取" + wanConnPath
									+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
							deal.setResult("-99");
							deal.setErrMsg("获取InternetGatewayDevice.WANDevice.1.WANConnectionDevice.下实例号失败");
							return deal;
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
				logger.warn("[GatherIPTVLanPortService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("不存在WANIP下的X_CT-COM_ServiceList节点");
				return deal;
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
				logger.warn("[GatherIPTVLanPortService] [{}]获取ServiceList失败", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("获取ServiceList失败");
				return deal;
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
							&& (entry.getValue().indexOf(IPTV) >= 0 || entry.getValue().indexOf("OTHER") >= 0 || entry.getValue().indexOf("Other") >= 0)){//X_CT-COM_ServiceList的值为IPTV的时候j\k
						
						iptvJ = j;
						iptvK = k;
					}
				}
			}
			
			if(!"-1".equals(iptvJ) && !"-1".equals(iptvK)){
				String[] gatherPath  = new String[]{wanConnPath+iptvJ+wanPPPConnection+iptvK+lanInterface};
				ArrayList<ParameValueOBJ> objList = acsCorba.getValue(deviceId, gatherPath);
				if(null != objList && objList.size() > 0){
					for(ParameValueOBJ pvobj : objList){
						if(pvobj.getName().contains("X_CT-COM_LanInterface")){
							iptvMap.put("bind_port",pvobj.getValue());
						}
					}
				}
				deal.setResult("0");
				deal.setErrMsg("执行成功");
			}else{
				deal.setResult("-99");
				deal.setErrMsg("未获取到IPTV通道");
			}
		}
		else {// 设备不在线，不能获取节点值
			logger.warn("设备不在线，无法获取节点值 ,device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
			return deal;
		}
		return deal;
	}
	
}
