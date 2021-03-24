
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.WifiDeviceDAO;
import com.linkage.itms.dispatch.obj.WifiInfoChecker;

/**
 * @author Administrator (Ailk No.)
 * @version 1.0
 * @since 2013-11-22
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class WifiInfoService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(WifiInfoService.class);

	@Override
	public String work(String inXml)
	{
		WifiInfoChecker binder = new WifiInfoChecker(inXml);
		if (false == binder.check())
		{
			logger.error(
					"servicename[WifiInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn("servicename[WifiInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(), inXml });
		WifiDeviceDAO wifiDao = new WifiDeviceDAO();
		Map<String, String> userInfoMap = wifiDao.queryWifiInfo(binder.getUserInfoType(), binder.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("servicename[WifiInfoService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { binder.getCmdId(), binder.getUserInfo()});
			binder.setResult(1002);
			binder.setResultDesc("查无此用户");
			return retXml(binder);
		}
		int hasWifi = StringUtil.getIntegerValue((userInfoMap.get("wlan_num")));
		binder.setResult(0);
		binder.setResultDesc("成功");
		int num = 0;
		if (hasWifi > 0) {
			num = 1;
		}
		else {
			num = 0;
		}
		binder.setHasWifi(num);
		return retXml(binder);
	}
	
	private String retXml(WifiInfoChecker binder) {
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(), "WifiInfoService");
		logger.warn("servicename[WifiInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
						new Object[] { binder.getCmdId(), binder.getUserInfo(), returnXml});
		return returnXml;
	}
}
