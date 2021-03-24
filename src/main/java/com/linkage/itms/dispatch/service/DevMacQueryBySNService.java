
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryDeviceVersionDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevMacQueryBySNCheck;

/**
 * 通过LOID查询设备MAC接口
 * 
 * @author zhaixx (Ailk No.)
 * @version 1.0
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DevMacQueryBySNService implements IService {

	private static final Logger logger = LoggerFactory.getLogger(DevMacQueryBySNService.class);

	@Override
	public String work(String inXml) {
		DevMacQueryBySNCheck checker = new DevMacQueryBySNCheck(inXml);
		if (!checker.check()) {
			logger.error("serviceName[DevMacQueryBySNService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		// 校验通过
		// 根据loid查询
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 传入类型为逻辑SN
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(2, checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("无此用户信息：" + checker.getUserInfo());
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "DevMacQueryBySNService");
			return checker.getReturnXml();
		}
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		if ("".equals(deviceId)) {
			checker.setResult(1002);
			checker.setResultDesc("此用户未绑定");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "DevMacQueryBySNService");
			return checker.getReturnXml();
		}
		String deviceSerialnumber  = StringUtil.getStringValue(userInfoMap, "device_serialnumber", "");
		if(!"".equals(deviceSerialnumber)){
			//截取后十二位
			deviceSerialnumber = deviceSerialnumber.substring(deviceSerialnumber.length()-12);
			String regex = "(.{2})";
			deviceSerialnumber = deviceSerialnumber.replaceAll (regex, "$1:");
			deviceSerialnumber = deviceSerialnumber.substring(0, 17);
		}
		checker.setDevMac(deviceSerialnumber);
		// 查询厂商型号
		QueryDeviceVersionDAO devDao = new QueryDeviceVersionDAO();
		Map<String, String> deviceVersion = devDao.getDeviceVersion(deviceId);
		if(null == deviceVersion || deviceVersion.isEmpty()){
			logger.warn("无此设备信息：" + checker.getUserInfo());
			checker.setResult(1001);
			checker.setResultDesc("无此设备信息");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "DevMacQueryBySNService");
			return checker.getReturnXml();
		}
		checker.setResult(0);
		checker.setResultDesc("成功");
		checker.setDevVendor(StringUtil.getStringValue(deviceVersion, "vendor_add", ""));
		checker.setDevModel(StringUtil.getStringValue(deviceVersion, "device_model", ""));
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "DevMacQueryBySNService");
		logger.warn( "servicename[DevMacQueryBySNService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml});
		 return returnXml;
	}
}
