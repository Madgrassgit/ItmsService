package com.linkage.itms.nmg.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.DevRebootChecker;

/**
* 设备重启接口
* 项目名称：ailk-itms-ItmsService   
* 类名称：BizRebootService   
* 类描述：   
* 创建人：guxl3   
* 创建时间：2019年3月27日 下午5:21:38   
* @version
 */
public class BizRebootService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(BizRebootService.class);
	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		DevRebootChecker devRebootChecker = new DevRebootChecker(inXml);
		if (false == devRebootChecker.check()) {
			logger.error(
					"servicename[BizRebootService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),
							devRebootChecker.getReturnXml() });
			return devRebootChecker.getReturnXml();
		}
		logger.warn(
				"servicename[BizRebootService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(devRebootChecker.getUserInfoType(), devRebootChecker
						.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[BizRebootService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
			devRebootChecker.setResult(1002);
			devRebootChecker.setResultDesc("无此用户信息");
		} 
		else
		{// 用户存在
			String userDevId = userInfoMap.get("device_id");
	
			if (StringUtil.IsEmpty(userDevId)) {
				// 用户未绑定终端
				logger.warn(
						"servicename[BizRebootService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
				devRebootChecker.setResult(1004);
				devRebootChecker.setResultDesc("此客户未绑定");
			}
			else
			{
				//重启
				logger.warn(
						"servicename[BizRebootService]cmdId[{}]userinfo[{}]调ACS重启设备",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
				int irt = DevReboot.reboot(userDevId);
				logger.warn(
						"servicename[BizRebootService]cmdId[{}]userinfo[{}]调ACS重启设备返回码：{}",
						new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),irt});
				if(1 == irt)
				{
					logger.warn(
							"servicename[BizRebootService]cmdId[{}]userinfo[{}]重启成功",
							new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
					devRebootChecker.setResult(0);
					devRebootChecker.setResultDesc("重启成功");
				}
				else
				{
					logger.warn(
							"servicename[BizRebootService]cmdId[{}]userinfo[{}]设备重启失败",
							new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo()});
					devRebootChecker.setResult(1006);
					devRebootChecker.setResultDesc("设备重启失败");
				}
			}
			
		}
		
		String returnXml = devRebootChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(devRebootChecker, devRebootChecker.getUserInfo(),
				"BizRebootService");

		logger.warn(
				"servicename[BizRebootService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { devRebootChecker.getCmdId(), devRebootChecker.getUserInfo(),returnXml});

		// 回单
		return returnXml;
	}
}
