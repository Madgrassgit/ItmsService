package com.linkage.itms.dispatch.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BindInfoCpmisChecker;


public class BindInfoCpmisService implements IService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(BindInfoCpmisService.class);

	@Override
	public String work(String inXml) {
		BindInfoCpmisChecker checker = new BindInfoCpmisChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.info("servicename[BindInfoCpmisService]cmdId[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), inXml});
				return checker.getReturnXml();
			}
			logger.info("servicename[BindInfoCpmisService]cmdId[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), inXml});
			int type=0;
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			//修改处
			Map<String, String> userInfoMap=null;
			String userDevId = "";
			switch (checker.getSearchType()) {
			// 客户账号
			case 1:
				 userInfoMap = userDevDao.queryUserInfo(
						checker.getUserInfoType(), checker.getUserInfo(), null);
				 type=1;
				 //修改处
				 if (null == userInfoMap || userInfoMap.isEmpty())
				{
					userInfoMap=userDevDao.queryUserInfo2(checker.getUserInfoType(), checker.getUserInfo(), null);
					type=2;
				}
				if (null == userInfoMap || userInfoMap.isEmpty()) {
					logger.warn("servicename[BindInfoCpmisService]cmdId[{}]userinfo[{}]查无此用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1002);
					checker.setResultDesc("查无此用户");
					return getReturnXml(checker);
				}
				String username = getStringValue(userInfoMap, "username");
				String deviceSn = getStringValue(userInfoMap, "device_serialnumber");
				String userline = getStringValue(userInfoMap, "userline");
				userDevId = getStringValue(userInfoMap, "device_id");
				if (StringUtil.IsEmpty(userDevId)) {
					// 未绑定设备
					logger.warn("servicename[BindInfoCpmisService]cmdId[{}]userinfo[{}]用户未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1003);
					checker.setResultDesc("未绑定设备");
					return getReturnXml(checker);
				}
				// 根据逻辑SN查询用户的所有业务账号，并用'|||'隔开返回
				StringBuffer usernameResult = new StringBuffer();
				if(type==1)
				{
					usernameResult.append(userDevDao.getAllUsername(username));
				}else if(type==2)
				{
					usernameResult.append(userDevDao.getAllUsername2(username));	
				}
				//判断是江西的加|||
				if("jx_dx".equals(Global.G_instArea)){
					usernameResult.append("|||");
				}
				checker.setUserName(usernameResult.toString());
				
				checker.setDevSn(getStringValue(userInfoMap, "oui") + "-" + deviceSn);
				checker.setBindType(userline);
				checker.setBindTime(formateTime(getStringValue(userInfoMap, "updatetime")));
				break;
			// 设备序列号
			case 2:
				ArrayList<HashMap<String, String>> devInfoMapList = null;
				// oui-串码
				if (checker.getDevSn().indexOf("-") != -1) {
					devInfoMapList = userDevDao.getDeviceByOui(checker.getDevSn());
					type=1;
				}
				// 设备序列号
				else {
					devInfoMapList = userDevDao.getTelePasswdByDevSn(checker.getDevSn());
					type=1;
				}
				//修改处
				if (null == devInfoMapList || devInfoMapList.isEmpty()) {
					if (checker.getDevSn().indexOf("-") != -1) {
						devInfoMapList = userDevDao.getDeviceByOui2(checker.getDevSn());
						type=2;
					}
					// 设备序列号
					else {
						devInfoMapList = userDevDao.getTelePasswdByDevSn2(checker.getDevSn());
						type=2;
					}
				}
				if (null == devInfoMapList || devInfoMapList.isEmpty()) {
					logger.warn("servicename[BindInfoCpmisService]cmdId[{}]devSn[{}]查无此设备",
							new Object[] { checker.getCmdId(), checker.getDevSn()});
					checker.setResult(1004);
					checker.setResultDesc("查无此设备");
					return getReturnXml(checker);
				}
				if (devInfoMapList.size() > 1) {
					logger.warn("servicename[BindInfoCpmisService]cmdId[{}]devSn[{}]查询到多台设备",
							new Object[] { checker.getCmdId(), checker.getDevSn()});
					checker.setResult(1006);
					checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					return getReturnXml(checker);
				}
				HashMap<String, String> devInfoMap = devInfoMapList.get(0);
				if(StringUtil.IsEmpty(devInfoMap.get("username"))) {
					// 未绑定设备
					logger.warn("servicename[BindInfoCpmisService]cmdId[{}]devSn[{}]未绑定设备",
							new Object[] { checker.getCmdId(), checker.getDevSn()});
					checker.setResult(1003);	
					checker.setResultDesc("未绑定设备");
					return getReturnXml(checker);
				}
				userDevId = getStringValue(devInfoMap, "device_id");
				// 根据逻辑SN查询用户的所有业务账号，并用'|||'隔开返回
				StringBuffer usernameResult1 = new StringBuffer();
				if(type==1){
					usernameResult1.append(userDevDao.getAllUsername(getStringValue(devInfoMap, "username")));
				}else if(type==2){
					usernameResult1.append(userDevDao.getAllUsername2(getStringValue(devInfoMap, "username")));
				}
				//判断江西
				if("jx_dx".equals(Global.G_instArea)){
					usernameResult1.append("|||");
				}
				checker.setUserName(usernameResult1.toString());
				checker.setDevSn(getStringValue(devInfoMap, "oui") + "-" +
						getStringValue(devInfoMap, "device_serialnumber"));
				checker.setBindType(getStringValue(devInfoMap, "userline"));
				checker.setBindTime(formateTime(getStringValue(devInfoMap, "updatetime")));
				break;
			default:
				break;
			}
			// 获取设备详细信息
			Map<String, String> devTypeInfoMap = userDevDao.getDeviceTypeInfo(userDevId);
			checker.setIp(getStringValue(devTypeInfoMap, "loopback_ip"));
			checker.setVendor(getStringValue(devTypeInfoMap, "vendor_name"));
			checker.setDevModel(getStringValue(devTypeInfoMap, "device_model"));
			checker.setHandwareVersion(getStringValue(devTypeInfoMap, "hardwareversion"));
			checker.setSoftwareVersion(getStringValue(devTypeInfoMap, "softwareversion"));
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.info("BindInfoCpmisService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(BindInfoCpmisChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "BindInfoCpmisService", checker.getCmdId());
		logger.info("servicename[BindInfoCpmisService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}

	/**
	 * 格式化数据
	 * @param map
	 * @param columName
	 * @return
	 */
	private String getStringValue(Map<String, String> map, String columName) {
		if (null == columName || null == map || null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
	
	/**
	 * 格式化时间
	 * @param time
	 * @return
	 */
	private String formateTime(String time) {
		if (null == time || "".equals(time)) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(Long.parseLong(time) * 1000);
	}
}
