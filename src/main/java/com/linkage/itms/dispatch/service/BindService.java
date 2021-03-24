package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BindChecker;

/**
 * bind方法的业务处理类 用户设备绑定
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class BindService implements IService {

	private static Logger logger = LoggerFactory.getLogger(BindService.class);

	// 登录用户ID
	private static final long ACC_OID = -3;
	// 处理人
	private static final String DEAL_STAFF = "综调";
	// 受理人员
	private static final int DEAL_STAFF_ID = -3;
	// 修障的原因
	private static final int FAULT_ID = -3;
	// 用户来源
	private static final int USERLINE = 1;
	
	/**
	 * 绑定执行方法
	 */
	@Override
	public String work(String inXml) {
		BindChecker binder = new BindChecker(inXml);
		if (false == binder.check()) {
			logger.error(
					"servicename[BindService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn(
				"servicename[BindService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 查询设备信息 
		ArrayList<HashMap<String, String>> devInfoMapList = userDevDao
				.queryDevInfo(binder.getDevSn());
		if (null == devInfoMapList || devInfoMapList.isEmpty()) {
			logger.warn(
					"servicename[BindService]cmdId[{}]userinfo[{}]查无此设备",
					new Object[] { binder.getCmdId(), binder.getUserInfo()});
			binder.setResult(1005);
			binder.setResultDesc("查无此设备");
		} else { // 查询到终端
			// 终端数是否唯一
			int size = devInfoMapList.size();
			if (size > 1) {
				logger.warn(
						"servicename[BindService]cmdId[{}]userinfo[{}]查询到多台设备",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(1006);
				binder.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			} else { // 终端唯一
				HashMap<String, String> resMap = devInfoMapList.get(0);
				String deviceCityId = resMap.get("city_id");
				if (!"nmg_dx".equals(Global.G_instArea) && false == serviceHandle.cityMatch(binder.getCityId(),
						deviceCityId)) {// 属地不匹配
					logger.warn(
							"servicename[BindService]cmdId[{}]userinfo[{}]属地不匹配，查无此设备",
							new Object[] { binder.getCmdId(), binder.getUserInfo()});
					binder.setResult(1005);
					binder.setResultDesc("查无此设备");
				} else {// 属地匹配
					String bindFlag = resMap.get("cpe_allocatedstatus");
					if ("1".equals(bindFlag)) {
						logger.warn(
								"servicename[BindService]cmdId[{}]userinfo[{}]设备已绑定",
								new Object[] { binder.getCmdId(), binder.getUserInfo()});
						binder.setResult(1005);
						binder.setResultDesc("设备已被绑定");
					} else { // 终端未绑定
						String deviceId = resMap.get("device_id");
						String deviceOui = resMap.get("oui");
						String deviceSn = resMap.get("device_serialnumber");
						// 查询用户信息 考虑属地因素
						Map<String, String> userInfoMap = userDevDao
								.queryUserInfo(binder.getUserInfoType(), binder
										.getUserInfo(), binder.getCityId());
						if (null == userInfoMap || userInfoMap.isEmpty()) {
							logger.warn(
									"servicename[BindService]cmdId[{}]userinfo[{}]查无此用户",
									new Object[] { binder.getCmdId(), binder.getUserInfo()});
							binder.setResult(1003);
							binder.setResultDesc("查无此用户");
						} else {// 用户存在
							long userId = StringUtil.getLongValue(userInfoMap
									.get("user_id"));
							String username = userInfoMap.get("username");
							String userCityId = userInfoMap.get("city_id");
							String userDevId = userInfoMap.get("device_id");
							// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
							if (!"nmg_dx".equals(Global.G_instArea) && !"jx_dx".equals(Global.G_instArea)
									&& false == serviceHandle.cityMatch(binder
									.getCityId(), userCityId)) {// 属地不匹配
								logger.warn(
										"servicename[BindService]cmdId[{}]userinfo[{}]用户属地不匹配",
										new Object[] { binder.getCmdId(), binder.getUserInfo()});
								binder.setResult(1003);
								binder.setResultDesc("查无此用户");
							} else {// 属地匹配
								if(!userDevDao.getUserType(userId).equals(resMap.get("device_type")))
								{//终端类型不匹配
									logger.warn(
											"servicename[BindService]cmdId[{}]userinfo[{}]用户与终端类型不匹配，不予绑定",
											new Object[] { binder.getCmdId(), binder.getUserInfo()});
									binder.setResult(1008);
									binder.setResultDesc("用户与设备终端类型不匹配，不予绑定");
								}
								else
								{//终端类型匹配
									if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
										// 绑定
										serviceHandle.itmsInst(ACC_OID, StringUtil
												.getStringValue(userId), username,
												userCityId, deviceId, deviceCityId,
												deviceOui, deviceSn, DEAL_STAFF, 1,
												USERLINE);
									} else {// 用户已绑定
										if (2 == binder.getBindType()) {// 修障
											logger.warn(
													"servicename[BindService]cmdId[{}]userinfo[{}]修障原因：{}",
													new Object[] { binder.getCmdId(), binder.getUserInfo(),binder.getDevDesc()});
											serviceHandle
													.ipossItmsModify(
															StringUtil
																	.getStringValue(userId),
															username,
															userCityId,
															userDevId,
															deviceId,
															deviceCityId,
															deviceOui,
															deviceSn,
															StringUtil
																	.getStringValue(FAULT_ID),
															DEAL_STAFF,
															StringUtil
																	.getStringValue(DEAL_STAFF_ID),
															USERLINE);
										} else {// 新装
											logger.warn(
													"servicename[BindService]cmdId[{}]userinfo[{}]用户已绑定终端",
													new Object[] { binder.getCmdId(), binder.getUserInfo()});
											binder.setResult(1004);
											binder.setResultDesc("用户已绑定终端");
										}
									}
								}
								
							}
						}
					}
				}
			}
		}
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(), "BindService");
		logger.warn(
				"servicename[BindService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}

}
