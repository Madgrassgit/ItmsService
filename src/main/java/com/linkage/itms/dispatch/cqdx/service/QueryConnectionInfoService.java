package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryConnectionInDealXML;

public class QueryConnectionInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryConnectionInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryConnectionInfoService]执行，入参为：{}", inXml);
		QueryConnectionInDealXML deal = new QueryConnectionInDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryConnectionInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();

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
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn
			userType = 6;
			username = serialNumber;
		}
		else {
			logger.warn("servicename[QueryConnectionInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		Map<String, String> userMap = userDevDao.queryUserInfo(userType, username, "");
		String deviceId = StringUtil.getStringValue(userMap, "device_id");
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[QueryConnectionInfoService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("servicename[QueryConnectionInfoService]未绑定设备！");
			deal.setResult("-99");
			deal.setErrMsg("未绑定设备！");
			return deal.returnXML();
		}
		
		logger.warn("serviceName[QueryConnectionInfoService]deviceid[{}]开始采集", deviceId);
		//掉CORBAR 采集    0表示采集所有节点 在原来基础上增加了一个参数(3)
		int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); 
		logger.warn("serviceName[QueryConnectionInfoService]deviceid[{}]采集结果[{}]", new Object[] {deviceId, rsint});
		// 采集失败
		if (rsint != 1) {
			logger.warn("serviceName[QueryConnectionInfoService]deviceid[{}]getData sg fail", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备采集失败！");
			return deal.returnXML();
		}
		DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();
		Map<String,String> ponInfoMap = deviceConfigDao.getPonInfo(deviceId);
		List<HashMap<String,String>> list = deviceConfigDao.getAllChannel(deviceId);
		
		deal.setSendPower(StringUtil.getStringValue(ponInfoMap, "tx_power"));
		deal.setReceivePower(StringUtil.getStringValue(ponInfoMap, "rx_power"));
		deal.setList(list);
		deal.setResult("0");
		deal.setErrMsg("成功！");
		String ret = deal.returnXML();
		deal.recordLog("QueryConnectionInfoService", username, serialNumber, inXml, ret);
		return ret;
	}
}
