
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DeviceSNChecker;

public class DeviceSNService implements IService{

	private static Logger logger = LoggerFactory.getLogger(DeviceSNService.class);

	@Override
	public String work(String inXml){
		// 检查合法性
		DeviceSNChecker checker = new DeviceSNChecker(inXml);
		if (false == checker.check()){
			logger.error(
					"servicename[DeviceSNService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(),checker.getUserInfo(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[DeviceSNService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(),checker.getUserInfo(), inXml });
		
		//获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfoOrderLastTime_XJ(checker.getUserInfoType(), checker.getUserInfo());
		
		String userId = StringUtil.getStringValue(userInfoMap, "user_id", "");
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userId)) {
			logger.warn("servicename[DeviceSNService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("servicename[DeviceSNService]cmdId[{}]userinfo[{}]未绑定设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("此用户未绑定");
			return checker.getReturnXml();
		}
		
		Map<String, String> map = userDevDao.queryDeviceSNInfo(deviceId);
		if (map==null || map.size()<=0) {
			checker.setResult(1004);
			checker.setResultDesc("无宽带业务");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		String devMac=StringUtil.getStringValue(map, "cpe_mac","");
		String devVendor=StringUtil.getStringValue(map, "vendor_add","");
		String devModel=StringUtil.getStringValue(map, "device_model","");
		String devVersiontype=StringUtil.getStringValue(map, "device_version_type","");
		String devWantype=StringUtil.getStringValue(map, "wan_type","");
		if (StringUtil.IsEmpty(devVersiontype)) {
			devVersiontype="99";
		}
		
		if ("0".equals(devVersiontype)|| "1".equals(devVersiontype)) {
			devVersiontype="1";
		}
		
		checker.setDevMac(devMac);
		checker.setDevVendor(devVendor);
		checker.setDevModel(devModel);
		checker.setDevVersiontype(devVersiontype);
		checker.setDevWantype(devWantype);
		
		checker.setResult(0);
		checker.setResultDesc("成功");
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "deviceSN");
		logger.warn(
				"servicename[DeviceSNService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml});
		return returnXml;
	}
}
