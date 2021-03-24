package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetLanConnectionStatusInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 家庭网关LAN口连接状态查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetLanConnectionStatusInfoService {
	private static Logger logger = LoggerFactory.getLogger(GetLanConnectionStatusInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[GetLanConnectionStatusInfoService]执行，入参为：{}", inXml);
		GetLanConnectionStatusInfoDealXML deal = new GetLanConnectionStatusInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GetLanConnectionStatusInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		if("".equals(logicId) && "".equals(pppUsename)){
			logger.warn("servicename[GetLanConnectionStatusInfoService]宽带账号和逻辑账号不能同时为空！");
			deal.setResult("-99");
			deal.setErrMsg("宽带账号和逻辑账号不能同时为空！");
			return deal.returnXML();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (!"".equals(pppUsename)){
			userMap = qdDao.queryUserByNetAccount(pppUsename);
		}else{
			userMap = qdDao.queryUserByLoid(logicId);
		}
		
		if (userMap.size() > 1)
		{
			deal.setResult("-99");
			deal.setErrMsg("数据不唯一，请使用逻辑SN查询");
			return deal.returnXML();
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			logger.warn("servicename[GetLanConnectionStatusInfoService]loid[{}]查无此用户",
					new Object[] { logicId });
			deal.setResult("-1");
			deal.setErrMsg("用户不存在");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{// 用户未绑定终端
			logger.warn("servicename[GetLanConnectionStatusInfoService]loid[{}]此客户未绑定",
					new Object[] { logicId });
			deal.setResult("-99");
			deal.setErrMsg("此客户未绑定");
			return deal.returnXML();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
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
			
		    String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		    List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("设备不支持LAN口状态查询");
				return deal.returnXML();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();
			for(String i : iList){
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".MACAddress",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.PacketsReceived",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.PacketsSent"};
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				
				String status = "";
				String macAddress = "";
				String received = "";
				String sent = "";
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						status = pvobj.getValue();
					}else if(pvobj.getName().contains("MACAddress")){
						macAddress = pvobj.getValue();
					}else if(pvobj.getName().contains("PacketsReceived")){
						received = pvobj.getValue();
					}else if(pvobj.getName().contains("PacketsSent")){
						sent = pvobj.getValue();
					}
				}
				if("Up".equalsIgnoreCase(status)){
					status = "0";
				}else if("Disabled".equalsIgnoreCase(status)){
					status = "1";
				}else if("NoLink".equalsIgnoreCase(status)){
					status = "3";
				}else{
					status = "2";
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp.put("LanPortNUM", i);
				tmp.put("name", "LAN"+i);
				tmp.put("status", status);
				tmp.put("macAddress", macAddress);
				tmp.put("PacketsReceived", received);
				tmp.put("PacketsSent", sent);
				lanList.add(tmp);
				tmp = null;
				status = null;
				received = null;
				macAddress = null;
				sent = null;
			}
			deal.setLanList(lanList);
			logger.warn("成功");
			deal.setResult("0");
			deal.setErrMsg("成功");
			return deal.returnXML();
			
		}// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
			logger.warn("return=({})", deal.returnXML());  // 打印回参
			return deal.returnXML();
		}
	
	}
}
