package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryWiFiDeviceMACChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QueryWiFiDeviceMACService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryWiFiDeviceMACService.class);
	
	
	public String work(String inXml){
		
		logger.warn("QueryDeviceMACService：inXml({})", inXml);
		
		QueryWiFiDeviceMACChecker checker = new QueryWiFiDeviceMACChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if(checker.getDevSn() != null && !"".equals(checker.getDevSn())){
			if(checker.getDevSn().length() < 6){
				checker.setResult(1001);
				checker.setResultDesc("用户信息非法，请输入设备序列号至少后六位");
				return checker.getReturnXml();
			}
			String dev_sub_sn = checker.getDevSn().substring(checker.getDevSn().length()-6,checker.getDevSn().length());
			userMap = qdDao.queryUserByDevSN(checker.getDevSn(),dev_sub_sn);
		}else{
			if (checker.getUserInfoType() == 1)
			{
				userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2)
			{
				userMap = qdDao.queryUserByLoid(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 3)
			{
				userMap = qdDao.queryUserByIptvAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 4)
			{
				userMap = qdDao.queryUserByVoipPhone(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 5)
			{
				userMap = qdDao.queryUserByVoipAccount(checker.getUserInfo());
			}else{
			}
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			String[] gatherPath = new String[]{"AssociatedDeviceMACAddress"};
		    String ssidPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration." + checker.getSsidNum() + ".AssociatedDevice.";
		    List<String> iList = corba.getIList(deviceId, ssidPath);
		    
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				checker.setResultDesc("该ssid下没有挂设备或者请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			List<HashMap<String,String>> macList = new ArrayList<HashMap<String,String>>();
			for(String i : iList){
				logger.warn("path:" + ssidPath + i);
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, ssidPath + i + "." + gatherPath[0]);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp.put("macId", i);
				tmp.put("mac", objLlist.get(0).getValue());
				macList.add(tmp);
				tmp = null;
			}
			checker.setMacList(macList);
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryDeviceMACService");
						
			return checker.getReturnXml();
			
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
