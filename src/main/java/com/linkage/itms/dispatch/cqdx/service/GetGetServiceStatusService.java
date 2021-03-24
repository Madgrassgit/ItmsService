package com.linkage.itms.dispatch.cqdx.service;

import java.util.Arrays;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetGetServiceStatusDealXML;

public class GetGetServiceStatusService {
	private static Logger logger = LoggerFactory.getLogger(GetGetServiceStatusService.class);

	public String work(String inXml) {
		logger.warn("servicename[GetGetServiceStatusService]执行，入参为：{}", inXml);
		GetGetServiceStatusDealXML deal = new GetGetServiceStatusDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GetGetServiceStatusService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		String customerId = deal.getCustomerId();
		String userInfo = "";
		int userInfoType = 0;
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userInfo = logicId;
			userInfoType = 2;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userInfo = pppUsename;
			userInfoType = 1;
		}
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn
			userInfo = serialNumber;
			userInfoType = 3;
		}else if(!StringUtil.IsEmpty(customerId)) {
			// 客户ID
			userInfo = customerId;
			userInfoType = 7;
		}else {
			logger.warn("servicename[GetGetServiceStatusService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		// 查询用户业务信息
		PublicDAO dao = new PublicDAO();
		Map<String, String> userMap = dao.queryUserInfo(userInfoType, userInfo);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[StartGetUserInfoDiagService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		deal.setResult("0");
		deal.setErrMsg("成功！");
		
		// 宽带业务状态
		if("0".equals(userMap.get("net_status"))){
			deal.setNetStatus("1");
		}else if("1".equals(userMap.get("net_status"))){
			deal.setNetStatus("2");
		}else{
			deal.setNetStatus("0");
		}
		
		// IPTV业务状态
		String[] iptvStatusArr = StringUtil.getStringValue(userMap.get("iptv_status")).split("\\|");
		if(Arrays.binarySearch(iptvStatusArr, "1") >=0){
			deal.setIptvStatus("2");
		}else if(Arrays.binarySearch(iptvStatusArr, "0") >=0){
			deal.setIptvStatus("1");
		}else{
			deal.setIptvStatus("0");
		}
		
		// 语音业务状态
//		String voipNum = "";
//		List<HashMap<String, String>> tabBssDevList = dao.getTabBssDevPortInfo(userMap.get("spec_id"));
//		if(null != tabBssDevList && !tabBssDevList.isEmpty() && null != tabBssDevList.get(0)){
//			voipNum = tabBssDevList.get(0).get("voice_num");
//		}
		
		String[] voipPortArr = StringUtil.getStringValue(userMap.get("voip_port")).split("\\|");
		String[] voipStatusArr = StringUtil.getStringValue(userMap.get("voip_status")).split("\\|");
		for(String vipPort : voipPortArr){
			if(vipPort.endsWith("00") || "0".equals(vipPort)){
				String voipStatus = voipStatusArr[Arrays.binarySearch(voipPortArr, vipPort)];
				if("0".equals(voipStatus)){
					deal.setVoip1Status("1");
				}else if("1".equals(voipStatus)){
					deal.setVoip1Status("2");
				}else{
					deal.setVoip1Status("0");
				}
			}
			if(vipPort.endsWith("01") || "1".equals(vipPort)){
				String voipStatus = voipStatusArr[Arrays.binarySearch(voipPortArr, vipPort)];
				if("0".equals(voipStatus)){
					deal.setVoip2Status("1");
				}else if("1".equals(voipStatus)){
					deal.setVoip2Status("2");
				}else{
					deal.setVoip2Status("0");
				}
			}
		}
		return deal.returnXML();
	}
}
