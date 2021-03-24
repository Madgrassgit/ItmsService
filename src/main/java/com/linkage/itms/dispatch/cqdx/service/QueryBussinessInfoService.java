package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryBussinessInfoDealXML;

public class QueryBussinessInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryBussinessInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryBussinessInfoService]执行，入参为：{}", inXml);
		QueryBussinessInfoDealXML deal = new QueryBussinessInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryBussinessInfoService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		PublicDAO dao = new PublicDAO();
		List<HashMap<String, String>> list = null;

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		int userType = 0;
		String username = "";
		UserDeviceDAO userDeviceDao = new UserDeviceDAO();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2; 
			username = logicId;
			
			if(StringUtil.isEmpty(pppUsename)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(2, logicId);
				if (null != userInfoMap && !userInfoMap.isEmpty())
				{
					deal.setPppUsename(StringUtil.getStringValue(userInfoMap, "username"));
				}
			}
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1; 
			username = pppUsename;
			
			if(StringUtil.isEmpty(logicId)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(1, pppUsename);
				if (null != userInfoMap && !userInfoMap.isEmpty())
				{
					deal.setLogicId(StringUtil.getStringValue(userInfoMap, "loid"));
				}
			}
		}
		else {
			logger.warn("servicename[QueryBussinessInfoService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		list = dao.getBussinessInfo(2, deal.getLogicId(), "1");
		if (list == null || list.isEmpty()) {
			logger.warn("servicename[QueryBussinessInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		
		StringBuffer serviceList = new StringBuffer();
		StringBuffer broadband = new StringBuffer();
		StringBuffer iptv = new StringBuffer();
		StringBuffer voip = new StringBuffer();
		for (HashMap<String, String> map : list) {
			deal.setLogicId(StringUtil.getStringValue(map, "loid"));
			deal.setSerialNumber(StringUtil.getStringValue(map, "device_serialnumber"));
			deal.setTerminalType(new UserDeviceDAO().queryDeviceTypeName(StringUtil.getStringValue(map, "device_serialnumber")));
			if ("10".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
				broadband.append("broadband|");
				deal.setPppUsename(StringUtil.getStringValue(map, "pppusename"));
			}
			if ("11".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
				iptv.append("iptv|");
			}
//			if ("14".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
//				broadband.append("voip|");
//			}
		}
		// 查询语音开通数
		List<HashMap<String, String>> listVoip = dao.getVoipBussinessInfo(2, deal.getLogicId());
		if(null != listVoip && !listVoip.isEmpty() && null != listVoip.get(0)){
			for(HashMap<String, String> mapVoip : listVoip){
				voip.append("voip|");
			}
		}
		
		if (broadband.length() > 0) {
			serviceList.append("broadband");
		}
		if (iptv.length() > 0) {
			if(broadband.length() > 0){
				serviceList.append("|");
			}
			serviceList.append("iptv");	
		}
		if (voip.length() > 0) {
			if(broadband.length() > 0 || iptv.length() > 0){
				serviceList.append("|");
			}
			serviceList.append("voip");
		}
		deal.setServiceList(serviceList.toString());
		deal.setResult("0");
		deal.setErrMsg("成功！");
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("QueryBussinessInfoService", username, deal.getSerialNumber(), inXml, ret);
		return ret;
	}
}
