package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.GetParameterValuesChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GetParameterValuesService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(GetParameterValuesService.class);
	
	
	public String work(String inXml){
		
		logger.warn("getParameterValues：inXml({})", inXml);
		
		GetParameterValuesChecker checker = new GetParameterValuesChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		
		String deviceId = "";
		
		// 根据设备序列号查询设备
		if (6 == checker.getUserInfoType()) {
			ArrayList<HashMap<String, String>> arrayList = userDevDao.getDevStatusInfo(checker.getUserInfo());
			if (null == arrayList || arrayList.isEmpty()) {
				checker.setResult(1004);
				checker.setResultDesc("此设备不存在");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			HashMap<String, String> map = arrayList.get(0);
			
			deviceId = StringUtil.getStringValue(map, "device_id", "");
			
			// 判断设备是否在线，只有设备在线，才可以获取设备的节点信息
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			
			// 设备正在被操作，不能获取节点值
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1008);
				checker.setResultDesc("设备正在被操作");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以获取节点值，device_id={}", deviceId);
				
				String [] arr = checker.getPathStr();
				
				// 调用Corba 获取节点的值
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr);
				
				if (null == objLlist || objLlist.isEmpty()) {
					checker.setResult(1009);
					checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				
				// 将获取到的节点值 塞进XML的节点，然后将XML返回
				checker.setParameterValues(objLlist);
				
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
				
			}
			// 设备不在线，不能获取节点值
			else {
				logger.warn("设备不在线，无法获取节点值");
				checker.setResult(1005);
				checker.setResultDesc("设备不在线，无法获取节点值");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
		}
		// 根据用户信息查询设备
		else {
			// 查询用户信息
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
					.getUserInfoType(), checker.getUserInfo());
			
			// 用户未绑定设备
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn("此用户未绑定设备："+checker.getUserInfo());
				checker.setResult(1002);
				checker.setResultDesc("查不到对应的客户信息");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			// 用户绑定了设备
			else {
				deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
				
				if ("".equals(deviceId)) {
					logger.warn("此用户没有设备关联信息："+checker.getUserInfo());
					checker.setResult(1003);
					checker.setResultDesc("此用户没有设备关联信息");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				
				// 判断设备是否在线，只有设备在线，才可以获取设备的节点信息
				int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
				
				// 设备正在被操作
				if (-3 == flag) {
					logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
					checker.setResult(1008);
					checker.setResultDesc("设备正在被操作，无法获取节点值，请稍候重新获取");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag) {
					logger.warn("设备在线，可以获取节点值，device_id={}", deviceId);
					
					String [] arr = checker.getPathStr();
					
					// 调用Corba 获取节点的值
					logger.warn("调用Corba，获取节点值");
					
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr);
					logger.warn("===objLlist=={}=======",objLlist);
					
					if (null == objLlist || objLlist.isEmpty()) {
						checker.setResult(1009);
						checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
						logger.warn("return=({})", checker.getReturnXml());  // 打印回参
						return checker.getReturnXml();
					}
					
					// 将获取到的节点值 塞进XML的节点，然后将XML返回
					checker.setParameterValues(objLlist);
					
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
					
				}
				// 设备不在线
				else {
					logger.warn("设备不在线，无法获取节点值");
					checker.setResult(1005);
					checker.setResultDesc("设备不在线，无法获取节点值");
					logger.warn("getParameterValues：return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
			}
		}
	}
}
