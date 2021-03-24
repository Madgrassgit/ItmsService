package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetWANConnectionStatusInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class GetWANConnectionStatusInfoService {
	private static Logger logger = LoggerFactory.getLogger(GetWANConnectionStatusInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[GetWANConnectionStatusInfoService]执行，入参为：{}", inXml);
		GetWANConnectionStatusInfoDealXML deal = new GetWANConnectionStatusInfoDealXML(inXml);
		if (false == deal.check()) {
			logger.warn("servicename[GetWANConnectionStatusInfoService]入参存在问题！");
			return deal.returnXML();
		}
		
		Map<String, String> userMap = null;
		PublicDAO dao = new PublicDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		//String serialNumber = deal.getSerialNumber();
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
			logger.warn("servicename[GetWANConnectionStatusInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		userMap = dao.queryUserInfoLan(userType, username);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[GetWANConnectionStatusInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get("device_id"))) {
			logger.warn("servicename[GetWANConnectionStatusInfoService]未绑定设备！");
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
			
			//采集accessType
			/*String accessType = null;
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			accessType = userDevDao.getAccType(deviceId);
			if (null == accessType || "null".equals(accessType) || "".equals(accessType))
			{
				String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
				ArrayList<ParameValueOBJ> accessTypeList = corba.getValue(deviceId, accessTypePath);
				if (accessTypeList != null && accessTypeList.size() != 0) {
					for (ParameValueOBJ pvobj : accessTypeList) {
						if (pvobj.getName().endsWith("WANAccessType")) {
							accessType = pvobj.getValue();
						}
					}
				}
			}*/
			//logger.warn("accessType为：[{}]", accessType);
			String checkAccessType = ".WANCommonInterfaceConfig";
			/*if("EPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANEponLinkConfig";
			}else if("GPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANGponLinkConfig";
			}else{
				logger.warn("[{}]accessType既不是EPON也不是GPON", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("上行方式既不是EPON也不是GPON");
				return deal.returnXML();
			}*/
			String wanConnPath = "InternetGatewayDevice.WANDevice.1";
			//String statusPath = ".Status";
			String statusPath = ".PhysicalLinkStatus";
		    String gatherPath = wanConnPath + checkAccessType + statusPath;
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
			if (null == objLlist || objLlist.isEmpty()) {
				logger.warn("[{}]采集失败", deviceId);
				deal.setResult("-99");
				deal.setErrMsg("采集失败，设备没有响应");
				return deal.returnXML();
			}
			for(ParameValueOBJ pvobj : objLlist){
				if(pvobj.getName().contains("Status")){
					String status = pvobj.getValue();
					deal.setStatus(status);
				}
			}
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("[{}]设备不在线，无法获取节点值", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互！");
			return deal.returnXML();
		}
		return deal.returnXML();
	}
}
