package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevRebootChecker;

/**
 * 设备重启接口
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2011-5-11 下午02:54:14
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class DevRebootService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(DevRebootService.class);
	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		DevRebootChecker devRebootChecker = new DevRebootChecker(inXml);
		if (false == devRebootChecker.check()) {
			logger.error(
					"servicename[DevRebootService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),
							devRebootChecker.getReturnXml() });
			return devRebootChecker.getReturnXml();
		}
		logger.warn(
				"servicename[DevRebootService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao
				.queryUserInfo(devRebootChecker.getUserInfoType(), devRebootChecker
						.getUserInfo(), devRebootChecker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[DevRebootService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
			devRebootChecker.setResult(1002);
			devRebootChecker.setResultDesc("无此用户信息");
		} 
		else
		{// 用户存在
//			long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
//			String username = userInfoMap.get("username");
//			String userCityId = userInfoMap.get("city_id");
			String userDevId = userInfoMap.get("device_id");

	
			if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
				logger.warn(
						"servicename[DevRebootService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
				devRebootChecker.setResult(1003);
				devRebootChecker.setResultDesc("此客户未绑定");
			}
			// (江西)判断设备是否繁忙或者业务正在下发
			else if ("jx_dx".equals(Global.G_instArea)
					&& false == CheckStrategyUtil.chechStrategy(userDevId))
			{
				logger.warn(
						"servicename[DevRebootService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						new Object[] { devRebootChecker.getCmdId(),
								devRebootChecker.getUserInfo() });
				devRebootChecker.setResult(1003);
				devRebootChecker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
			}
			else
			{
				//重启
				logger.warn(
						"servicename[DevRebootService]cmdId[{}]userinfo[{}]调ACS重启设备",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
				int irt = DevReboot.reboot(userDevId);
				logger.warn(
						"servicename[DevRebootService]cmdId[{}]userinfo[{}]调ACS重启设备返回码：{}",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),irt});
				if(1 == irt)
				{
					logger.warn(
							"servicename[DevRebootService]cmdId[{}]userinfo[{}]重启成功",
							new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
					devRebootChecker.setResult(0);
					devRebootChecker.setResultDesc("重启成功");
				}
				else
				{
					logger.warn(
							"servicename[DevRebootService]cmdId[{}]userinfo[{}]设备重启失败",
							new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
					devRebootChecker.setResult(1006);
					devRebootChecker.setResultDesc("设备重启失败");
				}
			}
			
		}
		
		String returnXml = devRebootChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(devRebootChecker, devRebootChecker.getUserInfo(),
				"DevRebootService");

		logger.warn(
				"servicename[TelepasswdService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),returnXml});

		// 回单
		return returnXml;
	}
}
