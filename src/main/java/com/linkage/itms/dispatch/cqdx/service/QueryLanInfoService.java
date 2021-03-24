package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryLanInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryLanInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryLanInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryLanInfoService]执行，入参为：{}", inXml);
		QueryLanInfoDealXML deal = new QueryLanInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryLanInfoService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
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
		else if(!StringUtil.IsEmpty(serialNumber) && serialNumber.length()>=6) {
			// 设备sn 
			userType = 6;
			username = serialNumber;
			
		}
		else {
			logger.warn("servicename[QueryLanInfoService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		userMap = dao.queryUserInfoLan(userType, username);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[QueryLanInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get("device_id"))) {
			logger.warn("servicename[QueryLanInfoService]未绑定设备！");
			deal.setResult("-11");
			deal.setErrMsg("未绑定设备！");
			return deal.returnXML();
		}
		String deviceId = StringUtil.getStringValue(userMap, "device_id");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-11");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
		    String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		    List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty()) {
				logger.warn("[{}]获取iList失败，返回", deviceId);
				deal.setResult("-11");
				deal.setErrMsg("节点值没有获取到，请确认节点路径是否正确!");
				return deal.returnXML();
			}
			logger.debug("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
			List<Map<String,String>> lanList = new ArrayList<Map<String,String>>();
			for(String i : iList){
				String[] gatherPath = new String[]{
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status",
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesReceived",
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesSent",
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.PacketsReceived",
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.PacketsSent",
					"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".MaxBitRate"
				};
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				logger.debug("[{}]获取objLlist成功，objLlist.size={}", deviceId, objLlist.size());
				
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp.put("name", i);
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						tmp.put("RstState", pvobj.getValue());
					}
					else if(pvobj.getName().contains("BytesReceived")){
						tmp.put("BytesReceived", pvobj.getValue());
					}
					else if(pvobj.getName().contains("BytesSent")){
						tmp.put("BytesSent", pvobj.getValue());
					}
					else if(pvobj.getName().contains("PacketsReceived")){
						tmp.put("PacketsReceived", pvobj.getValue());
					}
					else if(pvobj.getName().contains("PacketsSent")){
						tmp.put("PacketsSent", pvobj.getValue());
					}
					else if(pvobj.getName().contains("MaxBitRate")){
						tmp.put("MaxBitRate", pvobj.getValue());
					}
				}
				logger.debug("tmp="+tmp);
				lanList.add(tmp);
			}
			logger.warn("lanList.size="+lanList.size());
			deal.setList(lanList);
			deal.setResult("1");
			deal.setErrMsg("成功！");
			String ret = deal.returnXML();
			// 日志
			deal.recordLog("QueryLanInfoService", username, serialNumber, inXml, ret);
			return ret;
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			deal.setResult("-11");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
	}
}
