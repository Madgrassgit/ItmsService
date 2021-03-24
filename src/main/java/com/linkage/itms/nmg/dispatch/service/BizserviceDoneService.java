
package com.linkage.itms.nmg.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.nmg.dispatch.obj.ServiceDoneChecker;

/**
 * 业务下发
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class BizserviceDoneService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(BizserviceDoneService.class);

	/*
	 * 接口工作方法
	 */
	@Override
	public String work(String inXml)
	{
		// 检查合法性
		ServiceDoneChecker serviceDoner = new ServiceDoneChecker(inXml);
		if (false == serviceDoner.check())
		{
			logger.error(
					"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo(),
							serviceDoner.getReturnXml() });
			return serviceDoner.getReturnXml();
		}
		logger.warn(
				"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo(),
						inXml });
		boolean isBinded = false;
		long userId = 0L;
		String deviceId = null;
		String oui = null;
		String devSn = null;
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServUserDAO servUserDao = new ServUserDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 根据用户帐号 or 终端序列号
		if (1 == serviceDoner.getSearchType())
		{
			// 根据用户帐号获取
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					serviceDoner.getUserInfoType(), serviceDoner.getUserInfo(), serviceDoner.getCityId());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
				serviceDoner.setResult(1003);
				serviceDoner.setResultDesc("查无此客户");
			}
			else
			{
				userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
				deviceId = userInfoMap.get("device_id");
				String userCityId = userInfoMap.get("city_id");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn(
							"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]用户未绑定设备",
							new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
					serviceDoner.setResult(1004);
					serviceDoner.setResultDesc("用户未绑定");
				}
				else
				{
					// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
					if ((!"nx_dx".equals(Global.G_instArea))
							&& (!"xj_dx".equals(Global.G_instArea))
							&& (!"jx_dx".equals(Global.G_instArea))
							&& (!"nmg_dx".equals(Global.G_instArea))
							&& false == serviceHandle.cityMatch(serviceDoner.getCityId(),
									userCityId))
					{// 属地不匹配
						logger.warn(
								"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]属地不匹配，查无此用户",
								new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
						serviceDoner.setResult(1003);
						serviceDoner.setResultDesc("属地不匹配,查无此客户");
					}
					else
					{// 属地匹配
						oui = userInfoMap.get("oui");
						devSn = userInfoMap.get("device_serialnumber");
						isBinded = true;
					}
				}
			}
		}
		else if (2 == serviceDoner.getSearchType())
		{
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devInfoMapList = userDevDao
					.getTelePasswdByDevSn(serviceDoner.getDevSn());
			if (null == devInfoMapList || devInfoMapList.isEmpty())
			{
				logger.warn(
						"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]查无此设备",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
				serviceDoner.setResult(1012);
				serviceDoner.setResultDesc("查无此设备");
			}
			else if (devInfoMapList.size() > 1)
			{
				logger.warn(
						"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]查询到多台设备",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
				serviceDoner.setResult(1013);
				serviceDoner.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			}
			else
			{
				Map<String, String> devMap = devInfoMapList.get(0);
				if (StringUtil.IsEmpty(devMap.get("username")))
				{
					logger.warn(
							"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}].ITMS未绑devSn({})",
							new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo(),serviceDoner.getDevSn()});
					serviceDoner.setResult(1014);
					serviceDoner.setResultDesc("设备未绑定");
				}
				else
				{
					String deviceCityId = devMap.get("city_id");
					if ((!"nx_dx".equals(Global.G_instArea))&&(!"xj_dx".equals(Global.G_instArea))
							&& (!"nmg_dx".equals(Global.G_instArea))
							&& (false == serviceHandle.cityMatch(serviceDoner.getCityId(),
									deviceCityId)))
					{// 属地不匹配
						logger.warn(
								"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]属地不匹配，查无此设备",
								new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo()});
						serviceDoner.setResult(1012);
						serviceDoner.setResultDesc("属地不匹配,查无此设备");
					}
					else
					{// 属地匹配
						deviceId = devMap.get("device_id");
						userId = StringUtil.getLongValue(devMap.get("user_id"));
						oui = devMap.get("oui");
						devSn = devMap.get("device_serialnumber");
						isBinded = true;
					}
				}
			}
		}
		if (true == isBinded)
		{
			// (江西)判断设备是否繁忙或者业务正在下发
			if ("jx_dx".equals(Global.G_instArea) && false == CheckStrategyUtil.chechStrategy(deviceId))
			{
				logger.warn(
						"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						new Object[] { serviceDoner.getCmdId(),
								serviceDoner.getUserInfo() });
				serviceDoner.setResult(1009);
				serviceDoner.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
			}
			else
			{
				// 获取用户的业务信息
				ArrayList<HashMap<String, String>> servUserMapList = servUserDao
						.queryHgwcustServUserByDevId(userId);
				// 是否受理了该业务
				if (null == servUserMapList || servUserMapList.isEmpty())
				{
					logger.warn(
							"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]用户为受理任何业务",
							new Object[] { serviceDoner.getCmdId(),
									serviceDoner.getUserInfo() });
					serviceDoner.setResult(1009);
					serviceDoner.setResultDesc("用户未受理任何业务");
				}
				else
				{ // 全业务下发
					if (0 == serviceDoner.getServiceType())
					{
						// 更新业务用户表的开通状态
						servUserDao.updateServOpenStatus(userId);
						// 预读调用对象
						PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
								StringUtil.getStringValue(userId), "" + deviceId, ""
										+ oui, devSn, "", "1");
						if (1 != CreateObjectFactory.createPreProcess()
								.processServiceInterface(CreateObjectFactory.createPreProcess()
										.GetPPBindUserList(preInfoObj)))
						{
							logger.warn(
									"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]设备[{}]全业务下发，调用配置模块失败",
									new Object[] { serviceDoner.getCmdId(),
											serviceDoner.getUserInfo(), deviceId });
							serviceDoner.setResult(1016);
							serviceDoner.setResultDesc("业务下发失败，请稍后重试");
						}
					}
					else
					{ // 特定业务下发
						boolean hasSomeServ = false;
						// 遍历业务信息
						for (HashMap<String, String> servUserMap : servUserMapList)
						{
							if (serviceDoner.getServiceType() == StringUtil
									.getIntegerValue(servUserMap.get("serv_type_id")))
							{
								// 更新业务用户表的业务开通状态
								servUserDao.updateServOpenStatus(userId,
										serviceDoner.getServiceType());
								// 预读调用对象
								PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
										StringUtil.getStringValue(userId), deviceId, oui,
										devSn, StringUtil.getStringValue(serviceDoner
												.getServiceType()), "1");
								if (1 != CreateObjectFactory.createPreProcess()
										.processServiceInterface(CreateObjectFactory.createPreProcess()
												.GetPPBindUserList(preInfoObj)))
								{
									logger.warn(
											"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]设备[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
											new Object[] { serviceDoner.getCmdId(),
													serviceDoner.getUserInfo(), deviceId,
													serviceDoner.getServiceType() });
									serviceDoner.setResult(1016);
									serviceDoner.setResultDesc("业务下发失败，请稍后重试");
								}
								hasSomeServ = true;
								break;
							}
						}
						if (false == hasSomeServ)
						{
							logger.warn(
									"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]用户未受理该业务，无法进行业务下发",
									new Object[] { serviceDoner.getCmdId(),
											serviceDoner.getUserInfo() });
							serviceDoner.setResult(1009);
							serviceDoner.setResultDesc("用户未受理该业务");
						}
					}
				}
			}
		}
		String returnXml = serviceDoner.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(serviceDoner, serviceDoner.getUserInfo(),
				"BizserviceDoneService");
		logger.warn(
				"servicename[BizserviceDoneService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
