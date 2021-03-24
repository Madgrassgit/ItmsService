package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.OpenOrCloseAwifiChecker;


public class OpenOrCloseAwifiService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(OpenOrCloseAwifiService.class);
	
	
	public String work(String inXml){
		
		logger.warn("OpenOrCloseAwifi：inXml({})", inXml);
		
		OpenOrCloseAwifiChecker checker = new OpenOrCloseAwifiChecker(inXml);
		
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
			logger.warn("设备在线，可以进行关闭操作，device_id={}", deviceId);
			
			String ssid = checker.getSsid();
			String vlanIdMark = "32";
			int wireless_port = 4;
			int buss_level = 7;
			if("3".equals(ssid)){
				vlanIdMark = "33";
				wireless_port = 3;
			}
			
			
			String serviceId="2003";
			long time = new DateTimeUtil().getLongTime();  //入表时间，同时为任务id
			String res ="0";
			try{
	        	
				qdDao.doConfig(StringUtil.getLongValue(userMap.get(0), "user_id"),deviceId,serviceId,"1",vlanIdMark,ssid,time,wireless_port,buss_level);
	    		res = "1";
		    }catch (Exception e) {
		    	logger.warn("更新表失败");
		    	res = "-1";
		    	checker.setResult(1003);
				checker.setResultDesc("调用失败");
				return checker.getReturnXml();
			}
			
			if("1".equals(res)){
	        	if (true==CreateObjectFactory.createPreProcess().processDeviceStrategy(new String[]{deviceId},serviceId,new String[]{StringUtil.getStringValue(time)})){
	    			logger.debug("调用后台预读模块成功");
	    		} else {
	    			logger.warn("调用后台预读模块失败");
	    			checker.setResult(1003);
	    			checker.setResultDesc("调用失败");
	    			return checker.getReturnXml();
	    		}
	        }
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"OpenOrCloseAwifi");
						
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
