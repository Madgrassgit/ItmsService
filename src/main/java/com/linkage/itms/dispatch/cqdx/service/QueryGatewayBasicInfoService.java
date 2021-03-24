package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryGatewayBasicInfoDealXML;

public class QueryGatewayBasicInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryGatewayBasicInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryGatewayBasicInfoService]执行，入参为：{}", inXml);
		QueryGatewayBasicInfoDealXML deal = new QueryGatewayBasicInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryGatewayBasicInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String netName = deal.getPppUsename();
		
		PublicDAO publicDAO = new PublicDAO();
		List<HashMap<String, String>> servInfoMapList = new ArrayList<HashMap<String,String>>();
		List<HashMap<String, String>> servVoipMapList = new ArrayList<HashMap<String,String>>();
		if(!StringUtil.IsEmpty(logicId)){
			
			// 查询业务信息
			servInfoMapList = publicDAO.getBussinessInfo(2, logicId, null);
			if(null != servInfoMapList && !servInfoMapList.isEmpty() && null != servInfoMapList.get(0)){
				servVoipMapList = publicDAO.getVoipBussinessInfo(2, logicId);
			}
			
		}else if(!StringUtil.IsEmpty(netName)){
			// 查询业务信息
			servInfoMapList = publicDAO.getBussinessInfo(1, netName, null);
			if(null != servInfoMapList && !servInfoMapList.isEmpty() && null != servInfoMapList.get(0)){
				servVoipMapList = publicDAO.getVoipBussinessInfo(1, netName);
			}
		}else{
			logger.warn("servicename[QueryTerminalInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		String ppp_username="";
		String voip_name="";
		String area_code="";
		String register_status="";
		String manufacturer="";
		String device_type="";
		String hardware_version="";
		String access_time="";
		String serial_number="";
		String uplink_type="";
		String uplink = "";
		String lan_count="";
		String wireless_type="";
		String terminal_type="";
		String ip_address="";
		String mac_address="";
		String device_id = "";
		if(null != servInfoMapList && !servInfoMapList.isEmpty() && null != servInfoMapList.get(0)){
			// 遍历业务信息
			for(HashMap<String, String> servInfoMap : servInfoMapList){
				if(StringUtil.IsEmpty(serial_number)){
					serial_number = StringUtil.getStringValue(servInfoMap.get("device_serialnumber"));
				}
				if(StringUtil.IsEmpty(device_id)){
					device_id = StringUtil.getStringValue(servInfoMap.get("device_id"));
				}
				if("10".equals(StringUtil.getStringValue(servInfoMap, "serv_type_id"))){
					ppp_username = StringUtil.getStringValue(servInfoMap.get("pppusename"));
				}
			}
			// 遍历语音业务信息
			if(null != servVoipMapList && !servVoipMapList.isEmpty() && null != servVoipMapList.get(0)){
				for(HashMap<String, String> servVoipMap : servVoipMapList){
					if(StringUtil.IsEmpty(voip_name)){
						voip_name = StringUtil.getStringValue(servVoipMap.get("voip_username"));
					}else{
						voip_name = voip_name + "," + StringUtil.getStringValue(servVoipMap.get("voip_username"));
					}
				}
			}
			// 查询设备相关信息
			if(!StringUtil.IsEmpty(device_id)){
				logger.warn("设备信息不为空，进行处理");
				List<HashMap<String,String>> deviceInfoList =  publicDAO.getDeviceInfo(device_id);
				logger.warn("deviceInfoList="+deviceInfoList.size()+", deviceInfoList.get(0)="+deviceInfoList.get(0));
				if(null != deviceInfoList && !deviceInfoList.isEmpty() && null != deviceInfoList.get(0)){
					area_code = StringUtil.getStringValue(deviceInfoList.get(0).get("city_id"));
					register_status = StringUtil.getStringValue(deviceInfoList.get(0).get("device_status"));
					manufacturer = StringUtil.getStringValue(deviceInfoList.get(0).get("vendor_name"));
					device_type = StringUtil.getStringValue(deviceInfoList.get(0).get("device_model"));
					hardware_version = StringUtil.getStringValue(deviceInfoList.get(0).get("hardwareversion"));
					access_time = new DateTimeUtil(StringUtil.getLongValue((deviceInfoList.get(0).get("complete_time")))).getYYYYMMDDHHMMSS();
					uplink_type = getAccessNameType(StringUtil.getStringValue(deviceInfoList.get(0).get("access_style_relay_id")));
					uplink = getAccessName(StringUtil.getStringValue(deviceInfoList.get(0).get("access_style_relay_id")));
					ip_address = StringUtil.getStringValue(deviceInfoList.get(0).get("loopback_ip"));
					mac_address = StringUtil.getStringValue(deviceInfoList.get(0).get("cpe_mac"));
					terminal_type = manufacturer + "-" + uplink + "-" + StringUtil.getStringValue(deviceInfoList.get(0).get("oui")) + device_type;
					
					// 查询终端规格
					String specId = StringUtil.getStringValue(deviceInfoList.get(0).get("spec_id"));
					if(!StringUtil.IsEmpty(specId)){
						List<HashMap<String,String>> specList = publicDAO.getTabBssDevPortInfo(specId);
						if(null != specList && !specList.isEmpty() && null != specList.get(0)){
							lan_count = StringUtil.getStringValue(specList.get(0).get("lan_num"));
							wireless_type = getWanType(StringUtil.getStringValue(specList.get(0).get("wlan_num")));
						}
					}
				}
			}
			deal.setResult("0");
			deal.setErrMsg("执行成功！");
			deal.setPpp_username(ppp_username);
			deal.setVoip_name(voip_name);
			deal.setArea_code(area_code);
			deal.setRegister_status(register_status);
			deal.setManufacturer(manufacturer);
			deal.setDevice_type(device_type);
			deal.setHardware_version(hardware_version);
			deal.setAccess_time(access_time);
			deal.setUplink_type(uplink_type);
			deal.setIp_address(ip_address);
			deal.setMac_address(mac_address);
			deal.setTerminal_type(terminal_type);
			deal.setLan_count(lan_count);
			deal.setWireless_type(wireless_type);
			deal.setSerial_number(serial_number);
		}else{
			logger.warn("servicename[QueryTerminalInfoService]用户不存在！");
			deal.setResult("-1");
			deal.setErrMsg("用户不存在！");
			return deal.returnXML();
		}
		// 记录日志
		String ret = deal.returnXML();
		logger.warn("ret=" + ret);
		deal.recordLog("QueryGatewayBasicInfoService", "", "", inXml, ret);
		return deal.returnXML();
	}
	
	private String getAccessNameType(String accessTypeId){
		if("1".equals(accessTypeId)){
			return "02";
		}
		if("2".equals(accessTypeId)){
			return "03";
		}
		if("3".equals(accessTypeId) || "4".equals(accessTypeId)){
			return "01";
		}
		return "01";
	}
	
	private String getAccessName(String accessTypeId){
		if("1".equals(accessTypeId)){
			return "ADSL";
		}
		if("2".equals(accessTypeId)){
			return "LAN";
		}
		if("3".equals(accessTypeId) || "4".equals(accessTypeId)){
			return "EPON";
		}
		if("4".equals(accessTypeId)){
			return "GPON";
		}
		return "EPON";
	}
	
	private String getWanType(String wanNum){
		if(0 == StringUtil.getIntegerValue(wanNum)){
			return "0";
		}
		return "1";
	}
}
