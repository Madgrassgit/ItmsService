package com.linkage.itms.dispatch.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigNewDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.HgwServUserObj;
import com.linkage.itms.dispatch.obj.QueryDeviceServiceNew;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-5
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DevConfigServiceNewone implements IService
{
	// 日志记录
		private static final Logger logger = LoggerFactory.getLogger(DevConfigServiceNewone.class);
		private String wideNetMsg = "1";
		private String iptvMsg = "1";
		private String voipMsg = "1";
		private String accessType;
		private List<HashMap<String, String>> internetlist;
		private List<HashMap<String, String>> iptvlist;
		private List<HashMap<String, String>> voipPVClist;
		private List<HashMap<String, String>> linesList ;
		private List<HashMap<String, String>> registStatusList;
		private List<HashMap<String, String>> voiceServiceList;
		private List<HashMap<String, String>> IGMPSnoopingList;
		private String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
		private String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
		private String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
		private String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
		private String WLAN1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
		private String WLAN2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
		private String WLAN3 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3";
		private String WLAN4 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4";
		private DeviceConfigNewDAO dao = new DeviceConfigNewDAO();
		private Map<String, String> bindPortMap = new HashMap<String, String>();

		public DevConfigServiceNewone()
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
	@Override
	public String work(String inXml)
	{
		Map<String,String> resultMap = new HashMap<String, String>();
		Map<String,List<HashMap<String,String>>> configInfo = new HashMap<String, List<HashMap<String,String>>>();
		
		QueryDeviceServiceNew devConfiger = new QueryDeviceServiceNew(inXml);
		if (false == devConfiger.check()) {
			logger.error(
					"servicename[DevConfigService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo(),
							devConfiger.getReturnXml() });
			return devConfiger.getReturnXml();
		}
		logger.warn(
				"servicename[DevConfigService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户设备信息
		Map<String,String> userDevInfo = userDevDao.queryUserInfo(devConfiger.getUserInfoType(), devConfiger.getUserInfo());
		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn("servicename[DevConfigService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo()});
			devConfiger.setResult(1002);
			devConfiger.setResultDesc("无此客户信息");
		} else {
			Map<String, String> devMap = userDevDao.getDevStatus(userDevInfo
					.get("user_id"));
			String complete_time = devMap.get("complete_time");
			
			try {
				long time = Long.parseLong(complete_time + "000");
				// 注册时间:YYYY-MM-DD hh:mm:ss, 样例：2016-03-11 12:33:00
				Date date = new Date();
				date.setTime(time);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				devConfiger.setRegTime(sdf.format(date));
			} catch (Exception e) {
				devConfiger.setRegTime(complete_time + "（时间格式转换失败）");
			}
			
			String deviceId = userDevInfo.get("device_id");
			
			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(
						"servicename[DevConfigService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo()});
				devConfiger.setResult(1003);
				devConfiger.setResultDesc("此用户没有设备关联信息");
			}  else {
				int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);  // 在原来基础上增加了一个参数(3)
				logger.warn("servicename[DevConfigService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果：{}",
						new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo(),rsint});
				// 采集失败
				if (rsint != 1) { 
					logger.warn(
							"servicename[DevConfigService]cmdId[{}]userinfo[{}]采集数据失败",
							new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo()});
					devConfiger.setResult(1004);
					devConfiger.setResultDesc("设备采集失败");
				// 采集成功，获取需要的数据
				} else { 
					getAndCheckConf(deviceId, userDevInfo.get("city_id"), "js_dx", userDevInfo.get("user_id"));
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
//					根据需求单JSDX_ITMS-REQ-20120220-LUHJ-006及ItmsService接口文档，返回的参数不需要"配置信息检查结果"(result)节点
					configInfo.put("inter_list", internetlist);
					
					//IPTV
//					根据需求单JSDX_ITMS-REQ-20120220-LUHJ-006及ItmsService接口文档，返回的参数不需要"配置信息检查结果"(result)节点
					configInfo.put("iptv_list", iptvlist);
					
					//VOIP
//					根据需求单JSDX_ITMS-REQ-20120220-LUHJ-006及ItmsService接口文档，返回的参数不需要"配置信息检查结果"(result)节点
					configInfo.put("voip_list", voipPVClist);
					
//	  				add by zhangchy 2012-03-07 存放VOIP语音的线路信息  --begin--
					configInfo.put("lines_list", linesList);
					configInfo.put("registStatus_List", registStatusList);
					//configInfo.put("voiceService", voiceServiceList);
					//configInfo.put("IGmpSnooping_list", IGMPSnoopingList);
//	  				add by zhangchy 2012-03-07 存放VOIP语音的线路信息  --end --
					devConfiger.setResult(0);
					devConfiger.setResultDesc("成功");
					devConfiger.setResultMap(resultMap);
					devConfiger.setConfigInfo(configInfo);
				}
			}	
		}
		
		String returnXml = devConfiger.getReturnXml();
		// 记录日志
				new RecordLogDAO().recordDispatchLog(devConfiger, devConfiger.getUserInfo(),
						"DevConfigService");
		logger.warn(
				"servicename[DevConfigService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { devConfiger.getCmdId(), devConfiger.getUserInfo(),returnXml});
	
		return returnXml;
	}

	
	public void getAndCheckConf(String deviceId, String cityId, String shortName, String userId){
		
		logger.debug("DevConfigService==>getAndCheckConf({},{},{})", new Object[]{deviceId, cityId, shortName});
// 		回参不需要返回"配置信息检查结果" 注释 by zhangchy 2012-03-07    -----
		accessType = dao.getAccessType(deviceId, 1);
		if (true == StringUtil.IsEmpty(accessType))	{
			logger.error("预配置诊断:设备没有上行方式！！");
		}
// 		回参不需要返回"配置信息检查结果" 注释 by zhangchy 2012-03-07    -----
		internetlist = dao.getInternet(deviceId);   // Internet
		iptvlist = dao.getIPTV(deviceId);           // IPTV 
		voipPVClist = dao.getPVCVOIP(deviceId);     //
		registStatusList=dao.getLink_status(deviceId);
		//voiceServiceList = dao.getVoiceService(deviceId);
		linesList = dao.getVoipLines(deviceId);        //  add by zhangchy 2012-03-07 VOIP语音线路信息
		//IGMPSnoopingList = dao.getIGMPSnooping(deviceId);   // add by zhangchy 2012-03-07 IGMPSnooping 
	}
	
	
	private void internetDSLConf(Map<String, List<Map<String, String>>> preconfmap)
	{
		logger.warn("DSL宽带上网预配置");
		List<Map<String, String>> intPreList = preconfmap.get("INTERNET");
		for (int i = 0; i < intPreList.size(); i++)
		{
			boolean falg = false;
			Map<String, String> tmap = intPreList.get(i);
			if (internetlist.size() == 0)
			{
				wideNetMsg = "0";
			}
			else
			{
				for (int j = 0; j < internetlist.size(); j++)
				{
					Map<String, String> rmap = (Map<String, String>) internetlist.get(j);
					if (StringUtil.getStringValue(tmap.get("vpi_id")).equals(
							StringUtil.getStringValue(rmap.get("vpi_id")))
							&& StringUtil.getStringValue(tmap.get("vci_id")).equals(
									StringUtil.getStringValue(rmap.get("vci_id"))))
					{
						falg = true;
						break;
					}
				}
				if (falg == false)
				{
					wideNetMsg = "0";
					break;
				}
			}
		}
	}

	private void iptvtDSLConf(Map<String, List<Map<String, String>>> preconfmap)
	{
		List<Map<String, String>> iptvPreList = preconfmap.get("IPTV");
		for (int i = 0; i < iptvPreList.size(); i++)
		{
			boolean falg = false;
			Map<String, String> tmap = iptvPreList.get(i);
			if (iptvlist.size() == 0)
			{
				iptvMsg = "0";
			}
			else
			{
				for (int j = 0; j < iptvlist.size(); j++)
				{
					Map<String, String> rmap = (Map<String, String>) iptvlist.get(j);
					if (StringUtil.getStringValue(tmap.get("vpi_id")).equals(
							StringUtil.getStringValue(rmap.get("vpi_id")))
							&& StringUtil.getStringValue(tmap.get("vci_id")).equals(
									StringUtil.getStringValue(rmap.get("vci_id"))))
					{
						falg = true;
						break;
					}
				}
				if (falg == false)
				{
					iptvMsg = "0";
					break;
				}
			}
		}
	}
	private void voipDSLConf(Map<String, List<Map<String, String>>> preconfmap,String userId)
	{

			if (false == StringUtil.IsEmpty(userId))
			{
				List<Map<String, String>> voipPreList = preconfmap.get("VOIP");
				Map<String, String> emap = voipPreList.get(0);
				boolean falg1 = false;
				for (int i = 0; i < voipPVClist.size(); i++)
				{
					Map<String, String> vmap = (Map<String, String>) voipPVClist
							.get(i);
					if (StringUtil.getStringValue(emap.get("vpi_id")).equals(
							StringUtil.getStringValue(vmap.get("vpi_id")))
							&& StringUtil.getStringValue(emap.get("vci_id")).equals(
									StringUtil.getStringValue(vmap.get("vci_id"))))
					{
						falg1 = true;
					}
				}
				if (!falg1)
				{
					voipMsg = "0";
				}
			}
			else
			{
				voipMsg = "0";
				logger.warn("=====voipDSLConf 未绑定用户。");
			}
	}
	private void internetPONConf(Map<String, List<Map<String, String>>> preconfmap,String userId)
	{
		if (false == StringUtil.IsEmpty(userId))
		{
			// PON上网业务预配置
			if ("PON".equals(accessType) || "EPON".equals(accessType)
					|| "GPON".equals(accessType))
			{
				HgwServUserObj internetServobj = dao.queryHgwcustServUserByDevId(
						StringUtil.getLongValue(userId), 10);
				if (internetServobj != null && internetServobj.getVlanid() != null)
				{
					boolean falg = false;
					if (internetlist.size() == 0)
					{
						wideNetMsg = "0";
					}
					else
					{
						for (int j = 0; j < internetlist.size(); j++)
						{
							Map<String, String> rmap = (Map<String, String>) internetlist
									.get(j);
							if (StringUtil.getStringValue(rmap.get("vlan_id"))
									.equals(internetServobj.getVlanid()))
							{
								falg = true;
								break;
							}
						}
						if (falg == false)
						{
							wideNetMsg = "0";
						}
					}
				}
			}
		}
	}
	private void iptvPONConf(Map<String, List<Map<String, String>>> preconfmap,String userId)
	{
		logger.warn("IPTV预配置");
		List<Map<String, String>> iptvPreList = preconfmap.get("IPTV");
		for (int i = 0; i < iptvPreList.size(); i++)
		{
			logger.warn("IPTV预配置1");
			boolean falg = false;
			Map<String, String> tmap = iptvPreList.get(i);
			if (iptvlist.size() == 0)
			{
				logger.warn("IPTV预配置2");
				iptvMsg = "0";
			}
			else
			{
				for (int j = 0; j < iptvlist.size(); j++)
				{
					logger.warn("IPTV预配置3");
					Map<String, String> rmap = (Map<String, String>) iptvlist.get(j);
					if (StringUtil.getStringValue(tmap.get("vlan_id")).equals(
							StringUtil.getStringValue(rmap.get("vlan_id"))))
					{
						falg = true;
						break;
					}
				}
				if (falg == false)
				{
					iptvMsg = "0";
					break;
				}
			}
		}
	}
	private void voipPONConfig(Map<String, List<Map<String, String>>> preconfmap,String userId)
	{

			logger.debug("VOIP预配置7");
			if (false == StringUtil.IsEmpty(userId))
			{
				logger.debug("VOIP预配置8");
				List<HashMap<String, String>> voipuserlist = dao.getUserVOIP(userId);
					List<Map<String, String>> voipPreList = preconfmap.get("VOIP");
					logger.debug("VOIP预配置3");
					Map<String, String> emap = voipPreList.get(0);
					logger.debug("VOIP预配置4" + emap.get("vlan_id"));
					boolean falg1 = false;
					for (int i = 0; i < voipPVClist.size(); i++)
					{
						Map<String, String> vmap = (Map<String, String>) voipPVClist
								.get(i);
						if (StringUtil.getStringValue(emap.get("vlan_id")).equals(
								StringUtil.getStringValue(vmap.get("vlan_id"))))
						{
							falg1 = true;
						}
					}
					if (falg1)
					{
					}
					else
					{
						voipMsg = "0";
					}
			}
			else
			{
				voipMsg = "0";
				logger.warn("=====voipPONConf 未绑定用户。");
			}
	}
	
	public String getWideNetMsg()
	{
		return wideNetMsg;
	}
	
	public void setWideNetMsg(String wideNetMsg)
	{
		this.wideNetMsg = wideNetMsg;
	}
	
	public String getIptvMsg()
	{
		return iptvMsg;
	}
	
	public void setIptvMsg(String iptvMsg)
	{
		this.iptvMsg = iptvMsg;
	}
	
	public String getVoipMsg()
	{
		return voipMsg;
	}
	
	public void setVoipMsg(String voipMsg)
	{
		this.voipMsg = voipMsg;
	}
	
	public String getAccessType()
	{
		return accessType;
	}
	
	public void setAccessType(String accessType)
	{
		this.accessType = accessType;
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
	
	public List<HashMap<String, String>> getVoipPVClist()
	{
		return voipPVClist;
	}
	
	public void setVoipPVClist(List<HashMap<String, String>> voipPVClist)
	{
		this.voipPVClist = voipPVClist;
	}
	
	public List<HashMap<String, String>> getLinesList()
	{
		return linesList;
	}
	
	public void setLinesList(List<HashMap<String, String>> linesList)
	{
		this.linesList = linesList;
	}
	
	public List<HashMap<String, String>> getVoiceServiceList()
	{
		return voiceServiceList;
	}
	
	public void setVoiceServiceList(List<HashMap<String, String>> voiceServiceList)
	{
		this.voiceServiceList = voiceServiceList;
	}
	
	public List<HashMap<String, String>> getIGMPSnoopingList()
	{
		return IGMPSnoopingList;
	}
	
	public void setIGMPSnoopingList(List<HashMap<String, String>> iGMPSnoopingList)
	{
		IGMPSnoopingList = iGMPSnoopingList;
	}
	public DeviceConfigNewDAO getDao()
	{
		return dao;
	}
	
	public void setDao(DeviceConfigNewDAO dao)
	{
		this.dao = dao;
	}
	
	public List<HashMap<String, String>> getRegistStatusList()
	{
		return registStatusList;
	}
	
	public void setRegistStatusList(List<HashMap<String, String>> registStatusList)
	{
		this.registStatusList = registStatusList;
	}
	
}
