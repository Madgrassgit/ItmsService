
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.dao.ServiceDoneFailDAO;
import com.linkage.itms.hlj.dispatch.obj.ServiceDoneFailChecker;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-28
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ServiceDoneFail implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(ServiceDoneFail.class);

	@Override
	public String work(String jsonString)
	{
		logger.warn("ServiceDoneFail==>inParam:" + jsonString);
		ServiceDoneFailChecker checker = new ServiceDoneFailChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("终端业务下发失败记录接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("ServiceDoneFail==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String deviceId = "";
		String username = "";
		String openStatus = "";
		String faultReason = "";
		String loid = "";
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 1)
		{
			userMap = qdDao.queryDevByLoid(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 2)
		{
			userMap = qdDao.queryUserByDevSN(checker.getQueryNum());
		}
		else
		{
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1)
		{
			checker.setResult(1001);
			checker.setResultDesc("数据不唯一，请使用devSn查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(3);
			checker.setResultDesc("无设备信息");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		ServiceDoneFailDAO sdDao = new ServiceDoneFailDAO();
		loid = StringUtil.getStringValue(userMap.get(0), "loid", "");
		List<HashMap<String, String>> userMap2 = null;
		userMap2 = sdDao.queryUserByDeviceId(deviceId, loid);
		username = StringUtil.getStringValue(userMap2.get(0), "username", "");
		openStatus = StringUtil.getStringValue(userMap2.get(0), "open_status", "");
		faultReason = sdDao.queryFailResult(deviceId, username);
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("Loid", loid);
			jo.put("FailReason", faultReason);
			jo.put("RegResult", openStatus);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}
}
