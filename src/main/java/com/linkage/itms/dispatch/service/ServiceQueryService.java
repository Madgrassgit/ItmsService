package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.ServiceQueryChecker;

/**
 * 业务开通情况查询
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class ServiceQueryService implements IService {

	// 日志记录对象
	private static Logger logger = LoggerFactory
			.getLogger(ServiceQueryService.class);

	/**
	 * 业务开通情况查询工作方法
	 */
	@Override
	public String work(String inXml) {
		logger.warn("work({})", inXml);
		// 检查合法性
		ServiceQueryChecker serviceQueryer = new ServiceQueryChecker(inXml);
		if (false == serviceQueryer.check()) {
			logger.error("验证未通过，返回：\n" + serviceQueryer.getReturnXml());
			return serviceQueryer.getReturnXml();
		}
		long userId = 0L;
		boolean isBinded = false;
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServUserDAO servUserDao = new ServUserDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 根据用户帐号 or 终端序列号
		if (1 == serviceQueryer.getSearchType()) {
			// 根据用户帐号获取
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					serviceQueryer.getUserInfoType(), serviceQueryer
							.getUserInfo());
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn("无此用户：" + serviceQueryer.getUserInfo());
				serviceQueryer.setResult(1002);
				serviceQueryer.setResultDesc("查无此客户");
			} else {
				userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
				String deviceId = userInfoMap.get("device_id");
				String userCityId = userInfoMap.get("city_id");
				if (StringUtil.IsEmpty(deviceId)) {
					logger.warn("未绑定设备：" + serviceQueryer.getUserInfo());
					serviceQueryer.setResult(1004);
					serviceQueryer.setResultDesc("用户未绑定");
				} else {
					if (false == serviceHandle.cityMatch(serviceQueryer
							.getCityId(), userCityId)) {// 属地不匹配
						logger.warn("属地不匹配 查无此用户："
								+ serviceQueryer.getUserInfo());
						serviceQueryer.setResult(1003);
						serviceQueryer.setResultDesc("查无此用户");
					} else {// 属地匹配
						isBinded = true;
					}
				}
			}
		} else if (2 == serviceQueryer.getSearchType()) {
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devInfoMapList = userDevDao
					.getTelePasswdByDevSn(serviceQueryer.getDevSn());
			if (null == devInfoMapList || devInfoMapList.isEmpty()) {
				logger.warn("无此设备：" + serviceQueryer.getDevSn());
				serviceQueryer.setResult(1005);
				serviceQueryer.setResultDesc("查无此设备");
			} else if (devInfoMapList.size() > 1) {
				logger.warn("查询到多台设备：" + serviceQueryer.getDevSn());
				serviceQueryer.setResult(1006);
				serviceQueryer.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			} else {
				Map<String, String> devMap = devInfoMapList.get(0);
				String deviceCityId = devMap.get("city_id");
				if (false == serviceHandle.cityMatch(
						serviceQueryer.getCityId(), deviceCityId)) {// 属地不匹配
					logger.warn("属地不匹配 查无此设备：" + serviceQueryer.getDevSn());
					serviceQueryer.setResult(1005);
					serviceQueryer.setResultDesc("查无此设备");
				} else {// 属地匹配
					if (StringUtil.IsEmpty(devMap.get("username"))) {
						logger.error(".ITMS未绑devSn({}),queryUser({})",
								serviceQueryer.getDevSn(), serviceQueryer
										.getUserInfo());
						serviceQueryer.setResult(1004);
						serviceQueryer.setResultDesc("设备未绑定");
					} else {
						userId = StringUtil.getLongValue(devMap.get("user_id"));
						isBinded = true;
					}
				}
			}
		}

		if (true == isBinded) {// 已绑定
			// 获取用户的业务信息
			ArrayList<HashMap<String, String>> servUserMapList = servUserDao
					.queryHgwcustServUserByDevId(userId);
			// 是否受理了该业务
			if (null == servUserMapList || servUserMapList.isEmpty()) {
				serviceQueryer.setResult(1009);
				serviceQueryer.setResultDesc("用户未受理任何业务");
			} else {
				int size = servUserMapList.size();
				// 业务数目
				serviceQueryer.setServNum(size);
				// 业务代码
				int[] arrServCode = new int[size];
				// 业务结果
				int[] arrServResult = new int[size];
				// 遍历业务信息
				for (int i = 0; i < size; i++) {
					HashMap<String, String> servUserMap = servUserMapList
							.get(i);
					arrServCode[i] = StringUtil.getIntegerValue(servUserMap
							.get("serv_type_id"));
					arrServResult[i] = StringUtil.getIntegerValue(servUserMap
							.get("open_status"));
				}
				serviceQueryer.setArrServCode(arrServCode);
				serviceQueryer.setArrServResult(arrServResult);
			}
		}

		String returnXml = serviceQueryer.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(serviceQueryer, serviceQueryer.getUserInfo(), "serviceQuery");

		logger.warn("return({})", returnXml);

		// 回单
		return returnXml;
	}

}
