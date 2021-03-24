package com.linkage.itms.ct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.ct.obj.CtBridge2RoutedChecker;
import com.linkage.itms.dao.DeviceTypeDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;


/**
 * 桥改路由业务下发类
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtBridge2RoutedService implements IService {

	private static Logger logger = LoggerFactory
			.getLogger(CtBridge2RoutedService.class);

	// 接口调用数据对象
	CtBridge2RoutedChecker ctBridge2RoutedChecker;

	// 调预读使用的业务类型ID
	private static final int STRATEGY_SERVTYPEID = 3;
	// 类型ID
	private static final int PARAM_TYPE_ID = 2;

	
	/**
	 * 桥改路由业务下发
	 */
	@Override
	public String ctWorkService(String xmlParam) {
		logger.debug("ctWorkService()");
		// 检查合法性
		ctBridge2RoutedChecker = new CtBridge2RoutedChecker(xmlParam);
		if (false == ctBridge2RoutedChecker.check()) {
			logger.error("验证未通过，返回：\n" + ctBridge2RoutedChecker.getReturnXml());
			return ctBridge2RoutedChecker.getReturnXml();
		}

		// 初始化
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String ctUsername = ctBridge2RoutedChecker.getUsername();
		String ctDevSn = ctBridge2RoutedChecker.getDevSn();
		Map<String, String> userDevMap = null;

		// 查询用户信息
		userDevMap = userDevDao.queryUserInfo(1, ctUsername);
		if (null == userDevMap || userDevMap.isEmpty()) {
			// 未查询到用户
			logger.warn("查无此客户：" + ctUsername);
			ctBridge2RoutedChecker.setResult(1002);
			ctBridge2RoutedChecker.setResultDesc("查无此客户");
		} else {
			long userId = StringUtil.getLongValue(userDevMap.get("user_id"));
			String userDevId = userDevMap.get("device_id");
			if (StringUtil.IsEmpty(userDevId)) {
				// 用户未绑定终端
				logger.warn("未绑定终端：" + ctUsername);
				ctBridge2RoutedChecker.setResult(1003);
				ctBridge2RoutedChecker.setResultDesc("未绑定设备");
			} else {
				// 查询终端信息
				ArrayList<HashMap<String, String>> devMapList = userDevDao
						.queryDevInfo(ctDevSn);
				if (null == devMapList || devMapList.isEmpty()) {
					// 未查询到终端
					logger.warn("未查询到终端：" + ctDevSn);
					ctBridge2RoutedChecker.setResult(1004);
					ctBridge2RoutedChecker.setResultDesc("查无此设备");
				} else {
					int devSize = devMapList.size();
					if (devSize > 1) {
						// 查询到多台终端
						logger.warn("查询到多台设备：" + ctDevSn);
						ctBridge2RoutedChecker.setResult(1006);
						ctBridge2RoutedChecker
								.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					} else {
						// 只查询到一台
						HashMap<String, String> devMap = devMapList.get(0);
						String devId = devMap.get("device_id");
						String oui = devMap.get("oui");
						String devSn = devMap.get("device_serialnumber");
						if (userDevId.equals(devId)) {
							// 用户终端绑定关系正确获取终端相关信息
							int devTypeId = StringUtil.getIntegerValue(devMap
									.get("devicetype_id"));

							// 查询终端是否支持路由
							if (1 == DeviceTypeDAO.routedSupported(devTypeId)) {
								// 查询用户是否做过路由业务,是否路由业务已开通
								int routeStat = CtInfoQueryService
										.routeStat(userId);
								if (1 == routeStat) {
									logger.warn("{}已成功下发过该业务", ctUsername);
									ctBridge2RoutedChecker.setResult(1010);
									ctBridge2RoutedChecker
											.setResultDesc("已成功下发过该业务");
								} else {
									// 入或更新上网业务参数表
									ServUserDAO servUserDao = new ServUserDAO();
									if (0 == routeStat) {
										servUserDao.saveRoutedUser(userId,
												ctUsername,
												ctBridge2RoutedChecker
														.getPasswd(),
												PARAM_TYPE_ID, 0);
									}else{
										servUserDao.updateRouteUser(userId, PARAM_TYPE_ID);
									}
									// 调预读模块进行业务下发
									PreServInfoOBJ preInfoObj = new PreServInfoOBJ(StringUtil
											.getStringValue(userId), "" + devId, "" + oui,
											devSn, "" + STRATEGY_SERVTYPEID, "1");
									if (1 != CreateObjectFactory.createPreProcess()
											.processServiceInterface(CreateObjectFactory.createPreProcess()
													.GetPPBindUserList(preInfoObj))) {
										logger.warn("设备 "+userDevId+" 桥改路由，调用后台预读模块失败");
										ctBridge2RoutedChecker.setResult(1000);
										ctBridge2RoutedChecker.setResultDesc("未知错误，请稍后重试");
									}
								}
							} else {
								logger.warn("终端不支持：" + ctDevSn);
								ctBridge2RoutedChecker.setResult(1001);
								ctBridge2RoutedChecker.setResultDesc("终端不支持路由");
							}
						} else {
							// 用户终端绑定关系不正确
							logger.warn("查询到多台设备：" + ctDevSn);
							ctBridge2RoutedChecker.setResult(1008);
							ctBridge2RoutedChecker.setResultDesc("用户终端绑定关系不正确");
						}
					}
				}
			}
		}

		String returnXml = ctBridge2RoutedChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordCtLog(ctBridge2RoutedChecker, "ctBridge2Routed");

		logger.warn("return({})", returnXml);

		// 回单
		return returnXml;
	}
}
