
package com.linkage.itms.nmg.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.nmg.dispatch.obj.ModifyBinfInfoChecker;

/**
 * 用户设备解绑接口
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-30
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ModifyBinfInfoService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(ModifyBinfInfoService.class);
	// 处理人
	private static final String DEAL_STAFF = "综调";
	// 用户来源
	private static final int USERLINE = 1;

	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		ModifyBinfInfoChecker checker = new ModifyBinfInfoChecker(inXml);
		if (false == checker.check())
		{
			logger.error("servicename[ModifyBinfInfoService]loid[{}]验证未通过，返回：{}",
					new Object[] { checker.getLoid(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[ModifyBinfInfoService]loid[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getLoid(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(2, checker.getLoid());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("servicename[ModifyBinfInfoService]loid[{}]查无此用户",
					new Object[] { checker.getLoid() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
		}
		else
		{// 用户存在
			long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
			String username = userInfoMap.get("username");
			String userCityId = userInfoMap.get("city_id");
			String userDevId = userInfoMap.get("device_id");
			if (StringUtil.IsEmpty(userDevId))
			{// 用户未绑定终端
				logger.warn(
						"servicename[ModifyBinfInfoService]loid[{}]此客户未绑定",
						new Object[] { checker.getLoid() });
				checker.setResult(1004);
				checker.setResultDesc("此客户未绑定");
			}
			else
			{
				// 解绑
				serviceHandle.itmsRelease(StringUtil.getStringValue(userId), username,
						userCityId, userDevId, DEAL_STAFF, USERLINE);
			}
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getLoid(),
				"ModifyBinfInfoService");
		logger.warn(
				"servicename[ModifyBinfInfoService]loid[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getLoid(), returnXml });
		// 回单
		return returnXml;
	}
}
