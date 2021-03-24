package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.CloudQueryGetWayChecker;


public class CloudQueryGetWayService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryGetWayService.class);

	@Override
	public String work(String inXml) {
		CloudQueryGetWayChecker checker = new CloudQueryGetWayChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryGetWayService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudQueryGetWayService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
			
			QueryDevDAO qdDao = new QueryDevDAO();
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccountCloud(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoidCloud(checker.getUserInfo());
			}
			
			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("查询不到对应用户");
				checker.setIsNet("0");
				return checker.getReturnXml();
			}
//			if (userMap.size() > 1) {
//				checker.setResult(5);
//				checker.setResultDesc("查询到对应多个用户");
//				return checker.getReturnXml();
//			}
			
			String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
			String userIdStr =  StringUtil.getStringValue(userMap.get(0), "user_id");
			Long userId = StringUtil.getLongValue(userIdStr);
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return checker.getReturnXml();
			}
			
			// Loid
			checker.setLoid(StringUtil.getStringValue(userMap.get(0), "username"));
			StringBuffer loidPrev = new StringBuffer();
			int i = 0;
			for (HashMap<String, String> m : userMap) {
				if (i == 0) {
					i ++;
					continue;
				}
				loidPrev.append(StringUtil.getStringValue(m, "username"));
				loidPrev.append(";");
			}
			// LoidPrev 先设置为空
//			checker.setLoidPrev("");
			checker.setLoidPrev(loidPrev.toString());
			// DeviceSN
			checker.setDeviceSN(StringUtil.getStringValue(userMap.get(0), "device_serialnumber"));
			Map<String, String> deviceMap = userDevDao.getDeviceTypeInfo(deviceId);
			// DeviceType
			checker.setDeviceType("2");
			// DeviceVendor
			checker.setDeviceVendor(StringUtil.getStringValue(deviceMap, "vendor_name"));
			// DeviceModel
			checker.setDeviceModel(StringUtil.getStringValue(deviceMap, "device_model"));
			// Softwareversion
			checker.setSoftwareversion(StringUtil.getStringValue(deviceMap, "softwareversion"));
			// Hardwareversion
			checker.setHardwareversion(StringUtil.getStringValue(deviceMap, "hardwareversion"));
			
			IpsecServParamDAO ispDao = new IpsecServParamDAO();
			// IsNet
			checker.setIsNet(ispDao.queryIsNet(userId));
			// IsIpsecVPN
			checker.setIsIpsecVPN(ispDao.queryEnable(userId));
			// WanType
			checker.setWanType(StringUtil.getStringValue(userMap.get(0), "wan_type"));
			// IpAddr
			checker.setIpAddr(StringUtil.getStringValue(deviceMap, "loopback_ip"));
			// IpType
			checker.setIpType(ispDao.queryIpType(userId));
			// Online
			checker.setOnline(ispDao.queryOnline(deviceId));
			checker.setUserInfo(StringUtil.getStringValue(userMap.get(0), "account"));
			// Status
			checker.setStatus("0");
			// IpsecStatus
			checker.setIpsecStatus("0");
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudQueryGetWayService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryGetWayChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryGetWayService", checker.getCmdId());
		logger.warn("servicename[CloudQueryGetWayService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
