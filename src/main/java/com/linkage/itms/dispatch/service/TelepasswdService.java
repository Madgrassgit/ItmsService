
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
import com.linkage.itms.dispatch.obj.CallChecker;

/**
 * call方法的业务处理类
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class TelepasswdService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(TelepasswdService.class);

	/*
	 * 查询电信维护密码工作方法
	 */
	@Override
	public String work(String inXml)
	{
		// 检查合法性
		CallChecker checker = new CallChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[TelepasswdService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[TelepasswdService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 获取用户帐号 or 终端序列号
		if (1 == checker.getSearchType())
		{
			// 根据用户帐号获取
			// Map<String, String> userMap =
			// userDevDao.getTelePasswdByUsername(checker.getUsername());
			ArrayList<HashMap<String, String>> userMapList = userDevDao.queryUserInfoList(
					checker.getUserInfoType(), checker.getUsername(), checker.getCityId());
			if (null == userMapList || userMapList.isEmpty())
			{
				logger.warn(
						"servicename[TelepasswdService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
			}
			else
			{
				// 说明查询到了多个
				if(userMapList.size() != 1 && checker.getUserInfoType() != 1){
					checker.setResult(1006);
					checker.setResultDesc("账号对应多个用户，请根据设备序列号查询");
				}
				// 正常
				else{
					HashMap<String, String> userMap = userMapList.get(0);
					String deviceId = userMap.get("device_id");
					String userCityId = userMap.get("city_id");
					if (StringUtil.IsEmpty(deviceId))
					{
						logger.warn(
								"servicename[TelepasswdService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { checker.getCmdId(), checker.getUsername()});
						checker.setResult(1003);
						checker.setResultDesc("未绑定设备");
					}
					else
					{
						// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
						if (!"nmg_dx".equals(Global.G_instArea) && !"jx_dx".equals(Global.G_instArea) && false == serviceHandle.cityMatch(checker.getCityId(), userCityId))
						{// 属地不匹配
							logger.warn(
									"servicename[TelepasswdService]cmdId[{}]userinfo[{}]属地不匹配 查无此用户",
									new Object[] { checker.getCmdId(), checker.getUsername()});
							checker.setResult(1003);
							checker.setResultDesc("查无此用户");
						}
						else
						{// 属地匹配
							checker.setResult(0);
							checker.setResultDesc("成功");
							checker.setLoId(StringUtil.getStringValue(userMap, "username"));
							checker.setTelePasswd(userMap.get("x_com_passwd"));
							Map<String, String> devMap = userDevDao
									.getTelePasswdByUsername(userMap.get("user_id"));
							if (null == devMap || devMap.isEmpty())
							{
								logger.warn(
										"servicename[TelepasswdService]cmdId[{}]userinfo[{}]未绑定设备",
										new Object[] { checker.getCmdId(), checker.getUsername()});
								checker.setResult(1003);
								checker.setResultDesc("未绑定设备");
							}
							else
							{
								checker.setTelePasswd(devMap.get("x_com_passwd"));
							}
						}
					}
				}
			}
		}
		else if (2 == checker.getSearchType())
		{
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devlsit = userDevDao
					.getTelePasswdByDevSn(checker.getDevSn());
			if (null == devlsit || devlsit.isEmpty())
			{
				logger.warn(
						"servicename[TelepasswdService]cmdId[{}]userinfo[{}]无此设备:{}",
						new Object[] { checker.getCmdId(), checker.getUsername(),checker.getDevSn()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
			}
			else if (devlsit.size() > 1)
			{
				logger.warn(
						"servicename[TelepasswdService]cmdId[{}]userinfo[{}]查询到多台设备:{}",
						new Object[] { checker.getCmdId(), checker.getUsername(),checker.getDevSn()});
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			}
			else
			{
				Map<String, String> devMap = devlsit.get(0);
				String deviceCityId = devMap.get("city_id");
				if (!"nmg_dx".equals(Global.G_instArea) && false == serviceHandle.cityMatch(checker.getCityId(), deviceCityId))
				{// 属地不匹配
					logger.warn(
							"servicename[TelepasswdService]cmdId[{}]userinfo[{}]属地不匹配 查无此设备:{}",
							new Object[] { checker.getCmdId(), checker.getUsername(),checker.getDevSn()});
					checker.setResult(1005);
					checker.setResultDesc("查无此设备");
				}
				else
				{// 属地匹配
					checker.setResult(0);
					checker.setLoId(StringUtil.getStringValue(devMap, "username"));
					checker.setResultDesc("成功");
					checker.setTelePasswd(devMap.get("x_com_passwd"));
				}
			}
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "TelepasswdService");
		logger.warn(
				"servicename[TelepasswdService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
		// 回单
		return returnXml;
	}
}
