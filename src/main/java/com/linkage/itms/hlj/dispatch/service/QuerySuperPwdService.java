
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.dao.SuperPwdDAO;
import com.linkage.itms.hlj.dispatch.obj.QuerySuperPwdChecker;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-28
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QuerySuperPwdService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(QuerySuperPwdService.class);

	@Override
	public String work(String jsonString)
	{
		QuerySuperPwdChecker checker = new QuerySuperPwdChecker(jsonString);
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		if (false == checker.check())
		{
			logger.warn("超级密码查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("QuerySuperPwdService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String deviceId = "";
		String superPwd = "";
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
		SuperPwdDAO spDao = new SuperPwdDAO();
		superPwd = spDao.querySuperPwd(deviceId);
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
			jo.put("result", superPwd);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}
}
