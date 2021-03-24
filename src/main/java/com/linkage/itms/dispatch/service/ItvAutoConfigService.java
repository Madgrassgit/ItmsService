package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.ItvAutoConfigChecher;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-21
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ItvAutoConfigService implements IService
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(ItvAutoConfigService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	@Override
	public String work(String inXml)
	{
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		ItvAutoConfigChecher checker=new ItvAutoConfigChecher(inXml);
		if (false == checker.check()) {
			logger.error("servicename[ItvAutoConfigService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[ItvAutoConfigService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[ItvAutoConfigService]cmdId[{}]userinfo[{}]无此用户信息",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		else{
			String deviceId = userInfoMap.get("device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn("servicename[ItvAutoConfigService]cmdId[{}]userinfo[{}]此用户未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1003);
				checker.setResultDesc("此用户未绑定设备");
				return checker.getReturnXml();
			}else{
				Map<String,String> deviceMap = deviceInfoDAO.queryZeroConfByDeviceId(deviceId);
				if(null == deviceMap|| deviceMap.isEmpty()){
					logger.warn(
							"servicename[ItvAutoConfigService]cmdId[{}]光猫不支持机顶盒即插即用",
							new Object[] { checker.getCmdId()});
					checker.setResult(1000);
					checker.setResultDesc("光猫不支持机顶盒即插即用!");
					logger.warn("servicename[ItvAutoConfigService]cmdId[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId()});
				}else{
					if ("1".equals(deviceMap.get("zeroconf"))){
						checker.setResult(0);
						checker.setResultDesc("成功");
					}else {
						checker.setResult(1000);
						checker.setResultDesc("光猫不支持机顶盒即插即用!");
					}
					
				}
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn("servicename[ItvAutoConfigService]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(), returnXml });
		return returnXml;
	}
	
	public UserDeviceDAO getUserDevDao()
	{
		return userDevDao;
	}
	
	public void setUserDevDao(UserDeviceDAO userDevDao)
	{
		this.userDevDao = userDevDao;
	}
	
	
}
