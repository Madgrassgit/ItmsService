
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.ReleaseChecker;

/**
 * 用户设备解绑接口
 * 
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2011-5-11 下午02:54:14
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 */
public class ReleaseService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(ReleaseService.class);
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
		ReleaseChecker releaseChecker = new ReleaseChecker(inXml);
		if (false == releaseChecker.check())
		{
			logger.error(
					"servicename[ReleaseService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { releaseChecker.getCmdId(),
							releaseChecker.getUserInfo(), releaseChecker.getReturnXml() });
			return releaseChecker.getReturnXml();
		}
		logger.warn("servicename[ReleaseService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { releaseChecker.getCmdId(), releaseChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(releaseChecker.getUserInfoType(),
					releaseChecker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[ReleaseService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { releaseChecker.getCmdId(),
							releaseChecker.getUserInfo() });
			releaseChecker.setResult(1002);
			releaseChecker.setResultDesc("无此用户信息");
		}
		else
		{// 用户存在
			long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
			String username = userInfoMap.get("username");
			String userCityId = userInfoMap.get("city_id");
			String userDevId = userInfoMap.get("device_id");
			if (false == serviceHandle.cityMatch(releaseChecker.getCityId(), userCityId)&&(!"cq_dx".equals(Global.G_instArea)))
			{// 属地不匹配
				logger.warn(
						"servicename[ReleaseService]cmdId[{}]userinfo[{}]属地不匹配 查无此用户",
						new Object[] { releaseChecker.getCmdId(),
								releaseChecker.getUserInfo() });
				releaseChecker.setResult(1002);
				releaseChecker.setResultDesc("无此用户信息");
			}
			else
			{
				if (StringUtil.IsEmpty(userDevId))
				{// 用户未绑定终端
					logger.warn(
							"servicename[ReleaseService]cmdId[{}]userinfo[{}]此客户未绑定",
							new Object[] { releaseChecker.getCmdId(),
									releaseChecker.getUserInfo() });
					releaseChecker.setResult(1003);
					releaseChecker.setResultDesc("此客户未绑定");
				}
				else
				{
					// 解绑
					serviceHandle.itmsRelease(StringUtil.getStringValue(userId),
							username, userCityId, userDevId, DEAL_STAFF, USERLINE);
				}
			}
		}
		
		String returnXml = releaseChecker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(releaseChecker,
				releaseChecker.getUserInfo(), "ReleaseService");
		logger.warn("servicename[ReleaseService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { releaseChecker.getCmdId(), releaseChecker.getUserInfo(),
						returnXml });
		// 回单
		return returnXml;
	}
}
