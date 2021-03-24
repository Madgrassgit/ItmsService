package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryBbmsDevSNMACChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QueryBbmsDevSNMACService {
	private static Logger logger = LoggerFactory.getLogger(QueryBbmsDevSNMACService.class);

	public String work(String inXml) {
		QueryBbmsDevSNMACChecker checker = new QueryBbmsDevSNMACChecker(inXml);
		if (!checker.check())  {
			logger.warn("serviceName[QueryBbmsDevSNMACService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
			new Object[] {checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[QueryBbmsDevSNMACService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml });
		//获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		
		String userId = StringUtil.getStringValue(userInfoMap, "user_id", "");
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userId)) {
			logger.warn("servicename[QueryBbmsDevSNMACService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] {checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1001);
			checker.setResultDesc("无此客户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("servicename[QueryBbmsDevSNMACService]cmdId[{}]userinfo[{}]未绑定设备",
					new Object[] {checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO devInfDao = new DeviceInfoDAO();
		Map<String, String> map = devInfDao.queryBbmsDevSNMAC(userId);
		
		checker.setNetAccount(StringUtil.getStringValue(map, "netaccount", ""));
		checker.setDevMac(StringUtil.getStringValue(map, "cpe_mac", ""));
		checker.setOui(StringUtil.getStringValue(map, "oui", ""));
		checker.setDevSn(StringUtil.getStringValue(map, "device_serialnumber", ""));
		
		/*if ("jl_dx".equals(Global.G_instArea))
		{
			String devSn = StringUtil.getStringValue(map, "device_serialnumber", "");
			if (!"".equals(devSn) && null != devSn) {
				devSn = devSn.substring(devSn.length() - 12);
				String regex = "(.{2})";
				devSn = devSn.replaceAll(regex, "$1:");
				devSn = devSn.substring(0, devSn.length() - 1);
			}
			checker.setDevMac(devSn);
		}*/
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "QueryBbmsDevSNMAC");
		logger.warn("servicename[QueryBbmsDevSNMACService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getUserInfo(), returnXml});
		 return returnXml;
	}
}
