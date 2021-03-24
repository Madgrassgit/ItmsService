package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetWlanConnStatusInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * traceroute
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetWLanConnStatusInfoService
{
	private static Logger logger = LoggerFactory.getLogger(GetWLanConnStatusInfoService.class);
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;


	public String work(String inXml) {
		logger.warn("servicename[GetWLanConnStatusInfoService]执行，入参为：{}", inXml);
		GetWlanConnStatusInfoDealXML deal = new GetWlanConnStatusInfoDealXML();
		
		//校验入参
		Document document = deal.getXML(inXml);
		if (document == null) 
		{
			logger.warn("servicename[GetWLanConnStatusInfoService]解析入参错误！");
			return deal.returnXML();
		}
		else
		{
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			Map<String, String> userMap  = null;
			if(!StringUtil.IsEmpty(deal.getPppUsename()))
			{
				userMap = userDevDao.queryUserInfo(USERINFOTYPE_1, deal.getPppUsename(), null);
			}
			else if(!StringUtil.IsEmpty(deal.getLogicId()))
			{
				userMap = userDevDao.queryUserInfo(USERINFOTYPE_2, deal.getLogicId(), null);
			}
			
			if (userMap == null || userMap.isEmpty())
			{
				deal.setResult("-1");
				deal.setErrMsg("无此用户信息");
				return deal.returnXML();
			}
			if (StringUtil.IsEmpty(userMap.get("device_id")))
			{
				deal.setResult("-99");
				deal.setErrMsg("未绑定设备");
				return deal.returnXML();
			}
			
			String deviceId = StringUtil.getStringValue(userMap, "device_id", "");
			
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			
			// 设备正在被操作，不能获取节点值
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("设备不能正常交互");
				logger.warn("return=({})", deal.returnXML());  // 打印回参
				return deal.returnXML();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
				
			    String lanPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
			    List<String> iList = corba.getIList(deviceId, lanPath);
				if (null == iList || iList.isEmpty())
				{
					logger.warn("[{}]获取iList失败，返回", deviceId);
					deal.setResult("-99");
					if("nmg_dx".equals(Global.G_instArea)){
						deal.setErrMsg("该设备没有WIFI功能不支持查询");
					} else {
						deal.setErrMsg("节点值没有获取到，请确认节点路径是否正确");
					}
					return deal.returnXML();
				}else{
					logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
				}
				
				//InternetGatewayDevice.LANDevice.{i}.Hosts.Host.{i}.MACAddress
				List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();
				for(String i : iList){
					String[] gatherPath = new String[]{
							"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".SSID",
							"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".Status",
							"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".TotalBytesReceived",
							"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".TotalBytesSent"
							};
					
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
					if (null == objLlist || objLlist.isEmpty()) {
						continue;
					}
					HashMap<String,String> tmp = new HashMap<String,String>();
					
					String enable = "";
					String ssid = "";
					String received = "";
					String sent = "";
					for(ParameValueOBJ pvobj : objLlist){
						if(pvobj.getName().contains("SSID")){
							ssid = pvobj.getValue();
						}else if(pvobj.getName().contains("Status")){
							enable = pvobj.getValue();
						}else if(pvobj.getName().contains("TotalBytesReceived")){
							received = pvobj.getValue();
						}else if(pvobj.getName().contains("TotalBytesSent")){
							sent = pvobj.getValue();
						}
					}
					if("Up".equalsIgnoreCase(enable)){
						enable = "0";
					}else if("Disabled".equalsIgnoreCase(enable)){
						enable = "1";
					}else{
						enable = "2";
					}
					
					tmp.put("SSIDname", ssid);
					tmp.put("RstState", enable);
					tmp.put("BytesReceived", received);
					tmp.put("BytesSent", sent);
					
					wanList.add(tmp);
					tmp = null;
					objLlist.clear();
					gatherPath = null;
					enable = null;
					ssid = null;
					received = null;
					sent = null;
				}
				deal.setWanList(wanList);
				// 记录日志
				//new RecordLogDAO().recordDispatchLog(deal, deal.getUserInfo(),"QueryWlanStateService");
							
				return deal.returnXML();
				
			}
			// 设备不在线，不能获取节点值
			else {
				logger.warn("设备不在线，无法获取节点值");
				deal.setResult("-99");
				deal.setErrMsg("设备不能正常交互");
				logger.warn("return=({})", deal.returnXML());  // 打印回参
				return deal.returnXML();
			}
			
		}
	}
}
