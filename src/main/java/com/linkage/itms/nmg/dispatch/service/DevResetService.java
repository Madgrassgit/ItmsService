package com.linkage.itms.nmg.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DevReset;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.DevResetChecker;


/**
 * 设备重启接口
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2012-3-20 下午02:54:14
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class DevResetService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(DevResetService.class);
	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		logger.warn("reset:inXml({})",inXml);
		
		DevResetChecker devResetChecker = new DevResetChecker(inXml);
		if (false == devResetChecker.check()) {
			logger.error(
					"servicename[DevResetService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo(),
							devResetChecker.getReturnXml() });
			return devResetChecker.getReturnXml();
		}
		logger.warn(
				"servicename[DevResetService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao
				.queryUserInfo(devResetChecker.getUserInfoType(), devResetChecker
						.getUserInfo(), devResetChecker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[DevResetService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo()});
			devResetChecker.setResult(1002);
			devResetChecker.setResultDesc("无此用户信息");
		} 
		else
		{// 用户存在
			long userId = StringUtil.getLongValue(userInfoMap
					.get("user_id"));
			String username = userInfoMap.get("username");
			String userCityId = userInfoMap.get("city_id");
			String userDevId = userInfoMap.get("device_id");

	
			if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
				logger.warn(
						"servicename[DevResetService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo()});
				devResetChecker.setResult(1004);
				devResetChecker.setResultDesc("此客户未绑定");
			}
			// (江西)判断设备是否繁忙或者业务正在下发
			else if ("jx_dx".equals(Global.G_instArea)
					&& false == CheckStrategyUtil.chechStrategy(userDevId))
			{
				logger.warn(
						"servicename[DevResetService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						new Object[] { devResetChecker.getCmdId(),
								devResetChecker.getUserInfo() });
				devResetChecker.setResult(1003);
				devResetChecker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
			}
			else
			{
				 int irt  = 0;
				 
				 /**
				  * 流程调整为:
				  * 1、调用者（WEB或者ItmsService模块），将需要恢复出厂的用户业务状态置成未做，调用配置模块。
				  * 2、配置模块根据未做的业务生成业务下发策略，通知acs恢复出厂
				  * 3、调用者判断恢复出厂如果是失败，将状态还原。
				  */
				 userDevDao.updateCustStatus(userId);
				 
                 if("hb_dx".equals(Global.G_instArea) || "nmg_dx".equals(Global.G_instArea)){
                         irt = DevReset.reset4HB(userInfoMap);
                 }else{
                         irt = DevReset.reset(userDevId);
                 }
				logger.warn(
						"servicename[DevResetService]cmdId[{}]userinfo[{}]调ACS设备返回码：{}",
						new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo(),irt});
				if(1 == irt)
				{
					
					logger.warn(
							"servicename[DevResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置成功",
							new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo()});
					devResetChecker.setResult(0);
					devResetChecker.setResultDesc("设备恢复出厂设置成功");
					
				}
				else
				{
					// 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
					userDevDao.updateCustStatusFailure(userId);
					logger.warn(
							"servicename[DevResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置失败",
							new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo()});
					devResetChecker.setResult(1006);
					devResetChecker.setResultDesc("设备恢复出厂设置失败");
				}
			}
			
		}
		
		String returnXml = devResetChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(devResetChecker, devResetChecker.getUserInfo(),
				"DevResetService");

		logger.warn(
				"servicename[DevResetService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { devResetChecker.getCmdId(), devResetChecker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
