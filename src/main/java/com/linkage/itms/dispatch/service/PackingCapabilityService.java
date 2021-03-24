package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.PackingCapabilityChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class PackingCapabilityService implements IService {
	private static Logger logger = LoggerFactory.getLogger(PackingCapabilityService.class);

	@Override
	public String work(String inXml) {
		logger.warn("PackingCapabilityService==>inXml({})", inXml);
		PackingCapabilityChecker checker = new PackingCapabilityChecker(inXml);
		if (false == checker.check()) {
			logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		Map<String, String> userInfoMap = deviceInfoDAO.queryUserInfoForGS(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.size() == 0) {
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]查无此客户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		// 设备不存在
		if (StringUtil.IsEmpty(StringUtil.getStringValue(userInfoMap, "device_id", ""))) {
			checker.setResult(1003);
			checker.setResultDesc("未绑定设备");
			logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}

		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id");

		Map<String, String> map = deviceInfoDAO.queryDeviceInfo(deviceId);
		if (map != null && map.size() > 0) {
			checker.setCpetype(StringUtil.getStringValue(map, "devicetype"));
			checker.setCpemodle(StringUtil.getStringValue(map, "devicemodel"));
			checker.setCpewifi(StringUtil.getStringValue(map, "wifinum"));
			checker.setCpemaxspeed(StringUtil.getStringValue(map, "maxspeed"));
			checker.setCpelannum(StringUtil.getStringValue(map, "lannum"));
		}

		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (-6 == flag) {
			checker.setCpe_onlinestatus("0");
			checker.setResult(1008);
			checker.setResultDesc("设备正在被操作");
			logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]设备正在被操作，无法获取节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		if (1 == flag) {
			logger.warn("[{}]设备在线，正在进行采集操作", deviceId);
			checker.setCpe_onlinestatus("1");
			String[] gatherPath = new String[] { "InternetGatewayDevice.Services.X_CT-COM_MWBAND.TotalTerminalNumber",
					"InternetGatewayDevice.DeviceInfo.UpTime" };
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
			for (ParameValueOBJ pvobj : objLlist) {
				if (pvobj.getName().endsWith("TotalTerminalNumber")) {
					checker.setCepmaxuser(StringUtil.getStringValue(pvobj.getValue()));
				}
				if (pvobj.getName().endsWith("UpTime")) {
					checker.setCpeonlinetime(StringUtil.getStringValue(pvobj.getValue()));
				}
			}

			List<HashMap<String, String>> lansList = new ArrayList<HashMap<String, String>>();
			String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
			List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty() || iList.size() < 1) {
				checker.setResult(1009);
				checker.setResultDesc("获取Lan节点失败");
				logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]获取iList失败，返回",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				return checker.getReturnXml();
			}
			for (String i : iList) {
				String[] gatherLanPath = new String[] {
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig." + i + ".Status",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig." + i + ".MaxBitRate" };

				ArrayList<ParameValueOBJ> lanList = corba.getValue(deviceId, gatherLanPath);
				if (null == lanList || lanList.isEmpty() || lanList.size() < 1) {
					logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]获取lanList失败，返回",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					continue;
				}
				HashMap<String, String> tmp = new HashMap<String, String>();
				tmp.put("num", i);
				for (ParameValueOBJ pvobj : lanList) {
					if (pvobj.getName().endsWith("Status")) {
						tmp.put("Status", pvobj.getValue());
					} else if (pvobj.getName().endsWith("MaxBitRate")) {
						tmp.put("MaxBitRate", pvobj.getValue());
					}
				}
				lansList.add(tmp);
				tmp = null;
			}
			checker.setLansList(lansList);

			
			List<HashMap<String, String>> wansList = new ArrayList<HashMap<String, String>>();
			String wanPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
			List<String> iwanList = corba.getIList(deviceId, wanPath);
			if (null == iwanList || iwanList.isEmpty() || iwanList.size() < 1) {
				checker.setResult(1009);
				checker.setResultDesc("获取WIFI节点失败");
				logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]获取WIFI节点失败，返回",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				return checker.getReturnXml();
			}
			for (String i : iwanList) {
				String[] gatherWanPath = new String[] {
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration." + i + ".SSID",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration." + i + ".ChannelsInUse",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration." + i + ".X_CT-COM_PowerValue",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration." + i + ".Status",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration." + i + ".TotalAssociations",
						};

				ArrayList<ParameValueOBJ> wanList = corba.getValue(deviceId, gatherWanPath);
				if (null == wanList || wanList.isEmpty() || wanList.size() < 1) {
					logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]获取wanList失败，返回",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					continue;
				}
				
				String SSID="";
				String ChannelsInUse="";
				String PowerValue="";
				String Status="";
				String TotalAssociations="";
				for (ParameValueOBJ pvobj : wanList) {
					String value=pvobj.getValue();
					if (pvobj.getName().endsWith("SSID")) {
						SSID=value;
					} else if (pvobj.getName().endsWith("ChannelsInUse")) {
						ChannelsInUse=value;
					} else if (pvobj.getName().endsWith("X_CT-COM_PowerValue")) {
						PowerValue=value;
					} else if (pvobj.getName().endsWith("Status")) {
						Status=value;
					} else if (pvobj.getName().endsWith("TotalAssociations")) {
						TotalAssociations=value;
					}
				}
				if ("Up".equalsIgnoreCase(Status)) {
					HashMap<String, String> tmp = new HashMap<String, String>();
					tmp.put("num", i);
					tmp.put("SSID", SSID);
					tmp.put("ChannelsInUse", ChannelsInUse);
					tmp.put("X_CT-COM_PowerValue", PowerValue);
					tmp.put("Status", Status);
					tmp.put("TotalAssociations", TotalAssociations);
					wansList.add(tmp);
					tmp = null;
				}
				
			}
			checker.setWansList(wansList);
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "packingCapability");
			return checker.getReturnXml();
		} else {
			checker.setResult(0);
			checker.setResultDesc("设备不在线");
			checker.setCpe_onlinestatus("0");
			logger.warn("servicename[PackingCapabilityService]cmdId[{}]userInfo[{}]设备不在线",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
	}

}
