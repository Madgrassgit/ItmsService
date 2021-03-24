
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.IsRouteDeviceDao;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.OpenRouteChecker;

/**
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2014-4-9
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OpenrouteResult implements IService
{

	private static Logger logger = LoggerFactory.getLogger(OpenrouteResult.class);

	public String work(String param)
	{
		OpenRouteChecker binder = new OpenRouteChecker(param);
		if (false == binder.check())
		{
			logger.error(
					"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn(
				"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),
						param });
		IsRouteDeviceDao dao = new IsRouteDeviceDao();
		Map<String, String> devData = dao.queryRouteServ(binder.getUserInfoType(),
				binder.getUserInfo());
		if (null == devData || devData.isEmpty())
		{
			logger.warn(
					"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { binder.getCmdId(), binder.getUserInfo()});
			binder.setResult(1002);
			binder.setResultDesc("查无此用户");
		}
		else
		{
			String wanType = StringUtil.getStringValue(devData, "wan_type", "");
			String openResult = StringUtil.getStringValue(devData, "open_status", "");
			if ("2".equals(wanType))
			{
				logger.warn(
						"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]开通路由了",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(0);
				if ("0".equals(openResult))// 未做，等待执行
				{
					binder.setResultDesc("2");
				}
				else if ("1".equals(openResult))
				{
					binder.setResultDesc("0");
				}
				else
				{
					binder.setResultDesc("1");// 失败
				}
			}
			else
			{
				logger.warn(
						"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]没有开通路由",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(0);
				binder.setResultDesc("3");
			}
		}
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(),
				"OpenrouteResult");
		logger.warn(
				"servicename[OpenrouteResult]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
