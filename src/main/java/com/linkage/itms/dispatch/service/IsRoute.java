package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.IsRouteDeviceDao;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.IsRouteCheck;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2014-4-9
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class IsRoute implements IService
{
	private static Logger logger = LoggerFactory.getLogger(IsRoute.class);

	public String work(String param)
	{
		IsRouteCheck binder = new IsRouteCheck(param);
		if(false == binder.check())
		{
			logger.error(
					"servicename[IsRoute]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn(
				"servicename[IsRoute]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),
						param });
		IsRouteDeviceDao dao = new IsRouteDeviceDao();
		Map<String,String> devData = dao.queryRouteInfo(binder.getUserInfoType(), binder.getUserInfo());
		if(null == devData || devData.isEmpty())
		{
			logger.warn(
					"servicename[IsRoute]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { binder.getCmdId(), binder.getUserInfo()});
			binder.setResult(1002);
			binder.setResultDesc("查无此用户");
			binder.setIsRoute("1");
			binder.setNoReason("查无此用户");
		}
		else
		{
			String is_route = StringUtil.getStringValue(devData, "is_route");
			String device_id = StringUtil.getStringValue(devData, "device_id");
			String user_id = StringUtil.getStringValue(devData, "user_id");
			if(StringUtil.IsEmpty(user_id))
			{
				logger.warn(
						"servicename[IsRoute]cmdId[{}]userinfo[{}]用户不存在",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(1002);
				binder.setResultDesc("查无此用户");
				binder.setIsRoute("1");
				binder.setNoReason("1");
			}
			else if(StringUtil.IsEmpty(device_id))
			{
				logger.warn(
						"servicename[IsRoute]cmdId[{}]userinfo[{}]用户没有绑定设备",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(0);
				binder.setResultDesc("用户没有绑定设备");
				binder.setIsRoute("1");
				binder.setNoReason("2");
			}
			if(StringUtil.IsEmpty(is_route))
			{
				logger.warn(
						"servicename[IsRoute]cmdId[{}]userinfo[{}]设备不支持路由开通",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(0);
				binder.setResultDesc("设备不支持开通路由");
				binder.setIsRoute("1");
				binder.setNoReason("3");
			}
			else
			{
				logger.warn(
						"servicename[IsRoute]cmdId[{}]userinfo[{}]成功",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(0);
				binder.setResultDesc("成功");
				binder.setIsRoute("1".equals(is_route) ? "0" : is_route);
				binder.setNoReason("1".equals(is_route) ? "" : "设备不支持开通路由");
			}
		}
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(), "IsRoute");
		logger.warn(
				"servicename[IsRoute]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
