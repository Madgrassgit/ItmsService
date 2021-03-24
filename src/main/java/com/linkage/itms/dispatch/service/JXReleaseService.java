
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.ReleaseChecker;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年1月15日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class JXReleaseService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(ReleaseService.class);
	// 处理人
	private static final String DEAL_STAFF = "综调";
	// 用户来源
	private static final int USERLINE = 1;
	/**
	 * 查询类型：根据设备序列号查
	 */
	private static final int SEARCHTYPE_DEV = 2;

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
					"servicename[JXReleaseService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { releaseChecker.getCmdId(),
							releaseChecker.getUserInfo(), releaseChecker.getReturnXml() });
			return releaseChecker.getReturnXml();
		}
		logger.warn("servicename[JXReleaseService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { releaseChecker.getCmdId(), releaseChecker.getUserInfo(),
						inXml });
		// 查询类型为根据设备序列号解绑
		if (releaseChecker.getSearchType() == SEARCHTYPE_DEV)
		{
			unbindByDevSn(releaseChecker);
		}
		// 查询类型为根据用户信息解绑
		else
		{
			unbindByUserInfo(releaseChecker);
		}
		String returnXml = releaseChecker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(releaseChecker,
				releaseChecker.getUserInfo(), "JXReleaseService");
		logger.warn("servicename[JXReleaseService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { releaseChecker.getCmdId(), releaseChecker.getUserInfo(),
						returnXml });
		// 回单
		return returnXml;
	}

	/**
	 * 根据设备信息解绑
	 * 
	 * @param releaseChecker
	 */
	private void unbindByDevSn(ReleaseChecker releaseChecker)
	{
		logger.warn("servicename[JXReleaseService],根据设备信息解绑,devsn[{}]",
				releaseChecker.getDevSn());
		ServiceHandle serviceHandle = new ServiceHandle();
		QueryDevDAO devDao = new QueryDevDAO();
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String devSn = releaseChecker.getDevSn();
		String devSubSn = devSn.substring(devSn.length() - 6, devSn.length());
		Map<String, String> userInfoMap = null;
		List<HashMap<String, String>> devList = devDao.queryUserByDevSN(
				releaseChecker.getDevSn(), devSubSn);
		// 查无此设备
		if (null == devList || devList.isEmpty())
		{
			logger.warn("servicename[JXReleaseService]cmdId[{}]devsn[{}]查无此设备",
					new Object[] { releaseChecker.getCmdId(), releaseChecker.getDevSn() });
			releaseChecker.setResult(1002);
			releaseChecker.setResultDesc("无此设备信息");
		}
		// 查出多条设备
		else if (devList.size() > 1)
		{
			logger.warn("servicename[JXReleaseService]cmdId[{}]devsn[{}]此设备序列号查出多条设备",
					new Object[] { releaseChecker.getCmdId(), releaseChecker.getDevSn() });
			releaseChecker.setResult(1002);
			releaseChecker.setResultDesc("此设备序列号查出多条设备，请输入完整设备序列号");
		}
		else
		{
			Map<String, String> devMap = devList.get(0);
			// 设备未和用户绑定
			if (StringUtil.IsEmpty(StringUtil.getStringValue(devMap, "user_id")))
			{
				logger.warn(
						"servicename[JXReleaseService]cmdId[{}]devsn[{}]此设备未绑定用户，无法解绑",
						new Object[] { releaseChecker.getCmdId(),
								releaseChecker.getDevSn() });
				releaseChecker.setResult(1003);
				releaseChecker.setResultDesc("此设备未绑定用户，无法解绑");
			}
			// 设备和用户已绑定，可以解绑
			else
			{
				userInfoMap = userDevDao.getUserByID(StringUtil.getLongValue(devMap,
						"user_id"));
				long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
				String username = userInfoMap.get("username");
				String userCityId = userInfoMap.get("city_id");
				String userDevId = userInfoMap.get("device_id");
				// 解绑
				serviceHandle.itmsRelease(StringUtil.getStringValue(userId), username,
						userCityId, userDevId, DEAL_STAFF, USERLINE);
			}
		}
	}

	/**
	 * 根据用户信息解绑
	 * 
	 * @param releaseChecker
	 */
	private void unbindByUserInfo(ReleaseChecker releaseChecker)
	{
		logger.warn(
				"servicename[JXReleaseService],根据用户信息解绑,userInfoType[{}],userinfo[{}]",
				releaseChecker.getUserInfoType(), releaseChecker.getUserInfo());
		ServiceHandle serviceHandle = new ServiceHandle();
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				releaseChecker.getUserInfoType(), releaseChecker.getUserInfo(),
				releaseChecker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[JXReleaseService]cmdId[{}]userinfo[{}]查无此用户",
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
			if (StringUtil.IsEmpty(userDevId))
			{// 用户未绑定终端
				logger.warn(
						"servicename[JXReleaseService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { releaseChecker.getCmdId(),
								releaseChecker.getUserInfo() });
				releaseChecker.setResult(1003);
				releaseChecker.setResultDesc("此客户未绑定");
			}
			else
			{
				// 解绑
				serviceHandle.itmsRelease(StringUtil.getStringValue(userId), username,
						userCityId, userDevId, DEAL_STAFF, USERLINE);
			}
		}
	}
}
